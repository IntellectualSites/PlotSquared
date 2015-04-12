package com.intellectualcrafters.plot.listeners.worldedit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.eventbus.EventHandler.Priority;
import com.sk89q.worldedit.util.eventbus.Subscribe;
 
public class WEListener implements Listener {
    
    public static HashSet<String> bypass = new HashSet<>();
    
    final List<String> monitored = Arrays.asList(new String[] { "set", "replace", "overlay", "walls", "outline", "deform", "hollow", "smooth", "move", "stack", "naturalize", "paste", "count", "regen", "copy", "cut", "" });
    public final Set<String> blockedcmds = new HashSet<>(Arrays.asList("/gmask", "//gmask", "/worldedit:gmask"));
    public final Set<String> restrictedcmds = new HashSet<>(Arrays.asList("/up", "//up", "/worldedit:up"));
    
    @Subscribe(priority=Priority.VERY_EARLY)
    public void onEditSession(EditSessionEvent event) {
        String world = event.getWorld().getName();
        if (!PlotSquared.isPlotWorld(world)) {
            return;
        }
        Actor actor = event.getActor();
        if (actor != null && actor.isPlayer()) {
            String name = actor.getName();
            if (bypass.contains(name)) {
                return;
            }

            PlotPlayer player = UUIDHandler.getPlayer(actor.getName());
            HashSet<RegionWrapper> mask = getMask(player);
            if (mask.size() == 0) {
                event.setExtent(new NullExtent());
                return;
            }
            try {
                Region selection = WorldEdit.getInstance().getSession(player.getName()).getSelection(event.getWorld());
                Vector pos1 = selection.getMinimumPoint();
                Vector pos2 = selection.getMaximumPoint();
                RegionWrapper regionSelection = new RegionWrapper(Math.min(pos1.getBlockX(), pos2.getBlockX()), Math.max(pos1.getBlockX(), pos2.getBlockX()), Math.min(pos1.getBlockZ(), pos2.getBlockZ()), Math.max(pos1.getBlockZ(), pos2.getBlockZ()));
                if (!regionContains(regionSelection, mask)) {
                    event.setExtent(new NullExtent());
                    return;
                }
            } catch (IncompleteRegionException e) {}
            if (Settings.CHUNK_PROCESSOR) {
                event.setExtent(new ProcessedWEExtent(mask, event.getExtent()));
            }
            else {
                event.setExtent(new WEExtent(mask, event.getExtent()));
            }
        }
    }
    
    public static boolean maskContains(HashSet<RegionWrapper> mask, int x, int z) {
        for (RegionWrapper region : mask) {
            if ((x >= region.minX) && (x <= region.maxX) && (z >= region.minZ) && (z <= region.maxZ)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean intersects(RegionWrapper region1, RegionWrapper region2) {
        if ((region1.minX <= region2.maxX) && (region1.maxX >= region2.minX) && (region1.minZ <= region2.maxZ) && (region1.maxZ >= region2.minZ)) {
            return true;
        }
        return false;
    }
    
    public static boolean regionContains(RegionWrapper selection, HashSet<RegionWrapper> mask) {
        for (RegionWrapper region : mask) {
            if (intersects(region, selection)) {
                return true;
            }
        }
        return false;
    }
    
    public HashSet<RegionWrapper> getMask(PlotPlayer player) {
        HashSet<RegionWrapper> regions = new HashSet<>();
        UUID uuid = player.getUUID();
        for (Plot plot : PlotSquared.getPlots(player.getLocation().getWorld()).values()) {
            if (!plot.settings.getMerged(1) && !plot.settings.getMerged(2)) {
                if (plot.isOwner(uuid) || plot.helpers.contains(uuid)) {
                    Location pos1 = MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
                    Location pos2 = MainUtil.getPlotTopLoc(plot.world, plot.id);
                    regions.add(new RegionWrapper(pos1.getX(), pos2.getX(), pos1.getZ(), pos2.getZ()));
                }
            }
        }
        return regions;
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerCommand(final PlayerCommandPreprocessEvent e) {
        final Player p = e.getPlayer();
        final PlotPlayer pp = BukkitUtil.getPlayer(p);
        if (!PlotSquared.isPlotWorld(p.getWorld().getName()) || Permissions.hasPermission(pp, "plots.worldedit.bypass")) {
            return;
        }
        String cmd = e.getMessage().toLowerCase();
        if (cmd.contains(" ")) {
            cmd = cmd.substring(0, cmd.indexOf(" "));
        }
        if (this.restrictedcmds.contains(cmd)) {
            final Plot plot = MainUtil.getPlot(pp.getLocation());
            if ((plot == null) || !(plot.helpers.contains(DBFunc.everyone) || plot.helpers.contains(pp.getUUID()))) {
                e.setCancelled(true);
            }
            return;
        } else if (this.blockedcmds.contains(cmd)) {
            e.setCancelled(true);
            return;
        }
        if (!Settings.REQUIRE_SELECTION) {
            return;
        }
        for (final String c : this.monitored) {
            if (cmd.equals("//" + c) || cmd.equals("/" + c) || cmd.equals("/worldedit:/" + c)) {
                final Selection selection = PlotSquared.worldEdit.getSelection(p);
                if (selection == null) {
                    return;
                }
                final BlockVector pos1 = selection.getNativeMinimumPoint().toBlockVector();
                final BlockVector pos2 = selection.getNativeMaximumPoint().toBlockVector();
                
                HashSet<RegionWrapper> mask = getMask(pp);
                if (mask.size() == 0) {
                    MainUtil.sendMessage(pp, C.REQUIRE_SELECTION_IN_MASK, "Both points");
                    return;
                }
                if (!maskContains(mask, pos1.getBlockX(), pos1.getBlockZ())) {
                    e.setCancelled(true);
                    MainUtil.sendMessage(pp, C.REQUIRE_SELECTION_IN_MASK, "Position 1");
                }
                if (!maskContains(mask, pos2.getBlockX(), pos2.getBlockZ())) {
                    e.setCancelled(true);
                    MainUtil.sendMessage(pp, C.REQUIRE_SELECTION_IN_MASK, "Position 2");
                }
            }
        }
    }
}
 
