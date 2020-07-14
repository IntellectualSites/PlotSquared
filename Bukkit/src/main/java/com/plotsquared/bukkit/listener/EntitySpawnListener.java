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
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import io.papermc.lib.PaperLib;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import javax.annotation.Nonnull;

import java.util.List;

public class EntitySpawnListener implements Listener {

    private final static String KEY = "P2";
    private static boolean ignoreTP = false;
    private static boolean hasPlotArea = false;
    private static String areaName = null;

    public static void testNether(final Entity entity) {
        @Nonnull World world = entity.getWorld();
        if (world.getEnvironment() != World.Environment.NETHER
            && world.getEnvironment() != World.Environment.THE_END) {
            return;
        }
        test(entity);
    }

    public static void testCreate(final Entity entity) {
        @Nonnull World world = entity.getWorld();
        if (areaName == world.getName()) {
        } else {
            areaName = world.getName();
            hasPlotArea = PlotSquared.get().getPlotAreaManager().hasPlotArea(areaName);
        }
        if (!hasPlotArea) {
            return;
        }
        test(entity);
    }

    public static void test(Entity entity) {
        @Nonnull World world = entity.getWorld();
        List<MetadataValue> meta = entity.getMetadata(KEY);
        if (meta.isEmpty()) {
            if (PlotSquared.get().getPlotAreaManager().hasPlotArea(world.getName())) {
                entity.setMetadata(KEY,
                    new FixedMetadataValue((Plugin) PlotSquared.platform(), entity.getLocation()));
            }
        } else {
            org.bukkit.Location origin = (org.bukkit.Location) meta.get(0).value();
            World originWorld = origin.getWorld();
            if (!originWorld.equals(world)) {
                if (!ignoreTP) {
                    if (!world.getName().equalsIgnoreCase(originWorld + "_the_end")) {
                        if (entity.getType() == EntityType.PLAYER) {
                            return;
                        }
                        try {
                            ignoreTP = true;
                            PaperLib.teleportAsync(entity, origin);
                        } finally {
                            ignoreTP = false;
                        }
                        if (entity.getLocation().getWorld().equals(world)) {
                            entity.remove();
                        }
                    }
                } else {
                    if (entity.getType() == EntityType.PLAYER) {
                        return;
                    }
                    entity.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void creatureSpawnEvent(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        Location location = BukkitUtil.getLocation(entity.getLocation());
        PlotArea area = location.getPlotArea();
        if (!location.isPlotArea()) {
            return;
        }
        Plot plot = location.getOwnedPlotAbs();
        if (plot == null) {
            if (!area.isMobSpawning()) {
                EntityType type = entity.getType();
                switch (type) {
                    case DROPPED_ITEM:
                        if (Settings.Enabled_Components.KILL_ROAD_ITEMS) {
                            event.setCancelled(true);
                            break;
                        }
                    case PLAYER:
                        return;
                }
                if (type.isAlive() || !area.isMiscSpawnUnowned()) {
                    event.setCancelled(true);
                }
            }
            return;
        }
        if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
            event.setCancelled(true);
        }
        switch (entity.getType()) {
            case ENDER_CRYSTAL:
                if (PlayerEvents.checkEntity(entity, plot)) {
                    event.setCancelled(true);
                }
            case SHULKER:
                if (!entity.hasMetadata("shulkerPlot")) {
                    entity.setMetadata("shulkerPlot",
                        new FixedMetadataValue((Plugin) PlotSquared.platform(), plot.getId()));
                }
        }
    }

    @EventHandler public void onChunkLoad(ChunkLoadEvent event) {
        @Nonnull Chunk chunk = event.getChunk();
        for (final Entity entity : chunk.getEntities()) {
            testCreate(entity);
        }
    }

    @EventHandler public void onVehicle(VehicleUpdateEvent event) {
        testNether(event.getVehicle());
    }

    @EventHandler public void onVehicle(VehicleCreateEvent event) {
        testCreate(event.getVehicle());
    }

    @EventHandler public void onVehicle(VehicleBlockCollisionEvent event) {
        testNether(event.getVehicle());
    }

    @EventHandler public void onTeleport(EntityTeleportEvent event) {
        Entity ent = event.getEntity();
        if (ent instanceof Vehicle || ent instanceof ArmorStand) {
            testNether(event.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void vehicleMove(VehicleMoveEvent event) {
        testNether(event.getVehicle());
    }

    @EventHandler public void spawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.ARMOR_STAND) {
            testCreate(event.getEntity());
        }
    }
}
