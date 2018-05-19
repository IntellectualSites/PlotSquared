package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.configuration.InvalidConfigurationException;
import com.intellectualcrafters.configuration.file.YamlConfiguration;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.FileBytes;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.intellectualcrafters.plot.util.block.GlobalBlockQueue;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@CommandDeclaration(
        command = "export",
        permission = "plots.admin",
        description = "Export a world template",
        usage = "/plot template export <world>",
        category = CommandCategory.ADMINISTRATION)
public class TemplateExport extends TemplateCommand {

    public TemplateExport(Command parent, Boolean isStatic) {
        super(parent, isStatic);
    }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length != 1) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, getUsage());
            return false;
        }

        final PlotArea area = PS.get().getPlotAreaByString(args[0]);
        if (area == null) {
            MainUtil.sendMessage(player, C.NOT_VALID_PLOT_WORLD);
            return false;
        }
        final PlotManager manager = area.getPlotManager();
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    manager.exportTemplate(area);
                } catch (Exception e) { // Must recover from any exception thrown a third party template manager
                    e.printStackTrace();
                    MainUtil.sendMessage(player, "Failed: " + e.getMessage());
                    return;
                }
                MainUtil.sendMessage(player, "Done!");
            }
        });
        return true;
    }
}
