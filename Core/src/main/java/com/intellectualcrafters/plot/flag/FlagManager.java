package com.intellectualcrafters.plot.flag;

import com.google.common.collect.Sets;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotSettings;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.Permissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Flag Manager Utility
 *
 */
public class FlagManager {


    private static final HashSet<Flag<?>> reserved = Sets.newHashSet(Flags.ANALYSIS, Flags.DONE);

    /**
     * Reserve a flag so that it cannot be set by players
     * @param flag
     */
    public static void reserveFlag(Flag<?> flag) {
        reserved.add(flag);
    }

    /**
     * Get if a flag is reserved
     * @param flag
     * @return
     */
    public static boolean isReserved(Flag<?> flag) {
        return reserved.contains(flag);
    }

    /**
     * Get the reserved flags
     * @return
     */
    public static Set<Flag<?>> getReservedFlags() {
        return Collections.unmodifiableSet(reserved);
    }

    /**
     * Unreserve a flag
     * @param flag
     */
    public static void unreserveFlag(Flag<?> flag) {
        reserved.remove(flag);
    }

    public static String toString(HashMap<Flag<?>, Object> flags) {
        StringBuilder flag_string = new StringBuilder();
        int i = 0;
        Flag<?> flag;
        for (Map.Entry<Flag<?>, Object> entry : flags.entrySet()) {
            flag = entry.getKey();
            if (i != 0) {
                flag_string.append(",");
            }
           flag_string.append(flag.getName() + ":" + flag.valueToString(entry.getValue()).replaceAll(":", "¯").replaceAll(",", "´"));
            i++;
        }
        return flag_string.toString();
    }

    public static <V> V getSettingFlag(PlotArea area, PlotSettings settings, Flag<V> id) {
        Object value;
        if (settings.flags.isEmpty() || ((value = settings.flags.get(id)) == null)) {
            if (area == null || area.DEFAULT_FLAGS.isEmpty()) {
                return null;
            }
            return (V) area.DEFAULT_FLAGS.get(id);
        }
        return (V) value;
    }

    /**
     * Returns the raw flag<br>
     *  - Faster
     *  - You should not modify the flag
     * @param plot
     * @param flag
     * @return
     */
    public static <V> V getPlotFlagRaw(Plot plot, Flag<V> flag) {
        if (plot.owner == null) {
            return null;
        }
        return getSettingFlag(plot.getArea(), plot.getSettings(), flag);
    }

    /**
     * Add a flag to a plot
     * @param origin
     * @param flag
     * @param value
     */
    public static <V> boolean addPlotFlag(Plot origin, Flag<V> flag, Object value) {
        boolean result = EventUtil.manager.callFlagAdd(flag, origin);
        if (!result) {
            return false;
        }
        for (Plot plot : origin.getConnectedPlots()) {
            plot.getFlags().put(flag, value);
            plot.reEnter(); //TODO fix this so FlagTest will run during compile
            DBFunc.setFlags(plot, plot.getFlags());
        }
        return true;
    }

    public static <V> boolean addClusterFlag(PlotCluster cluster, Flag<V> flag, V value) {
        getSettingFlag(cluster.area, cluster.settings, flag);
        cluster.settings.flags.put(flag, value);
        DBFunc.setFlags(cluster, cluster.settings.flags);
        return true;
    }

    /**
     *
     * @param plot
     * @return set of flags
     */
    public static HashMap<Flag<?>, Object> getPlotFlags(Plot plot) {
        if (!plot.hasOwner()) {
            return null;
        }
        return getSettingFlags(plot.getArea(), plot.getSettings());
    }

    public static HashMap<Flag<?>, Object> getPlotFlags(PlotArea area, PlotSettings settings, boolean ignorePluginflags) {
        HashMap<Flag<?>, Object> flags = new HashMap<>();
        if (area != null && !area.DEFAULT_FLAGS.isEmpty()) {
            flags.putAll(area.DEFAULT_FLAGS);
        }
        if (ignorePluginflags) {
            for (Map.Entry<Flag<?>, Object> flag : settings.flags.entrySet()) {
                if (isReserved(flag.getKey())) {
                    continue;
                }
                flags.put(flag.getKey(), flag.getValue());
            }
        } else {
            flags.putAll(settings.flags);
        }

        return flags;
    }

    public static HashMap<Flag<?>, Object> getSettingFlags(PlotArea area, PlotSettings settings) {
        return getPlotFlags(area, settings, false);
    }

    public static boolean removePlotFlag(Plot plot, Flag<?> id) {
        Object value = plot.getFlags().remove(id);
        if (value == null) {
            return false;
        }
        boolean result = EventUtil.manager.callFlagRemove(id, plot, value);
        if (!result) {
            plot.getFlags().put(id, value);
            return false;
        }
        plot.reEnter();
        DBFunc.setFlags(plot, plot.getFlags());
        return true;
    }

    public static boolean removeClusterFlag(PlotCluster cluster, Flag id) {
        Object object = cluster.settings.flags.remove(id);
        if (object == null) {
            return false;
        }
        boolean result = EventUtil.manager.callFlagRemove(id, object, cluster);
        if (!result) {
            cluster.settings.flags.put(id, object);
            return false;
        }
        DBFunc.setFlags(cluster, cluster.settings.flags);
        return true;
    }

    public static void setPlotFlags(Plot origin, HashMap<Flag<?>, Object> flags) {
        for (Plot plot : origin.getConnectedPlots()) {
            if (flags != null && !flags.isEmpty()) {
                plot.getFlags().clear();
                for (Map.Entry<Flag<?>, Object> flag : flags.entrySet()) {
                    plot.getFlags().put(flag.getKey(), flag.getValue());
                }
            } else if (plot.getFlags().isEmpty()) {
                return;
            } else {
                plot.getFlags().clear();
            }
            plot.reEnter();
            DBFunc.setFlags(plot, plot.getFlags());
        }
    }

    public static void setClusterFlags(PlotCluster cluster, Set<Flag> flags) {
        if (flags != null && !flags.isEmpty()) {
            cluster.settings.flags.clear();
            for (Flag flag : flags) {
                cluster.settings.flags.put(flag, flag);
            }
        } else if (cluster.settings.flags.isEmpty()) {
            return;
        } else {
            cluster.settings.flags.clear();
        }
        DBFunc.setFlags(cluster, cluster.settings.flags);
    }

    /**
     * Get a list of registered {@link Flag} objects based on player permissions
     *
     * @param player with permissions
     *
     * @return List (AbstractFlag)
     */
    public static List<Flag> getFlags(PlotPlayer player) {
        List<Flag> returnFlags = new ArrayList<>();
        for (Flag flag : Flags.getFlags()) {
            if (Permissions.hasPermission(player, "plots.set.flag." + flag.getName().toLowerCase())) {
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
    public static Flag<?> getFlag(String string) {
        for (Flag flag : Flags.getFlags()) {
            if (flag.getName().equalsIgnoreCase(string)) {
                if (isReserved(flag)) {
                    return null;
                }
                return flag;
            }
        }
        return null;
    }

    public static HashMap<Flag<?>, Object> parseFlags(List<String> flagstrings) {
        HashMap<Flag<?>, Object> map = new HashMap<>();

        for (String key : flagstrings) {
            String[] split;
            if (key.contains(";")) {
                split = key.split(";");
            } else {
                split = key.split(":");
            }
            Flag<?> flag = getFlag(split[0]);
            Object value = flag.parseValue(split[1]);
            map.put(flag, value);
        }

        return map;
    }
}
