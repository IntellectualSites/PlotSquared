package com.intellectualcrafters.jnbt;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helps create compound tags.
 */
public class CompoundTagBuilder {

    private final Map<String, Tag> entries;

    /**
     * Create a new instance.
     */
    CompoundTagBuilder() {
        this.entries = new HashMap<String, Tag>();
    }

    /**
     * Create a new instance and use the given map (which will be modified).
     *
     * @param value the value
     */
    CompoundTagBuilder(final Map<String, Tag> value) {
        checkNotNull(value);
        this.entries = value;
    }

    /**
     * Create a new builder instance.
     *
     * @return a new builder
     */
    public static CompoundTagBuilder create() {
        return new CompoundTagBuilder();
    }

    /**
     * Put the given key and tag into the compound tag.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder put(final String key, final Tag value) {
        checkNotNull(key);
        checkNotNull(value);
        this.entries.put(key, value);
        return this;
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code ByteArrayTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putByteArray(final String key, final byte[] value) {
        return put(key, new ByteArrayTag(key, value));
    }

    /**
     * Put the given key and value into the compound tag as a {@code ByteTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putByte(final String key, final byte value) {
        return put(key, new ByteTag(key, value));
    }

    /**
     * Put the given key and value into the compound tag as a {@code DoubleTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putDouble(final String key, final double value) {
        return put(key, new DoubleTag(key, value));
    }

    /**
     * Put the given key and value into the compound tag as a {@code FloatTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putFloat(final String key, final float value) {
        return put(key, new FloatTag(key, value));
    }

    /**
     * Put the given key and value into the compound tag as a
     * {@code IntArrayTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putIntArray(final String key, final int[] value) {
        return put(key, new IntArrayTag(key, value));
    }

    /**
     * Put the given key and value into the compound tag as an {@code IntTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putInt(final String key, final int value) {
        return put(key, new IntTag(key, value));
    }

    /**
     * Put the given key and value into the compound tag as a {@code LongTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putLong(final String key, final long value) {
        return put(key, new LongTag(key, value));
    }

    /**
     * Put the given key and value into the compound tag as a {@code ShortTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putShort(final String key, final short value) {
        return put(key, new ShortTag(key, value));
    }

    /**
     * Put the given key and value into the compound tag as a {@code StringTag}.
     *
     * @param key   they key
     * @param value the value
     * @return this object
     */
    public CompoundTagBuilder putString(final String key, final String value) {
        return put(key, new StringTag(key, value));
    }

    /**
     * Put all the entries from the given map into this map.
     *
     * @param value the map of tags
     * @return this object
     */
    public CompoundTagBuilder putAll(final Map<String, ? extends Tag> value) {
        checkNotNull(value);
        for (final Map.Entry<String, ? extends Tag> entry : value.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Build an unnamed compound tag with this builder's entries.
     *
     * @return the new compound tag
     */
    public CompoundTag build() {
        return new CompoundTag(new HashMap<String, Tag>(this.entries));
    }

    /**
     * Build a new compound tag with this builder's entries.
     *
     * @param name the name of the tag
     * @return the created compound tag
     */
    public CompoundTag build(final String name) {
        return new CompoundTag(name, new HashMap<String, Tag>(this.entries));
    }

}
