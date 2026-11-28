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
package com.plotsquared.core.util;

import com.plotsquared.core.plot.flag.implementations.EntityChangeBlockFlag;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Covers whether an entity, for example a wither, may change a block of a road, a wall or an unowned plot.
 */
class PlotFlagUtilRoadEntityChangeTest {

    private static final boolean NO_ROAD_FLAGS = false;
    private static final boolean ROAD_FLAGS = true;

    @Test
    @DisplayName("An area without road flags denies entity block changes on the road")
    void areaWithoutRoadFlagsDeniesChange() {
        Assertions.assertFalse(
                PlotFlagUtil.mayEntityChangeBlockOnRoad(NO_ROAD_FLAGS, valueOf(EntityChangeBlockFlag.ENTITY_CHANGE_BLOCK_FALSE)),
                "an area without road flags must not let entities change road blocks"
        );
    }

    @Test
    @DisplayName("Road flag entity-change-block = false denies entity block changes on the road")
    void roadFlagFalseDeniesChange() {
        Assertions.assertFalse(
                PlotFlagUtil.mayEntityChangeBlockOnRoad(ROAD_FLAGS, valueOf(EntityChangeBlockFlag.ENTITY_CHANGE_BLOCK_FALSE)),
                "entity-change-block = false as road flag must deny the change"
        );
    }

    @Test
    @DisplayName("Road flag entity-change-block = true allows entity block changes on the road")
    void roadFlagTrueAllowsChange() {
        Assertions.assertTrue(
                PlotFlagUtil.mayEntityChangeBlockOnRoad(ROAD_FLAGS, valueOf(EntityChangeBlockFlag.ENTITY_CHANGE_BLOCK_TRUE)),
                "entity-change-block = true as road flag must allow the change"
        );
    }

    @Test
    @DisplayName("A stored entity-change-block = true is ignored while the area has no road flags")
    void roadFlagTrueIsIgnoredWithoutRoadFlags() {
        Assertions.assertFalse(
                PlotFlagUtil.mayEntityChangeBlockOnRoad(NO_ROAD_FLAGS, valueOf(EntityChangeBlockFlag.ENTITY_CHANGE_BLOCK_TRUE)),
                "a road flag value must not allow the change while road flags are disabled for the area"
        );
    }

    private static boolean valueOf(final EntityChangeBlockFlag flag) {
        return flag.getValue();
    }

}
