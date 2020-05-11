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
package com.plotsquared.core.plot.world;

import com.plotsquared.core.collection.QuadMap;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.StringMan;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class DefaultPlotAreaManager implements PlotAreaManager {

    final PlotArea[] noPlotAreas = new PlotArea[0];
    // All plot areas mapped by world
    private final HashMap<String, PlotArea[]> plotAreaMap = new HashMap<>();
    // All plot areas mapped by position
    private final HashMap<String, QuadMap<PlotArea>> plotAreaGrid = new HashMap<>();
    private final HashSet<Integer> plotAreaHashCheck = new HashSet<>();
    // All plot areas
    private PlotArea[] plotAreas = new PlotArea[0];
    // Optimization if there are no hash collisions
    private boolean plotAreaHasCollision = false;
    private String[] worlds = new String[0];

    @Override public PlotArea[] getAllPlotAreas() {
        return plotAreas;
    }

    @Override public PlotArea getApplicablePlotArea(Location location) {
        switch (this.plotAreas.length) {
            case 0:
                return null;
            case 1:
                return this.plotAreas[0].getWorldHash() == location.getWorld().hashCode()
                    && this.plotAreas[0].contains(location) && (!this.plotAreaHasCollision
                    || location.getWorld().equals(this.plotAreas[0].getWorldName())) ?
                    this.plotAreas[0] :
                    null;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                String world = location.getWorld();
                int hash = world.hashCode();
                for (PlotArea area : this.plotAreas) {
                    if (hash == area.getWorldHash()) {
                        if (area.contains(location.getX(), location.getZ()) && (
                            !this.plotAreaHasCollision || world.equals(area.getWorldName()))) {
                            return area;
                        }
                    }
                }
                return null;
            default:
                PlotArea[] areas = this.plotAreaMap.get(location.getWorld());
                if (areas == null) {
                    return null;
                }
                int z;
                int x;
                switch (areas.length) {
                    case 1:
                        return areas[0];
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        x = location.getX();
                        z = location.getZ();
                        for (PlotArea area : areas) {
                            if (area.contains(x, z)) {
                                return area;
                            }
                        }
                        return null;
                    default:
                        QuadMap<PlotArea> search = this.plotAreaGrid.get(location.getWorld());
                        return search.get(location.getX(), location.getZ());
                }
        }
    }

    @Override public void addPlotArea(PlotArea plotArea) {
        HashSet<PlotArea> localAreas =
            new HashSet<>(Arrays.asList(getPlotAreas(plotArea.getWorldName(), null)));
        HashSet<PlotArea> globalAreas = new HashSet<>(Arrays.asList(plotAreas));
        localAreas.add(plotArea);
        globalAreas.add(plotArea);
        this.plotAreas = globalAreas.toArray(new PlotArea[0]);
        this.plotAreaMap.put(plotArea.getWorldName(), localAreas.toArray(new PlotArea[0]));
        QuadMap<PlotArea> map = this.plotAreaGrid.get(plotArea.getWorldName());
        if (map == null) {
            map = new QuadMap<PlotArea>(Integer.MAX_VALUE, 0, 0) {
                @Override public CuboidRegion getRegion(PlotArea value) {
                    return value.getRegion();
                }
            };
            this.plotAreaGrid.put(plotArea.getWorldName(), map);
        }
        map.add(plotArea);
    }

    @Override public void removePlotArea(PlotArea area) {
        ArrayList<PlotArea> globalAreas = new ArrayList<>(Arrays.asList(plotAreas));
        globalAreas.remove(area);
        this.plotAreas = globalAreas.toArray(new PlotArea[0]);
        if (globalAreas.isEmpty()) {
            this.plotAreaMap.remove(area.getWorldName());
            this.plotAreaGrid.remove(area.getWorldName());
        } else {
            this.plotAreaMap.put(area.getWorldName(), globalAreas.toArray(new PlotArea[0]));
            this.plotAreaGrid.get(area.getWorldName()).remove(area);
        }
    }

    @Override public PlotArea getPlotArea(String world, String id) {
        PlotArea[] areas = this.plotAreaMap.get(world);
        if (areas == null) {
            return null;
        }
        if (areas.length == 1) {
            return areas[0];
        } else if (id == null) {
            return null;
        }
        for (PlotArea area : areas) {
            if (StringMan.isEqual(id, area.getId())) {
                return area;
            }
        }
        return null;
    }

    @Override public PlotArea getPlotArea(@NotNull Location location) {
        switch (this.plotAreas.length) {
            case 0:
                return null;
            case 1:
                PlotArea pa = this.plotAreas[0];
                if (pa.contains(location)) {
                    return pa;
                } else {
                    return null;
                }
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                String world = location.getWorld();
                int hash = world.hashCode();
                for (PlotArea area : this.plotAreas) {
                    if (hash == area.getWorldHash()) {
                        if (area.contains(location.getX(), location.getZ()) && (
                            !this.plotAreaHasCollision || world.equals(area.getWorldName()))) {
                            return area;
                        }
                    }
                }
                return null;
            default:
                PlotArea[] areas = this.plotAreaMap.get(location.getWorld());
                if (areas == null) {
                    return null;
                }
                int x;
                int z;
                switch (areas.length) {
                    case 0:
                        PlotArea a = areas[0];
                        return a.contains(location.getX(), location.getZ()) ? a : null;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        x = location.getX();
                        z = location.getZ();
                        for (PlotArea area : areas) {
                            if (area.contains(x, z)) {
                                return area;
                            }
                        }
                        return null;
                    default:
                        QuadMap<PlotArea> search = this.plotAreaGrid.get(location.getWorld());
                        return search.get(location.getX(), location.getZ());
                }
        }
    }

    @Override public PlotArea[] getPlotAreas(String world, CuboidRegion region) {
        if (region == null) {
            PlotArea[] areas = this.plotAreaMap.get(world);
            if (areas == null) {
                return noPlotAreas;
            }
            return areas;
        }
        QuadMap<PlotArea> areas = this.plotAreaGrid.get(world);
        if (areas == null) {
            return noPlotAreas;
        } else {
            Set<PlotArea> found = areas.get(region);
            return found.toArray(new PlotArea[0]);
        }
    }

    @Override public void addWorld(String worldName) {
        if (!this.plotAreaHasCollision && !this.plotAreaHashCheck.add(worldName.hashCode())) {
            this.plotAreaHasCollision = true;
        }
        Set<String> tmp = new LinkedHashSet<>();
        Collections.addAll(tmp, worlds);
        tmp.add(worldName);
        worlds = tmp.toArray(new String[0]);
    }

    @Override public void removeWorld(String worldName) {
        Set<String> tmp = new LinkedHashSet<>();
        Collections.addAll(tmp, worlds);
        tmp.remove(worldName);
        worlds = tmp.toArray(new String[0]);
    }

    @Override public String[] getAllWorlds() {
        return worlds;
    }
}
