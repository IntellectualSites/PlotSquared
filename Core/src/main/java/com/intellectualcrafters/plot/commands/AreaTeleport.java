package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.generator.AugmentedUtils;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.*;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

@CommandDeclaration(command = "tp",
        permission = "plots.area.tp",
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        description = "Teleport to PlotArea",
        aliases = {"goto","v","teleport","visit"},
        usage = "/plot area tp <area>",
        confirmation = true)
public class AreaTeleport extends SubCommand {

    public AreaTeleport(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length == 0) {
            C.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }

        PlotArea area = PS.get().getPlotAreaByString(args[0]);
        if (area == null) {
            C.NOT_VALID_PLOT_WORLD.send(player, args[0]);
            return false;
        }
        Location center;
        if (area.TYPE != 2) {
            center = WorldUtil.IMP.getSpawn(area.worldname);
        } else {
            RegionWrapper region = area.getRegion();
            center = new Location(area.worldname, region.minX + (region.maxX - region.minX) / 2, 0,
                    region.minZ + (region.maxZ - region.minZ) / 2);
            center.setY(1 + WorldUtil.IMP.getHighestBlock(area.worldname, center.getX(), center.getZ()));
        }
        player.teleport(center);
        return true;
    }
}
