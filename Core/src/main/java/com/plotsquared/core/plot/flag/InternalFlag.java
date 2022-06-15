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
package com.plotsquared.core.plot.flag;

/**
 * These are flags used in PlotSquared and PlotSquared
 * add-ons that can be used to associate information
 * with the plot, without users being able to access the
 * information.
 * <p>
 * These flags are not user assignable, nor do they
 * show up in `/plot info`, `/plot flag list`, etc.
 * <p>
 * PlotSquared add-ons should ignore these flags
 * when outputting flag information to users. An example
 * of such a scenario would be the flag listing in Dynmap.
 */
public interface InternalFlag {

}
