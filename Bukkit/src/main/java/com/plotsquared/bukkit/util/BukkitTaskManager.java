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
package com.plotsquared.bukkit.util;

import com.plotsquared.bukkit.BukkitPlatform;
import com.plotsquared.core.util.task.TaskManager;
import org.bukkit.Bukkit;

public class BukkitTaskManager extends TaskManager {

    private final BukkitPlatform bukkitMain;

    public BukkitTaskManager(BukkitPlatform bukkitMain) {
        this.bukkitMain = bukkitMain;
    }

    @Override public int taskRepeat(Runnable runnable, int interval) {
        return this.bukkitMain.getServer().getScheduler()
            .scheduleSyncRepeatingTask(this.bukkitMain, runnable, interval, interval);
    }

    @SuppressWarnings("deprecation") @Override
    public int taskRepeatAsync(Runnable runnable, int interval) {
        return this.bukkitMain.getServer().getScheduler()
            .scheduleAsyncRepeatingTask(this.bukkitMain, runnable, interval, interval);
    }

    @Override public void taskAsync(Runnable runnable) {
        if (this.bukkitMain.isEnabled()) {
            this.bukkitMain.getServer().getScheduler()
                .runTaskAsynchronously(this.bukkitMain, runnable);
        } else {
            runnable.run();
        }
    }

    @Override public void task(Runnable runnable) {
        this.bukkitMain.getServer().getScheduler().runTask(this.bukkitMain, runnable).getTaskId();
    }

    @Override public void taskLater(Runnable runnable, int delay) {
        this.bukkitMain.getServer().getScheduler().runTaskLater(this.bukkitMain, runnable, delay)
            .getTaskId();
    }

    @Override public void taskLaterAsync(Runnable runnable, int delay) {
        this.bukkitMain.getServer().getScheduler()
            .runTaskLaterAsynchronously(this.bukkitMain, runnable, delay);
    }

    @Override public void cancelTask(int task) {
        if (task != -1) {
            Bukkit.getScheduler().cancelTask(task);
        }
    }
}
