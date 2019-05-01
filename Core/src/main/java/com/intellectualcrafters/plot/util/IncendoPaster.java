package com.intellectualcrafters.plot.util;

import com.google.common.base.Charsets;
import com.intellectualcrafters.plot.PS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Single class paster for the Incendo paste service
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class IncendoPaster {

    /**
     * Upload service URL
     */
    public static final String UPLOAD_PATH = "https://www.athion.net/ISPaster/paste/upload";
    /**
     * Valid paste applications
     */
    public static final Collection<String>
        VALID_APPLICATIONS = Arrays
        .asList("plotsquared", "fastasyncworldedit", "incendopermissions", "kvantum");

    private final Collection<PasteFile> files = new ArrayList<>();
    private final String pasteApplication;

    /**
     * Construct a new paster
     *
     * @param pasteApplication The application that is sending the paste
     */
    public IncendoPaster(final String pasteApplication) {
        if (pasteApplication == null || pasteApplication.isEmpty()) {
            throw new IllegalArgumentException("paste application cannot be null, nor empty");
        }
        if (!VALID_APPLICATIONS.contains(pasteApplication.toLowerCase(Locale.ENGLISH))) {
            throw new IllegalArgumentException(String.format("Unknown application name: %s", pasteApplication));
        }
        this.pasteApplication = pasteApplication;
    }

    /**
     * Get an immutable collection containing all the files that have been added to this paster
     *
     * @return Unmodifiable collection
     */
    public final Collection<PasteFile> getFiles() {
        return Collections.unmodifiableCollection(this.files);
    }

    /**
     * Add a file to the paster
     *
     * @param file File to paste
     */
    public void addFile(final PasteFile file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        // Check to see that no duplicate files are submitted
        for (final PasteFile pasteFile : this.files) {
            if (pasteFile.fileName.equalsIgnoreCase(file.getFileName())) {
                throw new IllegalArgumentException(String.format("Found duplicate file with name %s",
                    file.getFileName()));
            }
        }
        this.files.add(file);
    }

    /**
     * Create a JSON string from the submitted information
     *
     * @return compiled JSON string
     */
    private String toJsonString() {
        final StringBuilder builder = new StringBuilder("{\n");
        builder.append("\"paste_application\": \"").append(this.pasteApplication).append("\",\n\"files\": \"");
        Iterator<PasteFile> fileIterator = this.files.iterator();
        while (fileIterator.hasNext()) {
            final PasteFile file = fileIterator.next();
            builder.append(file.getFileName());
            if (fileIterator.hasNext()) {
                builder.append(",");
            }
        }
        builder.append("\",\n");
        fileIterator = this.files.iterator();
        while (fileIterator.hasNext()) {
            final PasteFile file = fileIterator.next();
            builder.append("\"file-").append(file.getFileName()).append("\": \"")
                .append(file.getContent().replaceAll("\"", "\\\\\"")).append("\"");
            if (fileIterator.hasNext()) {
                builder.append(",\n");
            }
        }
        builder.append("\n}");
        return builder.toString();
    }

    /**
     * Upload the paste and return the status message
     *
     * @return Status message
     * @throws Throwable any and all exceptions
     */
    public final String upload() throws Throwable {
        final URL url = new URL(UPLOAD_PATH);
        final URLConnection connection = url.openConnection();
        final HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);
        final byte[] content = toJsonString().getBytes(Charsets.UTF_8);
        httpURLConnection.setFixedLengthStreamingMode(content.length);
        httpURLConnection.setRequestProperty("Content-Type", "application/json");
        httpURLConnection.setRequestProperty("Accept", "*/*");
        httpURLConnection.connect();
        try (final OutputStream stream = httpURLConnection.getOutputStream()) {
            stream.write(content);
        }
        if (!httpURLConnection.getResponseMessage().contains("OK")) {
            if (httpURLConnection.getResponseCode() == 413) {
                final long size = content.length;
                PS.debug(String.format("Paste Too Big > Size: %dMB", size / 1_000_000));
            }
            throw new IllegalStateException(String.format("Server returned status: %d %s",
                httpURLConnection.getResponseCode(), httpURLConnection.getResponseMessage()));
        }
        final StringBuilder input = new StringBuilder();
        try (final BufferedReader inputStream = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()))) {
            String line;
            while ((line = inputStream.readLine()) != null) {
                input.append(line).append("\n");
            }
        }
        return input.toString();
    }

    /**
     * Simple class that represents a paste file
     */
    public static class PasteFile {

        private final String fileName;
        private final String content;

        /**
         * Construct a new paste file
         *
         * @param fileName File name, cannot be empty, nor null
         * @param content File content, cannot be empty, nor null
         */
        public PasteFile(final String fileName, final String content) {
            if (fileName == null || fileName.isEmpty()) {
                throw new IllegalArgumentException("file name cannot be null, nor empty");
            }
            if (content == null || content.isEmpty()) {
                throw new IllegalArgumentException("content cannot be null, nor empty");
            }
            this.fileName = fileName;
            this.content = content;
        }

        /**
         * Get the file name
         *
         * @return File name
         */
        public String getFileName() {
            return this.fileName;
        }

        /**
         * Get the file content as a single string
         *
         * @return File content
         */
        public String getContent() {
            return this.content;
        }
    }

}
