package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.StringWrapper;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;

@CommandDeclaration(command = "setalias",
    permission = "plots.alias",
    description = "Set the plot name",
    usage = "/plot alias <set|remove> <alias>",
    aliases = {"alias", "sa", "name", "rename", "setname", "seta", "nameplot"},
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.PLAYER)
public class Alias extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {

        if (args.length == 0) {
            Captions.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }

        Location location = player.getLocation();
        Plot plot = location.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }

        if (!plot.hasOwner()) {
            sendMessage(player, Captions.PLOT_NOT_CLAIMED);
            return false;
        }

        if (!plot.isOwner(player.getUUID())) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return false;
        }

        boolean result = false;

        switch (args[0].toLowerCase()) {
            case "set":
                if (args.length != 2) {
                    Captions.COMMAND_SYNTAX.send(player, getUsage());
                    return false;
                }

                if (canExecuteCommand(player, Captions.PERMISSION_ALIAS_SET, false)
                    || canExecuteCommand(player, Captions.PERMISSION_ALIAS_SET_OBSOLETE, false)) {
                    result = setAlias(player, plot, args[1]);
                } else {
                    MainUtil.sendMessage(player, Captions.NO_PERMISSION);
                }

                break;
            case "remove":
                if (canExecuteCommand(player, Captions.PERMISSION_ALIAS_REMOVE, true)) {
                    result = removeAlias(player, plot);
                }
                break;
            default:
                Captions.COMMAND_SYNTAX.send(player, getUsage());
                result = false;
        }

        return result;
    }


    private boolean setAlias(PlotPlayer player, Plot plot, String alias) {
        if (alias.isEmpty()) {
            Captions.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }
        if (alias.length() >= 50) {
            MainUtil.sendMessage(player, Captions.ALIAS_TOO_LONG);
            return false;
        }
        if (alias.contains(" ")) {
            Captions.NOT_VALID_VALUE.send(player);
            return false;
        }
        if (MathMan.isInteger(alias)) {
            Captions.NOT_VALID_VALUE.send(player);
            return false;
        }
        for (Plot p : PlotSquared.get().getPlots(plot.getArea())) {
            if (p.getAlias().equalsIgnoreCase(alias)) {
                MainUtil.sendMessage(player, Captions.ALIAS_IS_TAKEN);
                return false;
            }
        }
        if (UUIDHandler.nameExists(new StringWrapper(alias)) || PlotSquared.get()
            .hasPlotArea(alias)) {
            MainUtil.sendMessage(player, Captions.ALIAS_IS_TAKEN);
            return false;
        }
        plot.setAlias(alias);
        MainUtil.sendMessage(player,
            Captions.ALIAS_SET_TO.getTranslated().replaceAll("%alias%", alias));
        return true;
    }

    private boolean removeAlias(PlotPlayer player, Plot plot) {
        plot.setAlias(null);
        MainUtil.sendMessage(player, Captions.ALIAS_REMOVED.getTranslated());
        return true;
    }

    private boolean canExecuteCommand(PlotPlayer player, Captions caption, boolean sendMessage) {
        if (!Permissions.hasPermission(player, caption)) {
            if (sendMessage) {
                MainUtil.sendMessage(player, Captions.NO_PERMISSION);
            }
            return false;
        }
        return true;
    }
}
