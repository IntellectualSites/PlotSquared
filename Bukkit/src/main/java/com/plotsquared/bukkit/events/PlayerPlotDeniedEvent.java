package com.plotsquared.bukkit.events;

import com.intellectualcrafters.plot.object.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class PlayerPlotDeniedEvent extends PlotEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Player initiator;
    private final boolean added;
    private final UUID player;

    /**
     * PlayerPlotDeniedEvent: Called when the denied UUID list is modified for a plot.
     *
     * @param initiator Player that initiated the event
     * @param plot      Plot in which the event occurred
     * @param player    Player that was denied/un-denied
     * @param added     true of add to deny list, false if removed
     */
    public PlayerPlotDeniedEvent(Player initiator, Plot plot, UUID player, boolean added) {
        super(plot);
        this.initiator = initiator;
        this.added = added;
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * If a user was added.
     *
     * @return boolean
     */
    public boolean wasAdded() {
        return this.added;
    }

    /**
     * The player added/removed.
     *
     * @return UUID
     */
    public UUID getPlayer() {
        return this.player;
    }

    /**
     * The player initiating the action.
     *
     * @return Player
     */
    public Player getInitiator() {
        return this.initiator;
    }

    @Override public HandlerList getHandlers() {
        return handlers;
    }
}
