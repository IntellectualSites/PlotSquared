package com.github.intellectualsites.plotsquared.plot.flags.types;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class ListFlag<V> extends PlotFlag<List<V>> {

    public ListFlag(final Captions category, final Captions description) {
        super(new ArrayList<>(), category, description);
    }

    @Override public List<V> merge(@NotNull List<V> oldValue, @NotNull List<V> newValue) {
        final List<V> mergedList = new ArrayList<>();
        mergedList.addAll(oldValue);
        mergedList.addAll(newValue);
        return this.setFlagValue(mergedList);
    }

    @Override public String toString() {
        return StringMan.join(this.getValue(), ",");
    }
}
