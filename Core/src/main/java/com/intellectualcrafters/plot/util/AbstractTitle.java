package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

public abstract class AbstractTitle {
    public static AbstractTitle TITLE_CLASS;
    
    public static void sendTitle(final PlotPlayer player, final String head, final String sub) {
        if (ConsolePlayer.isConsole(player)) {
            return;
        }
        if (TITLE_CLASS != null && !player.getAttribute("disabletitles")) {
            TITLE_CLASS.sendTitle(player, head, sub, 1, 2, 1);
        }
    }
    
    public abstract void sendTitle(final PlotPlayer player, final String head, final String sub, final int in, final int delay, final int out);
}
