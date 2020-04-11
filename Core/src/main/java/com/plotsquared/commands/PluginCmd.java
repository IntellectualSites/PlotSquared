package com.plotsquared.commands;

import com.plotsquared.PlotSquared;
import com.plotsquared.player.PlotPlayer;
import com.plotsquared.util.MainUtil;
import com.plotsquared.util.tasks.TaskManager;
import com.plotsquared.util.PremiumVerification;

@CommandDeclaration(command = "plugin",
    permission = "plots.use",
    description = "Show plugin information",
    usage = "/plot plugin",
    aliases = "version",
    category = CommandCategory.INFO)
public class PluginCmd extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        TaskManager.IMP.taskAsync(() -> {
            MainUtil.sendMessage(player, String.format(
                "$2>> $1&l" + PlotSquared.imp().getPluginName() + " $2($1Version$2: $1%s$2)",
                PlotSquared.get().getVersion()));
            MainUtil.sendMessage(player,
                "$2>> $1&lAuthors$2: $1Citymonstret $2& $1Empire92 $2& $1MattBDev $2& $1dordsor21 $2& $1NotMyFault");
            MainUtil.sendMessage(player,
                "$2>> $1&lWiki$2: $1https://github.com/IntellectualSites/PlotSquared/wiki");
            MainUtil.sendMessage(player, "$2>> $1&lPremium$2: $1" + PremiumVerification.isPremium());
        });
        return true;
    }
}
