package com.intellectualcrafters.plot.flag;

import com.intellectualcrafters.plot.object.Plot;

public abstract class Flag<V> {

    private final String name;

    /**
     * Flag object used to store basic information for a Plot. Flags are a
     * key/value pair. For a flag to be usable by a player, you need to
     * register it with PlotSquared.
     *
     * @param name the flag name
     */
    public Flag(String name) {
        this.name = name;
    }

    public abstract String valueToString(Object value);

    @Override
    public final String toString() {
        return "Flag { name='" + getName() + "'}";
    }

    public abstract V parseValue(String value);

    public abstract String getValueDescription();

    public final String getName() {
        return this.name;
    }

    public boolean isSet(Plot plot) {
        return FlagManager.getPlotFlagRaw(plot, this) != null;
    }
}
