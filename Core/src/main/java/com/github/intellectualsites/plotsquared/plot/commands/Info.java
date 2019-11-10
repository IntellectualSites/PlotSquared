package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotInventory;
import com.github.intellectualsites.plotsquared.plot.object.PlotItemStack;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.expiry.ExpireManager;

import java.util.UUID;

@CommandDeclaration(command = "info", aliases = "i", description = "Display plot info",
    usage = "/plot info <id> [-f, to force info]", category = CommandCategory.INFO)
public class Info extends SubCommand {

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
                case "likes":
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
            MainUtil.sendMessage(player, Captions.NOT_IN_PLOT.getTranslated());
            return false;
        }

        if (arg != null) {
            if (args.length == 1) {
                args = new String[0];
            } else {
                args = new String[] {args[1]};
            }
        }

        // hide-info flag
        if (plot.getFlag(Flags.HIDE_INFO).orElse(false)) {
            boolean allowed = false;
            for (final String argument : args) {
                if (argument.equalsIgnoreCase("-f")) {
                    if (!player
                        .hasPermission(Captions.PERMISSION_AREA_INFO_FORCE.getTranslated())) {
                        Captions.NO_PERMISSION.send(player, Captions.PERMISSION_AREA_INFO_FORCE);
                        return true;
                    }
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                Captions.PLOT_INFO_HIDDEN.send(player);
                return true;
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
            String name = MainUtil.getName(plot.getOwner());
            inv.setItem(1, new PlotItemStack(388, (short) 0, 1, "&cPlot Info",
                "&cID: &6" + plot.getId().toString(), "&cOwner: &6" + name,
                "&cAlias: &6" + plot.getAlias(),
                "&cBiome: &6" + plot.getBiome().toString().replaceAll("_", "").toLowerCase(),
                "&cCan Build: &6" + plot.isAdded(uuid),
                "&cSeen: &6" + MainUtil.secToTime((int) (ExpireManager.IMP.getAge(plot) / 1000)),
                "&cIs Denied: &6" + plot.isDenied(uuid)));
            inv.setItem(1, new PlotItemStack(388, (short) 0, 1, "&cTrusted",
                "&cAmount: &6" + plot.getTrusted().size(),
                "&8Click to view a list of the trusted users"));
            inv.setItem(1, new PlotItemStack(388, (short) 0, 1, "&cMembers",
                "&cAmount: &6" + plot.getMembers().size(),
                "&8Click to view a list of plot members"));
            inv.setItem(1, new PlotItemStack(388, (short) 0, 1, "&cDenied", "&cDenied",
                "&cAmount: &6" + plot.getDenied().size(),
                "&8Click to view a list of denied players"));
            inv.setItem(1, new PlotItemStack(388, (short) 0, 1, "&cFlags", "&cFlags",
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
            MainUtil.sendMessage(player, Captions.PLOT_INFO_UNCLAIMED,
                plot.getId().x + ";" + plot.getId().y);
            return true;
        }
        String info = Captions.PLOT_INFO.getTranslated();
        boolean full;
        if (arg != null) {
            info = getCaption(arg);
            if (info == null) {
                if (Settings.Ratings.USE_LIKES) {
                    MainUtil.sendMessage(player,
                        "&6Categories&7: &amembers&7, &aalias&7, &abiome&7, &aseen&7, &adenied&7, &aflags&7, &aid&7, &asize&7, &atrusted&7, "
                            + "&aowner&7, " + " &alikes");
                } else {
                    MainUtil.sendMessage(player,
                        "&6Categories&7: &amembers&7, &aalias&7, &abiome&7, &aseen&7, &adenied&7, &aflags&7, &aid&7, &asize&7, &atrusted&7, "
                            + "&aowner&7, " + " &arating");
                }
                return false;
            }
            full = true;
        } else {
            full = false;
        }
        MainUtil.format(info, plot, player, full, new RunnableVal<String>() {
            @Override public void run(String value) {
                MainUtil.sendMessage(player,
                    Captions.PLOT_INFO_HEADER.getTranslated() + '\n' + value + '\n'
                        + Captions.PLOT_INFO_FOOTER.getTranslated(), false);
            }
        });
        return true;
    }

    private String getCaption(String string) {
        switch (string) {
            case "trusted":
                return Captions.PLOT_INFO_TRUSTED.getTranslated();
            case "alias":
                return Captions.PLOT_INFO_ALIAS.getTranslated();
            case "biome":
                return Captions.PLOT_INFO_BIOME.getTranslated();
            case "denied":
                return Captions.PLOT_INFO_DENIED.getTranslated();
            case "flags":
                return Captions.PLOT_INFO_FLAGS.getTranslated();
            case "id":
                return Captions.PLOT_INFO_ID.getTranslated();
            case "size":
                return Captions.PLOT_INFO_SIZE.getTranslated();
            case "members":
                return Captions.PLOT_INFO_MEMBERS.getTranslated();
            case "owner":
                return Captions.PLOT_INFO_OWNER.getTranslated();
            case "rating":
                return Captions.PLOT_INFO_RATING.getTranslated();
            case "likes":
                return Captions.PLOT_INFO_LIKES.getTranslated();
            case "seen":
                return Captions.PLOT_INFO_SEEN.getTranslated();
            default:
                return null;
        }
    }
}
