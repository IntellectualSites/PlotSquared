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
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotFlagAddEvent;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.implementations.DescriptionFlag;
import com.plotsquared.core.util.EventDispatcher;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

@CommandDeclaration(command = "setdescription",
        permission = "plots.set.desc",
        usage = "/plot desc <description>",
        aliases = {"desc", "setdesc", "setd", "description"},
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.PLAYER)
public class Desc extends SetCommand {

    private final EventDispatcher eventDispatcher;

    @Inject
    public Desc(final @NonNull EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public boolean set(PlotPlayer<?> player, Plot plot, String desc) {
        if (desc.isEmpty()) {
            PlotFlagRemoveEvent event = this.eventDispatcher.callFlagRemove(plot
                    .getFlagContainer()
                    .getFlag(DescriptionFlag.class), plot);
            if (event.getEventResult() == Result.DENY) {
                player.sendMessage(
                        TranslatableCaption.of("events.event_denied"),
                        TagResolver.resolver("value", Tag.inserting(Component.text("Description removal")))
                );
                return false;
            }
            plot.removeFlag(event.getFlag());
            player.sendMessage(TranslatableCaption.of("desc.desc_unset"));
            return true;
        }
        PlotFlagAddEvent event = this.eventDispatcher.callFlagAdd(plot
                .getFlagContainer()
                .getFlag(DescriptionFlag.class)
                .createFlagInstance(desc), plot);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    TagResolver.resolver("value", Tag.inserting(Component.text("Description set")))
            );
            return false;
        }
        boolean result = plot.setFlag(event.getFlag());
        if (!result) {
            player.sendMessage(TranslatableCaption.of("flag.flag_not_added"));
            return false;
        }
        player.sendMessage(TranslatableCaption.of("desc.desc_set"));
        return true;
    }

}
