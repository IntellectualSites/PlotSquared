package com.plotsquared.sponge;

import com.google.inject.Inject;
import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.IPlotMain;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.*;
import com.plotsquared.bukkit.listeners.APlotListener;
import com.plotsquared.bukkit.util.SetupUtils;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.entity.player.gamemode.GameModes;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerChatEvent;
import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.event.state.ServerAboutToStartEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.World;

import java.io.File;
import java.util.Collection;
import java.util.UUID;

/**
 * Created by robin on 01/11/2014
 */

@Plugin(id = "PlotSquared", name = "PlotSquared", version = "3.0.0")
public class SpongeMain implements IPlotMain, PluginContainer {
    public static SpongeMain THIS;
    
    @Inject private Logger logger;
    @Inject private Game game;
    private Server server;
    
    private WorldModify modify;

    ////////////////////// SPONGE PLUGIN REGISTRATION ////////////////////
    @Override
    public String getId() {
        return "PlotSquared";
    }

    @Override
    public Object getInstance() {
        return THIS;
    }

    @Override
    public String getName() {
        return "PlotSquared";
    }

    @Override
    public String getVersion() {
        int[] version = PS.get().getVersion();
        String result = "";
        String prefix = "";
        for (int i : version) {
            result += prefix + i;
            prefix = ".";
        }
        return result;
    }
    ///////////////////////////////////////////////////////////////////////
    
    ///////////////////// ON ENABLE /////////////////////  
    @Subscribe
    public void onInit(PreInitializationEvent event) {
        log("PRE INIT");
    }
    
    @Subscribe
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        log("INIT");
        THIS = this;
        PS.instance = new PS(this);
        
        // Setup metrics
        if (Settings.METRICS) {
            try {
                final SpongeMetrics metrics = new SpongeMetrics(game, this);
                metrics.start();
                log(C.PREFIX.s() + "&6Metrics enabled.");
            } catch (final Exception e) {
                log(C.PREFIX.s() + "&cFailed to load up metrics.");
            }
        } else {
            log("&dUsing metrics will allow us to improve the plugin, please consider it :)");
        }
        
        // Set the generators for each world...
        server = game.getServer();
        Collection<World> worlds = server.getWorlds();
        if (worlds.size() > 0) {
            log("INJECTING WORLDS!!!!!!!");
            UUIDHandler.startCaching(null);
            for (World world : server.getWorlds()) {
                log("INJECTING WORLD: " + world.getName());
                world.setWorldGenerator(new SpongePlotGenerator(world.getName()));
            }
        }
        
        ConfigurationSection worldSection = PS.get().config.getConfigurationSection("worlds");
        for (String world : worldSection.getKeys(false)) {
            this.modify = new WorldModify(this);
            Game game = event.getGame();
            game.getRegistry().registerWorldGeneratorModifier(modify);
            game.getRegistry().getWorldBuilder()
            .name(world)
            .enabled(true)
            .loadsOnStartup(true)
            .keepsSpawnLoaded(true)
            .dimensionType(DimensionTypes.OVERWORLD)
            .generator(GeneratorTypes.DEBUG)
            .gameMode(GameModes.CREATIVE)
            .generatorModifiers(modify)
            .build();
        }
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public Game getGame() {
        return game;
    }
    
    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {
        // This is how events sort of work?
        Player player  = event.getUser();
        log(player.getWorld().getName());
    }

    @Override
    public void log(String message) {
        message = ConsoleColors.fromString(message);
        logger.info(message);
    }

    @Override
    public File getDirectory() {
        return new File("mods/PlotSquared");
    }

    @Override
    public void disable() {
        PS.get().disable();
        THIS = null;
    }

    @Override
    public int[] getPluginVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int[] getServerVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void handleKick(UUID uuid, C c) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public TaskManager getTaskManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void runEntityTask() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void registerCommands() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void registerPlayerEvents() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void registerInventoryEvents() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void registerPlotPlusEvents() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void registerForceFieldEvents() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void registerWorldEditEvents() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void registerTNTListener() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public EconHandler getEconomyHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlockManager initBlockManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EventUtil initEventUtil() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ChunkManager initChunkManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SetupUtils initSetupUtils() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HybridUtils initHybridUtils() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UUIDHandlerImplementation initUUIDHandler() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InventoryUtil initInventoryUtil() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean initPlotMeConverter() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void unregister(PlotPlayer player) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public SpongeGeneratorWrapper getGenerator(String world, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public APlotListener initPlotListener() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void registerChunkProcessor() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void registerWorldEvents() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public PlayerManager initPlayerManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServerName() {
        // TODO Auto-generated method stub
        return null;
    }
}