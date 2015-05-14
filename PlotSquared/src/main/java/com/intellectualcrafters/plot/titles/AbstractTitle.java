package com.intellectualcrafters.plot.titles;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.object.PlotPlayer;

public abstract class AbstractTitle {
    public static AbstractTitle TITLE_CLASS;

    public abstract void sendTitle(PlotPlayer player, String head, String sub, ChatColor head_color, ChatColor sub_color);
}
