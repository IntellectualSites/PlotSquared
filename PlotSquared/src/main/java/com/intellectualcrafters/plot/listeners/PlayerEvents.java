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

import com.intellectualcrafters.plot.*;
import com.intellectualcrafters.plot.commands.Setup;
import com.intellectualcrafters.plot.database.DBFunc;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.List;
import java.util.Set;

/**
 * Player Events involving plots
 *
 * @author Citymonstret
 */
@SuppressWarnings("unused")
public class PlayerEvents extends com.intellectualcrafters.plot.listeners.PlotListener implements Listener {

    @EventHandler
    public static void onWorldLoad(final WorldLoadEvent event) {
        PlotMain.loadWorld(event.getWorld());
    }

    @EventHandler
    public static void onJoin(final PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            event.getPlayer().saveData();
        }
        // textures(event.getPlayer());
        if (isInPlot(event.getPlayer().getLocation())) {
            plotEntry(event.getPlayer(), getCurrentPlot(event.getPlayer().getLocation()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void PlayerMove(final PlayerMoveEvent event) {
        try {
            final Player player = event.getPlayer();
            final Location from = event.getFrom();
            final Location to = event.getTo();
            if ((from.getBlockX() != to.getBlockX()) || (from.getBlockZ() != to.getBlockZ())) {
                if (!isPlotWorld(player.getWorld())) {
                    return;
                }
                if (enteredPlot(from, to)) {
                    final Plot plot = getCurrentPlot(event.getTo());
                    final boolean admin = PlotMain.hasPermission(player, "plots.admin");
                    if (plot.deny_entry(player) && !admin) {
                        event.setCancelled(true);
                        return;
                    }
                    plotEntry(player, plot);
                } else if (leftPlot(event.getFrom(), event.getTo())) {
                    final Plot plot = getCurrentPlot(event.getFrom());
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
        if (PlotMain.hasPermission(event.getPlayer(), "plots.admin")) {
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
        final World world = event.getLocation().getWorld();
        if (!isPlotWorld(world)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onPeskyMobsChangeTheWorldLikeWTFEvent( // LOL!
                                                              final EntityChangeBlockEvent event) {
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
                if (!PlotMain.hasPermission(p, "plots.admin")) {
                    event.setCancelled(true);
                }
            } else {
                final Plot plot = getCurrentPlot(b.getLocation());
                if (plot == null) {
                    if (!PlotMain.hasPermission(p, "plots.admin")) {
                        event.setCancelled(true);
                    }
                } else if (!plot.hasRights(p)) {
                    if (!PlotMain.hasPermission(p, "plots.admin")) {
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBlockPistonExtend(final BlockPistonExtendEvent e) {
        if (isInPlot(e.getBlock().getLocation())) {
            for (Block block : e.getBlocks()) {
                if (!isInPlot(block.getLocation())) {
                    e.setCancelled(true);
                }
            }
        }
        /*if (isInPlot(e.getBlock().getLocation())) {

            e.getDirection();
            final int modifier = e.getBlocks().size();
            Location l = e.getBlock().getLocation();
            {
                if (e.getDirection() == BlockFace.EAST) {
                    l = e.getBlock().getLocation().subtract(modifier, 0, 0);
                } else if (e.getDirection() == BlockFace.NORTH) {
                    l = e.getBlock().getLocation().subtract(0, 0, modifier);
                } else if (e.getDirection() == BlockFace.SOUTH) {
                    l = e.getBlock().getLocation().add(0, 0, modifier);
                } else if (e.getDirection() == BlockFace.WEST) {
                    l = e.getBlock().getLocation().add(modifier, 0, 0);
                }

                if (!isInPlot(l)) {
                    e.setCancelled(true);
                    return;
                }
            }
            for (final Block b : e.getBlocks()) {
                if (!isInPlot(b.getLocation())) {
                    return;
                }
                {
                    if (e.getDirection() == BlockFace.EAST) {
                        if (!isInPlot(b.getLocation().subtract(1, 0, 0))) {
                            e.setCancelled(true);
                        }
                    } else if (e.getDirection() == BlockFace.NORTH) {
                        if (!isInPlot(b.getLocation().subtract(0, 0, 1))) {
                            e.setCancelled(true);
                        }
                    } else if (e.getDirection() == BlockFace.SOUTH) {
                        if (!isInPlot(b.getLocation().add(0, 0, 1))) {
                            e.setCancelled(true);
                        }
                    } else if (e.getDirection() == BlockFace.WEST) {
                        if (!isInPlot(b.getLocation().add(1, 0, 0))) {
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }*/
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
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
        if (PlotMain.hasPermission(event.getPlayer(), "plots.admin")) {
            return;
        }
        if (isInPlot(event.getClickedBlock().getLocation())) {
            final Plot plot = getCurrentPlot(event.getClickedBlock().getLocation());

            // They shouldn't be allowed to access other people's chests

            // if (new ArrayList<>(Arrays.asList(new Material[] {
            // Material.STONE_BUTTON, Material.WOOD_BUTTON,
            // Material.LEVER, Material.STONE_PLATE, Material.WOOD_PLATE,
            // Material.CHEST, Material.TRAPPED_CHEST, Material.TRAP_DOOR,
            // Material.WOOD_DOOR, Material.WOODEN_DOOR,
            // Material.DISPENSER, Material.DROPPER
            //
            // })).contains(event.getClickedBlock().getType())) {
            // return;
            // }

            if (PlotMain.booleanFlags.containsKey(event.getClickedBlock().getType())) {
                final String flag = PlotMain.booleanFlags.get(event.getClickedBlock().getType());
                if ((plot.settings.getFlag(flag) != null) && getFlagValue(plot.settings.getFlag(flag).getValue())) {
                    return;
                }
            }

            if (!plot.hasRights(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
        if (PlayerFunctions.getPlot(event.getClickedBlock().getLocation()) == null) {
            event.setCancelled(true);
        }
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
        if (e.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            e.setCancelled(true);
            return;
        }
        final Block b = e.getBlock();
        if (b != null) {
            if (e.getPlayer() != null) {
                final Player p = e.getPlayer();
                if (!isInPlot(b.getLocation())) {
                    if (!PlotMain.hasPermission(p, "plots.admin")) {
                        e.setCancelled(true);
                    }
                } else {
                    final Plot plot = getCurrentPlot(b.getLocation());
                    if (plot == null) {
                        if (!PlotMain.hasPermission(p, "plots.admin")) {
                            e.setCancelled(true);
                        }
                    } else if (!plot.hasRights(p)) {
                        if (!PlotMain.hasPermission(p, "plots.admin")) {
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
                final Plot plot = getCurrentPlot(event.getTo());
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
            if ((event.getTo().getBlockX() >= 29999999) || (event.getTo().getBlockX() <= -29999999) || (event.getTo().getBlockZ() >= 29999999) || (event.getTo().getBlockZ() <= -29999999)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBucketEmpty(final PlayerBucketEmptyEvent e) {
        if (!PlotMain.hasPermission(e.getPlayer(), "plots.admin")) {
            final BlockFace bf = e.getBlockFace();
            final Block b = e.getBlockClicked().getLocation().add(bf.getModX(), bf.getModY(), bf.getModZ()).getBlock();
            if (isPlotWorld(b.getLocation())) {
                if (!isInPlot(b.getLocation())) {
                    PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
                    e.setCancelled(true);
                } else {
                    final Plot plot = getCurrentPlot(b.getLocation());
                    if (plot == null) {
                        PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
                        e.setCancelled(true);
                    } else if (!plot.hasRights(e.getPlayer())) {
                        PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
                        e.setCancelled(true);
                    }
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
                manager.clearPlot(null, plot);
                DBFunc.delete(plot.getWorld().getName(), plot);
                PlotMain.sendConsoleSenderMessage(String.format("&cPlot &6%s &cwas deleted + cleared due to &6%s&c getting banned", plot.getId(), event.getPlayer().getName()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public static void onBucketFill(final PlayerBucketFillEvent e) {
        if (!PlotMain.hasPermission(e.getPlayer(), "plots.admin")) {
            final Block b = e.getBlockClicked();
            if (isPlotWorld(b.getLocation())) {
                if (!isInPlot(b.getLocation())) {
                    PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
                    e.setCancelled(true);
                } else {
                    final Plot plot = getCurrentPlot(b.getLocation());
                    if (plot == null) {
                        PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
                        e.setCancelled(true);
                    } else if (!plot.hasRights(e.getPlayer())) {
                        PlayerFunctions.sendMessage(e.getPlayer(), C.NO_PLOT_PERMS);
                        e.setCancelled(true);
                    }
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
                if (!PlotMain.hasPermission(p, "plots.admin")) {
                    PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                    e.setCancelled(true);
                }
            } else {
                final Plot plot = getCurrentPlot(b.getLocation());
                if (plot == null) {
                    if (!PlotMain.hasPermission(p, "plots.admin")) {
                        PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                        e.setCancelled(true);
                    }
                } else if (!plot.hasRights(p)) {
                    if (!PlotMain.hasPermission(p, "plots.admin")) {
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
                    if (!PlotMain.hasPermission(p, "plots.admin")) {
                        PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                        e.setCancelled(true);
                    }
                } else {
                    final Plot plot = getCurrentPlot(l);
                    if (plot == null) {
                        if (!PlotMain.hasPermission(p, "plots.admin")) {
                            PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                            e.setCancelled(true);
                        }
                    } else if (!plot.hasRights(p)) {
                        if (!PlotMain.hasPermission(p, "plots.admin")) {
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
                if (!PlotMain.hasPermission(p, "plots.admin")) {
                    PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                    e.setCancelled(true);
                }
            } else {
                final Plot plot = getCurrentPlot(l);
                if (plot == null) {
                    if (!PlotMain.hasPermission(p, "plots.admin")) {
                        PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                        e.setCancelled(true);
                    }
                } else if (!plot.hasRights(p)) {
                    if (!PlotMain.hasPermission(p, "plots.admin")) {
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
                if (!aPlr && pW.PVE) {
                    return;
                } else if (aPlr && pW.PVP) {
                    return;
                }
                if (!isInPlot(l)) {
                    if (!PlotMain.hasPermission(p, "plots.admin")) {
                        PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                        e.setCancelled(true);
                    }
                } else {
                    final Plot plot = getCurrentPlot(l);
                    if (plot == null) {
                        if (!PlotMain.hasPermission(p, "plots.admin")) {
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
                        if (!PlotMain.hasPermission(p, "plots.admin")) {
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
                if (!PlotMain.hasPermission(p, "plots.admin")) {
                    PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                    e.setHatching(false);
                }
            } else {
                final Plot plot = getCurrentPlot(l);
                if (plot == null) {
                    if (!PlotMain.hasPermission(p, "plots.admin")) {
                        PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                        e.setHatching(false);
                    }
                } else if (!plot.hasRights(p)) {
                    if (!PlotMain.hasPermission(p, "plots.admin")) {
                        PlayerFunctions.sendMessage(p, C.NO_PLOT_PERMS);
                        e.setHatching(false);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChangeWorld(final PlayerChangedWorldEvent event) {
        /*
         * if (isPlotWorld(event.getFrom()) &&
         * (Settings.PLOT_SPECIFIC_RESOURCE_PACK.length() > 1)) {
         * event.getPlayer().setResourcePack("");
         * }
         * else {
         * textures(event.getPlayer());
         * }
         */
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
        if (isInPlot(event.getBlock().getLocation())) {
            final Plot plot = getCurrentPlot(event.getBlockPlaced().getLocation());
            if (!plot.hasRights(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
        if (PlayerFunctions.getPlot(event.getBlockPlaced().getLocation()) == null) {
            event.setCancelled(true);
        }
    }
}
