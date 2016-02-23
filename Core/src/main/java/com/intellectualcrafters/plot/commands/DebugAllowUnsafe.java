package com.intellectualcrafters.plot.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "debugallowunsafe",
description = "Allow unsafe actions until toggled off",
usage = "/plot debugallowunsafe",
category = CommandCategory.DEBUG,
requiredType = RequiredType.NONE,
permission = "plots.debugallowunsafe")
public class DebugAllowUnsafe extends SubCommand {
    
    public static final List<UUID> unsafeAllowed = new ArrayList<>();
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String... args) {
        
        if (unsafeAllowed.contains(plr.getUUID())) {
            unsafeAllowed.remove(plr.getUUID());
            sendMessage(plr, C.DEBUGALLOWUNSAFE_OFF);
        } else {
            unsafeAllowed.add(plr.getUUID());
            sendMessage(plr, C.DEBUGALLOWUNSAFE_ON);
        }
        return true;
    }
    
}
