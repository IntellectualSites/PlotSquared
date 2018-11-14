package com.github.intellectualsites.plotsquared.plot;

import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.configuration.MemorySection;
import com.github.intellectualsites.plotsquared.configuration.file.YamlConfiguration;
import com.github.intellectualsites.plotsquared.plot.commands.WE_Anywhere;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.config.Configuration;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.config.Storage;
import com.github.intellectualsites.plotsquared.plot.database.*;
import com.github.intellectualsites.plotsquared.plot.generator.GeneratorWrapper;
import com.github.intellectualsites.plotsquared.plot.generator.HybridPlotWorld;
import com.github.intellectualsites.plotsquared.plot.generator.HybridUtils;
import com.github.intellectualsites.plotsquared.plot.generator.IndependentPlotGenerator;
import com.github.intellectualsites.plotsquared.plot.listener.WESubscriber;
import com.github.intellectualsites.plotsquared.plot.logger.ILogger;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.object.worlds.DefaultPlotAreaManager;
import com.github.intellectualsites.plotsquared.plot.object.worlds.PlotAreaManager;
import com.github.intellectualsites.plotsquared.plot.object.worlds.SinglePlotArea;
import com.github.intellectualsites.plotsquared.plot.object.worlds.SinglePlotAreaManager;
import com.github.intellectualsites.plotsquared.plot.util.*;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.expiry.ExpireManager;
import com.github.intellectualsites.plotsquared.plot.util.expiry.ExpiryTask;
import com.sk89q.worldedit.WorldEdit;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.annotation.Nullable;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * An implementation of the core, with a static getter for easy access.
 */
@SuppressWarnings({"unused", "WeakerAccess"}) public class PlotSquared {
    private static final Set<Plot> EMPTY_SET =
        Collections.unmodifiableSet(Collections.<Plot>emptySet());
    private static PlotSquared instance;
    // Implementation
    public final IPlotMain IMP;
    // Current thread
    private final Thread thread;
    // WorldEdit instance
    public WorldEdit worldedit;
    public File styleFile;
    public File configFile;
    public File worldsFile;
    public File commandsFile;
    public File translationFile;
    public YamlConfiguration style;
    public YamlConfiguration config;
    public YamlConfiguration worlds;
    public YamlConfiguration storage;
    public YamlConfiguration commands;
    // Temporary hold the plots/clusters before the worlds load
    public HashMap<String, Set<PlotCluster>> clusters_tmp;
    public HashMap<String, HashMap<PlotId, Plot>> plots_tmp;
    // Implementation logger
    @Setter @Getter private ILogger logger;
    // Platform / Version / Update URL
    @Getter private Updater updater;
    private PlotVersion version;
    // Files and configuration
    @Getter private File jarFile = null; // This file
    private File storageFile;
    @Getter private PlotAreaManager plotAreaManager;

