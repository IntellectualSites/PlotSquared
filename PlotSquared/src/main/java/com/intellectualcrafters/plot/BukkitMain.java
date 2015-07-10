package com.intellectualcrafters.plot;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;

import com.intellectualcrafters.plot.commands.Add;
import com.intellectualcrafters.plot.commands.Auto;
import com.intellectualcrafters.plot.commands.BukkitCommand;
import com.intellectualcrafters.plot.commands.Chat;
import com.intellectualcrafters.plot.commands.Claim;
import com.intellectualcrafters.plot.commands.Clear;
import com.intellectualcrafters.plot.commands.Cluster;
import com.intellectualcrafters.plot.commands.Comment;
import com.intellectualcrafters.plot.commands.Condense;
import com.intellectualcrafters.plot.commands.Confirm;
import com.intellectualcrafters.plot.commands.Copy;
import com.intellectualcrafters.plot.commands.CreateRoadSchematic;
import com.intellectualcrafters.plot.commands.Database;
import com.intellectualcrafters.plot.commands.Debug;
import com.intellectualcrafters.plot.commands.DebugClaimTest;
import com.intellectualcrafters.plot.commands.DebugClear;
import com.intellectualcrafters.plot.commands.DebugExec;
import com.intellectualcrafters.plot.commands.DebugFill;
import com.intellectualcrafters.plot.commands.DebugFixFlags;
import com.intellectualcrafters.plot.commands.DebugLoadTest;
import com.intellectualcrafters.plot.commands.DebugRoadRegen;
import com.intellectualcrafters.plot.commands.DebugSaveTest;
import com.intellectualcrafters.plot.commands.DebugUUID;
import com.intellectualcrafters.plot.commands.Delete;
import com.intellectualcrafters.plot.commands.Deny;
import com.intellectualcrafters.plot.commands.Disable;
import com.intellectualcrafters.plot.commands.Download;
import com.intellectualcrafters.plot.commands.FlagCmd;
import com.intellectualcrafters.plot.commands.Help;
import com.intellectualcrafters.plot.commands.Home;
import com.intellectualcrafters.plot.commands.Inbox;
import com.intellectualcrafters.plot.commands.Info;
import com.intellectualcrafters.plot.commands.Inventory;
import com.intellectualcrafters.plot.commands.Kick;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.commands.Merge;
import com.intellectualcrafters.plot.commands.Move;
import com.intellectualcrafters.plot.commands.MusicSubcommand;
import com.intellectualcrafters.plot.commands.Purge;
import com.intellectualcrafters.plot.commands.Rate;
import com.intellectualcrafters.plot.commands.RegenAllRoads;
import com.intellectualcrafters.plot.commands.Reload;
import com.intellectualcrafters.plot.commands.Remove;
import com.intellectualcrafters.plot.commands.SchematicCmd;
import com.intellectualcrafters.plot.commands.Set;
import com.intellectualcrafters.plot.commands.SetOwner;
import com.intellectualcrafters.plot.commands.Setup;
import com.intellectualcrafters.plot.commands.Swap;
import com.intellectualcrafters.plot.commands.TP;
import com.intellectualcrafters.plot.commands.Target;
import com.intellectualcrafters.plot.commands.Template;
import com.intellectualcrafters.plot.commands.Toggle;
import com.intellectualcrafters.plot.commands.Trim;
import com.intellectualcrafters.plot.commands.Trust;
import com.intellectualcrafters.plot.commands.Unclaim;
import com.intellectualcrafters.plot.commands.Undeny;
import com.intellectualcrafters.plot.commands.Unlink;
import com.intellectualcrafters.plot.commands.Untrust;
import com.intellectualcrafters.plot.commands.Update;
import com.intellectualcrafters.plot.commands.Visit;
import com.intellectualcrafters.plot.commands.WE_Anywhere;
import com.intellectualcrafters.plot.commands.list;
import com.intellectualcrafters.plot.commands.plugin;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.plotme.ClassicPlotMeConnector;
import com.intellectualcrafters.plot.database.plotme.LikePlotMeConverter;
import com.intellectualcrafters.plot.database.plotme.PlotMeConnector_017;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.BukkitHybridUtils;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.listeners.APlotListener;
import com.intellectualcrafters.plot.listeners.ChunkListener;
import com.intellectualcrafters.plot.listeners.ForceFieldListener;
import com.intellectualcrafters.plot.listeners.InventoryListener;
import com.intellectualcrafters.plot.listeners.PlayerEvents;
import com.intellectualcrafters.plot.listeners.PlayerEvents_1_8;
import com.intellectualcrafters.plot.listeners.PlayerEvents_1_8_3;
import com.intellectualcrafters.plot.listeners.PlotListener;
import com.intellectualcrafters.plot.listeners.PlotPlusListener;
import com.intellectualcrafters.plot.listeners.TNTListener;
import com.intellectualcrafters.plot.listeners.WorldEvents;
import com.intellectualcrafters.plot.listeners.worldedit.WEListener;
import com.intellectualcrafters.plot.listeners.worldedit.WESubscriber;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.titles.AbstractTitle;
import com.intellectualcrafters.plot.titles.DefaultTitle;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.BlockUpdateUtil;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.ConsoleColors;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.InventoryUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlayerManager;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitChunkManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitEconHandler;
import com.intellectualcrafters.plot.util.bukkit.BukkitEventUtil;
import com.intellectualcrafters.plot.util.bukkit.BukkitInventoryUtil;
import com.intellectualcrafters.plot.util.bukkit.BukkitPlayerManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitSetBlockManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitSetupUtils;
import com.intellectualcrafters.plot.util.bukkit.BukkitTaskManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import com.intellectualcrafters.plot.util.bukkit.Metrics;
import com.intellectualcrafters.plot.util.bukkit.SendChunk;
import com.intellectualcrafters.plot.util.bukkit.SetBlockFast;
import com.intellectualcrafters.plot.util.bukkit.SetBlockFast_1_8;
import com.intellectualcrafters.plot.util.bukkit.SetBlockSlow;
import com.intellectualcrafters.plot.util.bukkit.SetGenCB;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;
import com.intellectualcrafters.plot.uuid.DefaultUUIDWrapper;
import com.intellectualcrafters.plot.uuid.LowerOfflineUUIDWrapper;
import com.intellectualcrafters.plot.uuid.OfflineUUIDWrapper;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

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
            } catch (Exception e) {
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
        File file = new File(this.getDirectory() + File.separator + "disabled.yml");
        if (file.exists()) {
            try {
                String[] split = new String(Files.readAllBytes(file.toPath())).split(",");
                for (String plugin : split) {
                    loadPlugin(plugin);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            file.delete();
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
        try {
            unloadRecursively(this);
        }
        catch (Exception e) {};
        THIS = null;
    }
    
    @Override
    public void log(String message) {
        if (message == null) {
            return;
        }
        message = message.replaceAll("\u00B2", "2");
        if (THIS != null && Bukkit.getServer().getConsoleSender() != null) {
            try {
                message = ChatColor.translateAlternateColorCodes('&', message);
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
        MainCommand.subCommands.add(new Download());
        MainCommand.subCommands.add(new Disable());
        MainCommand.subCommands.add(new Update());
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
    
    public boolean unloadPlugin(Plugin plugin) {
        try {
            plugin.getClass().getClassLoader().getResources("*");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        PluginManager pm = Bukkit.getServer().getPluginManager();
        Map<String, Plugin> ln;
        List<Plugin> pl;
        try {
            Field lnF = pm.getClass().getDeclaredField("lookupNames");
            lnF.setAccessible(true);
            ln = (Map) lnF.get(pm);
            
            Field plF = pm.getClass().getDeclaredField("plugins");
            plF.setAccessible(true);
            pl = (List) plF.get(pm);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        pm.disablePlugin(plugin);
        synchronized (pm) {
            ln.remove(plugin.getName());
            pl.remove(plugin);
        }
        JavaPluginLoader jpl = (JavaPluginLoader) plugin.getPluginLoader();
        Field loadersF = null;
        try {
            loadersF = jpl.getClass().getDeclaredField("loaders");
            loadersF.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Map<String, ?> loaderMap = (Map) loadersF.get(jpl);
            loaderMap.remove(plugin.getDescription().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeClassLoader(plugin);
        System.gc();
        System.gc();
        return true;
    }
    
    public boolean closeClassLoader(Plugin plugin) {
        try {
            ((URLClassLoader) plugin.getClass().getClassLoader()).close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean unloadRecursively(Plugin plugin) {
        try {
            Stack<String> pluginFiles = unloadRecursively(plugin.getName(), plugin, new Stack());
            File file = new File(this.getDirectory() + File.separator + "disabled.yml");
            file.createNewFile();
            String prefix = "";
            String all = "";
            while (pluginFiles.size() > 0) {
                String pop = pluginFiles.pop();
                all += prefix + pop.substring(0, pop.length() - 4);
                prefix = ",";
            }
            if (all.length() != 0) {
                PrintWriter out = new PrintWriter(this.getDirectory() + File.separator + "disabled.yml");
                out.write(all);
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    
    @Override
    public void loadPlugin(String name) {
        try {
            PluginManager manager = Bukkit.getServer().getPluginManager();
            Plugin plugin = manager.getPlugin(name);
            if (plugin != null) {
                manager.enablePlugin(plugin);
                return;
            }
            plugin = manager.loadPlugin(new File("plugins" + File.separator + name + (name.endsWith(".jar") ? "" : ".jar")));
            plugin.onLoad();
            manager.enablePlugin(plugin);
        } catch (Exception e) {}
    }
    
    @Override
    public File getFile() {
        return getFile(this);
    }
    
    public File getFile(JavaPlugin p) {
        try {
            Field f = JavaPlugin.class.getDeclaredField("file");
            f.setAccessible(true);
            return (File) f.get(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Stack<String> unloadRecursively(String doNotLoad, Plugin plugin, Stack<String> pluginFiles) {
        if (!plugin.getName().equals(doNotLoad)) {
            File file = getFile((JavaPlugin) plugin);
            pluginFiles.push(file.getName());
        }
        PluginManager pm = Bukkit.getPluginManager();
        for (Plugin p : pm.getPlugins()) {
            List<String> depend = p.getDescription().getDepend();
            if (depend != null) {
                for (String s : depend) {
                    if (s.equals(plugin.getName())) {
                        unloadRecursively(doNotLoad, p, pluginFiles);
                    }
                }
            }
            List<String> softDepend = p.getDescription().getSoftDepend();
            if (softDepend != null) {
                for (String s : softDepend) {
                    if (s.equals(plugin.getName())) {
                        unloadRecursively(doNotLoad, p, pluginFiles);
                    }
                }
            }
        }
        if (unloadPlugin(plugin)) {
            List<String> depend = plugin.getDescription().getDepend();
            if (depend != null) {
                for (String s : depend) {
                    Plugin p = pm.getPlugin(s);
                    if (p != null) {
                        unloadRecursively(doNotLoad, p, pluginFiles);
                    }
                }
            }
            List<String> softDepend = plugin.getDescription().getSoftDepend();
            if (softDepend != null) {
                for (String s : softDepend) {
                    Plugin p = pm.getPlugin(s);
                    if (p != null) {
                        unloadRecursively(doNotLoad, p, pluginFiles);
                    }
                }
            }
        }
        return pluginFiles;
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
                if (new LikePlotMeConverter("PlotMe").run(new ClassicPlotMeConnector())) return;
                if (new LikePlotMeConverter("PlotMe").run(new PlotMeConnector_017())) return;
                if (new LikePlotMeConverter("AthionPlots").run(new ClassicPlotMeConnector())) return;
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
            } else {
                UUIDHandler.uuidWrapper = new OfflineUUIDWrapper();
            }
            Settings.OFFLINE_MODE = true;
        } else if (checkVersion) {
            UUIDHandler.uuidWrapper = new DefaultUUIDWrapper();
            Settings.OFFLINE_MODE = false;
        } else {
            if (Settings.UUID_LOWERCASE) {
                UUIDHandler.uuidWrapper = new LowerOfflineUUIDWrapper();
            } else {
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
            } else if (UUIDHandler.uuidWrapper instanceof OfflineUUIDWrapper && !Bukkit.getOnlineMode()) {
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
