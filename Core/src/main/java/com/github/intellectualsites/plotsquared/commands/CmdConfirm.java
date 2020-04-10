package com.github.intellectualsites.plotsquared.commands;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.commands.CmdInstance;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;
import com.github.intellectualsites.plotsquared.util.MainUtil;
import com.github.intellectualsites.plotsquared.util.tasks.TaskManager;

public class CmdConfirm {

    public static CmdInstance getPending(PlotPlayer player) {
        return player.getMeta("cmdConfirm");
    }

    public static void removePending(PlotPlayer player) {
        player.deleteMeta("cmdConfirm");
    }

    public static void addPending(final PlotPlayer player, String commandStr,
        final Runnable runnable) {
        removePending(player);
        if (commandStr != null) {
            MainUtil.sendMessage(player, Captions.REQUIRES_CONFIRM, commandStr);
        }
        TaskManager.runTaskLater(new Runnable() {
            @Override public void run() {
                CmdInstance cmd = new CmdInstance(runnable);
                player.setMeta("cmdConfirm", cmd);
            }
        }, 1);
    }
}
