package com.intellectualcrafters.plot.listeners;

import java.util.Iterator;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.FlagManager;
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
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBigBoom(final BlockExplodeEvent event) {
        Block block = event.getBlock();
        Location loc = BukkitUtil.getLocation(block.getLocation());
        final String world = loc.getWorld();
        if (!PlotSquared.isPlotWorld(world)) {
            return;
        }
        final Plot plot = MainUtil.getPlot(loc);
        if ((plot != null) && plot.hasOwner()) {
            if (FlagManager.isPlotFlagTrue(plot, "explosion")) {
                final Iterator<Block> iter = event.blockList().iterator();
                while (iter.hasNext()) {
                    final Block b = iter.next();
                    if (!plot.equals(MainUtil.getPlot(BukkitUtil.getLocation(b.getLocation())))) {
                        iter.remove();
                    }
                }
                return;
            }
        }
        if (MainUtil.isPlotArea(loc)) {
            event.setCancelled(true);
        } else {
            final Iterator<Block> iter = event.blockList().iterator();
            while (iter.hasNext()) {
                iter.next();
                if (MainUtil.isPlotArea(loc)) {
                    iter.remove();
                }
            }
        }
    }
}
