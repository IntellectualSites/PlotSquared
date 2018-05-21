package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "set",
        permission = "plots.alias.set",
        description = "Set the plot name",
        usage = "/plot alias set <alias>",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE)
public class AliasSet extends SubCommand {

    public AliasSet(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {

        if (args.length != 1) {
            C.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }

        Location loc = player.getLocation();
        Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }

        if (!plot.hasOwner()) {
            sendMessage(player, C.PLOT_NOT_CLAIMED);
            return false;
        }

        if (!plot.isOwner(player.getUUID())) {
            MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
            return false;
        }

        String alias = args[0];
        if (alias.isEmpty() || alias == " ") {
            MainUtil.sendMessage(player, C.MISSING_ALIAS);
            return false;
        }
        if (alias.length() >= 50) {
            MainUtil.sendMessage(player, C.ALIAS_TOO_LONG);
            return false;
        }

        for (Plot p : PS.get().getPlots(plot.getArea())) {
            if (p.getAlias().equalsIgnoreCase(alias)) {
                MainUtil.sendMessage(player, C.ALIAS_IS_TAKEN);
                return false;
            }
        }
        if (UUIDHandler.nameExists(new StringWrapper(alias)) || PS.get().hasPlotArea(alias)) {
            MainUtil.sendMessage(player, C.ALIAS_IS_TAKEN);
            return false;
        }
        plot.setAlias(alias);
        MainUtil.sendMessage(player, C.ALIAS_SET_TO.s().replaceAll("%alias%", alias));

        return true;
    }
}
