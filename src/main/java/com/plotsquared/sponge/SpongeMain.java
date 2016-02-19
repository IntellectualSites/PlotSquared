package com.plotsquared.sponge;

import com.google.inject.Inject;
import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.IPlotMain;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.intellectualcrafters.plot.util.ChatManager;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.InventoryUtil;
import com.intellectualcrafters.plot.util.PlotQueue;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.UUIDHandlerImplementation;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.sponge.generator.SpongeBasicGen;
import com.plotsquared.sponge.generator.SpongeGeneratorWrapper;
import com.plotsquared.sponge.generator.WorldModify;
import com.plotsquared.sponge.listener.MainListener;
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
import com.plotsquared.sponge.uuid.SpongeLowerOfflineUUIDWrapper;
import com.plotsquared.sponge.uuid.SpongeOnlineUUIDWrapper;
import com.plotsquared.sponge.uuid.SpongeUUIDHandler;
import org.slf4j.Logger;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.translation.Translatable;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.WorldCreationSettings;
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

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

    private WorldModify modify;

    // stuff //
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

    public Text getText(final String m) {
        return Text.of(m);
    }

    public Translatable getTranslation(final String m) {
        return new Translatable() {
            @Override
            public Translation getTranslation() {
                return new Translation() {

                    @Override
                    public String getId() {
                        return m;
                    }

                    @Override
                    public String get(final Locale l, final Object... args) {
                        return m;
                    }

                    @Override
                    public String get(final Locale l) {
                        return m;
                    }
                };
            }
        };
    }

    private final PlotBlock NULL_BLOCK = new PlotBlock((short) 0, (byte) 0);
    private BlockState[][] blockMap;
    private Map<BlockState, PlotBlock> blockMapReverse;

    public BlockState getBlockState(final PlotBlock block) {
        if (blockMap[block.id] == null) {
            log("UNKNOWN BLOCK: " + block.toString());
            return null;
        } else if (blockMap[block.id].length <= block.data) {
            log("UNKNOWN BLOCK: " + block.toString() + " -> Using " + block.id + ":0 instead");
            return blockMap[block.id][0];
        }
        return blockMap[block.id][block.data];
    }

    public BlockState getBlockState(final int id) {
        return blockMap[id][0];
    }

    public Collection<BlockState> getAllStates() {
        return blockMapReverse.keySet();
    }

    public PlotBlock getPlotBlock(final BlockState state) {
        final PlotBlock val = blockMapReverse.get(state);
        if (val == null) {
            return NULL_BLOCK;
        }
        return val;
    }

    /////////

    ////////////////////// SPONGE PLUGIN REGISTRATION ////////////////////
    @Override
    public String getId() {
        return "PlotSquared";
    }

    @Override
    public Optional<Object> getInstance() {
        return Optional.<Object>of(THIS);
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

    ///////////////////////////////////////////////////////////////////////

    ///////////////////// ON ENABLE /////////////////////
    @Listener
    public void init(final GameInitializationEvent event) {
        log("P^2 INIT");
    }

    @Listener
    public void onInit(final GamePreInitializationEvent event) {
        // Hook for Project Worlds
        hookProjectWorlds();
    }

    public void hookProjectWorlds() {
        Optional<PluginContainer> plugin = game.getPluginManager().getPlugin("Project Worlds");
        if (plugin.isPresent()) {
            try {
                Class<?> clazz = Class.forName("com.gmail.trentech.pjw.modifiers.Modifiers");
                Method method = clazz.getMethod("put", String.class, WorldGeneratorModifier.class);
                SpongeBasicGen generator = new SpongeBasicGen(null);
                method.invoke(null, "plotsquared", new WorldModify(generator, false));
                log("Adding plotsquared modifier to Project Worlds");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Listener
    public void onServerAboutToStart(final GameAboutToStartServerEvent event) {
        log("P^2 ABOUT START");
        THIS = this;

        //
        resolver = game.getServiceManager().provide(GameProfileManager.class).get();
        server = game.getServer();
        //

        new PS(this, "Sponge");

        registerBlocks();

        final ConfigurationSection worldSection = PS.get().config.getConfigurationSection("worlds");
        if (worldSection != null) {
            for (final String world : worldSection.getKeys(false)) {
                createWorldFromConfig(world);
            }
        }
    }

    public WorldCreationSettings createWorldFromConfig(final String world) {
        final SpongeBasicGen generator = new SpongeBasicGen(world);
        final PlotArea plotworld = generator.getNewPlotWorld(world);
        SpongeGeneratorWrapper wrapper;
        if (plotworld.TYPE == 0) {
            wrapper = new SpongeGeneratorWrapper(world, generator);
        } else {
            wrapper = new SpongeGeneratorWrapper(world, null);
        }
        PS.get().loadWorld(world, (GeneratorWrapper<?>) wrapper);
        switch (plotworld.TYPE) {
            // Normal
            case 0: {
                modify = new WorldModify(generator, false);
                game.getRegistry().register(WorldModify.class, modify);
                WorldCreationSettings builder = WorldCreationSettings.builder().name(world).enabled(true).loadsOnStartup(true).keepsSpawnLoaded(true)
                        .dimension(DimensionTypes.OVERWORLD).generator(GeneratorTypes.OVERWORLD).usesMapFeatures(false).generatorModifiers(modify)
                        .build();
                return builder;
            }
            // Augmented
            default: {
                modify = new WorldModify(generator, true);
                game.getRegistry().register(WorldModify.class, modify);
                final WorldCreationSettings builder =
                        WorldCreationSettings.builder().name(world).enabled(true).loadsOnStartup(true).keepsSpawnLoaded(true)
                                .dimension(DimensionTypes.OVERWORLD)
                                .generator(GeneratorTypes.OVERWORLD).usesMapFeatures(false).generatorModifiers(modify).build();
                return builder;
            }
        }
    }

    public void registerBlock(final PlotBlock block, final BlockState state) {
        final BlockState[] val = blockMap[block.id];
        if (val == null) {
            blockMap[block.id] = new BlockState[block.data + 1];
        } else if (val.length <= block.data) {
            blockMap[block.id] = Arrays.copyOf(val, block.data + 1);
        } else if (val[block.data] != null) {
            return;
        }
        blockMap[block.id][block.data] = state;
        blockMapReverse.put(state, block);
    }

    public PlotBlock registerBlock(final BlockState state) {
        final PlotBlock val = blockMapReverse.get(state);
        if (val != null) {
            return val;
        }
        byte data;
        if (blockMap[0] == null) {
            blockMap[0] = new BlockState[1];
            data = 0;
        } else {
            data = (byte) (blockMap[0].length);
        }
        final PlotBlock block = new PlotBlock((short) 0, data);
        registerBlock(block, state);
        return block;
    }

    public void registerBlocks() {
        blockMap = new BlockState[256][];
        blockMapReverse = new HashMap<>();
        final HashMap<String, BlockState> states = new HashMap<>();

        PS.get().copyFile("ids.txt", "config");
        PS.get().copyFile("data.txt", "config");

        try {
            final File id_file = new File(getDirectory(), "config" + File.separator + "ids.txt");
            final List<String> id_lines = Files.readAllLines(id_file.toPath(), StandardCharsets.UTF_8);

            final File data_file = new File(getDirectory(), "config" + File.separator + "data.txt");
            final List<String> data_lines = Files.readAllLines(data_file.toPath(), StandardCharsets.UTF_8);

            Field[] fields = BlockTypes.class.getDeclaredFields();
            for (final Field field : fields) {
                final BlockType type = (BlockType) field.get(null);
                final BlockState state = type.getDefaultState();
                try {
                    states.put(type.getId() + ":" + 0, state);
                } catch (final Exception e) {
                }
            }
            final String packaze = "org.spongepowered.api.data.type.";
            for (String data_line : data_lines) {
                final String classname = packaze + data_line.trim();
                try {
                    final Class<?> clazz = Class.forName(classname);
                    fields = clazz.getDeclaredFields();
                    for (final Field field : fields) {
                        final CatalogType type = (CatalogType) field.get(null);
                        final String minecraft_id = type.getId();
                        final BlockState state = states.get(minecraft_id + ":" + 0);
                        if (state == null) {
                        }
                    }
                } catch (ClassNotFoundException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                }
            }

            PlotBlock block = null;
            for (int i = 0; i < id_lines.size(); i++) {
                final String line = id_lines.get(i).trim();
                switch (i % 3) {
                    case 0: {
                        block = Configuration.BLOCK.parseString(line);
                        break;
                    }
                    case 1: {
                        break;
                    }
                    case 2: {
                        final BlockState state = states.remove(line + ":" + block.data);
                        if (state == null) {
                            continue;
                        }
                        registerBlock(block, state);
                        break;
                    }
                }
            }
            for (final Entry<String, BlockState> state : states.entrySet()) {
                log("REGISTERING: " + registerBlock(state.getValue()) + " | " + state.getValue().getType());
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void log(String message) {
        message = C.format(message, C.replacements);
        if (!Settings.CONSOLE_COLOR) {
            message = message.replaceAll('\u00a7' + "[a-z|0-9]", "");
        }
        if ((server == null)) {
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
        return new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), (split.length == 3) ? Integer.parseInt(split[2]) : 0};
    }

    @Override
    public int[] getServerVersion() {
        log("Checking minecraft version: Sponge: ");
        final String version = game.getPlatform().getMinecraftVersion().getName();
        final String[] split = version.split("\\.");
        return new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), (split.length == 3) ? Integer.parseInt(split[2]) : 0};
    }

    @Override
    public InventoryUtil initInventoryUtil() {
        return new SpongeInventoryUtil();
    }

    @Override
    public GeneratorWrapper<?> getGenerator(final String world, final String name) {
        if (name == null) {
            return (GeneratorWrapper<?>) new SpongeGeneratorWrapper(world, null);
        }
        if (name.equals("PlotSquared")) {
            return (GeneratorWrapper<?>) new SpongeGeneratorWrapper(world, new SpongeBasicGen(world));
        } else {
            throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
        }
    }

    @Override public GeneratorWrapper<?> wrapPlotGenerator(String world, IndependentPlotGenerator generator) {
        return null;
    }

    @Override
    public EconHandler getEconomyHandler() {
        // TODO Auto-generated method stub
        // Nothing like Vault exists yet
        return new SpongeEconHandler();
    }

    @Override public PlotQueue initPlotQueue() {
        return null;
    }

    @Override public WorldUtil initWorldUtil() {
        return null;
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
        getGame().getCommandManager().register(THIS, new SpongeCommand(), "plots", "p", "plot", "ps", "plotsquared", "p2", "2");
    }

    @Override
    public void registerPlayerEvents() {
        game.getEventManager().registerListeners(this, new MainListener());
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
        // TODO Auto-generated method stub
        PS.log("initPlotMeConverter NOT IMPLEMENTED YET");
        return false;
    }

    @Override
    public void unregister(final PlotPlayer player) {
        SpongeUtil.removePlayer(player.getName());
    }

    @Override
    public void registerChunkProcessor() {
        // TODO Auto-generated method stub
        PS.log("registerChunkProcessor NOT IMPLEMENTED YET");
    }

    @Override
    public void registerWorldEvents() {
        // TODO Auto-generated method stub
        PS.log("registerWorldEvents NOT IMPLEMENTED YET");
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
    public void setGenerator(final String world) {
        // TODO THIS IS DONE DURING STARTUP ALREADY
    }

    @Override
    public AbstractTitle initTitleManager() {
        return new SpongeTitleManager();
    }

    @Override public List<String> getPluginIds() {
        return null;
    }

    @Override
    public PlotPlayer wrapPlayer(final Object obj) {
        if (obj instanceof Player) {
            return SpongeUtil.getPlayer((Player) obj);
        }
        //        else if (obj instanceof OfflinePlayer) {
        //            return BukkitUtil.getPlayer((OfflinePlayer) obj);
        //        }
        else if (obj instanceof String) {
            return UUIDHandler.getPlayer((String) obj);
        } else if (obj instanceof UUID) {
            return UUIDHandler.getPlayer((UUID) obj);
        }
        return null;
    }

    @Override
    public String getNMSPackage() {
        return "1_8_R3";
    }

    @Override
    public ChatManager<?> initChatManager() {
        return new SpongeChatManager();
    }
}
