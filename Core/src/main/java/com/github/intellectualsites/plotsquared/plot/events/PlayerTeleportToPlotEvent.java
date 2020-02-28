package com.github.intellectualsites.plotsquared.plot.events;

import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;

/**
 * Called when a player teleports to a plot
 */
public class PlayerTeleportToPlotEvent extends PlotPlayerEvent implements CancellablePlotEvent {

    private final Location from;
    private Result eventResult;

    /**
     * PlayerTeleportToPlotEvent: Called when a player teleports to a plot
     *
     * @param player That was teleported
     * @param from   Start location
     * @param plot   Plot to which the player was teleported
     */
    public PlayerTeleportToPlotEvent(PlotPlayer player, Location from, Plot plot) {
        super(player, plot);
        this.from = from;
    }

    /**
     * Get the from location
     *
     * @return Location
     */
    public Location getFrom() {
        return this.from;
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
