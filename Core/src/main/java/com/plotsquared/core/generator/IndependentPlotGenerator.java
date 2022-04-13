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
 *               Copyright (C) 2014 - 2022 IntellectualSites
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.generator;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.queue.ScopedQueueCoordinator;
import com.plotsquared.core.setup.PlotAreaBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This class allows for implementation independent world generation.
 * - Sponge/Bukkit API
 * Use the specify method to get the generator for that platform.
 */
public abstract class IndependentPlotGenerator {

    /**
     * Get the name of this generator.
     *
     * @return generator name
     */
    public abstract String getName();

    /**
     * Use the setBlock or setBiome method of the PlotChunk result parameter to make changes.
     * The PlotArea settings is the same one this was initialized with.
     * The PseudoRandom random is a fast random object.
     *
     * @param result   queue
     * @param settings PlotArea (settings)
     */
    public abstract void generateChunk(ScopedQueueCoordinator result, PlotArea settings);

    public boolean populateChunk(ScopedQueueCoordinator result, PlotArea setting) {
        return false;
    }

    /**
     * Return a new PlotArea object.
     *
     * @param world world name
     * @param id    (May be null) Area name
     * @param min   Min plot id (may be null)
     * @param max   Max plot id (may be null)
     * @return new plot area
     */
    public abstract PlotArea getNewPlotArea(String world, String id, PlotId min, PlotId max);

    /**
     * If any additional setup options need to be changed before world creation.
     * - e.g. If setup doesn't support some standard options
     *
     * @param builder the area builder to modify
     */
    public void processAreaSetup(PlotAreaBuilder builder) {
    }

    /**
     * It is preferred for the PlotArea object to do most of the initialization necessary.
     *
     * @param area area
     */
    public abstract void initialize(PlotArea area);

    /**
     * Get the generator for your specific implementation (bukkit/sponge).<br>
     * - e.g. YourIndependentGenerator.&lt;ChunkGenerator&gt;specify() - Would return a ChunkGenerator object<br>
     *
     * @param <T>   world
     * @param world ChunkGenerator Implementation
     * @return Chunk generator
     */
    @SuppressWarnings("unchecked")
    public <T> GeneratorWrapper<T> specify(final @NonNull String world) {
        return (GeneratorWrapper<T>) PlotSquared.platform().wrapPlotGenerator(world, this);
    }

    @Override
    public String toString() {
        return getName();
    }

}
