package com.intellectualcrafters.plot.util;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.commands.Unlink;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.events.PlotDeleteEvent;
import com.intellectualcrafters.plot.object.BukkitOfflinePlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class ExpireManager {
    public static ConcurrentHashMap<String, HashMap<Plot, Long>> expiredPlots = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Boolean> updatingPlots = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Long> timestamp = new ConcurrentHashMap<>();
    public static int task;

    public static long getTimeStamp(final String world) {
        if (timestamp.containsKey(world)) {
            return timestamp.get(world);
        } else {
            timestamp.put(world, 0l);
            return 0;
        }
    }

    public static boolean updateExpired(final String world) {
        updatingPlots.put(world, true);
        final long now = System.currentTimeMillis();
        if (now > getTimeStamp(world)) {
            timestamp.put(world, now + 86400000l);
            TaskManager.runTaskAsync(new Runnable() {
                @Override
                public void run() {
                    final HashMap<Plot, Long> plots = getOldPlots(world);
                    PlotSquared.log("&cFound " + plots.size() + " expired plots for " + world + "!");
                    expiredPlots.put(world, plots);
                    updatingPlots.put(world, false);
                }
            });
            return true;
        } else {
            updatingPlots.put(world, false);
            return false;
        }
    }

    public static void runTask() {
        ExpireManager.task = Bukkit.getScheduler().scheduleSyncRepeatingTask(BukkitMain.THIS, new Runnable() {
            @Override
            public void run() {
                for (final String world : PlotSquared.getPlotWorldsString()) {
                    if (!ExpireManager.updatingPlots.containsKey(world)) {
                        ExpireManager.updatingPlots.put(world, false);
                    }
                    final Boolean updating = ExpireManager.updatingPlots.get(world);
                    if (updating) {
                        return;
                    }
                    if (!expiredPlots.containsKey(world)) {
                        updateExpired(world);
                        return;
                    }
                    final Set<Plot> plots = expiredPlots.get(world).keySet();
                    if ((plots == null) || (plots.size() == 0)) {
                        if (updateExpired(world)) {
                            return;
                        }
                        continue;
                    }
                    final Plot plot = plots.iterator().next();
                    if (plot.owner != null) {
                        if (UUIDHandler.getPlayer(plot.owner) != null) {
                            expiredPlots.get(world).remove(plot);
                            return;
                        }
                    }
                    if (!isExpired(plot.owner)) {
                        expiredPlots.get(world).remove(plot);
                        return;
                    }
                    final PlotDeleteEvent event = new PlotDeleteEvent(world, plot.id);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        event.setCancelled(true);
                        return;
                    }
                    for (final UUID helper : plot.helpers) {
                        final PlotPlayer player = UUIDHandler.getPlayer(helper);
                        if (player != null) {
                            MainUtil.sendMessage(player, C.PLOT_REMOVED_HELPER, plot.id.toString());
                        }
                    }
                    final PlotManager manager = PlotSquared.getPlotManager(world);
                    if (plot.settings.isMerged()) {
                        Unlink.unlinkPlot(plot);
                    }
                    final PlotWorld plotworld = PlotSquared.getPlotWorld(world);
                    manager.clearPlot(plotworld, plot, false, null);
                    MainUtil.removeSign(plot);
                    DBFunc.delete(world, plot);
                    PlotSquared.removePlot(world, plot.id, true);
                    expiredPlots.get(world).remove(plot);
                    PlotSquared.log("&cDeleted expired plot: " + plot.id);
                    PlotSquared.log("&3 - World: " + plot.world);
                    if (plot.hasOwner()) {
                        PlotSquared.log("&3 - Owner: " + UUIDHandler.getName(plot.owner));
                    } else {
                        PlotSquared.log("&3 - Owner: Unowned");
                    }
                    return;
                }
            }
        }, 2400, 2400);
    }

    public static boolean isExpired(final UUID uuid) {
        final String name = UUIDHandler.getName(uuid);
        if (name != null) {
            final OfflinePlayer op = Bukkit.getOfflinePlayer(name);
            if (op.hasPlayedBefore()) {
                final long last = op.getLastPlayed();
                final long compared = System.currentTimeMillis() - last;
                if (compared >= (86400000l * Settings.AUTO_CLEAR_DAYS)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static HashMap<Plot, Long> getOldPlots(final String world) {
        final Collection<Plot> plots = PlotSquared.getPlots(world).values();
        final HashMap<Plot, Long> toRemove = new HashMap<>();
        final HashMap<UUID, Long> remove = new HashMap<>();
        final Set<UUID> keep = new HashSet<>();
        for (final Plot plot : plots) {
            final UUID uuid = plot.owner;
            if ((uuid == null) || remove.containsKey(uuid)) {
                Long stamp;
                if (uuid == null) {
                    stamp = 0l;
                } else {
                    stamp = remove.get(uuid);
                }
                toRemove.put(plot, stamp);
                continue;
            }
            if (keep.contains(uuid)) {
                continue;
            }
            final PlotPlayer player = UUIDHandler.getPlayer(uuid);
            if (player != null) {
                keep.add(uuid);
                continue;
            }
            final BukkitOfflinePlayer op = UUIDHandler.uuidWrapper.getOfflinePlayer(uuid);
            if ((op == null) || (op.getLastPlayed() == 0)) {
                continue;
            }
            long last = op.getLastPlayed();
            long compared = System.currentTimeMillis() - last;
            if (compared >= (86400000l * Settings.AUTO_CLEAR_DAYS)) {
                if (Settings.AUTO_CLEAR_CHECK_DISK) {
                    final String worldname = Bukkit.getWorlds().get(0).getName();
                    String foldername;
                    String filename = null;
                    if (BukkitMain.checkVersion(1, 7, 5)) {
                        foldername = "playerdata";
                        try {
                            filename = op.getUUID() + ".dat";
                        } catch (final Throwable e) {
                            filename = uuid.toString() + ".dat";
                        }
                    } else {
                        foldername = "players";
                        final String playername = UUIDHandler.getName(uuid);
                        if (playername != null) {
                            filename = playername + ".dat";
                        }
                    }
                    if (filename != null) {
                        final File playerFile = new File(worldname + File.separator + foldername + File.separator + filename);
                        if (!playerFile.exists()) {
                            PlotSquared.log("Could not find file: " + filename);
                        } else {
                            try {
                                last = playerFile.lastModified();
                                compared = System.currentTimeMillis() - last;
                                if (compared < (86400000l * Settings.AUTO_CLEAR_DAYS)) {
                                    keep.add(uuid);
                                    continue;
                                }
                            } catch (final Exception e) {
                                PlotSquared.log("Please disable disk checking in old plot auto clearing; Could not read file: " + filename);
                            }
                        }
                    }
                }
                toRemove.put(plot, last);
                remove.put(uuid, last);
                continue;
            }
            keep.add(uuid);
        }
        return toRemove;
    }
}
