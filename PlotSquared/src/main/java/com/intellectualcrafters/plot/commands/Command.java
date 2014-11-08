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

/**
 * Created by Citymonstret on 2014-08-03.
 *
 * @author Citymonstret
 */
public enum Command {

    // TODO new commands
    // (economy)
    // - /plot buy
    // - /plot sell <value>
    // (Rating system) (ratings can be stored as the average, and number of
    // ratings)
    // - /plot rate <number out of 10>
    // - /plot list <some parameter to list the most popular, and highest rated
    // plots>
    SWAP("swap"),
    /**
     *
     */
    INBOX("inbox"),
    /**
     *
     */
    DEBUGCLAIMTEST("debugclaimtest"),
    /**
     *
     */
    COMMENT("comment", "msg"),
    /**
     *
     */
    TRUSTED("trusted", "trust"),
    /**
     *
     */
    PASTE("paste"),
    CLIPBOARD("clipboard", "cboard"),
    COPY("copy"),
    /**
     *
     */
    KICK("kick", "k"),
    /**
     *
     */
    HELPERS("helpers", "hp"),
    /**
     *
     */
    DENIED("denied", "dn"),
    /**
     *
     */
    CLAIM("claim", "c"),
    /**
     *
     */
    MERGE("merge", "m"),
    /**
     *
     */
    UNLINK("unlink", "u"),
    /**
     *
     */
    CLEAR("clear", "clear", new CommandPermission("plots.clear")),
    /**
     *
     */
    DELETE("delete", "d", new CommandPermission("plots.delete")),
    /**
     *
     */
    DEBUG("debug", "database", new CommandPermission("plots.admin")),
    /**
     *
     */
    INTERFACE("interface", "int", new CommandPermission("plots.interface")),
    /**
     *
     */
    HOME("home", "h"),
    /**
     *
     */
    INFO("info", "i"),
    /**
     *
     */
    LIST("list", "l"),
    /**
     *
     */
    SET("set", "s"),
    /**
     *
     */
    PURGE("purge"),
    /**
     *
     */
    SETUP("setup"),
    /**
     *
     */
    TP("tp", "tp");
    /**
     *
     */
    private String command;
    /**
     *
     */
    private String alias;
    /**
     *
     */
    private CommandPermission permission;

    /**
     * @param command
     */
    Command(final String command) {
        this.command = command;
        this.alias = command;
        this.permission = new CommandPermission("plots." + command);
    }

    /**
     * @param command
     * @param permission
     */
    Command(final String command, final CommandPermission permission) {
        this.command = command;
        this.permission = permission;
        this.alias = command;
    }

    /**
     * @param command
     * @param alias
     */
    Command(final String command, final String alias) {
        this.command = command;
        this.alias = alias;
        this.permission = new CommandPermission("plots." + command);
    }

    /**
     * @param Command
     * @param alias
     * @param permission
     */
    Command(final String command, final String alias, final CommandPermission permission) {
        this.command = command;
        this.alias = alias;
        this.permission = permission;
    }

    /**
     * @return
     */
    public String getCommand() {
        return this.command;
    }

    /**
     * @return
     */
    public String getAlias() {
        return this.alias;
    }

    /**
     * @return
     */
    public CommandPermission getPermission() {
        return this.permission;
    }
}
