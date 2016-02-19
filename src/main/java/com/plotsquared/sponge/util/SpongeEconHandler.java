package com.plotsquared.sponge.util;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.sponge.object.SpongePlayer;
import org.apache.commons.lang.NotImplementedException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import java.math.BigDecimal;
import java.util.Optional;

public class SpongeEconHandler extends EconHandler {
    private EconomyService economyService;
    public SpongeEconHandler() {
        if (Sponge.getServiceManager().isRegistered(EconomyService.class)) {
            economyService = Sponge.getServiceManager().provide(EconomyService.class).get();
        } else {
            PS.log("No economy service was registered.");
        }
    }

    @Override
    public void withdrawMoney(PlotPlayer player, double amount) {
        Optional<UniqueAccount> account = economyService.getAccount(player.getUUID());
        if (account.isPresent()) {
            UniqueAccount acc = account.get();
            acc.withdraw(economyService.getDefaultCurrency(),BigDecimal.valueOf(amount), Cause.of("PlotSquared"));
        }
    }
    
    @Override
    public void depositMoney(PlotPlayer player, double amount) {
        Optional<UniqueAccount> account = economyService.getAccount(player.getUUID());
        if (account.isPresent()) {
            UniqueAccount acc = account.get();
            acc.deposit(economyService.getDefaultCurrency(),BigDecimal.valueOf(amount), Cause.of("PlotSquared"));
        }
    }
    
    @Override
    public void depositMoney(OfflinePlotPlayer player, double amount) {
        Optional<UniqueAccount> account = economyService.getAccount(player.getUUID());
        if (account.isPresent()) {
            UniqueAccount acc = account.get();
            acc.withdraw(economyService.getDefaultCurrency(),BigDecimal.valueOf(amount), Cause.of("PlotSquared"));
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
    
}
