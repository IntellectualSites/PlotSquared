package com.github.intellectualsites.plotsquared.bukkit.events;

import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

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
        super(player);
        this.from = from;
        this.plot = plot;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override public HandlerList getHandlers() {
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

    @Override public boolean isCancelled() {
        return this.cancelled;
    }

    @Override public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
