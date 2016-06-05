package com.plotsquared.bukkit.listeners;

import com.google.common.base.Optional;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.bukkit.events.PlayerEnterPlotEvent;
import com.plotsquared.bukkit.events.PlayerLeavePlotEvent;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.listener.PlotListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlotPlusListener extends PlotListener implements Listener {

    private static final HashMap<String, Interval> feedRunnable = new HashMap<>();
    private static final HashMap<String, Interval> healRunnable = new HashMap<>();

    public static void startRunnable(JavaPlugin plugin) {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (!healRunnable.isEmpty()) {
                    for (Iterator<Entry<String, Interval>> iterator = healRunnable.entrySet().iterator(); iterator.hasNext(); ) {
                        Entry<String, Interval> entry = iterator.next();
                        Interval value = entry.getValue();
                        ++value.count;
                        if (value.count == value.interval) {
                            value.count = 0;
                            Player player = Bukkit.getPlayer(entry.getKey());
                            if (player == null) {
                                iterator.remove();
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
                    for (Iterator<Entry<String, Interval>> iterator = feedRunnable.entrySet().iterator(); iterator.hasNext(); ) {
                        Entry<String, Interval> entry = iterator.next();
                        Interval value = entry.getValue();
                        ++value.count;
                        if (value.count == value.interval) {
                            value.count = 0;
                            Player player = Bukkit.getPlayer(entry.getKey());
                            if (player == null) {
                                iterator.remove();
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
    public void onInteract(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        Plot plot = BukkitUtil.getLocation(player).getOwnedPlot();
        if (plot == null) {
            return;
        }
        if (Flags.INSTABREAK.isTrue(plot)) {
            Block block = event.getBlock();
            BlockBreakEvent call = new BlockBreakEvent(block, player);
            Bukkit.getServer().getPluginManager().callEvent(call);
            if (!call.isCancelled()) {
                event.getBlock().breakNaturally();
            }
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
        if (Flags.INVINCIBLE.isTrue(plot)) {
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
        if (plot.isAdded(uuid) && Flags.DROP_PROTECTION.isTrue(plot)) {
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
        if (!plot.isAdded(uuid)) {
            if (Flags.ITEM_DROP.isFalse(plot)) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onPlotEnter(PlayerEnterPlotEvent event) {
        Player player = event.getPlayer();
        Plot plot = event.getPlot();
        Optional<Integer[]> feed = plot.getFlag(Flags.FEED);
        if (feed.isPresent()) {
            Integer[] value = feed.get();
            feedRunnable.put(player.getName(), new Interval(value[0], value[1], 20));
        }
        Optional<Integer[]> heal = plot.getFlag(Flags.HEAL);
        if (heal.isPresent()) {
            Integer[] value = heal.get();
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
