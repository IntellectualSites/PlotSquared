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
package com.plotsquared.core.configuration;

/**
 * Various settings for controlling the input and output of a {@link
 * Configuration}.
 */
class ConfigurationOptions {

    private final Configuration configuration;
    private boolean copyDefaults = false;

    protected ConfigurationOptions(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Returns the {@link Configuration} that this object is responsible for.
     *
     * @return Parent configuration
     */
    public Configuration configuration() {
        return configuration;
    }

    /**
     * Gets the char that will be used to separate {@link
     * ConfigurationSection}s.
     *
     * <p> This value is always '.'.
     *
     * @return Path separator
     */
    char pathSeparator() {
        return '.';
    }

    /**
     * Checks if the {@link Configuration} should copy values from its default
     * {@link Configuration} directly.
     *
     * <p>If this is true, all values in the default Configuration will be
     * directly copied, making it impossible to distinguish between values
     * that were set and values that are provided by default. As a result,
     * {@link ConfigurationSection#contains(String)} will always
     * return the same value as {@link
     * ConfigurationSection#isSet(String)}. The default value is
     * false.
     *
     * @return Whether or not defaults are directly copied
     */
    public boolean copyDefaults() {
        return copyDefaults;
    }

    /**
     * Sets if the {@link Configuration} should copy values from its default
     * {@link Configuration} directly.
     *
     * <p>If this is true, all values in the default Configuration will be
     * directly copied, making it impossible to distinguish between values
     * that were set and values that are provided by default. As a result,
     * {@link ConfigurationSection#contains(String)} will always
     * return the same value as {@link
     * ConfigurationSection#isSet(String)}. The default value is
     * false.
     *
     * @param value Whether or not defaults are directly copied
     * @return This object, for chaining
     */
    public ConfigurationOptions copyDefaults(boolean value) {
        copyDefaults = value;
        return this;
    }

}
