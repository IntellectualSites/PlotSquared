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
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.util.ReflectionUtils;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.extension.platform.NoCapablePlatformException;
import io.papermc.lib.PaperLib;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * This class (attempts to) restore block tile entity data, after the underlying block state has been placed.
 * This is used on chunk population (world generation) and in the platforms queue handler (as a fallback for WorldEdit placement).
 * <br />
 * This class relies heavily on reflective access, native minecraft methods and non-standardized WorldEdit / FAWE methods. It's
 * extremely prone to breakage between versions (Minecraft and/or (FA)WE), but supports most if not all possible tile entities.
 * Given the previous logic of this class was also non-reliable between version updates, and did only support a small subset of
 * tile entities, it's a fair trade-off.
 */
@ApiStatus.Internal
public class StateWrapper {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + StateWrapper.class.getSimpleName());

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Gson GSON = new GsonBuilder().registerTypeHierarchyAdapter(Tag.class, new NbtGsonSerializer()).create();
    private static final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackageName();

    private static final boolean FORCE_UPDATE_STATE = true;
    private static final boolean UPDATE_TRIGGER_PHYSICS = false;
    private static final boolean SUPPORTED = PlotSquared.platform().serverVersion()[1] > 20 ||
            (PlotSquared.platform().serverVersion()[1] == 20 && PlotSquared.platform().serverVersion()[2] >= 4);
    private static final String INITIALIZATION_ERROR_TEMPLATE = """
            Failed to initialize StateWrapper: {}
            Block-/Tile-Entities, pasted by schematics for example, won't be updated with their respective block data. This affects things like sign text, banner patterns, skulls, etc.
            Try updating your Server Software, PlotSquared and WorldEdit / FastAsyncWorldEdit first. If the issue persists, report it on the issue tracker.
            """;

    private static boolean NOT_SUPPORTED_NOTIFIED = false;
    private static boolean FAILED_INITIALIZATION = false;
    private static BukkitImplAdapter ADAPTER = null;
    private static Class<?> LIN_TAG_CLASS = null;
    private static Class<?> JNBT_TAG_CLASS = null;
    private static Class<?> CRAFT_BLOCK_ENTITY_STATE_CLASS = null;
    private static MethodHandle PAPERWEIGHT_ADAPTER_FROM_NATIVE = null;
    private static MethodHandle CRAFT_BLOCK_ENTITY_STATE_LOAD_DATA = null;
    private static MethodHandle CRAFT_BLOCK_ENTITY_STATE_UPDATE = null;
    private static MethodHandle TO_LIN_TAG = null;

    // SIGN HACK
    private static boolean PAPER_SIGN_NOTIFIED = false;
    private static boolean FAILED_SIGN_INITIALIZATION = false;
    private static Object KYORI_GSON_SERIALIZER = null;
    private static MethodHandle GSON_SERIALIZER_DESERIALIZE_TREE = null;
    private static MethodHandle BUKKIT_SIGN_SIDE_LINE_SET = null;

    public CompoundTag tag;

    public StateWrapper(CompoundTag tag) {
        this.tag = tag;
    }

    /**
     * Restore the TileEntity data to the given world at the given coordinates.
     *
     * @param worldName World name
     * @param x         x position
     * @param y         y position
     * @param z         z position
     * @return true if successful
     */
    public boolean restoreTag(String worldName, int x, int y, int z) {
        World world = BukkitUtil.getWorld(worldName);
        if (world == null) {
            return false;
        }
        return restoreTag(world.getBlockAt(x, y, z));
    }

    /**
     * Restore the TileEntity data to the given block
     *
     * @param block Block to restore to
     * @return true if successful
     */
    public boolean restoreTag(@NonNull Block block) {
        if (this.tag == null || FAILED_INITIALIZATION) {
            return false;
        }
        if (!SUPPORTED) {
            if (!NOT_SUPPORTED_NOTIFIED) {
                NOT_SUPPORTED_NOTIFIED = true;
                LOGGER.error(INITIALIZATION_ERROR_TEMPLATE, "Your server version is not supported. 1.20.4 or later is required");
            }
            return false;
        }
        if (ADAPTER == null) {
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
                LOGGER.error(INITIALIZATION_ERROR_TEMPLATE, "Failed to access required WorldEdit methods", e);
                FAILED_INITIALIZATION = true;
                return false;
            }
            try {
                CRAFT_BLOCK_ENTITY_STATE_CLASS = Class.forName(CRAFTBUKKIT_PACKAGE + ".block.CraftBlockEntityState");
                CRAFT_BLOCK_ENTITY_STATE_LOAD_DATA = findCraftBlockEntityStateLoadDataMethodHandle(CRAFT_BLOCK_ENTITY_STATE_CLASS);
                CRAFT_BLOCK_ENTITY_STATE_UPDATE = findCraftBlockEntityStateUpdateMethodHandle(CRAFT_BLOCK_ENTITY_STATE_CLASS);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
                LOGGER.error(INITIALIZATION_ERROR_TEMPLATE, "Failed to initialize required native method accessors", e);
                FAILED_INITIALIZATION = true;
                return false;
            }
        }
        try {
            final BlockState blockState = block.getState();
            if (!CRAFT_BLOCK_ENTITY_STATE_CLASS.isAssignableFrom(blockState.getClass())) {
                return false;
            }
            // get native tag
            Object nativeTag = PAPERWEIGHT_ADAPTER_FROM_NATIVE.invoke(
                    ADAPTER,
                    LIN_TAG_CLASS == null ? this.tag : TO_LIN_TAG.invoke(this.tag)
            );
            CRAFT_BLOCK_ENTITY_STATE_LOAD_DATA.invoke(blockState, nativeTag);
            if (blockState instanceof Sign sign) {
                Object text;
                if ((text = tag.getValue().get("front_text")) != null && text instanceof CompoundTag textTag) {
                    setSignTextHack(sign, textTag, true);
                }
                if ((text = tag.getValue().get("back_text")) != null && text instanceof CompoundTag textTag) {
                    setSignTextHack(sign, textTag, false);
                }
            }
            CRAFT_BLOCK_ENTITY_STATE_UPDATE.invoke(blockState, FORCE_UPDATE_STATE, UPDATE_TRIGGER_PHYSICS);
        } catch (Throwable e) {
            LOGGER.error("Failed to update tile entity", e);
        }
        return false;
    }

    /**
     * Set sign content on the bukkit tile entity. The server does not load sign content applied via the main logic
     * (CraftBlockEntity#load), as the SignEntity needs to have a valid ServerLevel assigned to it.
     * That's not possible on worldgen; therefore, this hack has to be used additionally.
     * <br />
     * Modern sign content (non-plain-text sign lines) require Paper.
     *
     * @param sign  The sign to apply data onto.
     * @param text  The compound tag containing the data for the sign side ({@code front_text} / {@code back_text})
     * @param front If the compound tag contains the data for the front side.
     * @throws Throwable if something went wrong when reflectively updating the sign.
     */
    private static void setSignTextHack(Sign sign, CompoundTag text, boolean front) throws Throwable {
        final SignSide side = sign.getSide(front ? Side.FRONT : Side.BACK);
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
                if (line instanceof StringTag stringTag) {
                    //noinspection deprecation - Paper deprecatiom
                    side.setLine(i, stringTag.getValue());
                    continue;
                }
                if (line instanceof ListTag || line instanceof CompoundTag) {
                    if (!initializeSignHack()) {
                        continue;
                    }
                    // Minecraft uses mixed lists / arrays in their sign texts. One line can be a complex component, whereas
                    // the following line could simply be a string. Those simpler lines are represented as `{"": ""}` (only in
                    // SNBT those will be shown as a standard string). Adventure can't parse those, so we handle these lines as
                    // plaintext lines (can't contain any other extra data either way).
                    if (line instanceof CompoundTag compoundTag && compoundTag.getValue().containsKey("")) {
                        //noinspection deprecation - Paper deprecatiom
                        side.setLine(i, compoundTag.getString(""));
                        continue;
                    }
                    // serializes the line content from JNBT to Gson JSON objects, passes that to adventure and deserializes
                    // into an adventure component.
                    BUKKIT_SIGN_SIDE_LINE_SET.invoke(
                            side, i, GSON_SERIALIZER_DESERIALIZE_TREE.invoke(
                                    KYORI_GSON_SERIALIZER,
                                    GSON.toJsonTree(line.getValue())
                            )
                    );
                }
            }
        }
    }

    private static boolean initializeSignHack() {
        if (FAILED_SIGN_INITIALIZATION) {
            return false;
        }
        if (KYORI_GSON_SERIALIZER != null) {
            return true; // already initialized
        }
        if (!PaperLib.isPaper()) {
            if (!PAPER_SIGN_NOTIFIED) {
                PAPER_SIGN_NOTIFIED = true;
                LOGGER.error("Can't populate non-plain sign line. To load modern sign content, use Paper.");
            }
            return false;
        }
        try {
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
            return true;
        } catch (Throwable e) {
            FAILED_SIGN_INITIALIZATION = true;
            LOGGER.error("Failed to initialize sign-hack. Signs populated by schematics might not have their line contents.", e);
            return false;
        }
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
            NoSuchMethodException, IllegalAccessException, ClassNotFoundException {
        for (final Method method : craftBlockEntityStateClass.getMethods()) {
            if (method.getName().equals("loadData") && method.getParameterCount() == 1) {
                return LOOKUP.unreflect(method);
            }
        }
        throw new NoSuchMethodException("Couldn't find #loadData(CompoundTag) in " + craftBlockEntityStateClass.getName());
    }

    private static MethodHandle findCraftBlockEntityStateUpdateMethodHandle(Class<?> craftBlockEntityStateClass) throws
            NoSuchMethodException, IllegalAccessException, ClassNotFoundException {
        for (final Method method : craftBlockEntityStateClass.getMethods()) {
            if (method.getReturnType().equals(Boolean.TYPE) && method.getParameterCount() == 2 &&
                    method.getParameterTypes()[0] == Boolean.TYPE && method.getParameterTypes()[1] == Boolean.TYPE) {
                return LOOKUP.unreflect(method);
            }
        }
        throw new NoSuchMethodException("Couldn't find method for #update(boolean, boolean) in " + craftBlockEntityStateClass.getName());
    }

}
