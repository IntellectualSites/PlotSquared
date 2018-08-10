package com.intellectualcrafters.jnbt;

/**
 * The {@code TAG_Byte} tag.
 */
public final class ByteTag extends Tag {

    private final byte value;

    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public ByteTag(byte value) {
        this.value = value;
    }

    /**
     * Creates the tag.
     *
     * @param name  the name of the tag
     * @param value the value of the tag
     */
    public ByteTag(String name, byte value) {
        super(name);
        this.value = value;
    }

    @Override public Byte getValue() {
        return this.value;
    }

    @Override public String toString() {
        String name = getName();
        String append = "";
        if (name != null && !name.isEmpty()) {
            append = "(\"" + getName() + "\")";
        }
        return "TAG_Byte" + append + ": " + this.value;
    }
}
