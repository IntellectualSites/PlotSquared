/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit;

import com.plotsquared.bukkit.generator.BukkitHybridUtils;
import com.plotsquared.bukkit.generator.BukkitPlotGenerator;
import com.plotsquared.bukkit.listener.ChunkListener;
import com.plotsquared.bukkit.listener.EntitySpawnListener;
import com.plotsquared.bukkit.listener.PlayerEvents;
import com.plotsquared.bukkit.listener.SingleWorldListener;
import com.plotsquared.bukkit.listener.WorldEvents;
import com.plotsquared.bukkit.placeholder.PlaceholderFormatter;
import com.plotsquared.bukkit.placeholder.Placeholders;
import com.plotsquared.bukkit.queue.BukkitLocalQueue;
import com.plotsquared.bukkit.schematic.BukkitSchematicHandler;
import com.plotsquared.bukkit.util.BukkitChatManager;
import com.plotsquared.bukkit.util.BukkitEconHandler;
import com.plotsquared.bukkit.util.BukkitInventoryUtil;
import com.plotsquared.bukkit.util.BukkitSetupUtils;
import com.plotsquared.bukkit.util.BukkitTaskManager;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.bukkit.util.SetGenCB;
import com.plotsquared.bukkit.util.UpdateUtility;
import com.plotsquared.bukkit.util.uuid.DefaultUUIDWrapper;
import com.plotsquared.bukkit.util.uuid.FileUUIDHandler;
import com.plotsquared.bukkit.util.uuid.LowerOfflineUUIDWrapper;
import com.plotsquared.bukkit.util.uuid.OfflineUUIDWrapper;
import com.plotsquared.bukkit.util.uuid.SQLUUIDHandler;
import com.plotsquared.core.IPlotMain;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.ChatFormatter;
import com.plotsquared.core.configuration.ConfigurationNode;
import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.generator.GeneratorWrapper;
import com.plotsquared.core.generator.HybridGen;
import com.plotsquared.core.generator.HybridUtils;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.generator.SingleWorldGenerator;
import com.plotsquared.core.listener.PlotListener;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaTerrainType;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.SetupObject;
import com.plotsquared.core.plot.message.PlainChatManager;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import com.plotsquared.core.queue.QueueProvider;
import com.plotsquared.core.util.ChatManager;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.core.util.ConsoleColors;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.InventoryUtil;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.PremiumVerification;
import com.plotsquared.core.util.ReflectionUtils;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.SetupUtils;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.uuid.UUIDHandler;
import com.plotsquared.core.util.uuid.UUIDHandlerImplementation;
import com.plotsquared.core.util.uuid.UUIDWrapper;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extension.platform.Actor;
import lombok.Getter;
import lombok.NonNull;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.plotsquared.core.util.PremiumVerification.getDownloadID;
import static com.plotsquared.core.util.PremiumVerification.getResourceID;
import static com.plotsquared.core.util.PremiumVerification.getUserID;
import static com.plotsquared.core.util.ReflectionUtils.getRefClass;

public final class BukkitMain extends JavaPlugin implements Listener, IPlotMain {

    @Getter private static WorldEdit worldEdit;

    static {
        try {
            Settings.load(new File("plugins/PlotSquared/config/settings.yml"));
        } catch (Throwable ignored) {
        }
    }

    private int[] version;
    @Getter private String pluginName;
    @Getter private SingleWorldListener singleWorldListener;
    private Method methodUnloadChunk0;
    private boolean methodUnloadSetup = false;
    private boolean metricsStarted;
    private static final int BSTATS_ID = 1404;

