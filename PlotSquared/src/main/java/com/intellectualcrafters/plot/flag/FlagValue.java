package com.intellectualcrafters.plot.flag;

import java.util.HashSet;

import org.apache.commons.lang.StringUtils;

import com.intellectualcrafters.plot.object.PlotBlock;

/**
 * Created 2014-11-17 for PlotSquared
 *
 * @author Citymonstret
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
    
    public String toString(Object t) {
    	return t.toString();
    }

    public abstract T getValue(Object t);

    public abstract T parse(String t);

    public abstract String getDescription();

    public static class BooleanValue extends FlagValue<Boolean> {
    	
        @Override
        public Boolean getValue(final Object t) {
            return (Boolean) t;
        }

        @Override
        public Boolean parse(final String t) {
        	try {
        		return Boolean.parseBoolean(t);
        	}
        	catch (IllegalArgumentException e) {
        		return null;
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
        	}
        	catch (IllegalArgumentException e) {
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
    	public String toString(Object t) {
    		Integer[] value = ((Integer[]) t);
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
            return new Integer[]{amount, seconds};
        }

        @Override
        public String getDescription() {
        	return "Value(s) must be numeric. /plot set flag {flag} {amount} [seconds]";
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
        		int value = Integer.parseInt(t);
        		if (value >= 0) {
        			return null;
        		}
        		return value;
        	}
        	catch (IllegalArgumentException e) {
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
        	}
        	catch (IllegalArgumentException e) {
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
        	}
        	catch (IllegalArgumentException e) {
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
        		long value = Long.parseLong(t);
        		if (value < 0) {
        			return null;
        		}
        		return value;
        	}
        	catch (IllegalArgumentException e) {
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
        		double value = Double.parseDouble(t);
        		if (value < 0) {
        			return null;
        		}
        		return value;
        	}
        	catch (IllegalArgumentException e) {
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
        		String[] split = t.split(":");
            	byte data;
            	if (split.length == 2) {
            		data = Byte.parseByte(split[1]);
            	}
            	else {
            		data = 0;
            	}
            	short id = Short.parseShort(split[0]);
            	return new PlotBlock(id, data);
        	}
        	catch (Exception e) {
        		return null;
        	}
        }

        @Override
        public String getDescription() {
        	return "Flag value must be a number (negative decimals are allowed)";
        }
    }
    
    public static class PlotBlockListValue extends FlagValue<HashSet<PlotBlock>> {

    	@Override
    	public String toString(Object t) {
    		return StringUtils.join((HashSet<PlotBlock>) t, ",");
    	}
    	
        @SuppressWarnings("unchecked")
		@Override
        public HashSet<PlotBlock> getValue(final Object t) {
        	return (HashSet<PlotBlock>)t;
        }

        @Override
        public HashSet<PlotBlock> parse(final String t) {
        	HashSet<PlotBlock> list = new HashSet<PlotBlock>();
            for (String item : t.split(",")) {
            	String[] split = item.split(":");
            	byte data;
            	if (split.length == 2) {
            		data = Byte.parseByte(split[1]);
            	}
            	else {
            		data = 0;
            	}
            	short id = Short.parseShort(split[0]);
            	PlotBlock block = new PlotBlock(id, data);
            	list.add(block);
            }
            return list;
        }

        @Override
        public String getDescription() {
        	return "Flag value must be a block list";
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
