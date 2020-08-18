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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.entity.EntityCategories;
import com.plotsquared.core.util.entity.EntityCategory;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.uuid.UUIDMapping;
import com.sk89q.worldedit.world.entity.EntityType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

@CommandDeclaration(command = "debug",
    category = CommandCategory.DEBUG,
    description = "Show debug information",
    usage = "/plot debug [msg]",
    permission = "plots.admin")
public class Debug extends SubCommand {

    private static final Logger logger = LoggerFactory.getLogger("P2/" + Debug.class.getSimpleName());

    private final PlotAreaManager plotAreaManager;
    private final WorldUtil worldUtil;

    @Inject public Debug(@Nonnull final PlotAreaManager plotAreaManager,
                         @Nonnull final WorldUtil worldUtil) {
        this.plotAreaManager = plotAreaManager;
        this.worldUtil = worldUtil;
    }

    @Override public boolean onCommand(PlotPlayer<?> player, String[] args) {
        if (args.length == 0 ) {
            player.sendMessage(TranslatableCaption.of("commandconfig.command_syntax"),
                    Template.of("value", "/plot debug <loadedchunks | debug-players | logging | entitytypes | msg>"));
        }
        if (args.length > 0) {
            if ("player".equalsIgnoreCase(args[0])) {
                for (Map.Entry<String, Object> meta : player.getMeta().entrySet()) {
                    player.sendMessage(StaticCaption.of("Key: " + meta.getKey() + " Value: " + meta.getValue().toString() + " , "));
                }
            }
        }
        if (args.length > 0 && "loadedchunks".equalsIgnoreCase(args[0])) {
            final long start = System.currentTimeMillis();
            player.sendMessage(TranslatableCaption.of("debug.fetching_loaded_chunks"));
            TaskManager.runTaskAsync(() -> player.sendMessage(StaticCaption
                .of("Loaded chunks: " + this.worldUtil.getChunkChunks(player.getLocation().getWorldName()).size() + " (" + (System.currentTimeMillis()
                    - start) + "ms) using thread: " + Thread.currentThread().getName())));
            return true;
        }
        if (args.length > 0 && "uuids".equalsIgnoreCase(args[0])) {
            final Collection<UUIDMapping> mappings = PlotSquared.get().getImpromptuUUIDPipeline().getAllImmediately();
            player.sendMessage(
                    TranslatableCaption.of("debug.cached_uuids"),
                    Template.of("value", String.valueOf(mappings.size()))
            );
            return true;
        }
        if (args.length > 0 && "debug-players".equalsIgnoreCase(args[0])) {
            player.sendMessage(TranslatableCaption.of("debug.player_in_debugmode"));
            for (final PlotPlayer<?> pp : PlotPlayer.getDebugModePlayers()) {
                player.sendMessage(
                        TranslatableCaption.of("debug.player_in_debugmode_list"),
                        Template.of("value", pp.getName())
                );
            }
            return true;
        }
        if (args.length > 0 && "logging".equalsIgnoreCase(args[0])) {
            logger.info("Info!");
            logger.warn("Warning!");
            logger.error("Error!", new RuntimeException());
            logger.debug("Debug!");
            return true;
        }
        if (args.length > 0 && "entitytypes".equalsIgnoreCase(args[0])) {
            EntityCategories.init();
            player.sendMessage(TranslatableCaption.of("debug.entity_categories"));
            EntityCategory.REGISTRY.forEach(category -> {
                final StringBuilder builder =
                    new StringBuilder("§7- §6").append(category.getId()).append("§7: §6");
                for (final EntityType entityType : category.getAll()) {
                    builder.append(entityType.getId()).append(" ");
                }
                player.sendMessage(StaticCaption.of("<prefix>" + builder.toString()));
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
        Set<TranslatableCaption> captions = PlotSquared.get().getCaptionMap(TranslatableCaption.DEFAULT_NAMESPACE).getCaptions().keySet();
        if ((args.length > 0) && args[0].equalsIgnoreCase("msg")) {
            StringBuilder msg = new StringBuilder();
            for (Caption caption : captions) {
                msg.append(caption.getComponent(ConsolePlayer.getConsole())).append("\n");
            }
            player.sendMessage(StaticCaption.of(msg.toString()));
            return true;
        }
        TextComponent.Builder information = TextComponent.builder();
        Component header = MINI_MESSAGE.parse(TranslatableCaption.of("debug.debug_header").getComponent(player) + "\n");
        String line = TranslatableCaption.of("debug.debug_line").getComponent(player) + "\n";
        String section = TranslatableCaption.of("debug.debug_section").getComponent(player) + "\n";
        information.append(header);
        information.append(MINI_MESSAGE.parse(section, Template.of("val", "PlotArea")));
        information.append(MINI_MESSAGE
            .parse(line, Template.of("var", "Plot Worlds"), Template.of("val", StringMan.join(this.plotAreaManager.getAllPlotAreas(), ", "))));
        information.append(
            MINI_MESSAGE.parse(line, Template.of("var", "Owned Plots"), Template.of("val", String.valueOf(PlotQuery.newQuery().allPlots().count()))));
        information.append(MINI_MESSAGE.parse(section, Template.of("val", "Messages")));
        information.append(MINI_MESSAGE.parse(line, Template.of("var", "Total Messages"), Template.of("val", String.valueOf(captions.size()))));
        information.append(MINI_MESSAGE.parse(line, Template.of("var", "View all captions"), Template.of("val", "/plot debug msg")));
        player.sendMessage(StaticCaption.of(information.toString()));
        return true;
    }
}
