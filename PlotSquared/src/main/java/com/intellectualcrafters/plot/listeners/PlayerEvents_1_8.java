package com.intellectualcrafters.plot.listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import com.intellectualcrafters.plot.util.bukkit.BukkitPlayerFunctions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class PlayerEvents_1_8 extends PlotListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onInteract(final PlayerInteractAtEntityEvent e) {
        final Location l = BukkitUtil.getLocation(e.getRightClicked().getLocation());
        if (isPlotWorld(l)) {
            final Player p = e.getPlayer();
            if (!isInPlot(l)) {
                if (!BukkitMain.hasPermission(p, "plots.admin.interact.road")) {
                    BukkitPlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.interact.road");
                    e.setCancelled(true);
                }
            } else {
                final Plot plot = MainUtil.getPlot(l);
                if ((plot == null) || !plot.hasOwner()) {
                    if (!BukkitMain.hasPermission(p, "plots.admin.interact.unowned")) {
                        BukkitPlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.interact.unowned");
                        e.setCancelled(true);
                    }
                } else {
                    UUID uuid = UUIDHandler.getUUID(p);
                    if (!plot.isAdded(uuid)) {
                        if (!BukkitMain.hasPermission(p, "plots.admin.interact.other")) {
                            if (isPlotArea(l)) {
                                BukkitPlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.interact.other");
                                e.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }
}
