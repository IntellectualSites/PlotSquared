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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.util.EconHandler;

/**
 * @author Jesse Boyd
 */
public abstract class PlotWorld {
    public final static boolean AUTO_MERGE_DEFAULT = false;
    public final static boolean ALLOW_SIGNS_DEFAULT = true;
    public final static boolean MOB_SPAWNING_DEFAULT = false;
    public final static String PLOT_BIOME_DEFAULT = "FOREST";
    public final static boolean PLOT_CHAT_DEFAULT = false;
    public final static boolean SCHEMATIC_CLAIM_SPECIFY_DEFAULT = false;
    public final static boolean SCHEMATIC_ON_CLAIM_DEFAULT = false;
    public final static String SCHEMATIC_FILE_DEFAULT = "null";
    public final static List<String> SCHEMATICS_DEFAULT = null;
    public final static boolean USE_ECONOMY_DEFAULT = false;
    public final static double PLOT_PRICE_DEFAULT = 100;
    public final static double MERGE_PRICE_DEFAULT = 100;
    public final static double SELL_PRICE_DEFAULT = 75;
    public final static boolean PVP_DEFAULT = false;
    public final static boolean PVE_DEFAULT = false;
    public final static boolean SPAWN_EGGS_DEFAULT = false;
    public final static boolean SPAWN_CUSTOM_DEFAULT = true;
    public final static boolean SPAWN_BREEDING_DEFAULT = false;
    public final static boolean WORLD_BORDER_DEFAULT = false;
    public final static int MAX_PLOT_MEMBERS_DEFAULT = 128;
    public final static int MAX_BUILD_HEIGHT_DEFAULT = 256;
    // are plot clusters enabled
    // require claim in cluster
    // TODO make this configurable
    // make non static and static_default_valu + add config option
    public static int[] BLOCKS;
    static {
        BLOCKS = new int[] { 1, 2, 3, 4, 5, 7, 14, 15, 16, 17, 19, 21, 22, 23, 24, 25, 35, 41, 42, 43, 45, 47, 48, 49, 52, 56, 57, 58, 61, 62, 73, 74, 80, 82, 84, 86, 87, 88, 91, 97, 98, 99, 100, 103, 110, 112, 120, 121, 123, 124, 125, 129, 133, 153, 155, 159, 162, 165, 166, 168, 170, 172, 173, 174, 179, 181 };
    }
    public final String worldname;
    public int MAX_PLOT_MEMBERS;
    public boolean AUTO_MERGE;
    public boolean ALLOW_SIGNS;
    public boolean MOB_SPAWNING;
    public String PLOT_BIOME;
    public boolean PLOT_CHAT;
    public boolean SCHEMATIC_CLAIM_SPECIFY = false;
    public boolean SCHEMATIC_ON_CLAIM;
    public String SCHEMATIC_FILE;
    public List<String> SCHEMATICS;
    public HashMap<String, Flag> DEFAULT_FLAGS;
    public boolean USE_ECONOMY;
    public double PLOT_PRICE;
    public double MERGE_PRICE;
    public double SELL_PRICE;
    public boolean PVP;
    public boolean PVE;
    public boolean SPAWN_EGGS;
    public boolean SPAWN_CUSTOM;
    public boolean SPAWN_BREEDING;
    public boolean WORLD_BORDER;
    public int TYPE = 0;
    public int TERRAIN = 0;
    public boolean HOME_ALLOW_NONMEMBER;
    public PlotLoc DEFAULT_HOME;
    public int MAX_BUILD_HEIGHT;

