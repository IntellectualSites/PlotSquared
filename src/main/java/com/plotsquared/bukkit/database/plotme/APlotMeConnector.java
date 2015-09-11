package com.plotsquared.bukkit.database.plotme;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.intellectualcrafters.configuration.file.FileConfiguration;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;

public abstract class APlotMeConnector
{
    public abstract Connection getPlotMeConnection(final String plugin, final FileConfiguration plotConfig, final String dataFolder);

    public abstract HashMap<String, HashMap<PlotId, Plot>> getPlotMePlots(final Connection connection) throws SQLException;

    public abstract boolean accepts(final String version);

    public String getWorld(final String world)
    {
        for (final World newworld : Bukkit.getWorlds())
        {
            if (newworld.getName().equalsIgnoreCase(world)) { return newworld.getName(); }
        }
        return world;
    }

    public boolean isValidConnection(final Connection connection)
    {
        return connection != null;
    }

    public void copyConfig(final FileConfiguration plotConfig, final String world, final String actualWorldName)
    {
        final Integer pathwidth = plotConfig.getInt("worlds." + world + ".PathWidth"); //
        PS.get().config.set("worlds." + actualWorldName + ".road.width", pathwidth);
        final Integer plotsize = plotConfig.getInt("worlds." + world + ".PlotSize"); //
        PS.get().config.set("worlds." + actualWorldName + ".plot.size", plotsize);
        final String wallblock = plotConfig.getString("worlds." + world + ".WallBlockId"); //
        PS.get().config.set("worlds." + actualWorldName + ".wall.block", wallblock);
        final String floor = plotConfig.getString("worlds." + world + ".PlotFloorBlockId"); //
        PS.get().config.set("worlds." + actualWorldName + ".plot.floor", Arrays.asList(floor));
        final String filling = plotConfig.getString("worlds." + world + ".PlotFillingBlockId"); //
        PS.get().config.set("worlds." + actualWorldName + ".plot.filling", Arrays.asList(filling));
        final String road = plotConfig.getString("worlds." + world + ".RoadMainBlockId");
        PS.get().config.set("worlds." + actualWorldName + ".road.block", road);
        Integer height = plotConfig.getInt("worlds." + world + ".RoadHeight"); //
        if (height == null)
        {
            height = 64;
        }
        PS.get().config.set("worlds." + actualWorldName + ".road.height", height);
    }

    public Location getPlotTopLocAbs(final int path, final int plot, final PlotId plotid)
    {
        final int px = plotid.x;
        final int pz = plotid.y;
        final int x = (px * (path + plot)) - ((int) Math.floor(path / 2)) - 1;
        final int z = (pz * (path + plot)) - ((int) Math.floor(path / 2)) - 1;
        return new Location(null, x, 256, z);
    }

    public Location getPlotBottomLocAbs(final int path, final int plot, final PlotId plotid)
    {
        final int px = plotid.x;
        final int pz = plotid.y;
        final int x = (px * (path + plot)) - plot - ((int) Math.floor(path / 2)) - 1;
        final int z = (pz * (path + plot)) - plot - ((int) Math.floor(path / 2)) - 1;
        return new Location(null, x, 1, z);
    }

    public void setMerged(final HashMap<String, HashMap<PlotId, boolean[]>> merges, final String world, final PlotId id, final int direction)
    {
        final HashMap<PlotId, boolean[]> plots = merges.get(world);
        PlotId id2;
        switch (direction)
        {
            case 0:
            {
                id2 = new PlotId(id.x, id.y);
                break;
            }
            case 1:
            {
                id2 = new PlotId(id.x, id.y);
                break;
            }
            case 2:
            {
                id2 = new PlotId(id.x, id.y);
                break;
            }
            case 3:
            {
                id2 = new PlotId(id.x, id.y);
                break;
            }
            default:
            {
                return;
            }
        }
        boolean[] merge1;
        boolean[] merge2;
        if (plots.containsKey(id))
        {
            merge1 = plots.get(id);
        }
        else
        {
            merge1 = new boolean[] { false, false, false, false };
        }
        if (plots.containsKey(id2))
        {
            merge2 = plots.get(id2);
        }
        else
        {
            merge2 = new boolean[] { false, false, false, false };
        }
        merge1[direction] = true;
        merge2[(direction + 2) % 4] = true;
        plots.put(id, merge1);
        plots.put(id2, merge1);
    }

}
