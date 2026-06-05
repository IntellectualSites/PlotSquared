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
package com.plotsquared.core.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveDirectoryRemovalWalker extends SimpleFileVisitor<Path> {

    public static final RecursiveDirectoryRemovalWalker INSTANCE = new RecursiveDirectoryRemovalWalker();

    private RecursiveDirectoryRemovalWalker() {
    }

    @Override
    public @NotNull FileVisitResult postVisitDirectory(@NotNull final Path dir, @Nullable final IOException exc) throws
            IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public @NotNull FileVisitResult visitFile(@NotNull final Path file, @NotNull final BasicFileAttributes attrs) throws
            IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
    }

}
