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

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotWorld;
import com.plotsquared.core.util.RegionUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rx.Observable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Plot world that contains several plot areas (clusters)
 */
public class ScatteredPlotWorld extends PlotWorld {

    private final List<PlotArea> areas = new LinkedList<>();
    private final Object treeLock = new Object();
    private RTree<PlotArea, Geometry> areaTree;

    /**
     * Create a new plot world with a given world name
     *
     * @param world World name
     */
    public ScatteredPlotWorld(@NotNull final String world) {
        super(world);
    }

    @Override @Nullable public PlotArea getArea(@NotNull final Location location) {
        if (this.areas.isEmpty()) {
            return null;
        }
        synchronized (this.treeLock) {
            final Observable<Entry<PlotArea, Geometry>> area =
                areaTree.search(Geometries.point(location.getX(), location.getZ()));
            if (area.isEmpty().toBlocking().first()) {
                return null;
            }
            return area.toBlocking().first().value();
        }
    }

    @Override @NotNull public Collection<PlotArea> getAreas() {
        return Collections.unmodifiableCollection(this.areas);
    }

    @Override public void addArea(@NotNull final PlotArea area) {
        this.areas.add(area);
        this.buildTree();
    }

    @Override public void removeArea(@NotNull final PlotArea area) {
        this.areas.remove(area);
        this.buildTree();
    }

    @Override @NotNull public Collection<PlotArea> getAreasInRegion(@NotNull final CuboidRegion region) {
        if (this.areas.isEmpty()) {
            return Collections.emptyList();
        }
        synchronized (this.treeLock) {
            final List<PlotArea> areas = new LinkedList<>();
            this.areaTree.search(RegionUtil.toRectangle(region)).toBlocking().forEach(entry -> areas.add(entry.value()));
            return areas;
        }
    }

    /**
     * Rebuild the area tree
     */
    private void buildTree() {
        synchronized (this.treeLock) {
            this.areaTree = RTree.create();
            for (final PlotArea area : areas) {
                this.areaTree = this.areaTree.add(area,
                    RegionUtil.toRectangle(area.getRegion()));
            }
        }
    }

}
