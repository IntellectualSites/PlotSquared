package com.plotsquared.core.plot;

import com.plotsquared.core.repository.PlotRepository;
import com.plotsquared.core.repository.PlotRoleRepository;
import com.plotsquared.core.repository.PlotSettingsRepository;
import com.plotsquared.core.repository.dbo.PlotSettingsDBOBuilder;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class PlotService {

    private static final String PLOT_SETTINGS_DEFAULT_POSITION = "default";

    private final PlotRepository plotRepository;
    private final PlotSettingsRepository plotSettingsRepository;
    private final PlotRoleRepository plotRoleRepository;

    @Inject
    public PlotService(
        final @NonNull PlotRepository plotRepository,
        final @NonNull PlotSettingsRepository plotSettingsRepository,
        final @NonNull PlotRoleRepository plotRoleRepository
    ) {
        this.plotRepository = plotRepository;
        this.plotSettingsRepository = plotSettingsRepository;
        this.plotRoleRepository = plotRoleRepository;
    }

    /**
     * Returns a list containing all the plots in the given {@code plotArea}.
     *
     * @param plotArea the area
     * @return all plots in the area
     */
    public @NonNull Collection<Plot> getPlotsInArea(final @NonNull PlotArea plotArea) {
        return this.plotRepository.getPlotsInArea(plotArea.getId())
                .map(plotDBO -> {
                    final var settings = this.plotSettingsRepository.findById(plotDBO)
                            .orElseGet(() -> PlotSettingsDBOBuilder.builder()
                                    .plot(plotDBO)
                                    .position(PLOT_SETTINGS_DEFAULT_POSITION)
                                    .build()
                            );
                    return plotDBO.toPlot(
                            plotArea,
                            settings,
                            this.plotRoleRepository.findAllFor(plotDBO),
                            List.of()
                    );
                }).collect(Collectors.toList());
    }
}
