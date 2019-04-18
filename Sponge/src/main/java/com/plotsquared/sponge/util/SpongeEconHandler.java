package com.plotsquared.sponge.util;

import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.sponge.object.SpongePlayer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import java.math.BigDecimal;
import java.util.Optional;

public class SpongeEconHandler extends EconHandler {
    private EconomyService econ;

    public SpongeEconHandler() {
        if (Sponge.getServiceManager().isRegistered(EconomyService.class)) {
            //noinspection OptionalGetWithoutIsPresent
            econ = Sponge.getServiceManager().provide(EconomyService.class).get();
        }
    }
    
    @Listener
    public void onChangeServiceProvider(ChangeServiceProviderEvent event) {
        if (event.getService().equals(EconomyService.class)) {
            econ = (EconomyService) event.getNewProviderRegistration().getProvider();
        }
    }

    @Override
    public void withdrawMoney(PlotPlayer player, double amount) {
        if (econ != null) {
            Optional<UniqueAccount> accOpt = econ.getOrCreateAccount(player.getUUID());
            if (accOpt.isPresent()) {
                UniqueAccount acc = accOpt.get();

                acc.withdraw(econ.getDefaultCurrency(), new BigDecimal(amount), SpongeUtil.CAUSE);
            }
        }
    }
    
    @Override
    public void depositMoney(PlotPlayer player, double amount) {
        if (econ != null) {
            Optional<UniqueAccount> accOpt = econ.getOrCreateAccount(player.getUUID());
            if (accOpt.isPresent()) {
                UniqueAccount acc = accOpt.get();
                acc.deposit(econ.getDefaultCurrency(), new BigDecimal(amount), SpongeUtil.CAUSE);
            }
        }
    }
    
    @Override
    public void depositMoney(OfflinePlotPlayer player, double amount) {
        if (econ != null) {
            Optional<UniqueAccount> accOpt = econ.getOrCreateAccount(player.getUUID());
            if (accOpt.isPresent()) {
                UniqueAccount acc = accOpt.get();
                acc.deposit(econ.getDefaultCurrency(), new BigDecimal(amount), SpongeUtil.CAUSE);
            }
        }
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
            Optional<UniqueAccount> accOpt = econ.getOrCreateAccount(player.getUUID());
            if (accOpt.isPresent()) {
                UniqueAccount acc = accOpt.get();
                BigDecimal balance = acc.getBalance(econ.getDefaultCurrency());
                return balance.doubleValue();
            }
        }
        return 0;
    }
    
}
