package com.intellectualcrafters.plot.database.plotme;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

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

public class ClassicPlotMeConnector extends APlotMeConnector {

    private String plugin;

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
        final HashMap<String, Integer> plotWidth = new HashMap<>();
        final HashMap<String, Integer> roadWidth = new HashMap<>();
        final HashMap<String, HashMap<PlotId, Plot>> plots = new HashMap<>();
        final HashMap<String, HashMap<PlotId, boolean[]>> merges = new HashMap<>();
        stmt = connection.prepareStatement("SELECT * FROM `" + plugin + "Plots`");
        r = stmt.executeQuery();
        boolean checkUUID = DBFunc.hasColumn(r, "ownerid");
        boolean merge = !plugin.equals("plotme") && Settings.CONVERT_PLOTME;
        while (r.next()) {
            final PlotId id = new PlotId(r.getInt("idX"), r.getInt("idZ"));
            final String name = r.getString("owner");
            final String world = LikePlotMeConverter.getWorld(r.getString("world"));
            if (!plots.containsKey(world)) {
                plots.put(world, new HashMap<PlotId, Plot>());
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

    @Override
    public boolean accepts(String version) {
        if (version ==  null) {
            return true;
        }
        return PS.get().canUpdate(version, "0.17.0");
    }
}
