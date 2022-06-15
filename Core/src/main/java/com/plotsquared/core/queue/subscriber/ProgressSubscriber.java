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
package com.plotsquared.core.queue.subscriber;

import com.plotsquared.core.queue.ChunkCoordinator;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface ProgressSubscriber {

    /**
     * Notify about a progress update in the coordinator
     *
     * @param coordinator Coordinator instance that triggered the notification
     * @param progress    Progress in the range [0, 1]
     */
    void notifyProgress(final @NonNull ChunkCoordinator coordinator, final double progress);

    /**
     * Notify the subscriber that its parent ChunkCoordinator has finished
     */
    void notifyEnd();

}
