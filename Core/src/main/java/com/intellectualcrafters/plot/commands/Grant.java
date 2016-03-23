package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.ByteArrayUtilities;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.UUID;

@CommandDeclaration(
        command = "grant",
        category = CommandCategory.CLAIMING,
        usage = "/plot grant <check|add> [player]",
        permission = "plots.grant",
        requiredType = RequiredType.NONE)
public class Grant extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer plr, String[] args) {
        final String arg0 = args[0].toLowerCase();
        switch (arg0) {
            case "add":
            case "check":
                if (Permissions.hasPermission(plr, "plots.grant." + arg0)) {
                    C.NO_PERMISSION.send(plr, "plots.grant." + arg0);
                    return false;
                }
                if (args.length > 2) {
                    break;
                }
                final UUID uuid = args.length == 2 ? UUIDHandler.getUUIDFromString(args[1]) : plr.getUUID();
                if (uuid == null) {
                    C.INVALID_PLAYER.send(plr, args[1]);
                    return false;
                }
                MainUtil.getPersistentMeta(uuid, "grantedPlots", new RunnableVal<byte[]>() {
                    @Override
                    public void run(byte[] array) {
                        if (arg0.equals("check")) { // check
                            int granted = array == null ? 0 : ByteArrayUtilities.bytesToInteger(array);
                            C.GRANTED_PLOTS.send(plr, granted);
                        } else { // add
                            int amount = 1 + (array == null ? 0 : ByteArrayUtilities.bytesToInteger(array));
                            boolean replace = array != null;
                            DBFunc.dbManager.addPersistentMeta(uuid, "grantedPlots", ByteArrayUtilities.integerToBytes(amount), replace);
                        }
                    }
                });
                return true;
        }
        C.COMMAND_SYNTAX.send(plr, getUsage());
        return false;
    }

}
