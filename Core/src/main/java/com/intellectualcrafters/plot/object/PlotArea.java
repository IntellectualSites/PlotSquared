package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.generator.GridPlotWorld;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlotGameMode;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.intellectualcrafters.plot.util.area.QuadMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jesse Boyd
 */
public abstract class PlotArea {

    public final String worldname;
    public final String id;
    public final PlotManager manager;
    public final int worldhash;
    private final PlotId min;
    private final PlotId max;
    private final ConcurrentHashMap<PlotId, Plot> plots = new ConcurrentHashMap<>();
    private final IndependentPlotGenerator generator;
    public int MAX_PLOT_MEMBERS = 128;
    public boolean AUTO_MERGE = false;
    public boolean ALLOW_SIGNS = true;
    public boolean MOB_SPAWNING = false;
    public boolean MOB_SPAWNER_SPAWNING = false;
    public int PLOT_BIOME = 1;
    public boolean PLOT_CHAT = false;
    public boolean SCHEMATIC_CLAIM_SPECIFY = false;
    public boolean SCHEMATIC_ON_CLAIM = false;
    public String SCHEMATIC_FILE = "null";
    public List<String> SCHEMATICS = null;
    public Map<Flag<?>, Object> DEFAULT_FLAGS;
    public boolean USE_ECONOMY = false;
    public Map<String, Double> PRICES = new HashMap<>();
    public boolean SPAWN_EGGS = false;
    public boolean SPAWN_CUSTOM = true;
    public boolean SPAWN_BREEDING = false;
    public boolean WORLD_BORDER = false;
    public int TYPE = 0;
    public int TERRAIN = 0;
    public boolean HOME_ALLOW_NONMEMBER = false;
    public PlotLoc DEFAULT_HOME;
    public int MAX_BUILD_HEIGHT = 256;
    public int MIN_BUILD_HEIGHT = 1;
    public PlotGameMode GAMEMODE = PlotGameMode.CREATIVE;
    private int hash;
    private RegionWrapper region;
    private ConcurrentHashMap<String, Object> meta;
    private QuadMap<PlotCluster> clusters;

    public PlotArea(String worldName, String id, IndependentPlotGenerator generator, PlotId min, PlotId max) {
        this.worldname = worldName;
        this.id = id;
        this.manager = generator != null ? generator.getNewPlotManager() : null;
        this.generator = generator;
        if (min == null || max == null) {
            if (min != max) {
                throw new IllegalArgumentException("None of the ids can be null for this constructor");
            }
            this.min = null;
            this.max = null;
        } else {
            this.min = min;
            this.max = max;
        }
        this.worldhash = worldName.hashCode();
    }

    /**
     * Create a new PlotArea object with no functionality/information.
     *     - Mainly used during startup before worlds are created as a temporary object
     * @param world
     * @return
     */
    public static PlotArea createGeneric(String world) {
        return new PlotArea(world, null, null, null, null) {
            @Override
            public void loadConfiguration(ConfigurationSection config) {}
            @Override
            public ConfigurationNode[] getSettingNodes() {return null;}
        };
    }

