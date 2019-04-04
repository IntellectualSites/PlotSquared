package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Getter @RequiredArgsConstructor public class PlotLoc {

    private final int x;
    private final int y;
    private final int z;

    public PlotLoc(int x, int z) {
        this(x, -1, z);
    }

    @Nullable public static PlotLoc fromString(final String input) {
        if (input == null || "side".equalsIgnoreCase(input)) {
            return null;
        } else if (StringMan.isEqualIgnoreCaseToAny(input, "center", "middle")) {
            return new PlotLoc(Integer.MAX_VALUE, Integer.MAX_VALUE);
        } else {
            try {
                String[] split = input.split(",");
                if (split.length == 2) {
                    return new PlotLoc(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                } else if (split.length == 3) {
                    return new PlotLoc(Integer.parseInt(split[0]), Integer.parseInt(split[1]),
                        Integer.parseInt(split[2]));
                } else {
                    throw new IllegalArgumentException(
                        String.format("Unable to deserialize: %s", input));
                }
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + this.x;
        result = (prime * result) + this.y;
        result = (prime * result) + this.z;
        return result;
    }

    @Override public String toString() {
        if (this.y == -1) {
            return String.format("%d,%d", x, z);
        }
        return String.format("%d,%d,%d", x, y, z);
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PlotLoc other = (PlotLoc) obj;
        return (this.x == other.x) && (this.y == other.y) && (this.z == other.z);
    }
}
