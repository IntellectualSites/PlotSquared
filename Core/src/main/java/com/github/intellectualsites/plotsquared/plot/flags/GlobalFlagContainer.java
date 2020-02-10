package com.github.intellectualsites.plotsquared.plot.flags;

import com.github.intellectualsites.plotsquared.plot.flags.implementations.ExplosionFlag;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class GlobalFlagContainer extends FlagContainer {

    @Getter private static final GlobalFlagContainer instance = new GlobalFlagContainer();

    private final Map<String, Class<?>> stringClassMap = new HashMap<>();

    @Override public PlotFlag<?> getFlagErased(Class<?> flagClass) {
        final PlotFlag<?> flag = super.getFlagErased(flagClass);
        if (flag != null) {
            return flag;
        } else {
            throw new IllegalStateException(String.format("Unrecognized flag '%s'. All flag types"
                + " must be present in the global flag container.", flagClass.getSimpleName()));
        }
    }

    @Nonnull @Override public <T> PlotFlag<T> getFlag(Class<? extends PlotFlag<T>> flagClass) {
        final PlotFlag<?> flag = super.getFlag(flagClass);
        if (flag != null) {
            return (PlotFlag<T>) flag;
        } else {
            throw new IllegalStateException(String.format("Unrecognized flag '%s'. All flag types"
                + " must be present in the global flag container.", flagClass.getSimpleName()));
        }
    }

    private GlobalFlagContainer() {
        super(null);
        // Register all default flags here
        this.addFlag(new ExplosionFlag());
    }

    @Override public void addFlag(PlotFlag<?> flag) {
        super.addFlag(flag);
        this.stringClassMap.put(flag.getName().toLowerCase(Locale.ENGLISH), flag.getClass());
    }

    public Class<?> getFlagClassFromString(final String name) {
        return this.stringClassMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    public PlotFlag<?> getFlagFromString(final String name) {
        final Class<?> flagClass = this.getFlagClassFromString(name);
        if (flagClass == null) {
            return null;
        }
        return getFlagErased(flagClass);
    }

}
