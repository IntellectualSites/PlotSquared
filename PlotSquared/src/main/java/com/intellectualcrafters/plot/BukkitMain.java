package com.intellectualcrafters.plot;

import java.io.File;
import java.util.Arrays;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.intellectualcrafters.plot.commands.Buy;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.commands.WE_Anywhere;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.PlotMeConverter;
import com.intellectualcrafters.plot.events.PlayerTeleportToPlotEvent;
import com.intellectualcrafters.plot.events.PlotDeleteEvent;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.listeners.ForceFieldListener;
import com.intellectualcrafters.plot.listeners.InventoryListener;
import com.intellectualcrafters.plot.listeners.PlayerEvents;
import com.intellectualcrafters.plot.listeners.PlayerEvents_1_8;
import com.intellectualcrafters.plot.listeners.PlotListener;
import com.intellectualcrafters.plot.listeners.PlotPlusListener;
import com.intellectualcrafters.plot.listeners.WorldEditListener;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.ConsoleColors;
import com.intellectualcrafters.plot.util.Metrics;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.SendChunk;
import com.intellectualcrafters.plot.util.SetBlockFast;
import com.intellectualcrafters.plot.util.SetBlockFast_1_8;
import com.intellectualcrafters.plot.util.SetBlockManager;
import com.intellectualcrafters.plot.util.SetBlockSlow;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.bukkit.BukkitTaskManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class BukkitMain extends JavaPlugin implements Listener,IPlotMain {
    
    public static BukkitMain THIS = null;
    public static PlotSquared MAIN = null;

    // TODO restructure this
    public static boolean hasPermission(final Player player, final String perm) {
        if ((player == null) || player.isOp() || player.hasPermission(PlotSquared.ADMIN_PERMISSION)) {
            return true;
        }
        if (player.hasPermission(perm)) {
            return true;
        }
        final String[] nodes = perm.split("\\.");
        final StringBuilder n = new StringBuilder();
        for (int i = 0; i < (nodes.length - 1); i++) {
            n.append(nodes[i] + ("."));
            if (player.hasPermission(n + "*")) {
                return true;
            }
        }
        return false;
    }
    
    // TODO restructure this
    public static boolean teleportPlayer(final Player player, final Location from, final Plot plot) {
        Plot bot = PlayerFunctions.getBottomPlot(player.getWorld().getName(), plot);
        final PlayerTeleportToPlotEvent event = new PlayerTeleportToPlotEvent(player, from, bot);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            final Location location = PlotHelper.getPlotHome(bot.world, bot);
            
            int x = location.getX();
            int z = location.getZ();
            
            
            if ((x >= 29999999) || (x <= -29999999) || (z >= 299999999) || (z <= -29999999)) {
                event.setCancelled(true);
                return false;
            }
            if (Settings.TELEPORT_DELAY == 0 || hasPermission(player, "plots.teleport.delay.bypass")) {
                PlayerFunctions.sendMessage(player, C.TELEPORTED_TO_PLOT);
                BukkitUtil.teleportPlayer(player, location);
                return true;
            }
            PlayerFunctions.sendMessage(player, C.TELEPORT_IN_SECONDS, Settings.TELEPORT_DELAY + "");
            final String name = player.getName();
            TaskManager.TELEPORT_QUEUE.add(name);
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    if (!TaskManager.TELEPORT_QUEUE.contains(name)) {
                        PlayerFunctions.sendMessage(player, C.TELEPORT_FAILED);
                        return;
                    }
                    TaskManager.TELEPORT_QUEUE.remove(name);
                    if (!player.isOnline()) {
                        return;
                    }
                    PlayerFunctions.sendMessage(player, C.TELEPORTED_TO_PLOT);
                    BukkitUtil.teleportPlayer(player, location);
                }
            }, Settings.TELEPORT_DELAY * 20);
            return true;
        }
        return !event.isCancelled();
    }
    
    @EventHandler
    public static void worldLoad(WorldLoadEvent event) {
        UUIDHandler.cacheAll();
    }

    @EventHandler
    public void PlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message.toLowerCase().startsWith("/plotme")) {
            Plugin plotme = Bukkit.getPluginManager().getPlugin("PlotMe");
            if (plotme == null) {
                Player player = event.getPlayer();
                if (Settings.USE_PLOTME_ALIAS) {
                    player.performCommand(message.replace("/plotme", "plots"));
                } else {
                    PlayerFunctions.sendMessage(player, C.NOT_USING_PLOTME);
                }
                event.setCancelled(true);
            }
        }
    }
    
    @Override
    public void onEnable() {
        MAIN = new PlotSquared(this);
        THIS = this;
        
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
        
        getServer().getPluginManager().registerEvents(this, this);
    }
    
    @Override
    public void onDisable() {
        MAIN.disable();
        MAIN = null;
        THIS = null;
    }

    @Override
    public void log(String message) {
        if (THIS == null || Bukkit.getServer().getConsoleSender() == null) {
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
    public void registerCommands() {
        final MainCommand command = new MainCommand();
        final PluginCommand plotCommand = getCommand("plots");
        plotCommand.setExecutor(command);
        plotCommand.setAliases(Arrays.asList("p", "ps", "plotme", "plot"));
        plotCommand.setTabCompleter(command);
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
                for (final String w : PlotSquared.getPlotWorlds()) {
                    world = Bukkit.getWorld(w);
                    try {
                        if (world.getLoadedChunks().length < 1) {
                            continue;
                        }
                        for (final Chunk chunk : world.getLoadedChunks()) {
                            final Entity[] entities = chunk.getEntities();
                            Entity entity;
                            for (int i = entities.length - 1; i >= 0; i--) {
                                if (!((entity = entities[i]) instanceof Player) && !PlotListener.isInPlot(entity.getLocation())) {
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
        if (!PlotSquared.setupPlotWorld(world, id)) {
            return null;
        }
        return new HybridGen(world);
    }
    
    public static boolean checkVersion(int major, int minor, int minor2) {
        try {
            String[] version = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
            int a = Integer.parseInt(version[0]);
            int b = Integer.parseInt(version[1]);
            int c = 0;
            if (version.length == 3) {
                c = Integer.parseInt(version[2]);
            }
            if (a > major || (a == major && b > minor) || (a == major && b == minor && c >= minor2)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void registerPlayerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
        if (checkVersion(1, 8, 0)) {
            getServer().getPluginManager().registerEvents(new PlayerEvents_1_8(), this);
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
            PlotSquared.worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
            final String version = PlotSquared.worldEdit.getDescription().getVersion();
            if ((version != null) && version.startsWith("5.")) {
                log("&cThis version of WorldEdit does not support PlotSquared.");
                log("&cPlease use WorldEdit 6+ for masking support");
                log("&c - http://builds.enginehub.org/job/worldedit");
            } else {
                getServer().getPluginManager().registerEvents(new WorldEditListener(), this);
                MainCommand.subCommands.add(new WE_Anywhere());
            }
        }
    }

    @Override
    public Economy getEconomy() {
        if ((getServer().getPluginManager().getPlugin("Vault") != null) && getServer().getPluginManager().getPlugin("Vault").isEnabled()) {
            final RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                MainCommand.subCommands.add(new Buy());
                return economyProvider.getProvider();
            }
        }
        return null;
    }

    @Override
    public void initSetBlockManager() {
        if (checkVersion(1, 8, 0)) {
            try {
                SetBlockManager.setBlockManager = new SetBlockFast_1_8();
            }
            catch (Throwable e) {
                e.printStackTrace();
                SetBlockManager.setBlockManager = new SetBlockSlow();
            }
        }
        else {
            try {
                SetBlockManager.setBlockManager = new SetBlockFast();
            } catch (Throwable e) {
                SetBlockManager.setBlockManager = new SetBlockSlow();
            }
        }
        try {
            new SendChunk();
            PlotHelper.canSendChunk = true;
        } catch (final Throwable e) {
            PlotHelper.canSendChunk = false;
        }
    }

    @Override
    public boolean initPlotMeConverter() {
        try {
            new PlotMeConverter().runAsync();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        if (Bukkit.getPluginManager().getPlugin("PlotMe") != null) {
            return true;
        }
        return false;
    }

    @Override
    public void getGenerator(String world, String name) {
        Plugin gen_plugin = Bukkit.getPluginManager().getPlugin(name);
        if (gen_plugin != null && gen_plugin.isEnabled()) {
            gen_plugin.getDefaultWorldGenerator(world, "");
        } else {
            new HybridGen(world);
        }
    }

    @Override
    public boolean callRemovePlot(String world, PlotId id) {
        final PlotDeleteEvent event = new PlotDeleteEvent(world, id);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            event.setCancelled(true);
            return false;
        }
        return true;
    }
}
