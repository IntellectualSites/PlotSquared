/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.placeholder;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import com.google.common.eventbus.Subscribe;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.placeholders.Placeholder;
import com.plotsquared.core.util.placeholders.PlaceholderRegistry;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Placeholder support for MVdWPlaceholderAPI
 */
public class MVdWPlaceholders {

    private static final String PREFIX = "plotsquared_";
    private final Plugin plugin;
    private final PlaceholderRegistry registry;

    public MVdWPlaceholders(
            final @NonNull Plugin plugin,
            final @NonNull PlaceholderRegistry registry
    ) {
        this.plugin = plugin;
        this.registry = registry;
        for (final Placeholder placeholder : registry.getPlaceholders()) {
            this.addPlaceholder(placeholder);
        }
        PlotSquared.get().getEventDispatcher().registerListener(this);
    }

    @Subscribe
    public void onNewPlaceholder(final PlaceholderRegistry.@NonNull PlaceholderAddedEvent event) {
        this.addPlaceholder(event.placeholder());
    }

    private void addPlaceholder(final @NonNull Placeholder placeholder) {
        PlaceholderAPI.registerPlaceholder(
                plugin,
                PREFIX + String.format("%s", placeholder.getKey()),
                placeholderReplaceEvent -> {
                    if (!placeholderReplaceEvent.isOnline() || placeholderReplaceEvent.getPlayer() == null) {
                        return "";
                    }
                    final PlotPlayer<Player> player = BukkitUtil.adapt(placeholderReplaceEvent.getPlayer());
                    String key = placeholderReplaceEvent.getPlaceholder().substring(PREFIX.length());
                    return registry.getPlaceholderValue(key, player);
                }
        );
    }

}
