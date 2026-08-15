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
import com.plotsquared.core.events.PlotChangeOwnerEvent;
import com.plotsquared.core.events.PlotUnlinkEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.TabCompletions;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

@CommandDeclaration(command = "setowner",
        permission = "plots.admin.command.setowner",
        usage = "/plot setowner <player>",
        aliases = {"owner", "so", "seto"},
        category = CommandCategory.CLAIMING,
        requiredType = RequiredType.NONE,
        confirmation = true)
public class Owner extends SetCommand {

    private final EventDispatcher eventDispatcher;

    @Inject
    public Owner(final @NonNull EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public boolean set(final PlotPlayer<?> player, final Plot plot, String value) {
        if (value == null || value.isEmpty()) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    TagResolver.resolver("value", Tag.inserting(Component.text("/plot setowner <owner>")))
            );
            return false;
        }
        @Nullable final UUID oldOwner = plot.getOwnerAbs();
        Set<Plot> plots = plot.getConnectedPlots();

        final Consumer<UUID> uuidConsumer = uuid -> {
            if (uuid == null && !value.equalsIgnoreCase("none") && !value.equalsIgnoreCase("null")
                    && !value.equalsIgnoreCase("-")) {
                player.sendMessage(
                        TranslatableCaption.of("errors.invalid_player"),
                        TagResolver.resolver("value", Tag.inserting(Component.text(value)))
                );
                return;
            }
            PlotChangeOwnerEvent event = this.eventDispatcher.callOwnerChange(
                    player,
                    plot,
                    plot.hasOwner() ? plot.getOwnerAbs() : null,
                    uuid,
                    plot.hasOwner()
            );
            if (event.getEventResult() == Result.DENY) {
                player.sendMessage(
                        TranslatableCaption.of("events.event_denied"),
                        TagResolver.resolver("value", Tag.inserting(Component.text("Owner change")))
                );
                return;
            }
            uuid = event.getNewOwner();
            boolean force = event.getEventResult() == Result.FORCE;
            if (uuid == null) {
                if (!force && !player.hasPermission(
                        Permission.PERMISSION_ADMIN_COMMAND_SET_OWNER,
                        true
                )) {
                    return;
                }
                PlotUnlinkEvent unlinkEvent = this.eventDispatcher.callUnlink(
                        plot.getArea(),
                        plot,
                        false,
                        false,
                        PlotUnlinkEvent.REASON.NEW_OWNER
                );
                if (unlinkEvent.getEventResult() == Result.DENY) {
                    player.sendMessage(
                            TranslatableCaption.of("events.event_denied"),
                            TagResolver.resolver("value", Tag.inserting(Component.text("Unlink on owner change")))
                    );
                    return;
                }
                if (plot.getPlotModificationManager().unlinkPlot(unlinkEvent.isCreateRoad(), unlinkEvent.isCreateRoad())) {
                    eventDispatcher.callPostUnlink(plot, PlotUnlinkEvent.REASON.NEW_OWNER);
                }
                Set<Plot> connected = plot.getConnectedPlots();
                for (Plot current : connected) {
                    current.unclaim();
                    current.getPlotModificationManager().removeSign();
                }
                eventDispatcher.callPostOwnerChange(player, plot, oldOwner);
                player.sendMessage(TranslatableCaption.of("owner.set_owner"));
                return;
            }
            final PlotPlayer<?> other = PlotSquared.platform().playerManager().getPlayerIfExists(uuid);
            if (plot.isOwner(uuid)) {
                player.sendMessage(
                        TranslatableCaption.of("member.already_owner"),
                        PlotSquared.platform().playerManager().getUsernameCaption(uuid)
                                .thenApply(caption -> TagResolver.resolver(
                                "player",
                                Tag.inserting(caption.toComponent(player))
                        ))
                );
                return;
            }
            if (!force && !player.hasPermission(Permission.PERMISSION_ADMIN_COMMAND_SET_OWNER)) {
                if (other == null) {
                    player.sendMessage(
                            TranslatableCaption.of("errors.invalid_player_offline"),
                            PlotSquared.platform().playerManager().getUsernameCaption(uuid)
                                    .thenApply(caption -> TagResolver.resolver(
                                    "player",
                                    Tag.inserting(caption.toComponent(player))
                            ))
                    );
                    return;
                }
                int size = plots.size();
                int currentPlots = (Settings.Limit.GLOBAL ?
                        other.getPlotCount() :
                        other.getPlotCount(plot.getWorldName())) + size;
                try (final MetaDataAccess<Integer> metaDataAccess = player.accessPersistentMetaData(PlayerMetaDataKeys.PERSISTENT_GRANTED_PLOTS)) {
                    int grants;
                    if (currentPlots >= other.getAllowedPlots()) {
                        if (metaDataAccess.isPresent()) {
                            grants = metaDataAccess.get().orElse(0);
                            if (grants <= 0) {
                                metaDataAccess.remove();
                                player.sendMessage(TranslatableCaption.of("permission.cant_transfer_more_plots"));
                                return;
                            }
                        }
                    }
                }
            }
            final UUID finalUUID = uuid;
            PlotSquared.get().getImpromptuUUIDPipeline().getSingle(uuid, (finalName, throwable) -> {
                final boolean removeDenied = plot.isDenied(finalUUID);
                Runnable run = () -> {
                    if (plot.setOwner(finalUUID, player)) {
                        if (removeDenied) {
                            plot.removeDenied(finalUUID);
                        }
                        plot.getPlotModificationManager().setSign(finalName);
                        player.sendMessage(TranslatableCaption.of("owner.set_owner"));
                        eventDispatcher.callPostOwnerChange(player, plot, oldOwner);
                        if (other != null) {
                            other.sendMessage(
                                    TranslatableCaption.of("owner.now_owner"),
                                    TagResolver.resolver(
                                            "plot",
                                            Tag.inserting(Component.text(plot.getArea() + ";" + plot.getId()))
                                    )
                            );
                        }
                    } else {
                        player.sendMessage(TranslatableCaption.of("owner.set_owner_cancelled"));
                    }
                };
                if (hasConfirmation(player)) {
                    CmdConfirm.addPending(player, "/plot setowner " + value, run);
                } else {
                    TaskManager.runTask(run);
                }
            });
        };

        if (value.length() == 36) {
            try {
                uuidConsumer.accept(UUID.fromString(value));
            } catch (Exception ignored) {
            }
        } else {
            PlotSquared.get().getImpromptuUUIDPipeline().getSingle(value, (uuid, throwable) -> uuidConsumer.accept(uuid));
        }
        return true;
    }

    @Override
    public Collection<Command> tab(final PlotPlayer<?> player, final String[] args, final boolean space) {
        return TabCompletions.completePlayers(player, String.join(",", args).trim(), Collections.emptyList());
    }

}
