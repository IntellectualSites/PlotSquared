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
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.InternalFlag;
import com.plotsquared.core.plot.flag.PlotFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class FlagListCommand extends FlagCommandBean {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();

    @Override
    protected Command.@NonNull Builder<PlotPlayer<?>> configurePlotCommand(
            final Command.@NonNull Builder<PlotPlayer<?>> builder
    ) {
        return builder.literal("list");
    }

    @Override
    public void execute(final @NonNull CommandContext<PlotPlayer<?>> commandContext) {
        final PlotPlayer<?> player = commandContext.sender();

        final Map<Component, ArrayList<String>> flags = new HashMap<>();
        for (PlotFlag<?, ?> plotFlag : GlobalFlagContainer.getInstance().getRecognizedPlotFlags()) {
            if (plotFlag instanceof InternalFlag) {
                continue;
            }
            final Component category = plotFlag.getFlagCategory().toComponent(player);
            final Collection<String> flagList = flags.computeIfAbsent(category, k -> new ArrayList<>());
            flagList.add(plotFlag.getName());
        }

        for (final Map.Entry<Component, ArrayList<String>> entry : flags.entrySet()) {
            Collections.sort(entry.getValue());
            Component category =
                    MINI_MESSAGE.deserialize(
                            TranslatableCaption.of("flag.flag_list_categories").getComponent(player),
                            TagResolver.resolver("category", Tag.inserting(entry.getKey().style(Style.empty())))
                    );
            TextComponent.Builder builder = Component.text().append(category);
            final Iterator<String> flagIterator = entry.getValue().iterator();
            while (flagIterator.hasNext()) {
                final String flag = flagIterator.next();
                builder.append(MINI_MESSAGE
                        .deserialize(
                                TranslatableCaption.of("flag.flag_list_flag").getComponent(player),
                                TagResolver.builder()
                                        .tag("command", Tag.preProcessParsed("/plat flag info " + flag))
                                        .tag("flag", Tag.inserting(Component.text(flag)))
                                        .tag("suffix", Tag.inserting(Component.text(flagIterator.hasNext() ? ", " : "")))
                                        .build()
                        ));
            }
            player.sendMessage(StaticCaption.of(MINI_MESSAGE.serialize(builder.build())));
        }
    }
}
