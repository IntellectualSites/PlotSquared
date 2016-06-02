package com.plotsquared.sponge.util;

import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.sponge.SpongeMain;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SpongeCommand implements CommandCallable {
    
    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        TaskManager.runTask(() -> {
            String id = source.getIdentifier();
            PlotPlayer plotPlayer = null;
            try {
                UUID uuid = UUID.fromString(id);

                Optional<Player> player = SpongeMain.THIS.getServer().getPlayer(uuid);
                if (player.isPresent()) {
                    plotPlayer = SpongeUtil.getPlayer(player.get());
                }
            } catch (Exception ignored) {
                plotPlayer = ConsolePlayer.getConsole();
            }
            MainCommand.onCommand(plotPlayer, arguments.isEmpty() ? new String[]{} : arguments.split(" "));
        });
        return CommandResult.success();
    }
    
    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        if (!(source instanceof Player)) {
            return null;
        }
        PlotPlayer player = SpongeUtil.getPlayer((Player) source);
        String[] args = arguments.split(" ");
        if (args.length == 0) {
            return Collections.singletonList(MainCommand.getInstance().toString());
        }
        Collection objects = MainCommand.getInstance().tab(player, args, arguments.endsWith(" "));
        if (objects == null) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (Object o : objects) {
            result.add(o.toString());
        }
        return result.isEmpty() ? null : result;
}
    
    @Override
    public boolean testPermission(CommandSource source) {
        return true;
    }
    
    @Override
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of("Shows plot help"));
    }
    
    @Override
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.of(Text.of("/plot"));
    }
    
    @Override
    public Text getUsage(CommandSource source) {
        return Text.of("/plot <command>");
    }
    
}
