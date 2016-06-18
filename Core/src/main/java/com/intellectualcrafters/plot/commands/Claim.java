package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Expression;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ByteArrayUtilities;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "claim",
        aliases = "c",
        description = "Claim the current plot you're standing on",
        category = CommandCategory.CLAIMING,
        requiredType = RequiredType.PLAYER,
        permission = "plots.claim", usage = "/plot claim")
public class Claim extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        String schematic = "";
        if (args.length >= 1) {
            schematic = args[0];
        }
        Location loc = player.getLocation();
        Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return sendMessage(player, C.NOT_IN_PLOT);
        }
        int currentPlots = Settings.Limit.GLOBAL ? player.getPlotCount() : player.getPlotCount(loc.getWorld());
        int grants = 0;
        if (currentPlots >= player.getAllowedPlots()) {
            if (player.hasPersistentMeta("grantedPlots")) {
                grants = ByteArrayUtilities.bytesToInteger(player.getPersistentMeta("grantedPlots"));
                if (grants <= 0) {
                    player.removePersistentMeta("grantedPlots");
                    return sendMessage(player, C.CANT_CLAIM_MORE_PLOTS);
                }
            } else {
                return sendMessage(player, C.CANT_CLAIM_MORE_PLOTS);
            }
        }
        if (!plot.canClaim(player)) {
            return sendMessage(player, C.PLOT_IS_CLAIMED);
        }
        PlotArea world = plot.getArea();
        if ((EconHandler.manager != null) && world.USE_ECONOMY) {
            Expression<Double> costExr = world.PRICES.get("claim");
            double cost = costExr.evalute((double) currentPlots);
            if (cost > 0d) {
                if (EconHandler.manager.getMoney(player) < cost) {
                    return sendMessage(player, C.CANNOT_AFFORD_PLOT, "" + cost);
                }
                EconHandler.manager.withdrawMoney(player, cost);
                sendMessage(player, C.REMOVED_BALANCE, cost + "");
            }
        }
        if (grants > 0) {
            if (grants == 1) {
                player.removePersistentMeta("grantedPlots");
            } else {
                player.setPersistentMeta("grantedPlots", ByteArrayUtilities.integerToBytes(grants - 1));
            }
            sendMessage(player, C.REMOVED_GRANTED_PLOT, "1", "" + (grants - 1));
        }
        if (!schematic.isEmpty()) {
            if (world.SCHEMATIC_CLAIM_SPECIFY) {
                if (!world.SCHEMATICS.contains(schematic.toLowerCase())) {
                    return sendMessage(player, C.SCHEMATIC_INVALID, "non-existent: " + schematic);
                }
                if (!Permissions.hasPermission(player, "plots.claim." + schematic) && !Permissions.hasPermission(player, "plots.admin.command.schematic")) {
                    return sendMessage(player, C.NO_SCHEMATIC_PERMISSION, schematic);
                }
            }
        }
        return plot.claim(player, false, schematic) || sendMessage(player, C.PLOT_NOT_CLAIMED);
    }
}
