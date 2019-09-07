package com.plotsquared.nukkit.events;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerEvent;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;

/**
 * Called when a player teleports to a plot
 */
public class PlayerTeleportToPlotEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Location from;
    private final Plot plot;
    private boolean cancelled;

    /**
     * PlayerTeleportToPlotEvent: Called when a player teleports to a plot
     *
     * @param player That was teleported
     * @param from   Start location
     * @param plot   Plot to which the player was teleported
     */
    public PlayerTeleportToPlotEvent(Player player, Location from, Plot plot) {
        this.player = player;
        this.from = from;
        this.plot = plot;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }


    /**
     * Get the from location
     *
     * @return Location
     */
    public Location getFrom() {
        return this.from;
    }

    /**
     * Get the plot involved
     *
     * @return Plot
     */
    public Plot getPlot() {
        return this.plot;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
}
