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

import com.google.common.base.Preconditions;
import com.plotsquared.core.command.Command;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.player.PlotPlayer;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import org.jetbrains.annotations.NotNull;

public class PatternUtil {

    public static BaseBlock apply(@NotNull Pattern pattern, int x, int y, int z) {
        Preconditions.checkNotNull(pattern, "Pattern may not be null");
        if (pattern instanceof BlockPattern || pattern instanceof RandomPattern
            || pattern instanceof BlockState || pattern instanceof BlockType
            || pattern instanceof BaseBlock) {
            return pattern.apply(BlockVector3.ZERO);
        }
        return pattern.apply(BlockVector3.at(x, y, z));
    }

    public static Pattern parse(PlotPlayer plotPlayer, String input) {
        return parse(plotPlayer, input, true);
    }

    public static Pattern parse(PlotPlayer plotPlayer, String input, boolean allowLegacy) {
        ParserContext context = new ParserContext();
        if (plotPlayer != null) {
            Actor actor = plotPlayer.toActor();
            context.setActor(actor);
            if (actor instanceof Player) {
                context.setWorld(((Player) actor).getWorld());
            }
            context.setSession(WorldEdit.getInstance().getSessionManager().get(actor));
            context.setRestricted(true);
        } else {
            context.setRestricted(false);
        }
        context.setPreferringWildcard(false);
        context.setTryLegacy(allowLegacy);
        try {
            return WorldEdit.getInstance().getPatternFactory().parseFromInput(input, context);
        } catch (InputParseException e) {
            throw new Command.CommandException(Captions.NOT_VALID_BLOCK, e.getMessage());
        }
    }

    public static boolean isAir(Pattern pattern) {
        if (pattern instanceof BlockPattern) {
            return ((BlockPattern) pattern).getBlock().getBlockType().getMaterial().isAir();
        }
        if (pattern instanceof BlockState) {
            return ((BlockState) pattern).getBlockType().getMaterial().isAir();
        }
        if (pattern instanceof BlockType) {
            return ((BlockType) pattern).getMaterial().isAir();
        }
        if (pattern instanceof BaseBlock) {
            return ((BaseBlock) pattern).getBlockType().getMaterial().isAir();
        }
        return false;
    }
}
