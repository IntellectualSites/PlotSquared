package com.plotsquared.sponge.events;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;

public class PlayerTeleportToPlotEvent extends PlayerEvent implements Cancellable {
    
    private final Location from;
    private final Plot plot;
    private boolean cancelled;
    
    /**
     * PlayerTeleportToPlotEvent: Called when a player teleports to a plot.
     *
     * @param player That was teleported
     * @param from Start location
     * @param plot Plot to which the player was teleported
     */
    public PlayerTeleportToPlotEvent(Player player, Location from, Plot plot) {
        super(player);
        this.from = from;
        this.plot = plot;
    }
    
    /**
     * Get the from location.
     *
     * @return Location
     */
    public Location getFrom() {
        return this.from;
    }
    
    /**
     * Get the plot involved.
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
    
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
