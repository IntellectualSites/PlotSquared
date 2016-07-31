package com.plotsquared.sponge.generator;

import com.flowpowered.math.vector.Vector3i;
import com.intellectualcrafters.plot.generator.AugmentedUtils;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.block.DelegateLocalBlockQueue;
import com.plotsquared.sponge.util.SpongeUtil;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.WorldGenerator;

import java.util.List;

public class SpongeAugmentedGenerator implements GenerationPopulator {
    
    private static SpongeAugmentedGenerator generator;

    private SpongeAugmentedGenerator() {}

    public static SpongeAugmentedGenerator get(World world) {
        WorldGenerator wg = world.getWorldGenerator();
        List<GenerationPopulator> populators = wg.getGenerationPopulators();
        for (GenerationPopulator populator : populators) {
            if (populator instanceof SpongeAugmentedGenerator) {
                return (SpongeAugmentedGenerator) populator;
            }
        }
        if (generator == null) {
            generator = new SpongeAugmentedGenerator();
        }
        populators.add(generator);
        return generator;
    }

    @Override
    public void populate(World world, MutableBlockVolume terrain, ImmutableBiomeArea biome) {
        Vector3i min = terrain.getBlockMin();
        int bx = min.getX();
        int bz = min.getZ();
        int cx = bx >> 4;
        int cz = bz >> 4;
        AugmentedUtils.generate(world.getName(), cx, cz, new DelegateLocalBlockQueue(null) {
            @Override
            public boolean setBlock(int x, int y, int z, int id, int data) {
                terrain.setBlock(bx + x, y, bz + z, SpongeUtil.getBlockState(id, data), SpongeUtil.CAUSE);
                return true;
            }

            @Override
            public PlotBlock getBlock(int x, int y, int z) {
                BlockState block = terrain.getBlock(bx + x, y, bz + z);
                return SpongeUtil.getPlotBlock(block);
            }

            @Override
            public boolean setBiome(int x, int z, String biome) {
                return false; // TODO ?
            }

            @Override
            public String getWorld() {
                return world.getName();
            }
        });
    }
}
