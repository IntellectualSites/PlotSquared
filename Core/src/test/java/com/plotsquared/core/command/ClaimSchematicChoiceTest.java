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

import com.plotsquared.core.permissions.PermissionHolder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.function.Predicate;

/**
 * Covers what {@code /plot claim} does with the schematic name it is given.
 */
class ClaimSchematicChoiceTest {

    private static final Predicate<String> ONLY_HOUSE_IS_LISTED = "house"::equals;

    @Test
    void unlistedSchematicRefusesTheClaim() {
        Assertions.assertEquals(
                Claim.SchematicChoice.UNKNOWN,
                Claim.schematicChoice("secret", true, ONLY_HOUSE_IS_LISTED, holder("plots.claim.secret"), false),
                "a schematic the area does not list must refuse the claim, even with its permission"
        );
    }

    @Test
    void pathAsSchematicNameRefusesTheClaim() {
        Assertions.assertEquals(
                Claim.SchematicChoice.UNKNOWN,
                Claim.schematicChoice("../../outside", true, ONLY_HOUSE_IS_LISTED, holder(), false),
                "a path that the area does not list as schematic must refuse the claim"
        );
    }

    @Test
    void listedSchematicWithoutPermissionRefusesTheClaim() {
        Assertions.assertEquals(
                Claim.SchematicChoice.NO_PERMISSION,
                Claim.schematicChoice("house", true, ONLY_HOUSE_IS_LISTED, holder(), false),
                "a listed schematic must refuse the claim for a player without plots.claim.house"
        );
    }

    @Test
    void permissionForAnotherSchematicRefusesTheClaim() {
        Assertions.assertEquals(
                Claim.SchematicChoice.NO_PERMISSION,
                Claim.schematicChoice("house", true, ONLY_HOUSE_IS_LISTED, holder("plots.claim.castle"), false),
                "the permission for another schematic must not allow claiming with house"
        );
    }

    @Test
    void listedSchematicWithItsPermissionIsUsed() {
        Assertions.assertEquals(
                Claim.SchematicChoice.USE,
                Claim.schematicChoice("house", true, ONLY_HOUSE_IS_LISTED, holder("plots.claim.house"), false),
                "a listed schematic must be used for a player with plots.claim.house"
        );
    }

    @Test
    void listedSchematicWithAdminSchematicPermissionIsUsed() {
        Assertions.assertEquals(
                Claim.SchematicChoice.USE,
                Claim.schematicChoice("house", true, ONLY_HOUSE_IS_LISTED, holder("plots.admin.command.schematic"), false),
                "a listed schematic must be used for a player with plots.admin.command.schematic"
        );
    }

    @Test
    void forcedClaimUsesListedSchematicWithoutPermission() {
        Assertions.assertEquals(
                Claim.SchematicChoice.USE,
                Claim.schematicChoice("house", true, ONLY_HOUSE_IS_LISTED, holder(), true),
                "a forced claim must use a listed schematic without asking for the permission"
        );
    }

    @Test
    void forcedClaimStillRefusesUnlistedSchematic() {
        Assertions.assertEquals(
                Claim.SchematicChoice.UNKNOWN,
                Claim.schematicChoice("secret", true, ONLY_HOUSE_IS_LISTED, holder(), true),
                "a forced claim must still refuse a schematic the area does not list"
        );
    }

    @Test
    void nameIsIgnoredWhenAreaDoesNotLetPlayersChoose() {
        Assertions.assertEquals(
                Claim.SchematicChoice.IGNORE,
                Claim.schematicChoice("house", false, ONLY_HOUSE_IS_LISTED, holder("plots.claim.house"), false),
                "a name must be dropped when the area does not let players choose a schematic"
        );
    }

    @Test
    void pathIsIgnoredWhenAreaDoesNotLetPlayersChoose() {
        Assertions.assertEquals(
                Claim.SchematicChoice.IGNORE,
                Claim.schematicChoice("../../outside", false, name -> true, holder("plots.admin.command.schematic"), true),
                "a path must be dropped when the area does not let players choose a schematic, whatever the player may do"
        );
    }

    @Test
    void missingNameLeavesTheClaimUnchanged() {
        Assertions.assertEquals(
                Claim.SchematicChoice.NONE,
                Claim.schematicChoice(null, true, ONLY_HOUSE_IS_LISTED, holder(), false),
                "a claim without a schematic name must continue unchanged"
        );
    }

    @Test
    void emptyNameLeavesTheClaimUnchanged() {
        Assertions.assertEquals(
                Claim.SchematicChoice.NONE,
                Claim.schematicChoice("", true, ONLY_HOUSE_IS_LISTED, holder(), false),
                "a claim with an empty schematic name must continue unchanged"
        );
    }

    private static PermissionHolder holder(final String... permissions) {
        final Set<String> granted = Set.of(permissions);
        return new PermissionHolder() {
            @Override
            public boolean hasPermission(final @NonNull String permission, final boolean notify) {
                return granted.contains(permission);
            }

            @Override
            public boolean hasPermission(final @Nullable String world, final @NonNull String permission) {
                return granted.contains(permission);
            }

            @Override
            public boolean hasKeyedPermission(
                    final @Nullable String world,
                    final @NonNull String permission,
                    final @NonNull String key
            ) {
                return false;
            }
        };
    }

}
