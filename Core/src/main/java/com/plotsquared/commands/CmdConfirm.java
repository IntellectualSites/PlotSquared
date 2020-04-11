package com.plotsquared.commands;

import com.plotsquared.config.Captions;
import com.plotsquared.player.PlotPlayer;
import com.plotsquared.util.MainUtil;
import com.plotsquared.util.tasks.TaskManager;

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
