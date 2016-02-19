package com.plotsquared.bukkit.generator;

import com.intellectualcrafters.plot.generator.AugmentedUtils;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

public class BukkitAugmentedGenerator extends BlockPopulator {
    
    private static BukkitAugmentedGenerator generator;
    
    private BukkitAugmentedGenerator() {}

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
    public void populate(World world, Random r, Chunk chunk) {
        AugmentedUtils.generate(world.getName(), chunk.getX(), chunk.getZ());
    }
}
