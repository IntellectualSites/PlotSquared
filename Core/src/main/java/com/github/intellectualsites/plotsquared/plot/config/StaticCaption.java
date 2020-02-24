package com.github.intellectualsites.plotsquared.plot.config;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor public final class StaticCaption implements Caption {

    private final String value;
    private final boolean usePrefix;

    public StaticCaption(final String value) {
        this(value, true);
    }

    @Override public String getTranslated() {
        return this.value;
    }

    @Override public boolean usePrefix() {
        return this.usePrefix;
    }

}
