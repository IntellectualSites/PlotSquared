package com.plotsquared.bukkit.titles;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.bukkit.object.BukkitPlayer;
import org.bukkit.entity.Player;

public class DefaultTitle_19 extends AbstractTitle {

    @Override
    public void sendTitle(PlotPlayer player, String head, String sub, int in, int delay, int out) {
        try {
            final Player playerObj = ((BukkitPlayer) player).player;
            playerObj.sendTitle(head, sub);
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    playerObj.sendTitle("", "");
                }
            }, delay * 20);
        } catch (Throwable ignored) {
            AbstractTitle.TITLE_CLASS = new DefaultTitle();
            AbstractTitle.TITLE_CLASS.sendTitle(player, head, sub, in, delay, out);
        }
    }
}
