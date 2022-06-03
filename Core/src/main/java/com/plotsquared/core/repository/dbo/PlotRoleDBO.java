package com.plotsquared.core.repository.dbo;

import com.plotsquared.core.player.PlotRole;
import io.soabase.recordbuilder.core.RecordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

@RecordBuilder
public record PlotRoleDBO(
        @NonNull PlotDBO plot,
        @NonNull UUID userId,
        @NonNull PlotRole plotRole
) {

    public @NonNull Key key() {
        return new Key(this.plot(), this.userId());
    }

    public record Key(@NonNull PlotDBO plot, @NonNull UUID userId) {
    }
}
