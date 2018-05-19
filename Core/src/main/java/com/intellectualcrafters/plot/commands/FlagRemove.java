package com.intellectualcrafters.plot.commands;

import com.google.common.base.Optional;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.flag.ListFlag;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.*;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.Arrays;
import java.util.Collection;

@CommandDeclaration(command = "remove",
        permission = "plots.flag.remove",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        description = "Remove a plot flag",
        usage = "/plot flag remove <flag> [values]")
public class FlagRemove extends FlagCommand {

    public FlagRemove(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length == 0) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, getUsage());
            return false;
        }
        Location loc = player.getLocation();
        Plot plot = loc.getPlotAbs();
        Flag<?> flag = getFlag(player, plot, args[0]);
        if (!Permissions.hasPermission(player, C.PERMISSION_SET_FLAG_KEY.f(args[0].toLowerCase()))) {
            if (args.length != 3) {
                MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_SET_FLAG_KEY.f(args[0].toLowerCase()));
                return false;
            }
            for (String entry : args[1].split(",")) {
                if (!Permissions.hasPermission(player, C.PERMISSION_SET_FLAG_KEY_VALUE.f(args[0].toLowerCase(), entry))) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_SET_FLAG_KEY_VALUE.f(args[0].toLowerCase(), entry));
                    return false;
                }
            }
        }
        if (args.length == 3 && flag instanceof ListFlag) {
            String value = StringMan.join(Arrays.copyOfRange(args, 2, args.length), " ");
            Optional<? extends Collection> flag1 = plot.getFlag((Flag<? extends Collection<?>>) flag);
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
        if(flag == Flags.TIME) {
            player.setTime(Long.MAX_VALUE);
        } else if(flag == Flags.WEATHER) {
            player.setWeather(PlotWeather.RESET);
        }
        MainUtil.sendMessage(player, C.FLAG_REMOVED);
        return true;
    }
}