/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.schematic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.DyeColor;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

final class StateWrapperPaper1_21_5 extends StateWrapperSpigot {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + StateWrapperPaper1_21_5.class.getSimpleName());

    private static final Gson GSON = new GsonBuilder().registerTypeHierarchyAdapter(Tag.class, new NbtGsonSerializer()).create();
    private static Object KYORI_GSON_SERIALIZER = null;
    private static MethodHandle GSON_SERIALIZER_DESERIALIZE_TREE = null;
    private static MethodHandle BUKKIT_SIGN_SIDE_LINE_SET = null;

    public StateWrapperPaper1_21_5() {
        super();
        try {
            initializeSignHack();
            LOGGER.info("Using {} for block data population", StateWrapperPaper1_21_5.class.getSimpleName());
        } catch (Throwable e) {
            throw new RuntimeException("Failed to initialize sign hack", e);
        }
    }

    @Override
    public void postEntityStateLoad(final @NonNull BlockState blockState, final @NonNull CompoundTag data) throws Throwable {
        // signs need special handling during generation
        if (blockState instanceof Sign sign) {
            if (data.getValue().get("front_text") instanceof CompoundTag textTag) {
                setSignTextHack(sign.getSide(Side.FRONT), textTag);
            }
            if (data.getValue().get("back_text") instanceof CompoundTag textTag) {
                setSignTextHack(sign.getSide(Side.BACK), textTag);
            }
        }
    }

    @Override
    public Logger logger() {
        return StateWrapperPaper1_21_5.LOGGER;
    }

    /**
     * Set sign content on the bukkit tile entity. The server does not load sign content applied via the main logic
     * (CraftBlockEntity#load), as the SignEntity needs to have a valid ServerLevel assigned to it.
     * That's not possible on worldgen; therefore, this hack has to be used additionally.
     * <br />
     * Modern sign content (non-plain-text sign lines) require Paper.
     *
     * @param side The sign side to apply data onto.
     * @param text The compound tag containing the data for the sign side ({@code front_text} / {@code back_text})
     * @throws Throwable if something went wrong when reflectively updating the sign.
     */
    private static void setSignTextHack(SignSide side, CompoundTag text) throws Throwable {
        if (text.containsKey("color")) {
            //noinspection UnstableApiUsage
            side.setColor(DyeColor.legacyValueOf(text.getString("color").toUpperCase(Locale.ROOT)));
        }
        if (text.containsKey("has_glowing_text")) {
            side.setGlowingText(text.getByte("has_glowing_text") == 1);
        }
        List<Tag> lines = text.getList("messages");
        if (lines != null) {
            for (int i = 0; i < Math.min(lines.size(), 3); i++) {
                Tag line = lines.get(i);
                Object content = line.getValue();
                // Minecraft uses mixed lists / arrays in their sign texts. One line can be a complex component, whereas
                // the following line could simply be a string. Those simpler lines are represented as `{"": ""}` (only in
                // SNBT those will be shown as a standard string).
                if (line instanceof CompoundTag compoundTag && compoundTag.getValue().containsKey("")) {
                    content = compoundTag.getValue().get("");
                }
                // absolute garbage way to try to handle stringified components (pre 1.21.5)
                else if (content instanceof String contentAsString && (contentAsString.startsWith("{") || contentAsString.startsWith("["))) {
                    try {
                        content = JsonParser.parseString(contentAsString);
                    } catch (JsonSyntaxException e) {
                        // well, it wasn't JSON after all
                    }
                }

                // serializes the line content from JNBT to Gson JSON objects, passes that to adventure and deserializes
                // into an adventure component.
                // pass all possible types of content into the deserializer (Strings, Compounds, Arrays), even though Strings
                // could be set directly via Sign#setLine(int, String). The overhead is minimal, the serializer can handle
                // strings - and we don't have to use the deprecated method.
                BUKKIT_SIGN_SIDE_LINE_SET.invoke(
                        side, i, GSON_SERIALIZER_DESERIALIZE_TREE.invoke(
                                KYORI_GSON_SERIALIZER,
                                content instanceof JsonElement ? content : GSON.toJsonTree(content)
                        )
                );
            }
        }
    }

    private static void initializeSignHack() throws Throwable {
        char[] dontObfuscate = new char[]{
                'n', 'e', 't', '.', 'k', 'y', 'o', 'r', 'i', '.', 'a', 'd', 'v', 'e', 'n', 't', 'u', 'r', 'e', '.',
                't', 'e', 'x', 't', '.', 's', 'e', 'r', 'i', 'a', 'l', 'i', 'z', 'e', 'r', '.', 'g', 's', 'o', 'n', '.',
                'G', 's', 'o', 'n', 'C', 'o', 'm', 'p', 'o', 'n', 'e', 'n', 't', 'S', 'e', 'r', 'i', 'a', 'l', 'i', 'z', 'e', 'r'
        };
        Class<?> gsonComponentSerializerClass = Class.forName(new String(dontObfuscate));
        KYORI_GSON_SERIALIZER = Arrays.stream(gsonComponentSerializerClass.getMethods())
                .filter(method -> method.getName().equals("gson"))
                .findFirst()
                .orElseThrow().invoke(null);
        GSON_SERIALIZER_DESERIALIZE_TREE = LOOKUP.unreflect(Arrays
                .stream(gsonComponentSerializerClass.getMethods())
                .filter(method -> method.getName().equals("deserializeFromTree") && method.getParameterCount() == 1)
                .findFirst()
                .orElseThrow());
        BUKKIT_SIGN_SIDE_LINE_SET = LOOKUP.unreflect(Arrays.stream(SignSide.class.getMethods())
                .filter(method -> method.getName().equals("line") && method.getParameterCount() == 2)
                .findFirst()
                .orElseThrow());
    }

}
