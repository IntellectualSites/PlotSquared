package com.plotsquared.sponge.util;

import com.google.inject.Singleton;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.EconHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import java.util.Optional;


@Singleton
public class SpongeEconHandler extends EconHandler {

    private final Optional<EconomyService> economyService; //TODO maybe don't store an optional?

    public SpongeEconHandler() {
        economyService = Sponge.serviceProvider().provide(EconomyService.class);
    }

    @Override
    public boolean init() {
        return Sponge.serviceProvider().provide(EconomyService.class).isPresent();
    }

    @Override
    public double getBalance(final PlotPlayer<?> player) {
        if(economyService.isPresent()) {
            final Optional<UniqueAccount> account = economyService.get().findOrCreateAccount(player.getUUID());
            return account
                    .map(uniqueAccount -> uniqueAccount.balance(economyService.get().defaultCurrency()).doubleValue())
                    .orElse(0.0);
        }
        return 0;
    }

    @Override
    public void withdrawMoney(final PlotPlayer<?> player, final double amount) {

    }

    @Override
    public void depositMoney(final PlotPlayer<?> player, final double amount) {

    }

    @Override
    public void depositMoney(final OfflinePlotPlayer player, final double amount) {
    }

    @Override
    public boolean isEnabled(final PlotArea plotArea) {
        return false;
    }

    @Override
    public @NonNull String format(final double balance) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSupported() {
        return Sponge.serviceProvider().provide(EconomyService.class).isPresent();
    }

}
