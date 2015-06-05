package com.intellectualcrafters.plot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.file.YamlConfiguration;

import com.intellectualcrafters.plot.commands.Cluster;
import com.intellectualcrafters.plot.commands.MainCommand;
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
import com.intellectualcrafters.plot.generator.AugmentedPopulator;
import com.intellectualcrafters.plot.generator.ClassicPlotWorld;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.generator.SquarePlotManager;
import com.intellectualcrafters.plot.generator.SquarePlotWorld;
import com.intellectualcrafters.plot.listeners.APlotListener;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotHandler;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.comment.CommentManager;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.ClusterManager;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.Logger;
import com.intellectualcrafters.plot.util.Logger.LogLevel;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlayerManager;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class PlotSquared {
    public static final String MAIN_PERMISSION = "plots.use";
    public static final String ADMIN_PERMISSION = "plots.admin";
    public static File styleFile;
    public static YamlConfiguration style;
    public static File configFile;
    public static YamlConfiguration config;
    public static File storageFile;
    public static YamlConfiguration storage;
    public static PlotSquared THIS = null; // This class
    public static File FILE = null; // This file
    public static IPlotMain IMP = null; // Specific implementation of PlotSquared
    public static String VERSION = null;
    public static TaskManager TASK = null;
    private static boolean LOADING_WORLD = false;
    public static Economy economy = null;
    public static WorldEditPlugin worldEdit = null;
    private final static HashMap<String, PlotWorld> plotworlds = new HashMap<>();
    private final static HashMap<String, PlotManager> plotmanagers = new HashMap<>();
    
    
    private static LinkedHashMap<String, HashMap<PlotId, Plot>> plots;
    
    
    private static Database database;
    public static Connection connection;
    
    public static Database getDatabase() {
        return database;
    }

    public static void updatePlot(final Plot plot) {
        final String world = plot.world;
        if (!plots.containsKey(world)) {
            plots.put(world, new HashMap<PlotId, Plot>());
        }
        plot.hasChanged = true;
        plots.get(world).put(plot.id, plot);
    }

    public static PlotWorld getPlotWorld(final String world) {
        if (plotworlds.containsKey(world)) {
            return plotworlds.get(world);
        }
        return null;
    }

    public static void addPlotWorld(final String world, final PlotWorld plotworld, final PlotManager manager) {
        plotworlds.put(world, plotworld);
        plotmanagers.put(world, manager);
        if (!plots.containsKey(world)) {
            plots.put(world, new HashMap<PlotId, Plot>());
        }
    }

    public static void removePlotWorld(final String world) {
        plots.remove(world);
        plotmanagers.remove(world);
        plotworlds.remove(world);
    }

    public static HashMap<String, HashMap<PlotId, Plot>> getAllPlotsRaw() {
        return plots;
    }

    public static void setAllPlotsRaw(final LinkedHashMap<String, HashMap<PlotId, Plot>> plots) {
        PlotSquared.plots = plots;
    }

    public static Set<Plot> getPlots() {
        final ArrayList<Plot> newplots = new ArrayList<>();
        for (final Entry<String, HashMap<PlotId, Plot>> entry : plots.entrySet()) {
            if (isPlotWorld(entry.getKey())) {
                newplots.addAll(entry.getValue().values());
            }
        }
        return new LinkedHashSet<>(newplots);
    }
    
    public static Set<Plot> getPlotsRaw() {
        final ArrayList<Plot> newplots = new ArrayList<>();
        for (final Entry<String, HashMap<PlotId, Plot>> entry : plots.entrySet()) {
            newplots.addAll(entry.getValue().values());
        }
        return new LinkedHashSet<>(newplots);
    }
    
    public static ArrayList<Plot> sortPlots(Collection<Plot> plots) {
        ArrayList<Plot> newPlots = new ArrayList<>();
        newPlots.addAll(plots);
        Collections.sort(newPlots, new Comparator<Plot>() {
            @Override
            public int compare(Plot p1, Plot p2) {
                int h1 = p1.hashCode();
                int h2 = p2.hashCode();
                if (h1 < 0) {
                    h1 = -h1*2 - 1;
                }
                else {
                    h1*=2;
                }
                if (h2 < 0) {
                    h2 = -h2*2 - 1;
                }
                else {
                    h2*=2;
                }
                return h1-h2;
            }
        });
        return newPlots;
    }
    
    public static ArrayList<Plot> sortPlots(Collection<Plot> plots, final String priorityWorld) {
        ArrayList<Plot> newPlots = new ArrayList<>();
        HashMap<PlotId, Plot> worldPlots = PlotSquared.plots.get(priorityWorld);
        if (worldPlots != null) {
            for (Plot plot : sortPlots(worldPlots.values())) {
                if (plots.contains(plot)) {
                    newPlots.add(plot);
                }
            }
        }
        ArrayList<String> worlds = new ArrayList<>(PlotSquared.plots.keySet());
        Collections.sort(worlds);
        for (String world : worlds) {
            if (!world.equals(priorityWorld)) {
                for (Plot plot : PlotSquared.plots.get(world).values()) {
                    if (plots.contains(plot)) {
                        newPlots.add(plot);
                    }
                }
            }
        }
        return newPlots;
    }
    
    public static ArrayList<Plot> sortPlotsByWorld(Collection<Plot> plots) {
        ArrayList<Plot> newPlots = new ArrayList<>();
        ArrayList<String> worlds = new ArrayList<>(PlotSquared.plots.keySet());
        Collections.sort(worlds);
        for (String world : worlds) {
            for (Plot plot : PlotSquared.plots.get(world).values()) {
                if (plots.contains(plot)) {
                    newPlots.add(plot);
                }
            }
        }
        return newPlots;
    }

    public static Set<Plot> getPlots(final String world, final String player) {
        final UUID uuid = UUIDHandler.getUUID(player);
        return getPlots(world, uuid);
    }

    public static Set<Plot> getPlots(final String world, final PlotPlayer player) {
        final UUID uuid = player.getUUID();
        return getPlots(world, uuid);
    }

    public static Set<Plot> getPlots(final String world, final UUID uuid) {
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

    public static boolean isPlotWorld(final String world) {
        return (plotworlds.containsKey(world));
    }

    public static PlotManager getPlotManager(final String world) {
        if (plotmanagers.containsKey(world)) {
            return plotmanagers.get(world);
        }
        return null;
    }

    public static String[] getPlotWorldsString() {
        final Set<String> strings = plots.keySet();
        return strings.toArray(new String[strings.size()]);
    }

    public static HashMap<PlotId, Plot> getPlots(final String world) {
        if (plots.containsKey(world)) {
            return plots.get(world);
        }
        return new HashMap<>();
    }

    public static Set<Plot> getPlots(final PlotPlayer player) {
        return getPlots(player.getUUID());
    }
    
    public static Set<Plot> getPlots(final UUID uuid) {
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

    public static boolean removePlot(final String world, final PlotId id, final boolean callEvent) {
        if (callEvent) {
            EventUtil.manager.callDelete(world, id);
        }
        plots.get(world).remove(id);
        if (MainUtil.lastPlot.containsKey(world)) {
            final PlotId last = MainUtil.lastPlot.get(world);
            final int last_max = Math.max(last.x, last.y);
            final int this_max = Math.max(id.x, id.y);
            if (this_max < last_max) {
                MainUtil.lastPlot.put(world, id);
            }
        }
        return true;
    }

    public static void loadWorld(final String world, PlotGenerator generator) {
        PlotWorld plotWorld = getPlotWorld(world); 
        if (plotWorld != null) {
            if (generator != null) {
                generator.init(plotWorld);
            }
            return;
        }
        final Set<String> worlds = (config.contains("worlds") ? config.getConfigurationSection("worlds").getKeys(false) : new HashSet<String>());
        final PlotGenerator plotGenerator;
        final PlotManager plotManager;
        final String path = "worlds." + world;
        if (!LOADING_WORLD && (generator != null) && (generator instanceof PlotGenerator)) {
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
            generator.init(plotWorld);
            MainUtil.setupBorder(world);
        } else {
            if (!worlds.contains(world)) {
                return;
            }
            if (!LOADING_WORLD) {
                LOADING_WORLD = true;
                try {
                    final String gen_string = config.getString("worlds." + world + "." + "generator.plugin");
                    if (gen_string == null) {
                        generator = new HybridGen(world);
                    } else {
                        generator = (PlotGenerator) IMP.getGenerator(world, gen_string);
                    }
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
                final PlotGenerator gen_class = generator;
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
                
                if (((plotWorld.TYPE == 2) && !Settings.ENABLE_CLUSTERS) || !(plotManager instanceof SquarePlotManager)) {
                    log("&c[ERROR] World '" + world + "' in settings.yml is not using PlotSquared generator! Please set the generator correctly or delete the world from the 'settings.yml'!");
                    return;
                }
                addPlotWorld(world, plotWorld, plotManager);
                if (plotWorld.TYPE == 2) {
                    if (ClusterManager.getClusters(world).size() > 0) {
                        for (final PlotCluster cluster : ClusterManager.getClusters(world)) {
                            new AugmentedPopulator(world, gen_class, cluster, plotWorld.TERRAIN == 2, plotWorld.TERRAIN != 2);
                        }
                    }
                } else if (plotWorld.TYPE == 1) {
                    new AugmentedPopulator(world, gen_class, null, plotWorld.TERRAIN == 2, plotWorld.TERRAIN != 2);
                }
                gen_class.init(plotWorld);
            }
        }
    }

    public static boolean setupPlotWorld(final String world, final String id) {
        if ((id != null) && (id.length() > 0)) {
            // save configuration
            final String[] split = id.split(",");
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
                            SquarePlotWorld.PLOT_WIDTH_DEFAULT = ((Integer) Configuration.INTEGER.parseString(value)).shortValue();
                            break;
                        }
                        case "g":
                        case "gap": {
                            SquarePlotWorld.ROAD_WIDTH_DEFAULT = ((Integer) Configuration.INTEGER.parseString(value)).shortValue();
                            break;
                        }
                        case "h":
                        case "height": {
                            ClassicPlotWorld.PLOT_HEIGHT_DEFAULT = (Integer) Configuration.INTEGER.parseString(value);
                            ClassicPlotWorld.ROAD_HEIGHT_DEFAULT = (Integer) Configuration.INTEGER.parseString(value);
                            ClassicPlotWorld.WALL_HEIGHT_DEFAULT = (Integer) Configuration.INTEGER.parseString(value);
                            break;
                        }
                        case "f":
                        case "floor": {
                            ClassicPlotWorld.TOP_BLOCK_DEFAULT = (PlotBlock[]) Configuration.BLOCKLIST.parseString(value);
                            break;
                        }
                        case "m":
                        case "main": {
                            ClassicPlotWorld.MAIN_BLOCK_DEFAULT = (PlotBlock[]) Configuration.BLOCKLIST.parseString(value);
                            break;
                        }
                        case "w":
                        case "wall": {
                            ClassicPlotWorld.WALL_FILLING_DEFAULT = (PlotBlock) Configuration.BLOCK.parseString(value);
                            break;
                        }
                        case "b":
                        case "border": {
                            ClassicPlotWorld.WALL_BLOCK_DEFAULT = (PlotBlock) Configuration.BLOCK.parseString(value);
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

    public static Connection getConnection() {
        return connection;
    }

    public PlotSquared(final IPlotMain imp_class) {
        SetupUtils.generators = new HashMap<>();
        THIS = this;
        IMP = imp_class;
        try {
            FILE = new File(PlotSquared.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        }
        catch (Exception e) {
            log("Could not determine file path");
        }
        VERSION = IMP.getVersion();
        economy = IMP.getEconomy();
        C.setupTranslations();
        C.saveTranslations();
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
        TASK = IMP.getTaskManager();
        if (C.ENABLED.s().length() > 0) {
            log(C.ENABLED.s());
        }
        setupConfigs();
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
        UUIDHandler.uuidWrapper = IMP.initUUIDHandler();
        // create event util class
        EventUtil.manager = IMP.initEventUtil();
        // create Hybrid utility class
        HybridUtils.manager = IMP.initHybridUtils();
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
        showDebug();
    }
    
    public void copyFile(String file, String folder) {
        try {
            byte[] buffer = new byte[2048];
            File output = PlotSquared.IMP.getDirectory();
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

    public void disable() {
        try {
            database.closeConnection();
        } catch (NullPointerException | SQLException e) {
            log("&cCould not close database connection!");
        }
    }

    public static void log(final String message) {
        IMP.log(message);
    }

    public void setupDatabase() {
        if (Settings.DB.USE_MYSQL) {
            try {
                database = new MySQL(THIS, Settings.DB.HOST_NAME, Settings.DB.PORT, Settings.DB.DATABASE, Settings.DB.USER, Settings.DB.PASSWORD);
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
                this.database = new SQLite(THIS, IMP.getDirectory() + File.separator + Settings.DB.SQLITE_DB + ".db"); 
                connection = this.database.openConnection();
                {
                    DBFunc.dbManager = new SQLManager(connection, Settings.DB.PREFIX);
                    final DatabaseMetaData meta = connection.getMetaData();
                    ResultSet res = meta.getTables(null, null, Settings.DB.PREFIX + "plot", null);
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
            return;
        }
    }

    public static void setupDefaultFlags() {
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

            @Override
            public String getValueDesc() {
                return "Flag value must be weather type: 'clear' or 'rain'";
            }
        });
    }

    public static void setupConfig() {
        config.set("version", VERSION);
        
        final Map<String, Object> options = new HashMap<>();
        // Command confirmation
        options.put("confirmation.clear", Settings.CONFIRM_CLEAR);
        options.put("confirmation.delete", Settings.CONFIRM_DELETE);
        options.put("confirmation.unlink", Settings.CONFIRM_UNLINK);
        
        // Protection
        options.put("protection.tnt-listener.enabled", Settings.TNT_LISTENER);
        options.put("protection.piston.falling-blocks", Settings.PISTON_FALLING_BLOCK_CHECK);
        
        // Clusters
        options.put("clusters.enabled", Settings.ENABLE_CLUSTERS);
        
        // PlotMe
        options.put("plotme-alias", Settings.USE_PLOTME_ALIAS);
        options.put("plotme-convert.enabled", Settings.CONVERT_PLOTME);
        options.put("plotme-convert.cache-uuids", Settings.CACHE_PLOTME);
        
        // UUID
        options.put("UUID.offline", Settings.OFFLINE_MODE);
        options.put("UUID.force-lowercase", Settings.UUID_LOWERCASE);
        options.put("uuid.read-from-disk", Settings.UUID_FROM_DISK);
        
        // Mob stuff
        options.put("kill_road_mobs", Settings.KILL_ROAD_MOBS_DEFAULT);
        options.put("mob_pathfinding", Settings.MOB_PATHFINDING_DEFAULT);
        
        // Clearing + Expiry
        options.put("clear.auto.enabled", false);
        options.put("clear.auto.days", 365);
        options.put("clear.check-disk", Settings.AUTO_CLEAR_CHECK_DISK);
        options.put("clear.on.ban", false);
        options.put("clear.fastmode", Settings.ENABLE_CLUSTERS);
        options.put("clear.auto.clear-interval-seconds", Settings.CLEAR_INTERVAL);
        
        // Schematics
        options.put("schematics.save_path", Settings.SCHEMATIC_SAVE_PATH);
        
        // Caching
        options.put("cache.permissions", Settings.PERMISSION_CACHING);
        
        // Titles
        options.put("titles", Settings.TITLES);
        
        // Teleportation
        options.put("teleport.on_login", Settings.TELEPORT_ON_LOGIN);
        options.put("teleport.delay", 0);
        
        // WorldEdit
        options.put("worldedit.require-selection-in-mask", Settings.REQUIRE_SELECTION);
        options.put("worldedit.max-volume", Settings.WE_MAX_VOLUME);
        options.put("worldedit.max-iterations", Settings.WE_MAX_ITERATIONS);
        options.put("worldedit.blacklist", Arrays.asList("cs", ".s", "restore", "snapshot", "delchunks", "listchunks"));
        
        // Chunk processor
        options.put("chunk-processor.enabled", Settings.CHUNK_PROCESSOR);
        options.put("chunk-processor.max-blockstates", Settings.CHUNK_PROCESSOR_MAX_BLOCKSTATES);
        options.put("chunk-processor.max-entities", Settings.CHUNK_PROCESSOR_MAX_ENTITIES);
        
        // Comments
        options.put("comments.notifications.enabled", Settings.COMMENT_NOTIFICATIONS);
        
        // Plot limits
        options.put("global_limit", Settings.GLOBAL_LIMIT);
        options.put("max_plots", Settings.MAX_PLOTS);
        options.put("claim.max-auto-area", Settings.MAX_AUTO_SIZE);

        // Misc
        options.put("console.color", Settings.CONSOLE_COLOR);
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
        Settings.TNT_LISTENER = config.getBoolean("protection.tnt-listener.enabled");
        Settings.PISTON_FALLING_BLOCK_CHECK = config.getBoolean("protection.piston.falling-blocks");
        
        // Clusters
        Settings.ENABLE_CLUSTERS = config.getBoolean("clusters.enabled");
        
        // PlotMe
        Settings.USE_PLOTME_ALIAS = config.getBoolean("plotme-alias");
        Settings.CONVERT_PLOTME = config.getBoolean("plotme-convert.enabled");
        Settings.CACHE_PLOTME = config.getBoolean("plotme-convert.cache-uuids");
        
        // UUID
        Settings.OFFLINE_MODE = config.getBoolean("UUID.offline");
        Settings.UUID_LOWERCASE = config.getBoolean("UUID.force-lowercase");
        Settings.UUID_FROM_DISK = config.getBoolean("uuid.read-from-disk");
        
        // Mob stuff
        Settings.KILL_ROAD_MOBS = config.getBoolean("kill_road_mobs");
        Settings.MOB_PATHFINDING = config.getBoolean("mob_pathfinding");
        
        // Clearing + Expiry
        Settings.FAST_CLEAR = config.getBoolean("clear.fastmode");
        Settings.AUTO_CLEAR_DAYS = config.getInt("clear.auto.days");
        Settings.AUTO_CLEAR_CHECK_DISK = config.getBoolean("clear.check-disk");
        Settings.AUTO_CLEAR = config.getBoolean("clear.auto.enabled");
        
        Settings.CLEAR_INTERVAL = config.getInt("clear.auto.clear-interval-seconds");
        
        // Schematics
        Settings.SCHEMATIC_SAVE_PATH = config.getString("schematics.save_path");
        
        // Caching
        Settings.PERMISSION_CACHING = config.getBoolean("cache.permissions");
        
        // Titles
        Settings.TITLES = config.getBoolean("titles");
        
        // Teleportation
        Settings.TELEPORT_DELAY = config.getInt("teleport.delay");
        Settings.TELEPORT_ON_LOGIN = config.getBoolean("teleport.on_login");
        
        // WorldEdit
        Settings.REQUIRE_SELECTION = config.getBoolean("worldedit.require-selection-in-mask");
        Settings.WE_MAX_VOLUME = config.getLong("worldedit.max-volume");
        Settings.WE_MAX_ITERATIONS = config.getLong("worldedit.max-iterations");
        Settings.WE_BLACKLIST = config.getStringList("worldedit.blacklist");
        
        // Chunk processor
        Settings.CHUNK_PROCESSOR = config.getBoolean("chunk-processor.enabled");
        Settings.CHUNK_PROCESSOR_MAX_BLOCKSTATES = config.getInt("chunk-processor.max-blockstates");
        Settings.CHUNK_PROCESSOR_MAX_ENTITIES= config.getInt("chunk-processor.max-entities");
        
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
        Settings.METRICS = config.getBoolean("metrics");
    }

    public static void setupConfigs() {
        final File folder = new File(IMP.getDirectory() + File.separator + "config");
        if (!folder.exists() && !folder.mkdirs()) {
            log(C.PREFIX.s() + "&cFailed to create the /plugins/config folder. Please create it manually.");
        }
        try {
            styleFile = new File(IMP.getDirectory() + File.separator + "translations" + File.separator + "style.yml");
            if (!styleFile.exists()) {
                if (!styleFile.createNewFile()) {
                    log("Could not create the style file, please create \"translations/style.yml\" manually");
                }
            }
            style = YamlConfiguration.loadConfiguration(styleFile);
            setupStyle();
        } catch (final Exception err) {
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

    private static void setupStorage() {
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

    public static void showDebug() {
        C.COLOR_1 = "&" + (style.getString("color.1"));
        C.COLOR_2 = "&" + (style.getString("color.2"));
        C.COLOR_3 = "&" + (style.getString("color.3"));
        C.COLOR_4 = "&" + (style.getString("color.4"));
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

    private static void setupStyle() {
        style.set("version", VERSION);
        final Map<String, Object> o = new HashMap<>();
        o.put("color.1", C.COLOR_1.substring(1));
        o.put("color.2", C.COLOR_2.substring(1));
        o.put("color.3", C.COLOR_3.substring(1));
        o.put("color.4", C.COLOR_4.substring(1));
        for (final Entry<String, Object> node : o.entrySet()) {
            if (!style.contains(node.getKey())) {
                style.set(node.getKey(), node.getValue());
            }
        }
    }

    public static double getJavaVersion() {
        return Double.parseDouble(System.getProperty("java.specification.version"));
    }

    public static Set<String> getPlotWorlds() {
        return plotworlds.keySet();
    }
    
    public static Collection<PlotWorld> getPlotWorldObjects() {
        return plotworlds.values();
    }
}
