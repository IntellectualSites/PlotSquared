package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.util.StringMan;

public class PlotLoc {

    public int x;
    public int y;
    public int z;

    public PlotLoc(int x, int z) {
        this.x = x;
        this.y = -1;
        this.z = z;
    }

    public PlotLoc(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static PlotLoc fromString(String input) {
        if ("side".equalsIgnoreCase(input)) {
            return null;
        } else if (StringMan.isEqualIgnoreCaseToAny(input, "center", "middle")) {
            return new PlotLoc(Integer.MAX_VALUE, Integer.MAX_VALUE);
        } else {
            try {
                String[] split = input.split(",");
                if (split.length == 2) {
                    return new PlotLoc(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                } else if (split.length == 3) {
                    return new PlotLoc(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
                } else {
                    throw new  IllegalArgumentException(String.format("Unable to deserialize: %s", input));
                }
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }
    
    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = (prime * result) + this.x;
        result = (prime * result) + this.y;
        result = (prime * result) + this.z;
        return result;
    }

    @Override
    public String toString() {
        if (this.y == -1) {
            return String.format("%d,%d", x, z);
        }
        return String.format("%d,%d,%d", x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PlotLoc other = (PlotLoc) obj;
        return (this.x == other.x) && (this.y == other.y) && (this.z == other.z);
    }
}
