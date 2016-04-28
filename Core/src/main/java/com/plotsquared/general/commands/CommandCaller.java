package com.plotsquared.general.commands;

import com.intellectualcrafters.plot.commands.RequiredType;

public interface CommandCaller {

    /**
     * Send the player a message.
     */
    void sendMessage(String message);

    /**
     * Check the player's permissions. Will be cached if permission caching is enabled.
     */
    boolean hasPermission(String perm);
    
    RequiredType getSuperCaller();
}
