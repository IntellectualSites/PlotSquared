package com.plotsquared.bukkit.titles;

import com.intellectualcrafters.plot.object.PlotPlayer;
import org.bukkit.ChatColor;

public abstract class AbstractTitle {
    public static AbstractTitle TITLE_CLASS;
    
    public static void sendTitle(PlotPlayer player, String head, String sub, ChatColor head_color, ChatColor sub_color) {
        if (TITLE_CLASS != null && !player.getAttribute("disabletitles")) {
            TITLE_CLASS.sendTitle(player, head, sub, head_color, sub_color, 1, 2, 1);
        }
    }

    public abstract void sendTitle(PlotPlayer player, String head, String sub, ChatColor head_color, ChatColor sub_color, int in, int delay, int out);
}
