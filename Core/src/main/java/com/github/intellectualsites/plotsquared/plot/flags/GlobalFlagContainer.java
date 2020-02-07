package com.github.intellectualsites.plotsquared.plot.flags;

import com.github.intellectualsites.plotsquared.plot.flags.implementations.ExplosionFlag;
import lombok.Getter;

import javax.annotation.Nonnull;

public final class GlobalFlagContainer extends FlagContainer {

    @Getter private static final GlobalFlagContainer instance = new GlobalFlagContainer();

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

}
