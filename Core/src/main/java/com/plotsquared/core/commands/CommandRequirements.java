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

import cloud.commandframework.keys.CloudKey;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Holder of {@link CommandRequirement} requirements.
 */
public final class CommandRequirements implements Iterable<@NonNull CommandRequirement> {

    /**
     * The key used to store the requirements in the {@link cloud.commandframework.meta.CommandMeta}.
     */
    public static final CloudKey<CommandRequirements> REQUIREMENTS_KEY = CloudKey.of(
            "requirements",
            CommandRequirements.class
    );

    /**
     * Creates a new instance.
     *
     * @param requirements the requirements
     * @return the instance
     */
    public static @NonNull CommandRequirements create(final @NonNull Collection<@NonNull CommandRequirement> requirements) {
        return new CommandRequirements(requirements);
    }

    private final List<CommandRequirement> requirements;

    private CommandRequirements(final @NonNull Collection<@NonNull CommandRequirement> requirements) {
        this.requirements = List.copyOf(requirements);
    }

    @Override
    public @NonNull Iterator<@NonNull CommandRequirement> iterator() {
        return this.requirements.iterator();
    }
}
