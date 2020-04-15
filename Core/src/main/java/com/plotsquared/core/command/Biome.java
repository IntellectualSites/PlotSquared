package com.plotsquared.core.command;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.StringMan;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;

@CommandDeclaration(command = "setbiome",
    permission = "plots.set.biome",
    description = "Set the plot biome",
    usage = "/plot biome [biome]",
    aliases = {"biome", "sb", "setb", "b"},
    category = CommandCategory.APPEARANCE,
    requiredType = RequiredType.NONE)
public class Biome extends SetCommand {

    @Override public boolean set(final PlotPlayer player, final Plot plot, final String value) {
        BiomeType biome = null;
        try {
            biome = BiomeTypes.get(value.toLowerCase());
        } catch (final Exception ignore) {
        }
        if (biome == null) {
            String biomes = StringMan
                .join(BiomeType.REGISTRY.values(), Captions.BLOCK_LIST_SEPARATOR.getTranslated());
            Captions.NEED_BIOME.send(player);
            MainUtil.sendMessage(player,
                Captions.SUBCOMMAND_SET_OPTIONS_HEADER.getTranslated() + biomes);
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(player, Captions.WAIT_FOR_TIMER);
            return false;
        }
        plot.addRunning();
        plot.setBiome(biome, () -> {
            plot.removeRunning();
            MainUtil
                .sendMessage(player, Captions.BIOME_SET_TO.getTranslated() + value.toLowerCase());
        });
        return true;
    }
}
