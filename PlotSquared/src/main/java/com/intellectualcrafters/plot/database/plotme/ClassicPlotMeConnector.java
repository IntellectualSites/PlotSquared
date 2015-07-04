package com.intellectualcrafters.plot.database.plotme;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.database.SQLite;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

public class ClassicPlotMeConnector extends APlotMeConnector {

    private String plugin;

    public static String getWorld(final String world) {
        for (final World newworld : Bukkit.getWorlds()) {
            if (newworld.getName().equalsIgnoreCase(world)) {
                return newworld.getName();
            }
        }
        return world;
    }
    
    @Override
    public Connection getPlotMeConnection(String plugin, FileConfiguration plotConfig, String dataFolder) {
        this.plugin = plugin.toLowerCase();
        try {
            if (plotConfig.getBoolean("usemySQL")) {
                final String user = plotConfig.getString("mySQLuname");
                final String password = plotConfig.getString("mySQLpass");
                final String con = plotConfig.getString("mySQLconn");
                return DriverManager.getConnection(con, user, password);
//                return new MySQL(plotsquared, hostname, port, database, username, password)
            } else {
                return new SQLite(PS.get(), dataFolder + File.separator + "plots.db").openConnection();
            }
        }
        catch (SQLException | ClassNotFoundException e) {}
        return null;
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
    
    @Override
    public HashMap<String, HashMap<PlotId, Plot>> getPlotMePlots(Connection connection) throws SQLException {
        ResultSet r;
        PreparedStatement stmt;
        final HashMap<String, Integer> plotWidth = new HashMap<>();
        final HashMap<String, Integer> roadWidth = new HashMap<>();
        final HashMap<String, HashMap<PlotId, Plot>> plots = new HashMap<>();
        final HashMap<String, HashMap<PlotId, boolean[]>> merges = new HashMap<>();
        stmt = connection.prepareStatement("SELECT * FROM `" + plugin + "Plots`");
        r = stmt.executeQuery();
        boolean checkUUID = DBFunc.hasColumn(r, "ownerid");
        boolean merge = !plugin.equals("plotme");
        while (r.next()) {
            final PlotId id = new PlotId(r.getInt("idX"), r.getInt("idZ"));
            final String name = r.getString("owner");
            final String world = LikePlotMeConverter.getWorld(r.getString("world"));
            if (!plots.containsKey(world)) {
                int plot = PS.get().config.getInt("worlds." + world + ".plot.size");
                int path = PS.get().config.getInt("worlds." + world + ".road.width");
                if (plot == 0 && path == 0) {
                    
                }
                plotWidth.put(world, plot);
                roadWidth.put(world, path);
                plots.put(world, new HashMap<PlotId, Plot>());
                if (merge) {
                    merges.put(world, new HashMap<PlotId,boolean[]>());
                }
            }
            if (merge) {
                int tx = r.getInt("topX");
                int tz = r.getInt("topZ");
                int bx = r.getInt("bottomX") - 1;
                int bz = r.getInt("bottomZ") - 1;
                int path = roadWidth.get(world);
                int plot = plotWidth.get(world);
                Location top = getPlotTopLocAbs(path, plot, id);
                Location bot = getPlotBottomLocAbs(path, plot, id);
                if (tx > top.getX()) {
                    setMerged(merges, world, id, 1);
                }
                if (tz > top.getZ()) {
                    setMerged(merges, world, id, 2);
                }
                if (bx < bot.getX()) {
                    setMerged(merges, world, id, 3);
                }
                if (bz > bot.getZ()) {
                    setMerged(merges, world, id, 0);
                }
            }
            UUID owner = UUIDHandler.getUUID(name);
            if (owner == null) {
                if (name.equals("*")) {
                    owner = DBFunc.everyone;
                }
                else {
                    if (checkUUID){
                        try {
                            byte[] bytes = r.getBytes("ownerid");
                            if (bytes != null) {
                                owner = UUID.nameUUIDFromBytes(bytes);
                                if (owner != null) {
                                    UUIDHandler.add(new StringWrapper(name), owner);
                                }
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (owner == null) {
                        MainUtil.sendConsoleMessage("&cCould not identify owner for plot: " + id + " -> '" + name + "'");
                        continue;
                    }
                }
            }
            else {
                UUIDHandler.add(new StringWrapper(name), owner);
            }
            final Plot plot = new Plot(world, id, owner);
            plots.get(world).put(id, plot);
        }
        
        for (Entry<String, HashMap<PlotId, boolean[]>> entry : merges.entrySet()) {
            String world = entry.getKey();
            for (Entry<PlotId, boolean[]> entry2 : entry.getValue().entrySet()) {
                HashMap<PlotId, Plot> newplots = plots.get(world);
                Plot plot = newplots.get(entry2.getKey());
                if (plot != null) {
                    plot.settings.setMerged(entry2.getValue());
                }
            }
        }
        
        r.close();
        stmt.close();
        
        try {
        
            MainUtil.sendConsoleMessage(" - " + plugin + "Denied");
            stmt = connection.prepareStatement("SELECT * FROM `" + plugin + "Denied`");
            r = stmt.executeQuery();
            
            while (r.next()) {
                final PlotId id = new PlotId(r.getInt("idX"), r.getInt("idZ"));
                final String name = r.getString("player");
                final String world = LikePlotMeConverter.getWorld(r.getString("world"));
                UUID denied = UUIDHandler.getUUID(name);
                if (denied == null) {
                    if (name.equals("*")) {
                        denied = DBFunc.everyone;
                    } else {
                        MainUtil.sendConsoleMessage("&6Could not identify denied for plot: " + id);
                        continue;
                    }
                }
                if (plots.get(world).containsKey(id)) {
                    plots.get(world).get(id).denied.add(denied);
                }
            }
            
            stmt = connection.prepareStatement("SELECT * FROM `" + plugin + "Allowed`");
            r = stmt.executeQuery();
            
            while (r.next()) {
                final PlotId id = new PlotId(r.getInt("idX"), r.getInt("idZ"));
                final String name = r.getString("player");
                final String world = LikePlotMeConverter.getWorld(r.getString("world"));
                UUID helper = UUIDHandler.getUUID(name);
                if (helper == null) {
                    if (name.equals("*")) {
                        helper = DBFunc.everyone;
                    } else {
                        MainUtil.sendConsoleMessage("&6Could not identify helper for plot: " + id);
                        continue;
                    }
                }
                if (plots.get(world).containsKey(id)) {
                    plots.get(world).get(id).trusted.add(helper);
                }
            }
            
            r.close();
            stmt.close();
        
        }
        catch (Exception e) {}
        return plots;
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
    
}
