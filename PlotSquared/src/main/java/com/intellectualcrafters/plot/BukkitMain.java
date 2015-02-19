package com.intellectualcrafters.plot;

import java.io.File;
import java.util.Arrays;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.intellectualcrafters.plot.commands.Buy;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.commands.WE_Anywhere;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.listeners.ForceFieldListener;
import com.intellectualcrafters.plot.listeners.InventoryListener;
import com.intellectualcrafters.plot.listeners.PlayerEvents;
import com.intellectualcrafters.plot.listeners.PlayerEvents_1_8;
import com.intellectualcrafters.plot.listeners.PlotListener;
import com.intellectualcrafters.plot.listeners.PlotPlusListener;
import com.intellectualcrafters.plot.listeners.WorldEditListener;
import com.intellectualcrafters.plot.util.Metrics;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitTaskManager;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class BukkitMain extends JavaPlugin implements Listener,IPlotMain {
    
    public static BukkitMain THIS = null;
    public static PlotSquared MAIN = null;

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
        
        // TODO world load event
    }
    
    @Override
    public void onDisable() {
        MAIN.disable();
        MAIN = null;
        THIS = null;
    }

    @Override
    public void log(String message) {
        // TODO Auto-generated method stub
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
            WorldEditPlugin worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");

            final String version = worldEdit.getDescription().getVersion();
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
}
