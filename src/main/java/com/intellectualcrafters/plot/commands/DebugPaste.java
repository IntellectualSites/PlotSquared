package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.HastebinUtility;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;

public class DebugPaste extends SubCommand {

    public DebugPaste() {
        super(Command.DEBUG_PASTE, "Upload settings.yml & latest.log to hastebin", "", CommandCategory.DEBUG, false);
    }

    @Override
    public boolean execute(final PlotPlayer plr, String... args) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    String link = HastebinUtility.upload(PS.get().configFile);
                    MainUtil.sendMessage(plr, C.SETTINGS_PASTE_UPLOADED.s().replace("%url%", link));
                    link = HastebinUtility.upload(new File(BukkitMain.THIS.getDirectory(), "../../logs/latest.log"));
                    MainUtil.sendMessage(plr, C.LATEST_LOG_UPLOADED.s().replace("%url%", link));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return true;
    }
}
