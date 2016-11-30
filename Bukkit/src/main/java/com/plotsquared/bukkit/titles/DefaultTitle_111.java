package com.plotsquared.bukkit.titles;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.plotsquared.bukkit.object.BukkitPlayer;
import org.bukkit.entity.Player;

@SuppressWarnings("deprecation")
public class DefaultTitle_111 extends AbstractTitle {

    @Override
    public void sendTitle(PlotPlayer player, String head, String sub, int in, int delay, int out) {
        try {
            final Player playerObj = ((BukkitPlayer) player).player;
            TitleManager_1_11 title = new TitleManager_1_11(head, sub, in, delay, out);
            title.send(playerObj);
        } catch (Throwable ignored) {
            AbstractTitle.TITLE_CLASS = new DefaultTitle_110();
            AbstractTitle.TITLE_CLASS.sendTitle(player, head, sub, in, delay, out);
        }
    }
}
