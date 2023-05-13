package com.plotsquared.core.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.commands.requirements.CommandRequirement;
import com.plotsquared.core.commands.requirements.Requirement;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.query.PlotQuery;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.TimeoutException;

public class CommandAlias implements PlotSquaredCommandContainer {

    @Requirement(CommandRequirement.PLAYER)
    @Requirement(CommandRequirement.PLOT_HAS_OWNER)
    @CommandPermission("plots.alias")
    @CommandMethod("${command.prefix} alias set <alias>")
    public void commandAliasSet(
            final @NonNull PlotPlayer<?> sender,
            final @NonNull Plot plot,
            @Argument("alias") final @NonNull String alias
    ) {
        final boolean isOwner = plot.isOwner(sender.getUUID());

        if (!isOwner && !sender.hasPermission(Permission.PERMISSION_ADMIN_ALIAS_SET)) {
            sender.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return;
        } else if (!sender.hasPermission(Permission.PERMISSION_ALIAS_SET)) {
            sender.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    TagResolver.resolver(
                            "node",
                            Tag.inserting(Permission.PERMISSION_ALIAS_SET)
                    )
            );
            return;
        }

        if (alias.length() >= 50) {
            sender.sendMessage(TranslatableCaption.of("alias.alias_too_long"));
            return;
        }

        if (MathMan.isInteger(alias)) {
            sender.sendMessage(TranslatableCaption.of("flag.not_valid_value")); // TODO this is obviously wrong
            return;
        }

        if (PlotQuery.newQuery().inArea(plot.getArea())
                .withAlias(alias)
                .anyMatch()) {
            sender.sendMessage(
                    TranslatableCaption.of("alias.alias_is_taken"),
                    TagResolver.resolver("alias", Tag.inserting(Component.text(alias)))
            );
            return;
        }

        if (Settings.UUID.OFFLINE) {
            plot.setAlias(alias);
            sender.sendMessage(
                    TranslatableCaption.of("alias.alias_set_to"),
                    TagResolver.resolver("alias", Tag.inserting(Component.text(alias)))
            );
            return;
        }

        PlotSquared.get().getImpromptuUUIDPipeline().getSingle(alias, ((uuid, throwable) -> {
            if (throwable instanceof TimeoutException) {
                sender.sendMessage(TranslatableCaption.of("players.fetching_players_timeout"));
            } else if (uuid != null) {
                sender.sendMessage(
                        TranslatableCaption.of("alias.alias_is_taken"),
                        TagResolver.resolver("alias", Tag.inserting(Component.text(alias)))
                );
            } else {
                plot.setAlias(alias);
                sender.sendMessage(
                        TranslatableCaption.of("alias.alias_set_to"),
                        TagResolver.resolver("alias", Tag.inserting(Component.text(alias)))
                );
            }
        }));
    }

    @Requirement(CommandRequirement.PLAYER)
    @Requirement(CommandRequirement.PLOT_HAS_OWNER)
    @CommandPermission("plots.alias")
    @CommandMethod("${command.prefix} alias remove")
    public void commandAliasRemove(
            final @NonNull PlotPlayer<?> sender,
            final @NonNull Plot plot
    ) {
        final boolean isOwner = plot.isOwner(sender.getUUID());

        if (!isOwner && !sender.hasPermission(Permission.PERMISSION_ADMIN_ALIAS_REMOVE)) {
            sender.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return;
        } else if (!sender.hasPermission(Permission.PERMISSION_ALIAS_REMOVE)) {
            sender.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    TagResolver.resolver(
                            "node",
                            Tag.inserting(Permission.PERMISSION_ALIAS_REMOVE)
                    )
            );
            return;
        }

        if (plot.getAlias().isEmpty()) {
            sender.sendMessage(
                    TranslatableCaption.of("alias.no_alias_set")
            );
            return;
        }

        final String currentAlias = plot.getAlias();
        plot.setAlias(null);

        sender.sendMessage(
                TranslatableCaption.of("alias.alias_removed"),
                TagResolver.resolver("alias", Tag.inserting(Component.text(currentAlias)))
        );
    }
}
