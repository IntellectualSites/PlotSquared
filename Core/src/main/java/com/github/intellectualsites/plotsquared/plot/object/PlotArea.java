package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Configuration;
import com.github.intellectualsites.plotsquared.plot.config.ConfigurationNode;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flags.FlagContainer;
import com.github.intellectualsites.plotsquared.plot.flags.FlagParseException;
import com.github.intellectualsites.plotsquared.plot.flags.GlobalFlagContainer;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DoneFlag;
import com.github.intellectualsites.plotsquared.plot.generator.GridPlotWorld;
import com.github.intellectualsites.plotsquared.plot.generator.IndependentPlotGenerator;
import com.github.intellectualsites.plotsquared.plot.util.EconHandler;
import com.github.intellectualsites.plotsquared.plot.util.EventUtil;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.area.QuadMap;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.world.RegionUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import lombok.Getter;
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

    public final String worldname;
    public final String id;
    @NotNull public final PlotManager manager;
    public final int worldhash;
    protected final ConcurrentHashMap<PlotId, Plot> plots = new ConcurrentHashMap<>();
    private final PlotId min;
    private final PlotId max;
    @NotNull private final IndependentPlotGenerator generator;
    public int MAX_PLOT_MEMBERS = 128;
    public boolean AUTO_MERGE = false;
    public boolean ALLOW_SIGNS = true;
    public boolean MISC_SPAWN_UNOWNED = false;
    public boolean MOB_SPAWNING = false;
    public boolean MOB_SPAWNER_SPAWNING = false;
    public BiomeType PLOT_BIOME = BiomeTypes.FOREST;
    public boolean PLOT_CHAT = false;
    public boolean SCHEMATIC_CLAIM_SPECIFY = false;
    public boolean SCHEMATIC_ON_CLAIM = false;
    public String SCHEMATIC_FILE = "null";
    public List<String> SCHEMATICS = null;
    public boolean USE_ECONOMY = false;
    public Map<String, Expression<Double>> PRICES = new HashMap<>();
    public boolean SPAWN_EGGS = false;
    public boolean SPAWN_CUSTOM = true;
    public boolean SPAWN_BREEDING = false;
    public boolean WORLD_BORDER = false;
    public int TYPE = 0;
    public int TERRAIN = 0;
    public boolean HOME_ALLOW_NONMEMBER = false;
    public PlotLoc NONMEMBER_HOME;
    public PlotLoc DEFAULT_HOME;
    public int MAX_BUILD_HEIGHT = 256;
    public int MIN_BUILD_HEIGHT = 1;
    public GameMode GAMEMODE = GameModes.CREATIVE;
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
        this.worldname = worldName;
        this.id = id;
        this.manager = createManager();
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
        this.worldhash = worldName.hashCode();
    }

    @NotNull protected abstract PlotManager createManager();

    public LocalBlockQueue getQueue(final boolean autoQueue) {
        return GlobalBlockQueue.IMP.getNewQueue(worldname, autoQueue);
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

    /**
     * Get the implementation independent generator for this area.
     *
     * @return the {@link IndependentPlotGenerator}
     */
    @NotNull public IndependentPlotGenerator getGenerator() {
        return this.generator;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PlotArea plotarea = (PlotArea) obj;
        return this.worldhash == plotarea.worldhash && this.worldname.equals(plotarea.worldname)
            && StringMan.isEqual(this.id, plotarea.id);
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
            Object constant = section.get(plotArea.worldname + '.' + setting.getConstant());
            if (constant == null || !constant
                .equals(section.get(this.worldname + '.' + setting.getConstant()))) {
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
            this.TERRAIN = config.getInt("generator.terrain");
            this.TYPE = config.getInt("generator.type");
        }
        this.MOB_SPAWNING = config.getBoolean("natural_mob_spawning");
        this.MISC_SPAWN_UNOWNED = config.getBoolean("misc_spawn_unowned");
        this.MOB_SPAWNER_SPAWNING = config.getBoolean("mob_spawner_spawning");
        this.AUTO_MERGE = config.getBoolean("plot.auto_merge");
        this.MAX_PLOT_MEMBERS = config.getInt("limits.max-members");
        this.ALLOW_SIGNS = config.getBoolean("plot.create_signs");
        this.PLOT_BIOME = Configuration.BIOME.parseString(config.getString("plot.biome"));
        this.SCHEMATIC_ON_CLAIM = config.getBoolean("schematic.on_claim");
        this.SCHEMATIC_FILE = config.getString("schematic.file");
        this.SCHEMATIC_CLAIM_SPECIFY = config.getBoolean("schematic.specify_on_claim");
        this.SCHEMATICS = new ArrayList<>(config.getStringList("schematic.schematics"));
        this.SCHEMATICS.replaceAll(String::toLowerCase);
        this.USE_ECONOMY = config.getBoolean("economy.use") && EconHandler.getEconHandler() != null;
        ConfigurationSection priceSection = config.getConfigurationSection("economy.prices");
        if (this.USE_ECONOMY) {
            this.PRICES = new HashMap<>();
            for (String key : priceSection.getKeys(false)) {
                this.PRICES.put(key, Expression.doubleExpression(priceSection.getString(key)));
            }
        }
        this.PLOT_CHAT = config.getBoolean("chat.enabled");
        this.WORLD_BORDER = config.getBoolean("world.border");
        this.MAX_BUILD_HEIGHT = config.getInt("world.max_height");
        this.MIN_BUILD_HEIGHT = config.getInt("world.min_height");

        switch (config.getString("world.gamemode").toLowerCase()) {
            case "creative":
            case "c":
            case "1":
                this.GAMEMODE = GameModes.CREATIVE;
                break;
            case "adventure":
            case "a":
            case "2":
                this.GAMEMODE = GameModes.ADVENTURE;
                break;
            case "spectator":
            case "3":
                this.GAMEMODE = GameModes.SPECTATOR;
                break;
            case "survival":
            case "s":
            case "0":
            default:
                this.GAMEMODE = GameModes.SURVIVAL;
                break;
        }

        String homeNonMembers = config.getString("home.nonmembers");
        String homeDefault = config.getString("home.default");
        this.DEFAULT_HOME = PlotLoc.fromString(homeDefault);
        this.HOME_ALLOW_NONMEMBER = homeNonMembers.equalsIgnoreCase(homeDefault);
        if (this.HOME_ALLOW_NONMEMBER) {
            this.NONMEMBER_HOME = DEFAULT_HOME;
        } else {
            this.NONMEMBER_HOME = PlotLoc.fromString(homeNonMembers);
        }

        if ("side".equalsIgnoreCase(homeDefault)) {
            this.DEFAULT_HOME = null;
        } else if (StringMan.isEqualIgnoreCaseToAny(homeDefault, "center", "middle")) {
            this.DEFAULT_HOME = new PlotLoc(Integer.MAX_VALUE, Integer.MAX_VALUE);
        } else {
            try {
                /*String[] split = homeDefault.split(",");
                this.DEFAULT_HOME =
                    new PlotLoc(Integer.parseInt(split[0]), Integer.parseInt(split[1]));*/
                this.DEFAULT_HOME = PlotLoc.fromString(homeDefault);
            } catch (NumberFormatException ignored) {
                this.DEFAULT_HOME = null;
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
        try {
            this.getFlagContainer().addAll(parseFlags(flags));
        } catch (FlagParseException e) {
            e.printStackTrace();
            PlotSquared.debug("&cInvalid default flags for " + this.worldname + ": " + StringMan
                .join(flags, ","));
        }
        this.SPAWN_EGGS = config.getBoolean("event.spawn.egg");
        this.SPAWN_CUSTOM = config.getBoolean("event.spawn.custom");
        this.SPAWN_BREEDING = config.getBoolean("event.spawn.breeding");
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
        options.put("natural_mob_spawning", this.MOB_SPAWNING);
        options.put("misc_spawn_unowned", this.MISC_SPAWN_UNOWNED);
        options.put("mob_spawner_spawning", this.MOB_SPAWNER_SPAWNING);
        options.put("plot.auto_merge", this.AUTO_MERGE);
        options.put("plot.create_signs", this.ALLOW_SIGNS);
        options.put("plot.biome", "FOREST");
        options.put("schematic.on_claim", this.SCHEMATIC_ON_CLAIM);
        options.put("schematic.file", this.SCHEMATIC_FILE);
        options.put("schematic.specify_on_claim", this.SCHEMATIC_CLAIM_SPECIFY);
        options.put("schematic.schematics", this.SCHEMATICS);
        options.put("economy.use", this.USE_ECONOMY);
        options.put("economy.prices.claim", 100);
        options.put("economy.prices.merge", 100);
        options.put("economy.prices.sell", 100);
        options.put("chat.enabled", this.PLOT_CHAT);
        options.put("flags.default", null);
        options.put("event.spawn.egg", this.SPAWN_EGGS);
        options.put("event.spawn.custom", this.SPAWN_CUSTOM);
        options.put("event.spawn.breeding", this.SPAWN_BREEDING);
        options.put("world.border", this.WORLD_BORDER);
        options.put("limits.max-members", this.MAX_PLOT_MEMBERS);
        options.put("home.default", "side");
        String position = config.getString("home.nonmembers",
            config.getBoolean("home.allow-nonmembers", false) ?
                config.getString("home.default", "side") :
                "side");
        options.put("home.nonmembers", position);
        options.put("world.max_height", this.MAX_BUILD_HEIGHT);
        options.put("world.min_height", this.MIN_BUILD_HEIGHT);
        options.put("world.gamemode", this.GAMEMODE.getName().toLowerCase());

        if (this.TYPE != 0) {
            options.put("generator.terrain", this.TERRAIN);
            options.put("generator.type", this.TYPE);
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
        if (this.id == null) {
            return this.worldname;
        } else {
            return this.worldname + ";" + this.id;
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
            this.manager.getPlotId(location.getX(), location.getY(), location.getZ());
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
            this.manager.getPlotId(location.getX(), location.getY(), location.getZ());
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
            this.manager.getPlotId(location.getX(), location.getY(), location.getZ());
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
            this.manager.getPlotId(location.getX(), location.getY(), location.getZ());
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
        return this.TYPE != 2 || RegionUtil.contains(getRegionAbs(), x, z);
    }

    public boolean contains(@NotNull final PlotId id) {
        return this.min == null || (id.x >= this.min.x && id.x <= this.max.x && id.y >= this.min.y
            && id.y <= this.max.y);
    }

    public boolean contains(@NotNull final Location location) {
        return StringMan.isEqual(location.getWorld(), this.worldname) && (getRegionAbs() == null
            || this.region.contains(location.getBlockVector3()));
    }

    @NotNull Set<Plot> getPlotsAbs(final UUID uuid) {
        if (uuid == null) {
            return Collections.emptySet();
        }
        final HashSet<Plot> myPlots = new HashSet<>();
        forEachPlotAbs(value -> {
            if (uuid.equals(value.owner)) {
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

    @Nullable public PlotCluster getFirstIntersectingCluster(@NotNull final PlotId pos1,
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

    @NotNull public PlotManager getPlotManager() {
        return this.manager;
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
        if (TYPE == 2) {
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
        if (!this.WORLD_BORDER) {
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

        final boolean result = EventUtil.manager.callAutoMerge(getPlotAbs(pos1), plotIds);
        if (!result) {
            return false;
        }

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

    private static Collection<PlotFlag<?, ?>> parseFlags(List<String> flagStrings) throws FlagParseException {
        final Collection<PlotFlag<?, ?>> flags = new ArrayList<>();
        for (final String key : flagStrings) {
            final String[] split;
            if (key.contains(";")) {
                split = key.split(";");
            } else {
                split = key.split(":");
            }
            final PlotFlag<?, ?> flagInstance = GlobalFlagContainer.getInstance().getFlagFromString(split[0]);
            if (flagInstance != null) {
                flags.add(flagInstance.parse(split[1]));
            }
        }
        return flags;
    }

}
