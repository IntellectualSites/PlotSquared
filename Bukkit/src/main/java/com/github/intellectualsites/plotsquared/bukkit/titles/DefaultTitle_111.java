package com.github.intellectualsites.plotsquared.bukkit.titles;

import com.github.intellectualsites.plotsquared.bukkit.object.BukkitPlayer;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.AbstractTitle;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation") public class DefaultTitle_111 extends AbstractTitle {

    @Override
    public void sendTitle(PlotPlayer player, String head, String sub, int in, int delay, int out) {
        try {
            final Player playerObj = ((BukkitPlayer) player).player;
            TitleManager_1_11 title = new TitleManager_1_11(head, sub, in, delay, out);
            title.send(playerObj);
            return;
        } catch (Throwable ignored) {
        }
    }
}
