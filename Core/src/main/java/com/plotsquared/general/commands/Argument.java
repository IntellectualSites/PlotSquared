package com.plotsquared.general.commands;

import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.MainUtil;

public abstract class Argument<T> {
    
    private final String name;
    private final T example;
    
    public Argument(final String name, final T example) {
        this.name = name;
        this.example = example;
    }
    
    public abstract T parse(final String in);
    
    @Override
    public final String toString() {
        return this.getName();
    }
    
    public final String getName() {
        return this.name;
    }
    
    public final T getExample() {
        return this.example;
    }
    
    public static final Argument<Integer> Integer = new Argument<Integer>("int", 16) {
        @Override
        public Integer parse(final String in) {
            Integer value = null;
            try {
                value = java.lang.Integer.parseInt(in);
            } catch (final Exception ignored) {}
            return value;
        }
    };
    
    public static final Argument<Boolean> Boolean = new Argument<Boolean>("boolean", true) {
        @Override
        public Boolean parse(final String in) {
            Boolean value = null;
            if (in.equalsIgnoreCase("true") || in.equalsIgnoreCase("Yes") || in.equalsIgnoreCase("1")) {
                value = true;
            } else if (in.equalsIgnoreCase("false") || in.equalsIgnoreCase("No") || in.equalsIgnoreCase("0")) {
                value = false;
            }
            return value;
        }
    };
    
    public static final Argument<String> String = new Argument<String>("String", "Example") {
        @Override
        public String parse(final String in) {
            return in;
        }
    };
    
    public static Argument<String> PlayerName = new Argument<String>("PlayerName", "Dinnerbone") {
        @Override
        public String parse(final String in) {
            return in.length() <= 16 ? in : null;
        }
    };
    
    public static Argument<PlotId> PlotID = new Argument<PlotId>("PlotID", new PlotId(-6, 3)) {
        @Override
        public PlotId parse(final String in) {
            return PlotId.fromString(in);
        }
    };
    
    public static Argument<Plot> Plot = new Argument<Plot>("Plot", new Plot(PlotArea.createGeneric("world"), new PlotId(3, -6), null)) {
        @Override
        public Plot parse(final String in) {
            return MainUtil.getPlotFromString(ConsolePlayer.getConsole(), in, false);
        }
    };
}
