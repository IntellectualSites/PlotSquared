package com.intellectualcrafters.plot.listeners;

public enum PlayerBlockEventType {
    // Non interactive
    EAT,
    READ,
    
    // Right click with monster egg
    SPAWN_MOB,
    
    // Dragon egg
    TELEPORT_OBJECT,
    
    // armor stands
    PLACE_MISC,
    // blocks
    PLACE_BLOCK,
    // paintings / item frames
    PLACE_HANGING,
    // vehicles
    PLACE_VEHICLE,

    // armor stands
    BREAK_MISC,
    // blocks
    BREAK_BLOCK,
    // paintings / item frames
    BREAK_HANGING,
    BREAK_VEHICLE,
    
    // armor stands
    INTERACT_MISC,
    // blocks
    INTERACT_BLOCK,
    // vehicle
    INTERACT_VEHICLE,
    // item frame / painting
    INTERACT_HANGING,
    
    // Pressure plate, tripwire etc
    TRIGGER_PHYSICAL,
}
