////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.GridPlotWorld;
import com.intellectualcrafters.plot.generator.IndependentPlotGenerator;
import com.intellectualcrafters.plot.util.*;
import com.intellectualcrafters.plot.util.area.QuadMap;

import java.util.*;
import java.util.Map.Entry;
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
    public HashMap<String, Flag> DEFAULT_FLAGS;
    public boolean USE_ECONOMY = false;
    public double PLOT_PRICE = 100;
    public double MERGE_PRICE = 100;
    public double SELL_PRICE = 100;
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
    public PlotGamemode GAMEMODE = PlotGamemode.CREATIVE;
    int hash;
    private RegionWrapper region;
    private ConcurrentHashMap<String, Object> meta;
    private QuadMap<PlotCluster> clusters;

    public PlotArea(final String worldname, String id, IndependentPlotGenerator generator, PlotId min, PlotId max) {
        this.worldname = worldname;
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
        this.worldhash = worldname.hashCode();
    }
    
    public static PlotArea createGeneric(String world) {
        return new PlotArea(world, null, null, null, null) {
            @Override
            public void loadConfiguration(ConfigurationSection config) {}
            @Override
            public ConfigurationNode[] getSettingNodes() {return null;}
        };
    }

    /**
     * Returns the region for this PlotArea
     * @return
     */
    public RegionWrapper getRegion() {
        region = getRegionAbs();
        if (region == null) {
            return new RegionWrapper(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        return region;
    }
    
    public RegionWrapper getRegionAbs() {
        if (region == null) {
            if (min != null) {
                Location bot = getPlotManager().getPlotBottomLocAbs(this, min);
                Location top = getPlotManager().getPlotTopLocAbs(this, max);
                this.region = new RegionWrapper(bot.getX() - 1, top.getX() + 1, bot.getZ() - 1, top.getZ() + 1);
            }
        }
        return region;
    }

    /**
     * Returns the min PlotId
     * @return
     */
    public PlotId getMin() {
        return min == null ? new PlotId(Integer.MIN_VALUE, Integer.MIN_VALUE) : min;
    }

    /**
     * Returns the max PlotId
     * @return
     */
    public PlotId getMax() {
        return max == null ? new PlotId(Integer.MAX_VALUE, Integer.MAX_VALUE) : max;
    }

    public IndependentPlotGenerator getGenerator() {
        return generator;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlotArea plotarea = (PlotArea) obj;
        return this.worldhash == plotarea.worldhash && this.worldname.equals(plotarea.worldname) && StringMan.isEqual(this.id, plotarea.id);
    }

    public Set<PlotCluster> getClusters() {
        return clusters == null ? new HashSet<PlotCluster>() : clusters.getAll();
    }

    public boolean isCompatible(PlotArea plotarea) {
        final ConfigurationSection section = PS.get().config.getConfigurationSection("worlds");
        for (final ConfigurationNode setting : plotarea.getSettingNodes()) {
            final Object constant = section.get(plotarea.worldname + "." + setting.getConstant());
            if (constant == null) {
                return false;
            }
            if (!constant.equals(section.get(worldname + "." + setting.getConstant()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * When a world is created, the following method will be called for each
     *
     * @param config Configuration Section
     */
    public void loadDefaultConfiguration(final ConfigurationSection config) {
        if ((min != null || max != null) && !(this instanceof GridPlotWorld)) {
            throw new IllegalArgumentException("Must extend GridPlotWorld to provide");
        }
        if (config.contains("generator.terrain")) {
            TERRAIN = config.getInt("generator.terrain");
            TYPE = config.getInt("generator.type");
        }
        MOB_SPAWNING = config.getBoolean("natural_mob_spawning");
        MOB_SPAWNER_SPAWNING = config.getBoolean("mob_spawner_spawning");
        AUTO_MERGE = config.getBoolean("plot.auto_merge");
        MAX_PLOT_MEMBERS = config.getInt("limits.max-members");
        ALLOW_SIGNS = config.getBoolean("plot.create_signs");
        PLOT_BIOME = WorldUtil.IMP.getBiomeFromString(Configuration.BIOME.parseString(config.getString("plot.biome")));
        SCHEMATIC_ON_CLAIM = config.getBoolean("schematic.on_claim");
        SCHEMATIC_FILE = config.getString("schematic.file");
        SCHEMATIC_CLAIM_SPECIFY = config.getBoolean("schematic.specify_on_claim");
        SCHEMATICS = config.getStringList("schematic.schematics");
        USE_ECONOMY = config.getBoolean("economy.use") && EconHandler.manager != null;
        PLOT_PRICE = config.getDouble("economy.prices.claim");
        MERGE_PRICE = config.getDouble("economy.prices.merge");
        SELL_PRICE = config.getDouble("economy.prices.sell");
        PLOT_CHAT = config.getBoolean("chat.enabled");
        WORLD_BORDER = config.getBoolean("world.border");
        MAX_BUILD_HEIGHT = config.getInt("world.max_height");
        MIN_BUILD_HEIGHT = config.getInt("min.max_height");

        switch (config.getString("world.gamemode").toLowerCase()) {
            case "survival":
            case "s":
            case "0":
                GAMEMODE = PlotGamemode.SURVIVAL;
                break;
            case "creative":
            case "c":
            case "1":
                GAMEMODE = PlotGamemode.CREATIVE;
                break;
            case "adventure":
            case "a":
            case "2":
                GAMEMODE = PlotGamemode.ADVENTURE;
                break;
            case "spectator":
            case "3":
                GAMEMODE = PlotGamemode.SPECTATOR;
                break;
            default:
                PS.debug("&cInvalid gamemode set for: " + worldname);
                break;
        }

        HOME_ALLOW_NONMEMBER = config.getBoolean("home.allow-nonmembers");
        final String homeDefault = config.getString("home.default");
        if ("side".equalsIgnoreCase(homeDefault)) {
            DEFAULT_HOME = null;
        } else if (StringMan.isEqualIgnoreCaseToAny(homeDefault, "center", "middle")) {
            DEFAULT_HOME = new PlotLoc(Integer.MAX_VALUE, Integer.MAX_VALUE);
        } else {
            try {
                final String[] split = homeDefault.split(",");
                DEFAULT_HOME = new PlotLoc(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            } catch (NumberFormatException e) {
                DEFAULT_HOME = null;
            }
        }

        List<String> flags = config.getStringList("flags.default");
        if (flags == null || flags.isEmpty()) {
            flags = config.getStringList("flags");
            if (flags == null || flags.isEmpty()) {
                flags = new ArrayList<>();
                final ConfigurationSection section = config.getConfigurationSection("flags");
                final Set<String> keys = section.getKeys(false);
                for (final String key : keys) {
                    if (!"default".equals(key)) {
                        flags.add(key + ";" + section.get(key));
                    }
                }
            }
        }
        try {
            DEFAULT_FLAGS = FlagManager.parseFlags(flags);
        } catch (final Exception e) {
            e.printStackTrace();
            PS.debug("&cInvalid default flags for " + worldname + ": " + StringMan.join(flags, ","));
            DEFAULT_FLAGS = new HashMap<>();
        }
        SPAWN_EGGS = config.getBoolean("event.spawn.egg");
        SPAWN_CUSTOM = config.getBoolean("event.spawn.custom");
        SPAWN_BREEDING = config.getBoolean("event.spawn.breeding");
        loadConfiguration(config);
    }
    
    public abstract void loadConfiguration(final ConfigurationSection config);
    
    /**
     * Saving core plotarea settings
     *
     * @param config Configuration Section
     */
    public void saveConfiguration(final ConfigurationSection config) {
        final HashMap<String, Object> options = new HashMap<>();
        options.put("natural_mob_spawning", MOB_SPAWNING);
        options.put("mob_spawner_spawning", MOB_SPAWNER_SPAWNING);
        options.put("plot.auto_merge", AUTO_MERGE);
        options.put("plot.create_signs", ALLOW_SIGNS);
        options.put("plot.biome", "FOREST");
        options.put("schematic.on_claim", SCHEMATIC_ON_CLAIM);
        options.put("schematic.file", SCHEMATIC_FILE);
        options.put("schematic.specify_on_claim", SCHEMATIC_CLAIM_SPECIFY);
        options.put("schematic.schematics", SCHEMATICS);
        options.put("economy.use", USE_ECONOMY);
        options.put("economy.prices.claim", PLOT_PRICE);
        options.put("economy.prices.merge", MERGE_PRICE);
        options.put("economy.prices.sell", SELL_PRICE);
        options.put("chat.enabled", PLOT_CHAT);
        options.put("flags.default", null);
        options.put("event.spawn.egg", SPAWN_EGGS);
        options.put("event.spawn.custom", SPAWN_CUSTOM);
        options.put("event.spawn.breeding", SPAWN_BREEDING);
        options.put("world.border", WORLD_BORDER);
        options.put("limits.max-members", MAX_PLOT_MEMBERS);
        options.put("home.default", "side");
        options.put("home.allow-nonmembers", false);
        options.put("world.max_height", MAX_BUILD_HEIGHT);
        options.put("world.min_height", MIN_BUILD_HEIGHT);
        options.put("world.gamemode", GAMEMODE.name().toLowerCase());

        if (TYPE != 0) {
            options.put("generator.terrain", TERRAIN);
            options.put("generator.type", TYPE);
        }
        final ConfigurationNode[] settings = getSettingNodes();
        /*
         * Saving generator specific settings
         */
        for (final ConfigurationNode setting : settings) {
            options.put(setting.getConstant(), setting.getValue());
        }
        for (final Entry<String, Object> stringObjectEntry : options.entrySet()) {
            if (!config.contains(stringObjectEntry.getKey())) {
                config.set(stringObjectEntry.getKey(), stringObjectEntry.getValue());
            }
        }
        if (!config.contains("flags")) {
            config.set("flags.use", "63,64,68,69,71,77,96,143,167,193,194,195,196,197,77,143,69,70,72,147,148,107,183,184,185,186,187,132");
        }
    }
    
    @Override
    public String toString() {
        return id == null ? worldname : worldname + ";" + id;
    }
    
    @Override
    public int hashCode() {
        if (hash != 0) {
            return hash;
        }
        return hash = toString().hashCode();
    }

    /**
     * Used for the <b>/plot setup</b> command Return null if you do not want to support this feature
     *
     * @return ConfigurationNode[]
     */
    public abstract ConfigurationNode[] getSettingNodes();
    
    public Plot getPlotAbs(Location loc) {
        PlotId pid = manager.getPlotId(this, loc.getX(), loc.getY(), loc.getZ());
        if (pid == null) {
            return null;
        }
        return getPlotAbs(pid);
    }
    
    public Plot getPlot(Location loc) {
        PlotId pid = manager.getPlotId(this, loc.getX(), loc.getY(), loc.getZ());
        if (pid == null) {
            return null;
        }
        return getPlot(pid);
    }
    
    public Plot getOwnedPlot(Location loc) {
        PlotId pid = manager.getPlotId(this, loc.getX(), loc.getY(), loc.getZ());
        if (pid == null) {
            return null;
        }
        Plot plot = plots.get(pid);
        return plot == null ? null : plot.getBasePlot(false);
    }
    
    public Plot getOwnedPlotAbs(Location loc) {
        PlotId pid = manager.getPlotId(this, loc.getX(), loc.getY(), loc.getZ());
        if (pid == null) {
            return null;
        }
        return plots.get(pid);
    }
    
    public Plot getOwnedPlotAbs(PlotId id) {
        return plots.get(id);
    }
    
    public Plot getOwnedPlot(PlotId id) {
        Plot plot = plots.get(id);
        return plot == null ? null : plot.getBasePlot(false);
    }

    public boolean contains(int x, int z) {
        return TYPE != 2 || getRegionAbs().isIn(x, z);
    }
    
    public boolean contains(PlotId id) {
        return min == null || (id.x >= min.x && id.x <= max.x && id.y >= min.y && id.y <= max.y);
    }

    public boolean contains(Location loc) {
        return StringMan.isEqual(loc.getWorld(), worldname) && (getRegionAbs() == null || region.isIn(loc.getX(), loc.getZ()));
    }
    
    public Set<Plot> getPlotsAbs(final UUID uuid) {
        final HashSet<Plot> plots = new HashSet<>();
        foreachPlotAbs(new RunnableVal<Plot>() {
            @Override
            public void run(Plot value) {
                if (value.owner.equals(uuid)) {
                    plots.add(value);
                }
            }
        });
        return plots;
    }
    
    public Set<Plot> getPlots(UUID uuid) {
        HashSet<Plot> plots = new HashSet<>();
        for (Plot plot : getPlots()) {
            if (plot.isBasePlot()) {
                if (plot.isOwner(uuid)) {
                    plots.add(plot);
                }
            }
        }
        return plots;
    }
    
    public Set<Plot> getPlots(PlotPlayer player) {
        return player != null ? getPlots(player.getUUID()) : new HashSet<Plot>();
    }
    
    public Set<Plot> getPlotsAbs(PlotPlayer player) {
        return player != null ? getPlotsAbs(player.getUUID()) : new HashSet<Plot>();
    }
    
    public int getPlotCount(UUID uuid) {
        int count = 0;
        if (!Settings.DONE_COUNTS_TOWARDS_LIMIT) {
            for (Plot plot : getPlotsAbs(uuid)) {
                if (!plot.getFlags().containsKey("done")) {
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
            if (min != null && (id.x < min.x || id.x > max.x || id.y < min.y || id.y > max.y)) {
                return null;
            }
            return new Plot(this, id);
        }
        return plot;
    }
    
    public Plot getPlot(PlotId id) {
        Plot plot = getOwnedPlotAbs(id);
        if (plot == null) {
            if (min != null && (id.x < min.x || id.x > max.x || id.y < min.y || id.y > max.y)) {
                return null;
            }
            return new Plot(this, id);
        }
        return plot.getBasePlot(false);
    }
    
    public int getPlotCount() {
        return plots.size();
    }
    
    public PlotCluster getCluster(Location loc) {
        if (!Settings.ENABLE_CLUSTERS) {
            return null;
        }
        Plot plot = getPlot(loc);
        if (plot == null) {
            return null;
        }
        return clusters != null ? clusters.get(plot.getId().x, plot.getId().y) : null;
    }
    
    public PlotCluster getFirstIntersectingCluster(PlotId pos1, PlotId pos2) {
        if (!Settings.ENABLE_CLUSTERS || clusters == null) {
            return null;
        }
        for (PlotCluster cluster : clusters.getAll()) {
            if (cluster.intersects(pos1, pos2)) {
                return cluster;
            }
        }
        return null;
    }

    public PlotCluster getCluster(PlotId id) {
        if (!Settings.ENABLE_CLUSTERS) {
            return null;
        }
        return clusters != null ? clusters.get(id.x, id.y) : null;
    }
    
    public PlotManager getPlotManager() {
        return manager;
    }
    
    /**
     * Session only plot metadata (session is until the server stops)<br>
     * <br>
     * For persistent metadata use the flag system
     * @see FlagManager
     * @param key
     * @param value
     */
    public void setMeta(final String key, final Object value) {
        if (meta == null) {
            meta = new ConcurrentHashMap<>();
        }
        meta.put(key, value);
    }
    
    /**
     * Get the metadata for a key<br>
     * <br>
     * For persistent metadata use the flag system
     * @param key
     * @return
     */
    public Object getMeta(final String key) {
        if (meta != null) {
            return meta.get(key);
        }
        return null;
    }
    
    public Collection<Plot> getPlots() {
        return plots.values();
    }
    
    public Set<Plot> getBasePlots() {
        HashSet<Plot> plots = new HashSet<>(getPlots());
        Iterator<Plot> iter = plots.iterator();
        while (iter.hasNext()) {
            if (!iter.next().isBasePlot()) {
                iter.remove();
            }
        }
        return plots;
    }

    public void foreachPlotAbs(RunnableVal<Plot> run) {
        for (Entry<PlotId, Plot> entry : plots.entrySet()) {
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
        return plots;
    }

    public Set<Entry<PlotId, Plot>> getPlotEntries() {
        return plots.entrySet();
    }
    
    public boolean addPlot(Plot plot) {
        for (PlotPlayer pp : plot.getPlayersInPlot()) {
            pp.setMeta("lastplot", plot);
        }
        return plots.put(plot.getId(), plot) == null;
    }
    
    public boolean addPlotIfAbsent(Plot plot) {
        if (plots.putIfAbsent(plot.getId(), plot) == null) {
            for (PlotPlayer pp : plot.getPlayersInPlot()) {
                pp.setMeta("lastplot", plot);
            }
            return true;
        }
        return false;
    }

    public boolean addPlotAbs(Plot plot) {
        return plots.put(plot.getId(), plot) == null;
    }

    /**
     * Check if the plots in a selection are unowned
     * @param pos1
     * @param pos2
     * @return
     */
    public boolean isUnowned(final PlotId pos1, final PlotId pos2) {
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
                    final PlotId id = new PlotId(x, y);
                    if (plots.get(id) != null) {
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
            final int border = meta + 16;
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
        if (!WORLD_BORDER) {
            return;
        }
        Integer meta = (Integer) getMeta("worldBorder");
        if (meta == null) {
            setMeta("worldBorder", 1);
        }
        for (final Plot plot : getPlots()) {
            plot.updateWorldBorder();
        }
    }

    /**
     * Delete the metadata for a key<br>
     *  - metadata is session only
     *  - deleting other plugin's metadata may cause issues
     * @param key
     */
    public void deleteMeta(final String key) {
        if (meta != null) {
            meta.remove(key);
        }
    }
    
    public boolean canClaim(final PlotPlayer player, final PlotId pos1, final PlotId pos2) {
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                final PlotId id = new PlotId(x, y);
                final Plot plot = getPlot(id);
                if (!plot.canClaim(player)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean mergePlots(final PlotPlayer player, final ArrayList<PlotId> plotIds) {
        if (EconHandler.manager != null && USE_ECONOMY) {
            final double cost = plotIds.size() * MERGE_PRICE;
            if (cost > 0d) {
                if (EconHandler.manager.getMoney(player) < cost) {
                    MainUtil.sendMessage(player, C.CANNOT_AFFORD_MERGE, "" + cost);
                    return false;
                }
                EconHandler.manager.withdrawMoney(player, cost);
                MainUtil.sendMessage(player, C.REMOVED_BALANCE, cost + "");
            }
        }
        return mergePlots(plotIds, true, true);
    }
    
    public boolean removePlot(PlotId id) {
        return plots.remove(id) != null;
    }

    public boolean mergePlots(final ArrayList<PlotId> plotIds, final boolean removeRoads, final boolean updateDatabase) {
        if (plotIds.size() < 2) {
            return false;
        }
        final PlotId pos1 = plotIds.get(0);
        final PlotId pos2 = plotIds.get(plotIds.size() - 1);
        final PlotManager manager = getPlotManager();
        
        final boolean result = EventUtil.manager.callMerge(getPlotAbs(pos1), plotIds);
        if (!result) {
            return false;
        }

        final HashSet<UUID> trusted = new HashSet<>();
        final HashSet<UUID> members = new HashSet<>();
        final HashSet<UUID> denied = new HashSet<>();
        
        manager.startPlotMerge(this, plotIds);
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                final PlotId id = new PlotId(x, y);
                final Plot plot = getPlotAbs(id);
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
    public HashSet<Plot> getPlotSelectionOwned(final PlotId pos1, final PlotId pos2) {
        final int size = (1 + pos2.x - pos1.x) * (1 + pos2.y - pos1.y);
        final HashSet<Plot> result = new HashSet<>();
        if (size < 16 || size < getPlotCount()) {
            for (final PlotId pid : MainUtil.getPlotSelectionIds(pos1, pos2)) {
                final Plot plot = getPlotAbs(pid);
                if (plot.hasOwner()) {
                    if (plot.getId().x > pos1.x || plot.getId().y > pos1.y || plot.getId().x < pos2.x || plot.getId().y < pos2.y) {
                        result.add(plot);
                    }
                }
            }
        } else {
            for (final Plot plot : getPlots()) {
                if (plot.getId().x > pos1.x || plot.getId().y > pos1.y || plot.getId().x < pos2.x || plot.getId().y < pos2.y) {
                    result.add(plot);
                }
            }
        }
        return result;
    }
    
    public void removeCluster(PlotCluster plotCluster) {
        if (!Settings.ENABLE_CLUSTERS || clusters == null) {
            throw new IllegalAccessError("Clusters not enabled!");
        }
        clusters.remove(plotCluster);
    }
    
    public void addCluster(PlotCluster plotCluster) {
        if (!Settings.ENABLE_CLUSTERS) {
            throw new IllegalAccessError("Clusters not enabled!");
        }
        if (clusters == null) {
            clusters = new QuadMap<PlotCluster>(Integer.MAX_VALUE, 0, 0, 64) {
                @Override
                public RegionWrapper getRegion(PlotCluster value) {
                    return new RegionWrapper(value.getP1().x, value.getP2().x, value.getP1().y, value.getP2().y);
                }
            };
        }
        clusters.add(plotCluster);
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
