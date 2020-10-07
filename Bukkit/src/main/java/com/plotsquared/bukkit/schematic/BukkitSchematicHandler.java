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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.schematic;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.WorldUtil;
import com.sk89q.jnbt.CompoundTag;

import javax.annotation.Nonnull;

/**
 * Schematic Handler.
 */
@Singleton public class BukkitSchematicHandler extends SchematicHandler {

    @Inject public BukkitSchematicHandler(@Nonnull final WorldUtil worldUtil) {
        super(worldUtil);
    }

    @Override
    public boolean restoreTile(QueueCoordinator queue, CompoundTag ct, int x, int y, int z) {
        return new StateWrapper(ct).restoreTag(queue.getWorld().getName(), x, y, z);
    }
}
