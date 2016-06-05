package com.intellectualcrafters.plot.config;

import com.intellectualcrafters.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Settings extends Config {

    /*
    START OF CONFIGURATION SECTION:
    NOTE: Fields are saved in declaration order, classes in reverse order
     */

    @Comment("These first 4 aren't configurable") // This is a comment
    @Final // Indicates that this value isn't configurable
    public static final String ISSUES = "https://github.com/IntellectualSites/PlotSquared/issues";
    @Final
    public static final String WIKI = "https://github.com/IntellectualSites/PlotSquared/wiki";
    @Final
    public static String VERSION = null; // These values are set from PS before loading
    @Final
    public static String PLATFORM = null; // These values are set from PS before loading

    @Comment("Show additional information in console")
    public static boolean DEBUG = true;
    @Comment({"The big annoying text that appears when you enter a plot", "For a single plot: `/plot flag set titles false`", "For just you: `/plot toggle titles`"})
    public static boolean TITLES = true;

    @Create // This value will be generated automatically
    public static final ConfigBlock<AUTO_CLEAR> AUTO_CLEAR = null; // A ConfigBlock is a section that can have multiple instances e.g. multiple expiry tasks

    @Comment("This is an auto clearing task called `task1`")
    @BlockName("task1") // The name for the default block
    public static final class AUTO_CLEAR extends ConfigBlock {
        @Create // This value has to be generated since an instance isn't static
        public CALIBRATION CALIBRATION = null;

        @Comment("See: https://github.com/IntellectualSites/PlotSquared/wiki/Plot-analysis")
        public static final class CALIBRATION {
            public int VARIETY = 0;
            public int VARIETY_SD = 0;
            public int CHANGES = 0;
            public int CHANGES_SD = 1;
            public int FACES = 0;
            public int FACES_SD = 0;
            public int DATA_SD = 0;
            public int AIR = 0;
            public int AIR_SD = 0;
            public int DATA = 0;
        }

        public int THRESHOLD = 1;
        public boolean CONFIRMATION = true;
        public int DAYS = 7;
        public List<String> WORLDS = new ArrayList<>(Arrays.asList("*"));
    }

    public static class CHUNK_PROCESSOR {
        @Comment("Auto trim will not save chunks which aren't claimed")
        public static boolean AUTO_TRIM = false;
        @Comment("Max tile entities per chunk")
        public static int MAX_TILES = 4096;
        @Comment("Max entities per chunk")
        public static int MAX_ENTITIES = 512;
        @Comment("Disable block physics")
        public static boolean DISABLE_PHYSICS = false;
    }

    public static class UUID {
        @Comment("Force PlotSquared to use offline UUIDs (it usually detects the right mode)")
        public static boolean OFFLINE = false;
        @Comment("Force PlotSquared to use lowercase UUIDs")
        public static boolean FORCE_LOWERCASE = false;
        @Comment("Use a database to store UUID/name info")
        public static boolean USE_SQLUUIDHANDLER = false;
        @Ignore
        public static boolean NATIVE_UUID_PROVIDER = false;
    }

    @Comment("Configure the paths PlotSquared will use")
    public static final class PATHS {
        public static String SCHEMATICS = "schematics";
        public static String BO3 = "bo3";
        public static String SCRIPTS = "scripts";
        public static String TEMPLATES = "templates";
        public static String TRANSLATIONS = "translations";
    }

    public static class WEB {
        @Comment("We are already hosting a web interface for you:")
        public static String URL = "http://empcraft.com/plots/";
        @Comment("The ip that will show up in the interface")
        public static String SERVER_IP = "your.ip.here";
    }

    public static final class DONE {
        @Comment("Require a done plot to download")
        public static boolean REQUIRED_FOR_DOWNLOAD = false;
        @Comment("Only done plots can be rated")
        public static boolean REQUIRED_FOR_RATINGS = false;
        @Comment("Restrict building when a plot is done")
        public static boolean RESTRICT_BUILDING = false;
        @Comment("The limit being how many plots a player can claim")
        public static boolean COUNTS_TOWARDS_LIMIT = true;
    }

    public static final class CHAT {
        @Comment("Sometimes console color doesn't work, you can disable it here")
        public static boolean CONSOLE_COLOR = true;
        @Comment("Should chat be interactive")
        public static boolean INTERACTIVE = true;
    }

    @Comment("Relating to how many plots someone can claim  ")
    public static final class LIMIT {
        @Comment("Should the limit be global (over multiple worlds)")
        public static boolean GLOBAL = false;
        @Comment("The range of permissions to check e.g. plots.plot.127")
        public static int MAX_PLOTS = 127;
    }

    @Comment("Switching from PlotMe?")
    public static final class PLOTME {
        @Comment("Cache the uuids from the PlotMe database")
        public static boolean CACHE_UUDS = false;
        @Comment("Have `/plotme` as a command alias")
        public static boolean ALIAS = false;
    }

    public static final class TELEPORT {
        @Comment("Teleport to your plot on death")
        public static boolean ON_DEATH = false;
        @Comment("Teleport to your plot on login")
        public static boolean ON_LOGIN = false;
        @Comment("Add a teleportation delay to all commands")
        public static int DELAY = 0;
    }

    public static final class REDSTONE {
        @Comment("Disable redstone in unoccupied plots")
        public static boolean DISABLE_UNOCCUPIED = false;
        @Comment("Disable redstone when all owners/trusted/members are offline")
        public static boolean DISABLE_OFFLINE = false;
    }

    public static final class CLAIM {
        @Comment("The max plots claimed in a single `/plot auto <size>` command")
        public static int MAX_AUTO_AREA = 4;
    }

    public static final class RATINGS {
        public static List<String> CATEGORIES = new ArrayList<>();
    }

    @Comment({"Enable or disable part of the plugin","Note: A cache will use some memory if enabled"})
    public static final class ENABLED_COMPONENTS { // Group the following values into a new config section
        @Comment("The database stores all the plots")
        public static boolean DATABASE = true;
        @Comment("Events are needed to track a lot of things")
        public static boolean EVENTS = true;
        @Comment("Commands are used to interact with the plugin")
        public static boolean COMMANDS = true;
        @Comment("The UUID cacher is used to resolve player names")
        public static boolean UUID_CACHE = true;
        @Comment("Notify players of updates")
        public static boolean UPDATER = true;
        @Comment("Optimizes permission checks")
        public static boolean PERMISSION_CACHE = true;
        @Comment("Optimizes block changing code")
        public static boolean BLOCK_CACHE = true;
        @Comment("Getting a rating won't need the database")
        public static boolean RATING_CACHE = true;
        @Comment("The converter will attempt to convert the PlotMe database")
        public static boolean PLOTME_CONVERTER = true;
        @Comment("Allow WorldEdit to be restricted to plots")
        public static boolean WORLDEDIT_RESTRICTIONS = true;
        @Comment("Allow economy to be used")
        public static boolean ECONOMY = true;
        @Comment("Send anonymous usage statistics")
        public static boolean METRICS = true;
        @Comment("Expiry will clear old or simplistic plots")
        public static boolean PLOT_EXPIRY = false;
        @Comment("Processes chunks (trimming, or entity/tile limits) ")
        public static boolean CHUNK_PROCESSOR = false;
        @Comment("Kill mobs or vehicles on roads")
        public static boolean KILL_ROAD_MOBS = false;
        public static boolean KILL_ROAD_VEHICLES = false;
        @Comment("Notify a player of any missed comments upon plot entry")
        public static boolean COMMENT_NOTIFIER = false;
        @Comment("Actively purge invalid database entries")
        public static boolean DATABASE_PURGER = false;
        @Comment("Delete plots when a player is banned")
        public static boolean BAN_DELETER = false;
    }

    /*
    END OF CONFIGURATION SECTION:
     */

    public static void save(File file) {
        save(file, Settings.class);
    }

    public static void load(File file) {
        load(file, Settings.class);
    }

    public static boolean convertLegacy(File file) {
        if (!file.exists()) {
            return false;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        // Protection
        REDSTONE.DISABLE_OFFLINE = config.getBoolean("protection.redstone.disable-offline");
        REDSTONE.DISABLE_UNOCCUPIED = config.getBoolean("protection.redstone.disable-unoccupied", REDSTONE.DISABLE_UNOCCUPIED);

        // PlotMe
        PLOTME.ALIAS = config.getBoolean("plotme-alias", PLOTME.ALIAS);
        ENABLED_COMPONENTS.PLOTME_CONVERTER = config.getBoolean("plotme-convert.enabled", ENABLED_COMPONENTS.PLOTME_CONVERTER);
        PLOTME.CACHE_UUDS = config.getBoolean("plotme-convert.cache-uuids", PLOTME.CACHE_UUDS);

        // UUID
        UUID.USE_SQLUUIDHANDLER = config.getBoolean("uuid.use_sqluuidhandler", UUID.USE_SQLUUIDHANDLER);
        UUID.OFFLINE = config.getBoolean("UUID.offline", UUID.OFFLINE);
        UUID.FORCE_LOWERCASE = config.getBoolean("UUID.force-lowercase", UUID.FORCE_LOWERCASE);

        // Mob stuff
        ENABLED_COMPONENTS.KILL_ROAD_MOBS = config.getBoolean("kill_road_mobs", ENABLED_COMPONENTS.KILL_ROAD_MOBS);
        ENABLED_COMPONENTS.KILL_ROAD_VEHICLES = config.getBoolean("kill_road_vehicles", ENABLED_COMPONENTS.KILL_ROAD_VEHICLES);

        // Clearing + Expiry
//        FAST_CLEAR = config.getBoolean("clear.fastmode");
        ENABLED_COMPONENTS.PLOT_EXPIRY = config.getBoolean("clear.auto.enabled", ENABLED_COMPONENTS.PLOT_EXPIRY);
        if (ENABLED_COMPONENTS.PLOT_EXPIRY) {
            ENABLED_COMPONENTS.BAN_DELETER = config.getBoolean("clear.on.ban");

            AUTO_CLEAR.put("task1", new AUTO_CLEAR());
            AUTO_CLEAR task = AUTO_CLEAR.get("task1");
            task.CALIBRATION = new AUTO_CLEAR.CALIBRATION();

            task.DAYS = config.getInt("clear.auto.days", task.DAYS);
            task.THRESHOLD = config.getInt("clear.auto.threshold", task.THRESHOLD);
            task.CONFIRMATION = config.getBoolean("clear.auto.confirmation", task.CONFIRMATION);
            task.CALIBRATION.CHANGES = config.getInt("clear.auto.calibration.changes", task.CALIBRATION.CHANGES);
            task.CALIBRATION.FACES = config.getInt("clear.auto.calibration.faces", task.CALIBRATION.FACES);
            task.CALIBRATION.DATA = config.getInt("clear.auto.calibration.data", task.CALIBRATION.DATA);
            task.CALIBRATION.AIR = config.getInt("clear.auto.calibration.air", task.CALIBRATION.AIR);
            task.CALIBRATION.VARIETY = config.getInt("clear.auto.calibration.variety", task.CALIBRATION.VARIETY);
            task.CALIBRATION.CHANGES_SD = config.getInt("clear.auto.calibration.changes_sd", task.CALIBRATION.CHANGES_SD);
            task.CALIBRATION.FACES_SD = config.getInt("clear.auto.calibration.faces_sd", task.CALIBRATION.FACES_SD);
            task.CALIBRATION.DATA_SD = config.getInt("clear.auto.calibration.data_sd", task.CALIBRATION.DATA_SD);
            task.CALIBRATION.AIR_SD = config.getInt("clear.auto.calibration.air_sd", task.CALIBRATION.AIR_SD);
            task.CALIBRATION.VARIETY_SD = config.getInt("clear.auto.calibration.variety_sd", task.CALIBRATION.VARIETY_SD);
        }

        // Done
        DONE.REQUIRED_FOR_RATINGS = config.getBoolean("approval.ratings.check-done", DONE.REQUIRED_FOR_RATINGS);
        DONE.COUNTS_TOWARDS_LIMIT = config.getBoolean("approval.done.counts-towards-limit", DONE.COUNTS_TOWARDS_LIMIT);
        DONE.RESTRICT_BUILDING = config.getBoolean("approval.done.restrict-building", DONE.RESTRICT_BUILDING);
        DONE.REQUIRED_FOR_DOWNLOAD = config.getBoolean("approval.done.required-for-download", DONE.REQUIRED_FOR_DOWNLOAD);

        // Schematics
        PATHS.SCHEMATICS = config.getString("schematics.save_path", PATHS.SCHEMATICS);
        PATHS.BO3 = config.getString("bo3.save_path", PATHS.BO3);

        // Web
        WEB.URL = config.getString("web.url", WEB.URL);
        WEB.SERVER_IP = config.getString("web.server-ip", WEB.SERVER_IP);

        // Caching
        ENABLED_COMPONENTS.PERMISSION_CACHE = config.getBoolean("cache.permissions", ENABLED_COMPONENTS.PERMISSION_CACHE);
        ENABLED_COMPONENTS.RATING_CACHE = config.getBoolean("cache.ratings", ENABLED_COMPONENTS.RATING_CACHE);

        // Rating system
        RATINGS.CATEGORIES = config.contains("ratings.categories") ? config.getStringList("ratings.categories") : RATINGS.CATEGORIES;

        // Titles
        TITLES = config.getBoolean("titles", TITLES);

        // Teleportation
        TELEPORT.DELAY = config.getInt("teleport.delay", TELEPORT.DELAY);
        TELEPORT.ON_LOGIN = config.getBoolean("teleport.on_login", TELEPORT.ON_LOGIN);
        TELEPORT.ON_DEATH = config.getBoolean("teleport.on_death", TELEPORT.ON_DEATH);

        // WorldEdit
//        WE_ALLOW_HELPER = config.getBoolean("worldedit.enable-for-helpers");

        // Chunk processor
        ENABLED_COMPONENTS.CHUNK_PROCESSOR = config.getBoolean("chunk-processor.enabled", ENABLED_COMPONENTS.CHUNK_PROCESSOR);
        CHUNK_PROCESSOR.AUTO_TRIM = config.getBoolean("chunk-processor.auto-unload", CHUNK_PROCESSOR.AUTO_TRIM);
        CHUNK_PROCESSOR.MAX_TILES = config.getInt("chunk-processor.max-blockstates", CHUNK_PROCESSOR.MAX_TILES);
        CHUNK_PROCESSOR.MAX_ENTITIES = config.getInt("chunk-processor.max-entities", CHUNK_PROCESSOR.MAX_ENTITIES);
        CHUNK_PROCESSOR.DISABLE_PHYSICS = config.getBoolean("chunk-processor.disable-physics", CHUNK_PROCESSOR.DISABLE_PHYSICS);

        // Comments
        ENABLED_COMPONENTS.COMMENT_NOTIFIER = config.getBoolean("comments.notifications.enabled", ENABLED_COMPONENTS.COMMENT_NOTIFIER);

        // Plot limits
        CLAIM.MAX_AUTO_AREA = config.getInt("claim.max-auto-area", CLAIM.MAX_AUTO_AREA);
        LIMIT.MAX_PLOTS = config.getInt("max_plots", LIMIT.MAX_PLOTS);
        LIMIT.GLOBAL = config.getBoolean("global_limit", LIMIT.GLOBAL);

        // Misc
        DEBUG = config.getBoolean("debug", DEBUG);
        CHAT.CONSOLE_COLOR = config.getBoolean("console.color", CHAT.CONSOLE_COLOR);
        CHAT.INTERACTIVE = config.getBoolean("chat.fancy", CHAT.INTERACTIVE);

        ENABLED_COMPONENTS.METRICS = config.getBoolean("metrics", ENABLED_COMPONENTS.METRICS);
        ENABLED_COMPONENTS.UPDATER = config.getBoolean("update-notifications", ENABLED_COMPONENTS.UPDATER);
        ENABLED_COMPONENTS.DATABASE_PURGER = config.getBoolean("auto-purge", ENABLED_COMPONENTS.DATABASE_PURGER);
        return true;
    }
}
