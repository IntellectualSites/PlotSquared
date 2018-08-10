package com.github.intellectualsites.plotsquared.jnbt;

/**
 * The {@code TAG_Long} tag.
 */
public final class LongTag extends Tag {

    private final long value;

    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public LongTag(long value) {
        this.value = value;
    }

    /**
     * Creates the tag.
     *
     * @param name  the name of the tag
     * @param value the value of the tag
     */
    public LongTag(String name, long value) {
        super(name);
        this.value = value;
    }

    @Override public Long getValue() {
        return this.value;
    }

    @Override public String toString() {
        String name = getName();
        String append = "";
        if (name != null && !name.isEmpty()) {
            append = "(\"" + getName() + "\")";
        }
        return "TAG_Long" + append + ": " + this.value;
    }
}
