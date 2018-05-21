package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringComparison;
import com.plotsquared.general.commands.Command;

import java.util.ArrayList;

public abstract class FlagCommand extends SubCommand {

    public FlagCommand() { super(MainCommand.getInstance(), true); }

    public FlagCommand(Command parent, boolean isStatic) { super(parent, isStatic); }

    public Flag<?> getFlag(PlotPlayer player, Plot plot, String key) {
        /*
         *  plot flag set fly true
         *  plot flag remove fly
         *  plot flag remove use 1,3
         *  plot flag add use 2,4
         *  plot flag list
         */
        if (plot == null) {
            MainUtil.sendMessage(player, C.NOT_IN_PLOT);
            return null;
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(player, C.PLOT_NOT_CLAIMED);
            return null;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, C.PERMISSION_SET_FLAG_OTHER)) {
            MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_SET_FLAG_OTHER);
            return null;
        }
        Flag<?> flag = FlagManager.getFlag(key);
        if (flag == null || flag.isReserved()) {
            boolean suggested = false;
            try {
                StringComparison<Flag<?>> stringComparison = new StringComparison<>(key, Flags.getFlags());
                String best = stringComparison.getBestMatch();
                if (best != null) {
                    MainUtil.sendMessage(player, C.NOT_VALID_FLAG_SUGGESTED, best);
                    suggested = true;
                }
            } catch (final Exception ignored) { /* Happens sometimes because of mean code */ }
            if (!suggested) {
                MainUtil.sendMessage(player, C.NOT_VALID_FLAG);
            }
            return null;
        }
        return flag;
    }
}
