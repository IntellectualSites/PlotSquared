package com.plotsquared.core.events;

import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotId;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * Called when a player swaps {@link #getPlot() their Plot} with {@link #target() another Plot}.
 *
 * @see com.plotsquared.core.command.Swap
 * @since TODO
 */
public class PlotSwapEvent extends PlotPlayerEvent implements CancellablePlotEvent {

    private Plot target;
    private boolean sendErrorMessage = true;
    private Result result = Result.ACCEPT;

    public PlotSwapEvent(final PlotPlayer<?> plotPlayer, final Plot plot, final Plot target) {
        super(plotPlayer, plot);
        this.target = target;
    }

    /**
     * Set the plot which should be swapped with {@link #getPlot()}.
     *
     * @param target The target swapping plot.
     * @since TODO
     */
    public void setTarget(@NonNull final Plot target) {
        this.target = Objects.requireNonNull(target);
    }

    /**
     * Set the new destination based off their X and Y coordinates. Calls {@link #setTarget(Plot)} while using the
     * {@link com.plotsquared.core.plot.PlotArea} provided by the current {@link #target()}.
     * <p>
     * <b>Note:</b> the coordinates are not minecraft world coordinates, but the underlying {@link PlotId}s coordinates.
     *
     * @param x The X coordinate of the {@link PlotId}
     * @param y The Y coordinate of the {@link PlotId}
     * @since TODO
     */
    public void setTarget(final int x, final int y) {
        this.target = Objects.requireNonNull(this.target.getArea()).getPlot(PlotId.of(x, y));
    }

    /**
     * Set whether to send a generic message to the user ({@code Swap was cancelled by an external plugin}). If set to {@code
     * false}, make sure to send a message to the player yourself to avoid confusion.
     *
     * @param sendErrorMessage {@code true} if PlotSquared should send a generic error message to the player.
     * @since TODO
     */
    public void setSendErrorMessage(final boolean sendErrorMessage) {
        this.sendErrorMessage = sendErrorMessage;
    }

    /**
     * The target plot to swap with.
     *
     * @return The plot.
     * @since TODO
     */
    public Plot target() {
        return target;
    }

    /**
     * @return {@code true} if PlotSquared should send a generic error message to the player.
     * @since TODO
     */
    public boolean sendErrorMessage() {
        return sendErrorMessage;
    }

    /**
     * The plot issuing the swap with {@link #target()}.
     *
     * @return The plot.
     */
    @Override
    public Plot getPlot() {
        return super.getPlot(); // delegate for overriding the documentation
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @Nullable Result getEventResult() {
        return this.result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEventResult(@Nullable final Result eventResult) {
        this.result = eventResult;
    }

}
