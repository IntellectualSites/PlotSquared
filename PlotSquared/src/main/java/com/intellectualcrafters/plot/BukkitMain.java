package com.intellectualcrafters.plot;

import com.intellectualcrafters.plot.commands.Buy;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.commands.WE_Anywhere;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.PlotMeConverter;
import com.intellectualcrafters.plot.events.PlayerTeleportToPlotEvent;
import com.intellectualcrafters.plot.events.PlotDeleteEvent;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.listeners.*;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ConsoleColors;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.*;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
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

import java.io.File;
import java.util.Arrays;

public class BukkitMain extends JavaPlugin implements Listener, IPlotMain {
    public static BukkitMain THIS = null;
    public static PlotSquared MAIN = null;
    
    @EventHandler
    public static void worldLoad(final WorldLoadEvent event) {
        UUIDHandler.cacheAll();
    }

    public static boolean checkVersion(final int major, final int minor, final int minor2) {
        try {
            final String[] version = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
            final int a = Integer.parseInt(version[0]);
            final int b = Integer.parseInt(version[1]);
            int c = 0;
            if (version.length == 3) {
                c = Integer.parseInt(version[2]);
            }
            if ((a > major) || ((a == major) && (b > minor)) || ((a == major) && (b == minor) && (c >= minor2))) {
                return true;
            }
            return false;
        } catch (final Exception e) {
            return false;
        }
    }
    
    @EventHandler
    public void PlayerCommand(final PlayerCommandPreprocessEvent event) {
        final String message = event.getMessage();
        if (message.toLowerCase().startsWith("/plotme")) {
            final Plugin plotme = Bukkit.getPluginManager().getPlugin("PlotMe");
            if (plotme == null) {
                final Player player = event.getPlayer();
                if (Settings.USE_PLOTME_ALIAS) {
                    player.performCommand(message.replace("/plotme", "plots"));
                } else {
                    BukkitPlayerFunctions.sendMessage(player, C.NOT_USING_PLOTME);
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
        if (!PlotSquared.setupPlotWorld(world, id)) {
            return null;
        }
        return new HybridGen(world);
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
            } catch (final Throwable e) {
                e.printStackTrace();
                SetBlockManager.setBlockManager = new SetBlockSlow();
            }
        } else {
            try {
                SetBlockManager.setBlockManager = new SetBlockFast();
            } catch (final Throwable e) {
                SetBlockManager.setBlockManager = new SetBlockSlow();
            }
        }
        try {
            new SendChunk();
            MainUtil.canSendChunk = true;
        } catch (final Throwable e) {
            MainUtil.canSendChunk = false;
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
    public void getGenerator(final String world, final String name) {
        final Plugin gen_plugin = Bukkit.getPluginManager().getPlugin(name);
        if ((gen_plugin != null) && gen_plugin.isEnabled()) {
            gen_plugin.getDefaultWorldGenerator(world, "");
        } else {
            new HybridGen(world);
        }
    }
    
    @Override
    public boolean callRemovePlot(final String world, final PlotId id) {
        final PlotDeleteEvent event = new PlotDeleteEvent(world, id);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            event.setCancelled(true);
            return false;
        }
        return true;
    }
}
