package com.intellectualcrafters.plot;

public class PlotId {
    public int x;
    public int y;

    public PlotId(int x, int y) {
        this.x = x;
        this.y = y;
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
        PlotId other = (PlotId) obj;
        return ((this.x == other.x) && (this.y == other.y));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + this.x;
        result = (prime * result) + this.y;
        return result;
    }
}
