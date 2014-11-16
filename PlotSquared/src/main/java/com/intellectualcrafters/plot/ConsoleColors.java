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

package com.intellectualcrafters.plot;

import org.bukkit.ChatColor;

public class ConsoleColors {

    public static String fromString(String input) {
        input = input.replaceAll("&0", fromChatColor(ChatColor.BLACK)).replaceAll("&1", fromChatColor(ChatColor.DARK_BLUE)).replaceAll("&2", fromChatColor(ChatColor.DARK_GREEN)).replaceAll("&3", fromChatColor(ChatColor.DARK_AQUA)).replaceAll("&4", fromChatColor(ChatColor.DARK_RED)).replaceAll("&5", fromChatColor(ChatColor.DARK_PURPLE)).replaceAll("&6", fromChatColor(ChatColor.GOLD)).replaceAll("&7", fromChatColor(ChatColor.GRAY)).replaceAll("&8", fromChatColor(ChatColor.DARK_GRAY)).replaceAll("&9", fromChatColor(ChatColor.BLUE)).replaceAll("&a", fromChatColor(ChatColor.GREEN)).replaceAll("&b", fromChatColor(ChatColor.AQUA)).replaceAll("&c", fromChatColor(ChatColor.RED)).replaceAll("&d", fromChatColor(ChatColor.LIGHT_PURPLE)).replaceAll("&e", fromChatColor(ChatColor.YELLOW)).replaceAll("&f", fromChatColor(ChatColor.WHITE)).replaceAll("&k", fromChatColor(ChatColor.MAGIC)).replaceAll("&l", fromChatColor(ChatColor.BOLD)).replaceAll("&m", fromChatColor(ChatColor.STRIKETHROUGH))
                .replaceAll("&n", fromChatColor(ChatColor.UNDERLINE)).replaceAll("&o", fromChatColor(ChatColor.ITALIC)).replaceAll("&r", fromChatColor(ChatColor.RESET));
        return input + "\u001B[0m";
    }

    /*
     * public static final String ANSI_RESET = "\u001B[0m"; public static final
     * String ANSI_BLACK = "\u001B[30m"; public static final String ANSI_RED =
     * "\u001B[31m"; public static final String ANSI_GREEN = "\u001B[32m";
     * public static final String ANSI_YELLOW = "\u001B[33m"; public static
     * final String ANSI_BLUE = "\u001B[34m"; public static final String
     * ANSI_PURPLE = "\u001B[35m"; public static final String ANSI_CYAN =
     * "\u001B[36m"; public static final String ANSI_WHITE = "\u001B[37m";
     * public static final String ANSI_BOLD = "\033[1m"; public static final
     * String ANSI_UNDERLINE = "\033[0m"; public static final String ANSI_ITALIC
     * = "\033[3m]";
     */

    public static String fromChatColor(final ChatColor color) {
        return chatColor(color).getLin();
    }

    public static ConsoleColor chatColor(final ChatColor color) {
        switch (color) {
            case RESET:
                return ConsoleColor.RESET;
            case GRAY:
            case DARK_GRAY:
                return ConsoleColor.WHITE;
            case BLACK:
                return ConsoleColor.BLACK;
            case DARK_RED:
            case RED:
                return ConsoleColor.RED;
            case GOLD:
            case YELLOW:
                return ConsoleColor.YELLOW;
            case DARK_GREEN:
            case GREEN:
                return ConsoleColor.GREEN;
            case AQUA:
            case DARK_AQUA:
                return ConsoleColor.CYAN;
            case LIGHT_PURPLE:
            case DARK_PURPLE:
                return ConsoleColor.PURPLE;
            case BLUE:
            case DARK_BLUE:
                return ConsoleColor.BLUE;
            case UNDERLINE:
                return ConsoleColor.UNDERLINE;
            case ITALIC:
                return ConsoleColor.ITALIC;
            case BOLD:
                return ConsoleColor.BOLD;
            default:
                return ConsoleColor.RESET;
        }
    }

    static enum ConsoleColor {
        RESET("\u001B[0m"),
        BLACK("\u001B[30m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        PURPLE("\u001B[35m"),
        CYAN("\u001B[36m"),
        WHITE("\u001B[37m"),
        BOLD("\033[1m"),
        UNDERLINE("\033[0m"),
        ITALIC("\033[3m");

        private final String win;
        private final String lin;

        ConsoleColor(final String lin) {
            this.lin = lin;
            this.win = lin;
        }

        @SuppressWarnings("unused")
        public String getWin() {
            return this.win;
        }

        public String getLin() {
            return this.lin;
        }
    }
}
