package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class ServerPlotFlag extends BooleanFlag<ServerPlotFlag> {

    public static final ServerPlotFlag SERVER_PLOT_TRUE = new ServerPlotFlag(true);
    public static final ServerPlotFlag SERVER_PLOT_FALSE = new ServerPlotFlag(false);

    private ServerPlotFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_SERVER_PLOT);
    }

    @Override protected ServerPlotFlag flagOf(@NotNull Boolean value) {
        return value ? SERVER_PLOT_TRUE : SERVER_PLOT_FALSE;
    }

}
