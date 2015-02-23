package com.intellectualcrafters.jnbt;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

/**
 * The {@code TAG_List} tag.
 */
public final class ListTag extends Tag {
    private final Class<? extends Tag> type;
    private final List<Tag> value;

    /**
     * Creates the tag with an empty name.
     *
     * @param type  the type of tag
     * @param value the value of the tag
     */
    public ListTag(final Class<? extends Tag> type, final List<? extends Tag> value) {
        super();
        checkNotNull(value);
        this.type = type;
        this.value = Collections.unmodifiableList(value);
    }

    /**
     * Creates the tag.
     *
     * @param name  the name of the tag
     * @param type  the type of tag
     * @param value the value of the tag
     */
    public ListTag(final String name, final Class<? extends Tag> type, final List<? extends Tag> value) {
        super(name);
        checkNotNull(value);
        this.type = type;
        this.value = Collections.unmodifiableList(value);
    }

    /**
     * Gets the type of item in this list.
     *
     * @return The type of item in this list.
     */
    public Class<? extends Tag> getType() {
        return this.type;
    }

    @Override
    public List<Tag> getValue() {
        return this.value;
    }

    /**
     * Create a new list tag with this tag's name and type.
     *
     * @param list the new list
     *
     * @return a new list tag
     */
    public ListTag setValue(final List<Tag> list) {
        return new ListTag(getName(), getType(), list);
    }

    /**
     * Get the tag if it exists at the given index.
     *
     * @param index the index
     *
     * @return the tag or null
     */
    @Nullable
    public Tag getIfExists(final int index) {
        try {
            return this.value.get(index);
        } catch (final NoSuchElementException e) {
            return null;
        }
    }

    /**
     * Get a byte array named with the given index. <p/> <p> If the index does not exist or its value is not a byte
     * array tag, then an empty byte array will be returned. </p>
     *
     * @param index the index
     *
     * @return a byte array
     */
    public byte[] getByteArray(final int index) {
        final Tag tag = getIfExists(index);
        if (tag instanceof ByteArrayTag) {
            return ((ByteArrayTag) tag).getValue();
        } else {
            return new byte[0];
        }
    }

    /**
     * Get a byte named with the given index. <p/> <p> If the index does not exist or its value is not a byte tag, then
     * {@code 0} will be returned. </p>
     *
     * @param index the index
     *
     * @return a byte
     */
    public byte getByte(final int index) {
        final Tag tag = getIfExists(index);
        if (tag instanceof ByteTag) {
            return ((ByteTag) tag).getValue();
        } else {
            return (byte) 0;
        }
    }

    /**
     * Get a double named with the given index. <p/> <p> If the index does not exist or its value is not a double tag,
     * then {@code 0} will be returned. </p>
     *
     * @param index the index
     *
     * @return a double
     */
    public double getDouble(final int index) {
        final Tag tag = getIfExists(index);
        if (tag instanceof DoubleTag) {
            return ((DoubleTag) tag).getValue();
        } else {
            return 0;
        }
    }

    /**
     * Get a double named with the given index, even if it's another type of number. <p/> <p> If the index does not
     * exist or its value is not a number, then {@code 0} will be returned. </p>
     *
     * @param index the index
     *
     * @return a double
     */
    public double asDouble(final int index) {
        final Tag tag = getIfExists(index);
        if (tag instanceof ByteTag) {
            return ((ByteTag) tag).getValue();
        } else if (tag instanceof ShortTag) {
            return ((ShortTag) tag).getValue();
        } else if (tag instanceof IntTag) {
            return ((IntTag) tag).getValue();
        } else if (tag instanceof LongTag) {
            return ((LongTag) tag).getValue();
        } else if (tag instanceof FloatTag) {
            return ((FloatTag) tag).getValue();
        } else if (tag instanceof DoubleTag) {
            return ((DoubleTag) tag).getValue();
        } else {
            return 0;
        }
    }

    /**
     * Get a float named with the given index. <p/> <p> If the index does not exist or its value is not a float tag,
     * then {@code 0} will be returned. </p>
     *
     * @param index the index
     *
     * @return a float
     */
    public float getFloat(final int index) {
        final Tag tag = getIfExists(index);
        if (tag instanceof FloatTag) {
            return ((FloatTag) tag).getValue();
        } else {
            return 0;
        }
    }

    /**
     * Get a {@code int[]} named with the given index. <p/> <p> If the index does not exist or its value is not an int
     * array tag, then an empty array will be returned. </p>
     *
     * @param index the index
     *
     * @return an int array
     */
    public int[] getIntArray(final int index) {
        final Tag tag = getIfExists(index);
        if (tag instanceof IntArrayTag) {
            return ((IntArrayTag) tag).getValue();
        } else {
            return new int[0];
        }
    }

    /**
     * Get an int named with the given index. <p/> <p> If the index does not exist or its value is not an int tag, then
     * {@code 0} will be returned. </p>
     *
     * @param index the index
     *
     * @return an int
     */
    public int getInt(final int index) {
        final Tag tag = getIfExists(index);
        if (tag instanceof IntTag) {
            return ((IntTag) tag).getValue();
        } else {
            return 0;
        }
    }

