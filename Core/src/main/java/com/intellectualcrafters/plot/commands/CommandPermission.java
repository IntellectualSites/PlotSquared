package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.Permissions;

public class CommandPermission {

    /**
     * Permission Node.
     */
    public final String permission;

    /**
     * Command Permission
     * @param permission Command Permission
     */
    public CommandPermission(String permission) {
        this.permission = permission.toLowerCase();
    }

    /**
     * Check the permissions of a player.
     * @param player The player to check permissions for
     *
     * @return true of player has the required permission node
     */
    public boolean hasPermission(PlotPlayer player) {
        return Permissions.hasPermission(player, this.permission);
    }
}
