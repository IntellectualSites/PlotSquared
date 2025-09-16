package com.plotsquared.core.accessor;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * An accessor to get the world name.
 *
 * @since 7.5.7
 * @version 1.0.0
 * @author IntellectualSites
 * @author TheMeinerLP
 */
public interface WorldNameAccessor {

    /**
     * Get the name of the world for this object
     *
     * @return World name
     */
    @NonNull
    String getWorldName();
}
