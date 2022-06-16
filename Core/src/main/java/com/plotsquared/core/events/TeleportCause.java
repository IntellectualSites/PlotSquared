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

import com.google.common.collect.Sets;

import java.util.EnumSet;
import java.util.Set;

/**
 * The reason for an internal player teleport.
 */
public enum TeleportCause {

    COMMAND,
    COMMAND_AREA_CREATE,
    COMMAND_AREA_TELEPORT,
    COMMAND_AUTO,
    COMMAND_CLAIM,
    COMMAND_CLEAR,
    COMMAND_CLUSTER_TELEPORT,
    COMMAND_DELETE,
    COMMAND_HOME,
    COMMAND_LIKE,
    COMMAND_MIDDLE,
    COMMAND_RATE,
    COMMAND_SETUP,
    COMMAND_TEMPLATE,
    COMMAND_VISIT,
    DEATH,
    DENIED,
    KICK,
    LOGIN,
    PLUGIN,
    UNKNOWN;

    /**
     * @since 6.1.0
     */
    public static final class CauseSets {

        public static final Set<TeleportCause> COMMAND = Sets.immutableEnumSet(EnumSet.range(
                TeleportCause.COMMAND,
                TeleportCause.COMMAND_VISIT
        ));
        @SuppressWarnings("unused")
        public static final Set<TeleportCause> PLUGIN = Sets.immutableEnumSet(EnumSet.range(
                TeleportCause.DEATH,
                TeleportCause.PLUGIN
        ));

    }
}
