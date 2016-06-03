package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotInventory;
import com.intellectualcrafters.plot.object.PlotItemStack;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "music",
        permission = "plots.music",
        description = "Player music in a plot",
        usage = "/plot music",
        category = CommandCategory.APPEARANCE,
        requiredType = RequiredType.PLAYER)
public class Music extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        Location loc = player.getLocation();
        final Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }
        if (!plot.isAdded(player.getUUID())) {
            sendMessage(player, C.NO_PLOT_PERMS);
            return true;
        }
        PlotInventory inv = new PlotInventory(player, 2, "Plot Jukebox") {
            @Override
            public boolean onClick(int index) {
                PlotItemStack item = getItem(index);
                if (item == null) {
                    return true;
                }
                int id = item.id == 7 ? 0 : item.id;
                if (id == 0) {
                    plot.removeFlag(Flags.MUSIC);
                } else {
                    plot.setFlag(Flags.MUSIC, id);
                }
                return false;
            }
        };
        int index = 0;
        for (int i = 2256; i < 2268; i++) {
            String name = "&r&6" + WorldUtil.IMP.getClosestMatchingName(PlotBlock.get((short) i, (byte) 0));
            String[] lore = {"&r&aClick to play!"};
            PlotItemStack item = new PlotItemStack(i, (byte) 0, 1, name, lore);
            inv.setItem(index, item);
            index++;
        }
        if (player.getMeta("music") != null) {
            String name = "&r&6Cancel music";
            String[] lore = {"&r&cClick to cancel!"};
            inv.setItem(index, new PlotItemStack(7, (short) 0, 1, name, lore));
        }
        inv.openInventory();
        return true;
    }
}
