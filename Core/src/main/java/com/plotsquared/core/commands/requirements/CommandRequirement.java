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

import cloud.commandframework.meta.CommandMeta;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public enum CommandRequirement {
    PLAYER(
            "",
            player -> !(player instanceof ConsolePlayer)
    ),
    IN_PLOT(
            "errors.not_in_plot",
            player -> player.getCurrentPlot() != null
    ),
    PLOT_HAS_OWNER(
            "info.plot_unowned",
            player -> Objects.requireNonNull(player.getCurrentPlot()).hasOwner(),
            IN_PLOT
    ),
    IS_OWNER(
            "permission.no_plot_perms",
            player -> Objects.requireNonNull(player.getCurrentPlot()).isOwner(player.getUUID()),
            PLOT_HAS_OWNER
    );

    public static final CommandMeta.Key<List<CommandRequirement>> COMMAND_REQUIREMENTS_KEY = CommandMeta.Key.of(
            new TypeToken<List<CommandRequirement>>() {
            },
            "command_requirements"
    );

    private final @NonNull Caption caption;
    private final @NonNull Predicate<@NonNull PlotPlayer<?>> requirementPredicate;
    private final @NonNull Set<@NonNull CommandRequirement> inheritedRequirements;

    CommandRequirement(
            final @NonNull String caption,
            final @NonNull Predicate<@NonNull PlotPlayer<?>> requirementPredicate,
            final @NonNull CommandRequirement... inheritedRequirements
    ) {
        this.caption = TranslatableCaption.of(caption);
        this.requirementPredicate = requirementPredicate;
        this.inheritedRequirements = EnumSet.copyOf(Arrays.asList(inheritedRequirements));
    }

    public @NonNull Set<@NonNull CommandRequirement> inheritedRequirements() {
        return Collections.unmodifiableSet(this.inheritedRequirements);
    }

    public @NonNull Caption caption() {
        return this.caption;
    }

    public boolean checkRequirement(final @NonNull PlotPlayer<?> player) {
        return this.requirementPredicate.test(player);
    }
}
