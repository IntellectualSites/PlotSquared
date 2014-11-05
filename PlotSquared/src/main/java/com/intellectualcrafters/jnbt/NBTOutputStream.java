package com.intellectualcrafters.jnbt;

import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * <p>
 * This class writes <strong>NBT</strong>, or <strong>Named Binary Tag</strong>
 * <code>Tag</code> objects to an underlying <code>OutputStream</code>.
 * </p>
 *
 * <p>
 * The NBT format was created by Markus Persson, and the specification may be
 * found at <a href="http://www.minecraft.net/docs/NBT.txt">
 * http://www.minecraft.net/docs/NBT.txt</a>.
 * </p>
 *
 * @author Graham Edgecombe
 *
 */
public final class NBTOutputStream implements Closeable {

    /**
     * The output stream.
     */
    private final DataOutputStream os;

    /**
     * Creates a new <code>NBTOutputStream</code>, which will write data to the
     * specified underlying output stream.
     *
     * @param os
     *            The output stream.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public NBTOutputStream(final OutputStream os) throws IOException {
        this.os = new DataOutputStream(os);
    }

    /**
     * Writes a tag.
     *
     * @param tag
     *            The tag to write.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public void writeTag(final Tag tag) throws IOException {
        final int type = NBTUtils.getTypeCode(tag.getClass());
        final String name = tag.getName();
        final byte[] nameBytes = name.getBytes(NBTConstants.CHARSET);

        this.os.writeByte(type);
        this.os.writeShort(nameBytes.length);
        this.os.write(nameBytes);

        if (type == NBTConstants.TYPE_END) {
            throw new IOException("Named TAG_End not permitted.");
        }

        writeTagPayload(tag);
    }

    /**
     * Writes tag payload.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeTagPayload(final Tag tag) throws IOException {
        final int type = NBTUtils.getTypeCode(tag.getClass());
        switch (type) {
            case NBTConstants.TYPE_END:
                writeEndTagPayload((EndTag) tag);
                break;
            case NBTConstants.TYPE_BYTE:
                writeByteTagPayload((ByteTag) tag);
                break;
            case NBTConstants.TYPE_SHORT:
                writeShortTagPayload((ShortTag) tag);
                break;
            case NBTConstants.TYPE_INT:
                writeIntTagPayload((IntTag) tag);
                break;
            case NBTConstants.TYPE_LONG:
                writeLongTagPayload((LongTag) tag);
                break;
            case NBTConstants.TYPE_FLOAT:
                writeFloatTagPayload((FloatTag) tag);
                break;
            case NBTConstants.TYPE_DOUBLE:
                writeDoubleTagPayload((DoubleTag) tag);
                break;
            case NBTConstants.TYPE_BYTE_ARRAY:
                writeByteArrayTagPayload((ByteArrayTag) tag);
                break;
            case NBTConstants.TYPE_STRING:
                writeStringTagPayload((StringTag) tag);
                break;
            case NBTConstants.TYPE_LIST:
                writeListTagPayload((ListTag) tag);
                break;
            case NBTConstants.TYPE_COMPOUND:
                writeCompoundTagPayload((CompoundTag) tag);
                break;
            case NBTConstants.TYPE_INT_ARRAY:
                writeIntArrayTagPayload((IntArrayTag) tag);
                break;
            default:
                throw new IOException("Invalid tag type: " + type + ".");
        }
    }

    /**
     * Writes a <code>TAG_Byte</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeByteTagPayload(final ByteTag tag) throws IOException {
        this.os.writeByte(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Byte_Array</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeByteArrayTagPayload(final ByteArrayTag tag) throws IOException {
        final byte[] bytes = tag.getValue();
        this.os.writeInt(bytes.length);
        this.os.write(bytes);
    }

    /**
     * Writes a <code>TAG_Compound</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeCompoundTagPayload(final CompoundTag tag) throws IOException {
        for (final Tag childTag : tag.getValue().values()) {
            writeTag(childTag);
        }
        this.os.writeByte((byte) 0); // end tag - better way?
    }

    /**
     * Writes a <code>TAG_List</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeListTagPayload(final ListTag tag) throws IOException {
        final Class<? extends Tag> clazz = tag.getType();
        final List<Tag> tags = tag.getValue();
        final int size = tags.size();

        this.os.writeByte(NBTUtils.getTypeCode(clazz));
        this.os.writeInt(size);
        for (int i = 0; i < size; ++i) {
            writeTagPayload(tags.get(i));
        }
    }

    /**
     * Writes a <code>TAG_String</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeStringTagPayload(final StringTag tag) throws IOException {
        final byte[] bytes = tag.getValue().getBytes(NBTConstants.CHARSET);
        this.os.writeShort(bytes.length);
        this.os.write(bytes);
    }

    /**
     * Writes a <code>TAG_Double</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeDoubleTagPayload(final DoubleTag tag) throws IOException {
        this.os.writeDouble(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Float</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeFloatTagPayload(final FloatTag tag) throws IOException {
        this.os.writeFloat(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Long</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeLongTagPayload(final LongTag tag) throws IOException {
        this.os.writeLong(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Int</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeIntTagPayload(final IntTag tag) throws IOException {
        this.os.writeInt(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Short</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeShortTagPayload(final ShortTag tag) throws IOException {
        this.os.writeShort(tag.getValue());
    }

    /**
     * Writes a <code>TAG_Empty</code> tag.
     *
     * @param tag
     *            The tag.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void writeEndTagPayload(final EndTag tag) {
        /* empty */
    }

    private void writeIntArrayTagPayload(final IntArrayTag tag) throws IOException {
        final int[] data = tag.getValue();
        this.os.writeInt(data.length);
        for (final int element : data) {
            this.os.writeInt(element);
        }
    }

    @Override
    public void close() throws IOException {
        this.os.close();
    }

}
