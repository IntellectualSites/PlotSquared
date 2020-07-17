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
package com.plotsquared.core.util.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class ObjectTaskRunnable<T> implements Runnable {

    @Getter private final CompletableFuture<Void> completionFuture = new CompletableFuture<>();

    private final Iterator<T> iterator;
    private final RunnableVal<T> task;

    @Override public void run() {
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
