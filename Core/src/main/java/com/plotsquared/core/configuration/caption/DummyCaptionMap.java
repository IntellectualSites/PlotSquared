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
package com.plotsquared.core.configuration.caption;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

/**
 * {@link CaptionMap} implementation that throws exception on all getters
 */
public class DummyCaptionMap implements CaptionMap {

    @Override
    public @NonNull String getMessage(final @NonNull TranslatableCaption caption)
            throws NoSuchCaptionException {
        throw new NoSuchCaptionException(caption);
    }

    @Override
    public @NonNull String getMessage(
            final @NonNull TranslatableCaption caption,
            final @NonNull LocaleHolder localeHolder
    ) throws NoSuchCaptionException {
        throw new NoSuchCaptionException(caption);
    }

    @Override
    public boolean supportsLocale(@NonNull Locale locale) {
        return false;
    }

    @Override
    public @NonNull Locale getLocale() {
        throw new UnsupportedOperationException("Cannot get locale of DummyCaptionMap");
    }

    @Override
    public @NonNull Set<TranslatableCaption> getCaptions() {
        return Collections.emptySet();
    }

}
