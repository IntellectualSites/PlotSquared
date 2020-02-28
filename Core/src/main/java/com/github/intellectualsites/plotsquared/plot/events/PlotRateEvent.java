package com.github.intellectualsites.plotsquared.plot.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.Rating;

public class PlotRateEvent extends PlotEvent implements CancellablePlotEvent {

    private final PlotPlayer rater;
    private Rating rating;
    private Result eventResult;

    public PlotRateEvent(PlotPlayer rater, Rating rating, Plot plot) {
        super(plot);
        this.rater = rater;
        this.rating = rating;
    }

    public PlotPlayer getRater() {
        return this.rater;
    }

    public Rating getRating() {
        return this.rating;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    @Override
    public Result getEventResult() {
        return eventResult;
    }

    @Override
    public int getEventResultRaw() {
        return eventResult.getValue();
    }

    @Override
    public void setEventResult(Result e) {
        this.eventResult = e;
    }
}
