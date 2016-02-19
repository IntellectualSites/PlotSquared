package com.plotsquared.sponge;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

import com.google.inject.Inject;
import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.IPlotMain;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.intellectualcrafters.plot.util.ChatManager;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.InventoryUtil;
import com.intellectualcrafters.plot.util.PlotQueue;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.UUIDHandlerImplementation;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.sponge.generator.SpongePlotGenerator;
import com.plotsquared.sponge.listener.ChunkProcessor;
import com.plotsquared.sponge.listener.MainListener;
import com.plotsquared.sponge.listener.WorldEvents;
import com.plotsquared.sponge.util.KillRoadMobs;
import com.plotsquared.sponge.util.SpongeChatManager;
import com.plotsquared.sponge.util.SpongeChunkManager;
import com.plotsquared.sponge.util.SpongeCommand;
import com.plotsquared.sponge.util.SpongeEconHandler;
import com.plotsquared.sponge.util.SpongeEventUtil;
import com.plotsquared.sponge.util.SpongeInventoryUtil;
import com.plotsquared.sponge.util.SpongeMetrics;
import com.plotsquared.sponge.util.SpongeTaskManager;
import com.plotsquared.sponge.util.SpongeTitleManager;
import com.plotsquared.sponge.util.SpongeUtil;
import com.plotsquared.sponge.util.block.FastQueue;
import com.plotsquared.sponge.util.block.SlowQueue;
import com.plotsquared.sponge.uuid.SpongeLowerOfflineUUIDWrapper;
import com.plotsquared.sponge.uuid.SpongeOnlineUUIDWrapper;
import com.plotsquared.sponge.uuid.SpongeUUIDHandler;

/**
 * Created by robin on 01/11/2014
 */

@Plugin(id = "PlotSquared", name = "PlotSquared", version = "3.0.0", dependencies = "before:WorldEdit,required-after:TotalEconomy")
public class SpongeMain implements IPlotMain, PluginContainer {
    public static SpongeMain THIS;

    @Inject
    private Logger logger;
    @Inject
    private Game game;
    private Server server;

    private GameProfileManager resolver;

    @Override
    public Logger getLogger() {
        return logger;
    }

    public Game getGame() {
        return game;
    }

    public Server getServer() {
        return server;
    }

    public GameProfileManager getResolver() {
        return resolver;
    }

    public SpongeMain getPlugin() {
        return THIS;
    }

    @Override
    public String getId() {
        return "PlotSquared";
    }

    @Override
    public Optional<Object> getInstance() {
        return Optional.<Object> of(THIS);
    }

    @Override
    public String getName() {
        return "PlotSquared";
    }

    @Override
    public String getVersion() {
        final int[] version = PS.get().getVersion();
        String result = "";
        String prefix = "";
        for (final int i : version) {
            result += prefix + i;
            prefix = ".";
        }
        return result;
    }

    @Listener
    public void init(final GameInitializationEvent event) {
        log("PlotSquared: Game init");
    }

    @Listener
    public void onInit(final GamePreInitializationEvent event) {
        log("PlotSquared: Game pre init");
    }
    
    @Listener
    public void onServerAboutToStart(final GameAboutToStartServerEvent event) {
        log("PlotSquared: Server init");
        THIS = this;
        resolver = game.getServiceManager().provide(GameProfileManager.class).get();
        server = game.getServer();
        game.getRegistry().register(WorldGeneratorModifier.class, (WorldGeneratorModifier) new HybridGen().specify());
        new PS(this, "Sponge");

    }

    @Override
    public void log(String message) {
        message = C.format(message, C.replacements);
        if (!Settings.CONSOLE_COLOR) {
            message = message.replaceAll('\u00a7' + "[a-z|0-9]", "");
        }
        if ((server == null) || (server.getConsole() == null)) {
            logger.info(message);
            return;
        }
        server.getConsole().sendMessage(Text.of(message));
    }

    @Override
    public File getDirectory() {
        return new File("mods/PlotSquared");
    }

    @Override
    public File getWorldContainer() {
        return new File("world");
    }

    @Override
    public void disable() {
        PS.get().disable();
        THIS = null;
    }

