package com.plotsquared.core.location;

public enum Direction {
    ALL(-1, "all"), NORTH(0, "north"), EAST(1, "east"), SOUTH(2, "south"), WEST(3,
        "west"), NORTHEAST(4, "northeast"), SOUTHEAST(5, "southeast"), SOUTHWEST(6,
        "southwest"), NORTHWEST(7, "northwest"),
    ;


    private int index;
    private String name;

    Direction(int index, String name) {

        this.index = index;
        this.name = name;
    }

    public static Direction getFromIndex(int index) {
        for (Direction value : values()) {
            if (value.getIndex() == index) {
                return value;
            }
        }
        return NORTH;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }
}
