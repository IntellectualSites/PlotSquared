package com.plotsquared.bukkit.util;

import com.plotsquared.bukkit.player.BukkitOfflinePlayer;
import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.EconHandler;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class BukkitEconHandler extends EconHandler {

    private Economy econ;
    private Permission perms;

    public boolean init() {
        if (this.econ == null || this.perms == null) {
            setupPermissions();
            setupEconomy();
        }
        return this.econ != null && this.perms != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider =
            Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        if (permissionProvider != null) {
            this.perms = permissionProvider.getProvider();
        }
        return this.perms != null;
    }

    private boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> economyProvider =
            Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            this.econ = economyProvider.getProvider();
        }
        return this.econ != null;
    }

    @Override public double getMoney(PlotPlayer player) {
        double bal = super.getMoney(player);
        if (Double.isNaN(bal)) {
            return this.econ.getBalance(((BukkitPlayer) player).player);
        }
        return bal;
    }

    @Override public void withdrawMoney(PlotPlayer player, double amount) {
        this.econ.withdrawPlayer(((BukkitPlayer) player).player, amount);
    }

    @Override public void depositMoney(PlotPlayer player, double amount) {
        this.econ.depositPlayer(((BukkitPlayer) player).player, amount);
    }

    @Override public void depositMoney(OfflinePlotPlayer player, double amount) {
        this.econ.depositPlayer(((BukkitOfflinePlayer) player).player, amount);
    }

    @Override public boolean hasPermission(String world, String player, String perm) {
        return this.perms.playerHas(world, Bukkit.getOfflinePlayer(player), perm);
    }

    @Override public double getBalance(PlotPlayer player) {
        return this.econ.getBalance(player.getName());
    }

    public void setPermission(String world, String player, String perm, boolean value) {
        if (value) {
            this.perms.playerAdd(world, player, perm);
        } else {
            this.perms.playerRemove(world, player, perm);
        }
    }

}
