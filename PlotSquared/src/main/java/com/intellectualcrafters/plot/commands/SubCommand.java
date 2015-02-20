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

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.util.bukkit.PlayerFunctions;

/**
 * SubCommand class
 *
 * @author Citymonstret
 */
@SuppressWarnings({"deprecation", "unused"}) public abstract class SubCommand {

    /**
     * Command
     */
    public final String cmd;
    /**
     * Permission node
     */
    public final CommandPermission permission;
    /**
     * Simple description
     */
    public final String description;
    /**
     * Aliases
     */
    public final ArrayList<String> alias;
    /**
     * Command usage
     */
    public final String usage;
    /**
     * The category
     */
    public final CommandCategory category;
    /**
     * Is this a player-online command?
     */
    public final boolean isPlayer;

    /**
     * @param cmd         Command /plot {cmd} <-- That!
     * @param permission  Permission Node
     * @param description Simple description
     * @param usage       Usage description: /plot command {args...}
     * @param alias       Command alias
     * @param category    CommandCategory. Pick whichever is closest to what you want.
     */
    public SubCommand(final String cmd, final String permission, final String description, final String usage, final String alias, final CommandCategory category, final boolean isPlayer) {
        this.cmd = cmd;
        this.permission = new CommandPermission(permission);
        this.description = description;
        this.alias = new ArrayList<String>();
        this.alias.add(alias);
        this.usage = usage;
        this.category = category;
        this.isPlayer = isPlayer;
    }

    /**
     * @param cmd         Command /plot {cmd} <-- That!
     * @param permission  Permission Node
     * @param description Simple description
     * @param usage       Usage description: /plot command {args...}
     * @param aliases     Command aliases
     * @param category    CommandCategory. Pick whichever is closest to what you want.
     */
    public SubCommand(final String cmd, final String permission, final String description, final String usage, final CommandCategory category, final boolean isPlayer, final String... aliases) {
        this.cmd = cmd;
        this.permission = new CommandPermission(permission);
        this.description = description;
        this.alias = new ArrayList<String>();
        this.alias.addAll(Arrays.asList(aliases));
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
        this.alias = new ArrayList<String>();
        this.alias.add(command.getAlias());
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
     *
     * @return true on success, false on failure
     */
    public abstract boolean execute(final Player plr, final String... args);

    /**
     * Execute the command as console
     *
     * @param args Arguments
     */
    public void executeConsole(final String... args) {
        this.execute(null, args);
    }

    /**
     * Send a message
     *
     * @param plr  Player who will receive the mssage
     * @param c    Caption
     * @param args Arguments (%s's)
     *
     * @see com.intellectualcrafters.plot.util.bukkit.PlayerFunctions#sendMessage(org.bukkit.entity.Player,
     * com.intellectualcrafters.plot.config.C, String...)
     */
    public boolean sendMessage(final Player plr, final C c, final String... args) {
        PlayerFunctions.sendMessage(plr, c, args);
        return true;
    }

    /**
     * CommandCategory
     *
     * @author Citymonstret
     * @author Empire92
     */
    public enum CommandCategory {
        /**
         * Claiming Commands
         * <p/>
         * Such as: /plot claim
         */
        CLAIMING("Claiming"),
        /**
         * Teleportation Commands
         * <p/>
         * Such as: /plot visit
         */
        TELEPORT("Teleportation"),
        /**
         * Action Commands
         * <p/>
         * Such as: /plot clear
         */
        ACTIONS("Actions"),
        /**
         * Information Commands
         * <p/>
         * Such as: /plot info
         */
        INFO("Information"),
        /**
         * Debug Commands
         * <p/>
         * Such as: /plot debug
         */
        DEBUG("Debug");

        /**
         * The category name (Readable)
         */
        private final String name;

        /**
         * Constructor
         *
         * @param name readable name
         */
        CommandCategory(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
