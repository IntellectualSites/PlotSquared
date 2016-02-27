package com.intellectualcrafters.plot;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.configuration.MemorySection;
import com.intellectualcrafters.configuration.file.YamlConfiguration;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.commands.WE_Anywhere;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.*;
import com.intellectualcrafters.plot.flag.AbstractFlag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.flag.FlagValue;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.*;
import com.intellectualcrafters.plot.util.area.QuadMap;
import com.plotsquared.listener.WESubscriber;
import com.sk89q.worldedit.WorldEdit;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * An implementation of the core,
 * with a static getter for easy access
 *
 * @author Sauilitired | Citymonstret
 * @author boy0001 | Empire92
 */
public class PS {
    
    // protected static:
    private static PS instance;
    /**
     * All plot areas mapped by world (quick world access)
     */
    private final HashMap<String, PlotArea[]> plotareamap = new HashMap<>();
    /**
     * All plot areas mapped by location (quick location based access)
     */
    private final HashMap<String, QuadMap<PlotArea>> plotareagrid = new HashMap<>();
    public HashMap<String, Set<PlotCluster>> clusters_tmp;
    public HashMap<String, HashMap<PlotId, Plot>> plots_tmp;
    // public:
    public File styleFile;
    public File configFile;
    public File translationFile;
    public YamlConfiguration style;
    public YamlConfiguration config;
    public YamlConfiguration storage;
    public IPlotMain IMP = null;
    public TaskManager TASK;
    public WorldEdit worldedit;
    public URL update;
    /**
     * All plot areas (quick global access)
     */
    private PlotArea[] plotareas = new PlotArea[0];
    // private:
    private File storageFile;
    private File FILE = null; // This file
    private int[] VERSION = null;
    private String PLATFORM = null;
    private String LAST_VERSION;
    private Database database;
    private Connection connection;
    private Thread thread;
    
