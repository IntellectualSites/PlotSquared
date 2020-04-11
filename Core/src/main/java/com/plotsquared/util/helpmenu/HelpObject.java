package com.plotsquared.util.helpmenu;

import com.plotsquared.commands.Argument;
import com.plotsquared.commands.Command;
import com.plotsquared.config.Captions;
import com.plotsquared.util.StringMan;

public class HelpObject {

    private final String _rendered;

    public HelpObject(final Command command, final String label) {
        _rendered = StringMan.replaceAll(Captions.HELP_ITEM.getTranslated(), "%usage%",
            command.getUsage().replaceAll("\\{label\\}", label), "[%alias%]",
            !command.getAliases().isEmpty() ?
                "(" + StringMan.join(command.getAliases(), "|") + ")" :
                "", "%desc%", command.getDescription(), "%arguments%",
            buildArgumentList(command.getRequiredArguments()), "{label}", label);
    }

    @Override public String toString() {
        return _rendered;
    }

    private String buildArgumentList(final Argument[] arguments) {
        if (arguments == null) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        for (final Argument<?> argument : arguments) {
            builder.append("[").append(argument.getName()).append(" (")
                .append(argument.getExample()).append(")],");
        }
        return arguments.length > 0 ? builder.substring(0, builder.length() - 1) : "";
    }
}
