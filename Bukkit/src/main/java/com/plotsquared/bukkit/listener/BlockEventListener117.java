/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.listener;

import com.google.inject.Inject;
import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.CopperOxideFlag;
import com.plotsquared.core.plot.flag.implementations.MiscInteractFlag;
import com.plotsquared.core.plot.flag.implementations.SculkSensorInteractFlag;
import com.plotsquared.core.util.PlotFlagUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockReceiveGameEvent;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public class BlockEventListener117 implements Listener {

    private static final Set<Material> COPPER_OXIDIZING = Set.of(
            Material.COPPER_BLOCK,
            Material.EXPOSED_COPPER,
            Material.WEATHERED_COPPER,
            Material.OXIDIZED_COPPER,
            Material.CUT_COPPER,
            Material.EXPOSED_CUT_COPPER,
            Material.WEATHERED_CUT_COPPER,
            Material.OXIDIZED_CUT_COPPER,
            Material.CUT_COPPER_STAIRS,
            Material.EXPOSED_CUT_COPPER_STAIRS,
            Material.WEATHERED_CUT_COPPER_STAIRS,
            Material.OXIDIZED_CUT_COPPER_STAIRS,
            Material.CUT_COPPER_SLAB,
            Material.EXPOSED_CUT_COPPER_SLAB,
            Material.WEATHERED_CUT_COPPER_SLAB,
            Material.OXIDIZED_CUT_COPPER_SLAB
    );

    @Inject
    public BlockEventListener117() {
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockReceiveGame(BlockReceiveGameEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.adapt(block.getLocation());
        Entity entity = event.getEntity();

        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }

        BukkitPlayer plotPlayer = null;

        if (entity instanceof Player player) {
            plotPlayer = BukkitUtil.adapt(player);
            if (area.notifyIfOutsideBuildArea(plotPlayer, location.getY())) {
                event.setCancelled(true);
                return;
            }
        }

        Plot plot = location.getOwnedPlot();
        if (plot == null && !PlotFlagUtil.isAreaRoadFlagsAndFlagEquals(
                area,
                MiscInteractFlag.class,
                true
        ) || plot != null && (!plot.getFlag(MiscInteractFlag.class) || !plot.getFlag(SculkSensorInteractFlag.class))) {
            if (plotPlayer != null) {
                if (plot != null) {
                    if (!plot.isAdded(plotPlayer.getUUID())) {
                        plot.debug(plotPlayer.getName() + " couldn't trigger sculk sensors because both " +
                                "sculk-sensor-interact and misc-interact = false");
                        event.setCancelled(true);
                    }
                }
                return;
            }
            if (entity instanceof Item item) {
                UUID itemThrower = item.getThrower();
                if (plot != null) {
                    if (itemThrower == null && (itemThrower = item.getOwner()) == null) {
                        plot.debug(
                                "A thrown item couldn't trigger sculk sensors because both sculk-sensor-interact and " +
                                        "misc-interact = false and the item's owner could not be resolved.");
                        event.setCancelled(true);
                        return;
                    }
                    if (!plot.isAdded(itemThrower)) {
                        if (!plot.isAdded(itemThrower)) {
                            plot.debug("A thrown item couldn't trigger sculk sensors because both sculk-sensor-interact and " +
                                    "misc-interact = false");
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFertilize(BlockFertilizeEvent event) {
        Block block = event.getBlock();
        List<org.bukkit.block.BlockState> blocks = event.getBlocks();
        Location location = BukkitUtil.adapt(block.getLocation());

        PlotArea area = location.getPlotArea();
        if (area == null) {
            for (int i = blocks.size() - 1; i >= 0; i--) {
                Location blockLocation = BukkitUtil.adapt(blocks.get(i).getLocation());
                if (blockLocation.isPlotArea()) {
                    blocks.remove(i);
                }
            }
        } else {
            Plot origin = area.getOwnedPlot(location);
            if (origin == null) {
                event.setCancelled(true);
                return;
            }
            for (int i = blocks.size() - 1; i >= 0; i--) {
                Location blockLocation = BukkitUtil.adapt(blocks.get(i).getLocation());
                if (!area.contains(blockLocation.getX(), blockLocation.getZ())) {
                    blocks.remove(i);
                    continue;
                }
                Plot plot = area.getOwnedPlot(blockLocation);
                if (!Objects.equals(plot, origin)) {
                    event.getBlocks().remove(i);
                    continue;
                }
                if (!area.buildRangeContainsY(location.getY())) {
                    event.getBlocks().remove(i);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
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
        if (COPPER_OXIDIZING.contains(event.getNewState().getType())) {
            if (!plot.getFlag(CopperOxideFlag.class)) {
                plot.debug("Copper could not oxide because copper-oxide = false");
                event.setCancelled(true);
            }
        }
    }

}
