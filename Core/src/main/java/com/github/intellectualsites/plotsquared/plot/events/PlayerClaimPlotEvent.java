package com.github.intellectualsites.plotsquared.plot.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;

import javax.annotation.Nullable;

public class PlayerClaimPlotEvent extends PlotPlayerEvent implements CancellablePlotEvent {

    private final boolean auto;
    private Result eventResult;
    private String schematic;

    /**
     * PlayerClaimPlotEvent: Called when a plot is claimed.
     *
     * @param player Player that claimed the plot
     * @param plot   Plot that was claimed
     */
    public PlayerClaimPlotEvent(PlotPlayer player, Plot plot, boolean auto,
        @Nullable String schematic) {
        super(player, plot);
        this.auto = auto;
        this.schematic = schematic;
    }

    /**
     * @return true if it was an automated claim, else false
     */
    public boolean wasAuto() {
        return this.auto;
    }

    /**
     * Obtain the schematic string as used by the {@link com.github.intellectualsites.plotsquared.plot.commands.Claim} command or null.
     *
     * @return schematic string
     */
    @Nullable public String getSchematic() {
        return this.schematic;
    }

    /**
     * Set the schematic string used in the claim.
     */
    @Nullable public void setSchematic(String schematic) {
        this.schematic = schematic;
    }

    @Override public Result getEventResult() {
        return eventResult;
    }

    @Override public void setEventResult(Result e) {
        this.eventResult = e;
    }
}
