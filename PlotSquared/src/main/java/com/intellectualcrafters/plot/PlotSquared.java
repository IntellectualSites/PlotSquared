package com.intellectualcrafters.plot;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import com.intellectualcrafters.plot.commands.Cluster;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.database.MySQL;
import com.intellectualcrafters.plot.database.SQLManager;
import com.intellectualcrafters.plot.database.SQLite;
import com.intellectualcrafters.plot.flag.AbstractFlag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.flag.FlagValue;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.ClusterManager;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.Logger;
import com.intellectualcrafters.plot.util.Logger.LogLevel;
import com.intellectualcrafters.plot.util.TaskManager;

public class PlotSquared {
    
    public static File styleFile;
    public static YamlConfiguration style;
    
    public static File configFile;
    public static YamlConfiguration config;
    
    public static File storageFile;
    public static YamlConfiguration storage;
    
    public static PlotSquared THIS = null; // This class
    public static IPlotMain IMP = null; // Specific implementation of PlotSquared
    public static String VERSION = null;
    public static TaskManager TASK = null;
    public static Economy economy = null;
    
    private final static HashMap<String, PlotWorld> plotworlds = new HashMap<>();
    private final static HashMap<String, PlotManager> plotmanagers = new HashMap<>();
    private static LinkedHashMap<String, HashMap<PlotId, Plot>> plots;
    
    private static MySQL mySQL;
    public static Connection connection;
    
    public static MySQL getMySQL() {
        return mySQL;
    }
    
    public static Connection getConnection() {
        return connection;
    }
    
    public PlotSquared(IPlotMain imp_class) {
        THIS = this;
        IMP = imp_class;
        VERSION = IMP.getVersion();
        
        C.setupTranslations();
        C.saveTranslations();
        
        if (getJavaVersion() < 1.7) {
            log(C.PREFIX.s() + "&cYour java version is outdated. Please update to at least 1.7.");
            // Didn't know of any other link :D
            log(C.PREFIX.s() + "&cURL: &6https://java.com/en/download/index.jsp");
            IMP.disable();
            return;
        }
        if (getJavaVersion() < 1.8) {
            log(C.PREFIX.s() + "&cIt's really recommended to run Java 1.8, as it increases performance");
        }
        
        TASK = IMP.getTaskManager();
        if (Settings.KILL_ROAD_MOBS) {
            IMP.runEntityTask();
        }
        if (C.ENABLED.s().length() > 0) {
            log(C.ENABLED.s());
        }
        
        setupConfigs();
        setupDefaultFlags();
        setupDatabase();
        
        // Events
        IMP.registerCommands();
        IMP.registerPlayerEvents();
        IMP.registerInventoryEvents();
        IMP.registerPlotPlusEvents();
        IMP.registerForceFieldEvents();
        IMP.registerWorldEditEvents();
        
        if (Settings.AUTO_CLEAR) {
            ExpireManager.runTask();
        }
        
        economy = IMP.getEconomy();
    }
    
    public void disable() {
        try {
            connection.close();
            mySQL.closeConnection();
        } catch (NullPointerException | SQLException e) {
            if (connection != null) {
                log("&cCould not close mysql connection!");
            }
        }
    }
    
    public static void log(String message) {
        IMP.log(message);
    }
    
