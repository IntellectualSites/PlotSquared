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
import java.util.Arrays;
import java.util.List;

import com.intellectualcrafters.plot.config.Configuration.SettingValue;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.util.StringMan;

/**
 * Configuration Node
 *
 */
public class ConfigurationNode
{
    private final String constant;
    private final Object default_value;
    private final String description;
    private final SettingValue type;
    private Object value;

    public ConfigurationNode(final String constant, final Object default_value, final String description, final SettingValue type, final boolean required)
    {
        this.constant = constant;
        this.default_value = default_value;
        this.description = description;
        value = default_value;
        this.type = type;
    }

    public SettingValue getType()
    {
        return type;
    }

    public boolean isValid(final String string)
    {
        try
        {
            final Object result = type.parseString(string);
            return result != null;
        }
        catch (final Exception e)
        {
            return false;
        }
    }

    public boolean setValue(final String string)
    {
        if (!type.validateValue(string)) { return false; }
        value = type.parseString(string);
        return true;
    }

    public Object getValue()
    {
        if (value instanceof String[])
        {
            return Arrays.asList((String[]) value);
        }
        else if (value instanceof Object[])
        {
            final List<String> values = new ArrayList<String>();
            for (final Object value : (Object[]) this.value)
            {
                values.add(value.toString());
            }
            return values;
        }
        else if (value instanceof PlotBlock) { return value.toString(); }
        return value;
    }

    public String getConstant()
    {
        return constant;
    }

    public Object getDefaultValue()
    {
        if (default_value instanceof Object[]) { return StringMan.join((Object[]) default_value, ","); }
        return default_value;
    }

    public String getDescription()
    {
        return description;
    }
}
