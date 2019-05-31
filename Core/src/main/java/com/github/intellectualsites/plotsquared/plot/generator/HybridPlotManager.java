package com.github.intellectualsites.plotsquared.plot.generator;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.commands.Template;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.ChunkManager;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;

public class HybridPlotManager extends ClassicPlotManager {

    public static boolean REGENERATIVE_CLEAR = true;

    private final HybridPlotWorld hybridPlotWorld;

    public HybridPlotManager(PlotArea plotArea) {
        super(plotArea);
        if (plotArea instanceof HybridPlotWorld){
            hybridPlotWorld = (HybridPlotWorld)plotArea;
        } else {
            throw new RuntimeException("HybridPlotManager requires plotArea to be an instance of HybridPlotWorld");
        }
    }

    @Override public void exportTemplate(PlotArea plotArea) throws IOException {
        HashSet<FileBytes> files = Sets.newHashSet(
            new FileBytes(Settings.Paths.TEMPLATES + "/tmp-data.yml", Template.getBytes(plotArea)));
        String dir = "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator
            + plotArea.worldname + File.separator;
        try {
            File sideroad =
                MainUtil.getFile(PlotSquared.get().IMP.getDirectory(), dir + "sideroad.schem");
            String newDir = "schematics" + File.separator + "GEN_ROAD_SCHEMATIC" + File.separator
                + "__TEMP_DIR__" + File.separator;
            if (sideroad.exists()) {
                files.add(new FileBytes(newDir + "sideroad.schem",
                    Files.readAllBytes(sideroad.toPath())));
            }
            File intersection =
                MainUtil.getFile(PlotSquared.get().IMP.getDirectory(), "intersection.schem");
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
        Template.zipAll(plotArea.worldname, files);
    }

    @Override public boolean createRoadEast(PlotArea plotArea, Plot plot) {
        super.createRoadEast(plotArea, plot);
        HybridPlotWorld hpw = (HybridPlotWorld) plotArea;
        PlotId id = plot.getId();
        PlotId id2 = new PlotId(id.x + 1, id.y);
        Location bot = getPlotBottomLocAbs(hpw, id2);
        Location top = getPlotTopLocAbs(hpw, id);
        Location pos1 = new Location(plotArea.worldname, top.getX() + 1, 0, bot.getZ() - 1);
        Location pos2 =
            new Location(plotArea.worldname, bot.getX(), Math.min(getWorldHeight(), 255),
                top.getZ() + 1);
        MainUtil.resetBiome(plotArea, pos1, pos2);
        if (!hpw.ROAD_SCHEMATIC_ENABLED) {
            return true;
        }
        LocalBlockQueue queue = hpw.getQueue(false);
        createSchemAbs(hpw, queue, pos1, pos2);
        queue.enqueue();
        return true;
    }

    private void createSchemAbs(HybridPlotWorld hpw, LocalBlockQueue queue, Location pos1,
        Location pos2) {
        int size = hpw.SIZE;
        int minY;
        if (Settings.Schematics.PASTE_ON_TOP) {
            minY = hpw.SCHEM_Y;
        } else {
            minY = 1;
        }
        BaseBlock airBlock = BlockTypes.AIR.getDefaultState().toBaseBlock();
        for (int x = pos1.getX(); x <= pos2.getX(); x++) {
            short absX = (short) ((x - hpw.ROAD_OFFSET_X) % size);
            if (absX < 0) {
                absX += size;
            }
            for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                short absZ = (short) ((z - hpw.ROAD_OFFSET_Z) % size);
                if (absZ < 0) {
                    absZ += size;
                }
                BaseBlock[] blocks = hpw.G_SCH.get(MathMan.pair(absX, absZ));
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
            }
        }
    }

    @Override public boolean createRoadSouth(PlotArea plotArea, Plot plot) {
        super.createRoadSouth(plotArea, plot);
        HybridPlotWorld hpw = (HybridPlotWorld) plotArea;
        PlotId id = plot.getId();
        PlotId id2 = new PlotId(id.x, id.y + 1);
        Location bot = getPlotBottomLocAbs(hpw, id2);
        Location top = getPlotTopLocAbs(hpw, id);
        Location pos1 = new Location(plotArea.worldname, bot.getX() - 1, 0, top.getZ() + 1);
        Location pos2 =
            new Location(plotArea.worldname, top.getX() + 1, Math.min(getWorldHeight(), 255),
                bot.getZ());
        MainUtil.resetBiome(plotArea, pos1, pos2);
        if (!hpw.ROAD_SCHEMATIC_ENABLED) {
            return true;
        }
        LocalBlockQueue queue = hpw.getQueue(false);
        createSchemAbs(hpw, queue, pos1, pos2);
        queue.enqueue();
        return true;
    }

