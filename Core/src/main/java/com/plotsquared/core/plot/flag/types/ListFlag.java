package com.plotsquared.core.plot.flag.types;

import com.plotsquared.core.config.Caption;
import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.util.StringMan;
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
