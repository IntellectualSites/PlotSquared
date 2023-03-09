package com.plotsquared.core.commands;

import cloud.commandframework.annotations.Argument;
import cloud.commandframework.annotations.CommandMethod;
import cloud.commandframework.annotations.CommandPermission;
import com.google.inject.Inject;
import com.plotsquared.core.command.Claim;
import com.plotsquared.core.commands.requirements.CommandRequirement;
import com.plotsquared.core.commands.requirements.Requirement;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.PlayerClaimPlotEvent;
import com.plotsquared.core.events.PlotMergeEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.PlotExpression;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CommandClaim implements PlotSquaredCommandContainer {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + Claim.class.getSimpleName());

    private final EventDispatcher eventDispatcher;
    private final EconHandler econHandler;

    @Inject
    CommandClaim(
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull EconHandler econHandler
    ) {
        this.eventDispatcher = eventDispatcher;
        this.econHandler = econHandler;
    }

    @Requirement(CommandRequirement.PLAYER)
    @Requirement(CommandRequirement.IN_PLOT)
    @CommandPermission("plots.add")
    @CommandMethod("${command.prefix} claim [schematic]")
    public void commandClaim(
            final @NonNull PlotPlayer<?> sender,
            final @NonNull Plot plot,
            @Argument("schematic") final @Nullable String schematic
    ) {
        final PlayerClaimPlotEvent event = this.eventDispatcher.callClaim(sender, plot, schematic);
        if (event.getEventResult() == Result.DENY) {
            sender.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    TagResolver.resolver("value", Tag.inserting(Component.text("Claim")))
            );
            return;
        }

        final boolean forceClaim = event.getEventResult() == Result.FORCE;
        final int currentPlots = Settings.Limit.GLOBAL ?
                sender.getPlotCount() :
                sender.getPlotCount(sender.getLocation().getWorldName());
        final PlotArea area = plot.getArea();

        try (final MetaDataAccess<Integer> metaDataAccess = sender.accessPersistentMetaData(
                PlayerMetaDataKeys.PERSISTENT_GRANTED_PLOTS
        )) {
            int grants = 0;
            if (currentPlots >= sender.getAllowedPlots() && !forceClaim) {
                if (metaDataAccess.isPresent()) {
                    grants = metaDataAccess.get().orElse(0);
                    if (grants <= 0) {
                        sender.sendMessage(
                                TranslatableCaption.of("permission.cant_claim_more_plots"),
                                TagResolver.resolver("amount", Tag.inserting(Component.text(grants)))
                        );
                        metaDataAccess.remove();
                    }
                } else {
                    sender.sendMessage(
                            TranslatableCaption.of("permission.cant_claim_more_plots"),
                            TagResolver.resolver("amount", Tag.inserting(Component.text(sender.getAllowedPlots())))
                    );
                    return;
                }
            }

            if (!plot.canClaim(sender)) {
                sender.sendMessage(TranslatableCaption.of("working.plot_is_claimed"));
                return;
            }

            if (schematic != null && !schematic.isEmpty()) {
                if (area.isSchematicClaimSpecify()) {
                    if (!area.hasSchematic(schematic)) {
                        sender.sendMessage(
                                TranslatableCaption.of("schematics.schematic_invalid_named"),
                                TagResolver.builder()
                                        .tag("schemname", Tag.inserting(Component.text(schematic)))
                                        .tag("reason", Tag.inserting(Component.text("non-existent")))
                                        .build()
                        );
                        return;
                    }

                    if (!sender.hasPermission(
                            Permission.PERMISSION_CLAIM_SCHEMATIC.format(schematic)
                    ) && !sender.hasPermission(
                            "plots.admin.command.schematic"
                    ) && !forceClaim) {
                        sender.sendMessage(
                                TranslatableCaption.of("permission.no_schematic_permission"),
                                TagResolver.resolver("value", Tag.inserting(Component.text(schematic)))
                        );
                        return;
                    }
                }
            }

            if (this.econHandler.isEnabled(area) && !forceClaim) {
                final PlotExpression costExr = area.getPrices().get("claim");
                final double cost = costExr.evaluate(currentPlots);

                if (cost > 0d) {
                    if (!this.econHandler.isSupported()) {
                        sender.sendMessage(TranslatableCaption.of("economy.vault_or_consumer_null"));
                        return;
                    }
                    if (this.econHandler.getMoney(sender) < cost) {
                        sender.sendMessage(
                                TranslatableCaption.of("economy.cannot_afford_plot"),
                                TagResolver.builder()
                                        .tag("money", Tag.inserting(Component.text(this.econHandler.format(cost))))
                                        .tag(
                                                "balance",
                                                Tag.inserting(
                                                        Component.text(this.econHandler.format(this.econHandler.getMoney(sender)))
                                                )
                                        )
                                        .build()
                        );
                        return;
                    }
                    this.econHandler.withdrawMoney(sender, cost);
                    sender.sendMessage(
                            TranslatableCaption.of("economy.removed_balance"),
                            TagResolver.builder()
                                    .tag("money", Tag.inserting(Component.text(this.econHandler.format(cost))))
                                    .tag(
                                            "balance",
                                            Tag.inserting(
                                                    Component.text(this.econHandler.format(this.econHandler.getMoney(sender)))
                                            )
                                    )
                                    .build()
                    );
                }
            }

            if (grants > 0) {
                if (grants == 1) {
                    metaDataAccess.remove();
                } else {
                    metaDataAccess.set(grants - 1);
                }
                sender.sendMessage(
                        TranslatableCaption.of("economy.removed_granted_plot"),
                        TagResolver.builder()
                                .tag("usedGrants", Tag.inserting(Component.text(grants - 1)))
                                .tag("remainingGrants", Tag.inserting(Component.text(grants)))
                                .build()
                );
            }
        }

        if (!sender.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_BORDER)) {
            final int border = area.getBorder();
            if (border != Integer.MAX_VALUE && plot.getDistanceFromOrigin() > border && !forceClaim) {
                sender.sendMessage(TranslatableCaption.of("border.denied"));
                return;
            }
        }

        // Actually update the owner :)
        plot.setOwnerAbs(sender.getUUID());

        DBFunc.createPlotSafe(plot, () -> {
            try {
                TaskManager.getPlatformImplementation().sync(() -> {
                    if (!plot.claim(sender, true, event.getSchematic(), false, false)) {
                        LOGGER.info("Failed to claim plot {}", plot.getId().toCommaSeparatedString());
                        sender.sendMessage(TranslatableCaption.of("working.plot_not_claimed"));
                        plot.setOwnerAbs(null);
                    } else if (area.isAutoMerge()) {
                        final PlotMergeEvent mergeEvent = this.eventDispatcher.callMerge(
                                plot,
                                Direction.ALL,
                                Integer.MAX_VALUE,
                                sender
                        );
                        if (mergeEvent.getEventResult() == Result.DENY) {
                            sender.sendMessage(
                                    TranslatableCaption.of("events.event_denied"),
                                    TagResolver.resolver("value", Tag.inserting(Component.text("Auto merge on claim")))
                            );
                        } else {
                            if (plot.getPlotModificationManager().autoMerge(
                                    mergeEvent.getDir(),
                                    mergeEvent.getMax(),
                                    sender.getUUID(),
                                    sender,
                                    true
                            )) {
                                eventDispatcher.callPostMerge(sender, plot);
                            }
                        }
                    }
                    return null;
                });
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }, () -> {
            LOGGER.info("Failed to add plot to database: {}", plot.getId().toCommaSeparatedString());
            sender.sendMessage(TranslatableCaption.of("working.plot_not_claimed"));
            plot.setOwnerAbs(null);
        });
    }
}
