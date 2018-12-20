package com.intellectualcrafters.plot.config;

import com.intellectualcrafters.plot.config.Configuration.SettingValue;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.StringMan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration Node.
 */
public class ConfigurationNode {

    private final String constant;
    private final Object defaultValue;
    private final String description;
    private final SettingValue type;
    private Object value;

    public ConfigurationNode(String constant, Object defaultValue, String description, SettingValue type) {
        this.constant = constant;
        this.defaultValue = defaultValue;
        this.description = description;
        this.value = defaultValue;
        this.type = type;
    }

    public SettingValue getType() {
        return this.type;
    }

    public boolean isValid(String string) {
        try {
            Object result = this.type.parseString(string);
            return result != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean setValue(String string) {
        if (!this.type.validateValue(string)) {
            return false;
        }
        this.value = this.type.parseString(string);
        return true;
    }

    public Object getValue() {
        if (this.value instanceof String[]) {
            return Arrays.asList((String[]) this.value);
        }
        if (this.value instanceof Object[]) {
            List<String> values = new ArrayList<>();
            for (Object value : (Object[]) this.value) {
                values.add(value.toString());
            }
            return values;
        }
        if (this.value instanceof PlotBlock) {
            return this.value.toString();
        }
        return this.value;
    }

    public String getConstant() {
        return this.constant;
    }

    public Object getDefaultValue() {
        if (this.defaultValue instanceof Object[]) {
            return StringMan.join((Object[]) this.defaultValue, ",");
        }
        return this.defaultValue;
    }

    public String getDescription() {
        return this.description;
    }
}
