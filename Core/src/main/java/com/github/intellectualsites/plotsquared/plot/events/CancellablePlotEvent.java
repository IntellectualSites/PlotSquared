package com.github.intellectualsites.plotsquared.plot.events;

public interface CancellablePlotEvent {

    PlotEvent.Result getEventResult();

    int getEventResultRaw();

    void setEventResult(PlotEvent.Result eventResult);

}
