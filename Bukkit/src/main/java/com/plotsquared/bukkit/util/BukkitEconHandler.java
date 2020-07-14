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
package com.plotsquared.bukkit.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.plotsquared.bukkit.player.BukkitOfflinePlayer;
import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.PermHandler;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import javax.annotation.Nullable;

@Singleton public class BukkitEconHandler extends EconHandler {

    private Economy econ;

    private final PermHandler permHandler;

    @Inject public BukkitEconHandler(@Nullable final PermHandler permHandler) {
        this.permHandler = permHandler;
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

    @Override public double getMoney(PlotPlayer<?> player) {
        double bal = super.getMoney(player);
        if (Double.isNaN(bal)) {
            return this.econ.getBalance(((BukkitPlayer) player).player);
        }
        return bal;
    }

    @Override public void withdrawMoney(PlotPlayer<?> player, double amount) {
        this.econ.withdrawPlayer(((BukkitPlayer) player).player, amount);
    }

    @Override public void depositMoney(PlotPlayer<?> player, double amount) {
        this.econ.depositPlayer(((BukkitPlayer) player).player, amount);
    }

    @Override public void depositMoney(OfflinePlotPlayer player, double amount) {
        this.econ.depositPlayer(((BukkitOfflinePlayer) player).player, amount);
    }

    /**
     * @deprecated Use {@link PermHandler#hasPermission(String, String, String)} instead
     */
    @Deprecated @Override public boolean hasPermission(String world, String player, String perm) {
        if (this.permHandler != null) {
            return this.permHandler.hasPermission(world, player, perm);
        } else {
            return false;
        }
    }

    @Override public double getBalance(PlotPlayer<?> player) {
        return this.econ.getBalance(player.getName());
    }

}
