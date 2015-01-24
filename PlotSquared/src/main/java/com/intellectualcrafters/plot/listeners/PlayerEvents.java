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
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldLoadEvent;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.commands.Setup;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotSelection;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.UUIDHandler;

/**
 * Player Events involving plots
 *
 * @author Citymonstret
 * @author Empire92
 */
public class PlayerEvents extends com.intellectualcrafters.plot.listeners.PlotListener implements Listener {

    @EventHandler
    public static void onWorldLoad(final WorldLoadEvent event) {
        PlotMain.loadWorld(event.getWorld());
    }

    @EventHandler
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
    
    @EventHandler
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
            final Location q = new Location(t.getWorld(), t.getBlockX(), t.getBlockY(), t.getZ());

            if ((f.getBlockX() != q.getBlockX()) || (f.getBlockZ() != q.getBlockZ())) {
                if (!isPlotWorld(player.getWorld())) {
                    return;
                }
                String worldname = q.getWorld().getName();
                if (PlotHelper.worldBorder.containsKey(worldname)) {
                	int border = PlotHelper.getBorder(worldname);
                	boolean passed = false;
                	if (t.getBlockX() >= border) {
                		q.setX(border);
                		passed = true;
                	}
                	if (t.getBlockZ() >= border) {
                		q.setZ(border);
                		passed = true;
                	}
                	if (passed) {
                		event.setTo(q);
                		PlayerFunctions.sendMessage(player, C.BORDER);
                		return;
                	}
                }
                Plot plot = getCurrentPlot(q);
                if (plot != null) {
                	if (!plot.equals(getCurrentPlot(f))) {
                		event.setCancelled(true);
                        return;
                	}
	                if (plot.deny_entry(player)) {
	                	if (!PlotMain.hasPermission(player, "plots.admin.entry.denied")) {
	                		event.setCancelled(true);
	                        return;
	                	}
	                }
                } else if (leftPlot(f, q)) {
                    plot = getCurrentPlot(event.getFrom());
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
        if (PlotMain.hasPermission(event.getPlayer(), "plots.admin.destroy.other")) {
            return;
        }
        if (isInPlot(event.getBlock().getLocation())) {
            if (event.getBlock().getY() == 0) {
                event.setCancelled(true);
                return;
            }
            final Plot plot = getCurrentPlot(event.getBlock().getLocation());
            if (!plot.hasRights(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
        if (PlayerFunctions.getPlot(event.getBlock().getLocation()) == null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public static void onBigBoom(final EntityExplodeEvent event) {
    	// TODO allow tnt explosion within some plots
    	// TODO prevent entity velocity from explosion in plotworld
        final World world = event.getLocation().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        event.setCancelled(true);
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
            }
        } else {
            final Block b = event.getBlock();
            final Player p = (Player) e;
            if (!isInPlot(b.getLocation())) {
                if (!PlotMain.hasPermission(p, "plots.admin.build.road")) {
                    event.setCancelled(true);
                }
            } else {
                final Plot plot = getCurrentPlot(b.getLocation());
                if (plot == null || !plot.hasOwner()) {
                    if (!PlotMain.hasPermission(p, "plots.admin.build.unowned")) {
                        event.setCancelled(true);
                    }
                } else if (!plot.hasRights(p)) {
                    if (!PlotMain.hasPermission(p, "plots.admin.build.other")) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onEntityBlockForm(final EntityBlockFormEvent event) {
        final World world = event.getBlock().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        if ((!(event.getEntity() instanceof Player))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBS(final BlockSpreadEvent e) {
        final Block b = e.getBlock();
        if (isPlotWorld(b.getLocation())) {
            if (!isInPlot(b.getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBF(final BlockFormEvent e) {
        final Block b = e.getBlock();
        if (isPlotWorld(b.getLocation())) {
            if (!isInPlot(b.getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBD(final BlockDamageEvent e) {
        final Block b = e.getBlock();
        if (isPlotWorld(b.getLocation())) {
            if (!isInPlot(b.getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onFade(final BlockFadeEvent e) {
        final Block b = e.getBlock();
        if (isPlotWorld(b.getLocation())) {
            if (!isInPlot(b.getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onChange(final BlockFromToEvent e) {
        final Block b = e.getToBlock();
        if (isPlotWorld(b.getLocation())) {
            if (!isInPlot(b.getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onGrow(final BlockGrowEvent e) {
        final Block b = e.getBlock();
        if (isPlotWorld(b.getLocation())) {
            if (!isInPlot(b.getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public static void onBlockPistonExtend(final BlockPistonExtendEvent e) {
        if (isInPlot(e.getBlock().getLocation())) {
            for (final Block block : e.getBlocks()) {
                if (!isInPlot(block.getLocation())) {
                    e.setCancelled(true);
                }
            }
        }
        /*
         * if (isInPlot(e.getBlock().getLocation())) {
         * e.getDirection();
         * final int modifier = e.getBlocks().size();
         * Location l = e.getBlock().getLocation();
         * {
         * if (e.getDirection() == BlockFace.EAST) {
         * l = e.getBlock().getLocation().subtract(modifier, 0, 0);
         * } else if (e.getDirection() == BlockFace.NORTH) {
         * l = e.getBlock().getLocation().subtract(0, 0, modifier);
         * } else if (e.getDirection() == BlockFace.SOUTH) {
         * l = e.getBlock().getLocation().add(0, 0, modifier);
         * } else if (e.getDirection() == BlockFace.WEST) {
         * l = e.getBlock().getLocation().add(modifier, 0, 0);
         * }
         * if (!isInPlot(l)) {
         * e.setCancelled(true);
         * return;
         * }
         * }
         * for (final Block b : e.getBlocks()) {
         * if (!isInPlot(b.getLocation())) {
         * return;
         * }
         * {
         * if (e.getDirection() == BlockFace.EAST) {
         * if (!isInPlot(b.getLocation().subtract(1, 0, 0))) {
         * e.setCancelled(true);
         * }
         * } else if (e.getDirection() == BlockFace.NORTH) {
         * if (!isInPlot(b.getLocation().subtract(0, 0, 1))) {
         * e.setCancelled(true);
         * }
         * } else if (e.getDirection() == BlockFace.SOUTH) {
         * if (!isInPlot(b.getLocation().add(0, 0, 1))) {
         * e.setCancelled(true);
         * }
         * } else if (e.getDirection() == BlockFace.WEST) {
         * if (!isInPlot(b.getLocation().add(1, 0, 0))) {
         * e.setCancelled(true);
         * }
         * }
         * }
         * }
         * }
         */
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public static void onBlockPistonRetract(final BlockPistonRetractEvent e) {
        final Block b = e.getRetractLocation().getBlock();
        if (isPlotWorld(b.getLocation()) && (e.getBlock().getType() == Material.PISTON_STICKY_BASE)) {
            if (!isInPlot(b.getLocation())) {
                e.setCancelled(true);
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
                if (!isInPlot(blocks.get(i).getLocation())) {
                    e.getBlocks().remove(i);
                }
            }
        }
    }

    @EventHandler
    public static void onInteract(final PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        final World world = event.getPlayer().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        Player player = event.getPlayer();
        if (isInPlot(event.getClickedBlock().getLocation())) {
            final Plot plot = getCurrentPlot(event.getClickedBlock().getLocation());
            if (!plot.hasOwner()) {
                if (PlotMain.hasPermission(player, "plots.admin.interact.unowned")) {
                    return;
                }
            }
            if (PlotMain.booleanFlags.containsKey(event.getClickedBlock().getType())) {
                final String flag = PlotMain.booleanFlags.get(event.getClickedBlock().getType());
                if ((FlagManager.getPlotFlag(plot, flag) != null) && getFlagValue(FlagManager.getPlotFlag(plot, flag).getValue())) {
                    return;
                }
            }
            if (!plot.hasRights(event.getPlayer())) {
                if (PlotMain.hasPermission(player, "plots.admin.interact.other")) {
                    return;
                }
                event.setCancelled(true);
            }
            return;
        }
        if (PlotMain.hasPermission(player, "plots.admin.interact.road")) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void MobSpawn(final CreatureSpawnEvent event) {
        final World world = event.getLocation().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        final PlotWorld pW = getPlotWorld(world);
        final CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if ((reason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) && pW.SPAWN_EGGS) {
            return;
        } else if ((reason == CreatureSpawnEvent.SpawnReason.BREEDING) && pW.SPAWN_BREEDING) {
            return;
        } else if ((reason == CreatureSpawnEvent.SpawnReason.CUSTOM) && pW.SPAWN_CUSTOM) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            return;
        }
        if (!isInPlot(event.getLocation())) {
            event.setCancelled(true);
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
        if (b != null) {
            if (e.getPlayer() != null) {
                final Player p = e.getPlayer();
                if (!isInPlot(b.getLocation())) {
                    if (!PlotMain.hasPermission(p, "plots.admin.build.road")) {
                        e.setCancelled(true);
                    }
                } else {
                    final Plot plot = getCurrentPlot(b.getLocation());
                    if (plot == null || !plot.hasOwner()) {
                        if (!PlotMain.hasPermission(p, "plots.admin.build.unowned")) {
                            e.setCancelled(true);
                        }
                    } else if (!plot.hasRights(p)) {
                        if (!PlotMain.hasPermission(p, "plots.admin.build.other")) {
                            e.setCancelled(true);
                        }
                    }
                }
            } else {
                e.setCancelled(true);
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
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBucketEmpty(final PlayerBucketEmptyEvent e) {
        final BlockFace bf = e.getBlockFace();
        final Block b = e.getBlockClicked().getLocation().add(bf.getModX(), bf.getModY(), bf.getModZ()).getBlock();
        if (isPlotWorld(b.getLocation())) {
            if (!isInPlot(b.getLocation())) {
                if (PlotMain.hasPermission(e.getPlayer(), "plots.admin.build.road")) {
                    return;
                }
                PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
                e.setCancelled(true);
            } else {
                final Plot plot = getCurrentPlot(b.getLocation());
                if (plot == null || !plot.hasOwner()) {
                    if (PlotMain.hasPermission(e.getPlayer(), "plots.admin.build.unowned")) {
                        return;
                    }
                    PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
                    e.setCancelled(true);
                } else if (!plot.hasRights(e.getPlayer())) {
                    if (PlotMain.hasPermission(e.getPlayer(), "plots.admin.build.other")) {
                        return;
                    }
                    PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public static void onInventoryClick(final InventoryClickEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase("PlotSquared Commands")) {
            event.setCancelled(true);
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
                final PlotManager manager = PlotMain.getPlotManager(plot.getWorld());
                manager.clearPlot(null, plot, true);
                DBFunc.delete(plot.getWorld().getName(), plot);
                PlotMain.sendConsoleSenderMessage(String.format("&cPlot &6%s &cwas deleted + cleared due to &6%s&c getting banned", plot.getId(), event.getPlayer().getName()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBucketFill(final PlayerBucketFillEvent e) {
        final Block b = e.getBlockClicked();
        if (isPlotWorld(b.getLocation())) {
            if (!isInPlot(b.getLocation())) {
                if (PlotMain.hasPermission(e.getPlayer(), "plots.admin.build.road")) {
                    return;
                }
                PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
                e.setCancelled(true);
            } else {
                final Plot plot = getCurrentPlot(b.getLocation());
                if (plot == null || !plot.hasOwner()) {
                    if (PlotMain.hasPermission(e.getPlayer(), "plots.admin.build.unowned")) {
                        return;
                    }
                    PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
                    e.setCancelled(true);
                } else if (!plot.hasRights(e.getPlayer())) {
                    if (PlotMain.hasPermission(e.getPlayer(), "plots.admin.build.other")) {
                        return;
                    }
                    PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onHangingPlace(final HangingPlaceEvent e) {
        final Block b = e.getBlock();
        if (isPlotWorld(b.getLocation())) {
            final Player p = e.getPlayer();
            if (!isInPlot(b.getLocation())) {
                if (!PlotMain.hasPermission(p, "plots.admin.build.road")) {
                    PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                    e.setCancelled(true);
                }
            } else {
                final Plot plot = getCurrentPlot(b.getLocation());
                if (plot == null || !plot.hasOwner()) {
                    if (!PlotMain.hasPermission(p, "plots.admin.build.unowned")) {
                        PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                        e.setCancelled(true);
                    }
                } else if (!plot.hasRights(p)) {
                    if (!PlotMain.hasPermission(p, "plots.admin.build.other")) {
                        PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                        e.setCancelled(true);
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
                        PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                        e.setCancelled(true);
                    }
                } else {
                    final Plot plot = getCurrentPlot(l);
                    if (plot == null || !plot.hasOwner()) {
                        if (!PlotMain.hasPermission(p, "plots.admin.destroy.unowned")) {
                            PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                            e.setCancelled(true);
                        }
                    } else if (!plot.hasRights(p)) {
                        if (!PlotMain.hasPermission(p, "plots.admin.destroy.other")) {
                            PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                            e.setCancelled(true);
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
                    PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                    e.setCancelled(true);
                }
            } else {
                final Plot plot = getCurrentPlot(l);
                if (plot == null || !plot.hasOwner()) {
                    if (!PlotMain.hasPermission(p, "plots.admin.interact.unowned")) {
                        PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                        e.setCancelled(true);
                    }
                } else if (!plot.hasRights(p)) {
                    if (!PlotMain.hasPermission(p, "plots.admin.interact.other")) {
                        PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                        e.setCancelled(true);
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
        if (isPlotWorld(l)) {
            if (d instanceof Player) {
                final Player p = (Player) d;
                final boolean aPlr = a instanceof Player;
                final PlotWorld pW = getPlotWorld(l.getWorld());
                if (!aPlr && pW.PVE && (!(a instanceof ItemFrame))) {
                    return;
                } else if (aPlr && pW.PVP) {
                    return;
                }
                if (!isInPlot(l)) {
                    if (!PlotMain.hasPermission(p, "plots.admin.pve.road")) {
                        PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                        e.setCancelled(true);
                    }
                } else {
                    final Plot plot = getCurrentPlot(l);
                    if (plot == null || !plot.hasOwner()) {
                        if (!PlotMain.hasPermission(p, "plots.admin.pve.unowned")) {
                            PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                            e.setCancelled(true);
                            return;
                        }
                    } else if (aPlr && !booleanFlag(plot, "pvp")) {
                        return;
                    }
                    if (!aPlr && !booleanFlag(plot, "pve")) {
                        return;
                    }
                    assert plot != null;
                    if (!plot.hasRights(p)) {
                        if (!PlotMain.hasPermission(p, "plots.admin.pve.other")) {
                            PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                            e.setCancelled(true);
                        }
                    }
                }
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
                    PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                    e.setHatching(false);
                }
            } else {
                final Plot plot = getCurrentPlot(l);
                if (plot == null || !plot.hasOwner()) {
                    if (!PlotMain.hasPermission(p, "plots.admin.projectile.unowned")) {
                        PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                        e.setHatching(false);
                    }
                } else if (!plot.hasRights(p)) {
                    if (!PlotMain.hasPermission(p, "plots.admin.projectile.other")) {
                        PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                        e.setHatching(false);
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
        if (isInPlot(event.getBlock().getLocation())) {
            final Plot plot = getCurrentPlot(event.getBlockPlaced().getLocation());
            if (!plot.hasOwner() && PlotMain.hasPermission(player, "plots.admin.build.unowned")) {
                return;
            }
            if (!plot.hasRights(player) && !PlotMain.hasPermission(player, "plots.admin.build.other")) {
                event.setCancelled(true);
            }
            return;
        }
        if (!PlotMain.hasPermission(player, "plots.admin.build.road")) {
            event.setCancelled(true);
        }
    }
}
