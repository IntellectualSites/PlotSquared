package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.StringMan;
import com.plotsquared.general.commands.Command;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateDocs {

    public static void main(String[] args) {
        MainCommand.getInstance().addCommand(new WE_Anywhere());
        MainCommand.getInstance().addCommand(new Cluster());
        final ArrayList<Command<PlotPlayer>> commands = MainCommand.getInstance().getCommands();
        log("### Want to document some commands?");
        log(" - This page is automatically generated");
        log(" - Fork the project and add a javadoc comment to one of the command classes");
        log(" - Then do a pull request and it will be added to this page");
        log("");
        log("# Contents");
        for (final CommandCategory category : CommandCategory.values()) {
            log("###### " + category.name());
            for (final Command<PlotPlayer> command : MainCommand.getCommands(category, null)) {
                log(" - [/plot " + command.getCommand() + "](https://github.com/IntellectualSites/PlotSquared/wiki/Commands#" + command.getCommand() + ")    ");
            }
            log("");
        }
        log("# Commands");
        for (final Command<PlotPlayer> command : commands) {
            printCommand(command);
        }
    }

    public static void printCommand(Command<PlotPlayer> command) {
        try {
            final String clazz = command.getClass().getSimpleName();
            final String name = command.getCommand();

            // Header
            final String source = "https://github.com/IntellectualSites/PlotSquared/tree/master/Core/src/main/java/com/intellectualcrafters/plot/commands/" + clazz + ".java";
            log("## [" + name.toUpperCase() + "](" + source + ")    ");

            final File file = new File("Core/src/main/java/com/intellectualcrafters/plot/commands/" + clazz + ".java");
            final List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            final List<String> perms = getPerms(name, lines);
            final List<String> usages = getUsage(name, lines);
            final String comment = getComments(lines);

            log("#### Description");
            log("`" + command.getDescription() + "`");
            if (!comment.isEmpty()) {
                log("##### Comments");
                log("``` java");
                log(comment);
                log("```");
            }

            log("#### Usage    ");
            {
                String mainUsage = command.getUsage().replaceAll("\\{label\\}", "plot");
                if (!usages.isEmpty() && !usages.get(0).equalsIgnoreCase(mainUsage)) {
                    log("##### Primary    ");
                    log(" - `" + mainUsage + "`    ");
                    log("");
                    log("##### Other    ");
                    log(" - `" + StringMan.join(usages, "`\n - `") + "`    ");
                    log("");
                } else {
                    log("`" + mainUsage + "`    ");
                }
            }

            if (command.getRequiredType() != RequiredType.NONE) {
                log("#### Required callers");
                log("`" + command.getRequiredType().name() + "`");
            }

            final Set<String> aliases = command.getAliases();
            if (!aliases.isEmpty()) {
                log("#### Aliases");
                log("`" + StringMan.getString(command.getAliases()) + "`");
            }

            log("#### Permissions");
            if (!perms.isEmpty()) {
                log("##### Primary");
                log(" - `" + command.getPermission() + "`    ");
                log("");
                log("##### Other");
                log(" - `" + StringMan.join(perms, "`\n - `") + "`");
                log("");
            } else {
                log("`" + command.getPermission() + "`    ");
            }
            log("***");
            log("");
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getUsage(String cmd, List<String> lines) {
        final Pattern p = Pattern.compile("\"([^\"]*)\"");
        HashSet<String> usages = new HashSet<String>();
        for (final String line : lines) {
            if (line.contains("COMMAND_SYNTAX") && !line.contains("getUsage()")) {
                final Matcher m = p.matcher(line);
                String prefix = "";
                StringBuilder usage = new StringBuilder();
                while (m.find()) {
                    String match = m.group(1);
                    usage.append(prefix).append(match);
                    prefix = " <arg> ";
                }
                if (usage.length() != 0) {
                    usages.add(usage.toString().trim().replaceAll("  ", " ").replaceAll("\\{label\\}", "plot"));
                }
            }
        }
        return new ArrayList<>(usages);
    }

    public static List<String> getPerms(String cmd, List<String> lines) {
        final HashSet<String> perms = new HashSet<>();
        final Pattern p = Pattern.compile("\"([^\"]*)\"");
        final Pattern p2 = Pattern.compile("C.PERMISSION_\\s*(\\w+)");
        String last = null;
        for (final String line : lines) {

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
                        }
                        else {
                            continue;
                        }
                    }
                    else {
                        perm = perm.substring(1, perm.length() - 1);
                    }
                    perms.add(perm);
                }
                final Matcher m = p.matcher(line);
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
            }
            else if (line.contains("Permissions.hasPermissionRange")) {
                String[] split = line.split("Permissions.hasPermissionRange");
                split = Arrays.copyOfRange(split, 1, split.length);
                for (String method : split) {
                    String perm = method.split("[,|)]")[1].trim();
                    if (!perm.equalsIgnoreCase(perm)) {
                        if (perm.startsWith("C.")) {
                            perm = C.valueOf(perm.split("\\.")[1]).s();
                        }
                        else {
                            continue;
                        }
                    }
                    else {
                        perm = perm.substring(1, perm.length() - 1);
                    }
                    perms.add(perm + ".<#>");
                }
            }
        }
        switch (cmd.toLowerCase()) {
            case "auto":
            case "claim": {
                perms.add("plots.plot.<#>");
                break;
            }
        }
        return new ArrayList<>(perms);
    }

    public static String getComments(List<String> lines) {
        final StringBuilder result = new StringBuilder();
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("/** ") || line.startsWith("*/ ") || line.startsWith("* ")) {
                line = line.replaceAll("/[*][*] ", "").replaceAll("[*]/ ", "").replaceAll("[*] ", "").trim();
                result.append(line + "\n");
            }
        }
        return result.toString().trim();
    }

    public static void log(final String s) {
        System.out.println(s);
    }
}
