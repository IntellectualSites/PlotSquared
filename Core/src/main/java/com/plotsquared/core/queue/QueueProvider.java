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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class QueueProvider {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + PlotSquared.class.getSimpleName());

    public static QueueProvider of(final @NonNull Class<? extends QueueCoordinator> primary) {
        return new QueueProvider() {

            @Override
            public QueueCoordinator getNewQueue(@NonNull World world) {
                try {
                    return (QueueCoordinator) primary.getConstructors()[0].newInstance(world);
                } catch (Throwable e) {
                    LOGGER.error("Error creating Queue: {} - Does it have the correct constructor(s)?", primary.getName());
                    if (!primary.getName().contains("com.plotsquared")) {
                        LOGGER.error(
                                "It looks like {} is a custom queue. Please look for a plugin in its classpath and report to them.",
                                primary.getSimpleName()
                        );
                    }
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    /**
     * Get a queue for the given world
     *
     * @param world world
     * @return new QueueCoordinator
     */
    public abstract QueueCoordinator getNewQueue(@NonNull World world);

}
