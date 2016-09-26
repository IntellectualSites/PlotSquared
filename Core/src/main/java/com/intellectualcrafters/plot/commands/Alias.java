package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "setalias",
        permission = "plots.set.alias",
        description = "Set the plot name",
        usage = "/plot alias <alias>",
        aliases = {"alias", "sa", "name", "rename", "setname", "seta", "nameplot"},
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE)
public class Alias extends SetCommand {

    @Override
    public boolean set(PlotPlayer player, Plot plot, String alias) {
        if (alias.isEmpty()) {
            C.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }
        if (alias.length() >= 50) {
            MainUtil.sendMessage(player, C.ALIAS_TOO_LONG);
            return false;
        }
        if (alias.contains(" ") || !StringMan.isAsciiPrintable(alias)) {
            C.NOT_VALID_VALUE.send(player);
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
