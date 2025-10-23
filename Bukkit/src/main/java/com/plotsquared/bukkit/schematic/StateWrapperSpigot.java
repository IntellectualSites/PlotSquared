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
import com.plotsquared.core.util.ReflectionUtils;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.bukkit.adapter.Refraction;
import com.sk89q.worldedit.extension.platform.NoCapablePlatformException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

final class StateWrapperSpigot implements StateWrapper {

    private static final boolean FORCE_UPDATE_STATE = true;
    private static final boolean UPDATE_TRIGGER_PHYSICS = false;
    private static final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackageName();

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + StateWrapperSpigot.class.getSimpleName());
    private static final Gson GSON = new GsonBuilder().registerTypeHierarchyAdapter(Tag.class, new NbtGsonSerializer()).create();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static BukkitImplAdapter ADAPTER = null;
    private static Class<?> LIN_TAG_CLASS = null;
    private static Class<?> JNBT_TAG_CLASS = null;
    private static Class<?> CRAFT_BLOCK_ENTITY_STATE_CLASS = null;
    private static Class<?> MINECRAFT_CHAT_COMPONENT_CLASS = null;
    private static Field CRAFT_SIGN_SIDE_SIGN_TEXT = null;
    private static MethodHandle PAPERWEIGHT_ADAPTER_FROM_NATIVE = null;
    private static MethodHandle CRAFT_BLOCK_ENTITY_STATE_LOAD_DATA = null;
    private static MethodHandle CRAFT_BLOCK_ENTITY_STATE_UPDATE = null;
    private static MethodHandle CRAFT_BLOCK_ENTITY_STATE_GET_SNAPSHOT = null;
    private static MethodHandle SIGN_BLOCK_ENTITY_SET_TEXT = null;
    private static MethodHandle MINECRAFT_DYE_COLOR_BY_NAME = null;
    private static MethodHandle CRAFT_CHAT_MESSAGE_FROM_JSON_OR_STRING = null;
    private static MethodHandle SIGN_TEXT_CONSTRUCTOR = null;
    private static MethodHandle TO_LIN_TAG = null;

    private static Object DYE_COLOR_BLACK = null;

    public StateWrapperSpigot() {
        try {
            findNbtCompoundClassType(clazz -> LIN_TAG_CLASS = clazz, clazz -> JNBT_TAG_CLASS = clazz);
            ReflectionUtils.RefClass worldEditPluginRefClass = ReflectionUtils.getRefClass(WorldEditPlugin.class);
            WorldEditPlugin worldEditPlugin = (WorldEditPlugin) worldEditPluginRefClass
                    .getMethod("getInstance")
                    .of(null)
                    .call();
            ADAPTER = (BukkitImplAdapter) worldEditPluginRefClass
                    .getMethod("getBukkitImplAdapter")
                    .of(worldEditPlugin)
                    .call();
            PAPERWEIGHT_ADAPTER_FROM_NATIVE = findPaperweightAdapterFromNativeMethodHandle(
                    ADAPTER.getClass(), LIN_TAG_CLASS, JNBT_TAG_CLASS
            );
            TO_LIN_TAG = findToLinTagMethodHandle(LIN_TAG_CLASS);
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | NoCapablePlatformException e) {
            throw new RuntimeException("Failed to access required WorldEdit methods", e);
        }
        try {
            final Class<?> SIGN_TEXT_CLASS = Class.forName("net.minecraft.world.level.block.entity.SignText");
            MINECRAFT_CHAT_COMPONENT_CLASS = Class.forName(Refraction.pickName(
                    "net.minecraft.network.chat.Component",
                    "net.minecraft.network.chat.IChatBaseComponent"
            ));
            final Class<?> MINECRAFT_DYE_COLOR_CLASS = Class.forName(Refraction.pickName(
                    "net.minecraft.world.item.DyeColor",
                    "net.minecraft.world.item.EnumColor"
            ));
            CRAFT_SIGN_SIDE_SIGN_TEXT = Class.forName(CRAFTBUKKIT_PACKAGE + ".block.sign.CraftSignSide")
                    .getDeclaredField("signText");
            CRAFT_SIGN_SIDE_SIGN_TEXT.setAccessible(true);
            CRAFT_BLOCK_ENTITY_STATE_CLASS = Class.forName(CRAFTBUKKIT_PACKAGE + ".block.CraftBlockEntityState");
            CRAFT_BLOCK_ENTITY_STATE_LOAD_DATA = findCraftBlockEntityStateLoadDataMethodHandle(CRAFT_BLOCK_ENTITY_STATE_CLASS);
            CRAFT_BLOCK_ENTITY_STATE_UPDATE = findCraftBlockEntityStateUpdateMethodHandle(CRAFT_BLOCK_ENTITY_STATE_CLASS);
            CRAFT_BLOCK_ENTITY_STATE_GET_SNAPSHOT = findCraftBlockEntityStateSnapshotMethodHandle(CRAFT_BLOCK_ENTITY_STATE_CLASS);
            SIGN_BLOCK_ENTITY_SET_TEXT = findSignBlockEntitySetTextMethodHandle(
                    Class.forName(Refraction.pickName(
                            "net.minecraft.world.level.block.entity.SignBlockEntity",
                            "net.minecraft.world.level.block.entity.TileEntitySign"
                    )),
                    SIGN_TEXT_CLASS
            );
            CRAFT_CHAT_MESSAGE_FROM_JSON_OR_STRING = findCraftChatMessageFromJsonOrStringMethodHandle(
                    MINECRAFT_CHAT_COMPONENT_CLASS);
            SIGN_TEXT_CONSTRUCTOR = findSignTextConstructor(
                    SIGN_TEXT_CLASS, MINECRAFT_CHAT_COMPONENT_CLASS, MINECRAFT_DYE_COLOR_CLASS
            );
            MINECRAFT_DYE_COLOR_BY_NAME = findDyeColorByNameMethodHandle(MINECRAFT_DYE_COLOR_CLASS);
            DYE_COLOR_BLACK = Objects.requireNonNull(
                    MINECRAFT_DYE_COLOR_BY_NAME.invoke("black", null), "couldn't find black dye color"
            );
        } catch (Throwable e) {
            throw new RuntimeException("Failed to initialize required native method accessors", e);
        }
    }

    @Override
    public boolean restore(final @NonNull Block block, final @NonNull CompoundTag data) {
        try {
            final BlockState blockState = block.getState();
            if (!CRAFT_BLOCK_ENTITY_STATE_CLASS.isAssignableFrom(blockState.getClass())) {
                return false;
            }
            // get native tag
            Object nativeTag = PAPERWEIGHT_ADAPTER_FROM_NATIVE.invoke(
                    ADAPTER,
                    LIN_TAG_CLASS == null ? data : TO_LIN_TAG.invoke(data)
            );
            // load block entity data
            CRAFT_BLOCK_ENTITY_STATE_LOAD_DATA.invoke(blockState, nativeTag);

            postEntityStateLoad(blockState, data);

            CRAFT_BLOCK_ENTITY_STATE_UPDATE.invoke(blockState, FORCE_UPDATE_STATE, UPDATE_TRIGGER_PHYSICS);
        } catch (Throwable e) {
            LOGGER.error("Failed to update tile entity", e);
        }
        return false;
    }

    public void postEntityStateLoad(final @NonNull BlockState blockState, final @NonNull CompoundTag data) throws Throwable {
        if (blockState instanceof Sign sign) {
            if (data.getValue().get("front_text") instanceof CompoundTag textTag) {
                setSignContents(true, sign.getSide(Side.FRONT), blockState, textTag);
            }
            if (data.getValue().get("back_text") instanceof CompoundTag textTag) {
                setSignContents(false, sign.getSide(Side.BACK), blockState, textTag);
            }
        }
    }

    private static void setSignContents(boolean front, SignSide side, BlockState blockState, CompoundTag data) throws Throwable {
        final List<Tag> messages = data.getList("messages");
        if (messages.size() != 4) {
            if (data.containsKey("color")) {
                //noinspection UnstableApiUsage
                side.setColor(DyeColor.legacyValueOf(data.getString("color").toUpperCase(Locale.ROOT)));
            }
            side.setGlowingText(data.getByte("has_glowing_text") == 1);
            return;
        }

        final String color = data.getString("color");
        final boolean glowing = data.getByte("has_glowing_text") == 1;
        final Object dyeColor = color.isEmpty() ? DYE_COLOR_BLACK : MINECRAFT_DYE_COLOR_BY_NAME.invoke(
                color.equalsIgnoreCase("silver") ? "light_gray" : color,
                DYE_COLOR_BLACK // fallback
        );

        Object[] components = new Object[messages.size()];
        for (int i = 0; i < components.length; i++) {
            final Tag message = messages.get(i);
            Object content;
            // unwrap possible nested entry for mixed array types in later versions
            if (message instanceof CompoundTag tag && tag.containsKey("")) {
                content = tag.getString("");
            } else {
                content = message.getValue();
            }
            // if the value is not a string, make it one so it can be converted to a chat component
            if (!(content instanceof String)) {
                content = GSON.toJson(content);
                LOGGER.info("GSON serialized to {}", content);
                LOGGER.info("factory to {}", CRAFT_CHAT_MESSAGE_FROM_JSON_OR_STRING.invoke((String) content));
            }
            // chat.Component
            components[i] = CRAFT_CHAT_MESSAGE_FROM_JSON_OR_STRING.invoke((String) content);
        }
        final Object typedComponents = Array.newInstance(MINECRAFT_CHAT_COMPONENT_CLASS, components.length);
        System.arraycopy(components, 0, typedComponents, 0, components.length);
        final Object signText = SIGN_TEXT_CONSTRUCTOR.invoke(typedComponents, typedComponents, dyeColor, glowing);

        // blockState == org.bukkit.craftbukkit.block.CraftBlockEntityState
        // --> net.minecraft.world.level.block.entity.SignBlockEntity
        SIGN_BLOCK_ENTITY_SET_TEXT.invoke(CRAFT_BLOCK_ENTITY_STATE_GET_SNAPSHOT.invoke(blockState), signText, front);
        CRAFT_SIGN_SIDE_SIGN_TEXT.set(side, signText);
    }

    /**
     * Initialize the used NBT tag class. For modern FAWE and WE that'll be Lin - for older ones JNBT.
     *
     * @throws ClassNotFoundException if neither can be found.
     */
    private static void findNbtCompoundClassType(Consumer<Class<?>> linClass, Consumer<Class<?>> jnbtClass) throws
            ClassNotFoundException {
        try {
            linClass.accept(Class.forName("org.enginehub.linbus.tree.LinTag"));
        } catch (ClassNotFoundException e) {
            jnbtClass.accept(Class.forName("com.sk89q.jnbt.Tag"));
        }
    }

    /**
     * Finds the {@code toLinTag} method on the {@code ToLinTag} interface, if lin-bus is available in the classpath.
     * <br />
     * Required to access the underlying lin tag of the used JNBT tag by PlotSquared, so it can be converted into the platforms
     * native tag later.
     *
     * @param linTagClass {@code Tag} class of lin-bus, or {@code null} if not available.
     * @return the MethodHandle for {@code toLinTag}, or {@code null} if lin-bus is not available in the classpath.
     * @throws ClassNotFoundException if the {@code ToLinTag} class could not be found.
     * @throws NoSuchMethodException  if no {@code toLinTag} method exists.
     * @throws IllegalAccessException shouldn't happen.
     */
    private static MethodHandle findToLinTagMethodHandle(Class<?> linTagClass) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException {
        if (linTagClass == null) {
            return null;
        }
        return LOOKUP.findVirtual(
                Class.forName("org.enginehub.linbus.tree.ToLinTag"),
                "toLinTag",
                MethodType.methodType(linTagClass)
        );
    }

    /**
     * Find the method (handle) to convert from native (= WE/FAWE) NBT tags to minecraft NBT tags.
     * <br />
     * Depending on the used version of WE/FAWE, this differs:
     * <ul>
     *     <li>On WE/FAWE version pre LinBus introduction: {@code fromNative(org.sk89q.jnbt.Tag)}</li>
     *     <li>On WE versions post LinBus introduction: {@code fromNative(org.enginehub.linbus.tree.LinTag)}</li>
     *     <li>On FAWE versions post LinBus introduction: {@code fromNativeLin(org.enginehub.linbus.tree.LinTag)}</li>
     * </ul>
     *
     * @param adapterClass The bukkit adapter implementation class
     * @param linTagClass  The lin-bus {@code Tag} class, if existing - otherwise {@code null}
     * @param jnbtTagClass The jnbt {@code Tag} class, if lin-bus was not found in classpath - otherwise {@code null}
     * @return the method.
     * @throws IllegalAccessException shouldn't happen as private lookup is used.
     * @throws NoSuchMethodException  if the method couldn't be found.
     */
    private static MethodHandle findPaperweightAdapterFromNativeMethodHandle(
            Class<?> adapterClass, Class<?> linTagClass, Class<?> jnbtTagClass
    ) throws IllegalAccessException, NoSuchMethodException {
        final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(adapterClass, LOOKUP);
        if (jnbtTagClass != null) {
            // usage of JNBT = identical method signatures for WE and FAWE
            return lookup.findVirtual(adapterClass, "fromNative", MethodType.methodType(Object.class, jnbtTagClass));
        }
        try {
            // FAWE
            return lookup.findVirtual(adapterClass, "fromNativeLin", MethodType.methodType(Object.class, linTagClass));
        } catch (NoSuchMethodException e) {
            // WE
            return lookup.findVirtual(adapterClass, "fromNative", MethodType.methodType(Object.class, linTagClass));
        }
    }

    private static MethodHandle findCraftBlockEntityStateLoadDataMethodHandle(Class<?> craftBlockEntityStateClass) throws
            NoSuchMethodException, IllegalAccessException {
        for (final Method method : craftBlockEntityStateClass.getMethods()) {
            if (method.getName().equals("loadData") && method.getParameterCount() == 1) {
                return LOOKUP.unreflect(method);
            }
        }
        throw new NoSuchMethodException("Couldn't find #loadData(CompoundTag) in " + craftBlockEntityStateClass.getName());
    }

    private static MethodHandle findCraftBlockEntityStateUpdateMethodHandle(Class<?> craftBlockEntityStateClass) throws
            NoSuchMethodException, IllegalAccessException {
        for (final Method method : craftBlockEntityStateClass.getMethods()) {
            if (method.getReturnType().equals(Boolean.TYPE) && method.getParameterCount() == 2 &&
                    method.getParameterTypes()[0] == Boolean.TYPE && method.getParameterTypes()[1] == Boolean.TYPE) {
                return LOOKUP.unreflect(method);
            }
        }
        throw new NoSuchMethodException("Couldn't find method for #update(boolean, boolean) in " + craftBlockEntityStateClass.getName());
    }

    private static MethodHandle findCraftBlockEntityStateSnapshotMethodHandle(Class<?> craftBlockEntityStateClass) throws
            IllegalAccessException, NoSuchMethodException {
        // doesn't seem to be obfuscated, but protected
        final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(craftBlockEntityStateClass, LOOKUP);
        return lookup.unreflect(craftBlockEntityStateClass.getDeclaredMethod("getSnapshot"));
    }

    private static MethodHandle findSignBlockEntitySetTextMethodHandle(Class<?> signBlockEntity, Class<?> signText) throws
            NoSuchMethodException, IllegalAccessException {
        for (final Method method : signBlockEntity.getMethods()) {
            if (method.getReturnType() == Boolean.TYPE && method.getParameterCount() == 2
                    && method.getParameterTypes()[0] == signText && method.getParameterTypes()[1] == Boolean.TYPE) {
                return LOOKUP.unreflect(method);
            }
        }
        throw new NoSuchMethodException("Couldn't lookup SignBlockEntity#setText(SignText, boolean) boolean");
    }

    private static MethodHandle findCraftChatMessageFromJsonOrStringMethodHandle(Class<?> minecraftChatComponent)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
        // public static IChatBaseComponent fromJSONOrString(String message)
        return LOOKUP.findStatic(
                Class.forName(CRAFTBUKKIT_PACKAGE + ".util.CraftChatMessage"),
                "fromJSONOrString",
                MethodType.methodType(minecraftChatComponent, String.class)
        );
    }

    private static MethodHandle findSignTextConstructor(Class<?> signText, Class<?> chatComponent, Class<?> dyeColorEnum) throws
            NoSuchMethodException, IllegalAccessException {
        return LOOKUP.findConstructor(
                signText, MethodType.methodType(
                        void.class,
                        chatComponent.arrayType(), chatComponent.arrayType(), dyeColorEnum, Boolean.TYPE
                )
        );
    }

    private static MethodHandle findDyeColorByNameMethodHandle(Class<?> dyeColorClass) throws
            NoSuchMethodException, IllegalAccessException {
        for (final Method method : dyeColorClass.getMethods()) {
            if (Modifier.isStatic(method.getModifiers()) && method.getParameterCount() == 2 && method.getParameterTypes()[0] == String.class) {
                return LOOKUP.unreflect(method);
            }
        }
        throw new NoSuchMethodException("Couldn't lookup static DyeColor.byName(String, DyeColor)");
    }

}
