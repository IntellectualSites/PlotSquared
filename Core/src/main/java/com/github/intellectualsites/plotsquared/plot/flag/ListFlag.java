package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Plot;

import java.util.Collection;

public abstract class ListFlag<V extends Collection<?>> extends Flag<V> {

    public ListFlag(Captions typeCaption, String name) {
        super(typeCaption, name);
    }

    public ListFlag(String name) {
        super(name);
    }

    public boolean contains(Plot plot, Object value) {
        V existing = plot.getFlag(this, null);
        return existing != null && existing.contains(value);
    }
}
