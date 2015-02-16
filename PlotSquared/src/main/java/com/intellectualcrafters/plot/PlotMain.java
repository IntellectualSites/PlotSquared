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

package com.intellectualcrafters.plot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import me.confuser.barapi.BarAPI;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.intellectualcrafters.plot.commands.Buy;
import com.intellectualcrafters.plot.commands.Cluster;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.commands.WE_Anywhere;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.database.MySQL;
import com.intellectualcrafters.plot.database.PlotMeConverter;
import com.intellectualcrafters.plot.database.SQLManager;
import com.intellectualcrafters.plot.database.SQLite;
import com.intellectualcrafters.plot.events.PlayerTeleportToPlotEvent;
import com.intellectualcrafters.plot.events.PlotDeleteEvent;
import com.intellectualcrafters.plot.flag.AbstractFlag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.flag.FlagValue;
import com.intellectualcrafters.plot.generator.AugmentedPopulator;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.listeners.ForceFieldListener;
import com.intellectualcrafters.plot.listeners.InventoryListener;
import com.intellectualcrafters.plot.listeners.PlayerEvents;
import com.intellectualcrafters.plot.listeners.PlayerEvents_1_8;
import com.intellectualcrafters.plot.listeners.PlotListener;
import com.intellectualcrafters.plot.listeners.PlotPlusListener;
import com.intellectualcrafters.plot.listeners.WorldEditListener;
import com.intellectualcrafters.plot.listeners.WorldGuardListener;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.titles.AbstractTitle;
import com.intellectualcrafters.plot.titles.DefaultTitle;
import com.intellectualcrafters.plot.util.AbstractSetBlock;
import com.intellectualcrafters.plot.util.ClusterManager;
import com.intellectualcrafters.plot.util.ConsoleColors;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.Lag;
import com.intellectualcrafters.plot.util.Logger;
import com.intellectualcrafters.plot.util.Logger.LogLevel;
import com.intellectualcrafters.plot.util.Metrics;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.SendChunk;
import com.intellectualcrafters.plot.util.SetBlockFast;
import com.intellectualcrafters.plot.util.SetBlockFast_1_8;
import com.intellectualcrafters.plot.util.SetBlockSlow;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.uuid.DefaultUUIDWrapper;
import com.intellectualcrafters.plot.uuid.OfflineUUIDWrapper;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * PlotMain class.
 *
 * @author Citymonstret
 * @author Empire92
 */
public class PlotMain extends JavaPlugin implements Listener {
    /**
     * Permission that allows for "everything"
     */
    public static final String ADMIN_PERMISSION = "plots.admin";
    /**
     * Storage version
     */
    public final static int storage_ver = 1;
    /**
     * All loaded plot worlds
     */
    private final static HashMap<String, PlotWorld> worlds = new HashMap<>();
    /**
     * All world managers
     */
    private final static HashMap<String, PlotManager> managers = new HashMap<>();
    /**
     * settings.properties
     */
    public static File configFile;
    /**
     * The main configuration file
     */
    public static YamlConfiguration config;
    /**
     * storage.properties
     */
    public static File storageFile;
    /**
     * Contains storage options
     */
    public static YamlConfiguration storage;
    /**
     * MySQL Connection
     */
    public static Connection connection;
    /**
     * WorldEdit object
     */
    public static WorldEditPlugin worldEdit = null;
    /**
     * BarAPI object
     */
    public static BarAPI barAPI = null;
    /**
     * World Guard Object
     */
    public static WorldGuardPlugin worldGuard = null;
    /**
     * World Guard Listener
     */
    public static WorldGuardListener worldGuardListener = null;
    /**
     * Economy Object (vault)
     */
    public static Economy economy;
    /**
     * Use Economy?
     */
    public static boolean useEconomy = false;
    private static PlotMain main = null;
    private static boolean LOADING_WORLD = false;
    /**
     * MySQL Object
     */
    private static MySQL mySQL;
    /**
     * List of all plots DO NOT USE EXCEPT FOR DATABASE PURPOSES
     */
    private static LinkedHashMap<String, HashMap<PlotId, Plot>> plots;

