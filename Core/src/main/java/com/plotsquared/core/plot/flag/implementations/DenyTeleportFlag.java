/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;

public class DenyTeleportFlag extends PlotFlag<DenyTeleportFlag.DeniedGroup, DenyTeleportFlag> {

    public static final DenyTeleportFlag DENY_TELEPORT_FLAG_NONE =
        new DenyTeleportFlag(DeniedGroup.NONE);

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected DenyTeleportFlag(@Nonnull DeniedGroup value) {
        super(value, Captions.FLAG_CATEGORY_ENUM, Captions.FLAG_DESCRIPTION_DENY_TELEPORT);
    }

    public static boolean allowsTeleport(PlotPlayer player, Plot plot) {
        final DeniedGroup value = plot.getFlag(DenyTeleportFlag.class);
        if (value == DeniedGroup.NONE) {
            return true;
        }
        final boolean result;
        switch (value) {
            case TRUSTED:
                result = !plot.getTrusted().contains(player.getUUID());
                break;
            case MEMBERS:
                result = !plot.getMembers().contains(player.getUUID());
                break;
            case NONMEMBERS:
                result = plot.isAdded(player.getUUID());
                break;
            case NONTRUSTED:
                result =
                    plot.getTrusted().contains(player.getUUID()) || plot.isOwner(player.getUUID());
                break;
            case NONOWNERS:
                result = plot.isOwner(player.getUUID());
                break;
            default:
                return true;
        }
        return result || player.hasPermission("plots.admin.entry.denied");
    }

    @Override public DenyTeleportFlag parse(@Nonnull String input) throws FlagParseException {
        final DeniedGroup group = DeniedGroup.fromString(input);
        if (group == null) {
            throw new FlagParseException(this, input, Captions.FLAG_ERROR_ENUM,
                "members, nonmembers, trusted, nontrusted, nonowners");
        }
        return flagOf(group);
    }

    @Override public DenyTeleportFlag merge(@Nonnull DeniedGroup newValue) {
        if (getValue().ordinal() < newValue.ordinal()) {
            return flagOf(newValue);
        }
        return this;
    }

    @Override public String toString() {
        return this.getValue().name();
    }

    @Override public String getExample() {
        return "trusted";
    }

    @Override protected DenyTeleportFlag flagOf(@Nonnull DeniedGroup value) {
        return new DenyTeleportFlag(value);
    }

    @Override public Collection<String> getTabCompletions() {
        return Arrays.asList("none", "members", "trusted", "nonmembers", "nontrusted", "nonowners");
    }

    public enum DeniedGroup {
        NONE, MEMBERS, TRUSTED, NONMEMBERS, NONTRUSTED, NONOWNERS;

        @Nullable public static DeniedGroup fromString(@Nonnull final String string) {
            for (final DeniedGroup group : values()) {
                if (group.name().equalsIgnoreCase(string)) {
                    return group;
                }
            }
            return null;
        }
    }

}
