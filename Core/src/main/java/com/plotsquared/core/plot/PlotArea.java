/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.plot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.collection.QuadMap;
import com.plotsquared.core.configuration.ConfigurationNode;
import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.ConfigurationUtil;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.generator.GridPlotWorld;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.inject.annotations.WorldConfig;
import com.plotsquared.core.location.BlockLoc;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.flag.FlagContainer;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.PlotExpression;
import com.plotsquared.core.util.RegionUtil;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author Jesse Boyd, Alexander Söderberg
 */
public abstract class PlotArea implements ComponentLike {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + PlotArea.class.getSimpleName());
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();
    private static final DecimalFormat FLAG_DECIMAL_FORMAT = new DecimalFormat("0");

    static {
        FLAG_DECIMAL_FORMAT.setMaximumFractionDigits(340);
    }

    protected final ConcurrentHashMap<PlotId, Plot> plots = new ConcurrentHashMap<>();
    @NonNull
    private final String worldName;
    private final String id;
    @NonNull
    private final PlotManager plotManager;
    private final int worldHash;
    private final PlotId min;
    private final PlotId max;
    @NonNull
    private final IndependentPlotGenerator generator;
    /**
     * Area flag container
     */
    private final FlagContainer flagContainer =
            new FlagContainer(GlobalFlagContainer.getInstance());
    private final FlagContainer roadFlagContainer =
            new FlagContainer(GlobalFlagContainer.getInstance());
    private final YamlConfiguration worldConfiguration;
    private final GlobalBlockQueue globalBlockQueue;
    private boolean roadFlags = false;
    private boolean autoMerge = false;
    private boolean allowSigns = true;
    private boolean miscSpawnUnowned = false;
    private boolean mobSpawning = false;
    private boolean mobSpawnerSpawning = false;
    private BiomeType plotBiome = BiomeTypes.FOREST;
    private boolean plotChat = true;
    private boolean forcingPlotChat = false;
    private boolean schematicClaimSpecify = false;
    private boolean schematicOnClaim = false;
    private String schematicFile = "null";
    private boolean spawnEggs = false;
    private boolean spawnCustom = true;
    private boolean spawnBreeding = false;
    private PlotAreaType type = PlotAreaType.NORMAL;
    private PlotAreaTerrainType terrain = PlotAreaTerrainType.NONE;
    private boolean homeAllowNonmember = false;
    private BlockLoc nonmemberHome;
    private BlockLoc defaultHome;
    private int maxBuildHeight = PlotSquared.platform().versionMaxHeight() + 1; // Exclusive
    private int minBuildHeight = PlotSquared.platform().versionMinHeight() + 1; // Inclusive
    private int maxGenHeight = PlotSquared.platform().versionMaxHeight(); // Inclusive
    private int minGenHeight = PlotSquared.platform().versionMinHeight(); // Inclusive
    private GameMode gameMode = GameModes.CREATIVE;
    private Map<String, PlotExpression> prices = new HashMap<>();
    private List<String> schematics = new ArrayList<>();
    private boolean worldBorder = false;
    private int borderSize = 1;
    private boolean useEconomy = false;
    private int hash;
    private CuboidRegion region;
    private ConcurrentHashMap<String, Object> meta;
    private QuadMap<PlotCluster> clusters;
    private String signMaterial = "OAK_WALL_SIGN";
    private String legacySignMaterial = "WALL_SIGN";

    public PlotArea(
            final @NonNull String worldName, final @Nullable String id,
            @NonNull IndependentPlotGenerator generator, final @Nullable PlotId min,
            final @Nullable PlotId max,
            @WorldConfig final @Nullable YamlConfiguration worldConfiguration,
            final @NonNull GlobalBlockQueue blockQueue
    ) {
        this.worldName = worldName;
        this.id = id;
        this.plotManager = createManager();
        this.generator = generator;
        this.globalBlockQueue = blockQueue;
        if (min == null || max == null) {
            if (min != max) {
                throw new IllegalArgumentException(
                        "None of the ids can be null for this constructor");
            }
            this.min = null;
            this.max = null;
        } else {
            this.min = min;
            this.max = max;
        }
        this.worldHash = worldName.hashCode();
        this.worldConfiguration = worldConfiguration;
    }

    private static void parseFlags(FlagContainer flagContainer, List<String> flagStrings) {
        for (final String key : flagStrings) {
            final String[] split;
            if (key.contains(";")) {
                split = key.split(";");
            } else {
                split = key.split(":");
            }
            final PlotFlag<?, ?> flagInstance =
                    GlobalFlagContainer.getInstance().getFlagFromString(split[0]);
            if (flagInstance != null) {
                try {
                    flagContainer.addFlag(flagInstance.parse(split[1]));
                } catch (final FlagParseException e) {
                    LOGGER.warn(
                            "Failed to parse default flag with key '{}' and value '{}'. "
                                    + "Reason: {}. This flag will not be added as a default flag.",
                            e.getFlag().getName(),
                            e.getValue(),
                            e.getErrorMessage()
                    );
                    e.printStackTrace();
                }
            } else {
                flagContainer.addUnknownFlag(split[0], split[1]);
            }
        }
    }

    @NonNull
    protected abstract PlotManager createManager();

    public QueueCoordinator getQueue() {
        return this.globalBlockQueue.getNewQueue(PlotSquared.platform().worldUtil().getWeWorld(worldName));
    }

    /**
     * Returns the region for this PlotArea, or a CuboidRegion encompassing
     * the whole world if none exists.
     *
     * @return CuboidRegion
     */
    public CuboidRegion getRegion() {
        this.region = getRegionAbs();
        if (this.region == null) {
            return new CuboidRegion(
                    BlockVector3.at(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE),
                    BlockVector3.at(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE)
            );
        }
        return this.region;
    }

    /**
     * Returns the region for this PlotArea.
     *
     * @return CuboidRegion or null if no applicable region
     */
    private CuboidRegion getRegionAbs() {
        if (this.region == null) {
            if (this.min != null) {
                Location bot = getPlotManager().getPlotBottomLocAbs(this.min);
                Location top = getPlotManager().getPlotTopLocAbs(this.max);
                BlockVector3 pos1 = bot.getBlockVector3().subtract(BlockVector3.ONE);
                BlockVector3 pos2 = top.getBlockVector3().add(BlockVector3.ONE);
                this.region = new CuboidRegion(pos1, pos2);
            }
        }
        return this.region;
    }

    /**
     * Returns the minimum value of a {@link PlotId}.
     *
     * @return the minimum value for a {@link PlotId}
     */
    public @NonNull PlotId getMin() {
        return this.min == null ? PlotId.of(Integer.MIN_VALUE, Integer.MIN_VALUE) : this.min;
    }

    /**
     * Returns the max PlotId.
     *
     * @return the maximum value for a {@link PlotId}
     */
    public @NonNull PlotId getMax() {
        return this.max == null ? PlotId.of(Integer.MAX_VALUE, Integer.MAX_VALUE) : this.max;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PlotArea plotarea = (PlotArea) obj;
        return this.getWorldHash() == plotarea.getWorldHash() && this.getWorldName()
                .equals(plotarea.getWorldName()) && StringMan.isEqual(this.getId(), plotarea.getId());
    }

    public Set<PlotCluster> getClusters() {
        return this.clusters == null ? new HashSet<>() : this.clusters.getAll();
    }

    /**
     * Check if a PlotArea is compatible (move/copy etc.).
     *
     * @param plotArea the {@link PlotArea} to compare
     * @return {@code true} if both areas are compatible
     */
    public boolean isCompatible(final @NonNull PlotArea plotArea) {
        final ConfigurationSection section = this.worldConfiguration.getConfigurationSection("worlds");
        for (ConfigurationNode setting : plotArea.getSettingNodes()) {
            Object constant = section.get(plotArea.worldName + '.' + setting.getConstant());
            if (constant == null || !constant
                    .equals(section.get(this.worldName + '.' + setting.getConstant()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * When a world is created, the following method will be called for each.
     *
     * @param config Configuration Section
     */
    public void loadDefaultConfiguration(ConfigurationSection config) {
        if ((this.min != null || this.max != null) && !(this instanceof GridPlotWorld)) {
            throw new IllegalArgumentException("Must extend GridPlotWorld to provide");
        }
        if (config.contains("generator.terrain")) {
            this.terrain = ConfigurationUtil.getTerrain(config);
            this.type = ConfigurationUtil.getType(config);
        }
        this.mobSpawning = config.getBoolean("natural_mob_spawning");
        this.miscSpawnUnowned = config.getBoolean("misc_spawn_unowned");
        this.mobSpawnerSpawning = config.getBoolean("mob_spawner_spawning");
        this.autoMerge = config.getBoolean("plot.auto_merge");
        this.allowSigns = config.getBoolean("plot.create_signs");
        if (PlotSquared.platform().serverVersion()[1] == 13) {
            this.legacySignMaterial = config.getString("plot.legacy_sign_material");
        } else {
            this.signMaterial = config.getString("plot.sign_material");
        }
        String biomeString = config.getString("plot.biome");
        if (!biomeString.startsWith("minecraft:")) {
            biomeString = "minecraft:" + biomeString;
            config.set("plot.biome", biomeString.toLowerCase());
        }
        this.plotBiome = ConfigurationUtil.BIOME.parseString(biomeString.toLowerCase());
        this.schematicOnClaim = config.getBoolean("schematic.on_claim");
        this.schematicFile = config.getString("schematic.file");
        this.schematicClaimSpecify = config.getBoolean("schematic.specify_on_claim");
        this.schematics = new ArrayList<>(config.getStringList("schematic.schematics"));
        this.schematics.replaceAll(String::toLowerCase);
        this.useEconomy = config.getBoolean("economy.use");
        ConfigurationSection priceSection = config.getConfigurationSection("economy.prices");
        if (this.useEconomy) {
            this.prices = new HashMap<>();
            for (String key : priceSection.getKeys(false)) {
                String raw = priceSection.getString(key);
                if (raw.contains("{arg}")) {
                    raw = raw.replace("{arg}", "plots");
                    priceSection.set(key, raw); // update if replaced
                }
                this.prices.put(key, PlotExpression.compile(raw, "plots"));
            }
        }
        this.plotChat = config.getBoolean("chat.enabled");
        this.forcingPlotChat = config.getBoolean("chat.forced");
        this.worldBorder = config.getBoolean("world.border");
        this.borderSize = config.getInt("world.border_size");
        this.maxBuildHeight = config.getInt("world.max_height");
        this.minBuildHeight = config.getInt("world.min_height");
        this.minGenHeight = config.getInt("world.min_gen_height");
        this.maxGenHeight = config.getInt("world.max_gen_height");

        switch (config.getString("world.gamemode").toLowerCase()) {
            case "creative", "c", "1" -> this.gameMode = GameModes.CREATIVE;
            case "adventure", "a", "2" -> this.gameMode = GameModes.ADVENTURE;
            case "spectator", "3" -> this.gameMode = GameModes.SPECTATOR;
            default -> this.gameMode = GameModes.SURVIVAL;
        }

        String homeNonMembers = config.getString("home.nonmembers");
        String homeDefault = config.getString("home.default");
        this.defaultHome = BlockLoc.fromString(homeDefault);
        this.homeAllowNonmember = homeNonMembers.equalsIgnoreCase(homeDefault);
        if (this.homeAllowNonmember) {
            this.nonmemberHome = defaultHome;
        } else {
            this.nonmemberHome = BlockLoc.fromString(homeNonMembers);
        }

        if ("side".equalsIgnoreCase(homeDefault)) {
            this.defaultHome = null;
        } else if (StringMan.isEqualIgnoreCaseToAny(homeDefault, "center", "middle", "centre")) {
            this.defaultHome = new BlockLoc(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } else {
            try {
                /*String[] split = homeDefault.split(",");
                this.DEFAULT_HOME =
                    new PlotLoc(Integer.parseInt(split[0]), Integer.parseInt(split[1]));*/
                this.defaultHome = BlockLoc.fromString(homeDefault);
            } catch (NumberFormatException ignored) {
                this.defaultHome = null;
            }
        }

        this.spawnEggs = config.getBoolean("event.spawn.egg");
        this.spawnCustom = config.getBoolean("event.spawn.custom");
        this.spawnBreeding = config.getBoolean("event.spawn.breeding");

        if (PlotSquared.get().isWeInitialised()) {
            loadFlags(config);
        } else {
            ConsolePlayer.getConsole().sendMessage(
                    TranslatableCaption.of("flags.delaying_loading_area_flags"),
                    TagResolver.resolver("area", Tag.inserting(Component.text(this.id == null ? this.worldName : this.id)))
            );
            TaskManager.runTaskLater(() -> loadFlags(config), TaskTime.ticks(1));
        }

        loadConfiguration(config);
    }

    private void loadFlags(ConfigurationSection config) {
        ConsolePlayer.getConsole().sendMessage(
                TranslatableCaption.of("flags.loading_area_flags"),
                TagResolver.resolver("area", Tag.inserting(Component.text(this.id == null ? this.worldName : this.id)))
        );
        List<String> flags = config.getStringList("flags.default");
        if (flags.isEmpty()) {
            flags = config.getStringList("flags");
            if (flags.isEmpty()) {
                flags = new ArrayList<>();
                ConfigurationSection section = config.getConfigurationSection("flags");
                Set<String> keys = section.getKeys(false);
                for (String key : keys) {
                    if (!"default".equals(key)) {
                        flags.add(key + ';' + section.get(key));
                    }
                }
            }
        }
        parseFlags(this.getFlagContainer(), flags);
        ConsolePlayer.getConsole().sendMessage(
                TranslatableCaption.of("flags.area_flags"),
                TagResolver.resolver("flags", Tag.inserting(Component.text(flags.toString())))
        );

        List<String> roadflags = config.getStringList("road.flags");
        if (roadflags.isEmpty()) {
            roadflags = new ArrayList<>();
            ConfigurationSection section = config.getConfigurationSection("road.flags");
            Set<String> keys = section.getKeys(false);
            for (String key : keys) {
                if (!"default".equals(key)) {
                    roadflags.add(key + ';' + section.get(key));
                }
            }
        }
        this.roadFlags = !roadflags.isEmpty();
        parseFlags(this.getRoadFlagContainer(), roadflags);
        ConsolePlayer.getConsole().sendMessage(
                TranslatableCaption.of("flags.road_flags"),
                TagResolver.resolver("flags", Tag.inserting(Component.text(roadflags.toString())))
        );
    }

    public abstract void loadConfiguration(ConfigurationSection config);

    /**
     * Saving core PlotArea settings.
     *
     * @param config Configuration Section
     */
    public void saveConfiguration(ConfigurationSection config) {
        HashMap<String, Object> options = new HashMap<>();
        options.put("natural_mob_spawning", this.isMobSpawning());
        options.put("misc_spawn_unowned", this.isMiscSpawnUnowned());
        options.put("mob_spawner_spawning", this.isMobSpawnerSpawning());
        options.put("plot.auto_merge", this.isAutoMerge());
        options.put("plot.create_signs", this.allowSigns());
        if (PlotSquared.platform().serverVersion()[1] == 13) {
            options.put("plot.legacy_sign_material", this.legacySignMaterial);
        } else {
            options.put("plot.sign_material", this.signMaterial());
        }
        options.put("plot.biome", "minecraft:forest");
        options.put("schematic.on_claim", this.isSchematicOnClaim());
        options.put("schematic.file", this.getSchematicFile());
        options.put("schematic.specify_on_claim", this.isSchematicClaimSpecify());
        options.put("schematic.schematics", this.getSchematics());
        options.put("economy.use", this.useEconomy());
        options.put("economy.prices.claim", 100);
        options.put("economy.prices.merge", 100);
        options.put("economy.prices.sell", 100);
        options.put("chat.enabled", this.isPlotChat());
        options.put("chat.forced", this.isForcingPlotChat());
        options.put("flags.default", null);
        options.put("event.spawn.egg", this.isSpawnEggs());
        options.put("event.spawn.custom", this.isSpawnCustom());
        options.put("event.spawn.breeding", this.isSpawnBreeding());
        options.put("world.border", this.hasWorldBorder());
        options.put("world.border_size", this.getBorderSize());
        options.put("home.default", "side");
        String position = config.getString(
                "home.nonmembers",
                config.getBoolean("home.allow-nonmembers", false) ?
                        config.getString("home.default", "side") :
                        "side"
        );
        options.put("home.nonmembers", position);
        options.put("world.max_height", this.getMaxBuildHeight());
        options.put("world.min_height", this.getMinBuildHeight());
        options.put("world.min_gen_height", this.getMinGenHeight());
        options.put("world.max_gen_height", this.getMaxGenHeight());
        options.put("world.gamemode", this.getGameMode().getName().toLowerCase());
        options.put("road.flags.default", null);

        if (this.getType() != PlotAreaType.NORMAL) {
            options.put("generator.terrain", this.getTerrain());
            options.put("generator.type", this.getType().toString());
        }
        ConfigurationNode[] settings = getSettingNodes();
        /*
         * Saving generator specific settings
         */
        for (ConfigurationNode setting : settings) {
            options.put(setting.getConstant(), setting.getValue());
        }
        for (Entry<String, Object> stringObjectEntry : options.entrySet()) {
            if (!config.contains(stringObjectEntry.getKey())) {
                config.set(stringObjectEntry.getKey(), stringObjectEntry.getValue());
            }
        }
        if (!config.contains("flags")) {
            config.set(
                    "flags.use",
                    "63,64,68,69,71,77,96,143,167,193,194,195,196,197,77,143,69,70,72,147,148,107,183,184,185,186,187,132"
            );
        }
        if (!config.contains("road.flags")) {
            config.set("road.flags.liquid-flow", false);
        }
    }

    @NonNull
    @Override
    public String toString() {
        if (this.getId() == null) {
            return this.getWorldName();
        } else {
            return this.getWorldName() + ";" + this.getId();
        }
    }

    @Override
    public @NotNull Component asComponent() {
        return Component.text(toString());
    }

    @Override
    public int hashCode() {
        if (this.hash != 0) {
            return this.hash;
        }
        return this.hash = toString().hashCode();
    }

    /**
     * Used for the <b>/plot setup</b> command Return null if you do not want to support this feature
     *
     * @return ConfigurationNode[]
     */
    public abstract ConfigurationNode[] getSettingNodes();

    /**
     * Gets the {@link Plot} at a location.
     *
     * @param location the location
     * @return the {@link Plot} or null if none exists
     */
    public @Nullable Plot getPlotAbs(final @NonNull Location location) {
        final PlotId pid =
                this.getPlotManager().getPlotId(location.getX(), location.getY(), location.getZ());
        if (pid == null) {
            return null;
        }
        return getPlotAbs(pid);
    }

    /**
     * Gets the base plot at a location.
     *
     * @param location the location
     * @return base Plot
     */
    public @Nullable Plot getPlot(final @NonNull Location location) {
        final PlotId pid =
                this.getPlotManager().getPlotId(location.getX(), location.getY(), location.getZ());
        if (pid == null) {
            return null;
        }
        return getPlot(pid);
    }

    /**
     * Get the owned base plot at a location.
     *
     * @param location the location
     * @return the base plot or null
     */
    public @Nullable Plot getOwnedPlot(final @NonNull Location location) {
        final PlotId pid =
                this.getPlotManager().getPlotId(location.getX(), location.getY(), location.getZ());
        if (pid == null) {
            return null;
        }
        Plot plot = this.plots.get(pid);
        return plot == null ? null : plot.getBasePlot(false);
    }

    /**
     * Get the owned plot at a location.
     *
     * @param location the location
     * @return Plot or null
     */
    public @Nullable Plot getOwnedPlotAbs(final @NonNull Location location) {
        final PlotId pid =
                this.getPlotManager().getPlotId(location.getX(), location.getY(), location.getZ());
        if (pid == null) {
            return null;
        }
        return this.plots.get(pid);
    }

    /**
     * Get the owned Plot at a PlotId.
     *
     * @param id the {@link PlotId}
     * @return the plot or null
     */
    public @Nullable Plot getOwnedPlotAbs(final @NonNull PlotId id) {
        return this.plots.get(id);
    }

    public @Nullable Plot getOwnedPlot(final @NonNull PlotId id) {
        Plot plot = this.plots.get(id);
        return plot == null ? null : plot.getBasePlot(false);
    }

    public boolean contains(final int x, final int z) {
        return this.getType() != PlotAreaType.PARTIAL || RegionUtil.contains(getRegionAbs(), x, z);
    }

    public boolean contains(final @NonNull PlotId id) {
        return this.min == null || (id.getX() >= this.min.getX() && id.getX() <= this.max.getX() &&
                id.getY() >= this.min.getY() && id.getY() <= this.max.getY());
    }

    public boolean contains(final @NonNull Location location) {
        return StringMan.isEqual(location.getWorldName(), this.getWorldName()) && (
                getRegionAbs() == null || this.region.contains(location.getBlockVector3()));
    }

    /**
     * Get if the {@code PlotArea}'s build range (min build height -> max build height) contains the given y value
     *
     * @param y y height
     * @return if build height contains y
     */
    public boolean buildRangeContainsY(int y) {
        return y >= minBuildHeight && y < maxBuildHeight;
    }

    /**
     * Utility method to check if the player is attempting to place blocks outside the build area, and notify of this if the
     * player does not have permissions.
     *
     * @param player Player to check
     * @param y      y height to check
     * @return true if outside build area with no permissions
     * @since 6.9.1
     */
    public boolean notifyIfOutsideBuildArea(PlotPlayer<?> player, int y) {
        if (!buildRangeContainsY(y) && !player.hasPermission(Permission.PERMISSION_ADMIN_BUILD_HEIGHT_LIMIT)) {
            player.sendMessage(
                    TranslatableCaption.of("height.height_limit"),
                    TagResolver.builder()
                            .tag("minheight", Tag.inserting(Component.text(minBuildHeight)))
                            .tag("maxheight", Tag.inserting(Component.text(maxBuildHeight)))
                            .build()
            );
            // Return true if "failed" as the method will always be inverted otherwise
            return true;
        }
        return false;
    }

    public @NonNull Set<Plot> getPlotsAbs(final UUID uuid) {
        if (uuid == null) {
            return Collections.emptySet();
        }
        final HashSet<Plot> myPlots = new HashSet<>();
        forEachPlotAbs(value -> {
            if (uuid.equals(value.getOwnerAbs())) {
                myPlots.add(value);
            }
        });
        return myPlots;
    }

    public @NonNull Set<Plot> getPlots(final @NonNull UUID uuid) {
        return getPlots().stream().filter(plot -> plot.isBasePlot() && plot.isOwner(uuid))
                .collect(ImmutableSet.toImmutableSet());
    }

    /**
     * A collection of the claimed plots in this {@link PlotArea}.
     *
     * @return a collection of claimed plots
     */
    public Collection<Plot> getPlots() {
        return this.plots.values();
    }

    public int getPlotCount(final @NonNull UUID uuid) {
        if (!Settings.Done.COUNTS_TOWARDS_LIMIT) {
            return (int) getPlotsAbs(uuid).stream().filter(plot -> !DoneFlag.isDone(plot)).count();
        }
        return getPlotsAbs(uuid).size();
    }

    /**
     * Retrieves the plots for the player in this PlotArea.
     *
     * @param player player to get plots of
     * @return set of player's plots
     * @deprecated Use {@link #getPlots(UUID)}
     */
    @Deprecated
    public Set<Plot> getPlots(final @NonNull PlotPlayer<?> player) {
        return getPlots(player.getUUID());
    }

    //todo check if this method is needed in this class

    public boolean hasPlot(final @NonNull UUID uuid) {
        return this.plots.entrySet().stream().anyMatch(entry -> entry.getValue().isOwner(uuid));
    }

    public int getPlotCount(final @Nullable PlotPlayer<?> player) {
        return player != null ? getPlotCount(player.getUUID()) : 0;
    }

    public @Nullable Plot getPlotAbs(final @NonNull PlotId id) {
        Plot plot = getOwnedPlotAbs(id);
        if (plot == null) {
            if (this.min != null && (id.getX() < this.min.getX() || id.getX() > this.max.getX() || id.getY() < this.min.getY()
                    || id.getY() > this.max.getY())) {
                return null;
            }
            return new Plot(this, id);
        }
        return plot;
    }

    public @Nullable Plot getPlot(final @NonNull PlotId id) {
        final Plot plot = getOwnedPlotAbs(id);
        if (plot == null) {
            if (this.min != null && (id.getX() < this.min.getX() || id.getX() > this.max.getX() || id.getY() < this.min.getY()
                    || id.getY() > this.max.getY())) {
                return null;
            }
            return new Plot(this, id);
        }
        return plot.getBasePlot(false);
    }

    /**
     * Retrieves the number of claimed plot in the {@link PlotArea}.
     *
     * @return the number of claimed plots
     */
    public int getPlotCount() {
        return this.plots.size();
    }

    public @Nullable PlotCluster getCluster(final @NonNull Location location) {
        final Plot plot = getPlot(location);
        if (plot == null) {
            return null;
        }
        return this.clusters != null ? this.clusters.get(plot.getId().getX(), plot.getId().getY()) : null;
    }

    public @Nullable PlotCluster getFirstIntersectingCluster(
            final @NonNull PlotId pos1,
            final @NonNull PlotId pos2
    ) {
        if (this.clusters == null) {
            return null;
        }
        for (PlotCluster cluster : this.clusters.getAll()) {
            if (cluster.intersects(pos1, pos2)) {
                return cluster;
            }
        }
        return null;
    }

    @Nullable PlotCluster getCluster(final @NonNull PlotId id) {
        return this.clusters != null ? this.clusters.get(id.getX(), id.getY()) : null;
    }

    /**
     * Session only plot metadata (session is until the server stops).
     * <br>
     * For persistent metadata use the flag system
     *
     * @param key   metadata key
     * @param value metadata value
     */
    public void setMeta(final @NonNull String key, final @Nullable Object value) {
        if (this.meta == null) {
            this.meta = new ConcurrentHashMap<>();
        }
        this.meta.put(key, value);
    }

    public @NonNull <T> T getMeta(final @NonNull String key, final @NonNull T def) {
        final Object v = getMeta(key);
        return v == null ? def : (T) v;
    }

    /**
     * Get the metadata for a key<br>
     * <br>
     * For persistent metadata use the flag system
     *
     * @param key metadata key to get value for
     * @return metadata value
     */
    public @Nullable Object getMeta(final @NonNull String key) {
        if (this.meta != null) {
            return this.meta.get(key);
        }
        return null;
    }

    @SuppressWarnings("unused")
    public @NonNull Set<Plot> getBasePlots() {
        final HashSet<Plot> myPlots = new HashSet<>(getPlots());
        myPlots.removeIf(plot -> !plot.isBasePlot());
        return myPlots;
    }

    private void forEachPlotAbs(Consumer<Plot> run) {
        for (final Entry<PlotId, Plot> entry : this.plots.entrySet()) {
            run.accept(entry.getValue());
        }
    }

    public void forEachBasePlot(Consumer<Plot> run) {
        for (final Plot plot : getPlots()) {
            if (plot.isBasePlot()) {
                run.accept(plot);
            }
        }
    }

    /**
     * Returns an ImmutableMap of PlotId's and Plots in this PlotArea.
     *
     * @return map of PlotId against Plot for all plots in this area
     * @deprecated Poorly implemented. May be removed in future.
     */
    //todo eventually remove
    @Deprecated
    public @NonNull Map<PlotId, Plot> getPlotsRaw() {
        return ImmutableMap.copyOf(plots);
    }

    public @NonNull Set<Entry<PlotId, Plot>> getPlotEntries() {
        return this.plots.entrySet();
    }

    public boolean addPlot(final @NonNull Plot plot) {
        for (final PlotPlayer<?> pp : plot.getPlayersInPlot()) {
            try (final MetaDataAccess<Plot> metaDataAccess = pp.accessTemporaryMetaData(
                    PlayerMetaDataKeys.TEMPORARY_LAST_PLOT)) {
                metaDataAccess.set(plot);
            }
        }
        return this.plots.put(plot.getId(), plot) == null;
    }

    public Plot getNextFreePlot(final PlotPlayer<?> player, @Nullable PlotId start) {
        int plots;
        PlotId center;
        PlotId min = getMin();
        PlotId max = getMax();
        if (getType() == PlotAreaType.PARTIAL) {
            center = PlotId.of(MathMan.average(min.getX(), max.getX()), MathMan.average(min.getY(), max.getY()));
            plots = Math.max(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1) + 1;
            if (start != null) {
                start = PlotId.of(start.getX() - center.getX(), start.getY() - center.getY());
            }
        } else {
            center = PlotId.of(0, 0);
            plots = Integer.MAX_VALUE;
        }
        for (int i = 0; i < plots; i++) {
            if (start == null) {
                start = getMeta("lastPlot", PlotId.of(0, 0));
            } else {
                start = start.getNextId();
            }
            PlotId currentId = PlotId.of(center.getX() + start.getX(), center.getY() + start.getY());
            Plot plot = getPlotAbs(currentId);
            if (plot != null && plot.canClaim(player)) {
                setMeta("lastPlot", start);
                return plot;
            }
        }
        return null;
    }

    public boolean addPlotIfAbsent(final @NonNull Plot plot) {
        if (this.plots.putIfAbsent(plot.getId(), plot) == null) {
            for (PlotPlayer<?> pp : plot.getPlayersInPlot()) {
                try (final MetaDataAccess<Plot> metaDataAccess = pp.accessTemporaryMetaData(
                        PlayerMetaDataKeys.TEMPORARY_LAST_PLOT)) {
                    metaDataAccess.set(plot);
                }
            }
            return true;
        }
        return false;
    }

    public boolean addPlotAbs(final @NonNull Plot plot) {
        return this.plots.put(plot.getId(), plot) == null;
    }

    /**
     * Get the plot border distance for a world<br>
     *
     * @return The border distance or Integer.MAX_VALUE if no border is set
     * @deprecated Use {@link PlotArea#getBorder(boolean)}
     */
    @Deprecated(forRemoval = true, since = "7.2.0")
    public int getBorder() {
        final Integer meta = (Integer) getMeta("worldBorder");
        if (meta != null) {
            int border = meta + 1;
            if (border == 0) {
                return Integer.MAX_VALUE;
            } else {
                return border;
            }
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Get the plot border distance for a world, specifying whether the returned value should include the world.border-size
     * value. This is a player-traversable area, where plots cannot be claimed
     *
     * @param getExtended If the extra border given by world.border-size should be included
     * @return Border distance of Integer.MAX_VALUE if no border is set
     * @since 7.2.0
     */
    public int getBorder(boolean getExtended) {
        final Integer meta = (Integer) getMeta("worldBorder");
        if (meta != null) {
            int border = meta + 1;
            if (border == 0) {
                return Integer.MAX_VALUE;
            } else {
                return getExtended ? border + borderSize : border;
            }
        }
        return Integer.MAX_VALUE;
    }

    /**
     * Setup the plot border for a world (usually done when the world is created).
     */
    public void setupBorder() {
        if (!this.hasWorldBorder()) {
            return;
        }
        final Integer meta = (Integer) getMeta("worldBorder");
        if (meta == null) {
            setMeta("worldBorder", 1);
        }
        for (final Plot plot : getPlots()) {
            plot.updateWorldBorder();
        }
    }

    /**
     * Delete the metadata for a key.
     * - metadata is session only
     * - deleting other plugin's metadata may cause issues
     *
     * @param key Meta data key
     */
    public void deleteMeta(final @NonNull String key) {
        if (this.meta != null) {
            this.meta.remove(key);
        }
    }

    public @Nullable List<Plot> canClaim(
            final @Nullable PlotPlayer<?> player, final @NonNull PlotId pos1,
            final @NonNull PlotId pos2
    ) {
        if (pos1.getX() == pos2.getX() && pos1.getY() == pos2.getY()) {
            if (getOwnedPlot(pos1) != null) {
                return null;
            }
            final Plot plot = getPlotAbs(pos1);
            if (plot == null) {
                return null;
            }
            if (plot.canClaim(player)) {
                return Collections.singletonList(plot);
            } else {
                return null;
            }
        }
        final List<Plot> plots = new LinkedList<>();
        for (int x = pos1.getX(); x <= pos2.getX(); x++) {
            for (int y = pos1.getY(); y <= pos2.getY(); y++) {
                final PlotId id = PlotId.of(x, y);
                final Plot plot = getPlotAbs(id);
                if (plot == null) {
                    return null;
                }
                if (!plot.canClaim(player)) {
                    return null;
                } else {
                    plots.add(plot);
                }
            }
        }
        return plots;
    }

    public boolean removePlot(final @NonNull PlotId id) {
        return this.plots.remove(id) != null;
    }

    /**
     * Merge a list of plots together. This is non-blocking for the world-changes that will be made. To run a task when the
     * world changes are complete, use {@link PlotArea#mergePlots(List, boolean, Runnable)};
     *
     * @param plotIds     List of plot IDs to merge
     * @param removeRoads If the roads between plots should be removed
     * @return if merges were completed successfully.
     */
    public boolean mergePlots(final @NonNull List<PlotId> plotIds, final boolean removeRoads) {
        return mergePlots(plotIds, removeRoads, null);
    }

    /**
     * Merge a list of plots together. This is non-blocking for the world-changes that will be made.
     *
     * @param plotIds     List of plot IDs to merge
     * @param removeRoads If the roads between plots should be removed
     * @param whenDone    Task to run when any merge world changes are complete. Also runs if no changes were made. Does not
     *                    run if there was an error or if too few plots IDs were supplied.
     * @return if merges were completed successfully.
     * @since 6.9.0
     */
    public boolean mergePlots(
            final @NonNull List<PlotId> plotIds, final boolean removeRoads, final @Nullable Runnable whenDone
    ) {
        if (plotIds.size() < 2) {
            return false;
        }

        final PlotId pos1 = plotIds.get(0);
        final PlotId pos2 = plotIds.get(plotIds.size() - 1);
        final PlotManager manager = getPlotManager();

        QueueCoordinator queue = getQueue();
        manager.startPlotMerge(plotIds, queue);
        final Set<UUID> trusted = new HashSet<>();
        final Set<UUID> members = new HashSet<>();
        final Set<UUID> denied = new HashSet<>();
        for (int x = pos1.getX(); x <= pos2.getX(); x++) {
            for (int y = pos1.getY(); y <= pos2.getY(); y++) {
                PlotId id = PlotId.of(x, y);
                Plot plot = getPlotAbs(id);
                trusted.addAll(plot.getTrusted());
                members.addAll(plot.getMembers());
                denied.addAll(plot.getDenied());
                if (removeRoads) {
                    plot.getPlotModificationManager().removeSign();
                }
            }
        }
        members.removeAll(trusted);
        denied.removeAll(trusted);
        denied.removeAll(members);
        for (int x = pos1.getX(); x <= pos2.getX(); x++) {
            for (int y = pos1.getY(); y <= pos2.getY(); y++) {
                final boolean lx = x < pos2.getX();
                final boolean ly = y < pos2.getY();
                final PlotId id = PlotId.of(x, y);
                final Plot plot = getPlotAbs(id);

                plot.setTrusted(trusted);
                plot.setMembers(members);
                plot.setDenied(denied);

                Plot plot2;
                if (lx) {
                    if (ly) {
                        if (!plot.isMerged(Direction.EAST) || !plot.isMerged(Direction.SOUTH)) {
                            if (removeRoads) {
                                plot.getPlotModificationManager().removeRoadSouthEast(queue);
                            }
                        }
                    }
                    if (!plot.isMerged(Direction.EAST)) {
                        plot2 = plot.getRelative(1, 0);
                        plot.mergePlot(plot2, removeRoads, queue);
                    }
                }
                if (ly) {
                    if (!plot.isMerged(Direction.SOUTH)) {
                        plot2 = plot.getRelative(0, 1);
                        plot.mergePlot(plot2, removeRoads, queue);
                    }
                }
            }
        }
        manager.finishPlotMerge(plotIds, queue);
        if (whenDone != null) {
            queue.setCompleteTask(whenDone);
        }
        queue.enqueue();
        return true;
    }

    /**
     * Get a set of owned plots within a selection (chooses the best algorithm based on selection size.
     * i.e. A selection of billions of plots will work fine
     *
     * @param pos1 first corner of selection
     * @param pos2 second corner of selection
     * @return the plots in the selection which are owned
     */
    public Set<Plot> getPlotSelectionOwned(final @NonNull PlotId pos1, final @NonNull PlotId pos2) {
        final int size = (1 + pos2.getX() - pos1.getX()) * (1 + pos2.getY() - pos1.getY());
        final Set<Plot> result = new HashSet<>();
        if (size < 16 || size < getPlotCount()) {
            for (final PlotId pid : Lists.newArrayList((Iterable<? extends PlotId>)
                    PlotId.PlotRangeIterator.range(pos1, pos2))) {
                final Plot plot = getPlotAbs(pid);
                if (plot.hasOwner()) {
                    if (plot.getId().getX() > pos1.getX() || plot.getId().getY() > pos1.getY()
                            || plot.getId().getX() < pos2.getX() || plot.getId().getY() < pos2.getY()) {
                        result.add(plot);
                    }
                }
            }
        } else {
            for (final Plot plot : getPlots()) {
                if (plot.getId().getX() > pos1.getX() || plot.getId().getY() > pos1.getY() || plot.getId().getX() < pos2.getX()
                        || plot.getId().getY() < pos2.getY()) {
                    result.add(plot);
                }
            }
        }
        return result;
    }

    @SuppressWarnings("WeakerAccess")
    public void removeCluster(final @Nullable PlotCluster plotCluster) {
        if (this.clusters == null) {
            throw new IllegalAccessError("Clusters not enabled!");
        }
        this.clusters.remove(plotCluster);
    }

    public void addCluster(final @Nullable PlotCluster plotCluster) {
        if (this.clusters == null) {
            this.clusters = new QuadMap<>(Integer.MAX_VALUE, 0, 0, 62) {
                @Override
                public CuboidRegion getRegion(PlotCluster value) {
                    BlockVector2 pos1 = BlockVector2.at(value.getP1().getX(), value.getP1().getY());
                    BlockVector2 pos2 = BlockVector2.at(value.getP2().getX(), value.getP2().getY());
                    return new CuboidRegion(
                            pos1.toBlockVector3(getMinGenHeight()),
                            pos2.toBlockVector3(getMaxGenHeight())
                    );
                }
            };
        }
        this.clusters.add(plotCluster);
    }

    public @Nullable PlotCluster getCluster(final String string) {
        for (PlotCluster cluster : getClusters()) {
            if (cluster.getName().equalsIgnoreCase(string)) {
                return cluster;
            }
        }
        return null;
    }

    /**
     * Get whether a schematic with that name is available or not.
     * If a schematic is available, it can be used for plot claiming.
     *
     * @param schematic the schematic to look for.
     * @return {@code true} if the schematic exists, {@code false} otherwise.
     */
    public boolean hasSchematic(@NonNull String schematic) {
        return getSchematics().contains(schematic.toLowerCase());
    }

    /**
     * Get whether economy is enabled and used on this plot area or not.
     *
     * @return {@code true} if this plot area uses economy, {@code false} otherwise.
     */
    public boolean useEconomy() {
        return useEconomy;
    }

    /**
     * Get whether the plot area is limited by a world border or not.
     *
     * @return {@code true} if the plot area has a world border, {@code false} otherwise.
     */
    public boolean hasWorldBorder() {
        return worldBorder;
    }

    /**
     * Get the "extra border" size of the plot area.
     *
     * @return Plot area extra border size
     * @since 7.2.0
     */
    public int getBorderSize() {
        return borderSize;
    }

    /**
     * Get whether plot signs are allowed or not.
     *
     * @return {@code true} if plot signs are allowed, {@code false} otherwise.
     */
    public boolean allowSigns() {
        return allowSigns;
    }

    /**
     * Get the plot sign material.
     *
     * @return the sign material.
     */
    public String signMaterial() {
        return signMaterial;
    }

    public String legacySignMaterial() {
        return legacySignMaterial;
    }

    /**
     * Get the value associated with the specified flag. This will look at
     * the default values stored in {@link GlobalFlagContainer}.
     *
     * @param flagClass The flag type (Class)
     * @param <T>       The flag value type
     * @return The flag value
     */
    public <T> T getFlag(final Class<? extends PlotFlag<T, ?>> flagClass) {
        return this.flagContainer.getFlag(flagClass).getValue();
    }

    /**
     * Get the value associated with the specified flag. This will look at
     * the default values stored in {@link GlobalFlagContainer}.
     *
     * @param flag The flag type (Any instance of the flag)
     * @param <V>  The flag type (Any instance of the flag)
     * @param <T>  flag value type
     * @return The flag value
     */
    public <T, V extends PlotFlag<T, ?>> T getFlag(final V flag) {
        final Class<?> flagClass = flag.getClass();
        final PlotFlag<?, ?> flagInstance = this.flagContainer.getFlagErased(flagClass);
        return FlagContainer.<T, V>castUnsafe(flagInstance).getValue();
    }

    /**
     * Get the value associated with the specified road flag. This will look at
     * the default values stored in {@link GlobalFlagContainer}.
     *
     * @param flagClass The flag type (Class)
     * @param <T>       the flag value type
     * @return The flag value
     */
    public <T> T getRoadFlag(final Class<? extends PlotFlag<T, ?>> flagClass) {
        return this.roadFlagContainer.getFlag(flagClass).getValue();
    }

    /**
     * Get the value associated with the specified road flag. This will look at
     * the default values stored in {@link GlobalFlagContainer}.
     *
     * @param flag The flag type (Any instance of the flag)
     * @param <V>  The flag type (Any instance of the flag)
     * @param <T>  flag value type
     * @return The flag value
     */
    public <T, V extends PlotFlag<T, ?>> T getRoadFlag(final V flag) {
        final Class<?> flagClass = flag.getClass();
        final PlotFlag<?, ?> flagInstance = this.roadFlagContainer.getFlagErased(flagClass);
        return FlagContainer.<T, V>castUnsafe(flagInstance).getValue();
    }

    public @NonNull String getWorldName() {
        return this.worldName;
    }

    public String getId() {
        return this.id;
    }

    public @NonNull PlotManager getPlotManager() {
        return this.plotManager;
    }

    public int getWorldHash() {
        return this.worldHash;
    }

    public @NonNull IndependentPlotGenerator getGenerator() {
        return this.generator;
    }

    public boolean isAutoMerge() {
        return this.autoMerge;
    }

    public boolean isMiscSpawnUnowned() {
        return this.miscSpawnUnowned;
    }

    public boolean isMobSpawning() {
        return this.mobSpawning;
    }

    public boolean isMobSpawnerSpawning() {
        return this.mobSpawnerSpawning;
    }

    public BiomeType getPlotBiome() {
        return this.plotBiome;
    }

    public boolean isPlotChat() {
        return this.plotChat;
    }

    public boolean isForcingPlotChat() {
        return this.forcingPlotChat;
    }

    public boolean isSchematicClaimSpecify() {
        return this.schematicClaimSpecify;
    }

    public boolean isSchematicOnClaim() {
        return this.schematicOnClaim;
    }

    public String getSchematicFile() {
        return this.schematicFile;
    }

    public boolean isSpawnEggs() {
        return this.spawnEggs;
    }

    public String getSignMaterial() {
        return this.signMaterial;
    }

    public boolean isSpawnCustom() {
        return this.spawnCustom;
    }

    public boolean isSpawnBreeding() {
        return this.spawnBreeding;
    }

    public PlotAreaType getType() {
        return this.type;
    }

    /**
     * Set the type of this plot area.
     *
     * @param type the type of the plot area.
     */
    public void setType(PlotAreaType type) {
        // TODO this should probably work only if type == null
        this.type = type;
    }

    public PlotAreaTerrainType getTerrain() {
        return this.terrain;
    }

    /**
     * Set the terrain generation type of this plot area.
     *
     * @param terrain the terrain type of the plot area.
     */
    public void setTerrain(PlotAreaTerrainType terrain) {
        this.terrain = terrain;
    }

    public boolean isHomeAllowNonmember() {
        return this.homeAllowNonmember;
    }

    /**
     * Get the location for non-members to be teleported to.
     *
     * @since 6.1.4
     */
    public BlockLoc nonmemberHome() {
        return this.nonmemberHome;
    }

    /**
     * Get the default location for players to be teleported to. May be overridden by {@link #nonmemberHome} if the player is
     * not a member of the plot.
     *
     * @since 6.1.4
     */
    public BlockLoc defaultHome() {
        return this.defaultHome;
    }

    protected void setDefaultHome(BlockLoc defaultHome) {
        this.defaultHome = defaultHome;
    }

    /**
     * Get the maximum height that changes to plot components (wall filling, air, all etc.) may operate to
     *
     * @since 7.3.4
     */
    public int getMaxComponentHeight() {
        return this.maxBuildHeight;
    }

    /**
     * Get the minimum height that changes to plot components (wall filling, air, all etc.) may operate to
     *
     * @since 7.3.4
     */
    public int getMinComponentHeight() {
        return this.minBuildHeight;
    }

    /**
     * Get the maximum height players may build in. Exclusive.
     */
    public int getMaxBuildHeight() {
        return this.maxBuildHeight;
    }

    /**
     * Get the minimum height players may build in. Inclusive.
     */
    public int getMinBuildHeight() {
        return this.minBuildHeight;
    }

    /**
     * Get the min height from which PlotSquared will generate blocks. Inclusive.
     *
     * @since 6.6.0
     */
    public int getMinGenHeight() {
        return this.minGenHeight;
    }

    /**
     * Get the max height to which PlotSquared will generate blocks. Inclusive.
     *
     * @since 6.6.0
     */
    public int getMaxGenHeight() {
        return this.maxGenHeight;
    }

    public GameMode getGameMode() {
        return this.gameMode;
    }

    public Map<String, PlotExpression> getPrices() {
        return this.prices;
    }

    protected List<String> getSchematics() {
        return this.schematics;
    }

    public boolean isRoadFlags() {
        return this.roadFlags;
    }

    public FlagContainer getFlagContainer() {
        return this.flagContainer;
    }

    public FlagContainer getRoadFlagContainer() {
        return this.roadFlagContainer;
    }

    public void setAllowSigns(boolean allowSigns) {
        this.allowSigns = allowSigns;
    }

}