    /**
     * Return an instance of MySQL
     */
    public static MySQL getMySQL() {
        return mySQL;
    }
    /**
     * Check a range of permissions e.g. 'plots.plot.<0-100>'<br> Returns highest integer in range.
     *
     * @param player to check
     * @param stub   to check
     * @param range  tp check
     *
     * @return permitted range
     */
    public static int hasPermissionRange(final Player player, final String stub, final int range) {
        if ((player == null) || player.isOp() || player.hasPermission(ADMIN_PERMISSION)) {
            return Byte.MAX_VALUE;
        }
        if (player.hasPermission(stub + ".*")) {
            return Byte.MAX_VALUE;
        }
        for (int i = range; i > 0; i--) {
            if (player.hasPermission(stub + "." + i)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Check a player for a permission<br> - Op has all permissions <br> - checks for '*' nodes
     *
     * @param player to check
     * @param perms  to check
     *
     * @return true of player has permissions
     */
    public static boolean hasPermissions(final Player player, final String[] perms) {
        // Assumes null player is console.
        if ((player == null) || player.isOp() || player.hasPermission(ADMIN_PERMISSION)) {
            return true;
        }
        for (final String perm : perms) {
            boolean permitted = false;
            if (player.hasPermission(perm)) {
                permitted = true;
            } else {
                final String[] nodes = perm.split("\\.");
                final StringBuilder n = new StringBuilder();
                for (int i = 0; i < (nodes.length - 1); i++) {
                    n.append(nodes[i]).append(".");
                    if (player.hasPermission(n + "*")) {
                        permitted = true;
                        break;
                    }
                }
            }
            if (!permitted) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check a player for a permission<br> - Op has all permissions <br> - checks for '*' nodes
     *
     * @param player to check
     * @param perm   to check
     *
     * @return true if player has the permission
     */
    public static boolean hasPermission(final Player player, final String perm) {
        if ((player == null) || player.isOp() || player.hasPermission(ADMIN_PERMISSION)) {
            return true;
        }
        if (player.hasPermission(perm)) {
            return true;
        }
        final String[] nodes = perm.split("\\.");
        final StringBuilder n = new StringBuilder();
        for (int i = 0; i < (nodes.length - 1); i++) {
            n.append(nodes[i] + ("."));
            if (player.hasPermission(n + "*")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get all plots
     *
     * @return HashMap containing the plot ID and the plot object.
     */
    public static Set<Plot> getPlots() {
        final ArrayList<Plot> _plots = new ArrayList<>();
        for (final HashMap<PlotId, Plot> world : plots.values()) {
            _plots.addAll(world.values());
        }
        return new LinkedHashSet<>(_plots);
    }

    /**
     * Get a sorted list of plots
     *
     * @return sorted list
     */
    public static LinkedHashSet<Plot> getPlotsSorted() {
        final ArrayList<Plot> _plots = new ArrayList<>();
        for (final HashMap<PlotId, Plot> world : plots.values()) {
            _plots.addAll(world.values());
        }
        return new LinkedHashSet<>(_plots);
    }

    /**
     * @param player player
     *
     * @return Set Containing the players plots
     *  - ignores non plot worlds
     */
    public static Set<Plot> getPlots(final Player player) {
        final UUID uuid = UUIDHandler.getUUID(player);
        final ArrayList<Plot> myplots = new ArrayList<>();
        for (final String world : plots.keySet()) {
            if (isPlotWorld(world)) {
                for (final Plot plot : plots.get(world).values()) {
                    if (plot.hasOwner()) {
                        if (plot.getOwner().equals(uuid)) {
                            myplots.add(plot);
                        }
                    }
                }
            }
        }
        return new HashSet<>(myplots);
    }

    /**
     * @param world  plot world
     * @param player plot owner
     *
     * @return players plots
     */
    public static Set<Plot> getPlots(final World world, final Player player) {
        final UUID uuid = UUIDHandler.getUUID(player);
        return getPlots(world, uuid);
    }
    
    /**
     * @param world  plot world
     * @param player plot owner
     *
     * @return players plots
     */
    public static Set<Plot> getPlots(final World world, final UUID uuid) {
        final ArrayList<Plot> myplots = new ArrayList<>();
        for (final Plot plot : getPlots(world).values()) {
            if (plot.hasOwner()) {
                if (plot.getOwner().equals(uuid)) {
                    myplots.add(plot);
                }
            }
        }
        return new HashSet<>(myplots);
    }

    /**
     * Get plots for the specified world
     *
     * @param world A world, in which you want to search for plots
     *
     * @return HashMap containing Plot IDs and Plot Objects
     */
    public static HashMap<PlotId, Plot> getPlots(final String world) {
        if (plots.containsKey(world)) {
            return plots.get(world);
        }
        return new HashMap<>();
    }

    /**
     * @param world plot world
     *
     * @return plots in world
     */
    public static HashMap<PlotId, Plot> getPlots(final World world) {
        if (plots.containsKey(world.getName())) {
            return plots.get(world.getName());
        }
        return new HashMap<>();
    }

    /**
     * get all plot worlds
     */
    public static String[] getPlotWorlds() {
        final Set<String> strings = worlds.keySet();
        return (strings.toArray(new String[strings.size()]));
    }

    /**
     * @return plots worlds
     */
    public static String[] getPlotWorldsString() {
        final Set<String> strings = plots.keySet();
        return strings.toArray(new String[strings.size()]);
    }

    /**
     * @param world plotworld(?)
     *
     * @return true if the world is a plotworld
     */
    public static boolean isPlotWorld(final World world) {
        return (worlds.containsKey(world.getName()));
    }

    /**
     * @param world plotworld(?)
     *
     * @return true if the world is a plotworld
     */
    public static boolean isPlotWorld(final String world) {
        return (worlds.containsKey(world));
    }

    /**
     * @param world World to get manager for
     *
     * @return manager for world
     */
    public static PlotManager getPlotManager(final World world) {
        if (managers.containsKey(world.getName())) {
            return managers.get(world.getName());
        }
        return null;
    }

    /**
     * @param world world
     *
     * @return PlotManager
     */
    public static PlotManager getPlotManager(final String world) {
        if (managers.containsKey(world)) {
            return managers.get(world);
        }
        return null;
    }

    /**
     * @param world to search
     *
     * @return PlotWorld object
     */
    public static PlotWorld getWorldSettings(final World world) {
        if (worlds.containsKey(world.getName())) {
            return worlds.get(world.getName());
        }
        return null;
    }

    /**
     * @param world to search
     *
     * @return PlotWorld object
     */
    public static PlotWorld getWorldSettings(final String world) {
        if (worlds.containsKey(world)) {
            return worlds.get(world);
        }
        return null;
    }

    /**
     * @param world world to search
     *
     * @return set containing the plots for a world
     */
    public static Plot[] getWorldPlots(final World world) {
        final Collection<Plot> values = plots.get(world.getName()).values();
        return (values.toArray(new Plot[values.size()]));
    }

    /**
     * Remove a plot
     *
     * @param world     The Plot World
     * @param id        The Plot ID
     * @param callEvent Whether or not to call the PlotDeleteEvent
     *
     * @return true if successful, false if not
     */
    public static boolean removePlot(final String world, final PlotId id, final boolean callEvent) {
        if (callEvent) {
            final PlotDeleteEvent event = new PlotDeleteEvent(world, id);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                event.setCancelled(true);
                return false;
            }
        }
        plots.get(world).remove(id);
        
        if (PlotHelper.lastPlot.containsKey(world)) {
        	PlotId last = PlotHelper.lastPlot.get(world);
        	int last_max = Math.max(last.x, last.y);
        	int this_max = Math.max(id.x, id.y);
        	
        	if (this_max < last_max) {
        		PlotHelper.lastPlot.put(world, id);
        	}
        }
        
        return true;
    }

    /**
     * Replace the plot object with an updated version
     *
     * @param plot plot object
     */
    public static void updatePlot(final Plot plot) {
        final String world = plot.world;
        if (!plots.containsKey(world)) {
            plots.put(world, new HashMap<PlotId, Plot>());
        }
        plot.hasChanged = true;
        plots.get(world).put(plot.id, plot);
    }

    /**
     * Get the java version
     *
     * @return Java Version as a double
     */
    public static double getJavaVersion() {
        return Double.parseDouble(System.getProperty("java.specification.version"));
    }

    /**
     * Get MySQL Connection
     *
     * @return connection MySQL Connection.
     */
    public static Connection getConnection() {
        return connection;
    }

    /**
     * Send a message to the console.
     *
     * @param string message
     */
    public static void sendConsoleSenderMessage(final String string) {
        if (PlotMain.main == null || getMain().getServer().getConsoleSender() == null) {
            System.out.println(ChatColor.stripColor(ConsoleColors.fromString(string)));
        } else {
        	String message = ChatColor.translateAlternateColorCodes('&', string);
        	if (!Settings.CONSOLE_COLOR) {
        		message = ChatColor.stripColor(message);
        	}
            getMain().getServer().getConsoleSender().sendMessage(message);
        }
    }

    /**
     * Teleport a player to a plot
     *
     * @param player Player to teleport
     * @param from   Previous Location
     * @param plot   Plot to teleport to
     *
     * @return true if successful
     */
    public static boolean teleportPlayer(final Player player, final Location from, final Plot plot) {
    	Plot bot = PlayerFunctions.getBottomPlot(player.getWorld(), plot);
        final PlayerTeleportToPlotEvent event = new PlayerTeleportToPlotEvent(player, from, bot);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            final Location location = PlotHelper.getPlotHome(Bukkit.getWorld(bot.world), bot);
            if ((location.getBlockX() >= 29999999) || (location.getBlockX() <= -29999999) || (location.getBlockZ() >= 299999999) || (location.getBlockZ() <= -29999999)) {
                event.setCancelled(true);
                return false;
            }
            if (Settings.TELEPORT_DELAY == 0 || hasPermission(player, "plots.teleport.delay.bypass")) {
                PlayerFunctions.sendMessage(player, C.TELEPORTED_TO_PLOT);
                player.teleport(location);
                return true;
            }
            PlayerFunctions.sendMessage(player, C.TELEPORT_IN_SECONDS, Settings.TELEPORT_DELAY + "");
            Location loc = player.getLocation();
            final World world = player.getWorld();
            final int x = loc.getBlockX();
            final int z = loc.getBlockZ();
            final String name = player.getName();
            TaskManager.TELEPORT_QUEUE.add(name);
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    if (!TaskManager.TELEPORT_QUEUE.contains(name)) {
                        PlayerFunctions.sendMessage(player, C.TELEPORT_FAILED);
                        return;
                    }
                    TaskManager.TELEPORT_QUEUE.remove(name);
                    if (!player.isOnline()) {
                        return;
                    }
                    Location loc = player.getLocation();
                    if (!loc.getWorld().equals(world)) {
                        PlayerFunctions.sendMessage(player, C.TELEPORT_FAILED);
                        return;
                    }
                    if (loc.getBlockX() != x || loc.getBlockZ() != z) {
                        PlayerFunctions.sendMessage(player, C.TELEPORT_FAILED);
                        return;
                    }
                    PlayerFunctions.sendMessage(player, C.TELEPORTED_TO_PLOT);
                    player.teleport(location);
                }
            }, Settings.TELEPORT_DELAY * 20);
            return true;
        }
        return !event.isCancelled();
    }

    /**
     * Send a message to the console
     *
     * @param c message
     */
    @SuppressWarnings("unused")
    public static void sendConsoleSenderMessage(final C c) {
        sendConsoleSenderMessage(c.s());
    }

    /**
     * Broadcast publicly
     *
     * @param c message
     */
    public static void Broadcast(final C c) {
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', C.PREFIX.s() + c.s()));
    }

    /**
     * Returns the main class.
     *
     * @return (this class)
     */
    public static PlotMain getMain() {
        return PlotMain.main;
    }

    /**
     * Broadcast a message to all admins
     *
     * @param c message
     */
    public static void BroadcastWithPerms(final C c) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(ADMIN_PERMISSION)) {
                PlayerFunctions.sendMessage(player, c);
            }
        }
        System.out.println(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', C.PREFIX.s() + c.s())));
    }

    /**
     * Reload all translations
     *
     * @throws IOException
     */
    public static void reloadTranslations() throws IOException {
        C.setupTranslations();
    }

    /**
     * Ge the last played time
     *
     * @param uuid UUID for the player
     *
     * @return last play time as a long
     */
    public static long getLastPlayed(final UUID uuid) {
        if (uuid == null) {
            return 0;
        }
        OfflinePlayer player;
        if (((player = UUIDHandler.uuidWrapper.getOfflinePlayer(uuid)) == null) || !player.hasPlayedBefore()) {
            return 0;
        }
        return player.getLastPlayed();
    }

    /**
     * Load configuration files
     */
    @SuppressWarnings("deprecation")
    public static void configs() {
        final File folder = new File(getMain().getDataFolder() + File.separator + "config");
        if (!folder.exists() && !folder.mkdirs()) {
            sendConsoleSenderMessage(C.PREFIX.s() + "&cFailed to create the /plugins/config folder. Please create it manually.");
        }
        try {
            configFile = new File(getMain().getDataFolder() + File.separator + "config" + File.separator + "settings.yml");
            if (!configFile.exists()) {
                if (!configFile.createNewFile()) {
                    sendConsoleSenderMessage("Could not create the settings file, please create \"settings.yml\" manually.");
                }
            }
            config = YamlConfiguration.loadConfiguration(configFile);
            setupConfig();
        } catch (final Exception err_trans) {
            Logger.add(LogLevel.DANGER, "Failed to save settings.yml");
            System.out.println("Failed to save settings.yml");
        }
        try {
            storageFile = new File(getMain().getDataFolder() + File.separator + "config" + File.separator + "storage.yml");
            if (!storageFile.exists()) {
                if (!storageFile.createNewFile()) {
                    sendConsoleSenderMessage("Could not the storage settings file, please create \"storage.yml\" manually.");
                }
            }
            storage = YamlConfiguration.loadConfiguration(storageFile);
            setupStorage();
        } catch (final Exception err_trans) {
            Logger.add(LogLevel.DANGER, "Failed to save storage.yml");
            System.out.println("Failed to save storage.yml");
        }
        try {
            config.save(configFile);
            storage.save(storageFile);
        } catch (final IOException e) {
            Logger.add(LogLevel.DANGER, "Configuration file saving failed");
            e.printStackTrace();
        }
        {
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
        }
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
                sendConsoleSenderMessage(C.PREFIX.s() + String.format("&cKey: &6%s&c, Value: &6%s", setting.getKey(), setting.getValue()));
            }
        }
    }

    /**
     * Kill all entities on roads
     */
    @SuppressWarnings("deprecation")
    public static void killAllEntities() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(getMain(), new Runnable() {
            long ticked = 0l;
            long error = 0l;

            {
                sendConsoleSenderMessage(C.PREFIX.s() + "KillAllEntities started.");
            }

            @Override
            public void run() {
                if (this.ticked > 36_000L) {
                    this.ticked = 0l;
                    if (this.error > 0) {
                        sendConsoleSenderMessage(C.PREFIX.s() + "KillAllEntities has been running for 6 hours. Errors: " + this.error);
                    }
                    this.error = 0l;
                }
                World world;
                for (final String w : getPlotWorlds()) {
                    getWorldSettings(w);
                    world = Bukkit.getServer().getWorld(w);
                    try {
                        if (world.getLoadedChunks().length < 1) {
                            continue;
                        }
                        for (final Chunk chunk : world.getLoadedChunks()) {
                            final Entity[] entities = chunk.getEntities();
                            Entity entity;
                            for (int i = entities.length - 1; i >= 0; i--) {
                                if (!((entity = entities[i]) instanceof Player) && !PlotListener.isInPlot(entity.getLocation())) {
                                    entity.remove();
                                }
                            }
                        }
                    } catch (final Throwable e) {
                        ++this.error;
                    } finally {
                        ++this.ticked;
                    }
                }
            }
        }, 20L, 20L);
    }

    /**
     * SETUP: settings.yml
     */
    public static void setupConfig() {
        final int config_ver = 1;
        config.set("version", config_ver);
        final Map<String, Object> options = new HashMap<>();
        options.put("teleport.delay", 0);
        options.put("auto_update", false);
        options.put("clusters.enabled", Settings.ENABLE_CLUSTERS);
        options.put("plotme-alias", Settings.USE_PLOTME_ALIAS);
        options.put("plotme-convert.enabled", Settings.CONVERT_PLOTME);
        options.put("claim.max-auto-area", Settings.MAX_AUTO_SIZE);
        options.put("UUID.offline", Settings.OFFLINE_MODE);
//        options.put("worldguard.enabled", Settings.WORLDGUARD);
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
//        options.put("uuid.api.location", Settings.API_URL);
//        options.put("uuid.api.custom", Settings.CUSTOM_API);
//        options.put("uuid.fecthing", Settings.UUID_FECTHING);
        options.put("uuid.read-from-disk", Settings.UUID_FROM_DISK);
        options.put("titles", Settings.TITLES);
        options.put("teleport.on_login", Settings.TELEPORT_ON_LOGIN);
//        options.put("perm-based-mob-cap.enabled", Settings.MOB_CAP_ENABLED);
//        options.put("perm-based-mob-cap.max", Settings.MOB_CAP);
        options.put("worldedit.require-selection-in-mask", Settings.REQUIRE_SELECTION);

        for (final Entry<String, Object> node : options.entrySet()) {
            if (!config.contains(node.getKey())) {
                config.set(node.getKey(), node.getValue());
            }
        }
        Settings.ENABLE_CLUSTERS = config.getBoolean("clusters.enabled");
        Settings.DEBUG = config.getBoolean("debug");
        if (Settings.DEBUG) {
            sendConsoleSenderMessage(C.PREFIX.s() + "&6Debug Mode Enabled (Default). Edit the config to turn this off.");
        }
        Settings.TELEPORT_DELAY = config.getInt("teleport.delay");
        Settings.CONSOLE_COLOR = config.getBoolean("console.color");
        Settings.TELEPORT_ON_LOGIN = config.getBoolean("teleport.on_login");
        Settings.USE_PLOTME_ALIAS = config.getBoolean("plotme-alias");
        Settings.CONVERT_PLOTME = config.getBoolean("plotme-convert.enabled");
        Settings.KILL_ROAD_MOBS = config.getBoolean("kill_road_mobs");
//        Settings.WORLDGUARD = config.getBoolean("worldguard.enabled");
        Settings.MOB_PATHFINDING = config.getBoolean("mob_pathf"
        		+ "inding");
        Settings.METRICS = config.getBoolean("metrics");
        Settings.AUTO_CLEAR_DAYS = config.getInt("clear.auto.days");
        Settings.AUTO_CLEAR_CHECK_DISK = config.getBoolean("clear.check-disk");
        Settings.MAX_AUTO_SIZE = config.getInt("claim.max-auto-area");
        Settings.AUTO_CLEAR = config.getBoolean("clear.auto.enabled");
        Settings.TITLES = config.getBoolean("titles");
//        Settings.MOB_CAP_ENABLED = config.getBoolean("perm-based-mob-cap.enabled");
//        Settings.MOB_CAP = config.getInt("perm-based-mob-cap.max");
        Settings.MAX_PLOTS = config.getInt("max_plots");
        if (Settings.MAX_PLOTS > 32767) {
            sendConsoleSenderMessage("&c`max_plots` Is set too high! This is a per player setting and does not need to be very large.");
            Settings.MAX_PLOTS = 32767;
        }
        
        
        Settings.SCHEMATIC_SAVE_PATH = config.getString("schematics.save_path");

        Settings.OFFLINE_MODE = config.getBoolean("UUID.offline");
        Settings.UUID_FROM_DISK = config.getBoolean("uuid.read-from-disk");

        Settings.REQUIRE_SELECTION = config.getBoolean("worldedit.require-selection-in-mask");
    }

    /**
     * Create a plotworld config section
     *
     * @param plotworld World to create the section for
     */
    public static void createConfiguration(final PlotWorld plotworld) {
        final Map<String, Object> options = new HashMap<>();

        for (final ConfigurationNode setting : plotworld.getSettingNodes()) {
            options.put(setting.getConstant(), setting.getValue());
        }

        for (final Entry<String, Object> node : options.entrySet()) {
            if (!config.contains(node.getKey())) {
                config.set(node.getKey(), node.getValue());
            }
        }

        try {
            config.save(PlotMain.configFile);
        } catch (final IOException e) {
            PlotMain.sendConsoleSenderMessage("&c[Warning] PlotSquared failed to save the configuration&7 (settings.yml may differ from the one in memory)\n - To force a save from console use /plots save");
        }
    }
    
    @EventHandler
    public void PlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message.toLowerCase().startsWith("/plotme")) {
            Plugin plotme = Bukkit.getPluginManager().getPlugin("PlotMe");
            if (plotme == null) {
                Player player = event.getPlayer();
                if (Settings.USE_PLOTME_ALIAS) {
                    player.performCommand(message.replace("/plotme", "plots"));
                }
                else {
                    PlayerFunctions.sendMessage(player, C.NOT_USING_PLOTME);
                }
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public static void worldLoad(WorldLoadEvent event) {
        if (!UUIDHandler.CACHED) {
            UUIDHandler.cacheAll();
            if (Settings.CONVERT_PLOTME) {
                if (Bukkit.getPluginManager().getPlugin("PlotMe") != null) {
                    sendConsoleSenderMessage("&c[IMPORTANT] THIS MESSAGE MAY BE EXTREMELY HELPFUL IF YOU HAVE TROUBLE CONVERTING PLOTME!");
                    sendConsoleSenderMessage("&c[IMPORTANT] - Make sure 'UUID.read-from-disk' is disabled (false)!");
                    sendConsoleSenderMessage("&c[IMPORTANT] - Sometimes the database can be locked, deleting PlotMe.jar beforehand will fix the issue!");
                    sendConsoleSenderMessage("&c[IMPORTANT] - After the conversion is finished, please set 'plotme-convert.enabled' to false in the 'settings.yml@'");
                }
                try {
                    new PlotMeConverter(PlotMain.getMain()).runAsync();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void loadWorld(final String world, final ChunkGenerator generator) {
        if (getWorldSettings(world) != null) {
            return;
        }

        final Set<String> worlds = (config.contains("worlds") ? config.getConfigurationSection("worlds").getKeys(false) : new HashSet<String>());

        final PlotWorld plotWorld;
        final PlotGenerator plotGenerator;
        final PlotManager plotManager;
        final String path = "worlds." + world;

        if (!LOADING_WORLD && (generator != null) && (generator instanceof PlotGenerator)) {
            plotGenerator = (PlotGenerator) generator;
            plotWorld = plotGenerator.getNewPlotWorld(world);
            plotManager = plotGenerator.getPlotManager();
            if (!world.equals("CheckingPlotSquaredGenerator")) {
                sendConsoleSenderMessage(C.PREFIX.s() + "&aDetected world load for '" + world + "'");
                sendConsoleSenderMessage(C.PREFIX.s() + "&3 - generator: &7" + plotGenerator.getClass().getName());
                sendConsoleSenderMessage(C.PREFIX.s() + "&3 - plotworld: &7" + plotWorld.getClass().getName());
                sendConsoleSenderMessage(C.PREFIX.s() + "&3 - manager: &7" + plotManager.getClass().getName());
            }
            if (!config.contains(path)) {
                config.createSection(path);
            }
            plotWorld.saveConfiguration(config.getConfigurationSection(path));
            plotWorld.loadDefaultConfiguration(config.getConfigurationSection(path));
            try {
                config.save(configFile);
            } catch (final IOException e) {
                e.printStackTrace();
            }
            // Now add it
            addPlotWorld(world, plotWorld, plotManager);
            PlotHelper.setupBorder(world);
        } else {
            if (!worlds.contains(world)) {
                return;
            }
            if (!LOADING_WORLD) {
                LOADING_WORLD = true;
                try {
                    String gen_string = config.getString("worlds." + world + "." + "generator.plugin");
                    Plugin gen_plugin = gen_string == null ? null : Bukkit.getPluginManager().getPlugin(gen_string);
                    if (gen_plugin != null && gen_plugin.isEnabled()) {
                        gen_plugin.getDefaultWorldGenerator(world, "");
                    }
                    else {
                        new HybridGen(world);
                    }
                }
                catch (Exception e) {
                    PlotMain.sendConsoleSenderMessage("&d=== Oh no! Please set the generator for the " + world + " ===");
                    e.printStackTrace();
                    LOADING_WORLD = false;
                    removePlotWorld(world);
                }
                finally {
                    LOADING_WORLD = false;
                }
            }
            else {
                PlotGenerator gen_class = (PlotGenerator) generator;
                plotWorld = gen_class.getNewPlotWorld(world);
                plotManager = gen_class.getPlotManager();
                if (!config.contains(path)) {
                    config.createSection(path);
                }
                plotWorld.TYPE = 2;
                plotWorld.TERRAIN = 0;
                plotWorld.saveConfiguration(config.getConfigurationSection(path));
                plotWorld.loadDefaultConfiguration(config.getConfigurationSection(path));
                try {
                    config.save(configFile);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                if (plotWorld.TYPE == 2 && !Settings.ENABLE_CLUSTERS) {
                    sendConsoleSenderMessage("&c[ERROR] World '" + world + "' in settings.yml is not using PlotSquared generator! Please enable plot custers or delete this world from your settings.yml!");
                    return;
                }
                addPlotWorld(world, plotWorld, plotManager);
                if (plotWorld.TYPE == 2) {
                    if (ClusterManager.getClusters(world).size() > 0) {
                        for (PlotCluster cluster : ClusterManager.getClusters(world)) {
                            new AugmentedPopulator(world, gen_class, cluster, plotWorld.TERRAIN == 2, plotWorld.TERRAIN != 2);
                        }
                    }
                }
                else if (plotWorld.TYPE == 1) {
                    new AugmentedPopulator(world, gen_class, null, plotWorld.TERRAIN == 2, plotWorld.TERRAIN != 2);
                }
            }
        }
    }

    /**
     * Adds an external world as a recognized PlotSquared world - The PlotWorld class created is based off the
     * configuration in the settings.yml - Do not use this method unless the required world is preconfigured in the
     * settings.yml
     *
     * @param world to load
     */
    public static void loadWorld(final World world) {
        if (world == null) {
            return;
        }
        final ChunkGenerator generator = world.getGenerator();
        loadWorld(world.getName(), generator);
    }

    /**
     * SETUP: storage.properties
     */
    private static void setupStorage() {
        storage.set("version", storage_ver);
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

    private static void addPlusFlags() {
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
    }

    private static void defaultFlags() {
        addPlusFlags();
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

    /**
     * Add a Plot world
     *
     * @param world     World to add
     * @param plotworld PlotWorld Object
     * @param manager   Plot Manager for the new world
     */
    public static void addPlotWorld(final String world, final PlotWorld plotworld, final PlotManager manager) {
        worlds.put(world, plotworld);
        managers.put(world, manager);
        if (!plots.containsKey(world)) {
            plots.put(world, new HashMap<PlotId, Plot>());
        }
    }

    /**
     * Remove a plot world
     *
     * @param world World to remove
     */
    public static void removePlotWorld(final String world) {
        plots.remove(world);
        managers.remove(world);
        worlds.remove(world);
    }
    
    public static void removePlots(final String world) {
        plots.put(world, new HashMap<PlotId, Plot>());
    }

    /**
     * Get all plots
     *
     * @return All Plos in a hashmap (world, Hashmap contiang ids and objects))
     */
    public static HashMap<String, HashMap<PlotId, Plot>> getAllPlotsRaw() {
        return plots;
    }

    /**
     * Set all plots
     *
     * @param plots New Plot HashMap
     */
    public static void setAllPlotsRaw(final HashMap<String, HashMap<PlotId, Plot>> plots) {
        PlotMain.plots = new LinkedHashMap<>(plots);
        // PlotMain.plots.putAll(plots);
    }

    /**
     * Set all plots
     *
     * @param plots New Plot LinkedHashMap
     */
    public static void setAllPlotsRaw(final LinkedHashMap<String, HashMap<PlotId, Plot>> plots) {
        PlotMain.plots = plots;
    }

    /**
     * Get the PlotSquared World Generator
     *
     * @see com.intellectualcrafters.plot.generator.WorldGenerator
     */
    @Override
    final public ChunkGenerator getDefaultWorldGenerator(final String world, final String id) {
        if (id != null && id.length() > 0) {
            // save configuration
            String[] split = id.split(",");
            HybridPlotWorld plotworld = new HybridPlotWorld(world);
            
            int width = HybridPlotWorld.PLOT_WIDTH_DEFAULT;
            int gap = HybridPlotWorld.ROAD_WIDTH_DEFAULT;
            int height = HybridPlotWorld.PLOT_HEIGHT_DEFAULT;
            PlotBlock[] floor = HybridPlotWorld.TOP_BLOCK_DEFAULT;
            PlotBlock[] main = HybridPlotWorld.MAIN_BLOCK_DEFAULT;
            PlotBlock wall = HybridPlotWorld.WALL_FILLING_DEFAULT;
            PlotBlock border = HybridPlotWorld.WALL_BLOCK_DEFAULT;
            
            for (String element : split) {
                String[] pair = element.split("=");
                if (pair.length != 2) {
                    sendConsoleSenderMessage("&cNo value provided for: &7" + element);
                    return null;
                }
                String key = pair[0].toLowerCase();
                String value = pair[1];
                try {
                    switch (key) {
                        case "s":
                        case "size": {
                            HybridPlotWorld.PLOT_WIDTH_DEFAULT = ((Integer) Configuration.INTEGER.parseString(value)).shortValue();
                            break;
                        }
                        case "g":
                        case "gap": {
                            HybridPlotWorld.ROAD_WIDTH_DEFAULT = ((Integer) Configuration.INTEGER.parseString(value)).shortValue();
                            break;
                        }
                        case "h":
                        case "height": {
                            HybridPlotWorld.PLOT_HEIGHT_DEFAULT = (Integer) Configuration.INTEGER.parseString(value);
                            HybridPlotWorld.ROAD_HEIGHT_DEFAULT = (Integer) Configuration.INTEGER.parseString(value);
                            HybridPlotWorld.WALL_HEIGHT_DEFAULT = (Integer) Configuration.INTEGER.parseString(value);
                            break;
                        }
                        case "f":
                        case "floor": {
                            HybridPlotWorld.TOP_BLOCK_DEFAULT = (PlotBlock[]) Configuration.BLOCKLIST.parseString(value);
                            break;   
                        }
                        case "m":
                        case "main": {
                            HybridPlotWorld.MAIN_BLOCK_DEFAULT = (PlotBlock[]) Configuration.BLOCKLIST.parseString(value);
                            break;
                        }
                        case "w":
                        case "wall": {
                            HybridPlotWorld.WALL_FILLING_DEFAULT = (PlotBlock) Configuration.BLOCK.parseString(value);
                            break;
                        }
                        case "b":
                        case "border": {
                            HybridPlotWorld.WALL_BLOCK_DEFAULT = (PlotBlock) Configuration.BLOCK.parseString(value);
                            break;
                        }
                        default: {
                            sendConsoleSenderMessage("&cKey not found: &7" + element);
                            return null;
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    sendConsoleSenderMessage("&cInvalid value: &7" + value + " in arg " + element);
                    return null;
                }
            }
            try {
                String root = "worlds." + world;
                if (!config.contains(root)) {
                    config.createSection(root);
                }
                plotworld.saveConfiguration(config.getConfigurationSection(root));
                HybridPlotWorld.PLOT_HEIGHT_DEFAULT = height;
                HybridPlotWorld.ROAD_HEIGHT_DEFAULT = height;
                HybridPlotWorld.WALL_HEIGHT_DEFAULT = height;
                HybridPlotWorld.TOP_BLOCK_DEFAULT = floor;
                HybridPlotWorld.MAIN_BLOCK_DEFAULT = main;
                HybridPlotWorld.WALL_BLOCK_DEFAULT = border;
                HybridPlotWorld.WALL_FILLING_DEFAULT = wall;
                HybridPlotWorld.PLOT_WIDTH_DEFAULT = width;
                HybridPlotWorld.ROAD_WIDTH_DEFAULT = gap;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new HybridGen(world);
    }

    /**
     * Setup the logger mechanics
     */
    private void setupLogger() {
        final File log = new File(getMain().getDataFolder() + File.separator + "logs" + File.separator + "plots.log");
        if (!log.exists()) {
            try {
                if (!new File(getMain().getDataFolder() + File.separator + "logs").mkdirs()) {
                    sendConsoleSenderMessage(C.PREFIX.s() + "&cFailed to create logs folder. Do it manually.");
                }
                if (log.createNewFile()) {
                    final FileWriter writer = new FileWriter(log);
                    writer.write("Created at: " + new Date().toString() + "\n\n\n");
                    writer.close();
                }
            } catch (final IOException e) {

                e.printStackTrace();
            }
        }
        Logger.setup(log);
        Logger.add(LogLevel.GENERAL, "Logger enabled");
    }
    
    /**
     * On Load.
     */
    @Override
    final public void onEnable() {
        PlotMain.main = this;
        // Setup the logger mechanics
        setupLogger();
        // Setup translations
        C.setupTranslations();
        C.saveTranslations();
        // Check for outdated java version.
        if (getJavaVersion() < 1.7) {
            sendConsoleSenderMessage(C.PREFIX.s() + "&cYour java version is outdated. Please update to at least 1.7.");
            // Didn't know of any other link :D
            sendConsoleSenderMessage(C.PREFIX.s() + "&cURL: &6https://java.com/en/download/index.jsp");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else if (getJavaVersion() < 1.8) {
            sendConsoleSenderMessage(C.PREFIX.s() + "&cIt's really recommended to run Java 1.8, as it increases performance");
        }
        // Setup configuration
        configs();
        defaultFlags();
        // Setup metrics
        if (Settings.METRICS) {
            try {
                final Metrics metrics = new Metrics(this);
                metrics.start();
                sendConsoleSenderMessage(C.PREFIX.s() + "&6Metrics enabled.");
            } catch (final Exception e) {
                sendConsoleSenderMessage(C.PREFIX.s() + "&cFailed to load up metrics.");
            }
        } else {
            // We should at least make them feel bad.
            sendConsoleSenderMessage("Using metrics will allow us to improve the plugin\nPlease consider it :)");
        }
        // Kill mobs on roads?
        if (Settings.KILL_ROAD_MOBS) {
            killAllEntities();
        }
        if (C.ENABLED.s().length() > 0) {
            sendConsoleSenderMessage(C.ENABLED);
        }
        final String[] tables;
        if (Settings.ENABLE_CLUSTERS) {
            MainCommand.subCommands.add(new Cluster());
            tables = new String[]{"plot_trusted", "plot_ratings", "plot_comments", "cluster"};
        }
        else {
            tables = new String[]{"plot_trusted", "plot_ratings", "plot_comments"};
        }

        // Add tables to this one, if we create more :D
        
        

        // Use mysql?
        if (Settings.DB.USE_MYSQL) {
            try {
                mySQL = new MySQL(this, Settings.DB.HOST_NAME, Settings.DB.PORT, Settings.DB.DATABASE, Settings.DB.USER, Settings.DB.PASSWORD);
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
                        // We should not repeat our self :P
                    }
                }
            } catch (final Exception e) {
                Logger.add(LogLevel.DANGER, "MySQL connection failed.");
                sendConsoleSenderMessage("&c[Plots] MySQL is not setup correctly. The plugin will disable itself.");
                if ((config == null) || config.getBoolean("debug")) {
                    sendConsoleSenderMessage("&d==== Here is an ugly stacktrace if you are interested in those things ====");
                    e.printStackTrace();
                    sendConsoleSenderMessage("&d==== End of stacktrace ====");
                    sendConsoleSenderMessage("&6Please go to the PlotSquared 'storage.yml' and configure MySQL correctly.");
                }
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
            plots = DBFunc.getPlots();
            if (Settings.ENABLE_CLUSTERS) {
            	ClusterManager.clusters = DBFunc.getClusters();
            }
        }
        // TODO: Implement mongo
        else if (Settings.DB.USE_MONGO) {
            // DBFunc.dbManager = new MongoManager();
            sendConsoleSenderMessage(C.PREFIX.s() + "MongoDB is not yet implemented");
        } else if (Settings.DB.USE_SQLITE) {
            try {
                connection = new SQLite(this, this.getDataFolder() + File.separator + Settings.DB.SQLITE_DB + ".db").openConnection();
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
                Logger.add(LogLevel.DANGER, "SQLite connection failed");
                sendConsoleSenderMessage(C.PREFIX.s() + "&cFailed to open SQLite connection. The plugin will disable itself.");
                sendConsoleSenderMessage("&9==== Here is an ugly stacktrace, if you are interested in those things ===");
                e.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
            plots = DBFunc.getPlots();
            if (Settings.ENABLE_CLUSTERS) {
            	ClusterManager.clusters = DBFunc.getClusters();
            }
        } else {
            Logger.add(LogLevel.DANGER, "No storage type is set.");
            sendConsoleSenderMessage(C.PREFIX + "&cNo storage type is set!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // Setup the command handler
        {
            final MainCommand command = new MainCommand();
            final PluginCommand plotCommand = getCommand("plots");
            plotCommand.setExecutor(command);
            plotCommand.setAliases(Arrays.asList("p", "ps", "plotme", "plot"));
            plotCommand.setTabCompleter(command);
        }
        
        // Main event handler
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
        if (checkVersion(1, 8, 0)) {
            getServer().getPluginManager().registerEvents(new PlayerEvents_1_8(), this);
        }
        // World load events
        getServer().getPluginManager().registerEvents(this, this);
        // Info Inventory
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        // Flag runnable
        PlotPlusListener.startRunnable(this);
        // Flag+ listener
        getServer().getPluginManager().registerEvents(new PlotPlusListener(), this);
        // Forcefield listener
        getServer().getPluginManager().registerEvents(new ForceFieldListener(), this);
        // Default flags

        if (getServer().getPluginManager().getPlugin("BarAPI") != null) {
            barAPI = (BarAPI) getServer().getPluginManager().getPlugin("BarAPI");
        }
        if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");

            final String version = worldEdit.getDescription().getVersion();
            if ((version != null) && version.startsWith("5.")) {
                PlotMain.sendConsoleSenderMessage("&cThis version of WorldEdit does not support PlotSquared.");
                PlotMain.sendConsoleSenderMessage("&cPlease use WorldEdit 6+ for masking support");
                PlotMain.sendConsoleSenderMessage("&c - http://builds.enginehub.org/job/worldedit");
            } else {
                getServer().getPluginManager().registerEvents(new WorldEditListener(), this);
                MainCommand.subCommands.add(new WE_Anywhere());
            }
        }
//        if (Settings.WORLDGUARD) {
//            if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
//                worldGuard = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
//                worldGuardListener = new WorldGuardListener(this);
//                getServer().getPluginManager().registerEvents(worldGuardListener, this);
//            }
//        }
        if (Settings.AUTO_CLEAR) {
            ExpireManager.runTask();
        }
        // Economy setup
        {
            if ((getServer().getPluginManager().getPlugin("Vault") != null) && getServer().getPluginManager().getPlugin("Vault").isEnabled()) {
                final RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                if (economyProvider != null) {
                    economy = economyProvider.getProvider();
                    MainCommand.subCommands.add(new Buy());
                }
            }
            useEconomy = (economy != null);
        }
        // TPS Measurement
        {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 100L, 1L);
        }
        // Test for SetBlockFast
        {
            if (checkVersion(1, 8, 0)) {
                try {
                    AbstractSetBlock.setBlockManager = new SetBlockSlow();
                }
                catch (Throwable e) {
                    e.printStackTrace();
                    AbstractSetBlock.setBlockManager = new SetBlockSlow();
                }
            }
            else {
                try {
                    AbstractSetBlock.setBlockManager = new SetBlockFast();
                } catch (Throwable e) {
                    AbstractSetBlock.setBlockManager = new SetBlockSlow();
                }
            }
            try {
                new SendChunk();
                PlotHelper.canSendChunk = true;
            } catch (final Throwable e) {
                PlotHelper.canSendChunk = false;
            }
        }
        // Setup the setup command
        {
            com.intellectualcrafters.plot.commands.plugin.setup(this);
        }
        // Handle UUIDS
        {
            boolean checkVersion = checkVersion(1, 7, 6);
            if (!checkVersion) {
                sendConsoleSenderMessage(C.PREFIX.s()+" &c[WARN] Titles are disabled - please update your version of Bukkit to support this feature.");
                Settings.TITLES = false;
                FlagManager.removeFlag(FlagManager.getFlag("titles"));
            }
            else {
            	AbstractTitle.TITLE_CLASS = new DefaultTitle();
            }
            if (Settings.OFFLINE_MODE) {
                UUIDHandler.uuidWrapper = new OfflineUUIDWrapper();
                Settings.OFFLINE_MODE = true;
            }
            else if (checkVersion) {
                UUIDHandler.uuidWrapper = new DefaultUUIDWrapper();
                Settings.OFFLINE_MODE = false;
            }
            else {
                UUIDHandler.uuidWrapper = new OfflineUUIDWrapper();
                Settings.OFFLINE_MODE = true;
            }
            if (Settings.OFFLINE_MODE) {
                sendConsoleSenderMessage(C.PREFIX.s()+" &6PlotSquared is using Offline Mode UUIDs either because of user preference, or because you are using an old version of Bukkit");
            }
            else {
                sendConsoleSenderMessage(C.PREFIX.s()+" &6PlotSquared is using online UUIDs");
            }
        }
        // Now we're finished :D
        if (C.ENABLED.s().length() > 0) {
            Broadcast(C.ENABLED);
        }
    }
    
    public static boolean checkVersion(int major, int minor, int minor2) {
        try {
            String[] version = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
            int a = Integer.parseInt(version[0]);
            int b = Integer.parseInt(version[1]);
            int c = 0;
            if (version.length == 3) {
                c = Integer.parseInt(version[2]);
            }
            if (a > major || (a == major && b > minor) || (a == major && b == minor && c >= minor2)) {
                return true;
            }
            return false;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * On unload
     */
    @Override
    final public void onDisable() {
        Logger.add(LogLevel.GENERAL, "Logger disabled");
        try {
            Logger.write();
        } catch (final IOException e1) {
            e1.printStackTrace();
        }
        try {
            connection.close();
            mySQL.closeConnection();
        } catch (NullPointerException | SQLException e) {
            if (connection != null) {
                Logger.add(LogLevel.DANGER, "Could not close mysql connection");
            }
        }
    }
}
