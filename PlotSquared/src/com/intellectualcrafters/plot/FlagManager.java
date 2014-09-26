package com.intellectualcrafters.plot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FlagManager {

    private static ArrayList<AbstractFlag> flags;

    /**
     * Register an AbstractFlag with PlotSquared
     * @param flag
     * @return
     */
    public static boolean addFlag(AbstractFlag flag) {
        if (getFlag(flag.getKey()) != null) {
            return false;
        }
        return flags.add(flag);
    }

    /**
     * Get a list of registered AbstractFlag objects
     * @return List (AbstractFlag)
     */
    public static List<AbstractFlag> getFlags() {
        return flags;
    }

    /**
     * Get an AbstractFlag by a string
     * Returns null if flag does not exist
     * @param string
     * @return AbstractFlag
     */
    public static AbstractFlag getFlag(String string) {
        for (AbstractFlag flag : flags) {
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
     *      If to create the flag if it does not exist
     * @return AbstractFlag
     */
    public static AbstractFlag getFlag(String string, boolean create) {
        if ((getFlag(string) == null) && create) {
            AbstractFlag flag = new AbstractFlag(string);
            addFlag(flag);
            return flag;
        }
        return getFlag(string);
    }
    
    /**
     * Remove a registered AbstractFlag
     * @param flag
     * @return boolean
     *      Result of operation
     */
    public static boolean removeFlag(AbstractFlag flag) {
        return flags.remove(flag);
    }

    /**
     * Get the flags for a plot
     * @param plot
     * @return List (AbstractFlag)
     */
    public static List<AbstractFlag> getPlotFlags(Plot plot) {
        Set<Flag> plotFlags = plot.settings.getFlags();
        List<AbstractFlag> flags = new ArrayList<>();
        for (Flag flag : plotFlags) {
            flags.add(flag.getAbstractFlag());
        }
        return flags;
    }
}
