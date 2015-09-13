package com.intellectualcrafters.plot.flag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.StringMan;

/**
 * Created 2014-11-17 for PlotSquared
 *
 */
public abstract class FlagValue<T> {
    private final Class<T> clazz;
    
    @SuppressWarnings("unchecked")
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
    
    public String toString(final Object t) {
        return t.toString();
    }
    
    public abstract T getValue(final Object t);
    
    public abstract T parse(final String t);
    
    public abstract String getDescription();
    
    public static class BooleanValue extends FlagValue<Boolean> {
        @Override
        public Boolean getValue(final Object t) {
            return (Boolean) t;
        }
        
        @Override
        public Boolean parse(final String t) {
            switch (t.toLowerCase()) {
                case "1":
                case "yes":
                case "allow":
                case "true": {
                    return true;
                }
                case "0":
                case "no":
                case "deny":
                case "false": {
                    return false;
                }
                default: {
                    return null;
                }
            }
        }
        
        @Override
        public String getDescription() {
            return "Flag value must be a boolean (true|false)";
        }
    }
    
    public static class IntegerValue extends FlagValue<Integer> {
        @Override
        public Integer getValue(final Object t) {
            return (Integer) t;
        }
        
        @Override
        public Integer parse(final String t) {
            try {
                return Integer.parseInt(t);
            } catch (final IllegalArgumentException e) {
                return null;
            }
        }
        
        @Override
        public String getDescription() {
            return "Flag value must be a whole number";
        }
    }
    
    public static class IntervalValue extends FlagValue<Integer[]> {
        @Override
        public String toString(final Object t) {
            final Integer[] value = ((Integer[]) t);
            return value[0] + " " + value[1];
        }
        
        @Override
        public Integer[] getValue(final Object t) {
            return (Integer[]) t;
        }
        
        @Override
        public Integer[] parse(final String t) {
            int seconds;
            int amount;
            final String[] values = t.split(" ");
            if (values.length < 2) {
                seconds = 1;
                try {
                    amount = Integer.parseInt(values[0]);
                } catch (final Exception e) {
                    return null;
                }
            } else {
                try {
                    amount = Integer.parseInt(values[0]);
                    seconds = Integer.parseInt(values[1]);
                } catch (final Exception e) {
                    return null;
                }
            }
            return new Integer[] { amount, seconds };
        }
        
        @Override
        public String getDescription() {
            return "Value(s) must be numeric. /plot set flag <flag> <interval> [amount]";
        }
    }
    
    public static class UnsignedIntegerValue extends FlagValue<Integer> {
        @Override
        public Integer getValue(final Object t) {
            return (Integer) t;
        }
        
        @Override
        public Integer parse(final String t) {
            try {
                final int value = Integer.parseInt(t);
                if (value < 0) {
                    return null;
                }
                return value;
            } catch (final IllegalArgumentException e) {
                return null;
            }
        }
        
        @Override
        public String getDescription() {
            return "Flag value must be a positive whole number (includes 0)";
        }
    }
    
    public static class DoubleValue extends FlagValue<Double> {
        @Override
        public Double getValue(final Object t) {
            return (Double) t;
        }
        
        @Override
        public Double parse(final String t) {
            try {
                return Double.parseDouble(t);
            } catch (final IllegalArgumentException e) {
                return null;
            }
        }
        
        @Override
        public String getDescription() {
            return "Flag value must be a number (negative decimals are allowed)";
        }
    }
    
    public static class LongValue extends FlagValue<Long> {
        @Override
        public Long getValue(final Object t) {
            return (Long) t;
        }
        
        @Override
        public Long parse(final String t) {
            try {
                return Long.parseLong(t);
            } catch (final IllegalArgumentException e) {
                return null;
            }
        }
        
        @Override
        public String getDescription() {
            return "Flag value must be a whole number (large numbers allowed)";
        }
    }
    
    public static class UnsignedLongValue extends FlagValue<Long> {
        @Override
        public Long getValue(final Object t) {
            return (Long) t;
        }
        
        @Override
        public Long parse(final String t) {
            try {
                final long value = Long.parseLong(t);
                if (value < 0) {
                    return null;
                }
                return value;
            } catch (final IllegalArgumentException e) {
                return null;
            }
        }
        
        @Override
        public String getDescription() {
            return "Flag value must be a positive whole number (large numbers allowed)";
        }
    }
    
    public static class UnsignedDoubleValue extends FlagValue<Double> {
        @Override
        public Double getValue(final Object t) {
            return (Double) t;
        }
        
        @Override
        public Double parse(final String t) {
            try {
                final double value = Double.parseDouble(t);
                if (value < 0) {
                    return null;
                }
                return value;
            } catch (final IllegalArgumentException e) {
                return null;
            }
        }
        
        @Override
        public String getDescription() {
            return "Flag value must be a positive number (decimals allowed)";
        }
    }
    
    public static class PlotBlockValue extends FlagValue<PlotBlock> {
        @Override
        public PlotBlock getValue(final Object t) {
            return (PlotBlock) t;
        }
        
        @Override
        public PlotBlock parse(final String t) {
            try {
                final String[] split = t.split(":");
                byte data;
                if (split.length == 2) {
                    if ("*".equals(split[1])) {
                        data = -1;
                    } else {
                        data = Byte.parseByte(split[1]);
                    }
                } else {
                    data = -1;
                }
                final short id = Short.parseShort(split[0]);
                return new PlotBlock(id, data);
            } catch (final Exception e) {
                return null;
            }
        }
        
