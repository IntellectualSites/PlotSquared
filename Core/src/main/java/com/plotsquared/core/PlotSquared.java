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

import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.ConfigurationUtil;
import com.plotsquared.core.configuration.MemorySection;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.Storage;
import com.plotsquared.core.configuration.caption.CaptionLoader;
import com.plotsquared.core.configuration.caption.CaptionMap;
import com.plotsquared.core.configuration.caption.DummyCaptionMap;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
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
import com.plotsquared.core.inject.factory.HybridPlotWorldFactory;
import com.plotsquared.core.listener.PlotListener;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.plot.BlockBucket;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotCluster;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.plot.expiration.ExpireManager;
import com.plotsquared.core.plot.expiration.ExpiryTask;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.FileUtils;
import com.plotsquared.core.util.LegacyConverter;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.ReflectionUtils;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.uuid.UUIDPipeline;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * An implementation of the core, with a static getter for easy access.
 */
@SuppressWarnings({"WeakerAccess"})
public class PlotSquared {

    private static final Logger logger = LoggerFactory.getLogger("P2/" + PlotSquared.class.getSimpleName());
    private static PlotSquared instance;

    // Implementation
    private final PlotPlatform<?> platform;
    // Current thread
    private final Thread thread;
    // UUID pipelines
    private final UUIDPipeline impromptuUUIDPipeline =
        new UUIDPipeline(Executors.newCachedThreadPool());
    private final UUIDPipeline backgroundUUIDPipeline =
        new UUIDPipeline(Executors.newSingleThreadExecutor());
    // WorldEdit instance
    private WorldEdit worldedit;
    private File configFile;
    private File worldsFile;
    private YamlConfiguration worldConfiguration;
    // Temporary hold the plots/clusters before the worlds load
    private HashMap<String, Set<PlotCluster>> clustersTmp;
    public HashMap<String, HashMap<PlotId, Plot>> plots_tmp;
    private YamlConfiguration config;
    // Localization
    private Map<String, CaptionMap> captionMaps;
    // Platform / Version / Update URL
    private PlotVersion version;
    // Files and configuration
    private File jarFile = null; // This file
    private File storageFile;
    private EventDispatcher eventDispatcher;
    private PlotListener plotListener;

