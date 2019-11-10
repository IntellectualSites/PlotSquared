package com.github.intellectualsites.plotsquared.plot.util.world;

import com.github.intellectualsites.plotsquared.plot.util.MathMan;
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
import lombok.NonNull;

import java.util.Map;

public final class BlockUtil {
    private BlockUtil(){}

    private static final ParserContext PARSER_CONTEXT = new ParserContext();

    private static final InputParser<BaseBlock> PARSER;

    static  {
        PARSER_CONTEXT.setRestricted(false);
        PARSER_CONTEXT.setPreferringWildcard(false);
        PARSER_CONTEXT.setTryLegacy(true);
        PARSER = WorldEdit.getInstance().getBlockFactory().getParsers().get(0);
    }

    public static final BlockState get(int id) {
        return LegacyMapper.getInstance().getBlockFromLegacy(id);
    }

    public static final BlockState get(int id, int data) {
        return LegacyMapper.getInstance().getBlockFromLegacy(id, data);
    }

    public static final BlockState get(String id) {
        if (id.length() == 1 && id.charAt(0) == '*') {
            return FuzzyBlockState.builder().type(BlockTypes.AIR).build();
        }
        id = id.toLowerCase();
        BlockType type = BlockType.REGISTRY.get(id);
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

    public static final BlockState[] parse(String commaDelimited) {
        String[] split = commaDelimited.split(",(?![^\\(\\[]*[\\]\\)])");
        BlockState[] result = new BlockState[split.length];
        for (int i = 0; i < split.length; i++) {
            result[i] = get(split[i]);
        }
        return result;
    }

    public static BlockState deserialize(@NonNull final Map<String, Object> map) {
        if (map.containsKey("material")) {
            final Object object = map.get("material");
            return get(object.toString());
        }
        return null;
    }

}
