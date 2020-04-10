package com.github.intellectualsites.plotsquared.commands;

import com.github.intellectualsites.plotsquared.plot.config.CaptionUtility;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.database.DBFunc;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;
import com.github.intellectualsites.plotsquared.util.tasks.RunnableVal;
import com.github.intellectualsites.plotsquared.util.tasks.RunnableVal2;
import com.github.intellectualsites.plotsquared.util.tasks.RunnableVal3;
import com.github.intellectualsites.plotsquared.util.MainUtil;
import com.github.intellectualsites.plotsquared.util.Permissions;
import com.github.intellectualsites.plotsquared.util.uuid.UUIDHandler;
import com.google.common.primitives.Ints;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "grant",
    category = CommandCategory.CLAIMING,
    usage = "/plot grant <check|add> [player]",
    permission = "plots.grant",
    requiredType = RequiredType.NONE)
public class Grant extends Command {

    public Grant() {
        super(MainCommand.getInstance(), true);
    }

    @Override public CompletableFuture<Boolean> execute(final PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        checkTrue(args.length >= 1 && args.length <= 2, Captions.COMMAND_SYNTAX, getUsage());
        final String arg0 = args[0].toLowerCase();
        switch (arg0) {
            case "add":
            case "check":
                if (!Permissions.hasPermission(player, CaptionUtility
                    .format(player, Captions.PERMISSION_GRANT.getTranslated(), arg0))) {
                    Captions.NO_PERMISSION.send(player, CaptionUtility
                        .format(player, Captions.PERMISSION_GRANT.getTranslated(), arg0));
                    return CompletableFuture.completedFuture(false);
                }
                if (args.length > 2) {
                    break;
                }
                final UUID uuid;
                if (args.length == 2) {
                    uuid = UUIDHandler.getUUIDFromString(args[1]);
                } else {
                    uuid = player.getUUID();
                }
                if (uuid == null) {
                    Captions.INVALID_PLAYER.send(player, args[1]);
                    return CompletableFuture.completedFuture(false);
                }
                MainUtil.getPersistentMeta(uuid, "grantedPlots", new RunnableVal<byte[]>() {
                    @Override public void run(byte[] array) {
                        if (arg0.equals("check")) { // check
                            int granted;
                            if (array == null) {
                                granted = 0;
                            } else {
                                granted = Ints.fromByteArray(array);
                            }
                            Captions.GRANTED_PLOTS.send(player, granted);
                        } else { // add
                            int amount;
                            if (array == null) {
                                amount = 1;
                            } else {
                                amount = 1 + Ints.fromByteArray(array);
                            }
                            boolean replace = array != null;
                            String key = "grantedPlots";
                            byte[] rawData = Ints.toByteArray(amount);
                            PlotPlayer online = UUIDHandler.getPlayer(uuid);
                            if (online != null) {
                                online.setPersistentMeta(key, rawData);
                            } else {
                                DBFunc.addPersistentMeta(uuid, key, rawData, replace);
                            }
                        }
                    }
                });
                return CompletableFuture.completedFuture(true);
        }
        Captions.COMMAND_SYNTAX.send(player, getUsage());
        return CompletableFuture.completedFuture(true);
    }
}
