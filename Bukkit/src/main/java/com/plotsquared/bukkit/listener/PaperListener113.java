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
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.listener;

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import org.bukkit.block.Banner;
import org.bukkit.block.Beacon;
import org.bukkit.block.Bed;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Comparator;
import org.bukkit.block.Conduit;
import org.bukkit.block.Container;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.DaylightDetector;
import org.bukkit.block.EnchantingTable;
import org.bukkit.block.EndGateway;
import org.bukkit.block.EnderChest;
import org.bukkit.block.Jukebox;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.Structure;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

public class PaperListener113 extends PaperListener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!Settings.Paper_Components.TILE_ENTITY_CHECK || !Settings.Enabled_Components.CHUNK_PROCESSOR) {
            return;
        }
        BlockState state = event.getBlock().getState(false);
        if (!(state instanceof Banner || state instanceof Beacon || state instanceof Bed
                || state instanceof CommandBlock || state instanceof Comparator || state instanceof Conduit
                || state instanceof Container || state instanceof CreatureSpawner || state instanceof DaylightDetector
                || state instanceof EnchantingTable || state instanceof EnderChest || state instanceof EndGateway
                || state instanceof Jukebox || state instanceof Sign || state instanceof Skull
                || state instanceof Structure)) {
            return;
        }
        final Location location = BukkitUtil.getLocation(event.getBlock().getLocation());
        final PlotArea plotArea = location.getPlotArea();
        if (plotArea == null) {
            return;
        }
        final int tileEntityCount = event.getBlock().getChunk().getTileEntities(false).length;
        if (tileEntityCount >= Settings.Chunk_Processor.MAX_TILES) {
            final PlotPlayer<?> plotPlayer = BukkitUtil.getPlayer(event.getPlayer());
            Captions.TILE_ENTITY_CAP_REACHED.send(plotPlayer, Settings.Chunk_Processor.MAX_TILES);
            event.setCancelled(true);
            event.setBuild(false);
        }
    }
}
