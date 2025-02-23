package com.plotsquared.core.util.comparator;

import com.plotsquared.core.plot.Plot;
import org.jetbrains.annotations.ApiStatus;

import java.util.Comparator;

/**
 * Sort plots by {@link Plot#temp} (being the auto increment id in database) in natural order for {@code temp > 0}.
 * For {@code temp < 1} sort by {@link Plot#hashCode()}
 */
@ApiStatus.Internal
public class PlotByCreationDateComparator implements Comparator<Plot> {

    @ApiStatus.Internal
    public static final Comparator<Plot> INSTANCE = new PlotByCreationDateComparator();

    private PlotByCreationDateComparator() {
    }

    @Override
    @SuppressWarnings("deprecation") // Plot#temp
    public int compare(final Plot first, final Plot second) {
        if (first.temp > 0 && second.temp > 0) {
            return Integer.compare(first.temp, second.temp);
        }
        // second is implicitly `< 1` (due to previous condition)
        if (first.temp > 0) {
            return 1;
        }
        // sort dangling plots (temp < 1) by their hashcode
        return Integer.compare(first.hashCode(), second.hashCode());
    }

}
