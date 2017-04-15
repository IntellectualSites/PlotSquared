package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Expression;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.ByteArrayUtilities;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "auto",
        permission = "plots.auto",
        category = CommandCategory.CLAIMING,
        requiredType = RequiredType.NONE,
        description = "Claim the nearest plot",
        aliases = "a",
        usage = "/plot auto [length,width]")
public class Auto extends SubCommand {

    public static PlotId getNextPlotId(PlotId id, int step) {
        int absX = Math.abs(id.x);
        int absY = Math.abs(id.y);
        if (absX > absY) {
            if (id.x > 0) {
                return new PlotId(id.x, id.y + 1);
            } else {
                return new PlotId(id.x, id.y - 1);
            }
        } else if (absY > absX) {
            if (id.y > 0) {
                return new PlotId(id.x - 1, id.y);
            } else {
                return new PlotId(id.x + 1, id.y);
            }
        } else {
            if (id.x == id.y && id.x > 0) {
                return new PlotId(id.x, id.y + step);
            }
            if (id.x == absX) {
                return new PlotId(id.x, id.y + 1);
            }
            if (id.y == absY) {
                return new PlotId(id.x, id.y - 1);
            }
            return new PlotId(id.x + 1, id.y);
        }
    }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        PlotArea plotarea = player.getApplicablePlotArea();
        if (plotarea == null) {
            if (EconHandler.manager != null) {
                for (PlotArea area : PS.get().getPlotAreaManager().getAllPlotAreas()) {
                    if (EconHandler.manager.hasPermission(area.worldname, player.getName(), "plots.auto")) {
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
            MainUtil.sendMessage(player, C.CANT_CLAIM_MORE_PLOTS_NUM, Settings.Claim.MAX_AUTO_AREA + "");
            return false;
        }
        int currentPlots = Settings.Limit.GLOBAL ? player.getPlotCount() : player.getPlotCount(plotarea.worldname);
        int diff = currentPlots - player.getAllowedPlots();
        if (diff + size_x * size_z > 0) {
            if (diff < 0) {
                    MainUtil.sendMessage(player, C.CANT_CLAIM_MORE_PLOTS_NUM, -diff + "");
                return false;
            } else if (player.hasPersistentMeta("grantedPlots")) {
                int grantedPlots = ByteArrayUtilities.bytesToInteger(player.getPersistentMeta("grantedPlots"));
                if (grantedPlots - diff < size_x * size_z) {
                    player.removePersistentMeta("grantedPlots");
                    return sendMessage(player, C.CANT_CLAIM_MORE_PLOTS);
                } else {
                    int left = grantedPlots - diff - size_x * size_z;
                    if (left == 0) {
                        player.removePersistentMeta("grantedPlots");
                    } else {
                        player.setPersistentMeta("grantedPlots", ByteArrayUtilities.integerToBytes(left));
                    }
                    sendMessage(player, C.REMOVED_GRANTED_PLOT, "" + left, "" + (grantedPlots - left));
                }
            } else {
                MainUtil.sendMessage(player, C.CANT_CLAIM_MORE_PLOTS);
                return false;
            }
        }
        if (schematic != null && !schematic.isEmpty()) {
            if (!plotarea.SCHEMATICS.contains(schematic.toLowerCase())) {
                sendMessage(player, C.SCHEMATIC_INVALID, "non-existent: " + schematic);
                return true;
            }
            if (!Permissions.hasPermission(player, C.PERMISSION_CLAIM_SCHEMATIC.f(schematic)) && !Permissions.hasPermission(player, C.PERMISSION_ADMIN_COMMAND_SCHEMATIC)) {
                MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLAIM_SCHEMATIC.f(schematic));
                return true;
            }
        }
        if (EconHandler.manager != null && plotarea.USE_ECONOMY) {
            Expression<Double> costExp = plotarea.PRICES.get("claim");
            double cost = costExp.evaluate((double) currentPlots);
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
            final String finalSchematic = schematic;
            autoClaimSafe(player, plotarea, null, new RunnableVal<Plot>() {
                @Override
                public void run(Plot value) {
                    if (value == null) {
                        MainUtil.sendMessage(player, C.NO_FREE_PLOTS);
                    } else {
                        value.claim(player, true, finalSchematic, false);
                    }
                }
            });
            return true;
        } else {
            if (plotarea.TYPE == 2) {
                // TODO
                MainUtil.sendMessage(player, C.NO_FREE_PLOTS);
                return false;
            }
            while (true) {
                PlotId start = getNextPlotId(getLastPlotId(plotarea), 1);
                PlotId end = new PlotId(start.x + size_x - 1, start.y + size_z - 1);
                plotarea.setMeta("lastPlot", start);
                if (plotarea.canClaim(player, start, end)) {
                    for (int i = start.x; i <= end.x; i++) {
                        for (int j = start.y; j <= end.y; j++) {
                            Plot plot = plotarea.getPlotAbs(new PlotId(i, j));
                            boolean teleport = i == end.x && j == end.y;
                            plot.claim(player, teleport, null);
                        }
                    }
                    if (size_x != 1 || size_z != 1) {
                        if (!plotarea.mergePlots(MainUtil.getPlotSelectionIds(start, end), true, true)) {
                            return false;
                        }
                    }
                    break;
                }
            }
            return true;
        }
    }

    public void autoClaimSafe(final PlotPlayer player, final PlotArea area, PlotId start, final RunnableVal<Plot> whenDone) {
        if (area.TYPE == 2) {
            PlotId min = area.getMin();
            PlotId max = area.getMax();
            if (start == null) start = new PlotId(min.x, min.y);
            while (!area.canClaim(player, start, start)) {
                if (++start.x > max.x) {
                    start.x = min.x;
                    if (++start.y > max.y) {
                        whenDone.run(null);
                        return;
                    }
                }
                start.recalculateHash();
            }
        } else {
            if (start == null) start = getLastPlotId(area);
            while (!area.canClaim(player, start, start)) {
                start = getNextPlotId(start, 1);
            }
        }
        Plot plot = area.getPlotAbs(start);
        if (plot.canClaim(player)) {
            plot.owner = player.getUUID();
            final PlotId finalStart = getNextPlotId(start, 1);
            area.setMeta("lastPlot", finalStart);
            whenDone.value = plot;
            DBFunc.createPlotSafe(plot, whenDone, new Runnable() {
                @Override
                public void run() {
                    autoClaimSafe(player, area, finalStart, whenDone);
                }
            });
            return;
        }
        autoClaimSafe(player, area, start, whenDone);
    }

    public PlotId getLastPlotId(PlotArea area) {
        PlotId value = (PlotId) area.getMeta("lastPlot");
        if (value == null) {
            value = new PlotId(0, 0);
            area.setMeta("lastPlot", value);
            return value;
        }
        return value;
    }
}
