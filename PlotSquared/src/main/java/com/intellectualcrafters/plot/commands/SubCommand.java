////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import org.bukkit.entity.Player;

/**
 * SubCommand class
 *
 * @author Citymonstret
 */
public abstract class SubCommand {
    public boolean isPlayer;
    /**
     * Command
     */
    public String cmd;
    /**
     * Permission node
     */
    public CommandPermission permission;
    /**
     * Simple description
     */
    public String description;
    /**
     * Alias
     */
    public String alias;

    /**
     * Command usage
     */
    public String usage;

    public CommandCategory category;

    /**
     * @param cmd         Command /plot {cmd} <-- That!
     * @param permission  Permission Node
     * @param description Simple description
     * @param usage       Usage description: /plot command {args...}
     * @param alias       Command alias
     * @param category    CommandCategory. Pick whichever closests to what you want.
     */
    public SubCommand(final String cmd, final String permission, final String description, final String usage, final String alias, final CommandCategory category, final boolean isPlayer) {
        this.cmd = cmd;
        this.permission = new CommandPermission(permission);
        this.description = description;
        this.alias = alias;
        this.usage = usage;
        this.category = category;
        this.isPlayer = isPlayer;
    }

    /**
     * @param command     Command /plot {cmd} <-- That!
     * @param description Simple description
     * @param usage       Usage description: /plot command {args...}
     * @param category    CommandCategory. Pick whichever closests to what you want.
     */
    public SubCommand(final Command command, final String description, final String usage, final CommandCategory category, final boolean isPlayer) {
        this.cmd = command.getCommand();
        this.permission = command.getPermission();
        this.alias = command.getAlias();
        this.description = description;
        this.usage = usage;
        this.category = category;
        this.isPlayer = isPlayer;
    }

    /**
     * Execute.
     *
     * @param plr  executor
     * @param args arguments
     * @return true on success, false on failure
     */
    public abstract boolean execute(final Player plr, final String... args);

    public void executeConsole(final String... args) {
        this.execute(null, args);
    }

    /**
     * Send a message
     *
     * @param plr
     * @param c
     * @param args
     */
    public boolean sendMessage(final Player plr, final C c, final String... args) {
        PlayerFunctions.sendMessage(plr, c, args);
        return true;
    }

    public enum CommandCategory {
        CLAIMING("Claiming"),
        TELEPORT("Teleportation"),
        ACTIONS("Actions"),
        INFO("Information"),
        DEBUG("Debug");
        private String name;

        CommandCategory(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
