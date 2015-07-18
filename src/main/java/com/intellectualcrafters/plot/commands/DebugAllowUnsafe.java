package com.intellectualcrafters.plot.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;

public class DebugAllowUnsafe extends SubCommand {

    public static final List<UUID> unsafeAllowed = new ArrayList<>();

    public DebugAllowUnsafe() {
        super(Command.ALLOWUNSAFE, "Allow unsafe actions until toggled off", "allowunsafe", CommandCategory.DEBUG, true);
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (plr == null) {
            MainUtil.sendMessage(plr, C.IS_CONSOLE);
            return false;
        }
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
