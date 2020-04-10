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
package com.github.intellectualsites.plotsquared.bukkit.generator;

import com.github.intellectualsites.plotsquared.plot.generator.AugmentedUtils;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BukkitAugmentedGenerator extends BlockPopulator {

    private static BukkitAugmentedGenerator generator;

    public static BukkitAugmentedGenerator get(World world) {
        for (BlockPopulator populator : world.getPopulators()) {
            if (populator instanceof BukkitAugmentedGenerator) {
                return (BukkitAugmentedGenerator) populator;
            }
        }
        if (generator == null) {
            generator = new BukkitAugmentedGenerator();
        }
        world.getPopulators().add(generator);
        return generator;
    }

    @Override
    public void populate(@NotNull World world, @NotNull Random random, @NotNull Chunk source) {
        AugmentedUtils.generate(source, world.getName(), source.getX(), source.getZ(), null);
    }
}
