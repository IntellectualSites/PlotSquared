package com.plotsquared.bukkit.database.plotme;

import com.intellectualcrafters.configuration.file.FileConfiguration;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.database.SQLite;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.UUIDHandler;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

public class PlotMeConnector_017 extends APlotMeConnector {
    private String plugin;
    private String prefix;
    
    @Override
    public Connection getPlotMeConnection(final String plugin, final FileConfiguration plotConfig, final String dataFolder) {
        this.plugin = plugin.toLowerCase();
        prefix = plugin + "core_";
        try {
            if (plotConfig.getBoolean("usemySQL")) {
                final String user = plotConfig.getString("mySQLuname");
                final String password = plotConfig.getString("mySQLpass");
                final String con = plotConfig.getString("mySQLconn");
                return DriverManager.getConnection(con, user, password);
            } else {
                final File file = new File(dataFolder + File.separator + "plotmecore.db");
                if (file.exists()) {
                    return new SQLite(dataFolder + File.separator + "plotmecore.db").openConnection();
                }
                return new SQLite(dataFolder + File.separator + "plots.db").openConnection();
            }
        } catch (SQLException | ClassNotFoundException e) {}
        return null;
    }
    
    @Override
    public HashMap<String, HashMap<PlotId, Plot>> getPlotMePlots(final Connection connection) throws SQLException {
        ResultSet r;
        PreparedStatement stmt;
        final HashMap<String, Integer> plotWidth = new HashMap<>();
        final HashMap<String, Integer> roadWidth = new HashMap<>();
        final HashMap<Integer, Plot> plots = new HashMap<>();
        final HashMap<String, HashMap<PlotId, boolean[]>> merges = new HashMap<>();
        try {
            stmt = connection.prepareStatement("SELECT * FROM `" + plugin + "core_plots`");
            r = stmt.executeQuery();
        } catch (Exception e) {
            PS.debug("========= Table does not exist =========");
            e.printStackTrace();
            PS.debug("=======================================");
            PS.debug("&8 - &7The database does not match the version specified in the PlotMe config");
            PS.debug("&8 - &7Please correct this, or if you are unsure, the most common is 0.16.3");
            return null;
        }
        final boolean checkUUID = DBFunc.hasColumn(r, "ownerID");
        final boolean merge = !plugin.equals("plotme") && Settings.CONVERT_PLOTME;
        while (r.next()) {
            final int key = r.getInt("plot_id");
            final PlotId id = new PlotId(r.getInt("plotX"), r.getInt("plotZ"));
            final String name = r.getString("owner");
            final String world = LikePlotMeConverter.getWorld(r.getString("world"));
            if (!plots.containsKey(world)) {
                if (merge) {
                    final int plot = PS.get().config.getInt("worlds." + world + ".plot.size");
                    final int path = PS.get().config.getInt("worlds." + world + ".road.width");
                    plotWidth.put(world, plot);
                    roadWidth.put(world, path);
                    merges.put(world, new HashMap<PlotId, boolean[]>());
                }
            }
            if (merge) {
                final int tx = r.getInt("topX");
                final int tz = r.getInt("topZ");
                final int bx = r.getInt("bottomX") - 1;
                final int bz = r.getInt("bottomZ") - 1;
                final int path = roadWidth.get(world);
                final int plot = plotWidth.get(world);
                final Location top = getPlotTopLocAbs(path, plot, id);
                final Location bot = getPlotBottomLocAbs(path, plot, id);
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
            UUID owner = UUIDHandler.getUUID(name, null);
            if (owner == null) {
                if (name.equals("*")) {
                    owner = DBFunc.everyone;
                } else {
                    if (checkUUID) {
                        try {
                            final byte[] bytes = r.getBytes("ownerid");
                            if (bytes != null) {
                                owner = UUID.nameUUIDFromBytes(bytes);
                                if (owner != null) {
                                    UUIDHandler.add(new StringWrapper(name), owner);
                                }
                            }
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (owner == null) {
                        PS.log("&cCould not identify owner for plot: " + id + " -> '" + name + "'");
                        continue;
                    }
                }
            } else {
                UUIDHandler.add(new StringWrapper(name), owner);
            }
            final Plot plot = new Plot(PlotArea.createGeneric(world), id, owner);
            plots.put(key, plot);
        }
        for (final Entry<Integer, Plot> entry : plots.entrySet()) {
            final Plot plot = entry.getValue();
            final HashMap<PlotId, boolean[]> mergeMap = merges.get(plot.getArea().worldname);
            if (mergeMap != null) {
                if (mergeMap.containsKey(plot.getId())) {
                    plot.setMerged(mergeMap.get(plot.getId()));
                }
            }
        }
        r.close();
        stmt.close();
        try {
            PS.log(" - " + plugin + "core_denied");
            stmt = connection.prepareStatement("SELECT * FROM `" + plugin + "core_denied`");
            r = stmt.executeQuery();
            
            while (r.next()) {
                final int key = r.getInt("plot_id");
                final Plot plot = plots.get(key);
                if (plot == null) {
                    PS.log("&6Denied (" + key + ") references deleted plot; ignoring entry.");
                    continue;
                }
                final UUID denied = UUID.fromString(r.getString("player"));
                plot.getDenied().add(denied);
            }
            
            PS.log(" - " + plugin + "core_allowed");
            stmt = connection.prepareStatement("SELECT * FROM `" + plugin + "core_allowed`");
            r = stmt.executeQuery();
            
            while (r.next()) {
                final int key = r.getInt("plot_id");
                final Plot plot = plots.get(key);
                if (plot == null) {
                    PS.log("&6Allowed (" + key + ") references deleted plot; ignoring entry.");
                    continue;
                }
                final UUID allowed = UUID.fromString(r.getString("player"));
                plot.getTrusted().add(allowed);
            }
            r.close();
            stmt.close();
            
        } catch (final Exception e) {
            e.printStackTrace();
        }
        final HashMap<String, HashMap<PlotId, Plot>> processed = new HashMap<>();
        
        for (final Entry<Integer, Plot> entry : plots.entrySet()) {
            final Plot plot = entry.getValue();
            HashMap<PlotId, Plot> map = processed.get(plot.getArea().worldname);
            if (map == null) {
                map = new HashMap<>();
                processed.put(plot.getArea().worldname, map);
            }
            map.put(plot.getId(), plot);
        }
        return processed;
    }
    
    @Override
    public boolean accepts(final String version) {
        if (version == null) {
            return false;
        }
        return !PS.get().canUpdate(version, "0.17");
    }
}
