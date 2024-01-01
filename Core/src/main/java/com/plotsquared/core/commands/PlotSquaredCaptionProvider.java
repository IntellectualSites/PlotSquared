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
package com.plotsquared.core.commands;

import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionProvider;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.CaptionMap;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * {@link CaptionProvider} that retrieves caption values from the {@link CaptionMap caption map}.
 */
public final class PlotSquaredCaptionProvider implements CaptionProvider<PlotPlayer<?>> {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + PlotSquaredCaptionProvider.class.getSimpleName());

    @Override
    public @Nullable String provide(final @NonNull Caption caption, final @NonNull PlotPlayer<?> recipient) {
        try {
            return PlotSquared.get()
                    .getCaptionMap(TranslatableCaption.DEFAULT_NAMESPACE)
                    .getMessage(TranslatableCaption.of(caption.key()), recipient);
        } catch (final CaptionMap.NoSuchCaptionException ignored) {
            LOGGER.warn("Missing caption '{}', will attempt to fall back on Cloud defaults", caption.key());
            return null;
        }
    }
}
