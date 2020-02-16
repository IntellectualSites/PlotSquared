package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.CaptionUtility;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flags.FlagParseException;
import com.github.intellectualsites.plotsquared.plot.flags.GlobalFlagContainer;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.flags.types.IntegerFlag;
import com.github.intellectualsites.plotsquared.plot.flags.types.ListFlag;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.StringComparison;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@CommandDeclaration(command = "flag", aliases = {"f",
    "flag"}, usage = "/plot flag <set|remove|add|list|info> <flag> <value>",
    description = "Set plot flags",
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.NONE,
    permission = "plots.flag")
public class FlagCmd extends SubCommand {

    private boolean checkPermValue(PlotPlayer player, PlotFlag<?, ?> flag, String key, String value) {
        key = key.toLowerCase();
        value = value.toLowerCase();
        String perm = CaptionUtility
            .format(Captions.PERMISSION_SET_FLAG_KEY_VALUE.getTranslated(), key.toLowerCase(), value.toLowerCase());
        if (flag instanceof IntegerFlag && MathMan.isInteger(value)) {
            try {
                int numeric = Integer.parseInt(value);
                perm = perm.substring(0, perm.length() - value.length() - 1);
                if (numeric > 0) {
                    int checkRange =
                        PlotSquared.get().getPlatform().equalsIgnoreCase("bukkit") ? numeric : Settings.Limit.MAX_PLOTS;
                    final boolean result = player.hasPermissionRange(perm, checkRange) >= numeric;
                    if (!result) {
                        MainUtil.sendMessage(player, Captions.NO_PERMISSION, CaptionUtility
                            .format(Captions.PERMISSION_SET_FLAG_KEY_VALUE.getTranslated(), key.toLowerCase(),
                                value.toLowerCase()));
                    }
                    return result;
                }

            } catch (NumberFormatException ignore) {
            }
            // TODO is this required at all?
        /* } else if (flag instanceof BlockTypeListFlag) {
            final BlockTypeListFlag blockListFlag = (BlockTypeListFlag) flag;
            try {
                BlockTypeListFlag parsedFlag = blockListFlag.parse(value);
                for (final BlockType block : parsedFlag.getValue()) {
                    final String permission = CaptionUtility
                        .format(Captions.PERMISSION_SET_FLAG_KEY_VALUE.getTranslated(),
                            key.toLowerCase(), block.toString().toLowerCase());
                    final boolean result = Permissions.hasPermission(player, permission);
                    if (!result) {
                        MainUtil.sendMessage(player, Captions.NO_PERMISSION, CaptionUtility
                            .format(Captions.PERMISSION_SET_FLAG_KEY_VALUE.getTranslated(),
                                key.toLowerCase(), value.toLowerCase()));
                        return false;
                    }
                }
            } catch (final Exception e) {
                return false;
            }
            return true;*/
        } else if (flag instanceof ListFlag) {
            final ListFlag<?, ?> listFlag = (ListFlag<?, ?>) flag;
            try {
                PlotFlag<? extends List<?>, ?> parsedFlag = listFlag.parse(value);
                for (final Object entry : parsedFlag.getValue()) {
                    final String permission = CaptionUtility
                        .format(Captions.PERMISSION_SET_FLAG_KEY_VALUE.getTranslated(), key.toLowerCase(),
                            entry.toString().toLowerCase());
                    final boolean result = Permissions.hasPermission(player, permission);
                    if (!result) {
                        MainUtil.sendMessage(player, Captions.NO_PERMISSION, CaptionUtility
                            .format(Captions.PERMISSION_SET_FLAG_KEY_VALUE.getTranslated(), key.toLowerCase(),
                                value.toLowerCase()));
                        return false;
                    }
                }
            } catch (final Exception e) {
                return false;
            }
            return true;
        }
        final boolean result = Permissions.hasPermission(player, perm);
        if (!result) {
            MainUtil.sendMessage(player, Captions.NO_PERMISSION, CaptionUtility
                .format(Captions.PERMISSION_SET_FLAG_KEY_VALUE.getTranslated(), key.toLowerCase(),
                    value.toLowerCase()));
        }
        return result;
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
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, getUsage());
            return false;
        }
        Location location = player.getLocation();
        Plot plot = location.getPlotAbs();
        if (plot == null) {
            MainUtil.sendMessage(player, Captions.NOT_IN_PLOT);
            return false;
        }
        if (!plot.hasOwner()) {
            sendMessage(player, Captions.PLOT_NOT_CLAIMED);
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, Captions.PERMISSION_SET_FLAG_OTHER)) {
            MainUtil.sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_SET_FLAG_OTHER);
            return false;
        }
        PlotFlag flag = null;
        if (args.length > 1) {
            flag = GlobalFlagContainer.getInstance().getFlagFromString(args[1]);
            if (flag == null) {
                boolean suggested = false;
                try {
                    StringComparison<PlotFlag<?, ?>> stringComparison =
                        new StringComparison<>(args[1], GlobalFlagContainer.getInstance().getFlagMap().values());
                    String best = stringComparison.getBestMatch();
                    if (best != null) {
                        MainUtil.sendMessage(player, Captions.NOT_VALID_FLAG_SUGGESTED, best);
                        suggested = true;
                    }
                } catch (final Exception ignored) { /* Happens sometimes because of mean code */ }
                if (!suggested) {
                    MainUtil.sendMessage(player, Captions.NOT_VALID_FLAG);
                }
                return false;
            }
        }
        switch (args[0].toLowerCase()) {
            case "info": {
                return info(player, args, flag);
            }
            case "set": {
                if (!Permissions.hasPermission(player, Captions.PERMISSION_SET_FLAG)) {
                    MainUtil.sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_SET_FLAG);
                    return false;
                }
                if (args.length < 3) {
                    MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, "/plot flag set <flag> <value>");
                    return false;
                }
                String value = StringMan.join(Arrays.copyOfRange(args, 2, args.length), " ");
                if (!checkPermValue(player, flag, args[1], value)) {
                    return false;
                }
                PlotFlag<?, ?> parsed;
                try {
                    parsed = flag.parse(value);
                } catch (FlagParseException e) {
                    MainUtil.sendMessage(player, "&c" + e.getErrorMessage());
                    return false;
                }
                plot.setFlag(parsed);
                MainUtil.sendMessage(player, Captions.FLAG_ADDED);
                return true;
            }
            case "remove": {
                if (!Permissions.hasPermission(player, Captions.PERMISSION_FLAG_REMOVE)) {
                    MainUtil.sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_FLAG_REMOVE);
                    return false;
                }
                if (args.length != 2 && args.length != 3) {
                    MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, "/plot flag remove <flag> [values]");
                    return false;
                }
                if (!Permissions.hasPermission(player,
                    CaptionUtility.format(Captions.PERMISSION_SET_FLAG_KEY.getTranslated(), args[1].toLowerCase()))) {
                    if (args.length != 3) {
                        MainUtil.sendMessage(player, Captions.NO_PERMISSION, CaptionUtility
                            .format(Captions.PERMISSION_SET_FLAG_KEY.getTranslated(), args[1].toLowerCase()));
                        return false;
                    }
                }
                if (args.length == 3 && flag instanceof ListFlag) {
                    String value = StringMan.join(Arrays.copyOfRange(args, 2, args.length), " ");
                    final ListFlag<?, ?> listFlag = (ListFlag<?, ?>) flag;
                    final List<?> list = plot.getFlag((Class<? extends ListFlag<?, ?>>) listFlag.getClass());
                    final PlotFlag<? extends List<?>, ?> parsedFlag;
                    try {
                        parsedFlag = listFlag.parse(value);
                    } catch (FlagParseException e) {
                        MainUtil.sendMessage(player, "&c" + e.getErrorMessage());
                        return false;
                    }
                    if (parsedFlag.getValue().isEmpty()) {
                        return !MainUtil.sendMessage(player, Captions.FLAG_NOT_REMOVED);
                    }
                    if (list.removeAll(parsedFlag.getValue())) {
                        if (list.isEmpty()) {
                            if (plot.removeFlag(flag)) {
                                return MainUtil.sendMessage(player, Captions.FLAG_REMOVED);
                            } else {
                                return !MainUtil.sendMessage(player, Captions.FLAG_NOT_REMOVED);
                            }
                        } else {
                            MainUtil.sendMessage(player, Captions.FLAG_REMOVED);
                        }
                    } else {
                        return !MainUtil.sendMessage(player, Captions.FLAG_NOT_REMOVED);
                    }
                    // TODO reimplement somewhere else: DBFunc.setFlags(plot, plot.getFlags());
                    return true;
                } else {
                    boolean result = plot.removeFlag(flag);
                    if (!result) {
                        MainUtil.sendMessage(player, Captions.FLAG_NOT_REMOVED);
                        return false;
                    }
                }
                /* TODO reimplement, maybe handle it with events?
                if (flag == Flags.TIME) {
                    player.setTime(Long.MAX_VALUE);
                } else if (flag == Flags.WEATHER) {
                    player.setWeather(PlotWeather.RESET);
                }*/
                MainUtil.sendMessage(player, Captions.FLAG_REMOVED);
                return true;
            }
            case "add":
                if (!Permissions.hasPermission(player, Captions.PERMISSION_FLAG_ADD)) {
                    MainUtil.sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_FLAG_ADD);
                    return false;
                }
                if (args.length < 3) {
                    MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, "/plot flag add <flag> <values>");
                    return false;
                }
                for (String entry : args[2].split(",")) {
                    if (!checkPermValue(player, flag, args[1], entry)) {
                        return false;
                    }
                }
                String value = StringMan.join(Arrays.copyOfRange(args, 2, args.length), " ");
                PlotFlag<?, ?> parsed;
                try {
                    parsed = flag.parse(value);
                } catch (FlagParseException e) {
                    MainUtil.sendMessage(player, "&c" + flag.getFlagDescription().getTranslated());
                    return false;
                }
                boolean result = plot.setFlag(flag.merge(parsed.getValue()));
                if (!result) {
                    MainUtil.sendMessage(player, Captions.FLAG_NOT_ADDED);
                    return false;
                }
                MainUtil.sendMessage(player, Captions.FLAG_ADDED);
                return true;
            case "list":
                if (!Permissions.hasPermission(player, Captions.PERMISSION_FLAG_LIST)) {
                    MainUtil.sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_FLAG_LIST);
                    return false;
                }
                if (args.length > 1) {
                    MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, "/plot flag list");
                    return false;
                }
                final Map<String, ArrayList<String>> flags = new HashMap<>();
                for (PlotFlag<?, ?> plotFlag : GlobalFlagContainer.getInstance().getRecognizedPlotFlags()) {
                    final String category = plotFlag.getFlagCategory().getTranslated();
                    final Collection<String> flagList = flags.computeIfAbsent(category, k -> new ArrayList<>());
                    flagList.add(plotFlag.getName());
                }

                final Iterator<Map.Entry<String, ArrayList<String>>> iterator = flags.entrySet().iterator();
                final StringJoiner message = new StringJoiner("\n");
                while (iterator.hasNext()) {
                    final Map.Entry<String, ArrayList<String>> flagsEntry = iterator.next();
                    final List<String> flagNames = flagsEntry.getValue();
                    Collections.sort(flagNames);
                    message.add(String.format(Captions.FLAG_LIST_ENTRY.formatted(), flagsEntry.getKey(),
                        StringMan.join(flagNames, ", ")));
                }
                MainUtil.sendMessage(player, message.toString());
                return true;
        }
        MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, getUsage());
        return false;
    }

    private boolean info(PlotPlayer player, String[] args, PlotFlag<?, ?> flag) {
        if (!Permissions.hasPermission(player, Captions.PERMISSION_SET_FLAG)) {
            MainUtil.sendMessage(player, Captions.NO_PERMISSION, "plots.flag.info");
            return false;
        }
        if (args.length != 2) {
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, "/plot flag info <flag>");
            return false;
        }
        // flag key
        MainUtil.sendMessage(player, Captions.FLAG_KEY, flag.getName());
        // flag type
        MainUtil.sendMessage(player, Captions.FLAG_TYPE, flag.getClass().getSimpleName());
        // Flag type description
        MainUtil.sendMessage(player, Captions.FLAG_DESC, flag.getFlagDescription().getTranslated());
        return true;
    }
}
