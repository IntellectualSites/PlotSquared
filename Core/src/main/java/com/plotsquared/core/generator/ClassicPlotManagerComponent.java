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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.plotsquared.core.generator;

import javax.annotation.Nonnull;
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

    public static Optional<ClassicPlotManagerComponent> fromString(@Nonnull final String string) {
        for (final ClassicPlotManagerComponent classicPlotManagerComponent : values()) {
            if (classicPlotManagerComponent.name().equalsIgnoreCase(string)) {
                return Optional.of(classicPlotManagerComponent);
            }
        }
        return Optional.empty();
    }

}
