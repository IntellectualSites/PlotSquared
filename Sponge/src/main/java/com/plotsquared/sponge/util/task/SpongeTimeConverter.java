package com.plotsquared.sponge.util.task;

import com.plotsquared.core.util.task.TaskTime;
import org.checkerframework.checker.index.qual.NonNegative;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.Ticks;

public class SpongeTimeConverter implements TaskTime.TimeConverter {

    @Override
    public @NonNegative long msToTicks(@NonNegative final long ms) {
        return 0; // TODO
    }

    @Override
    public @NonNegative long ticksToMs(@NonNegative final long ticks) {
        return 0; // TODO
    }

}
