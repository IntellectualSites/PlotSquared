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
package com.plotsquared.core.configuration.caption;

import com.google.common.base.Objects;
import com.plotsquared.core.PlotSquared;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Caption that is user modifiable
 */
public final class TranslatableCaption implements NamespacedCaption {

    /**
     * Default caption namespace
     */
    public static final String DEFAULT_NAMESPACE = "plotsquared";

    private final String namespace;
    private final String key;

    private TranslatableCaption(@Nonnull final String namespace, @Nonnull final String key) {
        this.namespace = namespace;
        this.key = key;
    }

    /**
     * Get a new {@link TranslatableCaption} instance
     *
     * @param rawKey Caption key in the format namespace:key. If no namespace is
     *               included, {@link #DEFAULT_NAMESPACE} will be used.
     * @return Caption instance
     */
    @Nonnull public static TranslatableCaption of(@Nonnull final String rawKey) {
        final String namespace;
        final String key;
        if (rawKey.contains(":")) {
            final String[] split = rawKey.split(Pattern.quote(":"));
            namespace = split[0];
            key = split[1];
        } else {
            namespace = DEFAULT_NAMESPACE;
            key = rawKey;
        }
        return new TranslatableCaption(namespace.toLowerCase(Locale.ENGLISH),
            key.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Get a new {@link TranslatableCaption} instance
     *
     * @param namespace Caption namespace
     * @param key       Caption key
     * @return Caption instance
     */
    @Nonnull public static TranslatableCaption of(@Nonnull final String namespace,
        @Nonnull final String key) {
        return new TranslatableCaption(namespace.toLowerCase(Locale.ENGLISH),
            key.toLowerCase(Locale.ENGLISH));
    }

    @Override @Nonnull public String getComponent(@Nonnull final LocaleHolder localeHolder) {
        return PlotSquared.get().getCaptionMap(this.namespace).getMessage(this, localeHolder);
    }

    @Override @Nonnull public String getKey() {
        return this.key;
    }

    @Override @Nonnull public String getNamespace() {
        return this.namespace;
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final TranslatableCaption that = (TranslatableCaption) o;
        return Objects.equal(this.getNamespace(), that.getNamespace()) && Objects
            .equal(this.getKey(), that.getKey());
    }

    @Override public int hashCode() {
        return Objects.hashCode(this.getNamespace(), this.getKey());
    }

}
