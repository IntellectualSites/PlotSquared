package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.util.PlotWeather;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class WeatherFlag extends PlotFlag<PlotWeather, WeatherFlag> {

    public static final WeatherFlag PLOT_WEATHER_FLAG_RAIN =
        new WeatherFlag(PlotWeather.RAIN);
    public static final WeatherFlag PLOT_WEATHER_FLAG_CLEAR =
        new WeatherFlag(PlotWeather.CLEAR);
    public static final WeatherFlag PLOT_WEATHER_FLAG_OFF =
        new WeatherFlag(PlotWeather.RESET);

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected WeatherFlag(@NotNull PlotWeather value) {
        super(value, Captions.FLAG_CATEGORY_WEATHER, Captions.FLAG_DESCRIPTION_WEATHER);
    }

    @Override public WeatherFlag parse(@NotNull String input) {
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

    @Override public WeatherFlag merge(@NotNull PlotWeather newValue) {
        return flagOf(newValue);
    }

    @Override public String toString() {
        return getValue().toString();
    }

    @Override public String getExample() {
        return "storm";
    }

    @Override protected WeatherFlag flagOf(@NotNull PlotWeather value) {
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
        return Arrays.asList("rain", "storm", "on", "lightning", "thunder", "clear", "off", "sun", "reset");
    }

}
