package com.plotsquared.core.events;

/**
 * PlotSquared event with {@link Result} to cancel, force, or allow.
 */
public interface CancellablePlotEvent {

    Result getEventResult();

    void setEventResult(Result eventResult);

    default int getEventResultRaw() {
        return getEventResult().getValue();
    }

}
