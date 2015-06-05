package com.intellectualcrafters.plot.listeners;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.listeners.worldedit.WEManager;
import com.intellectualcrafters.plot.object.BukkitPlayer;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotHandler;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

/**
 * Player Events involving plots
 *
 * @author Citymonstret
 * @author Empire92
 */
public class PlayerEvents extends com.intellectualcrafters.plot.listeners.PlotListener implements Listener {

    @EventHandler
    public void onRedstoneEvent(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        Location loc = BukkitUtil.getLocation(block.getLocation());
        if (!PlotSquared.isPlotWorld(loc.getWorld())) {
            return;
        }
        Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return;
        }
        Flag redstone = FlagManager.getPlotFlag(plot, "redstone");
        if (redstone == null || (Boolean) redstone.getValue()) {
            return;
        }
        if (!MainUtil.isPlotArea(plot)) {
            return;
        }
        switch (block.getType()) {
            case REDSTONE_LAMP_OFF:
            case REDSTONE_LAMP_ON:
            case PISTON_BASE:
            case PISTON_STICKY_BASE:
            case IRON_DOOR_BLOCK:
            case LEVER:
            case WOODEN_DOOR:
            case FENCE_GATE:
            case WOOD_BUTTON:
            case STONE_BUTTON:
            case IRON_PLATE:
            case WOOD_PLATE:
            case STONE_PLATE:
            case GOLD_PLATE:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
            case IRON_TRAPDOOR:
            case SPRUCE_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case ACACIA_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
            case POWERED_RAIL: {
                return;
            }
        }
        event.setNewCurrent(0);
    }
    
    public static void sendBlockChange(final org.bukkit.Location bloc, final Material type, final byte data) {
        TaskManager.runTaskLater(new Runnable() {
            @Override
            public void run() {
                String world = bloc.getWorld().getName();
                int x = bloc.getBlockX();
                int z = bloc.getBlockZ();
                int distance = Bukkit.getViewDistance() * 16;
                for (PlotPlayer player : UUIDHandler.players.values()) {
                    Location loc = player.getLocation();
                    if (loc.getWorld().equals(world)) {
                        if (16 * (Math.abs(loc.getX() - x)/16) > distance) {
                            continue;
                        }
                        if (16 * (Math.abs(loc.getZ() - z)/16) > distance) {
                            continue;
                        }
                        ((BukkitPlayer) player).player.sendBlockChange(bloc, type, data);
                    }
                }
            }
        }, 3);
    }

    @EventHandler
    public void onPhysicsEvent(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        Location loc = BukkitUtil.getLocation(block.getLocation());
        if (!PlotSquared.isPlotWorld(loc.getWorld())) {
            return;
        }
        switch (block.getType()) {
            case REDSTONE_COMPARATOR_OFF:
            case REDSTONE_COMPARATOR_ON: {
                Plot plot = MainUtil.getPlot(loc);
                if (plot == null) {
                    return;
                }
                Flag redstone = FlagManager.getPlotFlag(plot, "redstone");
                if (redstone == null || (Boolean) redstone.getValue()) {
                    return;
                }
                if (!MainUtil.isPlotArea(plot)) {
                    return;
                }
                event.setCancelled(true);
                return;
            }
        }
        if (Settings.PHYSICS_LISTENER && block.getType().hasGravity()) {
            Plot plot = MainUtil.getPlot(loc);
            if (plot == null) {
                return;
            }
            if (FlagManager.isPlotFlagTrue(plot, "disable-physics")) {
                event.setCancelled(true);
            }
        }
    }
    
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile entity = (Projectile) event.getEntity();
        Location loc = BukkitUtil.getLocation(entity);
        if (!PlotSquared.isPlotWorld(loc.getWorld())) {
            return;
        }
        Plot plot = MainUtil.getPlot(loc);
        if (!MainUtil.isPlotArea(loc)) {
            return;
        }
        ProjectileSource shooter = entity.getShooter();
        if (shooter instanceof BlockProjectileSource) {
            if (plot == null) {
                entity.remove();
                return;
            }
            Location sLoc = BukkitUtil.getLocation(((BlockProjectileSource) shooter).getBlock().getLocation());
            Plot sPlot = MainUtil.getPlot(sLoc);
            if (sPlot == null || !PlotHandler.sameOwners(plot, sPlot)) {
                entity.remove();
                return;
            }
        }
        else if ((shooter instanceof Player)) {
            PlotPlayer pp = BukkitUtil.getPlayer((Player) shooter);
            if (plot == null) {
                if (!pp.hasPermission("plots.projectile.unowned")) {
                    entity.remove();
                }
                return;
            }
            if (plot.isAdded(pp.getUUID())) {
                return;
            }
            if (pp.hasPermission("plots.projectile.other")) {
                return;
            }
            entity.remove();
        }
    }
    
    @EventHandler
    public void PlayerCommand(final PlayerCommandPreprocessEvent event) {
        final String message = event.getMessage();
        if (message.toLowerCase().startsWith("/plotme") || message.toLowerCase().startsWith("/ap")) {
            final Player player = event.getPlayer();
            if (Settings.USE_PLOTME_ALIAS) {
                player.performCommand(message.replace("/plotme", "plots"));
            } else {
                MainUtil.sendMessage(BukkitUtil.getPlayer(player), C.NOT_USING_PLOTME);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoad(final ChunkLoadEvent event) {
        final String worldname = event.getWorld().getName();
        final Chunk chunk = event.getChunk();
        if (MainUtil.worldBorder.containsKey(worldname)) {
            final int border = MainUtil.getBorder(worldname);
            final int x = Math.abs(chunk.getX() << 4);
            final int z = Math.abs(chunk.getZ() << 4);
            if ((x > border) || (z > border)) {
                chunk.unload(false, true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        BukkitUtil.removePlayer(player.getName());
        if (!player.hasPlayedBefore()) {
            player.saveData();
        }
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        final String username = pp.getName();
        final StringWrapper name = new StringWrapper(username);
        final UUID uuid = pp.getUUID();
        UUIDHandler.add(name, uuid);
        ExpireManager.dates.put(uuid, System.currentTimeMillis());
        if (PlotSquared.worldEdit != null) {
            if (Permissions.hasPermission(pp, "plots.worldedit.bypass")) {
                WEManager.bypass.add(pp.getName());
            }
        }
        final Location loc = BukkitUtil.getLocation(player.getLocation());
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return;
        }
        if (Settings.TELEPORT_ON_LOGIN) {
            MainUtil.teleportPlayer(pp, pp.getLocation(), plot);
            MainUtil.sendMessage(pp, C.TELEPORTED_TO_ROAD);
        }
        plotEntry(pp, plot);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void PlayerMove(final PlayerMoveEvent event) {
        final Location f = BukkitUtil.getLocation(event.getFrom());
        final Location t = BukkitUtil.getLocation(event.getTo());
        if ((f.getX() != t.getX()) || (f.getZ() != t.getZ())) {
            final Player player = event.getPlayer();
            if (Settings.TELEPORT_DELAY != 0) {
                TaskManager.TELEPORT_QUEUE.remove(player.getName());
            }
            final String worldname = t.getWorld();
            if (!PlotSquared.isPlotWorld(worldname)) {
                return;
            }
            if (MainUtil.worldBorder.containsKey(worldname)) {
                final int border = MainUtil.getBorder(worldname);
                boolean passed = true;
                if (t.getX() > border) {
                    event.getTo().setX(border);
                } else if (t.getX() < -border) {
                    event.getTo().setX(-border);
                } else if (t.getZ() > border) {
                    event.getTo().setZ(border);
                } else if (t.getZ() < -border) {
                    event.getTo().setZ(-border);
                } else {
                    passed = false;
                }
                if (passed) {
                    player.teleport(event.getTo());
                    final PlotPlayer pp = BukkitUtil.getPlayer(player);
                    MainUtil.sendMessage(pp, C.BORDER);
                    return;
                }
            }
            Plot plot = MainUtil.getPlot(t);
            if (plot != null) {
                final PlotPlayer pp = BukkitUtil.getPlayer(player);
                if (plot.denied.size() > 0) {
                    if (plot.isDenied(pp.getUUID())) {
                        if (!Permissions.hasPermission(pp, "plots.admin.entry.denied")) {
                            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.entry.denied");
                            if (!plot.equals(MainUtil.getPlot(BukkitUtil.getLocation(event.getFrom())))) {
                                player.teleport(event.getFrom());
                            }
                            else {
                                player.teleport(player.getWorld().getSpawnLocation());
                            }
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                if (!plot.equals(MainUtil.getPlot(f))) {
                    plotEntry(pp, plot);
                }
            } 
            else if (MainUtil.leftPlot(f, t)) {
                plot = MainUtil.getPlot(f);
                plotExit(BukkitUtil.getPlayer(player), plot);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final String world = player.getWorld().getName();
        if (!PlotSquared.isPlotWorld(world)) {
            return;
        }
        final PlotWorld plotworld = PlotSquared.getPlotWorld(world);
        final PlotPlayer plr = BukkitUtil.getPlayer(player);
        if (!plotworld.PLOT_CHAT && (plr.getMeta("chat") == null || !(Boolean) plr.getMeta("chat"))) {
            return;
        }
        final Location loc = BukkitUtil.getLocation(player);
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return;
        }
        final String message = event.getMessage();
        String format = C.PLOT_CHAT_FORMAT.s();
        final String sender = event.getPlayer().getDisplayName();
        final PlotId id = plot.id;
        final Set<Player> recipients = event.getRecipients();
        recipients.clear();
        for (final Player p : Bukkit.getOnlinePlayers()) {
            if (plot.equals(MainUtil.getPlot(BukkitUtil.getLocation(p)))) {
                recipients.add(p);
            }
        }
        format = format.replaceAll("%plot_id%", id.x + ";" + id.y).replaceAll("%sender%", sender).replaceAll("%msg%", message);
        format = ChatColor.translateAlternateColorCodes('&', format);
        event.setFormat(format);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void BlockDestroy(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final String world = player.getWorld().getName();
        if (!PlotSquared.isPlotWorld(world)) {
            return;
        }
        final Location loc = BukkitUtil.getLocation(event.getBlock().getLocation());
        final Plot plot = MainUtil.getPlot(loc);
        if (plot != null) {
            if (event.getBlock().getY() == 0) {
                event.setCancelled(true);
                return;
            }
            if (!plot.hasOwner()) {
                final PlotPlayer pp = BukkitUtil.getPlayer(player);
                if (Permissions.hasPermission(pp, "plots.admin.destroy.unowned")) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.destroy.unowned");
                event.setCancelled(true);
                return;
            }
            final PlotPlayer pp = BukkitUtil.getPlayer(player);
            if (!plot.isAdded(pp.getUUID())) {
                final Flag destroy = FlagManager.getPlotFlag(plot, "break");
                final Block block = event.getBlock();
                if ((destroy != null) && ((HashSet<PlotBlock>) destroy.getValue()).contains(new PlotBlock((short) block.getTypeId(), block.getData()))) {
                    return;
                }
                if (Permissions.hasPermission(pp, "plots.admin.destroy.other")) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.destroy.other");
                event.setCancelled(true);
                return;
            }
            return;
        }
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        if (Permissions.hasPermission(pp, "plots.admin.destroy.road")) {
            return;
        }
        if (MainUtil.isPlotArea(loc)) {
            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.destroy.road");
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBigBoom(final EntityExplodeEvent event) {
        Location loc = BukkitUtil.getLocation(event.getLocation());
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onWorldChanged(final PlayerChangedWorldEvent event) {
        final PlotPlayer player = BukkitUtil.getPlayer(event.getPlayer());
        if (PlotSquared.worldEdit != null) {
            if (!Permissions.hasPermission(player, "plots.worldedit.bypass")) {
                WEManager.bypass.remove(player.getName());
            }
            else {
                WEManager.bypass.add(player.getName());
            }
        }
        ((BukkitPlayer) player).hasPerm = new HashSet<>();
        ((BukkitPlayer) player).noPerm = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPeskyMobsChangeTheWorldLikeWTFEvent(final EntityChangeBlockEvent event) {
        final String world = event.getBlock().getWorld().getName();
        if (!PlotSquared.isPlotWorld(world)) {
            return;
        }
        final Entity e = event.getEntity();
        if (!(e instanceof Player)) {
            if (!(e instanceof org.bukkit.entity.FallingBlock)) {
                event.setCancelled(true);
                return;
            }
        } else {
            final Block b = event.getBlock();
            final Player p = (Player) e;
            final Location loc = BukkitUtil.getLocation(b.getLocation());
            Plot plot = MainUtil.getPlot(loc);
            if (plot == null) {
                if (MainUtil.isPlotAreaAbs(loc)) {
                    final PlotPlayer pp = BukkitUtil.getPlayer(p);
                    if (!Permissions.hasPermission(pp, "plots.admin.build.road")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.road");
                        event.setCancelled(true);
                        return;
                    }
                }
            } else {
                if (!plot.hasOwner()) {
                    final PlotPlayer pp = BukkitUtil.getPlayer(p);
                    if (!Permissions.hasPermission(pp, "plots.admin.build.unowned")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.unowned");
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    final PlotPlayer pp = BukkitUtil.getPlayer(p);
                    if (!plot.isAdded(pp.getUUID())) {
                        if (!Permissions.hasPermission(pp, "plots.admin.build.other")) {
                            if (MainUtil.isPlotArea(loc)) {
                                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.other");
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityBlockForm(final EntityBlockFormEvent e) {
        final String world = e.getBlock().getWorld().getName();
        if (!PlotSquared.isPlotWorld(world)) {
            return;
        }
        if ((!(e.getEntity() instanceof Player))) {
            if (MainUtil.isPlotArea(BukkitUtil.getLocation(e.getBlock().getLocation()))) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBS(final BlockSpreadEvent e) {
        final Block b = e.getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (PlotSquared.isPlotWorld(loc.getWorld())) {
            if (MainUtil.isPlotRoad(loc)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBF(final BlockFormEvent e) {
        final Block b = e.getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (PlotSquared.isPlotWorld(loc.getWorld())) {
            if (MainUtil.isPlotRoad(loc)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBD(final BlockDamageEvent event) {
        final Player player = event.getPlayer();
        if (player == null) {
            final Location loc = BukkitUtil.getLocation(event.getBlock().getLocation());
            if (PlotSquared.isPlotWorld(loc.getWorld())) {
                if (MainUtil.isPlotRoad(loc)) {
                    event.setCancelled(true);
                }
            }
        }
        final String world = player.getWorld().getName();
        if (!PlotSquared.isPlotWorld(world)) {
            return;
        }
        final Location loc = BukkitUtil.getLocation(event.getBlock().getLocation());
        final Plot plot = MainUtil.getPlot(loc);
        if (plot != null) {
            if (event.getBlock().getY() == 0) {
                event.setCancelled(true);
                return;
            }
            if (!plot.hasOwner()) {
                final PlotPlayer pp = BukkitUtil.getPlayer(player);
                if (Permissions.hasPermission(pp, "plots.admin.destroy.unowned")) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.destroy.unowned");
                event.setCancelled(true);
                return;
            }
            final PlotPlayer pp = BukkitUtil.getPlayer(player);
            if (!plot.isAdded(pp.getUUID())) {
                final Flag destroy = FlagManager.getPlotFlag(plot, "break");
                final Block block = event.getBlock();
                if ((destroy != null) && ((HashSet<PlotBlock>) destroy.getValue()).contains(new PlotBlock((short) block.getTypeId(), block.getData()))) {
                    return;
                }
                if (Permissions.hasPermission(pp, "plots.admin.destroy.other")) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.destroy.other");
                event.setCancelled(true);
                return;
            }
            return;
        }
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        if (Permissions.hasPermission(pp, "plots.admin.destroy.road")) {
            return;
        }
        if (MainUtil.isPlotArea(loc)) {
            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.destroy.road");
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFade(final BlockFadeEvent e) {
        final Block b = e.getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (PlotSquared.isPlotWorld(loc.getWorld())) {
            if (MainUtil.isPlotRoad(loc)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChange(final BlockFromToEvent e) {
        final Block b = e.getToBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (PlotSquared.isPlotWorld(loc.getWorld())) {
            if (MainUtil.isPlotRoad(loc)) {
                e.setCancelled(true);
            }
            else if (Settings.PHYSICS_LISTENER) {
                Plot plot = MainUtil.getPlot(loc);
                if (FlagManager.isPlotFlagTrue(plot, "disable-physics")) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onGrow(final BlockGrowEvent e) {
        final Block b = e.getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (PlotSquared.isPlotWorld(loc.getWorld())) {
            if (MainUtil.isPlotRoad(loc)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonExtend(final BlockPistonExtendEvent event) {
        final Block block = event.getBlock();
        Location loc = BukkitUtil.getLocation(block.getLocation());
        String world = loc.getWorld();
        if (!PlotSquared.isPlotWorld(world)) {
            return;
        }
        Plot plot = MainUtil.getPlot(loc);
        BlockFace face = event.getDirection();
        Vector relative = new Vector(face.getModX(), face.getModY(), face.getModZ());
        List<Block> blocks = event.getBlocks();
        for (final Block b : blocks) {
            Location bloc = BukkitUtil.getLocation(b.getLocation().add(relative));
            Plot newPlot = MainUtil.getPlot(bloc);
            if (!MainUtil.equals(plot, newPlot)) {
                event.setCancelled(true);
                return;
            }
        }
        if (!Settings.PISTON_FALLING_BLOCK_CHECK) {
            return;
        }
        org.bukkit.Location lastLoc;
        if (blocks.size() > 0) {
            lastLoc = blocks.get(blocks.size() - 1).getLocation().add(relative);
        }
        else {
            lastLoc = event.getBlock().getLocation().add(relative);
        }
        Entity[] ents = lastLoc.getChunk().getEntities();
        for (Entity entity : ents) {
            if (entity instanceof FallingBlock) {
                org.bukkit.Location eloc = entity.getLocation();
                if (eloc.distanceSquared(lastLoc) < 2) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private boolean pistonBlocks = true;
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonRetract(final BlockPistonRetractEvent event) {
        final Block block = event.getBlock();
        Location loc = BukkitUtil.getLocation(block.getLocation());
        String world = loc.getWorld();
        if (!PlotSquared.isPlotWorld(world)) {
            return;
        }
        if (block.getType() != Material.PISTON_STICKY_BASE && block.getType() != Material.PISTON_BASE && block.getType() != Material.PISTON_MOVING_PIECE) {
            return;
        }

        Plot plot = MainUtil.getPlot(loc);
        
        if (pistonBlocks) {
            try {
                for (Block pulled : event.getBlocks()) {
                    Plot other = MainUtil.getPlot(BukkitUtil.getLocation(pulled.getLocation()));
                    if (!MainUtil.equals(plot, other)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            catch (Throwable e) {
                pistonBlocks = false;
            }
        }
        if (!pistonBlocks && block.getType() != Material.PISTON_BASE) {
            BlockFace dir = event.getDirection();
            Location bloc = BukkitUtil.getLocation(block.getLocation().subtract(dir.getModX() * 2, dir.getModY() * 2, dir.getModZ() * 2));
            Plot newPlot = MainUtil.getPlot(bloc);
            if (!MainUtil.equals(plot, newPlot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onStructureGrow(final StructureGrowEvent e) {
        if (!PlotSquared.isPlotWorld(e.getWorld().getName())) {
            return;
        }
        final List<BlockState> blocks = e.getBlocks();
        if (blocks.size() == 0) {
            return;
        }
        Plot origin = MainUtil.getPlot(BukkitUtil.getLocation(blocks.get(0).getLocation()));
        BlockState start = blocks.get(0);
        for (int i = blocks.size() - 1; i >= 0; i--) {
            final Location loc = BukkitUtil.getLocation(blocks.get(i).getLocation());
            final Plot plot = MainUtil.getPlot(loc);
            if (!MainUtil.equals(plot, origin)) {
                e.getBlocks().remove(i);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent event) {
    	Action action = event.getAction();
        final Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        final Player player = event.getPlayer();
        final String world = player.getWorld().getName();
        if (!PlotSquared.isPlotWorld(world)) {
            return;
        }
        final Location loc = BukkitUtil.getLocation(event.getClickedBlock().getLocation());
        
        Plot plot = MainUtil.getPlot(loc);
        if (plot != null) {
            if (!plot.hasOwner()) {
                final PlotPlayer pp = BukkitUtil.getPlayer(player);
                if (Permissions.hasPermission(pp, "plots.admin.interact.unowned")) {
                    return;
                }
                if (action != Action.PHYSICAL && action != Action.LEFT_CLICK_BLOCK) {
                	MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.unowned");
                }
                event.setCancelled(true);
                return;
            }
            final Flag use = FlagManager.getPlotFlag(plot, "use");
            if ((use != null) && ((HashSet<PlotBlock>) use.getValue()).contains(new PlotBlock((short) block.getTypeId(), block.getData()))) {
                return;
            }
            final PlotPlayer pp = BukkitUtil.getPlayer(player);
            if (!plot.isAdded(pp.getUUID())) {
                if (Permissions.hasPermission(pp, "plots.admin.interact.other")) {
                    return;
                }
                if (action != Action.PHYSICAL && action != Action.LEFT_CLICK_BLOCK) {
                	MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.other");
                }
                event.setCancelled(true);
                return;
            }
            return;
        }
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        if (Permissions.hasPermission(pp, "plots.admin.interact.road")) {
            return;
        }
        if (MainUtil.isPlotArea(loc)) {
            if (action != Action.PHYSICAL && action != Action.LEFT_CLICK_BLOCK) {
        		MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.road");
        	}
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void MobSpawn(final CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            return;
        }
        final Location loc = BukkitUtil.getLocation(event.getLocation());
        final String world = loc.getWorld();
        if (!PlotSquared.isPlotWorld(world)) {
            return;
        }
        if (!MainUtil.isPlotArea(loc)) {
            return;
        }
        final PlotWorld pW = PlotSquared.getPlotWorld(world);
        final CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if ((reason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG || reason == CreatureSpawnEvent.SpawnReason.DISPENSE_EGG) && !pW.SPAWN_EGGS) {
            event.setCancelled(true);
            return;
        } else if ((reason == CreatureSpawnEvent.SpawnReason.BREEDING) && !pW.SPAWN_BREEDING) {
            event.setCancelled(true);
            return;
        } else if ((reason == CreatureSpawnEvent.SpawnReason.CUSTOM) && !pW.SPAWN_CUSTOM && !(event.getEntityType().getTypeId() == 30)) {
            event.setCancelled(true);
            return;
        }
        Plot plot = MainUtil.getPlot(loc);
        if (checkEntity(entity, plot)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    public void onEntityFall(EntityChangeBlockEvent event) {
        if (!Settings.PHYSICS_LISTENER) {
            return;
        }
        if (event.getEntityType() != EntityType.FALLING_BLOCK) {
            return;
        }
        Block block = event.getBlock();
        World world = block.getWorld();
        String worldname = world.getName();
        if (!PlotSquared.isPlotWorld(worldname)) {
            return;
        }
        Location loc = BukkitUtil.getLocation(block.getLocation());
        Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return;
        }
        if (FlagManager.isPlotFlagTrue(plot, "disable-physics")) {
            event.setCancelled(true);
        }
    }
    
    public boolean checkEntity(Entity entity, Plot plot) {
        if (plot != null && plot.owner != null) {
            Flag entityFlag = FlagManager.getPlotFlag(plot, "entity-cap");
            int[] mobs = ChunkManager.manager.countEntities(plot);
            if (entityFlag != null) {
                int cap = ((Integer) entityFlag.getValue());
                if (mobs[0] >= cap) {
                    return true;
                }
            }
            if (entity instanceof Creature) {
                Flag mobFlag = FlagManager.getPlotFlag(plot, "mob-cap");
                if (mobFlag != null) {
                    int cap = ((Integer) mobFlag.getValue());
                    if (mobs[3] >= cap) {
                        return true;
                    }
                }
                if (entity instanceof Animals) {
                    Flag animalFlag = FlagManager.getPlotFlag(plot, "animal-cap");
                    if (animalFlag != null) {
                        int cap = ((Integer) animalFlag.getValue());
                        if (mobs[1] >= cap) {
                            return true;
                        }
                    }
                }
                else if (entity instanceof Monster) {
                    Flag monsterFlag = FlagManager.getPlotFlag(plot, "hostile-cap");
                    if (monsterFlag != null) {
                        int cap = ((Integer) monsterFlag.getValue());
                        if (mobs[2] >= cap) {
                            return true;
                        }
                    }
                }
            }
            else if (entity instanceof Vehicle) {
                Flag vehicleFlag = FlagManager.getPlotFlag(plot, "vehicle-cap");
                if (vehicleFlag != null) {
                    int cap = ((Integer) vehicleFlag.getValue());
                    if (mobs[4] >= cap) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockIgnite(final BlockIgniteEvent e) {
        final Player player = e.getPlayer();
        final Block b = e.getBlock();
        final Location loc;
        if (b != null) {
            loc = BukkitUtil.getLocation(b.getLocation());
        } else {
            final Entity ent = e.getIgnitingEntity();
            if (ent != null) {
                loc = BukkitUtil.getLocation(ent);
            } else {
                if (player != null) {
                    loc = BukkitUtil.getLocation(player);
                } else {
                    return;
                }
            }
        }
        
        final String world;
        if (e.getBlock() != null) {
            world = e.getBlock().getWorld().getName();
        } else if (e.getIgnitingEntity() != null) {
            world = e.getIgnitingEntity().getWorld().getName();
        } else if (e.getPlayer() != null) {
            world = e.getPlayer().getWorld().getName();
        } else {
            return;
        }
        if (!PlotSquared.isPlotWorld(world)) {
            return;
        }
        if (e.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            e.setCancelled(true);
            return;
        }
        if (player == null) {
            if (MainUtil.isPlotArea(loc)) {
                e.setCancelled(true);
            }
            return;
        }
        final Player p = e.getPlayer();
        Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            if (MainUtil.isPlotAreaAbs(loc)) {
                final PlotPlayer pp = BukkitUtil.getPlayer(p);
                if (!Permissions.hasPermission(pp, "plots.admin.build.road")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.road");
                    e.setCancelled(true);
                    return;
                }
            }
        } else {
            if (!plot.hasOwner()) {
                final PlotPlayer pp = BukkitUtil.getPlayer(p);
                if (!Permissions.hasPermission(pp, "plots.admin.build.unowned")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.unowned");
                    e.setCancelled(true);
                    return;
                }
            } else {
                final PlotPlayer pp = BukkitUtil.getPlayer(p);
                if (!plot.isAdded(pp.getUUID())) {
                    if (!Permissions.hasPermission(pp, "plots.admin.build.other")) {
                        if (MainUtil.isPlotArea(loc)) {
                            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.other");
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(final PlayerTeleportEvent event) {
        if (event.getTo() == null || event.getFrom() == null) {
            return;
        }
        final Location f = BukkitUtil.getLocation(event.getFrom());
        final Location t = BukkitUtil.getLocation(event.getTo());
        final Location q = new Location(t.getWorld(), t.getX(), 64, t.getZ());
        final Player player = event.getPlayer();
        if (PlotSquared.isPlotWorld(q.getWorld())) {
            final Plot plot = MainUtil.getPlot(q);
            if (plot != null) {
                final PlotPlayer pp = BukkitUtil.getPlayer(player);
                if (plot.isDenied(pp.getUUID())) {
                    if (Permissions.hasPermission(pp, "plots.admin.enter.denied")) {
                        return;
                    }
                    MainUtil.sendMessage(pp, C.YOU_BE_DENIED);
                    event.setCancelled(true);
                    return;
                } else {
                    if (MainUtil.enteredPlot(f, t)) {
                        plotEntry(pp, plot);
                    }
                }
            } else {
                if (MainUtil.leftPlot(f, t)) {
                    final Plot plot2 = MainUtil.getPlot(f);
                    APlotListener.manager.plotExit(BukkitUtil.getPlayer(player), plot2);
                }
            }
            if ((q.getX() >= 29999999) || (q.getX() <= -29999999) || (q.getZ() >= 29999999) || (q.getZ() <= -29999999)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(final PlayerBucketEmptyEvent e) {
        final BlockFace bf = e.getBlockFace();
        final Block b = e.getBlockClicked().getLocation().add(bf.getModX(), bf.getModY(), bf.getModZ()).getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (PlotSquared.isPlotWorld(loc.getWorld())) {
            final PlotPlayer pp = BukkitUtil.getPlayer(e.getPlayer());
            Plot plot = MainUtil.getPlot(loc);
            if (plot == null) {
                if (MainUtil.isPlotAreaAbs(loc)) {
                    if (Permissions.hasPermission(pp, "plots.admin.build.road")) {
                        return;
                    }
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.road");
                    e.setCancelled(true);
                    return;
                }
            } else {
                if (!plot.hasOwner()) {
                    if (Permissions.hasPermission(pp, "plots.admin.build.unowned")) {
                        return;
                    }
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.unowned");
                    e.setCancelled(true);
                    return;
                } else if (!plot.isAdded(pp.getUUID())) {
                    final Flag use = FlagManager.getPlotFlag(plot, "use");
                    if ((use != null) && ((HashSet<PlotBlock>) use.getValue()).contains(new PlotBlock((short) e.getBucket().getId(), (byte) 0))) {
                        return;
                    }
                    if (Permissions.hasPermission(pp, "plots.admin.build.other")) {
                        return;
                    }
                    if (MainUtil.isPlotArea(loc)) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.other");
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(final InventoryClickEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase("PlotSquared Commands")) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority= EventPriority.MONITOR)
    public void onLeave(final PlayerQuitEvent event) {
        PlotPlayer pp = BukkitUtil.getPlayer(event.getPlayer());
        ExpireManager.dates.put(pp.getUUID(), System.currentTimeMillis());
        EventUtil.unregisterPlayer(pp);
        if (PlotSquared.worldEdit != null) {
            WEManager.bypass.remove(pp.getName());
        }
        if (Settings.DELETE_PLOTS_ON_BAN && event.getPlayer().isBanned()) {
            final Collection<Plot> plots = PlotSquared.getPlots(pp.getName()).values();
            for (final Plot plot : plots) {
                final PlotWorld plotworld = PlotSquared.getPlotWorld(plot.world);
                final PlotManager manager = PlotSquared.getPlotManager(plot.world);
                manager.clearPlot(plotworld, plot, true, null);
                DBFunc.delete(plot.world, plot);
                PlotSquared.log(String.format("&cPlot &6%s &cwas deleted + cleared due to &6%s&c getting banned", plot.getId(), event.getPlayer().getName()));
            }
        }
        BukkitUtil.removePlayer(pp.getName());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(final PlayerBucketFillEvent e) {
        final Block b = e.getBlockClicked();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (PlotSquared.isPlotWorld(loc.getWorld())) {
            final Player p = e.getPlayer();
            final PlotPlayer pp = BukkitUtil.getPlayer(p);
            Plot plot = MainUtil.getPlot(loc);
            if (plot == null) {
                if (MainUtil.isPlotAreaAbs(loc)) {
                    if (Permissions.hasPermission(pp, "plots.admin.build.road")) {
                        return;
                    }
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.road");
                    e.setCancelled(true);
                    return;
                }
            } else {
                if (!plot.hasOwner()) {
                    if (Permissions.hasPermission(pp, "plots.admin.build.unowned")) {
                        return;
                    }
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.unowned");
                    e.setCancelled(true);
                    return;
                } else if (!plot.isAdded(pp.getUUID())) {
                    final Flag use = FlagManager.getPlotFlag(plot, "use");
                    final Block block = e.getBlockClicked();
                    if ((use != null) && ((HashSet<PlotBlock>) use.getValue()).contains(new PlotBlock((short) block.getTypeId(), block.getData()))) {
                        return;
                    }
                    if (Permissions.hasPermission(pp, "plots.admin.build.other")) {
                        return;
                    }
                    if (MainUtil.isPlotArea(loc)) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.other");
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingPlace(final HangingPlaceEvent e) {
        final Block b = e.getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (PlotSquared.isPlotWorld(loc.getWorld())) {
            final Player p = e.getPlayer();
            final PlotPlayer pp = BukkitUtil.getPlayer(p);
            Plot plot = MainUtil.getPlot(loc);
            if (plot == null) {
                if (MainUtil.isPlotAreaAbs(loc)) {
                    if (!Permissions.hasPermission(pp, "plots.admin.build.road")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.road");
                        e.setCancelled(true);
                        return;
                    }
                }
            } else {
                if (!plot.hasOwner()) {
                    if (!Permissions.hasPermission(pp, "plots.admin.build.unowned")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.unowned");
                        e.setCancelled(true);
                        return;
                    }
                } else if (!plot.isAdded(pp.getUUID())) {
                    if (FlagManager.isPlotFlagTrue(plot, "hanging-place")) {
                        return;
                    }
                    if (!Permissions.hasPermission(pp, "plots.admin.build.other")) {
                        if (MainUtil.isPlotArea(loc)) {
                            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.other");
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreakByEntity(final HangingBreakByEntityEvent e) {
        final Entity r = e.getRemover();
        if (r instanceof Player) {
            final Player p = (Player) r;
            final Location l = BukkitUtil.getLocation(e.getEntity());
            final PlotPlayer pp = BukkitUtil.getPlayer(p);
            if (PlotSquared.isPlotWorld(l.getWorld())) {
                Plot plot = MainUtil.getPlot(l);
                if (plot == null) {
                    if (MainUtil.isPlotAreaAbs(l)) {
                        if (!Permissions.hasPermission(pp, "plots.admin.destroy.road")) {
                            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.destroy.road");
                            e.setCancelled(true);
                            return;
                        }
                    }
                } else {
                    if (!plot.hasOwner()) {
                        if (!Permissions.hasPermission(pp, "plots.admin.destroy.unowned")) {
                            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.destroy.unowned");
                            e.setCancelled(true);
                            return;
                        }
                    } else if (!plot.isAdded(pp.getUUID())) {
                        if (FlagManager.isPlotFlagTrue(plot, "hanging-break")) {
                            return;
                        }
                        if (!Permissions.hasPermission(pp, "plots.admin.destroy.other")) {
                            if (MainUtil.isPlotArea(l)) {
                                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.destroy.other");
                                e.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent e) {
        final Location l = BukkitUtil.getLocation(e.getRightClicked().getLocation());
        if (PlotSquared.isPlotWorld(l.getWorld())) {
            final Player p = e.getPlayer();
            final PlotPlayer pp = BukkitUtil.getPlayer(p);
            Plot plot = MainUtil.getPlot(l);
            if (plot == null) {
                if (!MainUtil.isPlotAreaAbs(l)) {
                    return;
                }
                if (!Permissions.hasPermission(pp, "plots.admin.interact.road")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.road");
                    e.setCancelled(true);
                    return;
                }
            } else {
                if (!plot.hasOwner()) {
                    if (!Permissions.hasPermission(pp, "plots.admin.interact.unowned")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.unowned");
                        e.setCancelled(true);
                        return;
                    }
                } else if (!plot.isAdded(pp.getUUID())) {
                    final Entity entity = e.getRightClicked();
                    if ((entity instanceof Monster) && FlagManager.isPlotFlagTrue(plot, "hostile-interact")) {
                        return;
                    }
                    if ((entity instanceof Animals) && FlagManager.isPlotFlagTrue(plot, "animal-interact")) {
                        return;
                    }
                    if ((entity instanceof Tameable) && ((Tameable) entity).isTamed() && FlagManager.isPlotFlagTrue(plot, "tamed-interact")) {
                        return;
                    }
                    if ((entity instanceof RideableMinecart) && FlagManager.isPlotFlagTrue(plot, "vehicle-use")) {
                        return;
                    }
                    if (!Permissions.hasPermission(pp, "plots.admin.interact.other")) {
                        if (MainUtil.isPlotArea(l)) {
                            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.other");
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVehicleCreate(final VehicleCreateEvent event) {
        Vehicle entity = event.getVehicle();
        Location loc = BukkitUtil.getLocation(entity);
        Plot plot = MainUtil.getPlot(loc);
        if (checkEntity(entity, plot)) {
            entity.remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVehicleDestroy(final VehicleDestroyEvent e) {
        final Location l = BukkitUtil.getLocation(e.getVehicle());
        if (PlotSquared.isPlotWorld(l.getWorld())) {
            final Entity d = e.getAttacker();
            if (d instanceof Player) {
                final Player p = (Player) d;
                PlotSquared.getPlotWorld(l.getWorld());
                final PlotPlayer pp = BukkitUtil.getPlayer(p);
                Plot plot = MainUtil.getPlot(l);
                if (plot == null) {
                    if (!MainUtil.isPlotAreaAbs(l)) {
                        return;
                    }
                    if (!Permissions.hasPermission(pp, "plots.admin.vehicle.break.road")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.vehicle.break.road");
                        e.setCancelled(true);
                        return;
                    }
                } else {
                    if (!plot.hasOwner()) {
                        if (!Permissions.hasPermission(pp, "plots.admin.vehicle.break.unowned")) {
                            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.vehicle.break.unowned");
                            e.setCancelled(true);
                            return;
                        }
                        return;
                    }
                    if (!plot.isAdded(pp.getUUID())) {
                        if (FlagManager.isPlotFlagTrue(plot, "vehicle-break")) {
                            return;
                        }
                        if (!Permissions.hasPermission(pp, "plots.admin.vehicle.break.other")) {
                            if (MainUtil.isPlotArea(l)) {
                                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.vehicle.break.other");
                                e.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(final EntityDamageByEntityEvent e) {
        final Location l = BukkitUtil.getLocation(e.getEntity());
        final Entity damager = e.getDamager();
        final Entity victim = e.getEntity();
        if ((Settings.TELEPORT_DELAY != 0) && (TaskManager.TELEPORT_QUEUE.size() > 0) && (victim instanceof Player)) {
            final Player player = (Player) victim;
            final String name = player.getName();
            if (TaskManager.TELEPORT_QUEUE.contains(name)) {
                TaskManager.TELEPORT_QUEUE.remove(name);
            }
        }
        if (PlotSquared.isPlotWorld(l.getWorld())) {
            Player p = null;
            Projectile projectile = null;
            if (damager instanceof Player) {
                p = (Player) damager;
            }
            else if (damager instanceof Projectile) {
                projectile = (Projectile) damager;
                //Arrow, Egg, EnderPearl, Fireball, Fish, FishHook, LargeFireball, SmallFireball, Snowball, ThrownExpBottle, ThrownPotion, WitherSkull
                if (damager instanceof Projectile) {
                    if (damager instanceof ThrownPotion) {
                        ThrownPotion potion = (ThrownPotion) damager;
                        Collection<PotionEffect> effects = potion.getEffects();
                        for (PotionEffect effect : effects) {
                            PotionEffectType type = effect.getType();
                            if (type == PotionEffectType.BLINDNESS || type == PotionEffectType.CONFUSION || type == PotionEffectType.HARM || type == PotionEffectType.INVISIBILITY  || type == PotionEffectType.POISON  || type == PotionEffectType.SLOW || type == PotionEffectType.SLOW_DIGGING || type == PotionEffectType.WEAKNESS || type == PotionEffectType.WITHER) {
                                ProjectileSource shooter = ((Projectile) damager).getShooter();
                                if (shooter == null || !(shooter instanceof Player)) {
                                    return;
                                }
                                p = (Player) shooter;
                                break;
                            }
                        }
                    }
                    else {
                        ProjectileSource shooter = projectile.getShooter();
                        if (shooter == null || !(shooter instanceof Player)) {
                            return;
                        }
                        p = (Player) shooter;
                    }
                }
                else {
                    return;
                }
            }
            if (p != null) {
                final boolean aPlr = victim instanceof Player;
                final PlotWorld pW = PlotSquared.getPlotWorld(l.getWorld());
                if (!aPlr && pW.PVE && (!(victim instanceof ItemFrame) && !(victim.getType().getTypeId() == 30))) {
                    return;
                } else if (aPlr && pW.PVP) {
                    return;
                }
                Plot plot = MainUtil.getPlot(l);
                if (plot == null) {
                    if (!MainUtil.isPlotAreaAbs(l)) {
                        return;
                    }
                    final PlotPlayer pp = BukkitUtil.getPlayer(p);
                    if (!Permissions.hasPermission(pp, "plots.admin.pve.road")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.pve.road");
                        e.setCancelled(true);
                        return;
                    }
                } else {
                    if (!plot.hasOwner()) {
                        final PlotPlayer pp = BukkitUtil.getPlayer(p);
                        if (!Permissions.hasPermission(pp, "plots.admin.pve.unowned")) {
                            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.pve.unowned");
                            if (projectile != null) {
                                projectile.remove();
                            }
                            e.setCancelled(true);
                            return;
                        }
                    } else if (aPlr && FlagManager.isBooleanFlag(plot, "pvp", false)) {
                        return;
                    }
                    if (!aPlr && FlagManager.isBooleanFlag(plot, "pve", false)) {
                        return;
                    }
                    assert plot != null;
                    final PlotPlayer pp = BukkitUtil.getPlayer(p);
                    if (!plot.isAdded(pp.getUUID())) {
                        if ((victim instanceof Monster) && FlagManager.isPlotFlagTrue(plot, "hostile-attack")) {
                            return;
                        }
                        if ((victim instanceof Animals) && FlagManager.isPlotFlagTrue(plot, "animal-attack")) {
                            return;
                        }
                        if ((victim instanceof Tameable) && ((Tameable) victim).isTamed() && FlagManager.isPlotFlagTrue(plot, "tamed-attack")) {
                            return;
                        }
                        if (!Permissions.hasPermission(pp, "plots.admin.pve.other")) {
                            if (MainUtil.isPlotArea(l)) {
                                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.pve.other");
                                if (projectile != null) {
                                    projectile.remove();
                                }
                                e.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
                return;
            }
            if ((damager instanceof Arrow) && MainUtil.isPlotArea(l) && (!(victim instanceof Creature))) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerEggThrow(final PlayerEggThrowEvent e) {
        final Location l = BukkitUtil.getLocation(e.getEgg().getLocation());
        if (PlotSquared.isPlotWorld(l.getWorld())) {
            final Player p = e.getPlayer();
            final PlotPlayer pp = BukkitUtil.getPlayer(p);
            Plot plot = MainUtil.getPlot(l);
            if (plot == null) {
                if (!MainUtil.isPlotAreaAbs(l)) {
                    return;
                }
                if (!Permissions.hasPermission(pp, "plots.admin.projectile.road")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.projectile.road");
                    e.setHatching(false);
                    return;
                }
            } else {
                if (!plot.hasOwner()) {
                    if (!Permissions.hasPermission(pp, "plots.admin.projectile.unowned")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.projectile.unowned");
                        e.setHatching(false);
                        return;
                    }
                } else if (!plot.isAdded(pp.getUUID())) {
                    if (!Permissions.hasPermission(pp, "plots.admin.projectile.other")) {
                        if (MainUtil.isPlotArea(l)) {
                            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.projectile.other");
                            e.setHatching(false);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled=true)
    public void BlockCreate(final BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final String world = player.getWorld().getName();
        if (!PlotSquared.isPlotWorld(world)) {
            return;
        }
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        final Location loc = BukkitUtil.getLocation(event.getBlock().getLocation());
        final Plot plot = MainUtil.getPlot(loc); 
        if (plot != null) {
            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, "plots.admin.build.unowned")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.unowned");
                    event.setCancelled(true);
                    return;
                }
            }
            else if (!plot.isAdded(pp.getUUID())) {
                final Flag place = FlagManager.getPlotFlag(plot, "place");
                final Block block = event.getBlock();
                if (((place == null) || !((HashSet<PlotBlock>) place.getValue()).contains(new PlotBlock((short) block.getTypeId(), block.getData()))) && !Permissions.hasPermission(pp, "plots.admin.build.other")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.other");
                    event.setCancelled(true);
                    return;
                }
            }
            if (Settings.PHYSICS_LISTENER) {
                if (FlagManager.isPlotFlagTrue(plot, "disable-physics")) {
                    Block block = event.getBlockPlaced();
                    sendBlockChange(block.getLocation(), block.getType(), block.getData());
                }
            }
            return;
        }
        else if (!Permissions.hasPermission(pp, "plots.admin.build.road")) {
            if (MainUtil.isPlotAreaAbs(loc)) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.road");
                event.setCancelled(true);
                return;
            }
        }
    }
}
