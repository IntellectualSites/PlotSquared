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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.util;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.FuzzyBlockState;
import com.sk89q.worldedit.world.registry.LegacyMapper;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Map;

/**
 * {@link BlockState} related utility methods
 */
public final class BlockUtil {

    private static final ParserContext PARSER_CONTEXT = new ParserContext();
    private static final InputParser<BaseBlock> PARSER;

    static {
        PARSER_CONTEXT.setRestricted(false);
        PARSER_CONTEXT.setPreferringWildcard(false);
        PARSER_CONTEXT.setTryLegacy(true);
        PARSER = WorldEdit.getInstance().getBlockFactory().getParsers().get(0);
    }

    private BlockUtil() {
    }

    /**
     * Get a {@link BlockState} from a legacy id
     *
     * @param id Legacy ID
     * @return Block state, or {@code null}
     */
    @Nullable public static BlockState get(@Nonnegative final int id) {
        return LegacyMapper.getInstance().getBlockFromLegacy(id);
    }

    /**
     * Get a {@link BlockState} from a legacy id-data pair
     *
     * @param id Legacy ID
     * @param data Legacy data
     * @return Block state, or {@code null}
     */
    @Nullable public static BlockState get(@Nonnegative final int id, final int data) {
        return LegacyMapper.getInstance().getBlockFromLegacy(id, data);
    }

    /**
     * Get a {@link BlockState} from its ID
     *
     * @param id String or integer ID
     * @return Parsed block state, or {@code null} if none
     *         could be parsed
     */
   @Nullable public static BlockState get(@Nonnull String id) {
        if (id.length() == 1 && id.charAt(0) == '*') {
            return FuzzyBlockState.builder().type(BlockTypes.AIR).build();
        }
        id = id.toLowerCase();
        BlockType type = BlockTypes.get(id);
        if (type != null) {
            return type.getDefaultState();
        }
        if (Character.isDigit(id.charAt(0))) {
            String[] split = id.split(":");
            if (MathMan.isInteger(split[0])) {
                if (split.length == 2) {
                    if (MathMan.isInteger(split[1])) {
                        return get(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                    }
                } else {
                    return get(Integer.parseInt(split[0]));
                }
            }
        }
        try {
            BaseBlock block = PARSER.parseFromInput(id, PARSER_CONTEXT);
            return block.toImmutableState();
        } catch (InputParseException e) {
            return null;
        }
    }

    /**
     * Parse a comma delimited list of block states
     *
     * @param commaDelimited List of block states
     * @return Parsed block states
     */
    @Nonnull public static BlockState[] parse(@Nonnull final String commaDelimited) {
        final String[] split = commaDelimited.split(",(?![^\\(\\[]*[\\]\\)])");
        final BlockState[] result = new BlockState[split.length];
        for (int i = 0; i < split.length; i++) {
            result[i] = get(split[i]);
        }
        return result;
    }

    /**
     * Deserialize a serialized {@link BlockState}
     *
     * @param map Serialized block state
     * @return Deserialized block state, or {@code null} if the map is
     *         not a properly serialized block state
     */
    @Nullable public static BlockState deserialize(@Nonnull final Map<String, Object> map) {
        if (map.containsKey("material")) {
            final Object object = map.get("material");
            return get(object.toString());
        }
        return null;
    }

}
