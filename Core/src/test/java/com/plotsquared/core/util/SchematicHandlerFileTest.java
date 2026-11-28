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
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

/**
 * Covers how a schematic name is turned into a file of the schematic folder.
 */
class SchematicHandlerFileTest {

    private static final String SCHEMATICS = "schematics";

    @TempDir
    File directory;

    @Test
    void nameWithoutExtensionResolvesToSchemFileInFolder() {
        Assertions.assertEquals(
                new File(new File(directory, SCHEMATICS), "house.schem"),
                SchematicHandler.schematicFile(directory, SCHEMATICS, "house"),
                "a plain name must resolve to its .schem file in the schematic folder"
        );
    }

    @Test
    void nameWithSchematicExtensionKeepsIt() {
        Assertions.assertEquals(
                new File(new File(directory, SCHEMATICS), "house.schematic"),
                SchematicHandler.schematicFile(directory, SCHEMATICS, "house.schematic"),
                "a name with the .schematic extension must resolve to that file"
        );
    }

    @Test
    void nameInSubFolderResolves() {
        Assertions.assertEquals(
                new File(new File(directory, SCHEMATICS), "towns" + File.separator + "house.schem"),
                SchematicHandler.schematicFile(directory, SCHEMATICS, "towns" + File.separator + "house"),
                "a name in a sub folder of the schematic folder must resolve"
        );
    }

    @Test
    void parentTraversalIsRejected() {
        Assertions.assertNull(
                SchematicHandler.schematicFile(directory, SCHEMATICS, "../../outside"),
                "a name that climbs out of the schematic folder with .. must not resolve"
        );
    }

    @Test
    void traversalToSiblingOfSchematicFolderIsRejected() {
        Assertions.assertNull(
                SchematicHandler.schematicFile(directory, SCHEMATICS, "../config/settings.yml"),
                "a name that points into a sibling of the schematic folder must not resolve"
        );
    }

    @Test
    void traversalThroughSubFolderIsRejected() {
        Assertions.assertNull(
                SchematicHandler.schematicFile(directory, SCHEMATICS, "towns/../../outside"),
                "a name that leaves the schematic folder through a sub folder must not resolve"
        );
    }

    @Test
    void traversalWithExtensionIsRejected() {
        Assertions.assertNull(
                SchematicHandler.schematicFile(directory, SCHEMATICS, "../../outside.schematic"),
                "a name with extension that climbs out of the schematic folder must not resolve"
        );
    }

    @Test
    void absoluteNameDoesNotLeaveSchematicFolder() {
        final File outside = new File(directory, "outside");
        final File resolved = SchematicHandler.schematicFile(directory, SCHEMATICS, outside.getAbsolutePath());
        Assertions.assertTrue(
                resolved == null || FileUtils.isInsideDirectory(new File(directory, SCHEMATICS), resolved),
                () -> "an absolute path given as name must not resolve outside the schematic folder: " + resolved
        );
    }

    @Test
    void backslashTraversalDoesNotLeaveSchematicFolder() {
        final File resolved = SchematicHandler.schematicFile(directory, SCHEMATICS, "..\\..\\outside");
        Assertions.assertTrue(
                resolved == null || FileUtils.isInsideDirectory(new File(directory, SCHEMATICS), resolved),
                () -> "a name with backslash traversal must not resolve outside the schematic folder: " + resolved
        );
    }

    @Test
    void traversalIsRejectedForAbsoluteSchematicFolder() {
        final File folder = new File(directory, "shared-schematics");
        Assertions.assertNull(
                SchematicHandler.schematicFile(directory, folder.getAbsolutePath(), "../outside"),
                "a name must not leave a schematic folder that is configured as absolute path"
        );
    }

    @Test
    void nameResolvesInsideAbsoluteSchematicFolder() {
        final File folder = new File(directory, "shared-schematics");
        Assertions.assertEquals(
                new File(folder, "house.schem"),
                SchematicHandler.schematicFile(directory, folder.getAbsolutePath(), "house"),
                "a plain name must resolve inside a schematic folder that is configured as absolute path"
        );
    }

}
