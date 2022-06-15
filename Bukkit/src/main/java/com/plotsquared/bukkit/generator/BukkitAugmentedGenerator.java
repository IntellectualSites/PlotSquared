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
package com.plotsquared.bukkit.generator;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.generator.AugmentedUtils;
import com.plotsquared.core.queue.QueueCoordinator;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.SideEffectSet;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.checkerframework.checker.nullness.qual.NonNull;

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
    public void populate(@NonNull World world, @NonNull Random random, @NonNull Chunk source) {
        QueueCoordinator queue = PlotSquared.platform().globalBlockQueue().getNewQueue(BukkitAdapter.adapt(world));
        // The chunk is already loaded and we do not want to load the chunk in "fully" by using any PaperLib methods.
        queue.setForceSync(true);
        queue.setSideEffectSet(SideEffectSet.none());
        queue.setBiomesEnabled(false);
        queue.setChunkObject(source);
        AugmentedUtils.generateChunk(world.getName(), source.getX(), source.getZ(), queue);
        queue.enqueue();
    }

}
