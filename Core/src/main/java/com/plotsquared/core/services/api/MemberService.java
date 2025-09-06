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
package com.plotsquared.core.services.api;

import com.plotsquared.core.plot.Plot;

import java.util.UUID;

public interface MemberService {

    void removeTrusted(Plot plot, UUID uuid);

    void removeMember(Plot plot, UUID uuid);

    void setTrusted(Plot plot, UUID uuid);

    void setMember(Plot plot, UUID uuid);

    void removeDenied(Plot plot, UUID uuid);

    void setDenied(Plot plot, UUID uuid);
}
