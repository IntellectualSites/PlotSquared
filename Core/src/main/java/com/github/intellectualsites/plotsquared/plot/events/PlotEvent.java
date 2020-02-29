package com.github.intellectualsites.plotsquared.plot.events;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public abstract class PlotEvent {

    private final Plot plot;
    private String name;

    public PlotEvent(Plot plot) {
        this.plot = plot;
    }

    public final Plot getPlot() {
        return this.plot;
    }

    @NotNull public String getEventName() {
        if (name == null) {
            name = getClass().getSimpleName();
        }
        return name;
    }

    public enum Result {
        DENY(0), ACCEPT(1), FORCE(2);

        private int value;
        private static Map<Integer, Result> map = new HashMap<>();

        Result(int value) {
            this.value = value;
        }

        static {
            for (Result eventResult : Result.values()) {
                map.put(eventResult.value, eventResult);
            }
        }

        public static Result valueOf(int eventResult) {
            return (Result) map.get(eventResult);
        }

        public int getValue() {
            return value;
        }
    }

}
