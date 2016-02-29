package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.CmdInstance;
import com.intellectualcrafters.plot.object.PlotPlayer;

public class CmdConfirm {
    public static CmdInstance getPending(final PlotPlayer player) {
        return player.<CmdInstance>getMeta("cmdConfirm");
    }
    
    public static void removePending(final PlotPlayer player) {
        player.deleteMeta("cmdConfirm");
    }
    
    public static void addPending(final PlotPlayer player, final String commandStr, final Runnable runnable) {
        removePending(player);
        MainUtil.sendMessage(player, C.REQUIRES_CONFIRM, commandStr);
        TaskManager.runTaskLater(new Runnable() {
            @Override
            public void run() {
                final CmdInstance cmd = new CmdInstance(runnable);
                player.setMeta("cmdConfirm", cmd);
            }
        }, 1);
    }
}
