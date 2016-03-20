package com.plotsquared.sponge.generator;

import com.flowpowered.math.vector.Vector3i;
import com.intellectualcrafters.plot.generator.AugmentedUtils;
import com.intellectualcrafters.plot.object.LazyResult;
import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;
import com.plotsquared.sponge.util.SpongeUtil;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.WorldGenerator;

import java.util.List;

public class SpongeAugmentedGenerator implements GenerationPopulator {
    
    private static SpongeAugmentedGenerator generator;

    private SpongeAugmentedGenerator() {
    }

    public static SpongeAugmentedGenerator get(World world) {
        WorldGenerator wg = world.getWorldGenerator();
        List<GenerationPopulator> populators = wg.getGenerationPopulators();
        for (GenerationPopulator poplator : populators) {
            if (poplator instanceof SpongeAugmentedGenerator) {
                return (SpongeAugmentedGenerator) poplator;
            }
        }
        if (generator == null) {
            generator = new SpongeAugmentedGenerator();
        }
        populators.add(generator);
        return generator;
    }
    
    @Override
    public void populate(final World world, final MutableBlockVolume terrain, final ImmutableBiomeArea biome) {
        Vector3i min = terrain.getBlockMin();
        final int bx = min.getX();
        final int bz = min.getZ();
        final int cx = bx >> 4;
        final int cz = bz >> 4;
        AugmentedUtils.generate(world.getName(), cx, cz, new LazyResult<PlotChunk<?>>() {
            @Override
            public PlotChunk<?> create() {
                ChunkWrapper wrap = SetQueue.IMP.new ChunkWrapper(world.getName(), cx, cz);
                return new PlotChunk<ChunkWrapper>(wrap) {
                    @Override
                    public ChunkWrapper getChunkAbs() {
                        return getChunkWrapper();
                    }
                    @Override
                    public void setBlock(int x, int y, int z, int id, byte data) {
                        terrain.setBlock(bx + x, y, bz + z, SpongeUtil.getBlockState(id, data));
                    }
                    @Override
                    public void setBiome(int x, int z, int biome) {
                        System.out.println("TODO set biome: " + biome); // TODO FIXME
                    }
                    @Override
                    public PlotChunk clone() {
                        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
                    }
                    @Override
                    public PlotChunk shallowClone() {
                        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
                    }
                    @Override
                    public void addToQueue() {}
                    @Override
                    public void flush(boolean fixLighting) {}
                };
            }
        });
    }
    
}
