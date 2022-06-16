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
package com.plotsquared.core.util;

import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.setup.PlotAreaBuilder;

import java.util.HashMap;

public abstract class SetupUtils {

    public static HashMap<String, GeneratorWrapper<?>> generators = new HashMap<>();
    protected boolean loaded = false;

    /**
     * @since 6.1.0
     */
    public abstract void updateGenerators(final boolean force);

    public abstract String getGenerator(final PlotArea plotArea);

    public abstract String setupWorld(final PlotAreaBuilder builder);

    public abstract void unload(String world, boolean save);

}
