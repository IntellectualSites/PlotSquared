package com.plotsquared.events;

import com.plotsquared.plot.Plot;
import org.jetbrains.annotations.NotNull;

public abstract class PlotEvent {

    private final Plot plot;
    private String name;

    public PlotEvent(Plot plot) {
        this.plot = plot;
    }

    /**
     * Obtain the plot involved in the event
     *
     * @return Plot
     */
    public final Plot getPlot() {
        return this.plot;
    }

    /**
     * Obtain the event's class name
     *
     * @return the event class name
     */
    @NotNull public String getEventName() {
        if (name == null) {
            name = getClass().getSimpleName();
        }
        return name;
    }

}
