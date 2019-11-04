package com.github.intellectualsites.plotsquared.configuration.file;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

/**
 * Various settings for controlling the input and output of a {@link
 * YamlConfiguration}.
 */
public class YamlConfigurationOptions extends FileConfigurationOptions {

    YamlConfigurationOptions(YamlConfiguration configuration) {
        super(configuration);
    }

    @Override public YamlConfiguration configuration() {
        return (YamlConfiguration) super.configuration();
    }

    @Override public YamlConfigurationOptions copyDefaults(boolean value) {
        super.copyDefaults(value);
        return this;
    }

    @Override public YamlConfigurationOptions header(String value) {
        super.header(value);
        return this;
    }

    @Override public YamlConfigurationOptions copyHeader(boolean value) {
        super.copyHeader(value);
        return this;
    }

    /**
     * Gets how much spaces should be used to indent each line.
     *
     * <p>The minimum value this may be is 2, and the maximum is 9.
     *
     * @return How much to indent by
     */
    int indent() {
        return 2;
    }

}
