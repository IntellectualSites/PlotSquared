package com.github.intellectualsites.plotsquared.commands;

import com.github.intellectualsites.plotsquared.plot.commands.RequiredType;
import net.kyori.text.Component;

public interface CommandCaller {

    /**
     * Send the player a message.
     *
     * @param message the message to send
     */
    void sendMessage(String message);

    /**
     * Send the player a message.
     *
     * @param message the message to send
     */
    void sendMessage(Component message);

    /**
     * Check the player's permissions. <i>Will be cached if permission caching is enabled.</i>
     *
     * @param permission the name of the permission
     */
    boolean hasPermission(String permission);

    boolean isPermissionSet(String permission);

    RequiredType getSuperCaller();
}
