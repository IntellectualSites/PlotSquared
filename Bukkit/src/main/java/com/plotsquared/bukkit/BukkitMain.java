package com.plotsquared.bukkit;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.IPlotMain;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.*;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.bukkit.commands.DebugUUID;
import com.plotsquared.bukkit.database.plotme.ClassicPlotMeConnector;
import com.plotsquared.bukkit.database.plotme.LikePlotMeConverter;
import com.plotsquared.bukkit.database.plotme.PlotMeConnector_017;
import com.plotsquared.bukkit.generator.BukkitPlotGenerator;
import com.plotsquared.bukkit.listeners.*;
import com.plotsquared.bukkit.listeners.worldedit.WEListener;
import com.plotsquared.bukkit.titles.DefaultTitle_19;
import com.plotsquared.bukkit.util.*;
import com.plotsquared.bukkit.util.block.*;
import com.plotsquared.bukkit.uuid.*;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class BukkitMain extends JavaPlugin implements Listener, IPlotMain {
    
    public static BukkitMain THIS;
    public static WorldEditPlugin worldEdit;
    
    private int[] version;
    
    @Override
    public int[] getServerVersion() {
        if (version == null) {
            try {
                version = new int[3];
                final String[] split = Bukkit.getBukkitVersion().split("-")[0].split("\\.");
                version[0] = Integer.parseInt(split[0]);
                version[1] = Integer.parseInt(split[1]);
                if (split.length == 3) {
                    version[2] = Integer.parseInt(split[2]);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                PS.debug(StringMan.getString(Bukkit.getBukkitVersion()));
                PS.debug(StringMan.getString(Bukkit.getBukkitVersion().split("-")[0].split("\\.")));
                return new int[]{Integer.MAX_VALUE, 0, 0};
            }
        }
        return version;
    }
    
    @Override
    public void onEnable() {
        THIS = this;
        new PS(this, "Bukkit");
    }
    
    @Override
    public void onDisable() {
        PS.get().disable();
        Bukkit.getScheduler().cancelTasks(this);
        THIS = null;
    }
    
    @Override
    public void log(String message) {
        if (message == null) {
            return;
        }
        if ((THIS != null) && (Bukkit.getServer().getConsoleSender() != null)) {
            try {
                message = C.color(message);
                if (!Settings.CONSOLE_COLOR) {
                    message = ChatColor.stripColor(message);
                }
                Bukkit.getServer().getConsoleSender().sendMessage(message);
                return;
            } catch (final Throwable e) {}
        }
        System.out.println(ConsoleColors.fromString(message));
    }
    
    @Override
    public void disable() {
        if (THIS != null) {
            onDisable();
        }
    }
    
    @Override
    public int[] getPluginVersion() {
        final String[] split = getDescription().getVersion().split("\\.");
        return new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]) };
    }
    
    @Override
    public void registerCommands() {
        final BukkitCommand bcmd = new BukkitCommand();
        final PluginCommand plotCommand = getCommand("plots");
        plotCommand.setExecutor(bcmd);
        plotCommand.setAliases(Arrays.asList("p", "ps", "plotme", "plot"));
        plotCommand.setTabCompleter(bcmd);
        MainCommand.getInstance().addCommand(new DebugUUID());
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
        return new BukkitTaskManager();
    }
    
    @Override
    public void runEntityTask() {
        log(C.PREFIX.s() + "KillAllEntities started.");
        TaskManager.runTaskRepeat(new Runnable() {
            @Override
            public void run() {
                PS.get().foreachPlotArea(new RunnableVal<PlotArea>() {
                    @Override
                    public void run(PlotArea pw) {
                        World world = Bukkit.getWorld(pw.worldname);
                        try {
                            if (world == null) {
                                return;
                            }
                            List<Entity> entities = world.getEntities();
                            Iterator<Entity> iter = entities.iterator();
                            while (iter.hasNext()) {
                                Entity entity = iter.next();
                                switch (entity.getType()) {
                                    case EGG:
                                    case ENDER_CRYSTAL:
                                    case COMPLEX_PART:
                                    case FISHING_HOOK:
                                    case ENDER_SIGNAL:
                                    case EXPERIENCE_ORB:
                                    case LEASH_HITCH:
                                    case FIREWORK:
                                    case WEATHER:
                                    case LIGHTNING:
                                    case WITHER_SKULL:
                                    case UNKNOWN:
                                    case PLAYER: {
                                        // non moving / unremovable
                                        continue;
                                    }
                                    case THROWN_EXP_BOTTLE:
                                    case SPLASH_POTION:
                                    case SNOWBALL:
                                    case ENDER_PEARL:
                                    case ARROW: {
                                        // managed elsewhere | projectile
                                        continue;
                                    }
                                    case ARMOR_STAND:
                                    case ITEM_FRAME:
                                    case PAINTING: {
                                        // TEMPORARILY CLASSIFY AS VEHICLE
                                    }
                                    case MINECART:
                                    case MINECART_CHEST:
                                    case MINECART_COMMAND:
                                    case MINECART_FURNACE:
                                    case MINECART_HOPPER:
                                    case MINECART_MOB_SPAWNER:
                                    case MINECART_TNT:
                                    case BOAT: {
                                        if (!Settings.KILL_ROAD_VEHICLES) {
                                            continue;
                                        }
                                        com.intellectualcrafters.plot.object.Location loc = BukkitUtil.getLocation(entity.getLocation());
                                        Plot plot = loc.getPlot();
                                        if (plot == null) {
                                            if (loc.isPlotArea()) {
                                                iter.remove();
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
                                            iter.remove();
                                            entity.remove();
                                        }
                                        continue;
                                    }
                                    case SMALL_FIREBALL:
                                    case FIREBALL:
                                    case DROPPED_ITEM: {
                                        // dropped item
                                        continue;
                                    }
                                    case PRIMED_TNT:
                                    case FALLING_BLOCK: {
                                        // managed elsewhere
                                        continue;
                                    }
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
                                    default: {
                                        if (!Settings.KILL_ROAD_MOBS) {
                                            continue;
                                        }
                                        final Location loc = entity.getLocation();
                                        if (BukkitUtil.getLocation(loc).isPlotRoad()) {
                                            final Entity passenger = entity.getPassenger();
                                            if (!(passenger instanceof Player)) {
                                                if (entity.getMetadata("keep").isEmpty()) {
                                                    iter.remove();
                                                    entity.remove();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (final Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, 20);
    }
    
    @Override
    final public ChunkGenerator getDefaultWorldGenerator(final String world, final String id) {
        final HybridGen result = new HybridGen();
        if (!PS.get().setupPlotWorld(world, id, result)) {
            return null;
        }
        return (ChunkGenerator) result.specify();
    }
    
    @Override
    public void registerPlayerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);
        if (PS.get().checkVersion(getServerVersion(), 1, 8, 0)) {
            getServer().getPluginManager().registerEvents(new PlayerEvents_1_8(), this);
        }
        if (PS.get().checkVersion(getServerVersion(), 1, 8, 3)) {
            getServer().getPluginManager().registerEvents(new PlayerEvents_1_8_3(), this);
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
        getServer().getPluginManager().registerEvents(new ForceFieldListener(), this);
    }
    
    @Override
    public boolean initWorldEdit() {
        if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            BukkitMain.worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
            final String version = BukkitMain.worldEdit.getDescription().getVersion();
            if ((version != null) && version.startsWith("5.")) {
                log("&cThis version of WorldEdit does not support PlotSquared.");
                log("&cPlease use WorldEdit 6+ for masking support");
                log("&c - http://builds.enginehub.org/job/worldedit");
            } else {
                getServer().getPluginManager().registerEvents(new WEListener(), this);
                return true;
            }
        }
        return false;
    }
    
    @Override
    public EconHandler getEconomyHandler() {
        try {
            final BukkitEconHandler econ = new BukkitEconHandler();
            if (econ.init()) {
                return econ;
            }
        } catch (final Throwable ignored) {
        }
        return null;
    }
    
    @Override
    public PlotQueue initPlotQueue() {
        try {
            new SendChunk();
            MainUtil.canSendChunk = true;
        } catch (final Throwable e) {
            e.printStackTrace();
            MainUtil.canSendChunk = false;
        }
        if (PS.get().checkVersion(getServerVersion(), 1, 9, 0)) {
            try {
                return new FastQueue_1_9();
            }
            catch (Throwable e) {
                e.printStackTrace();
                return new SlowQueue();
            }
        }
        if (PS.get().checkVersion(getServerVersion(), 1, 8, 0)) {
            try {
                return new FastQueue_1_8_3();
            } catch (Throwable e) {
                e.printStackTrace();
                try {
                    return new FastQueue_1_8();
                } catch (Throwable e2) {
                    e2.printStackTrace();
                    return new SlowQueue();
                }
            }
        }
        try {
            return new FastQueue_1_7();
        } catch (Throwable e) {
            e.printStackTrace();
            return new SlowQueue();
        }
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
                if (new LikePlotMeConverter("AthionPlots").run(new ClassicPlotMeConnector())) {
                    return;
                }
            }
        }, 20);
        return (Bukkit.getPluginManager().getPlugin("PlotMe") != null) || (Bukkit.getPluginManager().getPlugin("AthionPlots") != null);
    }
    
    @Override
    public GeneratorWrapper<?> getGenerator(final String world, final String name) {
        if (name == null) {
            return null;
        }
        final Plugin gen_plugin = Bukkit.getPluginManager().getPlugin(name);
        if ((gen_plugin != null) && gen_plugin.isEnabled()) {
            ChunkGenerator gen = gen_plugin.getDefaultWorldGenerator(world, "");
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
        final boolean checkVersion = PS.get().checkVersion(getServerVersion(), 1, 7, 6);
        UUIDWrapper wrapper;
        if (Settings.OFFLINE_MODE) {
            if (Settings.UUID_LOWERCASE) {
                wrapper = (new LowerOfflineUUIDWrapper());
            } else {
                wrapper = (new OfflineUUIDWrapper());
            }
            Settings.OFFLINE_MODE = true;
        } else if (checkVersion) {
            wrapper = (new DefaultUUIDWrapper());
            Settings.OFFLINE_MODE = false;
        } else {
            if (Settings.UUID_LOWERCASE) {
                wrapper = (new LowerOfflineUUIDWrapper());
            } else {
                wrapper = (new OfflineUUIDWrapper());
            }
            Settings.OFFLINE_MODE = true;
        }
        if (!checkVersion) {
            log(C.PREFIX.s() + " &c[WARN] Titles are disabled - please update your version of Bukkit to support this feature.");
            Settings.TITLES = false;
            FlagManager.removeFlag(FlagManager.getFlag("titles"));
        } else {
            AbstractTitle.TITLE_CLASS = new DefaultTitle_19();
            if (wrapper instanceof DefaultUUIDWrapper || wrapper.getClass() == OfflineUUIDWrapper.class && !Bukkit.getOnlineMode()) {
                Settings.TWIN_MODE_UUID = true;
            }
        }
        if (Settings.OFFLINE_MODE) {
            log(C.PREFIX.s() + " &6PlotSquared is using Offline Mode UUIDs either because of user preference, or because you are using an old version of Bukkit");
        } else {
            log(C.PREFIX.s() + " &6PlotSquared is using online UUIDs");
        }
        return Settings.USE_SQLUUIDHANDLER ? new SQLUUIDHandler(wrapper) : new FileUUIDHandler(wrapper);
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
    public void unregister(final PlotPlayer player) {
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
    public String getServerName() {
        return Bukkit.getServerName();
    }
    
    @Override
    public void startMetrics() {
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
            log(C.PREFIX.s() + "&6Metrics enabled.");
        } catch (IOException e) {
            log(C.PREFIX.s() + "&cFailed to load up metrics.");
        }
    }
    
    @Override
    public void setGenerator(final String worldname) {
        World world = BukkitUtil.getWorld(worldname);
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
            try {
                if (!PS.get().hasPlotArea(worldname)) {
                    SetGenCB.setGenerator(BukkitUtil.getWorld(worldname));
                }
            } catch (final Exception e) {
                log("Failed to reload world: " + world);
                Bukkit.getServer().unloadWorld(world, false);
            }
        }
        world = Bukkit.getWorld(worldname);
        final ChunkGenerator gen = world.getGenerator();
        if (gen instanceof BukkitPlotGenerator) {
            PS.get().loadWorld(worldname, (BukkitPlotGenerator) gen);
        } else if (gen != null) {
            PS.get().loadWorld(worldname, new BukkitPlotGenerator(worldname, gen));
        } else {
            if (PS.get().config.contains("worlds." + worldname)) {
                PS.get().loadWorld(worldname, null);
            }
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
    public PlotPlayer wrapPlayer(final Object obj) {
        if (obj instanceof Player) {
            return BukkitUtil.getPlayer((Player) obj);
        } else if (obj instanceof OfflinePlayer) {
            return BukkitUtil.getPlayer((OfflinePlayer) obj);
        } else if (obj instanceof String) {
            return UUIDHandler.getPlayer((String) obj);
        } else if (obj instanceof UUID) {
            return UUIDHandler.getPlayer((UUID) obj);
        }
        return null;
    }
    
    @Override
    public String getNMSPackage() {
        final Server server = Bukkit.getServer();
        final Class<?> bukkitServerClass = server.getClass();
        String[] pas = bukkitServerClass.getName().split("\\.");
        if (pas.length == 5) {
            return pas[3];
        }
        try {
            final Method getHandle = bukkitServerClass.getDeclaredMethod("getHandle");
            final Object handle = getHandle.invoke(server);
            final Class handleServerClass = handle.getClass();
            pas = handleServerClass.getName().split("\\.");
            if (pas.length == 5) {
                return pas[3];
            }
        } catch (IllegalAccessException | InvocationTargetException | SecurityException | NoSuchMethodException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        PS.debug("Unknown NMS package: " + StringMan.getString(pas));
        return "1_8_R3";
    }
    
    @Override
    public ChatManager<?> initChatManager() {
        if (Settings.FANCY_CHAT) {
            return new BukkitChatManager();
        } else {
            return new BukkitPlainChatManager();
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
            names.add(plugin.getName() + ";" + plugin.getDescription().getVersion() + ":" + plugin.isEnabled());
        }
        return names;
    }
}
