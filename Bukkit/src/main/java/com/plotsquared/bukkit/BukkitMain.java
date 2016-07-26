package com.plotsquared.bukkit;

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
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.object.chat.PlainChatManager;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.intellectualcrafters.plot.util.ChatManager;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.ConsoleColors;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.InventoryUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.UUIDHandlerImplementation;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.intellectualcrafters.plot.util.block.QueueProvider;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.bukkit.database.plotme.ClassicPlotMeConnector;
import com.plotsquared.bukkit.database.plotme.LikePlotMeConverter;
import com.plotsquared.bukkit.database.plotme.PlotMeConnector_017;
import com.plotsquared.bukkit.generator.BukkitPlotGenerator;
import com.plotsquared.bukkit.listeners.ChunkListener;
import com.plotsquared.bukkit.listeners.EntitySpawnListener;
import com.plotsquared.bukkit.listeners.PlayerEvents;
import com.plotsquared.bukkit.listeners.PlayerEvents183;
import com.plotsquared.bukkit.listeners.PlayerEvents_1_8;
import com.plotsquared.bukkit.listeners.PlayerEvents_1_9;
import com.plotsquared.bukkit.listeners.PlotPlusListener;
import com.plotsquared.bukkit.listeners.WorldEvents;
import com.plotsquared.bukkit.titles.DefaultTitle_19;
import com.plotsquared.bukkit.util.BukkitChatManager;
import com.plotsquared.bukkit.util.BukkitChunkManager;
import com.plotsquared.bukkit.util.BukkitCommand;
import com.plotsquared.bukkit.util.BukkitEconHandler;
import com.plotsquared.bukkit.util.BukkitEventUtil;
import com.plotsquared.bukkit.util.BukkitHybridUtils;
import com.plotsquared.bukkit.util.BukkitInventoryUtil;
import com.plotsquared.bukkit.util.BukkitSchematicHandler;
import com.plotsquared.bukkit.util.BukkitSetupUtils;
import com.plotsquared.bukkit.util.BukkitTaskManager;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.bukkit.util.Metrics;
import com.plotsquared.bukkit.util.SendChunk;
import com.plotsquared.bukkit.util.SetGenCB;
import com.plotsquared.bukkit.util.block.BukkitLocalQueue;
import com.plotsquared.bukkit.util.block.BukkitLocalQueue_1_7;
import com.plotsquared.bukkit.util.block.BukkitLocalQueue_1_8;
import com.plotsquared.bukkit.util.block.BukkitLocalQueue_1_8_3;
import com.plotsquared.bukkit.util.block.BukkitLocalQueue_1_9;
import com.plotsquared.bukkit.uuid.DefaultUUIDWrapper;
import com.plotsquared.bukkit.uuid.FileUUIDHandler;
import com.plotsquared.bukkit.uuid.LowerOfflineUUIDWrapper;
import com.plotsquared.bukkit.uuid.OfflineUUIDWrapper;
import com.plotsquared.bukkit.uuid.SQLUUIDHandler;
import com.sk89q.worldedit.WorldEdit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class BukkitMain extends JavaPlugin implements Listener, IPlotMain {

    public static WorldEdit worldEdit;

    private int[] version;

    @Override
    public int[] getServerVersion() {
        if (this.version == null) {
            try {
                this.version = new int[3];
                String[] split = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
                this.version[0] = Integer.parseInt(split[0]);
                this.version[1] = Integer.parseInt(split[1]);
                if (split.length == 3) {
                    this.version[2] = Integer.parseInt(split[2]);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                PS.debug(StringMan.getString(Bukkit.getBukkitVersion()));
                PS.debug(StringMan.getString(Bukkit.getBukkitVersion().split("-")[0].split("\\.")));
                return new int[]{1, 10, 0};
            }
        }
        return this.version;
    }

    @Override
    public void onEnable() {
        getServer().getName();
        new PS(this, "Bukkit");
        if (Settings.Enabled_Components.METRICS) {
            new Metrics(this).start();
            PS.log(C.PREFIX + "&6Metrics enabled.");
        } else {
            PS.log(C.CONSOLE_PLEASE_ENABLE_METRICS);
        }
    }

    @Override
    public void onDisable() {
        PS.get().disable();
        Bukkit.getScheduler().cancelTasks(this);
    }

    @Override
    public void log(String message) {
        try {
            message = C.color(message);
            if (!Settings.Chat.CONSOLE_COLOR) {
                message = ChatColor.stripColor(message);
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
    public void registerCommands() {
        BukkitCommand bukkitCommand = new BukkitCommand();
        PluginCommand plotCommand = getCommand("plots");
        plotCommand.setExecutor(bukkitCommand);
        plotCommand.setAliases(Arrays.asList("p", "ps", "plotme", "plot"));
        plotCommand.setTabCompleter(bukkitCommand);
    }

    @Override
    public File getDirectory() {
        return getDataFolder();
    }

    @Override
    public File getWorldContainer() {
        return Bukkit.getWorldContainer();
    }

    @Override
    public TaskManager getTaskManager() {
        return new BukkitTaskManager(this);
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
                        World world = Bukkit.getWorld(plotArea.worldname);
                        try {
                            if (world == null) {
                                return;
                            }
                            List<Entity> entities = world.getEntities();
                            Iterator<Entity> iterator = entities.iterator();
                            while (iterator.hasNext()) {
                                Entity entity = iterator.next();
                                switch (entity.getType()) {
                                    case EGG:
                                    case COMPLEX_PART:
                                    case FISHING_HOOK:
                                    case ENDER_SIGNAL:
                                    case LINGERING_POTION:
                                    case AREA_EFFECT_CLOUD:
                                    case EXPERIENCE_ORB:
                                    case LEASH_HITCH:
                                    case FIREWORK:
                                    case WEATHER:
                                    case LIGHTNING:
                                    case WITHER_SKULL:
                                    case UNKNOWN:
                                    case PLAYER:
                                        // non moving / unmovable
                                        continue;
                                    case THROWN_EXP_BOTTLE:
                                    case SPLASH_POTION:
                                    case SNOWBALL:
                                    case SHULKER_BULLET:
                                    case SPECTRAL_ARROW:
                                    case TIPPED_ARROW:
                                    case ENDER_PEARL:
                                    case ARROW:
                                        // managed elsewhere | projectile
                                        continue;
                                    case ITEM_FRAME:
                                    case PAINTING:
                                        // Not vehicles
                                        continue;
                                    case ARMOR_STAND:
                                        // Temporarily classify as vehicle
                                    case MINECART:
                                    case MINECART_CHEST:
                                    case MINECART_COMMAND:
                                    case MINECART_FURNACE:
                                    case MINECART_HOPPER:
                                    case MINECART_MOB_SPAWNER:
                                    case ENDER_CRYSTAL:
                                    case MINECART_TNT:
                                    case BOAT: {
                                        if (Settings.Enabled_Components.KILL_ROAD_VEHICLES) {
                                            com.intellectualcrafters.plot.object.Location location = BukkitUtil.getLocation(entity.getLocation());
                                            Plot plot = location.getPlot();
                                            if (plot == null) {
                                                if (location.isPlotArea()) {
                                                    iterator.remove();
                                                    entity.remove();
                                                }
                                                continue;
                                            }
                                            List<MetadataValue> meta = entity.getMetadata("plot");
                                            if (meta.isEmpty()) {
                                                continue;
                                            }
                                            Plot origin = (Plot) meta.get(0).value();
                                            if (!plot.equals(origin.getBasePlot(false))) {
                                                iterator.remove();
                                                entity.remove();
                                            }
                                            continue;
                                        } else {
                                            continue;
                                        }
                                    }
                                    case SMALL_FIREBALL:
                                    case FIREBALL:
                                    case DRAGON_FIREBALL:
                                    case DROPPED_ITEM:
                                        // dropped item
                                        continue;
                                    case PRIMED_TNT:
                                    case FALLING_BLOCK:
                                        // managed elsewhere
                                        continue;
                                    case POLAR_BEAR:
                                    case BAT:
                                    case BLAZE:
                                    case CAVE_SPIDER:
                                    case CHICKEN:
                                    case COW:
                                    case CREEPER:
                                    case ENDERMAN:
                                    case ENDERMITE:
                                    case ENDER_DRAGON:
                                    case GHAST:
                                    case GIANT:
                                    case GUARDIAN:
                                    case HORSE:
                                    case IRON_GOLEM:
                                    case MAGMA_CUBE:
                                    case MUSHROOM_COW:
                                    case OCELOT:
                                    case PIG:
                                    case PIG_ZOMBIE:
                                    case RABBIT:
                                    case SHEEP:
                                    case SILVERFISH:
                                    case SKELETON:
                                    case SLIME:
                                    case SNOWMAN:
                                    case SPIDER:
                                    case SQUID:
                                    case VILLAGER:
                                    case WITCH:
                                    case WITHER:
                                    case WOLF:
                                    case ZOMBIE:
                                    case SHULKER:
                                    default:
                                        if (Settings.Enabled_Components.KILL_ROAD_MOBS) {
                                            Location location = entity.getLocation();
                                            if (BukkitUtil.getLocation(location).isPlotRoad()) {
                                                if (entity instanceof LivingEntity) {
                                                    LivingEntity livingEntity = (LivingEntity) entity;
                                                    if (!livingEntity.isLeashed() || !entity.hasMetadata("keep")) {
                                                        Entity passenger = entity.getPassenger();
                                                        if (!(passenger instanceof Player) && entity.getMetadata("keep").isEmpty()) {
                                                            iterator.remove();
                                                            entity.remove();
                                                        }
                                                    }
                                                } else {
                                                    Entity passenger = entity.getPassenger();
                                                    if (!(passenger instanceof Player) && entity.getMetadata("keep").isEmpty()) {
                                                        iterator.remove();
                                                        entity.remove();
                                                    }
                                                }
                                            }
                                        }
                                }
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
    public final ChunkGenerator getDefaultWorldGenerator(String world, String id) {
        HybridGen result = new HybridGen();
        if (!PS.get().setupPlotWorld(world, id, result)) {
            return null;
        }
        return (ChunkGenerator) result.specify();
    }

    @Override
    public void registerPlayerEvents() {
        PlayerEvents main = new PlayerEvents();
        getServer().getPluginManager().registerEvents(main, this);
        try {
            getServer().getPluginManager().registerEvents(new EntitySpawnListener(), this);
        } catch (Throwable ignore) {}
        if (PS.get().checkVersion(getServerVersion(), 1, 8, 0)) {
            try {
                getServer().getPluginManager().registerEvents(new PlayerEvents_1_8(), this);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (PS.get().checkVersion(getServerVersion(), 1, 8, 3)) {
            try {
                getServer().getPluginManager().registerEvents(new PlayerEvents183(), this);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (PS.get().checkVersion(getServerVersion(), 1, 9, 0)) {
            try {
                getServer().getPluginManager().registerEvents(new PlayerEvents_1_9(main), this);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void registerInventoryEvents() {
        // Part of PlayerEvents - can be moved if necessary
    }

    @Override
    public void registerPlotPlusEvents() {
        PlotPlusListener.startRunnable(this);
        getServer().getPluginManager().registerEvents(new PlotPlusListener(), this);
    }

    @Override
    public void registerForceFieldEvents() {
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
        try {
            BukkitEconHandler econ = new BukkitEconHandler();
            if (econ.init()) {
                return econ;
            }
        } catch (Throwable ignored) {
            PS.debug("No economy detected!");
        }
        return null;
    }

    @Override
    public QueueProvider initBlockQueue() {
        try {
            new SendChunk();
            MainUtil.canSendChunk = true;
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e) {
            PS.debug(SendChunk.class + " does not support " + StringMan.getString(getServerVersion()));
            MainUtil.canSendChunk = false;
        }
        if (PS.get().checkVersion(getServerVersion(), 1, 9, 0)) {
            return QueueProvider.of(BukkitLocalQueue_1_9.class, BukkitLocalQueue.class);
        }
        if (PS.get().checkVersion(getServerVersion(), 1, 8, 3)) {
            return QueueProvider.of(BukkitLocalQueue_1_8_3.class, BukkitLocalQueue.class);
        }
        if (PS.get().checkVersion(getServerVersion(), 1, 8, 0)) {
            return QueueProvider.of(BukkitLocalQueue_1_8.class, BukkitLocalQueue.class);
        }
        return QueueProvider.of(BukkitLocalQueue_1_7.class, BukkitLocalQueue.class);
    }

    @Override
    public WorldUtil initWorldUtil() {
        return new BukkitUtil();
    }

    @Override
    public boolean initPlotMeConverter() {
        TaskManager.runTaskLaterAsync(new Runnable() {
            @Override
            public void run() {
                if (new LikePlotMeConverter("PlotMe").run(new ClassicPlotMeConnector())) {
                    return;
                }
                if (new LikePlotMeConverter("PlotMe").run(new PlotMeConnector_017())) {
                    return;
                }
            }
        }, 20);
        return Bukkit.getPluginManager().getPlugin("PlotMe") != null;
    }

    @Override
    public GeneratorWrapper<?> getGenerator(String world, String name) {
        if (name == null) {
            return null;
        }
        Plugin genPlugin = Bukkit.getPluginManager().getPlugin(name);
        if (genPlugin != null && genPlugin.isEnabled()) {
            ChunkGenerator gen = genPlugin.getDefaultWorldGenerator(world, "");
            if (gen instanceof GeneratorWrapper<?>) {
                return (GeneratorWrapper<?>) gen;
            }
            return new BukkitPlotGenerator(world, gen);
        } else {
            return new BukkitPlotGenerator(new HybridGen());
        }
    }

    @Override
    public HybridUtils initHybridUtils() {
        return new BukkitHybridUtils();
    }

    @Override
    public SetupUtils initSetupUtils() {
        return new BukkitSetupUtils();
    }

    @Override
    public UUIDHandlerImplementation initUUIDHandler() {
        boolean checkVersion = PS.get().checkVersion(getServerVersion(), 1, 7, 6);
        UUIDWrapper wrapper;
        if (Settings.UUID.OFFLINE) {
            if (Settings.UUID.FORCE_LOWERCASE) {
                wrapper = new LowerOfflineUUIDWrapper();
            } else {
                wrapper = new OfflineUUIDWrapper();
            }
            Settings.UUID.OFFLINE = true;
        } else if (checkVersion) {
            wrapper = new DefaultUUIDWrapper();
            Settings.UUID.OFFLINE = false;
        } else {
            if (Settings.UUID.FORCE_LOWERCASE) {
                wrapper = new LowerOfflineUUIDWrapper();
            } else {
                wrapper = new OfflineUUIDWrapper();
            }
            Settings.UUID.OFFLINE = true;
        }
        if (!checkVersion) {
            PS.log(C.PREFIX + " &c[WARN] Titles are disabled - please update your version of Bukkit to support this feature.");
            Settings.TITLES = false;
        } else {
            AbstractTitle.TITLE_CLASS = new DefaultTitle_19();
            if (wrapper instanceof DefaultUUIDWrapper || wrapper.getClass() == OfflineUUIDWrapper.class && !Bukkit.getOnlineMode()) {
                Settings.UUID.NATIVE_UUID_PROVIDER = true;
            }
        }
        if (Settings.UUID.OFFLINE) {
            PS.log(C.PREFIX
                    + " &6PlotSquared is using Offline Mode UUIDs either because of user preference, or because you are using an old version of "
                    + "Bukkit");
        } else {
            PS.log(C.PREFIX + " &6PlotSquared is using online UUIDs");
        }
        if (Settings.UUID.USE_SQLUUIDHANDLER) {
            return new SQLUUIDHandler(wrapper);
        } else {
            return new FileUUIDHandler(wrapper);
        }
    }

    @Override
    public ChunkManager initChunkManager() {
        return new BukkitChunkManager();
    }

    @Override
    public EventUtil initEventUtil() {
        return new BukkitEventUtil();
    }

    @Override
    public void unregister(PlotPlayer player) {
        BukkitUtil.removePlayer(player.getName());
    }

    @Override
    public void registerChunkProcessor() {
        getServer().getPluginManager().registerEvents(new ChunkListener(), this);
    }

    @Override
    public void registerWorldEvents() {
        getServer().getPluginManager().registerEvents(new WorldEvents(), this);
    }

    @Override
    public InventoryUtil initInventoryUtil() {
        return new BukkitInventoryUtil();
    }

    @Override
    public void startMetrics() {
        new Metrics(this).start();
        PS.log(C.PREFIX + "&6Metrics enabled.");
    }

    @Override
    public void setGenerator(String worldName) {
        World world = BukkitUtil.getWorld(worldName);
        if (world == null) {
            // create world
            ConfigurationSection worldConfig = PS.get().worlds.getConfigurationSection("worlds." + worldName);
            String manager = worldConfig.getString("generator.plugin", "PlotSquared");
            SetupObject setup = new SetupObject();
            setup.plotManager = manager;
            setup.setupGenerator = worldConfig.getString("generator.init", manager);
            setup.type = worldConfig.getInt("generator.type");
            setup.terrain = worldConfig.getInt("generator.terrain");
            setup.step = new ConfigurationNode[0];
            setup.world = worldName;
            SetupUtils.manager.setupWorld(setup);
        } else {
            try {
                if (!PS.get().hasPlotArea(worldName)) {
                    SetGenCB.setGenerator(BukkitUtil.getWorld(worldName));
                }
            } catch (Exception ignored) {
                PS.log("Failed to reload world: " + world);
                Bukkit.getServer().unloadWorld(world, false);
            }
        }
        world = Bukkit.getWorld(worldName);
        ChunkGenerator gen = world.getGenerator();
        if (gen instanceof BukkitPlotGenerator) {
            PS.get().loadWorld(worldName, (BukkitPlotGenerator) gen);
        } else if (gen != null) {
            PS.get().loadWorld(worldName, new BukkitPlotGenerator(worldName, gen));
        } else if (PS.get().worlds.contains("worlds." + worldName)) {
            PS.get().loadWorld(worldName, null);
        }
    }

    @Override
    public SchematicHandler initSchematicHandler() {
        return new BukkitSchematicHandler();
    }

    @Override
    public AbstractTitle initTitleManager() {
        // Already initialized in UUID handler
        return AbstractTitle.TITLE_CLASS;
    }

    @Override
    public PlotPlayer wrapPlayer(Object player) {
        if (player instanceof Player) {
            return BukkitUtil.getPlayer((Player) player);
        }
        if (player instanceof OfflinePlayer) {
            return BukkitUtil.getPlayer((OfflinePlayer) player);
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
        String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    @Override
    public ChatManager<?> initChatManager() {
        if (Settings.Chat.INTERACTIVE) {
            return new BukkitChatManager();
        } else {
            return new PlainChatManager();
        }
    }

    @Override
    public GeneratorWrapper<?> wrapPlotGenerator(IndependentPlotGenerator generator) {
        return new BukkitPlotGenerator(generator);
    }

    @Override
    public List<String> getPluginIds() {
        ArrayList<String> names = new ArrayList<>();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            names.add(plugin.getName() + ';' + plugin.getDescription().getVersion() + ':' + plugin.isEnabled());
        }
        return names;
    }
}
