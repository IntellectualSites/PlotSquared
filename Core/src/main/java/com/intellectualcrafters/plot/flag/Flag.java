package com.intellectualcrafters.plot.flag;

public class Flag<V> {

    private String name;

    /**
     * Flag object used to store basic information for a Plot. Flags are a
     * key/value pair. For a flag to be usable by a player, you need to
     * register it with PlotSquared.
     *
     * @param name Flag name
     */
    public Flag(String name) {
        this.name = name;
    }

    public String valueToString(Object value) {
        return null;
    }

    @Override
    public String toString() {
        return "Flag { name='" + getName() + "'}";
    }

    public V parseValue(String value) {
        return null;
    }

    public String getValueDescription() {
        return "";
    }

    public final String getName() {
        return this.name;
    }
}
