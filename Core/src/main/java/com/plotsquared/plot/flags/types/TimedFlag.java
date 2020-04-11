package com.plotsquared.plot.flags.types;

import com.plotsquared.config.Caption;
import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.FlagParseException;
import com.plotsquared.plot.flags.PlotFlag;
import org.jetbrains.annotations.NotNull;

public abstract class TimedFlag<T, F extends PlotFlag<TimedFlag.Timed<T>, F>>
    extends PlotFlag<TimedFlag.Timed<T>, F> {
    private final T defaultValue;

    protected TimedFlag(@NotNull Timed<T> value, T defaultValue, @NotNull Caption flagDescription) {
        super(value, Captions.FLAG_CATEGORY_INTERVALS, flagDescription);
        this.defaultValue = defaultValue;
    }

    @Override public F parse(@NotNull String input) throws FlagParseException {
        String[] split = input.split(" ", 2);
        int interval;
        try {
            interval = Integer.parseInt(split[0]);
        } catch (Throwable throwable) {
            throw new FlagParseException(this, input, Captions.NOT_A_NUMBER, split[0]);
        }
        if (interval < 1) {
            throw new FlagParseException(this, input, Captions.NUMBER_NOT_POSITIVE, split[0]);
        }
        if (split.length == 1) {
            return flagOf(new Timed<>(interval, defaultValue));
        }
        final T parsedValue = parseValue(split[1]);
        return flagOf(new Timed<>(interval, parsedValue));
    }

    @Override public F merge(@NotNull Timed<T> newValue) {
        return flagOf(
            new Timed<>(getValue().interval + newValue.interval, mergeValue(newValue.value)));
    }

    protected abstract T parseValue(String input) throws FlagParseException;

    protected abstract T mergeValue(T other);

    @Override public String toString() {
        return getValue().toString();
    }

    public static final class Timed<T> {
        private final int interval;
        private final T value;

        public Timed(int interval, T value) {
            this.interval = interval;
            this.value = value;
        }

        public int getInterval() {
            return interval;
        }

        public T getValue() {
            return value;
        }

        @Override public String toString() {
            return String.format("%d %s", interval, value);
        }
    }
}
