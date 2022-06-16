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
package com.plotsquared.core.queue;

import java.util.HashMap;
import java.util.Map;

public enum LightingMode {

    NONE(0),
    PLACEMENT(1),
    REPLACEMENT(2),
    ALL(3);

    private static final Map<Integer, LightingMode> map = new HashMap<>();

    static {
        for (LightingMode mode : LightingMode.values()) {
            map.put(mode.mode, mode);
        }
    }

    private final int mode;

    LightingMode(int mode) {
        this.mode = mode;
    }

    public static LightingMode valueOf(int mode) {
        return map.get(mode);
    }

    public int getMode() {
        return mode;
    }
}