    /**
     * Initialize PlotSquared with the desired Implementation class
     * @param imp_class
     */
    public PS(final IPlotMain imp_class, final String platform) {
        try {
            instance = this;
            thread = Thread.currentThread();
            SetupUtils.generators = new HashMap<>();
            IMP = imp_class;
            new ReflectionUtils(IMP.getNMSPackage());
            try {
                URL url = PS.class.getProtectionDomain().getCodeSource().getLocation();
                FILE = new File(new URL(url.toURI().toString().split("\\!")[0].replaceAll("jar:file", "file")).toURI().getPath());
            } catch (MalformedURLException | URISyntaxException | SecurityException | NullPointerException e) {
                e.printStackTrace();
                FILE = new File(IMP.getDirectory().getParentFile(), "PlotSquared.jar");
                if (!FILE.exists()) {
                    FILE = new File(IMP.getDirectory().getParentFile(), "PlotSquared-" + platform + ".jar");
                }
            }
            VERSION = IMP.getPluginVersion();
            PLATFORM = platform;
            EconHandler.manager = IMP.getEconomyHandler();
            if (getJavaVersion() < 1.7) {
                log(C.CONSOLE_JAVA_OUTDATED_1_7);
                IMP.disable();
                return;
            }
            if (getJavaVersion() < 1.8) {
                log(C.CONSOLE_JAVA_OUTDATED_1_8);
            }
            TASK = IMP.getTaskManager();
            if (!C.ENABLED.s().isEmpty()) {
                log(C.ENABLED.s());
            }
            setupConfigs();
            translationFile = new File(IMP.getDirectory() + File.separator + "translations" + File.separator + "PlotSquared.use_THIS.yml");
            C.load(translationFile);
            setupDefaultFlags();
            setupDatabase();
            CommentManager.registerDefaultInboxes();
            // Tasks
            if (Settings.KILL_ROAD_MOBS || Settings.KILL_ROAD_VEHICLES) {
                IMP.runEntityTask();
            }
            if (IMP.initWorldEdit()) {
                worldedit = WorldEdit.getInstance();
                WorldEdit.getInstance().getEventBus().register(new WESubscriber());
                MainCommand.getInstance().createCommand(new WE_Anywhere());
            }
            
            // Events
            IMP.registerCommands();
            IMP.registerPlayerEvents();
            IMP.registerInventoryEvents();
            IMP.registerPlotPlusEvents();
            IMP.registerForceFieldEvents();
            IMP.registerWorldEvents();
            if (Settings.METRICS) {
                IMP.startMetrics();
            } else {
                log(C.CONSOLE_PLEASE_ENABLE_METRICS);
            }
            IMP.startMetrics();
            if (Settings.CHUNK_PROCESSOR) {
                IMP.registerChunkProcessor();
            }
            // create UUIDWrapper
            UUIDHandler.implementation = IMP.initUUIDHandler();
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    PS.debug("Starting UUID caching");
                    UUIDHandler.startCaching(new Runnable() {
                        @Override
                        public void run() {
                            for (final Plot plot : getPlots()) {
                                if ((plot.hasOwner()) && (plot.temp != -1)) {
                                    if (UUIDHandler.getName(plot.owner) == null) {
                                        UUIDHandler.implementation.unknown.add(plot.owner);
                                    }
                                }
                            }
                            
                            // Auto clearing
                            if (Settings.AUTO_CLEAR) {
                                ExpireManager.runTask();
                            }
                            
                            // PlotMe
                            if (Settings.CONVERT_PLOTME || Settings.CACHE_PLOTME) {
                                TaskManager.runTaskLater(new Runnable() {
                                    
                                    @Override
                                    public void run() {
                                        if (IMP.initPlotMeConverter()) {
                                            log("&c=== IMPORTANT ===");
                                            log("&cTHIS MESSAGE MAY BE EXTREMELY HELPFUL IF YOU HAVE TROUBLE CONVERTING PLOTME!");
                                            log("&c - Make sure 'UUID.read-from-disk' is disabled (false)!");
                                            log("&c - Sometimes the database can be locked, deleting PlotMe.jar beforehand will fix the issue!");
                                            log("&c - After the conversion is finished, please set 'plotme-convert.enabled' to false in the 'settings.yml'");
                                        }
                                    }
                                }, 20);
                            }
                        }
                    });
                }
            }, 20);
            // create event util class
            EventUtil.manager = IMP.initEventUtil();
            // create Hybrid utility class
            HybridUtils.manager = IMP.initHybridUtils();
            // Inventory utility class
            InventoryUtil.manager = IMP.initInventoryUtil();
            // create setup util class
            SetupUtils.manager = IMP.initSetupUtils();
            // World Util
            WorldUtil.IMP = IMP.initWorldUtil();
            // Set block
            SetQueue.IMP.queue = IMP.initPlotQueue();
            // Set chunk
            ChunkManager.manager = IMP.initChunkManager();
            // Schematic handler
            SchematicHandler.manager = IMP.initSchematicHandler();
            // Titles
            AbstractTitle.TITLE_CLASS = IMP.initTitleManager();
            // Chat
            ChatManager.manager = IMP.initChatManager();
            
            // Check for updates
            TaskManager.runTaskAsync(new Runnable() {
                @Override
                public void run() {
                    final URL url = getUpdate();
                    if (url != null) {
                        update = url;
                    } else if ((LAST_VERSION != null) && !StringMan.join(VERSION, ".").equals(LAST_VERSION)) {
                        log("&aThanks for updating from: " + LAST_VERSION + " to " + StringMan.join(VERSION, "."));
                    }
                }
            });
            
            // World generators:
            final ConfigurationSection section = config.getConfigurationSection("worlds");
            if (section != null) {
                for (final String world : section.getKeys(false)) {
                    if (world.equals("CheckingPlotSquaredGenerator")) {
                        continue;
                    }
                    if (WorldUtil.IMP.isWorld(world)) {
                        IMP.setGenerator(world);
                    }
                }
                TaskManager.runTaskLater(new Runnable() {
                    @Override
                    public void run() {
                        for (final String world : section.getKeys(false)) {
                            if (world.equals("CheckingPlotSquaredGenerator")) {
                                continue;
                            }
                            if (!WorldUtil.IMP.isWorld(world)) {
                                PS.debug("&c`" + world + "` was not properly loaded - PlotSquared will now try to load it properly: ");
                                PS.debug("&8 - &7Are you trying to delete this world? Remember to remove it from the settings.yml, bukkit.yml and multiverse worlds.yml");
                                PS.debug("&8 - &7Your world management plugin may be faulty (or non existant)");
                                IMP.setGenerator(world);
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
        } catch (final Throwable e) {
            e.printStackTrace();
        }
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
     * Log a message to the IPlotMain logger
     *
     * @param message Message to log
     * @see IPlotMain#log(String)
     */
    public static void log(final Object message) {
        get().IMP.log(StringMan.getString(message));
    }
    
    public static void stacktrace() {
        System.err.println(StringMan.join(new Exception().getStackTrace(), "\n\tat "));
    }

    /**
     * Log a message to the IPlotMain logger
     *
     * @param message Message to log
     * @see IPlotMain#log(String)
     */
    public static void debug(final Object message) {
        if (Settings.DEBUG) {
            log(message);
        }
    }

    public boolean isMainThread(final Thread thread) {
        return this.thread == thread;
    }

    public boolean checkVersion(final int[] version, final int major, final int minor, final int minor2) {
        return (version[0] > major) || ((version[0] == major) && (version[1] > minor)) || ((version[0] == major) && (version[1] == minor) && (
                version[2] >= minor2));
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
     * Get the platform this is running on (Bukkit, Sponge)
     * @return
     */
    public String getPlatform() {
        return PLATFORM;
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
    public boolean updatePlot(final Plot plot) {
        return plot.getArea().addPlot(plot);
    }
    
    /**
     * Get the relevant plot area for a location.<br>
     *  - If there is only one plot area globally that will be returned<br>
     *  - If there is only one plot area in the world, it will return that<br>
     *  - If the plot area for a location cannot be unambiguously resolved; null will be returned<br>
     *  <br>
     * Note: An applicable plot area may not include the location i.e. clusters
     * @param loc
     * @return
     */
    public PlotArea getApplicablePlotArea(Location loc) {
        switch (plotareas.length) {
            case 0:
                return null;
            case 1:
                return plotareas[0];
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                String world = loc.getWorld();
                int hash = world.hashCode();
                for (PlotArea area : plotareas) {
                    if (hash == area.worldhash) {
                        if (area.contains(loc.getX(), loc.getZ()) && world.equals(area.worldname)) {
                            return area;
                        }
                    }
                }
                return null;
            default:
                PlotArea[] areas = plotareamap.get(loc.getWorld());
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
                        x = loc.getX();
                        y = loc.getY();
                        for (PlotArea area : areas) {
                            if (area.contains(x, y)) {
                                return area;
                            }
                        }
                        return null;
                    default:
                        QuadMap<PlotArea> search = plotareagrid.get(loc.getWorld());
                        return search.get(loc.getX(), loc.getZ());
                }
        }
    }
    
    public PlotArea getPlotArea(String world, String id) {
        PlotArea[] areas = plotareamap.get(world);
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
        PlotArea[] areas = plotareamap.get(world);
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
        PlotArea[] areas = plotareamap.get(split[0]);
        if (areas == null) {
            for (PlotArea area : plotareas) {
                if (area.worldname.equalsIgnoreCase(split[0])) {
                    if (area.id == null || (split.length == 2 && area.id.equalsIgnoreCase(split[1]))) {
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
        QuadMap<PlotArea> areas = plotareagrid.get(world);
        return areas != null ? areas.get(region) : new HashSet<PlotArea>();
    }

    /**
     * Get the plot area which contains a location.<br>
     *  - If the plot area does not contain a location, null will be returned
     * 
     * @param loc
     * @return
     */
    public PlotArea getPlotAreaAbs(Location loc) {
        switch (plotareas.length) {
            case 0:
                return null;
            case 1:
                PlotArea pa = plotareas[0];
                return pa.contains(loc) ? pa : null;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                String world = loc.getWorld();
                int hash = world.hashCode();
                for (PlotArea area : plotareas) {
                    if (hash == area.worldhash) {
                        if (area.contains(loc.getX(), loc.getZ()) && world.equals(area.worldname)) {
                            return area;
                        }
                    }
                }
                return null;
            default:
                PlotArea[] areas = plotareamap.get(loc.getWorld());
                if (areas == null) {
                    return null;
                }
                int x;
                int y;
                switch (areas.length) {
                    case 0:
                        PlotArea a = areas[0];
                        return a.contains(loc.getX(), loc.getZ()) ? a : null;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                        x = loc.getX();
                        y = loc.getY();
                        for (PlotArea area : areas) {
                            if (area.contains(x, y)) {
                                return area;
                            }
                        }
                        return null;
                    default:
                        QuadMap<PlotArea> search = plotareagrid.get(loc.getWorld());
                        return search.get(loc.getX(), loc.getZ());
                }
        }
    }

    public PlotManager getPlotManager(Plot plot) {
        return plot.getArea().manager;
    }
    
    public PlotManager getPlotManager(Location loc) {
        PlotArea pa = getPlotAreaAbs(loc);
        return pa != null ? pa.manager : null;
    }

    /**
     * Add a global reference to a plot world
     *
     * @param plotarea     World Name
     * @see #removePlotArea(PlotArea) To remove the reference
     */
    public void addPlotArea(final PlotArea plotarea) {
        HashMap<PlotId, Plot> plots = plots_tmp.remove(plotarea.toString());
        if (plots == null) {
            if (plotarea.TYPE == 2) {
                plots = plots_tmp.get(plotarea.worldname);
                if (plots != null) {
                    Iterator<Entry<PlotId, Plot>> iter = plots.entrySet().iterator();
                    while (iter.hasNext()) {
                        Entry<PlotId, Plot> next = iter.next();
                        PlotId id = next.getKey();
                        if (plotarea.contains(id)) {
                            next.getValue().setArea(plotarea);
                            iter.remove();
                        }
                    }
                }
            }
        } else {
            for (Entry<PlotId, Plot> entry : plots.entrySet()) {
                Plot plot = entry.getValue();
                plot.setArea(plotarea);
            }
        }
        if (Settings.ENABLE_CLUSTERS) {
            Set<PlotCluster> clusters = clusters_tmp.remove(plotarea.toString());
            if (clusters == null) {
                if (plotarea.TYPE == 2) {
                    clusters = clusters_tmp.get(plotarea.worldname);
                    if (clusters != null) {
                        Iterator<PlotCluster> iter = clusters.iterator();
                        while (iter.hasNext()) {
                            PlotCluster next = iter.next();
                            if (next.intersects(plotarea.getMin(), plotarea.getMax())) {
                                next.setArea(plotarea);
                                iter.remove();
                            }
                        }
                    }
                }
            } else {
                for (PlotCluster cluster : clusters) {
                    cluster.setArea(plotarea);
                }
            }
        }
        Set<PlotArea> localAreas = getPlotAreas(plotarea.worldname);
        Set<PlotArea> globalAreas = getPlotAreas();
        localAreas.add(plotarea);
        globalAreas.add(plotarea);
        plotareas = globalAreas.toArray(new PlotArea[globalAreas.size()]);
        plotareamap.put(plotarea.worldname, localAreas.toArray(new PlotArea[localAreas.size()]));
        QuadMap<PlotArea> map = plotareagrid.get(plotarea.worldname);
        if (map == null) {
            map = new QuadMap<PlotArea>(Integer.MAX_VALUE, 0, 0) {
                @Override
                public RegionWrapper getRegion(PlotArea value) {
                    return value.getRegion();
                }
            };
            plotareagrid.put(plotarea.worldname, map);
        }
        map.add(plotarea);
    }
    
    /**
     * Remove a plot world reference
     *
     * @param area World name
     * @see #addPlotArea(PlotArea) To add a reference
     */
    public void removePlotArea(final PlotArea area) {
        Set<PlotArea> areas = getPlotAreas(area.worldname);
        areas.remove(area);
        plotareas = areas.toArray(new PlotArea[areas.size()]);
        if (areas.isEmpty()) {
            plotareamap.remove(area.worldname);
            plotareagrid.remove(area.worldname);
        } else {
            plotareamap.put(area.worldname, areas.toArray(new PlotArea[areas.size()]));
            plotareagrid.get(area.worldname).remove(area);
        }
        setPlotsTmp(area);
    }
    
    public void removePlotAreas(String world) {
        for (PlotArea area : getPlotAreas(world)) {
            removePlotArea(area);
        }
    }

    private void setPlotsTmp(PlotArea area) {
        if (plots_tmp == null) {
            plots_tmp = new HashMap<>();
        }
        HashMap<PlotId, Plot> map = plots_tmp.get(area.toString());
        if (map == null) {
            map = new HashMap<>();
            plots_tmp.put(area.toString(), map);
        }
        for (Plot plot : area.getPlots()) {
            map.put(plot.getId(), plot);
        }
        if (clusters_tmp == null) {
            clusters_tmp = new HashMap<>();
        }
        clusters_tmp.put(area.toString(), area.getClusters());
    }
    
    public Set<PlotCluster> getClusters(String world) {
        HashSet<PlotCluster> set = new HashSet<>();
        if (Settings.ENABLE_CLUSTERS) {
            for (PlotArea area : getPlotAreas(world)) {
                set.addAll(area.getClusters());
            }
        }
        return set;

    }

    /**
     * A more generic way to filter plots - make your own method if you need complex filters
     * @param filters
     * @return
     */
    public Set<Plot> getPlots(final PlotFilter... filters) {
        final HashSet<Plot> set = new HashSet<>();
        foreachPlotArea(new RunnableVal<PlotArea>() {
            @Override
            public void run(PlotArea value) {
                for (final PlotFilter filter : filters) {
                    if (!filter.allowsArea(value)) {
                    }
                }
                for (Entry<PlotId, Plot> entry2 : value.getPlotEntries()) {
                    Plot plot = entry2.getValue();
                    for (final PlotFilter filter : filters) {
                        if (!filter.allowsPlot(plot)) {
                        }
                    }
                    set.add(plot);
                }
            }
        });
        return set;
    }

    /**
     * Get all the plots in a single set
     * @return Set of Plot
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
        if (plots_tmp == null) {
            plots_tmp = new HashMap<>();
        }
        for (Entry<String, HashMap<PlotId, Plot>> entry : plots.entrySet()) {
            String world = entry.getKey();
            PlotArea area = getPlotArea(world, null);
            if (area == null) {
                HashMap<PlotId, Plot> map = plots_tmp.get(world);
                if (map == null) {
                    map = new HashMap<>();
                    plots_tmp.put(world, map);
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
     * Get all the base plots in a single set (for merged plots it just returns the bottom plot)
     * @return Set of base Plot
     */
    public Set<Plot> getBasePlots() {
        int size = getPlotCount();
        final Set<Plot> result = new HashSet<>(size);
        foreachPlotArea(new RunnableVal<PlotArea>() {
            @Override
            public void run(PlotArea value) {
                for (Plot plot : value.getPlots()) {
                    if (plot.getMerged(0) || plot.getMerged(3)) {
                        continue;
                    }
                    result.add(plot);
                }
            }
        });
        return result;
    }

    public ArrayList<Plot> sortPlotsByTemp(final Collection<Plot> plots) {
        int max = 0;
        int overflowCount = 0;
        for (final Plot plot : plots) {
            if (plot.temp > 0) {
                if (plot.temp > max) {
                    max = plot.temp;
                }
            } else {
                overflowCount++;
            }
        }
        final Plot[] array = new Plot[max + 1];
        final List<Plot> overflow = new ArrayList<>(overflowCount);
        for (final Plot plot : plots) {
            if (plot.temp <= 0) {
                overflow.add(plot);
            } else {
                array[plot.temp] = plot;
            }
        }
        final ArrayList<Plot> result = new ArrayList<>(plots.size());
        for (final Plot plot : array) {
            if (plot != null) {
                result.add(plot);
            }
        }
        Collections.sort(overflow, new Comparator<Plot>() {
            @Override
            public int compare(final Plot a, final Plot b) {
                return a.hashCode() - b.hashCode();
            }
        });
        result.addAll(overflow);
        return result;
    }
    
    /**
     * Sort plots by hashcode
     * @param plots
     * @return
     * @deprecated Unchecked, please use {@link #sortPlots(Collection, SortType, PlotArea)} which has additional checks before calling this
     */
    @Deprecated
    public ArrayList<Plot> sortPlotsByHash(final Collection<Plot> plots) {
        int hardmax = 256000;
        int max = 0;
        int overflowSize = 0;
        for (final Plot plot : plots) {
            final int hash = MathMan.getPositiveId(plot.hashCode());
            if (hash > max) {
                if (hash >= hardmax) {
                    overflowSize++;
                } else {
                    max = hash;
                }
            }
        }
        hardmax = Math.min(hardmax, max);
        final Plot[] cache = new Plot[hardmax + 1];
        final List<Plot> overflow = new ArrayList<>(overflowSize);
        final ArrayList<Plot> extra = new ArrayList<>();
        for (final Plot plot : plots) {
            final int hash = MathMan.getPositiveId(plot.hashCode());
            if (hash < hardmax) {
                if (hash >= 0) {
                    cache[hash] = plot;
                } else {
                    extra.add(plot);
                }
            } else if ((Math.abs(plot.getId().x) > 15446) || (Math.abs(plot.getId().y) > 15446)) {
                extra.add(plot);
            } else {
                overflow.add(plot);
            }
        }
        final Plot[] overflowArray = overflow.toArray(new Plot[overflow.size()]);
        sortPlotsByHash(overflowArray);
        final ArrayList<Plot> result = new ArrayList<>(cache.length + overflowArray.length);
        for (final Plot plot : cache) {
            if (plot != null) {
                result.add(plot);
            }
        }
        Collections.addAll(result, overflowArray);
        for (final Plot plot : extra) {
            result.add(plot);
        }
        return result;
    }
    
    /**
     * Sort plots by creation timestamp
     * @param input
     * @deprecated Unchecked, use {@link #sortPlots(Collection, SortType, PlotArea)} instead which will call this after checks
     * @return
     */
    @Deprecated
    public ArrayList<Plot> sortPlotsByTimestamp(final Collection<Plot> input) {
        List<Plot> list;
        if (input instanceof ArrayList<?>) {
            list = (List<Plot>) input;
        } else {
            list = new ArrayList<>(input);
        }
        long min = Integer.MAX_VALUE;
        long max = 0;
        final int size = list.size();
        final int limit = Math.min(1048576, size * 2);
        for (final Plot plot : list) {
            final long time = plot.getTimestamp();
            if (time < min) {
                min = time;
            }
            if (time > max) {
                max = time;
            }
        }
        final long range = max - min;
        try {
            final ArrayList<Plot> overflow = new ArrayList<>();
            Plot[] plots;
            if ((range > limit) && (size > 1024)) {
                plots = new Plot[limit];
                final int factor = (int) ((range / limit));
                for (Plot plot : list) {
                    int index = (int) (plot.getTimestamp() - min) / factor;
                    if (index < 0) {
                        index = 0;
                    }
                    if (index >= plots.length) {
                        overflow.add(plot);
                        continue;
                    }
                    Plot current = plots[index];
                    while (true) {
                        if (current == null) {
                            plots[index] = plot;
                            break;
                        }
                        if (current.getTimestamp() > plot.getTimestamp()) {
                            plots[index] = plot;
                            plot = current;
                        }
                        index++;
                        if (index >= plots.length) {
                            overflow.add(plot);
                            break;
                        }
                        current = plots[index];
                    }
                }
            } else if ((range < size) || (size < 1024)) {
                final ArrayList<Plot> result = new ArrayList<>(list);
                Collections.sort(result, new Comparator<Plot>() {
                    @Override
                    public int compare(final Plot a, final Plot b) {
                        if (a.getTimestamp() > b.getTimestamp()) {
                            return -1;
                        } else if (b.getTimestamp() > a.getTimestamp()) {
                            return 1;
                        }
                        return 0;
                    }
                });
                return result;
            } else if (min != 0) {
                plots = new Plot[(int) range];
                for (Plot plot : list) {
                    int index = (int) (plot.getTimestamp() - min);
                    if (index >= plots.length) {
                        overflow.add(plot);
                        continue;
                    }
                    Plot current = plots[index];
                    while (true) {
                        if (current == null) {
                            plots[index] = plot;
                            break;
                        }
                        if (current.getTimestamp() > plot.getTimestamp()) {
                            plots[index] = plot;
                            plot = current;
                        }
                        index++;
                        if (index >= plots.length) {
                            overflow.add(plot);
                            break;
                        }
                        current = plots[index];
                    }
                }
            } else {
                plots = new Plot[(int) range];
                for (Plot plot : list) {
                    int index = (int) (plot.getTimestamp());
                    if (index >= plots.length) {
                        overflow.add(plot);
                        continue;
                    }
                    Plot current = plots[index];
                    // Move everything along until a free spot is found
                    while (true) {
                        if (current == null) {
                            plots[index] = plot;
                            break;
                        }
                        if (current.getTimestamp() > plot.getTimestamp()) {
                            plots[index] = plot;
                            plot = current;
                        }
                        index++;
                        if (index >= plots.length) {
                            overflow.add(plot);
                            break;
                        }
                        current = plots[index];
                    }
                }
            }
            final ArrayList<Plot> result = new ArrayList<>(size);
            if (!overflow.isEmpty()) {
                Collections.sort(overflow, new Comparator<Plot>() {
                    @Override
                    public int compare(final Plot a, final Plot b) {
                        if (a.getTimestamp() > b.getTimestamp()) {
                            return -1;
                        } else if (b.getTimestamp() > a.getTimestamp()) {
                            return 1;
                        }
                        return 0;
                    }
                });
                for (final Plot plot : overflow) {
                    result.add(plot);
                }
            }
            for (int i = plots.length - 1; i >= 0; i--) {
                if (plots[i] != null) {
                    result.add(plots[i]);
                }
            }
            return result;
        } catch (final Exception e) {
            e.printStackTrace();
            final ArrayList<Plot> result = new ArrayList<>(list);
            Collections.sort(result, new Comparator<Plot>() {
                @Override
                public int compare(final Plot a, final Plot b) {
                    if (a.getTimestamp() > b.getTimestamp()) {
                        return -1;
                    } else if (b.getTimestamp() > a.getTimestamp()) {
                        return 1;
                    }
                    return 0;
                }
            });
            return result;
        }
    }
    
    /**
     * @deprecated Unchecked, use {@link #sortPlots(Collection, SortType, PlotArea)}  instead which will in turn call this
     * @param input
     */
    @Deprecated
    public void sortPlotsByHash(final Plot[] input) {
        final List<Plot>[] bucket = new ArrayList[32];
        for (int i = 0; i < bucket.length; i++) {
            bucket[i] = new ArrayList<>();
        }
        boolean maxLength = false;
        int placement = 1;
        while (!maxLength) {
            maxLength = true;
            for (final Plot i : input) {
                int tmp = MathMan.getPositiveId(i.hashCode()) / placement;
                bucket[tmp & 31].add(i);
                if (maxLength && (tmp > 0)) {
                    maxLength = false;
                }
            }
            int a = 0;
            for (int b = 0; b < 32; b++) {
                for (final Plot i : bucket[b]) {
                    input[a++] = i;
                }
                bucket[b].clear();
            }
            placement *= 32;
        }
    }

    /**
     * Sort a collection of plots by world (with a priority world), then by hashcode
     * @param myplots
     * @param type The sorting method to use for each world (timestamp, or hash)
     * @param priorityArea - Use null, "world" or "gibberish" if you want default world order
     * @return ArrayList of plot
     */
    public ArrayList<Plot> sortPlots(final Collection<Plot> myplots, final SortType type, final PlotArea priorityArea) {
        // group by world
        // sort each
        final HashMap<PlotArea, Collection<Plot>> map = new HashMap<>();
        int totalSize = getPlotCount();
        if (myplots.size() == totalSize) {
            for (PlotArea area : plotareas) {
                map.put(area, area.getPlots());
            }
        } else {
            for (PlotArea area : plotareas) {
                map.put(area, new ArrayList<Plot>(0));
            }
            Collection<Plot> lastList = null;
            PlotArea lastWorld = null;
            for (final Plot plot : myplots) {
                if (lastWorld == plot.getArea()) {
                    lastList.add(plot);
                } else {
                    lastWorld = plot.getArea();
                    lastList = map.get(lastWorld);
                    lastList.add(plot);
                }
            }
        }
        ArrayList<PlotArea> areas = new ArrayList<>(Arrays.asList(plotareas));
        Collections.sort(areas, new Comparator<PlotArea>() {
            @Override
            public int compare(final PlotArea a, final PlotArea b) {
                if ((priorityArea != null) && StringMan.isEqual(a.toString(), b.toString())) {
                    return -1;
                }
                return a.hashCode() - b.hashCode();
            }
        });
        final ArrayList<Plot> toReturn = new ArrayList<>(myplots.size());
        for (final PlotArea area : areas) {
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
                default:
                    break;
            }
        }
        return toReturn;
    }
    
    /**
     * Get all the plots owned by a player name
     * @param world
     * @param player
     * @return Set of Plot
     */
    public Set<Plot> getPlots(final String world, final String player) {
        final UUID uuid = UUIDHandler.getUUID(player, null);
        return getPlots(world, uuid);
    }

    /**
     * Get all the plots owned by a player name
     * @param area
     * @param player
     * @return Set of Plot
     */
    public Set<Plot> getPlots(final PlotArea area, final String player) {
        final UUID uuid = UUIDHandler.getUUID(player, null);
        return getPlots(area, uuid);
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
     * Get all plots by a PlotPlayer
     * @param area
     * @param player
     * @return Set of plot
     */
    public Set<Plot> getPlots(final PlotArea area, final PlotPlayer player) {
        final UUID uuid = player.getUUID();
        return getPlots(area, uuid);
    }
    
    /**
     * Get all plots by a UUID in a world
     * @param world
     * @param uuid
     * @return Set of plot
     */
    public Set<Plot> getPlots(final String world, final UUID uuid) {
        final ArrayList<Plot> myplots = new ArrayList<>();
        for (final Plot plot : getPlots(world)) {
            if (plot.hasOwner()) {
                if (plot.isOwnerAbs(uuid)) {
                    myplots.add(plot);
                }
            }
        }
        return new HashSet<>(myplots);
    }
    
    /**
     * Get all plots by a UUID in an area
     * @param area
     * @param uuid
     * @return Set of plot
     */
    public Set<Plot> getPlots(final PlotArea area, final UUID uuid) {
        final ArrayList<Plot> myplots = new ArrayList<>();
        for (final Plot plot : getPlots(area)) {
            if (plot.hasOwner()) {
                if (plot.isOwnerAbs(uuid)) {
                    myplots.add(plot);
                }
            }
        }
        return new HashSet<>(myplots);
    }
    
    /**
     * Use {@link #hasPlotArea(String)}<br>
     * Note: Worlds may have more than one plot area
     * @deprecated
     * @param world
     * @return
     */
    @Deprecated
    public boolean isPlotWorld(String world) {
        return plotareamap.containsKey(world);
    }

    /**
     * Check if a plot world
     * @param world
     * @see #getPlotAreaByString(String) to get the PlotArea object
     * @return if a plot world is registered
     */
    public boolean hasPlotArea(final String world) {
        switch (plotareas.length) {
            case 0:
                return false;
            case 1:
                PlotArea a = plotareas[0];
                return world.hashCode() == a.worldhash && a.worldname.equals(world);
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
                int hash = world.hashCode();
                for (PlotArea area : plotareas) {
                    if (area.worldhash == hash && area.worldname.equals(world)) {
                        return true;
                    }
                }
                return false;
            default:
                return plotareamap.containsKey(world);
        }
    }
    
    public Collection<Plot> getPlots(final String world) {
        final HashSet<Plot> set = new HashSet<>();
        foreachPlotArea(world, new RunnableVal<PlotArea>() {
            @Override
            public void run(PlotArea value) {
                set.addAll(value.getPlots());
            }
        });
        return set;
    }
    
    public Collection<Plot> getPlots(final PlotArea area) {
        return area == null ? new HashSet<Plot>() : area.getPlots();
    }
    
    public Plot getPlot(PlotArea area, final PlotId id) {
        return area == null ? null : (id == null ? null : area.getPlot(id));
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
        foreachPlot(new RunnableVal<Plot>() {
            @Override
            public void run(Plot value) {
                if (value.isOwnerAbs(uuid)) {
                    myplots.add(value);
                }
            }
        });
        return new HashSet<>(myplots);
    }
    
    /**
     * Get the plots for a UUID
     * @param uuid
     * @return Set of Plot
     */
    public Set<Plot> getPlotsAbs(final UUID uuid) {
        final ArrayList<Plot> myplots = new ArrayList<>();
        foreachPlot(new RunnableVal<Plot>() {
            @Override
            public void run(Plot value) {
                if (value.isOwnerAbs(uuid)) {
                    myplots.add(value);
                }
            }
        });
        return new HashSet<>(myplots);
    }
    
    /**
     * Unregister a plot from local memory (does not call DB)
     * @param plot
     * @param callEvent If to call an event about the plot being removed
     * @return true if plot existed | false if it didn't
     */
    public boolean removePlot(Plot plot, final boolean callEvent) {
        if (plot == null) {
            return false;
        }
        if (callEvent) {
            EventUtil.manager.callDelete(plot);
        }
        if (plot.getArea().removePlot(plot.getId())) {
            PlotId last = (PlotId) plot.getArea().getMeta("lastPlot");
            final int last_max = Math.max(Math.abs(last.x), Math.abs(last.y));
            final int this_max = Math.max(Math.abs(plot.getId().x), Math.abs(plot.getId().y));
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
    public void loadWorld(final String world, final GeneratorWrapper<?> baseGenerator) {
        if (world.equals("CheckingPlotSquaredGenerator")) {
            return;
        }
        final Set<String> worlds = (config.contains("worlds") ? config.getConfigurationSection("worlds").getKeys(false) : new HashSet<String>());
        final String path = "worlds." + world;
        ConfigurationSection worldSection = config.getConfigurationSection(path);
        int type = worldSection != null ? worldSection.getInt("generator.type") : 0;
        if (type == 0) {
            if (plotareamap.containsKey(world)) {
                PS.debug("World possibly already loaded: " + world);
                return;
            }
            IndependentPlotGenerator pg;
            if (baseGenerator != null && baseGenerator.isFull()) {
                pg = baseGenerator.getPlotGenerator();
            }
            else if (worldSection != null) {
                String secondaryGeneratorName = worldSection.getString("generator.plugin");
                GeneratorWrapper<?> secondaryGenerator = IMP.getGenerator(world, secondaryGeneratorName);
                if (secondaryGenerator != null && secondaryGenerator.isFull()) {
                    pg = secondaryGenerator.getPlotGenerator();
                }
                else {
                    String primaryGeneratorName = worldSection.getString("generator.init");
                    GeneratorWrapper<?> primaryGenerator = IMP.getGenerator(world, primaryGeneratorName);
                    if (primaryGenerator != null && primaryGenerator.isFull()) {
                        pg = primaryGenerator.getPlotGenerator();
                    }
                    else {
                        return;
                    }
                }
            }
            else {
                return;
            }
            // Conventional plot generator
            PlotArea plotArea = pg.getNewPlotArea(world, null, null, null);
            PlotManager plotManager = pg.getNewPlotManager();
            log(C.PREFIX.s() + "&aDetected world load for '" + world + "'");
            log(C.PREFIX.s() + "&3 - generator: &7" + baseGenerator + ">" + pg);
            log(C.PREFIX.s() + "&3 - plotworld: &7" + plotArea.getClass().getName());
            log(C.PREFIX.s() + "&3 - manager: &7" + plotManager.getClass().getName());
            if (!config.contains(path)) {
                config.createSection(path);
                worldSection = config.getConfigurationSection(path);
            }
            plotArea.saveConfiguration(worldSection);
            plotArea.loadDefaultConfiguration(worldSection);
            try {
                config.save(configFile);
            } catch (final IOException e) {
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
                if (plotareamap.containsKey(world)) {
                    PS.debug("World possibly already loaded: " + world);
                    return;
                }
                log(C.PREFIX.s() + "&aDetected world load for '" + world + "'");
                String gen_string = worldSection.getString("generator.plugin");
                if (gen_string == null) {
                    gen_string = "PlotSquared";
                }
                if (type == 2) {
                    Set<PlotCluster> clusters = clusters_tmp != null ? clusters_tmp.get(world) : new HashSet<PlotCluster>();
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

                        log(C.PREFIX.s() + "&3 - " + name + "-" + pos1 + "-" + pos2);
                        GeneratorWrapper<?> areaGen = IMP.getGenerator(world, gen_string);
                        if (areaGen == null) {
                            throw new IllegalArgumentException("Invalid Generator: " + gen_string);
                        }
                        PlotArea pa = areaGen.getPlotGenerator().getNewPlotArea(world, name, pos1, pos2);
                        pa.saveConfiguration(worldSection);
                        pa.loadDefaultConfiguration(worldSection);
                        try {
                            config.save(configFile);
                        } catch (final IOException e) {
                            e.printStackTrace();
                        }
                        log(C.PREFIX.s() + "&c | &9generator: &7" + baseGenerator + ">" + areaGen);
                        log(C.PREFIX.s() + "&c | &9plotworld: &7" + pa);
                        log(C.PREFIX.s() + "&c | &9manager: &7" + pa);
                        log(C.PREFIX.s() + "&cNote: &7Area created for cluster:" + name + " (invalid or old configuration?)");
                        areaGen.getPlotGenerator().initialize(pa);
                        areaGen.augment(pa);
                        toLoad.add(pa);
                    }
                    for (PlotArea area : toLoad) {
                        addPlotArea(area);
                    }
                    return;
                }
                GeneratorWrapper<?> areaGen = IMP.getGenerator(world, gen_string);
                if (areaGen == null) {
                    throw new IllegalArgumentException("Invalid Generator: " + gen_string);
                }
                PlotArea pa = areaGen.getPlotGenerator().getNewPlotArea(world, null, null, null);
                pa.saveConfiguration(worldSection);
                pa.loadDefaultConfiguration(worldSection);
                try {
                    config.save(configFile);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                log(C.PREFIX.s() + "&3 - generator: &7" + baseGenerator + ">" + areaGen);
                log(C.PREFIX.s() + "&3 - plotworld: &7" + pa);
                log(C.PREFIX.s() + "&3 - manager: &7" + pa.getPlotManager());
                areaGen.getPlotGenerator().initialize(pa);
                areaGen.augment(pa);
                addPlotArea(pa);
                return;
            }
            if (type == 1) {
                throw new IllegalArgumentException("Invalid type for multi-area world. Expected `2`, got `" + type + "`");
            }
            for (String areaId : areasSection.getKeys(false)) {
                log(C.PREFIX.s() + "&3 - " + areaId);
                String[] split = areaId.split("-");
                if (split.length != 3) {
                    throw new IllegalArgumentException("Invalid Area identifier: " + areaId + ". Expected form `<name>-<pos1>-<pos2>`");
                }
                String name = split[0];
                PlotId pos1 = PlotId.fromString(split[1]);
                PlotId pos2 = PlotId.fromString(split[2]);
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
                String gen_string = clone.getString("generator.plugin");
                if (gen_string == null) {
                    gen_string = "PlotSquared";
                }
                GeneratorWrapper<?> areaGen = IMP.getGenerator(world, gen_string);
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
                    config.save(configFile);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                log(C.PREFIX.s() + "&aDetected area load for '" + world + "'");
                log(C.PREFIX.s() + "&c | &9generator: &7" + baseGenerator + ">" + areaGen);
                log(C.PREFIX.s() + "&c | &9plotworld: &7" + pa);
                log(C.PREFIX.s() + "&c | &9manager: &7" + pa.getPlotManager());
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
    public boolean setupPlotWorld(final String world, final String args, IndependentPlotGenerator generator) {
        if ((args != null) && (!args.isEmpty())) {
            // save configuration
            final String[] split = args.split(",");
            final HybridPlotWorld plotworld = new HybridPlotWorld(world, null, generator, null, null);
            for (final String element : split) {
                final String[] pair = element.split("=");
                if (pair.length != 2) {
                    log("&cNo value provided for: &7" + element);
                    return false;
                }
                final String key = pair[0].toLowerCase();
                final String value = pair[1];
                final String base = "worlds." + world + ".";
                try {
                    switch (key) {
                        case "s":
                        case "size": {
                            config.set(base + "plot.size", Configuration.INTEGER.parseString(value).shortValue());
                            break;
                        }
                        case "g":
                        case "gap": {
                            config.set(base + "road.width", Configuration.INTEGER.parseString(value).shortValue());
                            break;
                        }
                        case "h":
                        case "height": {
                            config.set(base + "road.height", Configuration.INTEGER.parseString(value).shortValue());
                            config.set(base + "plot.height", Configuration.INTEGER.parseString(value).shortValue());
                            config.set(base + "wall.height", Configuration.INTEGER.parseString(value).shortValue());
                            break;
                        }
                        case "f":
                        case "floor": {
                            config.set(base + "plot.floor",
                                    new ArrayList<>(Arrays.asList(StringMan.join(Configuration.BLOCKLIST.parseString(value), ",").split(","))));
                            break;
                        }
                        case "m":
                        case "main": {
                            config.set(base + "plot.filling",
                                    new ArrayList<>(Arrays.asList(StringMan.join(Configuration.BLOCKLIST.parseString(value), ",").split(","))));
                            break;
                        }
                        case "w":
                        case "wall": {
                            config.set(base + "wall.filling", Configuration.BLOCK.parseString(value).toString());
                            break;
                        }
                        case "b":
                        case "border": {
                            config.set(base + "wall.block", Configuration.BLOCK.parseString(value).toString());
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
                ConfigurationSection section = config.getConfigurationSection("worlds." + world);
                plotworld.saveConfiguration(section);
                plotworld.loadConfiguration(section);
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    
    public boolean canUpdate(final String current, final String other) {
        final String s1 = normalisedVersion(current);
        final String s2 = normalisedVersion(other);
        final int cmp = s1.compareTo(s2);
        return cmp < 0;
    }
    
    public String normalisedVersion(final String version) {
        return normalisedVersion(version, ".", 4);
    }
    
    public String normalisedVersion(final String version, final String sep, final int maxWidth) {
        final String[] split = Pattern.compile(sep, Pattern.LITERAL).split(version);
        final StringBuilder sb = new StringBuilder();
        for (final String s : split) {
            sb.append(String.format("%" + maxWidth + 's', s));
        }
        return sb.toString();
    }
    
    /**
     * Gets the default update URL, or null if the plugin is up to date
     * @return
     */
    public URL getUpdate() {
        final String pom = "https://raw.githubusercontent.com/IntellectualSites/PlotSquared/master/pom.xml";
        try {
            final URL page = new URL(pom);
            final URLConnection con = page.openConnection();
            final String agent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
            con.addRequestProperty("User-Agent", agent);
            String line;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("<version>")) {
                        line = line.replaceAll("[^\\d.]", "");
                        break;
                    }
                }
            }
            if (!canUpdate(config.getString("version"), line)) {
                PS.debug("&7PlotSquared is already up to date!");
                return null;
            }
            String dl = "https://raw.githubusercontent.com/IntellectualSites/PlotSquared/master/target/PlotSquared-${PLATFORM}.jar";
            dl = dl.replaceAll(Pattern.quote("${PLATFORM}"), getPlatform());
            log("&6PlotSquared v" + line + " is available:");
            log("&8 - &3Use: &7/plot update");
            log("&8 - &3Or: &7" + dl);
            return new URL(dl);
        } catch (IOException e) {
            e.printStackTrace();
            log("&dCould not check for updates (0)");
            log("&7 - Manually check for updates: " + pom);
        }
        return null;
    }
    
    public boolean update(final PlotPlayer sender, final URL url) {
        if (url == null) {
            return false;
        }
        try {
            final String name = FILE.getName();
            final File newJar = new File("plugins/update/" + name);
            MainUtil.sendMessage(sender, "$1Downloading from provided URL: &7" + url);
            MainUtil.sendMessage(sender, "$2 - User-Agent: " + "Mozilla/4.0");
            final URLConnection con = url.openConnection();
            con.addRequestProperty("User-Agent", "Mozilla/4.0");
            final InputStream stream = con.getInputStream();
            final File parent = newJar.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            MainUtil.sendMessage(sender, "$2 - Output: " + newJar);
            newJar.delete();
            Files.copy(stream, newJar.toPath());
            stream.close();
            MainUtil.sendMessage(sender, "$1The update will take effect when the server is restarted next");
            return true;
        } catch (IOException e) {
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
    public void copyFile(final String file, final String folder) {
        try {
            final File output = IMP.getDirectory();
            if (!output.exists()) {
                output.mkdirs();
            }
            final File newFile = new File((output + File.separator + folder + File.separator + file));
            if (newFile.exists()) {
                return;
            }
            try (InputStream stream = IMP.getClass().getResourceAsStream(file)) {
                final byte[] buffer = new byte[2048];
                if (stream == null) {
                    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(FILE))) {
                        ZipEntry ze = zis.getNextEntry();
                        while (ze != null) {
                            final String name = ze.getName();
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
            log("&cCould not save " + file);
        }
    }
    
    private Map<String, Map<PlotId, Plot>> getPlotsRaw() {
        HashMap<String, Map<PlotId, Plot>> map = new HashMap<>();
        for (PlotArea area : plotareas) {
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
            TASK = null;
            database = null;
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
        } catch (final NullPointerException e) {
            log("&cCould not close database connection!");
        }
    }

    /**
     * Setup the database connection
     */
    public void setupDatabase() {
        try {
            if (Settings.DB.USE_MONGO) {
                log(C.PREFIX.s() + "MongoDB is not yet implemented");
                log(C.PREFIX + "&cNo storage type is set!");
                IMP.disable();
                return;
            }
            if (DBFunc.dbManager == null) {
                if (Settings.DB.USE_MYSQL) {
                    database = new MySQL(Settings.DB.HOST_NAME, Settings.DB.PORT, Settings.DB.DATABASE, Settings.DB.USER, Settings.DB.PASSWORD);
                } else if (Settings.DB.USE_SQLITE) {
                    database = new SQLite(IMP.getDirectory() + File.separator + Settings.DB.SQLITE_DB + ".db");
                } else {
                    log(C.PREFIX + "&cNo storage type is set!");
                    IMP.disable();
                    return;
                }
            }
            DBFunc.dbManager = new SQLManager(database, Settings.DB.PREFIX, false);
            this.plots_tmp = DBFunc.getPlots();
            if (Settings.ENABLE_CLUSTERS) {
                this.clusters_tmp = DBFunc.getClusters();
            }
        } catch (ClassNotFoundException | SQLException e) {
            log(C.PREFIX.s() + "&cFailed to open DATABASE connection. The plugin will disable itself.");
            if (Settings.DB.USE_MONGO) {
                log("$4MONGO");
            } else if (Settings.DB.USE_MYSQL) {
                log("$4MYSQL");
            } else if (Settings.DB.USE_SQLITE) {
                log("$4SQLITE");
            }
            log("&d==== Here is an ugly stacktrace, if you are interested in those things ===");
            e.printStackTrace();
            log("&d==== End of stacktrace ====");
            log("&6Please go to the PlotSquared 'storage.yml' and configure the database correctly.");
            IMP.disable();
        }
    }
    
    /**
     * Setup the default flags for PlotSquared<br>
     *  - Create the flags
     *  - Register with FlagManager and parse raw flag values
     */
    public void setupDefaultFlags() {
        final List<String> booleanFlags = Arrays.asList("notify-enter", "notify-leave", "item-drop", "invincible", "instabreak", "drop-protection", "forcefield", "titles", "pve", "pvp",
        "no-worldedit", "redstone", "keep");
        final List<String> intervalFlags = Arrays.asList("feed", "heal");
        final List<String> stringFlags = Arrays.asList("greeting", "farewell");
        final List<String> intFlags = Arrays.asList("misc-cap", "entity-cap", "mob-cap", "animal-cap", "hostile-cap", "vehicle-cap", "music");
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
        FlagManager.addFlag(new AbstractFlag("gamemode") {

            @Override
            public PlotGamemode parseValueRaw(final String value) {
                switch (value.toLowerCase()) {
                    case "survival":
                    case "s":
                    case "0":
                        return PlotGamemode.SURVIVAL;
                    case "creative":
                    case "c":
                    case "1":
                        return PlotGamemode.CREATIVE;
                    case "adventure":
                    case "a":
                    case "2":
                        return PlotGamemode.ADVENTURE;
                    case "spectator":
                    case "3":
                        return PlotGamemode.SPECTATOR;
                    default:
                        return null;
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
            public PlotWeather parseValueRaw(final String value) {
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
        LAST_VERSION = config.getString("version");
        config.set("version", StringMan.join(VERSION, "."));
        config.set("platform", PLATFORM);

        final Map<String, Object> options = new HashMap<>();
        // Command confirmation
        options.put("confirmation.clear", Settings.CONFIRM_CLEAR);
        options.put("confirmation.delete", Settings.CONFIRM_DELETE);
        options.put("confirmation.unlink", Settings.CONFIRM_UNLINK);

        // Protection
        options.put("protection.redstone.disable-offline", Settings.REDSTONE_DISABLER);
        options.put("protection.redstone.disable-unoccupied", Settings.REDSTONE_DISABLER_UNOCCUPIED);

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

        final int keep = config.getInt("clear.keep-if-modified");
        final int ignore = config.getInt("clear.ignore-if-modified");
        if ((keep > 0) || (ignore > 0)) {
            options.put("clear.auto.threshold", 1);
            options.put("clear.auto.enabled", false);
            log("&cIMPORTANT MESSAGE ABOUT THIS UPDATE!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            log("&cSorry for all the exclamation marks, but this could be important.");
            log("&cPlot clearing has changed to a new system that requires calibration.");
            log("&cThis is how it will work: ");
            log("&c - Players will rate plots");
            log("&c - When enough plots are rated, you can run /plot debugexec calibrate-analysis");
            log("&c - You can decide the (rough) percentage of expired plots to clear");
            log("&c - To just clear all expired plot, ignore this and set: &7threshold: -1");
            log("&cMore information:&7 https://github.com/IntellectualSites/PlotSquared/wiki/Plot-analysis:");
        } else {
            options.put("clear.auto.threshold", Settings.CLEAR_THRESHOLD);
        }
        config.set("clear.keep-if-modified", null);
        config.set("clear.ignore-if-modified", null);

        // Done
        options.put("approval.ratings.require-done", Settings.REQUIRE_DONE);
        options.put("approval.done.counts-towards-limit", Settings.DONE_COUNTS_TOWARDS_LIMIT);
        options.put("approval.done.restrict-building", Settings.DONE_RESTRICTS_BUILDING);
        options.put("approval.done.required-for-download", Settings.DOWNLOAD_REQUIRES_DONE);

        // Schematics
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
        Settings.REDSTONE_DISABLER_UNOCCUPIED = config.getBoolean("protection.redstone.disable-unoccupied");

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
        Settings.KILL_ROAD_VEHICLES = config.getBoolean("kill_road_vehicles");

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
        Settings.AUTO_CLEAR_CONFIRMATION = config.getBoolean("clear.auto.confirmation"); // TODO FIXME

        // Done
        Settings.REQUIRE_DONE = config.getBoolean("approval.ratings.require-done");
        Settings.DONE_COUNTS_TOWARDS_LIMIT = config.getBoolean("approval.done.counts-towards-limit");
        Settings.DONE_RESTRICTS_BUILDING = config.getBoolean("approval.done.restrict-building");
        Settings.DOWNLOAD_REQUIRES_DONE = config.getBoolean("approval.done.required-for-download");

        // Schematics
        Settings.SCHEMATIC_SAVE_PATH = config.getString("schematics.save_path");
        Settings.BO3_SAVE_PATH = config.getString("bo3.save_path");

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
        Settings.QUEUE_COMMANDS = config.getBoolean("worldedit.queue-commands");
        Settings.REQUIRE_SELECTION = config.getBoolean("worldedit.require-selection-in-mask");
        Settings.WE_ALLOW_HELPER = config.getBoolean("worldedit.enable-for-helpers");
        Settings.WE_MAX_VOLUME = config.getLong("worldedit.max-volume");
        Settings.WE_MAX_ITERATIONS = config.getLong("worldedit.max-iterations");
        Settings.WE_BLACKLIST = config.getStringList("worldedit.blacklist");

        // Chunk processor
        Settings.CHUNK_PROCESSOR = config.getBoolean("chunk-processor.enabled");
        Settings.CHUNK_PROCESSOR_GC = config.getBoolean("chunk-processor.auto-unload");
        Settings.CHUNK_PROCESSOR_TRIM_ON_SAVE = config.getBoolean("chunk-processor.auto-trim");
        Settings.EXPERIMENTAL_FAST_ASYNC_WORLDEDIT = config.getBoolean("chunk-processor.experimental-fast-async-worldedit");
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
        Settings.UPDATE_NOTIFICATIONS = config.getBoolean("update-notifications");
        Settings.MERGE_REMOVES_ROADS = config.getBoolean("merge.remove-terrain");
        Settings.AUTO_PURGE = config.getBoolean("auto-purge", false);
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
        } catch (IOException err) {
            err.printStackTrace();
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
        } catch (IOException err_trans) {
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
        } catch (IOException err_trans) {
            log("Failed to save storage.yml");
        }
        try {
            style.save(styleFile);
            config.save(configFile);
            storage.save(storageFile);
        } catch (final IOException e) {
            log("Configuration file saving failed");
            e.printStackTrace();
        }
    }
    
    /**
     * Setup the storage file (load + save missing nodes)
     */
    private void setupStorage() {
        storage.set("version", StringMan.join(VERSION, "."));
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
        style.set("version", StringMan.join(VERSION, "."));
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
    
    public void foreachPlotArea(RunnableVal<PlotArea> runnable) {
        for (PlotArea area : plotareas) {
            runnable.run(area);
        }
    }
    
    public void foreachPlot(final RunnableVal<Plot> runnable) {
        for (PlotArea area : plotareas) {
            for (Plot plot : area.getPlots()) {
                runnable.run(plot);
            }
        }
    }
    
    public void foreachPlotRaw(final RunnableVal<Plot> runnable) {
        for (PlotArea area : plotareas) {
            for (Plot plot : area.getPlots()) {
                runnable.run(plot);
            }
        }
        if (plots_tmp != null) {
            for (Entry<String, HashMap<PlotId, Plot>> entry : plots_tmp.entrySet()) {
                for (Entry<PlotId, Plot> entry2 : entry.getValue().entrySet()) {
                    runnable.run(entry2.getValue());
                }
            }
        }
    }

    public void foreachBasePlot(RunnableVal<Plot> run) {
        for (PlotArea area : plotareas) {
            area.foreachBasePlot(run);
        }
    }

    public void foreachPlotArea(String world, RunnableVal<PlotArea> runnable) {
        PlotArea[] array = plotareamap.get(world);
        if (array == null) {
            return;
        }
        for (PlotArea area : array) {
            runnable.run(area);
        }
    }

    public PlotArea getFirstPlotArea() {
        return plotareas.length > 0 ? plotareas[0] : null;
    }

    public int getPlotAreaCount() {
        return plotareas.length;
    }

    public int getPlotCount() {
        int count = 0;
        for (PlotArea area : plotareas) {
            count += area.getPlotCount();
        }
        return count;
    }

    public int getPlotAreaCount(String world) {
        return plotareamap.size();
    }

    public Set<PlotArea> getPlotAreas() {
        HashSet<PlotArea> set = new HashSet<>(plotareas.length);
        Collections.addAll(set, plotareas);
        return set;
    }
    
    /**
     * @deprecated Since worlds can have multiple plot areas
     * @return Set of world names
     */
    @Deprecated
    public Set<String> getPlotWorldStrings() {
        HashSet<String> set = new HashSet<>(plotareamap.size());
        for (Entry<String, PlotArea[]> entry : plotareamap.entrySet()) {
            set.add(entry.getKey());
        }
        return set;
    }
    
    public boolean isAugmented(String world) {
        PlotArea[] areas = plotareamap.get(world);
        if (areas == null) {
            return false;
        }
        if (areas.length > 1) {
            return true;
        }
        return areas[0].TYPE != 0;
    }

    /**
     * Get a list of PlotArea objects
     * @return Collection of PlotArea objects
     */
    public Set<PlotArea> getPlotAreas(String world) {
        PlotArea[] areas = plotareamap.get(world);
        if (areas == null) {
            return new HashSet<>(0);
        }
        HashSet<PlotArea> set = new HashSet<>(areas.length);
        Collections.addAll(set, areas);
        return set;
    }

    public enum SortType {
        CREATION_DATE, CREATION_DATE_TIMESTAMP, DISTANCE_FROM_ORIGIN
    }
}
