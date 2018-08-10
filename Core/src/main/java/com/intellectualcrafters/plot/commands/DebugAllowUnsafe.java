package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CommandDeclaration(command = "debugallowunsafe", description = "Allow unsafe actions until toggled off", usage = "/plot debugallowunsafe", category = CommandCategory.DEBUG, requiredType = RequiredType.NONE, permission = "plots.debugallowunsafe")
public class DebugAllowUnsafe extends SubCommand {

    public static final List<UUID> unsafeAllowed = new ArrayList<>();

    @Override public boolean onCommand(PlotPlayer player, String[] args) {

        if (unsafeAllowed.contains(player.getUUID())) {
            unsafeAllowed.remove(player.getUUID());
            sendMessage(player, C.DEBUGALLOWUNSAFE_OFF);
        } else {
            unsafeAllowed.add(player.getUUID());
            sendMessage(player, C.DEBUGALLOWUNSAFE_ON);
        }
        return true;
    }

}
