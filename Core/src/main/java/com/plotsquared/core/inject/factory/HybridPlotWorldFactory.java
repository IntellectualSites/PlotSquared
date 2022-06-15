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
package com.plotsquared.core.inject.factory;

import com.google.inject.assistedinject.Assisted;
import com.plotsquared.core.generator.HybridPlotWorld;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.plot.PlotId;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nullable;

public interface HybridPlotWorldFactory {

    @NonNull HybridPlotWorld create(
            @NonNull @Assisted("world") String worldName,
            @Nullable @Assisted("id") String id,
            @NonNull IndependentPlotGenerator plotGenerator,
            @Nullable @Assisted("min") PlotId min,
            @Nullable @Assisted("max") PlotId max
    );

}
