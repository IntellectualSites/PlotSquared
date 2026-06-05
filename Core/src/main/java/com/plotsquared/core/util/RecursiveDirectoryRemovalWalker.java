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
