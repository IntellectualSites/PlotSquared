/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core;

import com.plotsquared.core.command.WE_Anywhere;
import com.plotsquared.core.components.ComponentPresetManager;
import com.plotsquared.core.configuration.Caption;
import com.plotsquared.core.configuration.caption.CaptionMap;
import com.plotsquared.core.configuration.CaptionUtility;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.ConfigurationUtil;
import com.plotsquared.core.configuration.MemorySection;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.Storage;
import com.plotsquared.core.configuration.caption.CaptionLoader;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.configuration.serialization.ConfigurationSerialization;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.database.Database;
import com.plotsquared.core.database.MySQL;
import com.plotsquared.core.database.SQLManager;
import com.plotsquared.core.database.SQLite;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.generator.HybridPlotWorld;
import com.plotsquared.core.generator.HybridUtils;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.listener.WESubscriber;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.BlockBucket;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotCluster;
import com.plotsquared.core.plot.PlotFilter;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.plot.comment.CommentManager;
import com.plotsquared.core.plot.expiration.ExpireManager;
import com.plotsquared.core.plot.expiration.ExpiryTask;
import com.plotsquared.core.plot.world.DefaultPlotAreaManager;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.InventoryUtil;
import com.plotsquared.core.util.LegacyConverter;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.ReflectionUtils;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.SetupUtils;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.logger.ILogger;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.uuid.UUIDPipeline;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * An implementation of the core, with a static getter for easy access.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class PlotSquared {
    private static final Set<Plot> EMPTY_SET = Collections.unmodifiableSet(Collections.emptySet());
    private static PlotSquared instance;
    // Implementation
    public final IPlotMain<?> IMP;
    // Current thread
    private final Thread thread;
    // UUID pipelines
    @Getter private final UUIDPipeline impromptuUUIDPipeline =
        new UUIDPipeline(Executors.newCachedThreadPool());
    @Getter private final UUIDPipeline backgroundUUIDPipeline =
        new UUIDPipeline(Executors.newSingleThreadExecutor());
    // WorldEdit instance
    public WorldEdit worldedit;
    public File styleFile;
    public File configFile;
    public File worldsFile;
    public File commandsFile;
    public File translationFile;
    public YamlConfiguration style;
    public YamlConfiguration worlds;
    public YamlConfiguration storage;
    public YamlConfiguration commands;
    // Temporary hold the plots/clusters before the worlds load
    public HashMap<String, Set<PlotCluster>> clusters_tmp;
    public HashMap<String, HashMap<PlotId, Plot>> plots_tmp;
    private YamlConfiguration config;
    // Localization
    @Getter private CaptionMap captionMap;
    // Implementation logger
    @Setter @Getter private ILogger logger;
    // Platform / Version / Update URL
    private PlotVersion version;
    // Files and configuration
    @Getter private File jarFile = null; // This file
    private File storageFile;
    @Getter private PlotAreaManager plotAreaManager;
    @Getter private EventDispatcher eventDispatcher;

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

        //
        // Register configuration serializable classes
        //
        ConfigurationSerialization.registerClass(BlockBucket.class, "BlockBucket");

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
            TaskManager.IMP = this.IMP.getTaskManager();

            // World Util. Has to be done before config files are loaded
            WorldUtil.IMP = this.IMP.initWorldUtil();

            if (!setupConfigs()) {
                return;
            }

            // Setup localization
            CaptionMap captionMap;
            if (Settings.Enabled_Components.PER_USER_LOCALE) {
                captionMap = CaptionLoader.loadAll(Paths.get("lang"));
            } else {
                String fileName = "messages_" + Settings.Enabled_Components.DEFAULT_LOCALE + ".json";
                captionMap = CaptionLoader.loadSingle(Paths.get("lang", fileName));
            }
            this.captionMap = captionMap;

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

            // Check if we need to convert old flag values, etc
            if (!getConfigurationVersion().equalsIgnoreCase("v5")) {
                // Perform upgrade
                if (DBFunc.dbManager.convertFlags()) {
                    log(Captions.PREFIX.getTranslated() + "Flags were converted successfully!");
                    // Update the config version
                    setConfigurationVersion("v5");
                }
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
            }
            // Required
            this.IMP.registerWorldEvents();
            if (Settings.Enabled_Components.CHUNK_PROCESSOR) {
                this.IMP.registerChunkProcessor();
            }
            // Create Event utility class
            eventDispatcher = new EventDispatcher();
            // create Hybrid utility class
            HybridUtils.manager = this.IMP.initHybridUtils();
            // Inventory utility class
            InventoryUtil.manager = this.IMP.initInventoryUtil();
            // create setup util class
            SetupUtils.manager = this.IMP.initSetupUtils();
            // Set block
            GlobalBlockQueue.IMP =
                new GlobalBlockQueue(IMP.initBlockQueue(), 1, Settings.QUEUE.TARGET_TIME);
            GlobalBlockQueue.IMP.runTask();
            // Set chunk
            ChunkManager.manager = this.IMP.initChunkManager();
            RegionManager.manager = this.IMP.initRegionManager();
            // Schematic handler
            SchematicHandler.manager = this.IMP.initSchematicHandler();
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
                        PlotSquared.log(Captions.PREFIX.getTranslated() + "&6" + IMP.getPluginName()
                            + " hooked into WorldEdit.");
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
                TaskManager.runTask(() -> EconHandler.initializeEconHandler());
            }

            if (Settings.Enabled_Components.COMPONENT_PRESETS) {
                try {
                    new ComponentPresetManager();
                } catch (final Exception e) {
                    PlotSquared.log(Captions.PREFIX + "Failed to initialize the preset system");
                    e.printStackTrace();
                }
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
                TaskManager.runTaskLater(() -> {
                    for (String world : section.getKeys(false)) {
                        if (world.equals("CheckingPlotSquaredGenerator")) {
                            continue;
                        }
                        if (!WorldUtil.IMP.isWorld(world) && !world.equals("*")) {
                            debug("`" + world + "` was not properly loaded - " + IMP.getPluginName()
                                + " will now try to load it properly: ");
                            debug(
                                " - Are you trying to delete this world? Remember to remove it from the worlds.yml, bukkit.yml and multiverse worlds.yml");
                            debug(
                                " - Your world management plugin may be faulty (or non existent)");
                            debug(
                                " This message may also be a false positive and could be ignored.");
                            PlotSquared.this.IMP.setGenerator(world);
                        }
                    }
                }, 1);
            }

            // Copy files
            copyFile("addplots.js", Settings.Paths.SCRIPTS);
            copyFile("addsigns.js", Settings.Paths.SCRIPTS);
            copyFile("automerge.js", Settings.Paths.SCRIPTS);
            copyFile("fixborders.js", Settings.Paths.SCRIPTS);
            copyFile("furthest.js", Settings.Paths.SCRIPTS);
            copyFile("mycommand.js", Settings.Paths.SCRIPTS);
            copyFile("setbiomes.js", Settings.Paths.SCRIPTS);
            copyFile("start.js", Settings.Paths.SCRIPTS);
            copyFile("town.template", Settings.Paths.TEMPLATES);
            copyFile("bridge.template", Settings.Paths.TEMPLATES);
            copyFile("de-DE.yml", Settings.Paths.TRANSLATIONS);
            copyFile("es-ES.yml", Settings.Paths.TRANSLATIONS);
            copyFile("zh-CN.yml", Settings.Paths.TRANSLATIONS);
            copyFile("it-IT.yml", Settings.Paths.TRANSLATIONS);
            copyFile("ko-KR.yml", Settings.Paths.TRANSLATIONS);
            copyFile("hu-HU.yml", Settings.Paths.TRANSLATIONS);
            copyFile("pt-BR.yml", Settings.Paths.TRANSLATIONS);
            copyFile("fr-FR.yml", Settings.Paths.TRANSLATIONS);
            showDebug();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        PlotSquared.log(Captions.PREFIX + CaptionUtility
            .format(ConsolePlayer.getConsole(), Captions.ENABLED.getTranslated(),
                IMP.getPluginName()));
    }

    /**
     * Gets an instance of PlotSquared.
     *
     * @return instance of PlotSquared
     */
    public static PlotSquared get() {
        return PlotSquared.instance;
    }

    @NotNull public static IPlotMain<?> imp() {
        if (instance != null && instance.IMP != null) {
            return instance.IMP;
        }
        throw new IllegalStateException("Plot main implementation is missing");
    }

    /**
     * Log a message to the IPlotMain logger.
     *
     * @param message Message to log
     * @see IPlotMain#log(String)
     */
    public static void log(Object message) {
        if (message == null || (message instanceof Caption ?
            ((Caption) message).getTranslated().isEmpty() :
            message.toString().isEmpty())) {
            return;
        }
        if (PlotSquared.get() == null || PlotSquared.get().getLogger() == null) {
            System.out.printf("[P2][Info] %s\n", StringMan.getString(message));
        } else {
            PlotSquared.get().getLogger().log(StringMan.getString(message));
        }
    }

    /**
     * Log a message to the IPlotMain logger.
     *
     * @param message Message to log
     * @see IPlotMain#log(String)
     */
    public static void debug(@Nullable Object message) {
        if (Settings.DEBUG) {
            if (PlotSquared.get() == null || PlotSquared.get().getLogger() == null) {
                System.out.printf("[P2][Debug] %s\n", StringMan.getString(message));
            } else {
                PlotSquared.get().getLogger().log(StringMan.getString(message));
            }
        }
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

    public boolean isMainThread(Thread thread) {
        return this.thread == thread;
    }

    /**
     * Check if `version` is &gt;= `version2`.
     *
     * @param version  First version
     * @param version2 Second version
     * @return true if `version` is &gt;= `version2`
     */
    public boolean checkVersion(int[] version, int... version2) {
        return version[0] > version2[0] || version[0] == version2[0] && version[1] > version2[1]
            || version[0] == version2[0] && version[1] == version2[1] && version[2] >= version2[2];
    }

    /**
     * Gets the current PlotSquared version.
     *
     * @return current version in config or null
     */
    public PlotVersion getVersion() {
        return this.version;
    }

    /**
     * Gets the server platform this plugin is running on this is running on.
     *
     * <p>This will be either <b>Bukkit</b> or <b>Sponge</b></p>
     *
     * @return the server implementation
     */
    public String getPlatform() {
        return Settings.PLATFORM;
    }

    public PlotManager getPlotManager(Plot plot) {
        return plot.getArea().getPlotManager();
    }

    public PlotManager getPlotManager(Location location) {
        PlotArea pa = getPlotAreaAbs(location);
        return pa != null ? pa.getPlotManager() : null;
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
            if (plotArea.getType() == PlotAreaType.PARTIAL) {
                plots = this.plots_tmp != null ? this.plots_tmp.get(plotArea.getWorldName()) : null;
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
            if (plotArea.getType() == PlotAreaType.PARTIAL) {
                clusters = this.clusters_tmp != null ?
                    this.clusters_tmp.get(plotArea.getWorldName()) :
                    null;
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
        if (!Settings.Enabled_Components.PERSISTENT_ROAD_REGEN) {
            return;
        }
        File file = new File(
            this.IMP.getDirectory() + File.separator + "persistent_regen_data_" + plotArea.getId()
                + "_" + plotArea.getWorldName());
        if (!file.exists()) {
            return;
        }
        TaskManager.runTask(() -> {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                List<Object> list = (List<Object>) ois.readObject();
                ArrayList<int[]> regionInts = (ArrayList<int[]>) list.get(0);
                ArrayList<int[]> chunkInts = (ArrayList<int[]>) list.get(1);
                HashSet<BlockVector2> regions = new HashSet<>();
                Set<BlockVector2> chunks = new HashSet<>();
                regionInts.forEach(l -> regions.add(BlockVector2.at(l[0], l[1])));
                chunkInts.forEach(l -> chunks.add(BlockVector2.at(l[0], l[1])));
                int height = (int) list.get(2);
                PlotSquared.log(
                    Captions.PREFIX + "Incomplete road regeneration found. Restarting in world "
                        + plotArea.getWorldName() + " with height " + height + ".");
                PlotSquared.debug("   Regions: " + regions.size());
                PlotSquared.debug("   Chunks: " + chunks.size());
                HybridUtils.UPDATE = true;
                HybridUtils.manager.scheduleRoadUpdate(plotArea, regions, height, chunks);
            } catch (IOException | ClassNotFoundException e) {
                PlotSquared.log(Captions.PREFIX + "Error restarting road regeneration.");
                e.printStackTrace();
            } finally {
                if (!file.delete()) {
                    PlotSquared.log(
                        Captions.PREFIX + "Error deleting persistent_regen_data_" + plotArea.getId()
                            + ". Please manually delete this file.");
                }
            }
        });
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
            if (area.getWorldName().equals(world)) {
                removePlotArea(area);
            }
        }
    }

    private void setPlotsTmp(PlotArea area) {
        if (this.plots_tmp == null) {
            this.plots_tmp = new HashMap<>();
        }
        HashMap<PlotId, Plot> map =
            this.plots_tmp.computeIfAbsent(area.toString(), k -> new HashMap<>());
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
     * Gets all the base plots in a single set (for merged plots it just returns
     * the bottom plot).
     *
     * @return Set of base Plots
     */
    public Set<Plot> getBasePlots() {
        return PlotQuery.newQuery().whereBasePlot().asSet();
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
        overflow.sort(Comparator.comparingInt(Plot::hashCode));
        result.addAll(overflow);
        return result;
    }

    /**
     * Sort plots by hashcode.
     *
     * @param plots the collection of plots to sort
     * @return the sorted collection
     */
    private ArrayList<Plot> sortPlotsByHash(Collection<Plot> plots) {
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
        Plot[] overflowArray = overflow.toArray(new Plot[0]);
        sortPlotsByHash(overflowArray);
        ArrayList<Plot> result = new ArrayList<>(cache.length + overflowArray.length);
        for (Plot plot : cache) {
            if (plot != null) {
                result.add(plot);
            }
        }
        Collections.addAll(result, overflowArray);
        result.addAll(extra);
        return result;
    }

    /**
     * Unchecked, use {@link #sortPlots(Collection, SortType, PlotArea)} instead which will in turn call this.
     *
     * @param input an array of plots to sort
     */
    private void sortPlotsByHash(Plot[] input) {
        List<Plot>[] bucket = new ArrayList[32];
        Arrays.fill(bucket, new ArrayList<>());
        boolean maxLength = false;
        int placement = 1;
        while (!maxLength) {
            maxLength = true;
            for (Plot plot : input) {
                int tmp = MathMan.getPositiveId(plot.hashCode()) / placement;
                bucket[tmp & 31].add(plot);
                if (maxLength && tmp > 0) {
                    maxLength = false;
                }
            }
            int a = 0;
            for (int i = 0; i < 32; i++) {
                for (Plot plot : bucket[i]) {
                    input[a++] = plot;
                }
                bucket[i].clear();
            }
            placement *= 32;
        }
    }

    private ArrayList<Plot> sortPlotsByTimestamp(Collection<Plot> plots) {
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
        Plot[] overflowArray = overflow.toArray(new Plot[0]);
        sortPlotsByHash(overflowArray);
        ArrayList<Plot> result = new ArrayList<>(cache.length + overflowArray.length);
        for (Plot plot : cache) {
            if (plot != null) {
                result.add(plot);
            }
        }
        Collections.addAll(result, overflowArray);
        result.addAll(extra);
        return result;
    }

    /**
     * Sort plots by creation timestamp.
     *
     * @param input
     * @return
     */
    private List<Plot> sortPlotsByModified(Collection<Plot> input) {
        List<Plot> list;
        if (input instanceof List) {
            list = (List<Plot>) input;
        } else {
            list = new ArrayList<>(input);
        }
        list.sort(Comparator.comparingLong(a -> ExpireManager.IMP.getTimestamp(a.getOwnerAbs())));
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
                map.put(area, new ArrayList<>(0));
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
        areas.sort((a, b) -> {
            if (priorityArea != null) {
                if (a.equals(priorityArea)) {
                    return -1;
                } else if (b.equals(priorityArea)) {
                    return 1;
                }
            }
            return a.hashCode() - b.hashCode();
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
     * @deprecated Use {@link PlotQuery}
     */
    @Deprecated public Set<Plot> getPlots(final PlotFilter... filters) {
        final List<PlotArea> areas = new LinkedList<>();
        for (final PlotArea plotArea : this.getPlotAreas()) {
            for (final PlotFilter filter : filters) {
                if (filter.allowsArea(plotArea)) {
                    areas.add(plotArea);
                }
            }
        }
        return PlotQuery.newQuery().inAreas(areas).thatPasses(plot -> {
            for (final PlotFilter filter : filters) {
                if (!filter.allowsPlot(plot)) {
                    return false;
                }
            }
            return true;
        }).asSet();
    }

    /**
     * Gets all the plots across all plotworlds in one {@code Set}.
     *
     * @return all the plots on the server loaded by this plugin
     */
    public Set<Plot> getPlots() {
        int size = getPlotCount();
        final Set<Plot> result = new HashSet<>(size);
        forEachPlotArea(value -> result.addAll(value.getPlots()));
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
                HashMap<PlotId, Plot> map =
                    this.plots_tmp.computeIfAbsent(world, k -> new HashMap<>());
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
     * Gets all the plots owned by a player name.
     *
     * @param world  the world
     * @param player the plot owner
     * @return Set of Plot
     */
    public Set<Plot> getPlots(String world, String player) {
        final UUID uuid = this.impromptuUUIDPipeline.getSingle(player, Settings.UUID.BLOCKING_TIMEOUT);
        return getPlots(world, uuid);
    }

    /**
     * Gets all the plots owned by a player name.
     *
     * @param area   the PlotArea
     * @param player the plot owner
     * @return Set of Plot
     */
    public Set<Plot> getPlots(PlotArea area, String player) {
        final UUID uuid = this.impromptuUUIDPipeline.getSingle(player, Settings.UUID.BLOCKING_TIMEOUT);
        return getPlots(area, uuid);
    }

    /**
     * Gets all plots by a PlotPlayer.
     *
     * @param world  the world
     * @param player the plot owner
     * @return Set of plot
     */
    public Set<Plot> getPlots(String world, PlotPlayer player) {
        return PlotQuery.newQuery().inWorld(world).ownedBy(player).asSet();
    }

    /**
     * Gets all plots by a PlotPlayer.
     *
     * @param area   the PlotArea
     * @param player the plot owner
     * @return Set of plot
     */
    public Set<Plot> getPlots(PlotArea area, PlotPlayer player) {
        return PlotQuery.newQuery().inArea(area).ownedBy(player).asSet();
    }

    /**
     * Gets all plots by a UUID in a world.
     *
     * @param world the world
     * @param uuid  the plot owner
     * @return Set of plot
     */
    public Set<Plot> getPlots(String world, UUID uuid) {
        return PlotQuery.newQuery().inWorld(world).ownedBy(uuid).asSet();
    }

    /**
     * Gets all plots by a UUID in an area.
     *
     * @param area the {@code PlotArea}
     * @param uuid the plot owner
     * @return Set of plots
     */
    public Set<Plot> getPlots(PlotArea area, UUID uuid) {
        return PlotQuery.newQuery().inArea(area).ownedBy(uuid).asSet();
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
        return PlotQuery.newQuery().inWorld(world).asCollection();
    }

    /**
     * Gets the plots for a PlotPlayer.
     *
     * @param player the player to retrieve the plots for
     * @return Set of Plot
     */
    public Set<Plot> getPlots(PlotPlayer player) {
        return PlotQuery.newQuery().ownedBy(player).asSet();
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
     * Gets the plots for a UUID.
     *
     * @param uuid the plot owner
     * @return Set of Plot's owned by the player
     */
    public Set<Plot> getPlots(final UUID uuid) {
        return PlotQuery.newQuery().ownedBy(uuid).asSet();
    }

    public boolean hasPlot(final UUID uuid) {
        return Arrays.stream(plotAreaManager.getAllPlotAreas())
            .anyMatch(area -> area.hasPlot(uuid));
    }

    public Set<Plot> getBasePlots(final UUID uuid) {
        return PlotQuery.newQuery().ownedBy(uuid).whereBasePlot().asSet();
    }

    /**
     * Gets the plots for a UUID.
     *
     * @param uuid the UUID of the owner
     * @return Set of Plot
     */
    public Set<Plot> getPlotsAbs(final UUID uuid) {
        return PlotQuery.newQuery().ownedBy(uuid).asSet();
    }

    /**
     * Unregisters a plot from local memory without calling the database.
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
            eventDispatcher.callDelete(plot);
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
        PlotAreaType type;
        if (worldSection != null) {
            type = MainUtil.getType(worldSection);
        } else {
            type = PlotAreaType.NORMAL;
        }
        if (type == PlotAreaType.NORMAL) {
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
            PlotManager plotManager = plotArea.getPlotManager();
            PlotSquared.log(Captions.PREFIX + "&aDetected world load for '" + world + "'");
            PlotSquared
                .log(Captions.PREFIX + "&3 - generator: &7" + baseGenerator + ">" + plotGenerator);
            PlotSquared.log(Captions.PREFIX + "&3 - plotworld: &7" + plotArea.getClass().getName());
            PlotSquared.log(
                Captions.PREFIX + "&3 - plotAreaManager: &7" + plotManager.getClass().getName());
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
                PlotSquared.log(Captions.PREFIX + "&aDetected world load for '" + world + "'");
                String gen_string = worldSection.getString("generator.plugin", IMP.getPluginName());
                if (type == PlotAreaType.PARTIAL) {
                    Set<PlotCluster> clusters =
                        this.clusters_tmp != null ? this.clusters_tmp.get(world) : new HashSet<>();
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

                        PlotSquared.log(Captions.PREFIX + "&3 - " + name + "-" + pos1 + "-" + pos2);
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
                        PlotSquared.log(
                            Captions.PREFIX + "&c | &9generator: &7" + baseGenerator + ">"
                                + areaGen);
                        PlotSquared.log(Captions.PREFIX + "&c | &9plotworld: &7" + pa);
                        PlotSquared.log(Captions.PREFIX + "&c | &9manager: &7" + pa);
                        PlotSquared.log(
                            Captions.PREFIX + "&cNote: &7Area created for cluster:" + name
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
                PlotSquared
                    .log(Captions.PREFIX + "&3 - generator: &7" + baseGenerator + ">" + areaGen);
                PlotSquared.log(Captions.PREFIX + "&3 - plotworld: &7" + pa);
                PlotSquared.log(Captions.PREFIX + "&3 - plotAreaManager: &7" + pa.getPlotManager());
                areaGen.getPlotGenerator().initialize(pa);
                areaGen.augment(pa);
                addPlotArea(pa);
                return;
            }
            if (type == PlotAreaType.AUGMENTED) {
                throw new IllegalArgumentException(
                    "Invalid type for multi-area world. Expected `PARTIAL`, got `"
                        + PlotAreaType.AUGMENTED + "`");
            }
            for (String areaId : areasSection.getKeys(false)) {
                PlotSquared.log(Captions.PREFIX + " - " + areaId);
                String[] split = areaId.split("(?<=[^;-])-");
                if (split.length != 3) {
                    throw new IllegalArgumentException("Invalid Area identifier: " + areaId
                        + ". Expected form `<name>-<pos1>-<pos2>`");
                }
                String name = split[0];
                PlotId pos1 = PlotId.fromString(split[1]);
                PlotId pos2 = PlotId.fromString(split[2]);
                if (name.isEmpty()) {
                    throw new IllegalArgumentException("Invalid Area identifier: " + areaId
                        + ". Expected form `<name>-<x1;z1>-<x2;z2>`");
                }
                PlotArea existing = getPlotArea(world, name);
                if (existing != null && name.equals(existing.getId())) {
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
                PlotSquared.log(Captions.PREFIX + "&aDetected area load for '" + world + "'");
                PlotSquared
                    .log(Captions.PREFIX + "&c | &9generator: &7" + baseGenerator + ">" + areaGen);
                PlotSquared.log(Captions.PREFIX + "&c | &9plotworld: &7" + pa);
                PlotSquared.log(Captions.PREFIX + "&c | &9manager: &7" + pa.getPlotManager());
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

            final List<String> validArguments = Arrays
                .asList("s=", "size=", "g=", "gap=", "h=", "height=", "f=", "floor=", "m=", "main=",
                    "w=", "wall=", "b=", "border=");

            // Calculate the number of expected arguments
            int expected = (int) validArguments.stream()
                .filter(validArgument -> args.toLowerCase(Locale.ENGLISH).contains(validArgument))
                .count();

            String[] split = args.toLowerCase(Locale.ENGLISH).split(",(?![^\\(\\[]*[\\]\\)])");

            if (split.length > expected) {
                // This means we have multi-block block buckets
                String[] combinedArgs = new String[expected];
                int index = 0;

                StringBuilder argBuilder = new StringBuilder();
                outer:
                for (final String string : split) {
                    for (final String validArgument : validArguments) {
                        if (string.contains(validArgument)) {
                            if (!argBuilder.toString().isEmpty()) {
                                combinedArgs[index++] = argBuilder.toString();
                                argBuilder = new StringBuilder();
                            }
                            argBuilder.append(string);
                            continue outer;
                        }
                    }
                    if (argBuilder.toString().charAt(argBuilder.length() - 1) != '=') {
                        argBuilder.append(",");
                    }
                    argBuilder.append(string);
                }

                if (!argBuilder.toString().isEmpty()) {
                    combinedArgs[index] = argBuilder.toString();
                }

                split = combinedArgs;
            }

            HybridPlotWorld plotworld = new HybridPlotWorld(world, null, generator, null, null);
            for (String element : split) {
                String[] pair = element.split("=");
                if (pair.length != 2) {
                    PlotSquared.log("&cNo value provided for: &7" + element);
                    return false;
                }
                String key = pair[0].toLowerCase();
                String value = pair[1];
                try {
                    String base = "worlds." + world + ".";
                    switch (key) {
                        case "s":
                        case "size":
                            this.worlds.set(base + "plot.size",
                                ConfigurationUtil.INTEGER.parseString(value).shortValue());
                            break;
                        case "g":
                        case "gap":
                            this.worlds.set(base + "road.width",
                                ConfigurationUtil.INTEGER.parseString(value).shortValue());
                            break;
                        case "h":
                        case "height":
                            this.worlds.set(base + "road.height",
                                ConfigurationUtil.INTEGER.parseString(value).shortValue());
                            this.worlds.set(base + "plot.height",
                                ConfigurationUtil.INTEGER.parseString(value).shortValue());
                            this.worlds.set(base + "wall.height",
                                ConfigurationUtil.INTEGER.parseString(value).shortValue());
                            break;
                        case "f":
                        case "floor":
                            this.worlds.set(base + "plot.floor",
                                ConfigurationUtil.BLOCK_BUCKET.parseString(value).toString());
                            break;
                        case "m":
                        case "main":
                            this.worlds.set(base + "plot.filling",
                                ConfigurationUtil.BLOCK_BUCKET.parseString(value).toString());
                            break;
                        case "w":
                        case "wall":
                            this.worlds.set(base + "wall.filling",
                                ConfigurationUtil.BLOCK_BUCKET.parseString(value).toString());
                            break;
                        case "b":
                        case "border":
                            this.worlds.set(base + "wall.block",
                                ConfigurationUtil.BLOCK_BUCKET.parseString(value).toString());
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
                plotworld.loadDefaultConfiguration(section);
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
        return Arrays.stream(split).map(s -> String.format("%4s", s)).collect(Collectors.joining());
    }

    /**
     * Copies a file from inside the jar to a location
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
                map.put(area.toString(), area.getPlotsMap());
            } else {
                map2.putAll(area.getPlotsMap());
            }
        }
        return map;
    }

    /**
     * Safely closes the database connection.
     */
    public void disable() {
        try {
            eventDispatcher.unregisterAll();
            checkRoadRegenPersistence();
            // Validate that all data in the db is correct
            final HashSet<Plot> plots = new HashSet<>();
            try {
                forEachPlotRaw(plots::add);
            } catch (final Exception ignored) {
            }
            DBFunc.validatePlots(plots);

            // Close the connection
            DBFunc.close();
        } catch (NullPointerException throwable) {
            throwable.printStackTrace();
            PlotSquared.log("&cCould not close database connection!");
        }
    }

    /**
     * Handle road regen persistence
     */
    private void checkRoadRegenPersistence() {
        if (!HybridUtils.UPDATE || !Settings.Enabled_Components.PERSISTENT_ROAD_REGEN || (
            HybridUtils.regions.isEmpty() && HybridUtils.chunks.isEmpty())) {
            return;
        }
        PlotSquared.log(
            Captions.PREFIX + "Road regeneration incomplete. Saving incomplete regions to disk.");
        PlotSquared.debug("   Regions: " + HybridUtils.regions.size());
        PlotSquared.debug("   Chunks: " + HybridUtils.chunks.size());
        ArrayList<int[]> regions = new ArrayList<>();
        ArrayList<int[]> chunks = new ArrayList<>();
        for (BlockVector2 r : HybridUtils.regions) {
            regions.add(new int[] {r.getBlockX(), r.getBlockZ()});
        }
        for (BlockVector2 c : HybridUtils.chunks) {
            chunks.add(new int[] {c.getBlockX(), c.getBlockZ()});
        }
        List<Object> list = new ArrayList<>();
        list.add(regions);
        list.add(chunks);
        list.add(HybridUtils.height);
        File file = new File(
            this.IMP.getDirectory() + File.separator + "persistent_regen_data_" + HybridUtils.area
                .getId() + "_" + HybridUtils.area.getWorldName());
        if (file.exists() && !file.delete()) {
            PlotSquared.log(Captions.PREFIX
                + "persistent_regen_data file already exists and could not be deleted.");
            return;
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(
            Files.newOutputStream(file.toPath(), StandardOpenOption.CREATE_NEW))) {
            oos.writeObject(list);
        } catch (IOException e) {
            PlotSquared.log(Captions.PREFIX + "Error create persistent_regen_data file.");
            e.printStackTrace();
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
                PlotSquared.log(Captions.PREFIX + "&cNo storage type is set!");
                this.IMP.shutdown(); //shutdown used instead of disable because no database is set
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
            PlotSquared.log(Captions.PREFIX
                + "&cFailed to open DATABASE connection. The plugin will disable itself.");
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
            this.IMP.shutdown(); //shutdown used instead of disable because of database error
        }
    }

    /**
     * Setup the default configuration.
     *
     * @throws IOException if the config failed to save
     */
    public void setupConfig() throws IOException {
        String lastVersionString = this.getConfig().getString("version");
        if (lastVersionString != null) {
            String[] split = lastVersionString.split("\\.");
            int[] lastVersion = new int[] {Integer.parseInt(split[0]), Integer.parseInt(split[1]),
                Integer.parseInt(split[2])};
            if (checkVersion(new int[] {3, 4, 0}, lastVersion)) {
                Settings.convertLegacy(configFile);
                if (getConfig().contains("worlds")) {
                    ConfigurationSection worldSection =
                        getConfig().getConfigurationSection("worlds");
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
        //Sets the version information for the settings.yml file
        try (InputStream stream = getClass().getResourceAsStream("/plugin.properties")) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
                String versionString = br.readLine();
                String commitString = br.readLine();
                String dateString = br.readLine();
                this.version = PlotVersion.tryParse(versionString, commitString, dateString);
                System.out.println("Version is " + this.version);
            }
        } catch (IOException throwable) {
            throwable.printStackTrace();
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
    public boolean setupConfigs() {
        File folder = new File(this.IMP.getDirectory(), "config");
        if (!folder.exists() && !folder.mkdirs()) {
            PlotSquared.log(Captions.PREFIX
                + "&cFailed to create the /plugins/config folder. Please create it manually.");
        }
        try {
            this.worldsFile = new File(folder, "worlds.yml");
            if (!this.worldsFile.exists() && !this.worldsFile.createNewFile()) {
                PlotSquared.log(
                    "Could not create the worlds file, please create \"worlds.yml\" manually.");
            }
            this.worlds = YamlConfiguration.loadConfiguration(this.worldsFile);

            if (this.worlds.contains("worlds")) {
                if (!this.worlds.contains("configuration_version") || (
                    !this.worlds.getString("configuration_version")
                        .equalsIgnoreCase(LegacyConverter.CONFIGURATION_VERSION) && !this.worlds
                        .getString("configuration_version").equalsIgnoreCase("v5"))) {
                    // Conversion needed
                    log(Captions.LEGACY_CONFIG_FOUND.getTranslated());
                    try {
                        com.google.common.io.Files
                            .copy(this.worldsFile, new File(folder, "worlds.yml.old"));
                        log(Captions.LEGACY_CONFIG_BACKUP.getTranslated());
                        final ConfigurationSection worlds =
                            this.worlds.getConfigurationSection("worlds");
                        final LegacyConverter converter = new LegacyConverter(worlds);
                        converter.convert();
                        this.worlds.set("worlds", worlds);
                        this.setConfigurationVersion(LegacyConverter.CONFIGURATION_VERSION);
                        log(Captions.LEGACY_CONFIG_DONE.getTranslated());
                    } catch (final Exception e) {
                        log(Captions.LEGACY_CONFIG_CONVERSION_FAILED.getTranslated());
                        e.printStackTrace();
                    }
                    // Disable plugin
                    this.IMP.shutdown();
                    return false;
                }
            } else {
                this.worlds.set("configuration_version", LegacyConverter.CONFIGURATION_VERSION);
            }
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
            PlotSquared.log("Failed to save style.yml");
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
        return true;
    }

    public String getConfigurationVersion() {
        return this.worlds.get("configuration_version", LegacyConverter.CONFIGURATION_VERSION)
            .toString();
    }

    public void setConfigurationVersion(final String newVersion) throws IOException {
        this.worlds.set("configuration_version", newVersion);
        this.worlds.save(this.worldsFile);
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
                PlotSquared.log(Captions.PREFIX + String
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
            this.style.set("Version", this.version.toString());
        }
        this.style.set("Information",
            "Left Row: PlotSquared color codes ($), right row: Minecraft color codes (&)");
        Map<String, Object> object = new HashMap<>(16);
        object.put("color.1", "6");
        object.put("color.2", "7");
        object.put("color.3", "8");
        object.put("color.4", "3");
        object.put("color.5", "1");
        object.put("color.6", "2");
        object.put("color.7", "4");
        object.put("color.8", "5");
        object.put("color.9", "9");
        object.put("color.10", "0");
        object.put("color.11", "a");
        object.put("color.12", "b");
        object.put("color.13", "c");
        object.put("color.14", "d");
        object.put("color.15", "e");
        object.put("color.16", "f");
        if (!this.style.contains("color")
            || this.style.getConfigurationSection("color").getValues(false).size() != object
            .size()) {
            for (Entry<String, Object> node : object.entrySet()) {
                this.style.set(node.getKey(), node.getValue());
            }
        }
        if (this.style.contains("version")) {
            this.style.set("version", null);
        }
    }

    /**
     * Gets the Java version.
     *
     * @return the java version
     */
    private double getJavaVersion() {
        return Double.parseDouble(System.getProperty("java.specification.version"));
    }

    public void forEachPlotArea(Consumer<? super PlotArea> action) {
        for (final PlotArea area : this.plotAreaManager.getAllPlotAreas()) {
            action.accept(area);
        }
    }

    public void forEachPlotArea(@NonNull final String world, Consumer<PlotArea> consumer) {
        final PlotArea[] array = this.plotAreaManager.getPlotAreas(world, null);
        if (array == null) {
            return;
        }
        for (final PlotArea area : array) {
            consumer.accept(area);
        }
    }

    public void forEachPlot(Consumer<Plot> consumer) {
        for (final PlotArea area : this.plotAreaManager.getAllPlotAreas()) {
            area.getPlots().forEach(consumer);
        }
    }

    public void forEachPlotRaw(Consumer<Plot> consumer) {
        for (final PlotArea area : this.plotAreaManager.getAllPlotAreas()) {
            area.getPlots().forEach(consumer);
        }
        if (this.plots_tmp != null) {
            for (final HashMap<PlotId, Plot> entry : this.plots_tmp.values()) {
                entry.values().forEach(consumer);
            }
        }
    }

    public void forEachBasePlot(Consumer<Plot> consumer) {
        for (final PlotArea area : this.plotAreaManager.getAllPlotAreas()) {
            area.forEachBasePlot(consumer);
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
        return Arrays.stream(this.plotAreaManager.getAllPlotAreas())
            .mapToInt(PlotArea::getPlotCount).sum();
    }

    public Set<PlotArea> getPlotAreas() {
        final Set<PlotArea> set = new HashSet<>();
        Collections.addAll(set, plotAreaManager.getAllPlotAreas());
        return Collections.unmodifiableSet(set);
    }

    /**
     * Check if the chunk uses vanilla/non-PlotSquared generation
     *
     * @param world            World name
     * @param chunkCoordinates Chunk coordinates
     * @return True if the chunk uses non-standard generation, false if not
     */
    public boolean isNonStandardGeneration(@NotNull final String world,
        @NotNull final BlockVector2 chunkCoordinates) {
        final Location location = new Location(world, chunkCoordinates.getBlockX() << 4, 64, chunkCoordinates.getBlockZ() << 4);
        final PlotArea area = plotAreaManager.getApplicablePlotArea(location);
        if (area == null) {
            return true;
        }
        return area.getTerrain() != PlotAreaTerrainType.NONE;
    }

    public boolean isAugmented(@NonNull final String world) {
        final PlotArea[] areas = plotAreaManager.getPlotAreas(world, null);
        return areas != null && (areas.length > 1 || areas[0].getType() != PlotAreaType.NORMAL);
    }

    /**
     * Gets a list of PlotArea objects.
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
     * Gets the relevant plot area for a specified location.
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

    public PlotArea getPlotArea(@NonNull final String world, final String id) {
        return plotAreaManager.getPlotArea(world, id);
    }

    /**
     * Gets the {@code PlotArea} which contains a location.
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
                if (area.getWorldName().equalsIgnoreCase(split[0])) {
                    if (area.getId() == null || split.length == 2 && area.getId()
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
                if (StringMan.isEqual(split[1], area.getId())) {
                    return area;
                }
            }
            return null;
        }
    }

    /**
     * Gets Plots based on alias
     *
     * @param alias     to search plots
     * @param worldname to filter alias to a specific world [optional] null means all worlds
     * @return Set&lt;{@link Plot }&gt; empty if nothing found
     */
    public Set<Plot> getPlotsByAlias(@Nullable final String alias,
        @NonNull final String worldname) {
        return PlotQuery.newQuery().inWorld(worldname).withAlias(alias).asSet();
    }

    public Set<PlotArea> getPlotAreas(final String world, final CuboidRegion region) {
        final PlotArea[] areas = plotAreaManager.getPlotAreas(world, region);
        final Set<PlotArea> set = new HashSet<>();
        Collections.addAll(set, areas);
        return Collections.unmodifiableSet(set);
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public enum SortType {
        CREATION_DATE, CREATION_DATE_TIMESTAMP, LAST_MODIFIED, DISTANCE_FROM_ORIGIN
    }
}
