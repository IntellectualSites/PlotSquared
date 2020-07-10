package com.plotsquared.core.inject.factory;

import com.plotsquared.core.backup.PlayerBackupProfile;
import com.plotsquared.core.plot.Plot;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface PlayerBackupProfileFactory {

    PlayerBackupProfile create(@NotNull UUID uuid, @NotNull Plot plot);

}
