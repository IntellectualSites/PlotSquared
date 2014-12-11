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

import com.intellectualcrafters.plot.config.Configuration.SettingValue;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

/**
 * Configuration Node
 *
 * @author Empire92
 */
public class ConfigurationNode {
    private final String constant;
    private final Object default_value;
    private final String description;
    private final SettingValue type;
    private Object value;

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
            return result != null;
        } catch (final Exception e) {
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
