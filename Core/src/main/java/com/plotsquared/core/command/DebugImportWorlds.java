package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import com.plotsquared.core.util.uuid.UUIDHandler;
import com.plotsquared.core.util.WorldUtil;
import com.google.common.base.Charsets;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "debugimportworlds",
    permission = "plots.admin",
    description = "Import worlds by player name",
    requiredType = RequiredType.CONSOLE,
    category = CommandCategory.TELEPORT)
public class DebugImportWorlds extends Command {
    public DebugImportWorlds() {
        super(MainCommand.getInstance(), true);
    }

    @Override public CompletableFuture<Boolean> execute(PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        // UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8))
        PlotAreaManager pam = PlotSquared.get().getPlotAreaManager();
        if (!(pam instanceof SinglePlotAreaManager)) {
            player.sendMessage("Must be a single plot area!");
            return CompletableFuture.completedFuture(false);
        }
        SinglePlotArea area = ((SinglePlotAreaManager) pam).getArea();
        PlotId id = new PlotId(0, 0);
        File container = PlotSquared.imp().getWorldContainer();
        if (container.equals(new File("."))) {
            player.sendMessage(
                "World container must be configured to be a separate directory to your base files!");
            return CompletableFuture.completedFuture(false);
        }
        for (File folder : container.listFiles()) {
            String name = folder.getName();
            if (!WorldUtil.IMP.isWorld(name) && PlotId.fromStringOrNull(name) == null) {
                UUID uuid;
                if (name.length() > 16) {
                    uuid = UUID.fromString(name);
                } else {
                    uuid = UUIDHandler.getUUID(name, null);
                }
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
        return CompletableFuture.completedFuture(true);
    }
}
