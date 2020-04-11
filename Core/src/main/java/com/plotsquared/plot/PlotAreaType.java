package com.plotsquared.plot;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum PlotAreaType {
    NORMAL,
    AUGMENTED,
    PARTIAL;

    private static final Map<String, PlotAreaType> types = Stream.of(values())
            .collect(Collectors.toMap(e -> e.toString().toLowerCase(), Function.identity()));

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
}
