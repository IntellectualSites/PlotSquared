package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.net.URL;

@CommandDeclaration(usage = "/plot download world",
        command = "world",
        aliases = {"mcr","mca"},
        category = CommandCategory.SCHEMATIC,
        requiredType = RequiredType.NONE,
        description = "Download your plot world file",
        permission = "plots.download.world")
public class DownloadWorld extends DownloadCommand {

    public DownloadWorld(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean download(final PlotPlayer player, final Plot plot, final String world) {
        MainUtil.sendMessage(player, "&cNote: The `.mca` files are 512x512");
        plot.addRunning();
        WorldUtil.IMP.saveWorld(world);
        WorldUtil.IMP.upload(plot, null, null, new RunnableVal<URL>() {
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
