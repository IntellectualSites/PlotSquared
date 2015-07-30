package com.plotsquared.sponge.events;

import org.spongepowered.api.entity.player.Player;

import com.intellectualcrafters.plot.object.Plot;

public class PlayerLeavePlotEvent extends PlayerEvent {

    private final Plot plot;
    private boolean cancelled;
    
    /**
     * PlayerLeavePlotEvent: Called when a player leaves a plot
     *
     * @param player Player that left the plot
     * @param plot   Plot that was left
     */
    public PlayerLeavePlotEvent(final Player player, final Plot plot) {
        super(player);
        this.plot = plot;
    }
    
    /**
     * Get the plot involved
     *
     * @return Plot
     */
    public Plot getPlot() {
        return this.plot;
    }
 }