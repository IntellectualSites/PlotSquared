package com.github.intellectualsites.plotsquared.sponge.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import org.spongepowered.api.entity.living.player.Player;

public class PlayerLeavePlotEvent extends PlayerEvent {

    private final Plot plot;

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
        return plot;
    }
}
