package com.plotsquared.bukkit.generator;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import com.intellectualcrafters.plot.generator.AugmentedUtils;

public class BukkitAugmentedGenerator extends BlockPopulator {
    
    private static BukkitAugmentedGenerator generator;
    
    private BukkitAugmentedGenerator() {};
    
    public static BukkitAugmentedGenerator get(World world) {
        for (BlockPopulator poplator : world.getPopulators()) {
            if (poplator instanceof BukkitAugmentedGenerator) {
                return (BukkitAugmentedGenerator) poplator;
            }
        }
        if (generator == null) {
            generator = new BukkitAugmentedGenerator();
        }
        world.getPopulators().add(generator);
        return generator;
    }

    @Override
    public void populate(final World world, Random r, final Chunk chunk) {
        AugmentedUtils.generate(world.getName(), chunk.getX(), chunk.getZ(), null);
    }
}
