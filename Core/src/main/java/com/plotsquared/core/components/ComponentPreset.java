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
package com.plotsquared.core.components;

import com.plotsquared.core.configuration.serialization.ConfigurationSerializable;
import com.plotsquared.core.configuration.serialization.SerializableAs;
import com.plotsquared.core.generator.ClassicPlotManagerComponent;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A preset that can be used to set a component from
 * the component GUI
 */
@SerializableAs("preset")
public record ComponentPreset(
        ClassicPlotManagerComponent component,
        String pattern,
        double cost,
        String permission,
        String displayName,
        List<String> description,
        ItemType icon
) implements ConfigurationSerializable {

    @SuppressWarnings("unchecked")
    public static ComponentPreset deserialize(final @NonNull Map<String, Object> map) {
        final ClassicPlotManagerComponent classicPlotManagerComponent = ClassicPlotManagerComponent
                .fromString(map.getOrDefault("component", "").toString()).orElseThrow(() ->
                        new IllegalArgumentException("The preset in components.yml needs a valid target component, got: " + map.get(
                                "component")));
        final String pattern = map.getOrDefault("pattern", "").toString();
        final double cost = Double.parseDouble(map.getOrDefault("cost", "0.0").toString());
        final String permission = map.getOrDefault("permission", "").toString();
        final String displayName = map.getOrDefault("name", "New Package").toString();
        final List<String> description = (List<String>) map.getOrDefault("description", new ArrayList<>());
        final ItemType icon = ItemTypes.get(map.getOrDefault("icon", "dirt").toString());
        return new ComponentPreset(classicPlotManagerComponent, pattern, cost, permission,
                displayName, description, icon
        );
    }

    @Override
    public Map<String, Object> serialize() {
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
