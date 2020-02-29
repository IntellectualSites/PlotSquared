package com.github.intellectualsites.plotsquared.plot.events;

public interface CancellablePlotEvent {

    PlotEvent.Result getEventResult();

    default int getEventResultRaw() {
        return getEventResult().getValue();
    }

    void setEventResult(PlotEvent.Result eventResult);

}
