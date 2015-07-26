package com.intellectualcrafters.plot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.intellectualcrafters.configuration.file.YamlConfiguration;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.database.Database;
import com.intellectualcrafters.plot.database.MySQL;
import com.intellectualcrafters.plot.database.SQLManager;
import com.intellectualcrafters.plot.database.SQLite;
import com.intellectualcrafters.plot.flag.AbstractFlag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.flag.FlagValue;
import com.plotsquared.bukkit.generator.AugmentedPopulator;
import com.intellectualcrafters.plot.generator.ClassicPlotWorld;
import com.plotsquared.bukkit.generator.HybridGen;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.generator.PlotGenerator2;
import com.intellectualcrafters.plot.generator.SquarePlotManager;
import com.intellectualcrafters.plot.generator.SquarePlotWorld;
import com.plotsquared.bukkit.listeners.APlotListener;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotFilter;
import com.intellectualcrafters.plot.object.PlotHandler;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.plotsquared.bukkit.object.comment.CommentManager;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.ClusterManager;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.InventoryUtil;
import com.intellectualcrafters.plot.util.Logger;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.Logger.LogLevel;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlayerManager;
import com.plotsquared.bukkit.util.SetupUtils;
import com.plotsquared.bukkit.util.UUIDHandler;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.bukkit.util.bukkit.uuid.FileUUIDHandler;
import com.plotsquared.bukkit.util.bukkit.uuid.SQLUUIDHandler;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

/**
 * An implementation of the core,
 * with a static getter for easy access
 *
 * @author Sauilitired | Citymonstret
 * @author boy0001 | Empire92
 */
public class PS {

    // protected static:
    public static PS instance;
    
    // private final:
    private final HashMap<String, PlotWorld> plotworlds = new HashMap<>();
    private final HashMap<String, PlotManager> plotmanagers = new HashMap<>();

    // public:
    public WorldEditPlugin worldEdit = null;
    public File configFile;
    public File translationFile;
    public YamlConfiguration style;
    public YamlConfiguration config;
    public YamlConfiguration storage;
    public IPlotMain IMP = null;
    public TaskManager TASK;
    public URL update;

    // private:
    private File styleFile;
    private File storageFile;
    private File FILE = null; // This file
    private int[] VERSION = null;
    private String LAST_VERSION;
    private boolean LOADING_WORLD = false;
    private LinkedHashMap<String, HashMap<PlotId, Plot>> plots;
    private Database database;
    private Connection connection;

    /**
     * Initialize PlotSquared with the desired Implementation class
     * @param imp_class
     */
    public PS(final IPlotMain imp_class) {
        instance = this;
        SetupUtils.generators = new HashMap<>();
        IMP = imp_class;
        try {
            FILE = new File(PS.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (Exception e) {
            log("Could not determine file path");
        }
        VERSION = IMP.getPluginVersion();
        EconHandler.manager = IMP.getEconomyHandler();
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
        this.TASK = IMP.getTaskManager();
        if (C.ENABLED.s().length() > 0) {
            log(C.ENABLED.s());
        }
        setupConfigs();
        this.translationFile = new File(IMP.getDirectory() + File.separator + "translations" + File.separator + "PlotSquared.use_THIS.yml");
        C.load(translationFile);
        setupDefaultFlags();
        setupDatabase();
        CommentManager.registerDefaultInboxes();
        // Tasks
        if (Settings.KILL_ROAD_MOBS) {
            IMP.runEntityTask();
        }
        // Events
        IMP.registerCommands();
        IMP.registerPlayerEvents();
        IMP.registerInventoryEvents();
        IMP.registerPlotPlusEvents();
        IMP.registerForceFieldEvents();
        IMP.registerWorldEditEvents();
        IMP.registerWorldEvents();
        if (Settings.TNT_LISTENER) {
            IMP.registerTNTListener();
        }
        if (Settings.CHUNK_PROCESSOR) {
            IMP.registerChunkProcessor();
        }
        // create UUIDWrapper
        UUIDHandler.setUUIDWrapper(IMP.initUUIDHandler());
        // create event util class
        EventUtil.manager = IMP.initEventUtil();
        // create Hybrid utility class
        HybridUtils.manager = IMP.initHybridUtils();
        // Inventory utility class
        InventoryUtil.manager = IMP.initInventoryUtil();
        // create setup util class
        SetupUtils.manager = IMP.initSetupUtils();
        // Set block
        BlockManager.manager = IMP.initBlockManager();
        // Set chunk
        ChunkManager.manager = IMP.initChunkManager();
        // Plot listener
        APlotListener.manager = IMP.initPlotListener();
        // Player manager
        PlayerManager.manager = IMP.initPlayerManager();

        // Check for updates
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                URL url = getUpdate();
                if (url != null) {
                    update = url;
                    log("&6You are running an older version of PlotSquared...");
                    log("&8 - &3Use: &7/plot update");
                    log("&8 - &3Or: &7" + url);
                }
                else if (LAST_VERSION != null && !VERSION.equals(LAST_VERSION)) {
                    log("&aThanks for updating from: " + LAST_VERSION + " to " + VERSION);
                }
            }
        });
        
        // PlotMe
        if (Settings.CONVERT_PLOTME || Settings.CACHE_PLOTME) {
            TaskManager.runTaskLater(new Runnable() {

                public void run() {
                    if (IMP.initPlotMeConverter()) {
                        log("&c=== IMPORTANT ===");
                        log("&cTHIS MESSAGE MAY BE EXTREMELY HELPFUL IF YOU HAVE TROUBLE CONVERTING PLOTME!");
                        log("&c - Make sure 'UUID.read-from-disk' is disabled (false)!");
                        log("&c - Sometimes the database can be locked, deleting PlotMe.jar beforehand will fix the issue!");
                        log("&c - After the conversion is finished, please set 'plotme-convert.enabled' to false in the 'settings.yml'");
                    }
                }
            }, 200);
        }
        if (Settings.AUTO_CLEAR) {
            ExpireManager.runTask();
        }

