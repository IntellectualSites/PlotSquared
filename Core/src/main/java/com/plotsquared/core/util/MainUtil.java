/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.util;

import com.plotsquared.core.configuration.Caption;
import com.plotsquared.core.configuration.CaptionUtility;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.task.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * plot functions
 */
public class MainUtil {

    private static final Logger logger =
        LoggerFactory.getLogger("P2/" + MainUtil.class.getSimpleName());

    /**
     * Cache of mapping x,y,z coordinates to the chunk array<br>
     * - Used for efficient world generation<br>
     */
    public static short[][] x_loc;
    public static short[][] y_loc;
    public static short[][] z_loc;
    public static short[][][] CACHE_I = null;
    public static short[][][] CACHE_J = null;

    /**
     * This cache is used for world generation and just saves a bit of calculation time when checking if something is in the plot area.
     */
    public static void initCache() {
        if (x_loc == null) {
            x_loc = new short[16][4096];
            y_loc = new short[16][4096];
            z_loc = new short[16][4096];
            for (int i = 0; i < 16; i++) {
                int i4 = i << 4;
                for (int j = 0; j < 4096; j++) {
                    int y = i4 + (j >> 8);
                    int a = j - ((y & 0xF) << 8);
                    int z1 = a >> 4;
                    int x1 = a - (z1 << 4);
                    x_loc[i][j] = (short) x1;
                    y_loc[i][j] = (short) y;
                    z_loc[i][j] = (short) z1;
                }
            }
        }
        if (CACHE_I == null) {
            CACHE_I = new short[256][16][16];
            CACHE_J = new short[256][16][16];
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 256; y++) {
                        short i = (short) (y >> 4);
                        short j = (short) ((y & 0xF) << 8 | z << 4 | x);
                        CACHE_I[y][x][z] = i;
                        CACHE_J[y][x][z] = j;
                    }
                }
            }
        }
    }

    /**
     * Send a message to the player.
     *
     * @param player  Player to receive message
     * @param message Message to send
     * @return true Can be used in things such as commands (return PlayerFunctions.sendMessage(...))
     */
    public static boolean sendMessage(PlotPlayer<?> player, String message) {
        return sendMessage(player, message, true);
    }

    /**
     * Send a message to console.
     *
     * @param caption
     * @param args
     */
    public static void sendConsoleMessage(Captions caption, String... args) {
        sendMessage(null, caption, args);
    }

    /**
     * Send a message to a player.
     *
     * @param player Can be null to represent console, or use ConsolePlayer.getConsole()
     * @param msg
     * @param prefix If the message should be prefixed with the configured prefix
     * @return
     */
    public static boolean sendMessage(PlotPlayer<?> player, @Nonnull String msg, boolean prefix) {
        if (!msg.isEmpty()) {
            if (player == null) {
                String message = CaptionUtility
                    .format(null, (prefix ? Captions.PREFIX.getTranslated() : "") + msg);
                logger.info(message);
            } else {
                player.sendMessage(CaptionUtility.format(player,
                    (prefix ? Captions.PREFIX.getTranslated() : "") + Captions.color(msg)));
            }
        }
        return true;
    }

    /**
     * Send a message to the player.
     *
     * @param player  the recipient of the message
     * @param caption the message to send
     * @return boolean success
     */
    public static boolean sendMessage(PlotPlayer<?> player, Caption caption, String... args) {
        return sendMessage(player, caption, (Object[]) args);
    }

    /**
     * Send a message to the player
     *
     * @param player  the recipient of the message
     * @param caption the message to send
     * @return boolean success
     */
    public static boolean sendMessage(final PlotPlayer<?> player, final Caption caption,
        final Object... args) {
        if (caption.getTranslated().isEmpty()) {
            return true;
        }
        TaskManager.runTaskAsync(() -> {
            String m = CaptionUtility.format(player, caption, args);
            if (player == null) {
                logger.info(m);
            } else {
                player.sendMessage(m);
            }
        });
        return true;
    }

}
