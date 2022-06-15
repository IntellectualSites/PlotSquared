/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.configuration.file;

import com.plotsquared.core.configuration.Configuration;
import com.plotsquared.core.configuration.MemoryConfiguration;
import com.plotsquared.core.configuration.MemoryConfigurationOptions;

/**
 * Various settings for controlling the input and output of a {@link
 * FileConfiguration}.
 */
public class FileConfigurationOptions extends MemoryConfigurationOptions {

    private String header = null;
    private boolean copyHeader = true;

    protected FileConfigurationOptions(MemoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public FileConfiguration configuration() {
        return (FileConfiguration) super.configuration();
    }

    @Override
    public FileConfigurationOptions copyDefaults(boolean value) {
        super.copyDefaults(value);
        return this;
    }

    /**
     * Gets the header that will be applied to the top of the saved output.
     *
     * <p>This header will be commented out and applied directly at the top of
     * the generated output of the {@link FileConfiguration}. It is not
     * required to include a newline at the end of the header as it will
     * automatically be applied, but you may include one if you wish for extra
     * spacing.
     *
     * <p>{@code null} is a valid value which will indicate that no header]
     * is to be applied. The default value is {@code null}.
     *
     * @return Header
     */
    public String header() {
        return header;
    }

    /**
     * Sets the header that will be applied to the top of the saved output.
     *
     * <p>This header will be commented out and applied directly at the top of
     * the generated output of the {@link FileConfiguration}. It is not
     * required to include a newline at the end of the header as it will
     * automatically be applied, but you may include one if you wish for extra
     * spacing.
     *
     * <p>{@code null} is a valid value which will indicate that no header
     * is to be applied.
     *
     * @param value New header
     * @return This object, for chaining
     */
    public FileConfigurationOptions header(String value) {
        header = value;
        return this;
    }

    /**
     * Gets whether or not the header should be copied from a default source.
     *
     * <p>If this is true, if a default {@link FileConfiguration} is passed to
     * {@link FileConfiguration#setDefaults(Configuration)}
     * then upon saving it will use the header from that config, instead of
     * the one provided here.
     *
     * <p>If no default is set on the configuration, or the default is not of
     * type FileConfiguration, or that config has no header ({@link #header()}
     * returns null) then the header specified in this configuration will be
     * used.
     *
     * <p>Defaults to true.
     *
     * @return Whether or not to copy the header
     */
    public boolean copyHeader() {
        return copyHeader;
    }

    /**
     * Sets whether or not the header should be copied from a default source.
     *
     * <p>If this is true, if a default {@link FileConfiguration} is passed to
     * {@link FileConfiguration#setDefaults(Configuration)}
     * then upon saving it will use the header from that config, instead of
     * the one provided here.
     *
     * <p>If no default is set on the configuration, or the default is not of
     * type FileConfiguration, or that config has no header ({@link #header()}
     * returns null) then the header specified in this configuration will be
     * used.
     *
     * <p>Defaults to true.
     *
     * @param value Whether or not to copy the header
     * @return This object, for chaining
     */
    public FileConfigurationOptions copyHeader(boolean value) {
        copyHeader = value;

        return this;
    }

}
