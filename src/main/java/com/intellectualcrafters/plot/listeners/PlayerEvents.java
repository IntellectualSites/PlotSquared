package com.intellectualcrafters.plot.listeners;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
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
import org.bukkit.event.inventory.InventoryCloseEvent;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.listeners.worldedit.WEManager;
import com.intellectualcrafters.plot.object.BukkitLazyBlock;
import com.intellectualcrafters.plot.object.BukkitPlayer;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotHandler;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotInventory;
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

    private boolean pistonBlocks = true;

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
                        if (16 * (Math.abs(loc.getX() - x) / 16) > distance) {
                            continue;
                        }
                        if (16 * (Math.abs(loc.getZ() - z) / 16) > distance) {
                            continue;
                        }
                        ((BukkitPlayer) player).player.sendBlockChange(bloc, type, data);
                    }
                }
            }
        }, 3);
    }

    @EventHandler
    public void onRedstoneEvent(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        Location loc = BukkitUtil.getLocation(block.getLocation());
        if (!PS.get().isPlotWorld(loc.getWorld())) {
            return;
        }
        Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return;
        }
        if (Settings.REDSTONE_DISABLER) {
            if (UUIDHandler.getPlayer(plot.owner) == null) {
                boolean disable = true;
                for (UUID trusted : plot.trusted) {
                    if (UUIDHandler.getPlayer(trusted) != null) {
                        disable = false;
                        break;
                    }
                }
                if (disable) {
                    event.setNewCurrent(0);
                    return;
                }
            }
        }
        Flag redstone = FlagManager.getPlotFlag(plot, "redstone");
        if (redstone == null || (Boolean) redstone.getValue()) {
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
            default: {
                event.setNewCurrent(0);
            }
        }
    }
    
    @EventHandler
    public void onPhysicsEvent(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        Location loc = BukkitUtil.getLocation(block.getLocation());
        if (!PS.get().isPlotWorld(loc.getWorld())) {
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
            default: {
                break;
            }
        }
        if (block.getType().hasGravity()) {
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
        Projectile entity = event.getEntity();
        Location loc = BukkitUtil.getLocation(entity);
        if (!PS.get().isPlotWorld(loc.getWorld())) {
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
        final String message = event.getMessage().toLowerCase().replaceAll("/", "");
        String[] split = message.split(" ");
        PluginCommand cmd = Bukkit.getServer().getPluginCommand(split[0]);
        if (cmd != null) {
            return;
        }
        if (split[0].equals("plotme") || split[0].equals("ap")) {
            final Player player = event.getPlayer();
            if (Settings.USE_PLOTME_ALIAS) {
                player.performCommand("plots " + StringUtils.join(Arrays.copyOfRange(split, 1, split.length), " "));
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
        if (PS.get().worldEdit != null) {
            if (Permissions.hasPermission(pp, "plots.worldedit.bypass")) {
                WEManager.bypass.add(pp.getName());
            }
        }
        if (PS.get().update != null && pp.hasPermission("plots.admin")) {
            MainUtil.sendMessage(pp, "&6An update for PlotSquared is available: &7/plot update");
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
        org.bukkit.Location from = event.getFrom();
        org.bukkit.Location to = event.getTo();
        int x2;
        if (from.getX() != (x2 = (int) to.getX()) ) {
            String worldname = to.getWorld().getName();
            PlotWorld plotworld = PS.get().getPlotWorld(worldname);
            if (plotworld == null) {
                return;
            }
            PlotManager plotManager = PS.get().getPlotManager(worldname);
            PlotId id = plotManager.getPlotId(plotworld, x2, 0, (int) to.getZ());
            Player player = event.getPlayer();
            PlotPlayer pp = BukkitUtil.getPlayer(player);
            PlotId lastId = (PlotId) pp.getMeta("lastplotid");
            if (id == null) {
                if (lastId == null) {
                    return;
                }
                Plot plot = MainUtil.getPlot(worldname, lastId);
                if (!plotExit(pp, plot)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.exit.denied");
                    if (plot.equals(MainUtil.getPlot(BukkitUtil.getLocation(from)))) {
                        player.teleport(from);
                    }
                    else {
                        player.teleport(player.getWorld().getSpawnLocation());
                    }
                    event.setCancelled(true);
                    return;
                }
            }
            else if (id.equals(lastId)) {
                    return;
            }
            else {
                Plot plot = MainUtil.getPlot(worldname, id);
                if (!plotEntry(pp, plot)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.entry.denied");
                    if (!plot.equals(MainUtil.getPlot(BukkitUtil.getLocation(from)))) {
                        player.teleport(from);
                    }
                    else {
                        player.teleport(player.getWorld().getSpawnLocation());
                    }
                    event.setCancelled(true);
                    return;
                }
            }
            Integer border = MainUtil.worldBorder.get(worldname);
            if (border != null) {
                if (x2 > border) {
                    to.setX(border - 4);
                    player.teleport(event.getTo());
                    MainUtil.sendMessage(pp, C.BORDER);
                    return;
                }
                else if (x2 < -border) {
                    to.setX(-border + 4);
                    player.teleport(event.getTo());
                    MainUtil.sendMessage(pp, C.BORDER);
                    return;
                }
            }
            return;
        }
        int z2;
        if ((int) from.getZ() != (z2 = (int) to.getZ()) ) {
            String worldname = to.getWorld().getName();
            PlotWorld plotworld = PS.get().getPlotWorld(worldname);
            if (plotworld == null) {
                return;
            }
            PlotManager plotManager = PS.get().getPlotManager(worldname);
            PlotId id = plotManager.getPlotId(plotworld, x2, 0, z2);
            Player player = event.getPlayer();
            PlotPlayer pp = BukkitUtil.getPlayer(player);
            PlotId lastId = (PlotId) pp.getMeta("lastplotid");
            if (id == null) {
                if (lastId == null) {
                    return;
                }
                Plot plot = MainUtil.getPlot(worldname, lastId);
                if (!plotExit(pp, plot)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.exit.denied");
                    if (plot.equals(MainUtil.getPlot(BukkitUtil.getLocation(from)))) {
                        player.teleport(from);
                    }
                    else {
                        player.teleport(player.getWorld().getSpawnLocation());
                    }
                    event.setCancelled(true);
                    return;
                }
            }
            else if (id.equals(lastId)) {
                    return;
            }
            else {
                Plot plot = MainUtil.getPlot(worldname, id);
                if (!plotEntry(pp, plot)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.entry.denied");
                    if (!plot.equals(MainUtil.getPlot(BukkitUtil.getLocation(from)))) {
                        player.teleport(from);
                    }
                    else {
                        player.teleport(player.getWorld().getSpawnLocation());
                    }
                    event.setCancelled(true);
                    return;
                }
            }
            Integer border = MainUtil.worldBorder.get(worldname);
            if (border != null) {
                if (z2 > border) {
                    to.setZ(border - 4);
                    player.teleport(event.getTo());
                    MainUtil.sendMessage(pp, C.BORDER);
                    return;
                }
                else if (z2 < -border) {
                    to.setZ(-border + 4);
                    player.teleport(event.getTo());
                    MainUtil.sendMessage(pp, C.BORDER);
                    return;
                }
            }
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final String world = player.getWorld().getName();
        if (!PS.get().isPlotWorld(world)) {
            return;
        }
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
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
            if (p.hasPermission("plots.admin.command.chat") || plot.equals(MainUtil.getPlot(BukkitUtil.getLocation(p)))) {
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
        if (!PS.get().isPlotWorld(world)) {
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
        if (!PS.get().isPlotWorld(world)) {
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
        if (PS.get().worldEdit != null) {
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
        if (!PS.get().isPlotWorld(world)) {
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
        if (!PS.get().isPlotWorld(world)) {
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
        if (PS.get().isPlotWorld(loc.getWorld())) {
            if (MainUtil.isPlotRoad(loc)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBF(final BlockFormEvent e) {
        final Block b = e.getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (PS.get().isPlotWorld(loc.getWorld())) {
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
            if (PS.get().isPlotWorld(loc.getWorld())) {
                if (MainUtil.isPlotRoad(loc)) {
                    event.setCancelled(true);
                }
            }
        }
        final String world = player.getWorld().getName();
        if (!PS.get().isPlotWorld(world)) {
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
        if (PS.get().isPlotWorld(loc.getWorld())) {
            if (MainUtil.isPlotRoad(loc)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChange(final BlockFromToEvent e) {
        final Block b = e.getToBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (PS.get().isPlotWorld(loc.getWorld())) {
            if (MainUtil.isPlotRoad(loc)) {
                e.setCancelled(true);
            }
            else {
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
        if (PS.get().isPlotWorld(loc.getWorld())) {
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
        if (!PS.get().isPlotWorld(world)) {
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
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonRetract(final BlockPistonRetractEvent event) {
        final Block block = event.getBlock();
        Location loc = BukkitUtil.getLocation(block.getLocation());
        String world = loc.getWorld();
        if (!PS.get().isPlotWorld(world)) {
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
        if (!PS.get().isPlotWorld(e.getWorld().getName())) {
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
        if (!PS.get().isPlotWorld(world)) {
            return;
        }
        Location loc = BukkitUtil.getLocation(block.getLocation());
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        PlayerBlockEventType eventType = null;
        BukkitLazyBlock lb;
        switch (action) {
            case PHYSICAL: {
                eventType = PlayerBlockEventType.TRIGGER_PHYSICAL;
                lb = new BukkitLazyBlock(block);
                break;
            }
            case RIGHT_CLICK_BLOCK: {
                Material blockType = block.getType();
                int blockId = blockType.getId();
                switch (blockType) {
                    case ANVIL:
                    case ACACIA_DOOR:
                    case BIRCH_DOOR:
                    case DARK_OAK_DOOR:
                    case IRON_DOOR:
                    case JUNGLE_DOOR:
                    case SPRUCE_DOOR:
                    case TRAP_DOOR:
                    case IRON_TRAPDOOR:
                    case WOOD_DOOR:
                    case WOODEN_DOOR:
                    case TRAPPED_CHEST:
                    case ENDER_CHEST:
                    case CHEST:
                    case ACACIA_FENCE_GATE:
                    case BIRCH_FENCE_GATE:
                    case DARK_OAK_FENCE_GATE:
                    case FENCE_GATE:
                    case JUNGLE_FENCE_GATE:
                    case SPRUCE_FENCE_GATE:
                    case LEVER:
                    case DIODE:
                    case DIODE_BLOCK_OFF:
                    case DIODE_BLOCK_ON:
                    case COMMAND:
                    case REDSTONE_COMPARATOR:
                    case REDSTONE_COMPARATOR_OFF:
                    case REDSTONE_COMPARATOR_ON:
                    case REDSTONE_ORE:
                    case WOOD_BUTTON:
                    case STONE_BUTTON:
                    case BEACON:
                    case BED_BLOCK:
                    case SIGN:
                    case SIGN_POST:
                    case ENCHANTMENT_TABLE:
                    case BREWING_STAND:
                    case STANDING_BANNER:
                    case BURNING_FURNACE:
                    case FURNACE:
                    case CAKE_BLOCK:
                    case DISPENSER:
                    case DROPPER:
                    case HOPPER:
                    case NOTE_BLOCK:
                    case JUKEBOX:
                    case WORKBENCH: {
                        eventType = PlayerBlockEventType.INTERACT_BLOCK;
                        break;
                    }
                    case DRAGON_EGG: {
                        eventType = PlayerBlockEventType.TELEPORT_OBJECT;
                        break;
                    }
                    default: {
                        break;
                    }
                }
                lb = new BukkitLazyBlock(blockId, block);
                ItemStack hand = player.getItemInHand();
                if (eventType != null && !player.isSneaking()) {
                    break;
                }
                if (hand == null) {
                    eventType = PlayerBlockEventType.INTERACT_BLOCK;
                    lb = new BukkitLazyBlock(block);
                    break;
                }
                // TODO calls both:
                // redstone ore
                
                lb = new BukkitLazyBlock(new PlotBlock((short) hand.getTypeId(), (byte) hand.getDurability()));
                switch (hand.getType()) {
                    case MONSTER_EGG:
                    case MONSTER_EGGS: {
                        eventType = PlayerBlockEventType.SPAWN_MOB;
                        break;
                    }
                    
                    case ARMOR_STAND: {
                        eventType = PlayerBlockEventType.PLACE_MISC;
                        break;
                    }
                    
                    case WRITTEN_BOOK:
                    case BOOK_AND_QUILL:
                    case BOOK: {
                        eventType = PlayerBlockEventType.READ;
                        break;
                    }
                    
                    case APPLE:
                    case BAKED_POTATO:
                    case MUSHROOM_SOUP:
                    case BREAD:
                    case CARROT:
                    case CARROT_ITEM:
                    case COOKIE:
                    case GRILLED_PORK:
                    case POISONOUS_POTATO:
                    case MUTTON:
                    case PORK:
                    case POTATO:
                    case POTATO_ITEM:
                    case POTION:
                    case PUMPKIN_PIE:
                    case RABBIT:
                    case RABBIT_FOOT:
                    case RABBIT_STEW:
                    case RAW_BEEF:
                    case RAW_FISH:
                    case RAW_CHICKEN: {
                        eventType = PlayerBlockEventType.EAT;
                        break;
                    }
                    
                    case MINECART :
                    case STORAGE_MINECART :
                    case POWERED_MINECART :
                    case HOPPER_MINECART :
                    case EXPLOSIVE_MINECART:
                    case COMMAND_MINECART:
                    case BOAT: {
                        eventType = PlayerBlockEventType.PLACE_VEHICLE;
                        break;
                    }
                    case PAINTING:
                    case ITEM_FRAME: {
                        eventType = PlayerBlockEventType.PLACE_HANGING;
                        break;
                    }
                    default: {
                        eventType = PlayerBlockEventType.INTERACT_BLOCK;
                        break;
                    }
                }
                break;
            }
            case LEFT_CLICK_BLOCK: {
                eventType = PlayerBlockEventType.BREAK_BLOCK;
                lb = new BukkitLazyBlock(block);
                break;
            }
            default: {
                return;
            }
        }
        if (!EventUtil.manager.checkPlayerBlockEvent(pp, eventType, loc, lb, true)) {
            event.setCancelled(true);
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
        if (!PS.get().isPlotWorld(world)) {
            return;
        }
        if (!MainUtil.isPlotArea(loc)) {
            return;
        }
        final PlotWorld pW = PS.get().getPlotWorld(world);
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
        if (event.getEntityType() != EntityType.FALLING_BLOCK) {
            return;
        }
        Block block = event.getBlock();
        World world = block.getWorld();
        String worldname = world.getName();
        if (!PS.get().isPlotWorld(worldname)) {
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
            int[] mobs = null;
            if (entityFlag != null) {
                int cap = ((Integer) entityFlag.getValue());
                if (mobs == null) mobs = ChunkManager.manager.countEntities(plot);
                if (mobs[0] >= cap) {
                    return true;
                }
            }
            if (entity instanceof Creature) {
                Flag mobFlag = FlagManager.getPlotFlag(plot, "mob-cap");
                if (mobFlag != null) {
                    int cap = ((Integer) mobFlag.getValue());
                    if (mobs == null) mobs = ChunkManager.manager.countEntities(plot);
                    if (mobs[3] >= cap) {
                        return true;
                    }
                }
                if (entity instanceof Animals) {
                    Flag animalFlag = FlagManager.getPlotFlag(plot, "animal-cap");
                    if (animalFlag != null) {
                        int cap = ((Integer) animalFlag.getValue());
                        if (mobs == null) mobs = ChunkManager.manager.countEntities(plot);
                        if (mobs[1] >= cap) {
                            return true;
                        }
                    }
                }
                else if (entity instanceof Monster) {
                    Flag monsterFlag = FlagManager.getPlotFlag(plot, "hostile-cap");
                    if (monsterFlag != null) {
                        int cap = ((Integer) monsterFlag.getValue());
                        if (mobs == null) mobs = ChunkManager.manager.countEntities(plot);
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
                    if (mobs == null) mobs = ChunkManager.manager.countEntities(plot);
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
        if (!PS.get().isPlotWorld(world)) {
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
        if (PS.get().isPlotWorld(q.getWorld())) {
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
                    plotExit(BukkitUtil.getPlayer(player), plot2);
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
        if (PS.get().isPlotWorld(loc.getWorld())) {
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
        HumanEntity clicker = event.getWhoClicked();
        if (!(clicker instanceof Player) ) {
            return;
        }
        Player player = (Player) clicker;
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        PlotInventory inv = (PlotInventory) pp.getMeta("inventory");
        if (inv != null && event.getRawSlot() == event.getSlot()) {
            if (!inv.onClick(event.getSlot())) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(final InventoryCloseEvent event) {
        HumanEntity closer = event.getPlayer();
        if (!(closer instanceof Player)) {
            return;
        }
        Player player = (Player) closer;
        BukkitUtil.getPlayer(player).deleteMeta("inventory");
    }
    
    
    @EventHandler(priority= EventPriority.MONITOR)
    public void onLeave(final PlayerQuitEvent event) {
        PlotPlayer pp = BukkitUtil.getPlayer(event.getPlayer());
        ExpireManager.dates.put(pp.getUUID(), System.currentTimeMillis());
        EventUtil.unregisterPlayer(pp);
        if (PS.get().worldEdit != null) {
            WEManager.bypass.remove(pp.getName());
        }
        if (Settings.DELETE_PLOTS_ON_BAN && event.getPlayer().isBanned()) {
            final Collection<Plot> plots = PS.get().getPlots(pp.getName()).values();
            for (final Plot plot : plots) {
                plot.delete();
                PS.log(String.format("&cPlot &6%s &cwas deleted + cleared due to &6%s&c getting banned", plot.getId(), event.getPlayer().getName()));
            }
        }
        BukkitUtil.removePlayer(pp.getName());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(final PlayerBucketFillEvent e) {
        final Block b = e.getBlockClicked();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (PS.get().isPlotWorld(loc.getWorld())) {
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
    public void onVehicleCreate(final VehicleCreateEvent event) {
        Vehicle entity = event.getVehicle();
        Location loc = BukkitUtil.getLocation(entity);
        Plot plot = MainUtil.getPlot(loc);
        if (checkEntity(entity, plot)) {
            entity.remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingPlace(final HangingPlaceEvent e) {
        final Block b = e.getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (PS.get().isPlotWorld(loc.getWorld())) {
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
            if (PS.get().isPlotWorld(l.getWorld())) {
                Plot plot = MainUtil.getPlot(l);
                if (plot == null) {
                    if (MainUtil.isPlotAreaAbs(l)) {
                        if (!Permissions.hasPermission(pp, "plots.admin.destroy.road")) {
                            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.destroy.road");
                            e.setCancelled(true);
                        }
                    }
                } else {
                    if (!plot.hasOwner()) {
                        if (!Permissions.hasPermission(pp, "plots.admin.destroy.unowned")) {
                            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.destroy.unowned");
                            e.setCancelled(true);
                        }
                    } else if (!plot.isAdded(pp.getUUID())) {
                        if (FlagManager.isPlotFlagTrue(plot, "hanging-break")) {
                            return;
                        }
                        if (!Permissions.hasPermission(pp, "plots.admin.destroy.other")) {
                            if (MainUtil.isPlotArea(l)) {
                                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.destroy.other");
                                e.setCancelled(true);
                            }
                        }
                    }
                }
            }
        } else if (r instanceof Projectile) {
            Projectile p = (Projectile) r;
            if (p.getShooter() instanceof Player) {
                Player shooter = (Player) p.getShooter();
                if (PS.get().isPlotWorld(BukkitUtil.getLocation(e.getEntity()).getWorld())) {
                    PlotPlayer player = BukkitUtil.getPlayer(shooter);
                    Plot plot = MainUtil.getPlot(BukkitUtil.getLocation(e.getEntity()));
                    if (plot != null) {
                        if (!plot.hasOwner()) {
                            if (!Permissions.hasPermission(player, "plots.admin.destroy.unowned")) {
                                MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.admin.destroy.unowned");
                                e.setCancelled(true);
                            }
                        } else if (!plot.isAdded(player.getUUID())) {
                            if (!FlagManager.isPlotFlagTrue(plot, "hanging-break")){
                                if (!Permissions.hasPermission(player, "plots.admin.destroy.other")) {
                                    if (MainUtil.isPlotArea(BukkitUtil.getLocation(e.getEntity()))) {
                                        MainUtil.sendMessage(player, C.NO_PERMISSION, "plots.admin.destroy.other");
                                        e.setCancelled(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent e) {
        final Location l = BukkitUtil.getLocation(e.getRightClicked().getLocation());
        if (PS.get().isPlotWorld(l.getWorld())) {
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
    public void onVehicleDestroy(final VehicleDestroyEvent e) {
        final Location l = BukkitUtil.getLocation(e.getVehicle());
        if (PS.get().isPlotWorld(l.getWorld())) {
            final Entity d = e.getAttacker();
            if (d instanceof Player) {
                final Player p = (Player) d;
                PS.get().getPlotWorld(l.getWorld());
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
        if (!PS.get().isPlotWorld(l.getWorld())) {
            return;
        }
        final Entity damager = e.getDamager();
        final Entity victim = e.getEntity();
        
        Location dloc = BukkitUtil.getLocation(damager);
        Location vloc = BukkitUtil.getLocation(victim);
        
        Plot dplot = MainUtil.getPlot(dloc);
        Plot vplot = MainUtil.getPlot(vloc);
        
        Plot plot;
        String stub;
        if (dplot == null && vplot == null) {
            if (!MainUtil.isPlotAreaAbs(dloc)) {
                return;
            }
            plot = null;
            stub = "road";
        }
        else {
            // Priorize plots for close to seamless pvp zones
            plot = vplot == null ? dplot : ((dplot == null || victim instanceof Player) ? vplot : (victim.getTicksLived() > damager.getTicksLived() ? dplot : vplot));
            stub = plot.hasOwner() ? "other" : "unowned";
        }
        
        Player player;
        if (damager instanceof Player) { // attacker is player
            player = (Player) damager;
        }
        else if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            ProjectileSource shooter = projectile.getShooter();
            if (shooter.getClass() == Player.class) { // shooter is player
                player = (Player) shooter;
            }
            else { // shooter is not player
                player = null;
            }
        }
        else { // Attacker is not player
            player = null;
        }
        
        if (player != null) {
            PlotPlayer pp = BukkitUtil.getPlayer(player);
            if (victim instanceof Hanging) { // hanging
                if (plot != null && (FlagManager.isPlotFlagTrue(plot, "hanging-break") || FlagManager.isPlotFlagTrue(plot, "pve") || plot.isAdded(pp.getUUID()))) {
                    return;
                }
                if (!Permissions.hasPermission(pp, "plots.admin.break." + stub)) {
                    e.setCancelled(true);
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.break." + stub);
                    return;
                }
            }
            else if (victim instanceof ArmorStand) {
                if (plot != null && (FlagManager.isPlotFlagTrue(plot, "misc-break") || FlagManager.isPlotFlagTrue(plot, "pve") || plot.isAdded(pp.getUUID()))) {
                    return;
                }
                if (!Permissions.hasPermission(pp, "plots.admin.break." + stub)) {
                    e.setCancelled(true);
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.break." + stub);
                    return;
                }
            }
            else if (victim instanceof Monster) { // victim is monster
                if (plot != null && (FlagManager.isPlotFlagTrue(plot, "hostile-break") || FlagManager.isPlotFlagTrue(plot, "pve") || plot.isAdded(pp.getUUID()))) {
                    return;
                }
                if (!Permissions.hasPermission(pp, "plots.admin.pve." + stub)) {
                    e.setCancelled(true);
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.pve." + stub);
                    return;
                }
            }
            else if (victim instanceof Tameable) { // victim is tameable
                if (plot != null && (FlagManager.isPlotFlagTrue(plot, "tamed-break") || FlagManager.isPlotFlagTrue(plot, "pve") || plot.isAdded(pp.getUUID()))) {
                    return;
                }
                if (!Permissions.hasPermission(pp, "plots.admin.pve." + stub)) {
                    e.setCancelled(true);
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.pve." + stub);
                    return;
                }
            }
            else if (victim instanceof Animals) { // victim is animal
                if (plot != null && (FlagManager.isPlotFlagTrue(plot, "animal-break") || FlagManager.isPlotFlagTrue(plot, "pve") || plot.isAdded(pp.getUUID()))) {
                    return;
                }
                if (!Permissions.hasPermission(pp, "plots.admin.pve." + stub)) {
                    e.setCancelled(true);
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.pve." + stub);
                    return;
                }
            }
            else if (victim instanceof Player) {
                if (plot != null) {
                    Flag pvp = FlagManager.getPlotFlag(plot, "pvp");
                    if (pvp == null) {
                        if (victim.getTicksLived() > damager.getTicksLived() ? plot.isAdded(BukkitUtil.getPlayer((Player) victim).getUUID()) : plot.isAdded(pp.getUUID())) {
                            return;
                        }
                    } else {
                        if ((Boolean) pvp.getValue()) {
                            return;
                        }
                    }
                }
                if (!Permissions.hasPermission(pp, "plots.admin.pvp." + stub)) {
                    e.setCancelled(true);
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.pvp." + stub);
                    return;
                }
            }
            else if (victim instanceof Vehicle) { // Vehicles are managed in vehicle destroy event
                return;
            }
            else { // victim is something else
                if (!Permissions.hasPermission(pp, "plots.admin.pvp." + stub)) {
                    e.setCancelled(true);
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.pvp." + stub);
                    return;
                }
                if (plot != null && FlagManager.isPlotFlagTrue(plot, "pvp")) {
                    return;
                }
            }
            return;
        }
        // player is null
        if ((damager instanceof Arrow) && (!(victim instanceof Creature))) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerEggThrow(final PlayerEggThrowEvent e) {
        final Location l = BukkitUtil.getLocation(e.getEgg().getLocation());
        if (PS.get().isPlotWorld(l.getWorld())) {
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
        if (!PS.get().isPlotWorld(world)) {
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
            if (FlagManager.isPlotFlagTrue(plot, "disable-physics")) {
                Block block = event.getBlockPlaced();
                sendBlockChange(block.getLocation(), block.getType(), block.getData());
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