    /**
     * Get an int named with the given index, even if it's another type of number. <p/> <p> If the index does not exist
     * or its value is not a number, then {@code 0} will be returned. </p>
     *
     * @param index the index
     *
     * @return an int
     */
    public int asInt(final int index) {
        final Tag tag = getIfExists(index);
        if (tag instanceof ByteTag) {
            return ((ByteTag) tag).getValue();
        } else if (tag instanceof ShortTag) {
            return ((ShortTag) tag).getValue();
        } else if (tag instanceof IntTag) {
            return ((IntTag) tag).getValue();
        } else if (tag instanceof LongTag) {
            return ((LongTag) tag).getValue().intValue();
        } else if (tag instanceof FloatTag) {
            return ((FloatTag) tag).getValue().intValue();
        } else if (tag instanceof DoubleTag) {
            return ((DoubleTag) tag).getValue().intValue();
        } else {
            return 0;
        }
    }

    /**
     * Get a list of tags named with the given index. <p/> <p> If the index does not exist or its value is not a list
     * tag, then an empty list will be returned. </p>
     *
     * @param index the index
     *
     * @return a list of tags
     */
    public List<Tag> getList(final int index) {
        final Tag tag = getIfExists(index);
        if (tag instanceof ListTag) {
            return ((ListTag) tag).getValue();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get a {@code TagList} named with the given index. <p/> <p> If the index does not exist or its value is not a list
     * tag, then an empty tag list will be returned. </p>
     *
     * @param index the index
     *
     * @return a tag list instance
     */
    public ListTag getListTag(final int index) {
        final Tag tag = getIfExists(index);
        if (tag instanceof ListTag) {
            return (ListTag) tag;
        } else {
            return new ListTag(StringTag.class, Collections.<Tag> emptyList());
        }
    }

    /**
     * Get a list of tags named with the given index. <p/> <p> If the index does not exist or its value is not a list
     * tag, then an empty list will be returned. If the given index references a list but the list of of a different
     * type, then an empty list will also be returned. </p>
     *
     * @param index    the index
     * @param listType the class of the contained type
     * @param <T>      the NBT type
     *
     * @return a list of tags
     */
    @SuppressWarnings("unchecked")
    public <T extends Tag> List<T> getList(final int index, final Class<T> listType) {
        final Tag tag = getIfExists(index);
        if (tag instanceof ListTag) {
            final ListTag listTag = (ListTag) tag;
            if (listTag.getType().equals(listType)) {
                return (List<T>) listTag.getValue();
            } else {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get a long named with the given index. <p/> <p> If the index does not exist or its value is not a long tag, then
     * {@code 0} will be returned. </p>
     *
     * @param index the index
     *
     * @return a long
     */
    public long getLong(final int index) {
        final Tag tag = getIfExists(index);
        if (tag instanceof LongTag) {
            return ((LongTag) tag).getValue();
        } else {
            return 0L;
        }
    }

    /**
     * Get a long named with the given index, even if it's another type of number. <p/> <p> If the index does not exist
     * or its value is not a number, then {@code 0} will be returned. </p>
     *
     * @param index the index
     *
     * @return a long
     */
    public long asLong(final int index) {
        final Tag tag = getIfExists(index);
        if (tag instanceof ByteTag) {
            return ((ByteTag) tag).getValue();
        } else if (tag instanceof ShortTag) {
            return ((ShortTag) tag).getValue();
        } else if (tag instanceof IntTag) {
            return ((IntTag) tag).getValue();
        } else if (tag instanceof LongTag) {
            return ((LongTag) tag).getValue();
        } else if (tag instanceof FloatTag) {
            return ((FloatTag) tag).getValue().longValue();
        } else if (tag instanceof DoubleTag) {
            return ((DoubleTag) tag).getValue().longValue();
        } else {
            return 0;
        }
    }

    /**
     * Get a short named with the given index. <p/> <p> If the index does not exist or its value is not a short tag,
     * then {@code 0} will be returned. </p>
     *
     * @param index the index
     *
     * @return a short
     */
    public short getShort(final int index) {
        final Tag tag = getIfExists(index);
        if (tag instanceof ShortTag) {
            return ((ShortTag) tag).getValue();
        } else {
            return 0;
        }
    }

    /**
     * Get a string named with the given index. <p/> <p> If the index does not exist or its value is not a string tag,
     * then {@code ""} will be returned. </p>
     *
     * @param index the index
     *
     * @return a string
     */
    public String getString(final int index) {
        final Tag tag = getIfExists(index);
        if (tag instanceof StringTag) {
            return ((StringTag) tag).getValue();
        } else {
            return "";
        }
    }

    @Override
    public String toString() {
        final String name = getName();
        String append = "";
        if ((name != null) && !name.equals("")) {
            append = "(\"" + this.getName() + "\")";
        }
        final StringBuilder bldr = new StringBuilder();
        bldr.append("TAG_List").append(append).append(": ").append(this.value.size()).append(" entries of type ").append(NBTUtils.getTypeName(this.type)).append("\r\n{\r\n");
        for (final Tag t : this.value) {
            bldr.append("   ").append(t.toString().replaceAll("\r\n", "\r\n   ")).append("\r\n");
        }
        bldr.append("}");
        return bldr.toString();
    }
}
