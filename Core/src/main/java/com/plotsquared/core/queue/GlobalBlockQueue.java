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
package com.plotsquared.core.queue;

import com.plotsquared.core.PlotSquared;
import com.sk89q.worldedit.world.World;
import org.checkerframework.checker.nullness.qual.NonNull;

public class GlobalBlockQueue {

    private QueueProvider provider;

    public GlobalBlockQueue(@NonNull QueueProvider provider) {
        this.provider = provider;
    }

    /**
     * Get a new {@link QueueCoordinator} for the given world.
     *
     * @param world world to get new queue for
     * @return new QueueCoordinator for world
     */
    public @NonNull QueueCoordinator getNewQueue(@NonNull World world) {
        QueueCoordinator queue = provider.getNewQueue(world);
        // Auto-inject into the queue
        PlotSquared.platform().injector().injectMembers(queue);
        return queue;
    }

    public QueueProvider getProvider() {
        return this.provider;
    }

    public void setQueueProvider(@NonNull QueueProvider provider) {
        this.provider = provider;
    }

}
