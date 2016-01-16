package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.CommandDeclaration;

/**
 * Legally stolen from https://github.com/manuelgu/PlotSquaredMiddle
 *
 * @author manuelgu, altered by Citymonstret
 */
@CommandDeclaration(
        command = "middle",
        aliases = { "center" },
        description = "Teleports you to the center of the current plot",
        usage = "/plot middle",
        category = CommandCategory.TELEPORT,
        requiredType = RequiredType.PLAYER
)
public class Middle extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] arguments) {
        final Location location = player.getLocation();
        final Plot plot = MainUtil.getPlotAbs(location);
        if (plot == null) {
            return sendMessage(player, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            return sendMessage(player, C.PLOT_UNOWNED);
        }
        if (!player.hasPermission("plots.middle")) {
            return sendMessage(player, C.NO_PERMISSION, "plots.middle");
        }

        RegionWrapper largestRegion = MainUtil.getLargestRegion(plot);
        final int x = ((largestRegion.maxX - largestRegion.minX) / 2) + largestRegion.minX;
        final int z = ((largestRegion.maxZ - largestRegion.minZ) / 2) + largestRegion.minZ;
        final int y = MainUtil.getHeighestBlock(plot.getWorld().worldname, x, z) + 1;

        player.teleport(new Location(plot.getWorld().worldname, x, y, z));
        return true;
    }
}
