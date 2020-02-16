package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.util.PlotWeather;
import org.jetbrains.annotations.NotNull;

public class PlotWeatherFlag extends PlotFlag<PlotWeather, PlotWeatherFlag> {

    public static final PlotWeatherFlag PLOT_WEATHER_FLAG_RAIN = new PlotWeatherFlag(PlotWeather.RAIN);
    public static final PlotWeatherFlag PLOT_WEATHER_FLAG_CLEAR = new PlotWeatherFlag(PlotWeather.CLEAR);
    public static final PlotWeatherFlag PLOT_WEATHER_FLAG_OFF = new PlotWeatherFlag(PlotWeather.RESET);

    /**
     * Construct a new flag instance.
     *
     * @param value           Flag value
     */
    protected PlotWeatherFlag(@NotNull PlotWeather value) {
        super(value, Captions.FLAG_CATEGORY_WEATHER, Captions.FLAG_DESCRIPTION_WEATHER);
    }

    @Override public PlotWeatherFlag parse(@NotNull String input) {
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

    @Override public PlotWeatherFlag merge(@NotNull PlotWeather newValue) {
        return flagOf(newValue);
    }

    @Override public String toString() {
        return getValue().toString();
    }

    @Override public String getExample() {
        return "storm";
    }

    @Override protected PlotWeatherFlag flagOf(@NotNull PlotWeather value) {
        switch (value) {
            case RAIN:
                return PLOT_WEATHER_FLAG_RAIN;
            case CLEAR:
                return PLOT_WEATHER_FLAG_CLEAR;
            default:
                return PLOT_WEATHER_FLAG_OFF;
        }
    }

}
