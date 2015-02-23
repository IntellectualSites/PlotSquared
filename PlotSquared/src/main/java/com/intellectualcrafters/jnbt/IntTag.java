package com.intellectualcrafters.jnbt;

/**
 * The {@code TAG_Int} tag.
 */
public final class IntTag extends Tag {
    private final int value;

    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public IntTag(final int value) {
        super();
        this.value = value;
    }

    /**
     * Creates the tag.
     *
     * @param name  the name of the tag
     * @param value the value of the tag
     */
    public IntTag(final String name, final int value) {
        super(name);
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        final String name = getName();
        String append = "";
        if ((name != null) && !name.equals("")) {
            append = "(\"" + this.getName() + "\")";
        }
        return "TAG_Int" + append + ": " + this.value;
    }
}
