package com.intellectualcrafters.plot.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.database.MySQL;
import com.intellectualcrafters.plot.database.SQLManager;
import com.intellectualcrafters.plot.database.SQLite;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "database",
aliases = { "convert" },
category = CommandCategory.DEBUG,
permission = "plots.database",
description = "Convert/Backup Storage",
requiredType = RequiredType.CONSOLE,
usage = "/plots database [world] <sqlite|mysql|import>"

)
public class Database extends SubCommand {
    
    public static void insertPlots(final SQLManager manager, final ArrayList<Plot> plots, final PlotPlayer player) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final ArrayList<Plot> ps = new ArrayList<>();
                    for (final Plot p : plots) {
                        ps.add(p);
                    }
                    MainUtil.sendMessage(player, "&6Starting...");
                    manager.createPlotsAndData(ps, new Runnable() {
                        @Override
                        public void run() {
                            MainUtil.sendMessage(player, "&6Database conversion finished!");
                            manager.close();
                        }
                    });
                } catch (final Exception e) {
                    MainUtil.sendMessage(player, "Failed to insert plot objects, see stacktrace for info");
                    e.printStackTrace();
                }
            }
        });
    }
    
    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length < 1) {
            MainUtil.sendMessage(player, "/plot database [world] <sqlite|mysql>");
            return false;
        }
        ArrayList<Plot> plots;
        if (PS.get().isPlotWorld(args[0])) {
            plots = PS.get().sortPlotsByTemp(PS.get().getPlotsInWorld(args[0]));
            args = Arrays.copyOfRange(args, 1, args.length);
        } else {
            plots = PS.get().sortPlotsByTemp(PS.get().getPlotsRaw());
        }
        if (args.length < 1) {
            MainUtil.sendMessage(player, "/plot database [world] <sqlite|mysql|import>");
            MainUtil.sendMessage(player, "[arg] indicates an optional argument");
            return false;
        }
        try {
            com.intellectualcrafters.plot.database.Database implementation;
            String prefix = "";
            switch (args[0].toLowerCase()) {
                case "import": {
                    if (args.length < 2) {
                        MainUtil.sendMessage(player, "/plot database import [sqlite file] [prefix]");
                        return false;
                    }
                    MainUtil.sendMessage(player, "&6Starting...");
                    implementation = new SQLite(PS.get().IMP.getDirectory() + File.separator + args[1] + ".db");
                    final SQLManager manager = new SQLManager(implementation, (args.length == 3) ? args[2] : "", true);
                    final ConcurrentHashMap<String, ConcurrentHashMap<PlotId, Plot>> map = manager.getPlots();
                    plots = new ArrayList<Plot>();
                    for (final Entry<String, ConcurrentHashMap<PlotId, Plot>> entry : map.entrySet()) {
                        for (final Entry<PlotId, Plot> entry2 : entry.getValue().entrySet()) {
                            final Plot plot = entry2.getValue();
                            if (PS.get().getPlot(plot.world, plot.id) != null) {
                                MainUtil.sendMessage(player, "Skipping duplicate plot: " + plot + " | id=" + plot.temp);
                                continue;
                            }
                            PS.get().updatePlot(plot);
                            plots.add(entry2.getValue());
                        }
                    }
                    DBFunc.createPlotsAndData(plots, new Runnable() {
                        @Override
                        public void run() {
                            MainUtil.sendMessage(player, "&6Database conversion finished!");
                        }
                    });
                    return true;
                }
                case "mysql":
                    if (args.length < 6) {
                        return MainUtil.sendMessage(player, "/plot database mysql [host] [port] [username] [password] [database] {prefix}");
                    }
                    final String host = args[1];
                    final String port = args[2];
                    final String username = args[3];
                    final String password = args[4];
                    final String database = args[5];
                    if (args.length > 6) {
                        prefix = args[6];
                    }
                    implementation = new MySQL(host, port, database, username, password);
                    break;
                case "sqlite":
                    if (args.length < 2) {
                        return MainUtil.sendMessage(player, "/plot database sqlite [file]");
                    }
                    implementation = new SQLite(PS.get().IMP.getDirectory() + File.separator + args[1] + ".db");
                    break;
                default:
                    return MainUtil.sendMessage(player, "/plot database [sqlite/mysql]");
            }
            try {
                final SQLManager manager = new SQLManager(implementation, prefix, true);
                insertPlots(manager, plots, player);
                return true;
            } catch (final Exception e) {
                MainUtil.sendMessage(player, "$1Failed to save plots, read stacktrace for info");
                MainUtil.sendMessage(player, "&d==== Here is an ugly stacktrace, if you are interested in those things ===");
                e.printStackTrace();
                MainUtil.sendMessage(player, "&d==== End of stacktrace ====");
                MainUtil.sendMessage(player, "$1Please make sure you are using the correct arguments!");
                return false;
            }
        } catch (final Exception e) {
            MainUtil.sendMessage(player, "$1Failed to open connection, read stacktrace for info");
            MainUtil.sendMessage(player, "&d==== Here is an ugly stacktrace, if you are interested in those things ===");
            e.printStackTrace();
            MainUtil.sendMessage(player, "&d==== End of stacktrace ====");
            MainUtil.sendMessage(player, "$1Please make sure you are using the correct arguments!");
            return false;
        }
    }
}
