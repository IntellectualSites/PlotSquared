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
import com.plotsquared.core.events.PlotMergeEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.Expression;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.StringMan;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

@CommandDeclaration(command = "merge",
    aliases = "m",
    description = "Merge the plot you are standing on with another plot",
    permission = "plots.merge",
    usage = "/plot merge <all | n | e | s | w> [removeroads]",
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.NONE,
    confirmation = true)
public class Merge extends SubCommand {

    public static final String[] values = new String[] {"north", "east", "south", "west"};
    public static final String[] aliases = new String[] {"n", "e", "s", "w"};
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();

    private final EventDispatcher eventDispatcher;
    private final EconHandler econHandler;

    @Inject public Merge(@Nonnull final EventDispatcher eventDispatcher,
                         @Nullable final EconHandler econHandler) {
        this.eventDispatcher = eventDispatcher;
        this.econHandler = econHandler;
    }
    
    public static String direction(float yaw) {
        yaw = yaw / 90;
        int i = Math.round(yaw);
        switch (i) {
            case -4:
            case 0:
            case 4:
                return "SOUTH";
            case -1:
            case 3:
                return "EAST";
            case -2:
            case 2:
                return "NORTH";
            case -3:
            case 1:
                return "WEST";
            default:
                return "";
        }
    }

