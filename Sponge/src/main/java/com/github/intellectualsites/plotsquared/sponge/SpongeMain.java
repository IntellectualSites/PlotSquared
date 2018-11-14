package com.github.intellectualsites.plotsquared.sponge;

import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.plot.IPlotMain;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.config.ConfigurationNode;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.generator.GeneratorWrapper;
import com.github.intellectualsites.plotsquared.plot.generator.HybridGen;
import com.github.intellectualsites.plotsquared.plot.generator.HybridUtils;
import com.github.intellectualsites.plotsquared.plot.generator.IndependentPlotGenerator;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.SetupObject;
import com.github.intellectualsites.plotsquared.plot.object.worlds.PlotAreaManager;
import com.github.intellectualsites.plotsquared.plot.object.worlds.SinglePlotArea;
import com.github.intellectualsites.plotsquared.plot.object.worlds.SinglePlotAreaManager;
import com.github.intellectualsites.plotsquared.plot.object.worlds.SingleWorldGenerator;
import com.github.intellectualsites.plotsquared.plot.util.*;
import com.github.intellectualsites.plotsquared.plot.util.block.QueueProvider;
import com.github.intellectualsites.plotsquared.plot.uuid.UUIDWrapper;
import com.github.intellectualsites.plotsquared.sponge.generator.SpongePlotGenerator;
import com.github.intellectualsites.plotsquared.sponge.listener.ChunkProcessor;
import com.github.intellectualsites.plotsquared.sponge.listener.MainListener;
import com.github.intellectualsites.plotsquared.sponge.listener.WorldEvents;
import com.github.intellectualsites.plotsquared.sponge.util.*;
import com.github.intellectualsites.plotsquared.sponge.util.block.SpongeLocalQueue;
import com.github.intellectualsites.plotsquared.sponge.uuid.SpongeLowerOfflineUUIDWrapper;
import com.github.intellectualsites.plotsquared.sponge.uuid.SpongeOnlineUUIDWrapper;
import com.github.intellectualsites.plotsquared.sponge.uuid.SpongeUUIDHandler;
import com.google.inject.Inject;
import net.minecrell.mcstats.SpongeStatsLite;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Plugin(id = "plotsquared", name = "PlotSquared", description = "Easy, yet powerful Plot World generation and management.", url = "https://github.com/IntellectualSites/PlotSquared", version = "3.5.0-SNAPSHOT")
public class SpongeMain implements IPlotMain {

    public static SpongeMain THIS;

    @Inject public PluginContainer plugin;
    @Inject public SpongeStatsLite stats;
    @Inject private Logger logger;
    @Inject private Game game;
    private Server server;

    @Inject @ConfigDir(sharedRoot = false) private Path privateConfigDir;

    private GameProfileManager resolver;

    private Logger getLogger() {
        return this.logger;
    }

    public Game getGame() {
        return this.game;
    }

    public Server getServer() {
        return this.server;
    }

    public GameProfileManager getResolver() {
        if (this.resolver == null) {
            this.resolver = this.game.getServer().getGameProfileManager();
        }
        return this.resolver;
    }

    public SpongeMain getPlugin() {
        return THIS;
    }

    @Listener public void onPreInitialize(GamePreInitializationEvent event) {
        getLogger().info(
            "The metrics section in PlotSquared is ignored in favor of the actual metrics reporter configurations.");
        this.stats.start();
    }

    @Listener public void onServerAboutToStart(GameAboutToStartServerEvent event) {
        THIS = this;
        new PlotSquared(this, "Sponge");
        this.server = this.game.getServer();
        this.game.getRegistry().register(WorldGeneratorModifier.class,
            (WorldGeneratorModifier) PlotSquared.get().IMP.getDefaultGenerator().specify(null));
        this.game.getRegistry().register(WorldGeneratorModifier.class,
            (WorldGeneratorModifier) new SingleWorldGenerator().specify(null));
        if (Settings.Enabled_Components.WORLDS) {
            TaskManager.IMP.taskRepeat(new Runnable() {
                @Override public void run() {
                    unload();
                }
            }, 20);
        }
    }

