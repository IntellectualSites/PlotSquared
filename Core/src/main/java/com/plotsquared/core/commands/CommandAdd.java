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
package com.plotsquared.core.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import com.google.inject.Inject;
import com.plotsquared.core.commands.arguments.PlotMember;
import com.plotsquared.core.commands.requirements.Requirement;
import com.plotsquared.core.commands.requirements.CommandRequirement;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.PlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

class CommandAdd implements PlotSquaredCommandContainer {

    private final EventDispatcher eventDispatcher;

    @Inject
    CommandAdd(final @NonNull EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Requirement(CommandRequirement.PLAYER)
    @Requirement(CommandRequirement.IS_OWNER)
    @CommandPermission("plots.add")
    @CommandMethod("${command.prefix} add [target]")
    public void commandAdd(
            final @NonNull PlotPlayer<?> sender,
            @Argument("target") final PlotMember target,
            final @NonNull Plot plot
    ) {
        if (target instanceof PlotMember.Everyone) {
            if (!sender.hasPermission(Permission.PERMISSION_TRUST_EVERYONE) && !sender.hasPermission(Permission.PERMISSION_ADMIN_COMMAND_TRUST)) {
                sender.sendMessage(
                        TranslatableCaption.of("errors.invalid_player"),
                        TagResolver.resolver("value", Tag.inserting(
                                PlayerManager.resolveName(target.uuid()).toComponent(sender)
                        ))
                );
                return;
            }
        } else if (plot.isOwner(target.uuid())) {
            sender.sendMessage(
                    TranslatableCaption.of("member.already_added"),
                    TagResolver.resolver("player", Tag.inserting(
                            PlayerManager.resolveName(target.uuid()).toComponent(sender)
                    ))
            );
            return;
        } else if (plot.getMembers().contains(target.uuid())) {
            sender.sendMessage(
                    TranslatableCaption.of("member.already_added"),
                    TagResolver.resolver("player", Tag.inserting(
                            PlayerManager.resolveName(target.uuid()).toComponent(sender)
                    ))
            );
            return;
        } else if (plot.getMembers().size() >= sender.hasPermissionRange(Permission.PERMISSION_ADD, Settings.Limit.MAX_PLOTS)) {
            sender.sendMessage(
                    TranslatableCaption.of("members.plot_max_members_added"),
                    TagResolver.resolver("amount", Tag.inserting(Component.text(plot.getMembers().size())))
            );
            return;
        }

        if (target instanceof PlotMember.Player) {
            if (!plot.removeTrusted(target.uuid())) {
                if (plot.getDenied().contains(target.uuid())) {
                    plot.removeDenied(target.uuid());
                }
            }
        }

        plot.addMember(target.uuid());
        this.eventDispatcher.callMember(sender, plot, target.uuid(), true);
        sender.sendMessage(TranslatableCaption.of("member.member_added"));
    }
}
