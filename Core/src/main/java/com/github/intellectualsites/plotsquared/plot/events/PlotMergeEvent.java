package com.github.intellectualsites.plotsquared.plot.events;

import com.github.intellectualsites.plotsquared.plot.object.Direction;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when several plots are merged
 * {@inheritDoc}
 */
public final class PlotMergeEvent extends PlotPlayerEvent implements CancellablePlotEvent {

    @Getter private final String world;
    @Getter @Setter private Direction dir;
    @Getter @Setter private int max;
    private Result eventResult;
    @Getter private PlotPlayer player;

    /**
     * PlotMergeEvent: Called when plots are merged
     *
     * @param world  World in which the event occurred
     * @param plot   Plot that was merged
     * @param dir    The direction of the merge
     * @param max    Max merge size
     * @param player The player attempting the merge
     */
    public PlotMergeEvent(@NotNull final String world, @NotNull final Plot plot,
        @NotNull final Direction dir, final int max, PlotPlayer player) {
        super(player, plot);
        this.world = world;
        this.dir = dir;
        this.max = max;
        this.player = player;
    }


    @Override public Result getEventResult() {
        return eventResult;
    }

    @Override public void setEventResult(Result e) {
        this.eventResult = e;
    }
}
