package com.plotsquared.core.location;

import com.plotsquared.core.util.StringMan;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;

//todo better description needed
/**
 * (x,y,z) or (x,z) representation for PlotSquared (hence the "Plot" prefix)
 */
@AllArgsConstructor public final class PlotLoc {

    private final int x;
    private final int y;
    private final int z;

    /**
     * Initialize a new {@link PlotLoc} and set the Y value to {@code -1}
     *
     * @param x X value
     * @param z Z value
     */
    public PlotLoc(final int x, final int z) {
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

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
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
