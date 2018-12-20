package com.plotsquared.nukkit.events;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerEvent;
import com.intellectualcrafters.plot.object.Plot;

public class PlayerClaimPlotEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Plot plot;
    private final boolean auto;
    private boolean cancelled;

    /**
     * PlayerClaimPlotEvent: Called when a plot is claimed.
     *
     * @param player Player that claimed the plot
     * @param plot   Plot that was claimed
     */
    public PlayerClaimPlotEvent(Player player, Plot plot, boolean auto) {
        this.player = player;
        this.plot = plot;
        this.auto = auto;
    }

    public static HandlerList getHandlers() {
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
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
