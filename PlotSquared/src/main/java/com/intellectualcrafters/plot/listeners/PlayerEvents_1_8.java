package com.intellectualcrafters.plot.listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class PlayerEvents_1_8 extends PlotListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onInteract(final PlayerInteractAtEntityEvent e) {
        final Location l = BukkitUtil.getLocation(e.getRightClicked().getLocation());
        if (isPlotWorld(l)) {
            final Player p = e.getPlayer();
            PlotPlayer pp = BukkitUtil.getPlayer(e.getPlayer());
            if (!isInPlot(l)) {
                if (!Permissions.hasPermission(pp, "plots.admin.interact.road")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.road");
                    e.setCancelled(true);
                }
            } else {
                final Plot plot = MainUtil.getPlot(l);
                if ((plot == null) || !plot.hasOwner()) {
                    if (!Permissions.hasPermission(pp, "plots.admin.interact.unowned")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.unowned");
                        e.setCancelled(true);
                    }
                } else {
                    UUID uuid = pp.getUUID();
                    if (!plot.isAdded(uuid)) {
                        if (!Permissions.hasPermission(pp, "plots.admin.interact.other")) {
                            if (isPlotArea(l)) {
                                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.other");
                                e.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }
}
