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

import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotWorld;
import com.plotsquared.core.util.PlotAreaConverter;
import com.plotsquared.core.util.RegionUtil;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.khelekore.prtree.MBR;
import org.khelekore.prtree.PRTree;
import org.khelekore.prtree.SimpleMBR;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Plot world that contains several plot areas (clusters)
 */
public class ScatteredPlotWorld extends PlotWorld {

    private static final PlotAreaConverter MBR_CONVERTER = new PlotAreaConverter();
    private static final int BRANCH_FACTOR = 30;

    private final List<PlotArea> areas = new LinkedList<>();
    private final Object treeLock = new Object();
    private PRTree<PlotArea> areaTree;

    /**
     * Create a new plot world with a given world name
     *
     * @param world World name
     */
    public ScatteredPlotWorld(@Nonnull final String world) {
        super(world);
    }

    @Override @Nullable public PlotArea getArea(@Nonnull final Location location) {
        if (this.areas.isEmpty()) {
            return null;
        }
        synchronized (this.treeLock) {
            for (final PlotArea area : this.areaTree.find(location.toMBR())) {
                if (area.contains(location)) {
                    return area;
                }
            }
        }
        return null;
    }

    @Override @Nonnull public Collection<PlotArea> getAreas() {
        return Collections.unmodifiableCollection(this.areas);
    }

    @Override public void addArea(@Nonnull final PlotArea area) {
        this.areas.add(area);
        this.buildTree();
    }

    @Override public void removeArea(@Nonnull final PlotArea area) {
        this.areas.remove(area);
        this.buildTree();
    }

    @Override @Nonnull public Collection<PlotArea> getAreasInRegion(@Nonnull final CuboidRegion region) {
        if (this.areas.isEmpty()) {
            return Collections.emptyList();
        }
        synchronized (this.treeLock) {
            final List<PlotArea> areas = new LinkedList<>();

            final BlockVector3 min = region.getMinimumPoint();
            final BlockVector3 max = region.getMaximumPoint();
            final MBR mbr = new SimpleMBR(min.getX(), max.getX(), min.getY(), max.getY(), min.getZ(), max.getZ());

            for (final PlotArea area : this.areaTree.find(mbr)) {
                if (RegionUtil.intersects(area.getRegion(), region)) {
                    areas.add(area);
                }
            }

            return areas;
        }
    }

    /**
     * Rebuild the area tree
     */
    private void buildTree() {
        synchronized (this.treeLock) {
            this.areaTree = new PRTree<>(MBR_CONVERTER, BRANCH_FACTOR);
            this.areaTree.load(this.areas);
        }
    }

}
