package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateDocs {

    public static void main(String[] args) {
        new WE_Anywhere();
        List<Command> commands = MainCommand.getInstance().getCommands();
        GenerateDocs.log("### Want to document some commands?");
        GenerateDocs.log(" - This page is automatically generated");
        GenerateDocs
            .log(" - Fork the project and add a javadoc comment to one of the command classes");
        GenerateDocs.log(" - Then do a pull request and it will be added to this page");
        GenerateDocs.log("");
        GenerateDocs.log("# Contents");
        for (CommandCategory category : CommandCategory.values()) {
            GenerateDocs.log("###### " + category.name());
            for (Command command : MainCommand.getInstance().getCommands(category, null)) {
                GenerateDocs.log(" - [/plot " + command.getId()
                    + "](https://github.com/IntellectualSites/PlotSquared/wiki/Commands#" + command
                    .getId() + ")    ");
            }
            GenerateDocs.log("");
        }
        GenerateDocs.log("# Commands");
        for (Command command : commands) {
            GenerateDocs.printCommand(command);
        }
    }

    public static void printCommand(Command command) {
        try {
            String clazz = command.getClass().getSimpleName();
            String name = command.getId();

            // Header
            String source =
                "https://github.com/IntellectualSites/PlotSquared/tree/master/Core/src/main/java/com/intellectualcrafters/plot/commands/"
                    + clazz + ".java";
            GenerateDocs.log("## [" + name.toUpperCase() + "](" + source + ")    ");

            File file = new File(
                "Core/src/main/java/com/intellectualcrafters/plot/commands/" + clazz + ".java");
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            List<String> perms = GenerateDocs.getPerms(name, lines);
            List<String> usages = GenerateDocs.getUsage(name, lines);
            String comment = GenerateDocs.getComments(lines);

            GenerateDocs.log("#### Description");
            GenerateDocs.log('`' + command.getDescription() + '`');
            if (!comment.isEmpty()) {
                GenerateDocs.log("##### Comments");
                GenerateDocs.log("``` java");
                GenerateDocs.log(comment);
                GenerateDocs.log("```");
            }

            GenerateDocs.log("#### Usage    ");
            String mainUsage = command.getUsage().replaceAll("\\{label\\}", "plot");
            if (!usages.isEmpty() && !usages.get(0).equalsIgnoreCase(mainUsage)) {
                GenerateDocs.log("##### Primary    ");
                GenerateDocs.log(" - `" + mainUsage + "`    ");
                GenerateDocs.log("");
                GenerateDocs.log("##### Other    ");
                GenerateDocs.log(" - `" + StringMan.join(usages, "`\n - `") + "`    ");
                GenerateDocs.log("");
            } else {
                GenerateDocs.log('`' + mainUsage + "`    ");
            }

            if (command.getRequiredType() != RequiredType.NONE) {
                GenerateDocs.log("#### Required callers");
                GenerateDocs.log('`' + command.getRequiredType().name() + '`');
            }

            List<String> aliases = command.getAliases();
            if (!aliases.isEmpty()) {
                GenerateDocs.log("#### Aliases");
                GenerateDocs.log('`' + StringMan.getString(command.getAliases()) + '`');
            }

            GenerateDocs.log("#### Permissions");
            if (!perms.isEmpty()) {
                GenerateDocs.log("##### Primary");
                GenerateDocs.log(" - `" + command.getPermission() + "`    ");
                GenerateDocs.log("");
                GenerateDocs.log("##### Other");
                GenerateDocs.log(" - `" + StringMan.join(perms, "`\n - `") + '`');
                GenerateDocs.log("");
            } else {
                GenerateDocs.log('`' + command.getPermission() + "`    ");
            }
            GenerateDocs.log("***");
            GenerateDocs.log("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getUsage(String cmd, List<String> lines) {
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        HashSet<String> usages = new HashSet<>();
        for (String line : lines) {
            if (line.contains("COMMAND_SYNTAX") && !line.contains("getUsage()")) {
                Matcher m = p.matcher(line);
                String prefix = "";
                StringBuilder usage = new StringBuilder();
                while (m.find()) {
                    String match = m.group(1);
                    usage.append(prefix).append(match);
                    prefix = " <arg> ";
                }
                if (usage.length() != 0) {
                    usages.add(usage.toString().trim().replaceAll("  ", " ")
                        .replaceAll("\\{label\\}", "plot"));
                }
            }
        }
        return new ArrayList<>(usages);
    }

    public static List<String> getPerms(String cmd, List<String> lines) {
        HashSet<String> perms = new HashSet<>();
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Pattern p2 = Pattern.compile("C.PERMISSION_\\s*(\\w+)");
        String last = null;
        for (String line : lines) {

            Matcher m2 = p2.matcher(line);
            while (m2.find()) {
                perms.add(C.valueOf("PERMISSION_" + m2.group(1)).s());
            }
            if (line.contains("Permissions.hasPermission(")) {
                String[] split = line.split("Permissions.hasPermission");
                split = Arrays.copyOfRange(split, 1, split.length);
                for (String method : split) {
                    String perm = method.split("[,|)]")[1].trim();
                    if (!perm.equalsIgnoreCase(perm)) {
                        if (perm.startsWith("C.")) {
                            perm = C.valueOf(perm.split("\\.")[1]).s();
                        } else {
                            continue;
                        }
                    } else {
                        perm = perm.substring(1, perm.length() - 1);
                    }
                    perms.add(perm);
                }
                Matcher m = p.matcher(line);
                while (m.find()) {
                    String perm = m.group(1);
                    if (perm.endsWith(".")) {
                        perm += "<arg>";
                    }
                    if (perm.startsWith(".")) {
                        perms.remove(last);
                        perms.add(last + perm);
                    } else if (perm.contains(".")) {
                        last = perm;
                        perms.add(perm);
                    }
                }
            } else if (line.contains("Permissions.hasPermissionRange")) {
                String[] split = line.split("Permissions.hasPermissionRange");
                split = Arrays.copyOfRange(split, 1, split.length);
                for (String method : split) {
                    String perm = method.split("[,|)]")[1].trim();
                    if (!perm.equalsIgnoreCase(perm)) {
                        if (perm.startsWith("C.")) {
                            perm = C.valueOf(perm.split("\\.")[1]).s();
                        } else {
                            continue;
                        }
                    } else {
                        perm = perm.substring(1, perm.length() - 1);
                    }
                    perms.add(perm + ".<#>");
                }
            }
        }
        switch (cmd.toLowerCase()) {
            case "auto":
            case "claim":
                perms.add("plots.plot.<#>");
                break;
        }
        return new ArrayList<>(perms);
    }

    public static String getComments(List<String> lines) {
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("/** ") || line.startsWith("*/ ") || line.startsWith("* ")) {
                line =
                    line.replaceAll("/[*][*] ", "").replaceAll("[*]/ ", "").replaceAll("[*] ", "")
                        .trim();
                result.append(line).append('\n');
            }
        }
        return result.toString().trim();
    }

    public static void log(String s) {
        System.out.println(s);
    }
}
