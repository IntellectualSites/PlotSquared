package com.github.intellectualsites.plotsquared.util;

import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DoneFlag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Entity related general utility methods
 */
@UtilityClass public final class EntityUtil {

    private static int capNumeral(@NonNull final String flagName) {
        int i;
        switch (flagName) {
            case "mob-cap":
                i = 3;
                break;
            case "hostile-cap":
                i = 2;
                break;
            case "animal-cap":
                i = 1;
                break;
            case "vehicle-cap":
                i = 4;
                break;
            case "misc-cap":
                i = 5;
                break;
            case "entity-cap":
            default:
                i = 0;
        }
        return i;
    }

    public static boolean checkEntity(Plot plot, PlotFlag<Integer, ?>... flags) {
        if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
            return true;
        }
        int[] mobs = null;
        for (PlotFlag<Integer, ?> flag : flags) {
            final int i = capNumeral(flag.getName());
            int cap = plot.getFlag(flag);
            if (cap == Integer.MAX_VALUE) {
                continue;
            }
            if (cap == 0) {
                return true;
            }
            if (mobs == null) {
                mobs = plot.countEntities();
            }
            if (mobs[i] >= cap) {
                plot.setMeta("EntityCount", mobs);
                plot.setMeta("EntityCountTime", System.currentTimeMillis());
                return true;
            }
        }
        if (mobs != null) {
            for (PlotFlag<Integer, ?> flag : flags) {
                final int i = capNumeral(flag.getName());
                mobs[i]++;
            }
            plot.setMeta("EntityCount", mobs);
            plot.setMeta("EntityCountTime", System.currentTimeMillis());
        }
        return false;
    }

}
