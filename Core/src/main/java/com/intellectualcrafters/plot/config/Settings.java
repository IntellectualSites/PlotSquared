package com.intellectualcrafters.plot.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Updater and DB settings
 *
 */
public class Settings {
    public static boolean USE_SQLUUIDHANDLER = false;

    public static boolean AUTO_PURGE = false;
    public static boolean UPDATE_NOTIFICATIONS = true;

    public static boolean FAST_CLEAR = false;
    public static boolean MERGE_REMOVES_ROADS = true;
    /**`
     * Default UUID_FECTHING: false
     */
    public static boolean PERMISSION_CACHING = true;
    public static boolean CACHE_RATINGS = true;
    public static boolean UUID_FROM_DISK = false;

    /**
     * Web
     */
    public static String WEB_URL = "http://empcraft.com/plots/";
    public static String WEB_IP = "your.ip.here";
    public static boolean DOWNLOAD_REQUIRES_DONE = false;
    /**
     * Ratings
     */
    public static List<String> RATING_CATEGORIES = null;
    public static boolean REQUIRE_DONE = false;
    public static boolean DONE_COUNTS_TOWARDS_LIMIT = false;
    public static boolean DONE_RESTRICTS_BUILDING = false;
    /**
     * PlotMe settings
     */
    public static boolean CONVERT_PLOTME = true;
    public static boolean CACHE_PLOTME = false;
    public static boolean USE_PLOTME_ALIAS = false;
    /**
     * Comment system
     */
    public static boolean COMMENT_NOTIFICATIONS = false;
    /**
     * Chunk processor
     */
    public static boolean CHUNK_PROCESSOR = false;
    public static boolean EXPERIMENTAL_FAST_ASYNC_WORLDEDIT = false;
    public static boolean CHUNK_PROCESSOR_TRIM_ON_SAVE = false;
    public static boolean CHUNK_PROCESSOR_GC = false;
    public static int CHUNK_PROCESSOR_MAX_BLOCKSTATES = 4096;
    public static int CHUNK_PROCESSOR_MAX_ENTITIES = 512;
    public static boolean CHUNK_PROCESSOR_DISABLE_PHYSICS = false;
    /**
     * Redstone disabler
     */
    public static boolean REDSTONE_DISABLER = false;
    public static boolean REDSTONE_DISABLER_UNOCCUPIED = false;
    /**
     * Max auto claiming size
     */
    public static int MAX_AUTO_SIZE = 4;
    /**
     * Default worldedit-require-selection-in-mask: false
     */
    public static boolean REQUIRE_SELECTION = true;
    public static boolean QUEUE_COMMANDS = false;
    public static boolean WE_ALLOW_HELPER = false;
    public static long WE_MAX_VOLUME = 500000;
    public static long WE_MAX_ITERATIONS = 1000;
    public static List<String> WE_BLACKLIST = new ArrayList<>();
    /**
     * Teleport to path on login
     */
    public static boolean TELEPORT_ON_LOGIN = false;
    /**
     * Teleport to path on death
     */
    public static boolean TELEPORT_ON_DEATH = false;
    /**
     * Display titles
     */
    public static boolean TITLES = true;
    /**
     * Schematic Save Path
     */
    public static String SCHEMATIC_SAVE_PATH = "schematics";
    /**
     * BO3 Save Path
     */
    public static String BO3_SAVE_PATH = "BO3";
    /**
     * Max allowed plots
     */
    public static int MAX_PLOTS = 127;
    /**
     * metrics
     */
    public static boolean METRICS = true;
    /**
     * Kill road mobs?
     */
    public static boolean KILL_ROAD_MOBS = false;
    public static boolean KILL_ROAD_VEHICLES = false;
    /**
     * Delete plots on ban?
     */
    public static boolean DELETE_PLOTS_ON_BAN = false;
    /**
     * Verbose?
     */
    public static boolean DEBUG = true;
    /**
     * Have colored console messages?
     */
    public static boolean CONSOLE_COLOR = true;
    /**
     * Fancy chat e.g. for /plot list
     */
    public static boolean FANCY_CHAT = true;
    /**
     * The delay (in seconds) before teleportation commences
     */
    public static int TELEPORT_DELAY = 0;
    /**
     * Auto clear enabled
     */
    public static boolean AUTO_CLEAR = false;
    public static boolean AUTO_CLEAR_CONFIRMATION = false;
    /**
     * Days until a plot gets cleared
     */
    public static int AUTO_CLEAR_DAYS = 360;
    public static int CLEAR_THRESHOLD = 1;
    public static int CLEAR_INTERVAL = 120;
    /**
     * Use the custom API
     */
    public static boolean CUSTOM_API = true;
    /**
     * Use offline mode storage
     */
    public static boolean TWIN_MODE_UUID = false;
    public static boolean OFFLINE_MODE = false;
    public static boolean UUID_LOWERCASE = false;
    /**
     * Use global plot limit?
     */
    public static boolean GLOBAL_LIMIT = false;

    /**
     * Database settings
     */
    public static class DB {
        /**
         * MongoDB enabled?
         */
        public static boolean USE_MONGO = false;
        /**
         * SQLite enabled?
         */
        public static boolean USE_SQLITE = true;
        /**
         * MySQL Enabled?
         */
        public static boolean USE_MYSQL = false; /* NOTE: Fixed connector */
        /**
         * SQLite Database name
         */
        public static String SQLITE_DB = "storage";
        /**
         * MySQL Host name
         */
        public static String HOST_NAME = "localhost";
        /**
         * MySQL Port
         */
        public static String PORT = "3306";
        /**
         * MySQL DB
         */
        public static String DATABASE = "plot_db";
        /**
         * MySQL User
         */
        public static String USER = "root";
        /**
         * MySQL Password
         */
        public static String PASSWORD = "password";
        /**
         * MySQL Prefix
         */
        public static String PREFIX = "";
    }
}
