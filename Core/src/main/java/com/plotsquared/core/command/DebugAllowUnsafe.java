package com.plotsquared.core.command;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.player.PlotPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CommandDeclaration(command = "debugallowunsafe",
    description = "Allow unsafe actions until toggled off",
    usage = "/plot debugallowunsafe",
    category = CommandCategory.DEBUG,
    requiredType = RequiredType.NONE,
    permission = "plots.debugallowunsafe")
public class DebugAllowUnsafe extends SubCommand {

    public static final List<UUID> unsafeAllowed = new ArrayList<>();

    @Override public boolean onCommand(PlotPlayer player, String[] args) {

        if (unsafeAllowed.contains(player.getUUID())) {
            unsafeAllowed.remove(player.getUUID());
            sendMessage(player, Captions.DEBUGALLOWUNSAFE_OFF);
        } else {
            unsafeAllowed.add(player.getUUID());
            sendMessage(player, Captions.DEBUGALLOWUNSAFE_ON);
        }
        return true;
    }

}
