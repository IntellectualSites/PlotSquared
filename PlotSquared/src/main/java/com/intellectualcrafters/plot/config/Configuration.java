////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualcrafters.plot.config;

import com.intellectualcrafters.plot.object.PlotBlock;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Configuration Utility
 *
 * @author Empire92
 */
@SuppressWarnings("unused")
public class Configuration {

    public static final SettingValue STRING = new SettingValue("STRING") {
        @Override
        public boolean validateValue(final String string) {
            return true;
        }

        @Override
        public Object parseString(final String string) {
            return string;
        }
    };

    public static final SettingValue STRINGLIST = new SettingValue("STRINGLIST") {
        @Override
        public boolean validateValue(final String string) {
            return true;
        }

        @Override
        public Object parseString(final String string) {
            return string.split(",");
        }
    };

    public static final SettingValue INTEGER = new SettingValue("INTEGER") {
        @Override
        public boolean validateValue(final String string) {
            try {
                int x = Integer.parseInt(string);
                return true;
            } catch (final Exception e) {
                return false;
            }
        }

        @Override
        public Object parseString(final String string) {
            return Integer.parseInt(string);
        }
    };

    public static final SettingValue BOOLEAN = new SettingValue("BOOLEAN") {
        @Override
        public boolean validateValue(final String string) {
            try {
                boolean b = Boolean.parseBoolean(string);
                return true;
            } catch (final Exception e) {
                return false;
            }
        }

        @Override
        public Object parseString(final String string) {
            return Boolean.parseBoolean(string);
        }
    };

    public static final SettingValue DOUBLE = new SettingValue("DOUBLE") {
        @Override
        public boolean validateValue(final String string) {
            try {
                double d = Double.parseDouble(string);
                return true;
            } catch (final Exception e) {
                return false;
            }
        }

        @Override
        public Object parseString(final String string) {
            return Double.parseDouble(string);
        }
    };

    public static final SettingValue BIOME = new SettingValue("BIOME") {
        @Override
        public boolean validateValue(final String string) {
            try {
                Biome.valueOf(string.toUpperCase());
                return true;
            } catch (final Exception e) {
                return false;
            }
        }

        @Override
        public Object parseString(final String string) {
            for (final Biome biome : Biome.values()) {
                if (biome.name().equals(string.toUpperCase())) {
                    return biome;
                }
            }
            return Biome.FOREST;
        }

        @Override
        public Object parseObject(final Object object) {
            return (((Biome) object)).toString();
        }
    };

    public static final SettingValue BLOCK = new SettingValue("BLOCK") {
        @Override
        public boolean validateValue(final String string) {
            try {
                if (string.contains(":")) {
                    final String[] split = string.split(":");
                    short s =
                            Short.parseShort(split[0]);
                    short z =
                            Short.parseShort(split[1]);
                } else {
                    short s = Short.parseShort(string);
                }
                return true;
            } catch (final Exception e) {
                return false;
            }
        }

        @Override
        public Object parseString(final String string) {
            if (string.contains(":")) {
                final String[] split = string.split(":");
                return new PlotBlock(Short.parseShort(split[0]), Byte.parseByte(split[1]));
            } else {
                return new PlotBlock(Short.parseShort(string), (byte) 0);
            }
        }

        @Override
        public Object parseObject(final Object object) {
            return ((PlotBlock) object).id + ":" + ((PlotBlock) object).data;
        }
    };
    public static final SettingValue BLOCKLIST = new SettingValue("BLOCKLIST") {
        @Override
        public boolean validateValue(final String string) {
            try {
                for (String block : string.split(",")) {
                    if (block.contains("%")) {
                        final String[] split = block.split("%");
                        int i = Integer.parseInt(split[0]);
                        block = split[1];
                    }
                    if (block.contains(":")) {
                        final String[] split = block.split(":");
                        short s = Short.parseShort(split[0]);
                        short z = Short.parseShort(split[1]);
                    } else {
                        short s = Short.parseShort(block);
                    }
                }
                return true;
            } catch (final Exception e) {
                return false;
            }
        }

        @Override
        public Object parseString(final String string) {
            final String[] blocks = string.split(",");
            final ArrayList<PlotBlock> parsedvalues = new ArrayList<>();

            final PlotBlock[] values = new PlotBlock[blocks.length];
            final int[] counts = new int[blocks.length];
            int min = 100;
            for (int i = 0; i < blocks.length; i++) {
                if (blocks[i].contains("%")) {
                    final String[] split = blocks[i].split("%");
                    blocks[i] = split[1];
                    final int value = Integer.parseInt(split[0]);
                    counts[i] = value;
                    if (value < min) {
                        min = value;
                    }
                } else {
                    counts[i] = 1;
                    if (1 < min) {
                        min = 1;
                    }
                }
                if (blocks[i].contains(":")) {
                    final String[] split = blocks[i].split(":");
                    values[i] = new PlotBlock(Short.parseShort(split[0]), Byte.parseByte(split[1]));
                } else {
                    values[i] = new PlotBlock(Short.parseShort(blocks[i]), (byte) 0);
                }
            }
            final int gcd = gcd(counts);
            for (int i = 0; i < counts.length; i++) {
                final int num = counts[i];
                for (int j = 0; j < (num / gcd); j++) {
                    parsedvalues.add(values[i]);
                }
            }

            return parsedvalues.toArray(new PlotBlock[parsedvalues.size()]);
        }

        @Override
        public Object parseObject(final Object object) {
            final List<String> list = new ArrayList<>();
            for (final PlotBlock block : (PlotBlock[]) object) {
                list.add((block.id + ":" + (block.data)));
            }
            return list;
        }
    };

    public static int gcd(final int a, final int b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }

    private static int gcd(final int[] a) {
        int result = a[0];
        for (int i = 1; i < a.length; i++) {
            result = gcd(result, a[i]);
        }
        return result;
    }

    /**
     * Create your own SettingValue object to make the management of plotworld
     * configuration easier
     */
    public static abstract class SettingValue {
        private final String type;

        public SettingValue(final String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }

        public Object parseObject(final Object object) {
            return object;
        }

        public abstract Object parseString(final String string);

        public abstract boolean validateValue(final String string);
    }
}
