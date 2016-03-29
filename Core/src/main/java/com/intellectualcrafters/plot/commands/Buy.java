package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;
import java.util.Set;

@CommandDeclaration(
        command = "buy",
        aliases = {"b"},
        description = "Buy the plot you are standing on",
        usage = "/plot buy",
        permission = "plots.buy",
        category = CommandCategory.CLAIMING,
        requiredType = RequiredType.NONE)
public class Buy extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        if (EconHandler.manager == null) {
            return sendMessage(plr, C.ECON_DISABLED);
        }
        Location loc = plr.getLocation();
        String world = loc.getWorld();
        if (!PS.get().hasPlotArea(world)) {
            return sendMessage(plr, C.NOT_IN_PLOT_WORLD);
        }
        Set<Plot> plots;
        Plot plot;
        if (args.length > 0) {
            try {
                plot = MainUtil.getPlotFromString(plr, world, true);
                if (plot == null) {
                    return false;
                }
                plots = plot.getConnectedPlots();
            } catch (Exception e) {
                return sendMessage(plr, C.NOT_VALID_PLOT_ID);
            }
        } else {
            plot = loc.getPlotAbs();
            plots = plot != null ? plot.getConnectedPlots() : null;
        }
        if (plots == null) {
            return sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            return sendMessage(plr, C.PLOT_UNOWNED);
        }
        int currentPlots = plr.getPlotCount() + plots.size();
        if (currentPlots > plr.getAllowedPlots()) {
            return sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS);
        }
        Flag flag = FlagManager.getPlotFlagRaw(plot, "price");
        if (flag == null) {
            return sendMessage(plr, C.NOT_FOR_SALE);
        }
        if (plot.isOwner(plr.getUUID())) {
            return sendMessage(plr, C.CANNOT_BUY_OWN);
        }
        double price = (double) flag.getValue();
        if ((EconHandler.manager != null) && (price > 0d)) {
            if (EconHandler.manager.getMoney(plr) < price) {
                return sendMessage(plr, C.CANNOT_AFFORD_PLOT, "" + price);
            }
            EconHandler.manager.withdrawMoney(plr, price);
            sendMessage(plr, C.REMOVED_BALANCE, price + "");
            EconHandler.manager.depositMoney(UUIDHandler.getUUIDWrapper().getOfflinePlayer(plot.owner), price);
            PlotPlayer owner = UUIDHandler.getPlayer(plot.owner);
            if (owner != null) {
                sendMessage(plr, C.PLOT_SOLD, plot.getId() + "", plr.getName(), price + "");
            }
            FlagManager.removePlotFlag(plot, "price");
        }
        plot.setOwner(plr.getUUID());
        MainUtil.sendMessage(plr, C.CLAIMED);
        return true;
    }
}
