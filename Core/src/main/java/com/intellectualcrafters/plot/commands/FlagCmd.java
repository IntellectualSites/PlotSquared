package com.intellectualcrafters.plot.commands;

import com.google.common.base.Optional;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.flag.ListFlag;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringMan;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@CommandDeclaration(
        command = "setflag",
        aliases = {"f", "flag", "setf", "setflag"},
        usage = "/plot flag <set|remove|add|list|info> <flag> <value>",
        description = "Set plot flags",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.PLAYER,
        permission = "plots.flag")
public class FlagCmd extends SubCommand {

    @Override
    public String getUsage() {
        return super.getUsage().replaceAll("<flag>", StringMan.join(Flags.getFlags(), "|"));
    }

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {

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
        if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, "plots.set.flag.other")) {
            MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.set.flag.other");
            return false;
        }
        if (args.length > 1 && FlagManager.isReserved(FlagManager.getFlag(args[1]))) {
            MainUtil.sendMessage(player, C.NOT_VALID_FLAG);
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "info": {
                if (!Permissions.hasPermission(player, "plots.set.flag")) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.flag.info");
                    return false;
                }
                if (args.length != 2) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag info <flag>");
                    return false;
                }
                Flag<?> flag = FlagManager.getFlag(args[1]);
                if (flag == null) {
                    MainUtil.sendMessage(player, C.NOT_VALID_FLAG);
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
                if (!Permissions.hasPermission(player, "plots.set.flag")) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.set.flag");
                    return false;
                }
                if (args.length < 3) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag set <flag> <value>");
                    return false;
                }
                Flag<?> af = FlagManager.getFlag(args[1].toLowerCase());
                if (af == null) {
                    MainUtil.sendMessage(player, C.NOT_VALID_FLAG);
                    return false;
                }
                String value = StringMan.join(Arrays.copyOfRange(args, 2, args.length), " ");
                if (!Permissions.hasPermission(player, "plots.set.flag." + args[1].toLowerCase() + "." + value.toLowerCase())) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.set.flag." + args[1].toLowerCase() + "." + value.toLowerCase());
                    return false;
                }
                Object parsed = af.parseValue(value);
                if (parsed == null) {
                    MainUtil.sendMessage(player, "&c" + af.getValueDescription());
                    return false;
                }
                boolean result = plot.setFlag(af, parsed);
                if (!result) {
                    MainUtil.sendMessage(player, C.FLAG_NOT_ADDED);
                    return false;
                }
                MainUtil.sendMessage(player, C.FLAG_ADDED);
                return true;
            }
            case "remove": {
                if (!Permissions.hasPermission(player, "plots.flagValue.remove")) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.flagValue.remove");
                    return false;
                }
                if (args.length != 2 && args.length != 3) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flagValue remove <flagValue> [values]");
                    return false;
                }
                Flag<?> flag1 = FlagManager.getFlag(args[1].toLowerCase());
                if (flag1 == null) {
                    MainUtil.sendMessage(player, C.NOT_VALID_FLAG);
                    return false;
                }
                Optional<?> flagValue = plot.getFlag(flag1);
                if (!Permissions.hasPermission(player, "plots.set.flagValue." + args[1].toLowerCase())) {
                    if (args.length != 2) {
                        MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.set.flagValue." + args[1].toLowerCase());
                        return false;
                    }
                    for (String entry : args[2].split(",")) {
                        if (!Permissions.hasPermission(player, "plots.set.flagValue." + args[1].toLowerCase() + "." + entry)) {
                            MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.set.flagValue." + args[1].toLowerCase() + "." + entry);
                            return false;
                        }
                    }
                }
                if (flagValue.isPresent()) {
                    MainUtil.sendMessage(player, C.FLAG_NOT_IN_PLOT);
                    return false;
                }
                if (args.length == 3 && flag1 instanceof ListFlag) {
                    String value = StringMan.join(Arrays.copyOfRange(args, 2, args.length), " ");
                    boolean listFlag = ((Collection) plot.getFlags().get(flag1)).remove(flag1.parseValue(value));
                    DBFunc.setFlags(plot, plot.getFlags());
                } else {
                    boolean result = plot.removeFlag(flag1);
                    if (!result) {
                        MainUtil.sendMessage(player, C.FLAG_NOT_REMOVED);
                        return false;
                    }
                }
                MainUtil.sendMessage(player, C.FLAG_REMOVED);
                return true;
            }
            case "add":
                if (!Permissions.hasPermission(player, "plots.flag.add")) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.flag.add");
                    return false;
                }
                if (args.length < 3) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag add <flag> <values>");
                    return false;
                }
                Flag af = FlagManager.getFlag(args[1].toLowerCase());
                if (af == null) {
                    MainUtil.sendMessage(player, C.NOT_VALID_FLAG);
                    return false;
                }
                for (String entry : args[2].split(",")) {
                    if (!Permissions.hasPermission(player, "plots.set.flag." + args[1].toLowerCase() + "." + entry)) {
                        MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.set.flag." + args[1].toLowerCase() + "." + entry);
                        return false;
                    }
                }
                String value = StringMan.join(Arrays.copyOfRange(args, 2, args.length), " ");
                Object parsed = af.parseValue(value);
                if (parsed == null) {
                    MainUtil.sendMessage(player, "&c" + af.getValueDescription());
                    return false;
                }
                Optional<?> flag = plot.getFlag(af);
                if (flag.isPresent()) {
                    if (af instanceof ListFlag) {
                        ((Collection) flag.get()).addAll((Collection) parsed);
                    }
                }
                boolean result = FlagManager.addPlotFlag(plot, af, parsed);
                if (!result) {
                    MainUtil.sendMessage(player, C.FLAG_NOT_ADDED);
                    return false;
                }
                MainUtil.sendMessage(player, C.FLAG_ADDED);
                return true;
            case "list":
                if (!Permissions.hasPermission(player, "plots.flag.list")) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.flag.list");
                    return false;
                }
                if (args.length != 1) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag list");
                    return false;
                }
                HashMap<String, ArrayList<String>> flags = new HashMap<>();
                for (Flag flag1 : Flags.getFlags()) {
                    String type = flag1.getClass().getSimpleName().replaceAll("Value", "");
                    if (!flags.containsKey(type)) {
                        flags.put(type, new ArrayList<String>());
                    }
                    //todo flags.get(type).add(flag1.getKey());
                }
                String message = "";
                String prefix = "";
                for (Map.Entry<String, ArrayList<String>> stringArrayListEntry : flags.entrySet()) {
                    message += prefix + "&6" + stringArrayListEntry.getKey() + ": &7" + StringMan.join(stringArrayListEntry.getValue(), ", ");
                    prefix = "\n";
                }
                MainUtil.sendMessage(player, message);
                return true;
        }
        MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot flag <set|remove|add|list|info>");
        return false;
    }
}