    public PlotWorld(final String worldname) {
        this.worldname = worldname;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PlotWorld plotworld = (PlotWorld) obj;
        ConfigurationSection section = PS.get().config.getConfigurationSection("worlds");
        for (ConfigurationNode setting : plotworld.getSettingNodes()) {
            Object constant = section.get(plotworld.worldname + "." + setting.getConstant());
            if (constant == null) {
                return false;
            }
            if (!constant.equals(section.get(this.worldname + "." + setting.getConstant())))  {
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
        if (config.contains("generator.terrain")) {
            this.TERRAIN = config.getInt("generator.terrain");
            this.TYPE = config.getInt("generator.type");
        }
        this.MOB_SPAWNING = config.getBoolean("natural_mob_spawning");
        this.AUTO_MERGE = config.getBoolean("plot.auto_merge");
        this.MAX_PLOT_MEMBERS = config.getInt("limits.max-members");
        this.ALLOW_SIGNS = config.getBoolean("plot.create_signs");
        this.PLOT_BIOME = (String) Configuration.BIOME.parseString(config.getString("plot.biome"));
        this.SCHEMATIC_ON_CLAIM = config.getBoolean("schematic.on_claim");
        this.SCHEMATIC_FILE = config.getString("schematic.file");
        this.SCHEMATIC_CLAIM_SPECIFY = config.getBoolean("schematic.specify_on_claim");
        this.SCHEMATICS = config.getStringList("schematic.schematics");
        this.USE_ECONOMY = config.getBoolean("economy.use") && (EconHandler.manager != null);
        this.PLOT_PRICE = config.getDouble("economy.prices.claim");
        this.MERGE_PRICE = config.getDouble("economy.prices.merge");
        this.SELL_PRICE = config.getDouble("economy.prices.sell");
        this.PLOT_CHAT = config.getBoolean("chat.enabled");
        this.WORLD_BORDER = config.getBoolean("world.border");
        this.MAX_BUILD_HEIGHT = config.getInt("world.max_height");
        
        this.HOME_ALLOW_NONMEMBER = config.getBoolean("home.allow-nonmembers");
        String homeDefault = config.getString("home.default");
        if (homeDefault.equalsIgnoreCase("side")) {
            DEFAULT_HOME = null;
        }
        else if (homeDefault.equalsIgnoreCase("center")) {
            DEFAULT_HOME = new PlotLoc(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
        else {
            try {
                String[] split = homeDefault.split(",");
                DEFAULT_HOME = new PlotLoc(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
            }
            catch (Exception e) {
                DEFAULT_HOME = null;
            }
        }
        
        List<String> flags = config.getStringList("flags.default");
        if (flags == null || flags.size() == 0) {
            flags = config.getStringList("flags");
            if (flags == null || flags.size() == 0) {
                flags = new ArrayList<String>();
                ConfigurationSection section = config.getConfigurationSection("flags");
                Set<String> keys = section.getKeys(false);
                for (String key : keys) {
                    if (!key.equals("default")) {
                        flags.add(key + ";" + section.get(key));
                    }
                }
            }
        }
        try {
            this.DEFAULT_FLAGS = FlagManager.parseFlags(flags);
        } catch (final Exception e) {
            e.printStackTrace();
            PS.log("&cInvalid default flags for " + this.worldname + ": " + StringUtils.join(flags, ","));
            this.DEFAULT_FLAGS = new HashMap<>();
        }
        this.PVP = config.getBoolean("event.pvp");
        this.PVE = config.getBoolean("event.pve");
        this.SPAWN_EGGS = config.getBoolean("event.spawn.egg");
        this.SPAWN_CUSTOM = config.getBoolean("event.spawn.custom");
        this.SPAWN_BREEDING = config.getBoolean("event.spawn.breeding");
        loadConfiguration(config);
    }

    public abstract void loadConfiguration(final ConfigurationSection config);

    /**
     * Saving core plotworld settings
     *
     * @param config Configuration Section
     */
    public void saveConfiguration(final ConfigurationSection config) {
        final HashMap<String, Object> options = new HashMap<>();
        options.put("natural_mob_spawning", PlotWorld.MOB_SPAWNING_DEFAULT);
        options.put("plot.auto_merge", PlotWorld.AUTO_MERGE_DEFAULT);
        options.put("plot.create_signs", PlotWorld.ALLOW_SIGNS_DEFAULT);
        options.put("plot.biome", PlotWorld.PLOT_BIOME_DEFAULT.toString());
        options.put("schematic.on_claim", PlotWorld.SCHEMATIC_ON_CLAIM_DEFAULT);
        options.put("schematic.file", PlotWorld.SCHEMATIC_FILE_DEFAULT);
        options.put("schematic.specify_on_claim", PlotWorld.SCHEMATIC_CLAIM_SPECIFY_DEFAULT);
        options.put("schematic.schematics", PlotWorld.SCHEMATICS_DEFAULT);
        options.put("economy.use", PlotWorld.USE_ECONOMY_DEFAULT);
        options.put("economy.prices.claim", PlotWorld.PLOT_PRICE_DEFAULT);
        options.put("economy.prices.merge", PlotWorld.MERGE_PRICE_DEFAULT);
        options.put("economy.prices.sell", PlotWorld.SELL_PRICE_DEFAULT);
        options.put("chat.enabled", PlotWorld.PLOT_CHAT_DEFAULT);
        options.put("flags.default", null);
        options.put("event.pvp", PlotWorld.PVP_DEFAULT);
        options.put("event.pve", PlotWorld.PVE_DEFAULT);
        options.put("event.spawn.egg", PlotWorld.SPAWN_EGGS_DEFAULT);
        options.put("event.spawn.custom", PlotWorld.SPAWN_CUSTOM_DEFAULT);
        options.put("event.spawn.breeding", PlotWorld.SPAWN_BREEDING_DEFAULT);
        options.put("world.border", PlotWorld.WORLD_BORDER_DEFAULT);
        options.put("limits.max-members", PlotWorld.MAX_PLOT_MEMBERS_DEFAULT);
        options.put("home.default", "side");
        options.put("home.allow-nonmembers", false);
        options.put("world.max_height", PlotWorld.MAX_BUILD_HEIGHT_DEFAULT);

        if (Settings.ENABLE_CLUSTERS && (this.TYPE != 0)) {
            options.put("generator.terrain", this.TERRAIN);
            options.put("generator.type", this.TYPE);
        }
        final ConfigurationNode[] settings = getSettingNodes();
        /*
         * Saving generator specific settings
         */
        for (final ConfigurationNode setting : settings) {
            options.put(setting.getConstant(), setting.getValue());
        }
        for (final String option : options.keySet()) {
            if (!config.contains(option)) {
                config.set(option, options.get(option));
            }
        }
    }

    /**
     * Used for the <b>/plot setup</b> command Return null if you do not want to support this feature
     *
     * @return ConfigurationNode[]
     */
    public abstract ConfigurationNode[] getSettingNodes();
}
