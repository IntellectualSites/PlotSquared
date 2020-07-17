/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.components;

import com.plotsquared.core.configuration.serialization.ConfigurationSerializable;
import com.plotsquared.core.configuration.serialization.SerializableAs;
import com.plotsquared.core.generator.ClassicPlotManagerComponent;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A preset that can be used to set a component from
 * the component GUI
 */
@SerializableAs("preset")
public class ComponentPreset implements ConfigurationSerializable {

    private final ClassicPlotManagerComponent component;
    private final String pattern;
    private final double cost;
    private final String permission;
    private final String displayName;
    private final List<String> description;
    private final ItemType icon;

    public ComponentPreset(ClassicPlotManagerComponent component, String pattern, double cost,
        String permission, String displayName, List<String> description, final ItemType icon) {
        this.component = component;
        this.pattern = pattern;
        this.cost = cost;
        this.permission = permission;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }

    public static ComponentPreset deserialize(@Nonnull final Map<String, Object> map) {
        final ClassicPlotManagerComponent classicPlotManagerComponent = ClassicPlotManagerComponent
            .fromString(map.getOrDefault("component", "").toString()).orElseThrow(() ->
                new IllegalArgumentException("The preset needs a valid target component"));
        final String pattern = map.getOrDefault("pattern", "").toString();
        final double cost = Double.parseDouble(map.getOrDefault("cost", "0.0").toString());
        final String permission = map.getOrDefault("permission", "").toString();
        final String displayName = map.getOrDefault("name", "New Package").toString();
        final List<String> description = (List<String>) map.getOrDefault("description", new ArrayList<>());
        final ItemType icon = ItemTypes.get(map.getOrDefault("icon", "dirt").toString());
        return new ComponentPreset(classicPlotManagerComponent, pattern, cost, permission,
            displayName, description, icon);
    }

    public ClassicPlotManagerComponent getComponent() {
        return this.component;
    }

    public String getPattern() {
        return this.pattern;
    }

    public double getCost() {
        return this.cost;
    }

    public String getPermission() {
        return this.permission;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public List<String> getDescription() {
        return this.description;
    }

    public ItemType getIcon() {
        return this.icon;
    }

    @Override public Map<String, Object> serialize() {
        final Map<String, Object> map = new HashMap<>();
        map.put("component", this.component.name().toLowerCase());
        map.put("pattern", this.pattern);
        map.put("cost", this.cost);
        map.put("permission", this.permission);
        map.put("name", this.displayName);
        map.put("description", this.description);
        map.put("icon", this.icon.getId().replace("minecraft:", ""));
        return map;
    }

}
