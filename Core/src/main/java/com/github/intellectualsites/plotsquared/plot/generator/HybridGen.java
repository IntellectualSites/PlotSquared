package com.github.intellectualsites.plotsquared.plot.generator;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.block.ScopedLocalBlockQueue;
import com.sk89q.worldedit.world.block.BaseBlock;

import java.util.HashMap;

public class HybridGen extends IndependentPlotGenerator {

    @Override public String getName() {
        return PlotSquared.imp().getPluginName();
    }

    private void placeSchem(HybridPlotWorld world, ScopedLocalBlockQueue result, short relativeX,
        short relativeZ, int x, int z) {
        int minY = Math.min(world.PLOT_HEIGHT, world.ROAD_HEIGHT);
        BaseBlock[] blocks = world.G_SCH.get(MathMan.pair(relativeX, relativeZ));
        if (blocks != null) {
            for (int y = 0; y < blocks.length; y++) {
                if (blocks[y] != null) {
                    result.setBlock(x, minY + y, z, blocks[y]);
                }
            }
        }
    }

    @Override public void generateChunk(ScopedLocalBlockQueue result, PlotArea settings) {
        HybridPlotWorld hpw = (HybridPlotWorld) settings;
        // Biome
        result.fillBiome(hpw.PLOT_BIOME);
        // Bedrock
        if (hpw.PLOT_BEDROCK) {
            for (short x = 0; x < 16; x++) {
                for (short z = 0; z < 16; z++) {
                    result.setBlock(x, 0, z, PlotBlock.get("bedrock"));
                }
            }
        }
        // Coords
        Location min = result.getMin();
        int cx = min.getX() >> 4;
        int cz = min.getZ() >> 4;
        int bx = (min.getX()) - hpw.ROAD_OFFSET_X;
        int bz = (min.getZ()) - hpw.ROAD_OFFSET_Z;
        short rbx;
        if (bx < 0) {
            rbx = (short) (hpw.SIZE + (bx % hpw.SIZE));
        } else {
            rbx = (short) (bx % hpw.SIZE);
        }
        short rbz;
        if (bz < 0) {
            rbz = (short) (hpw.SIZE + (bz % hpw.SIZE));
        } else {
            rbz = (short) (bz % hpw.SIZE);
        }
        short[] rx = new short[16];
        boolean[] gx = new boolean[16];
        boolean[] wx = new boolean[16];
        for (short i = 0; i < 16; i++) {
            short v = (short) (rbx + i);
            if (v >= hpw.SIZE) {
                v -= hpw.SIZE;
            }
            rx[i] = v;
            if (hpw.ROAD_WIDTH != 0) {
                gx[i] = v < hpw.PATH_WIDTH_LOWER || v > hpw.PATH_WIDTH_UPPER;
                wx[i] = v == hpw.PATH_WIDTH_LOWER || v == hpw.PATH_WIDTH_UPPER;
            }
        }
        short[] rz = new short[16];
        boolean[] gz = new boolean[16];
        boolean[] wz = new boolean[16];
        for (short i = 0; i < 16; i++) {
            short v = (short) (rbz + i);
            if (v >= hpw.SIZE) {
                v -= hpw.SIZE;
            }
            rz[i] = v;
            if (hpw.ROAD_WIDTH != 0) {
                gz[i] = v < hpw.PATH_WIDTH_LOWER || v > hpw.PATH_WIDTH_UPPER;
                wz[i] = v == hpw.PATH_WIDTH_LOWER || v == hpw.PATH_WIDTH_UPPER;
            }
        }
        // generation
        HashMap<Integer, BaseBlock[]> sch = hpw.G_SCH;
        for (short x = 0; x < 16; x++) {
            if (gx[x]) {
                for (short z = 0; z < 16; z++) {
                    // Road
                    for (int y = 1; y <= hpw.ROAD_HEIGHT; y++) {
                        result.setBlock(x, y, z, hpw.ROAD_BLOCK.getBlock());
                    }
                    if (hpw.ROAD_SCHEMATIC_ENABLED) {
                        placeSchem(hpw, result, rx[x], rz[z], x, z);
                    }
                }
            } else if (wx[x]) {
                for (short z = 0; z < 16; z++) {
                    if (gz[z]) {
                        // road
                        for (int y = 1; y <= hpw.ROAD_HEIGHT; y++) {
                            result.setBlock(x, y, z, hpw.ROAD_BLOCK.getBlock());
                        }
                        if (hpw.ROAD_SCHEMATIC_ENABLED) {
                            placeSchem(hpw, result, rx[x], rz[z], x, z);
                        }
                    } else {
                        // wall
                        for (int y = 1; y <= hpw.WALL_HEIGHT; y++) {
                            result.setBlock(x, y, z, hpw.WALL_FILLING.getBlock());
                        }
                        if (!hpw.ROAD_SCHEMATIC_ENABLED) {
                            result.setBlock(x, hpw.WALL_HEIGHT + 1, z, hpw.WALL_BLOCK.getBlock());
                        } else {
                            placeSchem(hpw, result, rx[x], rz[z], x, z);
                        }
                    }
                }
            } else {
                for (short z = 0; z < 16; z++) {
                    if (gz[z]) {
                        // road
                        for (int y = 1; y <= hpw.ROAD_HEIGHT; y++) {
                            result.setBlock(x, y, z, hpw.ROAD_BLOCK.getBlock());
                        }
                        if (hpw.ROAD_SCHEMATIC_ENABLED) {
                            placeSchem(hpw, result, rx[x], rz[z], x, z);
                        }
                    } else if (wz[z]) {
                        // wall
                        for (int y = 1; y <= hpw.WALL_HEIGHT; y++) {
                            result.setBlock(x, y, z, hpw.WALL_FILLING.getBlock());
                        }
                        if (!hpw.ROAD_SCHEMATIC_ENABLED) {
                            result.setBlock(x, hpw.WALL_HEIGHT + 1, z, hpw.WALL_BLOCK.getBlock());
                        } else {
                            placeSchem(hpw, result, rx[x], rz[z], x, z);
                        }
                    } else {
                        // plot
                        for (int y = 1; y < hpw.PLOT_HEIGHT; y++) {
                            result.setBlock(x, y, z, hpw.MAIN_BLOCK.getBlock());
                        }
                        result.setBlock(x, hpw.PLOT_HEIGHT, z, hpw.TOP_BLOCK.getBlock());
                        if (hpw.PLOT_SCHEMATIC) {
                            placeSchem(hpw, result, rx[x], rz[z], x, z);
                        }
                    }
                }
            }
        }
    }

/*    @Override public boolean populateChunk(ScopedLocalBlockQueue result, PlotArea settings) {
        HybridPlotWorld hpw = (HybridPlotWorld) settings;
        if (hpw.G_SCH_STATE != null) {
            Location min = result.getMin();
            int cx = min.getX() >> 4;
            int cz = min.getZ() >> 4;
            int p1x = cx << 4;
            int p1z = cz << 4;
            int bx = p1x - hpw.ROAD_OFFSET_X;
            int bz = p1z - hpw.ROAD_OFFSET_Z;
            short rbx;
            if (bx < 0) {
                rbx = (short) (hpw.SIZE + (bx % hpw.SIZE));
            } else {
                rbx = (short) (bx % hpw.SIZE);
            }
            short rbz;
            if (bz < 0) {
                rbz = (short) (hpw.SIZE + (bz % hpw.SIZE));
            } else {
                rbz = (short) (bz % hpw.SIZE);
            }
            short[] rx = new short[16];
            for (short i = 0; i < 16; i++) {
                short v = (short) (rbx + i);
                if (v >= hpw.SIZE) {
                    v -= hpw.SIZE;
                }
                rx[i] = v;
            }
            short[] rz = new short[16];
            for (short i = 0; i < 16; i++) {
                short v = (short) (rbz + i);
                if (v >= hpw.SIZE) {
                    v -= hpw.SIZE;
                }
                rz[i] = v;
            }
            LocalBlockQueue queue = null;
            for (short x = 0; x < 16; x++) {
                for (short z = 0; z < 16; z++) {
                    int pair = MathMan.pair(rx[x], rz[z]);
                    HashMap<Integer, CompoundTag> map = hpw.G_SCH_STATE.get(pair);
                    if (map != null) {
                        for (Entry<Integer, CompoundTag> entry : map.entrySet()) {
                            if (queue == null) {
                                queue = GlobalBlockQueue.IMP.getNewQueue(hpw.worldname, false);
                            }
                            CompoundTag tag = entry.getValue();
                            SchematicHandler.manager
                                .restoreTile(queue, tag, p1x + x, entry.getKey(), p1z + z);
                        }
                    }
                }
            }
            if (queue != null) {
                queue.flush();
            }
        }
        return false;
    }*/

    @Override public PlotArea getNewPlotArea(String world, String id, PlotId min, PlotId max) {
        return new HybridPlotWorld(world, id, this, min, max);
    }

    @Override public PlotManager getNewPlotManager() {
        return new HybridPlotManager();
    }

    @Override public void initialize(PlotArea area) {
        // All initialization is done in the PlotArea class
    }
}
