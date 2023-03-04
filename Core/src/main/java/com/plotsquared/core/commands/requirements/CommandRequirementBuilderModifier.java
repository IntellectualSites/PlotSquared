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
import com.plotsquared.core.player.PlotPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public class CommandRequirementBuilderModifier implements BiFunction<
        @NonNull Requirements,
        Command.@NonNull Builder<PlotPlayer<?>>,
        Command.@NonNull Builder<PlotPlayer<?>>
> {

    @Override
    public Command.@NonNull Builder<PlotPlayer<?>> apply(
            final @NonNull Requirements requirements,
            final Command.@NonNull Builder<PlotPlayer<?>> builder
    ) {
        // We use a list, because we want them to be evaluated in order.
        final Set<CommandRequirement> commandRequirements = EnumSet.noneOf(CommandRequirement.class);
        for (final Requirement requirement : requirements.value()) {
            this.addRequirements(commandRequirements, requirement.value());
        }
        // We then sort the requirements.
        final List<CommandRequirement> sortedRequirements = commandRequirements.stream().sorted().toList();
        // We then register all the types in the command metadata.
        return builder.meta(CommandRequirement.COMMAND_REQUIREMENTS_KEY, sortedRequirements);
    }

    private void addRequirements(
            final @NonNull Set<@NonNull CommandRequirement> requirements,
            final @NonNull CommandRequirement requirement
    ) {
        for (final CommandRequirement inheritedRequirement : requirement.inheritedRequirements()) {
            addRequirements(requirements, inheritedRequirement);
        }
        requirements.add(requirement);
    }
}
