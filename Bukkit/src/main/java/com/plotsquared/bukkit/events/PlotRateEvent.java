package com.plotsquared.bukkit.events;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.Rating;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class PlotRateEvent extends PlotEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final PlotPlayer rater;
    private Rating rating;
    private boolean cancelled = false;

    public PlotRateEvent(PlotPlayer rater, Rating rating, Plot plot) {
        super(plot);
        this.rater = rater;
        this.rating = rating;
    }

    public static HandlerList getHandlerList() {
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

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
