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
package com.plotsquared.core.events;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum for {@link CancellablePlotEvent}.
 * <p>
 * DENY: do not allow the event to happen
 * ALLOW: allow the event to continue as normal, subject to standard checks
 * FORCE: force the event to occur, even if normal checks would deny.
 * WARNING: this may have unintended consequences! Make sure you study the appropriate code before using!
 */
public enum Result {

    DENY(0),
    ACCEPT(1),
    FORCE(2);

    private static final Map<Integer, Result> map = new HashMap<>();

    static {
        for (Result eventResult : Result.values()) {
            map.put(eventResult.value, eventResult);
        }
    }

    private final int value;

    Result(int value) {
        this.value = value;
    }

    /**
     * Obtain the Result enum associated with the int value
     *
     * @param eventResult the int value
     * @return the corresponding Result
     */
    public static Result valueOf(int eventResult) {
        return map.get(eventResult);
    }

    /**
     * Get int value of enum
     *
     * @return integer value
     */
    public int getValue() {
        return value;
    }
}
