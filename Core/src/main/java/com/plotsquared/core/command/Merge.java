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
import com.plotsquared.core.events.PlotMergeEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.PlotExpression;
import com.plotsquared.core.util.StringMan;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

@CommandDeclaration(command = "merge",
        aliases = "m",
        permission = "plots.merge",
        usage = "/plot merge <all | n | e | s | w> [removeroads]",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        confirmation = true)
public class Merge extends SubCommand {

    public static final String[] values = new String[]{"north", "east", "south", "west"};
    public static final String[] aliases = new String[]{"n", "e", "s", "w"};

    private final EventDispatcher eventDispatcher;
    private final EconHandler econHandler;

    @Inject
    public Merge(
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull EconHandler econHandler
    ) {
        this.eventDispatcher = eventDispatcher;
        this.econHandler = econHandler;
    }

    public static String direction(float yaw) {
        yaw = yaw / 90;
        int i = Math.round(yaw);
        return switch (i) {
            case -4, 0, 4 -> "SOUTH";
            case -1, 3 -> "EAST";
            case -2, 2 -> "NORTH";
            case -3, 1 -> "WEST";
            default -> "";
        };
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
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
        if (plot.getVolume() > Integer.MAX_VALUE) {
            player.sendMessage(TranslatableCaption.of("schematics.schematic_too_large"));
            return false;
        }
        Direction direction = null;
        if (args.length == 0) {
            switch (direction(player.getLocationFull().getYaw())) {
                case "NORTH" -> direction = Direction.NORTH;
                case "EAST" -> direction = Direction.EAST;
                case "SOUTH" -> direction = Direction.SOUTH;
                case "WEST" -> direction = Direction.WEST;
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                if (args[0].equalsIgnoreCase(values[i]) || args[0].equalsIgnoreCase(aliases[i])) {
                    direction = Direction.getFromIndex(i);
                    break;
                }
            }
            if (direction == null && (args[0].equalsIgnoreCase("all") || args[0]
                    .equalsIgnoreCase("auto")) && player.hasPermission(Permission.PERMISSION_MERGE_ALL)) {
                direction = Direction.ALL;
            }
        }
        if (direction == null) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    TagResolver.resolver("value", Tag.inserting(Component.text(
                            "/plot merge <" + StringMan.join(values, " | ") + "> [removeroads]"
                    )))
            );
            player.sendMessage(
                    TranslatableCaption.of("help.direction"),
                    TagResolver.resolver("dir", Tag.inserting(Component.text(direction(location.getYaw()))))
            );
            return false;
        }
        final int size = plot.getConnectedPlots().size();
        int max = player.hasPermissionRange("plots.merge", Settings.Limit.MAX_PLOTS);
        PlotMergeEvent event =
                this.eventDispatcher.callMerge(plot, direction, max, player);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    TagResolver.resolver("value", Tag.inserting(Component.text("Merge")))
            );
            return false;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        direction = event.getDir();
        final int maxSize = event.getMax();

        if (!force && size - 1 > maxSize) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    TagResolver.resolver("node", Tag.inserting(Component.text(Permission.PERMISSION_MERGE + "." + (size + 1))))
            );
            return false;
        }
        final PlotArea plotArea = plot.getArea();
        PlotExpression priceExr = plotArea.getPrices().getOrDefault("merge", null);
        final double price = priceExr == null ? 0d : priceExr.evaluate(size);

        UUID uuid = player.getUUID();

        if (!force && !plot.isOwner(uuid)) {
            if (!player.hasPermission(Permission.PERMISSION_ADMIN_COMMAND_MERGE)) {
                player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
                return false;
            } else {
                uuid = plot.getOwnerAbs();
            }
        }
        if (direction == Direction.ALL) {
            boolean terrain = true;
            if (args.length == 2) {
                terrain = "true".equalsIgnoreCase(args[1]);
            }
            if (!force && !terrain && !player.hasPermission(Permission.PERMISSION_MERGE_KEEP_ROAD)) {
                player.sendMessage(
                        TranslatableCaption.of("permission.no_permission"),
                        TagResolver.resolver(
                                "node",
                                Tag.inserting(Permission.PERMISSION_MERGE_KEEP_ROAD)
                        )
                );
                return true;
            }
            if (plot.getPlotModificationManager().autoMerge(Direction.ALL, maxSize, uuid, player, terrain)) {
                if (this.econHandler.isEnabled(plotArea) && !player.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_ECON) && price > 0d) {
                    this.econHandler.withdrawMoney(player, price);
                    player.sendMessage(
                            TranslatableCaption.of("economy.removed_balance"),
                            TagResolver.resolver("money", Tag.inserting(Component.text(this.econHandler.format(price)))),
                            TagResolver.resolver(
                                    "balance",
                                    Tag.inserting(Component.text(this.econHandler.format(this.econHandler.getMoney(player))))
                            )
                    );
                }
                player.sendMessage(TranslatableCaption.of("merge.success_merge"));
                eventDispatcher.callPostMerge(player, plot);
                return true;
            }
            player.sendMessage(TranslatableCaption.of("merge.no_available_automerge"));
            return false;
        }
        if (!force && this.econHandler.isEnabled(plotArea) && !player.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_ECON) && price > 0d && this.econHandler.getMoney(
                player) < price) {
            player.sendMessage(
                    TranslatableCaption.of("economy.cannot_afford_merge"),
                    TagResolver.resolver("money", Tag.inserting(Component.text(this.econHandler.format(price))))
            );
            return false;
        }
        final boolean terrain;
        if (args.length == 2) {
            terrain = "true".equalsIgnoreCase(args[1]);
        } else {
            terrain = true;
        }
        if (!force && !terrain && !player.hasPermission(Permission.PERMISSION_MERGE_KEEP_ROAD)) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    TagResolver.resolver("node", Tag.inserting(Permission.PERMISSION_MERGE_KEEP_ROAD))
            );
            return true;
        }
        if (plot.getPlotModificationManager().autoMerge(direction, maxSize - size, uuid, player, terrain)) {
            if (this.econHandler.isEnabled(plotArea) && !player.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_ECON) && price > 0d) {
                this.econHandler.withdrawMoney(player, price);
                player.sendMessage(
                        TranslatableCaption.of("economy.removed_balance"),
                        TagResolver.resolver("money", Tag.inserting(Component.text(this.econHandler.format(price))))
                );
            }
            player.sendMessage(TranslatableCaption.of("merge.success_merge"));
            eventDispatcher.callPostMerge(player, plot);
            return true;
        }
        Plot adjacent = plot.getRelative(direction);
        if (adjacent == null || !adjacent.hasOwner() || adjacent
                .isMerged((direction.getIndex() + 2) % 4) || (!force && adjacent.isOwner(uuid))) {
            player.sendMessage(TranslatableCaption.of("merge.no_available_automerge"));
            return false;
        }
        if (!force && !player.hasPermission(Permission.PERMISSION_MERGE_OTHER)) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    TagResolver.resolver("node", Tag.inserting(Permission.PERMISSION_MERGE_OTHER))
            );
            return false;
        }
        java.util.Set<UUID> uuids = adjacent.getOwners();
        boolean isOnline = false;
        for (final UUID owner : uuids) {
            final PlotPlayer<?> accepter = PlotSquared.platform().playerManager().getPlayerIfExists(owner);
            if (!force && accepter == null) {
                continue;
            }
            isOnline = true;
            final Direction dir = direction;
            Runnable run = () -> {
                accepter.sendMessage(TranslatableCaption.of("merge.merge_accepted"));
                plot.getPlotModificationManager().autoMerge(dir, maxSize - size, owner, player, terrain);
                PlotPlayer<?> plotPlayer = PlotSquared.platform().playerManager().getPlayerIfExists(player.getUUID());
                if (plotPlayer == null) {
                    accepter.sendMessage(TranslatableCaption.of("merge.merge_not_valid"));
                    return;
                }
                if (this.econHandler.isEnabled(plotArea) && !player.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_ECON) && price > 0d) {
                    if (!force && this.econHandler.getMoney(player) < price) {
                        player.sendMessage(
                                TranslatableCaption.of("economy.cannot_afford_merge"),
                                TagResolver.resolver("money", Tag.inserting(Component.text(this.econHandler.format(price))))
                        );
                        return;
                    }
                    this.econHandler.withdrawMoney(player, price);
                    player.sendMessage(
                            TranslatableCaption.of("economy.removed_balance"),
                            TagResolver.resolver("money", Tag.inserting(Component.text(this.econHandler.format(price))))
                    );
                }
                player.sendMessage(TranslatableCaption.of("merge.success_merge"));
                eventDispatcher.callPostMerge(player, plot);
            };
            if (!force && hasConfirmation(player)) {
                CmdConfirm.addPending(accepter, MINI_MESSAGE.serialize(MINI_MESSAGE
                                .deserialize(
                                        TranslatableCaption.of("merge.merge_request_confirm").getComponent(player),
                                        TagResolver.builder()
                                                .tag("player", Tag.inserting(Component.text(player.getName())))
                                                .tag(
                                                        "location",
                                                        Tag.inserting(Component.text(plot.getWorldName() + " " + plot.getId()))
                                                )
                                                .build()
                                )),
                        run
                );
            } else {
                run.run();
            }
        }
        if (force || !isOnline) {
            if (force || player.hasPermission(Permission.PERMISSION_ADMIN_COMMAND_MERGE_OTHER_OFFLINE)) {
                if (plot.getPlotModificationManager().autoMerge(
                        direction,
                        maxSize - size,
                        uuids.iterator().next(),
                        player,
                        terrain
                )) {
                    if (this.econHandler.isEnabled(plotArea) && !player.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_ECON) && price > 0d) {
                        if (!force && this.econHandler.getMoney(player) < price) {
                            player.sendMessage(
                                    TranslatableCaption.of("economy.cannot_afford_merge"),
                                    TagResolver.resolver("money", Tag.inserting(Component.text(this.econHandler.format(price))))
                            );
                            return false;
                        }
                        this.econHandler.withdrawMoney(player, price);
                        player.sendMessage(
                                TranslatableCaption.of("economy.removed_balance"),
                                TagResolver.resolver("money", Tag.inserting(Component.text(this.econHandler.format(price))))
                        );
                    }
                    player.sendMessage(TranslatableCaption.of("merge.success_merge"));
                    eventDispatcher.callPostMerge(player, plot);
                    return true;
                }
            }
            player.sendMessage(TranslatableCaption.of("merge.no_available_automerge"));
            return false;
        }
        player.sendMessage(TranslatableCaption.of("merge.merge_requested"));
        return true;
    }

}