    @Override
    public int[] getPluginVersion() {
        final PluginContainer plugin = game.getPluginManager().getPlugin("PlotSquared").get();
        final String version = plugin.getVersion();
        log("Checking plugin version: PlotSquared: ");
        final String[] split = version.split("\\.");
        return new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]), (split.length == 3) ? Integer.parseInt(split[2]) : 0 };
    }

    @Override
    public int[] getServerVersion() {
        log("Checking minecraft version: Sponge: ");
        final String version = game.getPlatform().getMinecraftVersion().getName();
        final String[] split = version.split("\\.");
        return new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]), (split.length == 3) ? Integer.parseInt(split[2]) : 0 };
    }

    @Override
    public InventoryUtil initInventoryUtil() {
        return new SpongeInventoryUtil();
    }

    @Override
    public EconHandler getEconomyHandler() {
        SpongeEconHandler econ = new SpongeEconHandler();
        game.getEventManager().registerListeners(this, econ);
        return econ;
    }

    @Override
    public EventUtil initEventUtil() {
        return new SpongeEventUtil();
    }

    @Override
    public ChunkManager initChunkManager() {
        return new SpongeChunkManager();
    }

    @Override
    public SetupUtils initSetupUtils() {
        return new SpongeSetupUtils();
    }

    @Override
    public HybridUtils initHybridUtils() {
        return new SpongeHybridUtils();
    }

    @Override
    public SchematicHandler initSchematicHandler() {
        return new SpongeSchematicHandler();
    }

    @Override
    public TaskManager getTaskManager() {
        return new SpongeTaskManager();
    }

    @Override
    public void runEntityTask() {
        new KillRoadMobs().run();
    }

    @Override
    public void registerCommands() {
        getGame().getCommandManager().register(THIS, new SpongeCommand(), new String[] { "plots", "p", "plot", "ps", "plotsquared", "p2", "2" });
    }

    @Override
    public void registerPlayerEvents() {
        game.getEventManager().registerListeners(this, new MainListener());
    }

    @Override
    public void registerInventoryEvents() {
        // Part of PlayerEvents - can be moved if necessary
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
    public boolean initWorldEdit() {
        try {
            Class.forName("com.sk89q.worldedit.WorldEdit");
            return true;
        } catch (final Throwable e) {
            return false;
        }
    }

    @Override
    public UUIDHandlerImplementation initUUIDHandler() {
        UUIDWrapper wrapper;
        if (Settings.OFFLINE_MODE || !PS.get().checkVersion(getServerVersion(), 1, 7, 6)) {
            wrapper = new SpongeLowerOfflineUUIDWrapper();
        } else {
            wrapper = new SpongeOnlineUUIDWrapper();
        }
        return new SpongeUUIDHandler(wrapper);
    }

    @Override
    public boolean initPlotMeConverter() {
        // PlotMe was never ported to sponge
        return false;
    }

    @Override
    public void unregister(final PlotPlayer player) {
        SpongeUtil.removePlayer(player.getName());
    }

    @Override
    public void registerChunkProcessor() {
        game.getEventManager().registerListeners(this, new ChunkProcessor());
    }

    @Override
    public void registerWorldEvents() {
        game.getEventManager().registerListeners(this, new WorldEvents());
    }

    @Override
    public String getServerName() {
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
    public void setGenerator(final String worldname) {
        World world = SpongeUtil.getWorld(worldname);
        if (world == null) {
            // create world
            final ConfigurationSection worldConfig = PS.get().config.getConfigurationSection("worlds." + worldname);
            String manager = worldConfig.getString("generator.plugin");
            if (manager == null) {
                manager = "PlotSquared";
            }
            String generator = worldConfig.getString("generator.init");
            if (generator == null) {
                generator = manager;
            }
            
            final int type = worldConfig.getInt("generator.type");
            final int terrain = worldConfig.getInt("generator.terrain");
            final SetupObject setup = new SetupObject();
            setup.plotManager = manager;
            setup.setupGenerator = generator;
            setup.type = type;
            setup.terrain = terrain;
            setup.step = new ConfigurationNode[0];
            setup.world = worldname;
            SetupUtils.manager.setupWorld(setup);
        } else {
            throw new IllegalArgumentException("World already loaded: " + worldname + "???");
        }
        WorldGenerator wg = world.getWorldGenerator();
        GenerationPopulator gen = wg.getBaseGenerationPopulator();
        if (gen instanceof SpongePlotGenerator) {
            PS.get().loadWorld(worldname, (SpongePlotGenerator) gen);
        } else if (gen != null) {
            throw new UnsupportedOperationException("NOT IMPLEMENTED YET!");
        } else {
            if (PS.get().config.contains("worlds." + worldname)) {
                PS.get().loadWorld(worldname, null);
            }
        }
    }

    @Override
    public AbstractTitle initTitleManager() {
        return new SpongeTitleManager();
    }

    @Override
    public PlotPlayer wrapPlayer(final Object obj) {
        if (obj instanceof Player) {
            return SpongeUtil.getPlayer((Player) obj);
        }
        else if (obj instanceof String) {
            return UUIDHandler.getPlayer((String) obj);
        } else if (obj instanceof UUID) {
            return UUIDHandler.getPlayer((UUID) obj);
        }
        // TODO FIXME offline player
        return null;
    }

    @Override
    public String getNMSPackage() {
        return "TODO";//TODO FIXME
    }

    @Override
    public ChatManager<?> initChatManager() {
        return new SpongeChatManager();
    }
    
    @Override
    public PlotQueue initPlotQueue() {
        if (PS.get().checkVersion(getServerVersion(), 1, 8, 0)) {
            try {
                return new FastQueue();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return new SlowQueue();
    }
    
    @Override
    public WorldUtil initWorldUtil() {
        return new SpongeUtil();
    }
    
    @Override
    public GeneratorWrapper<?> getGenerator(String world, String name) {
        if (name == null) {
            return null;
        }
        Collection<WorldGeneratorModifier> wgms = game.getRegistry().getAllOf(WorldGeneratorModifier.class);
        for (WorldGeneratorModifier wgm : wgms) {
            if (StringMan.isEqualIgnoreCaseToAny(name, wgm.getName(), wgm.getId())) {
                if (wgm instanceof GeneratorWrapper<?>) {
                    return (GeneratorWrapper<?>) wgm;
                }
                return new SpongePlotGenerator(wgm);
            }
        }
        return new SpongePlotGenerator(new HybridGen());
    }
    
    @Override
    public GeneratorWrapper<?> wrapPlotGenerator(IndependentPlotGenerator generator) {
        return new SpongePlotGenerator(generator);
    }
    
    @Override
    public List<String> getPluginIds() {
        ArrayList<String> names = new ArrayList<>();
        for (PluginContainer plugin : game.getPluginManager().getPlugins()) {
            names.add(plugin.getName() + ";" + plugin.getVersion() + ":" + true);
        }
        return names;
    }
}
