////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.plotsquared.bukkit.listeners;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.bukkit.events.PlayerEnterPlotEvent;
import com.plotsquared.bukkit.events.PlayerLeavePlotEvent;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.listener.PlotListener;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

public class PlotPlusListener extends PlotListener implements Listener {

    private static final HashMap<String, Interval> feedRunnable = new HashMap<>();
    private static final HashMap<String, Interval> healRunnable = new HashMap<>();

    public static void startRunnable(JavaPlugin plugin) {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (!healRunnable.isEmpty()) {
                    for (Iterator<Entry<String, Interval>> iter = healRunnable.entrySet().iterator(); iter.hasNext(); ) {
                        Entry<String, Interval> entry = iter.next();
                        Interval value = entry.getValue();
                        ++value.count;
                        if (value.count == value.interval) {
                            value.count = 0;
                            Player player = Bukkit.getPlayer(entry.getKey());
                            if (player == null) {
                                iter.remove();
                                continue;
                            }
                            double level = player.getHealth();
                            if (level != value.max) {
                                player.setHealth(Math.min(level + value.amount, value.max));
                            }
                        }
                    }
                }
                if (!feedRunnable.isEmpty()) {
                    for (Iterator<Entry<String, Interval>> iter = feedRunnable.entrySet().iterator(); iter.hasNext(); ) {
                        Entry<String, Interval> entry = iter.next();
                        Interval value = entry.getValue();
                        ++value.count;
                        if (value.count == value.interval) {
                            value.count = 0;
                            Player player = Bukkit.getPlayer(entry.getKey());
                            if (player == null) {
                                iter.remove();
                                continue;
                            }
                            int level = player.getFoodLevel();
                            if (level != value.max) {
                                player.setFoodLevel(Math.min(level + value.amount, value.max));
                            }
                        }
                    }
                }
            }
        }, 0L, 20L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMelt(BlockFadeEvent event) {
        BlockState state = event.getNewState();

        if (state.getType() != Material.WATER && state.getType() != Material.STATIONARY_WATER) {
            return;
        }
        Plot plot = BukkitUtil.getLocation(state.getLocation()).getOwnedPlot();
        if (plot == null) {
            return;
        }
        if (!FlagManager.isBooleanFlag(plot, "ice-melt", false)) {
            event.setCancelled(true);
        }
        if (FlagManager.isPlotFlagFalse(plot, "snow-melt")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        Plot plot = BukkitUtil.getLocation(player).getOwnedPlot();
        if (plot == null) {
            return;
        }
        if (FlagManager.isBooleanFlag(plot, "instabreak", false)) {
            event.getBlock().breakNaturally();
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }
        Player player = (Player) event.getEntity();
        Plot plot = BukkitUtil.getLocation(player).getOwnedPlot();
        if (plot == null) {
            return;
        }
        if (FlagManager.isBooleanFlag(plot, "invincible", false)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        Plot plot = BukkitUtil.getLocation(player).getOwnedPlot();
        if (plot == null) {
            return;
        }
        UUID uuid = pp.getUUID();
        if (plot.isAdded(uuid) && FlagManager.isBooleanFlag(plot, "drop-protection", false)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        Plot plot = BukkitUtil.getLocation(player).getOwnedPlot();
        if (plot == null) {
            return;
        }
        UUID uuid = pp.getUUID();
        if (plot.isAdded(uuid) && FlagManager.isBooleanFlag(plot, "item-drop", false)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlotEnter(PlayerEnterPlotEvent event) {
        Player player = event.getPlayer();
        Plot plot = event.getPlot();
        Flag feed = FlagManager.getPlotFlagRaw(plot, "feed");
        if (feed != null) {
            Integer[] value = (Integer[]) feed.getValue();
            feedRunnable.put(player.getName(), new Interval(value[0], value[1], 20));
        }
        Flag heal = FlagManager.getPlotFlagRaw(plot, "heal");
        if (heal != null) {
            Integer[] value = (Integer[]) heal.getValue();
            healRunnable.put(player.getName(), new Interval(value[0], value[1], 20));
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();
        feedRunnable.remove(name);
        healRunnable.remove(name);
    }
    
    @EventHandler
    public void onPlotLeave(PlayerLeavePlotEvent event) {
        Player leaver = event.getPlayer();
        Plot plot = event.getPlot();
        if (!plot.hasOwner()) {
            return;
        }
        BukkitUtil.getPlayer(leaver);
        String name = leaver.getName();
        feedRunnable.remove(name);
        healRunnable.remove(name);
    }

    private static class Interval {

        final int interval;
        final int amount;
        final int max;
        public int count = 0;

        Interval(int interval, int amount, int max) {
            this.interval = interval;
            this.amount = amount;
            this.max = max;
        }
    }

}
