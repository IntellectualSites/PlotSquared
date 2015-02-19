package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;

public class MainUtil {
    // TODO messages / permission stuff
    
    /**
     * Send a message to the console
     *
     * @param c message
     */
    public static void sendConsoleSenderMessage(final C c) {
        PlotSquared.MAIN_IMP.sendConsoleSenderMessage(c.s());
    }
}
