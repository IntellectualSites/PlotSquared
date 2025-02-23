package com.plotsquared.core.util.comparator;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.util.MathMan;

import java.util.Comparator;

/**
 * Sort plots by their {@link DoneFlag} in reverse numeric natural order. (more recent "finished plots" first)
 * <br>
 * Non-finished plots last, unsorted.
 */
public class PlotByDoneComparator implements Comparator<Plot> {

    public static final PlotByDoneComparator INSTANCE = new PlotByDoneComparator();

    private PlotByDoneComparator() {
    }

    @Override
    public int compare(final Plot first, final Plot second) {
        String firstDone = first.getFlag(DoneFlag.class);
        String lastDone = second.getFlag(DoneFlag.class);
        if (MathMan.isInteger(firstDone)) {
            if (MathMan.isInteger(lastDone)) {
                return Integer.parseInt(lastDone) - Integer.parseInt(firstDone);
            }
            return -1; // only "first" is finished, so sort "second" after "first"
        }
        return 0; // neither is finished
    }

}
