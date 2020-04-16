/*
 *
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.plot.flag.types;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Container that class either contains a {@link BlockType}
 * or a {@link BlockCategory}
 */
@EqualsAndHashCode public class BlockTypeWrapper {

    @Nullable @Getter private final BlockType blockType;
    @Nullable @Getter private final BlockCategory blockCategory;

    private BlockTypeWrapper(@NotNull final BlockType blockType) {
        this.blockType = Preconditions.checkNotNull(blockType);
        this.blockCategory = null;
    }

    private BlockTypeWrapper(@NotNull final BlockCategory blockCategory) {
        this.blockType = null;
        this.blockCategory = Preconditions.checkNotNull(blockCategory);
    }

    @Override public String toString() {
        if (this.blockType != null) {
            final String key = this.blockType.toString();
            if (key.startsWith("minecraft:")) {
                return key.substring(10);
            } else {
                return key;
            }
        } else if(this.blockCategory != null) {
            final String key = this.blockCategory.toString();
            if (key.startsWith("minecraft:")) {
                return '#' + key.substring(10);
            } else {
                return '#' + key;
            }
        } else {
            return null;
        }
    }

    public boolean accepts(final BlockType blockType) {
        if (this.getBlockType() != null) {
            return this.getBlockType().equals(blockType);
        } else if (this.getBlockCategory() != null) {
            return this.getBlockCategory().contains(blockType);
        } else {
            return false;
        }
    }

    private static final Map<BlockType, BlockTypeWrapper> blockTypes = new HashMap<>();
    private static final Map<BlockCategory, BlockTypeWrapper> blockCategories = new HashMap<>();

    public static BlockTypeWrapper get(final BlockType blockType) {
        return blockTypes.computeIfAbsent(blockType, BlockTypeWrapper::new);
    }

    public static BlockTypeWrapper get(final BlockCategory blockCategory) {
        return blockCategories.computeIfAbsent(blockCategory, BlockTypeWrapper::new);
    }

}
