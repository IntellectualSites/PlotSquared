package com.intellectualcrafters.plot;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.bukkit.block.Biome;

public abstract class PlotWorld2 {
    
    public abstract SettingNode[] getSettingNodes();

    public static final SettingValue STRING = new SettingValue("STRING") {
        @Override
        public boolean validateValue(String string) {
            return true;
        }
        
        @Override
        public Object parseValue(String string) {
            return string;
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
        public Object parseValue(String string) {
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
        public Object parseValue(String string) {
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
        public Object parseValue(String string) {
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
        public Object parseValue(String string) {
            return Biome.valueOf(string.toUpperCase());
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
                } else {
                    Short.parseShort(string);
                }
                return true;
            }
            catch (Exception e) {
                return false;
            }
        }
        
        @Override
        public Object parseValue(String string) {
            return string;
        }
    };
    
    public static final SettingValue BLOCKLIST = new SettingValue("BLOCKLIST") {
        @Override
        public boolean validateValue(String string) {
            try {
                for (String block : string.split(",")) {
                    if (block.contains(":")) {
                        String[] split = block.split(":");
                        Short.parseShort(split[0]);
                        Short.parseShort(split[1]);
                    } else {
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
        public Object parseValue(String string) {
            return string.split(",");
        }
    };
    
    public static abstract class SettingValue {
        private String type;
        
        public SettingValue(String type) {
            this.type = type;
        }
        
        public String getType() {
            return this.type;
        }
        
        public abstract Object parseValue(String string);
        
        public abstract boolean validateValue(String string);
    }
    
    public static class SettingNode {
        private String constant;
        private Object default_value;
        private String description;
        private Object value = 0;
        private SettingValue type;

        public SettingNode(String constant, Object default_value, String description, SettingValue type) {
            this.constant = constant;
            this.default_value = default_value;
            this.description = description;
            this.type = type;
        }

        public String getType() {
            return this.type.getType();
        }

        public boolean setValue(String string) {
            if (!type.validateValue(string)) {
                return false;
            }
            this.value = type.parseValue(string);
            return true;
        }
        
        public Object getValue() {
            if (this.value instanceof String[]) {
                return Arrays.asList((String[]) this.value);
            }
            return this.value;
        }

        public String getConstant() {
            return this.constant;
        }

        public Object getDefaultValue() {
            if (this.default_value instanceof String[]) {
                return StringUtils.join((String[]) this.default_value, ",");
            }
            return this.default_value;
        }

        public String getDescription() {
            return this.description;
        }
    }
}
