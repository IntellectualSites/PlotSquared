package com.intellectualcrafters.plot.database;

import com.google.common.base.Charsets;
import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.config.Storage;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.flag.StringFlag;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotSettings;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.comment.PlotComment;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


@SuppressWarnings("SqlDialectInspection")
public class SQLManager implements AbstractDB {

    // Public final
    public final String SET_OWNER;
    public final String GET_ALL_PLOTS;
    public final String CREATE_PLOTS;
    public final String CREATE_SETTINGS;
    public final String CREATE_TIERS;
    public final String CREATE_PLOT;
    public final String CREATE_PLOT_SAFE;
    public final String CREATE_CLUSTER;
    private final String prefix;
    // Private Final
    private final Database database;
    private final boolean mySQL;
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
     * player_meta
     */
    public volatile ConcurrentHashMap<UUID, Queue<UniqueStatement>> playerTasks;
    /**
     * cluster
     * cluster_helpers
     * cluster_invited
     * cluster_settings
     */
    public volatile ConcurrentHashMap<PlotCluster, Queue<UniqueStatement>> clusterTasks;
    // Private
    private Connection connection;
    private boolean closed = false;

    /**
     * Constructor
     *
     * @param database
     * @param p prefix
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public SQLManager(final Database database, String p, boolean debug) throws SQLException, ClassNotFoundException {
        // Private final
        this.database = database;
        this.connection = database.openConnection();
        this.mySQL = database instanceof MySQL;
        this.globalTasks = new ConcurrentLinkedQueue<>();
        this.notifyTasks = new ConcurrentLinkedQueue<>();
        this.plotTasks = new ConcurrentHashMap<>();
        this.playerTasks = new ConcurrentHashMap<>();
        this.clusterTasks = new ConcurrentHashMap<>();
        this.prefix = p;
        this.SET_OWNER = "UPDATE `" + this.prefix + "plot` SET `owner` = ? WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND `world` = ?";
        this.GET_ALL_PLOTS = "SELECT `id`, `plot_id_x`, `plot_id_z`, `world` FROM `" + this.prefix + "plot`";
        this.CREATE_PLOTS = "INSERT INTO `" + this.prefix + "plot`(`plot_id_x`, `plot_id_z`, `owner`, `world`, `timestamp`) values ";
        this.CREATE_SETTINGS = "INSERT INTO `" + this.prefix + "plot_settings` (`plot_plot_id`) values ";
        this.CREATE_TIERS = "INSERT INTO `" + this.prefix + "plot_%tier%` (`plot_plot_id`, `user_uuid`) values ";
        this.CREATE_PLOT = "INSERT INTO `" + this.prefix + "plot`(`plot_id_x`, `plot_id_z`, `owner`, `world`, `timestamp`) VALUES(?, ?, ?, ?, ?)";

        if (mySQL) {
            this.CREATE_PLOT_SAFE = "INSERT IGNORE INTO `" + this.prefix + "plot`(`plot_id_x`, `plot_id_z`, `owner`, `world`, `timestamp`) SELECT ?, ?, ?, ?, ? FROM DUAL WHERE NOT EXISTS (SELECT null FROM `" + this.prefix + "plot` WHERE `world` = ? AND `plot_id_x` = ? AND `plot_id_z` = ?)";
        } else {
            this.CREATE_PLOT_SAFE = "INSERT INTO `" + this.prefix + "plot`(`plot_id_x`, `plot_id_z`, `owner`, `world`, `timestamp`) SELECT ?, ?, ?, ?, ? WHERE NOT EXISTS (SELECT null FROM `" + this.prefix + "plot` WHERE `world` = ? AND `plot_id_x` = ? AND `plot_id_z` = ?)";
        }
        this.CREATE_CLUSTER =
                "INSERT INTO `" + this.prefix + "cluster`(`pos1_x`, `pos1_z`, `pos2_x`, `pos2_z`, `owner`, `world`) VALUES(?, ?, ?, ?, ?, ?)";
        try {
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                long last = System.currentTimeMillis();
                while (true) {
                    if (SQLManager.this.closed) {
                        break;
                    }
                    boolean hasTask = !globalTasks.isEmpty() || !playerTasks.isEmpty() || !plotTasks.isEmpty() || !clusterTasks.isEmpty();
                    if (hasTask) {
                        if (SQLManager.this.mySQL && System.currentTimeMillis() - last > 550000 || !isValid()) {
                            last = System.currentTimeMillis();
                            reconnect();
                        }
                        if (!sendBatch()) {
                            try {
                                if (!getNotifyTasks().isEmpty()) {
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
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public boolean isValid() {
        try {
            if (connection.isClosed()) {
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
        try (PreparedStatement stmt = this.connection.prepareStatement("SELECT 1")) {
            stmt.executeQuery();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public void reconnect() {
        try {
            close();
            SQLManager.this.closed = false;
            SQLManager.this.connection = database.forceConnection();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public synchronized Queue<Runnable> getGlobalTasks() {
        return this.globalTasks;
    }

    public synchronized Queue<Runnable> getNotifyTasks() {
        return this.notifyTasks;
    }

    public synchronized void addPlotTask(Plot plot, UniqueStatement task) {
        if (plot == null) {
            plot = new Plot(null, new PlotId(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }
        Queue<UniqueStatement> tasks = this.plotTasks.get(plot);
        if (tasks == null) {
            tasks = new ConcurrentLinkedQueue<>();
            this.plotTasks.put(plot, tasks);
        }
        if (task == null) {
            task = new UniqueStatement(String.valueOf(plot.hashCode())) {

                @Override
                public PreparedStatement get() {
                    return null;
                }

                @Override
                public void set(PreparedStatement stmt) {}

                @Override
                public void addBatch(PreparedStatement statement) {}

                @Override
                public void execute(PreparedStatement statement) {}

            };
        }
        tasks.add(task);
    }

    public synchronized void addPlayerTask(UUID uuid, UniqueStatement task) {
        if (uuid == null) {
            return;
        }
        Queue<UniqueStatement> tasks = this.playerTasks.get(uuid);
        if (tasks == null) {
            tasks = new ConcurrentLinkedQueue<>();
            this.playerTasks.put(uuid, tasks);
        }
        if (task == null) {
            task = new UniqueStatement(String.valueOf(uuid.hashCode())) {

                @Override
                public PreparedStatement get() {
                    return null;
                }

                @Override
                public void set(PreparedStatement stmt) {}

                @Override
                public void addBatch(PreparedStatement statement) {}

                @Override
                public void execute(PreparedStatement statement) {}

            };
        }
        tasks.add(task);
    }

    public synchronized void addClusterTask(PlotCluster cluster, UniqueStatement task) {
        Queue<UniqueStatement> tasks = this.clusterTasks.get(cluster);
        if (tasks == null) {
            tasks = new ConcurrentLinkedQueue<>();
            this.clusterTasks.put(cluster, tasks);
        }
        if (task == null) {
            task = new UniqueStatement(String.valueOf(cluster.hashCode())) {

                @Override
                public PreparedStatement get() {
                    return null;
                }

                @Override
                public void set(PreparedStatement stmt) {}

                @Override
                public void addBatch(PreparedStatement statement) {}

                @Override
                public void execute(PreparedStatement statement) {}

            };
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

    public boolean sendBatch() {
        try {
            if (!getGlobalTasks().isEmpty()) {
                if (this.connection.getAutoCommit()) {
                    this.connection.setAutoCommit(false);
                }
                Runnable task = getGlobalTasks().remove();
                if (task != null) {
                    try {
                        task.run();
                    } catch (Throwable e) {
                        PS.debug("============ DATABASE ERROR ============");
                        PS.debug("There was an error updating the database.");
                        PS.debug(" - It will be corrected on shutdown");
                        PS.debug("========================================");
                        e.printStackTrace();
                        PS.debug("========================================");
                    }
                }
                commit();
                return true;
            }
            int count = -1;
            if (!this.plotTasks.isEmpty()) {
                count = 0;
                if (this.connection.getAutoCommit()) {
                    this.connection.setAutoCommit(false);
                }
                String method = null;
                PreparedStatement statement = null;
                UniqueStatement task = null;
                UniqueStatement lastTask = null;
                Iterator<Entry<Plot, Queue<UniqueStatement>>> iter = this.plotTasks.entrySet().iterator();
                while (iter.hasNext()) {
                    try {
                        Entry<Plot, Queue<UniqueStatement>> entry = iter.next();
                        Plot plot = entry.getKey();
                        Queue<UniqueStatement> tasks = entry.getValue();
                        if (tasks.isEmpty()) {
                            iter.remove();
                            continue;
                        }
                        task = tasks.remove();
                        count++;
                        if (task != null) {
                            if (task.method == null || !task.method.equals(method) || statement == null) {
                                if (statement != null) {
                                    lastTask.execute(statement);
                                    statement.close();
                                }
                                method = task.method;
                                statement = task.get();
                            }
                            task.set(statement);
                            task.addBatch(statement);
                            try {
                                if (statement != null && statement.isClosed()) {
                                    statement = null;
                                }
                            } catch (AbstractMethodError ignore) {}
                        }
                        lastTask = task;
                    } catch (Throwable e) {
                        PS.debug("============ DATABASE ERROR ============");
                        PS.debug("There was an error updating the database.");
                        PS.debug(" - It will be corrected on shutdown");
                        PS.debug("========================================");
                        e.printStackTrace();
                        PS.debug("========================================");
                    }
                }
                if (statement != null && task != null) {
                    task.execute(statement);
                    statement.close();
                }
            }
            if (!this.playerTasks.isEmpty()) {
                count = 0;
                if (this.connection.getAutoCommit()) {
                    this.connection.setAutoCommit(false);
                }
                String method = null;
                PreparedStatement statement = null;
                UniqueStatement task = null;
                UniqueStatement lastTask = null;
                for (Entry<UUID, Queue<UniqueStatement>> entry : this.playerTasks.entrySet()) {
                    try {
                        UUID uuid = entry.getKey();
                        if (this.playerTasks.get(uuid).isEmpty()) {
                            this.playerTasks.remove(uuid);
                            continue;
                        }
                        task = this.playerTasks.get(uuid).remove();
                        count++;
                        if (task != null) {
                            if (task.method == null || !task.method.equals(method)) {
                                if (statement != null) {
                                    lastTask.execute(statement);
                                    statement.close();
                                }
                                method = task.method;
                                statement = task.get();
                            }
                            task.set(statement);
                            task.addBatch(statement);
                        }
                        lastTask = task;
                    } catch (Throwable e) {
                        PS.debug("============ DATABASE ERROR ============");
                        PS.debug("There was an error updating the database.");
                        PS.debug(" - It will be corrected on shutdown");
                        PS.debug("========================================");
                        e.printStackTrace();
                        PS.debug("========================================");
                    }
                }
                if (statement != null && task != null) {
                    task.execute(statement);
                    statement.close();
                }
            }
            if (!this.clusterTasks.isEmpty()) {
                count = 0;
                if (this.connection.getAutoCommit()) {
                    this.connection.setAutoCommit(false);
                }
                String method = null;
                PreparedStatement statement = null;
                UniqueStatement task = null;
                UniqueStatement lastTask = null;
                for (Entry<PlotCluster, Queue<UniqueStatement>> entry : this.clusterTasks.entrySet()) {
                    try {
                        PlotCluster cluster = entry.getKey();
                        if (this.clusterTasks.get(cluster).isEmpty()) {
                            this.clusterTasks.remove(cluster);
                            continue;
                        }
                        task = this.clusterTasks.get(cluster).remove();
                        count++;
                        if (task != null) {
                            if (task.method == null || !task.method.equals(method)) {
                                if (statement != null) {
                                    lastTask.execute(statement);
                                    statement.close();
                                }
                                method = task.method;
                                statement = task.get();
                            }
                            task.set(statement);
                            task.addBatch(statement);
                        }
                        lastTask = task;
                    } catch (Throwable e) {
                        PS.debug("============ DATABASE ERROR ============");
                        PS.debug("There was an error updating the database.");
                        PS.debug(" - It will be corrected on shutdown");
                        PS.debug("========================================");
                        e.printStackTrace();
                        PS.debug("========================================");
                    }
                }
                if (statement != null && task != null) {
                    task.execute(statement);
                    statement.close();
                }
            }
            if (count > 0) {
                commit();
                return true;
            }
            if (count != -1) {
                if (!this.connection.getAutoCommit()) {
                    this.connection.setAutoCommit(true);
                }
            }
            if (!this.clusterTasks.isEmpty()) {
                this.clusterTasks.clear();
            }
            if (!this.plotTasks.isEmpty()) {
                this.plotTasks.clear();
            }
        } catch (Throwable e) {
            PS.debug("============ DATABASE ERROR ============");
            PS.debug("There was an error updating the database.");
            PS.debug(" - It will be corrected on shutdown");
            PS.debug("========================================");
            e.printStackTrace();
            PS.debug("========================================");
        }
        return false;
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
                statement.setInt(2, plot.getId().x);
                statement.setInt(3, plot.getId().y);
                statement.setString(4, plot.getArea().toString());
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement(SQLManager.this.SET_OWNER);
            }
        });
    }

    @Override
    public void createPlotsAndData(final List<Plot> myList, final Runnable whenDone) {
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
                                    plotMap.put(plot.getId(), plot);
                                }
                                ArrayList<SettingsPair> settings = new ArrayList<>();
                                final ArrayList<UUIDPair> helpers = new ArrayList<>();
                                final ArrayList<UUIDPair> trusted = new ArrayList<>();
                                final ArrayList<UUIDPair> denied = new ArrayList<>();

                                // Populating structures
                                try (PreparedStatement stmt = SQLManager.this.connection.prepareStatement(SQLManager.this.GET_ALL_PLOTS);
                                        ResultSet result = stmt.executeQuery()) {
                                    while (result.next()) {
                                        int id = result.getInt("id");
                                        int x = result.getInt("plot_id_x");
                                        int y = result.getInt("plot_id_z");
                                        PlotId plotId = new PlotId(x, y);
                                        Plot plot = plotMap.get(plotId);
                                        if (plot != null) {
                                            settings.add(new SettingsPair(id, plot.getSettings()));
                                            for (UUID uuid : plot.getDenied()) {
                                                denied.add(new UUIDPair(id, uuid));
                                            }
                                            for (UUID uuid : plot.getMembers()) {
                                                trusted.add(new UUIDPair(id, uuid));
                                            }
                                            for (UUID uuid : plot.getTrusted()) {
                                                helpers.add(new UUIDPair(id, uuid));
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
                                                                    SQLManager.this.connection.commit();
                                                                } catch (SQLException e) {
                                                                    e.printStackTrace();
                                                                }
                                                                if (whenDone != null) {
                                                                    whenDone.run();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            } catch (SQLException e) {
                                e.printStackTrace();
                                PS.debug("&7[WARN] Failed to set all helpers for plots");
                                try {
                                    SQLManager.this.connection.commit();
                                } catch (SQLException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    PS.debug("&7[WARN] Failed to set all helpers for plots");
                    try {
                        SQLManager.this.connection.commit();
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
    public void createTiers(ArrayList<UUIDPair> myList, final String tier, Runnable whenDone) {
        StmtMod<UUIDPair> mod = new StmtMod<UUIDPair>() {
            @Override
            public String getCreateMySQL(int size) {
                return getCreateMySQL(size, SQLManager.this.CREATE_TIERS.replaceAll("%tier%", tier), 2);
            }

            @Override
            public String getCreateSQLite(int size) {
                return getCreateSQLite(size,
                        "INSERT INTO `" + SQLManager.this.prefix + "plot_" + tier + "` SELECT ? AS `plot_plot_id`, ? AS `user_uuid`", 2);
            }

            @Override
            public String getCreateSQL() {
                return "INSERT INTO `" + SQLManager.this.prefix + "plot_" + tier + "` (`plot_plot_id`, `user_uuid`) VALUES(?,?)";
            }

            @Override
            public void setMySQL(PreparedStatement stmt, int i, UUIDPair pair) throws SQLException {
                stmt.setInt(i * 2 + 1, pair.id);
                stmt.setString(i * 2 + 2, pair.uuid.toString());
            }

            @Override
            public void setSQLite(PreparedStatement stmt, int i, UUIDPair pair) throws SQLException {
                stmt.setInt(i * 2 + 1, pair.id);
                stmt.setString(i * 2 + 2, pair.uuid.toString());
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
    public void createPlots(List<Plot> myList, Runnable whenDone) {
        StmtMod<Plot> mod = new StmtMod<Plot>() {
            @Override
            public String getCreateMySQL(int size) {
                return getCreateMySQL(size, SQLManager.this.CREATE_PLOTS, 5);
            }

            @Override
            public String getCreateSQLite(int size) {
                return getCreateSQLite(size, "INSERT INTO `" + SQLManager.this.prefix
                        + "plot` SELECT ? AS `id`, ? AS `plot_id_x`, ? AS `plot_id_z`, ? AS `owner`, ? AS `world`, ? AS `timestamp` ", 6);
            }

            @Override
            public String getCreateSQL() {
                return SQLManager.this.CREATE_PLOT;
            }

            @Override
            public void setMySQL(PreparedStatement stmt, int i, Plot plot) throws SQLException {
                stmt.setInt(i * 5 + 1, plot.getId().x);
                stmt.setInt(i * 5 + 2, plot.getId().y);
                try {
                    stmt.setString(i * 5 + 3, plot.owner.toString());
                } catch (SQLException ignored) {
                    stmt.setString(i * 5 + 3, AbstractDB.everyone.toString());
                }
                stmt.setString(i * 5 + 4, plot.getArea().toString());
                stmt.setTimestamp(i * 5 + 5, new Timestamp(plot.getTimestamp()));
            }

            @Override
            public void setSQLite(PreparedStatement stmt, int i, Plot plot) throws SQLException {
                stmt.setNull(i * 6 + 1, 4);
                stmt.setInt(i * 6 + 2, plot.getId().x);
                stmt.setInt(i * 6 + 3, plot.getId().y);
                try {
                    stmt.setString(i * 6 + 4, plot.owner.toString());
                } catch (SQLException ignored) {
                    stmt.setString(i * 6 + 4, AbstractDB.everyone.toString());
                }
                stmt.setString(i * 6 + 5, plot.getArea().toString());
                stmt.setTimestamp(i * 6 + 6, new Timestamp(plot.getTimestamp()));
            }

            @Override
            public void setSQL(PreparedStatement stmt, Plot plot) throws SQLException {
                stmt.setInt(1, plot.getId().x);
                stmt.setInt(2, plot.getId().y);
                stmt.setString(3, plot.owner.toString());
                stmt.setString(4, plot.getArea().toString());
                stmt.setTimestamp(5, new Timestamp(plot.getTimestamp()));

            }
        };
        setBulk(myList, mod, whenDone);
    }

    public <T> void setBulk(List<T> objList, StmtMod<T> mod, Runnable whenDone) {
        int size = objList.size();
        if (size == 0) {
            if (whenDone != null) {
                whenDone.run();
            }
            return;
        }
        int packet;
        if (this.mySQL) {
            packet = Math.min(size, 5000);
        } else {
            packet = Math.min(size, 50);
        }
        int amount = size / packet;
        try {
            int count = 0;
            PreparedStatement preparedStmt = null;
            int last = -1;
            for (int j = 0; j <= amount; j++) {
                List<T> subList = objList.subList(j * packet, Math.min(size, (j + 1) * packet));
                if (subList.isEmpty()) {
                    break;
                }
                String statement;
                if (last == -1) {
                    last = subList.size();
                    statement = mod.getCreateMySQL(subList.size());
                    preparedStmt = this.connection.prepareStatement(statement);
                }
                if (subList.size() != last || count % 5000 == 0 && count > 0) {
                    preparedStmt.executeBatch();
                    preparedStmt.close();
                    statement = mod.getCreateMySQL(subList.size());
                    preparedStmt = this.connection.prepareStatement(statement);
                }
                for (int i = 0; i < subList.size(); i++) {
                    count++;
                    T obj = subList.get(i);
                    mod.setMySQL(preparedStmt, i, obj);
                }
                last = subList.size();
                preparedStmt.addBatch();
            }
            PS.debug("&aBatch 1: " + count + " | " + objList.get(0).getClass().getCanonicalName());
            preparedStmt.executeBatch();
            preparedStmt.clearParameters();
            preparedStmt.close();
            if (whenDone != null) {
                whenDone.run();
            }
            return;
        } catch (SQLException e) {
            if (this.mySQL) {
                e.printStackTrace();
                PS.debug("&cERROR 1:  | " + objList.get(0).getClass().getCanonicalName());
            }
        }
        try {
            int count = 0;
            PreparedStatement preparedStmt = null;
            int last = -1;
            for (int j = 0; j <= amount; j++) {
                List<T> subList = objList.subList(j * packet, Math.min(size, (j + 1) * packet));
                if (subList.isEmpty()) {
                    break;
                }
                String statement;
                if (last == -1) {
                    last = subList.size();
                    statement = mod.getCreateSQLite(subList.size());
                    preparedStmt = this.connection.prepareStatement(statement);
                }
                if (subList.size() != last || count % 5000 == 0 && count > 0) {
                    preparedStmt.executeBatch();
                    preparedStmt.clearParameters();
                    statement = mod.getCreateSQLite(subList.size());
                    preparedStmt = this.connection.prepareStatement(statement);
                }
                for (int i = 0; i < subList.size(); i++) {
                    count++;
                    T obj = subList.get(i);
                    mod.setSQLite(preparedStmt, i, obj);
                }
                last = subList.size();
                preparedStmt.addBatch();
            }
            PS.debug("&aBatch 2: " + count + " | " + objList.get(0).getClass().getCanonicalName());
            preparedStmt.executeBatch();
            preparedStmt.clearParameters();
            preparedStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            PS.debug("&cERROR 2:  | " + objList.get(0).getClass().getCanonicalName());
            PS.debug("&6[WARN] Could not bulk save!");
            try (PreparedStatement preparedStmt = this.connection.prepareStatement(mod.getCreateSQL())) {
                for (T obj : objList) {
                    mod.setSQL(preparedStmt, obj);
                    preparedStmt.addBatch();
                }
                PS.debug("&aBatch 3");
                preparedStmt.executeBatch();
            } catch (SQLException e3) {
                e3.printStackTrace();
                PS.debug("&c[ERROR] Failed to save all!");
            }
        }
        if (whenDone != null) {
            whenDone.run();
        }
    }

    public void createSettings(final ArrayList<SettingsPair> myList, final Runnable whenDone) {
        final StmtMod<SettingsPair> mod = new StmtMod<SettingsPair>() {
            @Override
            public String getCreateMySQL(int size) {
                return getCreateMySQL(size, "INSERT INTO `" + SQLManager.this.prefix
                                + "plot_settings`(`plot_plot_id`,`biome`,`rain`,`custom_time`,`time`,`deny_entry`,`alias`,`flags`,`merged`,"
                                + "`position`) VALUES ",
                        10);
            }

            @Override
            public String getCreateSQLite(int size) {
                return getCreateSQLite(
                        size,
                        "INSERT INTO `"
                                + SQLManager.this.prefix
                                + "plot_settings` SELECT ? AS `plot_plot_id`, ? AS `biome`, ? AS `rain`, ? AS `custom_time`, ? AS `time`, ? AS "
                                + "`deny_entry`, ? AS `alias`, ? AS `flags`, ? AS `merged`, ? AS `position`",
                        10);
            }

            @Override
            public String getCreateSQL() {
                return "INSERT INTO `" + SQLManager.this.prefix + "plot_settings`(`plot_plot_id`) VALUES(?)";
            }

            @Override
            public void setMySQL(PreparedStatement statement, int i, SettingsPair pair) throws SQLException {
                statement.setInt(i * 10 + 1, pair.id); // id
                statement.setNull(i * 10 + 2, 4); // biome
                statement.setNull(i * 10 + 3, 4); // rain
                statement.setNull(i * 10 + 4, 4); // custom_time
                statement.setNull(i * 10 + 5, 4); // time
                statement.setNull(i * 10 + 6, 4); // deny_entry
                if (pair.settings.getAlias().isEmpty()) {
                    statement.setNull(i * 10 + 7, 4);
                } else {
                    statement.setString(i * 10 + 7, pair.settings.getAlias());
                }
                StringBuilder flag_string = new StringBuilder();
                int k = 0;
                for (Entry<Flag<?>, ?> flag : pair.settings.flags.entrySet()) {
                    if (k != 0) {
                        flag_string.append(',');
                    }
                    flag_string.append(flag.getKey().getName()).append(':').append(flag.getKey().valueToString(flag.getValue()).replaceAll(":", "¯")
                            .replaceAll(",", "´"));
                    k++;
                }
                statement.setString(i * 10 + 8, flag_string.toString());
                boolean[] merged = pair.settings.getMerged();
                int hash = MainUtil.hash(merged);
                statement.setInt(i * 10 + 9, hash);
                BlockLoc loc = pair.settings.getPosition();
                String position;
                if (loc.y == 0) {
                    position = "DEFAULT";
                } else {
                    position = loc.x + "," + loc.y + ',' + loc.z;
                }
                statement.setString(i * 10 + 10, position);
            }

            @Override
            public void setSQLite(PreparedStatement stmt, int i, SettingsPair pair) throws SQLException {
                stmt.setInt(i * 10 + 1, pair.id); // id
                stmt.setNull(i * 10 + 2, 4); // biome
                stmt.setNull(i * 10 + 3, 4); // rain
                stmt.setNull(i * 10 + 4, 4); // custom_time
                stmt.setNull(i * 10 + 5, 4); // time
                stmt.setNull(i * 10 + 6, 4); // deny_entry
                if (pair.settings.getAlias().isEmpty()) {
                    stmt.setNull(i * 10 + 7, 4);
                } else {
                    stmt.setString(i * 10 + 7, pair.settings.getAlias());
                }
                StringBuilder flag_string = new StringBuilder();
                int k = 0;
                for (Entry<Flag<?>, ?> flag : pair.settings.flags.entrySet()) {
                    if (k != 0) {
                        flag_string.append(',');
                    }
                    flag_string.append(flag.getKey().getName()).append(':').append(flag.getKey().valueToString(flag.getValue()).replaceAll(":", "¯")
                            .replaceAll(",", "´"));
                    k++;
                }
                stmt.setString(i * 10 + 8, flag_string.toString());
                boolean[] merged = pair.settings.getMerged();
                int n = 0;
                for (int j = 0; j < 4; ++j) {
                    n = (n << 1) + (merged[j] ? 1 : 0);
                }
                stmt.setInt(i * 10 + 9, n);
                BlockLoc loc = pair.settings.getPosition();
                String position;
                if (loc.y == 0) {
                    position = "DEFAULT";
                } else {
                    position = loc.x + "," + loc.y + ',' + loc.z;
                }
                stmt.setString(i * 10 + 10, position);
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
                return getCreateMySQL(size, SQLManager.this.CREATE_SETTINGS, 1);
            }

            @Override
            public String getCreateSQLite(int size) {
                return getCreateSQLite(
                        size,
                        "INSERT INTO `"
                                + SQLManager.this.prefix
                                + "plot_settings` SELECT ? AS `plot_plot_id`, ? AS `biome`, ? AS `rain`, ? AS `custom_time`, ? AS `time`, ? AS "
                                + "`deny_entry`, ? AS `alias`, ? AS `flags`, ? AS `merged`, ? AS `position` ",
                        10);
            }

            @Override
            public String getCreateSQL() {
                return "INSERT INTO `" + SQLManager.this.prefix + "plot_settings`(`plot_plot_id`) VALUES(?)";
            }

            @Override
            public void setMySQL(PreparedStatement stmt, int i, Integer id) throws SQLException {
                stmt.setInt(i + 1, id);
            }

            @Override
            public void setSQLite(PreparedStatement stmt, int i, Integer id) throws SQLException {
                stmt.setInt(i * 10 + 1, id);
                stmt.setNull(i * 10 + 2, 4);
                stmt.setNull(i * 10 + 3, 4);
                stmt.setNull(i * 10 + 4, 4);
                stmt.setNull(i * 10 + 5, 4);
                stmt.setNull(i * 10 + 6, 4);
                stmt.setNull(i * 10 + 7, 4);
                stmt.setNull(i * 10 + 8, 4);
                stmt.setNull(i * 10 + 9, 4);
                stmt.setString(i * 10 + 10, "DEFAULT");
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

    public void createPlotSafe(final Plot plot, final Runnable success, final Runnable failure) {
        final long timestamp = plot.getTimestamp();
        addPlotTask(plot, new UniqueStatement("createPlotSafe_" + plot.hashCode()) {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, plot.getId().x);
                stmt.setInt(2, plot.getId().y);
                stmt.setString(3, plot.owner.toString());
                stmt.setString(4, plot.getArea().toString());
                stmt.setTimestamp(5, new Timestamp(plot.getTimestamp()));
                stmt.setString(6, plot.getArea().toString());
                stmt.setInt(7, plot.getId().x);
                stmt.setInt(8, plot.getId().y);
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement(SQLManager.this.CREATE_PLOT_SAFE, Statement.RETURN_GENERATED_KEYS );
            }

            @Override
            public void execute(PreparedStatement statement) {

            }

            @Override
            public void addBatch(PreparedStatement statement) throws SQLException {
                int inserted = statement.executeUpdate();
                if (inserted > 0) {
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (keys.next()) {
                            plot.temp = keys.getInt(1);
                            addPlotTask(plot, new UniqueStatement("createPlotAndSettings_settings_" + plot.hashCode()) {
                                @Override
                                public void set(PreparedStatement stmt) throws SQLException {
                                    stmt.setInt(1, getId(plot));
                                }

                                @Override
                                public PreparedStatement get() throws SQLException {
                                    return SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_settings`(`plot_plot_id`) VALUES(?)");
                                }
                            });
                            if (success != null) addNotifyTask(success);
                            return;
                        }
                    }
                }
                if (failure != null) failure.run();
            }
        });
    }

    public void commit() {
        if (this.closed) {
            return;
        }
        try {
            if (!this.connection.getAutoCommit()) {
                this.connection.commit();
                this.connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createPlotAndSettings(final Plot plot, Runnable whenDone) {
        addPlotTask(plot, new UniqueStatement("createPlotAndSettings_" + plot.hashCode()) {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, plot.getId().x);
                stmt.setInt(2, plot.getId().y);
                stmt.setString(3, plot.owner.toString());
                stmt.setString(4, plot.getArea().toString());
                stmt.setTimestamp(5, new Timestamp(plot.getTimestamp()));
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement(SQLManager.this.CREATE_PLOT, Statement.RETURN_GENERATED_KEYS);
            }

            @Override
            public void execute(PreparedStatement statement) {}

            @Override
            public void addBatch(PreparedStatement statement) throws SQLException {
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        plot.temp = keys.getInt(1);
                    }
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
                return SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_settings`(`plot_plot_id`) VALUES(?)");
            }
        });
        addNotifyTask(whenDone);
    }

    /**
     * Create tables.
     *
     * @throws SQLException
     */
    @Override
    public void createTables() throws SQLException {
        String[] tables =
                new String[]{"plot", "plot_denied", "plot_helpers", "plot_comments", "plot_trusted", "plot_rating", "plot_settings", "cluster",
                        "player_meta"};
        DatabaseMetaData meta = this.connection.getMetaData();
        int create = 0;
        for (String s : tables) {
            ResultSet set = meta.getTables(null, null, this.prefix + s, new String[]{"TABLE"});
            //            ResultSet set = meta.getTables(null, null, prefix + s, null);
            if (!set.next()) {
                create++;
            }
            set.close();
        }
        if (create == 0) {
            return;
        }
        boolean addConstraint = create == tables.length;
        PS.debug("Creating tables");
        try (Statement stmt = this.connection.createStatement()) {
            if (this.mySQL) {
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `"
                        + this.prefix
                        + "plot` ("
                        + "`id` INT(11) NOT NULL AUTO_INCREMENT,"
                        + "`plot_id_x` INT(11) NOT NULL,"
                        + "`plot_id_z` INT(11) NOT NULL,"
                        + "`owner` VARCHAR(40) NOT NULL,"
                        + "`world` VARCHAR(45) NOT NULL,"
                        + "`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                        + "PRIMARY KEY (`id`)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=0");
                stmt
                        .addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_denied` (`plot_plot_id` INT(11) NOT NULL,"
                                + "`user_uuid` VARCHAR(40) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `"
                        + this.prefix
                        + "plot_helpers` ("
                        + "`plot_plot_id` INT(11) NOT NULL,"
                        + "`user_uuid` VARCHAR(40) NOT NULL"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `"
                        + this.prefix
                        + "plot_comments` ("
                        + "`world` VARCHAR(40) NOT NULL, `hashcode` INT(11) NOT NULL,"
                        + "`comment` VARCHAR(40) NOT NULL,"
                        + "`inbox` VARCHAR(40) NOT NULL,"
                        + "`timestamp` INT(11) NOT NULL,"
                        + "`sender` VARCHAR(40) NOT NULL"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `"
                        + this.prefix
                        + "plot_trusted` ("
                        + "`plot_plot_id` INT(11) NOT NULL,"
                        + "`user_uuid` VARCHAR(40) NOT NULL"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `"
                        + this.prefix
                        + "plot_settings` ("
                        + "  `plot_plot_id` INT(11) NOT NULL,"
                        + "  `biome` VARCHAR(45) DEFAULT 'FOREST',"
                        + "  `rain` INT(1) DEFAULT 0,"
                        + "  `custom_time` TINYINT(1) DEFAULT '0',"
                        + "  `time` INT(11) DEFAULT '8000',"
                        + "  `deny_entry` TINYINT(1) DEFAULT '0',"
                        + "  `alias` VARCHAR(50) DEFAULT NULL,"
                        + "  `flags` VARCHAR(512) DEFAULT NULL,"
                        + "  `merged` INT(11) DEFAULT NULL,"
                        + "  `position` VARCHAR(50) NOT NULL DEFAULT 'DEFAULT',"
                        + "  PRIMARY KEY (`plot_plot_id`)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `"
                        + this.prefix
                        + "plot_rating` ( `plot_plot_id` INT(11) NOT NULL, `rating` INT(2) NOT NULL, `player` VARCHAR(40) NOT NULL) ENGINE=InnoDB "
                        + "DEFAULT CHARSET=utf8");
                if (addConstraint) {
                    stmt.addBatch("ALTER TABLE `"
                            + this.prefix
                            + "plot_settings` ADD CONSTRAINT `"
                            + this.prefix
                            + "plot_settings_ibfk_1` FOREIGN KEY (`plot_plot_id`) REFERENCES `"
                            + this.prefix
                            + "plot` (`id`) ON DELETE CASCADE");
                }
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `"
                        + this.prefix
                        + "cluster` ("
                        + "`id` INT(11) NOT NULL AUTO_INCREMENT,"
                        + "`pos1_x` INT(11) NOT NULL,"
                        + "`pos1_z` INT(11) NOT NULL,"
                        + "`pos2_x` INT(11) NOT NULL,"
                        + "`pos2_z` INT(11) NOT NULL,"
                        + "`owner` VARCHAR(40) NOT NULL,"
                        + "`world` VARCHAR(45) NOT NULL,"
                        + "`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                        + "PRIMARY KEY (`id`)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=0");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `"
                        + this.prefix
                        + "cluster_helpers` ("
                        + "`cluster_id` INT(11) NOT NULL,"
                        + "`user_uuid` VARCHAR(40) NOT NULL"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `"
                        + this.prefix
                        + "cluster_invited` ("
                        + "`cluster_id` INT(11) NOT NULL,"
                        + "`user_uuid` VARCHAR(40) NOT NULL"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `"
                        + this.prefix
                        + "cluster_settings` ("
                        + "  `cluster_id` INT(11) NOT NULL,"
                        + "  `biome` VARCHAR(45) DEFAULT 'FOREST',"
                        + "  `rain` INT(1) DEFAULT 0,"
                        + "  `custom_time` TINYINT(1) DEFAULT '0',"
                        + "  `time` INT(11) DEFAULT '8000',"
                        + "  `deny_entry` TINYINT(1) DEFAULT '0',"
                        + "  `alias` VARCHAR(50) DEFAULT NULL,"
                        + "  `flags` VARCHAR(512) DEFAULT NULL,"
                        + "  `merged` INT(11) DEFAULT NULL,"
                        + "  `position` VARCHAR(50) NOT NULL DEFAULT 'DEFAULT',"
                        + "  PRIMARY KEY (`cluster_id`)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `"
                        + this.prefix
                        + "player_meta` ("
                        + " `meta_id` INT(11) NOT NULL AUTO_INCREMENT,"
                        + " `uuid` VARCHAR(40) NOT NULL,"
                        + " `key` VARCHAR(32) NOT NULL,"
                        + " `value` blob NOT NULL,"
                        + " PRIMARY KEY (`meta_id`)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
            } else {
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `"
                        + this.prefix
                        + "plot` ("
                        + "`id` INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "`plot_id_x` INT(11) NOT NULL,"
                        + "`plot_id_z` INT(11) NOT NULL,"
                        + "`owner` VARCHAR(45) NOT NULL,"
                        + "`world` VARCHAR(45) NOT NULL,"
                        + "`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP)");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_denied` (`plot_plot_id` INT(11) NOT NULL,"
                        + "`user_uuid` VARCHAR(40) NOT NULL)");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_helpers` (`plot_plot_id` INT(11) NOT NULL,"
                        + "`user_uuid` VARCHAR(40) NOT NULL)");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "plot_trusted` (`plot_plot_id` INT(11) NOT NULL,"
                        + "`user_uuid` VARCHAR(40) NOT NULL)");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `"
                        + this.prefix
                        + "plot_comments` ("
                        + "`world` VARCHAR(40) NOT NULL, `hashcode` INT(11) NOT NULL,"
                        + "`comment` VARCHAR(40) NOT NULL,"
                        + "`inbox` VARCHAR(40) NOT NULL, `timestamp` INT(11) NOT NULL,"
                        + "`sender` VARCHAR(40) NOT NULL"
                        + ')');
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `"
                        + this.prefix
                        + "plot_settings` ("
                        + "  `plot_plot_id` INT(11) NOT NULL,"
                        + "  `biome` VARCHAR(45) DEFAULT 'FOREST',"
                        + "  `rain` INT(1) DEFAULT 0,"
                        + "  `custom_time` TINYINT(1) DEFAULT '0',"
                        + "  `time` INT(11) DEFAULT '8000',"
                        + "  `deny_entry` TINYINT(1) DEFAULT '0',"
                        + "  `alias` VARCHAR(50) DEFAULT NULL,"
                        + "  `flags` VARCHAR(512) DEFAULT NULL,"
                        + "  `merged` INT(11) DEFAULT NULL,"
                        + "  `position` VARCHAR(50) NOT NULL DEFAULT 'DEFAULT',"
                        + "  PRIMARY KEY (`plot_plot_id`)"
                        + ')');
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix
                        + "plot_rating` (`plot_plot_id` INT(11) NOT NULL, `rating` INT(2) NOT NULL, `player` VARCHAR(40) NOT NULL)");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `"
                        + this.prefix
                        + "cluster` ("
                        + "`id` INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "`pos1_x` INT(11) NOT NULL,"
                        + "`pos1_z` INT(11) NOT NULL,"
                        + "`pos2_x` INT(11) NOT NULL,"
                        + "`pos2_z` INT(11) NOT NULL,"
                        + "`owner` VARCHAR(40) NOT NULL,"
                        + "`world` VARCHAR(45) NOT NULL,"
                        + "`timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP"
                        + ')');
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "cluster_helpers` (`cluster_id` INT(11) NOT NULL,"
                        + "`user_uuid` VARCHAR(40) NOT NULL)");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `" + this.prefix + "cluster_invited` (`cluster_id` INT(11) NOT NULL,"
                        + "`user_uuid` VARCHAR(40) NOT NULL)");
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `"
                        + this.prefix
                        + "cluster_settings` ("
                        + "  `cluster_id` INT(11) NOT NULL,"
                        + "  `biome` VARCHAR(45) DEFAULT 'FOREST',"
                        + "  `rain` INT(1) DEFAULT 0,"
                        + "  `custom_time` TINYINT(1) DEFAULT '0',"
                        + "  `time` INT(11) DEFAULT '8000',"
                        + "  `deny_entry` TINYINT(1) DEFAULT '0',"
                        + "  `alias` VARCHAR(50) DEFAULT NULL,"
                        + "  `flags` VARCHAR(512) DEFAULT NULL,"
                        + "  `merged` INT(11) DEFAULT NULL,"
                        + "  `position` VARCHAR(50) NOT NULL DEFAULT 'DEFAULT',"
                        + "  PRIMARY KEY (`cluster_id`)"
                        + ')');
                stmt.addBatch("CREATE TABLE IF NOT EXISTS `"
                        + this.prefix
                        + "player_meta` ("
                        + " `meta_id` INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + " `uuid` VARCHAR(40) NOT NULL,"
                        + " `key` VARCHAR(32) NOT NULL,"
                        + " `value` blob NOT NULL"
                        + ')');
            }
            stmt.executeBatch();
            stmt.clearBatch();
        }
    }

    @Override
    public void deleteSettings(final Plot plot) {
        addPlotTask(plot, new UniqueStatement("delete_plot_settings") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, getId(plot));
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_settings` WHERE `plot_plot_id` = ?");
            }
        });
    }

    @Override
    public void deleteHelpers(final Plot plot) {
        if (plot.getTrusted().isEmpty()) {
            return;
        }
        addPlotTask(plot, new UniqueStatement("delete_plot_helpers") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, getId(plot));
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_helpers` WHERE `plot_plot_id` = ?");
            }
        });
    }