    /**
     * Initialize PlotSquared with the desired Implementation class.
     *
     * @param iPlotMain Implementation of {@link PlotPlatform} used
     * @param platform  The platform being used
     */
    public PlotSquared(final PlotPlatform<?> iPlotMain, final String platform) {
        if (instance != null) {
            throw new IllegalStateException("Cannot re-initialize the PlotSquared singleton");
        }
        instance = this;

        this.thread = Thread.currentThread();
        this.platform = iPlotMain;
        Settings.PLATFORM = platform;

        // Initialize the class
        PlayerMetaDataKeys.load();

        //
        // Register configuration serializable classes
        //
        ConfigurationSerialization.registerClass(BlockBucket.class, "BlockBucket");

        try {
            new ReflectionUtils(this.platform.getNMSPackage());
            try {
                URL logurl = PlotSquared.class.getProtectionDomain().getCodeSource().getLocation();
                this.jarFile = new File(
                    new URL(logurl.toURI().toString().split("\\!")[0].replaceAll("jar:file", "file"))
                        .toURI().getPath());
            } catch (MalformedURLException | URISyntaxException | SecurityException e) {
                e.printStackTrace();
                this.jarFile = new File(this.platform.getDirectory().getParentFile(), "PlotSquared.jar");
                if (!this.jarFile.exists()) {
                    this.jarFile = new File(this.platform.getDirectory().getParentFile(),
                        "PlotSquared-" + platform + ".jar");
                }
            }

            if (!setupConfigs()) {
                return;
            }

            this.captionMaps = new HashMap<>();

            // Setup localization
            CaptionMap captionMap;
            if (Settings.Enabled_Components.PER_USER_LOCALE) {
                captionMap = CaptionLoader.loadAll(Paths.get("lang"));
            } else {
                String fileName = "messages_" + Settings.Enabled_Components.DEFAULT_LOCALE + ".json";
                captionMap = CaptionLoader.loadSingle(Paths.get("lang", fileName));
            }

            this.captionMaps.put(TranslatableCaption.DEFAULT_NAMESPACE, captionMap);
            this.worldedit = WorldEdit.getInstance();

            // Create Event utility class
            this.eventDispatcher = new EventDispatcher(this.worldedit);
            // Create plot listener
            this.plotListener = new PlotListener(this.eventDispatcher);

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
    }

    /**
     * Get the platform specific {@link PlotAreaManager} instance
     *
     * @return Plot area manager
     */
    @Nonnull public PlotAreaManager getPlotAreaManager() {
        return this.platform.getInjector().getInstance(PlotAreaManager.class);
    }

    /**
     * Gets an instance of PlotSquared.
     *
     * @return instance of PlotSquared
     */
    public static PlotSquared get() {
        return PlotSquared.instance;
    }

    /**
     * Get the platform specific implementation of PlotSquared
     *
     * @return Platform implementation
     */
    @Nonnull public static PlotPlatform<?> platform() {
        if (instance != null && instance.platform != null) {
            return instance.platform;
        }
        throw new IllegalStateException("Plot platform implementation is missing");
    }

    public void startExpiryTasks() {
        if (Settings.Enabled_Components.PLOT_EXPIRY) {
            ExpireManager.IMP = new ExpireManager(this.eventDispatcher);
            ExpireManager.IMP.runAutomatedTask();
            for (Settings.Auto_Clear settings : Settings.AUTO_CLEAR.getInstances()) {
                ExpiryTask task = new ExpiryTask(settings, this.getPlotAreaManager());
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
        if (clustersTmp == null || (clusters = clustersTmp.remove(plotArea.toString())) == null) {
            if (plotArea.getType() == PlotAreaType.PARTIAL) {
                clusters = this.clustersTmp != null ?
                    this.clustersTmp.get(plotArea.getWorldName()) :
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
        getPlotAreaManager().addPlotArea(plotArea);
        plotArea.setupBorder();
        if (!Settings.Enabled_Components.PERSISTENT_ROAD_REGEN) {
            return;
        }
        File file = new File(
            this.platform.getDirectory() + File.separator + "persistent_regen_data_" + plotArea.getId()
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
                logger.info("[P2] Incomplete road regeneration found. Restarting in world {} with height {}", plotArea.getWorldName(), height);
                logger.info("[P2]  - Regions: {}", regions.size());
                logger.info("[P2]  - Chunks: {}", chunks.size());
                HybridUtils.UPDATE = true;
                PlotSquared.platform().getHybridUtils().scheduleRoadUpdate(plotArea, regions, height, chunks);
            } catch (IOException | ClassNotFoundException e) {
                logger.error("[P2] Error restarting road regeneration", e);
            } finally {
                if (!file.delete()) {
                    logger.error("[P2] Error deleting persistent_regen_data_{}. Please delete this file manually", plotArea.getId());
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
        getPlotAreaManager().removePlotArea(area);
        setPlotsTmp(area);
    }

    public void removePlotAreas(@Nonnull final String world) {
        for (final PlotArea area : this.getPlotAreaManager().getPlotAreasSet(world)) {
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
        if (this.clustersTmp == null) {
            this.clustersTmp = new HashMap<>();
        }
        this.clustersTmp.put(area.toString(), area.getClusters());
    }

    public Set<PlotCluster> getClusters(@Nonnull final String world) {
        final Set<PlotCluster> set = new HashSet<>();
        for (final PlotArea area : this.getPlotAreaManager().getPlotAreasSet(world)) {
            set.addAll(area.getClusters());
        }
        return Collections.unmodifiableSet(set);

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
            } else if (Math.abs(plot.getId().getX()) > 15446 || Math.abs(plot.getId().getY()) > 15446) {
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
            } else if (Math.abs(plot.getId().getX()) > 15446 || Math.abs(plot.getId().getY()) > 15446) {
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
        int totalSize = Arrays.stream(this.getPlotAreaManager().getAllPlotAreas()).mapToInt(PlotArea::getPlotCount).sum();
        if (plots.size() == totalSize) {
            for (PlotArea area : getPlotAreaManager().getAllPlotAreas()) {
                map.put(area, area.getPlots());
            }
        } else {
            for (PlotArea area : getPlotAreaManager().getAllPlotAreas()) {
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
        List<PlotArea> areas = Arrays.asList(getPlotAreaManager().getAllPlotAreas());
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

    public void setPlots(@Nonnull final Map<String, HashMap<PlotId, Plot>> plots) {
        if (this.plots_tmp == null) {
            this.plots_tmp = new HashMap<>();
        }
        for (final Entry<String, HashMap<PlotId, Plot>> entry : plots.entrySet()) {
            final String world = entry.getKey();
            final PlotArea plotArea = this.getPlotAreaManager().getPlotArea(world, null);
            if (plotArea == null) {
                Map<PlotId, Plot> map = this.plots_tmp.computeIfAbsent(world, k -> new HashMap<>());
                map.putAll(entry.getValue());
            } else {
                for (Plot plot : entry.getValue().values()) {
                    plot.setArea(plotArea);
                    plotArea.addPlot(plot);
                }
            }
        }
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
            int last_max = Math.max(Math.abs(last.getX()), Math.abs(last.getY()));
            int this_max = Math.max(Math.abs(plot.getId().getX()), Math.abs(plot.getId().getY()));
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
        this.getPlotAreaManager().addWorld(world);
        Set<String> worlds;
        if (this.worldConfiguration.contains("worlds")) {
            worlds = this.worldConfiguration.getConfigurationSection("worlds").getKeys(false);
        } else {
            worlds = new HashSet<>();
        }
        String path = "worlds." + world;
        ConfigurationSection worldSection = this.worldConfiguration.getConfigurationSection(path);
        PlotAreaType type;
        if (worldSection != null) {
            type = ConfigurationUtil.getType(worldSection);
        } else {
            type = PlotAreaType.NORMAL;
        }
        if (type == PlotAreaType.NORMAL) {
            if (getPlotAreaManager().getPlotAreas(world, null).length != 0) {
                return;
            }
            IndependentPlotGenerator plotGenerator;
            if (baseGenerator != null && baseGenerator.isFull()) {
                plotGenerator = baseGenerator.getPlotGenerator();
            } else if (worldSection != null) {
                String secondaryGeneratorName = worldSection.getString("generator.plugin");
                GeneratorWrapper<?> secondaryGenerator =
                    this.platform.getGenerator(world, secondaryGeneratorName);
                if (secondaryGenerator != null && secondaryGenerator.isFull()) {
                    plotGenerator = secondaryGenerator.getPlotGenerator();
                } else {
                    String primaryGeneratorName = worldSection.getString("generator.init");
                    GeneratorWrapper<?> primaryGenerator =
                        this.platform.getGenerator(world, primaryGeneratorName);
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
            logger.info("[P2] Detected world load for '{}'", world);
            logger.info("[P2]  - generator: {}>{}", baseGenerator, plotGenerator);
            logger.info("[P2]  - plot world: {}", plotArea.getClass().getCanonicalName());
            logger.info("[P2] - plot area manager: {}", plotManager.getClass().getCanonicalName());
            if (!this.worldConfiguration.contains(path)) {
                this.worldConfiguration.createSection(path);
                worldSection = this.worldConfiguration.getConfigurationSection(path);
            }
            plotArea.saveConfiguration(worldSection);
            plotArea.loadDefaultConfiguration(worldSection);
            try {
                this.worldConfiguration.save(this.worldsFile);
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
                if (getPlotAreaManager().getPlotAreas(world, null).length != 0) {
                    return;
                }
                logger.info("[P2] Detected world load for '{}'", world);
                String gen_string = worldSection.getString("generator.plugin", platform.getPluginName());
                if (type == PlotAreaType.PARTIAL) {
                    Set<PlotCluster> clusters =
                        this.clustersTmp != null ? this.clustersTmp.get(world) : new HashSet<>();
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
                        logger.info("[P2]  - {}-{}-{}", name, pos1, pos2);
                        GeneratorWrapper<?> areaGen = this.platform.getGenerator(world, gen_string);
                        if (areaGen == null) {
                            throw new IllegalArgumentException("Invalid Generator: " + gen_string);
                        }
                        PlotArea pa =
                            areaGen.getPlotGenerator().getNewPlotArea(world, name, pos1, pos2);
                        pa.saveConfiguration(worldSection);
                        pa.loadDefaultConfiguration(worldSection);
                        try {
                            this.worldConfiguration.save(this.worldsFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        logger.info("[P2]  | generator: {}>{}", baseGenerator, areaGen);
                        logger.info("[P2]  | plot world: {}", pa);
                        logger.info("[P2]  | manager: {}", pa);
                        logger.info("[P2] Note: Area created for cluster '{}' (invalid or old configuration?)", name);
                        areaGen.getPlotGenerator().initialize(pa);
                        areaGen.augment(pa);
                        toLoad.add(pa);
                    }
                    for (PlotArea area : toLoad) {
                        addPlotArea(area);
                    }
                    return;
                }
                GeneratorWrapper<?> areaGen = this.platform.getGenerator(world, gen_string);
                if (areaGen == null) {
                    throw new IllegalArgumentException("Invalid Generator: " + gen_string);
                }
                PlotArea pa = areaGen.getPlotGenerator().getNewPlotArea(world, null, null, null);
                pa.saveConfiguration(worldSection);
                pa.loadDefaultConfiguration(worldSection);
                try {
                    this.worldConfiguration.save(this.worldsFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                logger.info("[P2]  - generator: {}>{}", baseGenerator, areaGen);
                logger.info("[P2]  - plot world: {}", pa);
                logger.info("[P2]  - plot area manager: {}", pa.getPlotManager());
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
                logger.info("[P2]  - {}", areaId);
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
                final PlotArea existing = this.getPlotAreaManager().getPlotArea(world, name);
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
                String gen_string = clone.getString("generator.plugin", platform.getPluginName());
                GeneratorWrapper<?> areaGen = this.platform.getGenerator(world, gen_string);
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
                    this.worldConfiguration.save(this.worldsFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                logger.info("[P2] Detected area load for '{}'", world);
                logger.info("[P2]  | generator: {}>{}", baseGenerator, areaGen);
                logger.info("[P2]  | plot world: {}", pa);
                logger.info("[P2]  | manager: {}", pa.getPlotManager());
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

            final HybridPlotWorldFactory hybridPlotWorldFactory = this.platform.getInjector().getInstance(HybridPlotWorldFactory.class);
            final HybridPlotWorld plotWorld = hybridPlotWorldFactory.create(world, null, generator, null, null);

            for (String element : split) {
                String[] pair = element.split("=");
                if (pair.length != 2) {
                    logger.error("[P2] No value provided for '{}'", element);
                    return false;
                }
                String key = pair[0].toLowerCase();
                String value = pair[1];
                try {
                    String base = "worlds." + world + ".";
                    switch (key) {
                        case "s":
                        case "size":
                            this.worldConfiguration.set(base + "plot.size",
                                ConfigurationUtil.INTEGER.parseString(value).shortValue());
                            break;
                        case "g":
                        case "gap":
                            this.worldConfiguration.set(base + "road.width",
                                ConfigurationUtil.INTEGER.parseString(value).shortValue());
                            break;
                        case "h":
                        case "height":
                            this.worldConfiguration.set(base + "road.height",
                                ConfigurationUtil.INTEGER.parseString(value).shortValue());
                            this.worldConfiguration.set(base + "plot.height",
                                ConfigurationUtil.INTEGER.parseString(value).shortValue());
                            this.worldConfiguration.set(base + "wall.height",
                                ConfigurationUtil.INTEGER.parseString(value).shortValue());
                            break;
                        case "f":
                        case "floor":
                            this.worldConfiguration.set(base + "plot.floor",
                                ConfigurationUtil.BLOCK_BUCKET.parseString(value).toString());
                            break;
                        case "m":
                        case "main":
                            this.worldConfiguration.set(base + "plot.filling",
                                ConfigurationUtil.BLOCK_BUCKET.parseString(value).toString());
                            break;
                        case "w":
                        case "wall":
                            this.worldConfiguration.set(base + "wall.filling",
                                ConfigurationUtil.BLOCK_BUCKET.parseString(value).toString());
                            break;
                        case "b":
                        case "border":
                            this.worldConfiguration.set(base + "wall.block",
                                ConfigurationUtil.BLOCK_BUCKET.parseString(value).toString());
                            break;
                        default:
                            logger.error("[P2] Key not found: {}", element);
                            return false;
                    }
                } catch (Exception e) {
                    logger.error("[P2] Invalid value '{}' for arg '{}'", value, element);
                    e.printStackTrace();
                    return false;
                }
            }
            try {
                ConfigurationSection section =
                    this.worldConfiguration.getConfigurationSection("worlds." + world);
                plotWorld.saveConfiguration(section);
                plotWorld.loadDefaultConfiguration(section);
                this.worldConfiguration.save(this.worldsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * Copies a file from inside the jar to a location
     *
     * @param file   Name of the file inside PlotSquared.jar
     * @param folder The output location relative to /plugins/PlotSquared/
     */
    public void copyFile(String file, String folder) {
        try {
            File output = this.platform.getDirectory();
            if (!output.exists()) {
                output.mkdirs();
            }
            File newFile = FileUtils.getFile(output, folder + File.separator + file);
            if (newFile.exists()) {
                return;
            }
            try (InputStream stream = this.platform.getClass().getResourceAsStream(file)) {
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
            logger.error("[P2] Could not save {}", file);
            e.printStackTrace();
        }
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
            logger.error("[P2] Could not close database connection", throwable);
            throwable.printStackTrace();
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
        logger.info("[P2] Road regeneration incomplete. Saving incomplete regions to disk");
        logger.info("[P2]  - regions: {}", HybridUtils.regions.size());
        logger.info("[P2]  - chunks: {}", HybridUtils.chunks.size());
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
            this.platform.getDirectory() + File.separator + "persistent_regen_data_" + HybridUtils.area
                .getId() + "_" + HybridUtils.area.getWorldName());
        if (file.exists() && !file.delete()) {
            logger.error("[P2] persistent_regene_data file already exists and could not be deleted");
            return;
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(
            Files.newOutputStream(file.toPath(), StandardOpenOption.CREATE_NEW))) {
            oos.writeObject(list);
        } catch (IOException e) {
            logger.error("[P2] Error creating persistent_region_data file", e);
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
                File file = FileUtils.getFile(platform.getDirectory(), Storage.SQLite.DB + ".db");
                database = new SQLite(file);
            } else {
                logger.error("[P2] No storage type is set. Disabling PlotSquared");
                this.platform.shutdown(); //shutdown used instead of disable because no database is set
                return;
            }
            DBFunc.dbManager = new SQLManager(database, Storage.PREFIX, this.eventDispatcher, this.plotListener, this.worldConfiguration);
            this.plots_tmp = DBFunc.getPlots();
            if (getPlotAreaManager() instanceof SinglePlotAreaManager) {
                SinglePlotArea area = ((SinglePlotAreaManager) getPlotAreaManager()).getArea();
                addPlotArea(area);
                ConfigurationSection section = worldConfiguration.getConfigurationSection("worlds.*");
                if (section == null) {
                    section = worldConfiguration.createSection("worlds.*");
                }
                area.saveConfiguration(section);
                area.loadDefaultConfiguration(section);
            }
            this.clustersTmp = DBFunc.getClusters();
        } catch (ClassNotFoundException | SQLException e) {
            logger.error("[P2] Failed to open database connection ({}). Disabling PlotSquared", Storage.MySQL.USE ? "MySQL" : "SQLite");
            logger.error("[P2] ==== Here is an ugly stacktrace, if you are interested in those things ===");
            e.printStackTrace();
            logger.error("[P2] &d==== End of stacktrace ====");
            logger.error("[P2] &6Please go to the {} 'storage.yml' and configure the database correctly",
                platform.getPluginName());
            this.platform.shutdown(); //shutdown used instead of disable because of database error
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
                    worldConfiguration.set("worlds", worldSection);
                    try {
                        worldConfiguration.save(worldsFile);
                    } catch (IOException e) {
                        logger.error("[P2] Failed to save worlds.yml", e);
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
        File folder = new File(this.platform.getDirectory(), "config");
        if (!folder.exists() && !folder.mkdirs()) {
            logger.error("[P2] Failed to create the /plugins/config folder. Please create it manually");
        }
        try {
            this.worldsFile = new File(folder, "worlds.yml");
            if (!this.worldsFile.exists() && !this.worldsFile.createNewFile()) {
                logger.error("[P2] Could not create the worlds file. Please create 'worlds.yml' manually");
            }
            this.worldConfiguration = YamlConfiguration.loadConfiguration(this.worldsFile);

            if (this.worldConfiguration.contains("worlds")) {
                if (!this.worldConfiguration.contains("configuration_version") || (
                    !this.worldConfiguration.getString("configuration_version")
                        .equalsIgnoreCase(LegacyConverter.CONFIGURATION_VERSION) && !this.worldConfiguration
                        .getString("configuration_version").equalsIgnoreCase("v5"))) {
                    // Conversion needed
                    logger.info("[P2] &aA legacy configuration file was detected. Conversion will be attempted.");
                    try {
                        com.google.common.io.Files
                            .copy(this.worldsFile, new File(folder, "worlds.yml.old"));
                        logger.info("[P2] &6A copy of worlds.yml has been saved in the file worlds.yml.old");
                        final ConfigurationSection worlds =
                            this.worldConfiguration.getConfigurationSection("worlds");
                        final LegacyConverter converter = new LegacyConverter(worlds);
                        converter.convert();
                        this.worldConfiguration.set("worlds", worlds);
                        this.setConfigurationVersion(LegacyConverter.CONFIGURATION_VERSION);
                        logger.info("[P2] &aThe conversion has finished. PlotSquared will now be disabled and the new configuration file will be used at next startup. Please review the new worlds.yml file. Please note that schematics will not be converted, as we are now using WorldEdit to handle schematics. You need to re-generate the schematics.");
                    } catch (final Exception e) {
                        logger.error("[P2] &cFailed to convert the legacy configuration file. See stack trace for information.", e);
                    }
                    // Disable plugin
                    this.platform.shutdown();
                    return false;
                }
            } else {
                this.worldConfiguration.set("configuration_version", LegacyConverter.CONFIGURATION_VERSION);
            }
        } catch (IOException ignored) {
            logger.error("[P2] Failed to save worlds.yml");
        }
        try {
            this.configFile = new File(folder, "settings.yml");
            if (!this.configFile.exists() && !this.configFile.createNewFile()) {
                logger.error("[P2] Could not create the settings file. Please create 'settings.yml' manually");
            }
            this.config = YamlConfiguration.loadConfiguration(this.configFile);
            setupConfig();
        } catch (IOException ignored) {
            logger.error("[P2] Failed to save settings.yml");
        }
        try {
            this.storageFile = new File(folder, "storage.yml");
            if (!this.storageFile.exists() && !this.storageFile.createNewFile()) {
                logger.error("[P2] Could not create the storage settings file. Please create 'storage.yml' manually");
            }
            YamlConfiguration.loadConfiguration(this.storageFile);
            setupStorage();
        } catch (IOException ignored) {
            logger.error("[P2] Failed to save storage.yml");
        }
        return true;
    }

    public String getConfigurationVersion() {
        return this.worldConfiguration.get("configuration_version", LegacyConverter.CONFIGURATION_VERSION)
            .toString();
    }

    public void setConfigurationVersion(final String newVersion) throws IOException {
        this.worldConfiguration.set("configuration_version", newVersion);
        this.worldConfiguration.save(this.worldsFile);
    }

    /**
     * Setup the storage file (load + save missing nodes).
     */
    private void setupStorage() {
        Storage.load(storageFile);
        Storage.save(storageFile);
        YamlConfiguration.loadConfiguration(storageFile);
    }

    /**
     * Show startup debug information.
     */
    private void showDebug() {
        if (Settings.DEBUG) {
            Map<String, Object> components = Settings.getFields(Settings.Enabled_Components.class);
            for (Entry<String, Object> component : components.entrySet()) {
                logger.info("[P2] Key: {} | Value: {}", component.getKey(), component.getValue());
            }
        }
    }

    public void forEachPlotRaw(Consumer<Plot> consumer) {
        for (final PlotArea area : this.getPlotAreaManager().getAllPlotAreas()) {
            area.getPlots().forEach(consumer);
        }
        if (this.plots_tmp != null) {
            for (final HashMap<PlotId, Plot> entry : this.plots_tmp.values()) {
                entry.values().forEach(consumer);
            }
        }
    }

    /**
     * Check if the chunk uses vanilla/non-PlotSquared generation
     *
     * @param world            World name
     * @param chunkCoordinates Chunk coordinates
     * @return True if the chunk uses non-standard generation, false if not
     */
    public boolean isNonStandardGeneration(@Nonnull final String world,
        @Nonnull final BlockVector2 chunkCoordinates) {
        final Location location = Location.at(world, chunkCoordinates.getBlockX() << 4, 64, chunkCoordinates.getBlockZ() << 4);
        final PlotArea area = getPlotAreaManager().getApplicablePlotArea(location);
        if (area == null) {
            return true;
        }
        return area.getTerrain() != PlotAreaTerrainType.NONE;
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public UUIDPipeline getImpromptuUUIDPipeline() {
        return this.impromptuUUIDPipeline;
    }

    public UUIDPipeline getBackgroundUUIDPipeline() {
        return this.backgroundUUIDPipeline;
    }

    public WorldEdit getWorldedit() {
        return this.worldedit;
    }

    public File getConfigFile() {
        return this.configFile;
    }

    public File getWorldsFile() {
        return this.worldsFile;
    }

    public YamlConfiguration getWorldConfiguration() {
        return this.worldConfiguration;
    }

    /**
     * Get the caption map belonging to a namespace. If none exists, a dummy
     * caption map will be returned.
     *
     * @param namespace Namespace
     * @return Map instance
     * @see #registerCaptionMap(String, CaptionMap) To register a caption map
     */
    @Nonnull public CaptionMap getCaptionMap(@Nonnull final String namespace) {
        return this.captionMaps.computeIfAbsent(namespace.toLowerCase(Locale.ENGLISH),
            missingNamespace -> new DummyCaptionMap());
    }

    /**
     * Register a caption map
     *
     * @param namespace Namespace
     * @param captionMap Map instance
     */
    public void registerCaptionMap(@Nonnull final String namespace, @Nonnull final CaptionMap captionMap) {
        if (namespace.equalsIgnoreCase(TranslatableCaption.DEFAULT_NAMESPACE)) {
            throw new IllegalArgumentException("Cannot replace default caption map");
        }
        this.captionMaps.put(namespace.toLowerCase(Locale.ENGLISH), captionMap);
    }

    public File getJarFile() {
        return this.jarFile;
    }

    public EventDispatcher getEventDispatcher() {
        return this.eventDispatcher;
    }

    public PlotListener getPlotListener() {
        return this.plotListener;
    }

    public enum SortType {
        CREATION_DATE, CREATION_DATE_TIMESTAMP, LAST_MODIFIED, DISTANCE_FROM_ORIGIN
    }
}
