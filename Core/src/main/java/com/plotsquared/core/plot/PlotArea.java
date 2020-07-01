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
package com.plotsquared.core.plot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.collection.QuadMap;
import com.plotsquared.core.configuration.CaptionUtility;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.ConfigurationNode;
import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.ConfigurationUtil;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.generator.GridPlotWorld;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.location.PlotLoc;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.flag.FlagContainer;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.LocalBlockQueue;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.Expression;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.RegionUtil;
import com.plotsquared.core.util.StringMan;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
public abstract class PlotArea {

    protected final ConcurrentHashMap<PlotId, Plot> plots = new ConcurrentHashMap<>();
    @Getter @NotNull private final String worldName;
    @Getter private final String id;
    @Getter @NotNull private final PlotManager plotManager;
    @Getter private final int worldHash;
    private final PlotId min;
    private final PlotId max;
    @Getter @NotNull private final IndependentPlotGenerator generator;
    @Getter private int maxPlotMembers = 128;
    @Getter private boolean autoMerge = false;
    @Setter private boolean allowSigns = true;
    @Getter private boolean miscSpawnUnowned = false;
    @Getter private boolean mobSpawning = false;
    @Getter private boolean mobSpawnerSpawning = false;
    @Getter private BiomeType plotBiome = BiomeTypes.FOREST;
    @Getter private boolean plotChat = true;
    @Getter private boolean forcingPlotChat = false;
    @Getter private boolean schematicClaimSpecify = false;
    @Getter private boolean schematicOnClaim = false;
    @Getter private String schematicFile = "null";
    @Getter private boolean spawnEggs = false;
    @Getter private boolean spawnCustom = true;
    @Getter private boolean spawnBreeding = false;
    @Getter private PlotAreaType type = PlotAreaType.NORMAL;
    @Getter private PlotAreaTerrainType terrain = PlotAreaTerrainType.NONE;
    @Getter private boolean homeAllowNonmember = false;
    @Getter private PlotLoc nonmemberHome;
    @Getter @Setter(AccessLevel.PROTECTED) private PlotLoc defaultHome;
    @Getter private int maxBuildHeight = 256;
    @Getter private int minBuildHeight = 1;
    @Getter private GameMode gameMode = GameModes.CREATIVE;
    @Getter private Map<String, Expression<Double>> prices = new HashMap<>();
    @Getter(AccessLevel.PROTECTED) private List<String> schematics = new ArrayList<>();
    @Getter private boolean roadRespectingGlobalFlags = false;
    private boolean worldBorder = false;
    private boolean useEconomy = false;
    private int hash;
    private CuboidRegion region;
    private ConcurrentHashMap<String, Object> meta;
    private QuadMap<PlotCluster> clusters;
    /**
     * Area flag container
     */
    @Getter private FlagContainer flagContainer =
        new FlagContainer(GlobalFlagContainer.getInstance());

    public PlotArea(@NotNull final String worldName, @Nullable final String id,
        @NotNull IndependentPlotGenerator generator, @Nullable final PlotId min,
        @Nullable final PlotId max) {
        this.worldName = worldName;
        this.id = id;
        this.plotManager = createManager();
        this.generator = generator;
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
    }

    @NotNull protected abstract PlotManager createManager();

