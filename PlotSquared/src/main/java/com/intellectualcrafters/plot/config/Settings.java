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

package com.intellectualcrafters.plot.config;

import org.bukkit.Bukkit;

/**
 * Updater and DB settings
 *
 * @author Citymonstret
 * @author Empire92
 */
public class Settings {
    /**
     * Default UUID_FECTHING: false
     */
    public static boolean UUID_FECTHING = false;
    public static boolean UUID_FROM_DISK = false;
    /**
    *
    */
    public static boolean CONVERT_PLOTME = true;
    public static boolean USE_PLOTME_ALIAS = false;
    /**
     *
     */
    public static int MAX_AUTO_SIZE = 4;
    /**
     * Default worldedit-require-selection-in-mask: false
     */
    public static boolean REQUIRE_SELECTION = true;
    /**
     * Default kill road mobs: true
     */
    public final static boolean KILL_ROAD_MOBS_DEFAULT = false;
    /**
     * Default mob pathfinding: true
     */
    public final static boolean MOB_PATHFINDING_DEFAULT = true;
    /**
     * Teleport to path on login
     */
    public static boolean TELEPORT_ON_LOGIN = false;
    /**
     * Mob Cap Enabled
     */
//    public static boolean MOB_CAP_ENABLED = false;
    /**
     * The Mob Cap
     */
//    public static int MOB_CAP = 20;
    /**
     * Display titles
     */
    public static boolean TITLES = true;
    /**
     * Schematic Save Path
     */
    public static String SCHEMATIC_SAVE_PATH = "/var/www/schematics";
    /**
     * Max allowed plots
     */
    public static int MAX_PLOTS = 127;
    /**
     * WorldGuard region on claimed plots
     */
    public static boolean WORLDGUARD = false;
    /**
     * metrics
     */
    public static boolean METRICS = true;
    /**
     * plot specific resource pack
     */
    public static String PLOT_SPECIFIC_RESOURCE_PACK = "";
    /**
     * Kill road mobs?
     */
    public static boolean KILL_ROAD_MOBS;
    /**
     * mob pathfinding?
     */
    public static boolean MOB_PATHFINDING;
    /**
     * Delete plots on ban?
     */
    public static boolean DELETE_PLOTS_ON_BAN = false;

    /**
     * Verbose?
     */
    public static boolean DEBUG = true;

    /**
     * Auto clear enabled
     */
    public static boolean AUTO_CLEAR = false;

    /**
     * Days until a plot gets cleared
     */
    public static int AUTO_CLEAR_DAYS = -1;
    
    public static int MIN_BLOCKS_CHANGED = -1;

    /**
     * API Location
     */
    public static String API_URL = "http://www.intellectualsites.com/minecraft.php";

    /**
     * Use the custom API
     */
    public static boolean CUSTOM_API = true;

    /**
     * Use offline mode storage
     */
    public static boolean OFFLINE_MODE = false;

    /**
     * Database settings
     *
     * @author Citymonstret
     */
    public static class DB {
        /**
         * MongoDB enabled?
         */
        public static boolean USE_MONGO = false;      /*
         * TODO: Implement Mongo
         * @Brandon
         */
        /**
         * SQLite enabled?
         */
        public static boolean USE_SQLITE = false;
        /**
         * MySQL Enabled?
         */
        public static boolean USE_MYSQL = true;       /* NOTE: Fixed connector */
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
