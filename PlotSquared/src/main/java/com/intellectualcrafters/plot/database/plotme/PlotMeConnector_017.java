package com.intellectualcrafters.plot.database.plotme;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;

import com.intellectualcrafters.configuration.file.FileConfiguration;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.database.SQLite;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class PlotMeConnector_017 extends APlotMeConnector {
    private String plugin;

    @Override
    public Connection getPlotMeConnection(String plugin, FileConfiguration plotConfig, String dataFolder) {
        this.plugin = plugin.toLowerCase();
        try {
            if (plotConfig.getBoolean("usemySQL")) {
                String user = plotConfig.getString("mySQLuname");
                String password = plotConfig.getString("mySQLpass");
                String con = plotConfig.getString("mySQLconn");
                return DriverManager.getConnection(con, user, password);
            } else {
                File file = new File(dataFolder + File.separator + "plotmecore.db");
                if (file.exists()) {
                    return new SQLite(dataFolder + File.separator + "plotmecore.db").openConnection();
                }
                return new SQLite(dataFolder + File.separator + "plots.db").openConnection();
            }
        }
        catch (SQLException | ClassNotFoundException e) {}
        return null;
    }

    @Override
    public HashMap<String, HashMap<PlotId, Plot>> getPlotMePlots(Connection connection) throws SQLException {
        ResultSet r;
        PreparedStatement stmt;
        HashMap<String, Integer> plotWidth = new HashMap<>();
        HashMap<String, Integer> roadWidth = new HashMap<>();
        final HashMap<Integer, Plot> plots = new HashMap<>();
        HashMap<String, HashMap<PlotId, boolean[]>> merges = new HashMap<>();
        stmt = connection.prepareStatement("SELECT * FROM `" + plugin + "core_plots`");
        r = stmt.executeQuery();
        boolean checkUUID = DBFunc.hasColumn(r, "ownerID");
        boolean merge = !plugin.equals("plotme") && Settings.CONVERT_PLOTME;
        while (r.next()) {
            int key = r.getInt("plot_id");
            PlotId id = new PlotId(r.getInt("plotX"), r.getInt("plotZ"));
            String name = r.getString("owner");
            String world = LikePlotMeConverter.getWorld(r.getString("world"));
            if (!plots.containsKey(world)) {
                if (merge) {
                    int plot = PS.get().config.getInt("worlds." + world + ".plot.size");
                    int path = PS.get().config.getInt("worlds." + world + ".road.width");
                    plotWidth.put(world, plot);
                    roadWidth.put(world, path);
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
            Plot plot = new Plot(world, id, owner);
            plots.put(key, plot);
        }
        for (Entry<Integer, Plot> entry : plots.entrySet()) {
            Plot plot = entry.getValue();
            HashMap<PlotId, boolean[]> mergeMap = merges.get(plot.world);
            if (mergeMap != null) {
                if (mergeMap.containsKey(plot.id)) {
                    plot.settings.setMerged(mergeMap.get(plot.id));
                }
            }
        }
        r.close();
        stmt.close();
        try {
            MainUtil.sendConsoleMessage(" - " + plugin + "_denied");
            stmt = connection.prepareStatement("SELECT * FROM `" + plugin + "_denied`");
            r = stmt.executeQuery();
            
            while (r.next()) {
                int key = r.getInt("plot_id");
                Plot plot = plots.get(key);
                if (plot == null) {
                    MainUtil.sendConsoleMessage("&6Denied (" + key + ") references deleted plot; ignoring entry.");
                    continue;
                }
                String name = r.getString("player");
                UUID denied = UUIDHandler.getUUID(name);
                if (denied == null) {
                    if (name.equals("*")) {
                        denied = DBFunc.everyone;
                    } else {
                        MainUtil.sendConsoleMessage("&6Denied (" + key + ") references incorrect name (`" + name + "`)");
                        continue;
                    }
                }
                plot.denied.add(denied);
            }
            
            MainUtil.sendConsoleMessage(" - " + plugin + "_allowed");
            stmt = connection.prepareStatement("SELECT * FROM `" + plugin + "_allowed`");
            r = stmt.executeQuery();
            
            while (r.next()) {
                int key = r.getInt("plot_id");
                Plot plot = plots.get(key);
                if (plot == null) {
                    MainUtil.sendConsoleMessage("&6Allowed (" + key + ") references deleted plot; ignoring entry.");
                    continue;
                }
                String name = r.getString("player");
                UUID allowed = UUIDHandler.getUUID(name);
                if (allowed == null) {
                    if (name.equals("*")) {
                        allowed = DBFunc.everyone;
                    } else {
                        MainUtil.sendConsoleMessage("&6Allowed (" + key + ") references incorrect name (`" + name + "`)");
                        continue;
                    }
                }
                plot.trusted.add(allowed);
            }
            r.close();
            stmt.close();
        
        }
        catch (Exception e) {}
        HashMap<String, HashMap<PlotId, Plot>> processed = new HashMap<>();
        
        for (Entry<Integer, Plot> entry : plots.entrySet()) {
            Plot plot = entry.getValue();
            HashMap<PlotId, Plot> map = processed.get(plot.world);
            if (map == null) {
                map = new HashMap<>();
                processed.put(plot.world, map);
            }
            map.put(plot.id, plot);
        }
        return processed ;
    }

    @Override
    public boolean accepts(String version) {
        if (version == null) {
            return false;
        }
        return !PS.get().canUpdate(version, "0.17");
    }
    
}
