package com.plotsquared.sponge;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.manipulator.mutable.block.StoneData;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerChatEvent;
import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.event.state.ServerAboutToStartEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.profile.GameProfileResolver;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.translation.Translatable;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.api.world.GeneratorTypes;
import org.spongepowered.api.world.World;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.IPlotMain;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.InventoryUtil;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.UUIDHandlerImplementation;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.sponge.generator.SpongeBasicGen;
import com.plotsquared.sponge.generator.SpongeGeneratorWrapper;
import com.plotsquared.sponge.generator.WorldModify;
import com.plotsquared.sponge.listener.MainListener;
import com.plotsquared.sponge.util.KillRoadMobs;
import com.plotsquared.sponge.util.SpongeBlockManager;
import com.plotsquared.sponge.util.SpongeChunkManager;
import com.plotsquared.sponge.util.SpongeCommand;
import com.plotsquared.sponge.util.SpongeEventUtil;
import com.plotsquared.sponge.util.SpongeInventoryUtil;
import com.plotsquared.sponge.util.SpongeMetrics;
import com.plotsquared.sponge.util.SpongeTaskManager;
import com.plotsquared.sponge.util.SpongeTitleManager;
import com.plotsquared.sponge.util.SpongeUtil;
import com.plotsquared.sponge.uuid.SpongeLowerOfflineUUIDWrapper;
import com.plotsquared.sponge.uuid.SpongeOnlineUUIDWrapper;
import com.plotsquared.sponge.uuid.SpongeUUIDHandler;

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
        return plugin;
    }
    
    public Text getText(String m) {
        return Texts.of(m);
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
                    public String get(Locale l, Object... args) {
                        return m;
                    }
                    
                    @Override
                    public String get(Locale l) {
                        return m;
                    }
                };
            }
        };
    }
    
    private PlotBlock NULL_BLOCK = new PlotBlock((short) 0, (byte) 0);
    private BlockState[][] blockMap;
    private Map<BlockState, PlotBlock> blockMapReverse;
    
    public BlockState getBlockState(PlotBlock block) {
        if (blockMap[block.id] == null) {
            log("UNKNOWN BLOCK: " + block.toString());
            return null;
        }
        else if (blockMap[block.id].length <= block.data) {
            log("UNKNOWN BLOCK: " + block.toString() + " -> Using " + block.id + ":0 instead");
            return blockMap[block.id][0];
        }
        return blockMap[block.id][block.data];
    }
    
    public BlockState getBlockState(int id) {
        return blockMap[id][0];
    }

    public Collection<BlockState> getAllStates() {
        return this.blockMapReverse.keySet();
    }
    
    public PlotBlock getPlotBlock(BlockState state) {
        PlotBlock val = blockMapReverse.get(state);
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
        
        //
        resolver = game.getServiceManager().provide(GameProfileResolver.class).get();
        plugin = game.getPluginManager().getPlugin("PlotSquared").get().getInstance();
        log("PLUGIN IS THIS: " + (plugin == this));
        plugin = this;
        server = game.getServer();
        //
        
        PS.instance = new PS(this);
        
        // TODO Until P^2 has json chat stuff for sponge, disable this
        Settings.FANCY_CHAT = false;
        // done
        
        registerBlocks();
        
        ConfigurationSection worldSection = PS.get().config.getConfigurationSection("worlds");
        if (worldSection != null) {
            for (String world : worldSection.getKeys(false)) {
                createWorldFromConfig(world);
            }
        }
    }
    
    public World createWorldFromConfig(String world) {
        SpongeBasicGen generator = new SpongeBasicGen(world);
        PlotWorld plotworld = generator.getNewPlotWorld(world);
        SpongeGeneratorWrapper wrapper;
        if (plotworld.TYPE == 0) {
            wrapper = new SpongeGeneratorWrapper(world, generator);
        }
        else {
            wrapper = new SpongeGeneratorWrapper(world, null);
        }
        PS.get().loadWorld(world, wrapper);
        switch (plotworld.TYPE) {
            // Normal
            case 0: {
                this.modify = new WorldModify(generator, false);
                game.getRegistry().registerWorldGeneratorModifier(modify);
                Optional<World> builder = game.getRegistry().createWorldBuilder()
                .name(world)
                .enabled(true)
                .loadsOnStartup(true)
                .keepsSpawnLoaded(true)
                .dimensionType(DimensionTypes.OVERWORLD)
                .generator(GeneratorTypes.FLAT)
                .usesMapFeatures(false)
                .generatorModifiers(modify)
                .build();
                return builder.get();
            }
            // Augmented
            default: {
                this.modify = new WorldModify(generator, true);
                game.getRegistry().registerWorldGeneratorModifier(modify);
                Optional<World> builder = game.getRegistry().createWorldBuilder()
                .name(world)
                .enabled(true)
                .loadsOnStartup(true)
                .keepsSpawnLoaded(true)
                .dimensionType(DimensionTypes.OVERWORLD)
                .generator(GeneratorTypes.OVERWORLD)
                .usesMapFeatures(false)
                .generatorModifiers(modify)
                .build();
                return builder.get();
            }
        }
    }
    
    public void registerBlock(PlotBlock block, BlockState state) {
        BlockState[] val = blockMap[block.id];
        if (val == null) {
            blockMap[block.id] = new BlockState[block.data + 1];
        }
        else if (val.length <= block.data) {
            blockMap[block.id] = Arrays.copyOf(val, block.data + 1);
        }
        else if (val[block.data] != null) {
            return;
        }
        blockMap[block.id][block.data] = state;
        blockMapReverse.put(state, block);
    }
    
    public PlotBlock registerBlock(BlockState state) {
        PlotBlock val = blockMapReverse.get(state);
        if (val != null) {
            return val;
        }
        byte data;
        if (blockMap[0] == null) {
            blockMap[0] = new BlockState[1];
            data = 0;
        }
        else {
            data = (byte) (blockMap[0].length);
        }
        PlotBlock block = new PlotBlock((short) 0, data);
        registerBlock(block, state);
        return block;
    }
    
    public void registerBlocks() {
        blockMap = new BlockState[256][];
        blockMapReverse = new HashMap<BlockState, PlotBlock>();
        HashMap<String, BlockState> states = new HashMap<>();
        
        PS.get().copyFile("ids.txt", "config");
        PS.get().copyFile("data.txt", "config");
        
        try {
            
            File id_file = new File(getDirectory(), "config" + File.separator + "ids.txt");
            List<String> id_lines = Files.readAllLines(id_file.toPath(), StandardCharsets.UTF_8);
            
            File data_file = new File(getDirectory(), "config" + File.separator + "data.txt");
            List<String> data_lines = Files.readAllLines(data_file.toPath(), StandardCharsets.UTF_8);
            
            Field[] fields = BlockTypes.class.getDeclaredFields();
            for (Field field : fields) {
                BlockType type = (BlockType) field.get(null);
                BlockState state = type.getDefaultState();
                if (state != null) {
                    try {
                        states.put(type.getId() + ":" + 0, state);
                    }
                    catch (Exception e) {}
                }
            }
            String packaze = "org.spongepowered.api.data.type.";
            for (int i = 0; i < data_lines.size(); i++) {
                String classname = packaze + data_lines.get(i).trim();
                try {
                    Class<?> clazz = Class.forName(classname);
                    fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        CatalogType type = (CatalogType) field.get(null);
                        String minecraft_id = type.getId();
                        BlockState state = states.get(minecraft_id + ":" + 0);
                        if (state == null) {
                            continue;
                        }
                    }
                }
                catch (Throwable e) {}
            }
            
            PlotBlock block = null;
            for (int i = 0; i < id_lines.size(); i++) {
                String line = id_lines.get(i).trim();
                switch(i%3) {
                    case 0: {
                        block = Configuration.BLOCK.parseString(line);
                        break;
                    }
                    case 1: {
                        break;
                    }
                    case 2: {
                        String minecraft_id = line;
                        BlockState state = states.remove(minecraft_id + ":" + block.data);
                        if (state == null) {
                            continue;
                        }
                        registerBlock(block, state);
                        break;
                    }
                }
            }
            for (Entry<String, BlockState> state : states.entrySet()) {
                log("REGISTERING: " + registerBlock(state.getValue()) + " | " + state.getValue().getType());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
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
        message = C.format(message, C.replacements);
        if (!Settings.CONSOLE_COLOR) {
            message = message.replaceAll('\u00a7' + "[a-z|0-9]", "");
        }
        if (server == null || server.getConsole() == null) {
            logger.info(message);
            return;
        }
        server.getConsole().sendMessage(Texts.of(message));
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
    public InventoryUtil initInventoryUtil() {
        return new SpongeInventoryUtil();
    }
    
    @Override
    public SpongeGeneratorWrapper getGenerator(String world, String name) {
        if (name == null) {
            return new SpongeGeneratorWrapper(world, null);
        }
        if (name.equals("PlotSquared")) {
            return new SpongeGeneratorWrapper(world, new SpongeBasicGen(world));
        }
        else {
            throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
        }
    }
    
    @Override
    public EconHandler getEconomyHandler() {
        // TODO Auto-generated method stub
        // Nothing like Vault exists yet
        PS.log("getEconomyHandler NOT IMPLEMENTED YET");
        return null;
    }

    @Override
    public BlockManager initBlockManager() {
        return new SpongeBlockManager();
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
        getGame().getCommandDispatcher().register(plugin, new SpongeCommand(), "plots", "p", "plot", "ps", "plotsquared", "p2", "2");
    }

    @Override
    public void registerPlayerEvents() {
        game.getEventManager().register(this, new MainListener());
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
    public boolean initPlotMeConverter() {
        // TODO Auto-generated method stub
        PS.log("initPlotMeConverter NOT IMPLEMENTED YET");
        return false;
    }

    @Override
    public void unregister(PlotPlayer player) {
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
    public void setGenerator(String world) {
        // TODO THIS IS DONE DURING STARTUP ALREADY
    }

    @Override
    public AbstractTitle initTitleManager() {
        return new SpongeTitleManager();
    }

    @Override
    public PlotPlayer wrapPlayer(Object obj) {
        if (obj instanceof Player) {
            return SpongeUtil.getPlayer((Player) obj);
        }
//        else if (obj instanceof OfflinePlayer) {
//            return BukkitUtil.getPlayer((OfflinePlayer) obj);
//        }
        else if (obj instanceof String) {
            return UUIDHandler.getPlayer((String) obj);
        }
        else if (obj instanceof UUID) {
            return UUIDHandler.getPlayer((UUID) obj);
        }
        return null;
    }

    @Override
    public String getNMSPackage() {
        return "1_8_R3";
    }
}