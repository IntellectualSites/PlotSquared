package com.plotsquared.core.util;

import com.google.inject.Inject;
import com.intellectualsites.arkitektonika.Arkitektonika;
import com.intellectualsites.arkitektonika.SchematicKeys;
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
    private static final Path TEMP_DIR = Paths.get("."); // TODO Path
    private final SchematicHandler schematicHandler;
    private final Arkitektonika arkitektonika;
    private final String baseUrl;

    @Inject
    public PlotUploader(@Nonnull final SchematicHandler schematicHandler) {
        this.schematicHandler = schematicHandler;
        this.baseUrl = Settings.Web.URL;
        this.arkitektonika = Arkitektonika.builder().withUrl(Settings.Web.URL).build();
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
        // TODO proper urls
        return PlotUploadResult.success(baseUrl + "download/" + schematicKeys.getAccessKey(),
                baseUrl + "delete/" + schematicKeys.getDeletionKey());
    }

    @Nullable
    private SchematicKeys uploadAndDelete(@Nonnull final Path file) {
        try {
            final CompletableFuture<SchematicKeys> upload = this.arkitektonika.upload(file.toFile());
            final SchematicKeys keys = upload.join();
            Files.delete(file);
            return keys;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CompletionException e) {
            return null;
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
