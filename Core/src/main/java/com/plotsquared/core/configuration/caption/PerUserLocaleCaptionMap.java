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

import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PerUserLocaleCaptionMap extends LocalizedCaptionMap {

    private final Map<Locale, CaptionMap> localeMap;

    public PerUserLocaleCaptionMap(Map<Locale, CaptionMap> localeMap) {
        super(Locale.ROOT, Collections.emptyMap());
        this.localeMap = localeMap;
    }

    @Override
    public @NonNull String getMessage(
            final @NonNull TranslatableCaption caption,
            final @NonNull LocaleHolder localeHolder
    ) throws NoSuchCaptionException {
        return this.localeMap.get(localeHolder.getLocale()).getMessage(caption);
    }

    @Override
    public boolean supportsLocale(final @NonNull Locale locale) {
        return this.localeMap.containsKey(locale);
    }

    @Override
    public @NonNull Set<TranslatableCaption> getCaptions() {
        return ImmutableSet.copyOf(this.localeMap.get(LocaleHolder.console().getLocale()).getCaptions());
    }

}
