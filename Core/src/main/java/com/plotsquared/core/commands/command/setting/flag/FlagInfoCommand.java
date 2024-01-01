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
package com.plotsquared.core.commands.command.setting.flag;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.keys.CloudKey;
import com.plotsquared.core.commands.parser.PlotFlagParser;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.flag.PlotFlag;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import static com.plotsquared.core.commands.parser.PlotFlagParser.plotFlagParser;

public final class FlagInfoCommand extends FlagCommandBean {

    private static final CloudKey<PlotFlag<?, ?>> COMPONENT_FLAG = CloudKey.of("flag", new TypeToken<PlotFlag<?, ?>>() {});

    @Override
    protected Command.@NonNull Builder<PlotPlayer<?>> configurePlotCommand(final Command.@NonNull Builder<PlotPlayer<?>> builder) {
        return builder.literal("info")
                .required(COMPONENT_FLAG, plotFlagParser(PlotFlagParser.FlagSource.GLOBAL));
    }

    @Override
    public void execute(final @NonNull CommandContext<PlotPlayer<?>> commandContext) {
        final PlotFlag<?, ?> plotFlag = commandContext.get(COMPONENT_FLAG);
        final PlotPlayer<?> player = commandContext.sender();

        player.sendMessage(TranslatableCaption.of("flag.flag_info_header"));
        // Flag name
        player.sendMessage(
                TranslatableCaption.of("flag.flag_info_name"),
                TagResolver.resolver("flag", Tag.inserting(Component.text(plotFlag.getName())))
        );
        // Flag category
        player.sendMessage(
                TranslatableCaption.of("flag.flag_info_category"),
                TagResolver.resolver(
                        "value",
                        Tag.inserting(plotFlag.getFlagCategory().toComponent(player))
                )
        );
        // Flag description
        // TODO maybe merge and \n instead?
        player.sendMessage(TranslatableCaption.of("flag.flag_info_description"));
        player.sendMessage(plotFlag.getFlagDescription());
        // Flag example
        player.sendMessage(
                TranslatableCaption.of("flag.flag_info_example"),
                TagResolver.builder()
                        .tag("command", Tag.preProcessParsed("/plot flag set"))
                        .tag("flag", Tag.preProcessParsed(plotFlag.getName()))
                        .tag("value", Tag.preProcessParsed(plotFlag.getExample()))
                        .build()
        );
        // Default value
        final String defaultValue = player.getLocation().getPlotArea().getFlagContainer()
                .getFlagErased(plotFlag.getClass()).toString();
        player.sendMessage(
                TranslatableCaption.of("flag.flag_info_default_value"),
                TagResolver.resolver("value", Tag.inserting(Component.text(defaultValue)))
        );
        // Footer. Done this way to prevent the duplicate-message-thingy from catching it
        player.sendMessage(TranslatableCaption.of("flag.flag_info_footer"));
    }
}
