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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import org.bukkit.event.world.WorldInitEvent;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.commands.Setup;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotSelection;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;

/**
 * Player Events involving plots
 *
 * @author Citymonstret
 * @author Empire92
 */
public class PlayerEvents extends com.intellectualcrafters.plot.listeners.PlotListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onWorldInit(final WorldInitEvent event) {
        PlotMain.loadWorld(event.getWorld());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onChunkLoad(final ChunkLoadEvent event) {
    	String worldname = event.getWorld().getName();
    	Chunk chunk = event.getChunk();
        if (PlotHelper.worldBorder.containsKey(worldname)) {
        	int border = PlotHelper.getBorder(worldname);
        	int x = Math.abs(chunk.getX() << 4);
        	int z = Math.abs(chunk.getZ() << 4);
        	if (x > border || z > border) {
        		chunk.unload(false, true);
        	}
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onJoin(final PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            player.saveData();
        }
        
        // UUID stuff
        String username = player.getName();
        StringWrapper name = new StringWrapper(username);
        UUID uuid = UUIDHandler.getUUID(player);
        UUIDHandler.add(name, uuid);
        
        // textures(event.getPlayer());
        if (isInPlot(event.getPlayer().getLocation())) {
            if (Settings.TELEPORT_ON_LOGIN) {
                event.getPlayer().teleport(PlotHelper.getPlotHomeDefault(getPlot(event.getPlayer())));
                PlayerFunctions.sendMessage(event.getPlayer(), C.TELEPORTED_TO_ROAD);
            } else {
                plotEntry(event.getPlayer(), getCurrentPlot(event.getPlayer().getLocation()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void PlayerMove(final PlayerMoveEvent event) {
        try {
            final Player player = event.getPlayer();
            final Location f = event.getFrom();
            final Location t = event.getTo();
            final Location q = new Location(t.getWorld(), t.getBlockX(), 0, t.getZ());
            if ((f.getBlockX() != q.getBlockX()) || (f.getBlockZ() != q.getBlockZ())) {
                if (Settings.TELEPORT_DELAY != 0 && TaskManager.TELEPORT_QUEUE.size() > 0) {
                    String name = player.getName();
                    if (TaskManager.TELEPORT_QUEUE.contains(name)) {
                        TaskManager.TELEPORT_QUEUE.remove(name);
                    }
                }
                if (!isPlotWorld(player.getWorld())) {
                    return;
                }
                String worldname = q.getWorld().getName();
                if (PlotHelper.worldBorder.containsKey(worldname)) {
                	int border = PlotHelper.getBorder(worldname);
                	boolean passed = false;
                	if (t.getBlockX() > border) {
                		q.setX(border);
                		passed = true;
                	}
                	else if (t.getBlockX() < -border) {
                		q.setX(-border);
                		passed = true;
                	}
                	if (t.getBlockZ() > border) {
                		q.setZ(border);
                		passed = true;
                	}
                	else if (t.getBlockZ() < -border) {
                		q.setZ(-border);
                		passed = true;
                	}
                	if (passed) {
                	    q.setY(t.getBlockY());
                		event.setTo(q);
                		PlayerFunctions.sendMessage(player, C.BORDER);
                		return;
                	}
                }
                Plot plot = getCurrentPlot(q);
                if (plot != null) {
                    if (plot.deny_entry(player)) {
                        if (!PlotMain.hasPermission(player, "plots.admin.entry.denied")) {
                            PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.admin.entry.denied");
                            event.setCancelled(true);
                            return;
                        }
                    }
                	if (!plot.equals(getCurrentPlot(f))) {
                		plotEntry(player, plot);
                	}
                } else if (leftPlot(f, event.getTo())) {
                    plot = getCurrentPlot(f);
                    plotExit(player, plot);
                }
            }
        } catch (final Exception e) {
            // Gotta catch 'em all.
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onChat(final AsyncPlayerChatEvent event) {
        final World world = event.getPlayer().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        final PlotWorld plotworld = PlotMain.getWorldSettings(world);
        if (!plotworld.PLOT_CHAT) {
            return;
        }
        if (getCurrentPlot(event.getPlayer().getLocation()) == null) {
            return;
        }
        final String message = event.getMessage();
        String format = C.PLOT_CHAT_FORMAT.s();
        final String sender = event.getPlayer().getDisplayName();
        final Plot plot = getCurrentPlot(event.getPlayer().getLocation());
        final PlotId id = plot.id;
        final Set<Player> recipients = event.getRecipients();
        recipients.clear();
        for (final Player p : Bukkit.getOnlinePlayers()) {
            if (getCurrentPlot(p.getLocation()) == plot) {
                recipients.add(p);
            }
        }
        format = format.replaceAll("%plot_id%", id.x + ";" + id.y).replaceAll("%sender%", sender).replaceAll("%msg%", message);
        format = ChatColor.translateAlternateColorCodes('&', format);
        event.setFormat(format);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public static void BlockDestroy(final BlockBreakEvent event) {
        final World world = event.getPlayer().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        final Plot plot = getCurrentPlot(loc);
        if (plot != null) {
            if (event.getBlock().getY() == 0) {
                event.setCancelled(true);
                return;
            }
            if (!plot.hasOwner()) {
                if (PlotMain.hasPermission(player, "plots.admin.destroy.unowned")) {
                    return;
                }
                PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.admin.destroy.unowned");
                event.setCancelled(true);
                return;
            }
            if (!plot.hasRights(event.getPlayer())) {
                Flag destroy = FlagManager.getPlotFlag(plot, "break");
                Block block = event.getBlock();
                if (destroy != null && ((HashSet<PlotBlock>) destroy.getValue()).contains(new PlotBlock((short) block.getTypeId(), (byte) block.getData()))) {
                    return;
                }
                if (PlotMain.hasPermission(event.getPlayer(), "plots.admin.destroy.other")) {
                    return;
                }
                PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.admin.destroy.other");
                event.setCancelled(true);
                return;
            }
            return;
        }
        if (PlotMain.hasPermission(event.getPlayer(), "plots.admin.destroy.road")) {
            return;
        }
        if (isPlotArea(loc)) {
            PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.admin.destroy.road");
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onBigBoom(final EntityExplodeEvent event) {
        final World world = event.getLocation().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        Location loc = event.getLocation();
        final Plot plot = getCurrentPlot(loc);
        if (plot != null && plot.hasOwner()) {
            if (FlagManager.isPlotFlagTrue(plot, "explosion")) {
                Iterator<Block> iter = event.blockList().iterator();
                while (iter.hasNext()) {
                    Block b = iter.next();
                    if (!plot.equals(getCurrentPlot(b.getLocation()))) {
                        iter.remove();
                    }
                }
                return;
            }
        }
        if (isPlotArea(loc)) { event.setCancelled(true); }
        else {
            Iterator<Block> iter = event.blockList().iterator();
            while (iter.hasNext()) {
                Block b = iter.next();
                if (isPlotArea(loc)) {
                    iter.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onPeskyMobsChangeTheWorldLikeWTFEvent(final EntityChangeBlockEvent event) {
        final World world = event.getBlock().getWorld();
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
            Location loc = b.getLocation();
            if (!isInPlot(loc)) {
                if (!PlotMain.hasPermission(p, "plots.admin.build.road")) {
                    PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.build.road");
                    event.setCancelled(true);
                    return;
                }
            } else {
                final Plot plot = getCurrentPlot(loc);
                if (plot == null || !plot.hasOwner()) {
                    if (!PlotMain.hasPermission(p, "plots.admin.build.unowned")) {
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.build.unowned");
                        event.setCancelled(true);
                        return;
                    }
                } else if (!plot.hasRights(p)) {
                    if (!PlotMain.hasPermission(p, "plots.admin.build.other")) {
                        if (isPlotArea(loc)) { 
                            PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.build.other");
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onEntityBlockForm(final EntityBlockFormEvent e) {
        final World world = e.getBlock().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        if ((!(e.getEntity() instanceof Player))) {
            if (isPlotArea(e.getBlock().getLocation())) { e.setCancelled(true); }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBS(final BlockSpreadEvent e) {
        final Block b = e.getBlock();
        Location loc = b.getLocation();
        if (isPlotWorld(loc)) {
            if (!isInPlot(loc)) {
                if (isPlotArea(e.getBlock().getLocation())) { e.setCancelled(true); }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBF(final BlockFormEvent e) {
        final Block b = e.getBlock();
        Location loc = b.getLocation();
        if (isPlotWorld(loc)) {
            if (!isInPlot(loc)) {
                if (isPlotArea(e.getBlock().getLocation())) { e.setCancelled(true); }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBD(final BlockDamageEvent e) {
        final Block b = e.getBlock();
        Location loc = b.getLocation();
        if (isPlotWorld(loc)) {
            if (!isInPlot(loc)) {
                if (isPlotArea(e.getBlock().getLocation())) { e.setCancelled(true); }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onFade(final BlockFadeEvent e) {
        final Block b = e.getBlock();
        Location loc = b.getLocation();
        if (isPlotWorld(loc)) {
            if (!isInPlot(loc)) {
                if (isPlotArea(e.getBlock().getLocation())) { e.setCancelled(true); }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onChange(final BlockFromToEvent e) {
        final Block b = e.getToBlock();
        Location loc = b.getLocation();
        if (isPlotWorld(loc)) {
            if (!isInPlot(loc)) {
                if (isPlotArea(e.getBlock().getLocation())) { e.setCancelled(true); }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onGrow(final BlockGrowEvent e) {
        final Block b = e.getBlock();
        Location loc = b.getLocation();
        if (isPlotWorld(loc)) {
            if (!isInPlot(loc)) {
                if (isPlotArea(e.getBlock().getLocation())) { e.setCancelled(true); }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public static void onBlockPistonExtend(final BlockPistonExtendEvent e) {
        if (isInPlot(e.getBlock().getLocation())) {
            for (final Block block : e.getBlocks()) {
                if (!isInPlot(block.getLocation())) {
                    if (isPlotArea(e.getBlock().getLocation())) { e.setCancelled(true); }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public static void onBlockPistonRetract(final BlockPistonRetractEvent e) {
        final Block b = e.getRetractLocation().getBlock();
        Location loc = b.getLocation();
        if (isPlotWorld(loc) && (e.getBlock().getType() == Material.PISTON_STICKY_BASE)) {
            if (!isInPlot(loc)) {
                if (isPlotArea(e.getBlock().getLocation())) { e.setCancelled(true); }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onStructureGrow(final StructureGrowEvent e) {
        final List<BlockState> blocks = e.getBlocks();
        boolean remove = false;
        for (int i = blocks.size() - 1; i >= 0; i--) {
            if (remove || isPlotWorld(blocks.get(i).getLocation())) {
                remove = true;
                Location loc = blocks.get(i).getLocation();
                if (!isInPlot(loc)) {
                    if (isPlotArea(loc)) { e.getBlocks().remove(i); }
                }
            }
        }
    }
    

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onInteract(final PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        final World world = event.getPlayer().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        Player player = event.getPlayer();
        Location loc = event.getClickedBlock().getLocation();
        if (isInPlot(loc)) {
            final Plot plot = getCurrentPlot(loc);
            if (!plot.hasOwner()) {
                if (PlotMain.hasPermission(player, "plots.admin.interact.unowned")) {
                    return;
                }
                PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.admin.interact.unowned");
                event.setCancelled(true);
                return;
            }
            Flag use = FlagManager.getPlotFlag(plot, "use");
            if (use != null && ((HashSet<PlotBlock>) use.getValue()).contains(new PlotBlock((short) block.getTypeId(), block.getData()))) {
                return;
            }
            if (!plot.hasRights(player)) {
                if (PlotMain.hasPermission(player, "plots.admin.interact.other")) {
                    return;
                }
                PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.admin.interact.other");
                event.setCancelled(true);
                return;
            }
            return;
        }
        if (PlotMain.hasPermission(player, "plots.admin.interact.road")) {
            return;
        }
        if (isPlotArea(loc)) { 
            PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.admin.interact.road");
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void MobSpawn(final CreatureSpawnEvent event) {
        final World world = event.getLocation().getWorld();
        if (event.getEntity() instanceof Player) {
            return;
        }
        if (!isPlotWorld(world)) {
            return;
        }
        Location loc = event.getLocation();
        if (!isPlotArea(loc)) {
            return;
        }
        final PlotWorld pW = getPlotWorld(world);
        final CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if ((reason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) && pW.SPAWN_EGGS) {
            event.setCancelled(true);
            return;
        } else if ((reason == CreatureSpawnEvent.SpawnReason.BREEDING) && pW.SPAWN_BREEDING) {
            event.setCancelled(true);
            return;
        } else if ((reason == CreatureSpawnEvent.SpawnReason.CUSTOM) && pW.SPAWN_CUSTOM && !(event.getEntityType().getTypeId() == 30)) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBlockIgnite(final BlockIgniteEvent e) {
        final World world;

        if (e.getBlock() != null) {
            world = e.getBlock().getWorld();
        } else if (e.getIgnitingEntity() != null) {
            world = e.getIgnitingEntity().getWorld();
        } else if (e.getPlayer() != null) {
            world = e.getPlayer().getWorld();
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
        final Block b = e.getBlock();
        Location loc = b.getLocation();
        if (b != null) {
            if (e.getPlayer() != null) {
                final Player p = e.getPlayer();
                if (!isInPlot(loc)) {
                    if (!PlotMain.hasPermission(p, "plots.admin.build.road")) {
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.build.road");
                        e.setCancelled(true);
                        return;
                    }
                } else {
                    final Plot plot = getCurrentPlot(loc);
                    if (plot == null || !plot.hasOwner()) {
                        if (!PlotMain.hasPermission(p, "plots.admin.build.unowned")) {
                            PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.build.unowned");
                            e.setCancelled(true);
                            return;
                        }
                    } else if (!plot.hasRights(p)) {
                        if (!PlotMain.hasPermission(p, "plots.admin.build.other")) {
                            if (isPlotArea(loc)) { 
                                PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.build.other");
                                e.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            } else {
                if (isPlotArea(loc)) { e.setCancelled(true); }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onTeleport(final PlayerTeleportEvent event) {
        final Location f = event.getFrom();
        final Location t = event.getTo();
        final Location q = new Location(t.getWorld(), t.getBlockX(), 64, t.getZ());

        if (isPlotWorld(q)) {
            if (isInPlot(q)) {
                final Plot plot = getCurrentPlot(q);
                if (plot.deny_entry(event.getPlayer())) {
                    PlayerFunctions.sendMessage(event.getPlayer(), C.YOU_BE_DENIED);
                    event.setCancelled(true);
                    return;
                } else {
                    if (enteredPlot(f, t)) {
                        plotEntry(event.getPlayer(), plot);
                    }
                }
            } else {
                if (leftPlot(f, t)) {
                    final Plot plot = getCurrentPlot(event.getFrom());
                    plotExit(event.getPlayer(), plot);
                }
            }
            if ((q.getBlockX() >= 29999999) || (q.getBlockX() <= -29999999) || (q.getBlockZ() >= 29999999) || (q.getBlockZ() <= -29999999)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBucketEmpty(final PlayerBucketEmptyEvent e) {
        final BlockFace bf = e.getBlockFace();
        final Block b = e.getBlockClicked().getLocation().add(bf.getModX(), bf.getModY(), bf.getModZ()).getBlock();
        Location loc = b.getLocation();
        if (isPlotWorld(loc)) {
            Player p = e.getPlayer();
            if (!isInPlot(loc)) {
                if (PlotMain.hasPermission(p, "plots.admin.build.road")) {
                    return;
                }
                PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.build.road");
                e.setCancelled(true);
                return;
            } else {
                final Plot plot = getCurrentPlot(loc);
                if (plot == null || !plot.hasOwner()) {
                    if (PlotMain.hasPermission(p, "plots.admin.build.unowned")) {
                        return;
                    }
                    PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.build.unowned");
                    e.setCancelled(true);
                    return;
                } else if (!plot.hasRights(e.getPlayer())) {
                    Flag use = FlagManager.getPlotFlag(plot, "use");
                    if (use != null && ((HashSet<PlotBlock>) use.getValue()).contains(new PlotBlock((short) e.getBucket().getId(), (byte) 0))) {
                        return;
                    }
                    if (PlotMain.hasPermission(p, "plots.admin.build.other")) {
                        return;
                    }
                    if (isPlotArea(loc)) { 
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.build.other");
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
        if (PlotSelection.currentSelection.containsKey(event.getPlayer().getName())) {
            PlotSelection.currentSelection.remove(event.getPlayer().getName());
        }
        if (Setup.setupMap.containsKey(event.getPlayer().getName())) {
            Setup.setupMap.remove(event.getPlayer().getName());
        }
        if (Settings.DELETE_PLOTS_ON_BAN && event.getPlayer().isBanned()) {
            final Set<Plot> plots = PlotMain.getPlots(event.getPlayer());
            for (final Plot plot : plots) {
                PlotWorld plotworld = PlotMain.getWorldSettings(plot.world);
                final PlotManager manager = PlotMain.getPlotManager(plot.getWorld());
                manager.clearPlot(null, plotworld, plot, true, null);
                DBFunc.delete(plot.getWorld().getName(), plot);
                PlotMain.sendConsoleSenderMessage(String.format("&cPlot &6%s &cwas deleted + cleared due to &6%s&c getting banned", plot.getId(), event.getPlayer().getName()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBucketFill(final PlayerBucketFillEvent e) {
        final Block b = e.getBlockClicked();
        Location loc = b.getLocation();
        if (isPlotWorld(loc)) {
            Player p = e.getPlayer();
            if (!isInPlot(loc)) {
                if (PlotMain.hasPermission(p, "plots.admin.build.road")) {
                    return;
                }
                PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.build.road");
                e.setCancelled(true);
                return;
            } else {
                final Plot plot = getCurrentPlot(loc);
                if (plot == null || !plot.hasOwner()) {
                    if (PlotMain.hasPermission(p, "plots.admin.build.unowned")) {
                        return;
                    }
                    PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.build.unowned");
                    e.setCancelled(true);
                    return;
                } else if (!plot.hasRights(e.getPlayer())) {
                    Flag use = FlagManager.getPlotFlag(plot, "use");
                    Block block = e.getBlockClicked();
                    if (use != null && ((HashSet<PlotBlock>) use.getValue()).contains(new PlotBlock((short) block.getTypeId(), block.getData()))) {
                        return;
                    }
                    if (PlotMain.hasPermission(p, "plots.admin.build.other")) {
                        return;
                    }
                    if (isPlotArea(loc)) { 
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.build.other");
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
        Location loc = b.getLocation();
        if (isPlotWorld(loc)) {
            final Player p = e.getPlayer();
            if (!isInPlot(loc)) {
                if (!PlotMain.hasPermission(p, "plots.admin.build.road")) {
                    PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.build.road");
                    e.setCancelled(true);
                    return;
                }
            } else {
                final Plot plot = getCurrentPlot(loc);
                if (plot == null || !plot.hasOwner()) {
                    if (!PlotMain.hasPermission(p, "plots.admin.build.unowned")) {
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.build.unowned");
                        e.setCancelled(true);
                        return;
                    }
                } else if (!plot.hasRights(p)) {
                    if (FlagManager.isPlotFlagTrue(plot, "hanging-place")) {
                        return;
                    }
                    if (!PlotMain.hasPermission(p, "plots.admin.build.other")) {
                        if (isPlotArea(loc)) { 
                            PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.build.other");
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
            final Location l = e.getEntity().getLocation();
            if (isPlotWorld(l)) {
                if (!isInPlot(l)) {
                    if (!PlotMain.hasPermission(p, "plots.admin.destroy.road")) {
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.destroy.road");
                        e.setCancelled(true);
                        return;
                    }
                } else {
                    final Plot plot = getCurrentPlot(l);
                    if (plot == null || !plot.hasOwner()) {
                        if (!PlotMain.hasPermission(p, "plots.admin.destroy.unowned")) {
                            PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.destroy.unowned");
                            e.setCancelled(true);
                            return;
                        }
                    } else if (!plot.hasRights(p)) {
                        if (FlagManager.isPlotFlagTrue(plot, "hanging-break")) {
                            return;
                        }
                        if (!PlotMain.hasPermission(p, "plots.admin.destroy.other")) {
                            if (isPlotArea(l)) {
                                PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.destroy.other");
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
        final Location l = e.getRightClicked().getLocation();
        if (isPlotWorld(l)) {
            final Player p = e.getPlayer();
            if (!isInPlot(l)) {
                if (!PlotMain.hasPermission(p, "plots.admin.interact.road")) {
                    PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.interact.road");
                    e.setCancelled(true);
                    return;
                }
            } else {
                final Plot plot = getCurrentPlot(l);
                if (plot == null || !plot.hasOwner()) {
                    if (!PlotMain.hasPermission(p, "plots.admin.interact.unowned")) {
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.interact.unowned");
                        e.setCancelled(true);
                        return;
                    }
                } else if (!plot.hasRights(p)) {
                    Entity entity = e.getRightClicked();
                    if (entity instanceof Monster && FlagManager.isPlotFlagTrue(plot, "hostile-interact")) {
                        return;
                    }
                    if (entity instanceof Animals && FlagManager.isPlotFlagTrue(plot, "animal-interact")) {
                        return;
                    }
                    if (entity instanceof Tameable && ((Tameable) entity).isTamed() && FlagManager.isPlotFlagTrue(plot, "tamed-interact")) {
                        return;
                    }
                    if (entity instanceof RideableMinecart && FlagManager.isPlotFlagTrue(plot, "vehicle-use")) {
                        return;
                    }
                    if (!PlotMain.hasPermission(p, "plots.admin.interact.other")) {
                        if (isPlotArea(l)) { 
                            PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.interact.other");
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onVehicleDestroy (VehicleDestroyEvent e) {
        final Location l = e.getVehicle().getLocation();
        if (isPlotWorld(l)) {
            Entity d = e.getAttacker();
            if (d instanceof Player) {
                final Player p = (Player) d;
                final PlotWorld pW = getPlotWorld(l.getWorld());
                if (!isInPlot(l)) {
                    if (!PlotMain.hasPermission(p, "plots.admin.vehicle.break.road")) {
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.vehicle.break.road");
                        e.setCancelled(true);
                        return;
                    }
                } else {
                    final Plot plot = getCurrentPlot(l);
                    if (plot == null || !plot.hasOwner()) {
                        if (!PlotMain.hasPermission(p, "plots.admin.vehicle.break.unowned")) {
                            PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.vehicle.break.unowned");
                            e.setCancelled(true);
                            return;
                        }
                        return;
                    } 
                    if (!plot.hasRights(p)) {
                        if (FlagManager.isPlotFlagTrue(plot, "vehicle-break")) {
                            return;
                        }
                        if (!PlotMain.hasPermission(p, "plots.admin.vehicle.break.other")) {
                            if (isPlotArea(l)) {
                                PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.vehicle.break.other");
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
        final Location l = e.getEntity().getLocation();
        final Entity d = e.getDamager();
        final Entity a = e.getEntity();
        
        if (Settings.TELEPORT_DELAY != 0 && TaskManager.TELEPORT_QUEUE.size() > 0 && a instanceof Player) {
            Player player = (Player) a;
            String name = player.getName();
            if (TaskManager.TELEPORT_QUEUE.contains(name)) {
                TaskManager.TELEPORT_QUEUE.remove(name);
            }
        }
        
        if (isPlotWorld(l)) {
            if (d instanceof Player) {
                final Player p = (Player) d;
                final boolean aPlr = a instanceof Player;
                final PlotWorld pW = getPlotWorld(l.getWorld());
                if (!aPlr && pW.PVE && (!(a instanceof ItemFrame) && !(a.getType().getTypeId() == 30) ) ) {
                    return;
                } else if (aPlr && pW.PVP) {
                    return;
                }
                if (!isInPlot(l)) {
                    if (!PlotMain.hasPermission(p, "plots.admin.pve.road")) {
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.pve.road");
                        e.setCancelled(true);
                        return;
                    }
                } else {
                    final Plot plot = getCurrentPlot(l);
                    if (plot == null || !plot.hasOwner()) {
                        if (!PlotMain.hasPermission(p, "plots.admin.pve.unowned")) {
                            PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.pve.unowned");
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
                    if (!plot.hasRights(p)) {
                        if (a instanceof Monster && FlagManager.isPlotFlagTrue(plot, "hostile-attack")) {
                            return;
                        }
                        if (a instanceof Animals && FlagManager.isPlotFlagTrue(plot, "animal-attack")) {
                            return;
                        }
                        if (a instanceof Tameable && ((Tameable) a).isTamed() && FlagManager.isPlotFlagTrue(plot, "tamed-attack")) {
                            return;
                        }
                        if (!PlotMain.hasPermission(p, "plots.admin.pve.other")) {
                            if (isPlotArea(l)) { 
                                PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.pve.other");
                                e.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
            if (d instanceof Arrow && isPlotArea(l) && (!(a instanceof Creature))) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onPlayerEggThrow(final PlayerEggThrowEvent e) {
        final Location l = e.getEgg().getLocation();
        if (isPlotWorld(l)) {
            final Player p = e.getPlayer();
            if (!isInPlot(l)) {
                if (!PlotMain.hasPermission(p, "plots.admin.projectile.road")) {
                    PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.projectile.road");
                    e.setHatching(false);
                    return;
                }
            } else {
                final Plot plot = getCurrentPlot(l);
                if (plot == null || !plot.hasOwner()) {
                    if (!PlotMain.hasPermission(p, "plots.admin.projectile.unowned")) {
                        PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.projectile.unowned");
                        e.setHatching(false);
                        return;
                    }
                } else if (!plot.hasRights(p)) {
                    if (!PlotMain.hasPermission(p, "plots.admin.projectile.other")) {
                        if (isPlotArea(l)) { 
                            PlayerFunctions.sendMessage(p, C.NO_PERMISSION, "plots.admin.projectile.other");
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
        final World world = event.getPlayer().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        if (PlotMain.hasPermission(event.getPlayer(), "plots.admin")) {
            return;
        }
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();
        if (isInPlot(loc)) {
            final Plot plot = getCurrentPlot(loc);
            if (!plot.hasOwner()) {
                if (PlotMain.hasPermission(player, "plots.admin.build.unowned")) {
                    return;
                }
                PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.admin.build.unowned");
                event.setCancelled(true);
                return;
            }
            if (!plot.hasRights(player)) {
                Flag place = FlagManager.getPlotFlag(plot, "place");
                Block block = event.getBlock();
                if (place != null && ((HashSet<PlotBlock>) place.getValue()).contains(new PlotBlock((short) block.getTypeId(), (byte) block.getData()))) {
                    return;
                }
                if (!PlotMain.hasPermission(player, "plots.admin.build.other")) {
                    PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.admin.build.other");
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }
        if (!PlotMain.hasPermission(player, "plots.admin.build.road")) {
            if (isPlotArea(loc)) { 
                PlayerFunctions.sendMessage(player, C.NO_PERMISSION, "plots.admin.build.road");
                event.setCancelled(true);
                return;
            }
        }
    }
}
