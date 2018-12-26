package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.*;

import javax.annotation.Nullable;
import java.util.Set;

@CommandDeclaration(command = "auto", permission = "plots.auto",
    category = CommandCategory.CLAIMING, requiredType = RequiredType.NONE,
    description = "Claim the nearest plot", aliases = "a", usage = "/plot auto [length,width]")
public class Auto extends SubCommand {

    @Deprecated public static PlotId getNextPlotId(PlotId id, int step) {
        return id.getNextId(step);
    }

    private static boolean checkAllowedPlots(PlotPlayer player, PlotArea plotarea,
        @Nullable Integer allowed_plots, int size_x, int size_z) {
        if (allowed_plots == null)
            allowed_plots = player.getAllowedPlots();
        int currentPlots =
            Settings.Limit.GLOBAL ? player.getPlotCount() : player.getPlotCount(plotarea.worldname);
        int diff = currentPlots - allowed_plots;
        if (diff + size_x * size_z > 0) {
            if (diff < 0) {
                MainUtil.sendMessage(player, C.CANT_CLAIM_MORE_PLOTS_NUM, -diff + "");
                return false;
            } else if (player.hasPersistentMeta("grantedPlots")) {
                int grantedPlots =
                    ByteArrayUtilities.bytesToInteger(player.getPersistentMeta("grantedPlots"));
                if (grantedPlots - diff < size_x * size_z) {
                    player.removePersistentMeta("grantedPlots");
                    MainUtil.sendMessage(player, C.CANT_CLAIM_MORE_PLOTS);
                    return false;
                } else {
                    int left = grantedPlots - diff - size_x * size_z;
                    if (left == 0) {
                        player.removePersistentMeta("grantedPlots");
                    } else {
                        player.setPersistentMeta("grantedPlots",
                            ByteArrayUtilities.integerToBytes(left));
                    }
                    MainUtil.sendMessage(player, C.REMOVED_GRANTED_PLOT, "" + left,
                        "" + (grantedPlots - left));
                }
            } else {
                MainUtil.sendMessage(player, C.CANT_CLAIM_MORE_PLOTS);
                return false;
            }
        }
        return true;
    }

    /**
     * Teleport the player home, or claim a new plot
     *
     * @param player
     * @param area
     * @param start
     * @param schem
     */
    public static void homeOrAuto(final PlotPlayer player, final PlotArea area, PlotId start,
        final String schem) {
        Set<Plot> plots = player.getPlots();
        if (!plots.isEmpty()) {
            plots.iterator().next().teleportPlayer(player);
        } else {
            autoClaimSafe(player, area, start, schem);
        }
    }

    /**
     * Claim a new plot for a player
     *
     * @param player
     * @param area
     * @param start
     * @param schem
     */
    public static void autoClaimSafe(final PlotPlayer player, final PlotArea area, PlotId start,
        final String schem) {
        autoClaimSafe(player, area, start, schem, null);
    }

    /**
     * Claim a new plot for a player
     *
     * @param player
     * @param area
     * @param start
     * @param schem
     */
    public static void autoClaimSafe(final PlotPlayer player, final PlotArea area, PlotId start,
        final String schem, @Nullable final Integer allowed_plots) {
        player.setMeta(Auto.class.getName(), true);
        autoClaimFromDatabase(player, area, start, new RunnableVal<Plot>() {
            @Override public void run(final Plot plot) {
                TaskManager.IMP.sync(new RunnableVal<Object>() {
                    @Override public void run(Object ignore) {
                        player.deleteMeta(Auto.class.getName());
                        if (plot == null) {
                            MainUtil.sendMessage(player, C.NO_FREE_PLOTS);
                        } else if (checkAllowedPlots(player, area, allowed_plots, 1, 1)) {
                            plot.claim(player, true, schem, false);
                            if (area.AUTO_MERGE) {
                                plot.autoMerge(-1, Integer.MAX_VALUE, player.getUUID(), true);
                            }
                        } else {
                            DBFunc.delete(plot);
                        }
                    }
                });
            }
        });
    }

