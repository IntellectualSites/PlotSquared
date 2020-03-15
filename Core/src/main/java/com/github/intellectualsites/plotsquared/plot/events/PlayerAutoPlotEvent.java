package com.github.intellectualsites.plotsquared.plot.events;

import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

public class PlayerAutoPlotEvent extends PlotEvent implements CancellablePlotEvent {

    private Result eventResult;
    private String schematic;
    @Getter private PlotPlayer player;
    @Getter private PlotArea plotArea;
    @Getter @Setter private int size_x;
    @Getter @Setter private int size_z;

    /**
     * PlayerAutoPlotEvent: called when a player attempts to auto claim a plot.
     *
     * @param player    The player attempting to auto claim
     * @param plotArea  The applicable plot area
     * @param schematic The schematic defined or null
     * @param size_x    The size of the auto area
     * @param size_z    The size of the auto area
     */
    public PlayerAutoPlotEvent(PlotPlayer player, PlotArea plotArea, @Nullable String schematic,
        int size_x, int size_z) {
        super(null);
        this.player = player;
        this.plotArea = plotArea;
        this.schematic = schematic;
        this.size_x = size_x;
        this.size_z = size_z;
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
    public void setSchematic(String schematic) {
        this.schematic = schematic;
    }

    @Override public Result getEventResult() {
        return eventResult;
    }

    @Override public void setEventResult(Result e) {
        this.eventResult = e;
    }
}
