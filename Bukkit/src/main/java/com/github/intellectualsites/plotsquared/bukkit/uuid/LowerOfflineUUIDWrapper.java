package com.github.intellectualsites.plotsquared.bukkit.uuid;

import com.github.intellectualsites.plotsquared.plot.object.OfflinePlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.google.common.base.Charsets;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class LowerOfflineUUIDWrapper extends OfflineUUIDWrapper {

    @NotNull @Override public UUID getUUID(PlotPlayer player) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName().toLowerCase()).getBytes(Charsets.UTF_8));
    }

    @Override public UUID getUUID(OfflinePlotPlayer player) {
        return UUID.nameUUIDFromBytes(
            ("OfflinePlayer:" + player.getName().toLowerCase()).getBytes(Charsets.UTF_8));
    }

    @Override public UUID getUUID(OfflinePlayer player) {
        return UUID.nameUUIDFromBytes(
            ("OfflinePlayer:" + Objects.requireNonNull(player.getName()).toLowerCase()).getBytes(Charsets.UTF_8));
    }

    @Override public UUID getUUID(String name) {
        return UUID
            .nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase()).getBytes(Charsets.UTF_8));
    }

}