    /**
     * Returns the region for this PlotArea or a RegionWrapper encompassing
     * the whole world if none exists.
     *
     * @return RegionWrapper
     */
    public RegionWrapper getRegion() {
        this.region = getRegionAbs();
        if (this.region == null) {
            return new RegionWrapper(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        return this.region;
    }

    /**
     * Returns the region for this PlotArea.
     *
     * @return RegionWrapper or null if no applicable region
     */
    public RegionWrapper getRegionAbs() {
        if (this.region == null) {
            if (this.min != null) {
                Location bot = getPlotManager().getPlotBottomLocAbs(this, this.min);
                Location top = getPlotManager().getPlotTopLocAbs(this, this.max);
                this.region = new RegionWrapper(bot.getX() - 1, top.getX() + 1, bot.getZ() - 1, top.getZ() + 1);
            }
        }
        return this.region;
    }

    /**
     * Returns the min PlotId.
     * @return
     */
    public PlotId getMin() {
        return this.min == null ? new PlotId(Integer.MIN_VALUE, Integer.MIN_VALUE) : this.min;
    }

    /**
     * Returns the max PlotId.
     * @return
     */
    public PlotId getMax() {
        return this.max == null ? new PlotId(Integer.MAX_VALUE, Integer.MAX_VALUE) : this.max;
    }

    /**
     * Get the implementation independent generator for this area.
     *
     * @return
     */
    public IndependentPlotGenerator getGenerator() {
        return this.generator;
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
        return this.worldhash == plotarea.worldhash && this.worldname.equals(plotarea.worldname) && StringMan.isEqual(this.id, plotarea.id);
    }

    public Set<PlotCluster> getClusters() {
        return this.clusters == null ? new HashSet<PlotCluster>() : this.clusters.getAll();
    }

    /**
     * Check if a PlotArea is compatible (move/copy etc).
     * @param plotArea
     * @return
     */
    public boolean isCompatible(PlotArea plotArea) {
        ConfigurationSection section = PS.get().config.getConfigurationSection("worlds");
        for (ConfigurationNode setting : plotArea.getSettingNodes()) {
            Object constant = section.get(plotArea.worldname + '.' + setting.getConstant());
            if (constant == null || !constant.equals(section.get(this.worldname + '.' + setting.getConstant()))) {
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
        this.MOB_SPAWNER_SPAWNING = config.getBoolean("mob_spawner_spawning");
        this.AUTO_MERGE = config.getBoolean("plot.auto_merge");
        this.MAX_PLOT_MEMBERS = config.getInt("limits.max-members");
        this.ALLOW_SIGNS = config.getBoolean("plot.create_signs");
        this.PLOT_BIOME = WorldUtil.IMP.getBiomeFromString(Configuration.BIOME.parseString(config.getString("plot.biome")));
        this.SCHEMATIC_ON_CLAIM = config.getBoolean("schematic.on_claim");
        this.SCHEMATIC_FILE = config.getString("schematic.file");
        this.SCHEMATIC_CLAIM_SPECIFY = config.getBoolean("schematic.specify_on_claim");
        this.SCHEMATICS = config.getStringList("schematic.schematics");
        this.USE_ECONOMY = config.getBoolean("economy.use") && EconHandler.getEconHandler() != null;
        ConfigurationSection priceSection = config.getConfigurationSection("economy.prices");
        if (this.USE_ECONOMY) {
            this.PRICES = new HashMap<>();
            for (String key : priceSection.getKeys(false)) {
                this.PRICES.put(key, priceSection.getDouble(key));
            }
        }
        this.PLOT_CHAT = config.getBoolean("chat.enabled");
        this.WORLD_BORDER = config.getBoolean("world.border");
        this.MAX_BUILD_HEIGHT = config.getInt("world.max_height");
        this.MIN_BUILD_HEIGHT = config.getInt("min.max_height");

        switch (config.getString("world.gamemode").toLowerCase()) {
            case "survival":
            case "s":
            case "0":
                this.GAMEMODE = PlotGameMode.SURVIVAL;
                break;
            case "creative":
            case "c":
            case "1":
                this.GAMEMODE = PlotGameMode.CREATIVE;
                break;
            case "adventure":
            case "a":
            case "2":
                this.GAMEMODE = PlotGameMode.ADVENTURE;
                break;
            case "spectator":
            case "3":
                this.GAMEMODE = PlotGameMode.SPECTATOR;
                break;
            default:
                this.GAMEMODE = PlotGameMode.NOT_SET;
                break;
        }

        this.HOME_ALLOW_NONMEMBER = config.getBoolean("home.allow-nonmembers");
        String homeDefault = config.getString("home.default");
        if ("side".equalsIgnoreCase(homeDefault)) {
            this.DEFAULT_HOME = null;
        } else if (StringMan.isEqualIgnoreCaseToAny(homeDefault, "center", "middle")) {
            this.DEFAULT_HOME = new PlotLoc(Integer.MAX_VALUE, Integer.MAX_VALUE);
        } else {
            try {
                String[] split = homeDefault.split(",");
                this.DEFAULT_HOME = new PlotLoc(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
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
            this.DEFAULT_FLAGS = FlagManager.parseFlags(flags);
        } catch (Exception e) {
            e.printStackTrace();
            PS.debug("&cInvalid default flags for " + this.worldname + ": " + StringMan.join(flags, ","));
            this.DEFAULT_FLAGS = new HashMap<>();
        }
        this.SPAWN_EGGS = config.getBoolean("event.spawn.egg");
        this.SPAWN_CUSTOM = config.getBoolean("event.spawn.custom");
        this.SPAWN_BREEDING = config.getBoolean("event.spawn.breeding");
        loadConfiguration(config);
    }

    public abstract void loadConfiguration(ConfigurationSection config);
    
    /**
     * Saving core PlotArea settings
     *
     * @param config Configuration Section
     */
    public void saveConfiguration(ConfigurationSection config) {
        HashMap<String, Object> options = new HashMap<>();
        options.put("natural_mob_spawning", this.MOB_SPAWNING);
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
        options.put("home.allow-nonmembers", false);
        options.put("world.max_height", this.MAX_BUILD_HEIGHT);
        options.put("world.min_height", this.MIN_BUILD_HEIGHT);
        options.put("world.gamemode", this.GAMEMODE.name().toLowerCase());

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
                PS.get().IMP.log(stringObjectEntry.toString());
                config.set(stringObjectEntry.getKey(), stringObjectEntry.getValue());
            }
        }
        if (!config.contains("flags")) {
            config.set("flags.use", "63,64,68,69,71,77,96,143,167,193,194,195,196,197,77,143,69,70,72,147,148,107,183,184,185,186,187,132");
        }
    }
    
    @Override
    public String toString() {
        if (this.id == null) {
            return this.worldname;
        } else {
            return this.worldname + ";" + this.id;
        }
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
     * Get the Plot at a location.
     * @param location
     * @return Plot
     */
    public Plot getPlotAbs(Location location) {
        PlotId pid = this.manager.getPlotId(this, location.getX(), location.getY(), location.getZ());
        if (pid == null) {
            return null;
        }
        return getPlotAbs(pid);
    }

    /**
     * Get the base plot at a location.
     * @param location
     * @return base Plot
     */
    public Plot getPlot(Location location) {
        PlotId pid = this.manager.getPlotId(this, location.getX(), location.getY(), location.getZ());
        if (pid == null) {
            return null;
        }
        return getPlot(pid);
    }

    /**
     * Get the base owned plot at a location.
     * @param location
     * @return base Plot or null
     */
    public Plot getOwnedPlot(Location location) {
        PlotId pid = this.manager.getPlotId(this, location.getX(), location.getY(), location.getZ());
        if (pid == null) {
            return null;
        }
        Plot plot = this.plots.get(pid);
        return plot == null ? null : plot.getBasePlot(false);
    }

    /**
     * Get the owned plot at a location.
     * @param location
     * @return Plot or null
     */
    public Plot getOwnedPlotAbs(Location location) {
        PlotId pid = this.manager.getPlotId(this, location.getX(), location.getY(), location.getZ());
        if (pid == null) {
            return null;
        }
        return this.plots.get(pid);
    }

    /**
     * Get the owned Plot at a PlotId.
     * @param id
     * @return Plot or null
     */
    public Plot getOwnedPlotAbs(PlotId id) {
        return this.plots.get(id);
    }
    
    public Plot getOwnedPlot(PlotId id) {
        Plot plot = this.plots.get(id);
        return plot == null ? null : plot.getBasePlot(false);
    }

    public boolean contains(int x, int z) {
        return this.TYPE != 2 || getRegionAbs().isIn(x, z);
    }
    
    public boolean contains(PlotId id) {
        return this.min == null || (id.x >= this.min.x && id.x <= this.max.x && id.y >= this.min.y && id.y <= this.max.y);
    }

    public boolean contains(Location location) {
        return StringMan.isEqual(location.getWorld(), this.worldname) && (getRegionAbs() == null || this.region
                .isIn(location.getX(), location.getZ()));
    }
    
    public Set<Plot> getPlotsAbs(final UUID uuid) {
        final HashSet<Plot> myPlots = new HashSet<>();
        foreachPlotAbs(new RunnableVal<Plot>() {
            @Override
            public void run(Plot value) {
                if (value.owner.equals(uuid)) {
                    myPlots.add(value);
                }
            }
        });
        return myPlots;
    }
    
    public Set<Plot> getPlots(final UUID uuid) {
        final HashSet<Plot> myplots = new HashSet<>();
        for (Plot plot : getPlots()) {
            if (plot.isBasePlot()) {
                if (plot.isOwner(uuid)) {
                    myplots.add(plot);
                }
            }
        }
        return myplots;
    }
    
    public Set<Plot> getPlots(PlotPlayer player) {
        return getPlots(player.getUUID());
    }
    
    public Set<Plot> getPlotsAbs(PlotPlayer player) {
        return player != null ? getPlotsAbs(player.getUUID()) : new HashSet<Plot>();
    }
    
    public int getPlotCount(UUID uuid) {
        int count = 0;
        if (!Settings.DONE_COUNTS_TOWARDS_LIMIT) {
            for (Plot plot : getPlotsAbs(uuid)) {
                if (!plot.hasFlag(Flags.DONE)) {
                    count++;
                }
            }
        } else {
            count += getPlotsAbs(uuid).size();
        }
        return count;
    }
    
    public int getPlotCount(PlotPlayer player) {
        return player != null ? getPlotCount(player.getUUID()) : 0;
    }

    public Plot getPlotAbs(PlotId id) {
        Plot plot = getOwnedPlotAbs(id);
        if (plot == null) {
            if (this.min != null && (id.x < this.min.x || id.x > this.max.x || id.y < this.min.y || id.y > this.max.y)) {
                return null;
            }
            return new Plot(this, id);
        }
        return plot;
    }
    
    public Plot getPlot(PlotId id) {
        Plot plot = getOwnedPlotAbs(id);
        if (plot == null) {
            if (this.min != null && (id.x < this.min.x || id.x > this.max.x || id.y < this.min.y || id.y > this.max.y)) {
                return null;
            }
            return new Plot(this, id);
        }
        return plot.getBasePlot(false);
    }
    
    public int getPlotCount() {
        return this.plots.size();
    }

    public PlotCluster getCluster(Location location) {
        Plot plot = getPlot(location);
        if (plot == null) {
            return null;
        }
        return this.clusters != null ? this.clusters.get(plot.getId().x, plot.getId().y) : null;
    }
    
    public PlotCluster getFirstIntersectingCluster(PlotId pos1, PlotId pos2) {
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

    public PlotCluster getCluster(PlotId id) {
        return this.clusters != null ? this.clusters.get(id.x, id.y) : null;
    }
    
    public PlotManager getPlotManager() {
        return this.manager;
    }
    
    /**
     * Session only plot metadata (session is until the server stops)<br>
     * <br>
     * For persistent metadata use the flag system
     * @see FlagManager
     * @param key
     * @param value
     */
    public void setMeta(String key, Object value) {
        if (this.meta == null) {
            this.meta = new ConcurrentHashMap<>();
        }
        this.meta.put(key, value);
    }
    
    /**
     * Get the metadata for a key<br>
     * <br>
     * For persistent metadata use the flag system
     * @param key
     * @return
     */
    public Object getMeta(String key) {
        if (this.meta != null) {
            return this.meta.get(key);
        }
        return null;
    }
    
    public Collection<Plot> getPlots() {
        return this.plots.values();
    }
    
    public Set<Plot> getBasePlots() {
        HashSet<Plot> myPlots = new HashSet<>(getPlots());
        Iterator<Plot> iterator = myPlots.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().isBasePlot()) {
                iterator.remove();
            }
        }
        return myPlots;
    }

    public void foreachPlotAbs(RunnableVal<Plot> run) {
        for (Entry<PlotId, Plot> entry : this.plots.entrySet()) {
            run.run(entry.getValue());
        }

    }

    public void foreachBasePlot(RunnableVal<Plot> run) {
        for (Plot plot : getPlots()) {
            if (plot.isBasePlot()) {
                run.run(plot);
            }
        }
    }

    public Map<PlotId, Plot> getPlotsRaw() {
        return this.plots;
    }

    public Set<Entry<PlotId, Plot>> getPlotEntries() {
        return this.plots.entrySet();
    }
    
    public boolean addPlot(Plot plot) {
        for (PlotPlayer pp : plot.getPlayersInPlot()) {
            pp.setMeta("lastplot", plot);
        }
        return this.plots.put(plot.getId(), plot) == null;
    }
    
    public boolean addPlotIfAbsent(Plot plot) {
        if (this.plots.putIfAbsent(plot.getId(), plot) == null) {
            for (PlotPlayer pp : plot.getPlayersInPlot()) {
                pp.setMeta("lastplot", plot);
            }
            return true;
        }
        return false;
    }

    public boolean addPlotAbs(Plot plot) {
        return this.plots.put(plot.getId(), plot) == null;
    }

    /**
     * Check if the plots in a selection are unowned
     * @param pos1
     * @param pos2
     * @return
     */
    public boolean isUnowned(PlotId pos1, PlotId pos2) {
        int area = (pos2.x - pos1.x + 1) * (pos2.y - pos1.y + 1);
        if (area > getPlotCount()) {
            for (Plot plot : getPlots()) {
                if (plot.getId().x >= pos1.x && plot.getId().x <= pos2.x && plot.getId().y >= pos1.y && plot.getId().y <= pos2.y) {
                    return false;
                }
            }
        } else {
            for (int x = pos1.x; x <= pos2.x; x++) {
                for (int y = pos1.y; y <= pos2.y; y++) {
                    PlotId id = new PlotId(x, y);
                    if (this.plots.get(id) != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Get the plot border distance for a world<br>
     * @return The border distance or Integer.MAX_VALUE if no border is set
     */
    public int getBorder() {
        Integer meta = (Integer) getMeta("worldBorder");
        if (meta != null) {
            int border = meta + 16;
            if (border == 0) {
                return Integer.MAX_VALUE;
            } else {
                return border;
            }
        }
        return Integer.MAX_VALUE;
    }
    
    /**
     * Setup the plot border for a world (usually done when the world is created)
     */
    public void setupBorder() {
        if (!this.WORLD_BORDER) {
            return;
        }
        Integer meta = (Integer) getMeta("worldBorder");
        if (meta == null) {
            setMeta("worldBorder", 1);
        }
        for (Plot plot : getPlots()) {
            plot.updateWorldBorder();
        }
    }

    /**
     * Delete the metadata for a key<br>
     *  - metadata is session only
     *  - deleting other plugin's metadata may cause issues
     * @param key
     */
    public void deleteMeta(String key) {
        if (this.meta != null) {
            this.meta.remove(key);
        }
    }

    public boolean canClaim(PlotPlayer player, PlotId pos1, PlotId pos2) {
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                PlotId id = new PlotId(x, y);
                Plot plot = getPlotAbs(id);
                if (!plot.canClaim(player)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean removePlot(PlotId id) {
        return this.plots.remove(id) != null;
    }

    public boolean mergePlots(ArrayList<PlotId> plotIds, boolean removeRoads, boolean updateDatabase) {
        if (plotIds.size() < 2) {
            return false;
        }
        PlotId pos1 = plotIds.get(0);
        PlotId pos2 = plotIds.get(plotIds.size() - 1);
        PlotManager manager = getPlotManager();

        boolean result = EventUtil.manager.callMerge(getPlotAbs(pos1), plotIds);
        if (!result) {
            return false;
        }

        HashSet<UUID> trusted = new HashSet<>();
        HashSet<UUID> members = new HashSet<>();
        HashSet<UUID> denied = new HashSet<>();
        
        manager.startPlotMerge(this, plotIds);
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
                boolean lx = x < pos2.x;
                boolean ly = y < pos2.y;
                PlotId id = new PlotId(x, y);
                Plot plot = getPlotAbs(id);
                plot.setTrusted(trusted);
                plot.setMembers(members);
                plot.setDenied(denied);
                Plot plot2;
                if (lx) {
                    if (ly) {
                        if (!plot.getMerged(1) || !plot.getMerged(2)) {
                            if (removeRoads) {
                                plot.removeRoadSouthEast();
                            }
                        }
                    }
                    if (!plot.getMerged(1)) {
                        plot2 = plot.getRelative(1, 0);
                        plot.mergePlot(plot2, removeRoads);
                    }
                }
                if (ly) {
                    if (!plot.getMerged(2)) {
                        plot2 = plot.getRelative(0, 1);
                        plot.mergePlot(plot2, removeRoads);
                    }
                }
            }
        }
        manager.finishPlotMerge(this, plotIds);
        return true;
    }
    
    /**
     * Get a set of owned plots within a selection (chooses the best algorithm based on selection size.<br>
     * i.e. A selection of billions of plots will work fine
     * @param pos1
     * @param pos2
     * @return
     */
    public HashSet<Plot> getPlotSelectionOwned(PlotId pos1, PlotId pos2) {
        int size = (1 + pos2.x - pos1.x) * (1 + pos2.y - pos1.y);
        HashSet<Plot> result = new HashSet<>();
        if (size < 16 || size < getPlotCount()) {
            for (PlotId pid : MainUtil.getPlotSelectionIds(pos1, pos2)) {
                Plot plot = getPlotAbs(pid);
                if (plot.hasOwner()) {
                    if (plot.getId().x > pos1.x || plot.getId().y > pos1.y || plot.getId().x < pos2.x || plot.getId().y < pos2.y) {
                        result.add(plot);
                    }
                }
            }
        } else {
            for (Plot plot : getPlots()) {
                if (plot.getId().x > pos1.x || plot.getId().y > pos1.y || plot.getId().x < pos2.x || plot.getId().y < pos2.y) {
                    result.add(plot);
                }
            }
        }
        return result;
    }
    
    public void removeCluster(PlotCluster plotCluster) {
        if (this.clusters == null) {
            throw new IllegalAccessError("Clusters not enabled!");
        }
        this.clusters.remove(plotCluster);
    }
    
    public void addCluster(PlotCluster plotCluster) {
        if (this.clusters == null) {
            this.clusters = new QuadMap<PlotCluster>(Integer.MAX_VALUE, 0, 0, 64) {
                @Override
                public RegionWrapper getRegion(PlotCluster value) {
                    return new RegionWrapper(value.getP1().x, value.getP2().x, value.getP1().y, value.getP2().y);
                }
            };
        }
        this.clusters.add(plotCluster);
    }
    
    public PlotCluster getCluster(String string) {
        for (PlotCluster cluster : getClusters()) {
            if (cluster.getName().equalsIgnoreCase(string)) {
                return cluster;
            }
        }
        return null;
    }
}