    public LocalBlockQueue getQueue(final boolean autoQueue) {
        return GlobalBlockQueue.IMP.getNewQueue(worldName, autoQueue);
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
                BlockVector3.at(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
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
    public PlotId getMin() {
        return this.min == null ? new PlotId(Integer.MIN_VALUE, Integer.MIN_VALUE) : this.min;
    }

    /**
     * Returns the max PlotId.
     *
     * @return the maximum value for a {@link PlotId}
     */
    public PlotId getMax() {
        return this.max == null ? new PlotId(Integer.MAX_VALUE, Integer.MAX_VALUE) : this.max;
    }

    @Override public boolean equals(Object obj) {
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
     * Check if a PlotArea is compatible (move/copy etc).
     *
     * @param plotArea the {@code PlotArea} to compare
     * @return true if both areas are compatible
     */
    public boolean isCompatible(PlotArea plotArea) {
        ConfigurationSection section = PlotSquared.get().worlds.getConfigurationSection("worlds");
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
            this.terrain = MainUtil.getTerrain(config);
            this.type = MainUtil.getType(config);
        }
        this.mobSpawning = config.getBoolean("natural_mob_spawning");
        this.miscSpawnUnowned = config.getBoolean("misc_spawn_unowned");
        this.mobSpawnerSpawning = config.getBoolean("mob_spawner_spawning");
        this.autoMerge = config.getBoolean("plot.auto_merge");
        this.maxPlotMembers = config.getInt("limits.max-members");
        this.allowSigns = config.getBoolean("plot.create_signs");
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
        this.useEconomy = config.getBoolean("economy.use") && EconHandler.getEconHandler() != null;
        ConfigurationSection priceSection = config.getConfigurationSection("economy.prices");
        if (this.useEconomy) {
            this.prices = new HashMap<>();
            for (String key : priceSection.getKeys(false)) {
                this.prices.put(key, Expression.doubleExpression(priceSection.getString(key)));
            }
        }
        this.plotChat = config.getBoolean("chat.enabled");
        this.forcingPlotChat = config.getBoolean("chat.forced");
        this.worldBorder = config.getBoolean("world.border");
        this.maxBuildHeight = config.getInt("world.max_height");
        this.minBuildHeight = config.getInt("world.min_height");

        switch (config.getString("world.gamemode").toLowerCase()) {
            case "creative":
            case "c":
            case "1":
                this.gameMode = GameModes.CREATIVE;
                break;
            case "adventure":
            case "a":
            case "2":
                this.gameMode = GameModes.ADVENTURE;
                break;
            case "spectator":
            case "3":
                this.gameMode = GameModes.SPECTATOR;
                break;
            case "survival":
            case "s":
            case "0":
            default:
                this.gameMode = GameModes.SURVIVAL;
                break;
        }

        String homeNonMembers = config.getString("home.nonmembers");
        String homeDefault = config.getString("home.default");
        this.defaultHome = PlotLoc.fromString(homeDefault);
        this.homeAllowNonmember = homeNonMembers.equalsIgnoreCase(homeDefault);
        if (this.homeAllowNonmember) {
            this.nonmemberHome = defaultHome;
        } else {
            this.nonmemberHome = PlotLoc.fromString(homeNonMembers);
        }

        if ("side".equalsIgnoreCase(homeDefault)) {
            this.defaultHome = null;
        } else if (StringMan.isEqualIgnoreCaseToAny(homeDefault, "center", "middle")) {
            this.defaultHome = new PlotLoc(Integer.MAX_VALUE, Integer.MAX_VALUE);
        } else {
            try {
                /*String[] split = homeDefault.split(",");
                this.DEFAULT_HOME =
                    new PlotLoc(Integer.parseInt(split[0]), Integer.parseInt(split[1]));*/
                this.defaultHome = PlotLoc.fromString(homeDefault);
            } catch (NumberFormatException ignored) {
                this.defaultHome = null;
            }
        }

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
        this.getFlagContainer().addAll(parseFlags(flags));

        StringBuilder flagBuilder = new StringBuilder();
        Collection<PlotFlag<?, ?>> flagCollection = this.getFlagContainer().getFlagMap().values();
        if (flagCollection.isEmpty()) {
            flagBuilder.append(Captions.NONE.getTranslated());
        } else {
            String prefix = " ";
            for (final PlotFlag<?, ?> flag : flagCollection) {
                Object value = flag.toString();
                flagBuilder.append(prefix).append(CaptionUtility
                    .format(null, Captions.PLOT_FLAG_LIST.getTranslated(), flag.getName(),
                        CaptionUtility.formatRaw(null, value.toString(), "")));
                prefix = ", ";
            }
        }

        PlotSquared.log(Captions.PREFIX + "&3 - default flags: &7" + flagBuilder.toString());
        this.spawnEggs = config.getBoolean("event.spawn.egg");
        this.spawnCustom = config.getBoolean("event.spawn.custom");
        this.spawnBreeding = config.getBoolean("event.spawn.breeding");
        this.roadRespectingGlobalFlags = config.getBoolean("road.respect-global-flags");
        loadConfiguration(config);
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
        options.put("limits.max-members", this.getMaxPlotMembers());
        options.put("home.default", "side");
        String position = config.getString("home.nonmembers",
            config.getBoolean("home.allow-nonmembers", false) ?
                config.getString("home.default", "side") :
                "side");
        options.put("home.nonmembers", position);
        options.put("world.max_height", this.getMaxBuildHeight());
        options.put("world.min_height", this.getMinBuildHeight());
        options.put("world.gamemode", this.getGameMode().getName().toLowerCase());
        options.put("road.respect-global-flags", this.isRoadRespectingGlobalFlags());

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
            config.set("flags.use",
                "63,64,68,69,71,77,96,143,167,193,194,195,196,197,77,143,69,70,72,147,148,107,183,184,185,186,187,132");
        }
    }

    @NotNull @Override public String toString() {
        if (this.getId() == null) {
            return this.getWorldName();
        } else {
            return this.getWorldName() + ";" + this.getId();
        }
    }

    @Override public int hashCode() {
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
     * Gets the {@code Plot} at a location.
     *
     * @param location the location
     * @return the {@code Plot} or null if none exists
     */
    @Nullable public Plot getPlotAbs(@NotNull final Location location) {
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
    @Nullable public Plot getPlot(@NotNull final Location location) {
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
    @Nullable public Plot getOwnedPlot(@NotNull final Location location) {
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
    @Nullable public Plot getOwnedPlotAbs(@NotNull final Location location) {
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
     * @param id the {@code PlotId}
     * @return the plot or null
     */
    @Nullable public Plot getOwnedPlotAbs(@NotNull final PlotId id) {
        return this.plots.get(id);
    }

    @Nullable public Plot getOwnedPlot(@NotNull final PlotId id) {
        Plot plot = this.plots.get(id);
        return plot == null ? null : plot.getBasePlot(false);
    }

    public boolean contains(final int x, final int z) {
        return this.getType() != PlotAreaType.PARTIAL || RegionUtil.contains(getRegionAbs(), x, z);
    }

    public boolean contains(@NotNull final PlotId id) {
        return this.min == null || (id.x >= this.min.x && id.x <= this.max.x && id.y >= this.min.y
            && id.y <= this.max.y);
    }

    public boolean contains(@NotNull final Location location) {
        return StringMan.isEqual(location.getWorld(), this.getWorldName()) && (
            getRegionAbs() == null || this.region.contains(location.getBlockVector3()));
    }

    @NotNull public Set<Plot> getPlotsAbs(final UUID uuid) {
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

    @NotNull public Set<Plot> getPlots(@NotNull final UUID uuid) {
        return getPlots().stream().filter(plot -> plot.isBasePlot() && plot.isOwner(uuid))
            .collect(ImmutableSet.toImmutableSet());
    }

    /**
     * A collection of the claimed plots in this {@code PlotArea}.
     *
     * @return a collection of claimed plots
     */
    public Collection<Plot> getPlots() {
        return this.plots.values();
    }

    public int getPlotCount(@NotNull final UUID uuid) {
        if (!Settings.Done.COUNTS_TOWARDS_LIMIT) {
            return (int) getPlotsAbs(uuid).stream().filter(plot -> !DoneFlag.isDone(plot)).count();
        }
        return getPlotsAbs(uuid).size();
    }

    /**
     * Retrieves the plots for the player in this PlotArea.
     *
     * @deprecated Use {@link #getPlots(UUID)}
     */
    @Deprecated public Set<Plot> getPlots(@NotNull final PlotPlayer player) {
        return getPlots(player.getUUID());
    }

    public boolean hasPlot(@NotNull final UUID uuid) {
        return this.plots.entrySet().stream().anyMatch(entry -> entry.getValue().isOwner(uuid));
    }

    //todo check if this method is needed in this class

    public int getPlotCount(@Nullable final PlotPlayer player) {
        return player != null ? getPlotCount(player.getUUID()) : 0;
    }

    @Nullable public Plot getPlotAbs(@NotNull final PlotId id) {
        Plot plot = getOwnedPlotAbs(id);
        if (plot == null) {
            if (this.min != null && (id.x < this.min.x || id.x > this.max.x || id.y < this.min.y
                || id.y > this.max.y)) {
                return null;
            }
            return new Plot(this, id);
        }
        return plot;
    }

    @Nullable public Plot getPlot(@NotNull final PlotId id) {
        final Plot plot = getOwnedPlotAbs(id);
        if (plot == null) {
            if (this.min != null && (id.x < this.min.x || id.x > this.max.x || id.y < this.min.y
                || id.y > this.max.y)) {
                return null;
            }
            return new Plot(this, id);
        }
        return plot.getBasePlot(false);
    }

    /**
     * Retrieves the number of claimed plot in the {@code PlotArea}.
     *
     * @return the number of claimed plots
     */
    public int getPlotCount() {
        return this.plots.size();
    }

    @Nullable public PlotCluster getCluster(@NotNull final Location location) {
        final Plot plot = getPlot(location);
        if (plot == null) {
            return null;
        }
        return this.clusters != null ? this.clusters.get(plot.getId().x, plot.getId().y) : null;
    }

    @Nullable
    public PlotCluster getFirstIntersectingCluster(@NotNull final PlotId pos1,
        @NotNull final PlotId pos2) {
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

    @Nullable PlotCluster getCluster(@NotNull final PlotId id) {
        return this.clusters != null ? this.clusters.get(id.x, id.y) : null;
    }

    /**
     * Session only plot metadata (session is until the server stops).
     * <br>
     * For persistent metadata use the flag system
     */
    public void setMeta(@NotNull final String key, @Nullable final Object value) {
        if (this.meta == null) {
            this.meta = new ConcurrentHashMap<>();
        }
        this.meta.put(key, value);
    }

    @NotNull public <T> T getMeta(@NotNull final String key, @NotNull final T def) {
        final Object v = getMeta(key);
        return v == null ? def : (T) v;
    }

    /**
     * Get the metadata for a key<br>
     * <br>
     * For persistent metadata use the flag system
     */
    @Nullable public Object getMeta(@NotNull final String key) {
        if (this.meta != null) {
            return this.meta.get(key);
        }
        return null;
    }

    @SuppressWarnings("unused") @NotNull public Set<Plot> getBasePlots() {
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
     */
    public Map<PlotId, Plot> getPlotsMap() {
        return ImmutableMap.copyOf(plots);
    }

    /**
     * Returns an ImmutableMap of PlotId's and Plots in this PlotArea.
     *
     * @deprecated Use {@link #getPlotsMap()}
     */
    //todo eventually remove
    @Deprecated @NotNull public Map<PlotId, Plot> getPlotsRaw() {
        return ImmutableMap.copyOf(plots);
    }

    @NotNull public Set<Entry<PlotId, Plot>> getPlotEntries() {
        return this.plots.entrySet();
    }

    public boolean addPlot(@NotNull final Plot plot) {
        for (PlotPlayer pp : plot.getPlayersInPlot()) {
            pp.setMeta(PlotPlayer.META_LAST_PLOT, plot);
        }
        return this.plots.put(plot.getId(), plot) == null;
    }

    public Plot getNextFreePlot(final PlotPlayer player, @Nullable PlotId start) {
        int plots;
        PlotId center;
        PlotId min = getMin();
        PlotId max = getMax();
        if (getType() == PlotAreaType.PARTIAL) {
            center = new PlotId(MathMan.average(min.x, max.x), MathMan.average(min.y, max.y));
            plots = Math.max(max.x - min.x + 1, max.y - min.y + 1) + 1;
            if (start != null) {
                start = new PlotId(start.x - center.x, start.y - center.y);
            }
        } else {
            center = new PlotId(0, 0);
            plots = Integer.MAX_VALUE;
        }
        for (int i = 0; i < plots; i++) {
            if (start == null) {
                start = getMeta("lastPlot", new PlotId(0, 0));
            } else {
                start = start.getNextId(1);
            }
            PlotId currentId = new PlotId(center.x + start.x, center.y + start.y);
            Plot plot = getPlotAbs(currentId);
            if (plot != null && plot.canClaim(player)) {
                setMeta("lastPlot", start);
                return plot;
            }
        }
        return null;
    }

    public boolean addPlotIfAbsent(@NotNull final Plot plot) {
        if (this.plots.putIfAbsent(plot.getId(), plot) == null) {
            for (PlotPlayer pp : plot.getPlayersInPlot()) {
                pp.setMeta(PlotPlayer.META_LAST_PLOT, plot);
            }
            return true;
        }
        return false;
    }

    public boolean addPlotAbs(@NotNull final Plot plot) {
        return this.plots.put(plot.getId(), plot) == null;
    }

    /**
     * Get the plot border distance for a world<br>
     *
     * @return The border distance or Integer.MAX_VALUE if no border is set
     */
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
    public void deleteMeta(@NotNull final String key) {
        if (this.meta != null) {
            this.meta.remove(key);
        }
    }

    public boolean canClaim(@Nullable final PlotPlayer player, @NotNull final PlotId pos1,
        @NotNull final PlotId pos2) {
        if (pos1.x == pos2.x && pos1.y == pos2.y) {
            if (getOwnedPlot(pos1) != null) {
                return false;
            }
            final Plot plot = getPlotAbs(pos1);
            if (plot == null) {
                return false;
            }
            return plot.canClaim(player);
        }
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                final PlotId id = new PlotId(x, y);
                final Plot plot = getPlotAbs(id);
                if (plot == null) {
                    return false;
                }
                if (!plot.canClaim(player)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean removePlot(@NotNull final PlotId id) {
        return this.plots.remove(id) != null;
    }

    public boolean mergePlots(@NotNull final List<PlotId> plotIds, final boolean removeRoads) {
        if (plotIds.size() < 2) {
            return false;
        }

        final PlotId pos1 = plotIds.get(0);
        final PlotId pos2 = plotIds.get(plotIds.size() - 1);
        final PlotManager manager = getPlotManager();

        manager.startPlotMerge(plotIds);
        final Set<UUID> trusted = new HashSet<>();
        final Set<UUID> members = new HashSet<>();
        final Set<UUID> denied = new HashSet<>();
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                PlotId id = new PlotId(x, y);
                Plot plot = getPlotAbs(id);
                trusted.addAll(plot.getTrusted());
                members.addAll(plot.getMembers());
                denied.addAll(plot.getDenied());
                if (removeRoads) {
                    plot.removeSign();
                }
            }
        }
        members.removeAll(trusted);
        denied.removeAll(trusted);
        denied.removeAll(members);
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                final boolean lx = x < pos2.x;
                final boolean ly = y < pos2.y;
                final PlotId id = new PlotId(x, y);
                final Plot plot = getPlotAbs(id);

                plot.setTrusted(trusted);
                plot.setMembers(members);
                plot.setDenied(denied);

                Plot plot2;
                if (lx) {
                    if (ly) {
                        if (!plot.getMerged(Direction.EAST) || !plot.getMerged(Direction.SOUTH)) {
                            if (removeRoads) {
                                plot.removeRoadSouthEast();
                            }
                        }
                    }
                    if (!plot.getMerged(Direction.EAST)) {
                        plot2 = plot.getRelative(1, 0);
                        plot.mergePlot(plot2, removeRoads);
                    }
                }
                if (ly) {
                    if (!plot.getMerged(Direction.SOUTH)) {
                        plot2 = plot.getRelative(0, 1);
                        plot.mergePlot(plot2, removeRoads);
                    }
                }
            }
        }
        manager.finishPlotMerge(plotIds);
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
    public Set<Plot> getPlotSelectionOwned(@NotNull final PlotId pos1, @NotNull final PlotId pos2) {
        final int size = (1 + pos2.x - pos1.x) * (1 + pos2.y - pos1.y);
        final Set<Plot> result = new HashSet<>();
        if (size < 16 || size < getPlotCount()) {
            for (final PlotId pid : MainUtil.getPlotSelectionIds(pos1, pos2)) {
                final Plot plot = getPlotAbs(pid);
                if (plot.hasOwner()) {
                    if (plot.getId().x > pos1.x || plot.getId().y > pos1.y
                        || plot.getId().x < pos2.x || plot.getId().y < pos2.y) {
                        result.add(plot);
                    }
                }
            }
        } else {
            for (final Plot plot : getPlots()) {
                if (plot.getId().x > pos1.x || plot.getId().y > pos1.y || plot.getId().x < pos2.x
                    || plot.getId().y < pos2.y) {
                    result.add(plot);
                }
            }
        }
        return result;
    }

    @SuppressWarnings("WeakerAccess")
    public void removeCluster(@Nullable final PlotCluster plotCluster) {
        if (this.clusters == null) {
            throw new IllegalAccessError("Clusters not enabled!");
        }
        this.clusters.remove(plotCluster);
    }

    public void addCluster(@Nullable final PlotCluster plotCluster) {
        if (this.clusters == null) {
            this.clusters = new QuadMap<PlotCluster>(Integer.MAX_VALUE, 0, 0, 62) {
                @Override public CuboidRegion getRegion(PlotCluster value) {
                    BlockVector2 pos1 = BlockVector2.at(value.getP1().x, value.getP1().y);
                    BlockVector2 pos2 = BlockVector2.at(value.getP2().x, value.getP2().y);
                    return new CuboidRegion(pos1.toBlockVector3(),
                        pos2.toBlockVector3(Plot.MAX_HEIGHT - 1));
                }
            };
        }
        this.clusters.add(plotCluster);
    }

    @Nullable public PlotCluster getCluster(final String string) {
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
     * @return true if the schematic exists, false otherwise.
     */
    public boolean hasSchematic(@NotNull String schematic) {
        return getSchematics().contains(schematic.toLowerCase());
    }

    /**
     * Get whether economy is enabled and used on this plot area or not.
     *
     * @return true if this plot area uses economy, false otherwise.
     */
    public boolean useEconomy() {
        return useEconomy;
    }

    /**
     * Get whether the plot area is limited by a world border or not.
     *
     * @return true if the plot area has a world border, false otherwise.
     */
    public boolean hasWorldBorder() {
        return worldBorder;
    }

    /**
     * Get whether plot signs are allowed or not.
     *
     * @return true if plot signs are allow, false otherwise.
     */
    public boolean allowSigns() {
        return allowSigns && (this.plots.size() > 1) /* Do not generate signs for single plots */;
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

    /**
     * Set the terrain generation type of this plot area.
     *
     * @param terrain the terrain type of the plot area.
     */
    public void setTerrain(PlotAreaTerrainType terrain) {
        this.terrain = terrain;
    }

    private static Collection<PlotFlag<?, ?>> parseFlags(List<String> flagStrings) {
        final Collection<PlotFlag<?, ?>> flags = new ArrayList<>();
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
                    flags.add(flagInstance.parse(split[1]));
                } catch (final FlagParseException e) {
                    PlotSquared.log(Captions.PREFIX.getTranslated() + String.format(
                        "§cFailed to parse default flag with key §6'%s'§c and value: §6'%s'§c."
                            + " Reason: %s. This flag will not be added as a default flag.",
                        e.getFlag().getName(), e.getValue(), e.getErrorMessage()));
                }
            }
        }
        return flags;
    }

    /**
     * Get the value associated with the specified flag. This will look at
     * the default values stored in {@link GlobalFlagContainer}.
     *
     * @param flagClass The flag type (Class)
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
     * @return The flag value
     */
    public <T, V extends PlotFlag<T, ?>> T getFlag(final V flag) {
        final Class<?> flagClass = flag.getClass();
        final PlotFlag<?, ?> flagInstance = this.flagContainer.getFlagErased(flagClass);
        return FlagContainer.<T, V>castUnsafe(flagInstance).getValue();
    }
}
