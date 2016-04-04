package com.intellectualcrafters.plot;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.configuration.MemorySection;
import com.intellectualcrafters.configuration.file.YamlConfiguration;
import com.intellectualcrafters.plot.commands.WE_Anywhere;
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
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotFilter;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.intellectualcrafters.plot.util.ChatManager;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.CommentManager;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.InventoryUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.PlotGameMode;
import com.intellectualcrafters.plot.util.PlotWeather;
import com.intellectualcrafters.plot.util.ReflectionUtils;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.intellectualcrafters.plot.util.area.QuadMap;
import com.plotsquared.listener.WESubscriber;
import com.sk89q.worldedit.WorldEdit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * An implementation of the core, with a static getter for easy access.
 */
public class PS {

    private static PS instance;
    private final HashSet<Integer> plotAreaHashCheck = new HashSet<>();
    /**
     * All plot areas mapped by world (quick world access).
     */
    private final HashMap<String, PlotArea[]> plotAreaMap = new HashMap<>();
    /**
     * All plot areas mapped by location (quick location based access).
     */
    private final HashMap<String, QuadMap<PlotArea>> plotAreaGrid = new HashMap<>();
    public HashMap<String, Set<PlotCluster>> clusters_tmp;
    public HashMap<String, HashMap<PlotId, Plot>> plots_tmp;
    public File styleFile;
    public File configFile;
    public File commandsFile;
    public File translationFile;
    public YamlConfiguration style;
    public YamlConfiguration config;
    public YamlConfiguration storage;
    public YamlConfiguration commands;
    public IPlotMain IMP = null;
    public TaskManager TASK;
    public WorldEdit worldedit;
    public URL update;
    private boolean plotAreaHasCollision = false;
    /**
     * All plot areas (quick global access).
     */
    private PlotArea[] plotAreas = new PlotArea[0];

    private File storageFile;
    private File file = null; // This file
    private int[] version;
    private int[] lastVersion;
    private String platform = null;
    private Database database;
    private Thread thread;

