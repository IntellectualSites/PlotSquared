package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.BO3Handler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.net.URL;

@CommandDeclaration(usage = "/plot download bo3",
        command = "bo3",
        aliases = {"bo2","b02","b03"},
        category = CommandCategory.SCHEMATIC,
        requiredType = RequiredType.NONE,
        description = "Download your plot BO3",
        permission = "plots.download.bo3")
public class DownloadBO3 extends DownloadCommand {

    public DownloadBO3(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean download(final PlotPlayer player, final Plot plot, final String world) {
        if (plot.getVolume() > 128d * 128d * 256) {
            C.SCHEMATIC_TOO_LARGE.send(player);
            return false;
        }
        plot.addRunning();
        BO3Handler.upload(plot, null, null, new RunnableVal<URL>() {
            @Override
            public void run(URL url) {
                plot.removeRunning();
                if (url == null) {
                    MainUtil.sendMessage(player, C.GENERATING_LINK_FAILED);
                    return;
                }
                MainUtil.sendMessage(player, url.toString());
            }
        });
        return true;
    }
}
