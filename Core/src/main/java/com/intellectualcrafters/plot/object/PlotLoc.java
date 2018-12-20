package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.util.StringMan;

public class PlotLoc {
    public int x;
    public int z;

    public PlotLoc(int x, int z) {
        this.x = x;
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
                return new PlotLoc(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
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
        result = (prime * result) + this.z;
        return result;
    }

    @Override
    public String toString() {
        return this.x + "," + this.z;
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
        return (this.x == other.x) && (this.z == other.z);
    }
}
