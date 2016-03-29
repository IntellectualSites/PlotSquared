package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.Permissions;

/**
 * Created by Citymonstret on 2014-08-03.
 *

 */
public class CommandPermission {

    /**
     * Permission Node
     */
    public final String permission;

    /**
     * @param permission Command Permission
     */
    public CommandPermission(String permission) {
        this.permission = permission.toLowerCase();
    }

    /**
     * @param player Does the player have the permission?
     *
     * @return true of player has the required permission node
     */
    public boolean hasPermission(PlotPlayer player) {
        return Permissions.hasPermission(player, this.permission);
    }
}
