////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.commands;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.events.PlayerClaimPlotEvent;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.bukkit.PlayerFunctions;

/**
 * @author Citymonstret
 */
public class Claim extends SubCommand {
    public Claim() {
        super(Command.CLAIM, "Claim the current plot you're standing on.", "claim", CommandCategory.CLAIMING, true);
    }
    
    public static boolean claimPlot(final Player player, final Plot plot, final boolean teleport, final boolean auto) {
        return claimPlot(player, plot, teleport, "", auto);
    }
    
    public static boolean claimPlot(final Player player, final Plot plot, final boolean teleport, final String schematic, final boolean auto) {
        if (plot.hasOwner() || plot.settings.isMerged()) {
            return false;
        }
        final PlayerClaimPlotEvent event = new PlayerClaimPlotEvent(player, plot, auto);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            MainUtil.createPlot(player, plot);
            MainUtil.setSign(player, plot);
            PlayerFunctions.sendMessage(player, C.CLAIMED);
            if (teleport) {
                PlotSquared.teleportPlayer(player, BukkitUtil.getLocation(entity), plot);
            }
            final World world = plot.world;
            final PlotWorld plotworld = PlotSquared.getPlotWorld(world);
            final Plot plot2 = PlotSquared.getPlots(player.getWorld()).get(plot.id);
            if (plotworld.SCHEMATIC_ON_CLAIM) {
                SchematicHandler.Schematic sch;
                if (schematic.equals("")) {
                    sch = SchematicHandler.getSchematic(plotworld.SCHEMATIC_FILE);
                } else {
                    sch = SchematicHandler.getSchematic(schematic);
                    if (sch == null) {
                        sch = SchematicHandler.getSchematic(plotworld.SCHEMATIC_FILE);
                    }
                }
                SchematicHandler.paste(BukkitUtil.getLocation(entity), sch, plot2, 0, 0);
            }
            PlotSquared.getPlotManager(plot.world).claimPlot(world, plotworld, plot);
            MainUtil.update(BukkitUtil.getLocation(entity));
        }
        return event.isCancelled();
    }
    
    @Override
    public boolean execute(final Player plr, final String... args) {
        String schematic = "";
        if (args.length >= 1) {
            schematic = args[0];
        }
        if (!PlayerFunctions.isInPlot(plr)) {
            return sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (PlayerFunctions.getPlayerPlotCount(plr.getWorld(), plr) >= PlayerFunctions.getAllowedPlots(plr)) {
            return sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS);
        }
        final Plot plot = PlayerFunctions.getCurrentPlot(plr);
        if (plot.hasOwner()) {
            return sendMessage(plr, C.PLOT_IS_CLAIMED);
        }
        final PlotWorld world = PlotSquared.getPlotWorld(plot.world);
        if (PlotSquared.useEconomy && world.USE_ECONOMY) {
            final double cost = world.PLOT_PRICE;
            if (cost > 0d) {
                final Economy economy = PlotSquared.economy;
                if (economy.getBalance(plr) < cost) {
                    return sendMessage(plr, C.CANNOT_AFFORD_PLOT, "" + cost);
                }
                economy.withdrawPlayer(plr, cost);
                sendMessage(plr, C.REMOVED_BALANCE, cost + "");
            }
        }
        if (!schematic.equals("")) {
            if (world.SCHEMATIC_CLAIM_SPECIFY) {
                if (!world.SCHEMATICS.contains(schematic.toLowerCase())) {
                    return sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent: " + schematic);
                }
                if (!BukkitMain.hasPermission(plr, "plots.claim." + schematic) && !plr.hasPermission("plots.admin.command.schematic")) {
                    return sendMessage(plr, C.NO_SCHEMATIC_PERMISSION, schematic);
                }
            }
        }
        return !claimPlot(plr, plot, false, schematic, false) || sendMessage(plr, C.PLOT_NOT_CLAIMED);
    }
}
