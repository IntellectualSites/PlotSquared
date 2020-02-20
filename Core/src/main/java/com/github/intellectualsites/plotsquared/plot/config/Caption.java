package com.github.intellectualsites.plotsquared.plot.config;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

public interface Caption {

    String getTranslated();

    default String formatted() {
        return StringMan.replaceFromMap(getTranslated(), Captions.replacements);
    }

    default void send(PlotPlayer caller, String... args) {
        send(caller, (Object[]) args);
    }

    default void send(PlotPlayer caller, Object... args) {
        String msg = CaptionUtility.format(caller, this, args);
        if (caller == null) {
            PlotSquared.log(msg);
        } else {
            caller.sendMessage(msg);
        }
    }

    boolean usePrefix();

}
