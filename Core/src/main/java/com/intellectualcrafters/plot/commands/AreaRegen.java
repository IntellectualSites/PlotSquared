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


@CommandDeclaration(command = "regen",
        permission = "plots.area.regen",
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        description = "Regenerate a PlotArea",
        aliases = {"clear","reset","regenerate"},
        usage = "/plot area regen",
        confirmation = true)
public class AreaRegen extends SubCommand {

    public AreaRegen(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (!Permissions.hasPermission(player, getPermission())) {
            C.NO_PERMISSION.send(player, getPermission());
            return false;
        }
        final PlotArea area = player.getApplicablePlotArea();
        if (area == null) {
            C.NOT_IN_PLOT_WORLD.send(player);
            return false;
        }
        if (area.TYPE != 2) {
            MainUtil.sendMessage(player, "$4Stop the server and delete: " + area.worldname + "/region");
            return false;
        }
        ChunkManager.largeRegionTask(area.worldname, area.getRegion(), new RunnableVal<ChunkLoc>() {
            @Override
            public void run(ChunkLoc value) {
                AugmentedUtils.generate(area.worldname, value.x, value.z, null);
            }
        }, null);
        return true;
    }

}