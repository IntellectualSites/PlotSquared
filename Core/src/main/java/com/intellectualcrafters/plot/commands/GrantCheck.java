package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.RunnableVal2;
import com.intellectualcrafters.plot.object.RunnableVal3;
import com.intellectualcrafters.plot.util.ByteArrayUtilities;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;
import java.util.UUID;

@CommandDeclaration(
        command = "check",
        category = CommandCategory.CLAIMING,
        usage = "/plot grant check [player]",
        permission = "plots.grant",
        requiredType = RequiredType.NONE)
public class GrantCheck extends SubCommand {

    public GrantCheck(Command parent, boolean isStatic) {
        super(parent, isStatic);
    }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length > 1) {
            C.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }

        final UUID uuid = args.length == 1 ? UUIDHandler.getUUIDFromString(args[0]) : player.getUUID();
        if (uuid == null) {
            C.INVALID_PLAYER.send(player, args[0]);
            return false;
        }
        MainUtil.getPersistentMeta(uuid, "grantedPlots", new RunnableVal<byte[]>() {
            @Override
            public void run(byte[] array) {
                int granted = array == null ? 0 : ByteArrayUtilities.bytesToInteger(array);
                C.GRANTED_PLOTS.send(player, granted);
            }
        });
        return true;
    }
}