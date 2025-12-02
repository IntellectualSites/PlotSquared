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
package com.plotsquared.bukkit.event;

import com.plotsquared.bukkit.player.BukkitPlayer;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockDamageEvent;

/**
 * Event that represents a player using (interacting with) a block in a plot.
 * This is a simple cancellable wrapper around the original {@link BlockDamageEvent}.
 */
public class UseBlockEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final BlockDamageEvent original;
    private final BukkitPlayer player;
    private final Block block;
    private boolean cancelled = false;

    public UseBlockEvent(BlockDamageEvent original, BukkitPlayer player, Block block) {
        this.original = original;
        this.player = player;
        this.block = block;
    }

    public BlockDamageEvent getOriginal() {
        return original;
    }

    public BukkitPlayer getPlotPlayer() {
        return player;
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
