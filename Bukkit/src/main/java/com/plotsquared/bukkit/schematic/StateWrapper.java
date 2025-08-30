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

import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.util.ReflectionUtils;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.extension.platform.NoCapablePlatformException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

@ApiStatus.Internal
public class StateWrapper {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + StateWrapper.class.getSimpleName());

    private static boolean FAILED_INITIALIZATION = false;
    private static BukkitImplAdapter ADAPTER = null;
    private static Class<?> LIN_TAG_CLASS = null;
    private static Class<?> JNBT_TAG_CLASS = null;
    private static Class<?> CRAFT_BLOCK_ENTITY_STATE_CLASS = null;
    private static MethodHandle PAPERWEIGHT_ADAPTER_FROM_NATIVE = null;
    private static MethodHandle CRAFT_BLOCK_ENTITY_STATE_LOAD_DATA = null;
    private static MethodHandle CRAFT_BLOCK_ENTITY_STATE_UPDATE = null;
    private static MethodHandle TO_LIN_TAG = null;

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
        if (ADAPTER == null) {
            try {
                initializeNbtCompoundClassType();
                ReflectionUtils.RefClass worldEditPluginRefClass = ReflectionUtils.getRefClass(WorldEditPlugin.class);
                WorldEditPlugin worldEditPlugin = (WorldEditPlugin) worldEditPluginRefClass
                        .getMethod("getInstance")
                        .of(null)
                        .call();
                ADAPTER = (BukkitImplAdapter) worldEditPluginRefClass
                        .getMethod("getBukkitImplAdapter")
                        .of(worldEditPlugin)
                        .call();
                PAPERWEIGHT_ADAPTER_FROM_NATIVE = findPaperweightAdapterFromNativeMethodHandle(ADAPTER.getClass());
                TO_LIN_TAG = findToLinTagMethodHandle(LIN_TAG_CLASS);
            } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | NoCapablePlatformException e) {
                LOGGER.error(
                        "Failed to access required WorldEdit methods, which are required to populate block data from " +
                                "schematics. Pasted blocks will not have their respective data", e
                );
                FAILED_INITIALIZATION = true;
                return false;
            }
            try {
                CRAFT_BLOCK_ENTITY_STATE_CLASS = findCraftBlockEntityStateClass();
                CRAFT_BLOCK_ENTITY_STATE_LOAD_DATA = findCraftBlockEntityStateLoadDataMethodHandle(CRAFT_BLOCK_ENTITY_STATE_CLASS);
                CRAFT_BLOCK_ENTITY_STATE_UPDATE = findCraftBlockEntityStateUpdateMethodHandle(CRAFT_BLOCK_ENTITY_STATE_CLASS);
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
                LOGGER.error(
                        "Failed to initialize required method accessors for block state population.",
                        e
                );
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
            CRAFT_BLOCK_ENTITY_STATE_UPDATE.invoke(blockState, true, false);
        } catch (Throwable e) {
            LOGGER.error("Failed to update tile entity", e);
        }
        return false;
    }

    private static void initializeNbtCompoundClassType() throws ClassNotFoundException {
        try {
            LIN_TAG_CLASS = Class.forName("org.enginehub.linbus.tree.LinTag");
        } catch (ClassNotFoundException e) {
            JNBT_TAG_CLASS = Class.forName("com.sk89q.jnbt.Tag");
        }
    }

    private static MethodHandle findToLinTagMethodHandle(Class<?> LIN_TAG_CLASS)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
        if (LIN_TAG_CLASS == null) {
            return null;
        }
        return MethodHandles.lookup().findVirtual(
                Class.forName("org.enginehub.linbus.tree.ToLinTag"),
                "toLinTag",
                MethodType.methodType(LIN_TAG_CLASS)
        );
    }

    private static MethodHandle findPaperweightAdapterFromNativeMethodHandle(Class<?> adapterClass)
            throws IllegalAccessException, NoSuchMethodException {
        final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(adapterClass, MethodHandles.lookup());
        if (JNBT_TAG_CLASS != null) {
            // usage of JNBT = identical method signatures for WE and FAWE
            return lookup.findVirtual(adapterClass, "fromNative", MethodType.methodType(Object.class, JNBT_TAG_CLASS));
        }
        try {
            // FAWE
            return lookup.findVirtual(adapterClass, "fromNativeLin", MethodType.methodType(Object.class, LIN_TAG_CLASS));
        } catch (NoSuchMethodException e) {
            // WE
            return lookup.findVirtual(adapterClass, "fromNative", MethodType.methodType(Object.class, LIN_TAG_CLASS));
        }
    }

    private static MethodHandle findCraftBlockEntityStateLoadDataMethodHandle(Class<?> craftBlockEntityStateClass)
            throws NoSuchMethodException, IllegalAccessException, ClassNotFoundException {
        final Class<?> compoundTagClass = Class.forName("net.minecraft.nbt.CompoundTag");
        for (final Method method : craftBlockEntityStateClass.getMethods()) {
            if (method.getReturnType().equals(Void.TYPE) && method.getParameterCount() == 1
                    && method.getParameterTypes()[0] == compoundTagClass) {
                return MethodHandles.lookup().unreflect(method);
            }
        }
        throw new NoSuchMethodException("Couldn't find method for #loadData(CompoundTag) in " + compoundTagClass.getName());
    }

    private static MethodHandle findCraftBlockEntityStateUpdateMethodHandle(Class<?> craftBlockEntityStateClass)
            throws NoSuchMethodException, IllegalAccessException, ClassNotFoundException {
        final Class<?> compoundTagClass = Class.forName("net.minecraft.nbt.CompoundTag");
        for (final Method method : craftBlockEntityStateClass.getMethods()) {
            if (method.getReturnType().equals(Boolean.TYPE) && method.getParameterCount() == 2
                    && method.getParameterTypes()[0] == Boolean.TYPE && method.getParameterTypes()[1] == Boolean.TYPE) {
                return MethodHandles.lookup().unreflect(method);
            }
        }
        throw new NoSuchMethodException("Couldn't find method for #update(boolean, boolean) in " + compoundTagClass.getName());
    }

    private static Class<?> findCraftBlockEntityStateClass() throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit.block.CraftBlockEntityState");
    }

}
