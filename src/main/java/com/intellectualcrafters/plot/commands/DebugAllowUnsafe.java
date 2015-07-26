package com.intellectualcrafters.plot.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualsites.commands.callers.CommandCaller;

public class DebugAllowUnsafe extends SubCommand {

    public static final List<UUID> unsafeAllowed = new ArrayList<>();

    public DebugAllowUnsafe() {
        super(Command.ALLOWUNSAFE, "Allow unsafe actions until toggled off", "allowunsafe", CommandCategory.DEBUG, true);
    }

    @Override
    public boolean onCommand(final CommandCaller caller, final String ... args) {
        final PlotPlayer plr = (PlotPlayer) caller.getSuperCaller();
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
