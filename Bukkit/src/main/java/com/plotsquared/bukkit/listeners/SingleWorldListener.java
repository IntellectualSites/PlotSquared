package com.plotsquared.bukkit.listeners;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.worlds.PlotAreaManager;
import com.intellectualcrafters.plot.object.worlds.SinglePlotAreaManager;
import com.intellectualcrafters.plot.util.ReflectionUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.Plugin;


import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

public class SingleWorldListener implements Listener {

    private Method methodGetHandleChunk;
    private Field mustSave, done, lit, s;

    public SingleWorldListener(Plugin plugin) throws Exception {
        ReflectionUtils.RefClass classChunk = getRefClass("{nms}.Chunk");
        ReflectionUtils.RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
        this.methodGetHandleChunk = classCraftChunk.getMethod("getHandle").getRealMethod();
        this.mustSave = classChunk.getField("mustSave").getRealField();
        try {
            this.done = classChunk.getField("done").getRealField();
            this.lit = classChunk.getField("lit").getRealField();
            this.s = classChunk.getField("s").getRealField();
        } catch (Throwable ignore) {
            ignore.printStackTrace();
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void markChunkAsClean(Chunk chunk) {
        try {
            Object nmsChunk = methodGetHandleChunk.invoke(chunk);
            if (done != null)       this.done.set(nmsChunk, true);
            if (mustSave != null)   this.mustSave.set(nmsChunk, false);
            if (lit != null)        this.lit.set(nmsChunk, false);
            if (s != null)          this.s.set(nmsChunk, false);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void handle(ChunkEvent event) {
        World world = event.getWorld();
        String name = world.getName();
        PlotAreaManager man = PS.get().getPlotAreaManager();
        if (!(man instanceof SinglePlotAreaManager)) return;
        if (!isPlotId(name)) return;

        markChunkAsClean(event.getChunk());
    }

//    @EventHandler
//    public void onPopulate(ChunkPopulateEvent event) {
//        handle(event);
//    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkLoad(ChunkLoadEvent event) {
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
