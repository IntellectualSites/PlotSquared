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
package com.plotsquared.core.commands.requirements;

import cloud.commandframework.Command;
import cloud.commandframework.execution.postprocessor.CommandPostprocessingContext;
import cloud.commandframework.execution.postprocessor.CommandPostprocessor;
import cloud.commandframework.services.types.ConsumerService;
import com.plotsquared.core.player.PlotPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandRequirementPostProcessor implements CommandPostprocessor<PlotPlayer<?>> {

    @Override
    public void accept(
            @NonNull final CommandPostprocessingContext<PlotPlayer<?>> context
    ) {
        final Command<PlotPlayer<?>> command = context.getCommand();
        final PlotPlayer<?> player = context.getCommandContext().getSender();;
        final List<CommandRequirement> commandRequirements = command.getCommandMeta().get(
                CommandRequirement.COMMAND_REQUIREMENTS_KEY
        ).orElse(List.of());

        for (final CommandRequirement requirement : commandRequirements) {
            if (requirement.checkRequirement(player)) {
                continue;
            }

            // They failed the requirement =(
            player.sendMessage(requirement.caption());

            // Then we interrupt to make sure the command isn't executed.
            ConsumerService.interrupt();
        }
    }
}
