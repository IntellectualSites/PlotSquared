package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.util.StringComparison;
import lombok.Getter;

public abstract class Flag<V> implements StringComparison.StringComparable {

    @Getter private final Captions typeCaption;
    private final String name;
    private boolean reserved = false;

    public Flag(Captions typeCaption, String name) {
        this.typeCaption = typeCaption;
        this.name = name;
    }

    /**
     * Flag object used to store basic information for a Plot. Flags are a
     * key/value pair. For a flag to be usable by a player, you need to
     * register it with PlotSquared.
     *
     * @param name the flag name
     */
    public Flag(String name) {
        this(null, name);
    }

    public Flag<V> reserve() {
        this.reserved = true;
        return this;
    }

    public boolean isReserved() {
        return this.reserved;
    }

    public void unreserve() {
        this.reserved = false;
    }

    public void register() {
        Flags.registerFlag(this);
    }

    public abstract String valueToString(Object value);

    @Override public final String toString() {
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

    @Override public String getComparableString() {
        return getName();
    }

    public String getCategoryCaption() {
        return this.typeCaption == null ?
            getClass().getSimpleName() :
            this.typeCaption.getTranslated();
    }

}
