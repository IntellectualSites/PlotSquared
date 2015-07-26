package com.plotsquared.bukkit.titles;

import org.bukkit.ChatColor;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.plotsquared.bukkit.object.BukkitPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

public class HackTitle extends AbstractTitle {
    @Override
    public void sendTitle(final PlotPlayer player, final String head, final String sub, final ChatColor head_color, final ChatColor sub_color, int in, int delay, int out) {
        try {
            final HackTitleManager title = new HackTitleManager(head, sub, in, delay, out);
            title.setTitleColor(head_color);
            title.setSubtitleColor(sub_color);
            title.send(((BukkitPlayer) player).player);
        } catch (final Throwable e) {
            PS.log("&cYour server version does not support titles!");
            Settings.TITLES = false;
            AbstractTitle.TITLE_CLASS = null;
        }
    }
}
