package com.github.intellectualsites.plotsquared.plot.flags;

import com.github.intellectualsites.plotsquared.plot.flags.implementations.DenyExitFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.ExplosionFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.FlightFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MusicFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.UntrustedVisitFlag;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class GlobalFlagContainer extends FlagContainer {

    @Getter private static final GlobalFlagContainer instance = new GlobalFlagContainer();
    private static Map<String, Class<?>> stringClassMap = new HashMap<>();

    private GlobalFlagContainer() {
        super(null, (flag, type) -> {
            if (type == PlotFlagUpdateType.FLAG_ADDED) {
                stringClassMap.put(flag.getName().toLowerCase(Locale.ENGLISH), flag.getClass());
            }
        });
        // Register all default flags here
        this.addFlag(ExplosionFlag.EXPLOSION_FALSE);
        this.addFlag(MusicFlag.MUSIC_FLAG_NONE);
        this.addFlag(FlightFlag.FLIGHT_FLAG_FALSE);
        this.addFlag(UntrustedVisitFlag.UNTRUSTED_VISIT_FLAG_TRUE);
        this.addFlag(DenyExitFlag.DENY_EXIT_FLAG_TRUE);
    }

    @Override public PlotFlag<?, ?> getFlagErased(Class<?> flagClass) {
        final PlotFlag<?, ?> flag = super.getFlagErased(flagClass);
        if (flag != null) {
            return flag;
        } else {
            throw new IllegalStateException(String.format("Unrecognized flag '%s'. All flag types"
                + " must be present in the global flag container.", flagClass.getSimpleName()));
        }
    }

    @Nonnull @Override
    public <V, T extends PlotFlag<V, ?>> T getFlag(Class<? extends T> flagClass) {
        final PlotFlag<?, ?> flag = super.getFlag(flagClass);
        if (flag != null) {
            return castUnsafe(flag);
        } else {
            throw new IllegalStateException(String.format("Unrecognized flag '%s'. All flag types"
                + " must be present in the global flag container.", flagClass.getSimpleName()));
        }
    }

    public Class<?> getFlagClassFromString(final String name) {
        return stringClassMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    public PlotFlag<?, ?> getFlagFromString(final String name) {
        final Class<?> flagClass = this.getFlagClassFromString(name);
        if (flagClass == null) {
            return null;
        }
        return getFlagErased(flagClass);
    }

}
