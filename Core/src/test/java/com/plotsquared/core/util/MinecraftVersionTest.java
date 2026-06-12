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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MinecraftVersionTest {

    @Test
    @DisplayName("isNewerOrEqualThan(MinecraftVersion)")
    void isNewerOrEqualThan() {
        assertTrue(MinecraftVersion.TINY_TAKEOVER.isNewerOrEqualThan(MinecraftVersion.TRICKY_TRIALS));
        assertTrue(MinecraftVersion.TINY_TAKEOVER.isNewerOrEqualThan(MinecraftVersion.TINY_TAKEOVER));
        assertTrue(new MinecraftVersion(1, 2, 3).isNewerOrEqualThan(new MinecraftVersion(1, 2, 2)));
        assertFalse(MinecraftVersion.CAVES_AND_CLIFFS.isNewerOrEqualThan(MinecraftVersion.CAVES_AND_CLIFFS_2));
    }

    @Test
    @DisplayName("isOlderOrEqualThan(MinecraftVersion)")
    void isOlderOrEqualThan() {
        assertTrue(MinecraftVersion.TRICKY_TRIALS.isOlderOrEqualThan(MinecraftVersion.TINY_TAKEOVER));
        assertTrue(MinecraftVersion.TRICKY_TRIALS.isOlderOrEqualThan(MinecraftVersion.TRICKY_TRIALS));
        assertTrue(new MinecraftVersion(1, 2, 2).isOlderOrEqualThan(new MinecraftVersion(1, 2, 3)));
        assertFalse(MinecraftVersion.CAVES_AND_CLIFFS_2.isOlderOrEqualThan(MinecraftVersion.CAVES_AND_CLIFFS));
    }

    @Test
    @DisplayName("isOlderThan(MinecraftVersion)")
    void isOlderThan() {
        assertTrue(MinecraftVersion.TRICKY_TRIALS.isOlderThan(MinecraftVersion.TINY_TAKEOVER));
        assertTrue(new MinecraftVersion(1, 2, 2).isOlderThan(new MinecraftVersion(1, 2, 3)));
        assertFalse(MinecraftVersion.TINY_TAKEOVER.isOlderThan(MinecraftVersion.TRICKY_TRIALS));
    }

    @Test
    @DisplayName("isNewerOrEqualThan(int)")
    void isNewerOrEqualThanPrimitiveMajor() {
        assertTrue(MinecraftVersion.TINY_TAKEOVER.isNewerOrEqualThan(25));
        assertTrue(new MinecraftVersion(4, 5, 6).isNewerOrEqualThan(3));
        assertFalse(MinecraftVersion.TINY_TAKEOVER.isNewerOrEqualThan(27));
    }

    @Test
    @DisplayName("isNewerOrEqualThan(int, int)")
    void isNewerOrEqualThanPrimitiveMajorMinor() {
        assertTrue(MinecraftVersion.TINY_TAKEOVER.isNewerOrEqualThan(25, 69));
        assertTrue(MinecraftVersion.TINY_TAKEOVER.isNewerOrEqualThan(26, 1));
        assertTrue(new MinecraftVersion(4, 5, 6).isNewerOrEqualThan(4, 5));
        assertFalse(MinecraftVersion.TINY_TAKEOVER.isNewerOrEqualThan(26, 2));
    }

    @Test
    @DisplayName("isOlderOrEqualThan(int)")
    void isOlderOrEqualThanPrimitiveMajor() {
        assertTrue(MinecraftVersion.TINY_TAKEOVER.isOlderOrEqualThan(27));
        assertTrue(new MinecraftVersion(4, 5, 6).isOlderOrEqualThan(5));
        assertFalse(MinecraftVersion.TINY_TAKEOVER.isOlderOrEqualThan(20));
    }

    @Test
    @DisplayName("isOlderOrEqualThan(int, int)")
    void isOlderOrEqualThanPrimitiveMajorMinor() {
        assertTrue(MinecraftVersion.TINY_TAKEOVER.isOlderOrEqualThan(27, 69));
        assertTrue(MinecraftVersion.TINY_TAKEOVER.isOlderOrEqualThan(26, 2));
        assertTrue(new MinecraftVersion(4, 5, 6).isOlderOrEqualThan(4, 5));
        assertFalse(MinecraftVersion.TINY_TAKEOVER.isOlderOrEqualThan(26, 0));
    }

}