    public void setupDatabase() {
        final String[] tables;
        if (Settings.ENABLE_CLUSTERS) {
            MainCommand.subCommands.add(new Cluster());
            tables = new String[]{"plot_trusted", "plot_ratings", "plot_comments", "cluster"};
        }
        else {
            tables = new String[]{"plot_trusted", "plot_ratings", "plot_comments"};
        }
        if (Settings.DB.USE_MYSQL) {
            try {
                mySQL = new MySQL(THIS, Settings.DB.HOST_NAME, Settings.DB.PORT, Settings.DB.DATABASE, Settings.DB.USER, Settings.DB.PASSWORD);
                connection = mySQL.openConnection();
                {
                    if (DBFunc.dbManager == null) {
                        DBFunc.dbManager = new SQLManager(connection, Settings.DB.PREFIX);
                    }
                    final DatabaseMetaData meta = connection.getMetaData();
                    ResultSet res = meta.getTables(null, null, Settings.DB.PREFIX + "plot", null);
                    if (!res.next()) {
                        DBFunc.createTables("mysql", true);
                    } else {
                        for (final String table : tables) {
                            res = meta.getTables(null, null, Settings.DB.PREFIX + table, null);
                            if (!res.next()) {
                                DBFunc.createTables("mysql", false);
                            }
                        }
                    }
                }
            } catch (final Exception e) {
                log("&c[Plots] MySQL is not setup correctly. The plugin will disable itself.");
                if ((config == null) || config.getBoolean("debug")) {
                    log("&d==== Here is an ugly stacktrace if you are interested in those things ====");
                    e.printStackTrace();
                    log("&d==== End of stacktrace ====");
                    log("&6Please go to the PlotSquared 'storage.yml' and configure MySQL correctly.");
                }
                IMP.disable();
                return;
            }
            plots = DBFunc.getPlots();
            if (Settings.ENABLE_CLUSTERS) {
                ClusterManager.clusters = DBFunc.getClusters();
            }
        }
        else if (Settings.DB.USE_MONGO) {
            // DBFunc.dbManager = new MongoManager();
            log(C.PREFIX.s() + "MongoDB is not yet implemented");
        }
        else if (Settings.DB.USE_SQLITE) {
            try {
                connection = new SQLite(THIS, IMP.getDirectory() + File.separator + Settings.DB.SQLITE_DB + ".db").openConnection();
                {
                    DBFunc.dbManager = new SQLManager(connection, Settings.DB.PREFIX);
                    final DatabaseMetaData meta = connection.getMetaData();
                    ResultSet res = meta.getTables(null, null, Settings.DB.PREFIX + "plot", null);
                    if (!res.next()) {
                        DBFunc.createTables("sqlite", true);
                    } else {
                        for (final String table : tables) {
                            res = meta.getTables(null, null, Settings.DB.PREFIX + table, null);
                            if (!res.next()) {
                                DBFunc.createTables("sqlite", false);
                            }
                        }
                    }
                }
            } catch (final Exception e) {
                log(C.PREFIX.s() + "&cFailed to open SQLite connection. The plugin will disable itself.");
                log("&9==== Here is an ugly stacktrace, if you are interested in those things ===");
                e.printStackTrace();
                IMP.disable();
                return;
            }
            plots = DBFunc.getPlots();
            if (Settings.ENABLE_CLUSTERS) {
                ClusterManager.clusters = DBFunc.getClusters();
            }
        } else {
            log(C.PREFIX + "&cNo storage type is set!");
            IMP.disable();
            return;
        }
    
        }
    
