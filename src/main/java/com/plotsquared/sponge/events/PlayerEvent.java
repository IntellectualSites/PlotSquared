package com.plotsquared.sponge.events;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.AbstractEvent;

public abstract class PlayerEvent extends AbstractEvent {

    public final Player player;

    public PlayerEvent(Player player) {
        this.player = player;
    }
    
    public Player getPlayer() {
        return this.player;
    }
    
}
