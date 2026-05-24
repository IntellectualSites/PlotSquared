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
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotArea;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;

@CommandDeclaration(command = "debugimportworlds",
        permission = "plots.admin",
        requiredType = RequiredType.CONSOLE,
        category = CommandCategory.TELEPORT)
public class DebugImportWorlds extends Command {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + DebugImportWorlds.class.getSimpleName());

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
        Path container = PlotSquared.platform().getWorldContainer("minecraft");
        if (container.equals(Path.of("."))) {
            player.sendMessage(TranslatableCaption.of("debugimportworlds.world_container"));
            return CompletableFuture.completedFuture(false);
        }
        try (Stream<Path> stream = Files.walk(container, 1)) {
            stream.map(path -> new PathWithName(path, path.getFileName().toString()))
                    .filter(p -> !this.worldUtil.isWorld(p.name()))
                    .filter(p -> PlotId.fromStringOrNull(p.name()) == null)
                    .forEach(new ImportAction(player, area, container));
        } catch (IOException e) {
            LOGGER.error("Failed to import world", e);
            throw new CommandException(StaticCaption.of("<red>World import failed. Check console.</red>"));
        }
        player.sendMessage(TranslatableCaption.of("players.done"));
        return CompletableFuture.completedFuture(true);
    }

    private record PathWithName(Path path, String name) {

    }

    private static class ImportAction implements Consumer<PathWithName> {

        private final PlotPlayer<?> player;
        private final PlotArea area;
        private final Path container;

        private PlotId id = PlotId.of(0, 0);

        private ImportAction(final PlotPlayer<?> player, final PlotArea area, final Path container) {
            this.player = player;
            this.area = area;
            this.container = container;
        }

        @Override
        public void accept(final PathWithName p) {
            UUID uuid;
            if (p.name().length() > 16) {
                uuid = UUID.fromString(p.name());
            } else {
                this.player.sendMessage(TranslatableCaption.of("players.fetching_player"));
                uuid = PlotSquared.get().getImpromptuUUIDPipeline().getSingle(p.name(), 60000L);
            }
            if (uuid == null) {
                uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + p.name()).getBytes(Charsets.UTF_8));
            }
            Path target;
            if (Files.exists(target = this.container.resolve(this.id.toCommaSeparatedString()))) {
                this.id = this.id.getNextId();
            }
            try {
                Files.move(p.path(), target);
                Objects.requireNonNull(this.area.getPlot(this.id)).setOwner(uuid);
            } catch (IOException ignored) {
            }
        }

    }

}
