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

import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import com.destroystokyo.paper.event.entity.SlimePathfindEvent;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Events specific to Paper. Some toit nups here
 */
@SuppressWarnings("unused")
public class PaperListener implements Listener {

    private Chunk lastChunk;

    @EventHandler public void onEntityPathfind(EntityPathfindEvent event) {
        if (!Settings.Paper_Components.ENTITY_PATHING) {
            return;
        }
        Location toLoc = BukkitUtil.getLocation(event.getLoc());
        Location fromLoc = BukkitUtil.getLocation(event.getEntity().getLocation());
        PlotArea tarea = toLoc.getPlotArea();
        if (tarea == null) {
            return;
        }
        PlotArea farea = fromLoc.getPlotArea();
        if (farea == null) {
            return;
        }
        if (tarea != farea) {
            event.setCancelled(true);
            return;
        }
        Plot tplot = toLoc.getPlot();
        Plot fplot = fromLoc.getPlot();
        if (tplot == null ^ fplot == null) {
            event.setCancelled(true);
            return;
        }
        if (tplot == null || tplot == fplot) {
            return;
        }
        if (fplot.isMerged() && fplot.getConnectedPlots().contains(fplot)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler public void onEntityPathfind(SlimePathfindEvent event) {
        if (!Settings.Paper_Components.ENTITY_PATHING) {
            return;
        }
        Pathfinder.PathResult path = event.getEntity().getPathfinder().getCurrentPath();

        // Unsure why it would be null, but best to cancel just in case ?
        if (path == null) {
            event.setCancelled(true);
            return;
        }
        org.bukkit.Location bukkitToLocation = path.getNextPoint();

        // Unsure why it would be null, but best to cancel just in case ?
        if (bukkitToLocation == null) {
            event.setCancelled(true);
            return;
        }

        Location toLoc = BukkitUtil.getLocation(bukkitToLocation);
        Location fromLoc = BukkitUtil.getLocation(event.getEntity().getLocation());
        PlotArea tarea = toLoc.getPlotArea();
        if (tarea == null) {
            return;
        }
        PlotArea farea = fromLoc.getPlotArea();
        if (farea == null) {
            return;
        }
        if (tarea != farea) {
            event.setCancelled(true);
            return;
        }
        Plot tplot = toLoc.getPlot();
        Plot fplot = fromLoc.getPlot();
        if (tplot == null ^ fplot == null) {
            event.setCancelled(true);
            return;
        }
        if (tplot == null || tplot == fplot) {
            return;
        }
        if (fplot.isMerged() && fplot.getConnectedPlots().contains(fplot)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler public void onPreCreatureSpawnEvent(PreCreatureSpawnEvent event) {
        if (!Settings.Paper_Components.PRE_SPAWN_LISTENER) {
            return;
        }
        Location location = BukkitUtil.getLocation(event.getSpawnLocation());
        PlotArea area = location.getPlotArea();
        if (!location.isPlotArea()) {
            return;
        }
        //If entities are spawning... the chunk should be loaded?
        Entity[] entities = event.getSpawnLocation().getChunk().getEntities();
        if (entities.length > Settings.Chunk_Processor.MAX_ENTITIES) {
            event.setCancelled(true);
            return;
        }
        CreatureSpawnEvent.SpawnReason reason = event.getReason();
        switch (reason) {
            case DISPENSE_EGG:
            case EGG:
            case OCELOT_BABY:
            case SPAWNER_EGG:
                if (!area.isSpawnEggs()) {
                    event.setCancelled(true);
                    return;
                }
                break;
            case REINFORCEMENTS:
            case NATURAL:
            case MOUNT:
            case PATROL:
            case RAID:
            case SHEARED:
            case SHOULDER_ENTITY:
            case SILVERFISH_BLOCK:
            case TRAP:
            case VILLAGE_DEFENSE:
            case VILLAGE_INVASION:
            case BEEHIVE:
            case CHUNK_GEN:
                if (!area.isMobSpawning()) {
                    event.setCancelled(true);
                    return;
                }
            case BREEDING:
                if (!area.isSpawnBreeding()) {
                    event.setCancelled(true);
                    return;
                }
                break;
            case BUILD_IRONGOLEM:
            case BUILD_SNOWMAN:
            case BUILD_WITHER:
            case CUSTOM:
                if (!area.isSpawnCustom() && event.getType() != EntityType.ARMOR_STAND) {
                    event.setCancelled(true);
                    return;
                }
                break;
            case SPAWNER:
                if (!area.isMobSpawnerSpawning()) {
                    event.setCancelled(true);
                    return;
                }
                break;
        }
        Plot plot = location.getOwnedPlotAbs();
        if (plot == null) {
            if (!area.isMobSpawning()) {
                EntityType type = event.getType();
                switch (type) {
                    case DROPPED_ITEM:
                        if (Settings.Enabled_Components.KILL_ROAD_ITEMS) {
                            event.setCancelled(true);
                            break;
                        }
                    case PLAYER:
                        return;
                }
                event.setCancelled(true);
            }
            return;
        }
        if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
            event.setCancelled(true);
        }
    }
}
