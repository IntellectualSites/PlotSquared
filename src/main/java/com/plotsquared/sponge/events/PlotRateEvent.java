package com.plotsquared.sponge.events;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.Rating;

public class PlotRateEvent extends PlotEvent {
    private final PlotPlayer rater;
    private Rating rating;
    
    public PlotRateEvent(final PlotPlayer rater, final Rating rating, final Plot plot) {
        super(plot);
        this.rater = rater;
        this.rating = rating;
    }
    
    public PlotPlayer getRater() {
        return rater;
    }
    
    public void setRating(final Rating rating) {
        this.rating = rating;
    }
    
    public Rating getRating() {
        return rating;
    }
}
