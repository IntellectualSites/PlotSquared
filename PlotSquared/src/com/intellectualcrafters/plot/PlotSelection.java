package com.intellectualcrafters.plot;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashMap;

/**
 * Created by Citymonstret on 2014-10-12.
 */
public class PlotSelection {

    public static HashMap<String, PlotSelection> currentSelection = new HashMap<>();

    private PlotBlock[] plotBlocks;

    private int width;

    private Plot plot;

    public PlotSelection(int width, World world, Plot plot) {
        this.width = width;
        this.plot = plot;

        plotBlocks = new PlotBlock[(width * width) * (world.getMaxHeight() - 1)];

        Location
                bot = PlotHelper.getPlotBottomLocAbs(world, plot.getId()),
                top = PlotHelper.getPlotTopLocAbs(world, plot.getId());
        int
                minX = bot.getBlockX(),
                maxX = top.getBlockX(),
                minZ = bot.getBlockZ(),
                maxZ = top.getBlockZ(),
                minY = 1,
                maxY = world.getMaxHeight();
        Block current;

        int index = 0;
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = minY; y < maxY; y++) {
                    current = world.getBlockAt(x + 1, y, z + 1);
                    plotBlocks[index++] = new PlotBlock(
                            (short) current.getTypeId(),
                            current.getData()
                    );
                }
            }
        }
        //Yay :D
    }

    public PlotBlock[] getBlocks() {
        return plotBlocks;
    }

    public int getWidth() {
        return width;
    }

    public Plot getPlot() {
        return plot;
    }

    public void paste(World world, Plot plot) {
        Location
                bot = PlotHelper.getPlotBottomLocAbs(world, plot.getId()),
                top = PlotHelper.getPlotTopLocAbs(world, plot.getId());
        int
                minX = bot.getBlockX(),
                maxX = top.getBlockX(),
                minZ = bot.getBlockZ(),
                maxZ = top.getBlockZ(),
                minY = 1,
                maxY = world.getMaxHeight();
        int index = 0;
        PlotBlock current;
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                for (int y = minY; y < maxY; y++) {
                    current = plotBlocks[index++];
                    world.getBlockAt(x + 1, y, z + 1).setTypeIdAndData(current.id, current.data, true);
                }
            }
        }
    }
}
