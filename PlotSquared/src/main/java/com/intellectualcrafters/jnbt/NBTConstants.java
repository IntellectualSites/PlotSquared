////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualcrafters.jnbt;

import java.nio.charset.Charset;

/**
 * A class which holds constant values.
 */
public final class NBTConstants {

    public static final Charset CHARSET = Charset.forName("UTF-8");

    public static final int TYPE_END = 0, TYPE_BYTE = 1, TYPE_SHORT = 2, TYPE_INT = 3, TYPE_LONG = 4, TYPE_FLOAT = 5, TYPE_DOUBLE = 6, TYPE_BYTE_ARRAY = 7, TYPE_STRING = 8, TYPE_LIST = 9, TYPE_COMPOUND = 10, TYPE_INT_ARRAY = 11;

    /**
     * Default private constructor.
     */
    private NBTConstants() {

    }

    /**
     * Convert a type ID to its corresponding {@link Tag} class.
     *
     * @param id type ID
     * @return tag class
     * @throws IllegalArgumentException thrown if the tag ID is not valid
     */
    public static Class<? extends Tag> getClassFromType(final int id) {
        switch (id) {
            case TYPE_END:
                return EndTag.class;
            case TYPE_BYTE:
                return ByteTag.class;
            case TYPE_SHORT:
                return ShortTag.class;
            case TYPE_INT:
                return IntTag.class;
            case TYPE_LONG:
                return LongTag.class;
            case TYPE_FLOAT:
                return FloatTag.class;
            case TYPE_DOUBLE:
                return DoubleTag.class;
            case TYPE_BYTE_ARRAY:
                return ByteArrayTag.class;
            case TYPE_STRING:
                return StringTag.class;
            case TYPE_LIST:
                return ListTag.class;
            case TYPE_COMPOUND:
                return CompoundTag.class;
            case TYPE_INT_ARRAY:
                return IntArrayTag.class;
            default:
                throw new IllegalArgumentException("Unknown tag type ID of " + id);
        }
    }

}
