package com.github.intellectualsites.plotsquared.plot.flags;

import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode(of = "flagMap") public class FlagContainer {

    private final FlagContainer parentContainer;
    private final Map<Class<?>, PlotFlag<?, ?>> flagMap = new HashMap<>();

    public FlagContainer(@Nullable final FlagContainer parentContainer) {
        this.parentContainer = parentContainer;
    }

    public FlagContainer getParentContainer() {
        return this.parentContainer;
    }

    protected Map<Class<?>, PlotFlag<?, ?>> getInternalPlotFlagMap() {
        return this.flagMap;
    }

    public Map<Class<?>, PlotFlag<?, ?>> getFlagMap() {
        return ImmutableMap.<Class<?>, PlotFlag<?, ?>>builder().putAll(this.flagMap).build();
    }

    public void addFlag(final PlotFlag<?, ? extends PlotFlag<?, ?>> flag) {
        this.flagMap.put(flag.getClass(), flag);
    }

    public void addAll(final Collection<PlotFlag<?, ?>> flags) {
        for (final PlotFlag<?, ?> flag : flags) {
            this.addFlag(flag);
        }
    }

    public Collection<PlotFlag<?, ?>> getRecognizedPlotFlags() {
        return this.getHighestClassContainer().getFlagMap().values();
    }

    public final FlagContainer getHighestClassContainer() {
        if (this.getParentContainer() != null) {
            return this.getParentContainer();
        }
        return this;
    }

    public PlotFlag<?, ?> getFlagErased(Class<?> flagClass) {
        final PlotFlag<?, ?> flag = this.flagMap.get(flagClass);
        if (flag != null) {
            return flag;
        } else {
            if (getParentContainer() != null) {
                return getParentContainer().getFlagErased(flagClass);
            }
        }
        return null;
    }

    public <T> PlotFlag<T, ?> getFlag(final Class<? extends PlotFlag<T, ?>> flagClass) {
        final PlotFlag<?, ?> flag = this.flagMap.get(flagClass);
        if (flag != null) {
            return (PlotFlag<T, ?>) flag;
        } else {
            if (getParentContainer() != null) {
                return getParentContainer().getFlag(flagClass);
            }
        }
        return null;
    }

}
