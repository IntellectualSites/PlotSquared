package com.intellectualcrafters.plot.flag;

import java.util.Collection;

public abstract class ListFlag<V extends Collection> extends Flag<V> {

    /**
     * Flag object used to store basic information for a Plot. Flags are a
     * key/value pair. For a flag to be usable by a player, you need to
     * register it with PlotSquared.
     *

     * @param name Flag name
     */
    public ListFlag(String name) {
        super(name);
    }
}
