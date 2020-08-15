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
package com.plotsquared.core.queue;

import com.plotsquared.core.PlotSquared;
import com.sk89q.worldedit.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

public abstract class QueueProvider {

    private static final Logger logger = LoggerFactory.getLogger("P2/" + PlotSquared.class.getSimpleName());

    public static QueueProvider of(@Nonnull final Class<? extends QueueCoordinator> primary) {
        return new QueueProvider() {

            @Override public QueueCoordinator getNewQueue(@Nonnull World world) {
                try {
                    return (QueueCoordinator) primary.getConstructors()[0].newInstance(world);
                } catch (Throwable e) {
                    logger.error("Error creating Queue: {} - Does it have the correct constructor(s)?", primary.getName());
                    if (!primary.getName().contains("com.plotsquared")) {
                        logger.error("It looks like {} is a custom queue. Please look for a plugin in its classpath and report to them.",
                            primary.getSimpleName());
                    }
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    /**
     * Get a queue for the given world
     */
    public abstract QueueCoordinator getNewQueue(@Nonnull World world);
}
