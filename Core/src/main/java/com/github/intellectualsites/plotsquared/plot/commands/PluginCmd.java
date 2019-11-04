package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.json.JSONObject;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.HttpUtil;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;

@CommandDeclaration(command = "plugin", permission = "plots.use",
    description = "Show plugin information", usage = "/plot plugin", aliases = "version",
    category = CommandCategory.INFO) public class PluginCmd extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        TaskManager.IMP.taskAsync(new Runnable() {
            @Override public void run() {
                MainUtil.sendMessage(player, String.format(
                    "$2>> $1&l" + PlotSquared.imp().getPluginName() + " $2($1Version$2: $1%s$2)",
                    PlotSquared.get().getVersion()));
                MainUtil.sendMessage(player,
                    "$2>> $1&lAuthors$2: $1Citymonstret $2& $1Empire92 $2& $1MattBDev $2& $1dordsor21");
                MainUtil.sendMessage(player,
                    "$2>> $1&lWiki$2: $1https://github.com/IntellectualSites/PlotSquared/wiki");
                //                MainUtil.sendMessage(player,
                //                    "$2>> $1&lNewest Version$2: $1" + getNewestVersionString());
            }
        });
        return true;
    }

    public String getNewestVersionString() {
        String str = HttpUtil
            .readUrl("https://api.github.com/repos/IntellectualSites/PlotSquared/releases/latest");
        JSONObject release = new JSONObject(str);
        return release.getString("name");
    }
}
