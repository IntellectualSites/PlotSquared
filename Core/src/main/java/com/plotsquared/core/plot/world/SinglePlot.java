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

import com.plotsquared.core.location.BlockLoc;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class SinglePlot extends Plot {

    private final Set<CuboidRegion> regions = Collections.singleton(
            new CuboidRegion(
                    BlockVector3.at(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE),
                    BlockVector3.at(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE)
            ));

    public SinglePlot(final @NonNull PlotArea area, final @NonNull PlotId id) {
        super(area, id);
    }

    public SinglePlot(
            PlotId id, UUID owner, HashSet<UUID> trusted, HashSet<UUID> members,
            HashSet<UUID> denied, String alias, BlockLoc position, Collection<PlotFlag<?, ?>> flags,
            PlotArea area, boolean[] merged, long timestamp, int temp
    ) {
        super(id, owner, trusted, members, denied, alias, position, flags, area, merged, timestamp,
                temp
        );
    }

    @Override
    public @NonNull String getWorldName() {
        return getId().toUnderscoreSeparatedString();
    }

    @Override
    public SinglePlotArea getArea() {
        return (SinglePlotArea) super.getArea();
    }

    @Override
    public void getSide(Consumer<Location> result) {
        getCenter(result);
    }

    @Override
    public boolean isLoaded() {
        getArea().loadWorld(getId());
        return super.isLoaded();
    }

    @NonNull
    @Override
    public Set<CuboidRegion> getRegions() {
        return regions;
    }

    // getCenter getSide getHome getDefaultHome getBiome
}
