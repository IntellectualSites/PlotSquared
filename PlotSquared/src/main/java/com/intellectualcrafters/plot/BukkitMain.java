package com.intellectualcrafters.plot;

import com.intellectualcrafters.plot.commands.*;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.plotme.ClassicPlotMeConnector;
import com.intellectualcrafters.plot.database.plotme.LikePlotMeConverter;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.BukkitHybridUtils;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.listeners.*;
import com.intellectualcrafters.plot.listeners.worldedit.WEListener;
import com.intellectualcrafters.plot.listeners.worldedit.WESubscriber;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.titles.AbstractTitle;
import com.intellectualcrafters.plot.titles.DefaultTitle;
import com.intellectualcrafters.plot.util.*;
import com.intellectualcrafters.plot.util.bukkit.*;
import com.intellectualcrafters.plot.uuid.DefaultUUIDWrapper;
import com.intellectualcrafters.plot.uuid.LowerOfflineUUIDWrapper;
import com.intellectualcrafters.plot.uuid.OfflineUUIDWrapper;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
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

    public static BukkitMain THIS = null;

    private int[] version;
    
    @Override
    public boolean checkVersion(final int major, final int minor, final int minor2) {
        if (version == null) {
            try {
                version = new int[3];
                final String[] split = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
                version[0] = Integer.parseInt(split[0]);
                version[1] = Integer.parseInt(split[1]);
                if (version.length == 3) {
                    version[2] = Integer.parseInt(split[2]);
                }
            }
            catch (Exception e) {
                return false;
            }
        }
        return (version[0] > major) || ((version[0] == major) && (version[1] > minor)) || ((version[0] == major) && (version[1] == minor) && (version[2] >= minor2));
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
            UUIDHandler.cacheAll(worlds.get(0).getName());
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
        message = message.replaceAll("\u00B2", "2");
        if ((THIS == null) || (Bukkit.getServer().getConsoleSender() == null)) {
            System.out.println(ChatColor.stripColor(ConsoleColors.fromString(message)));
        } else {
            message = ChatColor.translateAlternateColorCodes('&', message);
            if (!Settings.CONSOLE_COLOR) {
                message = ChatColor.stripColor(message);
            }
            Bukkit.getServer().getConsoleSender().sendMessage(message);
        }
    }

    @Override
    public void disable() {
        onDisable();
    }

    @Override
    public String getVersion() {
        return this.getDescription().getVersion();
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
        new MainCommand();
        MainCommand.subCommands.add(new Template());
        MainCommand.subCommands.add(new Setup());
        MainCommand.subCommands.add(new DebugUUID());
        MainCommand.subCommands.add(new DebugFill());
        MainCommand.subCommands.add(new DebugSaveTest());
        MainCommand.subCommands.add(new DebugLoadTest());
        MainCommand.subCommands.add(new CreateRoadSchematic());
        MainCommand.subCommands.add(new RegenAllRoads());
        MainCommand.subCommands.add(new DebugClear());
        MainCommand.subCommands.add(new Claim());
        MainCommand.subCommands.add(new Auto());
        MainCommand.subCommands.add(new Home());
        MainCommand.subCommands.add(new Visit());
        MainCommand.subCommands.add(new TP());
        MainCommand.subCommands.add(new Set());
        MainCommand.subCommands.add(new Toggle());
        MainCommand.subCommands.add(new Clear());
        MainCommand.subCommands.add(new Delete());
        MainCommand.subCommands.add(new SetOwner());
        if (Settings.ENABLE_CLUSTERS) {
            MainCommand.subCommands.add(new Cluster());
        }
        
        MainCommand.subCommands.add(new Trust());
        MainCommand.subCommands.add(new Add());
        MainCommand.subCommands.add(new Deny());
        MainCommand.subCommands.add(new Untrust());
        MainCommand.subCommands.add(new Remove());
        MainCommand.subCommands.add(new Undeny());
        
        MainCommand.subCommands.add(new Info());
        MainCommand.subCommands.add(new list());
        MainCommand.subCommands.add(new Help());
        MainCommand.subCommands.add(new Debug());
        MainCommand.subCommands.add(new SchematicCmd());
        MainCommand.subCommands.add(new plugin());
        MainCommand.subCommands.add(new Inventory());
        MainCommand.subCommands.add(new Purge());
        MainCommand.subCommands.add(new Reload());
        MainCommand.subCommands.add(new Merge());
        MainCommand.subCommands.add(new Unlink());
        MainCommand.subCommands.add(new Kick());
        MainCommand.subCommands.add(new Rate());
        MainCommand.subCommands.add(new DebugClaimTest());
        MainCommand.subCommands.add(new Inbox());
        MainCommand.subCommands.add(new Comment());
        MainCommand.subCommands.add(new Database());
        MainCommand.subCommands.add(new Unclaim());
        MainCommand.subCommands.add(new Swap());
        MainCommand.subCommands.add(new MusicSubcommand());
        MainCommand.subCommands.add(new DebugRoadRegen());
        MainCommand.subCommands.add(new Trim());
        MainCommand.subCommands.add(new DebugExec());
        MainCommand.subCommands.add(new FlagCmd());
        MainCommand.subCommands.add(new Target());
        MainCommand.subCommands.add(new DebugFixFlags());
        MainCommand.subCommands.add(new Move());
        MainCommand.subCommands.add(new Condense());
        MainCommand.subCommands.add(new Confirm());
        MainCommand.subCommands.add(new Copy());
        MainCommand.subCommands.add(new Chat());
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
                for (final String w : PS.get().getPlotWorlds()) {
                    world = Bukkit.getWorld(w);
                    try {
                        if (world.getLoadedChunks().length < 1) {
                            continue;
                        }
                        for (final Chunk chunk : world.getLoadedChunks()) {
                            final Entity[] entities = chunk.getEntities();
                            Entity entity;
                            for (int i = entities.length - 1; i >= 0; i--) {
                                if (!((entity = entities[i]) instanceof Player) && (MainUtil.getPlot(BukkitUtil.getLocation(entity)) == null)) {
                                    entity.remove();
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
        if (checkVersion(1, 8, 0)) {
            getServer().getPluginManager().registerEvents(new PlayerEvents_1_8(), this);
        }
        if (checkVersion(1, 8, 3)) {
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
                MainCommand.subCommands.add(new WE_Anywhere());
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
        if (checkVersion(1, 8, 0)) {
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
                if (!(new LikePlotMeConverter("PlotMe").run(new ClassicPlotMeConnector()))) {
                    new LikePlotMeConverter("AthionPlots").run(new ClassicPlotMeConnector());
                }
            }
        }, 20);
        return Bukkit.getPluginManager().getPlugin("PlotMe") != null || Bukkit.getPluginManager().getPlugin("AthionPlots") != null;
    }

    @Override
    public ChunkGenerator getGenerator(final String world, final String name) {
        final Plugin gen_plugin = Bukkit.getPluginManager().getPlugin(name);
        if ((gen_plugin != null) && gen_plugin.isEnabled()) {
            return gen_plugin.getDefaultWorldGenerator(world, "");
        } else {
            return new HybridGen(world);
        }
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
    public UUIDWrapper initUUIDHandler() {
        final boolean checkVersion = checkVersion(1, 7, 6);
        if (Settings.OFFLINE_MODE) {
            if (Settings.UUID_LOWERCASE) {
                UUIDHandler.uuidWrapper = new LowerOfflineUUIDWrapper();
            }
            else {
                UUIDHandler.uuidWrapper = new OfflineUUIDWrapper();
            }
            Settings.OFFLINE_MODE = true;
        } else if (checkVersion) {
            UUIDHandler.uuidWrapper = new DefaultUUIDWrapper();
            Settings.OFFLINE_MODE = false;
        } else {
            if (Settings.UUID_LOWERCASE) {
                UUIDHandler.uuidWrapper = new LowerOfflineUUIDWrapper();
            }
            else {
                UUIDHandler.uuidWrapper = new OfflineUUIDWrapper();
            }
            Settings.OFFLINE_MODE = true;
        }
        if (!checkVersion) {
            log(C.PREFIX.s() + " &c[WARN] Titles are disabled - please update your version of Bukkit to support this feature.");
            Settings.TITLES = false;
            FlagManager.removeFlag(FlagManager.getFlag("titles"));
        } else {
            AbstractTitle.TITLE_CLASS = new DefaultTitle();
            if (UUIDHandler.uuidWrapper instanceof DefaultUUIDWrapper) {
                Settings.TWIN_MODE_UUID = true;
            }
            else if (UUIDHandler.uuidWrapper instanceof OfflineUUIDWrapper && !Bukkit.getOnlineMode()) {
                Settings.TWIN_MODE_UUID = true;
            }
        }
        if (Settings.OFFLINE_MODE) {
            log(C.PREFIX.s() + " &6PlotSquared is using Offline Mode UUIDs either because of user preference, or because you are using an old version of Bukkit");
        } else {
            log(C.PREFIX.s() + " &6PlotSquared is using online UUIDs");
        }
        return UUIDHandler.uuidWrapper;
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
}
