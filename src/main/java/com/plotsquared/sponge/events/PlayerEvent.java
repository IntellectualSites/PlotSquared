package com.plotsquared.sponge.events;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.AbstractEvent;

public abstract class PlayerEvent extends AbstractEvent {
    
    public final Player player;
    
    public PlayerEvent(final Player player) {
        this.player = player;
    }
    
    public Player getPlayer() {
        return player;
    }
    
}
