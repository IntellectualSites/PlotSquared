package com.plotsquared.core.services.api;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotId;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlotService {

    CompletableFuture<Boolean> swapPlots(Plot plot1, Plot plot2);

    void movePlot(Plot originalPlot, Plot newPlot);

    void setOwner(Plot plot, UUID uuid);

    void createPlotsAndData(List<Plot> plots, Runnable whenDone);

    void createPlotSafe(
            final Plot plot, final Runnable success,
            final Runnable failure
    );

    void createPlotAndSettings(Plot plot, Runnable whenDone);

    void delete(Plot plot);

    @Deprecated(forRemoval = true, since = "8.0.0")
    void deleteRatings(Plot plot);

    HashMap<String, HashMap<PlotId, Plot>> getPlots();

}
