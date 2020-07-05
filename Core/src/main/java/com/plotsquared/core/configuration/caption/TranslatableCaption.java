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
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.configuration.caption;

import com.plotsquared.core.PlotSquared;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Caption that is user modifiable
 */
public final class TranslatableCaption implements KeyedCaption {

    @Getter private final String key;

    private TranslatableCaption(@NotNull final String key) {
        this.key = key;
    }

    /**
     * Get a new {@link TranslatableCaption} instance
     *
     * @param key Caption key
     * @return Caption instance
     */
    @NotNull public static TranslatableCaption of(@NotNull final String key) {
        return new TranslatableCaption(key.toLowerCase(Locale.ENGLISH));
    }

    @Override @NotNull public String getComponent(@NotNull final LocaleHolder localeHolder) {
        return PlotSquared.get().getCaptionMap().getMessage(this, localeHolder);
    }

}
