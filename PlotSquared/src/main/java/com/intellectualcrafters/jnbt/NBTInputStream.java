package com.intellectualcrafters.jnbt;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class reads <strong>NBT</strong>, or <strong>Named Binary Tag</strong> streams, and produces an object graph of
 * subclasses of the {@code Tag} object. <p/> <p> The NBT format was created by Markus Persson, and the specification
 * may be found at <a href="http://www.minecraft.net/docs/NBT.txt"> http://www.minecraft.net/docs/NBT.txt</a>. </p>
 */
public final class NBTInputStream implements Closeable {
    private final DataInputStream is;
    
    /**
     * Creates a new {@code NBTInputStream}, which will source its data from the specified input stream.
     *
     * @param is the input stream
     *
     * @throws IOException if an I/O error occurs
     */
    public NBTInputStream(final InputStream is) throws IOException {
        this.is = new DataInputStream(is);
    }
    
    /**
     * Reads an NBT tag from the stream.
     *
     * @return The tag that was read.
     *
     * @throws IOException if an I/O error occurs.
     */
    public Tag readTag() throws IOException {
        return readTag(0);
    }
    
    /**
     * Reads an NBT from the stream.
     *
     * @param depth the depth of this tag
     *
     * @return The tag that was read.
     *
     * @throws IOException if an I/O error occurs.
     */
    private Tag readTag(final int depth) throws IOException {
        final int type = this.is.readByte() & 0xFF;
        String name;
        if (type != NBTConstants.TYPE_END) {
            final int nameLength = this.is.readShort() & 0xFFFF;
            final byte[] nameBytes = new byte[nameLength];
            this.is.readFully(nameBytes);
            name = new String(nameBytes, NBTConstants.CHARSET);
        } else {
            name = "";
        }
        return readTagPayload(type, name, depth);
    }
    
    /**
     * Reads the payload of a tag, given the name and type.
     *
     * @param type  the type
     * @param name  the name
     * @param depth the depth
     *
     * @return the tag
     *
     * @throws IOException if an I/O error occurs.
     */
    private Tag readTagPayload(final int type, final String name, final int depth) throws IOException {
        switch (type) {
            case NBTConstants.TYPE_END:
                if (depth == 0) {
                    throw new IOException("TAG_End found without a TAG_Compound/TAG_List tag preceding it.");
                } else {
                    return new EndTag();
                }
            case NBTConstants.TYPE_BYTE:
                return new ByteTag(name, this.is.readByte());
            case NBTConstants.TYPE_SHORT:
                return new ShortTag(name, this.is.readShort());
            case NBTConstants.TYPE_INT:
                return new IntTag(name, this.is.readInt());
            case NBTConstants.TYPE_LONG:
                return new LongTag(name, this.is.readLong());
            case NBTConstants.TYPE_FLOAT:
                return new FloatTag(name, this.is.readFloat());
            case NBTConstants.TYPE_DOUBLE:
                return new DoubleTag(name, this.is.readDouble());
            case NBTConstants.TYPE_BYTE_ARRAY:
                int length = this.is.readInt();
                byte[] bytes = new byte[length];
                this.is.readFully(bytes);
                return new ByteArrayTag(name, bytes);
            case NBTConstants.TYPE_STRING:
                length = this.is.readShort();
                bytes = new byte[length];
                this.is.readFully(bytes);
                return new StringTag(name, new String(bytes, NBTConstants.CHARSET));
            case NBTConstants.TYPE_LIST:
                final int childType = this.is.readByte();
                length = this.is.readInt();
                final List<Tag> tagList = new ArrayList<Tag>();
                for (int i = 0; i < length; ++i) {
                    final Tag tag = readTagPayload(childType, "", depth + 1);
                    if (tag instanceof EndTag) {
                        throw new IOException("TAG_End not permitted in a list.");
                    }
                    tagList.add(tag);
                }
                return new ListTag(name, NBTUtils.getTypeClass(childType), tagList);
            case NBTConstants.TYPE_COMPOUND:
                final Map<String, Tag> tagMap = new HashMap<String, Tag>();
                while (true) {
                    final Tag tag = readTag(depth + 1);
                    if (tag instanceof EndTag) {
                        break;
                    } else {
                        tagMap.put(tag.getName(), tag);
                    }
                }
                return new CompoundTag(name, tagMap);
            case NBTConstants.TYPE_INT_ARRAY:
                length = this.is.readInt();
                final int[] data = new int[length];
                for (int i = 0; i < length; i++) {
                    data[i] = this.is.readInt();
                }
                return new IntArrayTag(name, data);
            default:
                throw new IOException("Invalid tag type: " + type + ".");
        }
    }
    
    @Override
    public void close() throws IOException {
        this.is.close();
    }
}
