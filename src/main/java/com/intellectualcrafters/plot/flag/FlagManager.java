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
package com.intellectualcrafters.plot.flag;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;

import java.util.*;

/**
 * Flag Manager Utility
 *
 * @author Citymonstret
 * @author Empire92
 */
@SuppressWarnings("unused")
public class FlagManager {
    // TODO add some flags
    // - Plot clear interval
    // - Mob cap
    // - customized plot composition
    
    private final static HashSet<String> reserved = new HashSet<>();
    
    private final static HashSet<AbstractFlag> flags = new HashSet<>();

    /**
     * Reserve a flag so that it cannot be set by players
     * @param flag
     */
    public static void reserveFlag(String flag) {
        reserved.add(flag);
    }
    
    /**
     * Get if a flag is reserved
     * @param flag
     * @return
     */
    public static boolean isReserved(String flag) {
        return reserved.contains(flag);
    }
    
    /**
     * Get the reserved flags
     * @return
     */
    public static HashSet<String> getReservedFlags() {
        return (HashSet<String>) reserved.clone();
    }
    
    /**
     * Unreserve a flag
     * @param flag
     */
    public static void unreserveFlag(String flag) {
        reserved.remove(flag);
    }
    
    /**
     * Register an AbstractFlag with PlotSquared
     *
     * @param af Flag to register
     *
     * @return boolean success
     */
    public static boolean addFlag(AbstractFlag af) {
        return addFlag(af, false);
    }
    
    public static boolean addFlag(AbstractFlag af, boolean reserved) {
        PS.log(C.PREFIX.s() + "&8 - Adding flag: &7" + af);
        for (PlotWorld plotworld : PS.get().getPlotWorldObjects()) {
            Flag flag = ((HashMap<String, Flag>) plotworld.DEFAULT_FLAGS.clone()).get(af.getKey());
            if (flag != null) {
                flag.setKey(af);
            }
        }
        if (PS.get().getAllPlotsRaw() != null) {
            for (final Plot plot : PS.get().getPlotsRaw()) {
                Flag flag = plot.getSettings().flags.get(af.getKey());
                if (flag != null) {
                    flag.setKey(af);
                }
            }
        }
        if ((getFlag(af.getKey()) == null) && flags.add(af)) {
            if (reserved) reserveFlag(af.getKey());
            return true;
        }
        return false;
    }

    public static Flag getSettingFlag(final String world, final PlotSettings settings, final String id) {
        Flag flag;
        if (settings.flags.size() == 0 || (flag = settings.flags.get(id)) == null) {
            PlotWorld plotworld = PS.get().getPlotWorld(world);
            if (plotworld == null) {
                return null;
            }
            if (plotworld.DEFAULT_FLAGS.size() == 0) {
                return null;
            }
            return ((HashMap<String, Flag>) plotworld.DEFAULT_FLAGS.clone()).get(id);
        }
        return flag;
    }
    
    public static boolean isBooleanFlag(final Plot plot, final String key, final boolean defaultValue) {
        final Flag flag = FlagManager.getPlotFlag(plot, key);
        if (flag == null) {
            return defaultValue;
        }
        final Object value = flag.getValue();
        if (value instanceof Boolean) {
            return (boolean) value;
        }
        return defaultValue;
    }

    /**
     * Get the value of a flag for a plot (respects flag defaults)
     * @param plot
     * @param flag
     * @return Flag
     */
    public static Flag getPlotFlag(final Plot plot, final String flag) {
        if (!plot.hasOwner()) {
            return null;
        }
        return getSettingFlag(plot.world, plot.getSettings(), flag);
    }

    public static boolean isPlotFlagTrue(final Plot plot, final String strFlag) {
        if (plot.owner == null) {
            return false;
        }
        final Flag flag = getPlotFlag(plot, strFlag);
        return !(flag == null || !((Boolean) flag.getValue()));
    }
    
