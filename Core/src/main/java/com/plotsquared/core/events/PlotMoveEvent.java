package com.plotsquared.core.events;

import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;

import java.util.Objects;

/**
 * Called when a {@link PlotPlayer} attempts to move a {@link Plot} to another {@link Plot}.
 * The Event-Result {@link Result#FORCE} does have no effect on the outcome. Only supported results are {@link Result#DENY} and
 * {@link Result#ACCEPT}.
 * <br>
 * <ul>
 * <li>{@link #getPlotPlayer()} is the initiator of the move action (most likely the command executor)</li>
 * <li>{@link #getPlot()} is the plot to be moved</li>
 * <li>{@link #destination()} is the plot, where the plot will be moved to.</li>
 * </ul>
 *
 * @since TODO
 */
public class PlotMoveEvent extends PlotPlayerEvent implements CancellablePlotEvent {

    private final Plot destination;
    private Result result = Result.ACCEPT;

    public PlotMoveEvent(final PlotPlayer<?> initiator, final Plot plot, final Plot destination) {
        super(initiator, plot);
        this.destination = destination;
    }

    public Plot destination() {
        return destination;
    }

    @Override
    public Result getEventResult() {
        return result;
    }

    @Override
    public void setEventResult(Result eventResult) {
        this.result = Objects.requireNonNull(eventResult);
    }

}
