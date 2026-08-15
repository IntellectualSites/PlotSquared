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
package com.plotsquared.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;

import java.util.Collection;

/**
 * A utility class for modifying components.
 *
 * @since 7.0.0
 */
public class ComponentHelper {

    /**
     * Joins multiple {@link Component}s into one final {@link ComponentLike}
     *
     * @param components The components to join
     * @param delimiter  The delimiter to use between the components
     * @return The joined components
     * @since 7.0.0
     */
    public static ComponentLike join(Collection<? extends ComponentLike> components, Component delimiter) {
        return join(components.toArray(ComponentLike[]::new), delimiter);
    }

    /**
     * Joins multiple {@link ComponentLike}s into one final {@link ComponentLike}
     *
     * @param components The components to join
     * @param delimiter  The delimiter to use between the components
     * @return The joined components
     * @since 7.0.0
     */
    public static Component join(ComponentLike[] components, Component delimiter) {
        TextComponent.Builder builder = Component.text();
        for (int i = 0, j = components.length; i < j; i++) {
            if (i > 0) {
                builder.append(delimiter);
            }
            builder.append(components[i]);
        }
        return builder.build();
    }

}
