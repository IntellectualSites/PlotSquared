package com.github.intellectualsites.plotsquared.nukkit.generator;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.level.ChunkLoadEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import com.github.intellectualsites.plotsquared.nukkit.NukkitMain;
import com.github.intellectualsites.plotsquared.plot.PS;
import com.github.intellectualsites.plotsquared.plot.generator.AugmentedUtils;

import java.util.concurrent.ConcurrentHashMap;

public class NukkitAugmentedGenerator implements Listener {

    private static NukkitAugmentedGenerator generator;
    private static ConcurrentHashMap<String, NukkitAugmentedGenerator> generators =
        new ConcurrentHashMap<>();

    private NukkitAugmentedGenerator(NukkitMain plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static NukkitAugmentedGenerator get(Level level) {
        NukkitAugmentedGenerator current = generators.get(level.getName());
        if (current != null) {
            return current;
        }
        if (generator == null) {
            NukkitMain plugin = ((NukkitMain) PS.get().IMP);
            generator = new NukkitAugmentedGenerator(plugin);
        }
        generators.put(level.getName(), generator);
        return generator;
    }

    @EventHandler private void onChunkLoad(ChunkLoadEvent event) {
        Level level = event.getLevel();
        generator = generators.get(level.getName());
        if (generator != null) {
            generator.populate(level, event.getChunk());
        }
    }

    private void populate(Level world, FullChunk chunk) {
        AugmentedUtils.generate(world.getName(), chunk.getX(), chunk.getZ(), null);
    }
}
