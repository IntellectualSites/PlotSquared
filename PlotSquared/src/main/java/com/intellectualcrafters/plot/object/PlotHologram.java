package com.intellectualcrafters.plot.object;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.util.PlotHelper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

/**
 * Created 2015-02-14 for PlotSquared
 *
 * @author Citymonstret
 */
public class PlotHologram {

    private static JavaPlugin plugin;
    private final PlotId id;
    private final String world;
    private Hologram hologram;

    public PlotHologram(final String world, final PlotId id) {
        this.id = id;
        this.world = world;
        this.hologram = createHologram(PlotMain.getPlotManager(world).getSignLoc(Bukkit.getWorld(world), PlotMain.getWorldSettings(world), PlotHelper.getPlot(Bukkit.getWorld(world), id)));
    }

    public static Hologram createHologram(final org.bukkit.Location location) {
        return HologramsAPI.createHologram(getPlugin(), location);
    }

    public static JavaPlugin getPlugin() {
        if (plugin == null) {
            plugin = JavaPlugin.getPlugin(PlotMain.class);
        }
        return plugin;
    }

    public static void removeAll() {
        final Collection<Hologram> holograms = HologramsAPI.getHolograms(getPlugin());
        for (final Hologram h : holograms) {
            h.delete();
        }
    }

}
