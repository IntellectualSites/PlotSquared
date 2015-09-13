package com.intellectualcrafters.jnbt;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code TAG_Compound} tag.
 */
public final class CompoundTag extends Tag {
    private final Map<String, Tag> value;
    
    /**
     * Creates the tag with an empty name.
     *
     * @param value the value of the tag
     */
    public CompoundTag(final Map<String, Tag> value) {
        super();
        this.value = Collections.unmodifiableMap(value);
    }
    
    /**
     * Creates the tag.
     *
     * @param name  the name of the tag
     * @param value the value of the tag
     */
    public CompoundTag(final String name, final Map<String, Tag> value) {
        super(name);
        this.value = Collections.unmodifiableMap(value);
    }
    
    /**
     * Returns whether this compound tag contains the given key.
     *
     * @param key the given key
     *
     * @return true if the tag contains the given key
     */
    public boolean containsKey(final String key) {
        return value.containsKey(key);
    }
    
    @Override
    public Map<String, Tag> getValue() {
        return value;
    }
    
    /**
     * Return a new compound tag with the given values.
     *
     * @param value the value
     *
     * @return the new compound tag
     */
    public CompoundTag setValue(final Map<String, Tag> value) {
        return new CompoundTag(getName(), value);
    }
    
    /**
     * Create a compound tag builder.
     *
     * @return the builder
     */
    public CompoundTagBuilder createBuilder() {
        return new CompoundTagBuilder(new HashMap<String, Tag>(value));
    }
    
    /**
     * Get a byte array named with the given key. <p> If the key does not exist or its value is not a byte array
     * tag, then an empty byte array will be returned. </p>
     *
     * @param key the key
     *
     * @return a byte array
     */
    public byte[] getByteArray(final String key) {
        final Tag tag = value.get(key);
        if (tag instanceof ByteArrayTag) {
            return ((ByteArrayTag) tag).getValue();
        } else {
            return new byte[0];
        }
    }
    
    /**
     * Get a byte named with the given key.  <p> If the key does not exist or its value is not a byte tag, then
     * {@code 0} will be returned. </p>
     *
     * @param key the key
     *
     * @return a byte
     */
    public byte getByte(final String key) {
        final Tag tag = value.get(key);
        if (tag instanceof ByteTag) {
            return ((ByteTag) tag).getValue();
        } else {
            return (byte) 0;
        }
    }
    
    /**
     * Get a double named with the given key. <p> If the key does not exist or its value is not a double tag, then
     * {@code 0} will be returned. </p>
     *
     * @param key the key
     *
     * @return a double
     */
    public double getDouble(final String key) {
        final Tag tag = value.get(key);
        if (tag instanceof DoubleTag) {
            return ((DoubleTag) tag).getValue();
        } else {
            return 0;
        }
    }
    
