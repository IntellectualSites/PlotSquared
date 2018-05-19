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
        command = "import",
        permission = "plots.admin",
        description = "Import a world template",
        usage = "/plot template import <world> <template>",
        category = CommandCategory.ADMINISTRATION)
public class TemplateImport extends TemplateCommand {

    public TemplateImport(Command parent, Boolean isStatic) {
        super(parent, isStatic);
    }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length != 2) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, getUsage());
            return false;
        }

        final String world = args[0];
        if (PS.get().hasPlotArea(world)) {
            MainUtil.sendMessage(player, C.SETUP_WORLD_TAKEN, world);
            return false;
        }
        boolean result = extractAllFiles(world, args[1]);
        if (!result) {
            MainUtil.sendMessage(player, "&cInvalid template file: " + args[1] + ".template");
            return false;
        }
        File worldFile = MainUtil.getFile(PS.get().IMP.getDirectory(), Settings.Paths.TEMPLATES + File.separator + "tmp-data.yml");
        YamlConfiguration worldConfig = YamlConfiguration.loadConfiguration(worldFile);
        PS.get().worlds.set("worlds." + world, worldConfig.get(""));
        try {
            PS.get().worlds.save(PS.get().worldsFile);
            PS.get().worlds.load(PS.get().worldsFile);
        } catch (InvalidConfigurationException | IOException e) {
            e.printStackTrace();
        }
        String manager = worldConfig.getString("generator.plugin", PS.imp().getPluginName());
        String generator = worldConfig.getString("generator.init", manager);
        int type = worldConfig.getInt("generator.type");
        int terrain = worldConfig.getInt("generator.terrain");

        SetupObject setup = new SetupObject();
        setup.plotManager = manager;
        setup.setupGenerator = generator;
        setup.type = type;
        setup.terrain = terrain;
        setup.step = new ConfigurationNode[0];
        setup.world = world;
        SetupUtils.manager.setupWorld(setup);
        GlobalBlockQueue.IMP.addTask(new Runnable() {
            @Override
            public void run() {
                MainUtil.sendMessage(player, "Done!");
                player.teleport(WorldUtil.IMP.getSpawn(world));
            }
        });
        return true;
    }
}
