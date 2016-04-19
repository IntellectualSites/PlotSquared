package com.intellectualcrafters.plot.flag;

public class IntervalFlag extends Flag<Integer[]> {

    public IntervalFlag(String name) {
        super(name);
    }

    @Override
    public String valueToString(Object value) {
        return null;
    }

    @Override public Integer[] parseValue(String value) {
        int seconds;
        int amount;
        String[] values = value.split(" ");
        if (values.length < 2) {
            seconds = 1;
            try {
                amount = Integer.parseInt(values[0]);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (values.length == 2) {
            try {
                amount = Integer.parseInt(values[0]);
                seconds = Integer.parseInt(values[1]);
            } catch (NumberFormatException e) {
                return null;
            }
        } else {
            return null;
        }
        return new Integer[]{amount, seconds};
    }

    @Override public String getValueDescription() {
        return "Value(s) must be numeric. /plot set flag <flag> <interval> [amount]";
    }
}