    @Override public int[] getServerVersion() {
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
                PlotSquared.debug(StringMan.getString(Bukkit.getBukkitVersion()));
                PlotSquared.debug(
                    StringMan.getString(Bukkit.getBukkitVersion().split("-")[0].split("\\.")));
                return new int[] {1, 13, 0};
            }
        }
        return this.version;
    }

    @Override public String getServerImplementation() {
        return Bukkit.getVersion();
    }

    @Override public void onEnable() {
        this.pluginName = getDescription().getName();
        PlotPlayer.registerConverter(Player.class, BukkitUtil::getPlayer);

        new PlotSquared(this, "Bukkit");

        if (PlotSquared.get().IMP.getServerVersion()[1] < 13) {
            System.out.println(
                "You can't use this version of PlotSquared on a server less than Minecraft 1.13.2.");
            System.out
                .println("Please check the download page for the link to the legacy versions.");
            System.out.println("The server will now be shutdown to prevent any corruption.");
            Bukkit.shutdown();
            return;
        }

        if (PremiumVerification.isPremium() && Settings.Enabled_Components.UPDATE_NOTIFICATIONS) {
            new UpdateUtility(this).updateChecker();
        }

        if (PremiumVerification.isPremium()) {
            PlotSquared.log(
                Captions.PREFIX + "&6PlotSquared version licensed to Spigot user " + getUserID());
            PlotSquared
                .log(Captions.PREFIX + "&6https://www.spigotmc.org/resources/" + getResourceID());
            PlotSquared.log(Captions.PREFIX + "&6Download ID: " + getDownloadID());
            PlotSquared.log(Captions.PREFIX + "&6Thanks for supporting us :)");
        } else {
            PlotSquared.log(Captions.PREFIX + "&6Couldn't verify purchase :(");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders().register();
            if (Settings.Enabled_Components.EXTERNAL_PLACEHOLDERS) {
                ChatFormatter.formatters.add(new PlaceholderFormatter());
            }
            PlotSquared.log(Captions.PREFIX + "&6PlotSquared hooked into PlaceholderAPI");
        } else {
            PlotSquared
                .debug(Captions.PREFIX + "&6PlaceholderAPI is not in use. Hook deactivated.");
        }

        this.startMetrics();
        if (Settings.Enabled_Components.WORLDS) {
            TaskManager.IMP.taskRepeat(this::unload, 20);
            try {
                singleWorldListener = new SingleWorldListener(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void unload() {
        if (!this.methodUnloadSetup) {
            this.methodUnloadSetup = true;
            try {
                ReflectionUtils.RefClass classCraftWorld = getRefClass("{cb}.CraftWorld");
                this.methodUnloadChunk0 = classCraftWorld.getRealClass()
                    .getDeclaredMethod("unloadChunk0", int.class, int.class, boolean.class);
                this.methodUnloadChunk0.setAccessible(true);
            } catch (Throwable event) {
                event.printStackTrace();
            }
        }
        final PlotAreaManager manager = PlotSquared.get().getPlotAreaManager();
        if (manager instanceof SinglePlotAreaManager) {
            long start = System.currentTimeMillis();
            final SinglePlotArea area = ((SinglePlotAreaManager) manager).getArea();

            outer:
            for (final World world : Bukkit.getWorlds()) {
                final String name = world.getName();
                final char char0 = name.charAt(0);
                if (!Character.isDigit(char0) && char0 != '-') {
                    continue;
                }

                if (!world.getPlayers().isEmpty()) {
                    continue;
                }

                PlotId id;
                try {
                    id = PlotId.fromString(name);
                } catch (IllegalArgumentException ignored) {
                    continue;
                }
                final Plot plot = area.getOwnedPlot(id);
                if (plot != null) {
                    if (!MainUtil.isServerOwned(plot) || PlotPlayer.wrap(plot.getOwner()) == null) {
                        if (world.getKeepSpawnInMemory()) {
                            world.setKeepSpawnInMemory(false);
                            return;
                        }
                        final Chunk[] chunks = world.getLoadedChunks();
                        if (chunks.length == 0) {
                            if (!Bukkit.unloadWorld(world, true)) {
                                PlotSquared.debug("Failed to unload " + world.getName());
                            }
                            return;
                        } else {
                            int index = 0;
                            do {
                                final Chunk chunkI = chunks[index++];
                                boolean result;
                                if (methodUnloadChunk0 != null) {
                                    try {
                                        result = (boolean) methodUnloadChunk0
                                            .invoke(world, chunkI.getX(), chunkI.getZ(), true);
                                    } catch (Throwable e) {
                                        methodUnloadChunk0 = null;
                                        e.printStackTrace();
                                        continue outer;
                                    }
                                } else {
                                    result = world.unloadChunk(chunkI.getX(), chunkI.getZ(), true);
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

    @Override public void onDisable() {
        PlotSquared.get().disable();
        Bukkit.getScheduler().cancelTasks(this);
    }

    @Override public void log(@NonNull String message) {
        try {
            message = Captions.color(message);
            if (!Settings.Chat.CONSOLE_COLOR) {
                message = ChatColor.stripColor(message);
            }
            this.getServer().getConsoleSender().sendMessage(message);
        } catch (final Throwable ignored) {
            System.out.println(ConsoleColors.fromString(message));
        }
    }

    @Override public void shutdown() {
        this.getServer().getPluginManager().disablePlugin(this);
    }

    @Override public int[] getPluginVersion() {
        String ver = getDescription().getVersion();
        if (ver.contains("-")) {
            ver = ver.split("-")[0];
        }
        String[] split = ver.split("\\.");
        return new int[] {Integer.parseInt(split[0]), Integer.parseInt(split[1]),
            Integer.parseInt(split[2])};
    }

    @Override public String getPluginVersionString() {
        return getDescription().getVersion();
    }

    @Override public void registerCommands() {
        final BukkitCommand bukkitCommand = new BukkitCommand();
        final PluginCommand plotCommand = getCommand("plots");
        if (plotCommand != null) {
            plotCommand.setExecutor(bukkitCommand);
            plotCommand.setAliases(Arrays.asList("p", "ps", "plotme", "plot"));
            plotCommand.setTabCompleter(bukkitCommand);
        }
    }

    @Override public File getDirectory() {
        return getDataFolder();
    }

    @Override public File getWorldContainer() {
        return Bukkit.getWorldContainer();
    }

    @Override public TaskManager getTaskManager() {
        return new BukkitTaskManager(this);
    }

    @Override @SuppressWarnings("deprecation") public void runEntityTask() {
        PlotSquared.log(Captions.PREFIX + "KillAllEntities started.");
        TaskManager.runTaskRepeat(() -> PlotSquared.get().forEachPlotArea(plotArea -> {
            final World world = Bukkit.getWorld(plotArea.getWorldName());
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
                        case FISHING_HOOK:
                        case ENDER_SIGNAL:
                        case AREA_EFFECT_CLOUD:
                        case EXPERIENCE_ORB:
                        case LEASH_HITCH:
                        case FIREWORK:
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
                        case ENDER_PEARL:
                        case ARROW:
                        case LLAMA_SPIT:
                        case TRIDENT:
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
                                com.plotsquared.core.location.Location location =
                                    BukkitUtil.getLocation(entity.getLocation());
                                Plot plot = location.getPlot();
                                if (plot == null) {
                                    if (location.isPlotArea()) {
                                        if (entity.hasMetadata("ps-tmp-teleport")) {
                                            continue;
                                        }
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
                                    if (entity.hasMetadata("ps-tmp-teleport")) {
                                        continue;
                                    }
                                    iterator.remove();
                                    entity.remove();
                                }
                            }
                            continue;
                        case SMALL_FIREBALL:
                        case FIREBALL:
                        case DRAGON_FIREBALL:
                        case DROPPED_ITEM:
                            if (Settings.Enabled_Components.KILL_ROAD_ITEMS && plotArea
                                .getOwnedPlotAbs(BukkitUtil.getLocation(entity.getLocation()))
                                == null) {
                                entity.remove();
                            }
                            // dropped item
                            continue;
                        case PRIMED_TNT:
                        case FALLING_BLOCK:
                            // managed elsewhere
                            continue;
                        case SHULKER:
                            if (Settings.Enabled_Components.KILL_ROAD_MOBS) {
                                LivingEntity livingEntity = (LivingEntity) entity;
                                List<MetadataValue> meta = entity.getMetadata("shulkerPlot");
                                if (!meta.isEmpty()) {
                                    if (livingEntity.isLeashed()) {
                                        continue;
                                    }
                                    List<MetadataValue> keep = entity.getMetadata("keep");
                                    if (!keep.isEmpty()) {
                                        continue;
                                    }

                                    PlotId originalPlotId = (PlotId) meta.get(0).value();
                                    if (originalPlotId != null) {
                                        com.plotsquared.core.location.Location pLoc =
                                            BukkitUtil.getLocation(entity.getLocation());
                                        PlotArea area = pLoc.getPlotArea();
                                        if (area != null) {
                                            PlotId currentPlotId = PlotId.of(area.getPlotAbs(pLoc));
                                            if (!originalPlotId.equals(currentPlotId) && (
                                                currentPlotId == null || !area
                                                    .getPlot(originalPlotId)
                                                    .equals(area.getPlot(currentPlotId)))) {
                                                if (entity.hasMetadata("ps-tmp-teleport")) {
                                                    continue;
                                                }
                                                iterator.remove();
                                                entity.remove();
                                            }
                                        }
                                    }
                                } else {
                                    //This is to apply the metadata to already spawned shulkers (see EntitySpawnListener.java)
                                    com.plotsquared.core.location.Location pLoc =
                                        BukkitUtil.getLocation(entity.getLocation());
                                    PlotArea area = pLoc.getPlotArea();
                                    if (area != null) {
                                        PlotId currentPlotId = PlotId.of(area.getPlotAbs(pLoc));
                                        if (currentPlotId != null) {
                                            entity.setMetadata("shulkerPlot",
                                                new FixedMetadataValue(
                                                    (Plugin) PlotSquared.get().IMP, currentPlotId));
                                        }
                                    }
                                }
                            }
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
                        case PARROT:
                        case SALMON:
                        case DOLPHIN:
                        case TROPICAL_FISH:
                        case DROWNED:
                        case COD:
                        case TURTLE:
                        case PUFFERFISH:
                        case PHANTOM:
                        case ILLUSIONER:
                        case CAT:
                        case PANDA:
                        case FOX:
                        case PILLAGER:
                        case TRADER_LLAMA:
                        case WANDERING_TRADER:
                        case RAVAGER:
                            //case BEE:
                        default: {
                            if (Settings.Enabled_Components.KILL_ROAD_MOBS) {
                                Location location = entity.getLocation();
                                if (BukkitUtil.getLocation(location).isPlotRoad()) {
                                    if (entity instanceof LivingEntity) {
                                        LivingEntity livingEntity = (LivingEntity) entity;
                                        if (!livingEntity.isLeashed() || !entity
                                            .hasMetadata("keep")) {
                                            Entity passenger = entity.getPassenger();
                                            if (!(passenger instanceof Player) && entity
                                                .getMetadata("keep").isEmpty()) {
                                                if (entity.hasMetadata("ps-tmp-teleport")) {
                                                    continue;
                                                }
                                                iterator.remove();
                                                entity.remove();
                                                continue;
                                            }
                                        }
                                    } else {
                                        Entity passenger = entity.getPassenger();
                                        if (!(passenger instanceof Player) && entity
                                            .getMetadata("keep").isEmpty()) {
                                            if (entity.hasMetadata("ps-tmp-teleport")) {
                                                continue;
                                            }
                                            iterator.remove();
                                            entity.remove();
                                            continue;
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
        }), 20);
    }

    @Override @Nullable
    public final ChunkGenerator getDefaultWorldGenerator(@NotNull final String worldName,
        final String id) {
        final IndependentPlotGenerator result;
        if (id != null && id.equalsIgnoreCase("single")) {
            result = new SingleWorldGenerator();
        } else {
            result = PlotSquared.get().IMP.getDefaultGenerator();
            if (!PlotSquared.get().setupPlotWorld(worldName, id, result)) {
                return null;
            }
        }
        return (ChunkGenerator) result.specify(worldName);
    }

    @Override public void registerPlayerEvents() {
        final PlayerEvents main = new PlayerEvents();
        getServer().getPluginManager().registerEvents(main, this);
        getServer().getPluginManager().registerEvents(new EntitySpawnListener(), this);
        PlotListener.startRunnable();
    }

    @Override public void registerForceFieldEvents() {
    }

    @Override public boolean initWorldEdit() {
        if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
            worldEdit = WorldEdit.getInstance();
            return true;
        }
        return false;
    }

    @Override public EconHandler getEconomyHandler() {
        try {
            BukkitEconHandler econ = new BukkitEconHandler();
            if (econ.init()) {
                return econ;
            }
        } catch (Throwable ignored) {
            PlotSquared.debug("No economy detected!");
        }
        return null;
    }

    @Override public QueueProvider initBlockQueue() {
        //TODO Figure out why this code is still here yet isn't being called anywhere.
        //        try {
        //            new SendChunk();
        //            MainUtil.canSendChunk = true;
        //        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e) {
        //            PlotSquared.debug(
        //                SendChunk.class + " does not support " + StringMan.getString(getServerVersion()));
        //            MainUtil.canSendChunk = false;
        //        }
        return QueueProvider.of(BukkitLocalQueue.class, BukkitLocalQueue.class);
    }

    @Override public WorldUtil initWorldUtil() {
        return new BukkitUtil();
    }

    @Override @Nullable
    public GeneratorWrapper<?> getGenerator(@NonNull final String world,
        @Nullable final String name) {
        if (name == null) {
            return null;
        }
        final Plugin genPlugin = Bukkit.getPluginManager().getPlugin(name);
        if (genPlugin != null && genPlugin.isEnabled()) {
            ChunkGenerator gen = genPlugin.getDefaultWorldGenerator(world, "");
            if (gen instanceof GeneratorWrapper<?>) {
                return (GeneratorWrapper<?>) gen;
            }
            return new BukkitPlotGenerator(world, gen);
        } else {
            return new BukkitPlotGenerator(world, PlotSquared.get().IMP.getDefaultGenerator());
        }
    }

    @Override public HybridUtils initHybridUtils() {
        return new BukkitHybridUtils();
    }

    @Override public SetupUtils initSetupUtils() {
        return new BukkitSetupUtils();
    }

    @Override public void startMetrics() {
        if (this.metricsStarted) {
            return;
        }
        this.metricsStarted = true;
        Metrics metrics = new Metrics(this, BSTATS_ID);// bstats
        metrics.addCustomChart(new Metrics.DrilldownPie("area_types", () -> {
            final Map<String, Map<String, Integer>> map = new HashMap<>();
            for (final PlotAreaType plotAreaType : PlotAreaType.values()) {
                final Map<String, Integer> terrainTypes = new HashMap<>();
                for (final PlotAreaTerrainType plotAreaTerrainType : PlotAreaTerrainType.values()) {
                    terrainTypes.put(plotAreaTerrainType.name().toLowerCase(), 0);
                }
                map.put(plotAreaType.name().toLowerCase(), terrainTypes);
            }
            for (final PlotArea plotArea : PlotSquared.get().getPlotAreas()) {
                final Map<String, Integer> terrainTypeMap =
                    map.get(plotArea.getType().name().toLowerCase());
                terrainTypeMap.put(plotArea.getTerrain().name().toLowerCase(),
                    terrainTypeMap.get(plotArea.getTerrain().name().toLowerCase()) + 1);
            }
            return map;
        }));
        metrics.addCustomChart(new Metrics.SimplePie("premium",
            () -> PremiumVerification.isPremium() ? "Premium" : "Non-Premium"));
    }

    @Override public ChunkManager initChunkManager() {
        return new BukkitChunkManager();
    }

    @Override public void unregister(@NonNull final PlotPlayer player) {
        BukkitUtil.removePlayer(player.getName());
    }

    @Override public void registerChunkProcessor() {
        getServer().getPluginManager().registerEvents(new ChunkListener(), this);
    }

    @Override public void registerWorldEvents() {
        getServer().getPluginManager().registerEvents(new WorldEvents(), this);
    }

    @NotNull @Override public IndependentPlotGenerator getDefaultGenerator() {
        return new HybridGen();
    }

    @Override public InventoryUtil initInventoryUtil() {
        return new BukkitInventoryUtil();
    }

    @Override public UUIDHandlerImplementation initUUIDHandler() {
        final UUIDWrapper wrapper;
        if (Settings.UUID.OFFLINE) {
            if (Settings.UUID.FORCE_LOWERCASE) {
                wrapper = new LowerOfflineUUIDWrapper();
            } else {
                wrapper = new OfflineUUIDWrapper();
            }
            Settings.UUID.OFFLINE = true;
        } else {
            wrapper = new DefaultUUIDWrapper();
            Settings.UUID.OFFLINE = false;
        }
        if (!Bukkit.getVersion().contains("git-Spigot")) {
            if (wrapper instanceof DefaultUUIDWrapper
                || wrapper.getClass() == OfflineUUIDWrapper.class && !Bukkit.getOnlineMode()) {
                Settings.UUID.NATIVE_UUID_PROVIDER = true;
            }
        }
        if (Settings.UUID.OFFLINE) {
            PlotSquared.log(Captions.PREFIX + "&6" + getPluginName()
                + " is using Offline Mode UUIDs either because of user preference, or because you are using an old version of "
                + "Bukkit");
        } else {
            PlotSquared.log(Captions.PREFIX + "&6" + getPluginName() + " is using online UUIDs");
        }
        if (Settings.UUID.USE_SQLUUIDHANDLER) {
            return new SQLUUIDHandler(wrapper);
        } else {
            return new FileUUIDHandler(wrapper);
        }
    }

    @Override public void setGenerator(@NonNull final String worldName) {
        World world = BukkitUtil.getWorld(worldName);
        if (world == null) {
            // create world
            ConfigurationSection worldConfig =
                PlotSquared.get().worlds.getConfigurationSection("worlds." + worldName);
            String manager = worldConfig.getString("generator.plugin", getPluginName());
            SetupObject setup = new SetupObject();
            setup.plotManager = manager;
            setup.setupGenerator = worldConfig.getString("generator.init", manager);
            setup.type = MainUtil.getType(worldConfig);
            setup.terrain = MainUtil.getTerrain(worldConfig);
            setup.step = new ConfigurationNode[0];
            setup.world = worldName;
            SetupUtils.manager.setupWorld(setup);
            world = Bukkit.getWorld(worldName);
        } else {
            try {
                if (!PlotSquared.get().hasPlotArea(worldName)) {
                    SetGenCB.setGenerator(BukkitUtil.getWorld(worldName));
                }
            } catch (Exception e) {
                PlotSquared.log("Failed to reload world: " + world + " | " + e.getMessage());
                Bukkit.getServer().unloadWorld(world, false);
                return;
            }
        }
        assert world != null;
        ChunkGenerator gen = world.getGenerator();
        if (gen instanceof BukkitPlotGenerator) {
            PlotSquared.get().loadWorld(worldName, (BukkitPlotGenerator) gen);
        } else if (gen != null) {
            PlotSquared.get().loadWorld(worldName, new BukkitPlotGenerator(worldName, gen));
        } else if (PlotSquared.get().worlds.contains("worlds." + worldName)) {
            PlotSquared.get().loadWorld(worldName, null);
        }
    }

    @Override public SchematicHandler initSchematicHandler() {
        return new BukkitSchematicHandler();
    }

    @Override @Nullable public PlotPlayer wrapPlayer(final Object player) {
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

    @Override public String getNMSPackage() {
        final String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    @Override public ChatManager<?> initChatManager() {
        if (Settings.Chat.INTERACTIVE) {
            return new BukkitChatManager();
        } else {
            return new PlainChatManager();
        }
    }

    @Override
    public GeneratorWrapper<?> wrapPlotGenerator(@Nullable final String world,
        @NonNull final IndependentPlotGenerator generator) {
        return new BukkitPlotGenerator(world, generator);
    }

    @Override public List<Map.Entry<Map.Entry<String, String>, Boolean>> getPluginIds() {
        List<Map.Entry<Map.Entry<String, String>, Boolean>> names = new ArrayList<>();
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            Map.Entry<String, String> id = new AbstractMap.SimpleEntry<>(plugin.getName(),
                plugin.getDescription().getVersion());
            names.add(new AbstractMap.SimpleEntry<>(id, plugin.isEnabled()));
        }
        return names;
    }

    @Override public Actor getConsole() {
        @NotNull ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        WorldEditPlugin wePlugin =
            ((WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit"));
        return wePlugin.wrapCommandSender(console);
    }
}
