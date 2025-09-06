package com.plotsquared.core.services.impl;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.persistence.repository.api.PlotRepository;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.services.api.PlotService;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlotDefaultService implements PlotService {

    private final PlotRepository repository;

    @Inject
    public PlotDefaultService(final PlotRepository repository) {
        this.repository = repository;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> swapPlots(final @NotNull Plot plot1, final @NotNull Plot plot2) {
        return CompletableFuture.completedFuture(this.repository.swapPlots(plot1, plot2));
    }

    @Override
    public void movePlot(final @NotNull Plot originalPlot, final @NotNull Plot newPlot) {
        this.repository.movePlots(originalPlot, newPlot);
    }

    @Override
    public void setOwner(final @NotNull Plot plot, final @NotNull UUID uuid) {
        this.repository.setOwner(plot, uuid);
    }

    @Override
    public void createPlotsAndData(final @NotNull List<Plot> plots, final Runnable whenDone) {
        this.repository.createPlotsAndData(plots);
        whenDone.run();
    }

    @Override
    public void createPlotSafe(final @NotNull Plot plot, final @NotNull Runnable success, final Runnable failure) {
        boolean created = this.repository.createPlotSafe(plot);
        if (created) {
            success.run();
        } else {
            failure.run();
        }
    }

    @Override
    public void createPlotAndSettings(final @NotNull Plot plot, final Runnable whenDone) {
        this.repository.createPlotAndSettings(plot);
        whenDone.run();
    }

    @Override
    public void delete(final @NotNull Plot plot) {
        this.repository.delete(plot);
    }

    @Override
    public void deleteRatings(final Plot plot) {
        this.repository.deleteRatings(plot);
    }

    @Override
    public HashMap<String, HashMap<PlotId, Plot>> getPlots() {
        return this.repository.getPlots();
    }

}
