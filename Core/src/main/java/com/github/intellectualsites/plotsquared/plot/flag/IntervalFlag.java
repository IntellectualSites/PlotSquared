package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class IntervalFlag extends Flag<IntervalFlag.Interval> {

    public IntervalFlag(String name) {
        super(Captions.FLAG_CATEGORY_INTERVALS, name);
    }

    @Override public String valueToString(Object value) {
        return value.toString();
    }

    @Override public Interval parseValue(String value) {
        int seconds;
        int amount;
        String[] values = value.split(" ");
        if (values.length < 2) {
            seconds = 1;
            try {
                amount = Integer.parseInt(values[0]);
            } catch (NumberFormatException ignored) {
                return null;
            }
        } else if (values.length == 2) {
            try {
                amount = Integer.parseInt(values[0]);
                seconds = Integer.parseInt(values[1]);
            } catch (NumberFormatException ignored) {
                return null;
            }
        } else {
            return null;
        }
        return new Interval(amount, seconds);
    }

    @Override public String getValueDescription() {
        return "Value(s) must be numeric. /plot set flag <flag> <interval> [amount]";
    }

    @EqualsAndHashCode @RequiredArgsConstructor @Getter public static final class Interval {

        private final int val1;
        private final int val2;

        public String toString() {
            return String.format("%d %d", this.val1, this.val2);
        }

    }

}
