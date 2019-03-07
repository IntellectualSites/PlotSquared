package com.plotsquared.sponge.util;

import com.google.common.collect.ImmutableList;
import com.intellectualcrafters.plot.commands.MainCommand;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.sponge.SpongeMain;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;
import java.util.*;

public class SpongeCommand implements CommandCallable {

    @Nonnull @Override
    public CommandResult process(@Nonnull CommandSource source, @Nonnull String arguments)
        throws CommandException {
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

    @Nonnull @Override
    public List<String> getSuggestions(@Nonnull CommandSource source, @Nonnull String arguments,
        Location<World> targetPosition)
            throws CommandException {
        if (!(source instanceof Player)) {
            return ImmutableList.of();
        }
        PlotPlayer player = SpongeUtil.getPlayer((Player) source);
        String[] args = arguments.split(" ");
        if (args.length == 0) {
            return Collections.singletonList(MainCommand.getInstance().toString());
        }
        Collection objects = MainCommand.getInstance().tab(player, args, arguments.endsWith(" "));
        if (objects != null && !objects.isEmpty()) {
            List<String> result = new ArrayList<>();
            for (Object o : objects) {
                result.add(o.toString());
            }
            return result;
        }
        List<String> names = new ArrayList<>();
        String startsWith = arguments.endsWith(" ") ? "" : args[args.length - 1];
        for (Map.Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            String name = entry.getKey();
            if (name.startsWith(startsWith)) {
                names.add(name);
            }
        }
        return names;
    }

    @Override public boolean testPermission(@Nonnull CommandSource source) {
        return true;
    }

    @Nonnull @Override public Optional<Text> getShortDescription(@Nonnull CommandSource source) {
        return Optional.of(Text.of("Shows plot help"));
    }

    @Nonnull @Override public Optional<Text> getHelp(@Nonnull CommandSource source) {
        return Optional.of(Text.of("/plot"));
    }

    @Nonnull @Override public Text getUsage(@Nonnull CommandSource source) {
        return Text.of("/plot <command>");
    }

}
