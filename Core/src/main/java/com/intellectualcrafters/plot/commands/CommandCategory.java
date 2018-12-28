package com.intellectualcrafters.plot.commands;

/**
 * CommandCategory.
 */
public enum CommandCategory {
    /**
     * Claiming CommandConfig.
     * Such as: /plot claim
     */
    CLAIMING("Claiming"),
    /**
     * Teleportation CommandConfig.
     * Such as: /plot visit
     */
    TELEPORT("Teleport"),
    /**
     * Protection.
     */
    SETTINGS("Protection"),
    /**
     * Chat.
     */
    CHAT("Chat"),
    /**
     * Web.
     */
    SCHEMATIC("Web"),
    /**
     * Cosmetic.
     */
    APPEARANCE("Cosmetic"),
    /**
     * Information CommandConfig.
     * Such as: /plot info
     */
    INFO("Info"),
    /**
     * Debug CommandConfig.
     * Such as: /plot debug
     */
    DEBUG("Debug"),
    /**
     * Administration commands.
     */
    ADMINISTRATION("Admin");
    /**
     * The category name (Readable).
     */
    private final String name;
    /**
     * Constructor.
     *
     * @param name readable name
     */
    CommandCategory(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
