package com.plotsquared.general.commands;

import com.intellectualcrafters.plot.commands.RequiredType;

public interface CommandCaller {

    void sendMessage(String message);

    boolean hasPermission(String perm);
    
    RequiredType getSuperCaller();
}
