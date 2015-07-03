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

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.object.comment.PlotComment;
import com.intellectualcrafters.plot.util.ClusterManager;
import com.intellectualcrafters.plot.util.TaskManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.block.Biome;

import java.sql.*;
import java.util.*;

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
    // Private Final
    private Connection connection;

    /**
     * Constructor
     *
     * @param c connection
     * @param p prefix
     */
    public SQLManager(final Connection c, final String p) {
        // Private final
        this.connection = c;
        this.prefix = p;
        // Set timout
        // setTimout();
        // Public final
        this.SET_OWNER = "UPDATE `" + this.prefix + "plot` SET `owner` = ? WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND `world` = ?";
        this.GET_ALL_PLOTS = "SELECT `id`, `plot_id_x`, `plot_id_z`, `world` FROM `" + this.prefix + "plot`";
        this.CREATE_PLOTS = "INSERT INTO `" + this.prefix + "plot`(`plot_id_x`, `plot_id_z`, `owner`, `world`) values ";
        this.CREATE_SETTINGS = "INSERT INTO `" + this.prefix + "plot_settings` (`plot_plot_id`) values ";
        this.CREATE_TIERS = "INSERT INTO `" + this.prefix + "plot_%tier%` (`plot_plot_id`, `user_uuid`) values ";
        this.CREATE_PLOT = "INSERT INTO `" + this.prefix + "plot`(`plot_id_x`, `plot_id_z`, `owner`, `world`) VALUES(?, ?, ?, ?)";
        this.CREATE_CLUSTER = "INSERT INTO `" + this.prefix + "cluster`(`pos1_x`, `pos1_z`, `pos2_x`, `pos2_z`, `owner`, `world`) VALUES(?, ?, ?, ?, ?, ?)";
        // schedule reconnect
        if (Settings.DB.USE_MYSQL) {
            TaskManager.runTaskRepeat(new Runnable() {
                @Override
                public void run() {
                    try {
                        SQLManager.this.connection = PS.get().getDatabase().forceConnection();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 11000);
        }
        updateTables();
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
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement statement = SQLManager.this.connection.prepareStatement(SQLManager.this.SET_OWNER);
                    statement.setString(1, uuid.toString());
                    statement.setInt(2, plot.id.x);
                    statement.setInt(3, plot.id.y);
                    statement.setString(4, plot.world);
                    statement.executeUpdate();
                    statement.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                    PS.log("&c[ERROR] " + "Could not set owner for plot " + plot.id);
                }
            }
        });
    }

    @Override
    public void createPlotsAndData(final ArrayList<Plot> myList, final Runnable whenDone) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create the plots
                    createPlots(myList);

                    // Creating datastructures
                    HashMap<PlotId, Plot> plotMap = new HashMap<>();
                    for (Plot plot : myList) {
                        plotMap.put(plot.id, plot);
                    }
                    ArrayList<SettingsPair> settings = new ArrayList<>();
                    ArrayList<UUIDPair> helpers = new ArrayList<>();
                    ArrayList<UUIDPair> trusted = new ArrayList<>();
                    ArrayList<UUIDPair> denied = new ArrayList<>();

                    // Populating structures
                    final PreparedStatement stmt = connection.prepareStatement(GET_ALL_PLOTS);
                    final ResultSet result = stmt.executeQuery();
                    while (result.next()) {
                        final int id = result.getInt("id");
                        int x = result.getInt("plot_id_x");
                        int y = result.getInt("plot_id_z");
                        PlotId plotId = new PlotId(x, y);
                        Plot plot = plotMap.get(plotId);
                        if (plot != null) {
                            settings.add(new SettingsPair(id, plot.settings));
                            if (plot.denied != null) {
                                for (UUID uuid : plot.denied) {
                                    denied.add(new UUIDPair(id, uuid));
                                }
                            }
                            if (plot.members != null) {
                                for (UUID uuid : plot.members) {
                                    trusted.add(new UUIDPair(id, uuid));
                                }
                            }
                            if (plot.trusted != null) {
                                for (UUID uuid : plot.trusted) {
                                    helpers.add(new UUIDPair(id, uuid));
                                }
                            }
                        }
                    }
                    createSettings(settings);
                    createTiers(helpers, "helpers");
                    createTiers(trusted, "trusted");
                    createTiers(denied, "denied");
                    TaskManager.runTaskLater(whenDone, 60);
                } catch (SQLException e) {
                    e.printStackTrace();
                    PS.log("&7[WARN] " + "Failed to set all helpers for plots");
                }
            }
        });
    }

    /**
     * Create a plot
     *
     * @param myList list of plots to be created
     */
    public void createTiers(final ArrayList<UUIDPair> myList, final String tier) {
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
        setBulk(myList, mod);
    }
    
    /**
     * Create a plot
     *
     * @param myList list of plots to be created
     */
    public void createPlots(final ArrayList<Plot> myList) {
        final StmtMod<Plot> mod = new StmtMod<Plot>() {
            @Override
            public String getCreateMySQL(int size) {
                return getCreateMySQL(size, CREATE_PLOTS, 4);
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
                stmt.setInt((i * 4) + 1, plot.id.x);
                stmt.setInt((i * 4) + 2, plot.id.y);
                try {
                    stmt.setString((i * 4) + 3, plot.owner.toString());
                } catch (final Exception e) {
                    stmt.setString((i * 4) + 3, DBFunc.everyone.toString());
                }
                stmt.setString((i * 4) + 4, plot.world);
            }

            @Override
            public void setSQLite(PreparedStatement stmt, int i, Plot plot) throws SQLException {
                stmt.setNull((i * 6) + 1, 4);
                stmt.setInt((i * 6) + 2, plot.id.x);
                stmt.setInt((i * 6) + 3, plot.id.y);
                try {
                    stmt.setString((i * 6) + 4, plot.owner.toString());
                } catch (final Exception e1) {
                    stmt.setString((i * 6) + 4, DBFunc.everyone.toString());
                }
                stmt.setString((i * 6) + 5, plot.world);
                stmt.setTimestamp((i * 6) + 6, new Timestamp(System.currentTimeMillis()));
            }

            @Override
            public void setSQL(PreparedStatement stmt, Plot plot) throws SQLException {
                stmt.setInt(1, plot.id.x);
                stmt.setInt(2, plot.id.y);
                stmt.setString(3, plot.owner.toString());
                stmt.setString(4, plot.world);
            }
        };
        setBulk(myList, mod);
    }

    public <T> void setBulk(ArrayList<T> objList, StmtMod<T> mod) {
        final int size = objList.size();
        if (size == 0) {
            return;
        }
        int packet;
        if (Settings.DB.USE_MYSQL) {
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
            PS.log("&aBatch 1: " + count + " | " + objList.get(0).getClass().getCanonicalName());
            preparedStmt.executeBatch();
            preparedStmt.clearParameters();
            preparedStmt.close();
            return;
        } catch (Exception e) {
            if (Settings.DB.USE_MYSQL) {
                e.printStackTrace();
                PS.log("&cERROR 1: " + " | " + objList.get(0).getClass().getCanonicalName());
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
                    return;
                }
                if (last == -1) {
                    last = subList.size();
                    statement = mod.getCreateSQLite(subList.size());
                    preparedStmt = this.connection.prepareStatement(statement.toString());
                }
                if (subList.size() != last || (count % 5000 == 0 && count > 0)) {
                    preparedStmt.executeBatch();
                    preparedStmt.close();

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
            PS.log("&aBatch 2: " + count + " | " + objList.get(0).getClass().getCanonicalName());
            preparedStmt.executeBatch();
            preparedStmt.clearParameters();
            preparedStmt.close();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            PS.log("&cERROR 2: " + " | " + objList.get(0).getClass().getCanonicalName());
        }

        PS.log("&6[WARN] " + "Could not bulk save!");
        try {
            PreparedStatement preparedStmt = null;
            String nonBulk = mod.getCreateSQL();
            preparedStmt = this.connection.prepareStatement(nonBulk.toString());
            for (final T obj : objList) {
                try {
                    mod.setSQL(preparedStmt, obj);
                    preparedStmt.addBatch();
                } catch (final Exception e3) {
                    PS.log("&c[ERROR] " + "Failed to save " + obj + "!");
                }
            }
            PS.log("&aBatch 3");
            preparedStmt.executeBatch();
            preparedStmt.close();
        } catch (Exception e3) {
            e3.printStackTrace();
            PS.log("&c[ERROR] " + "Failed to save all!");
        }
    }

    public void createSettings(final ArrayList<SettingsPair> myList) {
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
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                setBulk(myList, mod);
            }
        });
    }

    public void createEmptySettings(final ArrayList<Integer> myList) {
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
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                setBulk(myList, mod);
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
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                PreparedStatement stmt = null;
                try {
                    stmt = SQLManager.this.connection.prepareStatement(SQLManager.this.CREATE_PLOT);
                    stmt.setInt(1, plot.id.x);
                    stmt.setInt(2, plot.id.y);
                    stmt.setString(3, plot.owner.toString());
                    stmt.setString(4, plot.world);
                    stmt.executeUpdate();
                    stmt.close();
                } catch (final Exception e) {
                    e.printStackTrace();
                    PS.log("&c[ERROR] " + "Failed to save plot " + plot.id);
                }
            }
        });
    }

    @Override
    public void createPlotAndSettings(final Plot plot) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                PreparedStatement stmt = null;
                try {
                    stmt = SQLManager.this.connection.prepareStatement(SQLManager.this.CREATE_PLOT);
                    stmt.setInt(1, plot.id.x);
                    stmt.setInt(2, plot.id.y);
                    stmt.setString(3, plot.owner.toString());
                    stmt.setString(4, plot.world);
                    stmt.executeUpdate();
                    stmt.close();
                    final int id = getId(plot.world, plot.id);
                    stmt = SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_settings`(`plot_plot_id`) VALUES(" + "?)");
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    stmt.close();
                } catch (final Exception e) {
                    e.printStackTrace();
                    PS.log("&c[ERROR] " + "Failed to save plot " + plot.id);
                }
            }
        });
    }

    /**
     * Create tables
     *
     * @throws SQLException
     */
    @Override
    public void createTables(final String database) throws SQLException {
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
        PS.log("Creating tables");
        final boolean mysql = database.equals("mysql");
        final Statement stmt = this.connection.createStatement();
        if (mysql) {
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
    public void delete(final String world, final Plot plot) {
        PS.get().removePlot(world, plot.id, false);
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                PreparedStatement stmt = null;
                final int id = getId(world, plot.id);
                try {
                    stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_settings` WHERE `plot_plot_id` = ?");
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    stmt.close();
                    stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_helpers` WHERE `plot_plot_id` = ?");
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    stmt.close();
                    stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_trusted` WHERE `plot_plot_id` = ?");
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    stmt.close();
                    stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_denied` WHERE `plot_plot_id` = ?");
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    stmt.close();
                    stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_comments` WHERE `world` = ? AND `hashcode` = ?");
                    stmt.setString(1, world);
                    stmt.setInt(2, plot.hashCode());
                    stmt.executeUpdate();
                    stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot` WHERE `id` = ?");
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    stmt.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                    PS.log("&c[ERROR] " + "Failed to delete plot " + plot.id);
                }
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
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                PreparedStatement stmt = null;
                try {
                    stmt = SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_settings`(`plot_plot_id`) VALUES(" + "?)");
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    stmt.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getId(final String world, final PlotId id2) {
        PreparedStatement stmt = null;
        try {
            stmt = this.connection.prepareStatement("SELECT `id` FROM `" + this.prefix + "plot` WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND world = ? ORDER BY `timestamp` ASC");
            stmt.setInt(1, id2.x);
            stmt.setInt(2, id2.y);
            stmt.setString(3, world);
            final ResultSet r = stmt.executeQuery();
            int id = Integer.MAX_VALUE;
            while (r.next()) {
                id = r.getInt("id");
            }
            r.close();
            stmt.close();
            return id;
        } catch (final SQLException e) {
            e.printStackTrace();
        }
        return Integer.MAX_VALUE;
    }

    public void updateTables() {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load all plots, helpers, denied, trusted, and every setting from DB into a hashmap
     */
    @Override
    public LinkedHashMap<String, HashMap<PlotId, Plot>> getPlots() {
        final LinkedHashMap<String, HashMap<PlotId, Plot>> newplots = new LinkedHashMap<>();
        final HashMap<Integer, Plot> plots = new HashMap<>();
        Statement stmt = null;
        try {
            Set<String> worlds = new HashSet<>();
            if (PS.get().config.contains("worlds")) {
                worlds = PS.get().config.getConfigurationSection("worlds").getKeys(false);
            }
            final HashMap<String, UUID> uuids = new HashMap<String, UUID>();
            final HashMap<String, Integer> noExist = new HashMap<String, Integer>();
            /*
             * Getting plots
             */
            stmt = this.connection.createStatement();
            ResultSet r = stmt.executeQuery("SELECT `id`, `plot_id_x`, `plot_id_z`, `owner`, `world` FROM `" + this.prefix + "plot`");
            PlotId plot_id;
            int id;
            Plot p;
            String o;
            UUID user;
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
                p = new Plot(plot_id, user, new ArrayList<UUID>(), new ArrayList<UUID>(), new ArrayList<UUID>(), "", null, null, worldname, new boolean[]{false, false, false, false});
                plots.put(id, p);
            }

            if (Settings.CACHE_RATINGS) {
                r = stmt.executeQuery("SELECT `plot_plot_id`, `player`, `rating` FROM `" + this.prefix + "plot_rating`");
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
                        if (plot.settings.ratings == null) {
                            plot.settings.ratings = new HashMap<UUID, Integer>();
                        }
                        plot.settings.ratings.put(user, r.getInt("rating"));
                    } else {
                        PS.log("&cPLOT " + id + " in plot_helpers does not exist. Please create the plot or remove this entry.");
                    }
                }
            }

            /*
             * Getting helpers
             */
            r = stmt.executeQuery("SELECT `user_uuid`, `plot_plot_id` FROM `" + this.prefix + "plot_helpers`");
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
                    plot.addTrusted(user);
                } else {
                    PS.log("&cPLOT " + id + " in plot_helpers does not exist. Please create the plot or remove this entry.");
                }
            }
            /*
             * Getting trusted
             */
            r = stmt.executeQuery("SELECT `user_uuid`, `plot_plot_id` FROM `" + this.prefix + "plot_trusted`");
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
                    plot.addMember(user);
                } else {
                    PS.log("&cPLOT " + id + " in plot_trusted does not exist. Please create the plot or remove this entry.");
                }
            }
            /*
             * Getting denied
             */
            r = stmt.executeQuery("SELECT `user_uuid`, `plot_plot_id` FROM `" + this.prefix + "plot_denied`");
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
                    plot.addDenied(user);
                } else {
                    PS.log("&cPLOT " + id + " in plot_denied does not exist. Please create the plot or remove this entry.");
                }
            }
            r = stmt.executeQuery("SELECT * FROM `" + this.prefix + "plot_settings`");
            while (r.next()) {
                id = r.getInt("plot_plot_id");
                final Plot plot = plots.get(id);
                if (plot != null) {
                    plots.remove(id);
                    if (!newplots.containsKey(plot.world)) {
                        newplots.put(plot.world, new HashMap<PlotId, Plot>());
                    }
                    newplots.get(plot.world).put(plot.id, plot);
                    final String b = r.getString("biome");
                    if (b != null) {
                        for (final Biome mybiome : Biome.values()) {
                            if (mybiome.toString().equalsIgnoreCase(b)) {
                                break;
                            }
                        }
                    }
                    final String alias = r.getString("alias");
                    if (alias != null) {
                        plot.settings.setAlias(alias);
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
                                plot.settings.setPosition(BlockLoc.fromString(pos));
                            } catch (final Exception e) {
                            }
                    }
                    final Integer m = r.getInt("merged");
                    if (m != null) {
                        final boolean[] merged = new boolean[4];
                        for (int i = 0; i < 4; i++) {
                            merged[3 - i] = ((m) & (1 << i)) != 0;
                        }
                        plot.settings.setMerged(merged);
                    } else {
                        plot.settings.setMerged(new boolean[]{false, false, false, false});
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
                            if (StringUtils.isAlpha(element.replaceAll("_", "").replaceAll("-", ""))) {
                                Flag flag = new Flag(FlagManager.getFlag(element, true), "");
                                flags.put(flag.getKey(), flag);
                            } else {
                                PS.log("INVALID FLAG: " + element);
                            }
                        }
                    }
                    if (exception) {
                        PS.log("&cPlot " + id + " had an invalid flag. A fix has been attempted.");
                        PS.log("&c" + myflags);
                        setFlags(id, flags.values());
                    }
                    plot.settings.flags = flags;
                } else {
                    PS.log("&cPLOT " + id + " in plot_settings does not exist. Please create the plot or remove this entry.");
                }
            }
            stmt.close();
            r.close();
            if (plots.keySet().size() > 0) {
                createEmptySettings(new ArrayList<Integer>(plots.keySet()));
            }
            boolean invalidPlot = false;
            for (final String worldname : noExist.keySet()) {
                invalidPlot = true;
                PS.log("&c[WARNING] Found " + noExist.get(worldname) + " plots in DB for non existant world; '" + worldname + "'.");
            }
            if (invalidPlot) {
                PS.log("&c[WARNING] - Please create the world/s or remove the plots using the purge command");
            }
        } catch (final SQLException e) {
            PS.log("&7[WARN] " + "Failed to load plots.");
            e.printStackTrace();
        }
        return newplots;
    }

    @Override
    public void setMerged(final String world, final Plot plot, final boolean[] merged) {
        plot.settings.setMerged(merged);
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    int n = 0;
                    for (int i = 0; i < 4; ++i) {
                        n = (n << 1) + (merged[i] ? 1 : 0);
                    }
                    final PreparedStatement stmt = SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot_settings` SET `merged` = ? WHERE `plot_plot_id` = ?");
                    stmt.setInt(1, n);
                    stmt.setInt(2, getId(world, plot.id));
                    stmt.execute();
                    stmt.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                    PS.log("&7[WARN] " + "Could not set merged for plot " + plot.id);
                }
            }
        });
    }

    @Override
    public void swapPlots(final Plot p1, final Plot p2) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                /*
                 * We don't need to actually swap all the rows
                 *  - Just switch the plot_id_x and plot_id_z
                 *  - The other tables reference the `id` so it will cascade
                 */
                try {
                    final String world = p1.world;
                    final int id1 = getId(world, p1.id);
                    final int id2 = getId(world, p2.id);
                    final PlotId pos1 = p1.getId();
                    final PlotId pos2 = p2.getId();
                    PreparedStatement stmt = SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot` SET `plot_id_x` = ?, `plot_id_z` = ? WHERE `id` = ?");
                    stmt.setInt(1, pos2.x);
                    stmt.setInt(2, pos2.y);
                    stmt.setInt(3, id1);
                    stmt.executeUpdate();
                    stmt.close();
                    stmt = SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot` SET `plot_id_x` = ?, `plot_id_z` = ? WHERE `id` = ?");
                    stmt.setInt(1, pos1.x);
                    stmt.setInt(2, pos1.y);
                    stmt.setInt(3, id2);
                    stmt.executeUpdate();
                    stmt.close();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void movePlot(final Plot original, final Plot newPlot) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final int id = getId(original.world, original.id);
                    final PreparedStatement stmt = SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot` SET `plot_id_x` = ?, `plot_id_z` = ? WHERE `id` = ?");
                    stmt.setInt(1, newPlot.id.x);
                    stmt.setInt(2, newPlot.id.y);
                    stmt.setInt(3, id);
                    stmt.executeUpdate();
                    stmt.close();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void setFlags(final String world, final Plot plot, final Collection<Flag> flags) {
        final StringBuilder flag_string = new StringBuilder();
        int i = 0;
        for (final Flag flag : flags) {
            if (i != 0) {
                flag_string.append(",");
            }
            flag_string.append(flag.getKey() + ":" + flag.getValueString().replaceAll(":", "\u00AF").replaceAll(",", "\u00B4"));
            i++;
        }
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement stmt = SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot_settings` SET `flags` = ? WHERE `plot_plot_id` = ?");
                    stmt.setString(1, flag_string.toString());
                    stmt.setInt(2, getId(world, plot.id));
                    stmt.execute();
                    stmt.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                    PS.log("&7[WARN] " + "Could not set flag for plot " + plot.id);
                }
            }
        });
    }

    public void setFlags(final int id, final Collection<Flag> newflags) {
        final String flag_string = StringUtils.join(newflags, ",");
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement stmt = SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot_settings` SET `flags` = ? WHERE `plot_plot_id` = ?");
                    stmt.setString(1, flag_string);
                    stmt.setInt(2, id);
                    stmt.execute();
                    stmt.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                    PS.log("&7[WARN] " + "Could not set flag for plot " + id);
                }
            }
        });
    }

    @Override
    public void setAlias(final String world, final Plot plot, final String alias) {
        plot.settings.setAlias(alias);
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                PreparedStatement stmt = null;
                try {
                    stmt = SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot_settings` SET `alias` = ?  WHERE `plot_plot_id` = ?");
                    stmt.setString(1, alias);
                    stmt.setInt(2, getId(world, plot.id));
                    stmt.executeUpdate();
                    stmt.close();
                } catch (final SQLException e) {
                    PS.log("&7[WARN] " + "Failed to set alias for plot " + plot.id);
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Purge all plots with the following database IDs
     */
    @Override
    public void purgeIds(final String world, final Set<Integer> uniqueIds) {
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
                PreparedStatement stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + this.prefix + "plot_helpers` WHERE `plot_plot_id` = " + idstr + "");
                stmt.executeUpdate();
                stmt.close();
                stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + this.prefix + "plot_denied` WHERE `plot_plot_id` = " + idstr + "");
                stmt.executeUpdate();
                stmt.close();
                stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + this.prefix + "plot_settings` WHERE `plot_plot_id` = " + idstr + "");
                stmt.executeUpdate();
                stmt.close();
                stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + this.prefix + "plot_trusted` WHERE `plot_plot_id` = " + idstr + "");
                stmt.executeUpdate();
                stmt.close();
                stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + this.prefix + "plot` WHERE `id` = " + idstr2 + "");
                stmt.executeUpdate();
                stmt.close();
            } catch (final SQLException e) {
                e.printStackTrace();
                PS.log("&c[ERROR] " + "FAILED TO PURGE WORLD '" + world + "'!");
                return;
            }
        }
        PS.log("&6[INFO] " + "SUCCESSFULLY PURGED WORLD '" + world + "'!");
    }

    @Override
    public void purge(final String world, final Set<PlotId> plots) {
        PreparedStatement stmt;
        try {
            stmt = SQLManager.this.connection.prepareStatement("SELECT `id`, `plot_id_x`, `plot_id_z` FROM `" + this.prefix + "plot` WHERE `world` = ?");
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
            PS.log("&c[ERROR] " + "FAILED TO PURGE WORLD '" + world + "'!");
        }
    }

    @Override
    public void setPosition(final String world, final Plot plot, final String position) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                PreparedStatement stmt = null;
                try {
                    stmt = SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot_settings` SET `position` = ?  WHERE `plot_plot_id` = ?");
                    stmt.setString(1, position);
                    stmt.setInt(2, getId(world, plot.id));
                    stmt.executeUpdate();
                    stmt.close();
                } catch (final SQLException e) {
                    PS.log("&7[WARN] " + "Failed to set position for plot " + plot.id);
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public HashMap<String, Object> getSettings(final int id) {
        final HashMap<String, Object> h = new HashMap<String, Object>();
        PreparedStatement stmt = null;
        try {
            stmt = this.connection.prepareStatement("SELECT * FROM `" + this.prefix + "plot_settings` WHERE `plot_plot_id` = ?");
            stmt.setInt(1, id);
            final ResultSet r = stmt.executeQuery();
            String var;
            Object val;
            while (r.next()) {
                var = "biome";
                val = r.getObject(var);
                h.put(var, val);
                var = "rain";
                val = r.getObject(var);
                h.put(var, val);
                var = "custom_time";
                val = r.getObject(var);
                h.put(var, val);
                var = "time";
                val = r.getObject(var);
                h.put(var, val);
                var = "deny_entry";
                val = r.getObject(var);
                h.put(var, (short) 0);
                var = "alias";
                val = r.getObject(var);
                h.put(var, val);
                var = "position";
                val = r.getObject(var);
                h.put(var, val);
                var = "flags";
                val = r.getObject(var);
                h.put(var, val);
                var = "merged";
                val = r.getObject(var);
                h.put(var, val);
            }
            stmt.close();
            r.close();
        } catch (final SQLException e) {
            PS.log("&7[WARN] " + "Failed to load settings for plot: " + id);
            e.printStackTrace();
        }
        return h;
    }

    @Override
    public void removeComment(final String world, final Plot plot, final PlotComment comment) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement statement;
                    if (plot != null) {
                        statement = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_comments` WHERE `world` = ? AND `hashcode` = ? AND `comment` = ? AND `inbox` = ? AND `sender` = ?");
                        statement.setString(1, plot.world);
                        statement.setInt(2, plot.id.hashCode());
                        statement.setString(3, comment.comment);
                        statement.setString(4, comment.inbox);
                        statement.setString(5, comment.senderName);
                    } else {
                        statement = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_comments` WHERE `comment` = ? AND `inbox` = ? AND `sender` = ?");
                        statement.setString(1, comment.comment);
                        statement.setString(2, comment.inbox);
                        statement.setString(3, comment.senderName);
                    }
                    statement.executeUpdate();
                    statement.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                    PS.log("&7[WARN] " + "Failed to remove comment for plot " + plot.id);
                }
            }
        });
    }

    @Override
    public void clearInbox(final Plot plot, final String inbox) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    PreparedStatement statement;
                    if (plot != null) {
                        statement = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_comments` WHERE `world` = ? AND `hashcode` = ? AND `inbox` = ?");
                        statement.setString(1, plot.world);
                        statement.setInt(2, plot.id.hashCode());
                        statement.setString(3, inbox);
                    } else {
                        statement = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_comments` `inbox` = ?");
                        statement.setString(1, inbox);
                    }
                    statement.executeUpdate();
                    statement.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                    PS.log("&7[WARN] " + "Failed to remove comment for plot " + plot.id);
                }
            }
        });
    }

    @Override
    public void getComments(final String world, final Plot plot, final String inbox, final RunnableVal whenDone) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                final ArrayList<PlotComment> comments = new ArrayList<PlotComment>();
                try {
                    final PreparedStatement statement;
                    if (plot != null) {
                        statement = connection.prepareStatement("SELECT * FROM `" + prefix + "plot_comments` WHERE `world` = ? AND `hashcode` = ? AND `inbox` = ?");
                        statement.setString(1, plot.world);
                        statement.setInt(2, plot.id.hashCode());
                        statement.setString(3, inbox);
                    } else {
                        statement = connection.prepareStatement("SELECT * FROM `" + prefix + "plot_comments` WHERE `inbox` = ?");
                        statement.setString(1, inbox);
                    }
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
                    statement.close();
                    set.close();
                } catch (final SQLException e) {
                    PS.log("&7[WARN] " + "Failed to fetch comment");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void setComment(final String world, final Plot plot, final PlotComment comment) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement statement = SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_comments` (`world`, `hashcode`, `comment`, `inbox`, `timestamp`, `sender`) VALUES(?,?,?,?,?,?)");
                    statement.setString(1, plot.world);
                    statement.setInt(2, plot.id.hashCode());
                    statement.setString(3, comment.comment);
                    statement.setString(4, comment.inbox);
                    statement.setInt(5, (int) (comment.timestamp / 1000));
                    statement.setString(6, comment.senderName);
                    statement.executeUpdate();
                    statement.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                    PS.log("&7[WARN] " + "Failed to set comment for plot " + plot.id);
                }
            }
        });
    }

    @Override
    public void removeTrusted(final String world, final Plot plot, final UUID uuid) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement statement = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_helpers` WHERE `plot_plot_id` = ? AND `user_uuid` = ?");
                    statement.setInt(1, getId(world, plot.id));
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                    statement.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                    PS.log("&7[WARN] " + "Failed to remove helper for plot " + plot.id);
                }
            }
        });
    }

    @Override
    public void removeMember(final String world, final Plot plot, final UUID uuid) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement statement = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_trusted` WHERE `plot_plot_id` = ? AND `user_uuid` = ?");
                    statement.setInt(1, getId(world, plot.id));
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                    statement.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                    PS.log("&7[WARN] " + "Failed to remove trusted user for plot " + plot.id);
                }
            }
        });
    }

    @Override
    public void setTrusted(final String world, final Plot plot, final UUID uuid) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement statement = SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_helpers` (`plot_plot_id`, `user_uuid`) VALUES(?,?)");
                    statement.setInt(1, getId(world, plot.id));
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                    statement.close();
                } catch (final SQLException e) {
                    PS.log("&7[WARN] " + "Failed to set helper for plot " + plot.id);
                    e.printStackTrace();
                }
            }
        });
    }

    public void setHelper(final int id, final UUID uuid) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement statement = SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_helpers` (`plot_plot_id`, `user_uuid`) VALUES(?,?)");
                    statement.setInt(1, id);
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                    statement.close();
                } catch (final SQLException e) {
                    PS.log("&7[WARN] " + "Failed to set helper for id " + id);
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void setMember(final String world, final Plot plot, final UUID uuid) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement statement = SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_trusted` (`plot_plot_id`, `user_uuid`) VALUES(?,?)");
                    statement.setInt(1, getId(world, plot.id));
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                    statement.close();
                } catch (final SQLException e) {
                    PS.log("&7[WARN] " + "Failed to set plot trusted for plot " + plot.id);
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void removeDenied(final String world, final Plot plot, final UUID uuid) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement statement = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_denied` WHERE `plot_plot_id` = ? AND `user_uuid` = ?");
                    statement.setInt(1, getId(world, plot.id));
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                    statement.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                    PS.log("&7[WARN] " + "Failed to remove denied for plot " + plot.id);
                }
            }
        });
    }

    @Override
    public void setDenied(final String world, final Plot plot, final UUID uuid) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement statement = SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_denied` (`plot_plot_id`, `user_uuid`) VALUES(?,?)");
                    statement.setInt(1, getId(world, plot.id));
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                    statement.close();
                } catch (final SQLException e) {
                    PS.log("&7[WARN] " + "Failed to set denied for plot " + plot.id);
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public HashMap<UUID, Integer> getRatings(final Plot plot) {
        HashMap<UUID, Integer> map = new HashMap<UUID, Integer>();
        try {
            final PreparedStatement statement = this.connection.prepareStatement("SELECT `rating`, `player` FROM `" + this.prefix + "plot_rating` WHERE `plot_plot_id` = ? ");
            statement.setInt(1, getId(plot.world, plot.id));
            final ResultSet set = statement.executeQuery();
            while (set.next()) {
                UUID uuid = UUID.fromString(set.getString("player"));
                int rating = set.getInt("rating");
                map.put(uuid, rating);
            }
            statement.close();
            set.close();
        } catch (final SQLException e) {
            PS.log("&7[WARN] " + "Failed to fetch rating for plot " + plot.getId().toString());
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public void setRating(final Plot plot, final UUID rater, final int value) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement statement = SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_rating` (`plot_plot_id`, `rating`, `player`) VALUES(?,?,?)");
                    statement.setInt(1, getId(plot.world, plot.id));
                    statement.setInt(2, value);
                    statement.setString(3, rater.toString());
                    statement.executeUpdate();
                    statement.close();
                } catch (final SQLException e) {
                    PS.log("&7[WARN] " + "Failed to set denied for plot " + plot.id);
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void delete(final PlotCluster cluster) {
        ClusterManager.removeCluster(cluster);
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                PreparedStatement stmt = null;
                final int id = getClusterId(cluster.world, ClusterManager.getClusterId(cluster));
                try {
                    stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "cluster_settings` WHERE `cluster_id` = ?");
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    stmt.close();
                    stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "cluster_helpers` WHERE `cluster_id` = ?");
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "cluster_invited` WHERE `cluster_id` = ?");
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    stmt = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "cluster` WHERE `id` = ?");
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    stmt.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                    PS.log("&c[ERROR] " + "Failed to delete plot cluster: " + cluster.getP1() + ":" + cluster.getP2());
                }
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
                    PS.log("&cCluster " + id + " in cluster_helpers does not exist. Please create the cluster or remove this entry.");
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
                    PS.log("&cCluster " + id + " in cluster_invited does not exist. Please create the cluster or remove this entry.");
                }
            }
            r = stmt.executeQuery("SELECT * FROM `" + this.prefix + "cluster_settings`");
            while (r.next()) {
                id = r.getInt("cluster_id");
                cluster = clusters.get(id);
                if (cluster != null) {
                    final String b = r.getString("biome");
                    if (b != null) {
                        for (final Biome mybiome : Biome.values()) {
                            if (mybiome.toString().equalsIgnoreCase(b)) {
                                break;
                            }
                        }
                    }
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
                        PS.log("&cCluster " + id + " had an invalid flag. A fix has been attempted.");
                        PS.log("&c" + myflags);
                    }
                    cluster.settings.flags = flags;
                } else {
                    PS.log("&cCluster " + id + " in cluster_settings does not exist. Please create the cluster or remove this entry.");
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
            for (final String w : noExist.keySet()) {
                invalidPlot = true;
                PS.log("&c[WARNING] Found " + noExist.get(w) + " clusters in DB for non existant world; '" + w + "'.");
            }
            if (invalidPlot) {
                PS.log("&c[WARNING] - Please create the world/s or remove the clusters using the purge command");
            }
        } catch (final SQLException e) {
            PS.log("&7[WARN] " + "Failed to load clusters.");
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
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement stmt = SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "cluster_settings` SET `flags` = ? WHERE `cluster_id` = ?");
                    stmt.setString(1, flag_string.toString());
                    stmt.setInt(2, getClusterId(cluster.world, ClusterManager.getClusterId(cluster)));
                    stmt.execute();
                    stmt.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                    PS.log("&7[WARN] " + "Could not set flag for plot " + cluster);
                }
            }
        });
    }

    @Override
    public void setClusterName(final PlotCluster cluster, final String name) {
        cluster.settings.setAlias(name);
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                PreparedStatement stmt = null;
                try {
                    stmt = SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "cluster_settings` SET `alias` = ?  WHERE `cluster_id` = ?");
                    stmt.setString(1, name);
                    stmt.setInt(2, getClusterId(cluster.world, ClusterManager.getClusterId(cluster)));
                    stmt.executeUpdate();
                    stmt.close();
                } catch (final SQLException e) {
                    PS.log("&7[WARN] " + "Failed to set alias for cluster " + cluster);
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void removeHelper(final PlotCluster cluster, final UUID uuid) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement statement = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "cluster_helpers` WHERE `cluster_id` = ? AND `user_uuid` = ?");
                    statement.setInt(1, getClusterId(cluster.world, ClusterManager.getClusterId(cluster)));
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                    statement.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                    PS.log("&7[WARN] " + "Failed to remove helper for cluster " + cluster);
                }
            }
        });
    }

    @Override
    public void setHelper(final PlotCluster cluster, final UUID uuid) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement statement = SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "cluster_helpers` (`cluster_id`, `user_uuid`) VALUES(?,?)");
                    statement.setInt(1, getClusterId(cluster.world, ClusterManager.getClusterId(cluster)));
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                    statement.close();
                } catch (final SQLException e) {
                    PS.log("&7[WARN] " + "Failed to set helper for cluster " + cluster);
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void createCluster(final PlotCluster cluster) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                PreparedStatement stmt = null;
                try {
                    stmt = SQLManager.this.connection.prepareStatement(SQLManager.this.CREATE_CLUSTER);
                    stmt.setInt(1, cluster.getP1().x);
                    stmt.setInt(2, cluster.getP1().y);
                    stmt.setInt(3, cluster.getP2().x);
                    stmt.setInt(4, cluster.getP2().y);
                    stmt.setString(5, cluster.owner.toString());
                    stmt.setString(6, cluster.world);
                    stmt.executeUpdate();
                    stmt.close();
                    final int id = getClusterId(cluster.world, ClusterManager.getClusterId(cluster));
                    stmt = SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "cluster_settings`(`cluster_id`, `alias`) VALUES(?, ?" + ")");
                    stmt.setInt(1, id);
                    stmt.setString(2, cluster.settings.getAlias());
                    stmt.executeUpdate();
                    stmt.close();
                } catch (final Exception e) {
                    e.printStackTrace();
                    PS.log("&c[ERROR] " + "Failed to save cluster " + cluster);
                }
            }
        });
    }

    @Override
    public void resizeCluster(final PlotCluster current, final PlotClusterId resize) {
        final PlotId pos1 = new PlotId(current.getP1().x, current.getP1().y);
        final PlotId pos2 = new PlotId(current.getP2().x, current.getP2().y);
        current.setP1(resize.pos1);
        current.setP2(resize.pos2);
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                PreparedStatement stmt = null;
                try {
                    stmt = SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "cluster` SET `pos1_x` = ?, `pos1_z` = ?, `pos2_x` = ?, `pos2_z` = ?  WHERE `id` = ?");
                    stmt.setInt(1, pos1.x);
                    stmt.setInt(2, pos1.y);
                    stmt.setInt(3, pos2.x);
                    stmt.setInt(4, pos2.y);
                    stmt.setInt(5, getClusterId(current.world, ClusterManager.getClusterId(current)));
                    stmt.executeUpdate();
                    stmt.close();
                } catch (final SQLException e) {
                    PS.log("&7[WARN] " + "Failed to rezize cluster " + current);
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void setPosition(final PlotCluster cluster, final String position) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                PreparedStatement stmt = null;
                try {
                    stmt = SQLManager.this.connection.prepareStatement("UPDATE `" + SQLManager.this.prefix + "cluster_settings` SET `position` = ?  WHERE `cluster_id` = ?");
                    stmt.setString(1, position);
                    stmt.setInt(2, getClusterId(cluster.world, ClusterManager.getClusterId(cluster)));
                    stmt.executeUpdate();
                    stmt.close();
                } catch (final SQLException e) {
                    PS.log("&7[WARN] " + "Failed to set position for cluster " + cluster);
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public HashMap<String, Object> getClusterSettings(final int id) {
        final HashMap<String, Object> h = new HashMap<String, Object>();
        PreparedStatement stmt = null;
        try {
            stmt = this.connection.prepareStatement("SELECT * FROM `" + this.prefix + "cluster_settings` WHERE `cluster_id` = ?");
            stmt.setInt(1, id);
            final ResultSet r = stmt.executeQuery();
            String var;
            Object val;
            while (r.next()) {
                var = "biome";
                val = r.getObject(var);
                h.put(var, val);
                var = "rain";
                val = r.getObject(var);
                h.put(var, val);
                var = "custom_time";
                val = r.getObject(var);
                h.put(var, val);
                var = "time";
                val = r.getObject(var);
                h.put(var, val);
                var = "deny_entry";
                val = r.getObject(var);
                h.put(var, (short) 0);
                var = "alias";
                val = r.getObject(var);
                h.put(var, val);
                var = "position";
                val = r.getObject(var);
                h.put(var, val);
                var = "flags";
                val = r.getObject(var);
                h.put(var, val);
                var = "merged";
                val = r.getObject(var);
                h.put(var, val);
            }
            stmt.close();
            r.close();
        } catch (final SQLException e) {
            PS.log("&7[WARN] " + "Failed to load settings for cluster: " + id);
            e.printStackTrace();
        }
        return h;
    }

    @Override
    public void removeInvited(final PlotCluster cluster, final UUID uuid) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement statement = SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "cluster_invited` WHERE `cluster_id` = ? AND `user_uuid` = ?");
                    statement.setInt(1, getClusterId(cluster.world, ClusterManager.getClusterId(cluster)));
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                    statement.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                    PS.log("&7[WARN] " + "Failed to remove invited for cluster " + cluster);
                }
            }
        });
    }

    @Override
    public void setInvited(final String world, final PlotCluster cluster, final UUID uuid) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final PreparedStatement statement = SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "cluster_invited` (`cluster_id`, `user_uuid`) VALUES(?,?)");
                    statement.setInt(1, getClusterId(cluster.world, ClusterManager.getClusterId(cluster)));
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                    statement.close();
                } catch (final SQLException e) {
                    PS.log("&7[WARN] " + "Failed to set helper for cluster " + cluster);
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean deleteTables() {
        try {
            SQLManager.this.connection.close();
            SQLManager.this.connection = PS.get().getDatabase().forceConnection();
            final Statement stmt = this.connection.createStatement();
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
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
}
