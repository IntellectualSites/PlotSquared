////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotClusterId;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotSettings;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.comment.PlotComment;
import com.intellectualcrafters.plot.util.ClusterManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;

/**
 * @author Citymonstret
 */
public class SQLManager implements AbstractDB {
    // Public final
    public final String SET_OWNER;
    public final String GET_ALL_PLOTS;
    public final String CREATE_PLOTS;
    public final String CREATE_SETTINGS;
    public final String CREATE_TIERS;
    public final String CREATE_PLOT;
    public final String CREATE_CLUSTER;
    private final String prefix;
    // Private
    private Connection connection;
    private boolean CLOSED = false;
    // Private Final
    private final Database database;
    private final boolean MYSQL;

    /**
     * important tasks
     */
    public volatile Queue<Runnable> globalTasks;
    
    /**
     * Notify tasks
     */
    public volatile Queue<Runnable> notifyTasks;
    
    /**
     * plot
     * plot_denied
     * plot_helpers
     * plot_trusted
     * plot_comments
     * plot_settings
     * plot_rating
     */
    public volatile ConcurrentHashMap<Plot, Queue<UniqueStatement>> plotTasks;
    
    /**
     * cluster
     * cluster_helpers
     * cluster_invited
     * cluster_settings
     */
    public volatile ConcurrentHashMap<PlotCluster, Queue<UniqueStatement>> clusterTasks;
    private boolean debug;
    
    public synchronized Queue<Runnable> getGlobalTasks() {
        return globalTasks;
    }
    
    public synchronized Queue<Runnable> getNotifyTasks() {
        return notifyTasks;
    }
    
    public synchronized void addPlotTask(Plot plot, UniqueStatement task) {
        if (plot == null) {
            plot = new Plot("", new PlotId(Integer.MAX_VALUE, Integer.MAX_VALUE), null);
        }
        Queue<UniqueStatement> tasks = plotTasks.get(plot);
        if (tasks == null) {
            tasks = new ConcurrentLinkedQueue<>();
            plotTasks.put(plot, tasks);
        }
        if (task == null) {
            task = new UniqueStatement(plot.hashCode() + "") {

                @Override
                public PreparedStatement get() throws SQLException {
                    return null;
                }

                @Override
                public void set(PreparedStatement stmt) throws SQLException {}
                
                @Override
                public void addBatch(PreparedStatement stmt) throws SQLException {}
                
                @Override
                public void execute(PreparedStatement stmt) throws SQLException {}
                
            };
        }
        tasks.add(task);
    }
    
    public synchronized void addClusterTask(PlotCluster cluster, UniqueStatement task) {
        Queue<UniqueStatement> tasks = clusterTasks.get(cluster);
        if (tasks == null) {
            tasks = new ConcurrentLinkedQueue<>();
            clusterTasks.put(cluster, tasks);
        }
        tasks.add(task);
    }
    
    public synchronized void addGlobalTask(Runnable task) {
        getGlobalTasks().add(task);
    }
    
