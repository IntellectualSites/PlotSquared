package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.flags.FlagContainer;
import com.github.intellectualsites.plotsquared.plot.flags.InternalFlag;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.PlotSettings;
import com.github.intellectualsites.plotsquared.plot.util.EventUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Flag Manager Utility.
 */
public class FlagManager {

    public static <V> Optional<V> getPlotFlag(Plot plot, Flag<V> key) {
        V value = FlagManager.getPlotFlagRaw(plot, key);
        return Optional.ofNullable(value);
    }

    /**
     * Reserve a flag so that it cannot be set by players.
     *
     * @param flag the flag to reserve
     * @return false if the flag was already reserved, otherwise true
     */
    public static boolean reserveFlag(Flag<?> flag) {
        if (flag.isReserved()) {
            return false;
        }
        flag.reserve();
        return true;
    }

    /**
     * Check if a flag is reserved.
     *
     * @param flag the flag to check
     * @return true if the flag is reserved, false otherwise
     */
    public static boolean isReserved(Flag<?> flag) {
        return flag.isReserved();
    }

    /**
     * Get an immutable set of reserved flags.
     *
     * @return a set of reserved flags
     */
    public static Set<Flag<?>> getReservedFlags() {
        ImmutableSet.Builder<Flag<?>> reserved = ImmutableSet.builder();
        for (Flag flag : Flags.getFlags()) {
            if (flag.isReserved()) {
                reserved.add(flag);
            }
        }
        return reserved.build();
    }

    /**
     * Unreserve a flag.
     *
     * @param flag the flag to unreserve
     * @return true if the flag was unreserved
     */
    public static boolean unreserveFlag(Flag<?> flag) {
        if (flag.isReserved()) {
            flag.unreserve();
            return true;
        }
        return false;
    }

    public static String toString(HashMap<Flag<?>, Object> flags) {
        StringBuilder flag_string = new StringBuilder();
        int i = 0;
        for (Map.Entry<Flag<?>, Object> entry : flags.entrySet()) {
            try {
                Flag flag = entry.getKey();
                if (i != 0) {
                    flag_string.append(',');
                }
                flag_string.append(
                    flag.getName() + ':' + flag.valueToString(entry.getValue()).replaceAll(":", "¯")
                        .replaceAll(",", "´"));
                i++;
            } catch (Exception e) {
                PlotSquared
                    .debug("Failed to parse flag: " + entry.getKey() + "->" + entry.getValue());
                e.printStackTrace();
            }
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
     * - Faster
     * - You should not modify the flag
     *
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

    public static <V> boolean addPlotFlag(Plot origin, PlotFlag<V, ?> flag, Object value) {
        // TODO: Implement this
        return true;
    }

    /**
     * Add a flag to a plot.
     *
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

    /**
     * Returns a map of the {@link Flag}s and their values for the specified plot.
     *
     * @param plot the plot
     * @return a map of the flags and values for the plot, returns an empty map for unowned plots
     */
    public static Map<Flag<?>, Object> getPlotFlags(Plot plot) {
        if (!plot.hasOwner()) {
            return Collections.emptyMap();
        }
        return getSettingFlags(plot.getArea(), plot.getSettings());
    }

    public static Collection<PlotFlag<?, ?>> getPlotFlags(final Plot plot, final boolean ignorePluginFlags) {
        final Map<Class<?>, PlotFlag<?, ?>> flags = new HashMap<>();
        if (plot.getArea() != null && !plot.getArea().getFlagContainer().getFlagMap().isEmpty()) {
            final Map<Class<?>, PlotFlag<?, ?>> flagMap = plot.getArea().getFlagContainer().getFlagMap();
            flags.putAll(flagMap);
        }
        final Map<Class<?>, PlotFlag<?, ?>> flagMap = plot.getFlagContainer().getFlagMap();
        if (ignorePluginFlags) {
            for (final PlotFlag<?, ?> flag : flagMap.values()) {
                if (flag instanceof InternalFlag) {
                    continue;
                }
                flags.put(flag.getClass(), flag);
            }
        } else {
            flags.putAll(flagMap);
        }
        return flagMap.values();
    }

    public static Map<Flag<?>, Object> getSettingFlags(PlotArea area, PlotSettings settings) {
        return getPlotFlags(area, settings, false);
    }

    public static boolean removePlotFlag(Plot origin, PlotFlag<?, ?> flag) {
        // TODO: Implement
        return true;
    }

    /**
     * Removes a flag from a certain plot.
     *
     * @param origin the plot to remove the flag from
     * @param id     the flag to remove
     * @return true if the plot contained the flag and was removed successfully
     */
    public static boolean removePlotFlag(Plot origin, Flag<?> id) {
        for (Plot plot : origin.getConnectedPlots()) {
            Object value = plot.getFlags().remove(id);
            if (value == null) {
                return false;
            }
            if (plot == origin) {
                boolean result = EventUtil.manager.callFlagRemove(id, plot, value);
                if (!result) {
                    plot.getFlags().put(id, value);
                    return false;
                }
            }
            plot.reEnter();
            DBFunc.setFlags(plot, plot.getFlags());
        }
        return true;
    }

    public static void setPlotFlags(Plot origin, FlagContainer flagContainer) {
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

    /**
     * Get a list of registered {@link Flag} objects based on player permissions.
     *
     * @param player the player
     * @return a list of flags the specified player can use
     */
    public static List<Flag> getFlags(PlotPlayer player) {
        List<Flag> returnFlags = Flags.getFlags().stream().filter(flag -> Permissions
            .hasPermission(player, "plots.set.flag." + flag.getName().toLowerCase()))
            .collect(Collectors.toList());
        return returnFlags;
    }

    /**
     * Get a {@link Flag} specified by the specified {@code String}.
     *
     * @param string the flag name
     * @return the {@code Flag} object defined by the {@code String}
     */
    public static Flag<?> getFlag(String string) {
        return Flags.getFlag(string);
    }

    public static Flag<?> getFlag(String string, boolean ignoreReserved) {
        Flag<?> flag = Flags.getFlag(string);
        if (!ignoreReserved && flag != null && flag.isReserved()) {
            return null;
        }
        return flag;
    }

    public static Map<Flag<?>, Object> parseFlags(List<String> flagStrings) {
        HashMap<Flag<?>, Object> map = new HashMap<>();

        for (String key : flagStrings) {
            String[] split;
            if (key.contains(";")) {
                split = key.split(";");
            } else {
                split = key.split(":");
            }
            Flag<?> flag = getOrCreateFlag(split[0]);
            Object value = flag.parseValue(split[1]);
            map.put(flag, value);
        }

        return map;
    }
}
