package com.plotsquared.bukkit.titles;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.plotsquared.bukkit.object.BukkitPlayer;

public class DefaultTitle extends AbstractTitle {

    @Override
    public void sendTitle(PlotPlayer player, String head, String sub, int in, int delay, int out) {
        try {
            DefaultTitleManager title = new DefaultTitleManager(head, sub, in, delay, out);
            title.send(((BukkitPlayer) player).player);
        } catch (Throwable e) {
            AbstractTitle.TITLE_CLASS = new DefaultTitle_183();
            AbstractTitle.TITLE_CLASS.sendTitle(player, head, sub, in, delay, out);
        }
    }
}
