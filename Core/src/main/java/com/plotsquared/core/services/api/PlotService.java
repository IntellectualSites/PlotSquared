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
package com.plotsquared.core.services.api;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlotService {

    CompletableFuture<Boolean> swapPlots(Plot plot1, Plot plot2);

    void movePlot(Plot originalPlot, Plot newPlot);

    void setOwner(Plot plot, UUID uuid);

    void createPlotsAndData(List<Plot> plots, Runnable whenDone);

    void createPlotSafe(
            final Plot plot, final Runnable success,
            final Runnable failure
    );

    void createPlotAndSettings(Plot plot, Runnable whenDone);

    void delete(Plot plot);

    @Deprecated(forRemoval = true, since = "8.0.0")
    void deleteRatings(Plot plot);

    HashMap<String, HashMap<PlotId, Plot>> getPlots();

    void setMerged(Plot plot, boolean[] merged);

    void setAlias(Plot plot, String alias);

    void setPosition(Plot plot, String position);

    void purgeIds(Set<Integer> uniqueIds);

    void purge(PlotArea area, Set<PlotId> plotIds);

}
