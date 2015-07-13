package com.intellectualcrafters.plot.database.plotme;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.intellectualcrafters.configuration.file.FileConfiguration;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;

public abstract class APlotMeConnector {
    public abstract Connection getPlotMeConnection(String plugin, FileConfiguration plotConfig, String dataFolder);
    
    public abstract HashMap<String, HashMap<PlotId, Plot>> getPlotMePlots(Connection connection) throws SQLException;
    
    public abstract boolean accepts(String version);
    
    public String getWorld(final String world) {
        for (final World newworld : Bukkit.getWorlds()) {
            if (newworld.getName().equalsIgnoreCase(world)) {
                return newworld.getName();
            }
        }
        return world;
    }
    
    public Location getPlotTopLocAbs(int path, int plot, final PlotId plotid) {
        final int px = plotid.x;
        final int pz = plotid.y;
        final int x = (px * (path + plot)) - ((int) Math.floor(path / 2)) - 1;
        final int z = (pz * (path + plot)) - ((int) Math.floor(path / 2)) - 1;
        return new Location(null, x, 256, z);
    }
    
    public Location getPlotBottomLocAbs(int path, int plot, final PlotId plotid) {
        final int px = plotid.x;
        final int pz = plotid.y;
        final int x = (px * (path + plot)) - plot - ((int) Math.floor(path / 2)) - 1;
        final int z = (pz * (path + plot)) - plot - ((int) Math.floor(path / 2)) - 1;
        return new Location(null, x, 1, z);
    }
    
    public void setMerged(HashMap<String, HashMap<PlotId, boolean[]>> merges, String world, PlotId id, int direction) {
        HashMap<PlotId, boolean[]> plots = merges.get(world);
        PlotId id2;
        switch (direction) {
            case 0: {
                id2 = new PlotId(id.x, id.y);
                break;
            }
            case 1: {
                id2 = new PlotId(id.x, id.y);
                break;
            }
            case 2: {
                id2 = new PlotId(id.x, id.y);
                break;
            }
            case 3: {
                id2 = new PlotId(id.x, id.y);
                break;
            }
            default: {
                return;
            }
        }
        boolean[] merge1;
        boolean[] merge2;
        if (plots.containsKey(id)) {
            merge1 = plots.get(id);
        }
        else {
            merge1 = new boolean[]{false, false, false, false};
        }
        if (plots.containsKey(id2)) {
            merge2 = plots.get(id2);
        }
        else {
            merge2 = new boolean[]{false, false, false, false};
        }
        merge1[direction] = true;
        merge2[(direction + 2) % 4] = true;
        plots.put(id, merge1);
        plots.put(id2, merge1);
    }
    
}
