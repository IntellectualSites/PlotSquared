package com.intellectualcrafters.plot.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.PlayerFunctions;

public class PlayerEvents_1_8 extends PlotListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onInteract(final PlayerInteractAtEntityEvent e) {
        final Location l = e.getRightClicked().getLocation();
        if (isPlotWorld(l)) {
            final Player p = e.getPlayer();
            if (!isInPlot(l)) {
                if (!PlotSquared.hasPermission(p, "plots.admin.interact.road")) {
                    PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.interact.road");
                    e.setCancelled(true);
                }
            } else {
                final Plot plot = getCurrentPlot(l);
                if (plot == null || !plot.hasOwner()) {
                    if (!PlotSquared.hasPermission(p, "plots.admin.interact.unowned")) {
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.interact.unowned");
                        e.setCancelled(true);
                    }
                } else if (!plot.hasRights(p)) {
                    if (!PlotSquared.hasPermission(p, "plots.admin.interact.other")) {
                        if (isPlotArea(l)) { 
                            PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.interact.other");
                            e.setCancelled(true); 
                        }
                    }
                }
            }
        }
    }

}
