package com.plotsquared.bukkit.util.bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import com.intellectualcrafters.plot.PS;
import com.plotsquared.bukkit.generator.AugmentedPopulator;
import com.plotsquared.bukkit.util.SetupUtils;

public class SetGenCB {
    public static void setGenerator(World world) throws Exception {
        SetupUtils.manager.updateGenerators();
        PS.get().removePlotWorldAbs(world.getName());
        ChunkGenerator gen = world.getGenerator();
        if (gen == null) {
            return;
        }
        String name = gen.getClass().getCanonicalName();
        boolean set = false;
        for (ChunkGenerator newGen : SetupUtils.generators.values()) {
            if (newGen.getClass().getCanonicalName().equals(name)) {
                // set generator
                Field generator = world.getClass().getDeclaredField("generator");
                Field populators = world.getClass().getDeclaredField("populators");
                generator.setAccessible(true);
                populators.setAccessible(true);
                // Set populators (just in case)
                populators.set(world, new ArrayList<>());
                // Set generator
                Constructor<? extends ChunkGenerator> constructor = newGen.getClass().getConstructor(String.class);
                ChunkGenerator newNewGen = constructor.newInstance(world.getName());
                generator.set(world, newNewGen);
                populators.set(world, newNewGen.getDefaultPopulators(world));
                // end
                set = true;
                break;
            }
        }
        if (!set) {
            Iterator<BlockPopulator> iter = world.getPopulators().iterator();
            while (iter.hasNext()) {
                if (iter.next() instanceof AugmentedPopulator) {
                    iter.remove();
                }
            }
        }
        PS.get().loadWorld(world.getName(), PS.get().IMP.getGenerator(world.getName(), null));
    }
}
