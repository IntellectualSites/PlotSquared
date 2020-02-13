package com.github.intellectualsites.plotsquared.plot.config;

import com.github.intellectualsites.plotsquared.commands.CommandCaller;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

public interface Caption {

    String getTranslated();

    default String formatted() {
        return StringMan.replaceFromMap(getTranslated(), Captions.replacements);
    }

    default void send(CommandCaller caller, String... args) {
        send(caller, (Object[]) args);
    }

    default void send(CommandCaller caller, Object... args) {
        String msg = CaptionUtility.format(this, args);
        if (caller == null) {
            PlotSquared.log(msg);
        } else {
            caller.sendMessage(msg);
        }
    }

    boolean usePrefix();

}
