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
 *               Copyright (C) 2014 - 2022 IntellectualSites
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

import com.google.inject.Inject;
import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.CopperOxideFlag;
import com.plotsquared.core.plot.flag.implementations.MiscInteractFlag;
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
import java.util.UUID;

@SuppressWarnings("unused")
public class BlockEventListener117 implements Listener {

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

        Plot plot = location.getOwnedPlot();
        if (plot == null || !plot.getFlag(MiscInteractFlag.class)) {
            if (entity instanceof Player player) {
                BukkitPlayer plotPlayer = BukkitUtil.adapt(player);
                if (plot != null) {
                    if (!plot.isAdded(plotPlayer.getUUID())) {
                        plot.debug(plotPlayer.getName() + " couldn't trigger sculk sensors because misc-interact = false");
                        event.setCancelled(true);
                    }
                }
                return;
            }
            if (entity instanceof Item item) {
                UUID itemThrower = item.getThrower();
                if (plot != null) {
                    if (!plot.isAdded(itemThrower)) {
                        if (!plot.isAdded(itemThrower)) {
                            plot.debug("A thrown item couldn't trigger sculk sensors because misc-interact = false");
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
            if (!Objects.equals(plot, origin) && (!plot.isMerged() && !origin.isMerged())) {
                event.getBlocks().remove(i);
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
        switch (event.getNewState().getType()) {
            case COPPER_BLOCK:
            case EXPOSED_COPPER:
            case WEATHERED_COPPER:
            case OXIDIZED_COPPER:
            case CUT_COPPER:
            case EXPOSED_CUT_COPPER:
            case WEATHERED_CUT_COPPER:
            case OXIDIZED_CUT_COPPER:
            case CUT_COPPER_STAIRS:
            case EXPOSED_CUT_COPPER_STAIRS:
            case WEATHERED_CUT_COPPER_STAIRS:
            case OXIDIZED_CUT_COPPER_STAIRS:
            case CUT_COPPER_SLAB:
            case EXPOSED_CUT_COPPER_SLAB:
            case WEATHERED_CUT_COPPER_SLAB:
            case OXIDIZED_CUT_COPPER_SLAB:
                if (!plot.getFlag(CopperOxideFlag.class)) {
                    plot.debug("Copper could not oxide because copper-oxide = false");
                    event.setCancelled(true);
                }
        }
    }

}
