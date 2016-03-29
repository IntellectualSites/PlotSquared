package com.intellectualcrafters.plot.config;

import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.WorldUtil;

import java.util.ArrayList;

/**
 * Main Configuration Utility
 *

 */
public class Configuration {

    public static final SettingValue<String> STRING = new SettingValue<String>("STRING") {
        @Override
        public boolean validateValue(String string) {
            return true;
        }

        @Override
        public String parseString(String string) {
            return string;
        }
    };
    public static final SettingValue<String[]> STRINGLIST = new SettingValue<String[]>("STRINGLIST") {
        @Override
        public boolean validateValue(String string) {
            return true;
        }

        @Override
        public String[] parseString(String string) {
            return string.split(",");
        }
    };
    public static final SettingValue<Integer> INTEGER = new SettingValue<Integer>("INTEGER") {
        @Override
        public boolean validateValue(String string) {
            try {
                Integer.parseInt(string);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public Integer parseString(String string) {
            return Integer.parseInt(string);
        }
    };
    public static final SettingValue<Boolean> BOOLEAN = new SettingValue<Boolean>("BOOLEAN") {
        @Override
        public boolean validateValue(String string) {
            try {
                Boolean.parseBoolean(string);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public Boolean parseString(String string) {
            return Boolean.parseBoolean(string);
        }
    };
    public static final SettingValue<Double> DOUBLE = new SettingValue<Double>("DOUBLE") {
        @Override
        public boolean validateValue(String string) {
            try {
                Double.parseDouble(string);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public Double parseString(String string) {
            return Double.parseDouble(string);
        }
    };
    public static final SettingValue<String> BIOME = new SettingValue<String>("BIOME") {
        @Override
        public boolean validateValue(String string) {
            try {
                int biome = WorldUtil.IMP.getBiomeFromString(string.toUpperCase());
                return biome != -1;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public String parseString(String string) {
            if (validateValue(string)) {
                return string.toUpperCase();
            }
            return "FOREST";
        }
    };
    public static final SettingValue<PlotBlock> BLOCK = new SettingValue<PlotBlock>("BLOCK") {
        @Override
        public boolean validateValue(String string) {
            StringComparison<PlotBlock>.ComparisonResult value = WorldUtil.IMP.getClosestBlock(string);
            return !(value == null || value.match > 1);
        }

        @Override
        public PlotBlock parseString(String string) {
            StringComparison<PlotBlock>.ComparisonResult value = WorldUtil.IMP.getClosestBlock(string);
            if (value == null || value.match > 1) {
                return null;
            }
            return value.best;
        }
    };
    public static final SettingValue<PlotBlock[]> BLOCKLIST = new SettingValue<PlotBlock[]>("BLOCKLIST") {
        @Override
        public boolean validateValue(String string) {
            try {
                for (String block : string.split(",")) {
                    if (block.contains("%")) {
                        String[] split = block.split("%");
                        Integer.parseInt(split[0]);
                        block = split[1];
                    }
                    StringComparison<PlotBlock>.ComparisonResult value = WorldUtil.IMP.getClosestBlock(block);
                    if (value == null || value.match > 1) {
                        return false;
                    }
                }
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        @Override
        public PlotBlock[] parseString(String string) {
            String[] blocks = string.split(",");
            ArrayList<PlotBlock> parsedvalues = new ArrayList<>();
            PlotBlock[] values = new PlotBlock[blocks.length];
            int[] counts = new int[blocks.length];
            int min = 100;
            for (int i = 0; i < blocks.length; i++) {
                try {
                    if (blocks[i].contains("%")) {
                        String[] split = blocks[i].split("%");
                        blocks[i] = split[1];
                        int value = Integer.parseInt(split[0]);
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
                    StringComparison<PlotBlock>.ComparisonResult result = WorldUtil.IMP.getClosestBlock(blocks[i]);
                    if (result != null && result.match < 2) {
                        values[i] = result.best;
                    }
                } catch (NumberFormatException e) {
                }
            }
            int gcd = gcd(counts);
            for (int i = 0; i < counts.length; i++) {
                int num = counts[i];
                for (int j = 0; j < num / gcd; j++) {
                    parsedvalues.add(values[i]);
                }
            }
            return parsedvalues.toArray(new PlotBlock[parsedvalues.size()]);
        }
    };

    public static int gcd(int a, int b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }

    private static int gcd(int[] a) {
        int result = a[0];
        for (int i = 1; i < a.length; i++) {
            result = gcd(result, a[i]);
        }
        return result;
    }

    /**
     * Create your own SettingValue object to make the management of plotworld configuration easier
     */
    public abstract static class SettingValue<T> {

        private final String type;

        public SettingValue(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }

        public abstract T parseString(String string);

        public abstract boolean validateValue(String string);
    }
}
