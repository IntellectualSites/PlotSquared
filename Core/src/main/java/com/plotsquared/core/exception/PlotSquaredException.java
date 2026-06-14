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
package com.plotsquared.core.exception;

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.LocaleHolder;

/**
 * Internal use only. Used to allow adventure captions to be used in an exception
 *
 * @since 7.5.7
 */
public final class PlotSquaredException extends RuntimeException {

    private final Caption caption;

    /**
     * Create a new instance with the given caption
     *
     * @param caption caption
     */
    public PlotSquaredException(Caption caption) {
        this.caption = caption;
    }

    /**
     * Create a new instance with the given caption and cause
     *
     * @param caption caption
     * @param cause   cause
     */
    public PlotSquaredException(Caption caption, Exception cause) {
        super(cause);
        this.caption = caption;
    }

    @Override
    public String getMessage() {
        return caption.getComponent(LocaleHolder.console());
    }

    public Caption getCaption() {
        return caption;
    }

}
