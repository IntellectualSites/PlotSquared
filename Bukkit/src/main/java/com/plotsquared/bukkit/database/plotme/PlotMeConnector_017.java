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
import java.util.UUID;

public class PlotMeConnector_017 extends APlotMeConnector {

    private String plugin;

    @Override
    public Connection getPlotMeConnection(FileConfiguration plotConfig, String dataFolder) {
        this.plugin = this.plugin.toLowerCase();
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
        } catch (SQLException | ClassNotFoundException ignored) {
            //ignored
            ignored.printStackTrace();
        }
        return null;
    }

    @Override
    public HashMap<String, HashMap<PlotId, Plot>> getPlotMePlots(Connection connection) throws SQLException {
        ResultSet resultSet;
        PreparedStatement statement;
        HashMap<String, Integer> plotWidth = new HashMap<>();
        HashMap<String, Integer> roadWidth = new HashMap<>();
        HashMap<Integer, Plot> plots = new HashMap<>();
        HashMap<String, HashMap<PlotId, boolean[]>> merges = new HashMap<>();
        try {
            statement = connection.prepareStatement("SELECT * FROM `" + this.plugin + "core_plots`");
            resultSet = statement.executeQuery();
        } catch (SQLException e) {
            PS.debug("========= Table does not exist =========");
            e.printStackTrace();
            PS.debug("=======================================");
            PS.debug("&8 - &7The database does not match the version specified in the PlotMe config");
            PS.debug("&8 - &7Please correct this, or if you are unsure, the most common is 0.16.3");
            return null;
        }
        boolean checkUUID = DBFunc.hasColumn(resultSet, "ownerID");
        boolean merge = !this.plugin.equals("plotme") && Settings.CONVERT_PLOTME;
        while (resultSet.next()) {
            int key = resultSet.getInt("plot_id");
            PlotId id = new PlotId(resultSet.getInt("plotX"), resultSet.getInt("plotZ"));
            String name = resultSet.getString("owner");
            String world = LikePlotMeConverter.getWorld(resultSet.getString("world"));
            if (!plots.containsKey(world) && merge) {
                int plot = PS.get().config.getInt("worlds." + world + ".plot.size");
                int path = PS.get().config.getInt("worlds." + world + ".road.width");
                plotWidth.put(world, plot);
                roadWidth.put(world, path);
                merges.put(world, new HashMap<PlotId, boolean[]>());
            }
            if (merge) {
                int tx = resultSet.getInt("topX");
                int tz = resultSet.getInt("topZ");
                int bx = resultSet.getInt("bottomX") - 1;
                int bz = resultSet.getInt("bottomZ") - 1;
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
            UUID owner = UUIDHandler.getUUID(name, null);
            if (owner == null) {
                if (name.equals("*")) {
                    owner = DBFunc.everyone;
                } else {
                    if (checkUUID) {
                        byte[] bytes = resultSet.getBytes("ownerid");
                        if (bytes != null) {
                            owner = UUID.nameUUIDFromBytes(bytes);
                            UUIDHandler.add(new StringWrapper(name), owner);
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
            Plot plot = new Plot(PlotArea.createGeneric(world), id, owner);
            plots.put(key, plot);
        }
        for (Plot plot : plots.values()) {
            HashMap<PlotId, boolean[]> mergeMap = merges.get(plot.getArea().worldname);
            if (mergeMap != null) {
                if (mergeMap.containsKey(plot.getId())) {
                    plot.setMerged(mergeMap.get(plot.getId()));
                }
            }
        }
        resultSet.close();
        statement.close();
        try {
            PS.log(" - " + this.plugin + "core_denied");
            statement = connection.prepareStatement("SELECT * FROM `" + this.plugin + "core_denied`");
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int key = resultSet.getInt("plot_id");
                Plot plot = plots.get(key);
                if (plot == null) {
                    PS.log("&6Denied (" + key + ") references deleted plot; ignoring entry.");
                    continue;
                }
                UUID denied = UUID.fromString(resultSet.getString("player"));
                plot.getDenied().add(denied);
            }

            PS.log(" - " + this.plugin + "core_allowed");
            statement = connection.prepareStatement("SELECT * FROM `" + this.plugin + "core_allowed`");
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int key = resultSet.getInt("plot_id");
                Plot plot = plots.get(key);
                if (plot == null) {
                    PS.log("&6Allowed (" + key + ") references deleted plot; ignoring entry.");
                    continue;
                }
                UUID allowed = UUID.fromString(resultSet.getString("player"));
                plot.getTrusted().add(allowed);
            }
            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        HashMap<String, HashMap<PlotId, Plot>> processed = new HashMap<>();

        for (Plot plot : plots.values()) {
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
    public boolean accepts(String version) {
        if (version == null) {
            return false;
        }
        return !PS.get().canUpdate(version, "0.17");
    }
}
