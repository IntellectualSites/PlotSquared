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

package com.plotsquared.core.plot;

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum PlotAreaType {
    NORMAL(TranslatableCaption.of("plotareatype.plot_area_type_normal")),
    AUGMENTED(TranslatableCaption.of("plotareatype.plot_area_type_augmented")),
    PARTIAL(TranslatableCaption.of("plotareatype.plot_area_type_partial"));

    private final Caption description;

    private static final Map<String, PlotAreaType> types = Stream.of(values())
        .collect(Collectors.toMap(e -> e.toString().toLowerCase(), Function.identity()));

    PlotAreaType(@Nonnull final Caption description) {
        this.description = description;
    }

    public static Map<PlotAreaType, Caption> getDescriptionMap() {
        return Stream.of(values()).collect(Collectors.toMap(e -> e, PlotAreaType::getDescription));
    }

    public static Optional<PlotAreaType> fromString(String typeName) {
        return Optional.ofNullable(types.get(typeName.toLowerCase()));
    }

    @Deprecated public static Optional<PlotAreaType> fromLegacyInt(int typeId) {
        if (typeId < 0 || typeId >= values().length) {
            return Optional.empty();
        }
        return Optional.of(values()[typeId]);
    }

    public Caption getDescription() {
        return this.description;
    }
}
