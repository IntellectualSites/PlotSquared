//package com.intellectualcrafters.plot.object;
//
//import java.util.Collection;
//
//import org.bukkit.Bukkit;
//import org.bukkit.plugin.java.JavaPlugin;
//
//import com.gmail.filoghost.holographicdisplays.api.Hologram;
//import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
//import com.intellectualcrafters.plot.PlotSquared;
//import com.intellectualcrafters.plot.util.PlotHelper;
//
///**
// * Created 2015-02-14 for PlotSquared
// *
// * @author Citymonstret
// */
//public class PlotHologram {
//
//    private static JavaPlugin plugin;
//    private final PlotId id;
//    private final String world;
//    private Hologram hologram;
//
//    public PlotHologram(final String world, final PlotId id) {
//        this.id = id;
//        this.world = world;
//        this.hologram = createHologram(PlotSquared.getPlotManager(world).getSignLoc(Bukkit.getWorld(world), PlotSquared.getWorldSettings(world), PlotHelper.getPlot(Bukkit.getWorld(world), id)));
//    }
//
//    public static Hologram createHologram(final org.bukkit.Location location) {
//        return HologramsAPI.createHologram(getPlugin(), location);
//    }
//
//    public static JavaPlugin getPlugin() {
//        if (plugin == null) {
//            plugin = JavaPlugin.getPlugin(PlotSquared.class);
//        }
//        return plugin;
//    }
//
//    public static void removeAll() {
//        final Collection<Hologram> holograms = HologramsAPI.getHolograms(getPlugin());
//        for (final Hologram h : holograms) {
//            h.delete();
//        }
//    }
//
//}
