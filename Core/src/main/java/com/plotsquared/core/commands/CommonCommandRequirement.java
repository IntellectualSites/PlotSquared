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
package com.plotsquared.core.commands;

import cloud.commandframework.context.CommandContext;
import cloud.commandframework.keys.CloudKey;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Predicate;

/**
 * Common {@link CommandRequirement command requirements}.
 */
public enum CommonCommandRequirement implements CommandRequirement {
    /**
     * Requires that the command sender is currently in a plot.
     */
    REQUIRES_PLOT(TranslatableCaption.of("errors.not_in_plot"), ctx -> ctx.sender().getCurrentPlot() != null),
    /**
     * Requires that the command sender is in a claimed plot.
     */
    REQUIRES_OWNER(TranslatableCaption.of("working.plot_not_claimed"),
            ctx -> ctx.sender().getCurrentPlot() != null && ctx.sender().getCurrentPlot().hasOwner()
    );

    private final TranslatableCaption failureCaption;
    private final Predicate<CommandContext<PlotPlayer<?>>> predicate;

    CommonCommandRequirement(
            final @NonNull TranslatableCaption failureCaption,
            final @NonNull Predicate<CommandContext<PlotPlayer<?>>> predicate
    ) {
        this.failureCaption = failureCaption;
        this.predicate = predicate;
    }

    public @NonNull TranslatableCaption failureCaption() {
        return this.failureCaption;
    }

    @Override
    public boolean evaluate(final @NonNull CommandContext<PlotPlayer<?>> context) {
        return this.predicate.test(context);
    }

    @Override
    public @NonNull CloudKey<Boolean> key() {
        return CloudKey.of(String.format("requirement_%s", this.name()), Boolean.class);
    }
}