    public synchronized void addNotifyTask(Runnable task) {
        if (task != null) {
            getNotifyTasks().add(task);
        }
    }
    
    
    /**
     * Constructor
     *
     * @param c connection
     * @param p prefix
     * @throws Exception 
     */
    public SQLManager(final Database database, final String p, final boolean debug) throws Exception {
        this.debug = debug;
        // Private final
        this.database = database;
        this.connection = database.openConnection();
        this.MYSQL = (database instanceof MySQL);
        globalTasks = new ConcurrentLinkedQueue<>();
        notifyTasks = new ConcurrentLinkedQueue<>();
        plotTasks = new ConcurrentHashMap<>();
        clusterTasks = new ConcurrentHashMap<>();
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                long last = System.currentTimeMillis();
                while (true) {
                    if (CLOSED) {
                        break;
                    }
                    // schedule reconnect
                    if (MYSQL && System.currentTimeMillis() - last > 550000) {
                        last = System.currentTimeMillis();
                        try {
                            close();
                            CLOSED = false;
                            connection = database.forceConnection();
                        } catch (SQLException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!sendBatch()) {
                        try {
                            if (getNotifyTasks().size() > 0) {
                                for (Runnable task : getNotifyTasks()) {
                                    TaskManager.runTask(task);
                                }
                                getNotifyTasks().clear();
                            }
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        this.prefix = p;
        // Set timout
        // setTimout();
        // Public final
        this.SET_OWNER = "UPDATE `" + this.prefix + "plot` SET `owner` = ? WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND `world` = ?";
        this.GET_ALL_PLOTS = "SELECT `id`, `plot_id_x`, `plot_id_z`, `world` FROM `" + this.prefix + "plot`";
        this.CREATE_PLOTS = "INSERT INTO `" + this.prefix + "plot`(`plot_id_x`, `plot_id_z`, `owner`, `world`, `timestamp`) values ";
        this.CREATE_SETTINGS = "INSERT INTO `" + this.prefix + "plot_settings` (`plot_plot_id`) values ";
        this.CREATE_TIERS = "INSERT INTO `" + this.prefix + "plot_%tier%` (`plot_plot_id`, `user_uuid`) values ";
        this.CREATE_PLOT = "INSERT INTO `" + this.prefix + "plot`(`plot_id_x`, `plot_id_z`, `owner`, `world`, `timestamp`) VALUES(?, ?, ?, ?, ?)";
        this.CREATE_CLUSTER = "INSERT INTO `" + this.prefix + "cluster`(`pos1_x`, `pos1_z`, `pos2_x`, `pos2_z`, `owner`, `world`) VALUES(?, ?, ?, ?, ?, ?)";
        updateTables();
        createTables();
    }
    
    public boolean sendBatch() {
        try {
            if (getGlobalTasks().size() > 0) {
                if (connection.getAutoCommit()) {
                    connection.setAutoCommit(false);
                }
                Runnable task = getGlobalTasks().remove();
                if (task != null) {
                    task.run();
                }
                commit();
                return true;
            }
            int count = -1;
            if (plotTasks.size() > 0) {
                count = 0;
                if (connection.getAutoCommit()) {
                    connection.setAutoCommit(false);
                }
                String method = null;
                PreparedStatement stmt = null;
                UniqueStatement task = null;
                UniqueStatement lastTask = null;
//                ArrayList<Entry<Plot, Queue<UniqueStatement>>> keys = new ArrayList<>(plotTasks.entrySet());
                for (Entry<Plot, Queue<UniqueStatement>> entry : plotTasks.entrySet()) {
                    Plot plot = entry.getKey();
                    if (plotTasks.get(plot).size() == 0) {
                        plotTasks.remove(plot);
                        continue;
                    }
                    task = plotTasks.get(plot).remove();
                    count++;
                    if (task != null) {
                        if (task._method == null || !task._method.equals(method)) {
                            if (stmt != null) {
                                lastTask.execute(stmt);
                                stmt.close();
                            }
                            method = task._method;
                            stmt = task.get();
                        }
                        task.set(stmt);
                        task.addBatch(stmt);
                    }
                    lastTask = task;
                }
                if (stmt != null && task != null) {
                    task.execute(stmt);
                    stmt.close();
                }
            }
            if (clusterTasks.size() > 0) {
                count = 0;
                if (connection.getAutoCommit()) {
                    connection.setAutoCommit(false);
                }
                String method = null;
                PreparedStatement stmt = null;
                UniqueStatement task = null;
                UniqueStatement lastTask = null;
                for (Entry<PlotCluster, Queue<UniqueStatement>> entry : clusterTasks.entrySet()) {
                    PlotCluster cluster = entry.getKey();
                    if (clusterTasks.get(cluster).size() == 0) {
                        clusterTasks.remove(cluster);
                        continue;
                    }
                    task = clusterTasks.get(cluster).remove();
                    count++;
                    if (task != null) {
                        if (task._method == null || !task._method.equals(method)) {
                            if (stmt != null) {
                                lastTask.execute(stmt);
                                stmt.close();
                            }
                            method = task._method;
                            stmt = task.get();
                        }
                        task.set(stmt);
                        task.addBatch(stmt);
                    }
                    lastTask = task;
                }
                if (stmt != null && task != null) {
                    task.execute(stmt);
                    stmt.close();
                }
            }
            if (count > 0) {
                commit();
                return true;
            }
            else if (count != -1) {
                if (!connection.getAutoCommit()) {
                    connection.setAutoCommit(true);
                }
            }
            if (clusterTasks.size() > 0) {
                clusterTasks.clear();
               }
            if (plotTasks.size() > 0) {
                plotTasks.clear();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public abstract class UniqueStatement {
        public String _method;

        public UniqueStatement(String method) {
            this._method = method;
        }
        
        public void addBatch(PreparedStatement stmt) throws SQLException {
            stmt.addBatch();
        }

        public void execute(PreparedStatement stmt) throws SQLException {
            stmt.executeBatch();
        }

        public abstract PreparedStatement get() throws SQLException;
        
        public abstract void set(PreparedStatement stmt) throws SQLException;
    }

    public Connection getConnection() {
        return this.connection;
    }

    /**
     * Set Plot owner
     *
     * @param plot Plot Object
     * @param uuid Owner UUID
     */
    @Override
    public void setOwner(final Plot plot, final UUID uuid) {
        addPlotTask(plot, new UniqueStatement("setOwner") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setInt(2, plot.id.x);
                statement.setInt(3, plot.id.y);
                statement.setString(4, plot.world);
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement(SQLManager.this.SET_OWNER);
            }
        });
    }

    @Override
    public void createPlotsAndData(final ArrayList<Plot> myList, final Runnable whenDone) {
        addGlobalTask(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create the plots
                    createPlots(myList, new Runnable() {
                        @Override
                        public void run() {
                            try {
                         // Creating datastructures
                            HashMap<PlotId, Plot> plotMap = new HashMap<>();
                            for (Plot plot : myList) {
                                plotMap.put(plot.id, plot);
                            }
                            final ArrayList<SettingsPair> settings = new ArrayList<>();
                            final ArrayList<UUIDPair> helpers = new ArrayList<>();
                            final ArrayList<UUIDPair> trusted = new ArrayList<>();
                            final ArrayList<UUIDPair> denied = new ArrayList<>();

                            // Populating structures
                            final PreparedStatement stmt = connection.prepareStatement(GET_ALL_PLOTS);
                            try (ResultSet result = stmt.executeQuery()) {
                                while (result.next()) {
                                    final int id = result.getInt("id");
                                    int x = result.getInt("plot_id_x");
                                    int y = result.getInt("plot_id_z");
                                    PlotId plotId = new PlotId(x, y);
                                    Plot plot = plotMap.get(plotId);
                                    if (plot != null) {
                                        settings.add(new SettingsPair(id, plot.getSettings()));
                                        if (plot.getDenied() != null) {
                                            for (UUID uuid : plot.getDenied()) {
                                                denied.add(new UUIDPair(id, uuid));
                                            }
                                        }
                                        if (plot.getMembers() != null) {
                                            for (UUID uuid : plot.getMembers()) {
                                                trusted.add(new UUIDPair(id, uuid));
                                            }
                                        }
                                        if (plot.getTrusted() != null) {
                                            for (UUID uuid : plot.getTrusted()) {
                                                helpers.add(new UUIDPair(id, uuid));
                                            }
                                        }
                                    }
                            }
                            }
                            createSettings(settings, new Runnable() {
                                @Override
                                public void run() {
                                    createTiers(helpers, "helpers", new Runnable() {
                                        @Override
                                        public void run() {
                                            createTiers(trusted, "trusted", new Runnable() {
                                                @Override
                                                public void run() {
                                                    createTiers(denied, "denied", new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                connection.commit();
                                                            } catch (SQLException e) {
                                                                e.printStackTrace();
                                                            }
                                                            if (whenDone != null) whenDone.run();
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                            }
                            catch (SQLException e) {
                                e.printStackTrace();
                                PS.debug("&7[WARN] " + "Failed to set all helpers for plots");
                                try {
                                    connection.commit();
                                } catch (SQLException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    PS.debug("&7[WARN] " + "Failed to set all helpers for plots");
                    try {
                        connection.commit();
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Create a plot
     *
     * @param myList list of plots to be created
     */
    public void createTiers(final ArrayList<UUIDPair> myList, final String tier, final Runnable whenDone) {
        final StmtMod<UUIDPair> mod = new StmtMod<UUIDPair>() {
            @Override
            public String getCreateMySQL(int size) {
                return getCreateMySQL(size, CREATE_TIERS.replaceAll("%tier%", tier), 2);
            }

            @Override
            public String getCreateSQLite(int size) {
                return getCreateSQLite(size,
                        "INSERT INTO `" + prefix + "plot_" + tier + "` SELECT ? AS `plot_plot_id`, ? AS `user_uuid`", 2);
            }

            @Override
            public String getCreateSQL() {
                return "INSERT INTO `" + SQLManager.this.prefix + "plot_" + tier + "` (`plot_plot_id`, `user_uuid`) VALUES(?,?)";
            }

            @Override
            public void setMySQL(PreparedStatement stmt, int i, UUIDPair pair) throws SQLException {
                stmt.setInt((i * 2) + 1, pair.id);
                stmt.setString((i * 2) + 2, pair.uuid.toString());
            }

            @Override
            public void setSQLite(PreparedStatement stmt, int i, UUIDPair pair) throws SQLException {
                stmt.setInt((i * 2) + 1, pair.id);
                stmt.setString((i * 2) + 2, pair.uuid.toString());
            }

            @Override
            public void setSQL(PreparedStatement stmt, UUIDPair pair) throws SQLException {
                stmt.setInt(1, pair.id);
                stmt.setString(2, pair.uuid.toString());
            }
        };
        setBulk(myList, mod, whenDone);
    }
    
    /**
     * Create a plot
     *
     * @param myList list of plots to be created
     */
    public void createPlots(final ArrayList<Plot> myList, final Runnable whenDone) {
        final StmtMod<Plot> mod = new StmtMod<Plot>() {
            @Override
            public String getCreateMySQL(int size) {
                return getCreateMySQL(size, CREATE_PLOTS, 5);
            }

            @Override
            public String getCreateSQLite(int size) {
                return getCreateSQLite(size, "INSERT INTO `" + prefix + "plot` SELECT ? AS `id`, ? AS `plot_id_x`, ? AS `plot_id_z`, ? AS `owner`, ? AS `world`, ? AS `timestamp` ", 6);
            }

            @Override
            public String getCreateSQL() {
                return CREATE_PLOT;
            }

            @Override
            public void setMySQL(PreparedStatement stmt, int i, Plot plot) throws SQLException {
                stmt.setInt((i * 5) + 1, plot.id.x);
                stmt.setInt((i * 5) + 2, plot.id.y);
                try {
                    stmt.setString((i * 4) + 3, plot.owner.toString());
                } catch (final Exception e) {
                    stmt.setString((i * 4) + 3, everyone.toString());
                }
                stmt.setString((i * 5) + 4, plot.world);
                stmt.setTimestamp((i * 5) + 5, new Timestamp(plot.getTimestamp()));
            }

            @Override
            public void setSQLite(PreparedStatement stmt, int i, Plot plot) throws SQLException {
                stmt.setNull((i * 6) + 1, 4);
                stmt.setInt((i * 6) + 2, plot.id.x);
                stmt.setInt((i * 6) + 3, plot.id.y);
                try {
                    stmt.setString((i * 6) + 4, plot.owner.toString());
                } catch (final Exception e1) {
                    stmt.setString((i * 6) + 4, everyone.toString());
                }
                stmt.setString((i * 6) + 5, plot.world);
                stmt.setTimestamp((i * 6) + 6, new Timestamp(plot.getTimestamp()));
            }

            @Override
            public void setSQL(PreparedStatement stmt, Plot plot) throws SQLException {
                stmt.setInt(1, plot.id.x);
                stmt.setInt(2, plot.id.y);
                stmt.setString(3, plot.owner.toString());
                stmt.setString(4, plot.world);
                stmt.setTimestamp(5, new Timestamp(plot.getTimestamp()));
                
            }
        };
        setBulk(myList, mod, whenDone);
    }

    public <T> void setBulk(ArrayList<T> objList, StmtMod<T> mod, Runnable whenDone) {
        final int size = objList.size();
        if (size == 0) {
            if (whenDone != null) whenDone.run();
            return;
        }
        int packet;
        if (MYSQL) {
            packet = Math.min(size, 5000);
        } else {
            packet = Math.min(size, 50);
        }
        final int amount = size / packet;
        try {
            int count = 0;
            PreparedStatement preparedStmt = null;
            String statement = null;
            int last = -1;
            for (int j = 0; j <= amount; j++) {
                final List<T> subList = objList.subList(j * packet, Math.min(size, (j + 1) * packet));
                if (subList.size() == 0) {
                    break;
                }
                if (last == -1) {
                    last = subList.size();
                    statement = mod.getCreateMySQL(subList.size());
                    preparedStmt = this.connection.prepareStatement(statement.toString());
                }
                if (subList.size() != last || (count % 5000 == 0 && count > 0)) {
                    preparedStmt.executeBatch();
                    preparedStmt.close();
                    statement = mod.getCreateMySQL(subList.size());
                    preparedStmt = this.connection.prepareStatement(statement.toString());
                }
                for (int i = 0; i < subList.size(); i++) {
                    count++;
                    final T obj = subList.get(i);
                    mod.setMySQL(preparedStmt, i, obj);
                }
                last = subList.size();
                preparedStmt.addBatch();
            }
            PS.debug("&aBatch 1: " + count + " | " + objList.get(0).getClass().getCanonicalName());
            preparedStmt.executeBatch();
            preparedStmt.clearParameters();
            preparedStmt.close();
            if (whenDone != null) whenDone.run();
            return;
        } catch (Exception e) {
            if (MYSQL) {
                e.printStackTrace();
                PS.debug("&cERROR 1: " + " | " + objList.get(0).getClass().getCanonicalName());
            }
        }
        try {
            int count = 0;
            PreparedStatement preparedStmt = null;
            String statement = null;
            int last = -1;
            for (int j = 0; j <= amount; j++) {
                final List<T> subList = objList.subList(j * packet, Math.min(size, (j + 1) * packet));
                if (subList.size() == 0) {
                    break;
                }
                if (last == -1) {
                    last = subList.size();
                    statement = mod.getCreateSQLite(subList.size());
                    preparedStmt = this.connection.prepareStatement(statement.toString());
                }
                if (subList.size() != last || (count % 5000 == 0 && count > 0)) {
                    preparedStmt.executeBatch();
                    preparedStmt.clearParameters();
                    statement = mod.getCreateSQLite(subList.size());
                    preparedStmt = this.connection.prepareStatement(statement.toString());
                }
                for (int i = 0; i < subList.size(); i++) {
                    count++;
                    final T obj = subList.get(i);
                    mod.setSQLite(preparedStmt, i, obj);
                }
                last = subList.size();
                preparedStmt.addBatch();
            }
            PS.debug("&aBatch 2: " + count + " | " + objList.get(0).getClass().getCanonicalName());
            preparedStmt.executeBatch();
            preparedStmt.clearParameters();
            preparedStmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            PS.debug("&cERROR 2: " + " | " + objList.get(0).getClass().getCanonicalName());
            PS.debug("&6[WARN] " + "Could not bulk save!");
            try {
                PreparedStatement preparedStmt = null;
                String nonBulk = mod.getCreateSQL();
                preparedStmt = this.connection.prepareStatement(nonBulk.toString());
                for (final T obj : objList) {
                    try {
                        mod.setSQL(preparedStmt, obj);
                        preparedStmt.addBatch();
                    } catch (final Exception e3) {
                        PS.debug("&c[ERROR] " + "Failed to save " + obj + "!");
                    }
                }
                PS.debug("&aBatch 3");
                preparedStmt.executeBatch();
                preparedStmt.close();
            } catch (Exception e3) {
                e3.printStackTrace();
                PS.debug("&c[ERROR] " + "Failed to save all!");
            }
        }
        if (whenDone != null) whenDone.run();
    }

    public void createSettings(final ArrayList<SettingsPair> myList, final Runnable whenDone) {
        final StmtMod<SettingsPair> mod = new StmtMod<SettingsPair>() {
            @Override
            public String getCreateMySQL(int size) {
                return getCreateMySQL(size, "INSERT INTO `" + prefix + "plot_settings`(`plot_plot_id`,`biome`,`rain`,`custom_time`,`time`,`deny_entry`,`alias`,`flags`,`merged`,`position`) VALUES ", 10);
            }

            @Override
            public String getCreateSQLite(int size) {
                return getCreateSQLite(size, "INSERT INTO `" + prefix + "plot_settings` SELECT ? AS `plot_plot_id`, ? AS `biome`, ? AS `rain`, ? AS `custom_time`, ? AS `time`, ? AS `deny_entry`, ? AS `alias`, ? AS `flags`, ? AS `merged`, ? AS `position`", 10);
            }

            @Override
            public String getCreateSQL() {
                return "INSERT INTO `" + SQLManager.this.prefix + "plot_settings`(`plot_plot_id`) VALUES(?)";
            }

            @Override
            public void setMySQL(PreparedStatement stmt, int i, SettingsPair pair) throws SQLException {
                stmt.setInt((i * 10) + 1, pair.id); // id
                stmt.setNull((i * 10) + 2, 4);  // biome
                stmt.setNull((i * 10) + 3, 4);  // rain
                stmt.setNull((i * 10) + 4, 4);  // custom_time
                stmt.setNull((i * 10) + 5, 4);  // time
                stmt.setNull((i * 10) + 6, 4);  // deny_entry
                if (pair.settings.getAlias().equals("")) {
                    stmt.setNull((i * 10) + 7, 4);
                } else {
                    stmt.setString((i * 10) + 7, pair.settings.getAlias());
                }
                if (pair.settings.flags == null) {
                    stmt.setNull((i * 10) + 8, 4);
                } else {
                    final StringBuilder flag_string = new StringBuilder();
                    int k = 0;
                    for (final Flag flag : pair.settings.flags.values()) {
                        if (k != 0) {
                            flag_string.append(",");
                        }
                        flag_string.append(flag.getKey() + ":" + flag.getValueString().replaceAll(":", "\u00AF").replaceAll(",", "\u00B4"));
                        k++;
                    }
                    stmt.setString((i * 10) + 8, flag_string.toString());
                }
                boolean[] merged = pair.settings.getMerged();
                int n = 0;
                for (int j = 0; j < 4; ++j) {
                    n = (n << 1) + (merged[j] ? 1 : 0);
                }
                stmt.setInt((i * 10) + 9, n);
                BlockLoc loc = pair.settings.getPosition();
                String position;
                if (loc.y == 0) {
                    position = "DEFAULT";
                } else {
                    position = loc.x + "," + loc.y + "," + loc.z;
                }
                stmt.setString((i * 10) + 10, position);
            }

            @Override
            public void setSQLite(PreparedStatement stmt, int i, SettingsPair pair) throws SQLException {
                stmt.setInt((i * 10) + 1, pair.id); // id
                stmt.setNull((i * 10) + 2, 4);  // biome
                stmt.setNull((i * 10) + 3, 4);  // rain
                stmt.setNull((i * 10) + 4, 4);  // custom_time
                stmt.setNull((i * 10) + 5, 4);  // time
                stmt.setNull((i * 10) + 6, 4);  // deny_entry
                if (pair.settings.getAlias().equals("")) {
                    stmt.setNull((i * 10) + 7, 4);
                } else {
                    stmt.setString((i * 10) + 7, pair.settings.getAlias());
                }
                if (pair.settings.flags == null) {
                    stmt.setNull((i * 10) + 8, 4);
                } else {
                    final StringBuilder flag_string = new StringBuilder();
                    int k = 0;
                    for (final Flag flag : pair.settings.flags.values()) {
                        if (k != 0) {
                            flag_string.append(",");
                        }
                        flag_string.append(flag.getKey() + ":" + flag.getValueString().replaceAll(":", "\u00AF").replaceAll(",", "\u00B4"));
                        k++;
                    }
                    stmt.setString((i * 10) + 8, flag_string.toString());
                }
                boolean[] merged = pair.settings.getMerged();
                int n = 0;
                for (int j = 0; j < 4; ++j) {
                    n = (n << 1) + (merged[j] ? 1 : 0);
                }
                stmt.setInt((i * 10) + 9, n);
                BlockLoc loc = pair.settings.getPosition();
                String position;
                if (loc.y == 0) {
                    position = "DEFAULT";
                } else {
                    position = loc.x + "," + loc.y + "," + loc.z;
                }
                stmt.setString((i * 10) + 10, position);
            }

            @Override
            public void setSQL(PreparedStatement stmt, SettingsPair pair) throws SQLException {
                stmt.setInt(1, pair.id);
            }
        };
        addGlobalTask(new Runnable() {
            @Override
            public void run() {
                setBulk(myList, mod, whenDone);
            }
        });
    }

    public void createEmptySettings(final ArrayList<Integer> myList, final Runnable whenDone) {
        final StmtMod<Integer> mod = new StmtMod<Integer>() {
            @Override
            public String getCreateMySQL(int size) {
                return getCreateMySQL(size, CREATE_SETTINGS, 1);
            }

            @Override
            public String getCreateSQLite(int size) {
                return getCreateSQLite(size, "INSERT INTO `" + prefix + "plot_settings` SELECT ? AS `plot_plot_id`, ? AS `biome`, ? AS `rain`, ? AS `custom_time`, ? AS `time`, ? AS `deny_entry`, ? AS `alias`, ? AS `flags`, ? AS `merged`, ? AS `position` ", 10);
            }

            @Override
            public String getCreateSQL() {
                return "INSERT INTO `" + SQLManager.this.prefix + "plot_settings`(`plot_plot_id`) VALUES(?)";
            }

            @Override
            public void setMySQL(PreparedStatement stmt, int i, Integer id) throws SQLException {
                stmt.setInt((i * 1) + 1, id);
            }

            @Override
            public void setSQLite(PreparedStatement stmt, int i, Integer id) throws SQLException {
                stmt.setInt((i * 10) + 1, id);
                stmt.setNull((i * 10) + 2, 4);
                stmt.setNull((i * 10) + 3, 4);
                stmt.setNull((i * 10) + 4, 4);
                stmt.setNull((i * 10) + 5, 4);
                stmt.setNull((i * 10) + 6, 4);
                stmt.setNull((i * 10) + 7, 4);
                stmt.setNull((i * 10) + 8, 4);
                stmt.setNull((i * 10) + 9, 4);
                stmt.setString((i * 10) + 10, "DEFAULT");
            }

            @Override
            public void setSQL(PreparedStatement stmt, Integer id) throws SQLException {
                stmt.setInt(1, id);
            }
        };
        addGlobalTask(new Runnable() {
            @Override
            public void run() {
                setBulk(myList, mod, whenDone);
            }
        });
    }

    /**
     * Create a plot
     *
     * @param plot
     */
    @Override
    public void createPlot(final Plot plot) {
        addPlotTask(plot, new UniqueStatement("createPlot") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, plot.id.x);
                stmt.setInt(2, plot.id.y);
                stmt.setString(3, plot.owner.toString());
                stmt.setString(4, plot.world);
                stmt.setTimestamp(5, new Timestamp(plot.getTimestamp()));
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement(SQLManager.this.CREATE_PLOT);
            }
        });
    }
    
    public void commit() {
        try {
            if (CLOSED) {
                return;
            }
            if (!this.connection.getAutoCommit()) {
                this.connection.commit();
                this.connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createPlotAndSettings(final Plot plot, final Runnable whenDone) {
        addPlotTask(plot, new UniqueStatement("createPlotAndSettings_" + plot.hashCode()) {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, plot.id.x);
                stmt.setInt(2, plot.id.y);
                stmt.setString(3, plot.owner.toString());
                stmt.setString(4, plot.world);
                stmt.setTimestamp(5, new Timestamp(plot.getTimestamp()));
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement(SQLManager.this.CREATE_PLOT, Statement.RETURN_GENERATED_KEYS);
            }
            
            @Override
            public void execute(PreparedStatement stmt) throws SQLException {
                
            }
            
            @Override
            public void addBatch(PreparedStatement stmt) throws SQLException {
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                int id = -1;
                if (keys.next()) {
                    plot.temp = keys.getInt(1);
                }
            }
        });
        addPlotTask(plot, new UniqueStatement("createPlotAndSettings_settings_" + plot.hashCode()) {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, getId(plot));
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_settings`(`plot_plot_id`) VALUES(" + "?)");
            }
        });
        addNotifyTask(whenDone);
    }

    /**
     * Create tables
     *
     * @throws SQLException
     */
    @Override
    public void createTables() throws SQLException {
        final String[] tables;
        if (Settings.ENABLE_CLUSTERS) {
            tables = new String[]{"plot", "plot_denied", "plot_helpers", "plot_comments", "plot_trusted", "plot_rating", "plot_settings", "cluster"};
        } else {
            tables = new String[]{"plot", "plot_denied", "plot_helpers", "plot_comments", "plot_trusted", "plot_rating", "plot_settings"};
        }
        final DatabaseMetaData meta = connection.getMetaData();
        int create = 0;
        for (final String s : tables) {
            ResultSet set = meta.getTables(null, null, prefix + s, null);
            if (!set.next()) {
                create++;
            }
            set.close();
        }
        if (create == 0) {
            return;
        }
        boolean add_constraint;
        add_constraint = create == tables.length;
        PS.debug("Creating tables");
        final Statement stmt = this.connection.createStatement();
        if (MYSQL) {
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot` (" + "`id` INT(11) NOT NULL AUTO_INCREMENT," + "`plot_id_x` INT(11) NOT NULL," + "`plot_id_z` INT(11) NOT NULL," + "`owner` VARCHAR(40) NOT NULL," + "`world` VARCHAR(45) NOT NULL," + "`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," + "PRIMARY KEY (`id`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=0");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_denied` (" + "`plot_plot_id` INT(11) NOT NULL," + "`user_uuid` VARCHAR(40) NOT NULL" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_helpers` (" + "`plot_plot_id` INT(11) NOT NULL," + "`user_uuid` VARCHAR(40) NOT NULL" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_comments` (" + "`world` VARCHAR(40) NOT NULL, `hashcode` INT(11) NOT NULL," + "`comment` VARCHAR(40) NOT NULL," + "`inbox` VARCHAR(40) NOT NULL," + "`timestamp` INT(11) NOT NULL," + "`sender` VARCHAR(40) NOT NULL" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_trusted` (" + "`plot_plot_id` INT(11) NOT NULL," + "`user_uuid` VARCHAR(40) NOT NULL" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_settings` (" + "  `plot_plot_id` INT(11) NOT NULL," + "  `biome` VARCHAR(45) DEFAULT 'FOREST'," + "  `rain` INT(1) DEFAULT 0," + "  `custom_time` TINYINT(1) DEFAULT '0'," + "  `time` INT(11) DEFAULT '8000'," + "  `deny_entry` TINYINT(1) DEFAULT '0'," + "  `alias` VARCHAR(50) DEFAULT NULL," + "  `flags` VARCHAR(512) DEFAULT NULL," + "  `merged` INT(11) DEFAULT NULL," + "  `position` VARCHAR(50) NOT NULL DEFAULT 'DEFAULT'," + "  PRIMARY KEY (`plot_plot_id`)," + "  UNIQUE KEY `unique_alias` (`alias`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_rating` ( `plot_plot_id` INT(11) NOT NULL, `rating` INT(2) NOT NULL, `player` VARCHAR(40) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8");
            if (add_constraint) {
                stmt.addBatch("ALTER TABLE `" + this.prefix + "plot_settings` ADD CONSTRAINT `" + this.prefix + "plot_settings_ibfk_1` FOREIGN KEY (`plot_plot_id`) REFERENCES `" + this.prefix + "plot` (`id`) ON DELETE CASCADE");
            }
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "cluster` (" + "`id` INT(11) NOT NULL AUTO_INCREMENT," + "`pos1_x` INT(11) NOT NULL," + "`pos1_z` INT(11) NOT NULL," + "`pos2_x` INT(11) NOT NULL," + "`pos2_z` INT(11) NOT NULL," + "`owner` VARCHAR(40) NOT NULL," + "`world` VARCHAR(45) NOT NULL," + "`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," + "PRIMARY KEY (`id`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=0");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "cluster_helpers` (" + "`cluster_id` INT(11) NOT NULL," + "`user_uuid` VARCHAR(40) NOT NULL" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "cluster_invited` (" + "`cluster_id` INT(11) NOT NULL," + "`user_uuid` VARCHAR(40) NOT NULL" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "cluster_settings` (" + "  `cluster_id` INT(11) NOT NULL," + "  `biome` VARCHAR(45) DEFAULT 'FOREST'," + "  `rain` INT(1) DEFAULT 0," + "  `custom_time` TINYINT(1) DEFAULT '0'," + "  `time` INT(11) DEFAULT '8000'," + "  `deny_entry` TINYINT(1) DEFAULT '0'," + "  `alias` VARCHAR(50) DEFAULT NULL," + "  `flags` VARCHAR(512) DEFAULT NULL," + "  `merged` INT(11) DEFAULT NULL," + "  `position` VARCHAR(50) NOT NULL DEFAULT 'DEFAULT'," + "  PRIMARY KEY (`cluster_id`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
        } else {
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot` (" + "`id` INTEGER PRIMARY KEY AUTOINCREMENT," + "`plot_id_x` INT(11) NOT NULL," + "`plot_id_z` INT(11) NOT NULL," + "`owner` VARCHAR(45) NOT NULL," + "`world` VARCHAR(45) NOT NULL," + "`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_denied` (" + "`plot_plot_id` INT(11) NOT NULL," + "`user_uuid` VARCHAR(40) NOT NULL" + ")");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_helpers` (" + "`plot_plot_id` INT(11) NOT NULL," + "`user_uuid` VARCHAR(40) NOT NULL" + ")");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_trusted` (" + "`plot_plot_id` INT(11) NOT NULL," + "`user_uuid` VARCHAR(40) NOT NULL" + ")");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_comments` (" + "`world` VARCHAR(40) NOT NULL, `hashcode` INT(11) NOT NULL," + "`comment` VARCHAR(40) NOT NULL," + "`inbox` VARCHAR(40) NOT NULL, `timestamp` INT(11) NOT NULL," + "`sender` VARCHAR(40) NOT NULL" + ")");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_settings` (" + "  `plot_plot_id` INT(11) NOT NULL," + "  `biome` VARCHAR(45) DEFAULT 'FOREST'," + "  `rain` INT(1) DEFAULT 0," + "  `custom_time` TINYINT(1) DEFAULT '0'," + "  `time` INT(11) DEFAULT '8000'," + "  `deny_entry` TINYINT(1) DEFAULT '0'," + "  `alias` VARCHAR(50) DEFAULT NULL," + "  `flags` VARCHAR(512) DEFAULT NULL," + "  `merged` INT(11) DEFAULT NULL," + "  `position` VARCHAR(50) NOT NULL DEFAULT 'DEFAULT'," + "  PRIMARY KEY (`plot_plot_id`)" + ")");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_rating` (`plot_plot_id` INT(11) NOT NULL, `rating` INT(2) NOT NULL, `player` VARCHAR(40) NOT NULL)");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "cluster` (" + "`id` INTEGER PRIMARY KEY AUTOINCREMENT," + "`pos1_x` INT(11) NOT NULL," + "`pos1_z` INT(11) NOT NULL," + "`pos2_x` INT(11) NOT NULL," + "`pos2_z` INT(11) NOT NULL," + "`owner` VARCHAR(40) NOT NULL," + "`world` VARCHAR(45) NOT NULL," + "`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP" + ")");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "cluster_helpers` (" + "`cluster_id` INT(11) NOT NULL," + "`user_uuid` VARCHAR(40) NOT NULL" + ")");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "cluster_invited` (" + "`cluster_id` INT(11) NOT NULL," + "`user_uuid` VARCHAR(40) NOT NULL" + ")");
            stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "cluster_settings` (" + "  `cluster_id` INT(11) NOT NULL," + "  `biome` VARCHAR(45) DEFAULT 'FOREST'," + "  `rain` INT(1) DEFAULT 0," + "  `custom_time` TINYINT(1) DEFAULT '0'," + "  `time` INT(11) DEFAULT '8000'," + "  `deny_entry` TINYINT(1) DEFAULT '0'," + "  `alias` VARCHAR(50) DEFAULT NULL," + "  `flags` VARCHAR(512) DEFAULT NULL," + "  `merged` INT(11) DEFAULT NULL," + "  `position` VARCHAR(50) NOT NULL DEFAULT 'DEFAULT'," + "  PRIMARY KEY (`cluster_id`)" + ")");
        }
        stmt.executeBatch();
        stmt.clearBatch();
        stmt.close();
    }

    /**
     * Delete a plot
     *
     * @param plot
     */
    @Override
    public void delete(final Plot plot) {
        PS.get().removePlot(plot.world, plot.id, false);
        addPlotTask(plot, new UniqueStatement("delete_plot_settings") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, getId(plot));
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_settings` WHERE `plot_plot_id` = ?");
            }
        });
        addPlotTask(plot, new UniqueStatement("delete_plot_helpers") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, getId(plot));
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_helpers` WHERE `plot_plot_id` = ?");
            }
        });
        addPlotTask(plot, new UniqueStatement("delete_plot_trusted") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, getId(plot));
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_trusted` WHERE `plot_plot_id` = ?");
            }
        });
        addPlotTask(plot, new UniqueStatement("delete_plot_denied") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, getId(plot));
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_denied` WHERE `plot_plot_id` = ?");
            }
        });
        addPlotTask(plot, new UniqueStatement("delete_plot_comments") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setString(1, plot.world);
                stmt.setInt(2, plot.hashCode());
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_comments` WHERE `world` = ? AND `hashcode` = ?");
            }
        });
        addPlotTask(plot, new UniqueStatement("delete_plot") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, getId(plot));
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot` WHERE `id` = ?");
            }
        });
    }

    /**
     * Create plot settings
     *
     * @param id
     * @param plot
     */
    @Override
    public void createPlotSettings(final int id, final Plot plot) {
        addPlotTask(plot, new UniqueStatement("createPlotSettings") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, id);
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_settings`(`plot_plot_id`) VALUES(" + "?)");
            }
        });
    }
    
    @Override
    public int getId(Plot plot) {
        if (plot.temp > 0) {
            return plot.temp;
        }
        PreparedStatement stmt = null;
        try {
            commit();
            if (plot.temp > 0) {
                return plot.temp;
            }
            stmt = this.connection.prepareStatement("SELECT `id` FROM `" + this.prefix + "plot` WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND world = ? ORDER BY `timestamp` ASC");
            stmt.setInt(1, plot.id.x);
            stmt.setInt(2, plot.id.y);
            stmt.setString(3, plot.world);
            final ResultSet r = stmt.executeQuery();
            int id = Integer.MAX_VALUE;
            while (r.next()) {
                id = r.getInt("id");
            }
            r.close();
            stmt.close();
            if (id == Integer.MAX_VALUE || id == 0) {
                if (plot.temp > 0) {
                    return plot.temp;
                }
                id = 1/0;
            }
            plot.temp = id;
            return id;
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return Integer.MAX_VALUE;
    }
    
    public void updateTables() {
        
        // TODO task
        
        if (PS.get().getVersion().equals(PS.get().getLastVersion()) ||  PS.get().getLastVersion() == null) {
            return;
        }
        try {
            final DatabaseMetaData data = this.connection.getMetaData();
            ResultSet rs = data.getColumns(null, null, this.prefix + "plot_comments", "plot_plot_id");
            if (rs.next()) {
                rs.close();
                rs = data.getColumns(null, null, this.prefix + "plot_comments", "hashcode");
                if (!rs.next()) {
                    rs.close();
                    try {
                        final Statement statement = this.connection.createStatement();
                        statement.addBatch("DROP TABLE `" + this.prefix + "plot_comments`");
                        if (Settings.DB.USE_MYSQL) {
                            statement.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_comments` (" + "`world` VARCHAR(40) NOT NULL, `hashcode` INT(11) NOT NULL," + "`comment` VARCHAR(40) NOT NULL," + "`inbox` VARCHAR(40) NOT NULL," + "`timestamp` INT(11) NOT NULL," + "`sender` VARCHAR(40) NOT NULL" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
                        } else {
                            statement.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_comments` (" + "`world` VARCHAR(40) NOT NULL, `hashcode` INT(11) NOT NULL," + "`comment` VARCHAR(40) NOT NULL," + "`inbox` VARCHAR(40) NOT NULL, `timestamp` INT(11) NOT NULL," + "`sender` VARCHAR(40) NOT NULL" + ")");
                        }
                        statement.executeBatch();
                        statement.close();
                    } catch (SQLException e) {
                        final Statement statement = this.connection.createStatement();
                        statement.addBatch("ALTER IGNORE TABLE `" + this.prefix + "plot_comments` ADD `inbox` VARCHAR(11) DEFAULT `public`");
                        statement.addBatch("ALTER IGNORE TABLE `" + this.prefix + "plot_comments` ADD `timestamp` INT(11) DEFAULT 0");
                        statement.addBatch("ALTER TABLE `" + this.prefix + "plot` DROP `tier`");
                        statement.executeBatch();
                        statement.close();
                    }
                }
            }
            rs.close();
            rs = data.getColumns(null, null, this.prefix + "plot_denied", "plot_plot_id");
            if (rs.next()) {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("DELETE FROM `" + this.prefix + "plot_denied` WHERE `plot_plot_id` NOT IN (SELECT `id` FROM `" + this.prefix + "plot`)");
                    statement.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                
                rs.close();
                try (Statement statement = connection.createStatement()) {
                    for (String table : new String[]{"plot_denied", "plot_helpers", "plot_trusted"} ) {
                        ResultSet result = statement.executeQuery("SELECT plot_plot_id, user_uuid, COUNT(*) FROM " + this.prefix + table + " GROUP BY plot_plot_id, user_uuid HAVING COUNT(*) > 1");
                        if (result.next()) {
                            PS.debug("BACKING UP: " + this.prefix + table);
                            result.close();
                            statement.executeUpdate("CREATE TABLE "  + this.prefix + table + "_tmp AS SELECT * FROM " + this.prefix + table + " GROUP BY plot_plot_id, user_uuid");
                            statement.executeUpdate("DROP TABLE " + this.prefix + table);
                            statement.executeUpdate("CREATE TABLE " + this.prefix + table + " AS SELECT * FROM "  + this.prefix + table + "_tmp");
                            statement.executeUpdate("DROP TABLE " + this.prefix + table + "_tmp");
                            PS.debug("RESTORING: " + this.prefix + table);
                        }
                    }
                    statement.close();
                }
                catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
            
    }

    /**
     * Load all plots, helpers, denied, trusted, and every setting from DB into a hashmap
     */
    @Override
    public ConcurrentHashMap<String, ConcurrentHashMap<PlotId, Plot>> getPlots() {
        final ConcurrentHashMap<String, ConcurrentHashMap<PlotId, Plot>> newplots = new ConcurrentHashMap<String, ConcurrentHashMap<PlotId, Plot>>();
        final HashMap<Integer, Plot> plots = new HashMap<>();
        Statement stmt = null;
        try {
            Set<String> worlds = new HashSet<>();
            if (PS.get().config.contains("worlds")) {
                worlds = PS.get().config.getConfigurationSection("worlds").getKeys(false);
            }
            final HashMap<String, UUID> uuids = new HashMap<String, UUID>();
            final HashMap<String, Integer> noExist = new HashMap<String, Integer>();
            
            PlotId plot_id;
            int id;
            Plot p;
            String o;
            UUID user;
            
            /*
             * Getting plots
             */
            stmt = this.connection.createStatement();
            try (ResultSet r = stmt.executeQuery("SELECT `id`, `plot_id_x`, `plot_id_z`, `owner`, `world`, `timestamp` FROM `" + this.prefix + "plot`")) {
                while (r.next()) {
                    plot_id = new PlotId(r.getInt("plot_id_x"), r.getInt("plot_id_z"));
                    id = r.getInt("id");
                    final String worldname = r.getString("world");
                    if (!worlds.contains(worldname)) {
                        if (noExist.containsKey(worldname)) {
                            noExist.put(worldname, noExist.get(worldname) + 1);
                        } else {
                            noExist.put(worldname, 1);
                        }
                    }
                    o = r.getString("owner");
                    user = uuids.get(o);
                    if (user == null) {
                        user = UUID.fromString(o);
                        uuids.put(o, user);
                    }
                    Timestamp timestamp = null;
                    try {
                        timestamp = r.getTimestamp("timestamp");
                    }
                    catch (Exception e) {};
                    long time;
                    if (timestamp == null) {
                        time = plot_id.hashCode();
                    }
                    else {
                        time = timestamp.getTime();
                    }
                    p = new Plot(plot_id, user, new HashSet<UUID>(), new HashSet<UUID>(), new HashSet<UUID>(), "", null, null, worldname, new boolean[]{false, false, false, false}, time, id);
                    plots.put(id, p);
                }
            }
            if (Settings.CACHE_RATINGS) {
                try (ResultSet r = stmt.executeQuery("SELECT `plot_plot_id`, `player`, `rating` FROM `" + this.prefix + "plot_rating`")) {
                    while (r.next()) {
                        id = r.getInt("plot_plot_id");
                        o = r.getString("player");
                        user = uuids.get(o);
                        if (user == null) {
                            user = UUID.fromString(o);
                            uuids.put(o, user);
                        }
                        final Plot plot = plots.get(id);
                        if (plot != null) {
                            if (plot.getSettings().ratings == null) {
                                plot.getSettings().ratings = new HashMap<UUID, Integer>();
                            }
                            plot.getSettings().ratings.put(user, r.getInt("rating"));
                        } else {
                            PS.debug("&cPLOT " + id + " in plot_helpers does not exist. Please create the plot or remove this entry.");
                        }
                    }
                }
            }
            
            /*
             * Getting helpers
             */
            try (ResultSet r = stmt.executeQuery("SELECT `user_uuid`, `plot_plot_id` FROM `" + this.prefix + "plot_helpers`")) {
                while (r.next()) {
                    id = r.getInt("plot_plot_id");
                    o = r.getString("user_uuid");
                    user = uuids.get(o);
                    if (user == null) {
                        user = UUID.fromString(o);
                        uuids.put(o, user);
                    }
                    final Plot plot = plots.get(id);
                    if (plot != null) {
                        plot.getTrusted().add(user);
                    } else {
                        PS.debug("&cPLOT " + id + " in plot_helpers does not exist. Please create the plot or remove this entry.");
                    }
                }
            }
            
            /*
             * Getting trusted
             */
            try (ResultSet r = stmt.executeQuery("SELECT `user_uuid`, `plot_plot_id` FROM `" + this.prefix + "plot_trusted`")) {
                while (r.next()) {
                    id = r.getInt("plot_plot_id");
                    o = r.getString("user_uuid");
                    user = uuids.get(o);
                    if (user == null) {
                        user = UUID.fromString(o);
                        uuids.put(o, user);
                    }
                    final Plot plot = plots.get(id);
                    if (plot != null) {
                        plot.getMembers().add(user);
                    } else {
                        PS.debug("&cPLOT " + id + " in plot_trusted does not exist. Please create the plot or remove this entry.");
                    }
                }
            }
            
            /*
             * Getting denied
             */
            try (ResultSet r = stmt.executeQuery("SELECT `user_uuid`, `plot_plot_id` FROM `" + this.prefix + "plot_denied`")) {
                while (r.next()) {
                    id = r.getInt("plot_plot_id");
                    o = r.getString("user_uuid");
                    user = uuids.get(o);
                    if (user == null) {
                        user = UUID.fromString(o);
                        uuids.put(o, user);
                    }
                    final Plot plot = plots.get(id);
                    if (plot != null) {
                        plot.getDenied().add(user);
                    } else {
                        PS.debug("&cPLOT " + id + " in plot_denied does not exist. Please create the plot or remove this entry.");
                    }
                }
            }
            
            try (ResultSet r = stmt.executeQuery("SELECT * FROM `" + this.prefix + "plot_settings`")) {
                while (r.next()) {
                    id = r.getInt("plot_plot_id");
                    final Plot plot = plots.get(id);
                    if (plot != null) {
                        plots.remove(id);
                        if (!newplots.containsKey(plot.world)) {
                            newplots.put(plot.world, new ConcurrentHashMap<PlotId, Plot>());
                        }
                        newplots.get(plot.world).put(plot.id, plot);
                        final String alias = r.getString("alias");
                        if (alias != null) {
                            plot.getSettings().setAlias(alias);
                        }
                        final String pos = r.getString("position");
                        switch (pos.toLowerCase()) {
                            case "":
                            case "default":
                            case "0,0,0":
                            case "center":
                                break;
                            default:
                                try {
                                    plot.getSettings().setPosition(BlockLoc.fromString(pos));
                                } catch (final Exception e) {
                                }
                        }
                        final Integer m = r.getInt("merged");
                        if (m != null) {
                            final boolean[] merged = new boolean[4];
                            for (int i = 0; i < 4; i++) {
                                merged[3 - i] = ((m) & (1 << i)) != 0;
                            }
                            plot.getSettings().setMerged(merged);
                        } else {
                            plot.getSettings().setMerged(new boolean[]{false, false, false, false});
                        }
                        String[] flags_string;
                        final String myflags = r.getString("flags");
                        if (myflags == null) {
                            flags_string = new String[]{};
                        } else {
                            if (myflags.length() > 0) {
                                flags_string = myflags.split(",");
                            } else {
                                flags_string = new String[]{};
                            }
                        }
                        final HashMap<String, Flag> flags = new HashMap<>();
                        boolean exception = false;
                        for (String element : flags_string) {
                            if (element.contains(":")) {
                                final String[] split = element.split(":");
                                try {
                                    final String flag_str = split[1].replaceAll("\u00AF", ":").replaceAll("\u00B4", ",");
                                    final Flag flag = new Flag(FlagManager.getFlag(split[0], true), flag_str);
                                    flags.put(flag.getKey(), flag);
                                } catch (final Exception e) {
                                    e.printStackTrace();
                                    exception = true;
                                }
                            } else {
                                element = element.replaceAll("\u00AF", ":").replaceAll("\u00B4", ",");
                                if (StringMan.isAlpha(element.replaceAll("_", "").replaceAll("-", ""))) {
                                    Flag flag = new Flag(FlagManager.getFlag(element, true), "");
                                    flags.put(flag.getKey(), flag);
                                } else {
                                    PS.debug("INVALID FLAG: " + element);
                                }
                            }
                        }
                        if (exception) {
                            PS.debug("&cPlot " + id + " | " + plot + " had an invalid flag. A fix has been attempted.");
                            PS.debug("&c" + myflags);
                            this.setFlags(plot, flags.values());
                        }
                        plot.getSettings().flags = flags;
                    } else {
                        PS.debug("&cPLOT " + id + " in plot_settings does not exist. Please create the plot or remove this entry.");
                    }
                }
                stmt.close();
            }
            if (plots.entrySet().size() > 0) {
                this.createEmptySettings(new ArrayList<Integer>(plots.keySet()), null);
            }
            boolean invalidPlot = false;
            for (Entry<String, Integer> entry : noExist.entrySet()) {
                String worldname = entry.getKey();
                invalidPlot = true;
                PS.debug("&c[WARNING] Found " + noExist.get(worldname) + " plots in DB for non existant world; '" + worldname + "'.");
            }
            if (invalidPlot) {
                PS.debug("&c[WARNING] - Please create the world/s or remove the plots using the purge command");
            }
        } catch (final SQLException e) {
            PS.debug("&7[WARN] " + "Failed to load plots.");
            e.printStackTrace();
        }
        return newplots;
    }

    @Override
    public void setMerged(final Plot plot, final boolean[] merged) {
        plot.getSettings().setMerged(merged);
        addPlotTask(plot, new UniqueStatement("setMerged") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                int n = 0;
                for (int i = 0; i < 4; ++i) {
                    n = (n << 1) + (merged[i] ? 1 : 0);
                }
                stmt.setInt(1, n);
                stmt.setInt(2, getId(plot));
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot_settings` SET `merged` = ? WHERE `plot_plot_id` = ?");
            }
        });
    }

    @Override
    public void swapPlots(final Plot p1, final Plot p2) {
        final String world = p1.world;
        final int id1 = getId(p1);
        final int id2 = getId(p2);
        final PlotId pos1 = p1.getId();
        final PlotId pos2 = p2.getId();
        addPlotTask(p1, new UniqueStatement("swapPlots") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, pos2.x);
                stmt.setInt(2, pos2.y);
                stmt.setInt(3, id1);
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot` SET `plot_id_x` = ?, `plot_id_z` = ? WHERE `id` = ?");
            }
        });
        addPlotTask(p2, new UniqueStatement("swapPlots") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, pos1.x);
                stmt.setInt(2, pos1.y);
                stmt.setInt(3, id2);
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot` SET `plot_id_x` = ?, `plot_id_z` = ? WHERE `id` = ?");
            }
        });
    }

    @Override
    public void movePlot(final Plot original, final Plot newPlot) {
        addPlotTask(original, new UniqueStatement("movePlot") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, newPlot.id.x);
                stmt.setInt(2, newPlot.id.y);
                stmt.setInt(3, getId(original));
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot` SET `plot_id_x` = ?, `plot_id_z` = ? WHERE `id` = ?");
            }
        });
        addPlotTask(newPlot, null);
    }

    @Override
    public void setFlags(final Plot plot, final Collection<Flag> flags) {
        final StringBuilder flag_string = new StringBuilder();
        int i = 0;
        for (final Flag flag : flags) {
            if (i != 0) {
                flag_string.append(",");
            }
            flag_string.append(flag.getKey() + ":" + flag.getValueString().replaceAll(":", "\u00AF").replaceAll(",", "\u00B4"));
            i++;
        }
        addPlotTask(plot, new UniqueStatement("setFlags") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setString(1, flag_string.toString());
                stmt.setInt(2, getId(plot));
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot_settings` SET `flags` = ? WHERE `plot_plot_id` = ?");
            }
        });
    }

    @Override
    public void setAlias(final Plot plot, final String alias) {
        addPlotTask(plot, new UniqueStatement("setAlias") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setString(1, alias);
                stmt.setInt(2, getId(plot));
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot_settings` SET `alias` = ?  WHERE `plot_plot_id` = ?");
            }
        });
    }

    /**
     * Purge all plots with the following database IDs
     */
    @Override
    public void purgeIds(final String world, final Set<Integer> uniqueIds) {
        addGlobalTask(new Runnable() {
            @Override
            public void run() {
                if (uniqueIds.size() > 0) {
                    try {
                        String stmt_prefix = "";
                        final StringBuilder idstr2 = new StringBuilder("");
                        for (final Integer id : uniqueIds) {
                            idstr2.append(stmt_prefix + id);
                            stmt_prefix = " OR `id` = ";
                        }
                        stmt_prefix = "";
                        final StringBuilder idstr = new StringBuilder("");
                        for (final Integer id : uniqueIds) {
                            idstr.append(stmt_prefix + id);
                            stmt_prefix = " OR `plot_plot_id` = ";
                        }
                        PreparedStatement stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + prefix + "plot_helpers` WHERE `plot_plot_id` = " + idstr + "");
                        stmt.executeUpdate();
                        stmt.close();
                        stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + prefix + "plot_denied` WHERE `plot_plot_id` = " + idstr + "");
                        stmt.executeUpdate();
                        stmt.close();
                        stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + prefix + "plot_settings` WHERE `plot_plot_id` = " + idstr + "");
                        stmt.executeUpdate();
                        stmt.close();
                        stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + prefix + "plot_trusted` WHERE `plot_plot_id` = " + idstr + "");
                        stmt.executeUpdate();
                        stmt.close();
                        stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + prefix + "plot` WHERE `id` = " + idstr2 + "");
                        stmt.executeUpdate();
                        stmt.close();
                    } catch (final SQLException e) {
                        e.printStackTrace();
                        PS.debug("&c[ERROR] " + "FAILED TO PURGE WORLD '" + world + "'!");
                        return;
                    }
                }
                PS.debug("&6[INFO] " + "SUCCESSFULLY PURGED WORLD '" + world + "'!");
            }
        });
    }

    @Override
    public void purge(final String world, final Set<PlotId> plots) {
        addGlobalTask(new Runnable() {
            @Override
            public void run() {
                PreparedStatement stmt;
                try {
                    stmt = SQLManager.this.connection.prepareStatement("SELECT `id`, `plot_id_x`, `plot_id_z` FROM `" + prefix + "plot` WHERE `world` = ?");
                    stmt.setString(1, world);
                    final ResultSet r = stmt.executeQuery();
                    PlotId plot_id;
                    final Set<Integer> ids = new HashSet<>();
                    while (r.next()) {
                        plot_id = new PlotId(r.getInt("plot_id_x"), r.getInt("plot_id_z"));
                        if (plots.contains(plot_id)) {
                            ids.add(r.getInt("id"));
                        }
                    }
                    purgeIds(world, ids);
                    stmt.close();
                    r.close();
                    for (Iterator<PlotId> iter = plots.iterator(); iter.hasNext(); ) {
                        PlotId plotId = iter.next();
                        iter.remove();
                        PlotId id = new PlotId(plotId.x, plotId.y);
                        PS.get().removePlot(world, new PlotId(id.x, id.y), true);
                    }
                } catch (final SQLException e) {
                    e.printStackTrace();
                    PS.debug("&c[ERROR] " + "FAILED TO PURGE WORLD '" + world + "'!");
                }
            }
        });
    }

    @Override
    public void setPosition(final Plot plot, final String position) {
        addPlotTask(plot, new UniqueStatement("setPosition") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setString(1, position);
                stmt.setInt(2, getId(plot));
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot_settings` SET `position` = ?  WHERE `plot_plot_id` = ?");
            }
        });
    }

    @Override
    public void removeComment(final Plot plot, final PlotComment comment) {
        addPlotTask(plot, new UniqueStatement("removeComment") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                if (plot != null) {
                    statement.setString(1, plot.world);
                    statement.setInt(2, plot.id.hashCode());
                    statement.setString(3, comment.comment);
                    statement.setString(4, comment.inbox);
                    statement.setString(5, comment.senderName);
                }
                else {
                    statement.setString(1, comment.comment);
                    statement.setString(2, comment.inbox);
                    statement.setString(3, comment.senderName);
                }
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                if (plot != null) {
                    return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_comments` WHERE `world` = ? AND `hashcode` = ? AND `comment` = ? AND `inbox` = ? AND `sender` = ?");
                }
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_comments` WHERE `comment` = ? AND `inbox` = ? AND `sender` = ?");
            }
        });
    }

    @Override
    public void clearInbox(final Plot plot, final String inbox) {
        addPlotTask(plot, new UniqueStatement("clearInbox") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                if (plot != null) {
                    statement.setString(1, plot.world);
                    statement.setInt(2, plot.id.hashCode());
                    statement.setString(3, inbox);
                }
                else {
                    statement.setString(1, inbox);
                }
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                if (plot != null) {
                    return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_comments` WHERE `world` = ? AND `hashcode` = ? AND `inbox` = ?");
                }
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_comments` `inbox` = ?");
            }
        });
    }

    @Override
    public void getComments(final Plot plot, final String inbox, final RunnableVal whenDone) {
            addPlotTask(plot, new UniqueStatement("getComments_" + plot) {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                if (plot != null) {
                    statement.setString(1, plot.world);
                    statement.setInt(2, plot.id.hashCode());
                    statement.setString(3, inbox);
                }
                else {
                    statement.setString(1, inbox);
                }
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                if (plot != null) {
                    return SQLManager.this.connection.prepareStatement("SELECT * FROM `" + prefix + "plot_comments` WHERE `world` = ? AND `hashcode` = ? AND `inbox` = ?");
                }
                return SQLManager.this.connection.prepareStatement("SELECT * FROM `" + prefix + "plot_comments` WHERE `inbox` = ?");
            }
            
            @Override
            public void execute(PreparedStatement stmt) throws SQLException {
            }
            
            @Override
            public void addBatch(PreparedStatement statement) throws SQLException {
                final ArrayList<PlotComment> comments = new ArrayList<PlotComment>();
                final ResultSet set = statement.executeQuery();
                PlotComment comment;
                while (set.next()) {
                    final String sender = set.getString("sender");
                    final String world = set.getString("world");
                    final int hash = set.getInt("hashcode");
                    PlotId id;
                    if (hash != 0) {
                        id = PlotId.unpair(hash);
                    } else {
                        id = null;
                    }
                    final String msg = set.getString("comment");
                    final long timestamp = set.getInt("timestamp") * 1000;
                    comment = new PlotComment(world, id, msg, sender, inbox, timestamp);
                    comments.add(comment);
                    whenDone.value = comments;
                }
                TaskManager.runTask(whenDone);
            }
        });
    }

    @Override
    public void setComment(final Plot plot, final PlotComment comment) {
        addPlotTask(plot, new UniqueStatement("setComment") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setString(1, plot.world);
                statement.setInt(2, plot.id.hashCode());
                statement.setString(3, comment.comment);
                statement.setString(4, comment.inbox);
                statement.setInt(5, (int) (comment.timestamp / 1000));
                statement.setString(6, comment.senderName);
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_comments` (`world`, `hashcode`, `comment`, `inbox`, `timestamp`, `sender`) VALUES(?,?,?,?,?,?)");
            }
        });
    }

    @Override
    public void removeTrusted(final Plot plot, final UUID uuid) {
        addPlotTask(plot, new UniqueStatement("removeTrusted") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setInt(1, getId(plot));
                statement.setString(2, uuid.toString());
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_helpers` WHERE `plot_plot_id` = ? AND `user_uuid` = ?");
            }
        });
    }

    @Override
    public void removeMember(final Plot plot, final UUID uuid) {
        addPlotTask(plot, new UniqueStatement("removeMember") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setInt(1, getId(plot));
                statement.setString(2, uuid.toString());
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_trusted` WHERE `plot_plot_id` = ? AND `user_uuid` = ?");
            }
        });
    }

    @Override
    public void setTrusted(final Plot plot, final UUID uuid) {
        addPlotTask(plot, new UniqueStatement("setTrusted") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setInt(1, getId(plot));
                statement.setString(2, uuid.toString());
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_helpers` (`plot_plot_id`, `user_uuid`) VALUES(?,?)");
            }
        });
    }

    @Override
    public void setMember(final Plot plot, final UUID uuid) {
        addPlotTask(plot, new UniqueStatement("setMember") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setInt(1, getId(plot));
                statement.setString(2, uuid.toString());
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_trusted` (`plot_plot_id`, `user_uuid`) VALUES(?,?)");
            }
        });
    }

    @Override
    public void removeDenied(final Plot plot, final UUID uuid) {
        addPlotTask(plot, new UniqueStatement("removeDenied") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setInt(1, getId(plot));
                statement.setString(2, uuid.toString());
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_denied` WHERE `plot_plot_id` = ? AND `user_uuid` = ?");
            }
        });
    }

    @Override
    public void setDenied(final Plot plot, final UUID uuid) {
        addPlotTask(plot, new UniqueStatement("setDenied") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setInt(1, getId(plot));
                statement.setString(2, uuid.toString());
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_denied` (`plot_plot_id`, `user_uuid`) VALUES(?,?)");
            }
        });
    }

    @Override
    public HashMap<UUID, Integer> getRatings(final Plot plot) {
        HashMap<UUID, Integer> map = new HashMap<UUID, Integer>();
        try {
            final PreparedStatement statement = this.connection.prepareStatement("SELECT `rating`, `player` FROM `" + this.prefix + "plot_rating` WHERE `plot_plot_id` = ? ");
            statement.setInt(1, getId(plot));
            final ResultSet set = statement.executeQuery();
            while (set.next()) {
                UUID uuid = UUID.fromString(set.getString("player"));
                int rating = set.getInt("rating");
                map.put(uuid, rating);
            }
            statement.close();
            set.close();
        } catch (final SQLException e) {
            PS.debug("&7[WARN] " + "Failed to fetch rating for plot " + plot.getId().toString());
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public void setRating(final Plot plot, final UUID rater, final int value) {
        addPlotTask(plot, new UniqueStatement("setRating") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setInt(1, getId(plot));
                statement.setInt(2, value);
                statement.setString(3, rater.toString());
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_rating` (`plot_plot_id`, `rating`, `player`) VALUES(?,?,?)");
            }
        });
    }

    @Override
    public void delete(final PlotCluster cluster) {
        final int id = getClusterId(cluster.world, ClusterManager.getClusterId(cluster));
        addClusterTask(cluster, new UniqueStatement("delete_cluster_settings") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, id);
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "cluster_settings` WHERE `cluster_id` = ?");
            }
        });
        addClusterTask(cluster, new UniqueStatement("delete_cluster_helpers") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, id);
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "cluster_helpers` WHERE `cluster_id` = ?");
            }
        });
        addClusterTask(cluster, new UniqueStatement("delete_cluster_invited") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, id);
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "cluster_invited` WHERE `cluster_id` = ?");
            }
        });
        addClusterTask(cluster, new UniqueStatement("delete_cluster") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, id);
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "cluster` WHERE `id` = ?");
            }
        });
    }

    @Override
    public int getClusterId(final String world, final PlotClusterId id) {
        PreparedStatement stmt = null;
        try {
            stmt = this.connection.prepareStatement("SELECT `id` FROM `" + this.prefix + "cluster` WHERE `pos1_x` = ? AND `pos1_z` = ? AND `pos2_x` = ? AND `pos2_z` = ? AND `world` = ? ORDER BY `timestamp` ASC");
            stmt.setInt(1, id.pos1.x);
            stmt.setInt(2, id.pos1.y);
            stmt.setInt(3, id.pos2.x);
            stmt.setInt(4, id.pos2.y);
            stmt.setString(5, world);
            final ResultSet r = stmt.executeQuery();
            int c_id = Integer.MAX_VALUE;
            while (r.next()) {
                c_id = r.getInt("id");
            }
            stmt.close();
            r.close();
            return c_id;
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public HashMap<String, HashSet<PlotCluster>> getClusters() {
        final LinkedHashMap<String, HashSet<PlotCluster>> newClusters = new LinkedHashMap<>();
        final HashMap<Integer, PlotCluster> clusters = new HashMap<>();
        Statement stmt = null;
        try {
            Set<String> worlds = new HashSet<>();
            if (PS.get().config.contains("worlds")) {
                worlds = PS.get().config.getConfigurationSection("worlds").getKeys(false);
            }
            final HashMap<String, UUID> uuids = new HashMap<String, UUID>();
            final HashMap<String, Integer> noExist = new HashMap<String, Integer>();
            /*
             * Getting clusters
             */
            stmt = this.connection.createStatement();
            ResultSet r = stmt.executeQuery("SELECT * FROM `" + this.prefix + "cluster`");
            PlotId pos1;
            PlotId pos2;
            PlotCluster cluster;
            String owner;
            String worldname;
            UUID user;
            int id;
            while (r.next()) {
                pos1 = new PlotId(r.getInt("pos1_x"), r.getInt("pos1_z"));
                pos2 = new PlotId(r.getInt("pos2_x"), r.getInt("pos2_z"));
                id = r.getInt("id");
                worldname = r.getString("world");
                if (!worlds.contains(worldname)) {
                    if (noExist.containsKey(worldname)) {
                        noExist.put(worldname, noExist.get(worldname) + 1);
                    } else {
                        noExist.put(worldname, 1);
                    }
                }
                owner = r.getString("owner");
                user = uuids.get(owner);
                if (user == null) {
                    user = UUID.fromString(owner);
                    uuids.put(owner, user);
                }
                cluster = new PlotCluster(worldname, pos1, pos2, user);
                clusters.put(id, cluster);
            }
            /*
             * Getting helpers
             */
            r = stmt.executeQuery("SELECT `user_uuid`, `cluster_id` FROM `" + this.prefix + "cluster_helpers`");
            while (r.next()) {
                id = r.getInt("cluster_id");
                owner = r.getString("user_uuid");
                user = uuids.get(owner);
                if (user == null) {
                    user = UUID.fromString(owner);
                    uuids.put(owner, user);
                }
                cluster = clusters.get(id);
                if (cluster != null) {
                    cluster.helpers.add(user);
                } else {
                    PS.debug("&cCluster " + id + " in cluster_helpers does not exist. Please create the cluster or remove this entry.");
                }
            }
            /*
             * Getting invited
             */
            r = stmt.executeQuery("SELECT `user_uuid`, `cluster_id` FROM `" + this.prefix + "cluster_invited`");
            while (r.next()) {
                id = r.getInt("cluster_id");
                owner = r.getString("user_uuid");
                user = uuids.get(owner);
                if (user == null) {
                    user = UUID.fromString(owner);
                    uuids.put(owner, user);
                }
                cluster = clusters.get(id);
                if (cluster != null) {
                    cluster.invited.add(user);
                } else {
                    PS.debug("&cCluster " + id + " in cluster_invited does not exist. Please create the cluster or remove this entry.");
                }
            }
            r = stmt.executeQuery("SELECT * FROM `" + this.prefix + "cluster_settings`");
            while (r.next()) {
                id = r.getInt("cluster_id");
                cluster = clusters.get(id);
                if (cluster != null) {
                    final String alias = r.getString("alias");
                    if (alias != null) {
                        cluster.settings.setAlias(alias);
                    }
                    final String pos = r.getString("position");
                    switch (pos.toLowerCase()) {
                        case "":
                        case "default":
                        case "0,0,0":
                        case "center":
                            break;
                        default:
                            try {
                                final String[] split = pos.split(",");
                                final BlockLoc loc = new BlockLoc(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                                cluster.settings.setPosition(loc);
                            } catch (final Exception e) {
                            }
                    }
                    final Integer m = r.getInt("merged");
                    if (m != null) {
                        final boolean[] merged = new boolean[4];
                        for (int i = 0; i < 4; i++) {
                            merged[3 - i] = ((m) & (1 << i)) != 0;
                        }
                        cluster.settings.setMerged(merged);
                    } else {
                        cluster.settings.setMerged(new boolean[]{false, false, false, false});
                    }
                    String[] flags_string;
                    final String myflags = r.getString("flags");
                    if (myflags == null) {
                        flags_string = new String[]{};
                    } else {
                        if (myflags.length() > 0) {
                            flags_string = myflags.split(",");
                        } else {
                            flags_string = new String[]{};
                        }
                    }
                    final HashMap<String, Flag> flags = new HashMap<>();
                    boolean exception = false;
                    for (final String element : flags_string) {
                        if (element.contains(":")) {
                            final String[] split = element.split(":");
                            try {
                                final String flag_str = split[1].replaceAll("\u00AF", ":").replaceAll("�", ",");
                                final Flag flag = new Flag(FlagManager.getFlag(split[0], true), flag_str);
                                flags.put(flag.getKey(), flag);
                            } catch (final Exception e) {
                                e.printStackTrace();
                                exception = true;
                            }
                        } else {
                            Flag flag = new Flag(FlagManager.getFlag(element, true), "");
                            flags.put(flag.getKey(), flag);
                        }
                    }
                    if (exception) {
                        PS.debug("&cCluster " + id + " had an invalid flag. A fix has been attempted.");
                        PS.debug("&c" + myflags);
                    }
                    cluster.settings.flags = flags;
                } else {
                    PS.debug("&cCluster " + id + " in cluster_settings does not exist. Please create the cluster or remove this entry.");
                }
            }
            stmt.close();
            r.close();
            for (final PlotCluster c : clusters.values()) {
                final String world = c.world;
                if (!newClusters.containsKey(world)) {
                    newClusters.put(world, new HashSet<PlotCluster>());
                }
                newClusters.get(world).add(c);
            }
            boolean invalidPlot = false;
            for (Entry<String, Integer> entry : noExist.entrySet()) {
                String w = entry.getKey();
                invalidPlot = true;
                PS.debug("&c[WARNING] Found " + noExist.get(w) + " clusters in DB for non existant world; '" + w + "'.");
            }
            if (invalidPlot) {
                PS.debug("&c[WARNING] - Please create the world/s or remove the clusters using the purge command");
            }
        } catch (final SQLException e) {
            PS.debug("&7[WARN] " + "Failed to load clusters.");
            e.printStackTrace();
        }
        return newClusters;
    }

    @Override
    public void setFlags(final PlotCluster cluster, final Collection<Flag> flags) {
        final StringBuilder flag_string = new StringBuilder();
        int i = 0;
        for (final Flag flag : flags) {
            if (i != 0) {
                flag_string.append(",");
            }
            flag_string.append(flag.getKey() + ":" + flag.getValueString().replaceAll(":", "\u00AF").replaceAll(",", "\u00B4"));
            i++;
        }
        addClusterTask(cluster, new UniqueStatement("setFlags") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setString(1, flag_string.toString());
                stmt.setInt(2, getClusterId(cluster.world, ClusterManager.getClusterId(cluster)));
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "cluster_settings` SET `flags` = ? WHERE `cluster_id` = ?");
            }
        });
    }

    @Override
    public void setClusterName(final PlotCluster cluster, final String name) {
        addClusterTask(cluster, new UniqueStatement("setClusterName") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setString(1, name);
                stmt.setInt(2, getClusterId(cluster.world, ClusterManager.getClusterId(cluster)));
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "cluster_settings` SET `alias` = ?  WHERE `cluster_id` = ?");
            }
        });
        cluster.settings.setAlias(name);
    }

    @Override
    public void removeHelper(final PlotCluster cluster, final UUID uuid) {
        addClusterTask(cluster, new UniqueStatement("removeHelper") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setInt(1, getClusterId(cluster.world, ClusterManager.getClusterId(cluster)));
                statement.setString(2, uuid.toString());
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "cluster_helpers` WHERE `cluster_id` = ? AND `user_uuid` = ?");
            }
        });
    }

    @Override
    public void setHelper(final PlotCluster cluster, final UUID uuid) {
        addClusterTask(cluster, new UniqueStatement("setHelper") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setInt(1, getClusterId(cluster.world, ClusterManager.getClusterId(cluster)));
                statement.setString(2, uuid.toString());
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "cluster_helpers` (`cluster_id`, `user_uuid`) VALUES(?,?)");
            }
        });
    }

    @Override
    public void createCluster(final PlotCluster cluster) {
        addClusterTask(cluster, new UniqueStatement("createCluster") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, cluster.getP1().x);
                stmt.setInt(2, cluster.getP1().y);
                stmt.setInt(3, cluster.getP2().x);
                stmt.setInt(4, cluster.getP2().y);
                stmt.setString(5, cluster.owner.toString());
                stmt.setString(6, cluster.world);
                // TODO resultset getId
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement(SQLManager.this.CREATE_CLUSTER);
            }
        });
        addClusterTask(cluster, new UniqueStatement("createClusterSettings") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                final int id = getClusterId(cluster.world, ClusterManager.getClusterId(cluster));
                stmt.setInt(1, id);
                stmt.setString(2, cluster.settings.getAlias());
                stmt.executeUpdate();
                stmt.close();
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "cluster_settings`(`cluster_id`, `alias`) VALUES(?, ?" + ")");
            }
        });
    }

    @Override
    public void resizeCluster(final PlotCluster current, final PlotClusterId resize) {
        final PlotId pos1 = new PlotId(current.getP1().x, current.getP1().y);
        final PlotId pos2 = new PlotId(current.getP2().x, current.getP2().y);
        current.setP1(resize.pos1);
        current.setP2(resize.pos2);
        
        addClusterTask(current, new UniqueStatement("resizeCluster") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, pos1.x);
                stmt.setInt(2, pos1.y);
                stmt.setInt(3, pos2.x);
                stmt.setInt(4, pos2.y);
                stmt.setInt(5, getClusterId(current.world, ClusterManager.getClusterId(current)));
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "cluster` SET `pos1_x` = ?, `pos1_z` = ?, `pos2_x` = ?, `pos2_z` = ?  WHERE `id` = ?");
            }
        });
    }

    @Override
    public void setPosition(final PlotCluster cluster, final String position) {
        addClusterTask(cluster, new UniqueStatement("setPosition") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setString(1, position);
                stmt.setInt(2, getClusterId(cluster.world, ClusterManager.getClusterId(cluster)));
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "cluster_settings` SET `position` = ?  WHERE `cluster_id` = ?");
            }
        });
    }

    @Override
    public void removeInvited(final PlotCluster cluster, final UUID uuid) {
        addClusterTask(cluster, new UniqueStatement("removeInvited") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setInt(1, getClusterId(cluster.world, ClusterManager.getClusterId(cluster)));
                statement.setString(2, uuid.toString());
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "cluster_invited` WHERE `cluster_id` = ? AND `user_uuid` = ?");
            }
        });
    }

    @Override
    public void setInvited(final PlotCluster cluster, final UUID uuid) {
        addClusterTask(cluster, new UniqueStatement("setInvited") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setInt(1, getClusterId(cluster.world, ClusterManager.getClusterId(cluster)));
                statement.setString(2, uuid.toString());
            }
            
            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "cluster_invited` (`cluster_id`, `user_uuid`) VALUES(?,?)");
            }
        });
    }

    @Override
    public boolean deleteTables() {
        addGlobalTask(new Runnable() {
            @Override
            public void run() {
                try {
                    close();
                    SQLManager.this.connection = database.forceConnection();
                    final Statement stmt = connection.createStatement();
                    stmt.addBatch("DROP TABLE `" + prefix + "cluster_invited`");
                    stmt.addBatch("DROP TABLE `" + prefix + "cluster_helpers`");
                    stmt.addBatch("DROP TABLE `" + prefix + "cluster`");
                    stmt.addBatch("DROP TABLE `" + prefix + "plot_rating`");
                    stmt.addBatch("DROP TABLE `" + prefix + "plot_settings`");
                    stmt.addBatch("DROP TABLE `" + prefix + "plot_comments`");
                    stmt.addBatch("DROP TABLE `" + prefix + "plot_trusted`");
                    stmt.addBatch("DROP TABLE `" + prefix + "plot_helpers`");
                    stmt.addBatch("DROP TABLE `" + prefix + "plot_denied`");
                    stmt.executeBatch();
                    stmt.clearBatch();
                    stmt.close();
        
                    PreparedStatement statement = connection.prepareStatement("DROP TABLE `" + prefix + "plot`");
                    statement.executeUpdate();
                    statement.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return true;
    }

    private class UUIDPair {
        public final int id;
        public final UUID uuid;

        public UUIDPair(int id, UUID uuid) {
            this.id = id;
            this.uuid = uuid;
        }
    }

    private class SettingsPair {
        public final int id;
        public final PlotSettings settings;

        public SettingsPair(int id, PlotSettings settings) {
            this.id = id;
            this.settings = settings;
        }
    }

    @Override
    public void validateAllPlots(Set<Plot> toValidate) {
        try {
            if (connection.isClosed() || CLOSED) {
                CLOSED = false;
                connection = database.forceConnection();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        PS.debug("$1All DB transactions during this session are being validated (This may take a while if corrections need to be made)");
        commit();
        while (true) {
            if (!sendBatch()) {
                break;
            }
        }
        try {
            if (connection.getAutoCommit()) {
                connection.setAutoCommit(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ConcurrentHashMap<String, ConcurrentHashMap<PlotId, Plot>> database = getPlots();
        ArrayList<Plot> toCreate = new ArrayList<>();
        for (Plot plot : PS.get().getPlotsRaw()) {
            if (plot.temp == -1) {
                continue;
            }
            ConcurrentHashMap<PlotId, Plot> worldplots = database.get(plot.world);
            if (worldplots == null) {
                PS.debug("&8 - &7Creating plot (1): " + plot);
                toCreate.add(plot);
                continue;
            }
            Plot dataplot = worldplots.remove(plot.id);
            if (dataplot == null) {
                PS.debug("&8 - &7Creating plot (2): " + plot);
                toCreate.add(plot);
                continue;
            }
            // owner
            if (!plot.owner.equals(dataplot.owner)) {
                PS.debug("&8 - &7Setting owner: " + plot +" -> " + MainUtil.getName(plot.owner));
                setOwner(plot, plot.owner);
            }
            // trusted
            if (!plot.getTrusted().equals(dataplot.trusted)) {
                HashSet<UUID> toAdd = (HashSet<UUID>) plot.getTrusted().clone();
                HashSet<UUID> toRemove = (HashSet<UUID>) dataplot.getTrusted().clone();
                toRemove.removeAll(plot.getTrusted());
                toAdd.removeAll(dataplot.getTrusted());
                PS.debug("&8 - &7Correcting " + (toAdd.size() + toRemove.size()) + " trusted for: " + plot);
                if (toRemove.size() > 0) {
                    for (UUID uuid : toRemove) {
                        removeTrusted(plot, uuid);
                    }
                }
                if (toAdd.size() > 0) {
                    for (UUID uuid : toAdd) {
                        setTrusted(plot, uuid);
                    }
                }
            }
            if (!plot.getMembers().equals(dataplot.members)) {
                HashSet<UUID> toAdd = (HashSet<UUID>) plot.getMembers().clone();
                HashSet<UUID> toRemove = (HashSet<UUID>) dataplot.getMembers().clone();
                toRemove.removeAll(plot.getMembers());
                toAdd.removeAll(dataplot.getMembers());
                PS.debug("&8 - &7Correcting " + (toAdd.size() + toRemove.size()) + " members for: " + plot);
                if (toRemove.size() > 0) {
                    for (UUID uuid : toRemove) {
                        removeMember(plot, uuid);
                    }
                }
                if (toAdd.size() > 0) {
                    for (UUID uuid : toAdd) {
                        setMember(plot, uuid);
                    }
                }
            }
            if (!plot.getDenied().equals(dataplot.denied)) {
                HashSet<UUID> toAdd = (HashSet<UUID>) plot.getDenied().clone();
                HashSet<UUID> toRemove = (HashSet<UUID>) dataplot.getDenied().clone();
                toRemove.removeAll(plot.getDenied());
                toAdd.removeAll(dataplot.getDenied());
                PS.debug("&8 - &7Correcting " + (toAdd.size() + toRemove.size()) + " denied for: " + plot);
                if (toRemove.size() > 0) {
                    for (UUID uuid : toRemove) {
                        removeDenied(plot, uuid);
                    }
                }
                if (toAdd.size() > 0) {
                    for (UUID uuid : toAdd) {
                        setDenied(plot, uuid);
                    }
                }
            }
            PlotSettings ps = plot.getSettings();
            PlotSettings ds = dataplot.getSettings();
            boolean[] pm = ps.getMerged();
            boolean[] dm = ds.getMerged();
            if (pm[0] != dm[0] || pm[1] != dm[1] || pm[1] != dm[1] || pm[1] != dm[1]) {
                PS.debug("&8 - &7Correcting merge for: " + plot);
                setMerged(dataplot, ps.getMerged());
            }
            HashMap<String, Flag> pf = ps.flags;
            HashMap<String, Flag> df = ds.flags;
            if (pf.size() != 0 && df.size() != 0) {
                if (pf.size() != df.size() || !StringMan.isEqual(StringMan.joinOrdered(pf.values(), ","),StringMan.joinOrdered(df.values(), ","))) {
                    PS.debug("&8 - &7Correcting flags for: " + plot);
                    setFlags(plot, pf.values());
                }
            }
            // TODO comments (null)
            // TODO ratings (null)
            // TODO alias
            // TODO unconnected entries from helpers, trusted, denied, comments, settings, rating
        }
        
        for (Entry<String, ConcurrentHashMap<PlotId, Plot>> entry : database.entrySet()) {
            ConcurrentHashMap<PlotId, Plot> map = entry.getValue();
            if (map.size() > 0) {
                for (Entry<PlotId, Plot> entry2 : map.entrySet()) {
                    PS.debug("$1Plot was deleted: " + entry.getValue() + "// TODO implement this when sure safe");
                }
            }
        }
        commit();
    }

    @Override
    public void close() {
        try {
            CLOSED = true;
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}