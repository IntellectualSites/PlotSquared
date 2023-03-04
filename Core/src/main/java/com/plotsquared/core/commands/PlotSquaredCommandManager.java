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
import com.google.inject.Injector;
import com.plotsquared.core.commands.parsers.PlotMemberParser;
import com.plotsquared.core.player.PlotPlayer;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.stream.Stream;

public class PlotSquaredCommandManager {

    private final Injector injector;
    private final CommandManager<PlotPlayer<?>> commandManager;
    private final AnnotationParser<PlotPlayer<?>> annotationParser;

    @Inject
    public PlotSquaredCommandManager(
            final @NonNull Injector injector,
            final @NonNull CommandManager<PlotPlayer<?>> commandManager
    ) {
        this.injector = injector;
        this.commandManager = commandManager;
        this.annotationParser = new AnnotationParser<PlotPlayer<?>>(
                this.commandManager,
                new TypeToken<PlotPlayer<?>>() {
                },
                parameters -> SimpleCommandMeta.empty()
        );
    }

    /**
     * Scans the given {@code instance} for commands, and registers them.
     *
     * @param instance the instance to scan
     */
    public void scanClass(final @NonNull Object instance) {
        this.annotationParser.parse(instance);
    }

    /**
     * Initializes all the known commands.
     */
    public void initializeCommands() {
        // We start by scanning the parsers.
        Stream.of(
                PlotMemberParser.class
        ).map(this.injector::getInstance).forEach(this::scanClass);
        // Then we scan the commands.
        Stream.of(
                CommandAdd.class
        ).map(this.injector::getInstance).forEach(this::scanClass);
    }
}
