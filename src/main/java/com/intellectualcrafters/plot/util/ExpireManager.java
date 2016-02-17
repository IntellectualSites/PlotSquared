package com.intellectualcrafters.plot.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;

public class ExpireManager {
    public static ConcurrentHashMap<String, List<Plot>> expiredPlots = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Boolean> updatingPlots = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Long> timestamp = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<UUID, Long> dates = new ConcurrentHashMap<>();
    public static int task;
    
    public static long getTimeStamp(PlotArea area) {
        Long timestamp = (Long) area.getMeta("expiredTimestamp");
        return timestamp != null ? timestamp : 0;
    }
    
    public static boolean updateExpired(final PlotArea area) {
        area.setMeta("updatingExpired", true);
        final long now = System.currentTimeMillis();
        if (now > getTimeStamp(area)) {
            area.setMeta("expiredTimestamp", now + 86400000l);
            TaskManager.runTaskAsync(new Runnable() {
                @Override
                public void run() {
                    try {
                        final List<Plot> plots = getOldPlots(area);
                        PS.debug("$2[&5Expire&dManager$2] $4Found " + plots.size() + " expired plots for " + area + "!");
                        area.setMeta("expiredPlots", plots);
                        area.setMeta("updatingExpired", false);
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            return true;
        } else {
            area.setMeta("updatingExpired", false);
            return false;
        }
    }
    
    public static void runTask() {
        ExpireManager.task = TaskManager.runTaskRepeatAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    for (final PlotArea area : PS.get().getPlotAreas()) {
                        Boolean updating = (Boolean) area.getMeta("updatingExpired");
                        if (updating == null) {
                            updating = false;
                            area.setMeta("updatingExpired", false);
                        }
                        if (updating) {
                            PS.debug("$2[&5Expire&dManager$2] $4Waiting on fetch...");
                            return;
                        }
                        List<Plot> plots = (List<Plot>) area.getMeta("expiredPlots");
                        if (plots == null) {
                            PS.debug("$2[&5Expire&dManager$2] $4Updating expired plots for: " + area);
                            updateExpired(area);
                            return;
                        }
                        if ((plots.isEmpty())) {
                            if (updateExpired(area)) {
                                PS.debug("$2[&5Expire&dManager$2] $4Re-evaluating expired plots for: " + area);
                                return;
                            }
                            continue;
                        }
                        final Iterator<Plot> iter = plots.iterator();
                        final Plot plot = iter.next();
                        if (!isExpired(plot)) {
                            iter.remove();
                            PS.debug("$2[&5Expire&dManager$2] &bSkipping no longer expired: " + plot);
                            return;
                        }
                        for (final UUID helper : plot.getTrusted()) {
                            final PlotPlayer player = UUIDHandler.getPlayer(helper);
                            if (player != null) {
                                MainUtil.sendMessage(player, C.PLOT_REMOVED_USER, plot.getId().toString());
                            }
                        }
                        for (final UUID helper : plot.getMembers()) {
                            final PlotPlayer player = UUIDHandler.getPlayer(helper);
                            if (player != null) {
                                MainUtil.sendMessage(player, C.PLOT_REMOVED_USER, plot.getId().toString());
                            }
                        }
                        final RunnableVal<PlotAnalysis> run = new RunnableVal<PlotAnalysis>() {
                            @Override
                            public void run(PlotAnalysis changed) {
                                if ((Settings.CLEAR_THRESHOLD != -1) && (area.TYPE == 0) && (changed != null)) {
                                    if ((changed.changes != 0) && (changed.getComplexity() > Settings.CLEAR_THRESHOLD)) {
                                        PS.debug("$2[&5Expire&dManager$2] &bIgnoring modified plot: " + plot + " : " + changed.getComplexity() + " - " + changed.changes);
                                        iter.remove();
                                        FlagManager.addPlotFlag(plot, new Flag(FlagManager.getFlag("analysis"), changed.asList()));
                                        return;
                                    }
                                }
                                if (plot.isMerged()) {
                                    plot.unlinkPlot(true, false);
                                }
                                plot.deletePlot(null);
                                iter.remove();
                                final int complexity = changed == null ? 0 : changed.getComplexity();
                                final int modified = changed == null ? 0 : changed.changes;
                                PS.debug("$2[&5Expire&dManager$2] &cDeleted expired plot: " + plot + " : " + complexity + " - " + modified);
                                PS.debug("$4 - Area: " + plot.getArea());
                                if (plot.hasOwner()) {
                                    PS.debug("$4 - Owner: " + UUIDHandler.getName(plot.owner));
                                } else {
                                    PS.debug("$4 - Owner: Unowned");
                                }
                            }
                        };
                        if (plot.getRunning() > 0) {
                            PS.debug("$2[&5Expire&dManager$2] &bSkipping plot in use: " + plot);
                            iter.remove();
                            run();
                            return;
                        }
                        if ((Settings.CLEAR_THRESHOLD != -1) && (area.TYPE == 0)) {
                            final PlotAnalysis analysis = plot.getComplexity();
                            if (analysis != null) {
                                if (analysis.getComplexity() > Settings.CLEAR_THRESHOLD) {
                                    PS.debug("$2[&5Expire&dManager$2] &bSkipping modified: " + plot);
                                    iter.remove();
                                    run();
                                    return;
                                }
                            }
                            HybridUtils.manager.analyzePlot(plot, run);
                        } else {
                            run.value = null;
                            run.run();
                        }
                        return;
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }, Settings.CLEAR_INTERVAL * 20);
    }
    
    public static boolean isExpired(final UUID uuid) {
        if (UUIDHandler.getPlayer(uuid) != null) {
            return false;
        }
        final String name = UUIDHandler.getName(uuid);
        if (name != null) {
            long last;
            if (dates.containsKey(uuid)) {
                last = dates.get(uuid);
            } else {
                OfflinePlotPlayer opp;
                if (Settings.TWIN_MODE_UUID) {
                    opp = UUIDHandler.getUUIDWrapper().getOfflinePlayer(uuid);
                } else {
                    opp = UUIDHandler.getUUIDWrapper().getOfflinePlayer(name);
                }
                last = opp.getLastPlayed();
                if (last != 0) {
                    dates.put(uuid, last);
                } else {
                    return false;
                }
            }
            if (last == 0) {
                return false;
            }
            final long compared = System.currentTimeMillis() - last;
            if (compared >= (86400000l * Settings.AUTO_CLEAR_DAYS)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isExpired(final Plot plot) {
        for (final UUID owner : plot.getOwners()) {
            if (!isExpired(owner)) {
                return false;
            }
        }
        return true;
    }
    
    public static List<Plot> getOldPlots(final PlotArea area) {
        final ArrayList<Plot> plots = new ArrayList<>(area.getPlots());
        final List<Plot> toRemove = new ArrayList<>();
        for (Plot plot : plots) {
            final Flag keepFlag = FlagManager.getPlotFlagRaw(plot, "keep");
            if ((keepFlag != null) && (Boolean) keepFlag.getValue()) {
                continue;
            }
            final UUID uuid = plot.owner;
            if (uuid == null) {
                toRemove.add(plot);
                continue;
            }
            final PlotPlayer player = UUIDHandler.getPlayer(uuid);
            if (player != null) {
                continue;
            }
            if (isExpired(plot)) {
                toRemove.add(plot);
            }
        }
        return toRemove;
    }
}