    /**
     * Get a double named with the given key, even if it's another type of number. <p> If the key does not exist or
     * its value is not a number, then {@code 0} will be returned. </p>
     *
     * @param key the key
     *
     * @return a double
     */
    public double asDouble(final String key) {
        final Tag tag = value.get(key);
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
     * Get a float named with the given key.  <p> If the key does not exist or its value is not a float tag, then
     * {@code 0} will be returned. </p>
     *
     * @param key the key
     *
     * @return a float
     */
    public float getFloat(final String key) {
        final Tag tag = value.get(key);
        if (tag instanceof FloatTag) {
            return ((FloatTag) tag).getValue();
        } else {
            return 0;
        }
    }
    
    /**
     * Get a {@code int[]} named with the given key.  <p> If the key does not exist or its value is not an int array
     * tag, then an empty array will be returned. </p>
     *
     * @param key the key
     *
     * @return an int array
     */
    public int[] getIntArray(final String key) {
        final Tag tag = value.get(key);
        if (tag instanceof IntArrayTag) {
            return ((IntArrayTag) tag).getValue();
        } else {
            return new int[0];
        }
    }
    
    /**
     * Get an int named with the given key.  <p> If the key does not exist or its value is not an int tag, then
     * {@code 0} will be returned. </p>
     *
     * @param key the key
     *
     * @return an int
     */
    public int getInt(final String key) {
        final Tag tag = value.get(key);
        if (tag instanceof IntTag) {
            return ((IntTag) tag).getValue();
        } else {
            return 0;
        }
    }
    
    /**
     * Get an int named with the given key, even if it's another type of number.  <p> If the key does not exist or
     * its value is not a number, then {@code 0} will be returned. </p>
     *
     * @param key the key
     *
     * @return an int
     */
    public int asInt(final String key) {
        final Tag tag = value.get(key);
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
     * Get a list of tags named with the given key.  <p> If the key does not exist or its value is not a list tag,
     * then an empty list will be returned. </p>
     *
     * @param key the key
     *
     * @return a list of tags
     */
    public List<Tag> getList(final String key) {
        final Tag tag = value.get(key);
        if (tag instanceof ListTag) {
            return ((ListTag) tag).getValue();
        } else {
            return Collections.emptyList();
        }
    }
    
    /**
     * Get a {@code TagList} named with the given key.  <p> If the key does not exist or its value is not a list
     * tag, then an empty tag list will be returned. </p>
     *
     * @param key the key
     *
     * @return a tag list instance
     */
    public ListTag getListTag(final String key) {
        final Tag tag = value.get(key);
        if (tag instanceof ListTag) {
            return (ListTag) tag;
        } else {
            return new ListTag(key, StringTag.class, Collections.<Tag> emptyList());
        }
    }
    
    /**
     * Get a list of tags named with the given key.  <p> If the key does not exist or its value is not a list tag,
     * then an empty list will be returned. If the given key references a list but the list of of a different type, then
     * an empty list will also be returned. </p>
     *
     * @param key      the key
     * @param listType the class of the contained type
     * @param <T>      the type of list
     *
     * @return a list of tags
     */
    @SuppressWarnings("unchecked")
    public <T extends Tag> List<T> getList(final String key, final Class<T> listType) {
        final Tag tag = value.get(key);
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
     * Get a long named with the given key.  <p> If the key does not exist or its value is not a long tag, then
     * {@code 0} will be returned. </p>
     *
     * @param key the key
     *
     * @return a long
     */
    public long getLong(final String key) {
        final Tag tag = value.get(key);
        if (tag instanceof LongTag) {
            return ((LongTag) tag).getValue();
        } else {
            return 0L;
        }
    }
    
    /**
     * Get a long named with the given key, even if it's another type of number.  <p> If the key does not exist or
     * its value is not a number, then {@code 0} will be returned. </p>
     *
     * @param key the key
     *
     * @return a long
     */
    public long asLong(final String key) {
        final Tag tag = value.get(key);
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
            return 0L;
        }
    }
    
    /**
     * Get a short named with the given key.  <p> If the key does not exist or its value is not a short tag, then
     * {@code 0} will be returned. </p>
     *
     * @param key the key
     *
     * @return a short
     */
    public short getShort(final String key) {
        final Tag tag = value.get(key);
        if (tag instanceof ShortTag) {
            return ((ShortTag) tag).getValue();
        } else {
            return 0;
        }
    }
    
    /**
     * Get a string named with the given key.  <p> If the key does not exist or its value is not a string tag, then
     * {@code ""} will be returned. </p>
     *
     * @param key the key
     *
     * @return a string
     */
    public String getString(final String key) {
        final Tag tag = value.get(key);
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
            append = "(\"" + getName() + "\")";
        }
        final StringBuilder bldr = new StringBuilder();
        bldr.append("TAG_Compound").append(append).append(": ").append(value.size()).append(" entries\r\n{\r\n");
        for (final Map.Entry<String, Tag> entry : value.entrySet()) {
            bldr.append("   ").append(entry.getValue().toString().replaceAll("\r\n", "\r\n   ")).append("\r\n");
        }
        bldr.append("}");
        return bldr.toString();
    }
}
