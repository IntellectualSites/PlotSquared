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

import java.util.ArrayList;

import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.StringComparison;

/**
 * Main Configuration Utility
 *
 * @author Empire92
 */
@SuppressWarnings("unused")
public class Configuration {
    
    public static final SettingValue<String> STRING = new SettingValue<String>("STRING") {
        @Override
        public boolean validateValue(final String string) {
            return true;
        }

        @Override
        public String parseString(final String string) {
            return string;
        }
    };
    public static final SettingValue<String[]> STRINGLIST = new SettingValue<String[]>("STRINGLIST") {
        @Override
        public boolean validateValue(final String string) {
            return true;
        }

        @Override
        public String[] parseString(final String string) {
            return string.split(",");
        }
    };
    public static final SettingValue<Integer> INTEGER = new SettingValue<Integer>("INTEGER") {
        @Override
        public boolean validateValue(final String string) {
            try {
                Integer.parseInt(string);
                return true;
            } catch (final Exception e) {
                return false;
            }
        }

        @Override
        public Integer parseString(final String string) {
            return Integer.parseInt(string);
        }
    };
    public static final SettingValue<Boolean> BOOLEAN = new SettingValue<Boolean>("BOOLEAN") {
        @Override
        public boolean validateValue(final String string) {
            try {
                Boolean.parseBoolean(string);
                return true;
            } catch (final Exception e) {
                return false;
            }
        }

        @Override
        public Boolean parseString(final String string) {
            return Boolean.parseBoolean(string);
        }
    };
    public static final SettingValue<Double> DOUBLE = new SettingValue<Double>("DOUBLE") {
        @Override
        public boolean validateValue(final String string) {
            try {
                Double.parseDouble(string);
                return true;
            } catch (final Exception e) {
                return false;
            }
        }

        @Override
        public Double parseString(final String string) {
            return Double.parseDouble(string);
        }
    };
    public static final SettingValue<String> BIOME = new SettingValue<String>("BIOME") {
        @Override
        public boolean validateValue(final String string) {
            try {
                final int biome = BlockManager.manager.getBiomeFromString(string.toUpperCase());
                return biome != -1;
            } catch (final Exception e) {
                return false;
            }
        }

        @Override
        public String parseString(final String string) {
            if (validateValue(string)) {
                return string.toUpperCase();
            }
            return "FOREST";
        }
    };
    public static final SettingValue<PlotBlock> BLOCK = new SettingValue<PlotBlock>("BLOCK") {
        @Override
        public boolean validateValue(final String string) {
            try {
                if (string.contains(":")) {
                    final String[] split = string.split(":");
                    Short.parseShort(split[0]);
                    Short.parseShort(split[1]);
                } else {
                    Short.parseShort(string);
                }
                return true;
            } catch (final Exception e) {
                return false;
            }
        }

        @Override
        public PlotBlock parseString(final String string) {
            if (string.contains(":")) {
                final String[] split = string.split(":");
                return new PlotBlock(Short.parseShort(split[0]), Byte.parseByte(split[1]));
            } else {
                return new PlotBlock(Short.parseShort(string), (byte) 0);
            }
        }
    };
    public static final SettingValue<PlotBlock[]> BLOCKLIST = new SettingValue<PlotBlock[]>("BLOCKLIST") {
        @Override
        public boolean validateValue(final String string) {
            try {
                for (String block : string.split(",")) {
                    if (block.contains("%")) {
                        final String[] split = block.split("%");
                        Integer.parseInt(split[0]);
                        block = split[1];
                    }
                    StringComparison<PlotBlock>.ComparisonResult value = BlockManager.manager.getClosestBlock(block);
                    if (value == null || value.match > 1) {
                        return false;
                    }
                }
                return true;
            } catch (final Exception e) {
                return false;
            }
        }

        @Override
        public PlotBlock[] parseString(final String string) {
            final String[] blocks = string.split(",");
            final ArrayList<PlotBlock> parsedvalues = new ArrayList<>();
            final PlotBlock[] values = new PlotBlock[blocks.length];
            final int[] counts = new int[blocks.length];
            int min = 100;
            for (int i = 0; i < blocks.length; i++) {
                try {
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
                    StringComparison<PlotBlock>.ComparisonResult result = BlockManager.manager.getClosestBlock(blocks[i]);
                    if (result != null && result.match < 2) {
                        values[i] = result.best;
                    }
                }
                catch (Exception e) {}
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
     * Create your own SettingValue object to make the management of plotworld configuration easier
     */
    public static abstract class SettingValue<T> {
        private final String type;

        public SettingValue(final String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }

        public abstract T parseString(final String string);

        public abstract boolean validateValue(final String string);
    }
}
