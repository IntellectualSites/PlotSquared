package com.intellectualcrafters.plot.generator;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.PlotChunk;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.SetQueue.ChunkWrapper;

import java.util.Set;

public class AugmentedUtils {
    
    private static boolean enabled = true;
    
    public static void bypass(boolean bypass, Runnable run) {
        enabled = bypass;
        run.run();
        enabled = true;
    }

    public static boolean generate(final String world, final int cx, final int cz, LazyResult<PlotChunk<?>> lazyChunk) {
        if (!enabled) {
            return false;
        }
        if (lazyChunk == null) {
            lazyChunk = new LazyResult<PlotChunk<?>>() {
                @Override
                public PlotChunk<?> create() {
                    return SetQueue.IMP.queue.getChunk(SetQueue.IMP.new ChunkWrapper(world, cx, cz));
                }
            };
        }
        final int bx = cx << 4;
        final int bz = cz << 4;
        RegionWrapper region = new RegionWrapper(bx, bx + 15, bz, bz + 15);
        Set<PlotArea> areas = PS.get().getPlotAreas(world, region);
        if (areas.isEmpty()) {
            return false;
        }
        final PseudoRandom r = new PseudoRandom();
        r.state = (cx << 16) | (cz & 0xFFFF);
        ChunkWrapper wrap = SetQueue.IMP.new ChunkWrapper(world, cx, cz);
        boolean toReturn = false;
        for (final PlotArea area : areas) {
            if (area.TYPE == 0) {
                return false;
            }
            if (area.TERRAIN == 3) {
                continue;
            }
            IndependentPlotGenerator generator = area.getGenerator();
            if (generator == null) {
                continue;
            }
            final PlotChunk<?> result = lazyChunk.getOrCreate();
            final PlotChunk<?> primaryMask;
            // coords
            int bxx,bzz,txx,tzz;
            // gen
            if (area.TYPE == 2) {
                bxx = Math.max(0, area.getRegion().minX - bx);
                bzz = Math.max(0, area.getRegion().minZ - bz);
                txx = Math.min(15, area.getRegion().maxX - bx);
                tzz = Math.min(15, area.getRegion().maxZ - bz);
                primaryMask = new PlotChunk<Object>(wrap) {
                    @Override
                    public Object getChunkAbs() {
                        return null;
                    }
                    
                    @Override
                    public void setBlock(int x, int y, int z, int id, byte data) {
                        if (area.contains(bx + x, bz + z)) {
                            result.setBlock(x, y, z, id, data);
                        }
                    }
                    
                    @Override
                    public void setBiome(int x, int z, int biome) {
                        if (area.contains(bx + x, bz + z)) {
                            result.setBiome(x, z, biome);
                        }
                    }
                    
                    @Override
                    public PlotChunk clone() {
                        return null;
                    }
                    
                    @Override
                    public PlotChunk shallowClone() {
                        return null;
                    }
                };
            } else {
                bxx = bzz = 0;
                txx = tzz = 15;
                primaryMask = result;
            }
            PlotChunk<?> secondaryMask;
            PlotBlock air = new PlotBlock((short) 0, (byte) 0);
            if (area.TERRAIN == 2) {
                PlotManager manager = area.getPlotManager();
                final boolean[][] canPlace = new boolean[16][16];
                boolean has = false;
                for (int x = bxx; x <= txx; x++) {
                    for (int z = bzz; z <= tzz; z++) {
                        int rx = x + bx;
                        int rz = z + bz;
                        boolean can = manager.getPlotIdAbs(area, rx, 0, rz) == null;
                        if (can) {
                            for (int y = 1; y < 128; y++) {
                                result.setBlock(x, y, z, air);
                            }
                            canPlace[x][z] = can;
                            has = true;
                        }
                    }
                }
                if (!has) {
                    continue;
                } else {
                    toReturn = true;
                }
                secondaryMask = new PlotChunk<Object>(wrap) {
                    @Override
                    public Object getChunkAbs() {
                        return null;
                    }
                    
                    @Override
                    public void setBlock(int x, int y, int z, int id, byte data) {
                        if (canPlace[x][z]) {
                            primaryMask.setBlock(x, y, z, id, data);
                        }
                    }
                    
                    @Override
                    public void setBiome(int x, int z, int biome) {}
                    
                    @Override
                    public PlotChunk clone() {
                        return null;
                    }
                    
                    @Override
                    public PlotChunk shallowClone() {
                        return null;
                    }
                };
            } else {
                secondaryMask = primaryMask;
                for (int x = bxx; x <= txx; x++) {
                    for (int z = bzz; z <= tzz; z++) {
                        for (int y = 1; y < 128; y++) {
                            result.setBlock(x, y, z, air);
                        }
                    }
                }
                toReturn = true;
            }
            generator.generateChunk(secondaryMask, area, r);
        }
        if (lazyChunk.get() != null) {
            lazyChunk.get().addToQueue();
            lazyChunk.get().flush(false);
        }
        return toReturn;
    }
}
