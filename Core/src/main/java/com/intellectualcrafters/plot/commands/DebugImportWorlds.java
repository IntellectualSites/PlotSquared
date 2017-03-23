package com.intellectualcrafters.plot.commands;

import com.google.common.base.Charsets;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal2;
import com.intellectualcrafters.plot.object.RunnableVal3;
import com.intellectualcrafters.plot.object.worlds.PlotAreaManager;
import com.intellectualcrafters.plot.object.worlds.SinglePlotArea;
import com.intellectualcrafters.plot.object.worlds.SinglePlotAreaManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;
import java.io.File;
import java.util.UUID;
import java.util.zip.DeflaterOutputStream;

@CommandDeclaration(
        command = "debugimportworlds",
        permission = "plots.admin",
        description = "Import worlds by player name",
        requiredType = RequiredType.CONSOLE,
        category = CommandCategory.TELEPORT)
public class DebugImportWorlds extends Command {
    public DebugImportWorlds() {
        super(MainCommand.getInstance(), true);
    }

    @Override
    public void execute(PlotPlayer player, String[] args, RunnableVal3<Command, Runnable, Runnable> confirm, RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        // UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8))
        PlotAreaManager pam = PS.get().getPlotAreaManager();
        if (!(pam instanceof SinglePlotAreaManager)) {
            player.sendMessage("Must be a single plot area!");
            return;
        }
        SinglePlotArea area = ((SinglePlotAreaManager) pam).getArea();
        PlotId id = new PlotId(0, 0);
        File container = PS.imp().getWorldContainer();
        for (File folder : container.listFiles()) {
            String name = folder.getName();
            if (!WorldUtil.IMP.isWorld(name) && PlotId.fromString(name) == null) {
                UUID uuid = UUIDHandler.getUUID(name, null);
                if (uuid == null) {
                    uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
                }
                while (new File(container, id.toCommaSeparatedString()).exists()) {
                    id = Auto.getNextPlotId(id, 1);
                }
                File newDir = new File(container, id.toCommaSeparatedString());
                if (folder.renameTo(newDir)) {
                    area.getPlot(id).setOwner(uuid);
                }
            }
        }
        player.sendMessage("Done!");
    }
}
