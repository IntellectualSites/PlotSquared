package com.intellectualcrafters.plot;

public class PlotId {
    /**
     * x value
     */
    public Integer x;
    /**
     * y value
     */
    public Integer y;

    /**
     * PlotId class (PlotId x,y values do not correspond to Block locations)
     *
     * @param x
     *            The plot x coordinate
     * @param y
     *            The plot y coordinate
     */
    public PlotId(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlotId other = (PlotId) obj;
        return ((this.x == other.x) && (this.y == other.y));
    }

    @Override
    public String toString() {
        return this.x + ";" + this.y;
    }

    @Override
    public int hashCode() {
        if (x >= 0) {
            if (y >= 0) {
                return x*x + 3*x + 2*x*y + y + y*y;
            }
            else {
                int y1 = -y;
                return x*x + 3*x + 2*x*y1 + y1 + y1*y1 + 1;
            }
        }
        else {
            int x1 = -x;
            if (y >= 0) {
                return -(x1*x1 + 3*x1 + 2*x1*y + y + y*y);
            }
            else {
                int y1 = -y;
                return -(x1*x1 + 3*x1 + 2*x1*y1 + y1 + y1*y1 + 1);
            }
        }
    }
}
