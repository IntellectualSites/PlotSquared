package com.intellectualcrafters.plot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FlagManager {

    private static ArrayList<AbstractFlag> flags;

    public static boolean addFlag(AbstractFlag flag) {
        if (getFlag(flag.getKey()) != null) {
            return false;
        }
        return flags.add(flag);
    }

    public static List<AbstractFlag> getFlags() {
        return flags;
    }

    public static AbstractFlag getFlag(String string) {
        for (AbstractFlag flag : flags) {
            if (flag.getKey().equalsIgnoreCase(string)) {
                return flag;
            }
        }
        return null;
    }

    public static AbstractFlag getFlag(String string, boolean create) {
        if ((getFlag(string) == null) && create) {
            AbstractFlag flag = new AbstractFlag(string);
            return flag;
        }
        return getFlag(string);
    }

    public static List<AbstractFlag> getPlotFlags(Plot plot) {
        Set<Flag> plotFlags = plot.settings.getFlags();
        List<AbstractFlag> flags = new ArrayList<>();
        for (Flag flag : plotFlags) {
            flags.add(flag.getAbstractFlag());
        }
        return flags;
    }
}
