package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.expiry.ExpireManager;

import java.util.UUID;

@CommandDeclaration(command = "info", aliases = "i", description = "Display plot info",
    usage = "/plot info <id>", category = CommandCategory.INFO) public class Info
    extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        Plot plot;
        String arg;
        if (args.length > 0) {
            arg = args[0];
            switch (arg) {
                case "trusted":
                case "alias":
                case "inv":
                case "biome":
                case "denied":
                case "flags":
                case "id":
                case "size":
                case "members":
                case "seen":
                case "owner":
                case "rating":
                    plot = MainUtil.getPlotFromString(player, null, false);
                    break;
                default:
                    plot = MainUtil.getPlotFromString(player, arg, false);
                    if (args.length == 2) {
                        arg = args[1];
                    } else {
                        arg = null;
                    }
                    break;
            }
            if (plot == null) {
                plot = player.getCurrentPlot();
            }
        } else {
            arg = null;
            plot = player.getCurrentPlot();
        }
        if (plot == null) {
            MainUtil.sendMessage(player, C.NOT_IN_PLOT.s());
            return false;
        }
        if (arg != null) {
            if (args.length == 1) {
                args = new String[0];
            } else {
                args = new String[] {args[1]};
            }
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("inv")) {
            PlotInventory inv = new PlotInventory(player) {
                @Override public boolean onClick(int index) {
                    // TODO InfoInventory not implemented yet!!!!!!!!
                    // See plot rating or musicsubcommand on examples
                    return false;
                }
            };
            UUID uuid = player.getUUID();
            String name = MainUtil.getName(plot.guessOwner());
            inv.setItem(1,
                new PlotItemStack("emerald", 1, "&cPlot Info", "&cID: &6" + plot.getId().toString(),
                    "&cOwner: &6" + name, "&cAlias: &6" + plot.getAlias(),
                    "&cBiome: &6" + plot.getBiome().replaceAll("_", "").toLowerCase(),
                    "&cCan Build: &6" + plot.isAdded(uuid), "&cSeen: &6" + MainUtil
                    .secToTime((int) (ExpireManager.IMP.getAge(plot) / 1000)),
                    "&cIs Denied: &6" + plot.isDenied(uuid)));
            inv.setItem(1, new PlotItemStack("emerald", 1, "&cTrusted",
                "&cAmount: &6" + plot.getTrusted().size(),
                "&8Click to view a list of the trusted users"));
            inv.setItem(1, new PlotItemStack("emerald", 1, "&cMembers",
                "&cAmount: &6" + plot.getMembers().size(),
                "&8Click to view a list of plot members"));
            inv.setItem(1, new PlotItemStack("emerald", 1, "&cDenied", "&cDenied",
                "&cAmount: &6" + plot.getDenied().size(),
                "&8Click to view a list of denied players"));
            inv.setItem(1, new PlotItemStack("emerald", 1, "&cFlags", "&cFlags",
                "&cAmount: &6" + plot.getFlags().size(), "&8Click to view a list of plot flags"));
            inv.openInventory();
            return true;
        }
        boolean hasOwner = plot.hasOwner();
        // Wildcard player {added}
        boolean containsEveryone = plot.getTrusted().contains(DBFunc.EVERYONE);
        boolean trustedEveryone = plot.getMembers().contains(DBFunc.EVERYONE);
        // Unclaimed?
        if (!hasOwner && !containsEveryone && !trustedEveryone) {
            MainUtil
                .sendMessage(player, C.PLOT_INFO_UNCLAIMED, plot.getId().x + ";" + plot.getId().y);
            return true;
        }
        String info = C.PLOT_INFO.s();
        boolean full;
        if (arg != null) {
            info = getCaption(arg);
            if (info == null) {
                MainUtil.sendMessage(player,
                    "&6Categories&7: &amembers&7, &aalias&7, &abiome&7, &aseen&7, &adenied&7, &aflags&7, &aid&7, &asize&7, &atrusted&7, "
                        + "&aowner&7, &arating");
                return false;
            }
            full = true;
        } else {
            full = false;
        }
        MainUtil.format(info, plot, player, full, new RunnableVal<String>() {
            @Override public void run(String value) {
                MainUtil.sendMessage(player,
                    C.PLOT_INFO_HEADER.s() + '\n' + value + '\n' + C.PLOT_INFO_FOOTER.s(), false);
            }
        });
        return true;
    }

    private String getCaption(String string) {
        switch (string) {
            case "trusted":
                return C.PLOT_INFO_TRUSTED.s();
            case "alias":
                return C.PLOT_INFO_ALIAS.s();
            case "biome":
                return C.PLOT_INFO_BIOME.s();
            case "denied":
                return C.PLOT_INFO_DENIED.s();
            case "flags":
                return C.PLOT_INFO_FLAGS.s();
            case "id":
                return C.PLOT_INFO_ID.s();
            case "size":
                return C.PLOT_INFO_SIZE.s();
            case "members":
                return C.PLOT_INFO_MEMBERS.s();
            case "owner":
                return C.PLOT_INFO_OWNER.s();
            case "rating":
                return C.PLOT_INFO_RATING.s();
            case "seen":
                return C.PLOT_INFO_SEEN.s();
            default:
                return null;
        }
    }
}
