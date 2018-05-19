package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "setbiome",
        permission = "plots.set.biome",
        description = "Set the plot biome",
        usage = "/plot setbiome <biome>",
        aliases = {"sb","setb"},
        category = CommandCategory.APPEARANCE,
        requiredType = RequiredType.NONE)
public class Biome extends SetBiome {

    public Biome() { super(MainCommand.getInstance(), true); }

    @Override
    public boolean set(PlotPlayer player, Plot plot, String value) {
        return super.set(player, plot, value);
    }
}
