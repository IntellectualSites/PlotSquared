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
import com.sk89q.worldedit.regions.CuboidRegion;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * Ordinary plot world with a single plot area
 */
public class StandardPlotWorld extends PlotWorld {

    private final PlotArea area;

    public StandardPlotWorld(final @NonNull String world, final @Nullable PlotArea area) {
        super(world);
        this.area = area;
    }

    @Override
    public @Nullable PlotArea getArea(final @NonNull Location location) {
        return this.area;
    }

    @Override
    public @NonNull Collection<PlotArea> getAreas() {
        if (this.area == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(this.area);
    }

    @Override
    public @NonNull Collection<PlotArea> getAreasInRegion(final @NonNull CuboidRegion region) {
        return this.getAreas();
    }

}
