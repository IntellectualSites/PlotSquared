package com.plotsquared.core.events;

import com.plotsquared.core.player.PlotPlayer;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Called every time after PlotSquared calculated a players plot limit based on their permission.
 * <p>
 * May be used to grant a player more plots based on another rank or bought feature.
 *
 * @since TODO
 */
public class PlayerPlotLimitEvent {

    private final PlotPlayer<?> player;

    private int limit;

    public PlayerPlotLimitEvent(@NonNull final PlotPlayer<?> player, @NonNegative final int limit) {
        this.player = player;
        this.limit = limit;
    }

    /**
     * Overrides the previously calculated or set plot limit for {@link #player()}.
     *
     * @param limit The amount of plots a player may claim. Must be {@code 0} or greater.
     * @since TODO
     */
    public void setLimit(@NonNegative final int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Player plot limit must be greater or equal 0");
        }
        this.limit = limit;
    }

    /**
     * Returns the previous set limit, if none was overridden before this event handler the default limit based on the players
     * permissions node is returned.
     *
     * @return The currently defined plot limit of this player.
     * @since TODO
     */
    public @NonNegative int limit() {
        return limit;
    }

    /**
     * The player for which the limit is queried.
     *
     * @return the player.
     * @since TODO
     */
    public @NonNull PlotPlayer<?> player() {
        return player;
    }

}
