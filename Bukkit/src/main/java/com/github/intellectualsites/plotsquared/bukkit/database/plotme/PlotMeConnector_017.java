package com.github.intellectualsites.plotsquared.bukkit.database.plotme;

import com.github.intellectualsites.plotsquared.configuration.file.FileConfiguration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.database.SQLite;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.UUID;

public class PlotMeConnector_017 extends APlotMeConnector {

    private String plugin;

    @Override public Connection getPlotMeConnection(String plugin, FileConfiguration plotConfig,
        String dataFolder) {
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
                    return new SQLite(file).openConnection();
                }
                return new SQLite(new File(dataFolder + File.separator + "plots.db"))
                    .openConnection();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override public HashMap<String, HashMap<PlotId, Plot>> getPlotMePlots(Connection connection)
        throws SQLException {
        ResultSet resultSet;
        PreparedStatement statement;
        HashMap<String, Integer> plotWidth = new HashMap<>();
        HashMap<String, Integer> roadWidth = new HashMap<>();
        HashMap<Integer, Plot> plots = new HashMap<>();
        HashMap<String, HashMap<PlotId, boolean[]>> merges = new HashMap<>();
        try {
            statement =
                connection.prepareStatement("SELECT * FROM `" + this.plugin + "core_plots`");
            resultSet = statement.executeQuery();
        } catch (SQLException e) {
            PlotSquared.debug("========= Table does not exist =========");
            e.printStackTrace();
            PlotSquared.debug("=======================================");
            PlotSquared.debug(
                "&8 - &7The database does not match the version specified in the PlotMe config");
            PlotSquared.debug("&8 - &7Please correct this, or if you are unsure, the most common is 0.16.3");
            return null;
        }
        boolean checkUUID = DBFunc.hasColumn(resultSet, "ownerID");
        boolean merge =
            !"plotme".equals(this.plugin) && Settings.Enabled_Components.PLOTME_CONVERTER;
        while (resultSet.next()) {
            int key = resultSet.getInt("plot_id");
            PlotId id = new PlotId(resultSet.getInt("plotX"), resultSet.getInt("plotZ"));
            String name = resultSet.getString("owner");
            String world = LikePlotMeConverter.getWorld(resultSet.getString("world"));
            if (!plots.containsKey(world) && merge) {
                int plot = PlotSquared.get().worlds.getInt("worlds." + world + ".plot.size");
                int path = PlotSquared.get().worlds.getInt("worlds." + world + ".road.width");
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
                        PlotSquared.log(
                            "&cCould not identify owner for plot: " + id + " -> '" + name + '\'');
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
            HashMap<PlotId, boolean[]> mergeMap = merges.get(plot.getWorldName());
            if (mergeMap != null) {
                if (mergeMap.containsKey(plot.getId())) {
                    plot.setMerged(mergeMap.get(plot.getId()));
                }
            }
        }
        resultSet.close();
        statement.close();
        try {
            PlotSquared.log(" - " + this.plugin + "core_denied");
            statement =
                connection.prepareStatement("SELECT * FROM `" + this.plugin + "core_denied`");
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int key = resultSet.getInt("plot_id");
                Plot plot = plots.get(key);
                if (plot == null) {
                    PlotSquared.log("&6Denied (" + key + ") references deleted plot; ignoring entry.");
                    continue;
                }
                String player = resultSet.getString("player");
                UUID denied = player.equals("*") ? DBFunc.everyone : UUID.fromString(player);
                plot.getDenied().add(denied);
            }

            PlotSquared.log(" - " + this.plugin + "core_allowed");
            statement =
                connection.prepareStatement("SELECT * FROM `" + this.plugin + "core_allowed`");
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int key = resultSet.getInt("plot_id");
                Plot plot = plots.get(key);
                if (plot == null) {
                    PlotSquared.log("&6Allowed (" + key + ") references deleted plot; ignoring entry.");
                    continue;
                }
                String player = resultSet.getString("player");
                UUID allowed = player.equals("*") ? DBFunc.everyone : UUID.fromString(player);
                plot.getTrusted().add(allowed);
            }
            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        HashMap<String, HashMap<PlotId, Plot>> processed = new HashMap<>();

        for (Plot plot : plots.values()) {
            HashMap<PlotId, Plot> map = processed.get(plot.getWorldName());
            if (map == null) {
                map = new HashMap<>();
                processed.put(plot.getWorldName(), map);
            }
            map.put(plot.getId(), plot);
        }
        return processed;
    }

    @Override public boolean accepts(String version) {
        if (version == null) {
            return false;
        }
        return !PlotSquared.get().canUpdate(version, "0.17");
    }
}
