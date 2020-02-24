package com.github.intellectualsites.plotsquared.bukkit.listeners;

import com.github.intellectualsites.plotsquared.bukkit.events.PlayerEnterPlotEvent;
import com.github.intellectualsites.plotsquared.bukkit.events.PlayerLeavePlotEvent;
import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DropProtectionFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.FeedFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.HealFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.InstabreakFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.InvincibleFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.ItemDropFlag;
import com.github.intellectualsites.plotsquared.plot.flags.types.TimedFlag;
import com.github.intellectualsites.plotsquared.plot.listener.PlotListener;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

@SuppressWarnings("unused") public class PlotPlusListener extends PlotListener implements Listener {

    private static final HashMap<UUID, Interval> feedRunnable = new HashMap<>();
    private static final HashMap<UUID, Interval> healRunnable = new HashMap<>();

    public static void startRunnable(JavaPlugin plugin) {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!healRunnable.isEmpty()) {
                for (Iterator<Entry<UUID, Interval>> iterator =
                     healRunnable.entrySet().iterator(); iterator.hasNext(); ) {
                    Entry<UUID, Interval> entry = iterator.next();
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
                for (Iterator<Entry<UUID, Interval>> iterator =
                     feedRunnable.entrySet().iterator(); iterator.hasNext(); ) {
                    Entry<UUID, Interval> entry = iterator.next();
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
        }, 0L, 20L);
    }

    @EventHandler(priority = EventPriority.HIGH) public void onInteract(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        Plot plot = BukkitUtil.getLocation(player).getOwnedPlot();
        if (plot == null) {
            return;
        }
        if (plot.getFlag(InstabreakFlag.class)) {
            Block block = event.getBlock();
            BlockBreakEvent call = new BlockBreakEvent(block, player);
            Bukkit.getServer().getPluginManager().callEvent(call);
            if (!call.isCancelled()) {
                event.getBlock().breakNaturally();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH) public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }
        Plot plot = BukkitUtil.getLocation(event.getEntity()).getOwnedPlot();
        if (plot == null) {
            return;
        }
        if (plot.getFlag(InvincibleFlag.class)) {
            event.setCancelled(true);
        }
    }

    @EventHandler public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        Plot plot = BukkitUtil.getLocation(player).getOwnedPlot();
        if (plot == null) {
            return;
        }
        UUID uuid = pp.getUUID();
        if (!plot.isAdded(uuid)) {
            if (!plot.getFlag(ItemDropFlag.class)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler public void onPlotEnter(PlayerEnterPlotEvent event) {
        Player player = event.getPlayer();
        Plot plot = event.getPlot();
        TimedFlag.Timed<Integer> feed = plot.getFlag(FeedFlag.class);
        if (feed.getInterval() != 0 && feed.getValue() != 0) {
            feedRunnable.put(player.getUniqueId(), new Interval(feed.getInterval(), feed.getValue(), 20));
        }
        TimedFlag.Timed<Integer> heal = plot.getFlag(HealFlag.class);
        if (heal.getInterval() != 0 && heal.getValue() != 0) {
            healRunnable.put(player.getUniqueId(), new Interval(heal.getInterval(), heal.getValue(), 20));
        }
    }

    @EventHandler public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        feedRunnable.remove(player.getUniqueId());
        healRunnable.remove(player.getUniqueId());
    }

    @EventHandler public void onPlotLeave(PlayerLeavePlotEvent event) {
        Player leaver = event.getPlayer();
        Plot plot = event.getPlot();
        if (!plot.hasOwner()) {
            return;
        }
        BukkitUtil.getPlayer(leaver);
        feedRunnable.remove(leaver.getUniqueId());
        healRunnable.remove(leaver.getUniqueId());
    }

    @EventHandler public void onItemPickup(EntityPickupItemEvent event) {
        LivingEntity ent = event.getEntity();
        if (ent instanceof Player) {
            Player player = (Player) ent;
            PlotPlayer pp = BukkitUtil.getPlayer(player);
            Plot plot = BukkitUtil.getLocation(player).getOwnedPlot();
            if (plot == null) {
                return;
            }
            UUID uuid = pp.getUUID();
            if (!plot.isAdded(uuid) && plot.getFlag(DropProtectionFlag.class)) {
                event.setCancelled(true);
            }
        }
    }

    private static class Interval {

        final int interval;
        final int amount;
        final int max;
        int count = 0;

        Interval(int interval, int amount, int max) {
            this.interval = interval;
            this.amount = amount;
            this.max = max;
        }
    }

}