    @Override
    public void deleteTrusted(final Plot plot) {
        if (plot.getMembers().isEmpty()) {
            return;
        }
        addPlotTask(plot, new UniqueStatement("delete_plot_trusted") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, getId(plot));
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_trusted` WHERE `plot_plot_id` = ?");
            }
        });
    }

    @Override
    public void deleteDenied(final Plot plot) {
        if (plot.getDenied().isEmpty()) {
            return;
        }
        addPlotTask(plot, new UniqueStatement("delete_plot_denied") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, getId(plot));
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_denied` WHERE `plot_plot_id` = ?");
            }
        });
    }

    @Override
    public void deleteComments(final Plot plot) {
        addPlotTask(plot, new UniqueStatement("delete_plot_comments") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setString(1, plot.getArea().toString());
                stmt.setInt(2, plot.hashCode());
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_comments` WHERE `world` = ? AND `hashcode` = ?");
            }
        });
    }

    @Override
    public void deleteRatings(final Plot plot) {
        if (Settings.Enabled_Components.RATING_CACHE && plot.getSettings().getRatings().isEmpty()) {
            return;
        }
        addPlotTask(plot, new UniqueStatement("delete_plot_ratings") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, getId(plot));
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_rating` WHERE `plot_plot_id` = ?");
            }
        });
    }

    /**
     * Delete a plot.
     *
     * @param plot
     */
    @Override
    public void delete(final Plot plot) {
        PS.debug("Deleting plot... Id: " + plot.getId() + " World: " + plot.getWorldName() + " Owner: " + plot.owner + " Index: " + plot.temp);
        deleteSettings(plot);
        deleteDenied(plot);
        deleteHelpers(plot);
        deleteTrusted(plot);
        deleteComments(plot);
        deleteRatings(plot);
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
    public void createPlotSettings(final int id, Plot plot) {
        PS.debug("Creating plot... Id: " + plot.getId() + " World: " + plot.getWorldName() + " Owner: " + plot.owner + " Index: " + id);
        addPlotTask(plot, new UniqueStatement("createPlotSettings") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, id);
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_settings`(`plot_plot_id`) VALUES(?)");
            }
        });
    }

    @Override
    public int getClusterId(PlotCluster cluster) {
        if (cluster.temp > 0) {
            return cluster.temp;
        }
        try {
            commit();
            if (cluster.temp > 0) {
                return cluster.temp;
            }
            int c_id;
            try (PreparedStatement stmt = this.connection.prepareStatement("SELECT `id` FROM `"
                    + this.prefix
                    + "cluster` WHERE `pos1_x` = ? AND `pos1_z` = ? AND `pos2_x` = ? AND `pos2_z` = ? AND `world` = ? ORDER BY `timestamp` ASC")) {
                stmt.setInt(1, cluster.getP1().x);
                stmt.setInt(2, cluster.getP1().y);
                stmt.setInt(3, cluster.getP2().x);
                stmt.setInt(4, cluster.getP2().y);
                stmt.setString(5, cluster.area.toString());
                try (ResultSet resultSet = stmt.executeQuery()) {
                    c_id = Integer.MAX_VALUE;
                    while (resultSet.next()) {
                        c_id = resultSet.getInt("id");
                    }
                }
            }
            if (c_id == Integer.MAX_VALUE || c_id == 0) {
                if (cluster.temp > 0) {
                    return cluster.temp;
                }
                throw new SQLException("Cluster does not exist in database");
            }
            cluster.temp = c_id;
            return c_id;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public int getId(Plot plot) {
        if (plot.temp > 0) {
            return plot.temp;
        }
        try {
            commit();
            if (plot.temp > 0) {
                return plot.temp;
            }
            int id;
            try (PreparedStatement statement = this.connection.prepareStatement(
                    "SELECT `id` FROM `" + this.prefix + "plot` WHERE `plot_id_x` = ? AND `plot_id_z` = ? AND world = ? ORDER BY `timestamp` ASC")) {
                statement.setInt(1, plot.getId().x);
                statement.setInt(2, plot.getId().y);
                statement.setString(3, plot.getArea().toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    id = Integer.MAX_VALUE;
                    while (resultSet.next()) {
                        id = resultSet.getInt("id");
                    }
                }
            }
            if (id == Integer.MAX_VALUE || id == 0) {
                if (plot.temp > 0) {
                    return plot.temp;
                }
                throw new SQLException("Plot does not exist in database");
            }
            plot.temp = id;
            return id;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Integer.MAX_VALUE;
    }

    @Override public void updateTables(int[] oldVersion) {
        try {
            if (this.mySQL && !PS.get().checkVersion(oldVersion, 3, 3, 2)) {
                try (Statement stmt = this.connection.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE `" + this.prefix + "plots` DROP INDEX `unique_alias`");
                } catch (SQLException ignored) {}
            }
            DatabaseMetaData data = this.connection.getMetaData();
            ResultSet rs = data.getColumns(null, null, this.prefix + "plot_comments", "plot_plot_id");
            if (rs.next()) {
                rs.close();
                rs = data.getColumns(null, null, this.prefix + "plot_comments", "hashcode");
                if (!rs.next()) {
                    rs.close();
                    try (Statement statement = this.connection.createStatement()) {
                        statement.addBatch("DROP TABLE `" + this.prefix + "plot_comments`");
                        if (Storage.MySQL.USE) {
                            statement.addBatch("CREATE TABLE IF NOT EXISTS `"
                                    + this.prefix
                                    + "plot_comments` ("
                                    + "`world` VARCHAR(40) NOT NULL, `hashcode` INT(11) NOT NULL,"
                                    + "`comment` VARCHAR(40) NOT NULL,"
                                    + "`inbox` VARCHAR(40) NOT NULL,"
                                    + "`timestamp` INT(11) NOT NULL,"
                                    + "`sender` VARCHAR(40) NOT NULL"
                                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
                        } else {
                            statement.addBatch("CREATE TABLE IF NOT EXISTS `"
                                    + this.prefix
                                    + "plot_comments` ("
                                    + "`world` VARCHAR(40) NOT NULL, `hashcode` INT(11) NOT NULL,"
                                    + "`comment` VARCHAR(40) NOT NULL,"
                                    + "`inbox` VARCHAR(40) NOT NULL, `timestamp` INT(11) NOT NULL,"
                                    + "`sender` VARCHAR(40) NOT NULL"
                                    + ')');
                        }
                        statement.executeBatch();
                    } catch (SQLException ignored) {
                        try (Statement statement = this.connection.createStatement()) {
                            statement.addBatch("ALTER IGNORE TABLE `" + this.prefix + "plot_comments` ADD `inbox` VARCHAR(11) DEFAULT `public`");
                            statement.addBatch("ALTER IGNORE TABLE `" + this.prefix + "plot_comments` ADD `timestamp` INT(11) DEFAULT 0");
                            statement.addBatch("ALTER TABLE `" + this.prefix + "plot` DROP `tier`");
                            statement.executeBatch();
                        }
                    }
                }
            }
            rs.close();
            rs = data.getColumns(null, null, this.prefix + "plot_denied", "plot_plot_id");
            if (rs.next()) {
                try (Statement statement = this.connection.createStatement()) {
                    statement.executeUpdate(
                            "DELETE FROM `" + this.prefix + "plot_denied` WHERE `plot_plot_id` NOT IN (SELECT `id` FROM `" + this.prefix + "plot`)");
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                rs.close();
                try (Statement statement = this.connection.createStatement()) {
                    for (String table : new String[]{"plot_denied", "plot_helpers", "plot_trusted"}) {
                        ResultSet result = statement.executeQuery("SELECT plot_plot_id, user_uuid, COUNT(*) FROM " + this.prefix + table
                                + " GROUP BY plot_plot_id, user_uuid HAVING COUNT(*) > 1");
                        if (result.next()) {
                            PS.debug("BACKING UP: " + this.prefix + table);
                            result.close();
                            statement.executeUpdate("CREATE TABLE " + this.prefix + table + "_tmp AS SELECT * FROM " + this.prefix + table
                                    + " GROUP BY plot_plot_id, user_uuid");
                            statement.executeUpdate("DROP TABLE " + this.prefix + table);
                            statement.executeUpdate("CREATE TABLE " + this.prefix + table + " AS SELECT * FROM " + this.prefix + table + "_tmp");
                            statement.executeUpdate("DROP TABLE " + this.prefix + table + "_tmp");
                            PS.debug("RESTORING: " + this.prefix + table);
                        }
                    }
                } catch (SQLException e2) {
                    e2.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void deleteRows(ArrayList<Integer> rowIds, final String table, final String column) {
        setBulk(rowIds, new StmtMod<Integer>() {

            @Override
            public String getCreateMySQL(int size) {
                return getCreateMySQL(1, "DELETE FROM `" + table + "` WHERE `" + column + "` IN ", size);
            }

            @Override
            public String getCreateSQLite(int size) {
                return getCreateMySQL(1, "DELETE FROM `" + table + "` WHERE `" + column + "` IN ", size);
            }

            @Override
            public String getCreateSQL() {
                return "DELETE FROM `" + table + "` WHERE `" + column + "` = ?";
            }

            @Override
            public void setMySQL(PreparedStatement stmt, int i, Integer obj) throws SQLException {
                stmt.setInt(i + 1, obj);
            }

            @Override
            public void setSQLite(PreparedStatement stmt, int i, Integer obj) throws SQLException {
                stmt.setInt(i + 1, obj);
            }

            @Override
            public void setSQL(PreparedStatement stmt, Integer obj) throws SQLException {
                stmt.setInt(1, obj);
            }
        }, null);
    }

    /**
     * Load all plots, helpers, denied, trusted, and every setting from DB into a {@link HashMap}.
     */
    @Override
    public HashMap<String, HashMap<PlotId, Plot>> getPlots() {
        HashMap<String, HashMap<PlotId, Plot>> newPlots = new HashMap<>();
        HashMap<Integer, Plot> plots = new HashMap<>();
        try {
            HashSet<String> areas = new HashSet<>();
            if (PS.get().worlds.contains("worlds")) {
                ConfigurationSection worldSection = PS.get().worlds.getConfigurationSection("worlds");
                if (worldSection != null) {
                    for (String worldKey : worldSection.getKeys(false)) {
                        areas.add(worldKey);
                        ConfigurationSection areaSection = worldSection.getConfigurationSection(worldKey + ".areas");
                        if (areaSection != null) {
                            for (String areaKey : areaSection.getKeys(false)) {
                                String[] split = areaKey.split("(?<![;])-");
                                if (split.length == 3) {
                                    areas.add(worldKey + ';' + split[0]);
                                }
                            }
                        }
                    }
                }
            }
            HashMap<String, UUID> uuids = new HashMap<>();
            HashMap<String, AtomicInteger> noExist = new HashMap<>();

            /*
             * Getting plots
             */
            try (Statement statement = this.connection.createStatement()) {
                int id;
                String o;
                UUID user;
                try (ResultSet resultSet = statement
                        .executeQuery("SELECT `id`, `plot_id_x`, `plot_id_z`, `owner`, `world`, `timestamp` FROM `" + this.prefix + "plot`")) {
                    ArrayList<Integer> toDelete = new ArrayList<>();
                    while (resultSet.next()) {
                        PlotId plot_id = new PlotId(resultSet.getInt("plot_id_x"), resultSet.getInt("plot_id_z"));
                        id = resultSet.getInt("id");
                        String areaid = resultSet.getString("world");
                        if (!areas.contains(areaid)) {
                            if (Settings.Enabled_Components.DATABASE_PURGER) {
                                toDelete.add(id);
                                continue;
                            } else {
                                AtomicInteger value = noExist.get(areaid);
                                if (value != null) {
                                    value.incrementAndGet();
                                } else {
                                    noExist.put(areaid, new AtomicInteger(1));
                                }
                            }
                        }
                        o = resultSet.getString("owner");
                        user = uuids.get(o);
                        if (user == null) {
                            try {
                                user = UUID.fromString(o);
                            } catch (IllegalArgumentException e) {
                                if (Settings.UUID.FORCE_LOWERCASE) {
                                    user = UUID.nameUUIDFromBytes(("OfflinePlayer:" + o.toLowerCase()).getBytes(Charsets.UTF_8));
                                } else {
                                    user = UUID.nameUUIDFromBytes(("OfflinePlayer:" + o).getBytes(Charsets.UTF_8));
                                }
                            }
                            uuids.put(o, user);
                        }
                        long time;
                        try {
                            Timestamp timestamp = resultSet.getTimestamp("timestamp");
                            time = timestamp.getTime();
                        } catch (SQLException exception) {
                            String parsable = resultSet.getString("timestamp");
                            try {
                                time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(parsable).getTime();
                            } catch (ParseException e) {
                                PS.debug("Could not parse date for plot: #" + id + "(" + areaid + ";" + plot_id + ") (" + parsable + ")");
                                time = System.currentTimeMillis() + id;
                            }
                        }
                        Plot p = new Plot(plot_id, user, new HashSet<UUID>(), new HashSet<UUID>(), new HashSet<UUID>(), "", null, null, null, new boolean[]{false, false, false, false}, time, id);
                        HashMap<PlotId, Plot> map = newPlots.get(areaid);
                        if (map != null) {
                            Plot last = map.put(p.getId(), p);
                            if (last != null) {
                                if (Settings.Enabled_Components.DATABASE_PURGER) {
                                    toDelete.add(last.temp);
                                } else {
                                    PS.debug("&cPLOT #" + id + "(" + last + ") in `" + this.prefix
                                            + "plot` is a duplicate. Delete this plot or set `database-purger: true` in the settings.yml.");
                                }
                            }
                        } else {
                            map = new HashMap<>();
                            newPlots.put(areaid, map);
                            map.put(p.getId(), p);
                        }
                        plots.put(id, p);
                    }
                    deleteRows(toDelete, this.prefix + "plot", "id");
                }
                if (Settings.Enabled_Components.RATING_CACHE) {
                    try (ResultSet r = statement.executeQuery("SELECT `plot_plot_id`, `player`, `rating` FROM `" + this.prefix + "plot_rating`")) {
                        ArrayList<Integer> toDelete = new ArrayList<>();
                        while (r.next()) {
                            id = r.getInt("plot_plot_id");
                            o = r.getString("player");
                            user = uuids.get(o);
                            if (user == null) {
                                user = UUID.fromString(o);
                                uuids.put(o, user);
                            }
                            Plot plot = plots.get(id);
                            if (plot != null) {
                                plot.getSettings().getRatings().put(user, r.getInt("rating"));
                            } else if (Settings.Enabled_Components.DATABASE_PURGER) {
                                toDelete.add(id);
                            } else {
                                PS.debug("&cENTRY #" + id + "(" + plot + ") in `plot_rating` does not exist. Create this plot or set `database-purger: true` in the "
                                        + "settings.yml.");
                            }
                        }
                        deleteRows(toDelete, this.prefix + "plot_rating", "plot_plot_id");
                    }
                }

            /*
             * Getting helpers
             */
                try (ResultSet r = statement.executeQuery("SELECT `user_uuid`, `plot_plot_id` FROM `" + this.prefix + "plot_helpers`")) {
                    ArrayList<Integer> toDelete = new ArrayList<>();
                    while (r.next()) {
                        id = r.getInt("plot_plot_id");
                        o = r.getString("user_uuid");
                        user = uuids.get(o);
                        if (user == null) {
                            user = UUID.fromString(o);
                            uuids.put(o, user);
                        }
                        Plot plot = plots.get(id);
                        if (plot != null) {
                            plot.getTrusted().add(user);
                        } else if (Settings.Enabled_Components.DATABASE_PURGER) {
                            toDelete.add(id);
                        } else {
                            PS.debug("&cENTRY #" + id + "(" + plot + ") in `plot_helpers` does not exist. Create this plot or set `database-purger: true` in the settings"
                                    + ".yml.");
                        }
                    }
                    deleteRows(toDelete, this.prefix + "plot_helpers", "plot_plot_id");
                }

            /*
             * Getting trusted
             */
                try (ResultSet r = statement.executeQuery("SELECT `user_uuid`, `plot_plot_id` FROM `" + this.prefix + "plot_trusted`")) {
                    ArrayList<Integer> toDelete = new ArrayList<>();
                    while (r.next()) {
                        id = r.getInt("plot_plot_id");
                        o = r.getString("user_uuid");
                        user = uuids.get(o);
                        if (user == null) {
                            user = UUID.fromString(o);
                            uuids.put(o, user);
                        }
                        Plot plot = plots.get(id);
                        if (plot != null) {
                            plot.getMembers().add(user);
                        } else if (Settings.Enabled_Components.DATABASE_PURGER) {
                            toDelete.add(id);
                        } else {
                            PS.debug("&cENTRY #" + id + "(" + plot + ") in `plot_trusted` does not exist. Create this plot or set `database-purger: true` in the settings"
                                    + ".yml.");
                        }
                    }
                    deleteRows(toDelete, this.prefix + "plot_trusted", "plot_plot_id");
                }

            /*
             * Getting denied
             */
                try (ResultSet r = statement.executeQuery("SELECT `user_uuid`, `plot_plot_id` FROM `" + this.prefix + "plot_denied`")) {
                    ArrayList<Integer> toDelete = new ArrayList<>();
                    while (r.next()) {
                        id = r.getInt("plot_plot_id");
                        o = r.getString("user_uuid");
                        user = uuids.get(o);
                        if (user == null) {
                            user = UUID.fromString(o);
                            uuids.put(o, user);
                        }
                        Plot plot = plots.get(id);
                        if (plot != null) {
                            plot.getDenied().add(user);
                        } else if (Settings.Enabled_Components.DATABASE_PURGER) {
                            toDelete.add(id);
                        } else {
                            PS.debug("&cENTRY " + id
                                    + " in `plot_denied` does not exist. Create this plot or set `database-purger: true` in the settings.yml.");
                        }
                    }
                    deleteRows(toDelete, this.prefix + "plot_denied", "plot_plot_id");
                }

                try (ResultSet resultSet = statement.executeQuery("SELECT * FROM `" + this.prefix + "plot_settings`")) {
                    ArrayList<Integer> toDelete = new ArrayList<>();
                    while (resultSet.next()) {
                        id = resultSet.getInt("plot_plot_id");
                        Plot plot = plots.get(id);
                        if (plot != null) {
                            plots.remove(id);
                            String alias = resultSet.getString("alias");
                            if (alias != null) {
                                plot.getSettings().setAlias(alias);
                            }
                            String pos = resultSet.getString("position");
                            switch (pos.toLowerCase()) {
                                case "":
                                case "default":
                                case "0,0,0":
                                case "center":
                                    break;
                                default:
                                    try {
                                        plot.getSettings().setPosition(BlockLoc.fromString(pos));
                                    } catch (Exception ignored) {}
                            }
                            Integer m = resultSet.getInt("merged");
                            boolean[] merged = new boolean[4];
                            for (int i = 0; i < 4; i++) {
                                merged[3 - i] = (m & 1 << i) != 0;
                            }
                            plot.getSettings().setMerged(merged);
                            String[] flags_string;
                            String myflags = resultSet.getString("flags");
                            if (myflags == null || myflags.isEmpty()) {
                                flags_string = new String[]{};
                            } else {
                                flags_string = myflags.split(",");
                            }
                            HashMap<Flag<?>, Object> flags = new HashMap<>();
                            boolean exception = false;
                            for (String element : flags_string) {
                                if (element.contains(":")) {
                                    String[] split = element.split(":");
                                    try {
                                        String flag_str = split[1].replaceAll("¯", ":").replaceAll("\u00B4", ",");
                                        Flag<?> flag = FlagManager.getOrCreateFlag(split[0]);
                                        flags.put(flag, flag.parseValue(flag_str));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        exception = true;
                                    }
                                } else {
                                    element = element.replaceAll("\u00AF", ":").replaceAll("\u00B4", ",");
                                    if (StringMan.isAlpha(element.replaceAll("_", "").replaceAll("-", ""))) {
                                        Flag flag = FlagManager.getOrCreateFlag(element);
                                        if (flag == null) {
                                            flag = new StringFlag(element) {
                                                @Override public String getValueDescription() {
                                                    return "Generic Filler Flag";
                                                }
                                            };
                                        }
                                        flags.put(flag, flag.parseValue(""));
                                    } else {
                                        PS.debug("INVALID FLAG: " + element);
                                    }
                                }
                            }
                            if (exception) {
                                PS.debug("&cPlot #" + id + "(" + plot + ") | " + plot + " had an invalid flag. A fix has been attempted.");
                                PS.debug("&c" + myflags);
                                this.setFlags(plot, flags);
                            }
                            plot.getSettings().flags = flags;
                        } else if (Settings.Enabled_Components.DATABASE_PURGER) {
                            toDelete.add(id);
                        } else {
                            PS.debug(
                                    "&cENTRY #" + id + "(" + plot + ") in `plot_settings` does not exist. Create this plot or set `database-purger: true` in the settings"
                                            + ".yml.");
                        }
                    }
                    deleteRows(toDelete, this.prefix + "plot_settings", "plot_plot_id");
                }
            }
            if (!plots.entrySet().isEmpty()) {
                createEmptySettings(new ArrayList<>(plots.keySet()), null);
                for (Entry<Integer, Plot> entry : plots.entrySet()) {
                    entry.getValue().getSettings();
                }
            }
            boolean invalidPlot = false;
            for (Entry<String, AtomicInteger> entry : noExist.entrySet()) {
                String worldName = entry.getKey();
                invalidPlot = true;
                PS.debug("&c[WARNING] Found " + entry.getValue().intValue() + " plots in DB for non existent world; '" + worldName + "'.");
            }
            if (invalidPlot) {
                PS.debug("&c[WARNING] - Please create the world/s or remove the plots using the purge command");
            }
        } catch (SQLException e) {
            PS.debug("&7[WARN] Failed to load plots.");
            e.printStackTrace();
        }
        return newPlots;
    }

    @Override
    public void setMerged(final Plot plot, final boolean[] merged) {
        plot.getSettings().setMerged(merged);
        addPlotTask(plot, new UniqueStatement("setMerged") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                int hash = MainUtil.hash(merged);
                stmt.setInt(1, hash);
                stmt.setInt(2, getId(plot));
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot_settings` SET `merged` = ? WHERE `plot_plot_id` = ?");
            }
        });
    }

    @Override
    public void swapPlots(Plot plot1, Plot plot2) {
        final int id1 = getId(plot1);
        final int id2 = getId(plot2);
        final PlotId pos1 = plot1.getId();
        final PlotId pos2 = plot2.getId();
        addPlotTask(plot1, new UniqueStatement("swapPlots") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, pos2.x);
                stmt.setInt(2, pos2.y);
                stmt.setInt(3, id1);
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot` SET `plot_id_x` = ?, `plot_id_z` = ? WHERE `id` = ?");
            }
        });
        addPlotTask(plot2, new UniqueStatement("swapPlots") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, pos1.x);
                stmt.setInt(2, pos1.y);
                stmt.setInt(3, id2);
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot` SET `plot_id_x` = ?, `plot_id_z` = ? WHERE `id` = ?");
            }
        });
    }

    @Override
    public void movePlot(final Plot original, final Plot newPlot) {
        addPlotTask(original, new UniqueStatement("movePlot") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, newPlot.getId().x);
                stmt.setInt(2, newPlot.getId().y);
                stmt.setString(3, newPlot.getArea().toString());
                stmt.setInt(4, getId(original));
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement(
                        "UPDATE `" + SQLManager.this.prefix + "plot` SET `plot_id_x` = ?, `plot_id_z` = ?, `world` = ? WHERE `id` = ?");
            }
        });
        addPlotTask(newPlot, null);
    }

    @Override
    public void setFlags(final Plot plot, HashMap<Flag<?>, Object> flags) {
        final String flag_string = FlagManager.toString(flags);
        addPlotTask(plot, new UniqueStatement("setFlags") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setString(1, flag_string);
                stmt.setInt(2, getId(plot));
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot_settings` SET `flags` = ? WHERE `plot_plot_id` = ?");
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
                return SQLManager.this.connection
                        .prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot_settings` SET `alias` = ?  WHERE `plot_plot_id` = ?");
            }
        });
    }

    /**
     * Purge all plots with the following database IDs
     */
    @Override
    public void purgeIds(final Set<Integer> uniqueIds) {
        addGlobalTask(new Runnable() {
            @Override
            public void run() {
                if (!uniqueIds.isEmpty()) {
                    try {
                        ArrayList<Integer> uniqueIdsList = new ArrayList<Integer>(uniqueIds);
                        String stmt_prefix = "";
                        int size = uniqueIdsList.size();
                        int packet = 990;
                        int amount = size / packet;
                        int count = 0;
                        int last = -1;
                        for (int j = 0; j <= amount; j++) {
                            PS.debug("Purging " + (j * packet) + " / " + size);
                            List<Integer> subList = uniqueIdsList.subList(j * packet, Math.min(size, (j + 1) * packet));
                            if (subList.isEmpty()) {
                                break;
                            }
                            StringBuilder idstr2 = new StringBuilder("");
                            stmt_prefix = "";
                            for (Integer id : subList) {
                                idstr2.append(stmt_prefix).append(id);
                                stmt_prefix = " OR `id` = ";
                            }
                            stmt_prefix = "";
                            StringBuilder idstr = new StringBuilder();
                            for (Integer id : subList) {
                                idstr.append(stmt_prefix).append(id);
                                stmt_prefix = " OR `plot_plot_id` = ";
                            }
                            PreparedStatement stmt = SQLManager.this.connection
                                    .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_helpers` WHERE `plot_plot_id` = " + idstr);
                            stmt.executeUpdate();
                            stmt.close();
                            stmt = SQLManager.this.connection
                                    .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_denied` WHERE `plot_plot_id` = " + idstr);
                            stmt.executeUpdate();
                            stmt.close();
                            stmt = SQLManager.this.connection
                                    .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_settings` WHERE `plot_plot_id` = " + idstr);
                            stmt.executeUpdate();
                            stmt.close();
                            stmt = SQLManager.this.connection
                                    .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_trusted` WHERE `plot_plot_id` = " + idstr);
                            stmt.executeUpdate();
                            stmt.close();
                            stmt = SQLManager.this.connection
                                    .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot` WHERE `id` = " + idstr2);
                            stmt.executeUpdate();
                            stmt.close();
                            commit();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        PS.debug("&c[ERROR] FAILED TO PURGE PLOTS!");
                        return;
                    }
                }
                PS.debug("&6[INFO] SUCCESSFULLY PURGED " + uniqueIds.size() + " PLOTS!");
            }
        });
    }

    @Override
    public void purge(final PlotArea area, final Set<PlotId> plots) {
        addGlobalTask(new Runnable() {
            @Override
            public void run() {
                try (PreparedStatement stmt = SQLManager.this.connection
                        .prepareStatement("SELECT `id`, `plot_id_x`, `plot_id_z` FROM `" + SQLManager.this.prefix + "plot` WHERE `world` = ?")) {
                    stmt.setString(1, area.toString());
                    Set<Integer> ids;
                    try (ResultSet r = stmt.executeQuery()) {
                        ids = new HashSet<>();
                        while (r.next()) {
                            PlotId plot_id = new PlotId(r.getInt("plot_id_x"), r.getInt("plot_id_z"));
                            if (plots.contains(plot_id)) {
                                ids.add(r.getInt("id"));
                            }
                        }
                    }
                    purgeIds(ids);
                } catch (SQLException e) {
                    e.printStackTrace();
                    PS.debug("&c[ERROR] FAILED TO PURGE AREA '" + area + "'!");
                }
                for (Iterator<PlotId> iterator = plots.iterator(); iterator.hasNext(); ) {
                    PlotId plotId = iterator.next();
                    iterator.remove();
                    PlotId id = new PlotId(plotId.x, plotId.y);
                    area.removePlot(id);
                }
            }
        });
    }

    @Override
    public void setPosition(final Plot plot, final String position) {
        addPlotTask(plot, new UniqueStatement("setPosition") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setString(1, position == null ? "" : position);
                stmt.setInt(2, getId(plot));
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot_settings` SET `position` = ?  WHERE `plot_plot_id` = ?");
            }
        });
    }

    @Override
    public void removeComment(final Plot plot, final PlotComment comment) {
        addPlotTask(plot, new UniqueStatement("removeComment") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                if (plot != null) {
                    statement.setString(1, plot.getArea().toString());
                    statement.setInt(2, plot.getId().hashCode());
                    statement.setString(3, comment.comment);
                    statement.setString(4, comment.inbox);
                    statement.setString(5, comment.senderName);
                } else {
                    statement.setString(1, comment.comment);
                    statement.setString(2, comment.inbox);
                    statement.setString(3, comment.senderName);
                }
            }

            @Override
            public PreparedStatement get() throws SQLException {
                if (plot != null) {
                    return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix
                            + "plot_comments` WHERE `world` = ? AND `hashcode` = ? AND `comment` = ? AND `inbox` = ? AND `sender` = ?");
                }
                return SQLManager.this.connection.prepareStatement(
                        "DELETE FROM `" + SQLManager.this.prefix + "plot_comments` WHERE `comment` = ? AND `inbox` = ? AND `sender` = ?");
            }
        });
    }

    @Override
    public void clearInbox(final Plot plot, final String inbox) {
        addPlotTask(plot, new UniqueStatement("clearInbox") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                if (plot != null) {
                    statement.setString(1, plot.getArea().toString());
                    statement.setInt(2, plot.getId().hashCode());
                    statement.setString(3, inbox);
                } else {
                    statement.setString(1, inbox);
                }
            }

            @Override
            public PreparedStatement get() throws SQLException {
                if (plot != null) {
                    return SQLManager.this.connection.prepareStatement(
                            "DELETE FROM `" + SQLManager.this.prefix + "plot_comments` WHERE `world` = ? AND `hashcode` = ? AND `inbox` = ?");
                }
                return SQLManager.this.connection.prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_comments` `inbox` = ?");
            }
        });
    }

    @Override
    public void getComments(final Plot plot, final String inbox, final RunnableVal<List<PlotComment>> whenDone) {
        addPlotTask(plot, new UniqueStatement("getComments_" + plot) {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                if (plot != null) {
                    statement.setString(1, plot.getArea().toString());
                    statement.setInt(2, plot.getId().hashCode());
                    statement.setString(3, inbox);
                } else {
                    statement.setString(1, inbox);
                }
            }

            @Override
            public PreparedStatement get() throws SQLException {
                if (plot != null) {
                    return SQLManager.this.connection.prepareStatement(
                            "SELECT * FROM `" + SQLManager.this.prefix + "plot_comments` WHERE `world` = ? AND `hashcode` = ? AND `inbox` = ?");
                }
                return SQLManager.this.connection.prepareStatement("SELECT * FROM `" + SQLManager.this.prefix + "plot_comments` WHERE `inbox` = ?");
            }

            @Override
            public void execute(PreparedStatement statement) {}

            @Override
            public void addBatch(PreparedStatement statement) throws SQLException {
                ArrayList<PlotComment> comments = new ArrayList<>();
                try (ResultSet set = statement.executeQuery()) {
                    while (set.next()) {
                        String sender = set.getString("sender");
                        String world = set.getString("world");
                        int hash = set.getInt("hashcode");
                        PlotId id;
                        if (hash != 0) {
                            id = PlotId.unpair(hash);
                        } else {
                            id = null;
                        }
                        String msg = set.getString("comment");
                        long timestamp = set.getInt("timestamp") * 1000;
                        PlotComment comment = new PlotComment(world, id, msg, sender, inbox, timestamp);
                        comments.add(comment);
                        whenDone.value = comments;
                    }
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
                statement.setString(1, plot.getArea().toString());
                statement.setInt(2, plot.getId().hashCode());
                statement.setString(3, comment.comment);
                statement.setString(4, comment.inbox);
                statement.setInt(5, (int) (comment.timestamp / 1000));
                statement.setString(6, comment.senderName);
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("INSERT INTO `" + SQLManager.this.prefix
                        + "plot_comments` (`world`, `hashcode`, `comment`, `inbox`, `timestamp`, `sender`) VALUES(?,?,?,?,?,?)");
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
                return SQLManager.this.connection
                        .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_helpers` WHERE `plot_plot_id` = ? AND `user_uuid` = ?");
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
                return SQLManager.this.connection
                        .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_trusted` WHERE `plot_plot_id` = ? AND `user_uuid` = ?");
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
                return SQLManager.this.connection
                        .prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_helpers` (`plot_plot_id`, `user_uuid`) VALUES(?,?)");
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
                return SQLManager.this.connection
                        .prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_trusted` (`plot_plot_id`, `user_uuid`) VALUES(?,?)");
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
                return SQLManager.this.connection
                        .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "plot_denied` WHERE `plot_plot_id` = ? AND `user_uuid` = ?");
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
                return SQLManager.this.connection
                        .prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "plot_denied` (`plot_plot_id`, `user_uuid`) VALUES(?,?)");
            }
        });
    }

    @Override
    public HashMap<UUID, Integer> getRatings(Plot plot) {
        HashMap<UUID, Integer> map = new HashMap<>();
        try (PreparedStatement statement = this.connection
                .prepareStatement("SELECT `rating`, `player` FROM `" + this.prefix + "plot_rating` WHERE `plot_plot_id` = ? ")) {
            statement.setInt(1, getId(plot));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    UUID uuid = UUID.fromString(resultSet.getString("player"));
                    int rating = resultSet.getInt("rating");
                    map.put(uuid, rating);
                }
            }
        } catch (SQLException e) {
            PS.debug("&7[WARN] Failed to fetch rating for plot " + plot.getId().toString());
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
                return SQLManager.this.connection.prepareStatement(
                        "INSERT INTO `" + SQLManager.this.prefix + "plot_rating` (`plot_plot_id`, `rating`, `player`) VALUES(?,?,?)");
            }
        });
    }

    @Override
    public void delete(PlotCluster cluster) {
        final int id = getClusterId(cluster);
        addClusterTask(cluster, new UniqueStatement("delete_cluster_settings") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, id);
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "cluster_settings` WHERE `cluster_id` = ?");
            }
        });
        addClusterTask(cluster, new UniqueStatement("delete_cluster_helpers") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, id);
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "cluster_helpers` WHERE `cluster_id` = ?");
            }
        });
        addClusterTask(cluster, new UniqueStatement("delete_cluster_invited") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, id);
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "cluster_invited` WHERE `cluster_id` = ?");
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
    public void addPersistentMeta(final UUID uuid, final String key, final byte[] meta, final boolean replace) {
        addPlayerTask(uuid, new UniqueStatement("addPersistentMeta") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                if (replace) {
                    stmt.setBytes(1, meta);
                    stmt.setString(2, uuid.toString());
                    stmt.setString(3, key);
                } else {
                    stmt.setString(1, uuid.toString());
                    stmt.setString(2, key);
                    stmt.setBytes(3, meta);
                }
            }

            @Override
            public PreparedStatement get() throws SQLException {
                if (replace) {
                    return SQLManager.this.connection
                            .prepareStatement("UPDATE `" + SQLManager.this.prefix + "player_meta` SET `value` = ? WHERE `uuid` = ? AND `key` = ?");
                } else {
                    return SQLManager.this.connection
                            .prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "player_meta`(`uuid`, `key`, `value`) VALUES(?, ? ,?)");
                }
            }
        });
    }

    @Override
    public void removePersistentMeta(final UUID uuid, final String key) {
        addPlayerTask(uuid, new UniqueStatement("removePersistentMeta") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, key);
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "player_meta` WHERE `uuid` = ? AND `key` = ?");
            }
        });
    }

    @Override
    public void getPersistentMeta(final UUID uuid, final RunnableVal<Map<String, byte[]>> result) {
        addPlayerTask(uuid, new UniqueStatement("getPersistentMeta") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setString(1, uuid.toString());
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement("SELECT * FROM `" + SQLManager.this.prefix + "player_meta` WHERE `uuid` = ? ORDER BY `meta_id` ASC");
            }

            @Override
            public void execute(PreparedStatement statement) {}

            @Override
            public void addBatch(PreparedStatement statement) throws SQLException {
                ResultSet resultSet = statement.executeQuery();

                final Map<String, byte[]> metaMap = new HashMap<>();

                while (resultSet.next()) {
                    String key = resultSet.getString("key");
                    byte[] bytes = resultSet.getBytes("value");
                    metaMap.put(key, bytes);
                }

                resultSet.close();
                TaskManager.runTaskAsync(new Runnable() {
                    @Override
                    public void run() {
                        result.run(metaMap);
                    }
                });
            }

        });
    }

    @Override
    public HashMap<String, Set<PlotCluster>> getClusters() {
        LinkedHashMap<String, Set<PlotCluster>> newClusters = new LinkedHashMap<>();
        HashMap<Integer, PlotCluster> clusters = new HashMap<>();
        try {
            HashSet<String> areas = new HashSet<>();
            if (PS.get().worlds.contains("worlds")) {
                ConfigurationSection worldSection = PS.get().worlds.getConfigurationSection("worlds");
                if (worldSection != null) {
                    for (String worldKey : worldSection.getKeys(false)) {
                        areas.add(worldKey);
                        ConfigurationSection areaSection = worldSection.getConfigurationSection(worldKey + ".areas");
                        if (areaSection != null) {
                            for (String areaKey : areaSection.getKeys(false)) {
                                String[] split = areaKey.split("(?<![;])-");
                                if (split.length == 3) {
                                    areas.add(worldKey + ';' + split[0]);
                                }
                            }
                        }
                    }
                }
            }
            HashMap<String, UUID> uuids = new HashMap<>();
            HashMap<String, Integer> noExist = new HashMap<>();
            /*
             * Getting clusters
             */
            try (Statement stmt = this.connection.createStatement()) {
                ResultSet resultSet = stmt.executeQuery("SELECT * FROM `" + this.prefix + "cluster`");
                PlotCluster cluster;
                String owner;
                UUID user;
                int id;
                while (resultSet.next()) {
                    PlotId pos1 = new PlotId(resultSet.getInt("pos1_x"), resultSet.getInt("pos1_z"));
                    PlotId pos2 = new PlotId(resultSet.getInt("pos2_x"), resultSet.getInt("pos2_z"));
                    id = resultSet.getInt("id");
                    String areaid = resultSet.getString("world");
                    if (!areas.contains(areaid)) {
                        if (noExist.containsKey(areaid)) {
                            noExist.put(areaid, noExist.get(areaid) + 1);
                        } else {
                            noExist.put(areaid, 1);
                        }
                    }
                    owner = resultSet.getString("owner");
                    user = uuids.get(owner);
                    if (user == null) {
                        user = UUID.fromString(owner);
                        uuids.put(owner, user);
                    }
                    cluster = new PlotCluster(null, pos1, pos2, user, id);
                    clusters.put(id, cluster);
                    Set<PlotCluster> set = newClusters.get(areaid);
                    if (set == null) {
                        set = new HashSet<>();
                        newClusters.put(areaid, set);
                    }
                    set.add(cluster);
                }
                //Getting helpers
                resultSet = stmt.executeQuery("SELECT `user_uuid`, `cluster_id` FROM `" + this.prefix + "cluster_helpers`");
                while (resultSet.next()) {
                    id = resultSet.getInt("cluster_id");
                    owner = resultSet.getString("user_uuid");
                    user = uuids.get(owner);
                    if (user == null) {
                        user = UUID.fromString(owner);
                        uuids.put(owner, user);
                    }
                    cluster = clusters.get(id);
                    if (cluster != null) {
                        cluster.helpers.add(user);
                    } else {
                        PS.debug("&cCluster #" + id + "(" + cluster + ") in cluster_helpers does not exist. Please create the cluster or remove this entry.");
                    }
                }
                // Getting invited
                resultSet = stmt.executeQuery("SELECT `user_uuid`, `cluster_id` FROM `" + this.prefix + "cluster_invited`");
                while (resultSet.next()) {
                    id = resultSet.getInt("cluster_id");
                    owner = resultSet.getString("user_uuid");
                    user = uuids.get(owner);
                    if (user == null) {
                        user = UUID.fromString(owner);
                        uuids.put(owner, user);
                    }
                    cluster = clusters.get(id);
                    if (cluster != null) {
                        cluster.invited.add(user);
                    } else {
                        PS.debug("&cCluster #" + id + "(" + cluster + ") in cluster_invited does not exist. Please create the cluster or remove this entry.");
                    }
                }
                resultSet = stmt.executeQuery("SELECT * FROM `" + this.prefix + "cluster_settings`");
                while (resultSet.next()) {
                    id = resultSet.getInt("cluster_id");
                    cluster = clusters.get(id);
                    if (cluster != null) {
                        String alias = resultSet.getString("alias");
                        if (alias != null) {
                            cluster.settings.setAlias(alias);
                        }
                        String pos = resultSet.getString("position");
                        switch (pos.toLowerCase()) {
                            case "":
                            case "default":
                            case "0,0,0":
                            case "center":
                                break;
                            default:
                                try {
                                    BlockLoc loc = BlockLoc.fromString(pos);
                                    cluster.settings.setPosition(loc);
                                } catch (Exception ignored) {}
                        }
                        Integer m = resultSet.getInt("merged");
                        boolean[] merged = new boolean[4];
                        for (int i = 0; i < 4; i++) {
                            merged[3 - i] = (m & 1 << i) != 0;
                        }
                        cluster.settings.setMerged(merged);
                        String[] flags_string;
                        String myflags = resultSet.getString("flags");
                        if (myflags == null || myflags.isEmpty()) {
                            flags_string = new String[]{};
                        } else {
                            flags_string = myflags.split(",");
                        }
                        HashMap<Flag<?>, Object> flags = new HashMap<>();
                        for (String element : flags_string) {
                            if (element.contains(":")) {
                                String[] split = element.split(":");
                                String flag_str = split[1].replaceAll("\u00AF", ":").replaceAll("´", ",");
                                Flag flag = FlagManager.getOrCreateFlag(split[0]);
                                if (flag == null) {
                                    flag = new StringFlag(split[0]) {
                                        @Override public String getValueDescription() {
                                            return "Generic Filler Flag";
                                        }
                                    };
                                }
                                flags.put(flag, flag.parseValue(flag_str));
                            } else {
                                Flag flag = FlagManager.getOrCreateFlag(element);
                                if (flag == null) {
                                    flag = new StringFlag(element) {
                                        @Override public String getValueDescription() {
                                            return "Generic Filler Flag";
                                        }
                                    };
                                }
                                flags.put(flag, flag.parseValue(""));
                            }
                        }
                        cluster.settings.flags = flags;
                    } else {
                        PS.debug("&cCluster #" + id + "(" + cluster + ") in cluster_settings does not exist. Please create the cluster or remove this entry.");
                    }
                }
                resultSet.close();
            }
            boolean invalidPlot = false;
            for (Entry<String, Integer> entry : noExist.entrySet()) {
                String a = entry.getKey();
                invalidPlot = true;
                PS.debug("&c[WARNING] Found " + noExist.get(a) + " clusters in DB for non existent area; '" + a + "'.");
            }
            if (invalidPlot) {
                PS.debug("&c[WARNING] - Please create the world/s or remove the clusters using the purge command");
            }
        } catch (SQLException e) {
            PS.debug("&7[WARN] Failed to load clusters.");
            e.printStackTrace();
        }
        return newClusters;
    }

    @Override
    public void setFlags(final PlotCluster cluster, HashMap<Flag<?>, Object> flags) {
        final StringBuilder flag_string = new StringBuilder();
        int i = 0;
        for (Entry<Flag<?>, Object> flag : flags.entrySet()) {
            if (i != 0) {
                flag_string.append(',');
            }
            flag_string.append(flag.getKey().getName()).append(':')
                    .append(flag.getKey().valueToString(flag.getValue()).replaceAll(":", "\u00AF").replaceAll(",", "´"));
            i++;
        }
        addClusterTask(cluster, new UniqueStatement("setFlags") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setString(1, flag_string.toString());
                stmt.setInt(2, getClusterId(cluster));
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("UPDATE `" + SQLManager.this.prefix + "cluster_settings` SET `flags` = ? WHERE `cluster_id` = ?");
            }
        });
    }

    @Override
    public void setClusterName(final PlotCluster cluster, final String name) {
        addClusterTask(cluster, new UniqueStatement("setClusterName") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setString(1, name);
                stmt.setInt(2, getClusterId(cluster));
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("UPDATE `" + SQLManager.this.prefix + "cluster_settings` SET `alias` = ?  WHERE `cluster_id` = ?");
            }
        });
        cluster.settings.setAlias(name);
    }

    @Override
    public void removeHelper(final PlotCluster cluster, final UUID uuid) {
        addClusterTask(cluster, new UniqueStatement("removeHelper") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setInt(1, getClusterId(cluster));
                statement.setString(2, uuid.toString());
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "cluster_helpers` WHERE `cluster_id` = ? AND `user_uuid` = ?");
            }
        });
    }

    @Override
    public void setHelper(final PlotCluster cluster, final UUID uuid) {
        addClusterTask(cluster, new UniqueStatement("setHelper") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setInt(1, getClusterId(cluster));
                statement.setString(2, uuid.toString());
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "cluster_helpers` (`cluster_id`, `user_uuid`) VALUES(?,?)");
            }
        });
    }

    @Override
    public void createCluster(final PlotCluster cluster) {
        addClusterTask(cluster, new UniqueStatement("createCluster_" + cluster.hashCode()) {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, cluster.getP1().x);
                stmt.setInt(2, cluster.getP1().y);
                stmt.setInt(3, cluster.getP2().x);
                stmt.setInt(4, cluster.getP2().y);
                stmt.setString(5, cluster.owner.toString());
                stmt.setString(6, cluster.area.toString());
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement(SQLManager.this.CREATE_CLUSTER, Statement.RETURN_GENERATED_KEYS);
            }

            @Override
            public void execute(PreparedStatement statement) {}

            @Override
            public void addBatch(PreparedStatement statement) throws SQLException {
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        cluster.temp = keys.getInt(1);
                    }
                }
            }
        });
        addClusterTask(cluster, new UniqueStatement("createCluster_settings_" + cluster.hashCode()) {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, getClusterId(cluster));
                stmt.setString(2, cluster.settings.getAlias());
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "cluster_settings`(`cluster_id`, `alias`) VALUES(?, ?)");
            }
        });
    }

    @Override
    public void resizeCluster(final PlotCluster current, PlotId min, PlotId max) {
        final PlotId pos1 = new PlotId(current.getP1().x, current.getP1().y);
        final PlotId pos2 = new PlotId(current.getP2().x, current.getP2().y);
        current.setP1(min);
        current.setP2(max);

        addClusterTask(current, new UniqueStatement("resizeCluster") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setInt(1, pos1.x);
                stmt.setInt(2, pos1.y);
                stmt.setInt(3, pos2.x);
                stmt.setInt(4, pos2.y);
                stmt.setInt(5, getClusterId(current));
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection.prepareStatement(
                        "UPDATE `" + SQLManager.this.prefix + "cluster` SET `pos1_x` = ?, `pos1_z` = ?, `pos2_x` = ?, `pos2_z` = ?  WHERE `id` = ?");
            }
        });
    }

    @Override
    public void setPosition(final PlotCluster cluster, final String position) {
        addClusterTask(cluster, new UniqueStatement("setPosition") {
            @Override
            public void set(PreparedStatement stmt) throws SQLException {
                stmt.setString(1, position);
                stmt.setInt(2, getClusterId(cluster));
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("UPDATE `" + SQLManager.this.prefix + "cluster_settings` SET `position` = ?  WHERE `cluster_id` = ?");
            }
        });
    }

    @Override
    public void removeInvited(final PlotCluster cluster, final UUID uuid) {
        addClusterTask(cluster, new UniqueStatement("removeInvited") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setInt(1, getClusterId(cluster));
                statement.setString(2, uuid.toString());
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("DELETE FROM `" + SQLManager.this.prefix + "cluster_invited` WHERE `cluster_id` = ? AND `user_uuid` = ?");
            }
        });
    }

    @Override
    public void setInvited(final PlotCluster cluster, final UUID uuid) {
        addClusterTask(cluster, new UniqueStatement("setInvited") {
            @Override
            public void set(PreparedStatement statement) throws SQLException {
                statement.setInt(1, getClusterId(cluster));
                statement.setString(2, uuid.toString());
            }

            @Override
            public PreparedStatement get() throws SQLException {
                return SQLManager.this.connection
                        .prepareStatement("INSERT INTO `" + SQLManager.this.prefix + "cluster_invited` (`cluster_id`, `user_uuid`) VALUES(?,?)");
            }
        });
    }

    @Override
    public boolean deleteTables() {
        try (Statement stmt = this.connection.createStatement();
                PreparedStatement statement = this.connection.prepareStatement("DROP TABLE `" + this.prefix + "plot`")) {
            close();
            this.closed = false;
            SQLManager.this.connection = this.database.forceConnection();
            stmt.addBatch("DROP TABLE `" + this.prefix + "cluster_invited`");
            stmt.addBatch("DROP TABLE `" + this.prefix + "cluster_helpers`");
            stmt.addBatch("DROP TABLE `" + this.prefix + "cluster`");
            stmt.addBatch("DROP TABLE `" + this.prefix + "plot_rating`");
            stmt.addBatch("DROP TABLE `" + this.prefix + "plot_settings`");
            stmt.addBatch("DROP TABLE `" + this.prefix + "plot_comments`");
            stmt.addBatch("DROP TABLE `" + this.prefix + "plot_trusted`");
            stmt.addBatch("DROP TABLE `" + this.prefix + "plot_helpers`");
            stmt.addBatch("DROP TABLE `" + this.prefix + "plot_denied`");
            stmt.executeBatch();
            stmt.clearBatch();
            statement.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();

        }
        return true;
    }

    @Override
    public void validateAllPlots(Set<Plot> toValidate) {
        if (!isValid()) {
            reconnect();
        }
        PS.debug("$1All DB transactions during this session are being validated (This may take a while if corrections need to be made)");
        commit();
        while (true) {
            if (!sendBatch()) {
                break;
            }
        }
        try {
            if (this.connection.getAutoCommit()) {
                this.connection.setAutoCommit(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        HashMap<String, HashMap<PlotId, Plot>> database = getPlots();
        ArrayList<Plot> toCreate = new ArrayList<>();
        for (Plot plot : toValidate) {
            if (plot.temp == -1) {
                continue;
            }
            HashMap<PlotId, Plot> worldPlots = database.get(plot.getArea().toString());
            if (worldPlots == null) {
                PS.debug("&8 - &7Creating plot (1): " + plot);
                toCreate.add(plot);
                continue;
            }
            Plot dataPlot = worldPlots.remove(plot.getId());
            if (dataPlot == null) {
                PS.debug("&8 - &7Creating plot (2): " + plot);
                toCreate.add(plot);
                continue;
            }
            // owner
            if (!plot.owner.equals(dataPlot.owner)) {
                PS.debug("&8 - &7Setting owner: " + plot + " -> " + MainUtil.getName(plot.owner));
                setOwner(plot, plot.owner);
            }
            // trusted
            if (!plot.getTrusted().equals(dataPlot.getTrusted())) {
                HashSet<UUID> toAdd = (HashSet<UUID>) plot.getTrusted().clone();
                HashSet<UUID> toRemove = (HashSet<UUID>) dataPlot.getTrusted().clone();
                toRemove.removeAll(plot.getTrusted());
                toAdd.removeAll(dataPlot.getTrusted());
                PS.debug("&8 - &7Correcting " + (toAdd.size() + toRemove.size()) + " trusted for: " + plot);
                if (!toRemove.isEmpty()) {
                    for (UUID uuid : toRemove) {
                        removeTrusted(plot, uuid);
                    }
                }
                if (!toAdd.isEmpty()) {
                    for (UUID uuid : toAdd) {
                        setTrusted(plot, uuid);
                    }
                }
            }
            if (!plot.getMembers().equals(dataPlot.getMembers())) {
                HashSet<UUID> toAdd = (HashSet<UUID>) plot.getMembers().clone();
                HashSet<UUID> toRemove = (HashSet<UUID>) dataPlot.getMembers().clone();
                toRemove.removeAll(plot.getMembers());
                toAdd.removeAll(dataPlot.getMembers());
                PS.debug("&8 - &7Correcting " + (toAdd.size() + toRemove.size()) + " members for: " + plot);
                if (!toRemove.isEmpty()) {
                    for (UUID uuid : toRemove) {
                        removeMember(plot, uuid);
                    }
                }
                if (!toAdd.isEmpty()) {
                    for (UUID uuid : toAdd) {
                        setMember(plot, uuid);
                    }
                }
            }
            if (!plot.getDenied().equals(dataPlot.getDenied())) {
                HashSet<UUID> toAdd = (HashSet<UUID>) plot.getDenied().clone();
                HashSet<UUID> toRemove = (HashSet<UUID>) dataPlot.getDenied().clone();
                toRemove.removeAll(plot.getDenied());
                toAdd.removeAll(dataPlot.getDenied());
                PS.debug("&8 - &7Correcting " + (toAdd.size() + toRemove.size()) + " denied for: " + plot);
                if (!toRemove.isEmpty()) {
                    for (UUID uuid : toRemove) {
                        removeDenied(plot, uuid);
                    }
                }
                if (!toAdd.isEmpty()) {
                    for (UUID uuid : toAdd) {
                        setDenied(plot, uuid);
                    }
                }
            }
            boolean[] pm = plot.getMerged();
            boolean[] dm = dataPlot.getMerged();
            if (pm[0] != dm[0] || pm[1] != dm[1]) {
                PS.debug("&8 - &7Correcting merge for: " + plot);
                setMerged(dataPlot, plot.getMerged());
            }
            HashMap<Flag<?>, Object> pf = plot.getFlags();
            HashMap<Flag<?>, Object> df = dataPlot.getFlags();
            if (!pf.isEmpty() && !df.isEmpty()) {
                if (pf.size() != df.size() || !StringMan.isEqual(StringMan.joinOrdered(pf.values(), ","), StringMan.joinOrdered(df.values(), ","))) {
                    PS.debug("&8 - &7Correcting flags for: " + plot);
                    setFlags(plot, pf);
                }
            }
        }

        for (Entry<String, HashMap<PlotId, Plot>> entry : database.entrySet()) {
            HashMap<PlotId, Plot> map = entry.getValue();
            if (!map.isEmpty()) {
                for (Entry<PlotId, Plot> entry2 : map.entrySet()) {
                    PS.debug("$1Plot was deleted: " + entry2.getValue().toString() + "// TODO implement this when sure safe");
                }
            }
        }
        commit();
    }

    @Override
    public void replaceWorld(final String oldWorld, final String newWorld, final PlotId min, final PlotId max) {
        addGlobalTask(new Runnable() {
            @Override
            public void run() {
                if (min == null) {
                    try (PreparedStatement stmt = SQLManager.this.connection
                            .prepareStatement("UPDATE `" + SQLManager.this.prefix + "plot` SET `world` = ? WHERE `world` = ?")) {
                        stmt.setString(1, newWorld);
                        stmt.setString(2, oldWorld);
                        stmt.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    try (PreparedStatement stmt = SQLManager.this.connection
                            .prepareStatement("UPDATE `" + SQLManager.this.prefix + "cluster` SET `world` = ? WHERE `world` = ?")) {
                        stmt.setString(1, newWorld);
                        stmt.setString(2, oldWorld);
                        stmt.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    try (PreparedStatement stmt = SQLManager.this.connection.prepareStatement("UPDATE `"
                            + SQLManager.this.prefix
                            + "plot` SET `world` = ? WHERE `world` = ? AND `plot_id_x` BETWEEN ? AND ? AND `plot_id_z` BETWEEN ? AND ?")) {
                        stmt.setString(1, newWorld);
                        stmt.setString(2, oldWorld);
                        stmt.setInt(3, min.x);
                        stmt.setInt(4, max.x);
                        stmt.setInt(5, min.y);
                        stmt.setInt(6, max.y);
                        stmt.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    try (PreparedStatement stmt = SQLManager.this.connection.prepareStatement("UPDATE `"
                            + SQLManager.this.prefix
                            + "cluster` SET `world` = ? WHERE `world` = ? AND `pos1_x` <= ? AND `pos1_z` <= ? AND `pos2_x` >= ? AND `pos2_z` >= ?")) {
                        stmt.setString(1, newWorld);
                        stmt.setString(2, oldWorld);
                        stmt.setInt(3, max.x);
                        stmt.setInt(4, max.y);
                        stmt.setInt(5, min.x);
                        stmt.setInt(6, min.y);
                        stmt.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void replaceUUID(final UUID old, final UUID now) {
        addGlobalTask(new Runnable() {
            @Override
            public void run() {
                try (Statement stmt = SQLManager.this.connection.createStatement()) {
                    stmt.executeUpdate(
                            "UPDATE `" + SQLManager.this.prefix + "cluster` SET `owner` = '" + now.toString() + "' WHERE `owner` = '" + old.toString()
                                    + '\'');
                    stmt.executeUpdate(
                            "UPDATE `" + SQLManager.this.prefix + "cluster_helpers` SET `user_uuid` = '" + now.toString() + "' WHERE `user_uuid` = '"
                                    + old.toString()
                                    + '\'');
                    stmt.executeUpdate(
                            "UPDATE `" + SQLManager.this.prefix + "cluster_invited` SET `user_uuid` = '" + now.toString() + "' WHERE `user_uuid` = '"
                                    + old.toString()
                                    + '\'');
                    stmt.executeUpdate(
                            "UPDATE `" + SQLManager.this.prefix + "plot` SET `owner` = '" + now.toString() + "' WHERE `owner` = '" + old.toString()
                                    + '\'');
                    stmt.executeUpdate(
                            "UPDATE `" + SQLManager.this.prefix + "plot_denied` SET `user_uuid` = '" + now.toString() + "' WHERE `user_uuid` = '"
                                    + old.toString()
                                    + '\'');
                    stmt.executeUpdate(
                            "UPDATE `" + SQLManager.this.prefix + "plot_helpers` SET `user_uuid` = '" + now.toString() + "' WHERE `user_uuid` = '"
                                    + old.toString()
                                    + '\'');
                    stmt.executeUpdate(
                            "UPDATE `" + SQLManager.this.prefix + "plot_trusted` SET `user_uuid` = '" + now.toString() + "' WHERE `user_uuid` = '"
                                    + old.toString()
                                    + '\'');
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void close() {
        try {
            this.closed = true;
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public abstract class UniqueStatement {

        public final String method;

        public UniqueStatement(String method) {
            this.method = method;
        }

        public void addBatch(PreparedStatement statement) throws SQLException {
            statement.addBatch();
        }

        public void execute(PreparedStatement statement) throws SQLException {
            statement.executeBatch();
        }

        public abstract PreparedStatement get() throws SQLException;

        public abstract void set(PreparedStatement stmt) throws SQLException;
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
