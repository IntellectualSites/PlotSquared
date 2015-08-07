package com.plotsquared.bukkit.util;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EconHandler;
import com.plotsquared.bukkit.object.BukkitOfflinePlayer;

public class BukkitEconHandler extends EconHandler {
    
    private Economy econ;
    private Permission perms;
    
    public Economy getEconomy() {
        init();
        return econ;
    }
    
    public Permission getPermissions() {
        init();
        return perms;
    }
    
    public boolean init() {
        if (econ == null || perms == null) {
            setupPermissions();
            setupEconomy();
        }
        return econ != null && perms != null;
    }
    
    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            perms = permissionProvider.getProvider();
        }
        return (perms != null);
    }

    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = economyProvider.getProvider();
        }
        return (econ != null);
    }

    @Override
    public double getMoney(PlotPlayer player) {
        double bal = super.getMoney(player);
        if (Double.isNaN(bal)) {
            return econ.getBalance(player.getName());
        }
        return bal;
    }

    @Override
    public void withdrawMoney(PlotPlayer player, double amount) {
        econ.withdrawPlayer(player.getName(), amount);
    }

    @Override
    public void depositMoney(PlotPlayer player, double amount) {
        econ.depositPlayer(player.getName(), amount);
    }

    @Override
    public void depositMoney(OfflinePlotPlayer player, double amount) {
        econ.depositPlayer(((BukkitOfflinePlayer) player).player, amount);
    }

    @Override
    public void setPermission(String player, String perm, boolean value) {
        if (value) {
            perms.playerAdd((World) null, player, perm);
        }
        else {
            perms.playerRemove((World) null, player, perm);
        }
    }

    @Override
    public boolean hasPermission(String player, String perm) {
        return perms.playerHas((String) null, Bukkit.getOfflinePlayer(player), perm);
    }
}
