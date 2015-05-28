package com.intellectualcrafters.plot.titles;

import org.bukkit.ChatColor;

import com.intellectualcrafters.plot.object.BukkitPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;

public class DefaultTitle extends AbstractTitle {
    @Override
    public void sendTitle(final PlotPlayer player, final String head, final String sub, final ChatColor head_color, final ChatColor sub_color, int in, int delay, int out) {
        try {
            final DefaultTitleManager title = new DefaultTitleManager(head, sub, in, delay, out);
            title.setTitleColor(head_color);
            title.setSubtitleColor(sub_color);
            title.send(((BukkitPlayer) player).player);
        } catch (final Throwable e) {
            AbstractTitle.TITLE_CLASS = new DefaultTitle_183();
            AbstractTitle.TITLE_CLASS.sendTitle(player, head, sub, head_color, sub_color, in, delay, out);
        }
    }
}
