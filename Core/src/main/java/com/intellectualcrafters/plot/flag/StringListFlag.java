package com.intellectualcrafters.plot.flag;

import java.util.List;

public class StringListFlag extends ListFlag<List<String>> {

    /**
     * Flag object used to store basic information for a Plot. Flags are a
     * key/value pair. For a flag to be usable by a player, you need to
     * register it with PlotSquared.
     *

     * @param name Flag name
     */
    public StringListFlag(String name) {
        super(name);
    }
}
