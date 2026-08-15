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

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.listener.PlotListener;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.uuid.UUIDMapping;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@CommandDeclaration(usage = "/plot purge world:<world> area:<area> id:<id> owner:<owner> shared:<shared> unknown:[true | false] clear:[true | false]",
        command = "purge",
        permission = "plots.admin",
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.CONSOLE,
        confirmation = true)
public class Purge extends SubCommand {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + Purge.class.getSimpleName());

    private final PlotAreaManager plotAreaManager;
    private final PlotListener plotListener;

    @Inject
    public Purge(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull PlotListener plotListener
    ) {
        this.plotAreaManager = plotAreaManager;
        this.plotListener = plotListener;
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        if (args.length == 0) {
            sendUsage(player);
            return false;
        }

        String world = null;
        PlotArea area = null;
        PlotId id = null;
        UUID owner = null;
        UUID added = null;
        boolean clear = false;
        boolean unknown = false;
        for (String arg : args) {
            String[] split = arg.split(":");
            if (split.length != 2) {
                sendUsage(player);
                return false;
            }
            switch (split[0].toLowerCase()) {
                case "world", "w" -> world = split[1];
                case "area", "a" -> {
                    area = this.plotAreaManager.getPlotAreaByString(split[1]);
                    if (area == null) {
                        player.sendMessage(
                                TranslatableCaption.of("errors.not_valid_plot_world"),
                                TagResolver.resolver("value", Tag.inserting(Component.text(split[1])))
                        );
                        return false;
                    }
                }
                case "plotid", "id" -> {
                    try {
                        id = PlotId.fromString(split[1]);
                    } catch (IllegalArgumentException ignored) {
                        player.sendMessage(
                                TranslatableCaption.of("invalid.not_valid_plot_id"),
                                TagResolver.resolver("value", Tag.inserting(Component.text(split[1])))
                        );
                        return false;
                    }
                }
                case "owner", "o" -> {
                    UUIDMapping ownerMapping = PlotSquared.get().getImpromptuUUIDPipeline().getImmediately(split[1]);
                    if (ownerMapping == null) {
                        player.sendMessage(
                                TranslatableCaption.of("errors.invalid_player"),
                                TagResolver.resolver("value", Tag.inserting(Component.text(split[1])))
                        );
                        return false;
                    }
                    owner = ownerMapping.uuid();
                }
                case "shared", "s" -> {
                    UUIDMapping addedMapping = PlotSquared.get().getImpromptuUUIDPipeline().getImmediately(split[1]);
                    if (addedMapping == null) {
                        player.sendMessage(
                                TranslatableCaption.of("errors.invalid_player"),
                                TagResolver.resolver("value", Tag.inserting(Component.text(split[1])))
                        );
                        return false;
                    }
                    added = addedMapping.uuid();
                }
                case "clear", "c", "delete", "d", "del" -> clear = Boolean.parseBoolean(split[1]);
                case "unknown", "?", "u" -> unknown = Boolean.parseBoolean(split[1]);
                default -> {
                    sendUsage(player);
                    return false;
                }
            }
        }
        final HashSet<Plot> toDelete = new HashSet<>();
        for (Plot plot : PlotQuery.newQuery().whereBasePlot()) {
            if (world != null && !plot.getWorldName().equalsIgnoreCase(world)) {
                continue;
            }
            if (area != null && !plot.getArea().equals(area)) {
                continue;
            }
            if (id != null && !plot.getId().equals(id)) {
                continue;
            }
            if (owner != null && !plot.isOwner(owner)) {
                continue;
            }
            if (added != null && !plot.isAdded(added)) {
                continue;
            }
            if (unknown) {
                UUIDMapping uuidMapping = PlotSquared.get().getImpromptuUUIDPipeline().getImmediately(plot.getOwner());
                if (uuidMapping != null) {
                    continue;
                }
            }
            toDelete.addAll(plot.getConnectedPlots());
        }
        if (PlotSquared.get().plots_tmp != null) {
            for (Entry<String, HashMap<PlotId, Plot>> entry : PlotSquared.get().plots_tmp
                    .entrySet()) {
                String worldName = entry.getKey();
                if (world != null && !world.equalsIgnoreCase(worldName)) {
                    continue;
                }
                for (Entry<PlotId, Plot> entry2 : entry.getValue().entrySet()) {
                    Plot plot = entry2.getValue();
                    if (area != null && !plot.getArea().equals(area)) {
                        continue;
                    }
                    if (id != null && !plot.getId().equals(id)) {
                        continue;
                    }
                    if (owner != null && !plot.isOwner(owner)) {
                        continue;
                    }
                    if (added != null && !plot.isAdded(added)) {
                        continue;
                    }
                    if (unknown) {
                        UUIDMapping addedMapping = PlotSquared.get().getImpromptuUUIDPipeline().getImmediately(plot.getOwner());
                        if (addedMapping != null) {
                            continue;
                        }
                    }
                    toDelete.add(plot);
                }
            }
        }
        if (toDelete.isEmpty()) {
            player.sendMessage(TranslatableCaption.of("invalid.found_no_plots"));
            return false;
        }
        String cmd =
                "/plot purge " + StringMan.join(args, " ") + " (" + toDelete.size() + " plots)";
        boolean finalClear = clear;
        Runnable run = () -> {
            LOGGER.info("Calculating plots to purge, please wait...");
            HashSet<Integer> ids = new HashSet<>();
            Iterator<Plot> iterator = toDelete.iterator();
            AtomicBoolean cleared = new AtomicBoolean(true);
            Runnable runasync = new Runnable() {
                @Override
                public void run() {
                    while (iterator.hasNext() && cleared.get()) {
                        cleared.set(false);
                        Plot plot = iterator.next();
                        if (plot.temp != Integer.MAX_VALUE) {
                            try {
                                ids.add(plot.temp);
                                if (finalClear) {
                                    plot.getPlotModificationManager().clear(false, true, player,
                                            () -> LOGGER.info("Plot {} cleared by purge", plot.getId())
                                    );
                                } else {
                                    plot.getPlotModificationManager().removeSign();
                                }
                                plot.getArea().removePlot(plot.getId());
                                for (PlotPlayer<?> pp : plot.getPlayersInPlot()) {
                                    Purge.this.plotListener.plotEntry(pp, plot);
                                }
                            } catch (NullPointerException e) {
                                LOGGER.error("NullPointer during purge detected. This is likely"
                                        + " because you are deleting a world that has been removed", e);
                            }
                        }
                        cleared.set(true);
                    }
                    if (iterator.hasNext()) {
                        TaskManager.runTaskAsync(this);
                    } else {
                        TaskManager.runTask(() -> {
                            DBFunc.purgeIds(ids);
                            player.sendMessage(
                                    TranslatableCaption.of("purge.purge_success"),
                                    TagResolver.resolver(
                                            "amount",
                                            Tag.inserting(Component.text(ids.size() + "/" + toDelete.size()))
                                    )
                            );
                        });
                    }
                }
            };
            TaskManager.runTaskAsync(runasync);
        };
        if (hasConfirmation(player)) {
            if (unknown) {
                if (Settings.UUID.BACKGROUND_CACHING_ENABLED) {
                    player.sendMessage(TranslatableCaption.of("purge.confirm_purge_unknown_bg_enabled"));
                } else {
                    player.sendMessage(TranslatableCaption.of("purge.confirm_purge_unknown_bg_disabled"));
                }
            }
            CmdConfirm.addPending(player, cmd, run);
        } else {
            run.run();
        }
        return true;
    }

}
