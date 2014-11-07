package com.intellectualcrafters.plot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;

public class FlagManager {

    // TODO add some flags
    // - Plot clear interval
    // - Mob cap
    // - customized plot composition
    // - greeting / leaving message
    // OR in the flag command, allow users to set worldguard flags.

    private static ArrayList<AbstractFlag> flags = new ArrayList<AbstractFlag>();

    /**
     * Register an AbstractFlag with PlotSquared
     *
     * @param flag
     * @return
     */
    public static boolean addFlag(final AbstractFlag flag) {
        if (getFlag(flag.getKey()) != null) {
            return false;
        }
        return flags.add(flag);
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

    public static Flag[] removeFlag(final Set<Flag> flags, final String r) {
        final Flag[] flagArray = new Flag[flags.size() - 1];
        int index = 0;
        for (final Flag flag : flags) {
            if (!flag.getKey().equals(r)) {
                flagArray[index++] = flag;
            }
        }
        return flagArray;
    }

    /**
     * Get a list of registered AbstractFlag objects
     *
     * @return List (AbstractFlag)
     */
    public static List<AbstractFlag> getFlags() {
        return flags;
    }

    /**
     * Get a list of registerd AbstragFlag objects based on player permissions
     *
     * @param player
     *            with permissions
     * @return List (AbstractFlag)
     */
    public static List<AbstractFlag> getFlags(final Player player) {
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
     * @param string
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
     * @param string
     * @param create
     *            If to create the flag if it does not exist
     * @return AbstractFlag
     */
    public static AbstractFlag getFlag(final String string, final boolean create) {
        if ((getFlag(string) == null) && create) {
            final AbstractFlag flag = new AbstractFlag(string);
            addFlag(flag);
            return flag;
        }
        return getFlag(string);
    }

    /**
     * Remove a registered AbstractFlag
     *
     * @param flag
     * @return boolean Result of operation
     */
    public static boolean removeFlag(final AbstractFlag flag) {
        return flags.remove(flag);
    }

    public static Flag[] parseFlags(final List<String> flagstrings) {
        final Flag[] flags = new Flag[flagstrings.size()];
        for (int i = 0; i < flagstrings.size(); i++) {
            final String[] split = flagstrings.get(i).split(";");
            if (split.length == 1) {
                flags[i] = new Flag(getFlag(split[0], true), "");
            }
            else {
                flags[i] = new Flag(getFlag(split[0], true), split[1]);
            }
        }
        return flags;
    }

    /**
     * Get the flags for a plot
     *
     * @param plot
     * @return List (AbstractFlag)
     */
    public static List<AbstractFlag> getPlotFlags(final Plot plot) {
        final Set<Flag> plotFlags = plot.settings.getFlags();
        final List<AbstractFlag> flags = new ArrayList<>();
        for (final Flag flag : plotFlags) {
            flags.add(flag.getAbstractFlag());
        }
        return flags;
    }
}
