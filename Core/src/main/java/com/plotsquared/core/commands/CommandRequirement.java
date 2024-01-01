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
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Something that is required for a command to be executed.
 */
public interface CommandRequirement {

    /**
     * Returns the caption sent when the requirement is not met.
     *
     * @return the caption
     */
    @NonNull TranslatableCaption failureCaption();

    /**
     * Evaluates whether the requirement is met.
     *
     * @param context command context to evaluate
     * @return {@code true} if the requirement is met, else {@code false}
     */
    boolean evaluate(final @NonNull CommandContext<PlotPlayer<?>> context);

    /**
     * Returns the placeholder values.
     *
     * @return placeholder values
     */
    default @NonNull TagResolver @NonNull[] tagResolvers() {
        return new TagResolver[0];
    }

    /**
     * Returns a requirement that evaluates to {@code true} if the sender has the given {@code permission} or if
     * this requirement evaluates to {@code true}.
     *
     * @param permission the override permission
     * @return the new requirement
     */
    default @NonNull CommandRequirement withPermissionOverride(final @NonNull Permission permission) {
        final CommandRequirement thisRequirement = this;
        return new CommandRequirement() {
            @Override
            public @NonNull TranslatableCaption failureCaption() {
                return TranslatableCaption.of("permission.no_permission");
            }

            @Override
            public @NonNull TagResolver @NonNull [] tagResolvers() {
                return new TagResolver[] {
                        TagResolver.resolver("node", Tag.inserting(Permission.PERMISSION_SET_FLAG_OTHER))
                };
            }

            @Override
            public boolean evaluate(final @NonNull CommandContext<PlotPlayer<?>> context) {
                return context.sender().hasPermission(permission) || thisRequirement.evaluate(context);
            }
        };
    }
}
