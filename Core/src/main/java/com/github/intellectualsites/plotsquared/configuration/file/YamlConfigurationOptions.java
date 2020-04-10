/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.intellectualsites.plotsquared.configuration.file;

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