    public static void setupDefaultFlags() {
        final List<String> booleanFlags = Arrays.asList("notify-enter", "notify-leave", "item-drop", "invincible", "instabreak", "drop-protection", "forcefield", "titles", "pve", "pvp", "no-worldedit");
        final List<String> intervalFlags = Arrays.asList("feed", "heal");
        final List<String> stringFlags = Arrays.asList("greeting", "farewell");
        for (final String flag : stringFlags) {
            FlagManager.addFlag(new AbstractFlag(flag));
        }
        for (final String flag : intervalFlags) {
            FlagManager.addFlag(new AbstractFlag(flag, new FlagValue.IntervalValue()));
        }
        for (final String flag : booleanFlags) {
            FlagManager.addFlag(new AbstractFlag(flag, new FlagValue.BooleanValue()));
        }
        FlagManager.addFlag(new AbstractFlag("fly", new FlagValue.BooleanValue()));

        FlagManager.addFlag(new AbstractFlag("explosion", new FlagValue.BooleanValue()));

        FlagManager.addFlag(new AbstractFlag("hostile-interact", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("hostile-attack", new FlagValue.BooleanValue()));

        FlagManager.addFlag(new AbstractFlag("animal-interact", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("animal-attack", new FlagValue.BooleanValue()));

        FlagManager.addFlag(new AbstractFlag("tamed-interact", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("tamed-attack", new FlagValue.BooleanValue()));

        FlagManager.addFlag(new AbstractFlag("misc-interact", new FlagValue.BooleanValue()));

        FlagManager.addFlag(new AbstractFlag("hanging-place", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("hanging-break", new FlagValue.BooleanValue()));

        FlagManager.addFlag(new AbstractFlag("vehicle-use", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("vehicle-place", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("vehicle-break", new FlagValue.BooleanValue()));

        FlagManager.addFlag(new AbstractFlag("place", new FlagValue.PlotBlockListValue()));
        FlagManager.addFlag(new AbstractFlag("break", new FlagValue.PlotBlockListValue()));
        FlagManager.addFlag(new AbstractFlag("use", new FlagValue.PlotBlockListValue()));

        FlagManager.addFlag(new AbstractFlag("gamemode") {
            @Override
            public String parseValueRaw(final String value) {
                switch (value) {
                    case "creative":
                    case "c":
                    case "1":
                        return "creative";
                    case "survival":
                    case "s":
                    case "0":
                        return "survival";
                    case "adventure":
                    case "a":
                    case "2":
                        return "adventure";
                    default:
                        return null;
                }
            }

            @Override
            public String getValueDesc() {
                return "Flag value must be a gamemode: 'creative' , 'survival' or 'adventure'";
            }
        });

        FlagManager.addFlag(new AbstractFlag("price", new FlagValue.UnsignedDoubleValue()));

        FlagManager.addFlag(new AbstractFlag("time", new FlagValue.LongValue()));

        FlagManager.addFlag(new AbstractFlag("weather") {
            @Override
            public String parseValueRaw(final String value) {
                switch (value) {
                    case "rain":
                    case "storm":
                    case "on":
                        return "rain";
                    case "clear":
                    case "off":
                    case "sun":
                        return "clear";
                    default:
                        return null;
                }
            }

            @Override
            public String getValueDesc() {
                return "Flag value must be weather type: 'clear' or 'rain'";
            }
        });
    }
    
    public static void setupConfig() {
        config.set("version", VERSION);
        final Map<String, Object> options = new HashMap<>();
        options.put("teleport.delay", 0);
        options.put("auto_update", false);
        options.put("clusters.enabled", Settings.ENABLE_CLUSTERS);
        options.put("plotme-alias", Settings.USE_PLOTME_ALIAS);
        options.put("plotme-convert.enabled", Settings.CONVERT_PLOTME);
        options.put("claim.max-auto-area", Settings.MAX_AUTO_SIZE);
        options.put("UUID.offline", Settings.OFFLINE_MODE);
        options.put("kill_road_mobs", Settings.KILL_ROAD_MOBS_DEFAULT);
        options.put("mob_pathfinding", Settings.MOB_PATHFINDING_DEFAULT);
        options.put("console.color", Settings.CONSOLE_COLOR);
        options.put("metrics", true);
        options.put("debug", true);
        options.put("clear.auto.enabled", false);
        options.put("clear.auto.days", 365);
        options.put("clear.check-disk", Settings.AUTO_CLEAR_CHECK_DISK);
        options.put("clear.on.ban", false);
        options.put("max_plots", Settings.MAX_PLOTS);
        options.put("schematics.save_path", Settings.SCHEMATIC_SAVE_PATH);
        options.put("uuid.read-from-disk", Settings.UUID_FROM_DISK);
        options.put("titles", Settings.TITLES);
        options.put("teleport.on_login", Settings.TELEPORT_ON_LOGIN);
        options.put("worldedit.require-selection-in-mask", Settings.REQUIRE_SELECTION);

        for (final Entry<String, Object> node : options.entrySet()) {
            if (!config.contains(node.getKey())) {
                config.set(node.getKey(), node.getValue());
            }
        }
        Settings.ENABLE_CLUSTERS = config.getBoolean("clusters.enabled");
        Settings.DEBUG = config.getBoolean("debug");
        if (Settings.DEBUG) {
            log(C.PREFIX.s() + "&6Debug Mode Enabled (Default). Edit the config to turn this off.");
        }
        Settings.TELEPORT_DELAY = config.getInt("teleport.delay");
        Settings.CONSOLE_COLOR = config.getBoolean("console.color");
        Settings.TELEPORT_ON_LOGIN = config.getBoolean("teleport.on_login");
        Settings.USE_PLOTME_ALIAS = config.getBoolean("plotme-alias");
        Settings.CONVERT_PLOTME = config.getBoolean("plotme-convert.enabled");
        Settings.KILL_ROAD_MOBS = config.getBoolean("kill_road_mobs");
        Settings.MOB_PATHFINDING = config.getBoolean("mob_pathf"
                + "inding");
        Settings.METRICS = config.getBoolean("metrics");
        Settings.AUTO_CLEAR_DAYS = config.getInt("clear.auto.days");
        Settings.AUTO_CLEAR_CHECK_DISK = config.getBoolean("clear.check-disk");
        Settings.MAX_AUTO_SIZE = config.getInt("claim.max-auto-area");
        Settings.AUTO_CLEAR = config.getBoolean("clear.auto.enabled");
        Settings.TITLES = config.getBoolean("titles");
        Settings.MAX_PLOTS = config.getInt("max_plots");
        if (Settings.MAX_PLOTS > 32767) {
            log("&c`max_plots` Is set too high! This is a per player setting and does not need to be very large.");
            Settings.MAX_PLOTS = 32767;
        }
        Settings.SCHEMATIC_SAVE_PATH = config.getString("schematics.save_path");

        Settings.OFFLINE_MODE = config.getBoolean("UUID.offline");
        Settings.UUID_FROM_DISK = config.getBoolean("uuid.read-from-disk");

        Settings.REQUIRE_SELECTION = config.getBoolean("worldedit.require-selection-in-mask");
    }

    public static void setupConfigs() {
        final File folder = new File(IMP.getDirectory() + File.separator + "config");
        if (!folder.exists() && !folder.mkdirs()) {
            log(C.PREFIX.s() + "&cFailed to create the /plugins/config folder. Please create it manually.");
        }
        try {
            styleFile = new File(IMP.getDirectory() + File.separator + "translations" + File.separator + "style.yml");
            if (!styleFile.exists()) {
                if (!styleFile.createNewFile()) {
                    log("Could not create the style file, please create \"translations/style.yml\" manually");
                }
            }
            style = YamlConfiguration.loadConfiguration(styleFile);
            setupStyle();
        } catch (final Exception err) {
            Logger.add(LogLevel.DANGER, "Failed to save style.yml");
            System.out.println("failed to save style.yml");
        }
        try {
            configFile = new File(IMP.getDirectory() + File.separator + "config" + File.separator + "settings.yml");
            if (!configFile.exists()) {
                if (!configFile.createNewFile()) {
                    log("Could not create the settings file, please create \"settings.yml\" manually.");
                }
            }
            config = YamlConfiguration.loadConfiguration(configFile);
            setupConfig();
        } catch (final Exception err_trans) {
            Logger.add(LogLevel.DANGER, "Failed to save settings.yml");
            System.out.println("Failed to save settings.yml");
        }
        try {
            storageFile = new File(IMP.getDirectory() + File.separator + "config" + File.separator + "storage.yml");
            if (!storageFile.exists()) {
                if (!storageFile.createNewFile()) {
                    log("Could not the storage settings file, please create \"storage.yml\" manually.");
                }
            }
            storage = YamlConfiguration.loadConfiguration(storageFile);
            setupStorage();
        } catch (final Exception err_trans) {
            Logger.add(LogLevel.DANGER, "Failed to save storage.yml");
            System.out.println("Failed to save storage.yml");
        }
        try {
            style.save(styleFile);
            config.save(configFile);
            storage.save(storageFile);
        } catch (final IOException e) {
            Logger.add(LogLevel.DANGER, "Configuration file saving failed");
            e.printStackTrace();
        }
    }
    
    private static void setupStorage() {
        storage.set("version", VERSION);
        final Map<String, Object> options = new HashMap<>();
        options.put("mysql.use", false);
        options.put("sqlite.use", true);
        options.put("sqlite.db", "storage");
        options.put("mysql.host", "localhost");
        options.put("mysql.port", "3306");
        options.put("mysql.user", "root");
        options.put("mysql.password", "password");
        options.put("mysql.database", "plot_db");
        options.put("prefix", "");
        for (final Entry<String, Object> node : options.entrySet()) {
            if (!storage.contains(node.getKey())) {
                storage.set(node.getKey(), node.getValue());
            }
        }
    }
    
    public static void showDebug() {
    Settings.DB.USE_MYSQL = storage.getBoolean("mysql.use");
    Settings.DB.USER = storage.getString("mysql.user");
    Settings.DB.PASSWORD = storage.getString("mysql.password");
    Settings.DB.HOST_NAME = storage.getString("mysql.host");
    Settings.DB.PORT = storage.getString("mysql.port");
    Settings.DB.DATABASE = storage.getString("mysql.database");
    Settings.DB.USE_SQLITE = storage.getBoolean("sqlite.use");
    Settings.DB.SQLITE_DB = storage.getString("sqlite.db");
    Settings.DB.PREFIX = storage.getString("prefix");
    Settings.METRICS = config.getBoolean("metrics");
    Settings.AUTO_CLEAR = config.getBoolean("clear.auto.enabled");
    Settings.AUTO_CLEAR_DAYS = config.getInt("clear.auto.days");
    Settings.DELETE_PLOTS_ON_BAN = config.getBoolean("clear.on.ban");
    Settings.API_URL = config.getString("uuid.api.location");
    Settings.CUSTOM_API = config.getBoolean("uuid.api.custom");
    Settings.UUID_FECTHING = config.getBoolean("uuid.fetching");

    C.COLOR_1 = ChatColor.getByChar(style.getString("color.1"));
    C.COLOR_2 = ChatColor.getByChar(style.getString("color.2"));
    C.COLOR_3 = ChatColor.getByChar(style.getString("color.3"));
    C.COLOR_4 = ChatColor.getByChar(style.getString("color.4"));
    if (Settings.DEBUG) {
        final Map<String, String> settings = new HashMap<>();
        settings.put("Kill Road Mobs", "" + Settings.KILL_ROAD_MOBS);
        settings.put("Use Metrics", "" + Settings.METRICS);
        settings.put("Delete Plots On Ban", "" + Settings.DELETE_PLOTS_ON_BAN);
        settings.put("Mob Pathfinding", "" + Settings.MOB_PATHFINDING);
        settings.put("DB Mysql Enabled", "" + Settings.DB.USE_MYSQL);
        settings.put("DB SQLite Enabled", "" + Settings.DB.USE_SQLITE);
        settings.put("Auto Clear Enabled", "" + Settings.AUTO_CLEAR);
        settings.put("Auto Clear Days", "" + Settings.AUTO_CLEAR_DAYS);
        settings.put("Schematics Save Path", "" + Settings.SCHEMATIC_SAVE_PATH);
        settings.put("API Location", "" + Settings.API_URL);
        for (final Entry<String, String> setting : settings.entrySet()) {
            log(C.PREFIX.s() + String.format("&cKey: &6%s&c, Value: &6%s", setting.getKey(), setting.getValue()));
        }
    }
    }
    
    private static void setupStyle() {
        style.set("version", VERSION);
        final Map<String, Object> o = new HashMap<>();
        o.put("color.1", C.COLOR_1.getChar());
        o.put("color.2", C.COLOR_2.getChar());
        o.put("color.3", C.COLOR_3.getChar());
        o.put("color.4", C.COLOR_4.getChar());
        for (final Entry<String, Object> node : o.entrySet()) {
            if (!style.contains(node.getKey())) {
                style.set(node.getKey(), node.getValue());
            }
        }
    }

    public static double getJavaVersion() {
        return Double.parseDouble(System.getProperty("java.specification.version"));
    }

    public static Set<String> getPlotWorlds() {
        return plotworlds.keySet();
    }
}
