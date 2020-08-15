/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.listener;

import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.BlockBurnFlag;
import com.plotsquared.core.plot.flag.implementations.BlockIgnitionFlag;
import com.plotsquared.core.plot.flag.implementations.BreakFlag;
import com.plotsquared.core.plot.flag.implementations.CoralDryFlag;
import com.plotsquared.core.plot.flag.implementations.DisablePhysicsFlag;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.plot.flag.implementations.ExplosionFlag;
import com.plotsquared.core.plot.flag.implementations.GrassGrowFlag;
import com.plotsquared.core.plot.flag.implementations.IceFormFlag;
import com.plotsquared.core.plot.flag.implementations.IceMeltFlag;
import com.plotsquared.core.plot.flag.implementations.InstabreakFlag;
import com.plotsquared.core.plot.flag.implementations.KelpGrowFlag;
import com.plotsquared.core.plot.flag.implementations.LiquidFlowFlag;
import com.plotsquared.core.plot.flag.implementations.MycelGrowFlag;
import com.plotsquared.core.plot.flag.implementations.PlaceFlag;
import com.plotsquared.core.plot.flag.implementations.RedstoneFlag;
import com.plotsquared.core.plot.flag.implementations.SnowFormFlag;
import com.plotsquared.core.plot.flag.implementations.SnowMeltFlag;
import com.plotsquared.core.plot.flag.implementations.SoilDryFlag;
import com.plotsquared.core.plot.flag.implementations.VineGrowFlag;
import com.plotsquared.core.plot.flag.types.BlockTypeWrapper;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockType;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
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
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.material.Directional;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("unused")
public class BlockEventListener implements Listener {

    private final PlotAreaManager plotAreaManager;
    private final WorldEdit worldEdit;

    @Inject public BlockEventListener(@Nonnull final PlotAreaManager plotAreaManager, @Nonnull final WorldEdit worldEdit) {
        this.plotAreaManager = plotAreaManager;
        this.worldEdit = worldEdit;
    }

    public static void sendBlockChange(final org.bukkit.Location bloc, final BlockData data) {
        TaskManager.runTaskLater(() -> {
            String world = bloc.getWorld().getName();
            int x = bloc.getBlockX();
            int z = bloc.getBlockZ();
            int distance = Bukkit.getViewDistance() * 16;

            for (final PlotPlayer<?> player : PlotSquared.platform().getPlayerManager().getPlayers()) {
                Location location = player.getLocation();
                if (location.getWorldName().equals(world)) {
                    if (16 * Math.abs(location.getX() - x) / 16 > distance || 16 * Math.abs(location.getZ() - z) / 16 > distance) {
                        continue;
                    }
                    ((BukkitPlayer) player).player.sendBlockChange(bloc, data);
                }
            }
        }, TaskTime.ticks(3L));
    }

