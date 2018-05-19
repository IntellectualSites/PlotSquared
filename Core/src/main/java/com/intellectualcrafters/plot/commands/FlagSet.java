package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.*;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.Arrays;

@CommandDeclaration(command = "set",
        permission = "plots.set.flag",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        description = "Set a plot flag",
        usage = "/plot flag set <flag> <value>")
public class FlagSet extends FlagCommand {

    public FlagSet(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length != 2) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, getUsage());
            return false;
        }
        Location loc = player.getLocation();
        Plot plot = loc.getPlotAbs();
        Flag<?> flag = getFlag(player, plot, args[0]);
        String value = StringMan.join(Arrays.copyOfRange(args, 1, args.length), " ");
        if (!Permissions.hasPermission(player, C.PERMISSION_SET_FLAG_KEY_VALUE.f(args[0].toLowerCase(), value.toLowerCase()))) {
            MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_SET_FLAG_KEY_VALUE.f(args[0].toLowerCase(), value.toLowerCase()));
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
}
