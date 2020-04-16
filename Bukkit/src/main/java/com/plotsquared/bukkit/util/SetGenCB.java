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
package com.plotsquared.bukkit.util;

import com.plotsquared.bukkit.generator.BukkitAugmentedGenerator;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.util.SetupUtils;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class SetGenCB {

    public static void setGenerator(World world) throws Exception {
        SetupUtils.manager.updateGenerators();
        PlotSquared.get().removePlotAreas(world.getName());
        ChunkGenerator gen = world.getGenerator();
        if (gen == null) {
            return;
        }
        String name = gen.getClass().getCanonicalName();
        boolean set = false;
        for (GeneratorWrapper<?> wrapper : SetupUtils.generators.values()) {
            ChunkGenerator newGen = (ChunkGenerator) wrapper.getPlatformGenerator();
            if (newGen == null) {
                newGen = (ChunkGenerator) wrapper;
            }
            if (newGen.getClass().getCanonicalName().equals(name)) {
                // set generator
                Field generator = world.getClass().getDeclaredField("generator");
                Field populators = world.getClass().getDeclaredField("populators");
                generator.setAccessible(true);
                populators.setAccessible(true);
                // Set populators (just in case)
                populators.set(world, new ArrayList<>());
                // Set generator
                generator.set(world, newGen);
                populators.set(world, newGen.getDefaultPopulators(world));
                // end
                set = true;
                break;
            }
        }
        if (!set) {
            world.getPopulators()
                .removeIf(blockPopulator -> blockPopulator instanceof BukkitAugmentedGenerator);
        }
        PlotSquared.get()
            .loadWorld(world.getName(), PlotSquared.get().IMP.getGenerator(world.getName(), null));
    }
}