    @Override public boolean createRoadSouthEast(PlotArea plotArea, Plot plot) {
        super.createRoadSouthEast(plotArea, plot);
        HybridPlotWorld hpw = (HybridPlotWorld) plotArea;
        PlotId id = plot.getId();
        PlotId id2 = new PlotId(id.x + 1, id.y + 1);
        Location pos1 = getPlotTopLocAbs(hpw, id).add(1, 0, 1);
        Location pos2 = getPlotBottomLocAbs(hpw, id2);
        pos1.setY(0);
        pos2.setY(Math.min(getWorldHeight(), 255));
        LocalBlockQueue queue = hpw.getQueue(false);
        createSchemAbs(hpw, queue, pos1, pos2);
        if (hpw.ROAD_SCHEMATIC_ENABLED) {
            createSchemAbs(hpw, queue, pos1, pos2);
        }
        queue.enqueue();
        return true;
    }

    /**
     * <p>Clearing the plot needs to only consider removing the blocks - This implementation has
     * used the setCuboidAsync function, as it is fast, and uses NMS code - It also makes use of the
     * fact that deleting chunks is a lot faster than block updates This code is very messy, but you
     * don't need to do something quite as complex unless you happen to have 512x512 sized plots.
     * </p>
     */
    @Override public boolean clearPlot(final PlotArea plotArea, Plot plot,
        final Runnable whenDone) {
        final String world = plotArea.worldname;
        final HybridPlotWorld dpw = (HybridPlotWorld) plotArea;
        Location pos1 = plot.getBottomAbs();
        Location pos2 = plot.getExtendedTopAbs();
        // If augmented
        final boolean canRegen =
            (plotArea.TYPE == 0) && (plotArea.TERRAIN == 0) && REGENERATIVE_CLEAR;
        // The component blocks
        final BlockBucket plotfloor = dpw.TOP_BLOCK;
        final BlockBucket filling = dpw.MAIN_BLOCK;
        final PlotBlock bedrock;
        if (dpw.PLOT_BEDROCK) {
            bedrock = PlotBlock.get((short) 7, (byte) 0);
        } else {
            bedrock = PlotBlock.get((short) 0, (byte) 0);
        }
        final PlotBlock air = PlotBlock.get((short) 0, (byte) 0);
        final String biome = dpw.PLOT_BIOME;
        final LocalBlockQueue queue = plotArea.getQueue(false);
        ChunkManager.chunkTask(pos1, pos2, new RunnableVal<int[]>() {
            @Override public void run(int[] value) {
                // If the chunk isn't near the edge and it isn't an augmented world we can just regen the whole chunk
                if (canRegen && (value[6] == 0) && PlotSquared.imp().getServerVersion()[1] == 13) {
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
                top.setY(dpw.PLOT_HEIGHT);
                queue.setCuboid(bot, top, filling);
                bot.setY(dpw.PLOT_HEIGHT);
                top.setY(dpw.PLOT_HEIGHT + 1);
                queue.setCuboid(bot, top, plotfloor);
                bot.setY(dpw.PLOT_HEIGHT + 1);
                top.setY(getWorldHeight());
                queue.setCuboid(bot, top, air);
                // And finally set the schematic, the y value is unimportant for this function
                pastePlotSchematic(dpw, queue, bot, top);
            }
        }, () -> {
            queue.enqueue();
            // And notify whatever called this when plot clearing is done
            GlobalBlockQueue.IMP.addTask(whenDone);
        }, 10);
        return true;
    }

    public void pastePlotSchematic(HybridPlotWorld plotWorld, LocalBlockQueue queue,
        Location bottom, Location top) {
        if (!plotWorld.PLOT_SCHEMATIC) {
            return;
        }
        createSchemAbs(plotWorld, queue, bottom, top);
    }

    /**
     * Remove sign for a plot.
     */
    @Override public Location getSignLoc(PlotArea plotArea, Plot plot) {
        HybridPlotWorld dpw = (HybridPlotWorld) plotArea;
        return dpw.getSignLocation(plot);
    }
}
