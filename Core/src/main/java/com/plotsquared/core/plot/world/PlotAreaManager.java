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
import com.sk89q.worldedit.regions.CuboidRegion;
import org.jetbrains.annotations.NotNull;

public interface PlotAreaManager {

    /**
     * Get the plot area for a particular location. This
     * method assumes that the caller already knows that
     * the location belongs to a plot area, in which
     * case it will return the appropriate plot area.
     *
     * If the location does not belong to a plot area,
     * it may still return an area.
     *
     * @param location The location
     * @return An applicable area, or null
     */
    PlotArea getApplicablePlotArea(Location location);

    /**
     * Get the plot area, if there is any, for the given
     * location. This may return null, if given location
     * does not belong to a plot area.
     *
     * @param location The location
     * @return The area, if found
     */
    PlotArea getPlotArea(@NotNull Location location);

    PlotArea getPlotArea(String world, String id);

    PlotArea[] getPlotAreas(String world, CuboidRegion region);

    PlotArea[] getAllPlotAreas();

    String[] getAllWorlds();

    void addPlotArea(PlotArea area);

    void removePlotArea(PlotArea area);

    void addWorld(String worldName);

    void removeWorld(String worldName);

}
