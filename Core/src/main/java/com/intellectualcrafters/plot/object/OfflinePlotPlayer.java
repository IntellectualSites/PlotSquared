package com.intellectualcrafters.plot.object;

import java.util.UUID;

public interface OfflinePlotPlayer {

    /**
     * Get the {@code UUID} of this player
     * @return the player {@link UUID}
     */
    UUID getUUID();

    /**
     * Get the time in milliseconds when the player was last seen online.
     * @return the time in milliseconds when last online
     */
    long getLastPlayed();

    /**
     * Checks if this player is online.
     * @return true if this player is online
     */
    boolean isOnline();

    /**
     * Get the name of this player.
     * @return the player name
     */
    String getName();
}
