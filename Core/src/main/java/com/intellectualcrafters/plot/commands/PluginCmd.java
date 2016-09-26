package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.json.JSONObject;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.HttpUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "plugin",
        permission = "plots.use",
        description = "Show plugin information",
        aliases = "version",
        category = CommandCategory.INFO)
public class PluginCmd extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        MainUtil.sendMessage(player, String.format("$2>> $1&l" + PS.imp().getPluginName() + " $2($1Version$2: $1%s$2)", StringMan.join(PS.get().getVersion(), ".")));
        MainUtil.sendMessage(player, "$2>> $1&lAuthors$2: $1Citymonstret $2& $1Empire92 $2& $1MattBDev");
        MainUtil.sendMessage(player, "$2>> $1&lWiki$2: $1https://github.com/IntellectualCrafters/PlotSquared/wiki");
        MainUtil.sendMessage(player, "$2>> $1&lNewest Version$2: $1" + getNewestVersionString());
        return true;
    }

    public String getNewestVersionString() {
        String str = HttpUtil.readUrl("https://api.github.com/repos/IntellectualSites/PlotSquared/releases/latest");
        JSONObject release = new JSONObject(str);
        return release.getString("name");
    }
}
