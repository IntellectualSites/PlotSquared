package com.plotsquared.listener;

public enum PlayerBlockEventType {
    // Non interactive
    READ,

    // Right click with monster egg
    SPAWN_MOB,

    // Dragon egg
    TELEPORT_OBJECT,

    // armor stands
    PLACE_MISC, PLACE_VEHICLE,

    // armor stands
    INTERACT_BLOCK, // blocks

    // Pressure plate, tripwire etc
    TRIGGER_PHYSICAL,
}
