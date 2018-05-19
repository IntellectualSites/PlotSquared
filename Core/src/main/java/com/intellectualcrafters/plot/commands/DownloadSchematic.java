package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.net.URL;

@CommandDeclaration(usage = "/plot download schematic",
        command = "schematic",
        aliases = {"sch","schem"},
        category = CommandCategory.SCHEMATIC,
        requiredType = RequiredType.NONE,
        description = "Download your plot schematic",
        permission = "plots.download")
public class DownloadSchematic extends DownloadCommand {

    public DownloadSchematic(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean download(final PlotPlayer player, final Plot plot, final String world) {
        if (plot.getVolume() > Integer.MAX_VALUE) {
            C.SCHEMATIC_TOO_LARGE.send(player);
            return false;
        }
        plot.addRunning();
        SchematicHandler.manager.getCompoundTag(plot, new RunnableVal<CompoundTag>() {
            @Override
            public void run(CompoundTag value) {
                plot.removeRunning();
                SchematicHandler.manager.upload(value, null, null, new RunnableVal<URL>() {
                    @Override
                    public void run(URL url) {
                        if (url == null) {
                            MainUtil.sendMessage(player, C.GENERATING_LINK_FAILED);
                            return;
                        }
                        MainUtil.sendMessage(player, url.toString());
                    }
                });
            }
        });
        return true;
    }
}
