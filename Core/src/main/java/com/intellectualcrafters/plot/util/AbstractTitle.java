package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

public abstract class AbstractTitle {
    public static AbstractTitle TITLE_CLASS;

    public static void sendTitle(PlotPlayer player, String head, String sub) {
        if (player instanceof ConsolePlayer) {
            return;
        }
        if (TITLE_CLASS != null && !player.getAttribute("disabletitles")) {
            TITLE_CLASS.sendTitle(player, head, sub, 1, 2, 1);
        }
    }

    public abstract void sendTitle(PlotPlayer player, String head, String sub, int in, int delay,
        int out);
}
