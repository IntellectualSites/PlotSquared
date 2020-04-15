package com.plotsquared.core.command;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.task.TaskManager;

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
