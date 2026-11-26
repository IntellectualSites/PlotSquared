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

/**
 * Decides whether an attacker that is not a player, and was not fired by one, may damage a victim.
 */
final class NonPlayerDamage {

    private NonPlayerDamage() {
    }

    /**
     * An attacker standing on a plot is bound by the pve flag of the victim's plot and cannot damage anything on the
     * road. The same flag binds an attacker standing on the road, so that the road next to a plot is no place to
     * damage what is inside it. On the road itself the pve road flag decides.
     *
     * @param attackerInPlot     whether the attacker stands on a plot
     * @param victimInPlot       whether the victim stands on a plot
     * @param victimPlotPve      value of the pve flag of the victim's plot, {@code false} if the victim is on the road
     * @param roadPve            whether the area uses road flags and its pve road flag is set
     * @param fireworkExplosion  whether the damage is the explosion of a firework
     * @param arrowAtNonCreature whether the attacker is an arrow and the victim is not a creature
     * @return {@code true} if the damage is allowed
     */
    static boolean isAllowed(
            final boolean attackerInPlot,
            final boolean victimInPlot,
            final boolean victimPlotPve,
            final boolean roadPve,
            final boolean fireworkExplosion,
            final boolean arrowAtNonCreature
    ) {
        if (attackerInPlot) {
            return victimInPlot && victimPlotPve;
        }
        if (victimInPlot) {
            //disable the firework damage. too much of a headache to support at the moment.
            if (fireworkExplosion) {
                return false;
            }
            return victimPlotPve;
        }
        if (roadPve) {
            return true;
        }
        return !arrowAtNonCreature;
    }

}
