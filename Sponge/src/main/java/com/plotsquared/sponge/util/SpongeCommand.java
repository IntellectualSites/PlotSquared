package com.plotsquared.sponge.util;

import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringComparison;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.general.commands.Command;
import com.plotsquared.sponge.SpongeMain;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.*;

public class SpongeCommand implements CommandCallable {
    
    @Override
    public CommandResult process(final CommandSource cmd, final String string) throws CommandException {
        TaskManager.runTask(new Runnable() {
            @Override
            public void run() {
                final String id = cmd.getIdentifier();
                PlotPlayer pp;
                try {
                    final UUID uuid = UUID.fromString(id);
                    final Player player = SpongeMain.THIS.getServer().getPlayer(uuid).get();
                    pp = SpongeUtil.getPlayer(player);
                } catch (final Exception e) {
                    pp = ConsolePlayer.getConsole();
                }
                MainCommand.onCommand(pp, cmd.getName(), string.isEmpty() ? new String[]{} : string.split(" "));
            }
        });
        return CommandResult.success();
    }
    
    @Override
    public List<String> getSuggestions(final CommandSource source, final String string) throws CommandException {
        if (!(source instanceof Player)) {
            return null;
        }
        final PlotPlayer player = SpongeUtil.getPlayer((Player) source);
        String[] split = string.split(" ");
        if (split.length < 1) {
            return Collections.singletonList("plots");
        }
        if (split.length > 1) {
            return Collections.emptyList();
        }
        final Set<String> tabOptions = new HashSet<>();
        final String arg = split[0].toLowerCase();
        ArrayList<String> labels = new ArrayList<>();
        for (final Command<PlotPlayer> cmd : MainCommand.getInstance().getCommands()) {
            final String label = cmd.getCommand();
            HashSet<String> aliases = new HashSet<>(cmd.getAliases());
            aliases.add(label);
            for (String alias : aliases) {
                labels.add(alias);
                if (alias.startsWith(arg)) {
                    if (Permissions.hasPermission(player, cmd.getPermission())) {
                        tabOptions.add(label);
                    } else {
                        break;
                    }
                }
            }
        }
        String best = new StringComparison<>(arg, labels).getBestMatch();
        tabOptions.add(best);
        if (!tabOptions.isEmpty()) {
            return new ArrayList<>(tabOptions);
        }
        return Collections.emptyList();
}
    
    @Override
    public boolean testPermission(final CommandSource cmd) {
        return true;
    }
    
    @Override
    public Optional<? extends Text> getShortDescription(final CommandSource cmd) {
        return Optional.of(Text.of("Shows plot help"));
    }
    
    @Override
    public Optional<? extends Text> getHelp(final CommandSource cmd) {
        return Optional.of(Text.of("/plot help"));
    }
    
    @Override
    public Text getUsage(final CommandSource cmd) {
        return Text.of("/plot <command>");
    }
    
}
