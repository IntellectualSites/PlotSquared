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
package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.plot.PlotWeather;
import com.plotsquared.core.plot.flag.PlotFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Collection;

public class WeatherFlag extends PlotFlag<PlotWeather, WeatherFlag> {

    public static final WeatherFlag PLOT_WEATHER_FLAG_RAIN = new WeatherFlag(PlotWeather.RAIN);
    public static final WeatherFlag PLOT_WEATHER_FLAG_CLEAR = new WeatherFlag(PlotWeather.CLEAR);
    public static final WeatherFlag PLOT_WEATHER_FLAG_WORLD = new WeatherFlag(PlotWeather.WORLD);
    public static final WeatherFlag PLOT_WEATHER_FLAG_OFF = new WeatherFlag(PlotWeather.OFF);

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected WeatherFlag(@NonNull PlotWeather value) {
        super(
                value,
                TranslatableCaption.of("flags.flag_category_weather"),
                TranslatableCaption.of("flags.flag_description_weather")
        );
    }

    @Override
    public WeatherFlag parse(@NonNull String input) {
        return switch (input.toLowerCase()) {
            case "rain" -> flagOf(PlotWeather.RAIN);
            case "clear" -> flagOf(PlotWeather.CLEAR);
            case "reset" -> flagOf(PlotWeather.WORLD);
            default -> flagOf(PlotWeather.OFF);
        };
    }

    @Override
    public WeatherFlag merge(@NonNull PlotWeather newValue) {
        return flagOf(newValue);
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    @Override
    public String getExample() {
        return "rain";
    }

    @Override
    protected WeatherFlag flagOf(@NonNull PlotWeather value) {
        return switch (value) {
            case RAIN -> PLOT_WEATHER_FLAG_RAIN;
            case CLEAR -> PLOT_WEATHER_FLAG_CLEAR;
            case WORLD -> PLOT_WEATHER_FLAG_WORLD;
            default -> PLOT_WEATHER_FLAG_OFF;
        };
    }

    @Override
    public Collection<String> getTabCompletions() {
        return Arrays
                .asList("clear", "rain");
    }

}
