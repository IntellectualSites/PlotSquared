package com.intellectualcrafters.plot;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.block.Biome;

public class Configuration {
	public static final SettingValue STRING = new SettingValue("STRING") {
		@Override
		public boolean validateValue(String string) {
			return true;
		}

		@Override
		public Object parseString(String string) {
			return string;
		}
	};

	public static final SettingValue STRINGLIST = new SettingValue("STRINGLIST") {
		@Override
		public boolean validateValue(String string) {
			return true;
		}

		@Override
		public Object parseString(String string) {
			return string.split(",");
		}
	};

	public static final SettingValue INTEGER = new SettingValue("INTEGER") {
		@Override
		public boolean validateValue(String string) {
			try {
				Integer.parseInt(string);
				return true;
			}
			catch (Exception e) {
				return false;
			}
		}

		@Override
		public Object parseString(String string) {
			return Integer.parseInt(string);
		}
	};

	public static final SettingValue BOOLEAN = new SettingValue("BOOLEAN") {
		@Override
		public boolean validateValue(String string) {
			try {
				Boolean.parseBoolean(string);
				return true;
			}
			catch (Exception e) {
				return false;
			}
		}

		@Override
		public Object parseString(String string) {
			return Boolean.parseBoolean(string);
		}
	};

	public static final SettingValue DOUBLE = new SettingValue("DOUBLE") {
		@Override
		public boolean validateValue(String string) {
			try {
				Double.parseDouble(string);
				return true;
			}
			catch (Exception e) {
				return false;
			}
		}

		@Override
		public Object parseString(String string) {
			return Double.parseDouble(string);
		}
	};

	public static final SettingValue BIOME = new SettingValue("BIOME") {
		@Override
		public boolean validateValue(String string) {
			try {
				Biome.valueOf(string.toUpperCase());
				return true;
			}
			catch (Exception e) {
				return false;
			}
		}

		@Override
		public Object parseString(String string) {
			for (Biome biome : Biome.values()) {
				if (biome.name().equals(string.toUpperCase())) {
					return biome;
				}
			}
			return Biome.FOREST;
		}

		@Override
		public Object parseObject(Object object) {
			return ((Biome) object).toString();
		}
	};

	public static final SettingValue BLOCK = new SettingValue("BLOCK") {
		@Override
		public boolean validateValue(String string) {
			try {
				if (string.contains(":")) {
					String[] split = string.split(":");
					Short.parseShort(split[0]);
					Short.parseShort(split[1]);
				}
				else {
					Short.parseShort(string);
				}
				return true;
			}
			catch (Exception e) {
				return false;
			}
		}

		@Override
		public Object parseString(String string) {
			if (string.contains(":")) {
				String[] split = string.split(":");
				return new PlotBlock(Short.parseShort(split[0]), Byte.parseByte(split[1]));
			}
			else {
				return new PlotBlock(Short.parseShort(string), (byte) 0);
			}
		}

		@Override
		public Object parseObject(Object object) {
			return ((PlotBlock) object).id + ":" + ((PlotBlock) object).data;
		}
	};
	
	public static int gcd(int a, int b) {
	    if (b==0) return a;
	    return gcd(b,a%b);
	}
	private static int gcd(int[] a)
	{
	    int result = a[0];
	    for(int i = 1; i < a.length; i++)
	        result = gcd(result, a[i]);
	    return result;
	}

	public static final SettingValue BLOCKLIST = new SettingValue("BLOCKLIST") {
		@Override
		public boolean validateValue(String string) {
			try {
				for (String block : string.split(",")) {
				    if (block.contains("%")) {
				        String[] split = block.split("%");
				        Integer.parseInt(split[0]);
				        block = split[1];
				    }
					if (block.contains(":")) {
						String[] split = block.split(":");
						Short.parseShort(split[0]);
						Short.parseShort(split[1]);
					}
					else {
						Short.parseShort(block);
					}
				}
				return true;
			}
			catch (Exception e) {
				return false;
			}
		}

		@Override
		public Object parseString(String string) {
			String[] blocks = string.split(",");
			ArrayList<PlotBlock> parsedvalues = new ArrayList<PlotBlock>();
			
			
			PlotBlock[] values = new PlotBlock[blocks.length];
			int[] counts = new int[blocks.length];
			int min = 100;
			for (int i = 0; i < blocks.length; i++) {
			    if (blocks[i].contains("%")) {
			        String[] split = blocks[i].split("%");
			        blocks[i] = split[1];
			        int value = Integer.parseInt(split[0]);
			        counts[i] = value;
                    if (value<min) {
                        min = value;
                    }
			    }
			    else {
			        counts[i] = 1;
			        if (1<min) {
			            min = 1;
			        }
			    }
				if (blocks[i].contains(":")) {
					String[] split = blocks[i].split(":");
					values[i] = new PlotBlock(Short.parseShort(split[0]), Byte.parseByte(split[1]));
				}
				else {
					values[i] = new PlotBlock(Short.parseShort(blocks[i]), (byte) 0);
				}
			}
			int gcd = gcd(counts);
			for (int i = 0; i < counts.length; i++) {
			    int num = counts[i];
			    for (int j = 0; j<num/gcd; j++) {
			        parsedvalues.add(values[i]);
			    }
			}
			
			return parsedvalues.toArray(new PlotBlock[0]);
		}

		@Override
		public Object parseObject(Object object) {
			List<String> list = new ArrayList<String>();
			for (PlotBlock block : (PlotBlock[]) object) {
				list.add((block.id + ":" + (block.data)));
			}
			return list;
		}
	};

	/**
	 * Create your own SettingValue object to make the management of plotworld
	 * configuration easier
	 */
	public static abstract class SettingValue {
		private String type;

		public SettingValue(String type) {
			this.type = type;
		}

		public String getType() {
			return this.type;
		}

		public Object parseObject(Object object) {
			return object;
		}

		public abstract Object parseString(String string);

		public abstract boolean validateValue(String string);
	}
}
