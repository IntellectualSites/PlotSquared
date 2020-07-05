package com.plotsquared.core.configuration.caption;

import com.plotsquared.core.configuration.Caption;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link com.plotsquared.core.configuration.Caption} that can be identified by a key
 */
public interface KeyedCaption extends Caption {

    /**
     * Get the key that identifies this caption
     *
     * @return Caption key
     */
    @NotNull String getKey();

}