    public static boolean isPlotFlagFalse(final Plot plot, final String strFlag) {
        if (plot.owner == null) {
            return false;
        }
        final Flag flag = getPlotFlag(plot, strFlag);
        if (flag == null || ((Boolean) flag.getValue())) {
            return false;
        }
        return false;
    }

    /**
     * Get the value of a flag for a plot (ignores flag defaults)
     * @param plot
     * @param flag
     * @return Flag
     */
    public static Flag getPlotFlagAbs(final Plot plot, final String flag) {
        return getSettingFlagAbs(plot.getSettings(), flag);
    }

    public static Flag getSettingFlagAbs(final PlotSettings settings, final String flag) {
        if ((settings.flags == null) || (settings.flags.size() == 0)) {
            return null;
        }
        return settings.flags.get(flag);
    }

    /**
     * Add a flag to a plot
     * @param plot
     * @param flag
     */
    public static boolean addPlotFlag(final Plot plot, final Flag flag) {
        final boolean result = EventUtil.manager.callFlagAdd(flag, plot);
        if (!result) {
            return false;
        }
        plot.getSettings().flags.put(flag.getKey(), flag);
        MainUtil.reEnterPlot(plot);
        DBFunc.setFlags(plot, plot.getSettings().flags.values());
        return true;
    }
    
    public static boolean addPlotFlagAbs(final Plot plot, final Flag flag) {
        final boolean result = EventUtil.manager.callFlagAdd(flag, plot);
        if (!result) {
            return false;
        }
        plot.getSettings().flags.put(flag.getKey(), flag);
        return true;
    }

    public static boolean addClusterFlag(final PlotCluster cluster, final Flag flag) {
        //TODO plot cluster flag event
        final Flag hasFlag = getSettingFlag(cluster.world, cluster.settings, flag.getKey());
        cluster.settings.flags.put(flag.getKey(), flag);
        DBFunc.setFlags(cluster, cluster.settings.flags.values());
        return true;
    }

    /**
     *
     * @param plot
     * @return set of flags
     */
    public static HashMap<String, Flag> getPlotFlags(final Plot plot) {
        if (!plot.hasOwner()) {
            return null;
        }
        return getSettingFlags(plot.world, plot.getSettings());
    }

    public static HashMap<String, Flag> getPlotFlags(final String world, final PlotSettings settings, final boolean ignorePluginflags) {
        HashMap<String, Flag> flags = new HashMap<>();

        PlotWorld plotWorld = PS.get().getPlotWorld(world);
        if (plotWorld != null && plotWorld.DEFAULT_FLAGS.size() != 0) {
            flags.putAll(plotWorld.DEFAULT_FLAGS);
        }

        if (ignorePluginflags) {
            for (final Map.Entry<String, Flag> flag : settings.flags.entrySet()) {
                if (isReserved(flag.getValue().getAbstractFlag().getKey())) continue;
                flags.put(flag.getKey(), flag.getValue());
            }
        } else {
            flags.putAll(settings.flags);
        }

        return flags;
    }

    public static HashMap<String, Flag> getSettingFlags(final String world, final PlotSettings settings) {
        return getPlotFlags(world, settings, false);
    }

    public static boolean removePlotFlag(final Plot plot, final String id) {
        Flag flag = plot.getSettings().flags.remove(id);
        if (flag == null) {
            return false;
        }
        final boolean result = EventUtil.manager.callFlagRemove(flag, plot);
        if (!result) {
            plot.getSettings().flags.put(id, flag);
            return false;
        }
        MainUtil.reEnterPlot(plot);
        DBFunc.setFlags(plot, plot.getSettings().flags.values());
        return true;
    }

    public static boolean removeClusterFlag(final PlotCluster cluster, final String id) {
        Flag flag = cluster.settings.flags.remove(id);
        if (flag == null) {
            return false;
        }
        final boolean result = EventUtil.manager.callFlagRemove(flag, cluster);
        if (!result) {
            cluster.settings.flags.put(id, flag);
            return false;
        }
        DBFunc.setFlags(cluster, cluster.settings.flags.values());
        return true;
    }