        // Copy files
        copyFile("town.template", "templates");
        copyFile("skyblock.template", "templates");
        copyFile("german.yml", "translations");
        copyFile("s_chinese_unescaped.yml", "translations");
        copyFile("s_chinese.yml", "translations");
        copyFile("italian.yml", "translations");
        showDebug();
    }
    
    public boolean checkVersion(int[] version, int major, int minor, int minor2) {
        return (version[0] > major) || ((version[0] == major) && (version[1] > minor)) || ((version[0] == major) && (version[1] == minor) && (version[2] >= minor2));
    }
    
    /**
     * Get the instance of PlotSquared
     *
     * @return the instance created by IPlotMain
     */
    public static PS get() {
        return instance;
    }
    
    /**
     * Get the last PlotSquared version
     * @return last version in config or null
     */
    public String getLastVersion() {
        return LAST_VERSION;
    }
    
    /**
     * Get the current PlotSquared version
     * @return current version in config or null
     */
    public int[] getVersion() {
        return VERSION;
    }

    /**
     * Log a message to the IPlotMain logger
     *
     * @param message Message to log
     * @see IPlotMain#log(String)
     */
    public static void log(final String message) {
        get().IMP.log(message);
    }

    /**
     * Get the database object
     *
     * @return Database object
     * @see #getConnection() Get the database connection
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Update the global reference
     * to a plot object
     *
     * @param plot Plot Object to update
     */
    public void updatePlot(final Plot plot) {
        final String world = plot.world;
        if (!plots.containsKey(world)) {
            plots.put(world, new HashMap<PlotId, Plot>());
        }
        plots.get(world).put(plot.id, plot);
    }

    /**
     * Get the plot world based on the
     * name identifier
     *
     * @param world World Name
     * @return plot world | null if not existing
     * @see #getPlotWorldsString() Get all plot world names
     */
    public PlotWorld getPlotWorld(final String world) {
        if (plotworlds.containsKey(world)) {
            return plotworlds.get(world);
        }
        return null;
    }

    /**
     * Add a global reference to a plot world
     *
     * @param world     World Name
     * @param plotworld PlotWorld Instance
     * @param manager   PlotManager
     * @see #removePlotWorld(String) To remove the reference
     */
    public void addPlotWorld(final String world, final PlotWorld plotworld, final PlotManager manager) {
        plotworlds.put(world, plotworld);
        plotmanagers.put(world, manager);
        if (!plots.containsKey(world)) {
            plots.put(world, new HashMap<PlotId, Plot>());
        }
    }

    /**
     * Remove a plot world reference
     *
     * @param world World name
     * @see #addPlotWorld(String, PlotWorld, PlotManager) To add a reference
     */
    public void removePlotWorld(final String world) {
        plots.remove(world);
        plotmanagers.remove(world);
        plotworlds.remove(world);
    }

    /**
     * @param world World Name
     */
    public void removePlotWorldAbs(final String world) {
        plotmanagers.remove(world);
        plotworlds.remove(world);
    }

    /**
     * Get all plots as raw objects
     *
     * @see #getPlots() To get the plot objects
     * @see #getPlotsRaw() To get the plot objects
     *
     * @return HashMap containing the world name, and another map with the plot id and the plot object
     */
    public HashMap<String, HashMap<PlotId, Plot>> getAllPlotsRaw() {
        return plots;
    }
    
    /**
     * A more generic way to filter plots - make your own method if you need complex filters
     * @param filters
     * @return
     */
    public Set<Plot> getPlots(PlotFilter... filters) {
        HashSet<Plot> set = new HashSet<>();
        for (Entry<String, HashMap<PlotId, Plot>> entry : plots.entrySet()) {
            for (PlotFilter filter : filters) {
                if (!filter.allowsWorld(entry.getKey())) {
                    continue;
                }
            }
            for (Plot plot : entry.getValue().values()) {
                for (PlotFilter filter : filters) {
                    if (!filter.allowsPlot(plot)) {
                        continue;
                    }
                }
                set.add(plot);
            }
        }
        return set;
    }
    
    


    /**
     * Set all the plots as a raw object
     * @see #getAllPlotsRaw() to get the raw plot object
     * @param plots
     */
    public void setAllPlotsRaw(final LinkedHashMap<String, HashMap<PlotId, Plot>> plots) {
        this.plots = plots;
    }


    /**
     * Get all the plots in a single set
     * @return Set of Plot
     */
    public Set<Plot> getPlots() {
        final ArrayList<Plot> newplots = new ArrayList<>();
        for (final Entry<String, HashMap<PlotId, Plot>> entry : plots.entrySet()) {
            if (isPlotWorld(entry.getKey())) {
                newplots.addAll(entry.getValue().values());
            }
        }
        return new LinkedHashSet<>(newplots);
    }


    /**
     * Get the raw plot object
     * @return set of plot
     * @see #setAllPlotsRaw(LinkedHashMap) to set the raw plot object
     */
    public Set<Plot> getPlotsRaw() {
        final ArrayList<Plot> newplots = new ArrayList<>();
        for (final Entry<String, HashMap<PlotId, Plot>> entry : plots.entrySet()) {
            newplots.addAll(entry.getValue().values());
        }
        return new LinkedHashSet<>(newplots);
    }


    /**
     * Sort a collection of plots by the hashcode (assumes that all plots are in the same world)
     * @param plots
     * @return ArrayList of plot
     */
    @Deprecated
    public ArrayList<Plot> sortPlots(Collection<Plot> plots) {
        return sortPlotsByWorld(plots);
    }

    /**
     * Sort plots by hashcode
     * @param plots
     * @return
     */
    public ArrayList<Plot> sortPlotsByHash(Collection<Plot> plots) {
        int hardmax = 256000;
        int max = 0;
        int overflowSize = 0;
        for (Plot plot : plots) {
            int hash = MathMan.getPositiveId(plot.hashCode());
            if (hash > max) {
                if (hash >= hardmax) {
                    overflowSize++;
                }
                else {
                    max = hash;
                }
            }
        }
        hardmax = Math.min(hardmax, max);
        Plot[] cache = new Plot[hardmax + 1];
        List<Plot> overflow = new ArrayList<Plot>(overflowSize);
        for (Plot plot : plots) {
            int hash = MathMan.getPositiveId(plot.hashCode());
            if (hash < hardmax) {
                cache[hash] = plot;
            }
            else {
                overflow.add(plot);
            }
        }
        Plot[] overflowArray = overflow.toArray(new Plot[overflow.size()]);
        sortPlotsByHash(overflowArray);
        ArrayList<Plot> result = new ArrayList<Plot>(cache.length + overflowArray.length);
        for (Plot plot : cache) {
            if (plot != null) {
                result.add(plot);
            }
        }
        for (Plot plot : overflowArray) {
            result.add(plot);
        }
        return result;
    }
    
    public static void sortPlotsByHash(Plot[] input) {
        final int SIZE = 100;
        List<Plot>[] bucket = new ArrayList[SIZE];
        for (int i = 0; i < bucket.length; i++) {
            bucket[i] = new ArrayList<Plot>();
        }
        boolean maxLength = false;
        int tmp = -1, placement = 1;
        while (!maxLength) {
            maxLength = true;
            for (Plot i : input) {
                tmp = MathMan.getPositiveId(i.hashCode()) / placement;
                bucket[tmp % SIZE].add(i);
                if (maxLength && tmp > 0) {
                    maxLength = false;
                }
            }
            int a = 0;
            for (int b = 0; b < SIZE; b++) {
                for (Plot i : bucket[b]) {
                    input[a++] = i;
                }
                bucket[b].clear();
            }
            placement *= SIZE;
        }
    }
    
    /**
     * Sort a collection of plots by world (with a priority world), then by hashcode
     * @param plots
     * @param priorityWorld - Use "world" or "gibberish" if you don't care
     * @see #sortPlotsByWorld(Collection) to sort plots by world, then by hashcode
     * @see #sortPlots(Collection) to sort plots just by hashcode
     * @return ArrayList of plot
     */
    public ArrayList<Plot> sortPlots(Collection<Plot> plots, final String priorityWorld) {
        // group by world
        // sort each
        HashMap<String, Collection<Plot>> map = new HashMap<>();
        ArrayList<String> worlds = new ArrayList<String>(getPlotWorlds());
        int totalSize = 0;
        for (Entry<String, HashMap<PlotId, Plot>> entry : this.plots.entrySet()) {
            totalSize += entry.getValue().size();
        }
        if (plots.size() == totalSize) {
            for (Entry<String, HashMap<PlotId, Plot>> entry : this.plots.entrySet()) {
                map.put(entry.getKey(), entry.getValue().values());
            }
        }
        else {
            for (String world : worlds) {
                map.put(world, new ArrayList<Plot>(plots.size() / worlds.size()));
            }
            Collection<Plot> lastList = null;
            String lastWorld = "";
            for (Plot plot : plots) {
                if (StringMan.isEqual(lastWorld, plot.world)) {
                    lastList.add(plot);
                }
                else {
                    lastWorld = plot.world;
                    lastList = map.get(lastWorld);
                    lastList.add(plot);
                }
            }
        }
        Collections.sort(worlds, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                if (StringMan.isEqual(a, priorityWorld)) {
                    return -1;
                }
                return a.hashCode() - b.hashCode();
            }
        });
        ArrayList<Plot> toReturn = new ArrayList<Plot>(plots.size());
        for (String world : worlds) {
            toReturn.addAll(sortPlotsByHash(map.get(world)));
        }
        return toReturn;
    }


    /**
     * Sort a collection of plots by world, then by hashcode
     * @param plots
     * @see #sortPlots(Collection, String) to sort with a specific priority world
     * @see #sortPlots(Collection) to sort plots just by hashcode
     * @return ArrayList of plot
     */
    public ArrayList<Plot> sortPlotsByWorld(Collection<Plot> plots) {
        ArrayList<Plot> newPlots = new ArrayList<>();
        ArrayList<String> worlds = new ArrayList<>(this.plots.keySet());
        HashSet<Plot> set = new HashSet<>(plots);
        Collections.sort(worlds);
        for (String world : worlds) {
            for (Plot plot : this.plots.get(world).values()) {
                if (set.contains(plot)) {
                    newPlots.add(plot);
                }
            }
        }
        return newPlots;
    }


    /**
     * Get all the plots owned by a player name
     * @param world
     * @param player
     * @return Set of Plot
     */
    public Set<Plot> getPlots(final String world, final String player) {
        final UUID uuid = UUIDHandler.getUUID(player);
        return getPlots(world, uuid);
    }

    /**
     * Get all plots by a PlotPlayer
     * @param world
     * @param player
     * @return Set of plot
     */
    public Set<Plot> getPlots(final String world, final PlotPlayer player) {
        final UUID uuid = player.getUUID();
        return getPlots(world, uuid);
    }

    /**
     * Get all plots by a UUID in a world
     * @param world
     * @param uuid
     * @return Set of plot
     */
    public Set<Plot> getPlots(final String world, final UUID uuid) {
        final ArrayList<Plot> myplots = new ArrayList<>();
        for (final Plot plot : getPlots(world).values()) {
            if (plot.hasOwner()) {
                if (PlotHandler.isOwner(plot, uuid)) {
                    myplots.add(plot);
                }
            }
        }
        return new HashSet<>(myplots);
    }


    /**
     * Check if a plot world
     * @param world
     * @see #getPlotWorld(String) to get the PlotWorld object
     * @return if a plot world is registered
     */
    public boolean isPlotWorld(final String world) {
        return (plotworlds.containsKey(world));
    }

    /**
     * Get the plot manager for a world
     * @param world
     * @return the PlotManager object, or null if no registered PlotManager
     */
    public PlotManager getPlotManager(final String world) {
        if (plotmanagers.containsKey(world)) {
            return plotmanagers.get(world);
        }
        return null;
    }

    /**
     * Get a list of the plot worlds
     * @return A String array of the plot world names
     */
    public String[] getPlotWorldsString() {
        final Set<String> strings = plots.keySet();
        return strings.toArray(new String[strings.size()]);
    }


    /**
     * Get a map of the plots for a world
     * @param world
     * @return HashMap of PlotId to Plot
     */
    public HashMap<PlotId, Plot> getPlots(final String world) {
        if (plots.containsKey(world)) {
            return plots.get(world);
        }
        return new HashMap<>();
    }


    /**
     * Get the plots for a PlotPlayer
     * @param player
     * @return Set of Plot
     */
    public Set<Plot> getPlots(final PlotPlayer player) {
        return getPlots(player.getUUID());
    }


    /**
     * Get the plots for a UUID
     * @param uuid
     * @return Set of Plot
     */
    public Set<Plot> getPlots(final UUID uuid) {
        final ArrayList<Plot> myplots = new ArrayList<>();
        for (final String world : plots.keySet()) {
            if (isPlotWorld(world)) {
                for (final Plot plot : plots.get(world).values()) {
                    if (plot.hasOwner()) {
                        if (PlotHandler.isOwner(plot, uuid)) {
                            myplots.add(plot);
                        }
                    }
                }
            }
        }
        return new HashSet<>(myplots);
    }


    /**
     * Unregister a plot from local memory (does not call DB)
     * @param world
     * @param id
     * @param callEvent If to call an event about the plot being removed
     * @return true if plot existed | false if it didn't 
     */
    public boolean removePlot(final String world, final PlotId id, final boolean callEvent) {
        if (callEvent) {
            EventUtil.manager.callDelete(world, id);
        }
        HashMap<PlotId, Plot> allPlots = plots.get(world);
        if (allPlots == null) {
            return false;
        }
        Plot plot = allPlots.remove(id);
        if (MainUtil.lastPlot.containsKey(world)) {
            final PlotId last = MainUtil.lastPlot.get(world);
            final int last_max = Math.max(last.x, last.y);
            final int this_max = Math.max(id.x, id.y);
            if (this_max < last_max) {
                MainUtil.lastPlot.put(world, id);
            }
        }
        return plot != null;
    }


    /**
     * This method is called by the PlotGenerator class normally<br>
     *  - Initializes the PlotWorld and PlotManager classes<br>
     *  - Registers the PlotWorld and PlotManager classes<br>
     *  - Loads (and/or generates) the PlotWorld configuration<br>
     *  - Sets up the world border if configured<br>
     *  If loading an augmented plot world:<br>
     *  - Creates the AugmentedPopulator classes<br>
     *  - Injects the AugmentedPopulator classes if required
     * @param world The world to load
     * @param generator The generator for that world, or null if no generator
     */
    public void loadWorld(final String world, PlotGenerator2 generator) {
        PlotWorld plotWorld = getPlotWorld(world);
        if (plotWorld != null) {
            if (generator != null) {
                generator.initialize(plotWorld);
            }
            return;
        }
        final Set<String> worlds = (config.contains("worlds") ? config.getConfigurationSection("worlds").getKeys(false) : new HashSet<String>());
        final PlotGenerator2 plotGenerator;
        final PlotManager plotManager;
        final String path = "worlds." + world;
        if (!LOADING_WORLD && (generator.isFull())) {
            plotGenerator = generator;
            plotWorld = plotGenerator.getNewPlotWorld(world);
            plotManager = plotGenerator.getPlotManager();
            if (!world.equals("CheckingPlotSquaredGenerator")) {
                log(C.PREFIX.s() + "&aDetected world load for '" + world + "'");
                log(C.PREFIX.s() + "&3 - generator: &7" + plotGenerator.getClass().getName());
                log(C.PREFIX.s() + "&3 - plotworld: &7" + plotWorld.getClass().getName());
                log(C.PREFIX.s() + "&3 - manager: &7" + plotManager.getClass().getName());
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
            generator.initialize(plotWorld);
            MainUtil.setupBorder(world);
        } else {
            if (!worlds.contains(world)) {
                return;
            }
            if (!LOADING_WORLD) {
                LOADING_WORLD = true;
                try {
                    final String gen_string = config.getString("worlds." + world + "." + "generator.plugin");
                    generator.setGenerator(gen_string);
//                    if (gen_string == null) {
//                        generator = new HybridGen(world);
//                    } else {
//                        generator = (PlotGenerator) IMP.getGenerator(world, gen_string);
//                    }
                    loadWorld(world, generator);
                } catch (final Exception e) {
                    log("&d=== Oh no! Please set the generator for the " + world + " ===");
                    e.printStackTrace();
                    LOADING_WORLD = false;
                    removePlotWorld(world);
                } finally {
                    LOADING_WORLD = false;
                }
            } else {
                plotWorld = generator.getNewPlotWorld(world);
                plotManager = generator.getPlotManager();
                if (!config.contains(path)) {
                    config.createSection(path);
                }
                plotWorld.TYPE = generator.isFull() ? 0 : 2;
                plotWorld.TERRAIN = 0;
                plotWorld.saveConfiguration(config.getConfigurationSection(path));
                plotWorld.loadDefaultConfiguration(config.getConfigurationSection(path));

                try {
                    config.save(configFile);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                if (((plotWorld.TYPE == 2) && !Settings.ENABLE_CLUSTERS) || !(plotManager instanceof SquarePlotManager)) {
                    log("&c[ERROR] World '" + world + "' in settings.yml is not using PlotSquared generator! Please set the generator correctly or delete the world from the 'settings.yml'!");
                    return;
                }
                log(C.PREFIX.s() + "&aDetected world load for '" + world + "'");
                log(C.PREFIX.s() + "&3 - generator: &7" + generator.getName());
                log(C.PREFIX.s() + "&3 - plotworld: &7" + plotWorld.getClass().getName());
                log(C.PREFIX.s() + "&3 - manager: &7" + plotManager.getClass().getName());
                log(C.PREFIX.s() + "&3 - | terrain: &7" + plotWorld.TERRAIN);
                log(C.PREFIX.s() + "&3 - | type: &7" + plotWorld.TYPE);
                
                addPlotWorld(world, plotWorld, plotManager);
                if (plotWorld.TYPE == 2) {
                    if (ClusterManager.getClusters(world).size() > 0) {
                        for (final PlotCluster cluster : ClusterManager.getClusters(world)) {
                            log(C.PREFIX.s() + "&3 - &7| cluster: " + cluster);
                            generator.augment(cluster, plotWorld);
//                            new AugmentedPopulator(world, generator, cluster, plotWorld.TERRAIN == 2, plotWorld.TERRAIN != 2);
                        }
                    }
                } else if (plotWorld.TYPE == 1) {
                    generator.augment(null, plotWorld);
//                    new AugmentedPopulator(world, gen_class, null, plotWorld.TERRAIN == 2, plotWorld.TERRAIN != 2);
                }
                generator.initialize(plotWorld);
            }
        }
    }


    /**
     * Setup the configuration for a plot world based on world arguments<br>
     * e.g. /mv create <world> normal -g PlotSquared:<args>
     * @param world The name of the world
     * @param args The arguments
     * @return boolean | if valid arguments were provided 
     */
    public boolean setupPlotWorld(final String world, final String args) {
        if ((args != null) && (args.length() > 0)) {
            // save configuration
            final String[] split = args.split(",");
            final HybridPlotWorld plotworld = new HybridPlotWorld(world);
            final int width = SquarePlotWorld.PLOT_WIDTH_DEFAULT;
            final int gap = SquarePlotWorld.ROAD_WIDTH_DEFAULT;
            final int height = ClassicPlotWorld.PLOT_HEIGHT_DEFAULT;
            final PlotBlock[] floor = ClassicPlotWorld.TOP_BLOCK_DEFAULT;
            final PlotBlock[] main = ClassicPlotWorld.MAIN_BLOCK_DEFAULT;
            final PlotBlock wall = ClassicPlotWorld.WALL_FILLING_DEFAULT;
            final PlotBlock border = ClassicPlotWorld.WALL_BLOCK_DEFAULT;
            for (final String element : split) {
                final String[] pair = element.split("=");
                if (pair.length != 2) {
                    log("&cNo value provided for: &7" + element);
                    return false;
                }
                final String key = pair[0].toLowerCase();
                final String value = pair[1];
                try {
                    switch (key) {
                        case "s":
                        case "size": {
                            SquarePlotWorld.PLOT_WIDTH_DEFAULT = Configuration.INTEGER.parseString(value).shortValue();
                            break;
                        }
                        case "g":
                        case "gap": {
                            SquarePlotWorld.ROAD_WIDTH_DEFAULT = Configuration.INTEGER.parseString(value).shortValue();
                            break;
                        }
                        case "h":
                        case "height": {
                            ClassicPlotWorld.PLOT_HEIGHT_DEFAULT = Configuration.INTEGER.parseString(value);
                            ClassicPlotWorld.ROAD_HEIGHT_DEFAULT = Configuration.INTEGER.parseString(value);
                            ClassicPlotWorld.WALL_HEIGHT_DEFAULT = Configuration.INTEGER.parseString(value);
                            break;
                        }
                        case "f":
                        case "floor": {
                            ClassicPlotWorld.TOP_BLOCK_DEFAULT = Configuration.BLOCKLIST.parseString(value);
                            break;
                        }
                        case "m":
                        case "main": {
                            ClassicPlotWorld.MAIN_BLOCK_DEFAULT = Configuration.BLOCKLIST.parseString(value);
                            break;
                        }
                        case "w":
                        case "wall": {
                            ClassicPlotWorld.WALL_FILLING_DEFAULT = Configuration.BLOCK.parseString(value);
                            break;
                        }
                        case "b":
                        case "border": {
                            ClassicPlotWorld.WALL_BLOCK_DEFAULT = Configuration.BLOCK.parseString(value);
                            break;
                        }
                        default: {
                            log("&cKey not found: &7" + element);
                            return false;
                        }
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    log("&cInvalid value: &7" + value + " in arg " + element);
                    return false;
                }
            }
            try {
                final String root = "worlds." + world;
                if (!config.contains(root)) {
                    config.createSection(root);
                }
                plotworld.saveConfiguration(config.getConfigurationSection(root));
                ClassicPlotWorld.PLOT_HEIGHT_DEFAULT = height;
                ClassicPlotWorld.ROAD_HEIGHT_DEFAULT = height;
                ClassicPlotWorld.WALL_HEIGHT_DEFAULT = height;
                ClassicPlotWorld.TOP_BLOCK_DEFAULT = floor;
                ClassicPlotWorld.MAIN_BLOCK_DEFAULT = main;
                ClassicPlotWorld.WALL_BLOCK_DEFAULT = border;
                ClassicPlotWorld.WALL_FILLING_DEFAULT = wall;
                SquarePlotWorld.PLOT_WIDTH_DEFAULT = width;
                SquarePlotWorld.ROAD_WIDTH_DEFAULT = gap;
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    
    public boolean canUpdate(String current, String other) {
        String s1 = normalisedVersion(current);
        String s2 = normalisedVersion(other);
        int cmp = s1.compareTo(s2);
        return cmp < 0;
    }

    public String normalisedVersion(String version) {
        return normalisedVersion(version, ".", 4);
    }

    public String normalisedVersion(String version, String sep, int maxWidth) {
        String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }
        return sb.toString();
    }
    
    /**
     * Gets the default update URL, or null if the plugin is up to date
     * @return
     */
    public URL getUpdate() {
        String resource = "plotsquared.1177";
        String url = "https://www.spigotmc.org/resources/" + resource + "/history";
        String download = "<a href=\"resources/" + resource + "/download?version=";
        String version = "<td class=\"version\">";
        try {
            URL history = new URL(url);
            URLConnection con = history.openConnection();
            con.addRequestProperty("User-Agent", "Mozilla/5.0");
            InputStream stream = con.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            String l;
            URL link = null;
            String cur_ver = config.getString("version");
            String new_ver = null;
            while ((l = in.readLine()) != null) {
                if (l.length() > version.length() && l.startsWith(version)) {
                    new_ver = l.substring(version.length(), l.length() - 5);
                    break;
                }
                if (link == null && l.length() > download.length() && l.startsWith(download)) {
                    String subString = l.substring(download.length());
                    link = new URL("https://www.spigotmc.org/resources/" + resource + "/download?version=" + subString.substring(0, subString.indexOf("\"")));
                    continue;
                }
            }
            stream.close();
            in.close();
            if (new_ver == null || !canUpdate(cur_ver, new_ver))  {
                PS.log("&7PlotSquared is already up to date!");
                return null;
            }
            if (link == null) {
                PS.log("&dCould not check for updates");
                PS.log("&7 - Manually check for updates: " + url);
                return null;
            }
            return link;
        } catch (Exception e) {
            PS.log("&dCould not check for updates");
            PS.log("&7 - Manually check for updates: " + url);
            return null;
        }
    }
    
    public boolean update(PlotPlayer sender, URL url) {
        if (url == null) {
            return false;
        }
        try {
            File newJar = new File("plugins/update/PlotSquared.jar");
            MainUtil.sendMessage(sender, "$1Downloading from provided URL: &7" + url);
            MainUtil.sendMessage(sender, "$2 - User-Agent: " + "Mozilla/4.0");
            URLConnection con = url.openConnection();
            con.addRequestProperty("User-Agent", "Mozilla/4.0");
            InputStream stream = con.getInputStream();
            File parent = newJar.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            MainUtil.sendMessage(sender, "$2 - Output: " + newJar);
            newJar.delete();
            Files.copy(stream, newJar.toPath());
            stream.close();
            MainUtil.sendMessage(sender, "$1The update will take effect when the server is restarted next");  
            return true;
        }
        catch (Exception e) {
            MainUtil.sendMessage(sender, "Failed to update PlotSquared");
            MainUtil.sendMessage(sender, " - Please update manually");
            log("============ Stacktrace ============");
            e.printStackTrace();
            log("====================================");
        }
        return false;
    }


    /**
     * Get the database connection
     * @return The database connection
     */
    public Connection getConnection() {
        return connection;
    }


    /**
     * Copy a file from inside the jar to a location
     * @param file Name of the file inside PlotSquared.jar
     * @param folder The output location relative to /plugins/PlotSquared/
     */
    public void copyFile(String file, String folder) {
        try {
            byte[] buffer = new byte[2048];
            File output = IMP.getDirectory();
            if (!output.exists()) {
                output.mkdirs();
            }
            File newFile = new File((output + File.separator + folder + File.separator + file));
            if (newFile.exists()) {
                return;
            }
            ZipInputStream zis = new ZipInputStream(new FileInputStream(FILE));
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String name = ze.getName();
                if (name.equals(file)) {
                    new File(newFile.getParent()).mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    ze = null;
                }
                else {
                    ze = zis.getNextEntry();
                }
            }
            zis.closeEntry();
            zis.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            log("&cCould not save " + file);
        }
    }


    /**
     * Close the database connection
     */
    public void disable() {
        try {
            database.closeConnection();
            UUIDHandler.handleShutdown();
        } catch (NullPointerException | SQLException e) {
            log("&cCould not close database connection!");
        }
    }

    /**
     * Setup the database connection
     */
    public void setupDatabase() {
        if (Settings.DB.USE_MYSQL) {
            try {
                database = new MySQL(Settings.DB.HOST_NAME, Settings.DB.PORT, Settings.DB.DATABASE, Settings.DB.USER, Settings.DB.PASSWORD);
                connection = database.openConnection();
                {
                    if (DBFunc.dbManager == null) {
                        DBFunc.dbManager = new SQLManager(connection, Settings.DB.PREFIX);
                    }
                    DBFunc.createTables("mysql");
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
        } else if (Settings.DB.USE_MONGO) {
            // DBFunc.dbManager = new MongoManager();
            log(C.PREFIX.s() + "MongoDB is not yet implemented");
        } else if (Settings.DB.USE_SQLITE) {
            try {
                this.database = new SQLite(IMP.getDirectory() + File.separator + Settings.DB.SQLITE_DB + ".db");
                connection = this.database.openConnection();
                {
                    DBFunc.dbManager = new SQLManager(connection, Settings.DB.PREFIX);
                    final DatabaseMetaData meta = connection.getMetaData();
                    meta.getTables(null, null, Settings.DB.PREFIX + "plot", null);
                    DBFunc.createTables("sqlite");
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
        }
    }


    /**
     * Setup the default flags for PlotSquared<br>
     *  - Create the flags
     *  - Register with FlagManager and parse raw flag values
     */
    public void setupDefaultFlags() {
        final List<String> booleanFlags = Arrays.asList("notify-enter", "notify-leave", "item-drop", "invincible", "instabreak", "drop-protection", "forcefield", "titles", "pve", "pvp", "no-worldedit", "redstone", "keep");
        final List<String> intervalFlags = Arrays.asList("feed", "heal");
        final List<String> stringFlags = Arrays.asList("greeting", "farewell");
        final List<String> intFlags = Arrays.asList("entity-cap", "mob-cap", "animal-cap", "hostile-cap", "vehicle-cap", "music");
        for (final String flag : stringFlags) {
            FlagManager.addFlag(new AbstractFlag(flag));
        }
        for (final String flag : intervalFlags) {
            FlagManager.addFlag(new AbstractFlag(flag, new FlagValue.IntervalValue()));
        }
        for (final String flag : booleanFlags) {
            FlagManager.addFlag(new AbstractFlag(flag, new FlagValue.BooleanValue()));
        }
        for (final String flag : intFlags) {
            FlagManager.addFlag(new AbstractFlag(flag, new FlagValue.UnsignedIntegerValue()));
        }
        FlagManager.addFlag(new AbstractFlag("analysis", new FlagValue.IntegerListValue()), true);
        FlagManager.addFlag(new AbstractFlag("disable-physics", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("fly", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("explosion", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("mob-place", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("hostile-interact", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("hostile-attack", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("animal-interact", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("animal-attack", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("tamed-interact", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("tamed-attack", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("misc-interact", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("misc-place", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("misc-break", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("hanging-interact", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("hanging-place", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("hanging-break", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("vehicle-use", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("vehicle-place", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("vehicle-break", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("device-interact", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("place", new FlagValue.PlotBlockListValue()));
        FlagManager.addFlag(new AbstractFlag("break", new FlagValue.PlotBlockListValue()));
        FlagManager.addFlag(new AbstractFlag("use", new FlagValue.PlotBlockListValue()));
        FlagManager.addFlag(new AbstractFlag("blocked-cmds", new FlagValue.StringListValue()));
        FlagManager.addFlag(new AbstractFlag("ice-met", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("gamemode") {

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


            public String getValueDesc() {
                return "Flag value must be a gamemode: 'creative' , 'survival' or 'adventure'";
            }
        });
        FlagManager.addFlag(new AbstractFlag("price", new FlagValue.UnsignedDoubleValue()));
        FlagManager.addFlag(new AbstractFlag("time", new FlagValue.LongValue()));
        FlagManager.addFlag(new AbstractFlag("weather") {

            public String parseValueRaw(final String value) {
                switch (value) {
                    case "rain":
                    case "storm":
                    case "on":
                        return "rain";
                    case "lightning":
                    case "thunder":
                        return "thunder";
                    case "clear":
                    case "off":
                    case "sun":
                        return "clear";
                    default:
                        return null;
                }
            }


            public String getValueDesc() {
                return "Flag value must be weather type: 'clear' or 'rain'";
            }
        });
        FlagManager.addFlag(new AbstractFlag("description", new FlagValue.StringValue()), true);
    }


    /**
     * Setup the default configuration (settings.yml)
     */
    public void setupConfig() {
        LAST_VERSION = config.getString("version");
        config.set("version", VERSION);
        
        final Map<String, Object> options = new HashMap<>();
        // Command confirmation
        options.put("confirmation.clear", Settings.CONFIRM_CLEAR);
        options.put("confirmation.delete", Settings.CONFIRM_DELETE);
        options.put("confirmation.unlink", Settings.CONFIRM_UNLINK);
        
        // Protection
        options.put("protection.redstone.disable-offline", Settings.REDSTONE_DISABLER);
        options.put("protection.tnt-listener.enabled", Settings.TNT_LISTENER);
        options.put("protection.piston.falling-blocks", Settings.PISTON_FALLING_BLOCK_CHECK);
        
        // Clusters
        options.put("clusters.enabled", Settings.ENABLE_CLUSTERS);
        
        // PlotMe
        options.put("plotme-alias", Settings.USE_PLOTME_ALIAS);
        options.put("plotme-convert.enabled", Settings.CONVERT_PLOTME);
        options.put("plotme-convert.cache-uuids", Settings.CACHE_PLOTME);
        
        // UUID
        options.put("uuid.use_sqluuidhandler", Settings.USE_SQLUUIDHANDLER);
        options.put("UUID.offline", Settings.OFFLINE_MODE);
        options.put("UUID.force-lowercase", Settings.UUID_LOWERCASE);
        options.put("uuid.read-from-disk", Settings.UUID_FROM_DISK);
        
        // Mob stuff
        options.put("kill_road_mobs", Settings.KILL_ROAD_MOBS_DEFAULT);
        options.put("mob_pathfinding", Settings.MOB_PATHFINDING_DEFAULT);
        
        // Clearing + Expiry
        options.put("clear.fastmode", Settings.ENABLE_CLUSTERS);
        options.put("clear.on.ban", false);
        options.put("clear.auto.enabled", false);
        options.put("clear.auto.days", 365);
        options.put("clear.auto.clear-interval-seconds", Settings.CLEAR_INTERVAL);
        options.put("clear.auto.calibration.changes", 1);
        options.put("clear.auto.calibration.faces", 2);
        options.put("clear.auto.calibration.data", 32);
        options.put("clear.auto.calibration.air", 0);
        options.put("clear.auto.calibration.variety", 1);
        options.put("clear.auto.calibration.changes_sd", 64);
        options.put("clear.auto.calibration.faces_sd", 32);
        options.put("clear.auto.calibration.data_sd", 1);
        options.put("clear.auto.calibration.air_sd", 0);
        options.put("clear.auto.calibration.variety_sd", 1);
        
        int keep = config.getInt("clear.keep-if-modified");
        int ignore = config.getInt("clear.ignore-if-modified");
        if (keep > 0 || ignore > 0) {
            options.put("clear.auto.threshold", 1);
            log("&cIMPORTANT MESSAGE ABOUT THIS UPDATE!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            log("&cSorry for all the exclamation marks, but this could be important.");
            log("&cPlot clearing has changed to a new system that requires calibration.");
            log("&cThis is how it will work: ");
            log("&c - Players will rate plots");
            log("&c - When enough plots are rated, you can run /plot debugexec calibrate-analysis");
            log("&c - You can decide the (rough) percentage of expired plots to clear");
            log("&c - To just clear all expired plot, ignore this and set: &7threshold: -1");
            log("&cMore information:&7 https://github.com/IntellectualSites/PlotSquared/wiki/Plot-analysis:");
        }
        else {
            options.put("clear.auto.threshold", Settings.CLEAR_THRESHOLD);
        }
        config.set("clear.keep-if-modified", null);
        config.set("clear.ignore-if-modified", null);

        // Schematics
        options.put("schematics.save_path", Settings.SCHEMATIC_SAVE_PATH);
        
        // Web
        options.put("web.url", Settings.WEB_URL);
        options.put("web.server-ip", Settings.WEB_IP);
        
        // Caching
        options.put("cache.permissions", Settings.PERMISSION_CACHING);
        options.put("cache.ratings", Settings.CACHE_RATINGS);
        
        // Titles
        options.put("titles", Settings.TITLES);
        
        // Teleportation
        options.put("teleport.on_login", Settings.TELEPORT_ON_LOGIN);
        options.put("teleport.delay", 0);
        
        // WorldEdit
        options.put("worldedit.require-selection-in-mask", Settings.REQUIRE_SELECTION);
        options.put("worldedit.enable-for-helpers", Settings.WE_ALLOW_HELPER);
        options.put("worldedit.max-volume", Settings.WE_MAX_VOLUME);
        options.put("worldedit.max-iterations", Settings.WE_MAX_ITERATIONS);
        options.put("worldedit.blacklist", Arrays.asList("cs", ".s", "restore", "snapshot", "delchunks", "listchunks"));
        
        // Chunk processor
        options.put("chunk-processor.enabled", Settings.CHUNK_PROCESSOR);
        options.put("chunk-processor.max-blockstates", Settings.CHUNK_PROCESSOR_MAX_BLOCKSTATES);
        options.put("chunk-processor.max-entities", Settings.CHUNK_PROCESSOR_MAX_ENTITIES);
        options.put("chunk-processor.disable-physics", Settings.CHUNK_PROCESSOR_DISABLE_PHYSICS);
        
        // Comments
        options.put("comments.notifications.enabled", Settings.COMMENT_NOTIFICATIONS);
        
        // Plot limits
        options.put("global_limit", Settings.GLOBAL_LIMIT);
        options.put("max_plots", Settings.MAX_PLOTS);
        options.put("claim.max-auto-area", Settings.MAX_AUTO_SIZE);

        // Misc
        options.put("console.color", Settings.CONSOLE_COLOR);
        options.put("chat.fancy", Settings.FANCY_CHAT);
        options.put("metrics", true);
        options.put("debug", true);
        options.put("auto_update", false);
        
        for (final Entry<String, Object> node : options.entrySet()) {
            if (!config.contains(node.getKey())) {
                config.set(node.getKey(), node.getValue());
            }
        }
        
        // Command confirmation
        Settings.CONFIRM_CLEAR = config.getBoolean("confirmation.clear");
        Settings.CONFIRM_DELETE = config.getBoolean("confirmation.delete");
        Settings.CONFIRM_UNLINK = config.getBoolean("confirmation.unlink");
        
        // Protection
        Settings.REDSTONE_DISABLER = config.getBoolean("protection.redstone.disable-offline");
        Settings.TNT_LISTENER = config.getBoolean("protection.tnt-listener.enabled");
        Settings.PISTON_FALLING_BLOCK_CHECK = config.getBoolean("protection.piston.falling-blocks");
        
        // Clusters
        Settings.ENABLE_CLUSTERS = config.getBoolean("clusters.enabled");
        
        // PlotMe
        Settings.USE_PLOTME_ALIAS = config.getBoolean("plotme-alias");
        Settings.CONVERT_PLOTME = config.getBoolean("plotme-convert.enabled");
        Settings.CACHE_PLOTME = config.getBoolean("plotme-convert.cache-uuids");
        
        // UUID
        Settings.USE_SQLUUIDHANDLER = config.getBoolean("uuid.use_sqluuidhandler");
        Settings.OFFLINE_MODE = config.getBoolean("UUID.offline");
        Settings.UUID_LOWERCASE = Settings.OFFLINE_MODE && config.getBoolean("UUID.force-lowercase");
        Settings.UUID_FROM_DISK = config.getBoolean("uuid.read-from-disk");
        
        // Mob stuff
        Settings.KILL_ROAD_MOBS = config.getBoolean("kill_road_mobs");
        Settings.MOB_PATHFINDING = config.getBoolean("mob_pathfinding");
        
        // Clearing + Expiry
        Settings.FAST_CLEAR = config.getBoolean("clear.fastmode");
        Settings.DELETE_PLOTS_ON_BAN = config.getBoolean("clear.on.ban");
        Settings.AUTO_CLEAR_DAYS = config.getInt("clear.auto.days");
        Settings.CLEAR_THRESHOLD = config.getInt("clear.auto.threshold");
        Settings.AUTO_CLEAR = config.getBoolean("clear.auto.enabled");
        Settings.CLEAR_INTERVAL = config.getInt("clear.auto.clear-interval-seconds");

        // Clearing modifiers
        PlotAnalysis.MODIFIERS.changes = config.getInt("clear.auto.calibration.changes");
        PlotAnalysis.MODIFIERS.faces = config.getInt("clear.auto.calibration.faces");
        PlotAnalysis.MODIFIERS.data = config.getInt("clear.auto.calibration.data");
        PlotAnalysis.MODIFIERS.air = config.getInt("clear.auto.calibration.air");
        PlotAnalysis.MODIFIERS.variety = config.getInt("clear.auto.calibration.variety");
        PlotAnalysis.MODIFIERS.changes_sd = config.getInt("clear.auto.calibration.changes_sd");
        PlotAnalysis.MODIFIERS.faces_sd = config.getInt("clear.auto.calibration.faces_sd");
        PlotAnalysis.MODIFIERS.data_sd = config.getInt("clear.auto.calibration.data_sd");
        PlotAnalysis.MODIFIERS.air_sd = config.getInt("clear.auto.calibration.air_sd");
        PlotAnalysis.MODIFIERS.variety_sd = config.getInt("clear.auto.calibration.variety_sd");
        
        // Schematics
        Settings.SCHEMATIC_SAVE_PATH = config.getString("schematics.save_path");
        
        // Web
        Settings.WEB_URL = config.getString("web.url");
        Settings.WEB_IP = config.getString("web.server-ip");
        
        // Caching
        Settings.PERMISSION_CACHING = config.getBoolean("cache.permissions");
        Settings.CACHE_RATINGS = config.getBoolean("cache.ratings");
        
        // Rating system
        Settings.RATING_CATEGORIES = config.getStringList("ratings.categories");
        
        
        // Titles
        Settings.TITLES = config.getBoolean("titles");
        
        // Teleportation
        Settings.TELEPORT_DELAY = config.getInt("teleport.delay");
        Settings.TELEPORT_ON_LOGIN = config.getBoolean("teleport.on_login");
        
        // WorldEdit
        Settings.REQUIRE_SELECTION = config.getBoolean("worldedit.require-selection-in-mask");
        Settings.WE_ALLOW_HELPER = config.getBoolean("worldedit.enable-for-helpers");
        Settings.WE_MAX_VOLUME = config.getLong("worldedit.max-volume");
        Settings.WE_MAX_ITERATIONS = config.getLong("worldedit.max-iterations");
        Settings.WE_BLACKLIST = config.getStringList("worldedit.blacklist");
        
        // Chunk processor
        Settings.CHUNK_PROCESSOR = config.getBoolean("chunk-processor.enabled");
        Settings.CHUNK_PROCESSOR_MAX_BLOCKSTATES = config.getInt("chunk-processor.max-blockstates");
        Settings.CHUNK_PROCESSOR_MAX_ENTITIES = config.getInt("chunk-processor.max-entities");
        Settings.CHUNK_PROCESSOR_DISABLE_PHYSICS = config.getBoolean("chunk-processor.disable-physics");
        
        // Comments
        Settings.COMMENT_NOTIFICATIONS = config.getBoolean("comments.notifications.enabled");
        
        // Plot limits
        Settings.MAX_AUTO_SIZE = config.getInt("claim.max-auto-area");
        Settings.MAX_PLOTS = config.getInt("max_plots");
        if (Settings.MAX_PLOTS > 32767) {
            log("&c`max_plots` Is set too high! This is a per player setting and does not need to be very large.");
            Settings.MAX_PLOTS = 32767;
        }
        Settings.GLOBAL_LIMIT = config.getBoolean("global_limit");
        
        // Misc
        Settings.DEBUG = config.getBoolean("debug");
        if (Settings.DEBUG) {
            log(C.PREFIX.s() + "&6Debug Mode Enabled (Default). Edit the config to turn this off.");
        }
        Settings.CONSOLE_COLOR = config.getBoolean("console.color");
        if (!config.getBoolean("chat.fancy") || !checkVersion(IMP.getServerVersion(), 1, 8, 0)) {
            Settings.FANCY_CHAT = false;
        }
        Settings.METRICS = config.getBoolean("metrics");

        UUIDHandler.implementation = Settings.USE_SQLUUIDHANDLER ? new SQLUUIDHandler() : new FileUUIDHandler();
    }


    /**
     * Setup all configuration files<br>
     *  - Config: settings.yml<br>
     *  - Storage: storage.yml<br>
     *  - Translation: PlotSquared.use_THIS.yml, style.yml<br>
     */
    public void setupConfigs() {
        final File folder = new File(IMP.getDirectory() + File.separator + "config");
        if (!folder.exists() && !folder.mkdirs()) {
            log(C.PREFIX.s() + "&cFailed to create the /plugins/config folder. Please create it manually.");
        }
        try {
            styleFile = new File(IMP.getDirectory() + File.separator + "translations" + File.separator + "style.yml");
            if (!styleFile.exists()) {
                if (!styleFile.getParentFile().exists()) {
                    styleFile.getParentFile().mkdirs();
                }
                if (!styleFile.createNewFile()) {
                    log("Could not create the style file, please create \"translations/style.yml\" manually");
                }
            }
            style = YamlConfiguration.loadConfiguration(styleFile);
            setupStyle();
        } catch (final Exception err) {
            err.printStackTrace();
            Logger.add(LogLevel.DANGER, "Failed to save style.yml");
            log("failed to save style.yml");
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
            log("Failed to save settings.yml");
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
            log("Failed to save storage.yml");
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

    /**
     * Setup the storage file (load + save missing nodes)
     */
    private void setupStorage() {
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
    }


    /**
     * Show startup debug information
     */
    public void showDebug() {
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

    /**
     * Setup the style.yml file
     */
    private void setupStyle() {
        style.set("version", VERSION);
        final Map<String, Object> o = new HashMap<>();
        o.put("color.1", "6");
        o.put("color.2", "7");
        o.put("color.3", "8");
        o.put("color.4", "3");
        if (!style.contains("color")) {
            for (final Entry<String, Object> node : o.entrySet()) {
                style.set(node.getKey(), node.getValue());
            }
        }
    }


    /**
     * Get the java version
     * @return Java version as a double
     */
    public double getJavaVersion() {
        return Double.parseDouble(System.getProperty("java.specification.version"));
    }


    /**
     * Get the list of plot world names
     * @return Set of world names (String)
     */
    public Set<String> getPlotWorlds() {
        return plotworlds.keySet();
    }


    /**
     * Get a list of PlotWorld objects
     * @return Collection of PlotWorld objects
     */
    public Collection<PlotWorld> getPlotWorldObjects() {
        return plotworlds.values();
    }
}
