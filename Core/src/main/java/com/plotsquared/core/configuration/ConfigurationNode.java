/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.configuration;

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.plot.BlockBucket;
import com.plotsquared.core.util.StringMan;
import com.sk89q.worldedit.world.block.BlockState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Configuration Node.
 */
public class ConfigurationNode {

    private final String constant;
    private final Object defaultValue;
    private final Caption description;
    private final ConfigurationUtil.SettingValue<?> type;
    private final Collection<String> suggestions;
    private Object value;

    public ConfigurationNode(
            String constant, Object defaultValue, Caption description,
            ConfigurationUtil.SettingValue<?> type
    ) {
        this(constant, defaultValue, description, type, new ArrayList<>());
    }

    public ConfigurationNode(
            String constant, Object defaultValue, Caption description,
            ConfigurationUtil.SettingValue<?> type, Collection<String> suggestions
    ) {
        this.constant = constant;
        this.defaultValue = defaultValue;
        this.description = description;
        this.value = defaultValue;
        this.type = type;
        this.suggestions = suggestions;
    }

    public ConfigurationUtil.SettingValue<?> getType() {
        return this.type;
    }

    public boolean isValid(String string) {
        try {
            Object result = this.type.parseString(string);
            return result != null;
        } catch (Exception e) {
            if (e instanceof ConfigurationUtil.UnknownBlockException) {
                throw e;
            }
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
        if (this.value instanceof BlockBucket) {
            return this.value.toString();
        }
        if (this.value instanceof BlockState) {
            return this.value.toString();
        }
        return this.value;
    }

    public Object getDefaultValue() {
        if (this.defaultValue instanceof Object[]) {
            return StringMan.join((Object[]) this.defaultValue, ",");
        }
        return this.defaultValue;
    }

    public String getConstant() {
        return this.constant;
    }

    public Caption getDescription() {
        return this.description;
    }

    public Collection<String> getSuggestions() {
        return this.suggestions;
    }

}
