package com.plotsquared.bukkit.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.PlotGenerator;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.plotsquared.bukkit.generator.AugmentedPopulator;

public class SetGenCB
{
    public static void setGenerator(final World world) throws Exception
    {
        SetupUtils.manager.updateGenerators();
        PS.get().removePlotWorldAbs(world.getName());
        final ChunkGenerator gen = world.getGenerator();
        if (gen == null) { return; }
        final String name = gen.getClass().getCanonicalName();
        boolean set = false;
        for (final PlotGenerator<?> wrapper : SetupUtils.generators.values())
        {
            final ChunkGenerator newGen = (ChunkGenerator) wrapper.generator;
            if (newGen.getClass().getCanonicalName().equals(name))
            {
                // set generator
                final Field generator = world.getClass().getDeclaredField("generator");
                final Field populators = world.getClass().getDeclaredField("populators");
                generator.setAccessible(true);
                populators.setAccessible(true);
                // Set populators (just in case)
                populators.set(world, new ArrayList<>());
                // Set generator
                final Constructor<? extends ChunkGenerator> constructor = newGen.getClass().getConstructor(String.class);
                final ChunkGenerator newNewGen = constructor.newInstance(world.getName());
                generator.set(world, newNewGen);
                populators.set(world, newNewGen.getDefaultPopulators(world));
                // end
                set = true;
                break;
            }
        }
        if (!set)
        {
            final Iterator<BlockPopulator> iter = world.getPopulators().iterator();
            while (iter.hasNext())
            {
                if (iter.next() instanceof AugmentedPopulator)
                {
                    iter.remove();
                }
            }
        }
        PS.get().loadWorld(world.getName(), PS.get().IMP.getGenerator(world.getName(), null));
    }
}
