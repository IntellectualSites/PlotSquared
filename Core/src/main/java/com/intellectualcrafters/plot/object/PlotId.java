package com.intellectualcrafters.plot.object;

public class PlotId {
    /**
     * x value
     */
    public int x;
    /**
     * y value
     */
    public int y;
    private int hash;
    
    /**
     * PlotId class (PlotId x,y values do not correspond to Block locations)
     *
     * @param x The plot x coordinate
     * @param y The plot y coordinate
     */
    public PlotId(final int x, final int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Get a Plot Id based on a string
     *
     * @param string to create id from
     *
     * @return null if the string is invalid
     */
    public static PlotId fromString(final String string) {
        if (string == null) {
            return null;
        }
        final String[] parts = string.split(";");
        if (parts.length < 2) {
            return null;
        }
        int x;
        int y;
        try {
            x = Integer.parseInt(parts[0]);
            y = Integer.parseInt(parts[1]);
        } catch (final Exception e) {
            return null;
        }
        return new PlotId(x, y);
    }

    /**
     * Get the PlotId from the HashCode<br>
     * Note: Only accurate for small x,z values (short)
     * @param hash
     * @return
     */
    public static PlotId unpair(int hash) {
        return new PlotId(hash >> 16, hash & 0xFFFF);
    }

    /**
     * Get the PlotId in a relative direction
     * 0 = north<br>
     * 1 = east<br>
     * 2 = south<br>
     * 3 = west<br>
     * @param direction
     * @return PlotId
     */
    public PlotId getRelative(final int direction) {
        switch (direction) {
            case 0:
                return new PlotId(this.x, this.y - 1);
            case 1:
                return new PlotId(this.x + 1, this.y);
            case 2:
                return new PlotId(this.x, this.y + 1);
            case 3:
                return new PlotId(this.x - 1, this.y);
        }
        return this;
    }

    /**
     * Get the PlotId in a relative location
     * @param x
     * @param y
     * @return PlotId
     */
    public PlotId getRelative(int x, int y) {
        return new PlotId(this.x + x, this.y + y);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.hashCode() != obj.hashCode()) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PlotId other = (PlotId) obj;
        return x == other.x && y == other.y;
    }

    /**
     * e.g.
     * 5;-6
     * @return
     */
    @Override
    public String toString() {
        return x + ";" + y;
    }

    /**
     * The PlotId object caches the hashcode for faster mapping/fetching/sorting<br>
     *     - Recalculation is required if the x/y values change
     * TODO maybe make x/y values private and add this to the mutators
     */
    public void recalculateHash() {
        hash = 0;
        hashCode();
    }
    
    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = (x << 16) | (y & 0xFFFF);
        }
        return hash;
    }
}
