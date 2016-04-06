package com.plotsquared.bukkit.titles;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.plotsquared.bukkit.object.BukkitPlayer;

public class DefaultTitle_183 extends AbstractTitle {

    @Override
    public void sendTitle(PlotPlayer player, String head, String sub, int in, int delay, int out) {
        try {
            DefaultTitleManager_183 title = new DefaultTitleManager_183(head, sub, in, delay, out);
            title.send(((BukkitPlayer) player).player);
        } catch (Exception e) {
            AbstractTitle.TITLE_CLASS = new HackTitle();
            AbstractTitle.TITLE_CLASS.sendTitle(player, head, sub, in, delay, out);
        }
    }
}