    /**
     * Initialize PlotSquared with the desired Implementation class.
     *
     * @param iPlotMain Implementation of {@link IPlotMain} used
     * @param platform  The platform being used
     */
    public PlotSquared(final IPlotMain iPlotMain, final String platform) {
        if (instance != null) {
            throw new IllegalStateException("Cannot re-initialize the PlotSquared singleton");
        }
        instance = this;

        this.thread = Thread.currentThread();
        this.IMP = iPlotMain;
        this.logger = iPlotMain;
        Settings.PLATFORM = platform;

        try {
            new ReflectionUtils(this.IMP.getNMSPackage());
            try {
                URL url = PlotSquared.class.getProtectionDomain().getCodeSource().getLocation();
                this.jarFile = new File(
                    new URL(url.toURI().toString().split("\\!")[0].replaceAll("jar:file", "file"))
                        .toURI().getPath());
            } catch (MalformedURLException | URISyntaxException | SecurityException e) {
                e.printStackTrace();
                this.jarFile = new File(this.IMP.getDirectory().getParentFile(), "PlotSquared.jar");
                if (!this.jarFile.exists()) {
                    this.jarFile = new File(this.IMP.getDirectory().getParentFile(),
                        "PlotSquared-" + platform + ".jar");
                }
            }
            if (getJavaVersion() < 1.8) {
                PlotSquared.log(C.CONSOLE_JAVA_OUTDATED.f(IMP.getPluginName()));
            }
            TaskManager.IMP = this.IMP.getTaskManager();
            setupConfigs();
            this.translationFile = MainUtil.getFile(this.IMP.getDirectory(),
                Settings.Paths.TRANSLATIONS + File.separator + IMP.getPluginName()
                    + ".use_THIS.yml");
            C.load(this.translationFile);

            // Setup plotAreaManager
            if (Settings.Enabled_Components.WORLDS) {
                this.plotAreaManager = new SinglePlotAreaManager();
            } else {
                this.plotAreaManager = new DefaultPlotAreaManager();
            }

            // Database
            if (Settings.Enabled_Components.DATABASE) {
                setupDatabase();
            }
            // Comments
            CommentManager.registerDefaultInboxes();
            // Kill entities
            if (Settings.Enabled_Components.KILL_ROAD_MOBS
                || Settings.Enabled_Components.KILL_ROAD_VEHICLES) {
                this.IMP.runEntityTask();
            }
            if (Settings.Enabled_Components.EVENTS) {
                this.IMP.registerPlayerEvents();
                this.IMP.registerInventoryEvents();
                this.IMP.registerPlotPlusEvents();
            }
            // Required
            this.IMP.registerWorldEvents();
            if (Settings.Enabled_Components.METRICS) {
                this.IMP.startMetrics();
            } else {
                PlotSquared.log(C.CONSOLE_PLEASE_ENABLE_METRICS.f(IMP.getPluginName()));
            }
            if (Settings.Enabled_Components.CHUNK_PROCESSOR) {
                this.IMP.registerChunkProcessor();
            }
            // create UUIDWrapper
            UUIDHandler.implementation = this.IMP.initUUIDHandler();
            if (Settings.Enabled_Components.UUID_CACHE) {
                startUuidCatching();
            } else {
                // Start these separately
                UUIDHandler.add(new StringWrapper("*"), DBFunc.everyone);
                startExpiryTasks();
                startPlotMeConversion();
            }
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
            GlobalBlockQueue.IMP = new GlobalBlockQueue(IMP.initBlockQueue(), 1);
            GlobalBlockQueue.IMP.runTask();
            // Set chunk
            ChunkManager.manager = this.IMP.initChunkManager();
            // Schematic handler
            SchematicHandler.manager = this.IMP.initSchematicHandler();
            // Titles
            AbstractTitle.TITLE_CLASS = this.IMP.initTitleManager();
            // Chat
            ChatManager.manager = this.IMP.initChatManager();
            // Commands
            if (Settings.Enabled_Components.COMMANDS) {
                this.IMP.registerCommands();
            }
            // WorldEdit
            if (Settings.Enabled_Components.WORLDEDIT_RESTRICTIONS) {
                try {
                    if (this.IMP.initWorldEdit()) {
                        PlotSquared.debug(IMP.getPluginName() + " hooked into WorldEdit.");
                        this.worldedit = WorldEdit.getInstance();
                        WorldEdit.getInstance().getEventBus().register(new WESubscriber());
                        if (Settings.Enabled_Components.COMMANDS) {
                            new WE_Anywhere();
                        }

                    }
                } catch (Throwable e) {
                    PlotSquared.debug(
                        "Incompatible version of WorldEdit, please upgrade: http://builds.enginehub.org/job/worldedit?branch=master");
                }
            }
            // Economy
            if (Settings.Enabled_Components.ECONOMY) {
                TaskManager.runTask(new Runnable() {
                    @Override public void run() {
                        EconHandler.manager = PlotSquared.this.IMP.getEconomyHandler();
                    }
                });
            }

            // Check for updates
            if (Settings.Enabled_Components.UPDATER) {
                updater = new Updater();
                TaskManager.IMP.taskAsync(new Runnable() {
                    @Override public void run() {
                        updater.update(getPlatform(), getVersion());
                    }
                });
                TaskManager.IMP.taskRepeatAsync(new Runnable() {
                    @Override public void run() {
                        updater.update(getPlatform(), getVersion());
                    }
                }, 36000);
            }

            // World generators:
            final ConfigurationSection section = this.worlds.getConfigurationSection("worlds");
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
                    @Override public void run() {
                        for (String world : section.getKeys(false)) {
                            if (world.equals("CheckingPlotSquaredGenerator")) {
                                continue;
                            }
                            if (!WorldUtil.IMP.isWorld(world) && !world.equals("*")) {
                                debug("&c`" + world + "` was not properly loaded - " + IMP
                                    .getPluginName() + " will now try to load it properly: ");
                                debug(
                                    "&8 - &7Are you trying to delete this world? Remember to remove it from the settings.yml, bukkit.yml and multiverse worlds.yml");
                                debug(
                                    "&8 - &7Your world management plugin may be faulty (or non existent)");
                                PlotSquared.this.IMP.setGenerator(world);
                            }
                        }
                    }
                }, 1);
            }

            // Copy files
            copyFile("automerge.js", Settings.Paths.SCRIPTS);
            copyFile("town.template", Settings.Paths.TEMPLATES);
            copyFile("skyblock.template", Settings.Paths.TEMPLATES);
            copyFile("bridge.template", Settings.Paths.TEMPLATES);
            copyFile("de-DE.yml", Settings.Paths.TRANSLATIONS);
            copyFile("es-ES.yml", Settings.Paths.TRANSLATIONS);
            copyFile("zh-CN.yml", Settings.Paths.TRANSLATIONS);
            copyFile("it-IT.yml", Settings.Paths.TRANSLATIONS);
            copyFile("ko-KR.yml", Settings.Paths.TRANSLATIONS);
            showDebug();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        PlotSquared.log(C.ENABLED.f(IMP.getPluginName()));
    }

    /**
     * Get an instance of PlotSquared.
     *
     * @return instance of PlotSquared
     */
    public static PlotSquared get() {
        return PlotSquared.instance;
    }

    public static IPlotMain imp() {
        if (instance != null) {
            return instance.IMP;
        }
        return null;
    }

    /**
     * Log a message to the IPlotMain logger.
     *
     * @param message Message to log
     * @see IPlotMain#log(String)
     */
    public static void log(Object message) {
        if (message == null || message.toString().isEmpty()) {
            return;
        }
        PlotSquared.get().getLogger().log(StringMan.getString(message));
    }

    /**
     * Log a message to the IPlotMain logger.
     *
     * @param message Message to log
     * @see IPlotMain#log(String)
     */
    public static void debug(@Nullable Object message) {
        if (Settings.DEBUG) {
            PlotSquared.log(message);
        }
    }

    private void startUuidCatching() {
        TaskManager.runTaskLater(new Runnable() {
            @Override public void run() {
                debug("Starting UUID caching");
                UUIDHandler.startCaching(new Runnable() {
                    @Override public void run() {
                        UUIDHandler.add(new StringWrapper("*"), DBFunc.everyone);
                        foreachPlotRaw(new RunnableVal<Plot>() {
                            @Override public void run(Plot plot) {
                                if (plot.hasOwner() && plot.temp != -1) {
                                    if (UUIDHandler.getName(plot.owner) == null) {
                                        UUIDHandler.implementation.unknown.add(plot.owner);
                                    }
                                }
                            }
                        });
                        startExpiryTasks();
                        startPlotMeConversion();
                    }
                });
            }
        }, 20);
    }

    private void startExpiryTasks() {
        if (Settings.Enabled_Components.PLOT_EXPIRY) {
            ExpireManager.IMP = new ExpireManager();
            ExpireManager.IMP.runAutomatedTask();
            for (Settings.Auto_Clear settings : Settings.AUTO_CLEAR.getInstances()) {
                ExpiryTask task = new ExpiryTask(settings);
                ExpireManager.IMP.addTask(task);
            }
        }
    }

    private void startPlotMeConversion() {
        if (Settings.Enabled_Components.PLOTME_CONVERTER || Settings.PlotMe.CACHE_UUDS) {
            TaskManager.IMP.taskAsync(new Runnable() {
                @Override public void run() {
                    if (PlotSquared.this.IMP.initPlotMeConverter()) {
                        PlotSquared.log("&c=== IMPORTANT ===");
                        PlotSquared.log(
                            "&cTHIS MESSAGE MAY BE EXTREMELY HELPFUL IF YOU HAVE TROUBLE CONVERTING PlotMe!");
                        PlotSquared
                            .log("&c - Make sure 'UUID.read-from-disk' is disabled (false)!");
                        PlotSquared.log(
                            "&c - Sometimes the database can be locked, deleting PlotMe.jar beforehand will fix the issue!");
                        PlotSquared.log(
                            "&c - After the conversion is finished, please set 'plotme-converter' to false in the "
                                + "'settings.yml'");
                    }
                    Settings.Enabled_Components.PLOTME_CONVERTER = false;
                }
            });
        }
    }

    public boolean isMainThread(Thread thread) {
        return this.thread == thread;
    }

    /**
     * Check if `version` is >= `version2`.
     *
     * @param version  First version
     * @param version2 Second version
     * @return true if `version` is >= `version2`
     */
    public boolean checkVersion(int[] version, int... version2) {
        return version[0] > version2[0] || version[0] == version2[0] && version[1] > version2[1]
            || version[0] == version2[0] && version[1] == version2[1] && version[2] >= version2[2];
    }

    /**
     * Get the current PlotSquared version.
     *
     * @return current version in config or null
     */
    public PlotVersion getVersion() {
        return this.version;
    }

    /**
     * Get the server platform this plugin is running on this is running on.
     * <p>
     * <p>This will be either <b>Bukkit</b> or <b>Sponge</b></p>
     *
     * @return the server implementation
     */
    public String getPlatform() {
        return Settings.PLATFORM;
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
     * @param plotArea the {@code PlotArea} to add.
     * @see #removePlotArea(PlotArea) To remove the reference
     */
    public void addPlotArea(PlotArea plotArea) {
        HashMap<PlotId, Plot> plots;
        if (plots_tmp == null || (plots = plots_tmp.remove(plotArea.toString())) == null) {
            if (plotArea.TYPE == 2) {
                plots = this.plots_tmp != null ? this.plots_tmp.get(plotArea.worldname) : null;
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
            for (Plot entry : plots.values()) {
                entry.setArea(plotArea);
            }
        }
        Set<PlotCluster> clusters;
        if (clusters_tmp == null || (clusters = clusters_tmp.remove(plotArea.toString())) == null) {
            if (plotArea.TYPE == 2) {
                clusters =
                    this.clusters_tmp != null ? this.clusters_tmp.get(plotArea.worldname) : null;
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
        plotAreaManager.addPlotArea(plotArea);
        plotArea.setupBorder();
    }

    /**
     * Remove a plot world reference.
     *
     * @param area the {@code PlotArea} to remove
     */
    public void removePlotArea(PlotArea area) {
        plotAreaManager.removePlotArea(area);
        setPlotsTmp(area);
    }

    public void removePlotAreas(String world) {
        for (PlotArea area : getPlotAreas(world)) {
            if (area.worldname.equals(world)) {
                removePlotArea(area);
            }
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
        Set<PlotCluster> set = new HashSet<>();
        for (PlotArea area : getPlotAreas(world)) {
            set.addAll(area.getClusters());
        }
        return Collections.unmodifiableSet(set);

    }

    /**
     * Get all the base plots in a single set (for merged plots it just returns
     * the bottom plot).
     *
     * @return Set of base Plots
     */
    public Set<Plot> getBasePlots() {
        int size = getPlotCount();
        final Set<Plot> result = new HashSet<>(size);
        foreachPlotArea(new RunnableVal<PlotArea>() {
            @Override public void run(PlotArea value) {
                for (Plot plot : value.getPlots()) {
                    if (!plot.isBasePlot()) {
                        continue;
                    }
                    result.add(plot);
                }
            }
        });
        return Collections.unmodifiableSet(result);
    }

    public List<Plot> sortPlotsByTemp(Collection<Plot> plots) {
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
            @Override public int compare(Plot a, Plot b) {
                return a.hashCode() - b.hashCode();
            }
        });
        result.addAll(overflow);
        return result;
    }

    /**
     * Sort plots by hashcode.
     *
     * @param plots the collection of plots to sort
     * @return the sorted collection
     * @deprecated Unchecked, please use
     * {@link #sortPlots(Collection, SortType, PlotArea)} which has
     * additional checks before calling this
     */
    // TODO: Re-evaluate deprecation of this, as it's being used internally
    @Deprecated public ArrayList<Plot> sortPlotsByHash(Collection<Plot> plots) {
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

    /**
     * Unchecked, use {@link #sortPlots(Collection, SortType, PlotArea)} instead which will in turn call this.
     *
     * @param input an array of plots to sort
     */
    // TODO: Re-evaluate deprecation of this, as it's being used internally
    @Deprecated public void sortPlotsByHash(Plot[] input) {
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

    // TODO: Re-evaluate deprecation of this, as it's being used internally
    @Deprecated public ArrayList<Plot> sortPlotsByTimestamp(Collection<Plot> plots) {
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
     *
     * @param input
     * @return
     * @deprecated Unchecked, use {@link #sortPlots(Collection, SortType, PlotArea)} instead which will call this after checks
     */
    // TODO: Re-evaluate deprecation of this, as it's being used internally
    @Deprecated public List<Plot> sortPlotsByModified(Collection<Plot> input) {
        List<Plot> list;
        if (input instanceof List) {
            list = (List<Plot>) input;
        } else {
            list = new ArrayList<>(input);
        }
        Collections.sort(list, new Comparator<Plot>() {
            @Override public int compare(Plot a, Plot b) {
                return Long.compare(ExpireManager.IMP.getTimestamp(a.owner),
                    ExpireManager.IMP.getTimestamp(b.owner));
            }
        });
        return list;
    }

    /**
     * Sort a collection of plots by world (with a priority world), then
     * by hashcode.
     *
     * @param plots        the plots to sort
     * @param type         The sorting method to use for each world (timestamp, or hash)
     * @param priorityArea Use null, "world", or "gibberish" if you
     *                     want default world order
     * @return ArrayList of plot
     */
    public ArrayList<Plot> sortPlots(Collection<Plot> plots, SortType type,
        final PlotArea priorityArea) {
        // group by world
        // sort each
        HashMap<PlotArea, Collection<Plot>> map = new HashMap<>();
        int totalSize = getPlotCount();
        if (plots.size() == totalSize) {
            for (PlotArea area : plotAreaManager.getAllPlotAreas()) {
                map.put(area, area.getPlots());
            }
        } else {
            for (PlotArea area : plotAreaManager.getAllPlotAreas()) {
                map.put(area, new ArrayList<Plot>(0));
            }
            Collection<Plot> lastList = null;
            PlotArea lastWorld = null;
            for (Plot plot : plots) {
                if (lastWorld == plot.getArea()) {
                    lastList.add(plot);
                } else {
                    lastWorld = plot.getArea();
                    lastList = map.get(lastWorld);
                    lastList.add(plot);
                }
            }
        }
        List<PlotArea> areas = Arrays.asList(plotAreaManager.getAllPlotAreas());
        Collections.sort(areas, new Comparator<PlotArea>() {
            @Override public int compare(PlotArea a, PlotArea b) {
                if (priorityArea != null) {
                    if (a.equals(priorityArea)) {
                        return -1;
                    } else if (b.equals(priorityArea)) {
                        return 1;
                    }
                }
                return a.hashCode() - b.hashCode();
            }
        });
        ArrayList<Plot> toReturn = new ArrayList<>(plots.size());
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
                    break;
                default:
                    break;
            }
        }
        return toReturn;
    }

    /**
     * A more generic way to filter plots - make your own method
     * if you need complex filters.
     *
     * @param filters the filter
     * @return a filtered set of plots
     */
    public Set<Plot> getPlots(final PlotFilter... filters) {
        final HashSet<Plot> set = new HashSet<>();
        foreachPlotArea(new RunnableVal<PlotArea>() {
            @Override public void run(PlotArea value) {
                for (PlotFilter filter : filters) {
                    if (!filter.allowsArea(value)) {
                        return;
                    }
                }
                loop:
                for (Entry<PlotId, Plot> entry2 : value.getPlotEntries()) {
                    Plot plot = entry2.getValue();
                    for (PlotFilter filter : filters) {
                        if (!filter.allowsPlot(plot)) {
                            continue loop;
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
     *
     * @return Set of Plots
     */
    public Set<Plot> getPlots() {
        int size = getPlotCount();
        final Set<Plot> result = new HashSet<>(size);
        foreachPlotArea(new RunnableVal<PlotArea>() {
            @Override public void run(PlotArea value) {
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
                for (Plot plot : entry.getValue().values()) {
                    plot.setArea(area);
                    area.addPlot(plot);
                }
            }
        }
    }

    /**
     * Get all the plots owned by a player name.
     *
     * @param world  the world
     * @param player the plot owner
     * @return Set of Plot
     */
    public Set<Plot> getPlots(String world, String player) {
        final UUID uuid = UUIDHandler.getUUID(player, null);
        return getPlots(world, uuid);
    }

    /**
     * Get all the plots owned by a player name.
     *
     * @param area   the PlotArea
     * @param player the plot owner
     * @return Set of Plot
     */
    public Set<Plot> getPlots(PlotArea area, String player) {
        UUID uuid = UUIDHandler.getUUID(player, null);
        return getPlots(area, uuid);
    }

    /**
     * Get all plots by a PlotPlayer.
     *
     * @param world  the world
     * @param player the plot owner
     * @return Set of plot
     */
    public Set<Plot> getPlots(String world, PlotPlayer player) {
        return getPlots(world, player.getUUID());
    }

    /**
     * Get all plots by a PlotPlayer.
     *
     * @param area   the PlotArea
     * @param player the plot owner
     * @return Set of plot
     */
    public Set<Plot> getPlots(PlotArea area, PlotPlayer player) {
        return getPlots(area, player.getUUID());
    }

    /**
     * Get all plots by a UUID in a world.
     *
     * @param world the world
     * @param uuid  the plot owner
     * @return Set of plot
     */
    public Set<Plot> getPlots(String world, UUID uuid) {
        final Set<Plot> plots = new HashSet<>();
        for (final Plot plot : getPlots(world)) {
            if (plot.hasOwner() && plot.isOwnerAbs(uuid)) {
                plots.add(plot);
            }
        }
        return Collections.unmodifiableSet(plots);
    }

    /**
     * Get all plots by a UUID in an area.
     *
     * @param area the {@code PlotArea}
     * @param uuid the plot owner
     * @return Set of plots
     */
    public Set<Plot> getPlots(PlotArea area, UUID uuid) {
        final Set<Plot> plots = new HashSet<>();
        for (Plot plot : getPlots(area)) {
            if (plot.hasOwner() && plot.isOwnerAbs(uuid)) {
                plots.add(plot);
            }
        }
        return Collections.unmodifiableSet(plots);
    }

    /**
     * Check if a plot world.
     *
     * @param world the world
     * @return if a plot world is registered
     * @see #getPlotAreaByString(String) to get the PlotArea object
     */
    public boolean hasPlotArea(String world) {
        return plotAreaManager.getPlotAreas(world, null).length != 0;
    }

    public Collection<Plot> getPlots(String world) {
        final Set<Plot> set = new HashSet<>();
        foreachPlotArea(world, new RunnableVal<PlotArea>() {
            @Override public void run(PlotArea value) {
                set.addAll(value.getPlots());
            }
        });
        return set;
    }

    /**
     * Get the plots for a PlotPlayer.
     *
     * @param player the player to retrieve the plots for
     * @return Set of Plot
     */
    public Set<Plot> getPlots(PlotPlayer player) {
        return getPlots(player.getUUID());
    }

    public Collection<Plot> getPlots(PlotArea area) {
        return area == null ? EMPTY_SET : area.getPlots();
    }

    public Plot getPlot(PlotArea area, PlotId id) {
        return area == null ? null : id == null ? null : area.getPlot(id);
    }

    public Set<Plot> getBasePlots(PlotPlayer player) {
        return getBasePlots(player.getUUID());
    }

    /**
     * Get the plots for a UUID.
     *
     * @param uuid the plot owner
     * @return Set of Plot's owned by the player
     */
    public Set<Plot> getPlots(final UUID uuid) {
        final Set<Plot> plots = new HashSet<>();
        foreachPlot(new RunnableVal<Plot>() {
            @Override public void run(Plot value) {
                if (value.isOwnerAbs(uuid)) {
                    plots.add(value);
                }
            }
        });
        return Collections.unmodifiableSet(plots);
    }

    public boolean hasPlot(final UUID uuid) {
        for (final PlotArea area : plotAreaManager.getAllPlotAreas()) {
            if (area.hasPlot(uuid))
                return true;
        }
        return false;
    }

    public Set<Plot> getBasePlots(final UUID uuid) {
        final Set<Plot> plots = new HashSet<>();
        foreachBasePlot(new RunnableVal<Plot>() {
            @Override public void run(Plot value) {
                if (value.isOwner(uuid)) {
                    plots.add(value);
                }
            }
        });
        return Collections.unmodifiableSet(plots);
    }

    /**
     * Get the plots for a UUID.
     *
     * @param uuid the UUID of the owner
     * @return Set of Plot
     */
    public Set<Plot> getPlotsAbs(final UUID uuid) {
        final Set<Plot> plots = new HashSet<>();
        foreachPlot(new RunnableVal<Plot>() {
            @Override public void run(Plot value) {
                if (value.isOwnerAbs(uuid)) {
                    plots.add(value);
                }
            }
        });
        return Collections.unmodifiableSet(plots);
    }

    /**
     * Unregister a plot from local memory (does not call DB).
     *
     * @param plot      the plot to remove
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
     * This method is called by the PlotGenerator class normally.
     * <ul>
     * <li>Initializes the PlotArea and PlotManager classes
     * <li>Registers the PlotArea and PlotManager classes
     * <li>Loads (and/or generates) the PlotArea configuration
     * <li>Sets up the world border if configured
     * </ul>
     *
     * <p>If loading an augmented plot world:
     * <ul>
     * <li>Creates the AugmentedPopulator classes
     * <li>Injects the AugmentedPopulator classes if required
     * </ul>
     *
     * @param world         the world to load
     * @param baseGenerator The generator for that world, or null
     */
    public void loadWorld(String world, GeneratorWrapper<?> baseGenerator) {
        if (world.equals("CheckingPlotSquaredGenerator")) {
            return;
        }
        this.plotAreaManager.addWorld(world);
        Set<String> worlds;
        if (this.worlds.contains("worlds")) {
            worlds = this.worlds.getConfigurationSection("worlds").getKeys(false);
        } else {
            worlds = new HashSet<>();
        }
        String path = "worlds." + world;
        ConfigurationSection worldSection = this.worlds.getConfigurationSection(path);
        int type;
        if (worldSection != null) {
            type = worldSection.getInt("generator.type", 0);
        } else {
            type = 0;
        }
        if (type == 0) {
            if (plotAreaManager.getPlotAreas(world, null).length != 0) {
                debug("World possibly already loaded: " + world);
                return;
            }
            IndependentPlotGenerator plotGenerator;
            if (baseGenerator != null && baseGenerator.isFull()) {
                plotGenerator = baseGenerator.getPlotGenerator();
            } else if (worldSection != null) {
                String secondaryGeneratorName = worldSection.getString("generator.plugin");
                GeneratorWrapper<?> secondaryGenerator =
                    this.IMP.getGenerator(world, secondaryGeneratorName);
                if (secondaryGenerator != null && secondaryGenerator.isFull()) {
                    plotGenerator = secondaryGenerator.getPlotGenerator();
                } else {
                    String primaryGeneratorName = worldSection.getString("generator.init");
                    GeneratorWrapper<?> primaryGenerator =
                        this.IMP.getGenerator(world, primaryGeneratorName);
                    if (primaryGenerator != null && primaryGenerator.isFull()) {
                        plotGenerator = primaryGenerator.getPlotGenerator();
                    } else {
                        return;
                    }
                }
            } else {
                return;
            }
            // Conventional plot generator
            PlotArea plotArea = plotGenerator.getNewPlotArea(world, null, null, null);
            PlotManager plotManager = plotGenerator.getNewPlotManager();
            PlotSquared.log(C.PREFIX + "&aDetected world load for '" + world + "'");
            PlotSquared.log(C.PREFIX + "&3 - generator: &7" + baseGenerator + ">" + plotGenerator);
            PlotSquared.log(C.PREFIX + "&3 - plotworld: &7" + plotArea.getClass().getName());
            PlotSquared
                .log(C.PREFIX + "&3 - plotAreaManager: &7" + plotManager.getClass().getName());
            if (!this.worlds.contains(path)) {
                this.worlds.createSection(path);
                worldSection = this.worlds.getConfigurationSection(path);
            }
            plotArea.saveConfiguration(worldSection);
            plotArea.loadDefaultConfiguration(worldSection);
            try {
                this.worlds.save(this.worldsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Now add it
            addPlotArea(plotArea);
            plotGenerator.initialize(plotArea);
        } else {
            if (!worlds.contains(world)) {
                return;
            }
            ConfigurationSection areasSection = worldSection.getConfigurationSection("areas");
            if (areasSection == null) {
                if (plotAreaManager.getPlotAreas(world, null).length != 0) {
                    debug("World possibly already loaded: " + world);
                    return;
                }
                PlotSquared.log(C.PREFIX + "&aDetected world load for '" + world + "'");
                String gen_string = worldSection.getString("generator.plugin", IMP.getPluginName());
                if (type == 2) {
                    Set<PlotCluster> clusters = this.clusters_tmp != null ?
                        this.clusters_tmp.get(world) :
                        new HashSet<PlotCluster>();
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

                        PlotSquared.log(C.PREFIX + "&3 - " + name + "-" + pos1 + "-" + pos2);
                        GeneratorWrapper<?> areaGen = this.IMP.getGenerator(world, gen_string);
                        if (areaGen == null) {
                            throw new IllegalArgumentException("Invalid Generator: " + gen_string);
                        }
                        PlotArea pa =
                            areaGen.getPlotGenerator().getNewPlotArea(world, name, pos1, pos2);
                        pa.saveConfiguration(worldSection);
                        pa.loadDefaultConfiguration(worldSection);
                        try {
                            this.worlds.save(this.worldsFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        PlotSquared
                            .log(C.PREFIX + "&c | &9generator: &7" + baseGenerator + ">" + areaGen);
                        PlotSquared.log(C.PREFIX + "&c | &9plotworld: &7" + pa);
                        PlotSquared.log(C.PREFIX + "&c | &9manager: &7" + pa);
                        PlotSquared.log(C.PREFIX + "&cNote: &7Area created for cluster:" + name
                            + " (invalid or old configuration?)");
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
                    this.worlds.save(this.worldsFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PlotSquared.log(C.PREFIX + "&3 - generator: &7" + baseGenerator + ">" + areaGen);
                PlotSquared.log(C.PREFIX + "&3 - plotworld: &7" + pa);
                PlotSquared.log(C.PREFIX + "&3 - plotAreaManager: &7" + pa.getPlotManager());
                areaGen.getPlotGenerator().initialize(pa);
                areaGen.augment(pa);
                addPlotArea(pa);
                return;
            }
            if (type == 1) {
                throw new IllegalArgumentException(
                    "Invalid type for multi-area world. Expected `2`, got `" + 1 + "`");
            }
            for (String areaId : areasSection.getKeys(false)) {
                PlotSquared.log(C.PREFIX + "&3 - " + areaId);
                String[] split = areaId.split("(?<=[^;-])-");
                if (split.length != 3) {
                    throw new IllegalArgumentException("Invalid Area identifier: " + areaId
                        + ". Expected form `<name>-<pos1>-<pos2>`");
                }
                String name = split[0];
                PlotId pos1 = PlotId.fromString(split[1]);
                PlotId pos2 = PlotId.fromString(split[2]);
                if (pos1 == null || pos2 == null || name.isEmpty()) {
                    throw new IllegalArgumentException("Invalid Area identifier: " + areaId
                        + ". Expected form `<name>-<x1;z1>-<x2;z2>`");
                }
                PlotArea existing = getPlotArea(world, name);
                if (existing != null && name.equals(existing.id)) {
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
                String gen_string = clone.getString("generator.plugin", IMP.getPluginName());
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
                    this.worlds.save(this.worldsFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                PlotSquared.log(C.PREFIX + "&aDetected area load for '" + world + "'");
                PlotSquared.log(C.PREFIX + "&c | &9generator: &7" + baseGenerator + ">" + areaGen);
                PlotSquared.log(C.PREFIX + "&c | &9plotworld: &7" + pa);
                PlotSquared.log(C.PREFIX + "&c | &9manager: &7" + pa.getPlotManager());
                areaGen.getPlotGenerator().initialize(pa);
                areaGen.augment(pa);
                addPlotArea(pa);
            }
        }
    }

    /**
     * Setup the configuration for a plot world based on world arguments.
     *
     *
     * <i>e.g. /mv create &lt;world&gt; normal -g PlotSquared:&lt;args&gt;</i>
     *
     * @param world     The name of the world
     * @param args      The arguments
     * @param generator the plot generator
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
                    PlotSquared.log("&cNo value provided for: &7" + element);
                    return false;
                }
                String key = pair[0].toLowerCase();
                String value = pair[1];
                String base = "worlds." + world + ".";
                try {
                    switch (key) {
                        case "s":
                        case "size":
                            this.worlds.set(base + "plot.size",
                                Configuration.INTEGER.parseString(value).shortValue());
                            break;
                        case "g":
                        case "gap":
                            this.worlds.set(base + "road.width",
                                Configuration.INTEGER.parseString(value).shortValue());
                            break;
                        case "h":
                        case "height":
                            this.worlds.set(base + "road.height",
                                Configuration.INTEGER.parseString(value).shortValue());
                            this.worlds.set(base + "plot.height",
                                Configuration.INTEGER.parseString(value).shortValue());
                            this.worlds.set(base + "wall.height",
                                Configuration.INTEGER.parseString(value).shortValue());
                            break;
                        case "f":
                        case "floor":
                            this.worlds.set(base + "plot.floor", new ArrayList<>(Arrays.asList(
                                StringMan.join(Configuration.BLOCKLIST.parseString(value), ",")
                                    .split(","))));
                            break;
                        case "m":
                        case "main":
                            this.worlds.set(base + "plot.filling", new ArrayList<>(Arrays.asList(
                                StringMan.join(Configuration.BLOCKLIST.parseString(value), ",")
                                    .split(","))));
                            break;
                        case "w":
                        case "wall":
                            this.worlds.set(base + "wall.filling",
                                Configuration.BLOCK.parseString(value).toString());
                            break;
                        case "b":
                        case "border":
                            this.worlds.set(base + "wall.block",
                                Configuration.BLOCK.parseString(value).toString());
                            break;
                        default:
                            PlotSquared.log("&cKey not found: &7" + element);
                            return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    PlotSquared.log("&cInvalid value: &7" + value + " in arg " + element);
                    return false;
                }
            }
            try {
                ConfigurationSection section =
                    this.worlds.getConfigurationSection("worlds." + world);
                plotworld.saveConfiguration(section);
                plotworld.loadConfiguration(section);
                this.worlds.save(this.worldsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean canUpdate(@NonNull final String current, @NonNull final String other) {
        final String s1 = normalisedVersion(current);
        final String s2 = normalisedVersion(other);
        return s1.compareTo(s2) < 0;
    }

    public String normalisedVersion(@NonNull final String version) {
        final String[] split = Pattern.compile(".", Pattern.LITERAL).split(version);
        final StringBuilder sb = new StringBuilder();
        for (final String s : split) {
            sb.append(String.format("%" + 4 + 's', s));
        }
        return sb.toString();
    }

    public boolean update(PlotPlayer sender, URL url) {
        try {
            String name = this.jarFile.getName();
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
                    MainUtil.sendMessage(sender, "Failed to update " + IMP.getPluginName() + "");
                    MainUtil.sendMessage(sender, "Jar file failed to delete.");
                    MainUtil.sendMessage(sender, " - Please update manually");
                }
                Files.copy(stream, newJar.toPath());
            }
            MainUtil.sendMessage(sender,
                "$1The update will take effect when the server is restarted next");
            return true;
        } catch (IOException e) {
            MainUtil.sendMessage(sender, "Failed to update " + IMP.getPluginName() + "");
            MainUtil.sendMessage(sender, " - Please update manually");
            PlotSquared.log("============ Stacktrace ============");
            e.printStackTrace();
            PlotSquared.log("====================================");
        }
        return false;
    }

    /**
     * Copy a file from inside the jar to a location
     *
     * @param file   Name of the file inside PlotSquared.jar
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
                    try (ZipInputStream zis = new ZipInputStream(
                        new FileInputStream(this.jarFile))) {
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
            PlotSquared.log("&cCould not save " + file);
        }
    }

    private Map<String, Map<PlotId, Plot>> getPlotsRaw() {
        HashMap<String, Map<PlotId, Plot>> map = new HashMap<>();
        for (PlotArea area : this.plotAreaManager.getAllPlotAreas()) {
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
     * Close the database connection.
     */
    public void disable() {
        try {
            // Validate that all data in the db is correct
            final HashSet<Plot> plots = new HashSet<>();
            foreachPlotRaw(new RunnableVal<Plot>() {
                @Override public void run(Plot value) {
                    plots.add(value);
                }
            });
            DBFunc.validatePlots(plots);

            // Close the connection
            DBFunc.close();
            UUIDHandler.handleShutdown();
        } catch (NullPointerException ignored) {
            ignored.printStackTrace();
            PlotSquared.log("&cCould not close database connection!");
        }
    }

    /**
     * Setup the database connection.
     */
    public void setupDatabase() {
        try {
            if (DBFunc.dbManager != null) {
                DBFunc.dbManager.close();
            }
            Database database;
            if (Storage.MySQL.USE) {
                database = new MySQL(Storage.MySQL.HOST, Storage.MySQL.PORT, Storage.MySQL.DATABASE,
                    Storage.MySQL.USER, Storage.MySQL.PASSWORD);
            } else if (Storage.SQLite.USE) {
                File file = MainUtil.getFile(IMP.getDirectory(), Storage.SQLite.DB + ".db");
                database = new SQLite(file);
            } else {
                PlotSquared.log(C.PREFIX + "&cNo storage type is set!");
                this.IMP.disable();
                return;
            }
            DBFunc.dbManager = new SQLManager(database, Storage.PREFIX, false);
            this.plots_tmp = DBFunc.getPlots();
            if (plotAreaManager instanceof SinglePlotAreaManager) {
                SinglePlotArea area = ((SinglePlotAreaManager) plotAreaManager).getArea();
                addPlotArea(area);
                ConfigurationSection section = worlds.getConfigurationSection("worlds.*");
                if (section == null) {
                    section = worlds.createSection("worlds.*");
                }
                area.saveConfiguration(section);
                area.loadDefaultConfiguration(section);
            }
            this.clusters_tmp = DBFunc.getClusters();
        } catch (ClassNotFoundException | SQLException e) {
            PlotSquared.log(
                C.PREFIX + "&cFailed to open DATABASE connection. The plugin will disable itself.");
            if (Storage.MySQL.USE) {
                PlotSquared.log("$4MYSQL");
            } else if (Storage.SQLite.USE) {
                PlotSquared.log("$4SQLITE");
            }
            PlotSquared.log(
                "&d==== Here is an ugly stacktrace, if you are interested in those things ===");
            e.printStackTrace();
            PlotSquared.log("&d==== End of stacktrace ====");
            PlotSquared.log("&6Please go to the " + IMP.getPluginName()
                + " 'storage.yml' and configure the database correctly.");
            this.IMP.disable();
        }
    }

    /**
     * Setup the default configuration.
     *
     * @throws IOException if the config failed to save
     */
    public void setupConfig() throws IOException {
        String lastVersionString = this.config.getString("version");
        if (lastVersionString != null) {
            String[] split = lastVersionString.split("\\.");
            int[] lastVersion = new int[] {Integer.parseInt(split[0]), Integer.parseInt(split[1]),
                Integer.parseInt(split[2])};
            if (checkVersion(new int[] {3, 4, 0}, lastVersion)) {
                Settings.convertLegacy(configFile);
                if (config.contains("worlds")) {
                    ConfigurationSection worldSection = config.getConfigurationSection("worlds");
                    worlds.set("worlds", worldSection);
                    try {
                        worlds.save(worldsFile);
                    } catch (IOException e) {
                        PlotSquared.debug("Failed to save " + IMP.getPluginName() + " worlds.yml");
                        e.printStackTrace();
                    }
                }
                Settings.save(configFile);
            }
        }
        Settings.load(configFile);
        try {
            InputStream stream = getClass().getResourceAsStream("/plugin.properties");
            java.util.Scanner scanner = new java.util.Scanner(stream).useDelimiter("\\A");
            String versionString = scanner.next().trim();
            scanner.close();
            this.version = PlotVersion.tryParse(versionString);
            Settings.DATE = new Date(100 + version.year, version.month, version.day).toGMTString();
            Settings.BUILD = "https://ci.athion.net/job/PlotSquared/" + version.build;
            Settings.COMMIT = "https://github.com/IntellectualSites/PlotSquared/commit/" + Integer
                .toHexString(version.hash);
            System.out.println("Version is " + this.version);
        } catch (Throwable ignore) {
            ignore.printStackTrace();
        }
        Settings.save(configFile);
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Setup all configuration files<br>
     * - Config: settings.yml<br>
     * - Storage: storage.yml<br>
     * - Translation: PlotSquared.use_THIS.yml, style.yml<br>
     */
    public void setupConfigs() {
        File folder = new File(this.IMP.getDirectory(), "config");
        if (!folder.exists() && !folder.mkdirs()) {
            PlotSquared.log(C.PREFIX
                + "&cFailed to create the /plugins/config folder. Please create it manually.");
        }
        try {
            this.worldsFile = new File(folder, "worlds.yml");
            if (!this.worldsFile.exists() && !this.worldsFile.createNewFile()) {
                PlotSquared.log(
                    "Could not create the worlds file, please create \"worlds.yml\" manually.");
            }
            this.worlds = YamlConfiguration.loadConfiguration(this.worldsFile);
        } catch (IOException ignored) {
            PlotSquared.log("Failed to save settings.yml");
        }
        try {
            this.configFile = new File(folder, "settings.yml");
            if (!this.configFile.exists() && !this.configFile.createNewFile()) {
                PlotSquared.log(
                    "Could not create the settings file, please create \"settings.yml\" manually.");
            }
            this.config = YamlConfiguration.loadConfiguration(this.configFile);
            setupConfig();
        } catch (IOException ignored) {
            PlotSquared.log("Failed to save settings.yml");
        }
        try {
            this.styleFile = MainUtil.getFile(IMP.getDirectory(),
                Settings.Paths.TRANSLATIONS + File.separator + "style.yml");
            if (!this.styleFile.exists()) {
                if (!this.styleFile.getParentFile().exists()) {
                    this.styleFile.getParentFile().mkdirs();
                }
                if (!this.styleFile.createNewFile()) {
                    PlotSquared.log(
                        "Could not create the style file, please create \"translations/style.yml\" manually");
                }
            }
            this.style = YamlConfiguration.loadConfiguration(this.styleFile);
            setupStyle();
        } catch (IOException err) {
            err.printStackTrace();
            PlotSquared.log("failed to save style.yml");
        }
        try {
            this.storageFile = new File(folder, "storage.yml");
            if (!this.storageFile.exists() && !this.storageFile.createNewFile()) {
                PlotSquared.log(
                    "Could not the storage settings file, please create \"storage.yml\" manually.");
            }
            this.storage = YamlConfiguration.loadConfiguration(this.storageFile);
            setupStorage();
        } catch (IOException ignored) {
            PlotSquared.log("Failed to save storage.yml");
        }
        try {
            this.commandsFile = new File(folder, "commands.yml");
            if (!this.commandsFile.exists() && !this.commandsFile.createNewFile()) {
                PlotSquared.log(
                    "Could not the storage settings file, please create \"commands.yml\" manually.");
            }
            this.commands = YamlConfiguration.loadConfiguration(this.commandsFile);
        } catch (IOException ignored) {
            PlotSquared.log("Failed to save commands.yml");
        }
        try {
            this.style.save(this.styleFile);
            this.commands.save(this.commandsFile);
        } catch (IOException e) {
            PlotSquared.log("Configuration file saving failed");
            e.printStackTrace();
        }
    }

    /**
     * Setup the storage file (load + save missing nodes).
     */
    private void setupStorage() {
        Storage.load(storageFile);
        Storage.save(storageFile);
        storage = YamlConfiguration.loadConfiguration(storageFile);
    }

    /**
     * Show startup debug information.
     */
    private void showDebug() {
        if (Settings.DEBUG) {
            Map<String, Object> components = Settings.getFields(Settings.Enabled_Components.class);
            for (Entry<String, Object> component : components.entrySet()) {
                PlotSquared.log(C.PREFIX + String
                    .format("&cKey: &6%s&c, Value: &6%s", component.getKey(),
                        component.getValue()));
            }
        }
    }

    /**
     * Setup the style.yml file
     */
    private void setupStyle() {
        if (this.version != null) {
            this.style.set("version", this.version.toString());
        }
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
     * Get the Java version.
     *
     * @return the java version
     */
    public double getJavaVersion() {
        return Double.parseDouble(System.getProperty("java.specification.version"));
    }

    public void foreachPlotArea(@NonNull final RunnableVal<PlotArea> runnable) {
        for (final PlotArea area : this.plotAreaManager.getAllPlotAreas()) {
            runnable.run(area);
        }
    }

    public void foreachPlotArea(@NonNull final String world,
        @NonNull final RunnableVal<PlotArea> runnable) {
        final PlotArea[] array = this.plotAreaManager.getPlotAreas(world, null);
        if (array == null) {
            return;
        }
        for (final PlotArea area : array) {
            runnable.run(area);
        }
    }

    public void foreachPlot(@NonNull final RunnableVal<Plot> runnable) {
        for (final PlotArea area : this.plotAreaManager.getAllPlotAreas()) {
            area.getPlots().forEach(runnable::run);
        }
    }

    public void foreachPlotRaw(@NonNull final RunnableVal<Plot> runnable) {
        for (final PlotArea area : this.plotAreaManager.getAllPlotAreas()) {
            area.getPlots().forEach(runnable::run);
        }
        if (this.plots_tmp != null) {
            for (final HashMap<PlotId, Plot> entry : this.plots_tmp.values()) {
                entry.values().forEach(runnable::run);
            }
        }
    }

    public void foreachBasePlot(@NonNull final RunnableVal<Plot> run) {
        for (final PlotArea area : this.plotAreaManager.getAllPlotAreas()) {
            area.foreachBasePlot(run);
        }
    }

    public PlotArea getFirstPlotArea() {
        PlotArea[] areas = plotAreaManager.getAllPlotAreas();
        return areas.length > 0 ? areas[0] : null;
    }

    public int getPlotAreaCount() {
        return this.plotAreaManager.getAllPlotAreas().length;
    }

    public int getPlotCount() {
        int count = 0;
        for (final PlotArea area : this.plotAreaManager.getAllPlotAreas()) {
            count += area.getPlotCount();
        }
        return count;
    }

    public Set<PlotArea> getPlotAreas() {
        final Set<PlotArea> set = new HashSet<>();
        Collections.addAll(set, plotAreaManager.getAllPlotAreas());
        return Collections.unmodifiableSet(set);
    }

    public boolean isAugmented(@NonNull final String world) {
        final PlotArea[] areas = plotAreaManager.getPlotAreas(world, null);
        return areas != null && (areas.length > 1 || areas[0].TYPE != 0);
    }

    /**
     * Get a list of PlotArea objects.
     *
     * @param world the world
     * @return Collection of PlotArea objects
     */
    public Set<PlotArea> getPlotAreas(@NonNull final String world) {
        final Set<PlotArea> set = new HashSet<>();
        Collections.addAll(set, plotAreaManager.getPlotAreas(world, null));
        return set;
    }

    /**
     * Get the relevant plot area for a specified location.
     * <ul>
     * <li>If there is only one plot area globally that will be returned.
     * <li>If there is only one plot area in the world, it will return that.
     * <li>If the plot area for a location cannot be unambiguously
     * resolved, null will be returned.
     * </ul>
     * Note: An applicable plot area may not include the location i.e. clusters
     *
     * @param location the location
     * @return
     */
    public PlotArea getApplicablePlotArea(@NonNull final Location location) {
        return plotAreaManager.getApplicablePlotArea(location);
    }

    public PlotArea getPlotArea(@NonNull final String world, @NonNull final String id) {
        return plotAreaManager.getPlotArea(world, id);
    }

    /**
     * Get the {@code PlotArea} which contains a location.
     * <ul>
     * <li>If the plot area does not contain a location, null
     * will be returned.
     * </ul>
     *
     * @param location the location
     * @return the {@link PlotArea} in the location, null if non existent
     */
    public PlotArea getPlotAreaAbs(@NonNull final Location location) {
        return plotAreaManager.getPlotArea(location);
    }

    public PlotArea getPlotAreaByString(@NonNull final String search) {
        String[] split = search.split("[;,]");
        PlotArea[] areas = plotAreaManager.getPlotAreas(split[0], null);
        if (areas == null) {
            for (PlotArea area : plotAreaManager.getAllPlotAreas()) {
                if (area.worldname.equalsIgnoreCase(split[0])) {
                    if (area.id == null || split.length == 2 && area.id
                        .equalsIgnoreCase(split[1])) {
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

    /**
     * Get Plots based on alias
     *
     * @param alias     to search plots
     * @param worldname to filter alias to a specific world [optional] null means all worlds
     * @return Set<{                                                               @                                                               link                                                                                                                               Plot                                                               }> empty if nothing found
     */
    public Set<Plot> getPlotsByAlias(@Nullable final String alias,
        @NonNull final String worldname) {
        final Set<Plot> result = new HashSet<>();
        if (alias != null) {
            for (final Plot plot : getPlots()) {
                if (alias.equals(plot.getAlias()) && (worldname == null || worldname
                    .equals(plot.getWorldName()))) {
                    result.add(plot);
                }
            }
        }
        return Collections.unmodifiableSet(result);
    }

    public Set<PlotArea> getPlotAreas(final String world, final RegionWrapper region) {
        final PlotArea[] areas = plotAreaManager.getPlotAreas(world, region);
        final Set<PlotArea> set = new HashSet<>();
        Collections.addAll(set, areas);
        return Collections.unmodifiableSet(set);
    }

    public enum SortType {
        CREATION_DATE, CREATION_DATE_TIMESTAMP, LAST_MODIFIED, DISTANCE_FROM_ORIGIN
    }
}
