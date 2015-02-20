package com.intellectualcrafters.jnbt;

/**
 * The {@code TAG_Float} tag.
 */
public final class FloatTag extends Tag {
    private final float value;
    
    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public FloatTag(final float value) {
        super();
        this.value = value;
    }
    
    /**
     * Creates the tag.
     *
     * @param name  the name of the tag
     * @param value the value of the tag
     */
    public FloatTag(final String name, final float value) {
        super(name);
        this.value = value;
    }
    
    @Override
    public Float getValue() {
        return this.value;
    }
    
    @Override
    public String toString() {
        final String name = getName();
        String append = "";
        if ((name != null) && !name.equals("")) {
            append = "(\"" + this.getName() + "\")";
        }
        return "TAG_Float" + append + ": " + this.value;
    }
}
