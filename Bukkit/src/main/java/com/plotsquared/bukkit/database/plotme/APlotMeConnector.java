package com.plotsquared.bukkit.database.plotme;

import com.intellectualcrafters.configuration.file.FileConfiguration;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;

abstract class APlotMeConnector {

    public abstract Connection getPlotMeConnection(String plugin, FileConfiguration plotConfig, String dataFolder);

    public abstract HashMap<String, HashMap<PlotId, Plot>> getPlotMePlots(Connection connection) throws SQLException;

    public abstract boolean accepts(String version);

    public String getWorld(String world) {
        for (World newWorld : Bukkit.getWorlds()) {
            if (newWorld.getName().equalsIgnoreCase(world)) {
                return newWorld.getName();
            }
        }
        return world;
    }

    public boolean isValidConnection(Connection connection) {
        return connection != null;
    }

    public void copyConfig(FileConfiguration plotConfig, String world, String actualWorldName) {
        int pathWidth = plotConfig.getInt("worlds." + world + ".PathWidth"); //
        PS.get().config.set("worlds." + actualWorldName + ".road.width", pathWidth);
        int plotSize = plotConfig.getInt("worlds." + world + ".PlotSize"); //
        PS.get().config.set("worlds." + actualWorldName + ".plot.size", plotSize);
        String wallBlock = plotConfig.getString("worlds." + world + ".WallBlockId"); //
        PS.get().config.set("worlds." + actualWorldName + ".wall.block", wallBlock);
        String floor = plotConfig.getString("worlds." + world + ".PlotFloorBlockId"); //
        PS.get().config.set("worlds." + actualWorldName + ".plot.floor", Collections.singletonList(floor));
        String filling = plotConfig.getString("worlds." + world + ".PlotFillingBlockId"); //
        PS.get().config.set("worlds." + actualWorldName + ".plot.filling", Collections.singletonList(filling));
        String road = plotConfig.getString("worlds." + world + ".RoadMainBlockId");
        PS.get().config.set("worlds." + actualWorldName + ".road.block", road);
        int height = plotConfig.getInt("worlds." + world + ".RoadHeight"); //
        PS.get().config.set("worlds." + actualWorldName + ".road.height", height);
        PS.get().config.set("worlds." + actualWorldName + ".plot.height", height);
    }

    public Location getPlotTopLocAbs(int path, int plot, PlotId plotid) {
        int px = plotid.x;
        int pz = plotid.y;
        int x = (px * (path + plot)) - (int) Math.floor(path / 2) - 1;
        int z = (pz * (path + plot)) - (int) Math.floor(path / 2) - 1;
        return new Location(null, x, 256, z);
    }

    public Location getPlotBottomLocAbs(int path, int plot, PlotId plotid) {
        int px = plotid.x;
        int pz = plotid.y;
        int x = (px * (path + plot)) - plot - (int) Math.floor(path / 2) - 1;
        int z = (pz * (path + plot)) - plot - (int) Math.floor(path / 2) - 1;
        return new Location(null, x, 1, z);
    }

    public void setMerged(HashMap<String, HashMap<PlotId, boolean[]>> merges, String world, PlotId id, int direction) {
        HashMap<PlotId, boolean[]> plots = merges.get(world);
        PlotId id2 = new PlotId(id.x, id.y);
        boolean[] merge1;
        if (plots.containsKey(id)) {
            merge1 = plots.get(id);
        } else {
            merge1 = new boolean[] { false, false, false, false };
        }
        boolean[] merge2;
        if (plots.containsKey(id2)) {
            merge2 = plots.get(id2);
        } else {
            merge2 = new boolean[] { false, false, false, false };
        }
        merge1[direction] = true;
        merge2[(direction + 2) % 4] = true;
        plots.put(id, merge1);
        plots.put(id2, merge1);
    }
    
}
