/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotChangeOwnerEvent;
import com.plotsquared.core.events.PlotUnlinkEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@CommandDeclaration(command = "setowner",
    permission = "plots.set.owner",
    description = "Set the plot owner",
    usage = "/plot setowner <player>",
    aliases = {"owner", "so", "seto"},
    category = CommandCategory.CLAIMING,
    requiredType = RequiredType.NONE,
    confirmation = true)
public class Owner extends SetCommand {

    private final EventDispatcher eventDispatcher;
    
    @Inject public Owner(@Nonnull final EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }
    
    @Override public boolean set(final PlotPlayer player, final Plot plot, String value) {
        if (value == null || value.isEmpty()) {
            player.sendMessage(TranslatableCaption.of("owner.set_owner_missing_player"));
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    Template.of("value", "/plot setowner <owner>")
            );
            return false;
        }
        Set<Plot> plots = plot.getConnectedPlots();

        final Consumer<UUID> uuidConsumer = uuid -> {
            if (uuid == null && !value.equalsIgnoreCase("none") && !value.equalsIgnoreCase("null")
                && !value.equalsIgnoreCase("-")) {
                player.sendMessage(
                        TranslatableCaption.of("errors.invalid_player"),
                        Template.of("value", value)
                );
                return;
            }
            PlotChangeOwnerEvent event = this.eventDispatcher.callOwnerChange(player, plot, plot.hasOwner() ? plot.getOwnerAbs() : null, uuid,
                    plot.hasOwner()); 
            if (event.getEventResult() == Result.DENY) {
                player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    Template.of("value", "Owner change"));
                return;
            }
            uuid = event.getNewOwner();
            boolean force = event.getEventResult() == Result.FORCE;
            if (uuid == null) {
                if (!force && !Permissions
                    .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_SET_OWNER,
                        true)) {
                    return;
                }
                PlotUnlinkEvent unlinkEvent = this.eventDispatcher.callUnlink(plot.getArea(), plot, false, false, PlotUnlinkEvent.REASON.NEW_OWNER);
                if (unlinkEvent.getEventResult() == Result.DENY) {
                    player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    Template.of("value", "Unlink on owner change"));
                    return;
                }
                plot.unlinkPlot(unlinkEvent.isCreateRoad(), unlinkEvent.isCreateRoad());
                Set<Plot> connected = plot.getConnectedPlots();
                for (Plot current : connected) {
                    current.unclaim();
                    current.removeSign();
                }
                player.sendMessage(TranslatableCaption.of("owner.set_owner"));
                return;
            }
            final PlotPlayer<?> other = PlotSquared.platform().getPlayerManager().getPlayerIfExists(uuid);
            if (plot.isOwner(uuid)) {
                player.sendMessage(
                        TranslatableCaption.of("member.already_owner"),
                        Template.of("player", PlayerManager.getName(uuid))
                );
                return;
            }
            if (!force && !Permissions
                .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_SET_OWNER)) {
                if (other == null) {
                    player.sendMessage(
                            TranslatableCaption.of("invalid_player_offline"),
                            Template.of("player", PlayerManager.getName(uuid))
                    );
                    return;
                }
                int size = plots.size();
                int currentPlots = (Settings.Limit.GLOBAL ?
                    other.getPlotCount() :
                    other.getPlotCount(plot.getWorldName())) + size;
                if (currentPlots > other.getAllowedPlots()) {
                    player.sendMessage(TranslatableCaption.of("permission.cant_transfer_more_plots"));
                    return;
                }
            }
            final UUID finalUUID = uuid;
            PlotSquared.get().getImpromptuUUIDPipeline().getSingle(uuid, (finalName, throwable) -> {
                final boolean removeDenied = plot.isDenied(finalUUID);
                Runnable run = () -> {
                    if (plot.setOwner(finalUUID, player)) {
                        if (removeDenied)
                            plot.removeDenied(finalUUID);
                        plot.setSign(finalName);
                        player.sendMessage(TranslatableCaption.of("owner.set_owner"));
                        if (other != null) {
                            other.sendMessage(
                                    TranslatableCaption.of("owner.now_owner"),
                                    Template.of("plot", plot.getArea() + ";" + plot.getId())
                            );
                        }
                    } else {
                        player.sendMessage(TranslatableCaption.of("owner.set_owner_cancelled"));
                    }
                };
                if (hasConfirmation(player)) {
                    CmdConfirm.addPending(player, "/plot set owner " + value, run);
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
            PlotSquared.get().getImpromptuUUIDPipeline().getSingle(value, (uuid, throwable) -> {
               if (throwable instanceof TimeoutException) {
                   player.sendMessage(TranslatableCaption.of("players.fetching_players_timeout"));
               } else if (throwable != null) {
                   player.sendMessage(
                           TranslatableCaption.of("errors.invalid_player"),
                           Template.of("value", value)
                   );
               } else {
                   uuidConsumer.accept(uuid);
               }
            });
        }
        return true;
    }
}
