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

import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.inject.annotations.WorldConfig;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.queue.GlobalBlockQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class SquarePlotWorld extends GridPlotWorld {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + SquarePlotWorld.class.getSimpleName());

    public int PLOT_WIDTH = 42;
    public int ROAD_WIDTH = 7;
    public int ROAD_OFFSET_X = 0;
    public int ROAD_OFFSET_Z = 0;

    public SquarePlotWorld(
            final String worldName,
            final @Nullable String id,
            final @NonNull IndependentPlotGenerator generator,
            final @Nullable PlotId min,
            final @Nullable PlotId max,
            @WorldConfig final @NonNull YamlConfiguration worldConfiguration,
            final @NonNull GlobalBlockQueue blockQueue
    ) {
        super(worldName, id, generator, min, max, worldConfiguration, blockQueue);
    }

    @Override
    public void loadConfiguration(ConfigurationSection config) {
        if (!config.contains("plot.height")) {
            if (Settings.DEBUG) {
                LOGGER.info("- Configuration is null? ({})", config.getCurrentPath());
            }

        }
        this.PLOT_WIDTH = config.getInt("plot.size");
        this.ROAD_WIDTH = config.getInt("road.width");
        this.ROAD_OFFSET_X = config.getInt("road.offset.x");
        this.ROAD_OFFSET_Z = config.getInt("road.offset.z");
        this.SIZE = (short) (this.PLOT_WIDTH + this.ROAD_WIDTH);
    }

}
