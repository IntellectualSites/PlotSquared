package com.intellectualcrafters.plot.flag;

/**
 * Created 2014-11-17 for PlotSquared
 *
 * @author Citymonstret
 */
public abstract class FlagValue<T> {

    private final Class<T> clazz;

    public FlagValue() {
        this.clazz = (Class<T>) getClass();
    }

    public FlagValue(final Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException();
        }
        this.clazz = clazz;
    }

    public boolean validValue(final Object value) {
        return (value != null) && (value.getClass() == this.clazz);
    }

    public abstract T getValue(String t);

    public abstract String parse(String t);

    public abstract String getDescription();

    public static class BooleanValue extends FlagValue<Boolean> {

        @Override
        public Boolean getValue(final String t) {
            return null;
        }

        @Override
        public String parse(final String t) {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    public static class StringValue extends FlagValue<String> {

        @Override
        public String parse(final String s) {
            return s;
        }

        @Override
        public String getDescription() {
            return "Flag value must be alphanumeric";
        }

        @Override
        public String getValue(final String t) {
            return t;
        }
    }
}
