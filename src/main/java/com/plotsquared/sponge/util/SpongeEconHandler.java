package com.plotsquared.sponge.util;

import java.math.BigDecimal;
import java.util.Optional;

import org.apache.commons.lang.NotImplementedException;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.sponge.object.SpongePlayer;

public class SpongeEconHandler extends EconHandler {
    
    private EconomyService econ;

    @Listener
    public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
        if (event.getService().equals(EconomyService.class)) {
            this.econ = (EconomyService) event.getNewProviderRegistration().getProvider();
        }
    }

    @Override
    public void withdrawMoney(PlotPlayer player, double amount) {
        if (econ != null) {
            Optional<UniqueAccount> uOpt = econ.getAccount(player.getUUID());
            if (uOpt.isPresent()) {
                UniqueAccount acc = uOpt.get();
                acc.withdraw(econ.getDefaultCurrency(), new BigDecimal(amount), Cause.of("PlotSquared"));
            }
        }
    }
    
    @Override
    public void depositMoney(PlotPlayer player, double amount) {
        if (econ != null) {
            Optional<UniqueAccount> uOpt = econ.getAccount(player.getUUID());
            if (uOpt.isPresent()) {
                UniqueAccount acc = uOpt.get();
                acc.deposit(econ.getDefaultCurrency(), new BigDecimal(amount), Cause.of("PlotSquared"));
            }
        }
    }
    
    @Override
    public void depositMoney(OfflinePlotPlayer player, double amount) {
        if (econ != null) {
            Optional<UniqueAccount> uOpt = econ.getAccount(player.getUUID());
            if (uOpt.isPresent()) {
                UniqueAccount acc = uOpt.get();
                acc.deposit(econ.getDefaultCurrency(), new BigDecimal(amount), Cause.of("PlotSquared"));
            }
        }
    }
    
    @Override
    public void setPermission(String world, String player, String perm, boolean value) {
        // TODO Auto-generated method stub
        throw new NotImplementedException("TODO/WIP/NOT IMPLEMENTED!");
    }
    
    @Override
    public boolean hasPermission(String world, String player, String perm) {
        SpongePlayer obj = (SpongePlayer) UUIDHandler.getPlayer(player);
        if (obj != null) {
            return obj.player.hasPermission(perm);
        }
        // TODO offline
        return false;
    }
    
    @Override
    public double getBalance(PlotPlayer player) {
        if (econ != null) {
            Optional<UniqueAccount> uOpt = econ.getAccount(player.getUUID());
            if (uOpt.isPresent()) {
                UniqueAccount acc = uOpt.get();
                BigDecimal balance = acc.getBalance(econ.getDefaultCurrency());
                return balance.doubleValue();
            }
        }
        return 0;
    }
    
}
