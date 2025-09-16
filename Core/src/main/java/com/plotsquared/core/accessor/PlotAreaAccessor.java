package com.plotsquared.core.accessor;

import com.plotsquared.core.plot.PlotArea;


/**
 * An accessor to get the plot area.
 *
 * @since 7.5.7
 * @version 1.0.0
 * @author IntellectualSites
 * @author TheMeinerLP
 */
public interface PlotAreaAccessor {
    /**
     * Gets the plot world object for this plot<br>
     * - The generic PlotArea object can be casted to its respective class for more control (e.g. HybridPlotWorld)
     *
     * @return PlotArea
     */
    PlotArea getArea();
}