        @Override
        public String getDescription() {
            return "Flag value must be a number (negative decimals are allowed)";
        }
    }
    
    public interface ListValue {
        void add(final Object t, final String value);
        
        void remove(final Object t, final String value);
    }
    
    public static class PlotBlockListValue extends FlagValue<HashSet<PlotBlock>> implements ListValue {
        @SuppressWarnings("unchecked")
        @Override
        public String toString(final Object t) {
            return StringMan.join((HashSet<PlotBlock>) t, ",");
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public HashSet<PlotBlock> getValue(final Object t) {
            return (HashSet<PlotBlock>) t;
        }
        
        @Override
        public HashSet<PlotBlock> parse(final String t) {
            final HashSet<PlotBlock> list = new HashSet<PlotBlock>();
            for (final String item : t.split(",")) {
                final String[] split = item.split(":");
                byte data;
                if (split.length == 2) {
                    if ("*".equals(split[1])) {
                        data = -1;
                    } else {
                        data = Byte.parseByte(split[1]);
                    }
                } else {
                    data = -1;
                }
                final short id = Short.parseShort(split[0]);
                final PlotBlock block = new PlotBlock(id, data);
                list.add(block);
            }
            return list;
        }
        
        @Override
        public String getDescription() {
            return "Flag value must be a block list";
        }
        
        @Override
        public void add(final Object t, final String value) {
            try {
                ((HashSet<PlotBlock>) t).addAll(parse(value));
            } catch (final Exception e) {}
        }
        
        @Override
        public void remove(final Object t, final String value) {
            try {
                for (final PlotBlock item : parse(value)) {
                    ((HashSet<PlotBlock>) t).remove(item);
                }
            } catch (final Exception e) {}
        }
    }
    
    public static class IntegerListValue extends FlagValue<List<Integer>> implements ListValue {
        @SuppressWarnings("unchecked")
        @Override
        public String toString(final Object t) {
            return StringMan.join((List<Integer>) t, ",");
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public List<Integer> getValue(final Object t) {
            return (List<Integer>) t;
        }
        
        @Override
        public List<Integer> parse(final String t) {
            final String[] split = (t.split(","));
            final ArrayList<Integer> numbers = new ArrayList<Integer>();
            for (final String element : split) {
                numbers.add(Integer.parseInt(element));
            }
            return numbers;
        }
        
        @Override
        public String getDescription() {
            return "Flag value must be a integer list";
        }
        
        @Override
        public void add(final Object t, final String value) {
            try {
                ((List<Integer>) t).addAll(parse(value));
            } catch (final Exception e) {}
        }
        
        @Override
        public void remove(final Object t, final String value) {
            try {
                for (final Integer item : parse(value)) {
                    ((List<Integer>) t).remove(item);
                }
            } catch (final Exception e) {}
        }
    }
    
    @SuppressWarnings("ALL")
    public static class StringListValue extends FlagValue<List<String>> implements ListValue {
        
        @Override
        public String toString(final Object t) {
            return StringMan.join((List<String>) t, ",");
        }
        
        @Override
        public List<String> getValue(final Object t) {
            return (List<String>) t;
        }
        
        @Override
        public List<String> parse(final String t) {
            return Arrays.asList(t.split(","));
        }
        
        @Override
        public String getDescription() {
            return "Flag value must be a string list";
        }
        
        @Override
        public void add(final Object t, final String value) {
            try {
                ((List<String>) t).addAll(parse(value));
            } catch (final Exception ignored) {}
        }
        
        @Override
        public void remove(final Object t, final String value) {
            try {
                for (final String item : parse(value)) {
                    ((List<String>) t).remove(item);
                }
            } catch (final Exception e) {}
        }
    }
    
    public static class DoubleListValue extends FlagValue<List<Double>> implements ListValue {
        @SuppressWarnings("unchecked")
        @Override
        public String toString(final Object t) {
            return StringMan.join((List<Double>) t, ",");
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public List<Double> getValue(final Object t) {
            return (List<Double>) t;
        }
        
        @Override
        public List<Double> parse(final String t) {
            final String[] split = (t.split(","));
            final ArrayList<Double> numbers = new ArrayList<Double>();
            for (final String element : split) {
                numbers.add(Double.parseDouble(element));
            }
            return numbers;
        }
        
        @Override
        public String getDescription() {
            return "Flag value must be a integer list";
        }
        
        @Override
        public void add(final Object t, final String value) {
            try {
                ((List<Double>) t).addAll(parse(value));
            } catch (final Exception e) {}
        }
        
        @Override
        public void remove(final Object t, final String value) {
            try {
                for (final Double item : parse(value)) {
                    ((List<Double>) t).remove(item);
                }
            } catch (final Exception e) {}
        }
    }
    
    public static class StringValue extends FlagValue<String> {
        @Override
        public String parse(final String s) {
            return s;
        }
        
        @Override
        public String getDescription() {
            return "Flag value must be alphanumeric. Some special characters are allowed.";
        }
        
        @Override
        public String getValue(final Object t) {
            return t.toString();
        }
    }
}
