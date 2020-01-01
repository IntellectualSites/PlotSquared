package com.github.intellectualsites.plotsquared.plot.util.world;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
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

public class PatternUtil {
    public static BaseBlock apply(Pattern pattern, int x, int y, int z) {
        if (pattern instanceof BlockPattern
            || pattern instanceof RandomPattern
            || pattern instanceof BlockState
            || pattern instanceof BlockType
            || pattern instanceof BaseBlock) {
            return pattern.apply(BlockVector3.ZERO);
        }
        return pattern.apply(BlockVector3.at(x, y, z));
    }

    public static Pattern parse(PlotPlayer plotPlayer, String input) {
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
        context.setTryLegacy(true);
        try {
            Pattern pattern =
                WorldEdit.getInstance().getPatternFactory().parseFromInput(input, context);
            return pattern;
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
