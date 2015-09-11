package com.plotsquared.general.commands;

import com.intellectualcrafters.plot.commands.RequiredType;
import com.intellectualcrafters.plot.config.C;

public interface CommandCaller
{
    void sendMessage(final String message);

    void sendMessage(final C c, final String... args);

    boolean hasPermission(final String perm);

    RequiredType getSuperCaller();
}
