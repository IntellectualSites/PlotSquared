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
     * Generate everything using the vanilla generator but with PS roads.
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
