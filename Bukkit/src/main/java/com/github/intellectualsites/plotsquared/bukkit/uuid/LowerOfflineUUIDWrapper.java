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
package com.github.intellectualsites.plotsquared.bukkit.uuid;

import com.github.intellectualsites.plotsquared.plot.object.OfflinePlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.google.common.base.Charsets;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class LowerOfflineUUIDWrapper extends OfflineUUIDWrapper {

    @NotNull @Override public UUID getUUID(PlotPlayer player) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName().toLowerCase()).getBytes(Charsets.UTF_8));
    }

    @Override public UUID getUUID(OfflinePlotPlayer player) {
        return UUID.nameUUIDFromBytes(
            ("OfflinePlayer:" + player.getName().toLowerCase()).getBytes(Charsets.UTF_8));
    }

    @Override public UUID getUUID(OfflinePlayer player) {
        return UUID.nameUUIDFromBytes(
            ("OfflinePlayer:" + Objects.requireNonNull(player.getName()).toLowerCase()).getBytes(Charsets.UTF_8));
    }

    @Override public UUID getUUID(String name) {
        return UUID
            .nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase()).getBytes(Charsets.UTF_8));
    }

}
