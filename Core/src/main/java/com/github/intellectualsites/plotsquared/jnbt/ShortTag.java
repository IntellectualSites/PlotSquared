package com.github.intellectualsites.plotsquared.jnbt;

/**
 * The {@code TAG_Short} tag.
 */
public final class ShortTag extends Tag {

    private final short value;

    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public ShortTag(short value) {
        this.value = value;
    }

    /**
     * Creates the tag.
     *
     * @param name  the name of the tag
     * @param value the value of the tag
     */
    public ShortTag(String name, short value) {
        super(name);
        this.value = value;
    }

    @Override public Short getValue() {
        return this.value;
    }

    @Override public String toString() {
        String name = getName();
        String append = "";
        if (name != null && !name.isEmpty()) {
            append = "(\"" + getName() + "\")";
        }
        return "TAG_Short" + append + ": " + this.value;
    }
}
