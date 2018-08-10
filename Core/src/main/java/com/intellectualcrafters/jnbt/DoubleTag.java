package com.intellectualcrafters.jnbt;

/**
 * The {@code TAG_Double} tag.
 */
public final class DoubleTag extends Tag {

    private final double value;

    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public DoubleTag(double value) {
        this.value = value;
    }

    /**
     * Creates the tag.
     *
     * @param name  the name of the tag
     * @param value the value of the tag
     */
    public DoubleTag(String name, double value) {
        super(name);
        this.value = value;
    }

    @Override public Double getValue() {
        return this.value;
    }

    @Override public String toString() {
        String name = getName();
        String append = "";
        if (name != null && !name.isEmpty()) {
            append = "(\"" + getName() + "\")";
        }
        return "TAG_Double" + append + ": " + this.value;
    }
}
