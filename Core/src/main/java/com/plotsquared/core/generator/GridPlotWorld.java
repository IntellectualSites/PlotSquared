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
package com.plotsquared.core.generator;

import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.inject.annotations.WorldConfig;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.queue.GlobalBlockQueue;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class GridPlotWorld extends PlotArea {

    public short SIZE;

    public GridPlotWorld(
            String worldName, String id, @NonNull IndependentPlotGenerator generator,
            PlotId min, PlotId max, @WorldConfig final @NonNull YamlConfiguration worldConfiguration,
            final @NonNull GlobalBlockQueue blockQueue
    ) {
        super(worldName, id, generator, min, max, worldConfiguration, blockQueue);
    }

}
