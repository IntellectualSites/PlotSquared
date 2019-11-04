package com.github.intellectualsites.plotsquared.configuration;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

/**
 * Various settings for controlling the input and output of a {@link
 * MemoryConfiguration}.
 */
public class MemoryConfigurationOptions extends ConfigurationOptions {
    protected MemoryConfigurationOptions(MemoryConfiguration configuration) {
        super(configuration);
    }

    @Override public MemoryConfiguration configuration() {
        return (MemoryConfiguration) super.configuration();
    }

    @Override public MemoryConfigurationOptions copyDefaults(boolean value) {
        super.copyDefaults(value);
        return this;
    }

}