    public static void setPlotFlags(final Plot plot, final Set<Flag> flags) {
        if (flags != null && flags.size() != 0) {
            plot.getSettings().flags.clear();
            for (Flag flag : flags) {
                plot.getSettings().flags.put(flag.getKey(), flag);
            }
        }
        else if (plot.getSettings().flags.size() == 0) {
            return;
        }
        else {
            plot.getSettings().flags.clear();
        }
        MainUtil.reEnterPlot(plot);
        DBFunc.setFlags(plot, plot.getSettings().flags.values());
    }

    public static void setClusterFlags(final PlotCluster cluster, final Set<Flag> flags) {
        if (flags != null && flags.size() != 0) {
            cluster.settings.flags.clear();
            for (Flag flag : flags) {
                cluster.settings.flags.put(flag.getKey(), flag);
            }
        }
        else if (cluster.settings.flags.size() == 0) {
            return;
        }
        else {
            cluster.settings.flags.clear();
        }
        DBFunc.setFlags(cluster, cluster.settings.flags.values());
    }

    public static Flag[] removeFlag(final Flag[] flags, final String r) {
        final Flag[] f = new Flag[flags.length - 1];
        int index = 0;
        for (final Flag flag : flags) {
            if (!flag.getKey().equals(r)) {
                f[index++] = flag;
            }
        }
        return f;
    }

    public static Set<Flag> removeFlag(final Set<Flag> flags, final String r) {
        final HashSet<Flag> newflags = new HashSet<>();
        for (final Flag flag : flags) {
            if (!flag.getKey().equalsIgnoreCase(r)) {
                newflags.add(flag);
            }
        }
        return newflags;
    }

    /**
     * Get a list of registered AbstractFlag objects
     *
     * @return List (AbstractFlag)
     */
    public static HashSet<AbstractFlag> getFlags() {
        return flags;
    }

    /**
     * Get a list of registerd AbstragFlag objects based on player permissions
     *
     * @param player with permissions
     *
     * @return List (AbstractFlag)
     */
    public static List<AbstractFlag> getFlags(final PlotPlayer player) {
        final List<AbstractFlag> returnFlags = new ArrayList<>();
        for (final AbstractFlag flag : flags) {
            if (player.hasPermission("plots.set.flag." + flag.getKey().toLowerCase())) {
                returnFlags.add(flag);
            }
        }
        return returnFlags;
    }

    /**
     * Get an AbstractFlag by a string Returns null if flag does not exist
     *
     * @param string Flag Key
     *
     * @return AbstractFlag
     */
    public static AbstractFlag getFlag(final String string) {
        for (final AbstractFlag flag : flags) {
            if (flag.getKey().equalsIgnoreCase(string)) {
                return flag;
            }
        }
        return null;
    }

    /**
     * Get an AbstractFlag by a string
     *
     * @param string Flag Key
     * @param create If to create the flag if it does not exist
     *
     * @return AbstractFlag
     */
    public static AbstractFlag getFlag(final String string, final boolean create) {
        if ((getFlag(string) == null) && create) {
            final AbstractFlag flag = new AbstractFlag(string);
            return flag;
        }
        return getFlag(string);
    }

    /**
     * Remove a registered AbstractFlag
     *
     * @param flag Flag Key
     *
     * @return boolean Result of operation
     */
    public static boolean removeFlag(final AbstractFlag flag) {
        return flags.remove(flag);
    }

    public static HashMap<String, Flag> parseFlags(final List<String> flagstrings) {
        HashMap<String, Flag> map = new HashMap<String, Flag>();
        for (String key : flagstrings) {
            final String[] split;
            if (key.contains(";")) {
                split = key.split(";");
            }
            else {
                split = key.split(":");
            }
            Flag flag;
            if (split.length == 1) {
                flag = new Flag(getFlag(split[0], true), "");
            } else {
                flag = new Flag(getFlag(split[0], true), split[1]);
            }
            map.put(flag.getKey(), flag);
        }
        return map;
    }
}
