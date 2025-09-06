package com.plotsquared.core.services.api;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.PlotFlag;

public interface FlagService {

    void setFlag(Plot plot, PlotFlag<?, ?> flag);

    void removeFlag(Plot plot, PlotFlag<?, ?> flag);
}
