package com.intellectualcrafters.jnbt;

/**
 * Represents a NBT tag.
 */
public abstract class Tag {

    private final String name;

    /**
     * Create a new tag with an empty name.
     */
    Tag() {
        this("");
    }

    /**
     * Creates the tag with the specified name.
     *
     * @param name
     *            the name
     */
    Tag(String name) {
        if (name == null) {
            name = "";
        }
        this.name = name;
    }

    /**
     * Gets the name of this tag.
     *
     * @return the name of this tag
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Gets the value of this tag.
     *
     * @return the value
     */
    public abstract Object getValue();

}
