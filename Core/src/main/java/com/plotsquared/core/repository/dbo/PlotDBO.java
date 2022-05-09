package com.plotsquared.core.repository.dbo;

import com.plotsquared.core.location.BlockLoc;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.flag.PlotFlag;
import io.soabase.recordbuilder.core.RecordBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@RecordBuilder
public record PlotDBO(
        @Nullable Integer id,
        int plotIdX,
        int plotIdZ,
        @NonNull String world,
        @NonNull UUID owner,
        @NonNull Instant timestamp
) {

    public @NonNull Plot toPlot(
            final @NonNull PlotArea plotArea,
            final @NonNull PlotSettingsDBO plotSettingsDBO,
            final @NonNull Collection<PlotRoleDBO> plotRoles,
            final @NonNull Collection<PlotFlag<?, ?>> flags
    ) {
        final PlotId plotId = PlotId.of(this.plotIdX(), this.plotIdZ());
        final int id = Objects.requireNonNull(id(), "id may not be null");

        final Set<UUID> trusted = new HashSet<>();
        final Set<UUID> members = new HashSet<>();
        final Set<UUID> denied = new HashSet<>();
        for (final PlotRoleDBO plotRole : plotRoles) {
            switch (plotRole.plotRole()) {
                case TRUSTED -> trusted.add(plotRole.userId());
                case HELPER -> members.add(plotRole.userId());
                case DENIED -> denied.add(plotRole.userId());
            }
        }

        return new Plot(
                plotId,
                this.owner(),
                Collections.unmodifiableSet(trusted),
                Collections.unmodifiableSet(members),
                Collections.unmodifiableSet(denied),
                plotSettingsDBO.alias(),
                BlockLoc.fromString(plotSettingsDBO.position()),
                flags,
                plotArea,
                plotSettingsDBO.unwrapMerged(),
                this.timestamp().toEpochMilli(),
                id
        );
    }
}
