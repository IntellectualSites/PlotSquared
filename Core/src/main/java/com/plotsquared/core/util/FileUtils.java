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

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileUtils {

    private FileUtils() {
    }

    /**
     * Attempt to (recursively) delete a directory
     *
     * @param directory Directory to delete
     * @throws RuntimeException If the deletion fails
     */
    public static void deleteDirectory(final @NonNull File directory) {
        if (directory.exists()) {
            final File[] files = directory.listFiles();
            if (null != files) {
                for (final File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    }
                }
            }
        }
        if (!directory.delete()) {
            throw new RuntimeException(
                    String.format("Failed to delete directory %s", directory.getName()));
        }
    }

    public static @NonNull File getFile(final @NonNull File base, final @NonNull String path) {
        if (Paths.get(path).isAbsolute()) {
            return new File(path);
        }
        return new File(base, path);
    }

    /**
     * Check whether a file is located inside a directory, after resolving {@code .} and {@code ..} segments.
     *
     * @param directory the directory that must contain the file
     * @param file      the file to check
     * @return {@code true} if the file is inside the directory
     * @since 7.6.2
     */
    public static boolean isInsideDirectory(final @NonNull File directory, final @NonNull File file) {
        final Path parent = directory.toPath().toAbsolutePath().normalize();
        final Path child = file.toPath().toAbsolutePath().normalize();
        return !child.equals(parent) && child.startsWith(parent);
    }

}
