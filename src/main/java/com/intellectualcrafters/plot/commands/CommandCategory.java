package com.intellectualcrafters.plot.commands;

/**
 * CommandCategory
 *
 */
public enum CommandCategory
{
    /**
     * Claiming Commands
     *
     * Such as: /plot claim
     */
    CLAIMING("Claiming"),
    /**
     * Teleportation Commands
     *
     * Such as: /plot visit
     */
    TELEPORT("Teleportation"),
    /**
     * Action Commands
     *
     * Such as: /plot clear
     */
    ACTIONS("Actions"),
    /**
     * Information Commands
     *
     * Such as: /plot info
     */
    INFO("Information"),
    /**
     * Debug Commands
     *
     * Such as: /plot debug
     */
    DEBUG("Debug");
    /**
     * The category name (Readable)
     */
    private final String name;

    /**
     * Constructor
     *
     * @param name readable name
     */
    CommandCategory(final String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
