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
package com.plotsquared.core.commands.processing;

import cloud.commandframework.execution.postprocessor.CommandPostprocessingContext;
import cloud.commandframework.execution.postprocessor.CommandPostprocessor;
import cloud.commandframework.services.types.ConsumerService;
import com.plotsquared.core.commands.CommandRequirement;
import com.plotsquared.core.commands.CommandRequirements;
import com.plotsquared.core.player.PlotPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Processor that evaluates registered {@link CommandRequirement command requirements} before a command is executed.
 */
public final class CommandRequirementPostprocessor implements CommandPostprocessor<PlotPlayer<?>> {

    @Override
    public void accept(final @NonNull CommandPostprocessingContext<PlotPlayer<?>> processingContext) {
        final CommandRequirements requirements = processingContext.command().commandMeta().getOrDefault(
                CommandRequirements.REQUIREMENTS_KEY,
                null
        );
        if (requirements == null) {
            return;
        }
        for (final CommandRequirement requirement : requirements) {
            if (requirement.evaluate(processingContext.commandContext())) {
                continue;
            }
            processingContext.commandContext().sender().sendMessage(requirement.failureCaption(), requirement.tagResolvers());
            // Not allowed :(
            ConsumerService.interrupt();
        }
    }
}
