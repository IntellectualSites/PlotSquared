package com.intellectualcrafters.plot.util;

import java.util.HashSet;

import com.intellectualcrafters.plot.PlotMain;

public class TaskManager {
    
    public static HashSet<String> TELEPORT_QUEUE = new HashSet<>();
    
    public static void runTask(final Runnable r) {
        PlotMain.getMain().getServer().getScheduler().runTaskAsynchronously(PlotMain.getMain(), r);
    }
    
    public static void runTaskLater(final Runnable r, int delay) {
        if (r == null) {
            return;
        }
        PlotMain.getMain().getServer().getScheduler().runTaskLater(PlotMain.getMain(), r, delay);
    }
}
