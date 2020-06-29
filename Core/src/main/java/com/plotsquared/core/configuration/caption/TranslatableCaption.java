package com.plotsquared.core.configuration.caption;

import com.plotsquared.core.configuration.Caption;
import org.jetbrains.annotations.NotNull;

public final class TranslatableCaption implements Caption {
    @NotNull private final String key;

    private TranslatableCaption(@NotNull String key) {
        this.key = key;
    }

    public static TranslatableCaption of(@NotNull final String key) {
        return new TranslatableCaption(key);
    }

    @Override public String getTranslated() {
        return null;
    }

    @Override public boolean usePrefix() {
        return false;
    }

    @NotNull public String getKey() {
        return this.key;
    }
}
