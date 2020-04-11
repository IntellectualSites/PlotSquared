package com.plotsquared.bukkit.util;

import com.plotsquared.bukkit.generator.BukkitAugmentedGenerator;
import com.plotsquared.PlotSquared;
import com.plotsquared.generator.GeneratorWrapper;
import com.plotsquared.util.SetupUtils;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class SetGenCB {

    public static void setGenerator(World world) throws Exception {
        SetupUtils.manager.updateGenerators();
        PlotSquared.get().removePlotAreas(world.getName());
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
            world.getPopulators()
                .removeIf(blockPopulator -> blockPopulator instanceof BukkitAugmentedGenerator);
        }
        PlotSquared.get()
            .loadWorld(world.getName(), PlotSquared.get().IMP.getGenerator(world.getName(), null));
    }
}
