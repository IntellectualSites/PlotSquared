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
package com.plotsquared.bukkit.listener;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import com.plotsquared.core.util.ReflectionUtils;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.lang.reflect.Method;

import static com.plotsquared.core.util.ReflectionUtils.getRefClass;

public class SingleWorldListener implements Listener {

    private final Method methodSetUnsaved;
    private Method methodGetHandleChunk;
    private Object objChunkStatusFull = null;

    public SingleWorldListener() throws Exception {
        ReflectionUtils.RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
        ReflectionUtils.RefClass classChunkAccess = getRefClass("net.minecraft.world.level.chunk.IChunkAccess");
        this.methodSetUnsaved = classChunkAccess.getMethod("a", boolean.class).getRealMethod();
        try {
            this.methodGetHandleChunk = classCraftChunk.getMethod("getHandle").getRealMethod();
        } catch (NoSuchMethodException ignored) {
            try {
                String chunkStatus = PlotSquared.platform().serverVersion()[1] < 21
                        ? "net.minecraft.world.level.chunk" + ".ChunkStatus"
                        : "net.minecraft.world.level.chunk.status.ChunkStatus";
                ReflectionUtils.RefClass classChunkStatus = getRefClass(chunkStatus);
                this.objChunkStatusFull = classChunkStatus.getRealClass().getField("n").get(null);
                this.methodGetHandleChunk = classCraftChunk
                        .getMethod("getHandle", classChunkStatus.getRealClass())
                        .getRealMethod();
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void markChunkAsClean(Chunk chunk) {
        try {
            Object nmsChunk = objChunkStatusFull != null
                    ? this.methodGetHandleChunk.invoke(chunk, objChunkStatusFull)
                    : this.methodGetHandleChunk.invoke(chunk);
            methodSetUnsaved.invoke(nmsChunk, false);
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
        if (!SinglePlotArea.isSinglePlotWorld(name)) {
            return;
        }
        int x = event.getChunk().getX();
        int z = event.getChunk().getZ();
        if (x < 16 && x > -16 && z < 16 && z > -16) {
            // Allow spawn to generate
            return;
        }
        markChunkAsClean(event.getChunk());
    }

    //    @EventHandler
    //    public void onPopulate(ChunkPopulateEvent event) {
    //        handle(event);
    //    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkLoad(ChunkLoadEvent event) {
        // disable this for now, should address https://github.com/IntellectualSites/PlotSquared/issues/4413
        // handle(event);
    }

}
