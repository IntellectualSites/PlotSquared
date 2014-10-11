package com.intellectualcrafters.plot;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import com.intellectualcrafters.plot.Configuration.SettingValue;

public class ConfigurationNode {
	private String constant;
	private Object default_value;
	private String description;
	private Object value = 0;
	private SettingValue type;

	public ConfigurationNode(String constant, Object default_value,
			String description, SettingValue type, boolean required) {
		this.constant = constant;
		this.default_value = default_value;
		this.description = description;
		this.value = default_value;
		this.type = type;
	}

	public String getType() {
		return this.type.getType();
	}

	public boolean isValid(String string) {
		try {
			Object result = this.type.parseString(string);
			if (result == null) {
				return false;
			}
			return true;
		} catch (Exception e) {
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
