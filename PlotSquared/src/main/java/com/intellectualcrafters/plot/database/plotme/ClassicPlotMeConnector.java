package com.intellectualcrafters.plot.database.plotme;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.database.MySQL;
import com.intellectualcrafters.plot.database.SQLite;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class ClassicPlotMeConnector extends APlotMeConnector {

    @Override
    public Connection getPlotMeConnection(FileConfiguration plotConfig, String dataFolder) {
        try {
            if (plotConfig.getBoolean("usemySQL")) {
                final String user = plotConfig.getString("mySQLuname");
                final String password = plotConfig.getString("mySQLpass");
                final String con = plotConfig.getString("mySQLconn");
                return DriverManager.getConnection(con, user, password);
//                return new MySQL(plotsquared, hostname, port, database, username, password)
            } else {
                return new SQLite(PlotSquared.THIS, dataFolder + File.separator + "plots.db").openConnection();
            }
        }
        catch (SQLException | ClassNotFoundException e) {}
        return null;
    }

    @Override
    public HashMap<String, HashMap<PlotId, Plot>> getPlotMePlots(Connection connection) throws SQLException {
        ResultSet r;
        PreparedStatement stmt;
        final HashMap<String, Integer> plotSize = new HashMap<>();
        final HashMap<String, HashMap<PlotId, Plot>> plots = new HashMap<>();
        stmt = connection.prepareStatement("SELECT * FROM `plotmePlots`");
        r = stmt.executeQuery();
        boolean checkUUID = DBFunc.hasColumn(r, "ownerid");
        
        while (r.next()) {
            final PlotId id = new PlotId(r.getInt("idX"), r.getInt("idZ"));
            final String name = r.getString("owner");
            final String world = PlotMeConverter.getWorld(r.getString("world"));
            if (!plotSize.containsKey(world)) {
                final int size = r.getInt("topZ") - r.getInt("bottomZ");
                plotSize.put(world, size);
                plots.put(world, new HashMap<PlotId, Plot>());
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
            final Plot plot = new Plot(id, owner, new ArrayList<UUID>(), new ArrayList<UUID>(), world);
            plots.get(world).put(id, plot);
        }
        
        r.close();
        stmt.close();
        
        try {
        
            MainUtil.sendConsoleMessage(" - plotmeDenied");
            stmt = connection.prepareStatement("SELECT * FROM `plotmeDenied`");
            r = stmt.executeQuery();
            
            while (r.next()) {
                final PlotId id = new PlotId(r.getInt("idX"), r.getInt("idZ"));
                final String name = r.getString("player");
                final String world = PlotMeConverter.getWorld(r.getString("world"));
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
            
            stmt = connection.prepareStatement("SELECT * FROM `plotmeAllowed`");
            r = stmt.executeQuery();
            
            while (r.next()) {
                final PlotId id = new PlotId(r.getInt("idX"), r.getInt("idZ"));
                final String name = r.getString("player");
                final String world = PlotMeConverter.getWorld(r.getString("world"));
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
                    plots.get(world).get(id).helpers.add(helper);
                }
            }
            
            r.close();
            stmt.close();
        
        }
        catch (Exception e) {
            
        }
        
        return plots;
    }
    
}
