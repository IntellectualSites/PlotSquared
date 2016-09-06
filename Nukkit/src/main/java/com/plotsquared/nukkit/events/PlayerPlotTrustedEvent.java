package com.plotsquared.nukkit.events;

import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;
import com.intellectualcrafters.plot.object.Plot;
import java.util.UUID;

/**


 */
public class PlayerPlotTrustedEvent extends PlotEvent {

    private static final HandlerList handlers = new HandlerList();
    private final Player initiator;
    private final boolean added;
    private final UUID player;

    /**
     * PlayerPlotTrustedEvent: Called when a plot trusted user is added/removed
     *
     * @param initiator Player that initiated the event
     * @param plot      Plot in which the event occurred
     * @param player    Player that was added/removed from the trusted list
     * @param added     true of the player was added, false if the player was removed
     */
    public PlayerPlotTrustedEvent(Player initiator, Plot plot, UUID player, boolean added) {
        super(plot);
        this.initiator = initiator;
        this.added = added;
        this.player = player;
    }

    public static HandlerList getHandlers() {
        return handlers;
    }

    /**
     * If a player was added
     *
     * @return boolean
     */
    public boolean wasAdded() {
        return this.added;
    }

    /**
     * The UUID added/removed
     *
     * @return UUID
     */
    public UUID getPlayer() {
        return this.player;
    }

    /**
     * The player initiating the action
     *
     * @return Player
     */
    public Player getInitiator() {
        return this.initiator;
    }
}
