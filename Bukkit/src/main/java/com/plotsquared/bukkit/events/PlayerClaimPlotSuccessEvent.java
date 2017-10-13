package com.plotsquared.bukkit.events;

import com.intellectualcrafters.plot.object.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerClaimPlotSuccessEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Plot plot;
    private final boolean auto;
    /**
     * PlayerClaimPlotEvent: Called after a plot is claimed.
     *
     * @param player Player that claimed the plot
     * @param plot   Plot that was claimed
     */
    public PlayerClaimPlotSuccessEvent(Player player, Plot plot, boolean auto) {
        super(player);
        this.plot = plot;
        this.auto = auto;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the plot involved
     *
     * @return Plot
     */
    public Plot getPlot() {
        return this.plot;
    }

    /**
     * @return true if it was an automated claim, else false
     */
    public boolean wasAuto() {
        return this.auto;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
