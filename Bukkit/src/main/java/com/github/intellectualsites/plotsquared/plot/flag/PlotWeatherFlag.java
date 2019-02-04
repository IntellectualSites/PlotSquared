package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.util.PlotWeather;

public class PlotWeatherFlag extends Flag<PlotWeather> {

    public PlotWeatherFlag(String name) {
        super(name);
    }

    @Override public String valueToString(Object value) {
        return value.toString();
    }

    @Override public PlotWeather parseValue(String value) {
        switch (value.toLowerCase()) {
            case "rain":
            case "storm":
            case "on":
            case "lightning":
            case "thunder":
                return PlotWeather.RAIN;
            case "clear":
            case "off":
            case "sun":
                return PlotWeather.CLEAR;
            default:
                return PlotWeather.RESET;
        }
    }

    @Override public String getValueDescription() {
        return "Flag must be a weather: 'rain' or 'sun'";
    }
}
