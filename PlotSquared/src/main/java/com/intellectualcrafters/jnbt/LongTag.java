package com.intellectualcrafters.jnbt;

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
    public LongTag(final long value) {
        super();
        this.value = value;
    }

    /**
     * Creates the tag.
     *
     * @param name  the name of the tag
     * @param value the value of the tag
     */
    public LongTag(final String name, final long value) {
        super(name);
        this.value = value;
    }

    @Override
    public Long getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        final String name = getName();
        String append = "";
        if ((name != null) && !name.equals("")) {
            append = "(\"" + this.getName() + "\")";
        }
        return "TAG_Long" + append + ": " + this.value;
    }

}
