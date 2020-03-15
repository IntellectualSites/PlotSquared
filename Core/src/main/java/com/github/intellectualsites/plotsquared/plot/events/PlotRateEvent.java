package com.github.intellectualsites.plotsquared.plot.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.Rating;
import org.jetbrains.annotations.Nullable;

public class PlotRateEvent extends PlotEvent implements CancellablePlotEvent {

    private final PlotPlayer rater;
    @Nullable private Rating rating;
    private Result eventResult;

    /**
     * PlotRateEvent: Called when a player rates a plot
     *
     * @param rater  The player rating the plot
     * @param rating The rating being given
     * @param plot   The plot being rated
     */
    public PlotRateEvent(PlotPlayer rater, @Nullable Rating rating, Plot plot) {
        super(plot);
        this.rater = rater;
        this.rating = rating;
    }

    public PlotPlayer getRater() {
        return this.rater;
    }

    @Nullable public Rating getRating() {
        return this.rating;
    }

    public void setRating(@Nullable Rating rating) {
        this.rating = rating;
    }

    @Override public Result getEventResult() {
        return eventResult;
    }

    @Override public void setEventResult(Result e) {
        this.eventResult = e;
    }
}
