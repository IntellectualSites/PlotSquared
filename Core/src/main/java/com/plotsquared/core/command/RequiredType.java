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
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum RequiredType {
    CONSOLE(TranslatableCaption.of("console.not_console")),
    PLAYER(TranslatableCaption.of("console.is_console")),
    NONE(StaticCaption.of("Something went wrong: RequiredType=NONE")); // this caption should never be sent

    private final Caption caption;

    RequiredType(Caption caption) {
        this.caption = caption;
    }

    public boolean allows(CommandCaller player) {
        if (this == RequiredType.NONE) {
            return true;
        }
        return this == player.getSuperCaller();
    }

    public @NonNull Caption getErrorMessage() {
        return this.caption;
    }
}
