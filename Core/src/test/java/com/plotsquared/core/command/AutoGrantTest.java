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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AutoGrantTest {

    @Test
    void oneGrantAtLimitIsUsedUp() {
        Assertions.assertEquals(0, Auto.remainingGrants(1, 0, 1), "a single grant used for a single plot at the limit must leave no grants");
    }

    @Test
    void grantsBeyondTheRequestRemain() {
        Assertions.assertEquals(4, Auto.remainingGrants(5, 0, 1), "four grants must remain after one plot is paid from five grants");
    }

    @Test
    void freeSlotsAreUsedBeforeGrants() {
        Assertions.assertEquals(1, Auto.remainingGrants(3, 2, 4), "two free slots must be used before grants, leaving one of three grants");
    }

    @Test
    void playerOverLimitPaysFullyFromGrants() {
        Assertions.assertEquals(3, Auto.remainingGrants(4, -2, 1), "a player two plots over the limit must pay one plot from four grants, leaving three");
    }

    @Test
    void fourGrantsOverLimitAreUsedUpByTwoByTwo() {
        Assertions.assertEquals(0, Auto.remainingGrants(4, -2, 4), "a player two plots over the limit must pay a 2x2 claim fully from four grants, leaving none");
    }

    @Test
    void fourGrantsAtLimitAreUsedUpByTwoByTwo() {
        Assertions.assertEquals(0, Auto.remainingGrants(4, 0, 4), "four grants used for a 2x2 claim at the limit must leave no grants");
    }

    @Test
    void twoFreeSlotsAndTwoGrantsCoverTwoByTwo() {
        Assertions.assertEquals(0, Auto.remainingGrants(2, 2, 4), "two free slots and two grants must cover a 2x2 claim, leaving no grants");
    }

    @Test
    void grantsBeyondTwoByTwoRemain() {
        Assertions.assertEquals(2, Auto.remainingGrants(6, 0, 4), "two grants must remain after a 2x2 claim at the limit is paid from six grants");
    }

}
