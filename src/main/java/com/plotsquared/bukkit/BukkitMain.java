package com.plotsquared.bukkit;

import com.intellectualcrafters.plot.IPlotMain;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.*;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.plotsquared.bukkit.commands.BukkitCommand;
import com.plotsquared.bukkit.database.plotme.ClassicPlotMeConnector;
import com.plotsquared.bukkit.database.plotme.LikePlotMeConverter;
import com.plotsquared.bukkit.database.plotme.PlotMeConnector_017;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.BukkitHybridUtils;
import com.plotsquared.bukkit.generator.BukkitGeneratorWrapper;
import com.plotsquared.bukkit.generator.HybridGen;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.plotsquared.bukkit.listeners.*;
import com.plotsquared.bukkit.listeners.worldedit.WEListener;
import com.plotsquared.bukkit.listeners.worldedit.WESubscriber;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.plotsquared.bukkit.titles.AbstractTitle;
import com.plotsquared.bukkit.titles.DefaultTitle;
import com.intellectualcrafters.plot.util.*;
import com.intellectualcrafters.plot.uuid.DefaultUUIDWrapper;
import com.intellectualcrafters.plot.uuid.LowerOfflineUUIDWrapper;
import com.intellectualcrafters.plot.uuid.OfflineUUIDWrapper;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.bukkit.util.SetupUtils;
import com.plotsquared.bukkit.util.bukkit.*;
import com.plotsquared.bukkit.util.bukkit.uuid.FileUUIDHandler;
import com.plotsquared.bukkit.util.bukkit.uuid.SQLUUIDHandler;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BukkitMain extends JavaPlugin implements Listener, IPlotMain {
    
    public static BukkitMain THIS;
    
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
                return null;
            }
        }
        return version;
    }
    
    @Override
    public void onEnable() {
        THIS = this;
        PS.instance = new PS(this);
        if (Settings.METRICS) {
            try {
                final Metrics metrics = new Metrics(this);
                metrics.start();
                log(C.PREFIX.s() + "&6Metrics enabled.");
            } catch (final Exception e) {
                log(C.PREFIX.s() + "&cFailed to load up metrics.");
            }
        } else {
            log("&dUsing metrics will allow us to improve the plugin, please consider it :)");
        }
        List<World> worlds = Bukkit.getWorlds();
        if (worlds.size() > 0) {
            UUIDHandler.startCaching(null);
            for (World world : worlds) {
                try {
                    SetGenCB.setGenerator(world);
                } catch (Exception e) {
                    log("Failed to reload world: " + world.getName());
                    Bukkit.getServer().unloadWorld(world, false);
                }
            }
        }
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
            catch (Throwable e) {};
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
    public void handleKick(UUID uuid, C c) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            MainUtil.sendMessage(BukkitUtil.getPlayer(player), c);
            player.teleport(player.getWorld().getSpawnLocation());
        }
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
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
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
            PS.get().worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
            final String version = PS.get().worldEdit.getDescription().getVersion();
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
            } else if (wrapper instanceof OfflineUUIDWrapper && !Bukkit.getOnlineMode()) {
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
    public APlotListener initPlotListener() {
        return new PlotListener();
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
    public PlayerManager initPlayerManager() {
        return new BukkitPlayerManager();
    }
    
    @Override
    public InventoryUtil initInventoryUtil() {
        return new BukkitInventoryUtil();
    }

    @Override
    public String getServerName() {
        return Bukkit.getServerName();
    }
}
