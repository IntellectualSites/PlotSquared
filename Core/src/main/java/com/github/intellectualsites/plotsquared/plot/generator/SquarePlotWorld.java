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
package com.github.intellectualsites.plotsquared.plot.generator;

import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import org.jetbrains.annotations.NotNull;

public abstract class SquarePlotWorld extends GridPlotWorld {

    public int PLOT_WIDTH = 42;
    public int ROAD_WIDTH = 7;
    public int ROAD_OFFSET_X = 0;
    public int ROAD_OFFSET_Z = 0;

    public SquarePlotWorld(String worldName, String id, @NotNull IndependentPlotGenerator generator,
        PlotId min, PlotId max) {
        super(worldName, id, generator, min, max);
    }

    @Override public void loadConfiguration(ConfigurationSection config) {
        if (!config.contains("plot.height")) {
            PlotSquared.debug(" - &cConfiguration is null? (" + config.getCurrentPath() + ')');
        }
        this.PLOT_WIDTH = config.getInt("plot.size");
        this.ROAD_WIDTH = config.getInt("road.width");
        this.ROAD_OFFSET_X = config.getInt("road.offset.x");
        this.ROAD_OFFSET_Z = config.getInt("road.offset.z");
        this.SIZE = (short) (this.PLOT_WIDTH + this.ROAD_WIDTH);
    }
}
