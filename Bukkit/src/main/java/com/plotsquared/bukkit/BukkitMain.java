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
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.object.chat.PlainChatManager;
import com.intellectualcrafters.plot.object.worlds.PlotAreaManager;
import com.intellectualcrafters.plot.object.worlds.SinglePlotArea;
import com.intellectualcrafters.plot.object.worlds.SinglePlotAreaManager;
import com.intellectualcrafters.plot.object.worlds.SingleWorldGenerator;
import com.intellectualcrafters.plot.util.*;
import com.intellectualcrafters.plot.util.block.QueueProvider;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.bukkit.database.plotme.ClassicPlotMeConnector;
import com.plotsquared.bukkit.database.plotme.LikePlotMeConverter;
import com.plotsquared.bukkit.database.plotme.PlotMeConnector_017;
import com.plotsquared.bukkit.generator.BukkitPlotGenerator;
import com.plotsquared.bukkit.listeners.*;
import com.plotsquared.bukkit.titles.DefaultTitle_111;
import com.plotsquared.bukkit.util.*;
import com.plotsquared.bukkit.util.block.*;
import com.plotsquared.bukkit.uuid.*;
import com.sk89q.worldedit.WorldEdit;
import org.bukkit.Location;
import org.bukkit.*;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

public final class BukkitMain extends JavaPlugin implements Listener, IPlotMain {

    private static ConcurrentHashMap<String, Plugin> pluginMap;

    static {
        // Disable AWE as otherwise both fail to load
        PluginManager manager = Bukkit.getPluginManager();
        try {
            Settings.load(new File("plugins/PlotSquared/config/settings.yml"));
            if (Settings.Enabled_Components.PLOTME_CONVERTER) { // Only disable PlotMe if conversion is enabled
                Field pluginsField = manager.getClass().getDeclaredField("plugins");
                Field lookupNamesField = manager.getClass().getDeclaredField("lookupNames");
                pluginsField.setAccessible(true);
                lookupNamesField.setAccessible(true);
                List<Plugin> plugins = (List<Plugin>) pluginsField.get(manager);
                plugins.removeIf(plugin -> plugin.getName().startsWith("PlotMe"));
                Map<String, Plugin> lookupNames = (Map<String, Plugin>) lookupNamesField.get(manager);
                lookupNames.remove("PlotMe");
                lookupNames.remove("PlotMe-DefaultGenerator");
                pluginsField.set(manager, new ArrayList<Plugin>(plugins) {
                    @Override
                    public boolean add(Plugin plugin) {
                        if (plugin.getName().startsWith("PlotMe")) {
                            System.out.print("Disabling `" + plugin.getName() + "` for PlotMe conversion (configure in PlotSquared settings.yml)");
                        } else {
                            return super.add(plugin);
                        }
                        return false;
                    }
                });
                pluginMap = new ConcurrentHashMap<String, Plugin>(lookupNames) {
                    @Override
                    public Plugin put(String key, Plugin plugin) {
                        if (!plugin.getName().startsWith("PlotMe")) {
                            return super.put(key, plugin);
                        }
                        return null;
                    }
                };
                lookupNamesField.set(manager, pluginMap);
            }
        } catch (Throwable ignore) {}
    }

    public static WorldEdit worldEdit;