    @Override public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        Location location = player.getLocationFull();
        final Plot plot = location.getPlotAbs();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return false;
        }
        Direction direction = null;
        if (args.length == 0) {
            switch (direction(player.getLocationFull().getYaw())) {
                case "NORTH":
                    direction = Direction.NORTH;
                    break;
                case "EAST":
                    direction = Direction.EAST;
                    break;
                case "SOUTH":
                    direction = Direction.SOUTH;
                    break;
                case "WEST":
                    direction = Direction.WEST;
                    break;
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                if (args[0].equalsIgnoreCase(values[i]) || args[0].equalsIgnoreCase(aliases[i])) {
                    direction = Direction.getFromIndex(i);
                    break;
                }
            }
            if (direction == null && (args[0].equalsIgnoreCase("all") || args[0]
                .equalsIgnoreCase("auto"))) {
                direction = Direction.ALL;
            }
        }
        if (direction == null) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    Template.of("value", "/plot merge <" + StringMan.join(values, " | ") + "> [removeroads]")
            );
            player.sendMessage(
                    TranslatableCaption.of("help.direction"),
                    Template.of("dir", direction(location.getYaw()))
            );
            return false;
        }
        final int size = plot.getConnectedPlots().size();
        int max = Permissions.hasPermissionRange(player, "plots.merge", Settings.Limit.MAX_PLOTS);
        PlotMergeEvent event =
            this.eventDispatcher.callMerge(plot, direction, max, player);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    Template.of("value", "Merge"));
            return false;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        direction = event.getDir();
        final int maxSize = event.getMax();

        if (!force && size - 1 > maxSize) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    Template.of("node", "plots.merge." + (size + 1))
            );
            return false;
        }
        final PlotArea plotArea = plot.getArea();
        Expression<Double> priceExr = plotArea.getPrices().getOrDefault("merge", null);
        final double price = priceExr == null ? 0d : priceExr.evaluate((double) size);

        UUID uuid = player.getUUID();
        if (direction == Direction.ALL) {
            boolean terrain = true;
            if (args.length == 2) {
                terrain = "true".equalsIgnoreCase(args[1]);
            }
            if (!force && !terrain && !Permissions
                .hasPermission(player, Captions.PERMISSION_MERGE_KEEP_ROAD)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", "plots.merge.keeproad")
                    );
                return true;
            }
            if (plot.autoMerge(Direction.ALL, maxSize, uuid, terrain)) {
                if (this.econHandler != null && plotArea.useEconomy() && price > 0d) {
                    this.econHandler.withdrawMoney(player, price);
                    player.sendMessage(
                            TranslatableCaption.of("economy.removed_balance"),
                            Template.of("money", String.valueOf(price))
                    );
                }
                player.sendMessage(TranslatableCaption.of("merge.success_merge"));
                return true;
            }
            player.sendMessage(TranslatableCaption.of("merge.no_available_automerge"));
            return false;
        }
        if (!force && !plot.isOwner(uuid)) {
            if (!Permissions.hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_MERGE)) {
                player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
                return false;
            } else {
                uuid = plot.getOwnerAbs();
            }
        }
        if (!force && this.econHandler != null && plotArea.useEconomy() && price > 0d
            && this.econHandler.getMoney(player) < price) {
            player.sendMessage(
                    TranslatableCaption.of("economy.cannot_afford_merge"),
                    Template.of("money", String.valueOf(price))
            );
            return false;
        }
        final boolean terrain;
        if (args.length == 2) {
            terrain = "true".equalsIgnoreCase(args[1]);
        } else {
            terrain = true;
        }
        if (!force && !terrain && !Permissions
            .hasPermission(player, Captions.PERMISSION_MERGE_KEEP_ROAD)) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    Template.of("node", "plots.merge.keeproad")
            );
            return true;
        }
        if (plot.autoMerge(direction, maxSize - size, uuid, terrain)) {
            if (this.econHandler != null && plotArea.useEconomy() && price > 0d) {
                this.econHandler.withdrawMoney(player, price);
                player.sendMessage(
                        TranslatableCaption.of("economy.removed_balance"),
                        Template.of("money", String.valueOf(price))
                );
            }
            player.sendMessage(TranslatableCaption.of("success_merge"));
            return true;
        }
        Plot adjacent = plot.getRelative(direction);
        if (adjacent == null || !adjacent.hasOwner() || adjacent
            .getMerged((direction.getIndex() + 2) % 4) || (!force && adjacent.isOwner(uuid))) {
            player.sendMessage(TranslatableCaption.of("merge.no_available_automerge"));
            return false;
        }
        if (!force && !Permissions.hasPermission(player, Captions.PERMISSION_MERGE_OTHER)) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    Template.of("node", "plots.merge.other")
            );
            return false;
        }
        java.util.Set<UUID> uuids = adjacent.getOwners();
        boolean isOnline = false;
        for (final UUID owner : uuids) {
            final PlotPlayer accepter = PlotSquared.platform().getPlayerManager().getPlayerIfExists(owner);
            if (!force && accepter == null) {
                continue;
            }
            isOnline = true;
            final Direction dir = direction;
            Runnable run = () -> {
                accepter.sendMessage(TranslatableCaption.of("merge.merge_accepted"));
                plot.autoMerge(dir, maxSize - size, owner, terrain);
                PlotPlayer plotPlayer = PlotSquared.platform().getPlayerManager().getPlayerIfExists(player.getUUID());
                if (plotPlayer == null) {
                    accepter.sendMessage(TranslatableCaption.of("merge.merge_not_valid"));
                    return;
                }
                if (this.econHandler != null && plotArea.useEconomy() && price > 0d) {
                    if (!force && this.econHandler.getMoney(player) < price) {
                        player.sendMessage(
                                TranslatableCaption.of("economy.cannot_afford_merge"),
                                Template.of("money", String.valueOf(price))
                        );
                        return;
                    }
                    this.econHandler.withdrawMoney(player, price);
                    player.sendMessage(
                            TranslatableCaption.of("economy.removed_balance"),
                            Template.of("money", String.valueOf(price))
                    );
                }
                player.sendMessage(TranslatableCaption.of("merge.success_merge"));
            };
            if (!force && hasConfirmation(player)) {
                CmdConfirm.addPending(accepter, MINI_MESSAGE.serialize(MINI_MESSAGE
                        .parse(TranslatableCaption.of("merge.merge_request_confirm").getComponent(player), Template.of("player", player.getName()))),
                    run);
            } else {
                run.run();
            }
        }
        if (!force && !isOnline) {
            player.sendMessage(TranslatableCaption.of("merge.no_available_automerge"));
            return false;
        }
        player.sendMessage(TranslatableCaption.of("merge.merge_requested"));
        return true;
    }
}
