package com.plotsquared.core.plot.flag;

/**
 * These are flags used in PlotSquared and PlotSquared
 * add-ons that can be used to associate information
 * with the plot, without users being able to access the
 * information.
 * <p>
 * These flags are not user assignable, nor do they
 * show up in `/plot info`, `/plot flag list`, etc.
 * <p>
 * PlotSquared add-ons should ignore these flags
 * when outputting flag information to users. An example
 * of such a scenario would be the flag listing in Dynmap.
 */
public interface InternalFlag {
}
