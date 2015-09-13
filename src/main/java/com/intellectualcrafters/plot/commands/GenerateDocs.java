package com.intellectualcrafters.plot.commands;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.StringMan;
import com.plotsquared.general.commands.Command;

public class GenerateDocs {
    public static void main(final String[] args) {
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
    
    public static void printCommand(final Command<PlotPlayer> command) {
        try {
            final String clazz = command.getClass().getSimpleName();
            final String name = command.getCommand();
            
            // Header
            final String source = "https://github.com/IntellectualSites/PlotSquared/tree/master/src/main/java/com/intellectualcrafters/plot/commands/" + clazz + ".java";
            log("## [" + name.toUpperCase() + "](" + source + ")    ");
            
            final File file = new File("src/main/java/com/intellectualcrafters/plot/commands/" + clazz + ".java");
            final List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            final List<String> perms = getPerms(name, lines);
            final String comment = getComments(lines);
            
            log("#### Description");
            log("`" + command.getDescription() + "`");
            if (comment.length() > 0) {
                log("##### Comments");
                log("``` java");
                log(comment);
                log("```");
            }
            
            log("#### Usage");
            log("`" + command.getUsage().replaceAll("\\{label\\}", "plot") + "`");
            
            if (command.getRequiredType() != RequiredType.NONE) {
                log("#### Required callers");
                log("`" + command.getRequiredType().name() + "`");
            }
            
            final Set<String> aliases = command.getAliases();
            if (aliases.size() > 0) {
                log("#### Aliases");
                log("`" + StringMan.getString(command.getAliases()) + "`");
            }
            
            log("#### Permissions");
            log("##### Primary");
            log(" - `" + command.getPermission() + "`    ");
            if (perms.size() > 0) {
                log("");
                log("##### Other");
                log(" - `" + StringMan.join(perms, "`\n - `") + "`");
            }
            log("");
            log("***");
            log("");
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    
    public static List<String> getPerms(final String cmd, final List<String> lines) {
        final ArrayList<String> perms = new ArrayList<String>();
        final Pattern p = Pattern.compile("\"([^\"]*)\"");
        for (final String line : lines) {
            if (line.contains("Permissions.hasPermission(")) {
                final Matcher m = p.matcher(line);
                while (m.find()) {
                    String perm = m.group(1);
                    if (perm.endsWith(".")) {
                        perm += "<arg>";
                    }
                    if (perm.startsWith(".")) {
                        perms.set(perms.size() - 1, perms.get(perms.size() - 1) + perm);
                    } else if (perm.contains(".")) {
                        perms.add(perm);
                    }
                }
            }
        }
        switch (cmd.toLowerCase()) {
            case "auto":
            case "claim": {
                perms.add("plots.plot.#");
                break;
            }
        }
        return perms;
    }
    
    public static String getComments(final List<String> lines) {
        final StringBuilder result = new StringBuilder();
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("/** ") || line.startsWith("*/ ") || line.startsWith("* ")) {
                line = (line.replaceAll("/[*][*] ", "").replaceAll("[*]/ ", "").replaceAll("[*] ", "")).trim();
                result.append(line + "\n");
            }
        }
        return result.toString().trim();
    }
    
    public static void log(final String s) {
        System.out.println(s);
    }
}
