package com.github.intellectualsites.plotsquared.nukkit.events;

import cn.nukkit.event.HandlerList;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.Rating;

public class PlotRateEvent extends PlotEvent {

    private static final HandlerList handlers = new HandlerList();
    private final PlotPlayer rater;
    private Rating rating;

    public PlotRateEvent(PlotPlayer rater, Rating rating, Plot plot) {
        super(plot);
        this.rater = rater;
        this.rating = rating;
    }

    public static HandlerList getHandlers() {
        return handlers;
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

}
