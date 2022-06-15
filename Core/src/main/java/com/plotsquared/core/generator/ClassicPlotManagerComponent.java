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
package com.plotsquared.core.generator;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

/**
 * Plot components for {@link ClassicPlotManager}
 */
public enum ClassicPlotManagerComponent {
    FLOOR,
    WALL,
    ALL,
    AIR,
    MAIN,
    MIDDLE,
    OUTLINE,
    BORDER;

    public static String[] stringValues() {
        final ClassicPlotManagerComponent[] classicPlotManagerComponents = values();
        final String[] stringValues = new String[classicPlotManagerComponents.length];
        for (int i = 0; i < classicPlotManagerComponents.length; i++) {
            stringValues[i] = classicPlotManagerComponents[i].name().toLowerCase();
        }
        return stringValues;
    }

    public static Optional<ClassicPlotManagerComponent> fromString(final @NonNull String string) {
        for (final ClassicPlotManagerComponent classicPlotManagerComponent : values()) {
            if (classicPlotManagerComponent.name().equalsIgnoreCase(string)) {
                return Optional.of(classicPlotManagerComponent);
            }
        }
        return Optional.empty();
    }

}
