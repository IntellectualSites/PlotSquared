package com.plotsquared.bukkit.listeners;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.ChunkGenerator;

import com.intellectualcrafters.plot.PS;
import com.plotsquared.bukkit.generator.BukkitGeneratorWrapper;
import com.plotsquared.bukkit.generator.BukkitPlotGenerator;

public class WorldEvents implements Listener
{

    public static String lastWorld = null;

    public static String getName(final World world)
    {
        if ((lastWorld != null) && !lastWorld.equals("CheckingPlotSquaredGenerator"))
        {
            return lastWorld;
        }
        else
        {
            return world.getName();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public static void onWorldInit(final WorldInitEvent event)
    {
        final World world = event.getWorld();
        final String name = getName(world);
        final ChunkGenerator gen = world.getGenerator();
        if (gen instanceof BukkitPlotGenerator)
        {
            PS.get().loadWorld(name, new BukkitGeneratorWrapper(name, gen));
        }
        else
        {
            if (PS.get().config.contains("worlds." + name))
            {
                PS.get().loadWorld(name, new BukkitGeneratorWrapper(name, null));
            }
        }
        lastWorld = null;
    }
}
