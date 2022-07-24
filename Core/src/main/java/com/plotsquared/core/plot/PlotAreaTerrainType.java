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
package com.plotsquared.core.plot;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum PlotAreaTerrainType {

    /**
     * Don't use any vanilla world elements.
     */
    NONE,

    /**
     * Generate vanilla ores.
     */
    ORE,

    /**
     * Generate everything using the vanilla generator but with PlotSquared roads.
     */
    ROAD,

    /**
     * Generate everything using the vanilla generator.
     */
    ALL;

    private static final Map<String, PlotAreaTerrainType> types = Stream.of(values())
            .collect(Collectors.toMap(e -> e.toString().toLowerCase(), Function.identity()));

    public static Optional<PlotAreaTerrainType> fromString(String typeString) {
        return Optional.ofNullable(types.get(typeString.toLowerCase()));
    }

    @Deprecated
    public static Optional<PlotAreaTerrainType> fromLegacyInt(int typeId) {
        if (typeId < 0 || typeId >= values().length) {
            return Optional.empty();
        }
        return Optional.of(values()[typeId]);
    }
}
