package com.plotsquared.core.config;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.StringMan;

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