    @EventHandler public void onRedstoneEvent(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.adapt(block.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = location.getOwnedPlot();
        if (plot == null) {
            if (area.isRoadFlags() && !area.getRoadFlag(RedstoneFlag.class)) {
                event.setNewCurrent(0);
            }
            return;
        }
        if (!plot.getFlag(RedstoneFlag.class)) {
            event.setNewCurrent(0);
            plot.debug("Redstone event was cancelled because redstone = false");
            return;
        }
        if (Settings.Redstone.DISABLE_OFFLINE) {
            boolean disable = false;
            if (!plot.getOwner().equals(DBFunc.SERVER)) {
                if (plot.isMerged()) {
                    disable = true;
                    for (UUID owner : plot.getOwners()) {
                        if (PlotSquared.platform().getPlayerManager().getPlayerIfExists(owner) != null) {
                            disable = false;
                            break;
                        }
                    }
                } else {
                    disable = PlotSquared.platform().getPlayerManager().getPlayerIfExists(plot.getOwnerAbs()) == null;
                }
            }
            if (disable) {
                for (UUID trusted : plot.getTrusted()) {
                    if (PlotSquared.platform().getPlayerManager().getPlayerIfExists(trusted) != null) {
                        disable = false;
                        break;
                    }
                }
                if (disable) {
                    event.setNewCurrent(0);
                    plot.debug("Redstone event was cancelled because no trusted player was in the plot");
                    return;
                }
            }
        }
        if (Settings.Redstone.DISABLE_UNOCCUPIED) {
            for (final PlotPlayer<?> player : PlotSquared.platform().getPlayerManager().getPlayers()) {
                if (plot.equals(player.getCurrentPlot())) {
                    return;
                }
            }
            event.setNewCurrent(0);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST) public void onPhysicsEvent(BlockPhysicsEvent event) {
        switch (event.getChangedType()) {
            case COMPARATOR: {
                Block block = event.getBlock();
                Location location = BukkitUtil.adapt(block.getLocation());
                if (location.isPlotArea()) {
                    return;
                }
                Plot plot = location.getOwnedPlotAbs();
                if (plot == null) {
                    return;
                }
                if (!plot.getFlag(RedstoneFlag.class)) {
                    event.setCancelled(true);
                    plot.debug("Prevented comparator update because redstone = false");
                }
                return;
            }
            case ANVIL:
            case DRAGON_EGG:
            case GRAVEL:
            case SAND:
            case TURTLE_EGG:
            case TURTLE_HELMET:
            case TURTLE_SPAWN_EGG: {
                Block block = event.getBlock();
                Location location = BukkitUtil.adapt(block.getLocation());
                PlotArea area = location.getPlotArea();
                if (area == null) {
                    return;
                }
                Plot plot = area.getOwnedPlotAbs(location);
                if (plot == null) {
                    return;
                }
                if (plot.getFlag(DisablePhysicsFlag.class)) {
                    event.setCancelled(true);
                    plot.debug("Prevented block physics because disable-physics = true");
                }
                return;
            }
            default:
                if (Settings.Redstone.DETECT_INVALID_EDGE_PISTONS) {
                    Block block = event.getBlock();
                    switch (block.getType()) {
                        case PISTON:
                        case STICKY_PISTON:
                            org.bukkit.block.data.Directional piston = (org.bukkit.block.data.Directional) block.getBlockData();
                            Location location = BukkitUtil.adapt(block.getLocation());
                            PlotArea area = location.getPlotArea();
                            if (area == null) {
                                return;
                            }
                            Plot plot = area.getOwnedPlotAbs(location);
                            if (plot == null) {
                                return;
                            }
                            switch (piston.getFacing()) {
                                case EAST:
                                    location = location.add(1, 0, 0);
                                    break;
                                case SOUTH:
                                    location = location.add(-1, 0, 0);
                                    break;
                                case WEST:
                                    location = location.add(0, 0, 1);
                                    break;
                                case NORTH:
                                    location = location.add(0, 0, -1);
                                    break;
                            }
                            Plot newPlot = area.getOwnedPlotAbs(location);
                            if (!plot.equals(newPlot)) {
                                event.setCancelled(true);
                                plot.debug("Prevented piston update because of invalid edge piston detection");
                                return;
                            }
                    }
                }
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockCreate(BlockPlaceEvent event) {
        Location location = BukkitUtil.adapt(event.getBlock().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Player player = event.getPlayer();
        BukkitPlayer pp = BukkitUtil.adapt(player);
        Plot plot = area.getPlot(location);
        if (plot != null) {
            if ((location.getY() > area.getMaxBuildHeight() || location.getY() < area
                .getMinBuildHeight()) && !Permissions
                .hasPermission(pp, Permission.PERMISSION_ADMIN_BUILD_HEIGHT_LIMIT)) {
                event.setCancelled(true);
                pp.sendMessage(
                    TranslatableCaption.of("height.height_limit"),
                    Template.of("limit", String.valueOf(area.getMaxBuildHeight()))
                );
            }
            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, Permission.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                    pp.sendMessage(
                        TranslatableCaption.of("permission.no_permission_event"),
                        Template.of("node", "plots.admin.build.other")
                    );
                    event.setCancelled(true);
                    return;
                }
            } else if (!plot.isAdded(pp.getUUID())) {
                List<BlockTypeWrapper> place = plot.getFlag(PlaceFlag.class);
                if (place != null) {
                    Block block = event.getBlock();
                    if (place.contains(
                        BlockTypeWrapper.get(BukkitAdapter.asBlockType(block.getType())))) {
                        return;
                    }
                }
                if (!Permissions.hasPermission(pp, Permission.PERMISSION_ADMIN_BUILD_OTHER)) {
                    pp.sendMessage(
                        TranslatableCaption.of("permission.no_permission_event"),
                        Template.of("node", "plots.admin.build.other")
                    );
                    event.setCancelled(true);
                    plot.debug(player.getName() + " could not place " + event.getBlock().getType()
                        + " because of the place flag");
                    return;
                }
            } else if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
                if (!Permissions.hasPermission(pp, Permission.PERMISSION_ADMIN_BUILD_OTHER)) {
                    pp.sendMessage(
                        TranslatableCaption.of("permission.no_permission_event"),
                        Template.of("node", "plots.admin.build.other")
                    );
                    event.setCancelled(true);
                    return;
                }
            }
            if (plot.getFlag(DisablePhysicsFlag.class)) {
                Block block = event.getBlockPlaced();
                if (block.getType().hasGravity()) {
                    sendBlockChange(block.getLocation(), block.getBlockData());
                    plot.debug(event.getBlock().getType()
                        + " did not fall because of disable-physics = true");
                }
            }
        } else if (!Permissions.hasPermission(pp, Permission.PERMISSION_ADMIN_BUILD_ROAD)) {
            pp.sendMessage(
                TranslatableCaption.of("permission.no_permission_event"),
                Template.of("node", "plots.admin.build.other")
            );
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST) public void blockDestroy(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = BukkitUtil.adapt(event.getBlock().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getPlot(location);
        if (plot != null) {
            BukkitPlayer plotPlayer = BukkitUtil.adapt(player);
            if (event.getBlock().getY() == 0) {
                if (!Permissions
                    .hasPermission(plotPlayer, Permission.PERMISSION_ADMIN_DESTROY_GROUNDLEVEL)) {
                    plotPlayer.sendMessage(
                        TranslatableCaption.of("permission.no_permission_event"),
                        Template.of("node", "plots.admin.destroy.groundlevel")
                    );
                    event.setCancelled(true);
                    return;
                }
            } else if ((location.getY() > area.getMaxBuildHeight() || location.getY() < area
                .getMinBuildHeight()) && !Permissions
                .hasPermission(plotPlayer, Permission.PERMISSION_ADMIN_BUILD_HEIGHT_LIMIT)) {
                event.setCancelled(true);
                plotPlayer.sendMessage(
                    TranslatableCaption.of("height.height_limit"),
                    Template.of("limit", String.valueOf(area.getMaxBuildHeight()))
                );
            }
            if (!plot.hasOwner()) {
                if (!Permissions
                    .hasPermission(plotPlayer, Permission.PERMISSION_ADMIN_DESTROY_UNOWNED, true)) {
                    event.setCancelled(true);
                }
                return;
            }
            if (!plot.isAdded(plotPlayer.getUUID())) {
                List<BlockTypeWrapper> destroy = plot.getFlag(BreakFlag.class);
                Block block = event.getBlock();
                final BlockType blockType = BukkitAdapter.asBlockType(block.getType());
                for (final BlockTypeWrapper blockTypeWrapper : destroy) {
                    if (blockTypeWrapper.accepts(blockType)) {
                        return;
                    }
                }
                if (Permissions
                    .hasPermission(plotPlayer, Permission.PERMISSION_ADMIN_DESTROY_OTHER)) {
                    return;
                }
                plotPlayer.sendMessage(
                    TranslatableCaption.of("permission.no_permission_event"),
                    Template.of("node", "plots.admin.destroy.other")
                );
                event.setCancelled(true);
            } else if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
                if (!Permissions.hasPermission(plotPlayer, Permission.PERMISSION_ADMIN_BUILD_OTHER)) {
                    plotPlayer.sendMessage(
                        TranslatableCaption.of("permission.no_permission_event"),
                        Template.of("node", "plots.admin.build.other")
                    );
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }
        BukkitPlayer pp = BukkitUtil.adapt(player);
        if (Permissions.hasPermission(pp, Permission.PERMISSION_ADMIN_DESTROY_ROAD)) {
            return;
        }
        if (this.worldEdit!= null && pp.getAttribute("worldedit")) {
            if (player.getInventory().getItemInMainHand().getType() == Material
                .getMaterial(this.worldEdit.getConfiguration().wandItem)) {
                return;
            }
        }
        pp.sendMessage(
            TranslatableCaption.of("permission.no_permission_event"),
            Template.of("node", "plots.admin.destroy.road")
        );
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void onBlockSpread(BlockSpreadEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.adapt(block.getLocation());
        if (location.isPlotRoad()) {
            event.setCancelled(true);
            return;
        }
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot == null) {
            return;
        }
        switch (event.getSource().getType().toString()) {
            case "GRASS_BLOCK":
                if (!plot.getFlag(GrassGrowFlag.class)) {
                    plot.debug("Grass could not grow because grass-grow = false");
                    event.setCancelled(true);
                }
                break;
            case "MYCELIUM":
                if (!plot.getFlag(MycelGrowFlag.class)) {
                    plot.debug("Mycelium could not grow because mycel-grow = false");
                    event.setCancelled(true);
                }
                break;
            case "WEEPING_VINES":
            case "TWISTING_VINES":
            case "VINE":
                if (!plot.getFlag(VineGrowFlag.class)) {
                    plot.debug("Vine could not grow because vine-grow = false");
                    event.setCancelled(true);
                }
                break;
            case "KELP":
                if (!plot.getFlag(KelpGrowFlag.class)) {
                    plot.debug("Kelp could not grow because kelp-grow = false");
                    event.setCancelled(true);
                }
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void onBlockForm(BlockFormEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.adapt(block.getLocation());
        if (location.isPlotRoad()) {
            event.setCancelled(true);
            return;
        }
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot == null) {
            return;
        }
        switch (event.getNewState().getType()) {
            case SNOW:
            case SNOW_BLOCK:
                if (!plot.getFlag(SnowFormFlag.class)) {
                    plot.debug("Snow could not form because snow-form = false");
                    event.setCancelled(true);
                }
                return;
            case ICE:
            case FROSTED_ICE:
            case PACKED_ICE:
                if (!plot.getFlag(IceFormFlag.class)) {
                    plot.debug("Ice could not form because ice-form = false");
                    event.setCancelled(true);
                }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void onEntityBlockForm(EntityBlockFormEvent event) {
        String world = event.getBlock().getWorld().getName();
        if (!this.plotAreaManager.hasPlotArea(world)) {
            return;
        }
        Location location = BukkitUtil.adapt(event.getBlock().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot == null) {
            event.setCancelled(true);
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (!plot.hasOwner()) {
                BukkitPlayer plotPlayer = BukkitUtil.adapt(player);
                if (plot.getFlag(IceFormFlag.class)) {
                    plot.debug("Ice could not be formed because ice-form = false");
                    return;
                }
                event.setCancelled(true);
                return;
            }
            BukkitPlayer plotPlayer = BukkitUtil.adapt(player);
            if (!plot.isAdded(plotPlayer.getUUID())) {
                if (plot.getFlag(IceFormFlag.class)) {
                    plot.debug("Ice could not be formed because ice-form = false");
                    return;
                }
                event.setCancelled(true);
                return;
            }
            return;
        }
        if (!plot.getFlag(IceFormFlag.class)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        Location location = BukkitUtil.adapt(event.getBlock().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        Plot plot = area.getPlot(location);
        if (plot != null) {
            if (plot.getFlag(InstabreakFlag.class)) {
                Block block = event.getBlock();
                BlockBreakEvent call = new BlockBreakEvent(block, player);
                Bukkit.getServer().getPluginManager().callEvent(call);
                if (!call.isCancelled()) {
                    event.getBlock().breakNaturally();
                }
            }
            if (location.getY() == 0) {
                event.setCancelled(true);
                return;
            }
            if (!plot.hasOwner()) {
                BukkitPlayer plotPlayer = BukkitUtil.adapt(player);
                if (Permissions
                    .hasPermission(plotPlayer, Permission.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                    return;
                }
                event.setCancelled(true);
                return;
            }
            BukkitPlayer plotPlayer = BukkitUtil.adapt(player);
            if (!plot.isAdded(plotPlayer.getUUID())) {
                List<BlockTypeWrapper> destroy = plot.getFlag(BreakFlag.class);
                Block block = event.getBlock();
                if (destroy
                    .contains(BlockTypeWrapper.get(BukkitAdapter.asBlockType(block.getType())))
                    || Permissions
                    .hasPermission(plotPlayer, Permission.PERMISSION_ADMIN_DESTROY_OTHER)) {
                    return;
                }
                plot.debug(player.getName() + " could not break " + block.getType()
                    + " because it was not in the break flag");
                event.setCancelled(true);
                return;
            }
            return;
        }
        BukkitPlayer plotPlayer = BukkitUtil.adapt(player);
        if (Permissions.hasPermission(plotPlayer, Permission.PERMISSION_ADMIN_DESTROY_ROAD)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void onFade(BlockFadeEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.adapt(block.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot == null) {
            event.setCancelled(true);
            return;
        }
        switch (block.getType()) {
            case ICE:
                if (!plot.getFlag(IceMeltFlag.class)) {
                    plot.debug("Ice could not melt because ice-melt = false");
                    event.setCancelled(true);
                }
                break;
            case SNOW:
                if (!plot.getFlag(SnowMeltFlag.class)) {
                    plot.debug("Snow could not melt because snow-melt = false");
                    event.setCancelled(true);
                }
                break;
            case FARMLAND:
                if (!plot.getFlag(SoilDryFlag.class)) {
                    plot.debug("Soil could not dry because soil-dry = false");
                    event.setCancelled(true);
                }
                break;
            case TUBE_CORAL_BLOCK:
            case BRAIN_CORAL_BLOCK:
            case BUBBLE_CORAL_BLOCK:
            case FIRE_CORAL_BLOCK:
            case HORN_CORAL_BLOCK:
            case TUBE_CORAL:
            case BRAIN_CORAL:
            case BUBBLE_CORAL:
            case FIRE_CORAL:
            case HORN_CORAL:
            case TUBE_CORAL_FAN:
            case BRAIN_CORAL_FAN:
            case BUBBLE_CORAL_FAN:
            case FIRE_CORAL_FAN:
            case HORN_CORAL_FAN:
            case BRAIN_CORAL_WALL_FAN:
            case BUBBLE_CORAL_WALL_FAN:
            case FIRE_CORAL_WALL_FAN:
            case HORN_CORAL_WALL_FAN:
            case TUBE_CORAL_WALL_FAN:
                if (!plot.getFlag(CoralDryFlag.class)) {
                    plot.debug("Coral could not dry because coral-dry = false");
                    event.setCancelled(true);
                }
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void onChange(BlockFromToEvent event) {
        Block from = event.getBlock();

        // Check liquid flow flag inside of origin plot too
        final Location fLocation = BukkitUtil.adapt(from.getLocation());
        final PlotArea fromArea = fLocation.getPlotArea();
        if (fromArea != null) {
            final Plot plot = fromArea.getOwnedPlot(fLocation);
            if (plot != null && plot.getFlag(LiquidFlowFlag.class) == LiquidFlowFlag.FlowStatus.DISABLED && event.getBlock().isLiquid()) {
                plot.debug("Liquid could now flow because liquid-flow = disabled");
                event.setCancelled(true);
                return;
            }
        }

        Block to = event.getToBlock();
        Location tLocation = BukkitUtil.adapt(to.getLocation());
        PlotArea area = tLocation.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlot(tLocation);
        if (plot != null) {
            if (!area.contains(fLocation.getX(), fLocation.getZ()) || !Objects.equals(plot, area.getOwnedPlot(fLocation))) {
                event.setCancelled(true);
                return;
            }
            if (plot.getFlag(LiquidFlowFlag.class) == LiquidFlowFlag.FlowStatus.ENABLED && event.getBlock().isLiquid()) {
                return;
            }
            if (plot.getFlag(DisablePhysicsFlag.class)) {
                plot.debug(event.getBlock().getType() + " could not update because disable-physics = true");
                event.setCancelled(true);
                return;
            }
            if (plot.getFlag(LiquidFlowFlag.class) == LiquidFlowFlag.FlowStatus.DISABLED && event.getBlock().isLiquid()) {
                plot.debug("Liquid could not flow because liquid-flow = disabled");
                event.setCancelled(true);
            }
        } else if (!area.contains(fLocation.getX(), fLocation.getZ()) || !Objects.equals(null, area.getOwnedPlot(fLocation))) {
            event.setCancelled(true);
        } else if (event.getBlock().isLiquid()) {
            final org.bukkit.Location location = event.getBlock().getLocation();

            /*
                X = block location
                A-H = potential plot locations
               Z
               ^
               |    A B C
               o    D X E
               |    F G H
               v
                <-----O-----> x
             */
            if (BukkitUtil.adapt(location.clone().add(-1, 0, 1)  /* A */).getPlot() != null
                || BukkitUtil.adapt(location.clone().add(1, 0, 0)   /* B */).getPlot() != null
                || BukkitUtil.adapt(location.clone().add(1, 0, 1)   /* C */).getPlot() != null
                || BukkitUtil.adapt(location.clone().add(-1, 0, 0)  /* D */).getPlot() != null
                || BukkitUtil.adapt(location.clone().add(1, 0, 0)   /* E */).getPlot() != null
                || BukkitUtil.adapt(location.clone().add(-1, 0, -1) /* F */).getPlot() != null
                || BukkitUtil.adapt(location.clone().add(0, 0, -1)  /* G */).getPlot() != null
                || BukkitUtil.adapt(location.clone().add(1, 0, 1)   /* H */).getPlot() != null) {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void onGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.adapt(block.getLocation());
        if (location.isUnownedPlotArea()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.adapt(block.getLocation());
        BlockFace face = event.getDirection();
        Vector relative = new Vector(face.getModX(), face.getModY(), face.getModZ());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            if (!this.plotAreaManager.hasPlotArea(location.getWorldName())) {
                return;
            }
            for (Block block1 : event.getBlocks()) {
                Location bloc = BukkitUtil.adapt(block1.getLocation());
                if (bloc.isPlotArea() || bloc.add(relative.getBlockX(), relative.getBlockY(), relative.getBlockZ()).isPlotArea()) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (location.add(relative.getBlockX(), relative.getBlockY(), relative.getBlockZ()).isPlotArea()) {
                // Prevent pistons from extending if they are: bordering a plot
                // area, facing inside plot area, and not pushing any blocks
                event.setCancelled(true);
            }
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot == null) {
            event.setCancelled(true);
            return;
        }
        for (Block block1 : event.getBlocks()) {
            Location bloc = BukkitUtil.adapt(block1.getLocation());
            if (!area.contains(bloc.getX(), bloc.getZ()) || !area.contains(bloc.getX() + relative.getBlockX(), bloc.getZ() + relative.getBlockZ())) {
                event.setCancelled(true);
                return;
            }
            if (!plot.equals(area.getOwnedPlot(bloc)) || !plot
                .equals(area.getOwnedPlot(bloc.add(relative.getBlockX(), relative.getBlockY(), relative.getBlockZ())))) {
                event.setCancelled(true);
                return;
            }
        }
        if (!plot.equals(area.getOwnedPlot(location.add(relative.getBlockX(), relative.getBlockY(), relative.getBlockZ())))) {
            // This branch is only necessary to prevent pistons from extending
            // if they are: on a plot edge, facing outside the plot, and not
            // pushing any blocks
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.adapt(block.getLocation());
        BlockFace face = event.getDirection();
        Vector relative = new Vector(face.getModX(), face.getModY(), face.getModZ());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            if (!this.plotAreaManager.hasPlotArea(location.getWorldName())) {
                return;
            }
            for (Block block1 : event.getBlocks()) {
                Location bloc = BukkitUtil.adapt(block1.getLocation());
                if (bloc.isPlotArea() || bloc.add(relative.getBlockX(), relative.getBlockY(), relative.getBlockZ()).isPlotArea()) {
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot == null) {
            event.setCancelled(true);
            return;
        }
        for (Block block1 : event.getBlocks()) {
            Location bloc = BukkitUtil.adapt(block1.getLocation());
            if (!area.contains(bloc.getX(), bloc.getZ()) || !area.contains(bloc.getX() + relative.getBlockX(), bloc.getZ() + relative.getBlockZ())) {
                event.setCancelled(true);
                return;
            }
            if (!plot.equals(area.getOwnedPlot(bloc)) || !plot
                .equals(area.getOwnedPlot(bloc.add(relative.getBlockX(), relative.getBlockY(), relative.getBlockZ())))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void onBlockDispense(BlockDispenseEvent event) {
        Material type = event.getItem().getType();
        switch (type) {
            case SHULKER_BOX:
            case WHITE_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case LIGHT_GRAY_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case RED_SHULKER_BOX:
            case BLACK_SHULKER_BOX:
            case CARVED_PUMPKIN:
            case WITHER_SKELETON_SKULL:
            case FLINT_AND_STEEL:
            case BONE_MEAL:
            case SHEARS:
            case GLASS_BOTTLE:
            case GLOWSTONE:
            case COD_BUCKET:
            case PUFFERFISH_BUCKET:
            case SALMON_BUCKET:
            case TROPICAL_FISH_BUCKET:
            case BUCKET:
            case WATER_BUCKET:
            case LAVA_BUCKET: {
                if (event.getBlock().getType() == Material.DROPPER) {
                    return;
                }
                BlockFace targetFace = ((Directional) event.getBlock().getState().getData()).getFacing();
                Location location = BukkitUtil.adapt(event.getBlock().getRelative(targetFace).getLocation());
                if (location.isPlotRoad()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void onStructureGrow(StructureGrowEvent event) {
        if (!this.plotAreaManager.hasPlotArea(event.getWorld().getName())) {
            return;
        }
        List<org.bukkit.block.BlockState> blocks = event.getBlocks();
        if (blocks.isEmpty()) {
            return;
        }
        Location location = BukkitUtil.adapt(blocks.get(0).getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            for (int i = blocks.size() - 1; i >= 0; i--) {
                location = BukkitUtil.adapt(blocks.get(i).getLocation());
                if (location.isPlotArea()) {
                    blocks.remove(i);
                }
            }
            return;
        } else {
            Plot origin = area.getOwnedPlot(location);
            if (origin == null) {
                event.setCancelled(true);
                return;
            }
            for (int i = blocks.size() - 1; i >= 0; i--) {
                location = BukkitUtil.adapt(blocks.get(i).getLocation());
                if (!area.contains(location.getX(), location.getZ())) {
                    blocks.remove(i);
                    continue;
                }
                Plot plot = area.getOwnedPlot(location);
                if (!Objects.equals(plot, origin)) {
                    event.getBlocks().remove(i);
                }
            }
        }
        Plot origin = area.getPlot(location);
        if (origin == null) {
            event.setCancelled(true);
            return;
        }
        for (int i = blocks.size() - 1; i >= 0; i--) {
            location = BukkitUtil.adapt(blocks.get(i).getLocation());
            Plot plot = area.getOwnedPlot(location);
            /*
             * plot → the base plot of the merged area
             * origin → the plot where the event gets called
             */

            // Are plot and origin different AND are both plots merged
            if (!Objects.equals(plot, origin) && (!plot.isMerged() && !origin.isMerged())) {
                event.getBlocks().remove(i);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void onBigBoom(BlockExplodeEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.adapt(block.getLocation());
        String world = location.getWorldName();
        if (!this.plotAreaManager.hasPlotArea(world)) {
            return;
        }
        PlotArea area = location.getPlotArea();
        if (area == null) {
            Iterator<Block> iterator = event.blockList().iterator();
            while (iterator.hasNext()) {
                location = BukkitUtil.adapt(iterator.next().getLocation());
                if (location.isPlotArea()) {
                    iterator.remove();
                }
            }
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot == null || !plot.getFlag(ExplosionFlag.class)) {
            event.setCancelled(true);
            if (plot != null) {
                plot.debug("Explosion was cancelled because explosion = false");
            }
        }
        event.blockList().removeIf(blox -> !plot.equals(area.getOwnedPlot(BukkitUtil.adapt(blox.getLocation()))));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.adapt(block.getLocation());

        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }

        Plot plot = location.getOwnedPlot();
        if (plot == null || !plot.getFlag(BlockBurnFlag.class)) {
            if (plot != null) {
                plot.debug("Block burning was cancelled because block-burn = false");
            }
            event.setCancelled(true);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        Entity ignitingEntity = event.getIgnitingEntity();
        Block block = event.getBlock();
        BlockIgniteEvent.IgniteCause igniteCause = event.getCause();
        Location location1 = BukkitUtil.adapt(block.getLocation());
        PlotArea area = location1.getPlotArea();
        if (area == null) {
            return;
        }
        if (igniteCause == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            event.setCancelled(true);
            return;
        }

        Plot plot = area.getOwnedPlot(location1);
        if (player != null) {
            BukkitPlayer pp = BukkitUtil.adapt(player);
            if (plot == null) {
                if (!Permissions.hasPermission(pp, Permission.PERMISSION_ADMIN_BUILD_ROAD)) {
                    pp.sendMessage(
                        TranslatableCaption.of("permission.no_permission_event"),
                        Template.of("node", "plots.admin.build.road")
                    );
                    event.setCancelled(true);
                }
            } else if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, Permission.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                    pp.sendMessage(
                        TranslatableCaption.of("permission.no_permission_event"),
                        Template.of("node", "plots.admin.build.unowned")
                    );
                    event.setCancelled(true);
                }
            } else if (!plot.isAdded(pp.getUUID())) {
                if (!Permissions.hasPermission(pp, Permission.PERMISSION_ADMIN_BUILD_OTHER)) {
                    pp.sendMessage(
                        TranslatableCaption.of("permission.no_permission_event"),
                        Template.of("node", "plots.admin.build.other")
                    );
                    event.setCancelled(true);
                }
            } else if (!plot.getFlag(BlockIgnitionFlag.class)) {
                event.setCancelled(true);
            }
        } else {
            if (plot == null) {
                event.setCancelled(true);
                return;
            }
            if (ignitingEntity != null) {
                if (!plot.getFlag(BlockIgnitionFlag.class)) {
                    event.setCancelled(true);
                    plot.debug("Block ignition was cancelled because block-ignition = false");
                    return;
                }
                if (igniteCause == BlockIgniteEvent.IgniteCause.FIREBALL) {
                    if (ignitingEntity instanceof Fireball) {
                        Projectile fireball = (Projectile) ignitingEntity;
                        Location location = null;
                        if (fireball.getShooter() instanceof Entity) {
                            Entity shooter = (Entity) fireball.getShooter();
                            location = BukkitUtil.adapt(shooter.getLocation());
                        } else if (fireball.getShooter() instanceof BlockProjectileSource) {
                            Block shooter =
                                ((BlockProjectileSource) fireball.getShooter()).getBlock();
                            location = BukkitUtil.adapt(shooter.getLocation());
                        }
                        if (location != null && !plot.equals(location.getPlot())) {
                            event.setCancelled(true);
                        }
                    }
                }

            } else if (event.getIgnitingBlock() != null) {
                Block ignitingBlock = event.getIgnitingBlock();
                Plot plotIgnited = BukkitUtil.adapt(ignitingBlock.getLocation()).getPlot();
                if (igniteCause == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL && (
                    !plot.getFlag(BlockIgnitionFlag.class) || plotIgnited == null || !plotIgnited
                        .equals(plot)) || (igniteCause == BlockIgniteEvent.IgniteCause.SPREAD
                    || igniteCause == BlockIgniteEvent.IgniteCause.LAVA) && (
                    !plot.getFlag(BlockIgnitionFlag.class) || plotIgnited == null || !plotIgnited
                        .equals(plot))) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
