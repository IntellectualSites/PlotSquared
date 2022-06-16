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

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum PlotAreaType {
    NORMAL(TranslatableCaption.of("plotareatype.plot_area_type_normal")),
    AUGMENTED(TranslatableCaption.of("plotareatype.plot_area_type_augmented")),
    PARTIAL(TranslatableCaption.of("plotareatype.plot_area_type_partial"));

    private static final Map<String, PlotAreaType> types = Stream.of(values())
            .collect(Collectors.toMap(e -> e.toString().toLowerCase(), Function.identity()));
    private final Caption description;

    PlotAreaType(final @NonNull Caption description) {
        this.description = description;
    }

    public static Map<PlotAreaType, Caption> getDescriptionMap() {
        return Stream.of(values()).collect(Collectors.toMap(e -> e, PlotAreaType::getDescription));
    }

    public static Optional<PlotAreaType> fromString(String typeName) {
        return Optional.ofNullable(types.get(typeName.toLowerCase()));
    }

    @Deprecated
    public static Optional<PlotAreaType> fromLegacyInt(int typeId) {
        if (typeId < 0 || typeId >= values().length) {
            return Optional.empty();
        }
        return Optional.of(values()[typeId]);
    }

    public Caption getDescription() {
        return this.description;
    }
}
