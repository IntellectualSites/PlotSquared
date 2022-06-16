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

import com.google.inject.Singleton;
import com.plotsquared.bukkit.player.BukkitOfflinePlayer;
import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.EconHandler;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public class BukkitEconHandler extends EconHandler {

    private Economy econ;

    private static OfflinePlayer getBukkitOfflinePlayer(PlotPlayer<?> plotPlayer) {
        return ((BukkitPlayer) plotPlayer).player;
    }

    @Override
    public boolean init() {
        if (this.econ == null) {
            setupEconomy();
        }
        return this.econ != null;
    }

    private void setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> economyProvider =
                Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            this.econ = economyProvider.getProvider();
        }
    }

    @Override
    public double getMoney(PlotPlayer<?> player) {
        double bal = super.getMoney(player);
        if (Double.isNaN(bal)) {
            return this.econ.getBalance(getBukkitOfflinePlayer(player));
        }
        return bal;
    }

    @Override
    public void withdrawMoney(PlotPlayer<?> player, double amount) {
        this.econ.withdrawPlayer(getBukkitOfflinePlayer(player), amount);
    }

    @Override
    public void depositMoney(PlotPlayer<?> player, double amount) {
        this.econ.depositPlayer(getBukkitOfflinePlayer(player), amount);
    }

    @Override
    public void depositMoney(OfflinePlotPlayer player, double amount) {
        this.econ.depositPlayer(((BukkitOfflinePlayer) player).player, amount);
    }

    @Override
    public boolean isEnabled(PlotArea plotArea) {
        return plotArea.useEconomy();
    }

    @Override
    public @NonNull String format(double balance) {
        return this.econ.format(balance);
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public double getBalance(PlotPlayer<?> player) {
        return this.econ.getBalance(getBukkitOfflinePlayer(player));
    }

}
