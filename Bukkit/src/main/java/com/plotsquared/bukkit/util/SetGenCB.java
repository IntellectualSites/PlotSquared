package com.plotsquared.bukkit.util;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.GeneratorWrapper;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.plotsquared.bukkit.generator.BukkitAugmentedGenerator;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

public class SetGenCB {

    public static void setGenerator(final World world) throws Exception {
        SetupUtils.manager.updateGenerators();
        PS.get().removePlotAreas(world.getName());
        final ChunkGenerator gen = world.getGenerator();
        if (gen == null) {
            return;
        }
        final String name = gen.getClass().getCanonicalName();
        boolean set = false;
        for (final GeneratorWrapper<?> wrapper : SetupUtils.generators.values()) {
            ChunkGenerator newGen = (ChunkGenerator) wrapper.getPlatformGenerator();
            if (newGen == null) {
                newGen = (ChunkGenerator) wrapper;
            }
            if (newGen.getClass().getCanonicalName().equals(name)) {
                // set generator
                final Field generator = world.getClass().getDeclaredField("generator");
                final Field populators = world.getClass().getDeclaredField("populators");
                generator.setAccessible(true);
                populators.setAccessible(true);
                // Set populators (just in case)
                populators.set(world, new ArrayList<>());
                // Set generator
                generator.set(world, newGen);
                populators.set(world, newGen.getDefaultPopulators(world));
                // end
                set = true;
                break;
            }
        }
        if (!set) {
            final Iterator<BlockPopulator> iter = world.getPopulators().iterator();
            while (iter.hasNext()) {
                if (iter.next() instanceof BukkitAugmentedGenerator) {
                    iter.remove();
                }
            }
        }
        PS.get().loadWorld(world.getName(), PS.get().IMP.getGenerator(world.getName(), null));
    }
}
