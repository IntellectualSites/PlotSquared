package com.intellectualcrafters.jnbt;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@code TAG_Int_Array} tag.
 */
public final class IntArrayTag extends Tag {

    private final int[] value;

    /**
     * Creates the tag with an empty name.
     *
     * @param value
     *            the value of the tag
     */
    public IntArrayTag(final int[] value) {
        super();
        checkNotNull(value);
        this.value = value;
    }

    /**
     * Creates the tag.
     *
     * @param name
     *            the name of the tag
     * @param value
     *            the value of the tag
     */
    public IntArrayTag(final String name, final int[] value) {
        super(name);
        checkNotNull(value);
        this.value = value;
    }

    @Override
    public int[] getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        final StringBuilder hex = new StringBuilder();
        for (final int b : this.value) {
            final String hexDigits = Integer.toHexString(b).toUpperCase();
            if (hexDigits.length() == 1) {
                hex.append("0");
            }
            hex.append(hexDigits).append(" ");
        }
        final String name = getName();
        String append = "";
        if ((name != null) && !name.equals("")) {
            append = "(\"" + this.getName() + "\")";
        }
        return "TAG_Int_Array" + append + ": " + hex;
    }

}
