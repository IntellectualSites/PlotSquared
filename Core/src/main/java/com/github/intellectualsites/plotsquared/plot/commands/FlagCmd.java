package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.flag.*;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.*;
import com.google.common.base.Optional;

import java.util.*;

@CommandDeclaration(command = "setflag", aliases = {"f", "flag", "setf", "setflag"},
    usage = "/plot flag <set|remove|add|list|info> <flag> <value>", description = "Set plot flags",
    category = CommandCategory.SETTINGS, requiredType = RequiredType.NONE,
    permission = "plots.flag") public class FlagCmd extends SubCommand {

    private boolean checkPermValue(PlotPlayer player, Flag flag, String key, String value) {
        key = key.toLowerCase();
        value = value.toLowerCase();
        String perm = C.PERMISSION_SET_FLAG_KEY_VALUE.f(key.toLowerCase(), value.toLowerCase());
        if (flag instanceof IntegerFlag && MathMan.isInteger(value)) {
            try {
                int numeric = Integer.parseInt(value);
                perm = perm.substring(0, perm.length() - value.length() - 1);
                if (numeric > 0) {
                    int checkRange = PlotSquared.get().getPlatform().equalsIgnoreCase("bukkit") ?
                        numeric :
                        Settings.Limit.MAX_PLOTS;
                    return player.hasPermissionRange(perm, checkRange) >= numeric;
                }

            } catch (NumberFormatException ignore) {
            }
        }
        return Permissions.hasPermission(player, perm);
    }

    @Override public boolean onCommand(PlotPlayer player, String[] args) {

        /*
         *  plot flag set fly true
         *  plot flag remove fly
         *  plot flag remove use 1,3
         *  plot flag add use 2,4
         *  plot flag list
         */
        if (args.length == 0) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag <set|remove|add|list|info>");
            return false;
        }
        Location loc = player.getLocation();
        Plot plot = loc.getPlotAbs();
        if (plot == null) {
            MainUtil.sendMessage(player, C.NOT_IN_PLOT);
            return false;
        }
        if (!plot.hasOwner()) {
            sendMessage(player, C.PLOT_NOT_CLAIMED);
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, C.PERMISSION_SET_FLAG_OTHER)) {
            MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_SET_FLAG_OTHER);
            return false;
        }
        Flag<?> flag = null;
        if (args.length > 1) {
            flag = FlagManager.getFlag(args[1]);
            if (flag == null || flag.isReserved()) {
                boolean suggested = false;
                try {
                    StringComparison<Flag<?>> stringComparison =
                        new StringComparison<>(args[1], Flags.getFlags());
                    String best = stringComparison.getBestMatch();
                    if (best != null) {
                        MainUtil.sendMessage(player, C.NOT_VALID_FLAG_SUGGESTED, best);
                        suggested = true;
                    }
                } catch (final Exception ignored) { /* Happens sometimes because of mean code */ }
                if (!suggested) {
                    MainUtil.sendMessage(player, C.NOT_VALID_FLAG);
                }
                return false;
            }
        }
        switch (args[0].toLowerCase()) {
            case "info": {
                if (!Permissions.hasPermission(player, C.PERMISSION_SET_FLAG)) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.flag.info");
                    return false;
                }
                if (args.length != 2) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag info <flag>");
                    return false;
                }
                // flag key
                MainUtil.sendMessage(player, C.FLAG_KEY, flag.getName());
                // flag type
                MainUtil.sendMessage(player, C.FLAG_TYPE, flag.getClass().getSimpleName());
                // Flag type description
                MainUtil.sendMessage(player, C.FLAG_DESC, flag.getValueDescription());
                return true;
            }
            case "set": {
                if (!Permissions.hasPermission(player, C.PERMISSION_SET_FLAG)) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_SET_FLAG);
                    return false;
                }
                if (args.length < 3) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag set <flag> <value>");
                    return false;
                }
                String value = StringMan.join(Arrays.copyOfRange(args, 2, args.length), " ");
                if (!checkPermValue(player, flag, args[1], value)) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_SET_FLAG_KEY_VALUE
                        .f(args[1].toLowerCase(), value.toLowerCase()));
                    return false;
                }
                Object parsed = flag.parseValue(value);
                if (parsed == null) {
                    MainUtil.sendMessage(player, "&c" + flag.getValueDescription());
                    return false;
                }
                boolean result = plot.setFlag(flag, parsed);
                if (!result) {
                    MainUtil.sendMessage(player, C.FLAG_NOT_ADDED);
                    return false;
                }
                MainUtil.sendMessage(player, C.FLAG_ADDED);
                return true;
            }
            case "remove": {
                if (!Permissions.hasPermission(player, C.PERMISSION_FLAG_REMOVE)) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_FLAG_REMOVE);
                    return false;
                }
                if (args.length != 2 && args.length != 3) {
                    MainUtil
                        .sendMessage(player, C.COMMAND_SYNTAX, "/plot flag remove <flag> [values]");
                    return false;
                }
                if (!Permissions
                    .hasPermission(player, C.PERMISSION_SET_FLAG_KEY.f(args[1].toLowerCase()))) {
                    if (args.length != 3) {
                        MainUtil.sendMessage(player, C.NO_PERMISSION,
                            C.PERMISSION_SET_FLAG_KEY.f(args[1].toLowerCase()));
                        return false;
                    }
                    for (String entry : args[2].split(",")) {
                        if (!checkPermValue(player, flag, args[1], entry)) {
                            MainUtil.sendMessage(player, C.NO_PERMISSION,
                                C.PERMISSION_SET_FLAG_KEY_VALUE.f(args[1].toLowerCase(), entry));
                            return false;
                        }
                    }
                }
                if (args.length == 3 && flag instanceof ListFlag) {
                    String value = StringMan.join(Arrays.copyOfRange(args, 2, args.length), " ");
                    Optional<? extends Collection> flag1 =
                        plot.getFlag((Flag<? extends Collection<?>>) flag);
                    if (flag1.isPresent()) {
                        boolean o = flag1.get().removeAll((Collection) flag.parseValue(value));
                        if (o) {
                            MainUtil.sendMessage(player, C.FLAG_REMOVED);
                        } else {
                            MainUtil.sendMessage(player, C.FLAG_NOT_REMOVED);
                            return false;
                        }
                    }
                    DBFunc.setFlags(plot, plot.getFlags());
                    return true;
                } else {
                    boolean result = plot.removeFlag(flag);
                    if (!result) {
                        MainUtil.sendMessage(player, C.FLAG_NOT_REMOVED);
                        return false;
                    }
                }
                if (flag == Flags.TIME) {
                    player.setTime(Long.MAX_VALUE);
                } else if (flag == Flags.WEATHER) {
                    player.setWeather(PlotWeather.RESET);
                }
                MainUtil.sendMessage(player, C.FLAG_REMOVED);
                return true;
            }
            case "add":
                if (!Permissions.hasPermission(player, C.PERMISSION_FLAG_ADD)) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_FLAG_ADD);
                    return false;
                }
                if (args.length < 3) {
                    MainUtil
                        .sendMessage(player, C.COMMAND_SYNTAX, "/plot flag add <flag> <values>");
                    return false;
                }
                for (String entry : args[2].split(",")) {
                    if (!checkPermValue(player, flag, args[1], entry)) {
                        MainUtil.sendMessage(player, C.NO_PERMISSION,
                            C.PERMISSION_SET_FLAG_KEY_VALUE.f(args[1].toLowerCase(), entry));
                        return false;
                    }
                }
                String value = StringMan.join(Arrays.copyOfRange(args, 2, args.length), " ");
                Object parsed = flag.parseValue(value);
                if (parsed == null) {
                    MainUtil.sendMessage(player, "&c" + flag.getValueDescription());
                    return false;
                }
                Object val = parsed;
                if (flag instanceof ListFlag) {
                    Optional<? extends Collection> flag1 =
                        plot.getFlag((Flag<? extends Collection<?>>) flag);
                    if (flag1.isPresent()) {
                        boolean o = flag1.get().addAll((Collection) parsed);
                        if (o) {
                            MainUtil.sendMessage(player, C.FLAG_ADDED);
                            val = flag1.get();
                        } else {
                            MainUtil.sendMessage(player, C.FLAG_NOT_ADDED);
                            return false;
                        }
                    }
                }
                boolean result = plot.setFlag(flag, val);
                if (!result) {
                    MainUtil.sendMessage(player, C.FLAG_NOT_ADDED);
                    return false;
                }
                MainUtil.sendMessage(player, C.FLAG_ADDED);
                return true;
            case "list":
                if (!Permissions.hasPermission(player, C.PERMISSION_FLAG_LIST)) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_FLAG_LIST);
                    return false;
                }
                if (args.length > 1) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag list");
                    return false;
                }
                HashMap<String, ArrayList<String>> flags = new HashMap<>();
                for (Flag<?> flag1 : Flags.getFlags()) {
                    String type = flag1.getClass().getSimpleName();
                    if (!flags.containsKey(type)) {
                        flags.put(type, new ArrayList<String>());
                    }
                    flags.get(type).add(flag1.getName());
                }
                String message = "";
                String prefix = "";
                for (Map.Entry<String, ArrayList<String>> entry : flags.entrySet()) {
                    String category = entry.getKey();
                    List<String> flagNames = entry.getValue();
                    Collections.sort(flagNames);
                    message += prefix + "&6" + category + ": &7" + StringMan.join(flagNames, ", ");
                    prefix = "\n";
                }
                MainUtil.sendMessage(player, message);
                return true;
        }
        MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag <set|remove|add|list|info>");
        return false;
    }
}
