package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.database.MySQL;
import com.intellectualcrafters.plot.database.SQLManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

@CommandDeclaration(
        command = "database",
        aliases = {"convert"},
        category = CommandCategory.DEBUG,
        permission = "plots.database",
        description = "Convert/Backup Storage",
        requiredType = RequiredType.CONSOLE,
        usage = "/plots database <type> [details...]"
        
)
public class Database extends SubCommand {

    private static boolean sendMessageU(final UUID uuid, final String msg) {
        if (uuid == null) {
            PS.log(msg);
        } else {
            final PlotPlayer p = UUIDHandler.getPlayer(uuid);
            if ((p != null) && p.isOnline()) {
                return MainUtil.sendMessage(p, msg);
            } else {
                return sendMessageU(null, msg);
            }
        }
        return true;
    }

    public static void insertPlots(final SQLManager manager, final UUID requester, final Connection c) {
        final java.util.Set<Plot> plots = PS.get().getPlots();
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    final ArrayList<Plot> ps = new ArrayList<>();
                    for (final Plot p : plots) {
                        ps.add(p);
                    }
                    sendMessageU(requester, "&6Starting...");
                    manager.createPlotsAndData(ps, new Runnable() {
                        @Override
                        public void run() {
                            sendMessageU(requester, "&6Database conversion finished!");
                        }
                    });
                } catch (final Exception e) {
                    sendMessageU(requester, "Failed to insert plot objects, see stacktrace for info");
                    e.printStackTrace();
                }
                try {
                    c.close();
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        if (args.length < 1) {
            return sendMessage(plr, "/plot database [sqlite/mysql]");
        }
        final String type = new StringComparison(args[0], new String[] { "mysql", "sqlite" }).getBestMatch().toLowerCase();
        switch (type) {
            case "mysql":
                if (args.length < 6) {
                    return sendMessage(plr, "/plot database mysql [host] [port] [username] [password] [database] {prefix}");
                }
                final String host = args[1];
                final String port = args[2];
                final String username = args[3];
                final String password = args[4];
                final String database = args[5];
                String prefix = "";
                if (args.length > 6) {
                    prefix = args[6];
                }
                Connection n;
                try {
                    n = new MySQL(host, port, database, username, password).openConnection();
                    // Connection
                    if (n.isClosed()) {
                        return sendMessage(plr, "Failed to open connection");
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return sendMessage(plr, "Failed to open connection, read stacktrace for info");
                }
                final SQLManager manager = new SQLManager(n, prefix);
                try {
                    manager.createTables("mysql");
                } catch (final SQLException e) {
                    e.printStackTrace();
                    return sendMessage(plr, "Could not create the required tables and/or load the database") && sendMessage(plr, "Please see the stacktrace for more information");
                }
                UUID requester = null;
                requester = UUIDHandler.getUUID(plr);
                insertPlots(manager, requester, n);
                break;
            case "sqlite":
                if (args.length < 2) {
                    return sendMessage(plr, "/plot database sqlite [file name]");
                }
                sendMessage(plr, "This is not supported yet");
                break;
            default:
                return sendMessage(plr, "Unknown database type");
        }
        return false;
    }

    private boolean sendMessage(final PlotPlayer player, final String msg) {
        if (player == null) {
            PS.log(msg);
        } else {
            MainUtil.sendMessage(player, msg);
        }
        return true;
    }
}
