package com.github.intellectualsites.plotsquared.commands;

import com.github.intellectualsites.plotsquared.PlotSquared;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.config.CaptionUtility;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.database.DBFunc;
import com.github.intellectualsites.plotsquared.events.PlayerClaimPlotEvent;
import com.github.intellectualsites.plotsquared.events.PlotMergeEvent;
import com.github.intellectualsites.plotsquared.events.Result;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.util.EconHandler;
import com.github.intellectualsites.plotsquared.util.Permissions;
import com.github.intellectualsites.plotsquared.util.tasks.TaskManager;
import com.github.intellectualsites.plotsquared.util.tasks.RunnableVal;
import com.google.common.primitives.Ints;

@CommandDeclaration(command = "claim",
    aliases = "c",
    description = "Claim the current plot you're standing on",
    category = CommandCategory.CLAIMING,
    requiredType = RequiredType.PLAYER,
    permission = "plots.claim",
    usage = "/plot claim")
public class Claim extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        String schematic = null;
        if (args.length >= 1) {
            schematic = args[0];
        }
        Location location = player.getLocation();
        final Plot plot = location.getPlotAbs();
        if (plot == null) {
            return sendMessage(player, Captions.NOT_IN_PLOT);
        }
        PlayerClaimPlotEvent event = PlotSquared.get().getEventDispatcher().callClaim(player, plot, schematic);
        schematic = event.getSchematic();
        if (event.getEventResult() == Result.DENY) {
            sendMessage(player, Captions.EVENT_DENIED, "Claim");
            return true;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        int currentPlots = Settings.Limit.GLOBAL ?
            player.getPlotCount() :
            player.getPlotCount(location.getWorld());
        int grants = 0;
        if (currentPlots >= player.getAllowedPlots() && !force) {
            if (player.hasPersistentMeta("grantedPlots")) {
                grants = Ints.fromByteArray(player.getPersistentMeta("grantedPlots"));
                if (grants <= 0) {
                    player.removePersistentMeta("grantedPlots");
                    return sendMessage(player, Captions.CANT_CLAIM_MORE_PLOTS);
                }
            } else {
                return sendMessage(player, Captions.CANT_CLAIM_MORE_PLOTS);
            }
        }
        if (!plot.canClaim(player)) {
            return sendMessage(player, Captions.PLOT_IS_CLAIMED);
        }
        final PlotArea area = plot.getArea();
        if (schematic != null && !schematic.isEmpty()) {
            if (area.isSchematicClaimSpecify()) {
                if (!area.hasSchematic(schematic)) {
                    return sendMessage(player, Captions.SCHEMATIC_INVALID,
                        "non-existent: " + schematic);
                }
                if (!Permissions.hasPermission(player, CaptionUtility
                    .format(player, Captions.PERMISSION_CLAIM_SCHEMATIC.getTranslated(), schematic))
                    && !Permissions
                    .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_SCHEMATIC) && !force) {
                    return sendMessage(player, Captions.NO_SCHEMATIC_PERMISSION, schematic);
                }
            }
        }
        if ((EconHandler.manager != null) && area.useEconomy() && !force) {
            Expression<Double> costExr = area.getPrices().get("claim");
            double cost = costExr.evaluate((double) currentPlots);
            if (cost > 0d) {
                if (EconHandler.manager.getMoney(player) < cost) {
                    return sendMessage(player, Captions.CANNOT_AFFORD_PLOT, "" + cost);
                }
                EconHandler.manager.withdrawMoney(player, cost);
                sendMessage(player, Captions.REMOVED_BALANCE, cost + "");
            }
        }
        if (grants > 0) {
            if (grants == 1) {
                player.removePersistentMeta("grantedPlots");
            } else {
                player.setPersistentMeta("grantedPlots", Ints.toByteArray(grants - 1));
            }
            sendMessage(player, Captions.REMOVED_GRANTED_PLOT, "1", "" + (grants - 1));
        }
        int border = area.getBorder();
        if (border != Integer.MAX_VALUE && plot.getDistanceFromOrigin() > border && !force) {
            return !sendMessage(player, Captions.BORDER);
        }
        plot.owner = player.getUUID();
        final String finalSchematic = schematic;
        DBFunc.createPlotSafe(plot, () -> TaskManager.IMP.sync(new RunnableVal<Object>() {
            @Override public void run(Object value) {
                if (!plot.claim(player, true, finalSchematic)) {
                    PlotSquared.get().getLogger().log(Captions.PREFIX.getTranslated() +
                        String.format("Failed to claim plot %s", plot.getId().toCommaSeparatedString()));
                    sendMessage(player, Captions.PLOT_NOT_CLAIMED);
                    plot.owner = null;
                } else if (area.isAutoMerge()) {
                    PlotMergeEvent event = PlotSquared.get().getEventDispatcher()
                        .callMerge(plot, Direction.ALL, Integer.MAX_VALUE, player);
                    if (event.getEventResult() == Result.DENY) {
                        sendMessage(player, Captions.EVENT_DENIED, "Auto merge on claim");
                    } else {
                        plot.autoMerge(event.getDir(), event.getMax(), player.getUUID(), true);
                    }
                }
            }
        }), () -> {
            PlotSquared.get().getLogger().log(Captions.PREFIX.getTranslated() +
                String.format("Failed to add plot %s to the database", plot.getId().toCommaSeparatedString()));
            sendMessage(player, Captions.PLOT_NOT_CLAIMED);
            plot.owner = null;
        });
        return true;
    }
}
