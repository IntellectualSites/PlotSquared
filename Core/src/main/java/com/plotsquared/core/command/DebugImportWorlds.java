/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "debugimportworlds",
        permission = "plots.admin",
        requiredType = RequiredType.CONSOLE,
        category = CommandCategory.TELEPORT)
public class DebugImportWorlds extends Command {

    private final PlotAreaManager plotAreaManager;
    private final WorldUtil worldUtil;

    @Inject
    public DebugImportWorlds(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull WorldUtil worldUtil
    ) {
        super(MainCommand.getInstance(), true);
        this.plotAreaManager = plotAreaManager;
        this.worldUtil = worldUtil;
    }

    @Override
    public CompletableFuture<Boolean> execute(
            PlotPlayer<?> player, String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) throws CommandException {
        // UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8))
        if (!(this.plotAreaManager instanceof SinglePlotAreaManager)) {
            player.sendMessage(TranslatableCaption.of("debugimportworlds.single_plot_area"));
            return CompletableFuture.completedFuture(false);
        }
        SinglePlotArea area = ((SinglePlotAreaManager) this.plotAreaManager).getArea();
        PlotId id = PlotId.of(0, 0);
        File container = PlotSquared.platform().worldContainer();
        if (container.equals(new File("."))) {
            player.sendMessage(TranslatableCaption.of("debugimportworlds.world_container"));
            return CompletableFuture.completedFuture(false);
        }
        for (File folder : container.listFiles()) {
            String name = folder.getName();
            if (!this.worldUtil.isWorld(name) && PlotId.fromStringOrNull(name) == null) {
                UUID uuid;
                if (name.length() > 16) {
                    uuid = UUID.fromString(name);
                } else {
                    player.sendMessage(TranslatableCaption.of("players.fetching_player"));
                    uuid = PlotSquared.get().getImpromptuUUIDPipeline().getSingle(name, 60000L);
                }
                if (uuid == null) {
                    uuid =
                            UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
                }
                while (new File(container, id.toCommaSeparatedString()).exists()) {
                    id = id.getNextId();
                }
                File newDir = new File(container, id.toCommaSeparatedString());
                if (folder.renameTo(newDir)) {
                    area.getPlot(id).setOwner(uuid);
                }
            }
        }
        player.sendMessage(TranslatableCaption.of("players.done"));
        return CompletableFuture.completedFuture(true);
    }

}
