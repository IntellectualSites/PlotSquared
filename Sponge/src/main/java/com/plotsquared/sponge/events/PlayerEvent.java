package com.plotsquared.sponge.events;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

public abstract class PlayerEvent extends AbstractEvent {
    
    public final Player player;
    
    public PlayerEvent(final Player player) {
        this.player = player;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public Cause getCause() {
        return null;
    }

}
