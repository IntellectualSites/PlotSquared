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
package com.plotsquared.core.util.task;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

public class ObjectTaskRunnable<T> implements Runnable {

    private final CompletableFuture<Void> completionFuture = new CompletableFuture<>();

    private final Iterator<T> iterator;
    private final RunnableVal<T> task;

    public ObjectTaskRunnable(
            final Iterator<T> iterator,
            final RunnableVal<T> task
    ) {
        this.iterator = iterator;
        this.task = task;
    }

    public CompletableFuture<Void> getCompletionFuture() {
        return this.completionFuture;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        boolean hasNext;
        while ((hasNext = iterator.hasNext()) && System.currentTimeMillis() - start < 5) {
            task.value = iterator.next();
            task.run();
        }
        if (!hasNext) {
            completionFuture.complete(null);
        } else {
            TaskManager.runTaskLater(this, TaskTime.ticks(1L));
        }
    }

}
