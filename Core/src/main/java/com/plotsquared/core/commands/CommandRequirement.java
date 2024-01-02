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
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.requirement.Requirement;
import org.incendo.cloud.requirement.Requirements;

import java.util.List;

/**
 * Something that is required for a command to be executed.
 */
public interface CommandRequirement extends Requirement<PlotPlayer<?>, CommandRequirement> {

    /**
     * The key used to store the requirements in the {@link cloud.commandframework.meta.CommandMeta}.
     */
    CloudKey<Requirements<PlotPlayer<?>, CommandRequirement>> REQUIREMENTS_KEY = CloudKey.of(
            "requirements",
            new TypeToken<Requirements<PlotPlayer<?>, CommandRequirement>>() {
            }
    );

    /**
     * Returns the caption sent when the requirement is not met.
     *
     * @return the caption
     */
    @NonNull TranslatableCaption failureCaption();

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
            public @NonNull List<@NonNull CommandRequirement> parents() {
                return thisRequirement.parents();
            }

            @Override
            public boolean evaluateRequirement(final @NonNull CommandContext<PlotPlayer<?>> context) {
                return context.sender().hasPermission(permission) || thisRequirement.evaluateRequirement(context);
            }
        };
    }
}
