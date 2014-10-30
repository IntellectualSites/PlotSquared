package com.intellectualcrafters.plot.listeners;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.events.PlayerEnterPlotEvent;
import com.intellectualcrafters.plot.events.PlayerLeavePlotEvent;
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
 * Created by Citymonstret on 2014-10-30.
 */
public class PlotPlusListener extends PlotListener implements Listener {

    private static HashMap<String, Interval> feedRunnable = new HashMap<>();
    private static HashMap<String, Interval> healRunnable = new HashMap<>();

    public static void startRunnable(JavaPlugin plugin) {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for(Map.Entry<String, Interval> entry : feedRunnable.entrySet()) {
                    Interval value = entry.getValue();
                    ++value.count;
                    if(value.count == value.interval) {
                        value.count = 0;
                        Player player = Bukkit.getPlayer(entry.getKey());
                        int level = player.getFoodLevel();
                        if(level != value.max) {
                            player.setFoodLevel(Math.min(level + value.amount, value.max));
                        }
                    }
                }
                for(Map.Entry<String, Interval> entry : healRunnable.entrySet()) {
                    Interval value = entry.getValue();
                    ++value.count;
                    if(value.count == value.interval) {
                        value.count  = 0;
                        Player player = Bukkit.getPlayer(entry.getKey());
                        double level = player.getHealth();
                        if(level != value.max) {
                            player.setHealth(Math.min(level + value.amount, value.max));
                        }
                    }
                }
            }
        }, 0l, 20l);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if(!event.getInventory().getName().equals(ChatColor.RED + "Plot Jukebox"))
            return;
        event.setCancelled(true);
        if(!isInPlot(player)) {
            PlayerFunctions.sendMessage(player, C.NOT_IN_PLOT);
            return;
        }
        Plot plot = getPlot(player);
        if(!plot.hasRights(player)) {
            PlayerFunctions.sendMessage(player, C.NO_PLOT_PERMS);
            return;
        }
        Set<Player> plotPlayers = new HashSet<>();
        for(Player p : player.getWorld().getPlayers()) {
            if(isInPlot(p) && getPlot(p).equals(plot))
                plotPlayers.add(p);
        }
        RecordMeta meta = null;
        for(RecordMeta m : RecordMeta.metaList) {
            if(m.getMaterial() == event.getCurrentItem().getType()) {
                meta = m;
                break;
            }
        }
        if(meta == null)
            return;
        for(Player p : plotPlayers) {
            p.playEffect(p.getLocation(), Effect.RECORD_PLAY, meta.getMaterial());
            PlayerFunctions.sendMessage(p, C.RECORD_PLAY.s().replaceAll("%player", player.getName()).replaceAll("%name", meta.toString()));
        }

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(BlockDamageEvent event) {
        Player player = event.getPlayer();
        if(player.getGameMode() != GameMode.SURVIVAL) return;
        if(!isInPlot(player)) return;
        Plot plot = getPlot(player);
        if(booleanFlag(plot, "instabreak"))
            event.getBlock().breakNaturally();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if(event.getEntityType() != EntityType.PLAYER)
            return;
        Player player = (Player) event.getEntity();
        if(!isInPlot(player)) return;
        if(booleanFlag(getPlot(player), "invincible"))
            event.setCancelled(true);
    }

    @EventHandler public void onItemPickup(PlayerPickupItemEvent event) { if(isInPlot(event.getPlayer()) && !getPlot(event.getPlayer()).hasRights(event.getPlayer()) && booleanFlag(getPlot(event.getPlayer()), "drop-protection")) event.setCancelled(true); }

    @EventHandler public void onItemDrop(PlayerDropItemEvent event) { if(isInPlot(event.getPlayer()) && !getPlot(event.getPlayer()).hasRights(event.getPlayer()) &&  booleanFlag(getPlot(event.getPlayer()), "item-drop")) event.setCancelled(true);}

    @EventHandler
    public void onPlotEnter(PlayerEnterPlotEvent event) {
        Plot plot = event.getPlot();
        if(plot.settings.getFlag("greeting") != null) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plot.settings.getFlag("greeting").getValue()));
        }
        if(booleanFlag(plot, "notify-enter")) {
            if(plot.hasOwner()) {
                Player player = Bukkit.getPlayer(plot.getOwner());
                if(player == null) return;
                if(player.getUniqueId().equals(event.getPlayer().getUniqueId()))
                    return;
                if(player.isOnline()) {
                    PlayerFunctions.sendMessage(player,
                            C.NOTIFY_ENTER
                                    .s()
                                    .replace("%player", event.getPlayer().getName())
                                    .replace("%plot", plot.getId().toString()));
                }
            }
        }
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (feedRunnable.containsKey(event.getPlayer().getName()) ) {
            feedRunnable.remove(event.getPlayer().getName());
        }
        if (healRunnable.containsKey(event.getPlayer().getName()) ) {
            healRunnable.remove(event.getPlayer().getName());
        }
    }
    @EventHandler
    public void onPlotLeave(PlayerLeavePlotEvent event) {
        event.getPlayer().playEffect(event.getPlayer().getLocation(), Effect.RECORD_PLAY, 0);
        Plot plot = event.getPlot();
        if(plot.settings.getFlag("farewell") != null) {
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', plot.settings.getFlag("farewell").getValue()));
        }
        if (feedRunnable.containsKey(event.getPlayer().getName()) ) {
            feedRunnable.remove(event.getPlayer().getName());
        }
        if (healRunnable.containsKey(event.getPlayer().getName()) ) {
            healRunnable.remove(event.getPlayer().getName());
        }
        if(booleanFlag(plot, "notify-leave")) {
            if(plot.hasOwner()) {
                Player player = Bukkit.getPlayer(plot.getOwner());
                if(player == null) return;
                if(player.getUniqueId().equals(event.getPlayer().getUniqueId()))
                    return;
                if(player.isOnline()) {
                    PlayerFunctions.sendMessage(player,
                            C.NOTIFY_LEAVE
                                    .s()
                                    .replace("%player", event.getPlayer().getName())
                                    .replace("%plot", plot.getId().toString()));
                }
            }
        }
    }

    public static class Interval {
        public int interval;
        public int amount;
        public int count = 0;
        public int max;
        public Interval(int interval, int amount, int max) {
            this.interval = interval;
            this.amount = amount;
            this.max = max;
        }
    }

    /**
     * Created by Citymonstret on 2014-10-22.
     */
    public static class RecordMeta {
        public static List<RecordMeta> metaList = new ArrayList<>();
        static {
            for(int x = 3; x < 12; x++) {
                metaList.add(
                        new RecordMeta(
                                x + "",
                                Material.valueOf("RECORD_" + x)
                        )
                );
            }
        }
        private String name;
        private Material material;
        public RecordMeta(String name, Material material) {
            this.name = name;
            this.material = material;
        }
        @Override
        public String toString() {
            return this.name;
        }
        public Material getMaterial() {
            return this.material;
        }
    }
}
