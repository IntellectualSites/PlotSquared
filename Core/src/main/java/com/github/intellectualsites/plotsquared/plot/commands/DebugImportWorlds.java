package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.object.worlds.PlotAreaManager;
import com.github.intellectualsites.plotsquared.plot.object.worlds.SinglePlotArea;
import com.github.intellectualsites.plotsquared.plot.object.worlds.SinglePlotAreaManager;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;
import com.google.common.base.Charsets;

import java.io.File;
import java.util.UUID;

@CommandDeclaration(command = "debugimportworlds", permission = "plots.admin",
    description = "Import worlds by player name", requiredType = RequiredType.CONSOLE,
    category = CommandCategory.TELEPORT) public class DebugImportWorlds extends Command {
    public DebugImportWorlds() {
        super(MainCommand.getInstance(), true);
    }

    @Override public void execute(PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        // UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8))
        PlotAreaManager pam = PlotSquared.get().getPlotAreaManager();
        if (!(pam instanceof SinglePlotAreaManager)) {
            player.sendMessage("Must be a single plot area!");
            return;
        }
        SinglePlotArea area = ((SinglePlotAreaManager) pam).getArea();
        PlotId id = new PlotId(0, 0);
        File container = PlotSquared.imp().getWorldContainer();
        if (container.equals(new File("."))) {
            player.sendMessage("World container must be configured to be a separate directory to your base files!");
            return;
        }
        for (File folder : container.listFiles()) {
            String name = folder.getName();
            if (!WorldUtil.IMP.isWorld(name) && PlotId.fromString(name) == null) {
                UUID uuid = UUIDHandler.getUUID(name, null);
                if (uuid == null) {
                    uuid =
                        UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
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
