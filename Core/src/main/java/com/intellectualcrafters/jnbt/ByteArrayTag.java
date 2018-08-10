package com.intellectualcrafters.jnbt;

/**
 * The {@code TAG_Byte_Array} tag.
 */
public final class ByteArrayTag extends Tag {

    private final byte[] value;

    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public ByteArrayTag(byte[] value) {
        this.value = value;
    }

    /**
     * Creates the tag.
     *
     * @param name  the name of the tag
     * @param value the value of the tag
     */
    public ByteArrayTag(String name, byte[] value) {
        super(name);
        this.value = value;
    }

    @Override public byte[] getValue() {
        return this.value;
    }

    @Override public String toString() {
        StringBuilder hex = new StringBuilder();
        for (byte b : this.value) {
            String hexDigits = Integer.toHexString(b).toUpperCase();
            if (hexDigits.length() == 1) {
                hex.append("0");
            }
            hex.append(hexDigits).append(" ");
        }
        String name = getName();
        String append = "";
        if (name != null && !name.isEmpty()) {
            append = "(\"" + getName() + "\")";
        }
        return "TAG_Byte_Array" + append + ": " + hex;
    }
}
