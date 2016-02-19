package com.plotsquared.general.commands;

import com.intellectualcrafters.plot.commands.RequiredType;

public interface CommandCaller {
    void sendMessage(final String message);
    
    boolean hasPermission(final String perm);
    
    RequiredType getSuperCaller();
}
