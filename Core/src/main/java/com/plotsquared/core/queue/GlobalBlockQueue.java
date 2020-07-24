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
package com.plotsquared.core.queue;

import com.plotsquared.core.PlotSquared;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class GlobalBlockQueue {

    private final ConcurrentLinkedDeque<QueueCoordinator> activeQueues;
    private QueueProvider provider;

    public GlobalBlockQueue(@Nonnull QueueProvider provider) {
        this.provider = provider;
        this.activeQueues = new ConcurrentLinkedDeque<>();
    }

    /**
     * Get a new {@link QueueCoordinator} for the given world.
     */
    @Nonnull public QueueCoordinator getNewQueue(@Nonnull World world) {
        QueueCoordinator queue = provider.getNewQueue(world);
        // Auto-inject into the queue
        PlotSquared.platform().getInjector().injectMembers(queue);
        return queue;
    }

    public QueueProvider getProvider() {
        return this.provider;
    }

    public void setQueueProvider(@Nonnull QueueProvider provider) {
        this.provider = provider;
    }

    /**
     * Place an instance of {@link QueueCoordinator} into a list incase access is needed
     * and then start it.
     *
     * @param queue {@link QueueCoordinator} instance to start.
     * @return true if added to queue, false otherwise
     */
    public boolean enqueue(@Nonnull QueueCoordinator queue) {
        boolean success = false;
        if (queue.size() > 0 && !activeQueues.contains(queue)) {
            success = activeQueues.add(queue);
            queue.start();
        }
        return success;
    }

    public void dequeue(@Nonnull QueueCoordinator queue) {
        queue.cancel();
        activeQueues.remove(queue);
    }

    @Nonnull public List<QueueCoordinator> getActiveQueues() {
        return new ArrayList<>(activeQueues);
    }

    public boolean isDone() {
        return activeQueues.size() == 0;
    }
}
