package com.plotsquared.player;

import java.util.UUID;

public interface OfflinePlotPlayer {

    /**
     * Gets the {@code UUID} of this player
     *
     * @return the player {@link UUID}
     */
    UUID getUUID();

    /**
     * Gets the time in milliseconds when the player was last seen online.
     *
     * @return the time in milliseconds when last online
     * @deprecated This method may be inconsistent across platforms. The javadoc may be wrong depending on which platform is used.
     */
    @SuppressWarnings("DeprecatedIsStillUsed") @Deprecated long getLastPlayed();

    /**
     * Checks if this player is online.
     *
     * @return {@code true} if this player is online
     */
    boolean isOnline();

    /**
     * Gets the name of this player.
     *
     * @return the player name
     */
    String getName();
}
