package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ByteArrayUtilities;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "grant",
        category = CommandCategory.CLAIMING,
        usage = "/plot grant <check|add> [...]",
        permission = "plots.grant",
        requiredType = RequiredType.NONE
)
public class Grant extends SubCommand {

    void grantPlayer(PlotPlayer plr, String enteredName) {
        PlotPlayer player = UUIDHandler.getPlayer(enteredName);
        if (player == null) {
            sendMessage(plr, C.GRANTED_PLOT_FAILED, "Player not found");
        } else {
            int n = 1;
            if (player.hasPersistentMeta("grantedPlots")) {
                n += ByteArrayUtilities.bytesToInteger(player.getPersistentMeta("grantedPlots"));
            }
            player.setPersistentMeta("grantedPlots", ByteArrayUtilities.integerToBytes(n));
            sendMessage(plr, C.GRANTED_PLOT, enteredName);
        }
    }

    @Override
    public boolean onCommand(PlotPlayer plr, String[] arguments) {
        if (plr == null || plr instanceof ConsolePlayer) {
            if (arguments.length != 1) {
                MainUtil.sendMessage(null, "Usage: /plot grant <Player>");
            } else {
                grantPlayer(null, arguments[0]);
                return true;
            }
        } else {
            if (arguments.length < 1) {
                arguments = new String[] { "check" };
            }
            switch (arguments[0]) {
                case "check": {
                    int grantedPlots = 0;
                    if (plr.hasPersistentMeta("grantedPlots")) {
                        grantedPlots = ByteArrayUtilities.bytesToInteger(plr.getPersistentMeta("grantedPlots"));
                    }
                    return sendMessage(plr, C.GRANTED_PLOTS, "" + grantedPlots);
                }
                case "add": {
                    if (!plr.hasPermission("plots.grant.add")) {
                        return sendMessage(plr, C.NO_PERMISSION, "plots.grant.add");
                    }
                    if (arguments.length < 2) {
                        plr.sendMessage("&cUsage: /plot grant add <player>");
                    } else {
                        grantPlayer(plr, arguments[1]);
                    }
                } break;
                default: return onCommand(plr, new String[] { "check" });
            }
        }
        return true;
    }

}
