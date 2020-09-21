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

package com.plotsquared.bukkit.listener;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import com.plotsquared.core.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.plotsquared.core.util.ReflectionUtils.getRefClass;

@SuppressWarnings("unused")
public class SingleWorldListener implements Listener {

    private Method methodGetHandleChunk;
    private Field mustSave;

    public SingleWorldListener(JavaPlugin plugin) throws Exception {
        ReflectionUtils.RefClass classChunk = getRefClass("{nms}.Chunk");
        ReflectionUtils.RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
        this.methodGetHandleChunk = classCraftChunk.getMethod("getHandle").getRealMethod();
        try {
            this.mustSave = classChunk.getField("mustSave").getRealField();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void markChunkAsClean(Chunk chunk) {
        try {
            Object nmsChunk = methodGetHandleChunk.invoke(chunk);
            if (mustSave != null) {
                this.mustSave.set(nmsChunk, false);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void handle(ChunkEvent event) {
        World world = event.getWorld();
        String name = world.getName();
        PlotAreaManager man = PlotSquared.get().getPlotAreaManager();
        if (!(man instanceof SinglePlotAreaManager)) {
            return;
        }
        if (!isPlotId(name)) {
            return;
        }

        markChunkAsClean(event.getChunk());
    }

    //    @EventHandler
    //    public void onPopulate(ChunkPopulateEvent event) {
    //        handle(event);
    //    }

    @EventHandler(priority = EventPriority.LOWEST) public void onChunkLoad(ChunkLoadEvent event) {
        handle(event);
    }

    private boolean isPlotId(String worldName) {
        int len = worldName.length();
        int separator = 0;
        for (int i = 0; i < len; i++) {
            switch (worldName.charAt(i)) {
                case ',':
                case ';':
                    separator++;
                    break;
                case '-':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
                default:
                    return false;
            }
        }
        return separator == 1;
    }
}
