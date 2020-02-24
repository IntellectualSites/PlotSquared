package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.CaptionUtility;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.Direction;
import com.github.intellectualsites.plotsquared.plot.object.Expression;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.object.TeleportCause;
import com.github.intellectualsites.plotsquared.plot.util.EconHandler;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.google.common.primitives.Ints;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@CommandDeclaration(command = "auto",
    permission = "plots.auto",
    category = CommandCategory.CLAIMING,
    requiredType = RequiredType.NONE,
    description = "Claim the nearest plot",
    aliases = "a",
    usage = "/plot auto [length,width]")
public class Auto extends SubCommand {

    @Deprecated public static PlotId getNextPlotId(PlotId id, int step) {
        return id.getNextId(step);
    }

    private static boolean checkAllowedPlots(PlotPlayer player, PlotArea plotarea,
        @Nullable Integer allowedPlots, int sizeX, int sizeZ) {
        if (allowedPlots == null) {
            allowedPlots = player.getAllowedPlots();
        }
        int currentPlots;
        if (Settings.Limit.GLOBAL) {
            currentPlots = player.getPlotCount();
        } else {
            currentPlots = player.getPlotCount(plotarea.worldname);
        }
        int diff = currentPlots - allowedPlots;
        if (diff + sizeX * sizeZ > 0) {
            if (diff < 0) {
                MainUtil.sendMessage(player, Captions.CANT_CLAIM_MORE_PLOTS_NUM, -diff + "");
                return false;
            } else if (player.hasPersistentMeta("grantedPlots")) {
                int grantedPlots = Ints.fromByteArray(player.getPersistentMeta("grantedPlots"));
                if (grantedPlots - diff < sizeX * sizeZ) {
                    player.removePersistentMeta("grantedPlots");
                    MainUtil.sendMessage(player, Captions.CANT_CLAIM_MORE_PLOTS);
                    return false;
                } else {
                    int left = grantedPlots - diff - sizeX * sizeZ;
                    if (left == 0) {
                        player.removePersistentMeta("grantedPlots");
                    } else {
                        player.setPersistentMeta("grantedPlots", Ints.toByteArray(left));
                    }
                    MainUtil.sendMessage(player, Captions.REMOVED_GRANTED_PLOT, "" + left,
                        "" + (grantedPlots - left));
                }
            } else {
                MainUtil.sendMessage(player, Captions.CANT_CLAIM_MORE_PLOTS);
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
     * @param schematic
     */
    public static void homeOrAuto(final PlotPlayer player, final PlotArea area, PlotId start,
        final String schematic) {
        Set<Plot> plots = player.getPlots();
        if (!plots.isEmpty()) {
            plots.iterator().next().teleportPlayer(player, TeleportCause.COMMAND);
        } else {
            autoClaimSafe(player, area, start, schematic);
        }
    }

    /**
     * Claim a new plot for a player
     *
     * @param player
     * @param area
     * @param start
     * @param schematic
     */
    public static void autoClaimSafe(final PlotPlayer player, final PlotArea area, PlotId start,
        final String schematic) {
        autoClaimSafe(player, area, start, schematic, null);
    }

    /**
     * Claim a new plot for a player
     *
     * @param player
     * @param area
     * @param start
     * @param schematic
     */
    public static void autoClaimSafe(final PlotPlayer player, final PlotArea area, PlotId start,
        final String schematic, @Nullable final Integer allowedPlots) {
        player.setMeta(Auto.class.getName(), true);
        autoClaimFromDatabase(player, area, start, new RunnableVal<Plot>() {
            @Override public void run(final Plot plot) {
                TaskManager.IMP.sync(new RunnableVal<Object>() {
                    @Override public void run(Object ignore) {
                        player.deleteMeta(Auto.class.getName());
                        if (plot == null) {
                            MainUtil.sendMessage(player, Captions.NO_FREE_PLOTS);
                        } else if (checkAllowedPlots(player, area, allowedPlots, 1, 1)) {
                            plot.claim(player, true, schematic, false);
                            if (area.AUTO_MERGE) {
                                plot.autoMerge(Direction.ALL, Integer.MAX_VALUE, player.getUUID(),
                                    true);
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
        DBFunc.createPlotSafe(plot, whenDone,
            () -> autoClaimFromDatabase(player, area, plot.getId(), whenDone));
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
                MainUtil.sendMessage(player, Captions.NOT_IN_PLOT_WORLD);
                return false;
            }
        }
        int size_x = 1;
        int size_z = 1;
        String schematic = null;
        if (args.length > 0) {
            if (Permissions.hasPermission(player, Captions.PERMISSION_AUTO_MEGA)) {
                try {
                    String[] split = args[0].split(",|;");
                    if (split[1] == null) {
                        MainUtil.sendMessage(player, "Correct use /plot auto [length,width]");
                        size_x = 1;
                        size_z = 1;
                    } else {
                        size_x = Integer.parseInt(split[0]);
                        size_z = Integer.parseInt(split[1]);
                    }
                    if (size_x < 1 || size_z < 1) {
                        MainUtil.sendMessage(player, "Error: size<=0");
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
                // PlayerFunctions.sendMessage(plr, Captions.NO_PERMISSION);
                // return false;
            }
        }
        if (size_x * size_z > Settings.Claim.MAX_AUTO_AREA) {
            MainUtil.sendMessage(player, Captions.CANT_CLAIM_MORE_PLOTS_NUM,
                Settings.Claim.MAX_AUTO_AREA + "");
            return false;
        }
        final int allowed_plots = player.getAllowedPlots();
        if (player.getMeta(Auto.class.getName(), false) || !checkAllowedPlots(player, plotarea,
            allowed_plots, size_x, size_z)) {
            return false;
        }

        if (schematic != null && !schematic.isEmpty()) {
            if (!plotarea.SCHEMATICS.contains(schematic.toLowerCase())) {
                sendMessage(player, Captions.SCHEMATIC_INVALID, "non-existent: " + schematic);
                return true;
            }
            if (!Permissions.hasPermission(player, CaptionUtility
                .format(player, Captions.PERMISSION_CLAIM_SCHEMATIC.getTranslated(), schematic))
                && !Permissions
                .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_SCHEMATIC)) {
                MainUtil.sendMessage(player, Captions.NO_PERMISSION, CaptionUtility
                    .format(player, Captions.PERMISSION_CLAIM_SCHEMATIC.getTranslated(),
                        schematic));
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
                    sendMessage(player, Captions.CANNOT_AFFORD_PLOT, "" + cost);
                    return true;
                }
                EconHandler.manager.withdrawMoney(player, cost);
                sendMessage(player, Captions.REMOVED_BALANCE, cost + "");
            }
        }
        // TODO handle type 2 the same as normal worlds!
        if (size_x == 1 && size_z == 1) {
            autoClaimSafe(player, plotarea, null, schematic, allowed_plots);
            return true;
        } else {
            if (plotarea.TYPE == 2) {
                MainUtil.sendMessage(player, Captions.NO_FREE_PLOTS);
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
                            if (plot == null) {
                                return false;
                            }
                            plot.claim(player, teleport, null);
                        }
                    }
                    if (!plotarea.mergePlots(MainUtil.getPlotSelectionIds(start, end), true)) {
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
