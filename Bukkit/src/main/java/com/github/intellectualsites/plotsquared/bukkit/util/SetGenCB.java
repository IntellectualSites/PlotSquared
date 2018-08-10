package com.github.intellectualsites.plotsquared.bukkit.util;

import com.github.intellectualsites.plotsquared.bukkit.generator.BukkitAugmentedGenerator;
import com.github.intellectualsites.plotsquared.plot.PS;
import com.github.intellectualsites.plotsquared.plot.generator.GeneratorWrapper;
import com.github.intellectualsites.plotsquared.plot.util.SetupUtils;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

public class SetGenCB {

    public static void setGenerator(World world) throws Exception {
        SetupUtils.manager.updateGenerators();
        PS.get().removePlotAreas(world.getName());
        ChunkGenerator gen = world.getGenerator();
        if (gen == null) {
            return;
        }
        String name = gen.getClass().getCanonicalName();
        boolean set = false;
        for (GeneratorWrapper<?> wrapper : SetupUtils.generators.values()) {
            ChunkGenerator newGen = (ChunkGenerator) wrapper.getPlatformGenerator();
            if (newGen == null) {
                newGen = (ChunkGenerator) wrapper;
            }
            if (newGen.getClass().getCanonicalName().equals(name)) {
                // set generator
                Field generator = world.getClass().getDeclaredField("generator");
                Field populators = world.getClass().getDeclaredField("populators");
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
            Iterator<BlockPopulator> iterator = world.getPopulators().iterator();
            while (iterator.hasNext()) {
                if (iterator.next() instanceof BukkitAugmentedGenerator) {
                    iterator.remove();
                }
            }
        }
        PS.get().loadWorld(world.getName(), PS.get().IMP.getGenerator(world.getName(), null));
    }
}
