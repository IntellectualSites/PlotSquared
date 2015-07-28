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
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.listener.APlotListener;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.entity.player.gamemode.GameModes;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerChatEvent;
import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.event.state.ServerAboutToStartEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.profile.GameProfileResolver;
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
    
    private GameProfileResolver resolver;
    
    private WorldModify modify;
    
    private Object plugin;
    
    // stuff //
    public Logger getLogger() {
        return logger;
    }
    
    public Game getGame() {
        return game;
    }
    
    public Server getServer() {
        return server;
    }
    
    public GameProfileResolver getResolver() {
        return resolver;
    }
    
    public Object getPlugin() {
        return this.plugin;
    }
    /////////

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
        
        // resolver
        resolver = game.getServiceManager().provide(GameProfileResolver.class).get();
        plugin = game.getPluginManager().getPlugin("PlotSquared").get().getInstance();
        
        PS.instance = new PS(this);
        
        // Set the generators for each world...
        server = game.getServer();
        Collection<World> worlds = server.getWorlds();
        if (worlds.size() > 0) {
            log("INJECTING WORLDS!!!!!!!");
            for (World world : server.getWorlds()) {
                log("INJECTING WORLD: " + world.getName());
                world.setWorldGenerator(new SpongePlotGenerator(world.getName()));
            }
        }
        
        ConfigurationSection worldSection = PS.get().config.getConfigurationSection("worlds");
        if (worldSection != null) {
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
        PluginContainer plugin = game.getPluginManager().getPlugin("PlotSquared").get();
        String version = plugin.getVersion();
        log("Checking plugin version: PlotSquared: ");
        String[] split = version.split("\\.");
        return new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]), (split.length == 3) ? Integer.parseInt(split[2]) : 0 };
    }

    @Override
    public int[] getServerVersion() {
        log("Checking minecraft version: Sponge: ");
        String version = game.getPlatform().getMinecraftVersion().getName();
        String[] split = version.split("\\.");
        return new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]), (split.length == 3) ? Integer.parseInt(split[2]) : 0 };
    }

    @Override
    public TaskManager getTaskManager() {
        return new SpongeTaskManager();
    }

    @Override
    public void runEntityTask() {
        // TODO Auto-generated method stub
        log("runEntityTask is not implemented!");
    }

    @Override
    public void registerCommands() {
        // TODO Auto-generated method stub
        log("registerCommands is not implemented!");
    }

    @Override
    public void registerPlayerEvents() {
        // TODO Auto-generated method stub
        log("registerPlayerEvents is not implemented!");
    }

    @Override
    public void registerInventoryEvents() {
        // TODO Auto-generated method stub
        log("registerInventoryEvents is not implemented!");
    }

    @Override
    public void registerPlotPlusEvents() {
        // TODO Auto-generated method stub
        log("registerPlotPlusEvents is not implemented!");
    }

    @Override
    public void registerForceFieldEvents() {
        // TODO Auto-generated method stub
        log("registerForceFieldEvents is not implemented!");
    }

    @Override
    public void registerWorldEditEvents() {
        // TODO Auto-generated method stub
        log("registerWorldEditEvents is not implemented!");
    }

    @Override
    public void registerTNTListener() {
        // TODO Auto-generated method stub
        log("registerTNTListener is not implemented!");
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
        UUIDWrapper wrapper;
        if (Settings.OFFLINE_MODE || !PS.get().checkVersion(this.getServerVersion(), 1, 7, 6)) {
            wrapper = new SpongeLowerOfflineUUIDWrapper();
        }
        else {
            wrapper = new SpongeOnlineUUIDWrapper();
        }
        return new SpongeUUIDHandler(wrapper);
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
     // TODO FIXME
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    @Override
    public void startMetrics() {
        try {
            final SpongeMetrics metrics = new SpongeMetrics(game, this);
            metrics.start();
            log(C.PREFIX.s() + "&6Metrics enabled.");
        } catch (final Exception e) {
            log(C.PREFIX.s() + "&cFailed to load up metrics.");
        }
    }

    @Override
    public void setGenerator(String world) {
        // TODO Auto-generated method stub
        
    }
}