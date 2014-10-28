package com.intellectualcrafters.jnbt;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@code TAG_String} tag.
 */
public final class StringTag extends Tag {

    private final String value;

    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public StringTag(String value) {
        super();
        checkNotNull(value);
        this.value = value;
    }

    /**
     * Creates the tag.
     *
     * @param name the name of the tag
     * @param value the value of the tag
     */
    public StringTag(String name, String value) {
        super(name);
        checkNotNull(value);
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        String name = getName();
        String append = "";
        if (name != null && !name.equals("")) {
            append = "(\"" + this.getName() + "\")";
        }
        return "TAG_String" + append + ": " + value;
    }

}
