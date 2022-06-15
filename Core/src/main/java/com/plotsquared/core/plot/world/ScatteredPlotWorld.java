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
package com.plotsquared.core.plot.world;

import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotWorld;
import com.plotsquared.core.util.PlotAreaConverter;
import com.plotsquared.core.util.RegionUtil;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.khelekore.prtree.MBR;
import org.khelekore.prtree.PRTree;
import org.khelekore.prtree.SimpleMBR;

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
    public ScatteredPlotWorld(final @NonNull String world) {
        super(world);
    }

    @Override
    public @Nullable PlotArea getArea(final @NonNull Location location) {
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

    @Override
    public @NonNull Collection<PlotArea> getAreas() {
        return Collections.unmodifiableCollection(this.areas);
    }

    @Override
    public void addArea(final @NonNull PlotArea area) {
        this.areas.add(area);
        this.buildTree();
    }

    @Override
    public void removeArea(final @NonNull PlotArea area) {
        this.areas.remove(area);
        this.buildTree();
    }

    @Override
    public @NonNull Collection<PlotArea> getAreasInRegion(final @NonNull CuboidRegion region) {
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
