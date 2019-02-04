package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.object.CmdInstance;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;

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
        if (commandStr != null)
            MainUtil.sendMessage(player, C.REQUIRES_CONFIRM, commandStr);
        TaskManager.runTaskLater(() -> {
            CmdInstance cmd = new CmdInstance(runnable);
            player.setMeta("cmdConfirm", cmd);
        }, 1);
    }
}
