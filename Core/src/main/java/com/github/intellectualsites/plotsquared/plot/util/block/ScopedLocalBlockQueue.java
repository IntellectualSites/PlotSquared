package com.github.intellectualsites.plotsquared.plot.util.block;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.PlotManager;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.sk89q.worldedit.world.block.BaseBlock;

public class ScopedLocalBlockQueue extends DelegateLocalBlockQueue {
    private final int minX;
    private final int minY;
    private final int minZ;

    private final int maxX;
    private final int maxY;
    private final int maxZ;

    private final int dx;
    private final int dy;
    private final int dz;

    public ScopedLocalBlockQueue(LocalBlockQueue parent, Location min, Location max) {
        super(parent);
        this.minX = min.getX();
        this.minY = min.getY();
        this.minZ = min.getZ();

        this.maxX = max.getX();
        this.maxY = max.getY();
        this.maxZ = max.getZ();

        this.dx = maxX - minX;
        this.dy = maxY - minY;
        this.dz = maxZ - minZ;
    }


    @Override public boolean setBiome(int x, int z, String biome) {
        return x >= 0 && x <= dx && z >= 0 && z <= dz && super.setBiome(x + minX, z + minZ, biome);
    }

    public void fillBiome(String biome) {
        for (int x = 0; x <= dx; x++) {
            for (int z = 0; z < dz; z++) {
                setBiome(x, z, biome);
            }
        }
    }

    @Override public boolean setBlock(int x, int y, int z, BaseBlock id) {
        return x >= 0 && x <= dx && y >= 0 && y <= dy && z >= 0 && z <= dz && super
            .setBlock(x + minX, y + minY, z + minZ, id);
    }

    @Override public boolean setBlock(int x, int y, int z, PlotBlock id) {
        return x >= 0 && x <= dx && y >= 0 && y <= dy && z >= 0 && z <= dz && super
            .setBlock(x + minX, y + minY, z + minZ, id);
    }

    public Location getMin() {
        return new Location(getWorld(), minX, minY, minZ);
    }

    public Location getMax() {
        return new Location(getWorld(), maxX, maxY, maxZ);
    }

    /**
     * Run a task for each x,z value corresponding to the plot at that location<br>
     * - Plot: The plot at the x,z (may be null)<br>
     * - Location: The location in the chunk (y = 0)<br>
     * - PlotChunk: Reference to this chunk object<br>
     *
     * @param task
     */
    public void mapByType2D(RunnableVal3<Plot, Integer, Integer> task) {
        int bx = minX;
        int bz = minZ;
        PlotArea area = PlotSquared.get().getPlotArea(getWorld(), null);
        Location location = new Location(getWorld(), bx, 0, bz);
        if (area != null) {
            PlotManager manager = area.getPlotManager();
            for (int x = 0; x < 16; x++) {
                location.setX(bx + x);
                for (int z = 0; z < 16; z++) {
                    location.setZ(bz + z);
                    task.run(area.getPlotAbs(location), x, z);
                }
            }
        } else {
            for (int x = 0; x < 16; x++) {
                location.setX(bx + x);
                for (int z = 0; z < 16; z++) {
                    location.setZ(bz + z);
                    task.run(location.getPlotAbs(), x, z);
                }
            }
        }
    }
}
