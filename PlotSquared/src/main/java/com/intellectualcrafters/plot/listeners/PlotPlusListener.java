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

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.events.PlayerEnterPlotEvent;
import com.intellectualcrafters.plot.events.PlayerLeavePlotEvent;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import org.bukkit.*;
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

import java.util.*;

/**
 * Created 2014-10-30 for PlotSquared
 *
 * @author Citymonstret
 */
@SuppressWarnings({"deprecation", "unused"})
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
        if (!isInPlot(player)) {
            PlayerFunctions.sendMessage(player, C.NOT_IN_PLOT);
            return;
        }
        final Plot plot = getPlot(player);
        if (!plot.hasRights(player)) {
            PlayerFunctions.sendMessage(player, C.NO_PLOT_PERMS);
            return;
        }
        final Set<Player> plotPlayers = new HashSet<>();
        for (final Player p : player.getWorld().getPlayers()) {
            if (isInPlot(p) && getPlot(p).equals(plot)) {
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
            PlayerFunctions.sendMessage(p, C.RECORD_PLAY.s().replaceAll("%player", player.getName()).replaceAll("%name", meta.toString()));
        }

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(final BlockDamageEvent event) {
        final Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        if (!isInPlot(player)) {
            return;
        }
        final Plot plot = getPlot(player);
        if (booleanFlag(plot, "instabreak")) {
            event.getBlock().breakNaturally();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(final EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }
        final Player player = (Player) event.getEntity();
        if (!isInPlot(player)) {
            return;
        }
        if (booleanFlag(getPlot(player), "invincible")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickup(final PlayerPickupItemEvent event) {
        if (isInPlot(event.getPlayer()) && !getPlot(event.getPlayer()).hasRights(event.getPlayer()) && booleanFlag(getPlot(event.getPlayer()), "drop-protection")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(final PlayerDropItemEvent event) {
        if (isInPlot(event.getPlayer()) && !getPlot(event.getPlayer()).hasRights(event.getPlayer()) && booleanFlag(getPlot(event.getPlayer()), "item-drop")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlotEnter(final PlayerEnterPlotEvent event) {
        final Plot plot = event.getPlot();
        if (plot.settings.getFlag("greeting") != null) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plot.settings.getFlag("greeting").getValue()));
        }
        if (booleanFlag(plot, "notify-enter")) {
            if (plot.hasOwner()) {
                final Player player = Bukkit.getPlayer(plot.getOwner());
                if (player == null) {
                    return;
                }
                if (player.getUniqueId().equals(event.getPlayer().getUniqueId())) {
                    return;
                }
                if (player.isOnline()) {
                    PlayerFunctions.sendMessage(player, C.NOTIFY_ENTER.s().replace("%player", event.getPlayer().getName()).replace("%plot", plot.getId().toString()));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (feedRunnable.containsKey(event.getPlayer().getName())) {
            feedRunnable.remove(event.getPlayer().getName());
        }
        if (healRunnable.containsKey(event.getPlayer().getName())) {
            healRunnable.remove(event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onPlotLeave(final PlayerLeavePlotEvent event) {
        event.getPlayer().playEffect(event.getPlayer().getLocation(), Effect.RECORD_PLAY, 0);
        final Plot plot = event.getPlot();
        if (plot.settings.getFlag("farewell") != null) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plot.settings.getFlag("farewell").getValue()));
        }
        if (feedRunnable.containsKey(event.getPlayer().getName())) {
            feedRunnable.remove(event.getPlayer().getName());
        }
        if (healRunnable.containsKey(event.getPlayer().getName())) {
            healRunnable.remove(event.getPlayer().getName());
        }
        if (booleanFlag(plot, "notify-leave")) {
            if (plot.hasOwner()) {
                final Player player = Bukkit.getPlayer(plot.getOwner());
                if (player == null) {
                    return;
                }
                if (player.getUniqueId().equals(event.getPlayer().getUniqueId())) {
                    return;
                }
                if (player.isOnline()) {
                    PlayerFunctions.sendMessage(player, C.NOTIFY_LEAVE.s().replace("%player", event.getPlayer().getName()).replace("%plot", plot.getId().toString()));
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
