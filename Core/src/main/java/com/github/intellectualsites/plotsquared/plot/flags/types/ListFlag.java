package com.github.intellectualsites.plotsquared.plot.flags.types;

import com.github.intellectualsites.plotsquared.plot.config.Caption;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ListFlag<V, F extends PlotFlag<List<V>, F>> extends PlotFlag<List<V>, F> {

    public ListFlag(final List<V> valueList, final Captions category, final Caption description) {
        super(Collections.unmodifiableList(valueList), category, description);
    }

    @Override public F merge(@NotNull List<V> newValue) {
        final List<V> mergedList = new ArrayList<>();
        mergedList.addAll(getValue());
        mergedList.addAll(newValue);
        return this.flagOf(mergedList);
    }

    @Override public String toString() {
        return StringMan.join(this.getValue(), ",");
    }
}
