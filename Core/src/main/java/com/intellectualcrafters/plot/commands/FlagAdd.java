package com.intellectualcrafters.plot.commands;

import com.google.common.base.Optional;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.ListFlag;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.*;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.Arrays;
import java.util.Collection;

@CommandDeclaration(command = "set",
        permission = "plots.flag.add",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        description = "Add a plot flag",
        usage = "/plot flag add <flag> <values>")
public class FlagAdd extends FlagCommand {

    public FlagAdd(Command parent, boolean isStatic) {
        super(parent, isStatic);
    }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length < 2) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, getUsage());
            return false;
        }
        Location loc = player.getLocation();
        Plot plot = loc.getPlotAbs();
        Flag<?> flag = getFlag(player, plot, args[0]);
        for (String entry : args[1].split(",")) {
            if (!Permissions.hasPermission(player, C.PERMISSION_SET_FLAG_KEY_VALUE.f(args[0].toLowerCase(), entry))) {
                MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_SET_FLAG_KEY_VALUE.f(args[0].toLowerCase(), entry));
                return false;
            }
        }
        String value = StringMan.join(Arrays.copyOfRange(args, 1, args.length), " ");
        Object parsed = flag.parseValue(value);
        if (parsed == null) {
            MainUtil.sendMessage(player, "&c" + flag.getValueDescription());
            return false;
        }
        Object val = parsed;
        if (flag instanceof ListFlag) {
            Optional<? extends Collection> flag1 = plot.getFlag((Flag<? extends Collection<?>>) flag);
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
    }
}