    public static void autoClaimFromDatabase(final PlotPlayer player, final PlotArea area,
        PlotId start, final RunnableVal<Plot> whenDone) {
        final Plot plot = area.getNextFreePlot(player, start);
        if (plot == null) {
            whenDone.run(null);
            return;
        }
        whenDone.value = plot;
        plot.owner = player.getUUID();
        DBFunc.createPlotSafe(plot, whenDone, new Runnable() {
            @Override public void run() {
                autoClaimFromDatabase(player, area, plot.getId(), whenDone);
            }
        });
    }

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        PlotArea plotarea = player.getApplicablePlotArea();
        if (plotarea == null) {
            if (EconHandler.manager != null) {
                for (PlotArea area : PlotSquared.get().getPlotAreaManager().getAllPlotAreas()) {
                    if (EconHandler.manager
                        .hasPermission(area.worldname, player.getName(), "plots.auto")) {
                        if (plotarea != null) {
                            plotarea = null;
                            break;
                        }
                        plotarea = area;
                    }
                }
            }
            if (plotarea == null) {
                MainUtil.sendMessage(player, C.NOT_IN_PLOT_WORLD);
                return false;
            }
        }
        int size_x = 1;
        int size_z = 1;
        String schematic = null;
        if (args.length > 0) {
            if (Permissions.hasPermission(player, C.PERMISSION_AUTO_MEGA)) {
                try {
                    String[] split = args[0].split(",|;");
                    size_x = Integer.parseInt(split[0]);
                    size_z = Integer.parseInt(split[1]);
                    if (size_x < 1 || size_z < 1) {
                        MainUtil.sendMessage(player, "&cError: size<=0");
                    }
                    if (args.length > 1) {
                        schematic = args[1];
                    }
                } catch (NumberFormatException ignored) {
                    size_x = 1;
                    size_z = 1;
                    schematic = args[0];
                    // PlayerFunctions.sendMessage(plr,
                    // "&cError: Invalid size (X,Y)");
                    // return false;
                }
            } else {
                schematic = args[0];
                // PlayerFunctions.sendMessage(plr, C.NO_PERMISSION);
                // return false;
            }
        }
        if (size_x * size_z > Settings.Claim.MAX_AUTO_AREA) {
            MainUtil.sendMessage(player, C.CANT_CLAIM_MORE_PLOTS_NUM,
                Settings.Claim.MAX_AUTO_AREA + "");
            return false;
        }
        final int allowed_plots = player.getAllowedPlots();
        if (player.getMeta(Auto.class.getName(), false) || !checkAllowedPlots(player, plotarea,
            allowed_plots, size_x, size_z))
            return false;

        if (schematic != null && !schematic.isEmpty()) {
            if (!plotarea.SCHEMATICS.contains(schematic.toLowerCase())) {
                sendMessage(player, C.SCHEMATIC_INVALID, "non-existent: " + schematic);
                return true;
            }
            if (!Permissions.hasPermission(player, C.PERMISSION_CLAIM_SCHEMATIC.f(schematic))
                && !Permissions.hasPermission(player, C.PERMISSION_ADMIN_COMMAND_SCHEMATIC)) {
                MainUtil.sendMessage(player, C.NO_PERMISSION,
                    C.PERMISSION_CLAIM_SCHEMATIC.f(schematic));
                return true;
            }
        }
        if (EconHandler.manager != null && plotarea.USE_ECONOMY) {
            Expression<Double> costExp = plotarea.PRICES.get("claim");
            double cost = costExp.evaluate((double) (Settings.Limit.GLOBAL ?
                player.getPlotCount() :
                player.getPlotCount(plotarea.worldname)));
            cost = (size_x * size_z) * cost;
            if (cost > 0d) {
                if (EconHandler.manager.getMoney(player) < cost) {
                    sendMessage(player, C.CANNOT_AFFORD_PLOT, "" + cost);
                    return true;
                }
                EconHandler.manager.withdrawMoney(player, cost);
                sendMessage(player, C.REMOVED_BALANCE, cost + "");
            }
        }
        // TODO handle type 2 the same as normal worlds!
        if (size_x == 1 && size_z == 1) {
            autoClaimSafe(player, plotarea, null, schematic, allowed_plots);
            return true;
        } else {
            if (plotarea.TYPE == 2) {
                MainUtil.sendMessage(player, C.NO_FREE_PLOTS);
                return false;
            }
            while (true) {
                PlotId start = plotarea.getMeta("lastPlot", new PlotId(0, 0)).getNextId(1);
                PlotId end = new PlotId(start.x + size_x - 1, start.y + size_z - 1);
                if (plotarea.canClaim(player, start, end)) {
                    plotarea.setMeta("lastPlot", start);
                    for (int i = start.x; i <= end.x; i++) {
                        for (int j = start.y; j <= end.y; j++) {
                            Plot plot = plotarea.getPlotAbs(new PlotId(i, j));
                            boolean teleport = i == end.x && j == end.y;
                            plot.claim(player, teleport, null);
                        }
                    }
                    if (!plotarea
                        .mergePlots(MainUtil.getPlotSelectionIds(start, end), true, true)) {
                        return false;
                    }
                    break;
                }
                plotarea.setMeta("lastPlot", start);
            }
            return true;
        }
    }
}
