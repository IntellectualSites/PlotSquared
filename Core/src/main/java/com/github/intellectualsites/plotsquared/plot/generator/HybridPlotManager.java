package com.github.intellectualsites.plotsquared.plot.generator;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.commands.Template;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.FileBytes;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.util.ChunkManager;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.world.BlockUtil;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;

public class HybridPlotManager extends ClassicPlotManager {

    public static boolean REGENERATIVE_CLEAR = true;

    private final HybridPlotWorld hybridPlotWorld;

    public HybridPlotManager(HybridPlotWorld hybridPlotWorld) {
        super(hybridPlotWorld);
        this.hybridPlotWorld = hybridPlotWorld;
    }

    @Override public void exportTemplate() throws IOException {
        HashSet<FileBytes> files = Sets.newHashSet(
            new FileBytes(Settings.Paths.TEMPLATES + "/tmp-data.yml",
                Template.getBytes(hybridPlotWorld)));
        String dir = "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator
            + hybridPlotWorld.worldname + File.separator;
        try {
            File sideRoad =
                MainUtil.getFile(PlotSquared.get().IMP.getDirectory(), dir + "sideroad.schem");
            String newDir = "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator
                + "__TEMP_DIR__" + File.separator;
            if (sideRoad.exists()) {
                files.add(new FileBytes(newDir + "sideroad.schem",
                    Files.readAllBytes(sideRoad.toPath())));
            }
            File intersection =
                MainUtil.getFile(PlotSquared.get().IMP.getDirectory(), dir + "intersection.schem");
            if (intersection.exists()) {
                files.add(new FileBytes(newDir + "intersection.schem",
                    Files.readAllBytes(intersection.toPath())));
            }
            File plot = MainUtil.getFile(PlotSquared.get().IMP.getDirectory(), dir + "plot.schem");
            if (plot.exists()) {
                files.add(new FileBytes(newDir + "plot.schem", Files.readAllBytes(plot.toPath())));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Template.zipAll(hybridPlotWorld.worldname, files);
    }

    @Override public boolean createRoadEast(Plot plot) {
        super.createRoadEast(plot);
        PlotId id = plot.getId();
        PlotId id2 = new PlotId(id.x + 1, id.y);
        Location bot = getPlotBottomLocAbs(id2);
        Location top = getPlotTopLocAbs(id);
        Location pos1 = new Location(hybridPlotWorld.worldname, top.getX() + 1, 0, bot.getZ() - 1);
        Location pos2 =
            new Location(hybridPlotWorld.worldname, bot.getX(), Math.min(getWorldHeight(), 255),
                top.getZ() + 1);
        MainUtil.resetBiome(hybridPlotWorld, pos1, pos2);
        if (!hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
            return true;
        }
        LocalBlockQueue queue = hybridPlotWorld.getQueue(false);
        createSchemAbs(queue, pos1, pos2);
        queue.enqueue();
        return true;
    }

    private void createSchemAbs(LocalBlockQueue queue, Location pos1, Location pos2) {
        int size = hybridPlotWorld.SIZE;
        int minY;
        if (Settings.Schematics.PASTE_ON_TOP) {
            minY = hybridPlotWorld.SCHEM_Y;
        } else {
            minY = 1;
        }
        BaseBlock airBlock = BlockTypes.AIR.getDefaultState().toBaseBlock();
        for (int x = pos1.getX(); x <= pos2.getX(); x++) {
            short absX = (short) ((x - hybridPlotWorld.ROAD_OFFSET_X) % size);
            if (absX < 0) {
                absX += size;
            }
            for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                short absZ = (short) ((z - hybridPlotWorld.ROAD_OFFSET_Z) % size);
                if (absZ < 0) {
                    absZ += size;
                }
                BaseBlock[] blocks = hybridPlotWorld.G_SCH.get(MathMan.pair(absX, absZ));
                if (blocks != null) {
                    for (int y = 0; y < blocks.length; y++) {
                        if (blocks[y] != null) {
                            queue.setBlock(x, minY + y, z, blocks[y]);
                        } else {
                            // This is necessary, otherwise any blocks not specified in the schematic will remain after a clear
                            queue.setBlock(x, minY + y, z, airBlock);
                        }
                    }
                }
                BiomeType biome = hybridPlotWorld.G_SCH_B.get(MathMan.pair(absX, absZ));
                if (biome != null) {
                    queue.setBiome(x, z, biome);
                } else {
                    queue.setBiome(x, z, hybridPlotWorld.PLOT_BIOME);
                }
            }
        }
    }

    @Override public boolean createRoadSouth(Plot plot) {
        super.createRoadSouth(plot);
        PlotId id = plot.getId();
        PlotId id2 = new PlotId(id.x, id.y + 1);
        Location bot = getPlotBottomLocAbs(id2);
        Location top = getPlotTopLocAbs(id);
        Location pos1 = new Location(hybridPlotWorld.worldname, bot.getX() - 1, 0, top.getZ() + 1);
        Location pos2 =
            new Location(hybridPlotWorld.worldname, top.getX() + 1, Math.min(getWorldHeight(), 255),
                bot.getZ());
        MainUtil.resetBiome(hybridPlotWorld, pos1, pos2);
        if (!hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
            return true;
        }
        LocalBlockQueue queue = hybridPlotWorld.getQueue(false);
        createSchemAbs(queue, pos1, pos2);
        queue.enqueue();
        return true;
    }

    @Override public boolean createRoadSouthEast(Plot plot) {
        super.createRoadSouthEast(plot);
        PlotId id = plot.getId();
        PlotId id2 = new PlotId(id.x + 1, id.y + 1);
        Location pos1 = getPlotTopLocAbs(id).add(1, 0, 1);
        Location pos2 = getPlotBottomLocAbs(id2);
        pos1.setY(0);
        pos2.setY(Math.min(getWorldHeight(), 255));
        LocalBlockQueue queue = hybridPlotWorld.getQueue(false);
        createSchemAbs(queue, pos1, pos2);
        if (hybridPlotWorld.ROAD_SCHEMATIC_ENABLED) {
            createSchemAbs(queue, pos1, pos2);
        }
        return queue.enqueue();
    }

    /**
     * <p>Clearing the plot needs to only consider removing the blocks - This implementation has
     * used the setCuboidAsync function, as it is fast, and uses NMS code - It also makes use of the
     * fact that deleting chunks is a lot faster than block updates This code is very messy, but you
     * don't need to do something quite as complex unless you happen to have 512x512 sized plots.
     * </p>
     */
    @Override public boolean clearPlot(Plot plot, final Runnable whenDone) {
        final String world = hybridPlotWorld.worldname;
        Location pos1 = plot.getBottomAbs();
        Location pos2 = plot.getExtendedTopAbs();
        // If augmented
        final boolean canRegen =
            (hybridPlotWorld.TYPE == 0) && (hybridPlotWorld.TERRAIN == 0) && REGENERATIVE_CLEAR;
        // The component blocks
        final Pattern plotfloor = hybridPlotWorld.TOP_BLOCK.toPattern();
        final Pattern filling = hybridPlotWorld.MAIN_BLOCK.toPattern();
        final BlockState bedrock;
        if (hybridPlotWorld.PLOT_BEDROCK) {
            bedrock = BlockUtil.get((short) 7, (byte) 0);
        } else {
            bedrock = BlockUtil.get((short) 0, (byte) 0);
        }
        final BlockState air = BlockUtil.get((short) 0, (byte) 0);
        final BiomeType biome = hybridPlotWorld.PLOT_BIOME;
        final LocalBlockQueue queue = hybridPlotWorld.getQueue(false);
        ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
            @Override public void run(int[] value) {
                // If the chunk isn't near the edge and it isn't an augmented world we can just regen the whole chunk
                if (canRegen && (value[6] == 0)) {
                    queue.regenChunk(value[0], value[1]);
                    return;
                }
                /* Otherwise we need to set each component, as we don't want to regenerate the road or other plots that share the same chunk.*/
                // Set the biome
                MainUtil.setBiome(world, value[2], value[3], value[4], value[5], biome);
                // These two locations are for each component (e.g. bedrock, main block, floor, air)
                Location bot = new Location(world, value[2], 0, value[3]);
                Location top = new Location(world, value[4], 1, value[5]);
                queue.setCuboid(bot, top, bedrock);
                // Each component has a different layer
                bot.setY(1);
                top.setY(hybridPlotWorld.PLOT_HEIGHT);
                queue.setCuboid(bot, top, filling);
                bot.setY(hybridPlotWorld.PLOT_HEIGHT);
                top.setY(hybridPlotWorld.PLOT_HEIGHT + 1);
                queue.setCuboid(bot, top, plotfloor);
                bot.setY(hybridPlotWorld.PLOT_HEIGHT + 1);
                top.setY(getWorldHeight());
                queue.setCuboid(bot, top, air);
                // And finally set the schematic, the y value is unimportant for this function
                pastePlotSchematic(queue, bot, top);
            }
        }, () -> {
            queue.enqueue();
            // And notify whatever called this when plot clearing is done
            GlobalBlockQueue.IMP.addEmptyTask(whenDone);
        }, 10);
        return true;
    }

    public void pastePlotSchematic(LocalBlockQueue queue, Location bottom, Location top) {
        if (!hybridPlotWorld.PLOT_SCHEMATIC) {
            return;
        }
        createSchemAbs(queue, bottom, top);
    }

    /**
     * Retrieves the location of where a sign should be for a plot.
     *
     * @param plot The plot
     * @return The location where a sign should be
     */
    @Override public Location getSignLoc(Plot plot) {
        return hybridPlotWorld.getSignLocation(plot);
    }
}