    /**
     * Initialize PlotSquared with the desired Implementation class.
     * @param imp_class
     */
    public PS(IPlotMain imp_class, String platform) {
        try {
            PS.instance = this;
            this.thread = Thread.currentThread();
            SetupUtils.generators = new HashMap<>();
            this.IMP = imp_class;
            new ReflectionUtils(this.IMP.getNMSPackage());
            try {
                URL url = PS.class.getProtectionDomain().getCodeSource().getLocation();
                this.file = new File(new URL(url.toURI().toString().split("\\!")[0].replaceAll("jar:file", "file")).toURI().getPath());
            } catch (MalformedURLException | URISyntaxException | SecurityException | NullPointerException e) {
                e.printStackTrace();
                this.file = new File(this.IMP.getDirectory().getParentFile(), "PlotSquared.jar");
                if (!this.file.exists()) {
                    this.file = new File(this.IMP.getDirectory().getParentFile(), "PlotSquared-" + platform + ".jar");
                }
            }
            this.version = this.IMP.getPluginVersion();
            this.platform = platform;
            if (getJavaVersion() < 1.7) {
                PS.log(C.CONSOLE_JAVA_OUTDATED_1_7);
                this.IMP.disable();
                return;
            }
            if (getJavaVersion() < 1.8) {
                PS.log(C.CONSOLE_JAVA_OUTDATED_1_8);
            }
            this.TASK = this.IMP.getTaskManager();
            if (!C.ENABLED.s().isEmpty()) {
                PS.log(C.ENABLED);
            }
            setupConfigs();
            this.translationFile = new File(this.IMP.getDirectory() + File.separator + "translations" + File.separator + "PlotSquared.use_THIS.yml");
            C.load(this.translationFile);
            setupDefaultFlags();
            setupDatabase();
            CommentManager.registerDefaultInboxes();
            // Tasks
            if (Settings.KILL_ROAD_MOBS || Settings.KILL_ROAD_VEHICLES) {
                this.IMP.runEntityTask();
            }
            if (this.IMP.initWorldEdit()) {
                this.worldedit = WorldEdit.getInstance();
                WorldEdit.getInstance().getEventBus().register(new WESubscriber());
                new WE_Anywhere();
            }

            // Events
            this.IMP.registerCommands();
            this.IMP.registerPlayerEvents();
            this.IMP.registerInventoryEvents();
            this.IMP.registerPlotPlusEvents();
            this.IMP.registerForceFieldEvents();
            this.IMP.registerWorldEvents();
            if (Settings.METRICS) {
                this.IMP.startMetrics();
            } else {
                PS.log(C.CONSOLE_PLEASE_ENABLE_METRICS);
            }
            if (Settings.CHUNK_PROCESSOR) {
                this.IMP.registerChunkProcessor();
            }
            // create UUIDWrapper
            UUIDHandler.implementation = this.IMP.initUUIDHandler();
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    debug("Starting UUID caching");
                    UUIDHandler.startCaching(new Runnable() {
                        @Override
                        public void run() {
                            for (Plot plot : getPlots()) {
                                if (plot.hasOwner() && plot.temp != -1) {
                                    if (UUIDHandler.getName(plot.owner) == null) {
                                        UUIDHandler.implementation.unknown.add(plot.owner);
                                    }
                                }
                            }
                            // Auto clearing
                            if (Settings.AUTO_CLEAR) {
                                ExpireManager.IMP = new ExpireManager();
                                if (Settings.AUTO_CLEAR_CONFIRMATION) {
                                    ExpireManager.IMP.runConfirmedTask();
                                } else {
                                    ExpireManager.IMP.runAutomatedTask();
                                }
                            }
                            // PlotMe
                            if (Settings.CONVERT_PLOTME || Settings.CACHE_PLOTME) {
                                TaskManager.runTaskLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (PS.this.IMP.initPlotMeConverter()) {
                                            PS.log("&c=== IMPORTANT ===");
                                            PS.log("&cTHIS MESSAGE MAY BE EXTREMELY HELPFUL IF YOU HAVE TROUBLE CONVERTING PLOTME!");
                                            PS.log("&c - Make sure 'UUID.read-from-disk' is disabled (false)!");
                                            PS.log("&c - Sometimes the database can be locked, deleting PlotMe.jar beforehand will fix the issue!");
                                            PS.log("&c - After the conversion is finished, please set 'plotme-convert.enabled' to false in the "
                                                    + "'settings.yml'");
                                        }
                                    }
                                }, 20);
                            }
                        }
                    });
                }
            }, 20);
            // create event util class
            EventUtil.manager = this.IMP.initEventUtil();
            // create Hybrid utility class
            HybridUtils.manager = this.IMP.initHybridUtils();
            // Inventory utility class
            InventoryUtil.manager = this.IMP.initInventoryUtil();
            // create setup util class
            SetupUtils.manager = this.IMP.initSetupUtils();
            // World Util
            WorldUtil.IMP = this.IMP.initWorldUtil();
            // Set block
            SetQueue.IMP.queue = this.IMP.initPlotQueue();
            // Set chunk
            ChunkManager.manager = this.IMP.initChunkManager();
            // Schematic handler
            SchematicHandler.manager = this.IMP.initSchematicHandler();
            // Titles
            AbstractTitle.TITLE_CLASS = this.IMP.initTitleManager();
            // Chat
            ChatManager.manager = this.IMP.initChatManager();
            // Economy
            TaskManager.runTask(new Runnable() {
                @Override
                public void run() {
                    EconHandler.manager = PS.this.IMP.getEconomyHandler();
                }
            });

            // Check for updates
            TaskManager.runTaskAsync(new Runnable() {
                @Override
                public void run() {
                    URL url = Updater.getUpdate();
                    if (url != null) {
                        PS.this.update = url;
                    } else if (PS.this.lastVersion == null) {
                        PS.log("&aThanks for installing PlotSquared!");
                    } else if (!get().checkVersion(PS.this.lastVersion, PS.this.version)) {
                        PS.log("&aThanks for updating from " + StringMan.join(PS.this.lastVersion, ".") + " to " + StringMan
                                .join(PS.this.version, ".")
                                + "!");
                        DBFunc.dbManager.updateTables(PS.this.lastVersion);
                    }
                }
            });

            // World generators:
            final ConfigurationSection section = this.config.getConfigurationSection("worlds");
            if (section != null) {
                for (String world : section.getKeys(false)) {
                    if (world.equals("CheckingPlotSquaredGenerator")) {
                        continue;
                    }
                    if (WorldUtil.IMP.isWorld(world)) {
                        this.IMP.setGenerator(world);
                    }
                }
                TaskManager.runTaskLater(new Runnable() {
                    @Override
                    public void run() {
                        for (String world : section.getKeys(false)) {
                            if (world.equals("CheckingPlotSquaredGenerator")) {
                                continue;
                            }
                            if (!WorldUtil.IMP.isWorld(world)) {
                                debug("&c`" + world + "` was not properly loaded - PlotSquared will now try to load it properly: ");
                                debug(
                                        "&8 - &7Are you trying to delete this world? Remember to remove it from the settings.yml, bukkit.yml and "
                                                + "multiverse worlds.yml");
                                debug("&8 - &7Your world management plugin may be faulty (or non existent)");
                                PS.this.IMP.setGenerator(world);
                            }
                        }
                    }
                }, 1);
            }

            // Copy files
            copyFile("automerge.js", "scripts");
            copyFile("town.template", "templates");
            copyFile("skyblock.template", "templates");
            copyFile("german.yml", "translations");
            copyFile("s_chinese_unescaped.yml", "translations");
            copyFile("s_chinese.yml", "translations");
            copyFile("italian.yml", "translations");
            showDebug();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the instance of PlotSquared.
     *
     * @return the instance created by IPlotMain
     */
    public static PS get() {
        return PS.instance;
    }

    /**
     * Log a message to the IPlotMain logger.
     *
     * @param message Message to log
     * @see IPlotMain#log(String)
     */
    public static void log(Object message) {
        PS.get().IMP.log(StringMan.getString(message));
    }

    public static void stacktrace() {
        System.err.println(StringMan.join(new Exception().getStackTrace(), "\n\tat "));
    }

    /**
     * Log a message to the IPlotMain logger.
     *
     * @param message Message to log
     * @see IPlotMain#log(String)
     */
    public static void debug(Object message) {
        if (Settings.DEBUG) {
            PS.log(message);
        }
    }

    public boolean isMainThread(Thread thread) {
        return this.thread == thread;
    }

    /**
     * Check if `version` is >= `version2`.
     * @param version
     * @param version2
     * @return true if `version` is >= `version2`
     */
    public boolean checkVersion(int[] version, int... version2) {
        return version[0] > version2[0] || version[0] == version2[0] && version[1] > version2[1] || version[0] == version2[0]
                && version[1] == version2[1] && version[2] >= version2[2];
    }

    /**
     * Get the last PlotSquared version.
     * @return last version in config or null
     */
    public int[] getLastVersion() {
        return this.lastVersion;
    }

    /**
     * Get the current PlotSquared version.
     * @return current version in config or null
     */
    public int[] getVersion() {
        return this.version;
    }

    /**
     * <p>Get the server platform this plugin is running on this is running on.
     * This will be either <b>Bukkit</b> or <b>Sponge</b></p>
     * @return The server platform
     */
    public String getPlatform() {
        return this.platform;
    }

    /**
     * Get the database object.
     *
     * @return Database object
     * @see Database#getConnection() To get the database connection
     */
    public Database getDatabase() {
        return this.database;
    }

    /**
     * Get the relevant plot area for a location.
     * <ul>
     *     <li>If there is only one plot area globally that will be returned</li>
     *     <li>If there is only one plot area in the world, it will return that</li>
     *     <li>If the plot area for a location cannot be unambiguously resolved; null will be returned</li>
     * </ul>
     * Note: An applicable plot area may not include the location i.e. clusters
     * @param location
     * @return
     */
    public PlotArea getApplicablePlotArea(Location location) {
        switch (this.plotAreas.length) {
            case 0:
                return null;
            case 1:
                return this.plotAreas[0];
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                String world = location.getWorld();
                int hash = world.hashCode();
                for (PlotArea area : this.plotAreas) {
                    if (hash == area.worldhash) {
                        if (area.contains(location.getX(), location.getZ()) && (!this.plotAreaHasCollision || world.equals(area.worldname))) {
                            return area;
                        }
                    }
                }
                return null;
            default:
                PlotArea[] areas = this.plotAreaMap.get(location.getWorld());
                if (areas == null) {
                    return null;
                }
                int y;
                int x;
                switch (areas.length) {
                    case 1:
                        return areas[0];
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        x = location.getX();
                        y = location.getY();
                        for (PlotArea area : areas) {
                            if (area.contains(x, y)) {
                                return area;
                            }
                        }
                        return null;
                    default:
                        QuadMap<PlotArea> search = this.plotAreaGrid.get(location.getWorld());
                        return search.get(location.getX(), location.getZ());
                }
        }
    }

    public PlotArea getPlotArea(String world, String id) {
        PlotArea[] areas = this.plotAreaMap.get(world);
        if (areas == null) {
            return null;
        }
        if (areas.length == 1) {
            return areas[0];
        } else if (id == null) {
            return null;
        } else {
            for (PlotArea area : areas) {
                if (StringMan.isEqual(id, area.id)) {
                    return area;
                }
            }
            return null;
        }
    }

    public PlotArea getPlotAreaAbs(String world, String id) {
        PlotArea[] areas = this.plotAreaMap.get(world);
        if (areas == null) {
            return null;
        }
        for (PlotArea area : areas) {
            if (StringMan.isEqual(id, area.id)) {
                return area;
            }
        }
        return null;
    }

    public PlotArea getPlotAreaByString(String search) {
        String[] split = search.split(";|,");
        PlotArea[] areas = this.plotAreaMap.get(split[0]);
        if (areas == null) {
            for (PlotArea area : this.plotAreas) {
                if (area.worldname.equalsIgnoreCase(split[0])) {
                    if (area.id == null || split.length == 2 && area.id.equalsIgnoreCase(split[1])) {
                        return area;
                    }
                }
            }
            return null;
        }
        if (areas.length == 1) {
            return areas[0];
        } else if (split.length == 1) {
            return null;
        } else {
            for (PlotArea area : areas) {
                if (StringMan.isEqual(split[1], area.id)) {
                    return area;
                }
            }
            return null;
        }
    }

    public Set<PlotArea> getPlotAreas(String world, RegionWrapper region) {
        QuadMap<PlotArea> areas = this.plotAreaGrid.get(world);
        return areas != null ? areas.get(region) : new HashSet<PlotArea>();
    }

    /**
     * Get the plot area which contains a location.
     * <ul>
     *     <li>If the plot area does not contain a location, null will be returned.</li>
     * </ul>
     *
     * @param location
     * @return
     */
    public PlotArea getPlotAreaAbs(Location location) {
        switch (this.plotAreas.length) {
            case 0:
                return null;
            case 1:
                PlotArea pa = this.plotAreas[0];
                return pa.contains(location) ? pa : null;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                String world = location.getWorld();
                int hash = world.hashCode();
                for (PlotArea area : this.plotAreas) {
                    if (hash == area.worldhash) {
                        if (area.contains(location.getX(), location.getZ()) && (!this.plotAreaHasCollision || world.equals(area.worldname))) {
                            return area;
                        }
                    }
                }
                return null;
            default:
                PlotArea[] areas = this.plotAreaMap.get(location.getWorld());
                if (areas == null) {
                    return null;
                }
                int x;
                int y;
                switch (areas.length) {
                    case 0:
                        PlotArea a = areas[0];
                        return a.contains(location.getX(), location.getZ()) ? a : null;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        x = location.getX();
                        y = location.getY();
                        for (PlotArea area : areas) {
                            if (area.contains(x, y)) {
                                return area;
                            }
                        }
                        return null;
                    default:
                        QuadMap<PlotArea> search = this.plotAreaGrid.get(location.getWorld());
                        return search.get(location.getX(), location.getZ());
                }
        }
    }

    public PlotManager getPlotManager(Plot plot) {
        return plot.getArea().manager;
    }

    public PlotManager getPlotManager(Location location) {
        PlotArea pa = getPlotAreaAbs(location);
        return pa != null ? pa.manager : null;
    }

    /**
     * Add a global reference to a plot world.
     *
     * @param plotArea The PlotArea
     * @see #removePlotArea(PlotArea) To remove the reference
     */
    public void addPlotArea(PlotArea plotArea) {
        HashMap<PlotId, Plot> plots = this.plots_tmp.remove(plotArea.toString());
        if (plots == null) {
            if (plotArea.TYPE == 2) {
                plots = this.plots_tmp.get(plotArea.worldname);
                if (plots != null) {
                    Iterator<Entry<PlotId, Plot>> iterator = plots.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Entry<PlotId, Plot> next = iterator.next();
                        PlotId id = next.getKey();
                        if (plotArea.contains(id)) {
                            next.getValue().setArea(plotArea);
                            iterator.remove();
                        }
                    }
                }
            }
        } else {
            for (Entry<PlotId, Plot> entry : plots.entrySet()) {
                Plot plot = entry.getValue();
                plot.setArea(plotArea);
            }
        }
        Set<PlotCluster> clusters = this.clusters_tmp.remove(plotArea.toString());
        if (clusters == null) {
            if (plotArea.TYPE == 2) {
                clusters = this.clusters_tmp.get(plotArea.worldname);
                if (clusters != null) {
                    Iterator<PlotCluster> iterator = clusters.iterator();
                    while (iterator.hasNext()) {
                        PlotCluster next = iterator.next();
                        if (next.intersects(plotArea.getMin(), plotArea.getMax())) {
                            next.setArea(plotArea);
                            iterator.remove();
                        }
                    }
                }
            }
        } else {
            for (PlotCluster cluster : clusters) {
                cluster.setArea(plotArea);
            }
        }
        Set<PlotArea> localAreas = getPlotAreas(plotArea.worldname);
        Set<PlotArea> globalAreas = getPlotAreas();
        localAreas.add(plotArea);
        globalAreas.add(plotArea);
        this.plotAreas = globalAreas.toArray(new PlotArea[globalAreas.size()]);
        this.plotAreaMap.put(plotArea.worldname, localAreas.toArray(new PlotArea[localAreas.size()]));
        QuadMap<PlotArea> map = this.plotAreaGrid.get(plotArea.worldname);
        if (map == null) {
            map = new QuadMap<PlotArea>(Integer.MAX_VALUE, 0, 0) {
                @Override
                public RegionWrapper getRegion(PlotArea value) {
                    return value.getRegion();
                }
            };
            this.plotAreaGrid.put(plotArea.worldname, map);
        }
        map.add(plotArea);
    }

    /**
     * Remove a plot world reference.
     *
     * @param area The PlotArea
     * @see #addPlotArea(PlotArea) To add a reference
     */
    public void removePlotArea(PlotArea area) {
        Set<PlotArea> areas = getPlotAreas(area.worldname);
        areas.remove(area);
        this.plotAreas = areas.toArray(new PlotArea[areas.size()]);
        if (areas.isEmpty()) {
            this.plotAreaMap.remove(area.worldname);
            this.plotAreaGrid.remove(area.worldname);
        } else {
            this.plotAreaMap.put(area.worldname, areas.toArray(new PlotArea[areas.size()]));
            this.plotAreaGrid.get(area.worldname).remove(area);
        }
        setPlotsTmp(area);
    }

    public void removePlotAreas(String world) {
        for (PlotArea area : getPlotAreas(world)) {
            removePlotArea(area);
        }
    }

    private void setPlotsTmp(PlotArea area) {
        if (this.plots_tmp == null) {
            this.plots_tmp = new HashMap<>();
        }
        HashMap<PlotId, Plot> map = this.plots_tmp.get(area.toString());
        if (map == null) {
            map = new HashMap<>();
            this.plots_tmp.put(area.toString(), map);
        }
        for (Plot plot : area.getPlots()) {
            map.put(plot.getId(), plot);
        }
        if (this.clusters_tmp == null) {
            this.clusters_tmp = new HashMap<>();
        }
        this.clusters_tmp.put(area.toString(), area.getClusters());
    }

    public Set<PlotCluster> getClusters(String world) {
        HashSet<PlotCluster> set = new HashSet<>();
        for (PlotArea area : getPlotAreas(world)) {
            set.addAll(area.getClusters());
        }
        return set;

    }

    /**
     * A more generic way to filter plots - make your own method if you need complex filters.
     * @param filters
     * @return
     */
    public Set<Plot> getPlots(final PlotFilter... filters) {
        final HashSet<Plot> set = new HashSet<>();
        foreachPlotArea(new RunnableVal<PlotArea>() {
            @Override
            public void run(PlotArea value) {
                for (PlotFilter filter : filters) {
                    if (!filter.allowsArea(value)) {
                        continue;
                    }
                }
                for (Entry<PlotId, Plot> entry2 : value.getPlotEntries()) {
                    Plot plot = entry2.getValue();
                    for (PlotFilter filter : filters) {
                        if (!filter.allowsPlot(plot)) {
                            continue;
                        }
                    }
                    set.add(plot);
                }
            }
        });
        return set;
    }

    /**
     * Get all the plots in a single set.
     * @return Set of Plots
     */
    public Set<Plot> getPlots() {
        int size = getPlotCount();
        final Set<Plot> result = new HashSet<>(size);
        foreachPlotArea(new RunnableVal<PlotArea>() {
            @Override
            public void run(PlotArea value) {
                result.addAll(value.getPlots());
            }
        });
        return result;
    }

    public void setPlots(HashMap<String, HashMap<PlotId, Plot>> plots) {
        if (this.plots_tmp == null) {
            this.plots_tmp = new HashMap<>();
        }
        for (Entry<String, HashMap<PlotId, Plot>> entry : plots.entrySet()) {
            String world = entry.getKey();
            PlotArea area = getPlotArea(world, null);
            if (area == null) {
                HashMap<PlotId, Plot> map = this.plots_tmp.get(world);
                if (map == null) {
                    map = new HashMap<>();
                    this.plots_tmp.put(world, map);
                }
                map.putAll(entry.getValue());
            } else {
                for (Entry<PlotId, Plot> entry2 : entry.getValue().entrySet()) {
                    Plot plot = entry2.getValue();
                    plot.setArea(area);
                    area.addPlot(plot);
                }
            }
        }
    }

    /**
     * Get all the base plots in a single set (for merged plots it just returns the bottom plot).
     * @return Set of base Plots
     */
    public Set<Plot> getBasePlots() {
        int size = getPlotCount();
        final Set<Plot> result = new HashSet<>(size);
        foreachPlotArea(new RunnableVal<PlotArea>() {
            @Override
            public void run(PlotArea value) {
                for (Plot plot : value.getPlots()) {
                    if (!plot.isBasePlot()) {
                        continue;
                    }
                    result.add(plot);
                }
            }
        });
        return result;
    }

    public ArrayList<Plot> sortPlotsByTemp(Collection<Plot> plots) {
        int max = 0;
        int overflowCount = 0;
        for (Plot plot : plots) {
            if (plot.temp > 0) {
                if (plot.temp > max) {
                    max = plot.temp;
                }
            } else {
                overflowCount++;
            }
        }
        Plot[] array = new Plot[max + 1];
        List<Plot> overflow = new ArrayList<>(overflowCount);
        for (Plot plot : plots) {
            if (plot.temp <= 0) {
                overflow.add(plot);
            } else {
                array[plot.temp] = plot;
            }
        }
        ArrayList<Plot> result = new ArrayList<>(plots.size());
        for (Plot plot : array) {
            if (plot != null) {
                result.add(plot);
            }
        }
        Collections.sort(overflow, new Comparator<Plot>() {
            @Override
            public int compare(Plot a, Plot b) {
                return a.hashCode() - b.hashCode();
            }
        });
        result.addAll(overflow);
        return result;
    }

    /**
     * Sort plots by hashcode.
     * @param plots
     * @return
     * @deprecated Unchecked, please use {@link #sortPlots(Collection, SortType, PlotArea)} which has additional checks before calling this
     */
    @Deprecated
    public ArrayList<Plot> sortPlotsByHash(Collection<Plot> plots) {
        int hardmax = 256000;
        int max = 0;
        int overflowSize = 0;
        for (Plot plot : plots) {
            int hash = MathMan.getPositiveId(plot.hashCode());
            if (hash > max) {
                if (hash >= hardmax) {
                    overflowSize++;
                } else {
                    max = hash;
                }
            }
        }
        hardmax = Math.min(hardmax, max);
        Plot[] cache = new Plot[hardmax + 1];
        List<Plot> overflow = new ArrayList<>(overflowSize);
        ArrayList<Plot> extra = new ArrayList<>();
        for (Plot plot : plots) {
            int hash = MathMan.getPositiveId(plot.hashCode());
            if (hash < hardmax) {
                if (hash >= 0) {
                    cache[hash] = plot;
                } else {
                    extra.add(plot);
                }
            } else if (Math.abs(plot.getId().x) > 15446 || Math.abs(plot.getId().y) > 15446) {
                extra.add(plot);
            } else {
                overflow.add(plot);
            }
        }
        Plot[] overflowArray = overflow.toArray(new Plot[overflow.size()]);
        sortPlotsByHash(overflowArray);
        ArrayList<Plot> result = new ArrayList<>(cache.length + overflowArray.length);
        for (Plot plot : cache) {
            if (plot != null) {
                result.add(plot);
            }
        }
        Collections.addAll(result, overflowArray);
        for (Plot plot : extra) {
            result.add(plot);
        }
        return result;
    }

    @Deprecated
    public ArrayList<Plot> sortPlotsByTimestamp(Collection<Plot> plots) {
        int hardMax = 256000;
        int max = 0;
        int overflowSize = 0;
        for (Plot plot : plots) {
            int hash = MathMan.getPositiveId(plot.hashCode());
            if (hash > max) {
                if (hash >= hardMax) {
                    overflowSize++;
                } else {
                    max = hash;
                }
            }
        }
        hardMax = Math.min(hardMax, max);
        Plot[] cache = new Plot[hardMax + 1];
        List<Plot> overflow = new ArrayList<>(overflowSize);
        ArrayList<Plot> extra = new ArrayList<>();
        for (Plot plot : plots) {
            int hash = MathMan.getPositiveId(plot.hashCode());
            if (hash < hardMax) {
                if (hash >= 0) {
                    cache[hash] = plot;
                } else {
                    extra.add(plot);
                }
            } else if (Math.abs(plot.getId().x) > 15446 || Math.abs(plot.getId().y) > 15446) {
                extra.add(plot);
            } else {
                overflow.add(plot);
            }
        }
        Plot[] overflowArray = overflow.toArray(new Plot[overflow.size()]);
        sortPlotsByHash(overflowArray);
        ArrayList<Plot> result = new ArrayList<>(cache.length + overflowArray.length);
        for (Plot plot : cache) {
            if (plot != null) {
                result.add(plot);
            }
        }
        Collections.addAll(result, overflowArray);
        for (Plot plot : extra) {
            result.add(plot);
        }
        return result;
    }

    /**
     * Sort plots by creation timestamp.
     * @param input
     * @deprecated Unchecked, use {@link #sortPlots(Collection, SortType, PlotArea)} instead which will call this after checks
     * @return
     */
    @Deprecated
    public List<Plot> sortPlotsByModified(Collection<Plot> input) {
        List<Plot> list;
        if (input instanceof List) {
            list = (List<Plot>) input;
        } else {
            list = new ArrayList<>(input);
        }
        Collections.sort(list, new Comparator<Plot>() {
            @Override
            public int compare(Plot a, Plot b) {
                return Long.compare(ExpireManager.IMP.getTimestamp(a.owner), ExpireManager.IMP.getTimestamp(b.owner));
            }
        });
        return list;
    }

    /**
     * @deprecated Unchecked, use {@link #sortPlots(Collection, SortType, PlotArea)}  instead which will in turn call this
     * @param input
     */
    @Deprecated
    public void sortPlotsByHash(Plot[] input) {
        List<Plot>[] bucket = new ArrayList[32];
        for (int i = 0; i < bucket.length; i++) {
            bucket[i] = new ArrayList<>();
        }
        boolean maxLength = false;
        int placement = 1;
        while (!maxLength) {
            maxLength = true;
            for (Plot i : input) {
                int tmp = MathMan.getPositiveId(i.hashCode()) / placement;
                bucket[tmp & 31].add(i);
                if (maxLength && tmp > 0) {
                    maxLength = false;
                }
            }
            int a = 0;
            for (int b = 0; b < 32; b++) {
                for (Plot i : bucket[b]) {
                    input[a++] = i;
                }
                bucket[b].clear();
            }
            placement *= 32;
        }
    }

    /**
     * Sort a collection of plots by world (with a priority world), then by hashcode.
     * @param myPlots
     * @param type The sorting method to use for each world (timestamp, or hash)
     * @param priorityArea Use null, "world", or "gibberish" if you want default world order
     * @return ArrayList of plot
     */
    public ArrayList<Plot> sortPlots(Collection<Plot> myPlots, SortType type, final PlotArea priorityArea) {
        // group by world
        // sort each
        HashMap<PlotArea, Collection<Plot>> map = new HashMap<>();
        int totalSize = getPlotCount();
        if (myPlots.size() == totalSize) {
            for (PlotArea area : this.plotAreas) {
                map.put(area, area.getPlots());
            }
        } else {
            for (PlotArea area : this.plotAreas) {
                map.put(area, new ArrayList<Plot>(0));
            }
            Collection<Plot> lastList = null;
            PlotArea lastWorld = null;
            for (Plot plot : myPlots) {
                if (lastWorld == plot.getArea()) {
                    lastList.add(plot);
                } else {
                    lastWorld = plot.getArea();
                    lastList = map.get(lastWorld);
                    lastList.add(plot);
                }
            }
        }
        List<PlotArea> areas = Arrays.asList(this.plotAreas);
        Collections.sort(areas, new Comparator<PlotArea>() {
            @Override
            public int compare(PlotArea a, PlotArea b) {
                if (priorityArea != null && StringMan.isEqual(a.toString(), b.toString())) {
                    return -1;
                }
                return a.hashCode() - b.hashCode();
            }
        });
        ArrayList<Plot> toReturn = new ArrayList<>(myPlots.size());
        for (PlotArea area : areas) {
            switch (type) {
                case CREATION_DATE:
                    toReturn.addAll(sortPlotsByTemp(map.get(area)));
                    break;
                case CREATION_DATE_TIMESTAMP:
                    toReturn.addAll(sortPlotsByTimestamp(map.get(area)));
                    break;
                case DISTANCE_FROM_ORIGIN:
                    toReturn.addAll(sortPlotsByHash(map.get(area)));
                    break;
                case LAST_MODIFIED:
                    toReturn.addAll(sortPlotsByModified(map.get(area)));
                default:
                    break;
            }
        }
        return toReturn;
    }

    /**
     * Get all the plots owned by a player name.
     * @param world
     * @param player
     * @return Set of Plot
     */
    public Set<Plot> getPlots(String world, String player) {
        UUID uuid = UUIDHandler.getUUID(player, null);
        return getPlots(world, uuid);
    }

    /**
     * Get all the plots owned by a player name.
     * @param area
     * @param player
     * @return Set of Plot
     */
    public Set<Plot> getPlots(PlotArea area, String player) {
        UUID uuid = UUIDHandler.getUUID(player, null);
        return getPlots(area, uuid);
    }

    /**
     * Get all plots by a PlotPlayer.
     * @param world
     * @param player
     * @return Set of plot
     */
    public Set<Plot> getPlots(String world, PlotPlayer player) {
        UUID uuid = player.getUUID();
        return getPlots(world, uuid);
    }

    /**
     * Get all plots by a PlotPlayer.
     * @param area
     * @param player
     * @return Set of plot
     */
    public Set<Plot> getPlots(PlotArea area, PlotPlayer player) {
        UUID uuid = player.getUUID();
        return getPlots(area, uuid);
    }

    /**
     * Get all plots by a UUID in a world.
     * @param world
     * @param uuid
     * @return Set of plot
     */
    public Set<Plot> getPlots(String world, UUID uuid) {
        ArrayList<Plot> myPlots = new ArrayList<>();
        for (Plot plot : getPlots(world)) {
            if (plot.hasOwner()) {
                if (plot.isOwnerAbs(uuid)) {
                    myPlots.add(plot);
                }
            }
        }
        return new HashSet<>(myPlots);
    }

    /**
     * Get all plots by a UUID in an area.
     * @param area
     * @param uuid
     * @return Set of plot
     */
    public Set<Plot> getPlots(PlotArea area, UUID uuid) {
        ArrayList<Plot> myplots = new ArrayList<>();
        for (Plot plot : getPlots(area)) {
            if (plot.hasOwner()) {
                if (plot.isOwnerAbs(uuid)) {
                    myplots.add(plot);
                }
            }
        }
        return new HashSet<>(myplots);
    }

    /**
     * Use {@link #hasPlotArea(String)}.
     * Note: Worlds may have more than one plot area
     * @deprecated
     * @param world
     * @return
     */
    @Deprecated
    public boolean isPlotWorld(String world) {
        return this.plotAreaMap.containsKey(world);
    }

    /**
     * Check if a plot world.
     * @param world
     * @see #getPlotAreaByString(String) to get the PlotArea object
     * @return if a plot world is registered
     */
    public boolean hasPlotArea(String world) {
        switch (this.plotAreas.length) {
            case 0:
                return false;
            case 1:
                PlotArea a = this.plotAreas[0];
                return world.hashCode() == a.worldhash && (!this.plotAreaHasCollision || a.worldname.equals(world));
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                int hash = world.hashCode();
                for (PlotArea area : this.plotAreas) {
                    if (area.worldhash == hash && (!this.plotAreaHasCollision || area.worldname.equals(world))) {
                        return true;
                    }
                }
                return false;
            default:
                return this.plotAreaMap.containsKey(world);
        }
    }

    public Collection<Plot> getPlots(String world) {
        final HashSet<Plot> set = new HashSet<>();
        foreachPlotArea(world, new RunnableVal<PlotArea>() {
            @Override
            public void run(PlotArea value) {
                set.addAll(value.getPlots());
            }
        });
        return set;
    }

    public Collection<Plot> getPlots(PlotArea area) {
        return area == null ? new HashSet<Plot>() : area.getPlots();
    }

    public Plot getPlot(PlotArea area, PlotId id) {
        return area == null ? null : id == null ? null : area.getPlot(id);
    }

    /**
     * Get the plots for a PlotPlayer.
     * @param player
     * @return Set of Plot
     */
    public Set<Plot> getPlots(PlotPlayer player) {
        return getPlots(player.getUUID());
    }

    public Set<Plot> getBasePlots(PlotPlayer player) {
        return getBasePlots(player.getUUID());
    }

    /**
     * Get the plots for a UUID.
     * @param uuid
     * @return Set of Plot
     */
    public Set<Plot> getPlots(final UUID uuid) {
        final ArrayList<Plot> myPlots = new ArrayList<>();
        foreachPlot(new RunnableVal<Plot>() {
            @Override
            public void run(Plot value) {
                if (value.isOwnerAbs(uuid)) {
                    myPlots.add(value);
                }
            }
        });
        return new HashSet<>(myPlots);
    }

    public Set<Plot> getBasePlots(final UUID uuid) {
        final ArrayList<Plot> myplots = new ArrayList<>();
        foreachBasePlot(new RunnableVal<Plot>() {
            @Override
            public void run(Plot value) {
                if (value.isOwner(uuid)) {
                    myplots.add(value);
                }
            }
        });
        return new HashSet<>(myplots);
    }

    /**
     * Get the plots for a UUID.
     * @param uuid The UUID of the owner
     * @return Set of Plot
     */
    public Set<Plot> getPlotsAbs(final UUID uuid) {
        final ArrayList<Plot> myPlots = new ArrayList<>();
        foreachPlot(new RunnableVal<Plot>() {
            @Override
            public void run(Plot value) {
                if (value.isOwnerAbs(uuid)) {
                    myPlots.add(value);
                }
            }
        });
        return new HashSet<>(myPlots);
    }

    /**
     * Unregister a plot from local memory (does not call DB)
     * @param plot
     * @param callEvent If to call an event about the plot being removed
     * @return true if plot existed | false if it didn't
     */
    public boolean removePlot(Plot plot, boolean callEvent) {
        if (plot == null) {
            return false;
        }
        if (callEvent) {
            EventUtil.manager.callDelete(plot);
        }
        if (plot.getArea().removePlot(plot.getId())) {
            PlotId last = (PlotId) plot.getArea().getMeta("lastPlot");
            int last_max = Math.max(Math.abs(last.x), Math.abs(last.y));
            int this_max = Math.max(Math.abs(plot.getId().x), Math.abs(plot.getId().y));
            if (this_max < last_max) {
                plot.getArea().setMeta("lastPlot", plot.getId());
            }
            return true;
        }
        return false;
    }

    /**
     * This method is called by the PlotGenerator class normally<br>
     *  - Initializes the PlotArea and PlotManager classes<br>
     *  - Registers the PlotArea and PlotManager classes<br>
     *  - Loads (and/or generates) the PlotArea configuration<br>
     *  - Sets up the world border if configured<br>
     *  If loading an augmented plot world:<br>
     *  - Creates the AugmentedPopulator classes<br>
     *  - Injects the AugmentedPopulator classes if required
     * @param world The world to load
     * @param baseGenerator The generator for that world, or null if no generator
     */
    public void loadWorld(String world, GeneratorWrapper<?> baseGenerator) {
        if (world.equals("CheckingPlotSquaredGenerator")) {
            return;
        }
        if (!this.plotAreaHasCollision && !this.plotAreaHashCheck.add(world.hashCode())) {
            this.plotAreaHasCollision = true;
        }
        Set<String> worlds = this.config.contains("worlds") ? this.config.getConfigurationSection("worlds").getKeys(false) : new HashSet<String>();
        String path = "worlds." + world;
        ConfigurationSection worldSection = this.config.getConfigurationSection(path);
        int type = worldSection != null ? worldSection.getInt("generator.type") : 0;
        if (type == 0) {
            if (this.plotAreaMap.containsKey(world)) {
                debug("World possibly already loaded: " + world);
                return;
            }
            IndependentPlotGenerator pg;
            if (baseGenerator != null && baseGenerator.isFull()) {
                pg = baseGenerator.getPlotGenerator();
            } else if (worldSection != null) {
                String secondaryGeneratorName = worldSection.getString("generator.plugin");
                GeneratorWrapper<?> secondaryGenerator = this.IMP.getGenerator(world, secondaryGeneratorName);
                if (secondaryGenerator != null && secondaryGenerator.isFull()) {
                    pg = secondaryGenerator.getPlotGenerator();
                } else {
                    String primaryGeneratorName = worldSection.getString("generator.init");
                    GeneratorWrapper<?> primaryGenerator = this.IMP.getGenerator(world, primaryGeneratorName);
                    if (primaryGenerator != null && primaryGenerator.isFull()) {
                        pg = primaryGenerator.getPlotGenerator();
                    } else {
                        return;
                    }
                }
            } else {
                return;
            }
            // Conventional plot generator
            PlotArea plotArea = pg.getNewPlotArea(world, null, null, null);
            PlotManager plotManager = pg.getNewPlotManager();
            PS.log(C.PREFIX + "&aDetected world load for '" + world + "'");
            PS.log(C.PREFIX + "&3 - generator: &7" + baseGenerator + ">" + pg);
            PS.log(C.PREFIX + "&3 - plotworld: &7" + plotArea.getClass().getName());
            PS.log(C.PREFIX + "&3 - manager: &7" + plotManager.getClass().getName());
            if (!this.config.contains(path)) {
                this.config.createSection(path);
                worldSection = this.config.getConfigurationSection(path);
            }
            plotArea.saveConfiguration(worldSection);
            plotArea.loadDefaultConfiguration(worldSection);
            try {
                this.config.save(this.configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Now add it
            addPlotArea(plotArea);
            pg.initialize(plotArea);
            plotArea.setupBorder();
        } else {
            if (!worlds.contains(world)) {
                return;
            }
            ConfigurationSection areasSection = worldSection.getConfigurationSection("areas");
            if (areasSection == null) {
                if (this.plotAreaMap.containsKey(world)) {
                    debug("World possibly already loaded: " + world);
                    return;
                }
                PS.log(C.PREFIX + "&aDetected world load for '" + world + "'");
                String gen_string = worldSection.getString("generator.plugin", "PlotSquared");
                if (type == 2) {
                    Set<PlotCluster> clusters = this.clusters_tmp != null ? this.clusters_tmp.get(world) : new HashSet<PlotCluster>();
                    if (clusters == null) {
                        throw new IllegalArgumentException("No cluster exists for world: " + world);
                    }
                    ArrayDeque<PlotArea> toLoad = new ArrayDeque<>();
                    for (PlotCluster cluster : clusters) {
                        PlotId pos1 = cluster.getP1(); // Cluster pos1
                        PlotId pos2 = cluster.getP2(); // Cluster pos2
                        String name = cluster.getName(); // Cluster name
                        String fullId = name + "-" + pos1 + "-" + pos2;
                        worldSection.createSection("areas." + fullId);
                        DBFunc.replaceWorld(world, world + ";" + name, pos1, pos2); // NPE

                        PS.log(C.PREFIX + "&3 - " + name + "-" + pos1 + "-" + pos2);
                        GeneratorWrapper<?> areaGen = this.IMP.getGenerator(world, gen_string);
                        if (areaGen == null) {
                            throw new IllegalArgumentException("Invalid Generator: " + gen_string);
                        }
                        PlotArea pa = areaGen.getPlotGenerator().getNewPlotArea(world, name, pos1, pos2);
                        pa.saveConfiguration(worldSection);
                        pa.loadDefaultConfiguration(worldSection);
                        try {
                            this.config.save(this.configFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        PS.log(C.PREFIX + "&c | &9generator: &7" + baseGenerator + ">" + areaGen);
                        PS.log(C.PREFIX + "&c | &9plotworld: &7" + pa);
                        PS.log(C.PREFIX + "&c | &9manager: &7" + pa);
                        PS.log(C.PREFIX + "&cNote: &7Area created for cluster:" + name + " (invalid or old configuration?)");
                        areaGen.getPlotGenerator().initialize(pa);
                        areaGen.augment(pa);
                        toLoad.add(pa);
                    }
                    for (PlotArea area : toLoad) {
                        addPlotArea(area);
                    }
                    return;
                }
                GeneratorWrapper<?> areaGen = this.IMP.getGenerator(world, gen_string);
                if (areaGen == null) {
                    throw new IllegalArgumentException("Invalid Generator: " + gen_string);
                }
                PlotArea pa = areaGen.getPlotGenerator().getNewPlotArea(world, null, null, null);
                pa.saveConfiguration(worldSection);
                pa.loadDefaultConfiguration(worldSection);
                try {
                    this.config.save(this.configFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PS.log(C.PREFIX + "&3 - generator: &7" + baseGenerator + ">" + areaGen);
                PS.log(C.PREFIX + "&3 - plotworld: &7" + pa);
                PS.log(C.PREFIX + "&3 - manager: &7" + pa.getPlotManager());
                areaGen.getPlotGenerator().initialize(pa);
                areaGen.augment(pa);
                addPlotArea(pa);
                return;
            }
            if (type == 1) {
                throw new IllegalArgumentException("Invalid type for multi-area world. Expected `2`, got `" + type + "`");
            }
            for (String areaId : areasSection.getKeys(false)) {
                PS.log(C.PREFIX + "&3 - " + areaId);
                int i1 = areaId.indexOf("-");
                int i2 = areaId.indexOf(";");
                if (i1 == -1 || i2 == -1) {
                    throw new IllegalArgumentException("Invalid Area identifier: " + areaId + ". Expected form `<name>-<pos1>-<pos2>`");
                }
                String name = areaId.substring(0, i1);
                String rest = areaId.substring(i1 + 1);
                int i3 = rest.indexOf("-",  i2 - name.length() - 1);
                PlotId pos1 = PlotId.fromString(rest.substring(0, i3));
                PlotId pos2 = PlotId.fromString(rest.substring(i3 + 1));
                if (pos1 == null || pos2 == null || name.isEmpty()) {
                    throw new IllegalArgumentException("Invalid Area identifier: " + areaId + ". Expected form `<name>-<x1;z1>-<x2;z2>`");
                }
                if (getPlotAreaAbs(world, name) != null) {
                    continue;
                }
                ConfigurationSection section = areasSection.getConfigurationSection(areaId);
                YamlConfiguration clone = new YamlConfiguration();
                for (String key : section.getKeys(true)) {
                    if (section.get(key) instanceof MemorySection) {
                        continue;
                    }
                    if (!clone.contains(key)) {
                        clone.set(key, section.get(key));
                    }
                }
                for (String key : worldSection.getKeys(true)) {
                    if (worldSection.get(key) instanceof MemorySection) {
                        continue;
                    }
                    if (!key.startsWith("areas") && !clone.contains(key)) {
                        clone.set(key, worldSection.get(key));
                    }
                }
                String gen_string = clone.getString("generator.plugin", "PlotSquared");
                GeneratorWrapper<?> areaGen = this.IMP.getGenerator(world, gen_string);
                if (areaGen == null) {
                    throw new IllegalArgumentException("Invalid Generator: " + gen_string);
                }
                PlotArea pa = areaGen.getPlotGenerator().getNewPlotArea(world, name, pos1, pos2);
                pa.saveConfiguration(clone);
                // netSections is the combination of
                for (String key : clone.getKeys(true)) {
                    if (clone.get(key) instanceof MemorySection) {
                        continue;
                    }
                    if (!worldSection.contains(key)) {
                        worldSection.set(key, clone.get(key));
                    } else {
                        Object value = worldSection.get(key);
                        if (!Objects.equals(value, clone.get(key))) {
                            section.set(key, clone.get(key));
                        }
                    }
                }
                pa.loadDefaultConfiguration(clone);
                try {
                    this.config.save(this.configFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PS.log(C.PREFIX + "&aDetected area load for '" + world + "'");
                PS.log(C.PREFIX + "&c | &9generator: &7" + baseGenerator + ">" + areaGen);
                PS.log(C.PREFIX + "&c | &9plotworld: &7" + pa);
                PS.log(C.PREFIX + "&c | &9manager: &7" + pa.getPlotManager());
                areaGen.getPlotGenerator().initialize(pa);
                areaGen.augment(pa);
                addPlotArea(pa);
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
    public boolean setupPlotWorld(String world, String args, IndependentPlotGenerator generator) {
        if (args != null && !args.isEmpty()) {
            // save configuration
            String[] split = args.split(",");
            HybridPlotWorld plotworld = new HybridPlotWorld(world, null, generator, null, null);
            for (String element : split) {
                String[] pair = element.split("=");
                if (pair.length != 2) {
                    PS.log("&cNo value provided for: &7" + element);
                    return false;
                }
                String key = pair[0].toLowerCase();
                String value = pair[1];
                String base = "worlds." + world + ".";
                try {
                    switch (key) {
                        case "s":
                        case "size":
                            this.config.set(base + "plot.size", Configuration.INTEGER.parseString(value).shortValue());
                            break;
                        case "g":
                        case "gap":
                            this.config.set(base + "road.width", Configuration.INTEGER.parseString(value).shortValue());
                            break;
                        case "h":
                        case "height":
                            this.config.set(base + "road.height", Configuration.INTEGER.parseString(value).shortValue());
                            this.config.set(base + "plot.height", Configuration.INTEGER.parseString(value).shortValue());
                            this.config.set(base + "wall.height", Configuration.INTEGER.parseString(value).shortValue());
                            break;
                        case "f":
                        case "floor":
                            this.config.set(base + "plot.floor",
                                    new ArrayList<>(Arrays.asList(StringMan.join(Configuration.BLOCKLIST.parseString(value), ",").split(","))));
                            break;
                        case "m":
                        case "main":
                            this.config.set(base + "plot.filling",
                                    new ArrayList<>(Arrays.asList(StringMan.join(Configuration.BLOCKLIST.parseString(value), ",").split(","))));
                            break;
                        case "w":
                        case "wall":
                            this.config.set(base + "wall.filling", Configuration.BLOCK.parseString(value).toString());
                            break;
                        case "b":
                        case "border":
                            this.config.set(base + "wall.block", Configuration.BLOCK.parseString(value).toString());
                            break;
                        default:
                            PS.log("&cKey not found: &7" + element);
                            return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    PS.log("&cInvalid value: &7" + value + " in arg " + element);
                    return false;
                }
            }
            try {
                ConfigurationSection section = this.config.getConfigurationSection("worlds." + world);
                plotworld.saveConfiguration(section);
                plotworld.loadConfiguration(section);
                this.config.save(this.configFile);
            } catch (IOException e) {
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
        String[] split = Pattern.compile(".", Pattern.LITERAL).split(version);
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(String.format("%" + 4 + 's', s));
        }
        return sb.toString();
    }

    public boolean update(PlotPlayer sender, URL url) {
        try {
            String name = this.file.getName();
            File newJar = new File("plugins/update/" + name);
            MainUtil.sendMessage(sender, "$1Downloading from provided URL: &7" + url);
            URLConnection con = url.openConnection();
            try (InputStream stream = con.getInputStream()) {
                File parent = newJar.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                MainUtil.sendMessage(sender, "$2 - Output: " + newJar);
                if (!newJar.delete()) {
                    MainUtil.sendMessage(sender, "Failed to update PlotSquared");
                    MainUtil.sendMessage(sender, "Jar file failed to delete.");
                    MainUtil.sendMessage(sender, " - Please update manually");
                }
                Files.copy(stream, newJar.toPath());
            }
            MainUtil.sendMessage(sender, "$1The update will take effect when the server is restarted next");
            return true;
        } catch (IOException e) {
            MainUtil.sendMessage(sender, "Failed to update PlotSquared");
            MainUtil.sendMessage(sender, " - Please update manually");
            PS.log("============ Stacktrace ============");
            e.printStackTrace();
            PS.log("====================================");
        }
        return false;
    }

    /**
     * Copy a file from inside the jar to a location
     * @param file Name of the file inside PlotSquared.jar
     * @param folder The output location relative to /plugins/PlotSquared/
     */
    public void copyFile(String file, String folder) {
        try {
            File output = this.IMP.getDirectory();
            if (!output.exists()) {
                output.mkdirs();
            }
            File newFile = MainUtil.getFile(output, folder + File.separator + file);
            if (newFile.exists()) {
                return;
            }
            try (InputStream stream = this.IMP.getClass().getResourceAsStream(file)) {
                byte[] buffer = new byte[2048];
                if (stream == null) {
                    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(this.file))) {
                        ZipEntry ze = zis.getNextEntry();
                        while (ze != null) {
                            String name = ze.getName();
                            if (name.equals(file)) {
                                new File(newFile.getParent()).mkdirs();
                                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                                    int len;
                                    while ((len = zis.read(buffer)) > 0) {
                                        fos.write(buffer, 0, len);
                                    }
                                }
                                ze = null;
                            } else {
                                ze = zis.getNextEntry();
                            }
                        }
                        zis.closeEntry();
                    }
                    return;
                }
                newFile.createNewFile();
                try (FileOutputStream fos = new FileOutputStream(newFile)) {
                    int len;
                    while ((len = stream.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            PS.log("&cCould not save " + file);
        }
    }

    private Map<String, Map<PlotId, Plot>> getPlotsRaw() {
        HashMap<String, Map<PlotId, Plot>> map = new HashMap<>();
        for (PlotArea area : this.plotAreas) {
            Map<PlotId, Plot> map2 = map.get(area.toString());
            if (map2 == null) {
                map.put(area.toString(), area.getPlotsRaw());
            } else {
                map2.putAll(area.getPlotsRaw());
            }
        }
        return map;
    }

    /**
     * Close the database connection
     */
    public void disable() {
        try {
            this.TASK = null;
            this.database = null;
            // Validate that all data in the db is correct
            final HashSet<Plot> plots = new HashSet<>();
            foreachPlotRaw(new RunnableVal<Plot>() {
                @Override
                public void run(Plot value) {
                    plots.add(value);
                }
            });
            DBFunc.validatePlots(plots);

            // Close the connection
            DBFunc.close();
            UUIDHandler.handleShutdown();
        } catch (NullPointerException e) {
            PS.log("&cCould not close database connection!");
        }
    }

    /**
     * Setup the database connection
     */
    public void setupDatabase() {
        try {
            if (Settings.DB.USE_MONGO) {
                PS.log(C.PREFIX + "MongoDB is not yet implemented");
                PS.log(C.PREFIX + "&cNo storage type is set!");
                this.IMP.disable();
                return;
            }
            if (DBFunc.dbManager == null) {
                if (Settings.DB.USE_MYSQL) {
                    this.database = new MySQL(Settings.DB.HOST_NAME, Settings.DB.PORT, Settings.DB.DATABASE, Settings.DB.USER, Settings.DB.PASSWORD);
                } else if (Settings.DB.USE_SQLITE) {
                    this.database = new SQLite(this.IMP.getDirectory() + File.separator + Settings.DB.SQLITE_DB + ".db");
                } else {
                    PS.log(C.PREFIX + "&cNo storage type is set!");
                    this.IMP.disable();
                    return;
                }
            }
            DBFunc.dbManager = new SQLManager(this.database, Settings.DB.PREFIX, false);
            this.plots_tmp = DBFunc.getPlots();
            this.clusters_tmp = DBFunc.getClusters();
        } catch (ClassNotFoundException | SQLException e) {
            PS.log(C.PREFIX + "&cFailed to open DATABASE connection. The plugin will disable itself.");
            if (Settings.DB.USE_MONGO) {
                PS.log("$4MONGO");
            } else if (Settings.DB.USE_MYSQL) {
                PS.log("$4MYSQL");
            } else if (Settings.DB.USE_SQLITE) {
                PS.log("$4SQLITE");
            }
            PS.log("&d==== Here is an ugly stacktrace, if you are interested in those things ===");
            e.printStackTrace();
            PS.log("&d==== End of stacktrace ====");
            PS.log("&6Please go to the PlotSquared 'storage.yml' and configure the database correctly.");
            this.IMP.disable();
        }
    }

    /**
     * Setup the default flags for PlotSquared<br>
     *  - Create the flags
     *  - Register with FlagManager and parse raw flag values
     */
    public void setupDefaultFlags() {
        List<String> booleanFlags =
                Arrays.asList("notify-enter", "notify-leave", "item-drop", "invincible", "instabreak", "drop-protection", "forcefield", "titles",
                        "pve", "pvp",
                        "no-worldedit", "redstone");
        List<String> intervalFlags = Arrays.asList("feed", "heal");
        List<String> stringFlags = Arrays.asList("greeting", "farewell");
        List<String> intFlags = Arrays.asList("misc-cap", "entity-cap", "mob-cap", "animal-cap", "hostile-cap", "vehicle-cap", "music");
        for (String flag : stringFlags) {
            FlagManager.addFlag(new AbstractFlag(flag));
        }
        for (String flag : intervalFlags) {
            FlagManager.addFlag(new AbstractFlag(flag, new FlagValue.IntervalValue()));
        }
        for (String flag : booleanFlags) {
            FlagManager.addFlag(new AbstractFlag(flag, new FlagValue.BooleanValue()));
        }
        for (String flag : intFlags) {
            FlagManager.addFlag(new AbstractFlag(flag, new FlagValue.UnsignedIntegerValue()));
        }
        FlagManager.addFlag(new AbstractFlag("done", new FlagValue.StringValue()), true);
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
        FlagManager.addFlag(new AbstractFlag("player-interact", new FlagValue.BooleanValue()));
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
        FlagManager.addFlag(new AbstractFlag("ice-melt", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("block-ignition", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("block-burn", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("fire-spread", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("snow-melt", new FlagValue.BooleanValue()));
        FlagManager.addFlag(new AbstractFlag("keep") {
            @Override
            public Object parseValueRaw(String value) {
                if (MathMan.isInteger(value)) {
                    return Long.parseLong(value);
                }
                switch (value.toLowerCase()) {
                    case "true":
                        return true;
                    case "false":
                        return false;
                    default:
                        return MainUtil.timeToSec(value) * 1000 + System.currentTimeMillis();
                }
            }

            @Override
            public String getValueDesc() {
                return "Flag value must a timestamp or a boolean";
            }
        });
        FlagManager.addFlag(new AbstractFlag("gamemode") {

            @Override
            public PlotGameMode parseValueRaw(String value) {
                switch (value.toLowerCase()) {
                    case "survival":
                    case "s":
                    case "0":
                        return PlotGameMode.SURVIVAL;
                    case "creative":
                    case "c":
                    case "1":
                        return PlotGameMode.CREATIVE;
                    case "adventure":
                    case "a":
                    case "2":
                        return PlotGameMode.ADVENTURE;
                    case "spectator":
                    case "3":
                        return PlotGameMode.SPECTATOR;
                    default:
                        return PlotGameMode.NOT_SET;
                }
            }

            @Override
            public String getValueDesc() {
                return "Flag value must be a gamemode: 'creative' , 'survival', 'adventure' or 'spectator'";
            }
        });
        FlagManager.addFlag(new AbstractFlag("price", new FlagValue.UnsignedDoubleValue()));
        FlagManager.addFlag(new AbstractFlag("time", new FlagValue.LongValue()));
        FlagManager.addFlag(new AbstractFlag("weather") {

            @Override
            public PlotWeather parseValueRaw(String value) {
                switch (value.toLowerCase()) {
                    case "rain":
                    case "storm":
                    case "on":
                    case "lightning":
                    case "thunder":
                        return PlotWeather.RAIN;
                    case "clear":
                    case "off":
                    case "sun":
                        return PlotWeather.CLEAR;
                    default:
                        return null;
                }
            }

            @Override
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
        String lastVersionString = this.config.getString("version");
        if (lastVersionString != null) {
            String[] split = lastVersionString.split("\\.");
            this.lastVersion = new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])};
        }

        this.config.set("version", StringMan.join(this.version, "."));
        this.config.set("platform", this.platform);

        Map<String, Object> options = new HashMap<>();

        // Protection
        options.put("protection.redstone.disable-offline", Settings.REDSTONE_DISABLER);
        options.put("protection.redstone.disable-unoccupied", Settings.REDSTONE_DISABLER_UNOCCUPIED);

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
        options.put("kill_road_vehicles", Settings.KILL_ROAD_VEHICLES);
        options.put("kill_road_mobs", Settings.KILL_ROAD_MOBS);

        // Clearing + Expiry
        options.put("clear.fastmode", false);
        options.put("clear.on.ban", false);
        options.put("clear.auto.enabled", true);
        options.put("clear.auto.days", 7);
        options.put("clear.auto.clear-interval-seconds", Settings.CLEAR_INTERVAL);
        options.put("clear.auto.calibration.changes", 1);
        options.put("clear.auto.calibration.faces", 0);
        options.put("clear.auto.calibration.data", 0);
        options.put("clear.auto.calibration.air", 0);
        options.put("clear.auto.calibration.variety", 0);
        options.put("clear.auto.calibration.changes_sd", 1);
        options.put("clear.auto.calibration.faces_sd", 0);
        options.put("clear.auto.calibration.data_sd", 0);
        options.put("clear.auto.calibration.air_sd", 0);
        options.put("clear.auto.calibration.variety_sd", 0);
        options.put("clear.auto.confirmation", Settings.AUTO_CLEAR_CONFIRMATION); // TODO FIXME

        int keep = this.config.getInt("clear.keep-if-modified");
        int ignore = this.config.getInt("clear.ignore-if-modified");
        if (keep > 0 || ignore > 0) {
            options.put("clear.auto.threshold", 1);
            options.put("clear.auto.enabled", false);
            PS.log("&cIMPORTANT MESSAGE ABOUT THIS UPDATE!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            PS.log("&cSorry for all the exclamation marks, but this could be important.");
            PS.log("&cPlot clearing has changed to a new system that requires calibration.");
            PS.log("&cThis is how it will work: ");
            PS.log("&c - Players will rate plots");
            PS.log("&c - When enough plots are rated, you can run /plot debugexec calibrate-analysis");
            PS.log("&c - You can decide the (rough) percentage of expired plots to clear");
            PS.log("&c - To just clear all expired plot, ignore this and set: &7threshold: -1");
            PS.log("&cMore information:&7 https://github.com/IntellectualSites/PlotSquared/wiki/Plot-analysis:");
        } else {
            options.put("clear.auto.threshold", Settings.CLEAR_THRESHOLD);
        }
        this.config.set("clear.keep-if-modified", null);
        this.config.set("clear.ignore-if-modified", null);

        // Done
        options.put("approval.ratings.require-done", Settings.REQUIRE_DONE);
        options.put("approval.done.counts-towards-limit", Settings.DONE_COUNTS_TOWARDS_LIMIT);
        options.put("approval.done.restrict-building", Settings.DONE_RESTRICTS_BUILDING);
        options.put("approval.done.required-for-download", Settings.DOWNLOAD_REQUIRES_DONE);

        // Schematics
        if (StringMan.isEqual(this.config.getString("schematic.save_path"), "plugins/PlotSquared/schematics")) {
            this.config.set("schematics.save_path", Settings.SCHEMATIC_SAVE_PATH);
        }
        options.put("schematics.save_path", Settings.SCHEMATIC_SAVE_PATH);
        options.put("bo3.save_path", Settings.BO3_SAVE_PATH);

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
        options.put("teleport.on_death", Settings.TELEPORT_ON_DEATH);
        options.put("teleport.delay", Settings.TELEPORT_DELAY);

        // WorldEdit
        options.put("worldedit.require-selection-in-mask", Settings.REQUIRE_SELECTION);
        options.put("worldedit.queue-commands", Settings.QUEUE_COMMANDS);
        options.put("worldedit.enable-for-helpers", Settings.WE_ALLOW_HELPER);
        options.put("worldedit.max-volume", Settings.WE_MAX_VOLUME);
        options.put("worldedit.max-iterations", Settings.WE_MAX_ITERATIONS);
        options.put("worldedit.blacklist", Arrays.asList("cs", ".s", "restore", "snapshot", "delchunks", "listchunks"));

        // Chunk processor
        options.put("chunk-processor.enabled", Settings.CHUNK_PROCESSOR);
        options.put("chunk-processor.auto-unload", Settings.CHUNK_PROCESSOR_GC);
        options.put("chunk-processor.experimental-fast-async-worldedit", Settings.EXPERIMENTAL_FAST_ASYNC_WORLDEDIT);
        options.put("chunk-processor.auto-trim", Settings.CHUNK_PROCESSOR_TRIM_ON_SAVE);
        options.put("chunk-processor.max-blockstates", Settings.CHUNK_PROCESSOR_MAX_BLOCKSTATES);
        options.put("chunk-processor.max-entities", Settings.CHUNK_PROCESSOR_MAX_ENTITIES);
        options.put("chunk-processor.disable-physics", Settings.CHUNK_PROCESSOR_DISABLE_PHYSICS);

        // Comments
        options.put("comments.notifications.enabled", Settings.COMMENT_NOTIFICATIONS);

        // Plot limits
        options.put("global_limit", Settings.GLOBAL_LIMIT);
        options.put("max_plots", Settings.MAX_PLOTS);
        options.put("claim.max-auto-area", Settings.MAX_AUTO_SIZE);
        options.put("merge.remove-terrain", Settings.MERGE_REMOVES_ROADS);

        // Misc
        options.put("console.color", Settings.CONSOLE_COLOR);
        options.put("chat.fancy", Settings.FANCY_CHAT);
        options.put("metrics", true);
        options.put("debug", true);
        options.put("update-notifications", Settings.UPDATE_NOTIFICATIONS);

        for (Entry<String, Object> node : options.entrySet()) {
            if (!this.config.contains(node.getKey())) {
                this.config.set(node.getKey(), node.getValue());
            }
        }

        // Protection
        Settings.REDSTONE_DISABLER = this.config.getBoolean("protection.redstone.disable-offline");
        Settings.REDSTONE_DISABLER_UNOCCUPIED = this.config.getBoolean("protection.redstone.disable-unoccupied");

        // PlotMe
        Settings.USE_PLOTME_ALIAS = this.config.getBoolean("plotme-alias");
        Settings.CONVERT_PLOTME = this.config.getBoolean("plotme-convert.enabled");
        Settings.CACHE_PLOTME = this.config.getBoolean("plotme-convert.cache-uuids");

        // UUID
        Settings.USE_SQLUUIDHANDLER = this.config.getBoolean("uuid.use_sqluuidhandler");
        Settings.OFFLINE_MODE = this.config.getBoolean("UUID.offline");
        Settings.UUID_LOWERCASE = Settings.OFFLINE_MODE && this.config.getBoolean("UUID.force-lowercase");
        Settings.UUID_FROM_DISK = this.config.getBoolean("uuid.read-from-disk");

        // Mob stuff
        Settings.KILL_ROAD_MOBS = this.config.getBoolean("kill_road_mobs");
        Settings.KILL_ROAD_VEHICLES = this.config.getBoolean("kill_road_vehicles");

        // Clearing + Expiry
        Settings.FAST_CLEAR = this.config.getBoolean("clear.fastmode");
        Settings.DELETE_PLOTS_ON_BAN = this.config.getBoolean("clear.on.ban");
        Settings.AUTO_CLEAR_DAYS = this.config.getInt("clear.auto.days");
        Settings.CLEAR_THRESHOLD = this.config.getInt("clear.auto.threshold");
        Settings.AUTO_CLEAR = this.config.getBoolean("clear.auto.enabled");
        Settings.CLEAR_INTERVAL = this.config.getInt("clear.auto.clear-interval-seconds");

        // Clearing modifiers
        PlotAnalysis.MODIFIERS.changes = this.config.getInt("clear.auto.calibration.changes");
        PlotAnalysis.MODIFIERS.faces = this.config.getInt("clear.auto.calibration.faces");
        PlotAnalysis.MODIFIERS.data = this.config.getInt("clear.auto.calibration.data");
        PlotAnalysis.MODIFIERS.air = this.config.getInt("clear.auto.calibration.air");
        PlotAnalysis.MODIFIERS.variety = this.config.getInt("clear.auto.calibration.variety");
        PlotAnalysis.MODIFIERS.changes_sd = this.config.getInt("clear.auto.calibration.changes_sd");
        PlotAnalysis.MODIFIERS.faces_sd = this.config.getInt("clear.auto.calibration.faces_sd");
        PlotAnalysis.MODIFIERS.data_sd = this.config.getInt("clear.auto.calibration.data_sd");
        PlotAnalysis.MODIFIERS.air_sd = this.config.getInt("clear.auto.calibration.air_sd");
        PlotAnalysis.MODIFIERS.variety_sd = this.config.getInt("clear.auto.calibration.variety_sd");
        Settings.AUTO_CLEAR_CONFIRMATION = this.config.getBoolean("clear.auto.confirmation"); // TODO FIXME

        // Done
        Settings.REQUIRE_DONE = this.config.getBoolean("approval.ratings.require-done");
        Settings.DONE_COUNTS_TOWARDS_LIMIT = this.config.getBoolean("approval.done.counts-towards-limit");
        Settings.DONE_RESTRICTS_BUILDING = this.config.getBoolean("approval.done.restrict-building");
        Settings.DOWNLOAD_REQUIRES_DONE = this.config.getBoolean("approval.done.required-for-download");

        // Schematics
        Settings.SCHEMATIC_SAVE_PATH = this.config.getString("schematics.save_path");


        Settings.BO3_SAVE_PATH = this.config.getString("bo3.save_path");

        // Web
        Settings.WEB_URL = this.config.getString("web.url");
        Settings.WEB_IP = this.config.getString("web.server-ip");

        // Caching
        Settings.PERMISSION_CACHING = this.config.getBoolean("cache.permissions");
        Settings.CACHE_RATINGS = this.config.getBoolean("cache.ratings");

        // Rating system
        Settings.RATING_CATEGORIES = this.config.getStringList("ratings.categories");

        // Titles
        Settings.TITLES = this.config.getBoolean("titles");

        // Teleportation
        Settings.TELEPORT_DELAY = this.config.getInt("teleport.delay");
        Settings.TELEPORT_ON_LOGIN = this.config.getBoolean("teleport.on_login");
        Settings.TELEPORT_ON_DEATH = this.config.getBoolean("teleport.on_death");

        // WorldEdit
        Settings.QUEUE_COMMANDS = this.config.getBoolean("worldedit.queue-commands");
        Settings.REQUIRE_SELECTION = this.config.getBoolean("worldedit.require-selection-in-mask");
        Settings.WE_ALLOW_HELPER = this.config.getBoolean("worldedit.enable-for-helpers");
        Settings.WE_MAX_VOLUME = this.config.getLong("worldedit.max-volume");
        Settings.WE_MAX_ITERATIONS = this.config.getLong("worldedit.max-iterations");
        Settings.WE_BLACKLIST = this.config.getStringList("worldedit.blacklist");

        // Chunk processor
        Settings.CHUNK_PROCESSOR = this.config.getBoolean("chunk-processor.enabled");
        Settings.CHUNK_PROCESSOR_GC = this.config.getBoolean("chunk-processor.auto-unload");
        Settings.CHUNK_PROCESSOR_TRIM_ON_SAVE = this.config.getBoolean("chunk-processor.auto-trim");
        Settings.EXPERIMENTAL_FAST_ASYNC_WORLDEDIT = this.config.getBoolean("chunk-processor.experimental-fast-async-worldedit");
        Settings.CHUNK_PROCESSOR_MAX_BLOCKSTATES = this.config.getInt("chunk-processor.max-blockstates");
        Settings.CHUNK_PROCESSOR_MAX_ENTITIES = this.config.getInt("chunk-processor.max-entities");
        Settings.CHUNK_PROCESSOR_DISABLE_PHYSICS = this.config.getBoolean("chunk-processor.disable-physics");

        // Comments
        Settings.COMMENT_NOTIFICATIONS = this.config.getBoolean("comments.notifications.enabled");

        // Plot limits
        Settings.MAX_AUTO_SIZE = this.config.getInt("claim.max-auto-area");
        Settings.MAX_PLOTS = this.config.getInt("max_plots");
        if (Settings.MAX_PLOTS > 32767) {
            PS.log("&c`max_plots` Is set too high! This is a per player setting and does not need to be very large.");
            Settings.MAX_PLOTS = 32767;
        }
        Settings.GLOBAL_LIMIT = this.config.getBoolean("global_limit");

        // Misc
        Settings.DEBUG = this.config.getBoolean("debug");
        if (Settings.DEBUG) {
            PS.log(C.PREFIX + "&6Debug Mode Enabled (Default). Edit the config to turn this off.");
        }
        Settings.CONSOLE_COLOR = this.config.getBoolean("console.color");
        if (!this.config.getBoolean("chat.fancy") || !checkVersion(this.IMP.getServerVersion(), 1, 8, 0)) {
            Settings.FANCY_CHAT = false;
        }
        Settings.METRICS = this.config.getBoolean("metrics");
        Settings.UPDATE_NOTIFICATIONS = this.config.getBoolean("update-notifications");
        Settings.MERGE_REMOVES_ROADS = this.config.getBoolean("merge.remove-terrain");
        Settings.AUTO_PURGE = this.config.getBoolean("auto-purge", false);
    }

    /**
     * Setup all configuration files<br>
     *  - Config: settings.yml<br>
     *  - Storage: storage.yml<br>
     *  - Translation: PlotSquared.use_THIS.yml, style.yml<br>
     */
    public void setupConfigs() {
        File folder = new File(this.IMP.getDirectory() + File.separator + "config");
        if (!folder.exists() && !folder.mkdirs()) {
            PS.log(C.PREFIX + "&cFailed to create the /plugins/config folder. Please create it manually.");
        }
        try {
            this.styleFile = new File(this.IMP.getDirectory() + File.separator + "translations" + File.separator + "style.yml");
            if (!this.styleFile.exists()) {
                if (!this.styleFile.getParentFile().exists()) {
                    this.styleFile.getParentFile().mkdirs();
                }
                if (!this.styleFile.createNewFile()) {
                    PS.log("Could not create the style file, please create \"translations/style.yml\" manually");
                }
            }
            this.style = YamlConfiguration.loadConfiguration(this.styleFile);
            setupStyle();
        } catch (IOException err) {
            err.printStackTrace();
            PS.log("failed to save style.yml");
        }
        try {
            this.configFile = new File(this.IMP.getDirectory() + File.separator + "config" + File.separator + "settings.yml");
            if (!this.configFile.exists()) {
                if (!this.configFile.createNewFile()) {
                    PS.log("Could not create the settings file, please create \"settings.yml\" manually.");
                }
            }
            this.config = YamlConfiguration.loadConfiguration(this.configFile);
            setupConfig();
        } catch (IOException err_trans) {
            PS.log("Failed to save settings.yml");
        }
        try {
            this.storageFile = new File(this.IMP.getDirectory() + File.separator + "config" + File.separator + "storage.yml");
            if (!this.storageFile.exists()) {
                if (!this.storageFile.createNewFile()) {
                    PS.log("Could not the storage settings file, please create \"storage.yml\" manually.");
                }
            }
            this.storage = YamlConfiguration.loadConfiguration(this.storageFile);
            setupStorage();
        } catch (IOException err_trans) {
            PS.log("Failed to save storage.yml");
        }
        try {
            this.commandsFile = new File(this.IMP.getDirectory() + File.separator + "config" + File.separator + "commands.yml");
            if (!this.commandsFile.exists()) {
                if (!this.commandsFile.createNewFile()) {
                    PS.log("Could not the storage settings file, please create \"commands.yml\" manually.");
                }
            }
            this.commands = YamlConfiguration.loadConfiguration(this.commandsFile);
            setupStorage();
        } catch (IOException err_trans) {
            PS.log("Failed to save commands.yml");
        }
        try {
            this.style.save(this.styleFile);
            this.config.save(this.configFile);
            this.storage.save(this.storageFile);
            this.commands.save(this.commandsFile);
        } catch (IOException e) {
            PS.log("Configuration file saving failed");
            e.printStackTrace();
        }
    }

    /**
     * Setup the storage file (load + save missing nodes)
     */
    private void setupStorage() {
        this.storage.set("version", StringMan.join(this.version, "."));
        Map<String, Object> options = new HashMap<>(9);
        options.put("mysql.use", false);
        options.put("sqlite.use", true);
        options.put("sqlite.db", "storage");
        options.put("mysql.host", "localhost");
        options.put("mysql.port", "3306");
        options.put("mysql.user", "root");
        options.put("mysql.password", "password");
        options.put("mysql.database", "plot_db");
        options.put("prefix", "");
        for (Entry<String, Object> node : options.entrySet()) {
            if (!this.storage.contains(node.getKey())) {
                this.storage.set(node.getKey(), node.getValue());
            }
        }
        Settings.DB.USE_MYSQL = this.storage.getBoolean("mysql.use");
        Settings.DB.USER = this.storage.getString("mysql.user");
        Settings.DB.PASSWORD = this.storage.getString("mysql.password");
        Settings.DB.HOST_NAME = this.storage.getString("mysql.host");
        Settings.DB.PORT = this.storage.getString("mysql.port");
        Settings.DB.DATABASE = this.storage.getString("mysql.database");
        Settings.DB.USE_SQLITE = this.storage.getBoolean("sqlite.use");
        Settings.DB.SQLITE_DB = this.storage.getString("sqlite.db");
        Settings.DB.PREFIX = this.storage.getString("prefix");
    }

    /**
     * Show startup debug information.
     */
    private void showDebug() {
        if (Settings.DEBUG) {
            Map<String, String> settings = new HashMap<>(9);
            settings.put("Kill Road Mobs", "" + Settings.KILL_ROAD_MOBS);
            settings.put("Use Metrics", "" + Settings.METRICS);
            settings.put("Delete Plots On Ban", "" + Settings.DELETE_PLOTS_ON_BAN);
            settings.put("DB Mysql Enabled", "" + Settings.DB.USE_MYSQL);
            settings.put("DB SQLite Enabled", "" + Settings.DB.USE_SQLITE);
            settings.put("Auto Clear Enabled", "" + Settings.AUTO_CLEAR);
            settings.put("Auto Clear Days", "" + Settings.AUTO_CLEAR_DAYS);
            settings.put("Schematics Save Path", "" + Settings.SCHEMATIC_SAVE_PATH);
            settings.put("API Location", "" + Settings.API_URL);
            for (Entry<String, String> setting : settings.entrySet()) {
                PS.log(C.PREFIX + String.format("&cKey: &6%s&c, Value: &6%s", setting.getKey(), setting.getValue()));
            }
        }
    }

    /**
     * Setup the style.yml file
     */
    private void setupStyle() {
        this.style.set("version", StringMan.join(this.version, "."));
        Map<String, Object> o = new HashMap<>(4);
        o.put("color.1", "6");
        o.put("color.2", "7");
        o.put("color.3", "8");
        o.put("color.4", "3");
        if (!this.style.contains("color")) {
            for (Entry<String, Object> node : o.entrySet()) {
                this.style.set(node.getKey(), node.getValue());
            }
        }
    }

    /**
     * Get the java version.
     * @return Java version as a double
     */
    public double getJavaVersion() {
        return Double.parseDouble(System.getProperty("java.specification.version"));
    }

    public void foreachPlotArea(RunnableVal<PlotArea> runnable) {
        for (PlotArea area : this.plotAreas) {
            runnable.run(area);
        }
    }

    public void foreachPlot(RunnableVal<Plot> runnable) {
        for (PlotArea area : this.plotAreas) {
            for (Plot plot : area.getPlots()) {
                runnable.run(plot);
            }
        }
    }

    public void foreachPlotRaw(RunnableVal<Plot> runnable) {
        for (PlotArea area : this.plotAreas) {
            for (Plot plot : area.getPlots()) {
                runnable.run(plot);
            }
        }
        if (this.plots_tmp != null) {
            for (Entry<String, HashMap<PlotId, Plot>> entry : this.plots_tmp.entrySet()) {
                for (Entry<PlotId, Plot> entry2 : entry.getValue().entrySet()) {
                    runnable.run(entry2.getValue());
                }
            }
        }
    }

    public void foreachBasePlot(RunnableVal<Plot> run) {
        for (PlotArea area : this.plotAreas) {
            area.foreachBasePlot(run);
        }
    }

    public void foreachPlotArea(String world, RunnableVal<PlotArea> runnable) {
        PlotArea[] array = this.plotAreaMap.get(world);
        if (array == null) {
            return;
        }
        for (PlotArea area : array) {
            runnable.run(area);
        }
    }

    public PlotArea getFirstPlotArea() {
        return this.plotAreas.length > 0 ? this.plotAreas[0] : null;
    }

    public int getPlotAreaCount() {
        return this.plotAreas.length;
    }

    public int getPlotCount() {
        int count = 0;
        for (PlotArea area : this.plotAreas) {
            count += area.getPlotCount();
        }
        return count;
    }

    public int getPlotAreaCount(String world) {
        return this.plotAreaMap.size();
    }

    public Set<PlotArea> getPlotAreas() {
        HashSet<PlotArea> set = new HashSet<>(this.plotAreas.length);
        Collections.addAll(set, this.plotAreas);
        return set;
    }

    /**
     * @deprecated Since worlds can have multiple plot areas
     * @return Set of world names
     */
    @Deprecated
    public Set<String> getPlotWorldStrings() {
        HashSet<String> set = new HashSet<>(this.plotAreaMap.size());
        for (Entry<String, PlotArea[]> entry : this.plotAreaMap.entrySet()) {
            set.add(entry.getKey());
        }
        return set;
    }

    public boolean isAugmented(String world) {
        PlotArea[] areas = this.plotAreaMap.get(world);
        if (areas == null) {
            return false;
        }
        if (areas.length > 1) {
            return true;
        }
        return areas[0].TYPE != 0;
    }

    /**
     * Get a list of PlotArea objects.
     * @param world
     * @return Collection of PlotArea objects
     */
    public Set<PlotArea> getPlotAreas(String world) {
        PlotArea[] areas = this.plotAreaMap.get(world);
        if (areas == null) {
            return new HashSet<>(0);
        }
        HashSet<PlotArea> set = new HashSet<>(areas.length);
        Collections.addAll(set, areas);
        return set;
    }

    public enum SortType {
        CREATION_DATE, CREATION_DATE_TIMESTAMP, LAST_MODIFIED, DISTANCE_FROM_ORIGIN
    }
}
