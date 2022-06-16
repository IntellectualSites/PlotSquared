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

import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class EconHandler {

    /**
     * Returns an econ handler that:
     * <ul>
     *     <li>Returns {@code false} on {@link #isEnabled(PlotArea)}</li>
     *     <li>Returns {@link Double#MIN_VALUE} on {@link #getBalance(PlotPlayer)}</li>
     *     <li>Doesn't do anything for {@link #withdrawMoney(PlotPlayer, double)},
     *          {@link #depositMoney(OfflinePlotPlayer, double)}
     *          {@link #depositMoney(PlotPlayer, double)}</li>
     * </ul>
     *
     * @return A null econ handler
     */
    public static EconHandler nullEconHandler() {
        return new NullEconHandler();
    }

    public abstract boolean init();

    public double getMoney(PlotPlayer<?> player) {
        if (player instanceof ConsolePlayer) {
            return Double.MAX_VALUE;
        }
        return getBalance(player);
    }

    public abstract double getBalance(PlotPlayer<?> player);

    public abstract void withdrawMoney(PlotPlayer<?> player, double amount);

    public abstract void depositMoney(PlotPlayer<?> player, double amount);

    public abstract void depositMoney(OfflinePlotPlayer player, double amount);

    /**
     * Returns whether economy is enabled in the given plot area or not.
     * Implementations should only return true if {@link #isSupported()} returns
     * true too.
     *
     * @param plotArea the plot area to check
     * @return {@code true} if economy is enabled on the given plot area, {@code false} otherwise.
     */
    public abstract boolean isEnabled(PlotArea plotArea);

    /**
     * Formats the given balance into a human-readable number.
     *
     * @param balance the balance to format.
     * @return the balance as formatted string.
     */
    public abstract @NonNull String format(double balance);

    /**
     * Returns whether economy is supported by the server or not.
     *
     * @return {@code true} if economy is supported, {@code false} otherwise.
     */
    public abstract boolean isSupported();

    private static final class NullEconHandler extends EconHandler {

        @Override
        public boolean init() {
            return false;
        }

        @Override
        public double getBalance(PlotPlayer<?> player) {
            return Double.MIN_VALUE;
        }

        @Override
        public void withdrawMoney(PlotPlayer<?> player, double amount) {

        }

        @Override
        public void depositMoney(PlotPlayer<?> player, double amount) {

        }

        @Override
        public void depositMoney(OfflinePlotPlayer player, double amount) {

        }

        @Override
        public boolean isEnabled(PlotArea plotArea) {
            return false;
        }

        @Override
        public @NonNull String format(double balance) {
            return "";
        }

        @Override
        public boolean isSupported() {
            return false;
        }

    }

}
