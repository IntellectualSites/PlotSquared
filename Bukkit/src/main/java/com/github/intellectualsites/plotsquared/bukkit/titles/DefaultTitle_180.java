package com.github.intellectualsites.plotsquared.bukkit.titles;

import com.github.intellectualsites.plotsquared.bukkit.object.BukkitPlayer;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.AbstractTitle;

public class DefaultTitle_180 extends AbstractTitle {

    @Override
    public void sendTitle(PlotPlayer player, String head, String sub, int in, int delay, int out) {
        try {
            DefaultTitleManager title = new DefaultTitleManager(head, sub, in, delay, out);
            title.send(((BukkitPlayer) player).player);
        } catch (Exception ignored) {
            AbstractTitle.TITLE_CLASS = new DefaultTitle_19();
            AbstractTitle.TITLE_CLASS.sendTitle(player, head, sub, in, delay, out);
        }
    }
}
