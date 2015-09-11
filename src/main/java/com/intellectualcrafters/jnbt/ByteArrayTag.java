package com.intellectualcrafters.jnbt;

/**
 * The {@code TAG_Byte_Array} tag.
 */
public final class ByteArrayTag extends Tag
{
    private final byte[] value;

    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public ByteArrayTag(final byte[] value)
    {
        super();
        this.value = value;
    }

    /**
     * Creates the tag.
     *
     * @param name  the name of the tag
     * @param value the value of the tag
     */
    public ByteArrayTag(final String name, final byte[] value)
    {
        super(name);
        this.value = value;
    }

    @Override
    public byte[] getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        final StringBuilder hex = new StringBuilder();
        for (final byte b : value)
        {
            final String hexDigits = Integer.toHexString(b).toUpperCase();
            if (hexDigits.length() == 1)
            {
                hex.append("0");
            }
            hex.append(hexDigits).append(" ");
        }
        final String name = getName();
        String append = "";
        if ((name != null) && !name.equals(""))
        {
            append = "(\"" + getName() + "\")";
        }
        return "TAG_Byte_Array" + append + ": " + hex;
    }
}
