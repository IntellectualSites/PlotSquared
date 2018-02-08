package com.plotsquared.nukkit;

import cn.nukkit.Nukkit;
import cn.nukkit.OfflinePlayer;
import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.Listener;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.metadata.MetadataValue;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.IPlotMain;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.generator.*;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.object.chat.PlainChatManager;
import com.intellectualcrafters.plot.object.worlds.PlotAreaManager;
import com.intellectualcrafters.plot.object.worlds.SinglePlotArea;
import com.intellectualcrafters.plot.object.worlds.SinglePlotAreaManager;
import com.intellectualcrafters.plot.util.*;
import com.intellectualcrafters.plot.util.block.QueueProvider;
import com.plotsquared.nukkit.generator.NukkitPlotGenerator;
import com.plotsquared.nukkit.listeners.PlayerEvents;
import com.plotsquared.nukkit.listeners.WorldEvents;
import com.plotsquared.nukkit.util.*;
import com.plotsquared.nukkit.util.block.NukkitHybridGen;
import com.plotsquared.nukkit.util.block.NukkitLocalQueue;
import com.plotsquared.nukkit.uuid.FileUUIDHandler;
import com.plotsquared.nukkit.uuid.LowerOfflineUUIDWrapper;
import com.sk89q.worldedit.WorldEdit;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public final class NukkitMain extends PluginBase implements Listener, IPlotMain {

    public static WorldEdit worldEdit;

    private int[] version;
    private String name;

    @Override
    public int[] getServerVersion() {
        if (this.version == null) {
            try {
                this.version = new int[3];
                String[] split = Nukkit.API_VERSION.split("\\.");
                this.version[0] = Integer.parseInt(split[0]);
                this.version[1] = Integer.parseInt(split[1]);
                if (split.length == 3) {
                    this.version[2] = Integer.parseInt(split[2]);
                }
            } catch (NumberFormatException e) {
                return new int[]{1, 0, 0};
            }
        }
        return this.version;
    }

    @Override
    public void onEnable() {
        try {
            this.name = getDescription().getName();
            getServer().getName();
            new PS(this, "Nukkit");
            if (Settings.Enabled_Components.METRICS) {
                new Metrics(this).start();
                PS.log(C.PREFIX + "&6Metrics enabled.");
            } else {
                PS.log(C.CONSOLE_PLEASE_ENABLE_METRICS.f(getPluginName()));
            }
            Generator.addGenerator(NukkitHybridGen.class, getPluginName(), 1);
            if (Settings.Enabled_Components.WORLDS) {
                TaskManager.IMP.taskRepeat(new Runnable() {
                    @Override
                    public void run() {
                        unload();
                    }
                }, 20);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void unload() {
        PlotAreaManager manager = PS.get().getPlotAreaManager();
        if (manager instanceof SinglePlotAreaManager) {
            long start = System.currentTimeMillis();
            SinglePlotArea area = ((SinglePlotAreaManager) manager).getArea();
            Map<Integer, Level> worlds = getServer().getLevels();
            Level unload = null;
            for (Level world : getServer().getLevels().values()) {
                String name = world.getName();
                PlotId id = PlotId.fromString(name);
                if (id != null) {
                    Plot plot = area.getOwnedPlot(id);
                    if (plot != null) {
                        List<PlotPlayer> players = plot.getPlayersInPlot();
                        if (players.isEmpty() && PlotPlayer.wrap(plot.owner) == null) {
                            unload = world;
                            break;
                        }
                    }
                }
            }
            if (unload != null) {
                Map<Long, ? extends FullChunk> chunks = unload.getChunks();
                FullChunk[] toUnload = chunks.values().toArray(new FullChunk[chunks.size()]);
                for (FullChunk chunk : toUnload) {
                    try {
                        chunk.unload(true, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (System.currentTimeMillis() - start > 20) {
                        return;
                    }
                }
                getServer().unloadLevel(unload, true);
            }
        }
    }

    @Override
    public void onDisable() {
        PS.get().disable();
        getServer().getScheduler().cancelAllTasks();
    }

    @Override
    public void log(String message) {
        try {
            message = C.color(message);
            if (!Settings.Chat.CONSOLE_COLOR) {
                message = message.replaceAll('\u00A7' + "[0-9]", "");
            }
            this.getServer().getConsoleSender().sendMessage(message);
        } catch (Throwable ignored) {
            System.out.println(ConsoleColors.fromString(message));
        }
    }

    @Override
    public void disable() {
        onDisable();
    }

    @Override
    public int[] getPluginVersion() {
        String ver = getDescription().getVersion();
        if (ver.contains("-")) {
            ver = ver.split("-")[0];
        }
        String[] split = ver.split("\\.");
        return new int[]{Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2])};
    }

    @Override public String getPluginVersionString() {
        return getDescription().getVersion();
    }

    @Override
    public String getPluginName() {
        return name;
    }

    @Override
    public void registerCommands() {
        NukkitCommand bukkitCommand = new NukkitCommand("plot", new String[] {"p","plot","ps","plotsquared","p2","2"});
        getServer().getCommandMap().register("plot", bukkitCommand);
    }

    @Override
    public File getDirectory() {
        return getDataFolder();
    }

    @Override
    public File getWorldContainer() {
        return new File("worlds");
    }

    @Override
    public TaskManager getTaskManager() {
        return new NukkitTaskManager(this);
    }

    @Override
    public void runEntityTask() {
        PS.log(C.PREFIX + "KillAllEntities started.");
        TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                PS.get().foreachPlotArea(new RunnableVal<PlotArea>() {
                    @Override
                    public void run(PlotArea plotArea) {
                        Level world = getServer().getLevelByName(plotArea.worldname);
                        try {
                            if (world == null) {
                                return;
                            }
                            Entity[] entities = world.getEntities();
                            for (Entity entity : entities) {
                                if (entity instanceof Player) {
                                    continue;
                                }
                                com.intellectualcrafters.plot.object.Location location = NukkitUtil.getLocation(entity.getLocation());
                                Plot plot = location.getPlot();
                                if (plot == null) {
                                    if (location.isPlotArea()) {
                                        entity.kill();
                                    }
                                    continue;
                                }
                                List<MetadataValue> meta = entity.getMetadata("plot");
                                if (meta.isEmpty()) {
                                    continue;
                                }
                                Plot origin = (Plot) meta.get(0).value();
                                if (!plot.equals(origin.getBasePlot(false))) {
                                    entity.kill();
                                }
                                continue;
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, 20);
    }

    @Override
    public void registerPlayerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
    }

    @Override
    public void registerInventoryEvents() {
        PS.debug("Not implemented: registerPlotPlusEvents");
    }

    @Override
    public void registerPlotPlusEvents() {
        PS.debug("Not implemented: registerPlotPlusEvents");
    }

    @Override
    public void registerForceFieldEvents() {
        PS.debug("Not implemented: registerPlotPlusEvents");
    }

    @Override
    public boolean initWorldEdit() {
        if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            worldEdit = WorldEdit.getInstance();
            return true;
        }
        return false;
    }

    @Override
    public EconHandler getEconomyHandler() {
        return null;
    }

    @Override
    public QueueProvider initBlockQueue() {
        return QueueProvider.of(NukkitLocalQueue.class, null);
    }

    @Override
    public WorldUtil initWorldUtil() {
        return new NukkitUtil(this);
    }

    @Override
    public boolean initPlotMeConverter() {
        return false; // No PlotMe for MCPE
    }

    @Override
    public GeneratorWrapper<?> getGenerator(String world, String name) {
        if (name == null) {
            return null;
        }
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("world", world);
        try {
            Class<? extends Generator> gen = Generator.getGenerator(name);
            if (gen != null) {
                Generator instance = gen.getConstructor(Map.class).newInstance(map);
                if (instance instanceof GeneratorWrapper) {
                    return (GeneratorWrapper<?>) instance;
                }
                map.put("generator", instance);
                return new NukkitPlotGenerator(map);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return new NukkitHybridGen(map);
    }

    @Override
    public HybridUtils initHybridUtils() {
        return new NukkitHybridUtils();
    }

    @Override
    public SetupUtils initSetupUtils() {
        return new NukkitSetupUtils(this);
    }

    @Override
    public UUIDHandlerImplementation initUUIDHandler() {
        Settings.UUID.FORCE_LOWERCASE = true;
        Settings.UUID.OFFLINE = true;
        LowerOfflineUUIDWrapper wrapper = new LowerOfflineUUIDWrapper();
        return new FileUUIDHandler(wrapper);
    }

    @Override
    public ChunkManager initChunkManager() {
        return new NukkitChunkManager();
    }

    @Override
    public EventUtil initEventUtil() {
        return new NukkitEventUtil(this);
    }

    @Override
    public void unregister(PlotPlayer player) {
        NukkitUtil.removePlayer(player.getName());
    }

    @Override
    public void registerChunkProcessor() {
        PS.debug("Not implemented: registerChunkProcessor");
    }

    @Override
    public void registerWorldEvents() {
        getServer().getPluginManager().registerEvents(new WorldEvents(), this);
    }

    @Override
    public InventoryUtil initInventoryUtil() {
        return new NukkitInventoryUtil();
    }

    @Override
    public void startMetrics() {
        new Metrics(this).start();
        PS.log(C.PREFIX + "&6Metrics enabled.");
    }

    @Override
    public void setGenerator(String worldName) {
        Level world = getServer().getLevelByName(worldName);
        if (world == null) {
            // create world
            ConfigurationSection worldConfig = PS.get().worlds.getConfigurationSection("worlds." + worldName);
            String manager = worldConfig.getString("generator.plugin", getPluginName());
            SetupObject setup = new SetupObject();
            setup.plotManager = manager;
            setup.setupGenerator = worldConfig.getString("generator.init", manager);
            setup.type = worldConfig.getInt("generator.type");
            setup.terrain = worldConfig.getInt("generator.terrain");
            setup.step = new ConfigurationNode[0];
            setup.world = worldName;
            SetupUtils.manager.setupWorld(setup);
            world = getServer().getLevelByName(worldName);
        } else {
            HashMap<String, Object> map = new HashMap<>();
            map.put("world", world.getName());
            map.put("plot-generator", PS.get().IMP.getDefaultGenerator());
            setGenerator(world, new NukkitPlotGenerator(map));
        }
        if (world != null) {
            try {
                Field fieldInstance = Level.class.getDeclaredField("generatorInstance");
                fieldInstance.setAccessible(true);
                Generator gen = (Generator) fieldInstance.get(world);
                if (gen instanceof NukkitPlotGenerator) {
                    PS.get().loadWorld(worldName, (NukkitPlotGenerator) gen);
                } else if (gen instanceof GeneratorWrapper) {
                    PS.get().loadWorld(worldName, (GeneratorWrapper) gen);
                } else if (PS.get().worlds.contains("worlds." + worldName)) {
                    PS.get().loadWorld(worldName, null);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void setGenerator(Level level, Generator generator) {
        try {
            Field fieldClass = Level.class.getDeclaredField("generator");
            Field fieldInstance = Level.class.getDeclaredField("generatorInstance");
            fieldClass.setAccessible(true);
            fieldInstance.setAccessible(true);
            fieldClass.set(level, generator.getClass());
            fieldInstance.set(level, generator);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public SchematicHandler initSchematicHandler() {
        return new NukkitSchematicHandler(this);
    }

    @Override
    public AbstractTitle initTitleManager() {
        return new NukkitTitleUtil();
    }

    @Override
    public PlotPlayer wrapPlayer(Object player) {
        if (player instanceof Player) {
            return NukkitUtil.getPlayer((Player) player);
        }
        if (player instanceof OfflinePlayer) {
            return NukkitUtil.getPlayer((OfflinePlayer) player);
        }
        if (player instanceof String) {
            return UUIDHandler.getPlayer((String) player);
        }
        if (player instanceof UUID) {
            return UUIDHandler.getPlayer((UUID) player);
        }
        return null;
    }

    @Override
    public String getNMSPackage() {
        return "";
    }

    @Override
    public ChatManager<?> initChatManager() {
        return new PlainChatManager();
    }

    @Override
    public GeneratorWrapper<?> wrapPlotGenerator(String world, IndependentPlotGenerator generator) {
        HashMap<String, Object> settings = new HashMap<>();
        settings.put("world", world);
        settings.put("plot-generator", generator);
        return new NukkitPlotGenerator(settings);
    }

    @Override
    public List<String> getPluginIds() {
        ArrayList<String> names = new ArrayList<>();
        for (Map.Entry<String, Plugin> entry : getServer().getPluginManager().getPlugins().entrySet()) {
            Plugin plugin = entry.getValue();
            names.add(entry.getKey() + ';' + plugin.getDescription().getVersion() + ':' + plugin.isEnabled());
        }
        return names;
    }

    @Override
    public IndependentPlotGenerator getDefaultGenerator() {
        return new HybridGen() {
            @Override
            public PlotManager getNewPlotManager() {
                return new HybridPlotManager() {
                    @Override
                    public int getWorldHeight() {
                        return 255;
                    }
                };
            }
        };
    }
}
