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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
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
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
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
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.BukkitPlayer;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.CmdConfirm;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SetupUtils;
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
    public void PlayerCommand(final PlayerCommandPreprocessEvent event) {
        final String message = event.getMessage();
        if (message.toLowerCase().startsWith("/plotme")) {
            final Plugin plotme = Bukkit.getPluginManager().getPlugin("PlotMe");
            if (plotme == null) {
                final Player player = event.getPlayer();
                if (Settings.USE_PLOTME_ALIAS) {
                    player.performCommand(message.replace("/plotme", "plots"));
                } else {
                    MainUtil.sendMessage(BukkitUtil.getPlayer(player), C.NOT_USING_PLOTME);
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onChunkLoad(final ChunkLoadEvent event) {
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            player.saveData();
        }
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        final String username = pp.getName();
        final StringWrapper name = new StringWrapper(username);
        final UUID uuid = pp.getUUID();
        UUIDHandler.add(name, uuid);
        final Location loc = BukkitUtil.getLocation(player.getLocation());
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return;
        }
        if (Settings.TELEPORT_ON_LOGIN) {
            BukkitUtil.teleportPlayer(player, MainUtil.getPlotHomeDefault(plot));
            MainUtil.sendMessage(pp, C.TELEPORTED_TO_ROAD);
        }
        plotEntry(player, plot);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void PlayerMove(final PlayerMoveEvent event) {
        final Location f = BukkitUtil.getLocation(event.getFrom());
        final Location t = BukkitUtil.getLocation(event.getTo());
        if ((f.getX() != t.getX()) || (f.getZ() != t.getZ())) {
            final Player player = event.getPlayer();
            if (Settings.TELEPORT_DELAY != 0) {
                TaskManager.TELEPORT_QUEUE.remove(player.getName());
            }
            final String worldname = t.getWorld();
            if (!isPlotWorld(worldname)) {
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
            Plot plot = getCurrentPlot(t);
            if (plot != null) {
                if (plot.denied.size() > 0) {
                    final PlotPlayer pp = BukkitUtil.getPlayer(player);
                    if (plot.isDenied(pp.getUUID())) {
                        if (!Permissions.hasPermission(pp, "plots.admin.entry.denied")) {
                            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.entry.denied");
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
                if (!plot.equals(getCurrentPlot(f))) {
                    plotEntry(player, plot);
                }
            } else if (leftPlot(f, t)) {
                plot = getCurrentPlot(f);
                plotExit(player, plot);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final String world = player.getWorld().getName();
        if (!isPlotWorld(world)) {
            return;
        }
        final PlotWorld plotworld = PlotSquared.getPlotWorld(world);
        if (!plotworld.PLOT_CHAT) {
            return;
        }
        final Location loc = BukkitUtil.getLocation(player);
        final Plot plot = getCurrentPlot(loc);
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
            if (getCurrentPlot(BukkitUtil.getLocation(p)) == plot) {
                recipients.add(p);
            }
        }
        format = format.replaceAll("%plot_id%", id.x + ";" + id.y).replaceAll("%sender%", sender).replaceAll("%msg%", message);
        format = ChatColor.translateAlternateColorCodes('&', format);
        event.setFormat(format);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public static void BlockDestroy(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final String world = player.getWorld().getName();
        if (!isPlotWorld(world)) {
            return;
        }
        final Location loc = BukkitUtil.getLocation(event.getBlock().getLocation());
        final Plot plot = getCurrentPlot(loc);
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
        if (isPlotArea(loc)) {
            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.destroy.road");
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onBigBoom(final EntityExplodeEvent event) {
        Location loc = BukkitUtil.getLocation(event.getLocation());
        final String world = loc.getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        final Plot plot = getCurrentPlot(loc);
        if ((plot != null) && plot.hasOwner()) {
            if (FlagManager.isPlotFlagTrue(plot, "explosion")) {
                final Iterator<Block> iter = event.blockList().iterator();
                while (iter.hasNext()) {
                    final Block b = iter.next();
                    if (!plot.equals(getCurrentPlot(BukkitUtil.getLocation(b.getLocation())))) {
                        iter.remove();
                    }
                }
                return;
            }
        }
        if (isPlotArea(loc)) {
            event.setCancelled(true);
        } else {
            final Iterator<Block> iter = event.blockList().iterator();
            while (iter.hasNext()) {
                iter.next();
                if (isPlotArea(loc)) {
                    iter.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onWorldChanged(final PlayerChangedWorldEvent event) {
        final PlotPlayer player = BukkitUtil.getPlayer(event.getPlayer());
        ((BukkitPlayer) player).hasPerm = new HashSet<>();
        ((BukkitPlayer) player).noPerm = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onPeskyMobsChangeTheWorldLikeWTFEvent(final EntityChangeBlockEvent event) {
        final String world = event.getBlock().getWorld().getName();
        if (!isPlotWorld(world)) {
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
            if (!isInPlot(loc)) {
                final PlotPlayer pp = BukkitUtil.getPlayer(p);
                if (!Permissions.hasPermission(pp, "plots.admin.build.road")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.road");
                    event.setCancelled(true);
                    return;
                }
            } else {
                final Plot plot = getCurrentPlot(loc);
                if ((plot == null) || !plot.hasOwner()) {
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
                            if (isPlotArea(loc)) {
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
    public static void onEntityBlockForm(final EntityBlockFormEvent e) {
        final String world = e.getBlock().getWorld().getName();
        if (!isPlotWorld(world)) {
            return;
        }
        if ((!(e.getEntity() instanceof Player))) {
            if (isPlotArea(BukkitUtil.getLocation(e.getBlock().getLocation()))) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBS(final BlockSpreadEvent e) {
        final Block b = e.getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (isPlotWorld(loc)) {
            if (!isInPlot(loc)) {
                if (isPlotArea(BukkitUtil.getLocation(e.getBlock().getLocation()))) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBF(final BlockFormEvent e) {
        final Block b = e.getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (isPlotWorld(loc)) {
            if (!isInPlot(loc)) {
                if (isPlotArea(BukkitUtil.getLocation(e.getBlock().getLocation()))) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBD(final BlockDamageEvent event) {
        final Player player = event.getPlayer();
        if (player == null) {
            final Location loc = BukkitUtil.getLocation(event.getBlock().getLocation());
            if (isPlotWorld(loc)) {
                if (!isInPlot(loc)) {
                    if (isPlotArea(loc)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
        final String world = player.getWorld().getName();
        if (!isPlotWorld(world)) {
            return;
        }
        final Location loc = BukkitUtil.getLocation(event.getBlock().getLocation());
        final Plot plot = getCurrentPlot(loc);
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
        if (isPlotArea(loc)) {
            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.destroy.road");
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onFade(final BlockFadeEvent e) {
        final Block b = e.getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (isPlotWorld(loc)) {
            if (!isInPlot(loc)) {
                if (isPlotArea(BukkitUtil.getLocation(e.getBlock().getLocation()))) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onChange(final BlockFromToEvent e) {
        final Block b = e.getToBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (isPlotWorld(loc)) {
            if (!isInPlot(loc)) {
                if (isPlotArea(BukkitUtil.getLocation(e.getBlock().getLocation()))) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onGrow(final BlockGrowEvent e) {
        final Block b = e.getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (isPlotWorld(loc)) {
            if (!isInPlot(loc)) {
                if (isPlotArea(BukkitUtil.getLocation(e.getBlock().getLocation()))) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public static void onBlockPistonExtend(final BlockPistonExtendEvent event) {
        final Block block = event.getBlock();
        Location loc = BukkitUtil.getLocation(block.getLocation());
        String world = loc.getWorld();
        if (!PlotSquared.isPlotWorld(world)) {
            return;
        }
        Plot plot = MainUtil.getPlot(loc);
        if (plot != null) {
            BlockFace face = event.getDirection();
            Vector relative = new Vector(face.getModX(), face.getModY(), face.getModZ());
            for (final Block b : event.getBlocks()) {
                Location bloc = BukkitUtil.getLocation(b.getLocation().add(relative));
                Plot newPlot = MainUtil.getPlot(bloc);
                if (!plot.equals(newPlot)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public static void onBlockPistonRetract(final BlockPistonRetractEvent event) {
        final Block block = event.getBlock();
        Location loc = BukkitUtil.getLocation(block.getLocation());
        String world = loc.getWorld();
        if (!PlotSquared.isPlotWorld(world)) {
            return;
        }
        if (block.getType() != Material.PISTON_STICKY_BASE && block.getType() != Material.PISTON_MOVING_PIECE) {
            return;
        }
        Plot plot = MainUtil.getPlot(loc);
        if (plot != null) {
            BlockFace dir = event.getDirection();
            Location bloc = BukkitUtil.getLocation(block.getLocation().subtract(dir.getModX() * 2, dir.getModY() * 2, dir.getModZ() * 2));
            Plot newPlot = MainUtil.getPlot(bloc);
            if (!plot.equals(newPlot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onStructureGrow(final StructureGrowEvent e) {
        if (!isPlotWorld(e.getWorld().getName())) {
            return;
        }
        final List<BlockState> blocks = e.getBlocks();
        for (int i = blocks.size() - 1; i >= 0; i--) {
            final Location loc = BukkitUtil.getLocation(blocks.get(i).getLocation());
            final Plot plot = getCurrentPlot(loc);
            if ((plot == null) || !plot.hasOwner()) {
                e.getBlocks().remove(i);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onInteract(final PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            return;
        }
        final Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        final Player player = event.getPlayer();
        final String world = player.getWorld().getName();
        if (!isPlotWorld(world)) {
            return;
        }
        final Location loc = BukkitUtil.getLocation(event.getClickedBlock().getLocation());
        if (isInPlot(loc)) {
            final Plot plot = getCurrentPlot(loc);
            if (!plot.hasOwner()) {
                final PlotPlayer pp = BukkitUtil.getPlayer(player);
                if (Permissions.hasPermission(pp, "plots.admin.interact.unowned")) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.unowned");
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
                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.other");
                event.setCancelled(true);
                return;
            }
            return;
        }
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        if (Permissions.hasPermission(pp, "plots.admin.interact.road")) {
            return;
        }
        if (isPlotArea(loc)) {
            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.road");
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void MobSpawn(final CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }
        final Location loc = BukkitUtil.getLocation(event.getLocation());
        final String world = loc.getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        if (!isPlotArea(loc)) {
            return;
        }
        final PlotWorld pW = PlotSquared.getPlotWorld(world);
        final CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if ((reason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) && !pW.SPAWN_EGGS) {
            event.setCancelled(true);
            return;
        } else if ((reason == CreatureSpawnEvent.SpawnReason.BREEDING) && !pW.SPAWN_BREEDING) {
            event.setCancelled(true);
            return;
        } else if ((reason == CreatureSpawnEvent.SpawnReason.CUSTOM) && !pW.SPAWN_CUSTOM && !(event.getEntityType().getTypeId() == 30)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBlockIgnite(final BlockIgniteEvent e) {
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
        if (!isPlotWorld(world)) {
            return;
        }
        if (e.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            e.setCancelled(true);
            return;
        }
        if (player == null) {
            if (isPlotArea(loc)) {
                e.setCancelled(true);
            }
            return;
        }
        final Player p = e.getPlayer();
        if (!isInPlot(loc)) {
            final PlotPlayer pp = BukkitUtil.getPlayer(p);
            if (!Permissions.hasPermission(pp, "plots.admin.build.road")) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.road");
                e.setCancelled(true);
                return;
            }
        } else {
            final Plot plot = getCurrentPlot(loc);
            if ((plot == null) || !plot.hasOwner()) {
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
                        if (isPlotArea(loc)) {
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
    public static void onTeleport(final PlayerTeleportEvent event) {
        final Location f = BukkitUtil.getLocation(event.getFrom());
        final Location t = BukkitUtil.getLocation(event.getTo());
        final Location q = new Location(t.getWorld(), t.getX(), 64, t.getZ());
        final Player player = event.getPlayer();
        if (isPlotWorld(q)) {
            if (isInPlot(q)) {
                final Plot plot = getCurrentPlot(q);
                final PlotPlayer pp = BukkitUtil.getPlayer(player);
                if (plot.isDenied(pp.getUUID())) {
                    MainUtil.sendMessage(BukkitUtil.getPlayer(player), C.YOU_BE_DENIED);
                    event.setCancelled(true);
                    return;
                } else {
                    if (enteredPlot(f, t)) {
                        plotEntry(player, plot);
                    }
                }
            } else {
                if (leftPlot(f, t)) {
                    final Plot plot = getCurrentPlot(f);
                    plotExit(player, plot);
                }
            }
            if ((q.getX() >= 29999999) || (q.getX() <= -29999999) || (q.getZ() >= 29999999) || (q.getZ() <= -29999999)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBucketEmpty(final PlayerBucketEmptyEvent e) {
        final BlockFace bf = e.getBlockFace();
        final Block b = e.getBlockClicked().getLocation().add(bf.getModX(), bf.getModY(), bf.getModZ()).getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (isPlotWorld(loc)) {
            final PlotPlayer pp = BukkitUtil.getPlayer(e.getPlayer());
            if (!isInPlot(loc)) {
                if (Permissions.hasPermission(pp, "plots.admin.build.road")) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.road");
                e.setCancelled(true);
                return;
            } else {
                final Plot plot = getCurrentPlot(loc);
                if ((plot == null) || !plot.hasOwner()) {
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
                    if (isPlotArea(loc)) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.other");
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onInventoryClick(final InventoryClickEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase("PlotSquared Commands")) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public static void onLeave(final PlayerQuitEvent event) {
        final String name = event.getPlayer().getName();
        if (SetupUtils.setupMap.containsKey(name)) {
            SetupUtils.setupMap.remove(name);
        }
        CmdConfirm.removePending(name);
        BukkitUtil.removePlayer(name);
        if (Settings.DELETE_PLOTS_ON_BAN && event.getPlayer().isBanned()) {
            final Collection<Plot> plots = PlotSquared.getPlots(name).values();
            for (final Plot plot : plots) {
                final PlotWorld plotworld = PlotSquared.getPlotWorld(plot.world);
                final PlotManager manager = PlotSquared.getPlotManager(plot.world);
                manager.clearPlot(plotworld, plot, true, null);
                DBFunc.delete(plot.world, plot);
                PlotSquared.log(String.format("&cPlot &6%s &cwas deleted + cleared due to &6%s&c getting banned", plot.getId(), event.getPlayer().getName()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBucketFill(final PlayerBucketFillEvent e) {
        final Block b = e.getBlockClicked();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (isPlotWorld(loc)) {
            final Player p = e.getPlayer();
            final PlotPlayer pp = BukkitUtil.getPlayer(p);
            if (!isInPlot(loc)) {
                if (Permissions.hasPermission(pp, "plots.admin.build.road")) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.road");
                e.setCancelled(true);
                return;
            } else {
                final Plot plot = getCurrentPlot(loc);
                if ((plot == null) || !plot.hasOwner()) {
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
                    if (isPlotArea(loc)) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.other");
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onHangingPlace(final HangingPlaceEvent e) {
        final Block b = e.getBlock();
        final Location loc = BukkitUtil.getLocation(b.getLocation());
        if (isPlotWorld(loc)) {
            final Player p = e.getPlayer();
            final PlotPlayer pp = BukkitUtil.getPlayer(p);
            if (!isInPlot(loc)) {
                if (!Permissions.hasPermission(pp, "plots.admin.build.road")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.road");
                    e.setCancelled(true);
                    return;
                }
            } else {
                final Plot plot = getCurrentPlot(loc);
                if ((plot == null) || !plot.hasOwner()) {
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
                        if (isPlotArea(loc)) {
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
    public static void onHangingBreakByEntity(final HangingBreakByEntityEvent e) {
        final Entity r = e.getRemover();
        if (r instanceof Player) {
            final Player p = (Player) r;
            final Location l = BukkitUtil.getLocation(e.getEntity());
            final PlotPlayer pp = BukkitUtil.getPlayer(p);
            if (isPlotWorld(l)) {
                if (!isInPlot(l)) {
                    if (!Permissions.hasPermission(pp, "plots.admin.destroy.road")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.destroy.road");
                        e.setCancelled(true);
                        return;
                    }
                } else {
                    final Plot plot = getCurrentPlot(l);
                    if ((plot == null) || !plot.hasOwner()) {
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
                            if (isPlotArea(l)) {
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
    public static void onPlayerInteractEntity(final PlayerInteractEntityEvent e) {
        final Location l = BukkitUtil.getLocation(e.getRightClicked().getLocation());
        if (isPlotWorld(l)) {
            final Player p = e.getPlayer();
            final PlotPlayer pp = BukkitUtil.getPlayer(p);
            if (!isInPlot(l)) {
                if (!Permissions.hasPermission(pp, "plots.admin.interact.road")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.interact.road");
                    e.setCancelled(true);
                    return;
                }
            } else {
                final Plot plot = getCurrentPlot(l);
                if ((plot == null) || !plot.hasOwner()) {
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
                        if (isPlotArea(l)) {
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
    public static void onVehicleDestroy(final VehicleDestroyEvent e) {
        final Location l = BukkitUtil.getLocation(e.getVehicle());
        if (isPlotWorld(l)) {
            final Entity d = e.getAttacker();
            if (d instanceof Player) {
                final Player p = (Player) d;
                PlotSquared.getPlotWorld(l.getWorld());
                final PlotPlayer pp = BukkitUtil.getPlayer(p);
                if (!isInPlot(l)) {
                    if (!Permissions.hasPermission(pp, "plots.admin.vehicle.break.road")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.vehicle.break.road");
                        e.setCancelled(true);
                        return;
                    }
                } else {
                    final Plot plot = getCurrentPlot(l);
                    if ((plot == null) || !plot.hasOwner()) {
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
                            if (isPlotArea(l)) {
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
    public static void onEntityDamageByEntityEvent(final EntityDamageByEntityEvent e) {
        final Location l = BukkitUtil.getLocation(e.getEntity());
        final Entity d = e.getDamager();
        final Entity a = e.getEntity();
        if ((Settings.TELEPORT_DELAY != 0) && (TaskManager.TELEPORT_QUEUE.size() > 0) && (a instanceof Player)) {
            final Player player = (Player) a;
            final String name = player.getName();
            if (TaskManager.TELEPORT_QUEUE.contains(name)) {
                TaskManager.TELEPORT_QUEUE.remove(name);
            }
        }
        if (isPlotWorld(l)) {
            if (d instanceof Player) {
                final Player p = (Player) d;
                final boolean aPlr = a instanceof Player;
                final PlotWorld pW = PlotSquared.getPlotWorld(l.getWorld());
                if (!aPlr && pW.PVE && (!(a instanceof ItemFrame) && !(a.getType().getTypeId() == 30))) {
                    return;
                } else if (aPlr && pW.PVP) {
                    return;
                }
                if (!isInPlot(l)) {
                    final PlotPlayer pp = BukkitUtil.getPlayer(p);
                    if (!Permissions.hasPermission(pp, "plots.admin.pve.road")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.pve.road");
                        e.setCancelled(true);
                        return;
                    }
                } else {
                    final Plot plot = getCurrentPlot(l);
                    if ((plot == null) || !plot.hasOwner()) {
                        final PlotPlayer pp = BukkitUtil.getPlayer(p);
                        if (!Permissions.hasPermission(pp, "plots.admin.pve.unowned")) {
                            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.pve.unowned");
                            e.setCancelled(true);
                            return;
                        }
                    } else if (aPlr && booleanFlag(plot, "pvp", false)) {
                        return;
                    }
                    if (!aPlr && booleanFlag(plot, "pve", false)) {
                        return;
                    }
                    assert plot != null;
                    final PlotPlayer pp = BukkitUtil.getPlayer(p);
                    if (!plot.isAdded(pp.getUUID())) {
                        if ((a instanceof Monster) && FlagManager.isPlotFlagTrue(plot, "hostile-attack")) {
                            return;
                        }
                        if ((a instanceof Animals) && FlagManager.isPlotFlagTrue(plot, "animal-attack")) {
                            return;
                        }
                        if ((a instanceof Tameable) && ((Tameable) a).isTamed() && FlagManager.isPlotFlagTrue(plot, "tamed-attack")) {
                            return;
                        }
                        if (!Permissions.hasPermission(pp, "plots.admin.pve.other")) {
                            if (isPlotArea(l)) {
                                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.pve.other");
                                e.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
            if ((d instanceof Arrow) && isPlotArea(l) && (!(a instanceof Creature))) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onPlayerEggThrow(final PlayerEggThrowEvent e) {
        final Location l = BukkitUtil.getLocation(e.getEgg().getLocation());
        if (isPlotWorld(l)) {
            final Player p = e.getPlayer();
            final PlotPlayer pp = BukkitUtil.getPlayer(p);
            if (!isInPlot(l)) {
                if (!Permissions.hasPermission(pp, "plots.admin.projectile.road")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.projectile.road");
                    e.setHatching(false);
                    return;
                }
            } else {
                final Plot plot = getCurrentPlot(l);
                if ((plot == null) || !plot.hasOwner()) {
                    if (!Permissions.hasPermission(pp, "plots.admin.projectile.unowned")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.projectile.unowned");
                        e.setHatching(false);
                        return;
                    }
                } else if (!plot.isAdded(pp.getUUID())) {
                    if (!Permissions.hasPermission(pp, "plots.admin.projectile.other")) {
                        if (isPlotArea(l)) {
                            MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.projectile.other");
                            e.setHatching(false);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void BlockCreate(final BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final String world = player.getWorld().getName();
        if (!isPlotWorld(world)) {
            return;
        }
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
        if (Permissions.hasPermission(pp, "plots.admin")) {
            return;
        }
        final Location loc = BukkitUtil.getLocation(event.getBlock().getLocation());
        if (isInPlot(loc)) {
            final Plot plot = getCurrentPlot(loc);
            if (!plot.hasOwner()) {
                if (Permissions.hasPermission(pp, "plots.admin.build.unowned")) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.unowned");
                event.setCancelled(true);
                return;
            }
            if (!plot.isAdded(pp.getUUID())) {
                final Flag place = FlagManager.getPlotFlag(plot, "place");
                final Block block = event.getBlock();
                if ((place != null) && ((HashSet<PlotBlock>) place.getValue()).contains(new PlotBlock((short) block.getTypeId(), block.getData()))) {
                    return;
                }
                if (!Permissions.hasPermission(pp, "plots.admin.build.other")) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.other");
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }
        if (!Permissions.hasPermission(pp, "plots.admin.build.road")) {
            if (isPlotArea(loc)) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION, "plots.admin.build.road");
                event.setCancelled(true);
                return;
            }
        }
    }
}
