package com.github.intellectualsites.plotsquared.commands;

public interface CommandCaller {

    /**
     * Send the player a message.
     *
     * @param message the message to send
     */
    void sendMessage(String message);

    /**
     * Check the player's permissions. <i>Will be cached if permission caching is enabled.</i>
     *
     * @param permission the name of the permission
     */
    boolean hasPermission(String permission);

    boolean isPermissionSet(String permission);

    RequiredType getSuperCaller();
}
