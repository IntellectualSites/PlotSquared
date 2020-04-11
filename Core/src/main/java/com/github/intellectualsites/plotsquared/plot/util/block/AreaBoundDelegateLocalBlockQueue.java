/*
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
package com.github.intellectualsites.plotsquared.plot.util.block;

import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;

public class AreaBoundDelegateLocalBlockQueue extends DelegateLocalBlockQueue {

    @Getter private final PlotArea area;

    public AreaBoundDelegateLocalBlockQueue(@NotNull final PlotArea area,
        @Nullable final LocalBlockQueue parent) {
        super(parent);
        this.area = Objects.requireNonNull(area);
    }

    @Override public boolean setBlock(int x, int y, int z, BlockState id) {
        if (area.contains(x, z)) {
            return super.setBlock(x, y, z, id);
        }
        return false;
    }

    @Override public boolean setBlock(int x, int y, int z, BaseBlock id) {
        if (area.contains(x, z)) {
            return super.setBlock(x, y, z, id);
        }
        return false;
    }

    @Override public boolean setBlock(int x, int y, int z, Pattern pattern) {
        if (area.contains(x, z)) {
            return super.setBlock(x, y, z, pattern);
        }
        return false;
    }

    @Override public boolean setBiome(int x, int z, BiomeType biome) {
        if (area.contains(x, z)) {
            return super.setBiome(x, z, biome);
        }
        return false;
    }

}
