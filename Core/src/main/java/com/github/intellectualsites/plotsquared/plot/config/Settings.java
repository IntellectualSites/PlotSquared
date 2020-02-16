package com.github.intellectualsites.plotsquared.plot.config;

import com.github.intellectualsites.plotsquared.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Settings extends Config {

    /*
    START OF CONFIGURATION SECTION:
    NOTE: Fields are saved in declaration order, classes in reverse order
     */

    @Comment("These first 7 aren't configurable") // This is a comment
    @Final // Indicates that this value isn't configurable
    public static String ISSUES = "https://github.com/IntellectualSites/PlotSquared/issues";
    @Final public static String SUGGESTION =
        "https://github.com/IntellectualSites/PlotSquaredSuggestions";
    @Final public static String WIKI =
        "https://github.com/IntellectualSites/PlotSquared/wiki";
    @Final public static String DATE; // These values are set from P2 before loading
    @Final public static String BUILD; // These values are set from P2 before loading
    @Final public static String COMMIT; // These values are set from P2 before loading
    @Final public static String PLATFORM; // These values are set from P2 before loading

    @Comment("Show additional information in console") public static boolean DEBUG = false;
    @Comment({"The big annoying text that appears when you enter a plot",
        "For a single plot: `/plot flag set titles false`", "For just you: `/plot toggle titles`", "For all plots: Add `titles: false` in the worlds.yml flags block"})
    public static boolean TITLES = true;

    @Create // This value will be generated automatically
    public static ConfigBlock<Auto_Clear> AUTO_CLEAR = null;
    // A ConfigBlock is a section that can have multiple instances e.g. multiple expiry tasks

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
        Redstone.DISABLE_OFFLINE = config.getBoolean("protection.redstone.disable-offline");
        Redstone.DISABLE_UNOCCUPIED = config
            .getBoolean("protection.redstone.disable-unoccupied", Redstone.DISABLE_UNOCCUPIED);

        // UUID
        UUID.USE_SQLUUIDHANDLER =
            config.getBoolean("uuid.use_sqluuidhandler", UUID.USE_SQLUUIDHANDLER);
        UUID.OFFLINE = config.getBoolean("UUID.offline", UUID.OFFLINE);
        UUID.FORCE_LOWERCASE = config.getBoolean("UUID.force-lowercase", UUID.FORCE_LOWERCASE);

        // Mob stuff
        Enabled_Components.KILL_ROAD_MOBS =
            config.getBoolean("kill_road_mobs", Enabled_Components.KILL_ROAD_MOBS);
        Enabled_Components.KILL_ROAD_VEHICLES =
            config.getBoolean("kill_road_vehicles", Enabled_Components.KILL_ROAD_VEHICLES);

        // Clearing + Expiry
        //FAST_CLEAR = config.getBoolean("clear.fastmode");
        Enabled_Components.PLOT_EXPIRY =
            config.getBoolean("clear.auto.enabled", Enabled_Components.PLOT_EXPIRY);
        if (Enabled_Components.PLOT_EXPIRY) {
            Enabled_Components.BAN_DELETER = config.getBoolean("clear.on.ban");
            AUTO_CLEAR = new ConfigBlock<>();
            AUTO_CLEAR.put("task1", new Auto_Clear());
            Auto_Clear task = AUTO_CLEAR.get("task1");
            task.CALIBRATION = new Auto_Clear.CALIBRATION();

            task.DAYS = config.getInt("clear.auto.days", task.DAYS);
            task.THRESHOLD = config.getInt("clear.auto.threshold", task.THRESHOLD);
            task.CONFIRMATION = config.getBoolean("clear.auto.confirmation", task.CONFIRMATION);
            task.CALIBRATION.CHANGES =
                config.getInt("clear.auto.calibration.changes", task.CALIBRATION.CHANGES);
            task.CALIBRATION.FACES =
                config.getInt("clear.auto.calibration.faces", task.CALIBRATION.FACES);
            task.CALIBRATION.DATA =
                config.getInt("clear.auto.calibration.data", task.CALIBRATION.DATA);
            task.CALIBRATION.AIR =
                config.getInt("clear.auto.calibration.air", task.CALIBRATION.AIR);
            task.CALIBRATION.VARIETY =
                config.getInt("clear.auto.calibration.variety", task.CALIBRATION.VARIETY);
            task.CALIBRATION.CHANGES_SD =
                config.getInt("clear.auto.calibration.changes_sd", task.CALIBRATION.CHANGES_SD);
            task.CALIBRATION.FACES_SD =
                config.getInt("clear.auto.calibration.faces_sd", task.CALIBRATION.FACES_SD);
            task.CALIBRATION.DATA_SD =
                config.getInt("clear.auto.calibration.data_sd", task.CALIBRATION.DATA_SD);
            task.CALIBRATION.AIR_SD =
                config.getInt("clear.auto.calibration.air_sd", task.CALIBRATION.AIR_SD);
            task.CALIBRATION.VARIETY_SD =
                config.getInt("clear.auto.calibration.variety_sd", task.CALIBRATION.VARIETY_SD);
        }

        // Done
        Done.REQUIRED_FOR_RATINGS =
            config.getBoolean("approval.ratings.check-done", Done.REQUIRED_FOR_RATINGS);
        Done.COUNTS_TOWARDS_LIMIT =
            config.getBoolean("approval.done.counts-towards-limit", Done.COUNTS_TOWARDS_LIMIT);
        Done.RESTRICT_BUILDING =
            config.getBoolean("approval.done.restrict-building", Done.RESTRICT_BUILDING);
        Done.REQUIRED_FOR_DOWNLOAD =
            config.getBoolean("approval.done.required-for-download", Done.REQUIRED_FOR_DOWNLOAD);

        // Schematics
        Paths.SCHEMATICS = config.getString("schematics.save_path", Paths.SCHEMATICS);

        // Web
        Web.URL = config.getString("web.url", Web.URL);

        // Caching
        Enabled_Components.PERMISSION_CACHE =
            config.getBoolean("cache.permissions", Enabled_Components.PERMISSION_CACHE);
        Enabled_Components.RATING_CACHE =
            config.getBoolean("cache.ratings", Enabled_Components.RATING_CACHE);

        // Rating system
        Ratings.CATEGORIES = config.contains("ratings.categories") ?
            config.getStringList("ratings.categories") :
            Ratings.CATEGORIES;

        // Titles
        TITLES = config.getBoolean("titles", TITLES);

        // Teleportation
        Teleport.DELAY = config.getInt("teleport.delay", Teleport.DELAY);
        Teleport.ON_LOGIN = config.getBoolean("teleport.on_login", Teleport.ON_LOGIN);
        Teleport.ON_DEATH = config.getBoolean("teleport.on_death", Teleport.ON_DEATH);

        // WorldEdit
        //WE_ALLOW_HELPER = config.getBoolean("worldedit.enable-for-helpers");

        // Chunk processor
        Enabled_Components.CHUNK_PROCESSOR =
            config.getBoolean("chunk-processor.enabled", Enabled_Components.CHUNK_PROCESSOR);
        Chunk_Processor.AUTO_TRIM =
            config.getBoolean("chunk-processor.auto-unload", Chunk_Processor.AUTO_TRIM);
        Chunk_Processor.MAX_TILES =
            config.getInt("chunk-processor.max-blockstates", Chunk_Processor.MAX_TILES);
        Chunk_Processor.MAX_ENTITIES =
            config.getInt("chunk-processor.max-entities", Chunk_Processor.MAX_ENTITIES);
        Chunk_Processor.DISABLE_PHYSICS =
            config.getBoolean("chunk-processor.disable-physics", Chunk_Processor.DISABLE_PHYSICS);

        // Comments
        Enabled_Components.COMMENT_NOTIFIER = config
            .getBoolean("comments.notifications.enabled", Enabled_Components.COMMENT_NOTIFIER);

        // Plot limits
        Claim.MAX_AUTO_AREA = config.getInt("claim.max-auto-area", Claim.MAX_AUTO_AREA);
        Limit.MAX_PLOTS = config.getInt("max_plots", Limit.MAX_PLOTS);
        Limit.GLOBAL = config.getBoolean("global_limit", Limit.GLOBAL);

        // Misc
        DEBUG = config.getBoolean("debug", DEBUG);
        Chat.CONSOLE_COLOR = config.getBoolean("console.color", Chat.CONSOLE_COLOR);
        Chat.INTERACTIVE = config.getBoolean("chat.fancy", Chat.INTERACTIVE);

        Enabled_Components.UPDATER =
            config.getBoolean("update-notifications", Enabled_Components.UPDATER);
        Enabled_Components.DATABASE_PURGER =
            config.getBoolean("auto-purge", Enabled_Components.DATABASE_PURGER);
        return true;
    }

    @Comment("This is an auto clearing task called `task1`") @BlockName("task1")
    // The name for the default block
    public static final class Auto_Clear extends ConfigBlock {
        @Create // This value has to be generated since an instance isn't static
        public CALIBRATION CALIBRATION = null;
        public int THRESHOLD = -1;
        public int REQUIRED_PLOTS = -1;
        public boolean CONFIRMATION = true;
        public int DAYS = 90;
        public int SKIP_ACCOUNT_AGE_DAYS = -1;
        public List<String> WORLDS = new ArrayList<>(Collections.singletonList("*"));


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
    }


    public static class Chunk_Processor {
        @Comment("Auto trim will not save chunks which aren't claimed") public static boolean
            AUTO_TRIM = false;
        @Comment("Max tile entities per chunk") public static int MAX_TILES = 4096;
        @Comment("Max entities per chunk") public static int MAX_ENTITIES = 512;
        @Comment("Disable block physics") public static boolean DISABLE_PHYSICS = false;
    }


    public static class UUID {
        @Comment("Force using offline UUIDs (it usually detects the right mode)")
        public static boolean OFFLINE = false;
        @Comment("Force using lowercase UUIDs") public static boolean FORCE_LOWERCASE = false;
        @Comment("Use a database to store UUID/name info") public static boolean
            USE_SQLUUIDHANDLER = false;
        @Ignore public static boolean NATIVE_UUID_PROVIDER = false;
    }


    @Comment("General settings") public static final class General {
        @Comment("Display scientific numbers (4.2E8)") public static boolean SCIENTIFIC = false;
        @Comment("Replace wall when merging") public static boolean MERGE_REPLACE_WALL = true;
    }


    @Comment("Schematic Settings") public static final class Schematics {
        @Comment(
            "Whether schematic based generation should paste schematic on top of plots, or from Y=1")
        public static boolean PASTE_ON_TOP = true;
    }


    @Comment("Configure the paths that will be used") public static final class Paths {
        public static String SCHEMATICS = "schematics";
        public static String SCRIPTS = "scripts";
        public static String TEMPLATES = "templates";
        public static String TRANSLATIONS = "translations";
    }


    public static class Web {
        @Comment({"The web interface for schematics", " - All schematics are anonymous and private",
            " - Downloads can be deleted by the user",
            " - Supports plot uploads, downloads and saves",}) public static String URL =
            "https://empcraft.com/plots/";
        @Comment({"The web interface for assets", " - All schematics are organized and public",
            " - Assets can be searched, selected and downloaded",}) public static String ASSETS =
            "https://empcraft.com/assetpack/";

    }


    public static final class Done {
        @Comment("Require a plot marked as done to download") public static boolean REQUIRED_FOR_DOWNLOAD =
            false;
        @Comment("Only plots marked as done can be rated") public static boolean REQUIRED_FOR_RATINGS = false;
        @Comment("Restrict building when a plot is marked as done") public static boolean RESTRICT_BUILDING =
            false;
        @Comment("The limit being how many plots a player can claim") public static boolean
            COUNTS_TOWARDS_LIMIT = true;
    }


    public static final class Chat {
        @Comment("Sometimes console color doesn't work, you can disable it here")
        public static boolean CONSOLE_COLOR = true;
        @Comment("Should the chat be interactive?") public static boolean INTERACTIVE = true;
    }


    @Comment("Relating to how many plots someone can claim  ") public static final class Limit {
        @Comment("Should the limit be global (over multiple worlds)") public static boolean GLOBAL =
            false;
        @Comment({"The max. range of permissions to check e.g. plots.plot.127", "The value covers the range to check only, you need to assign the permission to players/groups still",
        "Modifying the value does NOT change the amount of plots players can claim"}) public static int
            MAX_PLOTS = 127;
    }


    public static final class Confirmation {
        @Comment("Teleport to your plot on death") public static int CONFIRMATION_TIMEOUT_SECONDS =
            20;
    }


    public static final class Teleport {
        @Comment("Teleport to your plot on death") public static boolean ON_DEATH = false;
        @Comment("Teleport to your plot on login") public static boolean ON_LOGIN = false;
        @Comment("Teleport to your plot on claim") public static boolean ON_CLAIM = true;
        @Comment({"Add a delay to all teleport commands", "Assign `plots.teleport.delay.bypass` to bypass the cooldown."}) public static int DELAY = 0;
        @Comment("The visit command is ordered by world instead of globally") public static boolean
            PER_WORLD_VISIT = false;
    }


    public static final class Redstone {
        @Comment("Disable redstone in unoccupied plots") public static boolean DISABLE_UNOCCUPIED =
            false;
        @Comment("Disable redstone when all owners/trusted/members are offline")
        public static boolean DISABLE_OFFLINE = false;
        @Comment(
            "Detect and cancel invalid pistons on the edge of plots (e.g. placed with WorldEdit)")
        public static boolean DETECT_INVALID_EDGE_PISTONS = false;
    }


    public static final class Claim {
        @Comment("The max plots claimed in a single `/plot auto <size>` command") public static int
            MAX_AUTO_AREA = 4;
    }


    public static final class Ratings {
        @Comment("Replace the rating system with a like system. Will add /plot like/dislike,"
            + " and remove the rating command") public static boolean USE_LIKES = false;
        @Comment("Rating categories") public static List<String> CATEGORIES = new ArrayList<>();
    }


    @Comment(
        {"Enable or disable parts of the plugin", "Note: A cache will use some memory if enabled"})
    public static final class Enabled_Components { // Group the following values into a new config section
        @Comment("The database stores all the plots") public static boolean DATABASE = true;
        @Comment("Events are needed to track a lot of things") public static boolean EVENTS = true;
        @Comment("Commands are used to interact with the plugin") public static boolean COMMANDS =
            true;
        @Comment("The UUID cacher is used to resolve player names") public static boolean
            UUID_CACHE = true;
        @Comment("The plugin auto updater will notify you if updates are available.") public static boolean UPDATER = true;
        @Comment("Stores user metadata in a database") public static boolean PERSISTENT_META = true;
        @Comment("Optimizes permission checks") public static boolean PERMISSION_CACHE = true;
        @Comment("Optimizes block changing code") public static boolean BLOCK_CACHE = true;
        @Comment("Getting a rating won't need the database") public static boolean RATING_CACHE =
            true;
        @Comment("Allow WorldEdit to be restricted to plots") public static boolean
            WORLDEDIT_RESTRICTIONS = true;
        @Comment("Allow economy to be used") public static boolean ECONOMY = true;
        @Comment("Expiry will clear old or simplistic plots") public static boolean PLOT_EXPIRY =
            false;
        @Comment("Processes chunks (trimming, or entity/tile limits) ") public static boolean
            CHUNK_PROCESSOR = false;
        @Comment("Kill mobs on roads") public static boolean KILL_ROAD_MOBS = false;
        @Comment("Kill items on roads") public static boolean KILL_ROAD_ITEMS = false;
        @Comment("Kill vehicles on roads") public static boolean KILL_ROAD_VEHICLES = false;
        @Comment("Notify a player of any missed comments upon plot entry") public static boolean
            COMMENT_NOTIFIER = false;
        @Comment("Let players claim entire worlds with PlotSquared") public static boolean WORLDS =
            false;
        @Comment("Actively purge invalid database entries") public static boolean DATABASE_PURGER =
            false;
        @Comment("Delete plots when a player is banned") public static boolean BAN_DELETER = false;
        @Comment({"Prevent possibly unsafe blocks from being used in plot components", "Can be bypassed with `/plot debugallowunsafe`"})
        public static boolean PREVENT_UNSAFE = true;
    }
}
