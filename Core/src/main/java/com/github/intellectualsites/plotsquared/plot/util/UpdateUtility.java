package com.github.intellectualsites.plotsquared.plot.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.incendo.jenkins.Jenkins;
import org.incendo.jenkins.objects.ArtifactDescription;
import org.incendo.jenkins.objects.BuildInfo;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateUtility {

    private final String jobName;
    private final Pattern artifactPattern;

    private final Jenkins jenkins;

    public UpdateUtility(@NonNull final String jenkinsPath, @NonNull final String jobName,
        @NonNull final String artifactPattern) {
        this.jobName = jobName;
        this.artifactPattern = Pattern.compile(artifactPattern);
        this.jenkins = Jenkins.newBuilder().withPath(jenkinsPath).build();
    }

    private void fetchLatestBuildInfo(final BiConsumer<BuildInfo, Throwable> whenDone) {
        this.jenkins.getJobInfo(jobName).whenCompleteAsync((jobInfo, exception) -> {
           if (jobInfo == null && exception != null) {
               whenDone.accept(null, exception);
           } else if (jobInfo != null) {
               jobInfo.getLastSuccessfulBuild().getBuildInfo()
                   .whenComplete(whenDone);
           } else {
               whenDone.accept(null, new IllegalStateException(
                   String.format("Could not fetch job info for job %s", this.jobName)));
           }
        });
    }

    private void getMatchingArtifact(final BiConsumer<ArtifactDescription, Throwable> whenDone) {
        this.fetchLatestBuildInfo((buildInfo, throwable) -> {
            if (throwable != null) {
                whenDone.accept(null, throwable);
            } else {
                final Collection<ArtifactDescription> artifacts = buildInfo.getArtifacts();
                final Optional<ArtifactDescription> artifact = artifacts.stream().filter(artifactDescription -> {
                    final String name = artifactDescription.getFileName();
                    final Matcher matcher = artifactPattern.matcher(name);
                    return matcher.matches();
                }).findAny();
                if (artifact.isPresent()) {
                    final ArtifactDescription artifactDescription = artifact.get();
                    whenDone.accept(artifactDescription, null);
                } else {
                    whenDone.accept(null,
                        new NullPointerException(String.format("Could not find any matching artifacts in build %d", buildInfo.getId())));
                }
            }
        });
    }

    public void checkForUpdate(final String currentVersion,
        final BiConsumer<UpdateDescription, Throwable> whenDone) {
        this.getMatchingArtifact(((artifactDescription, throwable) -> {
            if (throwable != null) {
                whenDone.accept(null, new RuntimeException(
                    String.format("Failed to read artifact description: %s", throwable.getMessage()), throwable));
            } else {
                try {
                    final String version = this.isNewer(currentVersion, artifactDescription);
                    if (version != null) {
                        whenDone.accept(new UpdateDescription(version, artifactDescription.getUrl()),
                            null);
                    } else {
                        whenDone.accept(null, null);
                    }
                } catch (final Throwable exception) {
                    whenDone.accept(null,
                        new RuntimeException(String.format("Failed to compare versions: %s",
                            exception.getMessage()), exception));
                }
            }
        }));
    }

    private String isNewer(@NonNull final String currentVersion, @NonNull final ArtifactDescription artifact) {
        final Matcher matcher = artifactPattern.matcher(artifact.getFileName());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Artifact file name does not match artifact pattern");
        }
        final String version = matcher.group("version");
        if (version == null) {
            throw new IllegalArgumentException("Given artifact does not contain version");
        }
        return compareVersions(currentVersion, version) < 0 ? version : null;
    }

    /**
     * Compare two given versions in the format $major.$minor
     * @param oldVersion current version
     * @param newVersion other version
     * @return -1 if the current version is older, 1 is the versions are the same,
     *         and 1 if the current version is newer
     */
    private int compareVersions(@NonNull final String oldVersion, @NonNull final String newVersion) {
        // Versions look this this: major.minor :P
        final int[] oldNums = splitVersion(oldVersion);
        final int[] newNums = splitVersion(newVersion);

        if (oldNums == null || newNums == null) {
            throw new IllegalArgumentException("Could not extract version data");
        }

        // Compare major version
        if (oldNums[0] != -1 && newNums[0] != -1) {
            if (oldNums[0] < newNums[0]) {
                return -1;
            } else if (oldNums[0] > newNums[0]) {
                return 1;
            }
        }

        // Compare minor versions
        return Integer.compare(oldNums[1], newNums[1]);
    }

    private int[] splitVersion(@NonNull final String versionString) {
        final String[] parts = versionString.split("\\.");
        switch (parts.length) {
            case 0: return new int[] {-1, -1};
            case 1: return new int[] {-1, Integer.parseInt(parts[0])};
            case 2: return new int[] {Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
            default: return null;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class UpdateDescription {
        private final String version;
        private final String url;
    }

}
