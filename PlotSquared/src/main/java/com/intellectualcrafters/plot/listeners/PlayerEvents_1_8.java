package com.intellectualcrafters.plot.listeners;

import java.util.UUID;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;

public class PlayerEvents_1_8 extends PlotListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(final PlayerInteractAtEntityEvent e) {
        Entity entity = e.getRightClicked();
        if (!(entity instanceof ArmorStand)) {
            return;
        }
        final Location l = BukkitUtil.getLocation(e.getRightClicked().getLocation());
        String world = l.getWorld();
        if (!PlotSquared.isPlotWorld(world)) {
            return;
        }
        Plot plot = MainUtil.getPlot(l);
        PlotPlayer pp = BukkitUtil.getPlayer(e.getPlayer());
        if (plot == null) {
            if (!MainUtil.isPlotArea(l)) {
                return;
            }
            if (!Permissions.hasPermission(pp, "plots.admin.interact.road")) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.road");
                e.setCancelled(true);
            }
        }
        else {
            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, "plots.admin.interact.unowned")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.unowned");
                    e.setCancelled(true);
                }
            }
            else {
                final UUID uuid = pp.getUUID();
                if (!plot.isAdded(uuid)) {
                    if (!Permissions.hasPermission(pp, "plots.admin.interact.other")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.other");
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}
