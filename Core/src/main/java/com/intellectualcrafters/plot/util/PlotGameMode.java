package com.intellectualcrafters.plot.util;

public enum PlotGameMode {
    NOT_SET(-1, ""), SURVIVAL(0, "survival"), CREATIVE(1, "creative"), ADVENTURE(2,
        "adventure"), SPECTATOR(3, "spectator");

    private final int id;
    private final String name;

    PlotGameMode(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * The magic-value id of the GameMode.
     *
     * @return the GameMode id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Get the name of this GameMode
     *
     * @return the GameMode name
     */
    public String getName() {
        return this.name;
    }
}
