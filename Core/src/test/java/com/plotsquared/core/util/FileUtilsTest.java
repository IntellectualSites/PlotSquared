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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

public class FileUtilsTest {

    private static final File SCHEMATICS = new File("base/schematics");

    @Test
    public void fileDirectlyInDirectoryIsInside() {
        Assertions.assertTrue(
                FileUtils.isInsideDirectory(SCHEMATICS, new File("base/schematics/house.schem")),
                "A file directly inside the directory must be reported as inside"
        );
    }

    @Test
    public void fileInSubDirectoryIsInside() {
        Assertions.assertTrue(
                FileUtils.isInsideDirectory(SCHEMATICS, new File("base/schematics/a/b/house.schem")),
                "A file in a sub directory must be reported as inside"
        );
    }

    @Test
    public void parentTraversalIsOutside() {
        Assertions.assertFalse(
                FileUtils.isInsideDirectory(SCHEMATICS, new File("base/schematics/../../other/secret.schem")),
                "A path that climbs out of the directory with .. must be reported as outside"
        );
    }

    @Test
    public void traversalThatReturnsIntoDirectoryIsInside() {
        Assertions.assertTrue(
                FileUtils.isInsideDirectory(SCHEMATICS, new File("base/schematics/a/../house.schem")),
                "A .. segment that resolves back into the directory must be reported as inside"
        );
    }

    @Test
    public void siblingDirectoryWithSamePrefixIsOutside() {
        Assertions.assertFalse(
                FileUtils.isInsideDirectory(SCHEMATICS, new File("base/schematics-evil/house.schem")),
                "A sibling directory sharing a name prefix must not be reported as inside"
        );
    }

    @Test
    public void directoryItselfIsNotInside() {
        Assertions.assertFalse(
                FileUtils.isInsideDirectory(SCHEMATICS, new File("base/schematics")),
                "The directory itself must not be reported as inside itself"
        );
    }

}
