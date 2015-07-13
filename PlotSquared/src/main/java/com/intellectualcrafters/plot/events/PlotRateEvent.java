package com.intellectualcrafters.plot.events;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;

import org.bukkit.event.HandlerList;

/**
 * Created 2015-07-13 for PlotSquaredGit
 *
 * @author Citymonstret
 */
public class PlotRateEvent extends PlotEvent {

    private static HandlerList handlers = new HandlerList();
    private final PlotPlayer rater;
    private int rating;

    public PlotRateEvent(final PlotPlayer rater, final int rating, final Plot plot) {
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

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getRating() {
        return this.rating;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
