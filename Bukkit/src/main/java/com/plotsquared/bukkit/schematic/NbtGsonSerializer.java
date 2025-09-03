package com.plotsquared.bukkit.schematic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.LongArrayTag;
import com.sk89q.jnbt.Tag;

import java.lang.reflect.Type;

final class NbtGsonSerializer implements JsonSerializer<Tag> {

    @Override
    public JsonElement serialize(final Tag src, final Type typeOfSrc, final JsonSerializationContext context) {
        if (src instanceof CompoundTag compoundTag) {
            JsonObject object = new JsonObject();
            compoundTag.getValue().forEach((s, tag) -> object.add(s, context.serialize(tag)));
            return object;
        }
        if (src instanceof ListTag listTag) {
            JsonArray array = new JsonArray();
            listTag.getValue().forEach(tag -> array.add(context.serialize(tag)));
            return array;
        }
        if (src instanceof ByteArrayTag byteArrayTag) {
            JsonArray array = new JsonArray();
            for (final byte b : byteArrayTag.getValue()) {
                array.add(b);
            }
            return array;
        }
        if (src instanceof IntArrayTag intArrayTag) {
            JsonArray array = new JsonArray();
            for (final int i : intArrayTag.getValue()) {
                array.add(i);
            }
            return array;
        }
        if (src instanceof LongArrayTag longArrayTag) {
            JsonArray array = new JsonArray();
            for (final long l : longArrayTag.getValue()) {
                array.add(l);
            }
            return array;
        }
        if (src.getValue() instanceof Number number) {
            return new JsonPrimitive(number);
        }
        if (src.getValue() instanceof String string) {
            return new JsonPrimitive(string);
        }
        throw new IllegalArgumentException("Don't know how to serialize " + src);
    }

}
