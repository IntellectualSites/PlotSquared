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

import cloud.commandframework.CommandManager;
import cloud.commandframework.annotations.AnnotationParser;
import cloud.commandframework.meta.SimpleCommandMeta;
import com.google.inject.Inject;
import com.plotsquared.core.player.PlotPlayer;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PlotSquaredCommandManager {

    private final CommandManager<PlotPlayer<?>> commandManager;
    private final AnnotationParser<PlotPlayer<?>> annotationParser;

    @Inject
    public PlotSquaredCommandManager(
            final @NonNull CommandManager<PlotPlayer<?>> commandManager
    ) {
        this.commandManager = commandManager;
        this.annotationParser = new AnnotationParser<PlotPlayer<?>>(
                this.commandManager,
                new TypeToken<PlotPlayer<?>>() {
                },
                parameters -> SimpleCommandMeta.empty()
        );
    }

    /**
     * Scans the given {@link Class class} for commands, and registers them.
     *
     * @param clazz the class to scan.
     */
    public void scanClass(final @NonNull Class<?> clazz) {
        this.annotationParser.parse(clazz);
    }
}
