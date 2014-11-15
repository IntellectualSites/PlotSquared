package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.StringComparsion;
import com.intellectualcrafters.plot.database.MySQL;
import com.intellectualcrafters.plot.database.SQLManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created 2014-11-15 for PlotSquared
 *
 * @author Citymonstret
 */
public class Database extends SubCommand {

    final String[] tables = new String[]{
            "plot_trusted", "plot_ratings", "plot_comments"
    };

    public Database() {
        super(Command.DATABASE, "Convert/Backup Storage", "database [type] [...details]", CommandCategory.DEBUG, false);
    }

    private static boolean sendMessageU(UUID uuid, String msg) {
        if (uuid == null) {
            PlotMain.sendConsoleSenderMessage(msg);
        } else {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline())
                return PlayerFunctions.sendMessage(p, msg);
            else
                return sendMessageU(null, msg);
        }
        return true;
    }

    public static void insertPlots(final SQLManager manager, final UUID requester, final Connection c) {
        Plugin p = PlotMain.getPlugin(PlotMain.class);
        final java.util.Set<Plot> plots = PlotMain.getPlots();
        p.getServer().getScheduler().runTaskAsynchronously(p, new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<Plot> ps = new ArrayList<>();
                    for (Plot p : plots)
                        ps.add(p);
                    manager.createPlots(ps);
                    manager.createAllSettingsAndHelpers(ps);
                    sendMessageU(requester, "&6Database conversion finished");
                } catch (Exception e) {
                    sendMessageU(requester, "Failed to insert plot objects, see stacktrace for info");
                    e.printStackTrace();
                }
                try {
                    c.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean execute(Player plr, String... args) {
        if (args.length < 1) {
            return sendMessage(plr, "/plot database [sqlite/mysql]");
        }
        String type = new StringComparsion(args[0], new String[]{"mysql", "sqlite"}).getBestMatch().toLowerCase();
        switch (type) {
            case "mysql":
                if (args.length < 6) {
                    return sendMessage(plr, "/plot database mysql [host] [port] [username] [password] [database] {prefix}");
                }
                String host =
                        args[1];
                String port =
                        args[2];
                String username =
                        args[3];
                String password =
                        args[4];
                String database =
                        args[5];
                String prefix =
                        "";
                if (args.length > 6) {
                    prefix = args[6];
                }
                Connection n;
                try {
                    n = new MySQL(
                            PlotMain.getPlugin(PlotMain.class),
                            host,
                            port,
                            database,
                            username,
                            password
                    ).openConnection();
                    // Connection
                    if (n.isClosed()) {
                        return sendMessage(plr, "Failed to open connection");
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                    return sendMessage(plr, "Failed to open connection, read stacktrace for info");
                }
                SQLManager manager = new SQLManager(n, prefix);
                try {
                    final DatabaseMetaData meta = n.getMetaData();
                    ResultSet set = meta.getTables(null, null, prefix + "plot", null);
                    if (!set.next()) {
                        manager.createTables("mysql", true);
                    } else {
                        for (String s : tables) {
                            set = meta.getTables(null, null, prefix + s, null);
                            if (!set.next()) {
                                manager.createTables("mysql", false);
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    return sendMessage(plr, "Could not create the required tables and/or load the database") &&
                            sendMessage(plr, "Please see the stacktrace for more information");
                }
                UUID requester = null;
                if (plr != null) {
                    requester = plr.getUniqueId();
                }
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

    private boolean sendMessage(Player player, String msg) {
        if (player == null) {
            PlotMain.sendConsoleSenderMessage(msg);
        } else {
            PlayerFunctions.sendMessage(player, msg);
        }
        return true;
    }
}
