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
package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.plot.PlotWeather;
import com.plotsquared.core.plot.flag.PlotFlag;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;

public class WeatherFlag extends PlotFlag<PlotWeather, WeatherFlag> {

    public static final WeatherFlag PLOT_WEATHER_FLAG_RAIN = new WeatherFlag(PlotWeather.RAIN);
    public static final WeatherFlag PLOT_WEATHER_FLAG_CLEAR = new WeatherFlag(PlotWeather.CLEAR);
    public static final WeatherFlag PLOT_WEATHER_FLAG_OFF = new WeatherFlag(PlotWeather.RESET);

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected WeatherFlag(@Nonnull PlotWeather value) {
        super(value, Captions.FLAG_CATEGORY_WEATHER, Captions.FLAG_DESCRIPTION_WEATHER);
    }

    @Override public WeatherFlag parse(@Nonnull String input) {
        switch (input.toLowerCase()) {
            case "rain":
            case "storm":
            case "on":
            case "lightning":
            case "thunder":
                return flagOf(PlotWeather.RAIN);
            case "clear":
            case "off":
            case "sun":
                return flagOf(PlotWeather.CLEAR);
            default:
                return flagOf(PlotWeather.RESET);
        }
    }

    @Override public WeatherFlag merge(@Nonnull PlotWeather newValue) {
        return flagOf(newValue);
    }

    @Override public String toString() {
        return getValue().toString();
    }

    @Override public String getExample() {
        return "storm";
    }

    @Override protected WeatherFlag flagOf(@Nonnull PlotWeather value) {
        switch (value) {
            case RAIN:
                return PLOT_WEATHER_FLAG_RAIN;
            case CLEAR:
                return PLOT_WEATHER_FLAG_CLEAR;
            default:
                return PLOT_WEATHER_FLAG_OFF;
        }
    }

    @Override public Collection<String> getTabCompletions() {
        return Arrays
            .asList("rain", "storm", "on", "lightning", "thunder", "clear", "off", "sun", "reset");
    }

}
