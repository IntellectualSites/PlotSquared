package com.github.intellectualsites.plotsquared.plot.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when several merged plots are unlinked
 * {@inheritDoc}
 */
public final class PlotUnlinkEvent extends PlotEvent implements CancellablePlotEvent {

    @Getter private final PlotArea area;
    @Getter @Setter boolean createRoad;
    @Getter @Setter boolean createSign;
    @Getter REASON reason;
    private Result eventResult = Result.ACCEPT;

    /**
     * PlotUnlinkEvent: Called when a mega plot is unlinked
     *
     * @param area       The applicable plot area
     * @param plot       The plot being unlinked from
     * @param createRoad Whether to regenerate the road
     * @param createSign Whether to regenerate signs
     * @param reason     The {@link REASON} for the unlink
     */
    public PlotUnlinkEvent(@NotNull final PlotArea area, Plot plot, boolean createRoad,
        boolean createSign, REASON reason) {
        super(plot);
        this.area = area;
        this.createRoad = createRoad;
        this.createSign = createSign;
        this.reason = reason;
    }

    @Override public Result getEventResult() {
        return eventResult;
    }

    @Override public void setEventResult(Result e) {
        this.eventResult = e;
    }

    public enum REASON {
        NEW_OWNER, PLAYER_COMMAND, CLEAR, DELETE, EXPIRE_DELETE
    }
}
