package com.intellectualcrafters.plot;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import com.intellectualcrafters.plot.Configuration.SettingValue;

public class ConfigurationNode {
    private final String       constant;
    private final Object       default_value;
    private final String       description;
    private Object             value;
    private final SettingValue type;

    public ConfigurationNode(final String constant, final Object default_value, final String description, final SettingValue type, final boolean required) {
        this.constant = constant;
        this.default_value = default_value;
        this.description = description;
        this.value = default_value;
        this.type = type;
    }

    public SettingValue getType() {
        return this.type;
    }

    public boolean isValid(final String string) {
        try {
            final Object result = this.type.parseString(string);
            if (result == null) {
                return false;
            }
            return true;
        }
        catch (final Exception e) {
            return false;
        }
    }

    public boolean setValue(final String string) {
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
