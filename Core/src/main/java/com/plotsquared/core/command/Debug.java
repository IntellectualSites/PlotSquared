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
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.entity.EntityCategories;
import com.plotsquared.core.util.entity.EntityCategory;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.uuid.UUIDMapping;
import com.sk89q.worldedit.world.entity.EntityType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandDeclaration(command = "debug",
        category = CommandCategory.DEBUG,
        usage = "/plot debug",
        permission = "plots.admin")
public class Debug extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final WorldUtil worldUtil;

    @Inject
    public Debug(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull WorldUtil worldUtil
    ) {
        this.plotAreaManager = plotAreaManager;
        this.worldUtil = worldUtil;
    }

    @Override
    public boolean onCommand(PlotPlayer<?> player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    TagResolver.resolver(
                            "value",
                            Tag.inserting(Component.text("/plot debug <player | debug-players | entitytypes | msg>"))
                    )
            );
        }
        if (args.length > 0) {
            if ("player".equalsIgnoreCase(args[0])) {
                for (Map.Entry<String, Object> meta : player.getMeta().entrySet()) {
                    player.sendMessage(StaticCaption.of("Key: " + meta.getKey() + " Value: " + meta
                            .getValue()
                            .toString() + " , "));
                }
                return true;
            }
        }
        if (args.length > 0 && "uuids".equalsIgnoreCase(args[0])) {
            final Collection<UUIDMapping> mappings = PlotSquared.get().getImpromptuUUIDPipeline().getAllImmediately();
            player.sendMessage(
                    TranslatableCaption.of("debug.cached_uuids"),
                    TagResolver.resolver("value", Tag.inserting(Component.text(mappings.size())))
            );
            return true;
        }
        if (args.length > 0 && "debug-players".equalsIgnoreCase(args[0])) {
            player.sendMessage(TranslatableCaption.of("debug.player_in_debugmode"));
            for (final PlotPlayer<?> pp : PlotPlayer.getDebugModePlayers()) {
                player.sendMessage(
                        TranslatableCaption.of("debug.player_in_debugmode_list"),
                        TagResolver.resolver("value", Tag.inserting(Component.text(pp.getName())))
                );
            }
            return true;
        }
        if (args.length > 0 && "entitytypes".equalsIgnoreCase(args[0])) {
            EntityCategories.init();
            player.sendMessage(TranslatableCaption.of("debug.entity_categories"));
            EntityCategory.REGISTRY.forEach(category -> {
                final StringBuilder builder =
                        new StringBuilder("<gray>-</gray> <gold>").append(category.getId()).append("</gold><gray>: <gold>");
                for (final EntityType entityType : category.getAll()) {
                    builder.append(entityType.getId()).append(" ");
                }
                builder.append("</gold>");
                player.sendMessage(StaticCaption.of("<prefix>" + builder));
            });
            EntityType.REGISTRY.values().stream().sorted(Comparator.comparing(EntityType::getId))
                    .forEach(entityType -> {
                        long categoryCount = EntityCategory.REGISTRY.values().stream()
                                .filter(category -> category.contains(entityType)).count();
                        if (categoryCount > 0) {
                            return;
                        }
                        player.sendMessage(StaticCaption.of("<prefix>" + entityType.getName() + " is in "
                                + categoryCount + " categories"));
                    });
            return true;
        }
        Set<TranslatableCaption> captions = PlotSquared
                .get()
                .getCaptionMap(TranslatableCaption.DEFAULT_NAMESPACE)
                .getCaptions();
        TextComponent.Builder information = Component.text();
        Component header = TranslatableCaption.of("debug.debug_header").toComponent(player)
                .append(Component.newline());
        String line = TranslatableCaption.of("debug.debug_line").getComponent(player) + "\n";
        String section = TranslatableCaption.of("debug.debug_section").getComponent(player) + "\n";
        information.append(header);
        information.append(MINI_MESSAGE.deserialize(
                section,
                TagResolver.resolver("val", Tag.inserting(Component.text("PlotArea")))
        ));
        information.append(MINI_MESSAGE
                .deserialize(
                        line,
                        TagResolver.builder()
                                .tag("var", Tag.inserting(Component.text("Plot Worlds")))
                                .tag(
                                        "val",
                                        Tag.inserting(Component.text(StringMan.join(
                                                this.plotAreaManager.getAllPlotAreas(),
                                                ", "
                                        )))
                                )
                                .build()
                ));
        information.append(
                MINI_MESSAGE.deserialize(
                        line,
                        TagResolver.builder()
                                .tag("var", Tag.inserting(Component.text("Owned Plots")))
                                .tag(
                                        "val",
                                        Tag.inserting(Component.text(PlotQuery.newQuery().allPlots().count()))
                                )
                                .build()
                ));
        information.append(MINI_MESSAGE.deserialize(
                section,
                TagResolver.resolver("val", Tag.inserting(Component.text("Messages")))
        ));
        information.append(MINI_MESSAGE.deserialize(
                line,
                TagResolver.builder()
                        .tag("var", Tag.inserting(Component.text("Total Messages")))
                        .tag(
                                "val",
                                Tag.inserting(Component.text(captions.size()))
                        )
                        .build()
        ));
        player.sendMessage(StaticCaption.of(MINI_MESSAGE.serialize(information.build())));
        return true;
    }

    @Override
    public Collection<Command> tab(final PlotPlayer<?> player, String[] args, boolean space) {
        return Stream.of("debug-players", "entitytypes")
                .filter(value -> value.startsWith(args[0].toLowerCase(Locale.ENGLISH)))
                .map(value -> new Command(null, false, value, "plots.admin", RequiredType.NONE, null) {
                }).collect(Collectors.toList());
    }

}
