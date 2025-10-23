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

import com.plotsquared.core.util.ReflectionUtils;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.bukkit.adapter.Refraction;
import com.sk89q.worldedit.extension.platform.NoCapablePlatformException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

final class StateWrapperSpigot implements StateWrapper {

    private static final boolean FORCE_UPDATE_STATE = true;
    private static final boolean UPDATE_TRIGGER_PHYSICS = false;
    private static final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackageName();

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + StateWrapperSpigot.class.getSimpleName());
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static BukkitImplAdapter ADAPTER = null;
    private static Class<?> LIN_TAG_CLASS = null;
    private static Class<?> CRAFT_BLOCK_ENTITY_STATE_CLASS = null;
    private static Field CRAFT_SIGN_SIDE_SIGN_TEXT = null;
    private static Field CRAFT_SIGN_SIDE_LINES = null;
    private static MethodHandle PAPERWEIGHT_ADAPTER_FROM_NATIVE = null;
    private static MethodHandle CRAFT_BLOCK_ENTITY_STATE_LOAD_DATA = null;
    private static MethodHandle CRAFT_BLOCK_ENTITY_STATE_UPDATE = null;
    private static MethodHandle CRAFT_BLOCK_ENTITY_STATE_GET_SNAPSHOT = null;
    private static MethodHandle SIGN_BLOCK_ENTITY_SET_TEXT = null;
    private static MethodHandle DECODER_PARSE = null;
    private static MethodHandle DATA_RESULT_GET_OR_THROW = null;
    private static MethodHandle TO_LIN_TAG = null;

    private static Object SIGN_TEXT_DIRECT_CODEC = null;
    private static Object NBT_OPS_INSTANCE = null;

    public StateWrapperSpigot() {
        try {
            ReflectionUtils.RefClass worldEditPluginRefClass = ReflectionUtils.getRefClass(WorldEditPlugin.class);
            WorldEditPlugin worldEditPlugin = (WorldEditPlugin) worldEditPluginRefClass
                    .getMethod("getInstance")
                    .of(null)
                    .call();
            ADAPTER = (BukkitImplAdapter) worldEditPluginRefClass
                    .getMethod("getBukkitImplAdapter")
                    .of(worldEditPlugin)
                    .call();
            LIN_TAG_CLASS = Class.forName("org.enginehub.linbus.tree.LinTag"); // provided WE / FAWE version is too old
            PAPERWEIGHT_ADAPTER_FROM_NATIVE = findPaperweightAdapterFromNativeMethodHandle(ADAPTER.getClass());
            TO_LIN_TAG = findToLinTagMethodHandle();
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | NoCapablePlatformException e) {
            throw new RuntimeException("Failed to access required WorldEdit classes or methods", e);
        }
        try {
            final Class<?> SIGN_TEXT_CLASS = Class.forName("net.minecraft.world.level.block.entity.SignText");
            final Class<?> CRAFT_SIGN_SIDE_CLASS = Class.forName(CRAFTBUKKIT_PACKAGE + ".block.sign.CraftSignSide");
            CRAFT_SIGN_SIDE_SIGN_TEXT = CRAFT_SIGN_SIDE_CLASS.getDeclaredField("signText");
            CRAFT_SIGN_SIDE_SIGN_TEXT.setAccessible(true);
            CRAFT_SIGN_SIDE_LINES = CRAFT_SIGN_SIDE_CLASS.getDeclaredField("lines");
            CRAFT_SIGN_SIDE_LINES.setAccessible(true);
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
            final Class<?> CODEC_CLASS = Class.forName("com.mojang.serialization.Codec");
            final Class<?> DECODER_CLASS = Class.forName("com.mojang.serialization.Decoder");
            final Class<?> DATA_RESULT_CLASS = Class.forName("com.mojang.serialization.DataResult");
            final Class<?> DYNAMIC_OPS_CLASS = Class.forName("com.mojang.serialization.DynamicOps");
            final Class<?> NBT_OPS_CLASS = Class.forName(Refraction.pickName(
                    "net.minecraft.nbt.NbtOps",
                    "net.minecraft.nbt.DynamicOpsNBT"
            ));
            SIGN_TEXT_DIRECT_CODEC = Arrays.stream(SIGN_TEXT_CLASS.getFields())
                    .filter(field -> Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()))
                    .filter(field -> field.getType() == CODEC_CLASS)
                    .findFirst().orElseThrow().get(null);
            DECODER_PARSE = LOOKUP.findVirtual(
                    DECODER_CLASS, "parse", MethodType.methodType(
                            DATA_RESULT_CLASS, DYNAMIC_OPS_CLASS, Object.class
                    )
            );
            NBT_OPS_INSTANCE = Arrays.stream(NBT_OPS_CLASS.getFields())
                    .filter(field -> Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()))
                    .filter(field -> field.getType() == NBT_OPS_CLASS)
                    .findFirst().orElseThrow().get(null);
            DATA_RESULT_GET_OR_THROW = LOOKUP.findVirtual(
                    DATA_RESULT_CLASS, "getOrThrow",
                    MethodType.genericMethodType(0)
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
            Object nativeTag = PAPERWEIGHT_ADAPTER_FROM_NATIVE.invoke(ADAPTER, TO_LIN_TAG.invoke(data));
            // load block entity data
            CRAFT_BLOCK_ENTITY_STATE_LOAD_DATA.invoke(blockState, nativeTag);

            // signs need to be handled explicitly (at least during worldgen)
            if (blockState instanceof Sign sign) {
                if (data.getValue().get("front_text") instanceof CompoundTag textTag) {
                    setSignContents(true, sign.getSide(Side.FRONT), blockState, textTag);
                }
                if (data.getValue().get("back_text") instanceof CompoundTag textTag) {
                    setSignContents(false, sign.getSide(Side.BACK), blockState, textTag);
                }
            }

            CRAFT_BLOCK_ENTITY_STATE_UPDATE.invoke(blockState, FORCE_UPDATE_STATE, UPDATE_TRIGGER_PHYSICS);
        } catch (Throwable e) {
            LOGGER.error("Failed to update tile entity", e);
        }
        return false;
    }

    private static void setSignContents(boolean front, SignSide side, BlockState blockState, CompoundTag data) throws Throwable {
        Object nativeTag = PAPERWEIGHT_ADAPTER_FROM_NATIVE.invoke(ADAPTER, TO_LIN_TAG.invoke(data));
        Object dataResult = DECODER_PARSE.invoke(SIGN_TEXT_DIRECT_CODEC, NBT_OPS_INSTANCE, nativeTag);
        Object signText = DATA_RESULT_GET_OR_THROW.invoke(dataResult);

        // set the SignText on the underlying tile entity snapshot (SignBlockEntity)
        SIGN_BLOCK_ENTITY_SET_TEXT.invoke(CRAFT_BLOCK_ENTITY_STATE_GET_SNAPSHOT.invoke(blockState), signText, front);
        // and update the SignText field on the CraftSignSide - changes are otherwise not reflected
        CRAFT_SIGN_SIDE_SIGN_TEXT.set(side, signText);

        // reset cached lines to null, so it can be re-retrieved from SignText (for API access etc.)
        CRAFT_SIGN_SIDE_LINES.set(side, null);
    }

    /**
     * Finds the {@code toLinTag} method on the {@code ToLinTag} interface, if lin-bus is available in the classpath.
     * <br />
     * Required to access the underlying lin tag of the used JNBT tag by PlotSquared, so it can be converted into the platforms
     * native tag later.
     *
     * @return the MethodHandle for {@code toLinTag}, or {@code null} if lin-bus is not available in the classpath.
     * @throws ClassNotFoundException if the {@code ToLinTag} class could not be found.
     * @throws NoSuchMethodException  if no {@code toLinTag} method exists.
     * @throws IllegalAccessException shouldn't happen.
     */
    private static MethodHandle findToLinTagMethodHandle() throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException {
        return LOOKUP.findVirtual(
                Class.forName("org.enginehub.linbus.tree.ToLinTag"),
                "toLinTag",
                MethodType.methodType(LIN_TAG_CLASS)
        );
    }

    /**
     * Find the method (handle) to convert from native (= WE/FAWE) NBT tags to minecraft NBT tags.
     * <br />
     * Depending on the used version of WE/FAWE, this differs:
     * <ul>
     *     <li>On WE versions post LinBus introduction: {@code fromNative(org.enginehub.linbus.tree.LinTag)}</li>
     *     <li>On FAWE versions post LinBus introduction: {@code fromNativeLin(org.enginehub.linbus.tree.LinTag)}</li>
     * </ul>
     *
     * @param adapterClass The bukkit adapter implementation class
     * @return the method.
     * @throws IllegalAccessException shouldn't happen as private lookup is used.
     * @throws NoSuchMethodException  if the method couldn't be found.
     */
    private static MethodHandle findPaperweightAdapterFromNativeMethodHandle(Class<?> adapterClass) throws
            IllegalAccessException, NoSuchMethodException {
        final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(adapterClass, LOOKUP);
        try {
            // FAWE
            return lookup.findVirtual(adapterClass, "fromNativeLin", MethodType.methodType(Object.class, LIN_TAG_CLASS));
        } catch (NoSuchMethodException e) {
            // WE
            return lookup.findVirtual(adapterClass, "fromNative", MethodType.methodType(Object.class, LIN_TAG_CLASS));
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

}