    private int[] version;
    private String name;
    private SingleWorldListener singleWorldListener;
    private boolean metricsStarted;

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
                return new int[]{1, 13, 0};
            }
        }
        return this.version;
    }

    @Nonnull @Override
    public String getServerImplementation() {
        return Bukkit.getVersion();
    }

    @Override
    public void onEnable() {
        if (getServerVersion()[1] > 12) {
            Bukkit.getLogger().severe("================================================");
            Bukkit.getLogger().severe("====== THIS PLOTSQUARED VERSION IS NOT ======");
            Bukkit.getLogger().severe("======  FOR USE ON MINECRAFT 1.13 OR   ======");
            Bukkit.getLogger().severe("======     ABOVE. DISABLING PLUGIN.    ======");
            Bukkit.getLogger().severe("======          DOWNLOAD FROM          ======");
            Bukkit.getLogger().severe("https://ci.athion.net/job/PlotSquared-Breaking/");
            Bukkit.getLogger().severe("==============================================");
            Bukkit.getPluginManager().disablePlugin(this);
            throw new UnsupportedOperationException("INCORRECT PLOTSQUARED VERSION");
        }
        if (pluginMap != null) {
            pluginMap.put("PlotMe-DefaultGenerator", this);
        }
        this.name = getDescription().getName();
        new PS(this, "Bukkit");
        if (Settings.Enabled_Components.WORLDS) {
            TaskManager.IMP.taskRepeat(this::unload, 20);
            try {
                singleWorldListener = new SingleWorldListener(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public SingleWorldListener getSingleWorldListener() {
        return singleWorldListener;
    }

    private Method methodUnloadChunk0;
    private boolean methodUnloadSetup = false;

    public void unload() {
        if (!methodUnloadSetup) {
            methodUnloadSetup = true;
            try {
                ReflectionUtils.RefClass classCraftWorld = getRefClass("{cb}.CraftWorld");
                methodUnloadChunk0 = classCraftWorld.getRealClass().getDeclaredMethod("unloadChunk0", int.class, int.class, boolean.class);
                methodUnloadChunk0.setAccessible(true);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        PlotAreaManager manager = PS.get().getPlotAreaManager();
        if (manager instanceof SinglePlotAreaManager) {
            long start = System.currentTimeMillis();
            SinglePlotArea area = ((SinglePlotAreaManager) manager).getArea();
            outer:
            for (World world : Bukkit.getWorlds()) {
                String name = world.getName();
                char char0 = name.charAt(0);
                if (!Character.isDigit(char0) && char0 != '-') continue;
                if (!world.getPlayers().isEmpty()) {
                    continue;
                }

                PlotId id = PlotId.fromString(name);
                if (id != null) {
                    Plot plot = area.getOwnedPlot(id);
                    if (plot != null) {
                        if (PlotPlayer.wrap(plot.owner) == null) {
                            if (world.getKeepSpawnInMemory()) {
                                world.setKeepSpawnInMemory(false);
                                return;
                            }
                            Chunk[] chunks = world.getLoadedChunks();
                            if (chunks.length == 0) {
                                if (!Bukkit.unloadWorld(world, true)) {
                                    PS.debug("Failed to unload " + world.getName());
                                }
                                return;
                            } else {
                                int index = 0;
                                do {
                                    Chunk chunkI = chunks[index++];
                                    boolean result;
                                    if (methodUnloadChunk0 != null) {
                                        try {
                                            result = (boolean) methodUnloadChunk0.invoke(world, chunkI.getX(), chunkI.getZ(), true);
                                        } catch (Throwable e) {
                                            methodUnloadChunk0 = null;
                                            e.printStackTrace();
                                            continue outer;
                                        }
                                    } else {
                                        result = world.unloadChunk(chunkI.getX(), chunkI.getZ(), true, false);
                                    }
                                    if (!result) {
                                        continue outer;
                                    }
                                    if (System.currentTimeMillis() - start > 5) {
                                        return;
                                    }
                                } while (index < chunks.length);
                            }
                        }
                    }
                }
            }
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
    public String getPluginName() {
        return name;
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
        TaskManager.runTaskRepeat(() -> PS.get().foreachPlotArea(new RunnableVal<PlotArea>() {
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
                            case LLAMA_SPIT:
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
                            case BOAT:
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
                            case SMALL_FIREBALL:
                            case FIREBALL:
                            case DRAGON_FIREBALL:
                            case DROPPED_ITEM:
                                if (Settings.Enabled_Components.KILL_ROAD_ITEMS && plotArea.getOwnedPlotAbs(BukkitUtil.getLocation(entity.getLocation())) == null) {
                                    entity.remove();
                                }
                                // dropped item
                                continue;
                            case PRIMED_TNT:
                            case FALLING_BLOCK:
                                // managed elsewhere
                                continue;
                            case LLAMA:
                            case DONKEY:
                            case MULE:
                            case ZOMBIE_HORSE:
                            case SKELETON_HORSE:
                            case HUSK:
                            case ELDER_GUARDIAN:
                            case WITHER_SKELETON:
                            case STRAY:
                            case ZOMBIE_VILLAGER:
                            case EVOKER:
                            case EVOKER_FANGS:
                            case VEX:
                            case VINDICATOR:
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
                            default: {
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
                                                    continue;
                                                }
                                            }
                                        } else {
                                            Entity passenger = entity.getPassenger();
                                            if (!(passenger instanceof Player) && entity.getMetadata("keep").isEmpty()) {
                                                iterator.remove();
                                                entity.remove();
                                                continue;
                                            }
                                        }
                                    }
                                }
                                continue;
                            }
                            case SHULKER: {
                                if (Settings.Enabled_Components.KILL_ROAD_MOBS) {
                                    LivingEntity livingEntity = (LivingEntity) entity;
                                    List<MetadataValue> meta = entity.getMetadata("plot");
                                    if (meta != null && !meta.isEmpty()) {
                                        if (livingEntity.isLeashed()) continue;

                                        List<MetadataValue> keep = entity.getMetadata("keep");
                                        if (keep != null && !keep.isEmpty()) continue;

                                        PlotId originalPlotId = (PlotId) meta.get(0).value();
                                        if (originalPlotId != null) {
                                            com.intellectualcrafters.plot.object.Location pLoc = BukkitUtil.getLocation(entity.getLocation());
                                            PlotArea area = pLoc.getPlotArea();
                                            if (area != null) {
                                                PlotId currentPlotId = PlotId.of(area.getPlotAbs(pLoc));
                                                if (!originalPlotId.equals(currentPlotId) && (currentPlotId == null || !area.getPlot(originalPlotId).equals(area.getPlot(currentPlotId)))) {
                                                    iterator.remove();
                                                    entity.remove();
                                                }
                                            }
                                        }
                                    } else {
                                        //This is to apply the metadata to already spawned shulkers (see EntitySpawnListener.java)
                                        com.intellectualcrafters.plot.object.Location pLoc = BukkitUtil.getLocation(entity.getLocation());
                                        PlotArea area = pLoc.getPlotArea();
                                        if (area != null) {
                                            PlotId currentPlotId = PlotId.of(area.getPlotAbs(pLoc));
                                            if (currentPlotId != null) {
                                                entity.setMetadata("plot", new FixedMetadataValue((Plugin) PS.get().IMP, currentPlotId));
                                            }
                                        }
                                    }
                                }
                                continue;
                            }
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }), 20);
    }

    @Override
    public final ChunkGenerator getDefaultWorldGenerator(String world, String id) {
        if (Settings.Enabled_Components.PLOTME_CONVERTER) {
            initPlotMeConverter();
            Settings.Enabled_Components.PLOTME_CONVERTER = false;
        }
        IndependentPlotGenerator result;
        if (id != null && id.equalsIgnoreCase("single")) {
            result = new SingleWorldGenerator();
        } else {
            result = PS.get().IMP.getDefaultGenerator();
            if (!PS.get().setupPlotWorld(world, id, result)) {
                return null;
            }
        }
        return (ChunkGenerator) result.specify(world);
    }

    @Override
    public void registerPlayerEvents() {
        PlayerEvents main = new PlayerEvents();
        getServer().getPluginManager().registerEvents(main, this);
        try {
            getServer().getClass().getMethod("spigot");
            Class.forName("org.bukkit.event.entity.EntitySpawnEvent");
            getServer().getPluginManager().registerEvents(new EntitySpawnListener(), this);
        } catch (NoSuchMethodException | ClassNotFoundException ignored) {
            PS.debug("Not running Spigot. Skipping EntitySpawnListener event.");
        }
        if (PS.get().checkVersion(getServerVersion(), 1, 7, 9)) {
            try {
                getServer().getPluginManager().registerEvents(new EntityPortal_1_7_9(), this);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (PS.get().checkVersion(getServerVersion(), BukkitVersion.v1_8_0)) {
            try {
                getServer().getPluginManager().registerEvents(new PlayerEvents_1_8(), this);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (PS.get().checkVersion(getServerVersion(), BukkitVersion.v1_8_3)) {
            try {
                getServer().getPluginManager().registerEvents(new PlayerEvents183(), this);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        if (PS.get().checkVersion(getServerVersion(), BukkitVersion.v1_9_0)) {
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
        if (PS.get().checkVersion(getServerVersion(), BukkitVersion.v1_12_0)) {
            getServer().getPluginManager().registerEvents(new PlotPlusListener_1_12(), this);
        } else {
            getServer().getPluginManager().registerEvents(new PlotPlusListener_Legacy(), this);
        }
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
		if (PS.get().checkVersion(getServerVersion(), BukkitVersion.v1_13_0)) {
            return QueueProvider.of(BukkitLocalQueue.class, BukkitLocalQueue.class);
        }
        if (PS.get().checkVersion(getServerVersion(), BukkitVersion.v1_9_0)) {
            return QueueProvider.of(BukkitLocalQueue_1_9.class, BukkitLocalQueue.class);
        }
        if (PS.get().checkVersion(getServerVersion(), BukkitVersion.v1_8_3)) {
            return QueueProvider.of(BukkitLocalQueue_1_8_3.class, BukkitLocalQueue.class);
        }
        if (PS.get().checkVersion(getServerVersion(), BukkitVersion.v1_8_0)) {
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
        if (new LikePlotMeConverter("PlotMe").run(new ClassicPlotMeConnector())) {
            return true;
        } else if (new LikePlotMeConverter("PlotMe").run(new PlotMeConnector_017())) {
            return true;
        }
        return false;
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
            return new BukkitPlotGenerator(PS.get().IMP.getDefaultGenerator());
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
        boolean checkVersion = false;
        try {
            OfflinePlayer.class.getDeclaredMethod("getUniqueId");
            checkVersion = true;
        } catch (Throwable ignore) {}
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
            AbstractTitle.TITLE_CLASS = new DefaultTitle_111();
            if (wrapper instanceof DefaultUUIDWrapper || wrapper.getClass() == OfflineUUIDWrapper.class && !Bukkit.getOnlineMode()) {
                Settings.UUID.NATIVE_UUID_PROVIDER = true;
            }
        }
        if (Settings.UUID.OFFLINE) {
            PS.log(C.PREFIX
                    + " &6" + getPluginName() + " is using Offline Mode UUIDs either because of user preference, or because you are using an old version of "
                    + "Bukkit");
        } else {
            PS.log(C.PREFIX + " &6" + getPluginName() + " is using online UUIDs");
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
    public IndependentPlotGenerator getDefaultGenerator() {
        return new HybridGen();
    }

    @Override
    public InventoryUtil initInventoryUtil() {
        return new BukkitInventoryUtil();
    }

    @Override
    public void startMetrics() {
        if (this.metricsStarted) {
            return;
        }
        Metrics metrics = new Metrics(this);// bstats
        PS.log(C.PREFIX + "&6Metrics enabled.");
        this.metricsStarted = true;
    }

    @Override
    public void setGenerator(String worldName) {
        World world = BukkitUtil.getWorld(worldName);
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
            world = Bukkit.getWorld(worldName);
        } else {
            try {
                if (!PS.get().hasPlotArea(worldName)) {
                    SetGenCB.setGenerator(BukkitUtil.getWorld(worldName));
                }
            } catch (Exception ex) {
                PS.log("Failed to reload world: " + world + " | " + ex.getMessage());
                Bukkit.getServer().unloadWorld(world, false);
                return;
            }
        }
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
    public GeneratorWrapper<?> wrapPlotGenerator(String world, IndependentPlotGenerator generator) {
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
