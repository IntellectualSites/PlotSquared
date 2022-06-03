package com.plotsquared.core.repository.dbo;

import io.soabase.recordbuilder.core.RecordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@RecordBuilder
public record PlotSettingsDBO(
        @NonNull PlotDBO plot,
        @Nullable String alias,
        Integer merged,
        @NonNull String position
) {

    /**
     * Unwraps {@link #merged()} into an array indicating whether the plot is merged in
     * any given cardinal direction. The indices of the array correspond to the ordinals of
     * {@link com.plotsquared.core.location.Direction}.
     *
     * @return unwrapped merged status
     */
    public boolean@NonNull [] unwrapMerged() {
        final boolean[] merged = new boolean[4];
        for (int i = 0; i < 4; i++) {
            merged[3 - i] = (this.merged() & 1 << i) != 0;
        }
        return merged;
    }
}
