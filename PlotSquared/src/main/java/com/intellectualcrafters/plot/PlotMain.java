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

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.commands.Auto;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.commands.WE_Anywhere;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.*;
import com.intellectualcrafters.plot.events.PlayerTeleportToPlotEvent;
import com.intellectualcrafters.plot.events.PlotDeleteEvent;
import com.intellectualcrafters.plot.flag.AbstractFlag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.generator.HybridPlotManager;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.listeners.*;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.*;
import com.intellectualcrafters.plot.util.Logger.LogLevel;
import com.intellectualcrafters.plot.uuid.DefaultUUIDWrapper;
import com.intellectualcrafters.plot.uuid.OfflineUUIDWrapper;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import me.confuser.barapi.BarAPI;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.*;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * PlotMain class.
 *
 * @author Citymonstret
 * @author Empire92
 */
@SuppressWarnings("unused") public class PlotMain extends JavaPlugin implements Listener {
    /**
     * Permission that allows for "everything"
     */
    public static final String ADMIN_PERMISSION = "plots.admin";
    /**
     * Storage version
     */
    public final static int storage_ver = 1;
    /**
     * Boolean Flags (material)
     */
    public final static HashMap<Material, String> booleanFlags = new HashMap<>();
    /**
     * Initialize the material flags
     */
    static {
        booleanFlags.put(Material.WOODEN_DOOR, "wooden_door");
        booleanFlags.put(Material.IRON_DOOR, "iron_door");
        booleanFlags.put(Material.STONE_BUTTON, "stone_button");
        booleanFlags.put(Material.WOOD_BUTTON, "wooden_button");
        booleanFlags.put(Material.LEVER, "lever");
        booleanFlags.put(Material.WOOD_PLATE, "wooden_plate");
        booleanFlags.put(Material.STONE_PLATE, "stone_plate");
        booleanFlags.put(Material.CHEST, "chest");
        booleanFlags.put(Material.TRAPPED_CHEST, "trapped_chest");
        booleanFlags.put(Material.TRAP_DOOR, "trap_door");
        booleanFlags.put(Material.DISPENSER, "dispenser");
        booleanFlags.put(Material.DROPPER, "dropper");
    }
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
     * Check for expired plots
     */
    public static void checkForExpiredPlots() {
        final JavaPlugin plugin = PlotMain.getMain();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    checkExpired(plugin, true);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0l, 86_40_00L);
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
        if ((player == null) || player.isOp()) {
            return true;
        }
        if (player.hasPermission(perm)) {
            return true;
        }
        final String[] nodes = perm.split("\\.");
        final StringBuilder n = new StringBuilder();
        for (int i = 0; i < (nodes.length - 1); i++) {
            n.append(nodes[i]).append(".");
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
     * TODO: <b>Implement better system The whole point of this system is to recycle old plots</b> <br> So why not just
     * allow users to claim old plots, and try to hide the fact that the are owned. <br> <br> Reduce amount of expired
     * plots: <br> - On /plot <br> auto<br> - allow claiming of old plot, clear it so the user doesn't know<br> - On
     * /plot info,<br> - show that the plot is expired and allowed to be claimed Have the task run less often:<br> - Run
     * the task when there are very little, or no players online (great for small servers)<br> - Run the task at startup
     * (also only useful for small servers)<br> Also, in terms of faster code:<br> - Have an array of plots, sorted by
     * expiry time.<br> - Add new plots to the end.<br> - The task then only needs to go through the first few plots
     *
     * @param plugin Plugin
     * @param async  Call async?
     */
    private static void checkExpired(final JavaPlugin plugin, final boolean async) {
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    for (final String world : getPlotWorldsString()) {
                        if (plots.containsKey(world)) {

                            final ArrayList<Plot> toDeletePlot = new ArrayList<>();

                            for (final Plot plot : plots.get(world).values()) {
                                if (plot.owner == null) {
                                    continue;
                                }
                                final long lastPlayed = getLastPlayed(plot.owner);
                                if (lastPlayed == 0) {
                                    continue;
                                }
                                final long compared = System.currentTimeMillis() - lastPlayed;
                                if (TimeUnit.MILLISECONDS.toDays(compared) >= Settings.AUTO_CLEAR_DAYS) {
                                    final PlotDeleteEvent event = new PlotDeleteEvent(world, plot.id);
                                    Bukkit.getServer().getPluginManager().callEvent(event);
                                    if (event.isCancelled()) {
                                        event.setCancelled(true);
                                    } else {
                                        toDeletePlot.add(plot);
                                    }
                                }
                            }
                            for (final Plot plot : toDeletePlot) {
                                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                    @Override
                                    public void run() {
                                        final World worldobj = Bukkit.getWorld(world);
                                        PlotHelper.clear(worldobj, plot, true);
                                        PlotHelper.removeSign(worldobj, plot);
                                        DBFunc.delete(world, plot);
                                        removePlot(world, plot.id, true);
                                        if ((Math.abs(plot.id.x) < Math.abs(Auto.lastPlot.x)) && (Math.abs(plot.id.y) < Math.abs(Auto.lastPlot.y))) {
                                            Auto.lastPlot = plot.id;
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            });
        } else {
            for (final String world : getPlotWorldsString()) {
                if (PlotMain.plots.containsKey(world)) {
                    for (final Plot plot : PlotMain.plots.get(world).values()) {
                        if (PlayerFunctions.hasExpired(plot)) {
                            final PlotDeleteEvent event = new PlotDeleteEvent(world, plot.id);
                            Bukkit.getServer().getPluginManager().callEvent(event);
                            if (event.isCancelled()) {
                                event.setCancelled(true);
                            } else {
                                DBFunc.delete(world, plot);
                            }
                        }
                    }
                }
            }
        }
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
        if (getMain().getServer().getConsoleSender() == null) {
            System.out.println(ChatColor.stripColor(ConsoleColors.fromString(string)));
        } else {
            getMain().getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', string));
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
        final PlayerTeleportToPlotEvent event = new PlayerTeleportToPlotEvent(player, from, plot);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            final Location location = PlotHelper.getPlotHome(Bukkit.getWorld(plot.world), plot);
            if ((location.getBlockX() >= 29999999) || (location.getBlockX() <= -29999999) || (location.getBlockZ() >= 299999999) || (location.getBlockZ() <= -29999999)) {
                event.setCancelled(true);
                return false;
            }
            player.teleport(location);
            PlayerFunctions.sendMessage(player, C.TELEPORTED_TO_PLOT);
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
    private static void setupConfig() {
        final int config_ver = 1;
        config.set("version", config_ver);
        final Map<String, Object> options = new HashMap<>();
        options.put("auto_update", false);
        options.put("claim.max-auto-area", Settings.MAX_AUTO_SIZE);
        options.put("UUID.offline", Settings.OFFLINE_MODE);
        options.put("worldguard.enabled", Settings.WORLDGUARD);
        options.put("kill_road_mobs", Settings.KILL_ROAD_MOBS_DEFAULT);
        options.put("mob_pathfinding", Settings.MOB_PATHFINDING_DEFAULT);
        options.put("metrics", true);
        options.put("debug", true);
        options.put("clear.auto.enabled", false);
        options.put("clear.auto.days", 365);
        options.put("clear.on.ban", false);
        options.put("max_plots", Settings.MAX_PLOTS);
        options.put("schematics.save_path", Settings.SCHEMATIC_SAVE_PATH);
        options.put("uuid.api.location", Settings.API_URL);
        options.put("uuid.api.custom", Settings.CUSTOM_API);
        options.put("uuid.fecthing", Settings.UUID_FECTHING);
        options.put("UUID.read-from-disk", Settings.UUID_FROM_DISK);
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
        Settings.DEBUG = config.getBoolean("debug");
        if (Settings.DEBUG) {
            sendConsoleSenderMessage(C.PREFIX.s() + "&6Debug Mode Enabled (Default). Edit the config to turn this off.");
        }
        Settings.TELEPORT_ON_LOGIN = config.getBoolean("teleport.on_login");
        Settings.KILL_ROAD_MOBS = config.getBoolean("kill_road_mobs");
        Settings.WORLDGUARD = config.getBoolean("worldguard.enabled");
        Settings.MOB_PATHFINDING = config.getBoolean("mob_pathfinding");
        Settings.METRICS = config.getBoolean("metrics");
        Settings.AUTO_CLEAR_DAYS = config.getInt("clear.auto.days");
        Settings.MAX_AUTO_SIZE = config.getInt("claim.max-auto-area");
        Settings.AUTO_CLEAR = config.getBoolean("clear.auto.enabled");
        Settings.TITLES = config.getBoolean("titles");
//        Settings.MOB_CAP_ENABLED = config.getBoolean("perm-based-mob-cap.enabled");
//        Settings.MOB_CAP = config.getInt("perm-based-mob-cap.max");
        Settings.MAX_PLOTS = config.getInt("max_plots");
        Settings.SCHEMATIC_SAVE_PATH = config.getString("schematics.save_path");

        Settings.OFFLINE_MODE = config.getBoolean("UUID.offline");
        Settings.UUID_FROM_DISK = config.getBoolean("UUID.read-from-disk");

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
    public static void worldLoad(WorldLoadEvent event) {
        if (!UUIDHandler.CACHED) {
            UUIDHandler.cacheAll();
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

        if ((generator != null) && (generator instanceof PlotGenerator)) {
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
        } else {
            if (worlds.contains(world)) {
                sendConsoleSenderMessage("&cWorld '" + world + "' in settings.yml is not using PlotSquared generator!");

                plotWorld = new HybridPlotWorld(world);
                plotManager = new HybridPlotManager();

                if (!config.contains(path)) {
                    config.createSection(path);
                }

                plotWorld.saveConfiguration(config.getConfigurationSection(path));
                plotWorld.loadConfiguration(config.getConfigurationSection(path));

                try {
                    config.save(configFile);
                } catch (final IOException e) {
                    e.printStackTrace();
                }

                // Now add it :p
                addPlotWorld(world, plotWorld, plotManager);
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
        options.put("mysql.use", true);
        options.put("sqlite.use", false);
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
        final List<String> booleanFlags = Arrays.asList("notify-enter", "notify-leave", "item-drop", "invincible", "instabreak", "drop-protection", "forcefield", "titles", "pve", "pvp");
        final List<String> intervalFlags = Arrays.asList("feed", "heal");
        final List<String> stringFlags = Arrays.asList("greeting", "farewell");
        for (final String flag : stringFlags) {
            FlagManager.addFlag(new AbstractFlag(flag) {
                @Override
                public String parseValue(final String value) {
                    return value;
                }

                @Override
                public String getValueDesc() {
                    return "Value must be a string, supports color codes (&)";
                }
            });
        }
        for (final String flag : intervalFlags) {
            FlagManager.addFlag(new AbstractFlag(flag) {
                @Override
                public String parseValue(final String value) {
                    int seconds;
                    int amount;
                    final String[] values = value.split(" ");
                    if (values.length < 2) {
                        seconds = 1;
                        try {
                            amount = Integer.parseInt(values[0]);
                        } catch (final Exception e) {
                            return null;
                        }
                    } else {
                        try {
                            amount = Integer.parseInt(values[0]);
                            seconds = Integer.parseInt(values[1]);
                        } catch (final Exception e) {
                            return null;
                        }
                    }
                    return amount + " " + seconds;
                }

                @Override
                public String getValueDesc() {
                    return "Value(s) must be numeric. /plot set flag {flag} {amount} [seconds]";
                }
            });
        }
        for (final String flag : booleanFlags) {
            FlagManager.addFlag(new AbstractFlag(flag) {
                @Override
                public String parseValue(final String value) {
                    switch (value) {
                        case "on":
                        case "1":
                        case "true":
                        case "enabled":
                            return "true";
                        case "off":
                        case "0":
                        case "false":
                        case "disabled":
                            return "false";
                        default:
                            return null;
                    }
                }

                @Override
                public String getValueDesc() {
                    return "Value must be true/false, 1/0, on/off, enabled/disabled";
                }
            });
        }
    }

    private static void defaultFlags() {
        addPlusFlags();
        FlagManager.addFlag(new AbstractFlag("fly") {
            @Override
            public String parseValue(final String value) {
                switch (value) {
                    case "on":
                    case "enabled":
                    case "true":
                    case "1":
                        return "true";
                    case "off":
                    case "disabled":
                    case "false":
                    case "0":
                        return "false";
                    default:
                        return null;
                }
            }

            @Override
            public String getValueDesc() {
                return "Flag value must be a boolean: true, false, enabled, disabled";
            }
        });

        for (final String str : booleanFlags.values()) {
            FlagManager.addFlag(new AbstractFlag(str) {

                @Override
                public String parseValue(final String value) {
                    switch (value) {
                        case "true":
                        case "1":
                        case "yes":
                            return "true";
                        case "false":
                        case "off":
                        case "0":
                            return "false";
                        default:
                            return null;
                    }
                }

                @Override
                public String getValueDesc() {
                    return "Flag value must be a boolean: true, false";
                }

            });
        }

        FlagManager.addFlag(new AbstractFlag("gamemode") {
            @Override
            public String parseValue(final String value) {
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

        FlagManager.addFlag(new AbstractFlag("time") {
            @Override
            public String parseValue(final String value) {
                try {
                    return Long.parseLong(value) + "";
                } catch (final Exception e) {
                    return null;
                }
            }

            @Override
            public String getValueDesc() {
                return "Flag value must be a time in ticks: 0=sunrise 12000=noon 18000=sunset 24000=night";
            }
        });

        FlagManager.addFlag(new AbstractFlag("weather") {
            @Override
            public String parseValue(final String value) {
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
    @SuppressWarnings("deprecation")
    final public void onEnable() {
        PlotMain.main = this;
        // Setup the logger mechanics
        setupLogger();
        // Setup translations
        C.setupTranslations();
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
            Broadcast(C.ENABLED);
        }

        // Add tables to this one, if we create more :D
        final String[] tables = new String[]{"plot_trusted", "plot_ratings", "plot_comments"};

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
        }
        // TODO: Implement mongo
        else if (Settings.DB.USE_MONGO) {
            // DBFunc.dbManager = new MongoManager();
            sendConsoleSenderMessage(C.PREFIX.s() + "MongoDB is not yet implemented");
        } else if (Settings.DB.USE_SQLITE) {
            try {
                connection = new SQLite(this, Settings.DB.SQLITE_DB + ".db").openConnection();
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
        } else {
            Logger.add(LogLevel.DANGER, "No storage type is set.");
            sendConsoleSenderMessage(C.PREFIX + "&cNo storage type is set!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // We should not start the plugin if
        // plotme is present. Maybe do this
        // earlier, and no register any commands
        // nor listeners, just run the converter?
        if (getServer().getPluginManager().getPlugin("PlotMe") != null) {
            try {
                new PlotMeConverter(this).runAsync();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        // Setup the command handler
        {
            final MainCommand command = new MainCommand();
            final PluginCommand plotCommand = getCommand("plots");
            plotCommand.setExecutor(command);
            plotCommand.setAliases(Arrays.asList("p", "ps", "plotme", "plot"));
            plotCommand.setTabCompleter(command);
        }
//        if (Settings.MOB_CAP_ENABLED) {
//            getServer().getPluginManager().registerEvents(new EntityListener(), this);
//        }

        // Main event handler
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
        // World load events
        getServer().getPluginManager().registerEvents(this, this);
        // Info Inventory
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        // Flag runnable
        PlotPlusListener.startRunnable(this);
        // Flag+ listener
        getServer().getPluginManager().registerEvents(new PlotPlusListener(), this);
        // Forcefield listener
        getServer().getPluginManager().registerEvents(new ForceFieldListener(this), this);
        // Default flags
        defaultFlags();

        if (getServer().getPluginManager().getPlugin("BarAPI") != null) {
            barAPI = (BarAPI) getServer().getPluginManager().getPlugin("BarAPI");
        }
        if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");

            final String version = worldEdit.getDescription().getVersion();
            if ((version != null) && version.startsWith("5.")) {
                PlotMain.sendConsoleSenderMessage("&cThis version of WorldEdit does not support PlotSquared.");
                PlotMain.sendConsoleSenderMessage("&cPlease use WorldEdit 6+");
                PlotMain.sendConsoleSenderMessage("&c - http://builds.enginehub.org/job/worldedit");
            } else {
                getServer().getPluginManager().registerEvents(new WorldEditListener(), this);
                MainCommand.subCommands.add(new WE_Anywhere());
            }
        }
        if (Settings.WORLDGUARD) {
            if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
                worldGuard = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
                worldGuardListener = new WorldGuardListener(this);
                getServer().getPluginManager().registerEvents(worldGuardListener, this);
            }
        }
        if (Settings.AUTO_CLEAR) {
            checkExpired(PlotMain.getMain(), true);
            checkForExpiredPlots();
        }
        // Economy setup
        {
            if ((getServer().getPluginManager().getPlugin("Vault") != null) && getServer().getPluginManager().getPlugin("Vault").isEnabled()) {
                final RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                if (economyProvider != null) {
                    economy = economyProvider.getProvider();
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
            try {
                new SetBlockFast();
                PlotHelper.canSetFast = true;
            } catch (final Throwable e) {
                PlotHelper.canSetFast = false;
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
            boolean checkVersion = checkVersion();
            if (!checkVersion && Settings.TITLES) {
                sendConsoleSenderMessage(C.PREFIX.s()+" &c[WARN] Titles are enabled - please update your version of Bukkit to support this feature.");
                Settings.TITLES = false;
                FlagManager.removeFlag(FlagManager.getFlag("titles"));
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
    
    public static boolean checkVersion() {
        try {
            String[] version = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
            int a = Integer.parseInt(version[0]);
            int b = Integer.parseInt(version[1]);
            int c = 0;
            if (version.length == 3) {
                c = Integer.parseInt(version[2]);
            }
            if (a < 2) {
                if (a < 1) {
                    return false;
                }
                if (b < 7) {
                    return false;
                }
                if (b == 7 && c < 5) {
                    return false;
                }
            }
            return true;
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
        try {
            C.saveTranslations();
        } catch (final Exception e) {
            sendConsoleSenderMessage("Failed to save translations");
            Logger.add(LogLevel.DANGER, "Failed to save translations");
            e.printStackTrace();
        }
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
