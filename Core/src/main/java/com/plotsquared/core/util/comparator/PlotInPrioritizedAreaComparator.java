package com.plotsquared.core.util.comparator;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;

import javax.annotation.Nullable;
import java.util.Comparator;

public class PlotInPrioritizedAreaComparator implements Comparator<Plot> {

    private final PlotArea priorityArea;

    public PlotInPrioritizedAreaComparator(@Nullable final PlotArea area) {
        this.priorityArea = area;
    }

    @Override
    public int compare(final Plot first, final Plot second) {
        if (this.priorityArea == null) {
            return 0; // no defined priority? don't sort
        }
        if (this.priorityArea.equals(first.getArea())) {
            return -1;
        }
        if (this.priorityArea.equals(second.getArea())) {
            return 1;
        }
        return 0; // same area, don't sort
    }

}
