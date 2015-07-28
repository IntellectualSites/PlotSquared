package com.plotsquared.bukkit.titles;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.bukkit.object.BukkitPlayer;
import org.bukkit.ChatColor;

public class DefaultTitle_183 extends AbstractTitle {
    @Override
    public void sendTitle(final PlotPlayer player, final String head, final String sub, int in, int delay, int out) {
        try {
            final DefaultTitleManager_183 title = new DefaultTitleManager_183(head, sub, in, delay, out);
            title.send(((BukkitPlayer) player).player);
        } catch (final Throwable e) {
            AbstractTitle.TITLE_CLASS = new HackTitle();
            AbstractTitle.TITLE_CLASS.sendTitle(player, head, sub, in, delay, out);
        }
    }
}
