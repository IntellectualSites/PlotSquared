package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.configuration.file.YamlConfiguration;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.FileBytes;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.plotsquared.general.commands.Command;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public abstract class TemplateCommand extends SubCommand {

    public TemplateCommand(Command parent, Boolean isStatic) {
        super(parent, isStatic);
    }

    public static boolean extractAllFiles(String world, String template) {
        try {
            File folder = MainUtil.getFile(PS.get().IMP.getDirectory(), Settings.Paths.TEMPLATES);
            if (!folder.exists()) {
                return false;
            }
            File input = new File(folder + File.separator + template + ".template");
            File output = PS.get().IMP.getDirectory();
            if (!output.exists()) {
                output.mkdirs();
            }
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(input))) {
                ZipEntry ze = zis.getNextEntry();
                byte[] buffer = new byte[2048];
                while (ze != null) {
                    if (!ze.isDirectory()) {
                        String name = ze.getName().replace('\\', File.separatorChar).replace('/', File.separatorChar);
                        File newFile = new File((output + File.separator + name).replaceAll("__TEMP_DIR__", world));
                        File parent = newFile.getParentFile();
                        if (parent != null) {
                            parent.mkdirs();
                        }
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                    ze = zis.getNextEntry();
                }
                zis.closeEntry();
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] getBytes(PlotArea plotArea) {
        ConfigurationSection section = PS.get().worlds.getConfigurationSection("worlds." + plotArea.worldname);
        YamlConfiguration config = new YamlConfiguration();
        String generator = SetupUtils.manager.getGenerator(plotArea);
        if (generator != null) {
            config.set("generator.plugin", generator);
        }
        for (String key : section.getKeys(true)) {
            config.set(key, section.get(key));
        }
        return config.saveToString().getBytes();
    }

    public static void zipAll(String world, Set<FileBytes> files) throws IOException {
        File output = MainUtil.getFile(PS.get().IMP.getDirectory(), Settings.Paths.TEMPLATES);
        output.mkdirs();
        try (FileOutputStream fos = new FileOutputStream(output + File.separator + world + ".template");
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            for (FileBytes file : files) {
                ZipEntry ze = new ZipEntry(file.path);
                zos.putNextEntry(ze);
                zos.write(file.data);
            }
            zos.closeEntry();
        }
    }
}
