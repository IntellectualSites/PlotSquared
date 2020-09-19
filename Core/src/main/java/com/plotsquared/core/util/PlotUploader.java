/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.util;

import com.google.inject.Inject;
import com.intellectualsites.arkitektonika.Arkitektonika;
import com.intellectualsites.arkitektonika.SchematicKeys;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.plot.Plot;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.zip.GZIPOutputStream;

public class PlotUploader {
    private static final Logger logger = LoggerFactory.getLogger("P2/" + PlotUploader.class.getSimpleName());
    private static final Path TEMP_DIR = Paths.get(PlotSquared.platform().getDirectory().getPath());
    private final SchematicHandler schematicHandler;
    private final Arkitektonika arkitektonika;

    @Inject
    public PlotUploader(@Nonnull final SchematicHandler schematicHandler) {
        this.schematicHandler = schematicHandler;
        this.arkitektonika = Arkitektonika.builder().withUrl(Settings.Arkitektonika.BACKEND_URL).build();
    }

    public CompletableFuture<PlotUploadResult> upload(@Nonnull final Plot plot) {
        return this.schematicHandler.getCompoundTag(plot)
                .handle((tag, t) -> {
                    plot.removeRunning();
                    return tag;
                })
                .thenApply(this::writeToTempFile)
                .thenApply(this::uploadAndDelete)
                .thenApply(this::wrapIntoResult);
    }

    private PlotUploadResult wrapIntoResult(SchematicKeys schematicKeys) {
        if (schematicKeys == null) {
            return PlotUploadResult.failed();
        }
        String download = Settings.Arkitektonika.DOWNLOAD_URL.replace("{key}", schematicKeys.getAccessKey());
        String delete = Settings.Arkitektonika.DELETE_URL.replace("{key}", schematicKeys.getAccessKey());
        return PlotUploadResult.success(download, delete);
    }

    @Nullable
    private SchematicKeys uploadAndDelete(@Nonnull final Path file) {
        try {
            final CompletableFuture<SchematicKeys> upload = this.arkitektonika.upload(file.toFile());
            return upload.join();
        } catch (CompletionException e) {
            logger.error("Failed to upload schematic", e);
            return null;
        } finally {
            try {
                Files.delete(file);
            } catch (IOException e) {
                logger.error("Failed to delete temporary file {}", file, e);
            }
        }
    }

    private Path writeToTempFile(@Nonnull final CompoundTag schematic) {
        try {
            final Path tempFile = Files.createTempFile(TEMP_DIR, null, null);
            try (final OutputStream stream = Files.newOutputStream(tempFile)) {
                writeSchematic(schematic, stream);
            }
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes a schematic provided as CompoundTag to an OutputStream.
     *
     * @param schematic The schematic to write to the stream
     * @param stream    The stream to write the schematic to
     * @throws IOException if an I/O error occurred
     */
    private void writeSchematic(@Nonnull final CompoundTag schematic, @Nonnull final OutputStream stream)
            throws IOException {
        try (final NBTOutputStream nbtOutputStream = new NBTOutputStream(new GZIPOutputStream(stream))) {
            nbtOutputStream.writeNamedTag("Schematic", schematic);
        }
    }

    public static class PlotUploadResult {
        private final boolean success;
        private final String downloadUrl;
        private final String deletionUrl;

        private PlotUploadResult(boolean success, @Nullable final String downloadUrl,
                                 @Nullable final String deletionUrl) {
            this.success = success;
            this.downloadUrl = downloadUrl;
            this.deletionUrl = deletionUrl;
        }

        public static PlotUploadResult success(@Nonnull final String downloadUrl, @Nullable final String deletionUrl) {
            return new PlotUploadResult(true, downloadUrl, deletionUrl);
        }

        public static PlotUploadResult failed() {
            return new PlotUploadResult(false, null, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getDownloadUrl() {
            return downloadUrl;
        }

        public String getDeletionUrl() {
            return deletionUrl;
        }
    }
}
