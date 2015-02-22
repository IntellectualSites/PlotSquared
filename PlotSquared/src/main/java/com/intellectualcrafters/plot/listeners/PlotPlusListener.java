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
package com.intellectualcrafters.plot.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.events.PlayerEnterPlotEvent;
import com.intellectualcrafters.plot.events.PlayerLeavePlotEvent;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

/**
 * Created 2014-10-30 for PlotSquared
 *
 * @author Citymonstret
 */
@SuppressWarnings({ "deprecation", "unused" })
public class PlotPlusListener extends PlotListener implements Listener {
    private final static HashMap<String, Interval> feedRunnable = new HashMap<>();
    private final static HashMap<String, Interval> healRunnable = new HashMap<>();
    
    public static void startRunnable(final JavaPlugin plugin) {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (final Map.Entry<String, Interval> entry : feedRunnable.entrySet()) {
                    final Interval value = entry.getValue();
                    ++value.count;
                    if (value.count == value.interval) {
                        value.count = 0;
                        final Player player = Bukkit.getPlayer(entry.getKey());
                        final int level = player.getFoodLevel();
                        if (level != value.max) {
                            player.setFoodLevel(Math.min(level + value.amount, value.max));
                        }
                    }
                }
                for (final Map.Entry<String, Interval> entry : healRunnable.entrySet()) {
                    final Interval value = entry.getValue();
                    ++value.count;
                    if (value.count == value.interval) {
                        value.count = 0;
                        final Player player = Bukkit.getPlayer(entry.getKey());
                        final double level = player.getHealth();
                        if (level != value.max) {
                            player.setHealth(Math.min(level + value.amount, value.max));
                        }
                    }
                }
            }
        }, 0l, 20l);
    }
    
    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        if (!event.getInventory().getName().equals(ChatColor.RED + "Plot Jukebox")) {
            return;
        }
        event.setCancelled(true);
        final Plot plot = MainUtil.getPlot(BukkitUtil.getLocation(player));
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        if (plot == null) {
            MainUtil.sendMessage(pp, C.NOT_IN_PLOT);
            return;
        }
        UUID uuid = pp.getUUID();
        if (!plot.isAdded(uuid)) {
            MainUtil.sendMessage(pp, C.NO_PLOT_PERMS);
            return;
        }
        final Set<Player> plotPlayers = new HashSet<>();
        for (final Player p : player.getWorld().getPlayers()) {
            Plot newPlot = MainUtil.getPlot(BukkitUtil.getLocation(player));
            if (plot.equals(newPlot)) {
                plotPlayers.add(p);
            }
        }
        RecordMeta meta = null;
        for (final RecordMeta m : RecordMeta.metaList) {
            if (m.getMaterial() == event.getCurrentItem().getType()) {
                meta = m;
                break;
            }
        }
        if (meta == null) {
            return;
        }
        for (final Player p : plotPlayers) {
            p.playEffect(p.getLocation(), Effect.RECORD_PLAY, meta.getMaterial());
            MainUtil.sendMessage(pp, C.RECORD_PLAY.s().replaceAll("%player", player.getName()).replaceAll("%name", meta.toString()));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(final BlockDamageEvent event) {
        final Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        final Plot plot = MainUtil.getPlot(BukkitUtil.getLocation(player));
        if (plot == null) {
            return;
        }
        if (booleanFlag(plot, "instabreak", false)) {
            event.getBlock().breakNaturally();
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(final EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }
        final Player player = (Player) event.getEntity();
        final Plot plot = MainUtil.getPlot(BukkitUtil.getLocation(player));
        if (plot == null) {
            return;
        }
        if (booleanFlag(plot, "invincible", false)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onItemPickup(final PlayerPickupItemEvent event) {
        final Player player = event.getPlayer();
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        final Plot plot = MainUtil.getPlot(pp.getLocation());
        if (plot == null) {
            return;
        }
        UUID uuid = pp.getUUID();
        if (plot.isAdded(uuid) && booleanFlag(plot, "drop-protection", false)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onItemDrop(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        final Plot plot = MainUtil.getPlot(pp.getLocation());
        if (plot == null) {
            return;
        }
        UUID uuid = pp.getUUID();
        if (plot.isAdded(uuid) && booleanFlag(plot, "item-drop", false)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onPlotEnter(final PlayerEnterPlotEvent event) {
        final Plot plot = event.getPlot();
        if (FlagManager.getPlotFlag(plot, "greeting") != null) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', C.PREFIX_GREETING.s().replaceAll("%id%", plot.id + "") + FlagManager.getPlotFlag(plot, "greeting").getValueString()));
        }
        if (booleanFlag(plot, "notify-enter", false)) {
            if (plot.hasOwner()) {
                final PlotPlayer pp = UUIDHandler.getPlayer(plot.getOwner());
                if (pp == null) {
                    return;
                }
                final Player trespasser = event.getPlayer();
                PlotPlayer pt = BukkitUtil.getPlayer(trespasser);
                if (pp.getUUID().equals(pt.getUUID())) {
                    return;
                }
                if (Permissions.hasPermission(pt, "plots.flag.notify-enter.bypass")) {
                    return;
                }
                if (pp.isOnline()) {
                    MainUtil.sendMessage(pp, C.NOTIFY_ENTER.s().replace("%player", trespasser.getName()).replace("%plot", plot.getId().toString()));
                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String name = player.getName();
        if (feedRunnable.containsKey(name)) {
            feedRunnable.remove(name);
        }
        if (healRunnable.containsKey(name)) {
            healRunnable.remove(name);
        }
    }
    
    @EventHandler
    public void onPlotLeave(final PlayerLeavePlotEvent event) {
        Player leaver = event.getPlayer();
        leaver.playEffect(leaver.getLocation(), Effect.RECORD_PLAY, 0);
        final Plot plot = event.getPlot();
        if (FlagManager.getPlotFlag(plot, "farewell") != null) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', C.PREFIX_FAREWELL.s().replaceAll("%id%", plot.id + "") + FlagManager.getPlotFlag(plot, "farewell").getValueString()));
        }
        PlotPlayer pl = BukkitUtil.getPlayer(leaver);
        String name = pl.getName();
        if (feedRunnable.containsKey(leaver)) {
            feedRunnable.remove(leaver);
        }
        if (healRunnable.containsKey(leaver)) {
            healRunnable.remove(leaver);
        }
        if (booleanFlag(plot, "notify-leave", false)) {
            if (plot.hasOwner()) {
                final PlotPlayer pp = UUIDHandler.getPlayer(plot.getOwner());
                if (pp == null) {
                    return;
                }
                if (pp.getUUID().equals(pl.getUUID())) {
                    return;
                }
                if (Permissions.hasPermission(pl, "plots.flag.notify-leave.bypass")) {
                    return;
                }
                if (pp.isOnline()) {
                    MainUtil.sendMessage(pp, C.NOTIFY_LEAVE.s().replace("%player", pl.getName()).replace("%plot", plot.getId().toString()));
                }
            }
        }
    }
    
    public static class Interval {
        public final int interval;
        public final int amount;
        public final int max;
        public int count = 0;
        
        public Interval(final int interval, final int amount, final int max) {
            this.interval = interval;
            this.amount = amount;
            this.max = max;
        }
    }
    
    /**
     * Record Meta Class
     *
     * @author Citymonstret
     */
    public static class RecordMeta {
        public final static List<RecordMeta> metaList = new ArrayList<>();
        static {
            for (int x = 3; x < 12; x++) {
                metaList.add(new RecordMeta(x + "", Material.valueOf("RECORD_" + x)));
            }
        }
        private final String name;
        private final Material material;
        
        public RecordMeta(final String name, final Material material) {
            this.name = name;
            this.material = material;
        }
        
        @Override
        public String toString() {
            return this.name;
        }
        
        @Override
        public int hashCode() {
            return this.name.hashCode();
        }
        
        public Material getMaterial() {
            return this.material;
        }
    }
}