    public void unload() {
        PlotAreaManager manager = PlotSquared.get().getPlotAreaManager();
        if (manager instanceof SinglePlotAreaManager) {
            SinglePlotArea area = ((SinglePlotAreaManager) manager).getArea();
            for (World world : Sponge.getServer().getWorlds()) {
                String name = world.getName();
                PlotId id = PlotId.fromString(name);
                if (id != null) {
                    Plot plot = area.getOwnedPlot(id);
                    if (plot != null) {
                        List<PlotPlayer> players = plot.getPlayersInPlot();
                        if (players.isEmpty() && PlotPlayer.wrap(plot.owner) == null) {
                            try {
                                world.save();
                            } catch (IOException e) {
                                e.printStackTrace();
                                return;
                            }
                            long start = System.currentTimeMillis();
                            for (Chunk chunk : world.getLoadedChunks()) {
                                chunk.unloadChunk();
                                if (System.currentTimeMillis() - start > 10) {
                                    return;
                                }
                            }
                            Sponge.getServer().unloadWorld(world);
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override public void log(String message) {
        message = C.format(message, C.replacements);
        if (!Settings.Chat.CONSOLE_COLOR) {
            message = message.replaceAll('\u00a7' + "[a-z|0-9]", "");
        }
        if (this.server == null) {
            this.logger.info(message);
            return;
        }
        this.server.getConsole().sendMessage(SpongeUtil.getText(message));
    }

    @Override public File getDirectory() {
        return privateConfigDir.toFile();
    }

    @Override public File getWorldContainer() {
        return new File(game.getSavesDirectory().toFile(), "world");
    }

    @Override public void disable() {
        PlotSquared.get().disable();
        THIS = null;
    }

    @Override public int[] getPluginVersion() {
        String ver = this.plugin.getVersion().orElse("");
        String[] split = ver.split("[\\.|-]");
        return new int[] {Integer.parseInt(split[0]), Integer.parseInt(split[1]),
            Integer.parseInt(split[2])};
    }

    @Override public String getPluginVersionString() {
        return this.plugin.getVersion().orElse("");
    }

    @Override public String getPluginName() {
        return "PlotSquared";
    }

    @Override public int[] getServerVersion() {
        PlotSquared.log("Checking minecraft version: Sponge: ");
        String version = this.game.getPlatform().getMinecraftVersion().getName();
        String[] split = version.split("\\.");
        return new int[] {Integer.parseInt(split[0]), Integer.parseInt(split[1]),
            split.length == 3 ? Integer.parseInt(split[2]) : 0};
    }

    @Override public InventoryUtil initInventoryUtil() {
        return new SpongeInventoryUtil();
    }

    @Override public EconHandler getEconomyHandler() {
        SpongeEconHandler econ = new SpongeEconHandler();
        Sponge.getEventManager().registerListeners(this, econ);
        return econ;
    }

    @Override public EventUtil initEventUtil() {
        return new SpongeEventUtil();
    }

    @Override public ChunkManager initChunkManager() {
        return new SpongeChunkManager();
    }

    @Override public SetupUtils initSetupUtils() {
        return new SpongeSetupUtils();
    }

    @Override public HybridUtils initHybridUtils() {
        return new SpongeHybridUtils();
    }

    @Override public SchematicHandler initSchematicHandler() {
        return new SpongeSchematicHandler();
    }

    @Override public TaskManager getTaskManager() {
        return new SpongeTaskManager(this);
    }

    @Override public void runEntityTask() {
        new KillRoadMobs().run();
    }

    @Override public void registerCommands() {
        getGame().getCommandManager()
            .register(THIS, new SpongeCommand(), "plots", "p", "plot", "ps", "plotsquared", "p2",
                "2");
    }

    @Override public void registerPlayerEvents() {
        Sponge.getEventManager().registerListeners(this, new MainListener());
    }

    @Override public void registerInventoryEvents() {
        // Part of PlayerEvents - can be moved if necessary
    }

    @Override public void registerPlotPlusEvents() {
        PlotSquared.log("registerPlotPlusEvents is not implemented!");
    }

    @Override public void registerForceFieldEvents() {
    }

    @Override public boolean initWorldEdit() {
        try {
            Class.forName("com.sk89q.worldedit.WorldEdit");
        } catch (ClassNotFoundException ignored) {
            return false;
        }
        return true;
    }

    @Override public UUIDHandlerImplementation initUUIDHandler() {
        UUIDWrapper wrapper;
        if (Settings.UUID.OFFLINE) {
            wrapper = new SpongeLowerOfflineUUIDWrapper();
        } else {
            wrapper = new SpongeOnlineUUIDWrapper();
        }
        return new SpongeUUIDHandler(wrapper);
    }

    @Override public boolean initPlotMeConverter() {
        return false;
    }

    @Override public void unregister(PlotPlayer player) {
        SpongeUtil.removePlayer(player.getName());
    }

    @Override public void registerChunkProcessor() {
        Sponge.getEventManager().registerListeners(this, new ChunkProcessor());
    }

    @Override public void registerWorldEvents() {
        Sponge.getEventManager().registerListeners(this, new WorldEvents());
    }

    @Override public void startMetrics() {
    }

    @Override public void setGenerator(String worldName) {
        World world = SpongeUtil.getWorld(worldName);
        if (world == null) {
            // create world
            ConfigurationSection worldConfig =
                PlotSquared.get().worlds.getConfigurationSection("worlds." + worldName);
            String manager = worldConfig.getString("generator.plugin", "PlotSquared");
            String generator = worldConfig.getString("generator.init", manager);

            int type = worldConfig.getInt("generator.type");
            int terrain = worldConfig.getInt("generator.terrain");
            SetupObject setup = new SetupObject();
            setup.plotManager = manager;
            setup.setupGenerator = generator;
            setup.type = type;
            setup.terrain = terrain;
            setup.step = new ConfigurationNode[0];
            setup.world = worldName;
            SetupUtils.manager.setupWorld(setup);
            world = SpongeUtil.getWorld(worldName);
        } else {
            throw new IllegalArgumentException("World already loaded: " + worldName + "???");
        }
        WorldGenerator wg = world.getWorldGenerator();
        GenerationPopulator gen = wg.getBaseGenerationPopulator();
        if (gen instanceof GeneratorWrapper) {
            PlotSquared.get().loadWorld(worldName, (GeneratorWrapper) gen);
        } else {
            throw new UnsupportedOperationException(
                "NOT IMPLEMENTED YET2! " + worldName + " | " + gen);
        }
    }

    @Override public AbstractTitle initTitleManager() {
        return new SpongeTitleManager();
    }

    @Override public PlotPlayer wrapPlayer(Object player) {
        if (player instanceof Player) {
            return SpongeUtil.getPlayer((Player) player);
        } else if (UUIDHandler.implementation == null) {
            return null;
        } else if (player instanceof String) {
            return UUIDHandler.getPlayer((String) player);
        } else if (player instanceof UUID) {
            return UUIDHandler.getPlayer((UUID) player);
        }
        // TODO FIXME offline player
        return null;
    }

    @Override public String getNMSPackage() {
        return "";//TODO FIXME
    }

    @Override public ChatManager<?> initChatManager() {
        return new SpongeChatManager();
    }

    @Override public QueueProvider initBlockQueue() {
        MainUtil.canSendChunk = true;
        return QueueProvider.of(SpongeLocalQueue.class, null);
    }

    @Override public WorldUtil initWorldUtil() {
        return new SpongeUtil();
    }

    @Override public GeneratorWrapper<?> getGenerator(String world, String name) {
        if (name == null) {
            return null;
        }
        Collection<WorldGeneratorModifier> wgms =
            this.game.getRegistry().getAllOf(WorldGeneratorModifier.class);
        for (WorldGeneratorModifier wgm : wgms) {
            if (StringMan.isEqualIgnoreCaseToAny(name, wgm.getName(), wgm.getId())) {
                if (wgm instanceof GeneratorWrapper<?>) {
                    return (GeneratorWrapper<?>) wgm;
                }
                return new SpongePlotGenerator(wgm);
            }
        }
        return new SpongePlotGenerator(PlotSquared.get().IMP.getDefaultGenerator());
    }

    @Override
    public GeneratorWrapper<?> wrapPlotGenerator(String world, IndependentPlotGenerator generator) {
        return new SpongePlotGenerator(generator);
    }

    @Override public List<String> getPluginIds() {
        return this.game.getPluginManager().getPlugins().stream()
            .map(plugin1 -> plugin1.getName() + ';' + plugin1.getVersion() + ':' + true)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override public IndependentPlotGenerator getDefaultGenerator() {
        return new HybridGen();
    }
}
