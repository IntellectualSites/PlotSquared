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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.plotsquared.core.inject.factory.ProgressSubscriberFactory;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.WorldUtil;
import com.sk89q.jnbt.CompoundTag;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Schematic Handler.
 */
@Singleton
public class BukkitSchematicHandler extends SchematicHandler {

    @Inject
    public BukkitSchematicHandler(final @NonNull WorldUtil worldUtil, @NonNull ProgressSubscriberFactory subscriberFactory) {
        super(worldUtil, subscriberFactory);
    }

    @Override
    public boolean restoreTile(QueueCoordinator queue, CompoundTag ct, int x, int y, int z) {
        return new StateWrapper(ct).restoreTag(queue.getWorld().getName(), x, y, z);
    }

}
