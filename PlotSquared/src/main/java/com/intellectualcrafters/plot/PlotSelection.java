package com.intellectualcrafters.plot;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

/**
 * Created by Citymonstret on 2014-10-12.
 */
public class PlotSelection {

    public static HashMap<String, PlotSelection> currentSelection = new HashMap<>();

    private final PlotBlock[]                    plotBlocks;

    private final int                            width;

    private final Plot                           plot;

    private final Biome                          biome;

    public PlotSelection(final int width, final World world, final Plot plot) {
        this.width = width;
        this.plot = plot;

        this.plotBlocks = new PlotBlock[(width * width) * (world.getMaxHeight() - 1)];

        final Location bot = PlotHelper.getPlotBottomLocAbs(world, plot.getId()), top = PlotHelper.getPlotTopLocAbs(world, plot.getId());
        final int minX = bot.getBlockX(), maxX = top.getBlockX(), minZ = bot.getBlockZ(), maxZ = top.getBlockZ(), minY = 1, maxY = world.getMaxHeight();
        Block current;

        this.biome = world.getBiome(minX, minZ);

        int index = 0;
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = minY; y < maxY; y++) {
                    current = world.getBlockAt(x + 1, y, z + 1);
                    this.plotBlocks[index++] = new PlotBlock((short) current.getTypeId(), current.getData());
                }
            }
        }

        // Yay :D
    }

    public PlotBlock[] getBlocks() {
        return this.plotBlocks;
    }

    public int getWidth() {
        return this.width;
    }

    public Plot getPlot() {
        return this.plot;
    }

    public static boolean swap(final World world, final PlotId id1, final PlotId id2) {

        final Location bot2 = PlotHelper.getPlotBottomLocAbs(world, id2).add(1, 0, 1);
        final Location bot1 = PlotHelper.getPlotBottomLocAbs(world, id1).add(1, 0, 1);
        final Location top1 = PlotHelper.getPlotTopLocAbs(world, id1);

        final int minX = bot1.getBlockX(), maxX = top1.getBlockX(), minZ = bot1.getBlockZ(), maxZ = top1.getBlockZ(),

                minX2 = bot2.getBlockX(), minZ2 = bot2.getBlockZ();

        final boolean canSetFast = PlotHelper.canSetFast;

        for (int x = 0; x <= (maxX - minX); x++) {
            for (int z = 0; z <= (maxZ - minZ); z++) {
                for (int y = 1; y <= world.getMaxHeight(); y++) {

                    final Block block1 = world.getBlockAt(x + minX, y, z + minZ);
                    final Block block2 = world.getBlockAt(x + minX2, y, z + minZ2);

                    final BlockWrapper b1 = wrapBlock(block1);
                    final BlockWrapper b2 = wrapBlock(block2);

                    if ((b1.id != b2.id) || (b1.data != b2.data)) {
                        if (canSetFast) {
                            try {
                                SetBlockFast.set(world, b1.x, b1.y, b1.z, b2.id, b2.data);
                                SetBlockFast.set(world, b2.x, b2.y, b2.z, b1.id, b1.data);
                            }
                            catch (final NoSuchMethodException e) {
                                PlotHelper.canSetFast = false;
                            }
                        }
                        else {
                            if ((b1.id != b2.id) && (b1.data != b2.data)) {
                                block1.setTypeIdAndData(b2.id, b2.data, false);
                                block2.setTypeIdAndData(b1.id, b1.data, false);
                            }
                            else if (b1.id != b2.id) {
                                block1.setTypeId(b2.id);
                                block2.setTypeId(b1.id);
                            }
                            else {
                                block1.setData(b2.data);
                                block2.setData(b1.data);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private static BlockWrapper wrapBlock(final Block block) {
        return new BlockWrapper(block.getX(), block.getY(), block.getZ(), (short) block.getTypeId(), block.getData());
    }

    public void paste(final World world, final Plot plot) {

        final Location bot = PlotHelper.getPlotBottomLocAbs(world, plot.getId()), top = PlotHelper.getPlotTopLocAbs(world, plot.getId());
        final int minX = bot.getBlockX(), maxX = top.getBlockX(), minZ = bot.getBlockZ(), maxZ = top.getBlockZ(), minY = 1, maxY = world.getMaxHeight();

        if (this.biome != world.getBiome(minX, minZ)) {
            PlotHelper.setBiome(world, plot, this.biome);
        }

        int index = 0;
        PlotBlock current;
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = minY; y < maxY; y++) {
                    current = this.plotBlocks[index++];
                    world.getBlockAt(x + 1, y, z + 1).setTypeIdAndData(current.id, current.data, true);
                }
            }
        }
    }
}
