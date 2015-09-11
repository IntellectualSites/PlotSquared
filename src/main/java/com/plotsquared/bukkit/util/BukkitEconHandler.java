package com.plotsquared.bukkit.util;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EconHandler;
import com.plotsquared.bukkit.object.BukkitOfflinePlayer;

public class BukkitEconHandler extends EconHandler
{

    private Economy econ;
    private Permission perms;

    public Economy getEconomy()
    {
        init();
        return econ;
    }

    public Permission getPermissions()
    {
        init();
        return perms;
    }

    public boolean init()
    {
        if ((econ == null) || (perms == null))
        {
            setupPermissions();
            setupEconomy();
        }
        return (econ != null) && (perms != null);
    }

    private boolean setupPermissions()
    {
        final RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null)
        {
            perms = permissionProvider.getProvider();
        }
        return (perms != null);
    }

    private boolean setupEconomy()
    {
        final RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null)
        {
            econ = economyProvider.getProvider();
        }
        return (econ != null);
    }

    @Override
    public double getMoney(final PlotPlayer player)
    {
        final double bal = super.getMoney(player);
        if (Double.isNaN(bal)) { return econ.getBalance(player.getName()); }
        return bal;
    }

    @Override
    public void withdrawMoney(final PlotPlayer player, final double amount)
    {
        econ.withdrawPlayer(player.getName(), amount);
    }

    @Override
    public void depositMoney(final PlotPlayer player, final double amount)
    {
        econ.depositPlayer(player.getName(), amount);
    }

    @Override
    public void depositMoney(final OfflinePlotPlayer player, final double amount)
    {
        econ.depositPlayer(((BukkitOfflinePlayer) player).player, amount);
    }

    @Override
    public void setPermission(final String world, final String player, final String perm, final boolean value)
    {
        if (value)
        {
            perms.playerAdd(world, player, perm);
        }
        else
        {
            perms.playerRemove(world, player, perm);
        }
    }

    @Override
    public boolean hasPermission(final String world, final String player, final String perm)
    {
        return perms.playerHas(world, Bukkit.getOfflinePlayer(player), perm);
    }
}
