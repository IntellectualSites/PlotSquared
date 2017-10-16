package com.plotsquared.bukkit.events;

import com.intellectualcrafters.plot.object.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerClaimPlotEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final Plot plot;
    private final boolean auto;
    private final int autoIndex;
    private boolean cancelled;

    /**
     * PlayerClaimPlotEvent: Called when a plot is claimed.
     *
     * @param player Player that claimed the plot
     * @param plot   Plot that was claimed
     */
    public PlayerClaimPlotEvent(Player player, Plot plot, boolean auto, int autoIndex) {
        super(player);
        this.plot = plot;
        this.auto = auto;
        this.autoIndex = autoIndex;
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

    /**
     * @return index if multiple plots were claimed together (eg. /plot auto 2,2 -> 0,1,2,3)
     */
    public int getAutoIndex() {
        return this.autoIndex;
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
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
