package com.plotsquared.bukkit;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.MetadataValueAdapter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.intellectualcrafters.plot.IPlotMain;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.commands.WE_Anywhere;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.BlockUpdateUtil;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.ConsoleColors;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.InventoryUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.UUIDHandlerImplementation;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.bukkit.database.plotme.ClassicPlotMeConnector;
import com.plotsquared.bukkit.database.plotme.LikePlotMeConverter;
import com.plotsquared.bukkit.database.plotme.PlotMeConnector_017;
import com.plotsquared.bukkit.generator.BukkitGeneratorWrapper;
import com.plotsquared.bukkit.generator.HybridGen;
import com.plotsquared.bukkit.listeners.ChunkListener;
import com.plotsquared.bukkit.listeners.ForceFieldListener;
import com.plotsquared.bukkit.listeners.PlayerEvents;
import com.plotsquared.bukkit.listeners.PlayerEvents_1_8;
import com.plotsquared.bukkit.listeners.PlayerEvents_1_8_3;
import com.plotsquared.bukkit.listeners.PlotPlusListener;
import com.plotsquared.bukkit.listeners.TNTListener;
import com.plotsquared.bukkit.listeners.WorldEvents;
import com.plotsquared.bukkit.listeners.worldedit.WEListener;
import com.plotsquared.bukkit.listeners.worldedit.WESubscriber;
import com.plotsquared.bukkit.titles.DefaultTitle;
import com.plotsquared.bukkit.util.BukkitChunkManager;
import com.plotsquared.bukkit.util.BukkitCommand;
import com.plotsquared.bukkit.util.BukkitEconHandler;
import com.plotsquared.bukkit.util.BukkitEventUtil;
import com.plotsquared.bukkit.util.BukkitHybridUtils;
import com.plotsquared.bukkit.util.BukkitInventoryUtil;
import com.plotsquared.bukkit.util.BukkitSchematicHandler;
import com.plotsquared.bukkit.util.BukkitSetBlockManager;
import com.plotsquared.bukkit.util.BukkitSetupUtils;
import com.plotsquared.bukkit.util.BukkitTaskManager;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.bukkit.util.Metrics;
import com.plotsquared.bukkit.util.SendChunk;
import com.plotsquared.bukkit.util.SetBlockFast;
import com.plotsquared.bukkit.util.SetBlockFast_1_8;
import com.plotsquared.bukkit.util.SetBlockSlow;
import com.plotsquared.bukkit.util.SetGenCB;
import com.plotsquared.bukkit.uuid.DefaultUUIDWrapper;
import com.plotsquared.bukkit.uuid.FileUUIDHandler;
import com.plotsquared.bukkit.uuid.LowerOfflineUUIDWrapper;
import com.plotsquared.bukkit.uuid.OfflineUUIDWrapper;
import com.plotsquared.bukkit.uuid.SQLUUIDHandler;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class BukkitMain extends JavaPlugin implements Listener, IPlotMain {
    
    public static BukkitMain THIS;
    public static WorldEditPlugin worldEdit;
    
    private int[] version;


    @Override
    public int[] getServerVersion() {
        if (version == null) {
            try {
                version = new int[3];
                final String[] split = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
                version[0] = Integer.parseInt(split[0]);
                version[1] = Integer.parseInt(split[1]);
                if (version.length == 3) {
                    version[2] = Integer.parseInt(split[2]);
                }
            } catch (Exception e) {
                e.printStackTrace();
                PS.debug(StringMan.getString(Bukkit.getBukkitVersion()));
                PS.debug(StringMan.getString(Bukkit.getBukkitVersion().split("-")[0].split("\\.")));
                return new int[] { Integer.MAX_VALUE, 0, 0 };
            }
        }
        return version;
    }
    
    @Override
    public void onEnable() {
        THIS = this;
        PS.instance = new PS(this);
    }
    
    @Override
    public void onDisable() {
        PS.get().disable();
        THIS = null;
    }
    
    @Override
    public void log(String message) {
        if (message == null) {
            return;
        }
        if (THIS != null && Bukkit.getServer().getConsoleSender() != null) {
            try {
                message = C.color(message);
                if (!Settings.CONSOLE_COLOR) {
                    message = ChatColor.stripColor(message);
                }
                Bukkit.getServer().getConsoleSender().sendMessage(message);
                return;
            }
            catch (Throwable e) {}
        }
        System.out.println(ConsoleColors.fromString(message));
    }
    
    @Override
    public void disable() {
        if (THIS != null) {
            onDisable();
        }
    }
    
    @Override
    public int[] getPluginVersion() {
        String[] split = this.getDescription().getVersion().split("\\.");
        return new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]) };
    }
    
    @Override
    public void registerCommands() {
        final BukkitCommand bcmd = new BukkitCommand();
        final PluginCommand plotCommand = getCommand("plots");
        plotCommand.setExecutor(bcmd);
        plotCommand.setAliases(Arrays.asList("p", "ps", "plotme", "plot"));
        plotCommand.setTabCompleter(bcmd);
    }
    
    @Override
    public File getDirectory() {
        return getDataFolder();
    }
    
    @Override
    public TaskManager getTaskManager() {
        return new BukkitTaskManager();
    }
    
    @Override
    public void runEntityTask() {
        log(C.PREFIX.s() + "KillAllEntities started.");
        TaskManager.runTaskRepeat(new Runnable() {
            long ticked = 0l;
            long error = 0l;
            
            @Override
            public void run() {
                if (this.ticked > 36_000L) {
                    this.ticked = 0l;
                    if (this.error > 0) {
                        log(C.PREFIX.s() + "KillAllEntities has been running for 6 hours. Errors: " + this.error);
                    }
                    this.error = 0l;
                }
                World world;
                for (final PlotWorld pw : PS.get().getPlotWorldObjects()) {
                    world = Bukkit.getWorld(pw.worldname);
                    try {
                    for (Entity entity : world.getEntities()) {
                        switch (entity.getType()) {
                            case EGG:
                            case ENDER_CRYSTAL:
                            case COMPLEX_PART:
                            case ARMOR_STAND:
                            case FISHING_HOOK:
                            case ENDER_SIGNAL:
                            case EXPERIENCE_ORB:
                            case LEASH_HITCH:
                            case FIREWORK:
                            case WEATHER:
                            case LIGHTNING:
                            case WITHER_SKULL:
                            case UNKNOWN:
                            case ITEM_FRAME:
                            case PAINTING:
                            case PLAYER: {
                                // non moving / unremovable
                                continue;
                            }
                            case THROWN_EXP_BOTTLE:
                            case SPLASH_POTION:
                            case SNOWBALL:
                            case ENDER_PEARL:
                            case ARROW: {
                                // managed elsewhere | projectile
                                continue;
                            }
                            case MINECART:
                            case MINECART_CHEST:
                            case MINECART_COMMAND:
                            case MINECART_FURNACE:
                            case MINECART_HOPPER:
                            case MINECART_MOB_SPAWNER:
                            case MINECART_TNT:
                            case BOAT: {
                                // vehicle
                                continue;
                            }
                            case SMALL_FIREBALL:
                            case FIREBALL:
                            case DROPPED_ITEM: {
                                // dropped item
                                continue;
                            }
                            case PRIMED_TNT:
                            case FALLING_BLOCK:  {
                                // managed elsewhere
                                continue;
                            }
                            case BAT:
                            case BLAZE:
                            case CAVE_SPIDER:
                            case CHICKEN:
                            case COW:
                            case CREEPER:
                            case ENDERMAN:
                            case ENDERMITE:
                            case ENDER_DRAGON:
                            case GHAST:
                            case GIANT:
                            case GUARDIAN:
                            case HORSE:
                            case IRON_GOLEM:
                            case MAGMA_CUBE:
                            case MUSHROOM_COW:
                            case OCELOT:
                            case PIG:
                            case PIG_ZOMBIE:
                            case RABBIT:
                            case SHEEP:
                            case SILVERFISH:
                            case SKELETON:
                            case SLIME:
                            case SNOWMAN:
                            case SPIDER:
                            case SQUID:
                            case VILLAGER:
                            case WITCH:
                            case WITHER:
                            case WOLF:
                            case ZOMBIE:
                            default: {
                                Location loc = entity.getLocation();
                                if (MainUtil.isPlotRoad(BukkitUtil.getLocation(loc))) {
                                    entity.remove();
                                }
                                break;
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
        }, 20);
    }
    
    @Override
    final public ChunkGenerator getDefaultWorldGenerator(final String world, final String id) {
        WorldEvents.lastWorld = world;
        if (!PS.get().setupPlotWorld(world, id)) {
            return null;
        }
        HybridGen result = new HybridGen(world);
        TaskManager.runTaskLater(new Runnable() {
            @Override
            public void run() {
                if (WorldEvents.lastWorld != null && WorldEvents.lastWorld.equals(world)) {
                    WorldEvents.lastWorld = null;
                }
            }
        }, 20);
        return result;
    }
    
    @Override
    public void registerPlayerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
        if (PS.get().checkVersion(this.getServerVersion(), 1, 8, 0)) {
            getServer().getPluginManager().registerEvents(new PlayerEvents_1_8(), this);
        }
        if (PS.get().checkVersion(this.getServerVersion(), 1, 8, 3)) {
            getServer().getPluginManager().registerEvents(new PlayerEvents_1_8_3(), this);
        }
    }
    
    @Override
    public void registerInventoryEvents() {
        
        // Part of PlayerEvents - can be moved if necessary
        
    }
    
    @Override
    public void registerPlotPlusEvents() {
        PlotPlusListener.startRunnable(this);
        getServer().getPluginManager().registerEvents(new PlotPlusListener(), this);
    }
    
    @Override
    public void registerForceFieldEvents() {
        getServer().getPluginManager().registerEvents(new ForceFieldListener(), this);
    }
    
    @Override
    public void registerWorldEditEvents() {
        if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            BukkitMain.worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
            final String version = BukkitMain.worldEdit.getDescription().getVersion();
            if ((version != null) && version.startsWith("5.")) {
                log("&cThis version of WorldEdit does not support PlotSquared.");
                log("&cPlease use WorldEdit 6+ for masking support");
                log("&c - http://builds.enginehub.org/job/worldedit");
            } else {
                getServer().getPluginManager().registerEvents(new WEListener(), this);
                WorldEdit.getInstance().getEventBus().register(new WESubscriber());
                MainCommand.getInstance().createCommand(new WE_Anywhere());
            }
        }
    }
    
    @Override
    public EconHandler getEconomyHandler() {
        try {
            BukkitEconHandler econ = new BukkitEconHandler();
            if (econ.init()) {
                return econ;
            }
        } catch (Throwable e) {
        }
        return null;
    }
    
    @Override
    public BlockManager initBlockManager() {
        if (PS.get().checkVersion(this.getServerVersion(), 1, 8, 0)) {
            try {
                BukkitSetBlockManager.setBlockManager = new SetBlockFast_1_8();
            } catch (final Throwable e) {
                e.printStackTrace();
                BukkitSetBlockManager.setBlockManager = new SetBlockSlow();
            }
            try {
                new SendChunk();
                MainUtil.canSendChunk = true;
            } catch (final Throwable e) {
                MainUtil.canSendChunk = false;
            }
        } else {
            try {
                BukkitSetBlockManager.setBlockManager = new SetBlockFast();
            } catch (final Throwable e) {
                MainUtil.canSetFast = false;
                BukkitSetBlockManager.setBlockManager = new SetBlockSlow();
            }
        }
        BlockUpdateUtil.setBlockManager = BukkitSetBlockManager.setBlockManager;
        return BlockManager.manager = new BukkitUtil();
    }
    
    @Override
    public boolean initPlotMeConverter() {
        TaskManager.runTaskLaterAsync(new Runnable() {
            @Override
            public void run() {
                if (new LikePlotMeConverter("PlotMe").run(new ClassicPlotMeConnector())) return;
                if (new LikePlotMeConverter("PlotMe").run(new PlotMeConnector_017())) return;
                if (new LikePlotMeConverter("AthionPlots").run(new ClassicPlotMeConnector())) return;
            }
        }, 20);
        return Bukkit.getPluginManager().getPlugin("PlotMe") != null || Bukkit.getPluginManager().getPlugin("AthionPlots") != null;
    }
    
    @Override
    public BukkitGeneratorWrapper getGenerator(final String world, final String name) {
        if (name == null) {
            return new BukkitGeneratorWrapper(world, null);
        }
        final Plugin gen_plugin = Bukkit.getPluginManager().getPlugin(name);
        ChunkGenerator gen;
        if ((gen_plugin != null) && gen_plugin.isEnabled()) {
            gen = gen_plugin.getDefaultWorldGenerator(world, "");
        } else {
            gen = new HybridGen(world);
        }
        return new BukkitGeneratorWrapper(world, gen);
    }
    
    @Override
    public HybridUtils initHybridUtils() {
        return new BukkitHybridUtils();
    }
    
    @Override
    public SetupUtils initSetupUtils() {
        return new BukkitSetupUtils();
    }
    
    @Override
    public UUIDHandlerImplementation initUUIDHandler() {
        final boolean checkVersion = PS.get().checkVersion(this.getServerVersion(), 1, 7, 6);
        UUIDWrapper wrapper;
        if (Settings.OFFLINE_MODE) {
            if (Settings.UUID_LOWERCASE) {
                wrapper = (new LowerOfflineUUIDWrapper());
            } else {
                wrapper = (new OfflineUUIDWrapper());
            }
            Settings.OFFLINE_MODE = true;
        } else if (checkVersion) {
            wrapper = (new DefaultUUIDWrapper());
            Settings.OFFLINE_MODE = false;
        } else {
            if (Settings.UUID_LOWERCASE) {
                wrapper = (new LowerOfflineUUIDWrapper());
            } else {
                wrapper = (new OfflineUUIDWrapper());
            }
            Settings.OFFLINE_MODE = true;
        }
        if (!checkVersion) {
            log(C.PREFIX.s() + " &c[WARN] Titles are disabled - please update your version of Bukkit to support this feature.");
            Settings.TITLES = false;
            FlagManager.removeFlag(FlagManager.getFlag("titles"));
        } else {
            AbstractTitle.TITLE_CLASS = new DefaultTitle();
            if (wrapper instanceof DefaultUUIDWrapper) {
                Settings.TWIN_MODE_UUID = true;
            } else if (wrapper.getClass() == OfflineUUIDWrapper.class && !Bukkit.getOnlineMode()) {
                Settings.TWIN_MODE_UUID = true;
            }
        }
        if (Settings.OFFLINE_MODE) {
            log(C.PREFIX.s() + " &6PlotSquared is using Offline Mode UUIDs either because of user preference, or because you are using an old version of Bukkit");
        } else {
            log(C.PREFIX.s() + " &6PlotSquared is using online UUIDs");
        }
        return Settings.USE_SQLUUIDHANDLER ? new SQLUUIDHandler(wrapper) : new FileUUIDHandler(wrapper);
    }
    
    @Override
    public ChunkManager initChunkManager() {
        return new BukkitChunkManager();
    }
    
    @Override
    public EventUtil initEventUtil() {
        return new BukkitEventUtil();
    }
    
    @Override
    public void registerTNTListener() {
        getServer().getPluginManager().registerEvents(new TNTListener(), this);
    }
    
    @Override
    public void unregister(PlotPlayer player) {
        BukkitUtil.removePlayer(player.getName());
    }
    
    @Override
    public void registerChunkProcessor() {
        getServer().getPluginManager().registerEvents(new ChunkListener(), this);
    }
    
    @Override
    public void registerWorldEvents() {
        getServer().getPluginManager().registerEvents(new WorldEvents(), this);
    }
    
    @Override
    public InventoryUtil initInventoryUtil() {
        return new BukkitInventoryUtil();
    }

    @Override
    public String getServerName() {
        return Bukkit.getServerName();
    }

    @Override
    public void startMetrics() {
        try {
            final Metrics metrics = new Metrics(this);
            metrics.start();
            log(C.PREFIX.s() + "&6Metrics enabled.");
        } catch (final Exception e) {
            log(C.PREFIX.s() + "&cFailed to load up metrics.");
        }
    }

    @Override
    public void setGenerator(String world) {
        try {
            SetGenCB.setGenerator(BukkitUtil.getWorld(world));
        } catch (Exception e) {
            log("Failed to reload world: " + world);
            Bukkit.getServer().unloadWorld(world, false);
        }
    }

    @Override
    public SchematicHandler initSchematicHandler() {
        return new BukkitSchematicHandler();
    }

    @Override
    public AbstractTitle initTitleManager() {
        // Already initialized in UUID handler
        return AbstractTitle.TITLE_CLASS;
    }

    @Override
    public PlotPlayer wrapPlayer(Object obj) {
        if (obj instanceof Player) {
            return BukkitUtil.getPlayer((Player) obj);
        }
        else if (obj instanceof OfflinePlayer) {
            return BukkitUtil.getPlayer((OfflinePlayer) obj);
        }
        else if (obj instanceof String) {
            return UUIDHandler.getPlayer((String) obj);
        }
        else if (obj instanceof UUID) {
            return UUIDHandler.getPlayer((UUID) obj);
        }
        return null;
    }

    @Override
    public String getNMSPackage() {
        final Server server = Bukkit.getServer();
        final Class<?> bukkitServerClass = server.getClass();
        String[] pas = bukkitServerClass.getName().split("\\.");
        if (pas.length == 5) {
            final String verB = pas[3];
            return verB;
        }
        try {
            final Method getHandle = bukkitServerClass.getDeclaredMethod("getHandle");
            final Object handle = getHandle.invoke(server);
            final Class handleServerClass = handle.getClass();
            pas = handleServerClass.getName().split("\\.");
            if (pas.length == 5) {
                final String verM = pas[3];
                    return verM;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        PS.debug("Unknown NMS package: " + StringMan.getString(pas));
        return "1_8_R3";
    }
}
