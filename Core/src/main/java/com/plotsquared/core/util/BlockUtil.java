/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

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
        List<InputParser<BaseBlock>> parsers = WorldEdit.getInstance().getBlockFactory().getParsers();
        PARSER = parsers.get(parsers.size() - 1); // Default parser is always at the end
    }

    private BlockUtil() {
    }

    /**
     * Get a {@link BlockState} from a legacy id
     *
     * @param id Legacy ID
     * @return Block state, or {@code null}
     */
    public static @Nullable BlockState get(final int id) {
        return LegacyMapper.getInstance().getBlockFromLegacy(id);
    }

    /**
     * Get a {@link BlockState} from a legacy id-data pair
     *
     * @param id   Legacy ID
     * @param data Legacy data
     * @return Block state, or {@code null}
     */
    public static @Nullable BlockState get(final int id, final int data) {
        return LegacyMapper.getInstance().getBlockFromLegacy(id, data);
    }

    /**
     * Get a {@link BlockState} from its ID
     *
     * @param id String or integer ID
     * @return Parsed block state, or {@code null} if none
     *         could be parsed
     */
    public static @Nullable BlockState get(final @NonNull String id) {
        if (id.length() == 1 && id.charAt(0) == '*') {
            return FuzzyBlockState.builder().type(BlockTypes.AIR).build();
        }
        String mutableId;
        mutableId = id.toLowerCase();
        BlockType type = BlockTypes.get(mutableId);
        if (type != null) {
            return type.getDefaultState();
        }
        if (Character.isDigit(mutableId.charAt(0))) {
            String[] split = mutableId.split(":");
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
            BaseBlock block = PARSER.parseFromInput(mutableId, PARSER_CONTEXT);
            return block.toImmutableState();
        } catch (InputParseException e) {
            return null;
        }
    }

}
