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
package com.plotsquared.bukkit.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Covers where an attacker that is not a player may damage a victim, depending on whether each of them stands on a
 * plot or on the road.
 */
class NonPlayerDamageTest {

    private static final boolean IN_PLOT = true;
    private static final boolean ON_ROAD = false;
    private static final boolean PVE = true;
    private static final boolean NO_PVE = false;
    private static final boolean ROAD_PVE = true;
    private static final boolean NO_ROAD_PVE = false;
    private static final boolean FIREWORK = true;
    private static final boolean NO_FIREWORK = false;
    private static final boolean ARROW_AT_NON_CREATURE = true;
    private static final boolean NO_ARROW_AT_NON_CREATURE = false;

    @Test
    void attackerOnRoadCannotDamageVictimInPlotWithoutPve() {
        Assertions.assertFalse(
                NonPlayerDamage.isAllowed(ON_ROAD, IN_PLOT, NO_PVE, NO_ROAD_PVE, NO_FIREWORK, NO_ARROW_AT_NON_CREATURE),
                "an attacker on the road must not damage a victim in a plot whose pve flag is false"
        );
    }

    @Test
    void attackerOnRoadCanDamageVictimInPlotWithPve() {
        Assertions.assertTrue(
                NonPlayerDamage.isAllowed(ON_ROAD, IN_PLOT, PVE, NO_ROAD_PVE, NO_FIREWORK, NO_ARROW_AT_NON_CREATURE),
                "an attacker on the road must be able to damage a victim in a plot whose pve flag is true"
        );
    }

    @Test
    void roadPveDoesNotOpenPlotWithoutPveToAttackerOnRoad() {
        Assertions.assertFalse(
                NonPlayerDamage.isAllowed(ON_ROAD, IN_PLOT, NO_PVE, ROAD_PVE, NO_FIREWORK, NO_ARROW_AT_NON_CREATURE),
                "the pve road flag must not allow damage to a victim in a plot whose pve flag is false"
        );
    }

    @Test
    void arrowFromRoadCannotDamageNonCreatureInPlotWithoutPve() {
        Assertions.assertFalse(
                NonPlayerDamage.isAllowed(ON_ROAD, IN_PLOT, NO_PVE, NO_ROAD_PVE, NO_FIREWORK, ARROW_AT_NON_CREATURE),
                "an arrow from the road must not damage a victim that is no creature in a plot whose pve flag is false"
        );
    }

    @Test
    void fireworkFromRoadCannotDamageVictimInPlotWithPve() {
        Assertions.assertFalse(
                NonPlayerDamage.isAllowed(ON_ROAD, IN_PLOT, PVE, NO_ROAD_PVE, FIREWORK, NO_ARROW_AT_NON_CREATURE),
                "a firework explosion must not damage a victim in a plot, even if the pve flag of the plot is true"
        );
    }

    @Test
    void attackerInPlotCannotDamageVictimInPlotWithoutPve() {
        Assertions.assertFalse(
                NonPlayerDamage.isAllowed(IN_PLOT, IN_PLOT, NO_PVE, NO_ROAD_PVE, NO_FIREWORK, NO_ARROW_AT_NON_CREATURE),
                "an attacker in a plot must not damage a victim in a plot whose pve flag is false"
        );
    }

    @Test
    void attackerInPlotCanDamageVictimInPlotWithPve() {
        Assertions.assertTrue(
                NonPlayerDamage.isAllowed(IN_PLOT, IN_PLOT, PVE, NO_ROAD_PVE, NO_FIREWORK, NO_ARROW_AT_NON_CREATURE),
                "an attacker in a plot must be able to damage a victim in a plot whose pve flag is true"
        );
    }

    @Test
    void fireworkInPlotCanDamageVictimInPlotWithPve() {
        Assertions.assertTrue(
                NonPlayerDamage.isAllowed(IN_PLOT, IN_PLOT, PVE, NO_ROAD_PVE, FIREWORK, NO_ARROW_AT_NON_CREATURE),
                "a firework explosion inside a plot must stay bound by the pve flag of the victim's plot only"
        );
    }

    @Test
    void attackerInPlotCannotDamageVictimOnRoad() {
        Assertions.assertFalse(
                NonPlayerDamage.isAllowed(IN_PLOT, ON_ROAD, NO_PVE, NO_ROAD_PVE, NO_FIREWORK, NO_ARROW_AT_NON_CREATURE),
                "an attacker in a plot must not damage a victim on the road"
        );
    }

    @Test
    void attackerInPlotCannotDamageVictimOnRoadWithRoadPve() {
        Assertions.assertFalse(
                NonPlayerDamage.isAllowed(IN_PLOT, ON_ROAD, NO_PVE, ROAD_PVE, NO_FIREWORK, NO_ARROW_AT_NON_CREATURE),
                "an attacker in a plot must not damage a victim on the road, even if the pve road flag is set"
        );
    }

    @Test
    void attackerOnRoadCanDamageVictimOnRoadWithRoadPve() {
        Assertions.assertTrue(
                NonPlayerDamage.isAllowed(ON_ROAD, ON_ROAD, NO_PVE, ROAD_PVE, NO_FIREWORK, ARROW_AT_NON_CREATURE),
                "the pve road flag must allow any damage between attacker and victim on the road"
        );
    }

    @Test
    void attackerOnRoadCanDamageVictimOnRoadWithoutRoadPve() {
        Assertions.assertTrue(
                NonPlayerDamage.isAllowed(ON_ROAD, ON_ROAD, NO_PVE, NO_ROAD_PVE, NO_FIREWORK, NO_ARROW_AT_NON_CREATURE),
                "an attacker on the road must be able to damage a victim on the road without the pve road flag"
        );
    }

    @Test
    void arrowOnRoadCannotDamageNonCreatureOnRoadWithoutRoadPve() {
        Assertions.assertFalse(
                NonPlayerDamage.isAllowed(ON_ROAD, ON_ROAD, NO_PVE, NO_ROAD_PVE, NO_FIREWORK, ARROW_AT_NON_CREATURE),
                "an arrow must not damage a victim that is no creature on the road without the pve road flag"
        );
    }

    @Test
    void fireworkOnRoadCanDamageVictimOnRoad() {
        Assertions.assertTrue(
                NonPlayerDamage.isAllowed(ON_ROAD, ON_ROAD, NO_PVE, NO_ROAD_PVE, FIREWORK, NO_ARROW_AT_NON_CREATURE),
                "a firework explosion on the road must be able to damage a victim on the road"
        );
    }

}
