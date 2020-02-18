package com.github.intellectualsites.plotsquared.plot.config;

import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.configuration.file.YamlConfiguration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Captions class.
 */
public enum Captions implements Caption {

    //@formatter:off
    //<editor-fold desc="Static Flags">
    FLAG_USE("use", "static.flags"),
    FLAG_BREAK("break", "static.flags"),
    FLAG_PLACE("place", "static.flags"),
    FLAG_PVP("pvp", "static.flags"),
    FLAG_HANGING_PLACE("hanging-place", "static.flags"),
    FLAG_HANGING_BREAK("hanging-break", "static.flags"),
    FLAG_MISC_INTERACT("misc-interact", "static.flags"),
    FLAG_MISC_BREAK("misc-break", "static.flags"),
    FLAG_MISC_PLACE("misc-place", "static.flags"),
    FLAG_VEHICLE_BREAK("vehicle-break", "static.flags"),
    FLAG_HOSTILE_INTERACT("hostile-interact", "static.flags"),
    FLAG_DEVICE_INTERACT("device-interact", "static.flags"),
    FLAG_ANIMAL_INTERACT("animal-interact", "static.flags"),
    FLAG_VEHICLE_USE("vehicle-use", "static.flags"),
    FLAG_VEHICLE_PLACE("vehicle-place", "static.flags"),
    FLAG_PLAYER_INTERACT("player-interact", "static.flags"),
    FLAG_TAMED_INTERACT("tamed-interact", "static.flags"),
    FLAG_DISABLE_PHYSICS("disable-physics", "static.flags"),
    FLAG_MOB_PLACE("mob-place", "static.flags"),
    //</editor-fold>
    //<editor-fold desc="Static Permission">
    PERMISSION_STAR("*", "static.permissions"),
    PERMISSION_ADMIN("plots.admin", "static.permissions"),
    PERMISSION_ADMIN_SUDO_AREA("plots.admin.area.sudo", "static.permissions"),
    PERMISSION_PROJECTILE_UNOWNED("plots.projectile.unowned", "static.permissions"),
    PERMISSION_PROJECTILE_OTHER("plots.projectile.other", "static.permissions"),
    PERMISSION_ADMIN_INTERACT_BLOCKED_CMDS("plots.admin.interact.blockedcommands", "static.permissions"),
    PERMISSION_WORLDEDIT_BYPASS("plots.worldedit.bypass", "static.permissions"),
    PERMISSION_PLOT_TOGGLE_TITLES("plots.toggle.titles", "static.permissions"),
    PERMISSION_PLOT_TOGGLE_CHAT("plots.toggle.chat", "static.permissions"),
    PERMISSION_PLOT_TOGGLE_TIME("plots.toggle.time", "static.permissions"),
    PERMISSION_ADMIN_UPDATE_NOTIFICATION("plots.admin.update.notify", "static.permissions"),
    PERMISSION_ADMIN_EXIT_DENIED("plots.admin.exit.denied", "static.permissions"),
    PERMISSION_ADMIN_ENTRY_DENIED("plots.admin.entry.denied", "static.permissions"),
    PERMISSION_ADMIN_ENTRY_FORCEFIELD("plots.admin.entry.forcefield", "static.permissions"),
    PERMISSION_COMMANDS_CHAT("plots.admin.command.chat", "static.permissions"),
    PERMISSION_MERGE_OTHER("plots.merge.other", "static.permissions"),
    PERMISSION_MERGE_KEEP_ROAD("plots.merge.keeproad", "static.permissions"),
    PERMISSION_ADMIN_DESTROY_UNOWNED("plots.admin.destroy.unowned", "static.permissions"),
    PERMISSION_ADMIN_DESTROY_GROUNDLEVEL("plots.admin.destroy.groundlevel", "static.permissions"),
    PERMISSION_ADMIN_DESTROY_OTHER("plots.admin.destroy.other", "static.permissions"),
    PERMISSION_ADMIN_DESTROY_ROAD("plots.admin.destroy.road", "static.permissions"),
    PERMISSION_ADMIN_BUILD_ROAD("plots.admin.build.road", "static.permissions"),
    PERMISSION_ADMIN_BUILD_UNOWNED("plots.admin.build.unowned", "static.permissions"),
    PERMISSION_ADMIN_BUILD_OTHER("plots.admin.build.other", "static.permissions"),
    PERMISSION_ADMIN_INTERACT_ROAD("plots.admin.interact.road", "static.permissions"),
    PERMISSION_ADMIN_INTERACT_UNOWNED("plots.admin.interact.unowned", "static.permissions"),
    PERMISSION_ADMIN_INTERACT_OTHER("plots.admin.interact.other", "static.permissions"),
    PERMISSION_ADMIN_BUILD_HEIGHT_LIMIT("plots.admin.build.heightlimit", "static.permissions"),
    PERMISSION_ADMIN_UPDATE("plots.admin.command.update", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_RATE("plots.admin.command.rate", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_TRUST("plots.admin.command.trust", "static.permissions"),
    PERMISSION_TRUST_EVERYONE("plots.trust.everyone", "static.permissions"),
    PERMISSION_AREA_CREATE("plots.area.create", "static.permissions"),
    PERMISSION_AREA_INFO("plots.area.info","static.permissions"),
    PERMISSION_AREA_INFO_FORCE("plots.admin.info.force", "static.permissions"),
    PERMISSION_AREA_LIST("plots.area.list", "static.permissions"),
    PERMISSION_AREA_REGEN("plots.area.regen", "static.permissions"),
    PERMISSION_AREA_TP("plots.area.tp", "static.permissions"),
    PERMISSION_AUTO_MEGA("plots.auto.mega", "static.permissions"),
    PERMISSION_CLAIM_SCHEMATIC("plots.claim.%s0", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_SCHEMATIC("plots.admin.command.schematic", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_CLEAR("plots.admin.command.clear", "static.permissions"),
    PERMISSION_CONTINUE("plots.continue", "static.permissions"),
    PERMISSION_CLUSTER_LIST("plots.cluster.list", "static.permissions"),
    PERMISSION_CLUSTER_CREATE("plots.cluster.create", "static.permissions"),
    PERMISSION_CLUSTER_CREATE_OTHER("plots.cluster.create.other", "static.permissions"),
    PERMISSION_CLUSTER_SIZE("plots.cluster.size", "static.permissions"),
    PERMISSION_CLUSTER_DELETE("plots.cluster.delete", "static.permissions"),
    PERMISSION_CLUSTER_DELETE_OTHER("plots.cluster.delete.other", "static.permissions"),
    PERMISSION_CLUSTER_RESIZE("plots.cluster.resize", "static.permissions"),
    PERMISSION_CLUSTER_RESIZE_OTHER("plots.cluster.resize.other", "static.permissions"),
    PERMISSION_CLUSTER_RESIZE_SHRINK("plots.cluster.resize.shrink", "static.permissions"),
    PERMISSION_CLUSTER_RESIZE_EXPAND("plots.cluster.resize.expand", "static.permissions"),
    PERMISSION_CLUSTER("plots.cluster", "static.permissions"),
    PERMISSION_CLUSTER_INVITE("plots.cluster.invite", "static.permissions"),
    PERMISSION_CLUSTER_INVITE_OTHER("plots.cluster.invite.other", "static.permissions"),
    PERMISSION_CLUSTER_KICK("plots.cluster.kick", "static.permissions"),
    PERMISSION_CLUSTER_KICK_OTHER("plots.cluster.kick.other", "static.permissions"),
    PERMISSION_CLUSTER_LEAVE("plots.cluster.leave", "static.permissions"),
    PERMISSION_CLUSTER_HELPERS("plots.cluster.helpers", "static.permissions"),
    PERMISSION_CLUSTER_TP("plots.cluster.tp", "static.permissions"),
    PERMISSION_CLUSTER_TP_OTHER("plots.cluster.tp.other", "static.permissions"),
    PERMISSION_CLUSTER_INFO("plots.cluster.info", "static.permissions"),
    PERMISSION_CLUSTER_SETHOME("plots.cluster.sethome", "static.permissions"),
    PERMISSION_CLUSTER_SETHOME_OTHER("plots.cluster.sethome.other", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_CONTINUE("plots.admin.command.continue", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_DELETE("plots.admin.command.delete", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_DENY("plots.admin.command.deny", "static.permissions"),
    PERMISSION_DENY_EVERYONE("plots.deny.everyone", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_DONE("plots.admin.command.done", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_DOWNLOAD("plots.admin.command.download", "static.permissions"),
    PERMISSION_DOWNLOAD_WORLD("plots.download.world", "static.permissions"),
    PERMISSION_SET_FLAG_OTHER("plots.set.flag.other", "static.permissions"),
    PERMISSION_SET_FLAG("plots.set.flag", "static.permissions"),
    PERMISSION_SET_FLAG_KEY("plots.set.flag.%s0", "static.permissions"),
    PERMISSION_SET_FLAG_KEY_VALUE("plots.set.flag.%s0.%s1", "static.permissions"),
    PERMISSION_FLAG_REMOVE("plots.flag.remove", "static.permissions"),
    PERMISSION_FLAG_ADD("plots.flag.add", "static.permissions"),
    PERMISSION_FLAG_LIST("plots.flag.list", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_KICK("plots.admin.command.kick", "static.permissions"),
    PERMISSION_GRANT("plots.grant.%s0", "static.permissions"),
    PERMISSION_LIST_FOR_SALE("plots.list.forsale", "static.permissions"),
    PERMISSION_LIST_MINE("plots.list.mine", "static.permissions"),
    PERMISSION_LIST_SHARED("plots.list.shared", "static.permissions"),
    PERMISSION_LIST_WORLD("plots.list.world", "static.permissions"),
    PERMISSION_LIST_WORLD_NAME("plots.list.world.%s0", "static.permissions"),
    PERMISSION_LIST_TOP("plots.list.top", "static.permissions"),
    PERMISSION_LIST_ALL("plots.list.all", "static.permissions"),
    PERMISSION_LIST_UNOWNED("plots.list.unowned", "static.permissions"),
    PERMISSION_LIST_UNKNOWN("plots.list.unknown", "static.permissions"),
    PERMISSION_LIST_PLAYER("plots.list.player", "static.permissions"),
    PERMISSION_LIST_DONE("plots.list.done", "static.permissions"),
    PERMISSION_LIST_EXPIRED("plots.list.expired", "static.permissions"),
    PERMISSION_LIST_FUZZY("plots.list.fuzzy", "static.permissions"),
    PERMISSION_LIST_AREA("plots.list.area", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_LOAD("plots.admin.command.load", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_MERGE("plots.admin.command.merge", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_SET_OWNER("plots.admin.command.setowner", "static.permissions"),
    PERMISSION_COMMENT("plots.comment", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_REMOVE("plots.admin.command.remove", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_SAVE("plots.admin.command.save", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_SCHEMATIC_PASTE("plots.admin.command.schematic.paste", "static.permissions"),
    PERMISSION_SCHEMATIC_PASTE("plots.schematic.paste", "static.permissions"),
    PERMISSION_SCHEMATIC_LIST("plots.schematic.list", "static.permissions"),
    PERMISSION_SCHEMATIC_SAVE("plots.schematic.save", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_SCHEMATIC_SAVE("plots.admin.command.schematic.save", "static.permissions"),
    PERMISSION_SET_COMPONENT("plots.set.%s0", "static.permissions"),
    PERMISSION_ADMIN_COMMAND("plots.admin.command.%s0", "static.permissions"),
    PERMISSION_ADMIN_COMMAND_UNLINK("plots.ad2min.command.unlink", "static.permissions"),
    PERMISSION_VISIT_UNOWNED("plots.visit.unowned", "static.permissions"),
    PERMISSION_VISIT_OWNED("plots.visit.owned", "static.permissions"),
    PERMISSION_SHARED("plots.visit.shared", "static.permissions"),
    PERMISSION_VISIT_OTHER("plots.visit.other", "static.permissions"),
    PERMISSION_HOME("plots.home", "static.permissions"),
    PERMISSION_ALIAS_SET_OBSOLETE("plots.set.alias", "static.permissions"), // Note this is for backwards compatibility
    PERMISSION_ALIAS_SET("plots.alias.set", "static.permissions"),
    PERMISSION_ALIAS_REMOVE("plots.alias.remove", "static.permissions"),
    //</editor-fold>
    //<editor-fold desc="Static Console">
    CONSOLE_JAVA_OUTDATED(
        "&cYour version of java is outdated. It is highly recommended that you update to Java 8 as it increases performance "
            + "and security. %s0 will require Java 8 in a future update.",
        "static.console"),
    CONSOLE_PLEASE_ENABLE_METRICS(
        "&dPlease enable metrics for %s0. Using metrics improves plugin stability, performance, and features. "
            + "Bug fixes and new features are influenced on metrics.", "static.console"),
    //</editor-fold>
    //<editor-fold desc="Confirm">
    EXPIRED_CONFIRM("$2Confirmation has expired, please run the command again!", "Confirm"),
    FAILED_CONFIRM("$2You have no pending actions to confirm!", "Confirm"),
    REQUIRES_CONFIRM("$2Are you sure you wish to execute: $1%s$2?&-$2This cannot be undone! If you are sure: $1/plot confirm", "Confirm"),
    //</editor-fold>
    //<editor-fold desc="Move">
    MOVE_SUCCESS("$4Successfully moved plot.", "Move"),
    COPY_SUCCESS("$4Successfully copied plot.", "Move"),
    REQUIRES_UNOWNED("$2The location specified is already occupied.", "Move"),
    //</editor-fold>
    //<editor-fold desc="Area Create">
    REQUIRES_UNMERGED("$2The plot cannot be merged", "debug"),
    SET_ATTRIBUTE("$4Successfully set %s0 set to %s1", "Set"),
    //</editor-fold>
    //<editor-fold desc="Web">
    GENERATING_LINK("$1Processing plot...", "Web"),
    GENERATING_LINK_FAILED("$2Failed to generate download link!", "Web"),
    SAVE_FAILED("$2Failed to save", "Web"),
    LOAD_NULL("$2Please use $4/plot load $2to get a list of schematics", "Web"),
    LOAD_FAILED("$2Failed to load schematic", "Web"),
    LOAD_LIST("$2To load a schematic, use $1/plot load #", "Web"),
    SAVE_SUCCESS("$1Successfully saved!", "Web"),
    //</editor-fold>
    //<editor-fold desc="Compass">
    COMPASS_TARGET("$4Successfully targeted plot with compass", "Compass"),
    //</editor-fold>
    //<editor-fold desc="Cluster">
    CLUSTER_AVAILABLE_ARGS(
        "$1The following sub commands are available: $4list$2, $4create$2, $4delete$2, $4resize$2, $4invite$2, $4kick$2, $4leave$2, "
            + "$4members$2, $4info$2, $4tp$2, $4sethome", "Cluster"),
    CLUSTER_LIST_HEADING("$2There are $1%s$2 clusters in this world", "Cluster"),
    CLUSTER_LIST_ELEMENT("$2 - $1%s&-", "Cluster"),
    CLUSTER_INTERSECTION("$2The proposed area overlaps with: %s0", "Cluster"),
    CLUSTER_OUTSIDE("$2The proposed area is outside the plot area: %s0", "Cluster"),
    CLUSTER_ADDED("$4Successfully created the cluster.", "Cluster"),
    CLUSTER_DELETED("$4Successfully deleted the cluster.", "Cluster"),
    CLUSTER_RESIZED("$4Successfully resized the cluster.", "Cluster"),
    CLUSTER_ADDED_USER("$4Successfully added user to the cluster.", "Cluster"),
    CANNOT_KICK_PLAYER("$2You cannot kick that player", "Cluster"),
    CLUSTER_INVITED("$1You have been invited to the following cluster: $2%s", "Cluster"),
    CLUSTER_REMOVED("$1You have been removed from cluster: $2%s", "Cluster"),
    CLUSTER_KICKED_USER("$4Successfully kicked the user", "Cluster"),
    INVALID_CLUSTER("$1Invalid cluster name: $2%s", "Cluster"),
    CLUSTER_NOT_ADDED("$2That player was not added to the plot cluster", "Cluster"),
    CLUSTER_CANNOT_LEAVE("$1You must delete or transfer ownership before leaving", "Cluster"),
    CLUSTER_ADDED_HELPER("$4Successfully added a helper to the cluster", "Cluster"),
    CLUSTER_REMOVED_HELPER("$4Successfully removed a helper from the cluster", "Cluster"),
    CLUSTER_REGENERATED("$4Successfully started cluster regeneration", "Cluster"),
    CLUSTER_TELEPORTING("$4Teleporting...", "Cluster"),
    CLUSTER_INFO("$1Current cluster: $2%id%&-$1Name: $2%name%&-$1Owner: $2%owner%&-$1Size: $2%size%&-$1Rights: $2%rights%", "Cluster"),
    //</editor-fold>
    //<editor-fold desc="Border">
    BORDER("$2You are outside the current map border", "Border"),
    WORLDEDIT_BYPASS("$2&oTo bypass your restrictions use $4/plot wea", "WorldEdit Masks"),
    WORLDEDIT_BYPASSED("$2Currently bypassing WorldEdit restriction.", "WorldEdit Masks"),
    GAMEMODE_WAS_BYPASSED("$1You bypassed the GameMode ($2{gamemode}$1) $1set for $2{plot}", "GameMode"),
    HEIGHT_LIMIT("$1This plot area has a height limit of $2{limit}", "Height Limit"),
    //</editor-fold>
    //<editor-fold desc="Records">
    NOTIFY_ENTER("$2%player $2entered your plot ($1%plot$2)", "Records"),
    NOTIFY_LEAVE("$2%player $2left your plot ($1%plot$2)", "Records"),
    //</editor-fold>
    //<editor-fold desc="Swap">
    SWAP_OVERLAP("$2The proposed areas are not allowed to overlap", "Swap"),
    SWAP_DIMENSIONS("$2The proposed areas must have comparable dimensions", "Swap"),
    SWAP_SYNTAX("$2/plot swap <id>", "Swap"),
    SWAP_SUCCESS("$4Successfully swapped plots", "Swap"),
    //</editor-fold>
    //<editor-fold desc="Comments">
    INBOX_NOTIFICATION("%s unread messages. Use /plot inbox", "Comment"),
    NOT_VALID_INBOX_INDEX("$2No comment at index %s", "Comment"),
    INBOX_ITEM("$2 - $4%s", "Comment"),
    COMMENT_SYNTAX("$2Use /plot comment [X;Z] <%s> <comment>", "Comment"),
    INVALID_INBOX("$2That is not a valid inbox.&-$1Accepted values: %s", "Comment"),
    NO_PERM_INBOX("$2You do not have permission for that inbox", "Comment"),
    NO_PERM_INBOX_MODIFY("$2You do not have permission to modify that inbox", "Comment"),
    NO_PLOT_INBOX("$2You must stand in or supply a plot argument", "Comment"),
    COMMENT_REMOVED_SUCCESS("$4Successfully deleted comment/s:n$2 - '$3%s$2'", "Comment"),
    COMMENT_REMOVED_FAILURE("$4Failed to delete comment!", "Comment"),
    COMMENT_ADDED("$4A comment has been left", "Comment"),
    COMMENT_HEADER("$2&m---------&r $1Comments $2&m---------&r", "Comment"),
    INBOX_EMPTY("$2No comments", "Comment"),
    //</editor-fold>
    //<editor-fold desc="Console">
    NOT_CONSOLE("$2For safety reasons, this command can only be executed by console.", "Console"),
    IS_CONSOLE("$2This command can only be executed by a player.", "Console"),
    //</editor-fold>
    //<editor-fold desc="Clipboard">
    PASTE_FAILED("$2Failed to paste the selection. Reason: $2%s", "Clipboard"),
    //</editor-fold>
    //<editor-fold desc="Toggle">
    TOGGLE_ENABLED("$2Enabled setting: %s", "Toggle"),
    TOGGLE_DISABLED("$2Disabled setting: %s", "Toggle"),
    COMMAND_BLOCKED("$2That command is not allowed in this plot", "Blocked Command"),
    //</editor-fold>
    //<editor-fold desc="Done">
    DONE_ALREADY_DONE("$2This plot is already marked as done", "Done"),
    DONE_NOT_DONE("$2This plot is not marked as done.", "Done"),
    DONE_INSUFFICIENT_COMPLEXITY("$2This plot is too simple. Please add more detail before using this command.", "Done"),
    DONE_SUCCESS("$1Successfully marked this plot as done.", "Done"),
    DONE_REMOVED("$1You may now continue building in this plot.", "Done"),
    //</editor-fold>
    //<editor-fold desc="Ratings">
    RATINGS_PURGED("$2Purged ratings for this plot", "Ratings"),
    RATING_NOT_VALID("$2You need to specify a number between 1 and 10", "Ratings"),
    RATING_ALREADY_EXISTS("$2You have already rated plot $2%s", "Ratings"),
    RATING_APPLIED("$4You successfully rated plot $2%s", "Ratings"),
    RATING_DISLIKED("$4You successfully disliked plot $2%s", "Ratings"),
    RATING_LIKED("$4You successfully liked plot $2%s", "Ratings"),
    RATING_NOT_YOUR_OWN("$2You cannot rate your own plot", "Ratings"),
    RATING_NOT_DONE("$2You can only rate finished plots.", "Ratings"),
    RATING_NOT_OWNED("$2You cannot rate a plot that is not claimed by anyone", "Ratings"),
    //</editor-fold>
    //<editor-fold desc="Tutorial">
    RATE_THIS("$2Rate this plot!", "Tutorial"),
    COMMENT_THIS("$2Leave some feedback on this plot: %s", "Tutorial"),
    //</editor-fold>
    //<editor-fold desc="Economy">
    ECON_DISABLED("$2Economy is not enabled", "Economy"),
    CANNOT_AFFORD_PLOT("$2You cannot afford to buy this plot. It costs $1%s", "Economy"),
    NOT_FOR_SALE("$2This plot is not for sale", "Economy"),
    CANNOT_BUY_OWN("$2You cannot buy your own plot", "Economy"),
    PLOT_SOLD("$4Your plot; $1%s0$4, has been sold to $1%s1$4 for $1$%s2", "Economy"),
    CANNOT_AFFORD_MERGE("$2You cannot afford to merge the plots. It costs $1%s","Economy"),
    ADDED_BALANCE("$1%s $2has been added to your balance", "Economy"),
    REMOVED_BALANCE("$1%s $2has been taken from your balance", "Economy"),
    REMOVED_GRANTED_PLOT("$2You used %s plot grant(s), you've got $1%s $2left", "Economy"),
    //</editor-fold>
    //<editor-fold desc="Setup">
    SETUP_INIT("$1Usage: $2/plot setup <value>", "Setup"),
    SETUP_STEP("$3[$1Step %s0$3] $1%s1 $2- $1Expecting: $2%s2 $1Default: $2%s3", "Setup"),
    SETUP_INVALID_ARG("$2%s0 is not a valid argument for step %s1. To cancel setup use: $1/plot setup cancel", "Setup"),
    SETUP_VALID_ARG("$2Value $1%s0 $2set to %s1", "Setup"),
    SETUP_FINISHED(
        "$4You should have been teleported to the created world. Otherwise you will need to set the generator manually using the bukkit.yml or "
            + "your chosen world management plugin.", "Setup"),
    SETUP_WORLD_TAKEN("$2%s is already a world", "Setup"),
    SETUP_MISSING_WORLD(
        "$2You need to specify a world name ($1/plot setup &l<world>$1 <generator>$2)&-$1Additional commands:&-$2 - $1/plot setup <value>&-$2 -"
            + " $1/plot setup back&-$2 - $1/plot setup cancel", "Setup"),
    SETUP_MISSING_GENERATOR(
        "$2You need to specify a generator ($1/plot setup <world> &l<generator>&r$2)&-$1Additional commands:&-$2 - $1/plot setup <value>&-$2 - "
            + "$1/plot setup back&-$2 - $1/plot setup cancel", "Setup"),

    SETUP_INVALID_GENERATOR("$2Invalid generator. Possible options: %s", "Setup"),
    //</editor-fold>
    //<editor-fold desc="Schematic">
    SCHEMATIC_TOO_LARGE("$2The plot is too large for this action!", "Schematics"),
    SCHEMATIC_MISSING_ARG("$2You need to specify an argument. Possible values: $1save$2, $1paste $2, $1exportall$2, $1list", "Schematics"),
    SCHEMATIC_INVALID("$2That is not a valid schematic. Reason: $2%s", "Schematics"),
    SCHEMATIC_VALID("$2That is a valid schematic", "Schematics"),
    SCHEMATIC_PASTE_FAILED("$2Failed to paste the schematic", "Schematics"),
    SCHEMATIC_PASTE_SUCCESS("$4The schematic pasted successfully", "Schematics"),
    SCHEMATIC_LIST("$4Saved Schematics: $1%s", "Schematics"),
    SCHEMATIC_ROAD_CREATED("$1Saved new road schematic. To test the schematic, fly to a few other plots and run /plot debugroadregen", "Schematics"),
    MCA_FILE_SIZE("$1Note: The `.mca` files are 512x512", "Schematics"),
    SCHEMATIC_EXPORTALL_STARTED("$1Starting export...", "Schematics"),
    SCHEMATIC_EXPORTALL_WORLD_ARGS("$1Need world argument. Use $3/plot sch exportall <area>", "Schematics"),
    SCHEMATIC_EXPORTALL_WORLD("$1Invalid world. Use &3/plot sch exportall <area>", "Schematic"),
    SCHEMATIC_EXPORTALL_MASS_STARTED("$1Schematic mass export has been started. This may take a while", "Schematics"),
    SCHEMATIC_EXPORTALL_COUNT("$1Found $3%s $1plots...", "Schematics"),
    SCHEMATIC_EXPORTALL_FINISHED("$1Finished mass export", "Schematics"),
    SCHEMATIC_EXPORTALL_SINGLE_FINISHED("$1Finished export", "Schematics"),
    TASK_IN_PROCESS("$1Task is already running.", "Error"),
    //</editor-fold>
    //<editor-fold desc="Titles">
    TITLE_ENTERED_PLOT("$1Plot: %world%;%x%;%z%", "Titles"),
    TITLE_ENTERED_PLOT_SUB("$4Owned by %s", "Titles"),
    PREFIX_GREETING("$1%id%$2> ", "Titles"),
    PREFIX_FAREWELL("$1%id%$2> ", "Titles"),
    //</editor-fold>
    //<editor-fold desc="Core">
    PREFIX("$3[$1P2$3] $2", "Core"),
    ENABLED("$1%s0 is now enabled", "Core"),
    //</editor-fold>
    //<editor-fold desc="Reload">
    RELOADED_CONFIGS("$1Translations and world settings have been reloaded", "Reload"),
    RELOAD_FAILED("$2Failed to reload file configurations", "Reload"),
    //</editor-fold>
    //<editor-fold desc="Description">
    DESC_SET("$2Plot description set", "Desc"),
    DESC_UNSET("$2Plot description unset", "Desc"),
    //</editor-fold>
    //<editor-fold desc="Alias">
    ALIAS_SET_TO("$2Plot alias set to $1%alias%", "Alias"),
    ALIAS_REMOVED("$2Plot alias removed", "Alias"),

    ALIAS_TOO_LONG("$2The alias must be < 50 characters in length", "Alias"),
    ALIAS_IS_TAKEN("$2That alias is already taken", "Alias"),
    //</editor-fold>
    //<editor-fold desc="Position">
    POSITION_SET("$1Home position set to your current location", "Position"),
    POSITION_UNSET("$1Home position reset to the default location", "Position"),
    HOME_ARGUMENT("$2Use /plot set home [none]", "Position"),
    //</editor-fold>
    //<editor-fold desc="Permission">
    NO_SCHEMATIC_PERMISSION("$2You don't have the permission required to use schematic $1%s", "Permission"),
    NO_PERMISSION("$2You are lacking the permission node: $1%s", "Permission"),
    NO_PERMISSION_EVENT("$2You are lacking the permission node: $1%s", "Permission"),
    NO_PLOT_PERMS("$2You must be the plot owner to perform this action", "Permission"),
    CANT_CLAIM_MORE_PLOTS("$2You can't claim more plots.", "Permission"),
    CANT_CLAIM_MORE_CLUSTERS("$2You can't claim more clusters.", "Permission"),

    CANT_TRANSFER_MORE_PLOTS("$2You can't send more plots to that user", "Permission"),
    CANT_CLAIM_MORE_PLOTS_NUM("$2You can't claim more than $1%s $2plots at once", "Permission"),
    //</editor-fold>
    //<editor-fold desc="Merge">
    MERGE_NOT_VALID("$2This merge request is no longer valid.", "Merge"),
    MERGE_ACCEPTED("$2The merge request has been accepted", "Merge"),
    SUCCESS_MERGE("$2Plots have been merged!", "Merge"),
    MERGE_REQUESTED("$2Successfully sent a merge request", "Merge"),
    MERGE_REQUEST_CONFIRM("Merge request from %s", "Permission"),
    NO_PERM_MERGE("$2You are not the owner of the plot: $1%plot%", "Merge"),
    NO_AVAILABLE_AUTOMERGE("$2You do not own any adjacent plots in the specified direction or are not allowed to merge to the required size.", "Merge"),
    UNLINK_IMPOSSIBLE("$2You can only unlink a mega-plot", "Merge"),
    UNMERGE_CANCELLED("$1Unlink has been cancelled", "Merge"),
    UNLINK_SUCCESS("$2Successfully unlinked plots.", "Merge"),
    //</editor-fold>
    //<editor-fold desc="CommandConfig">
    NOT_VALID_SUBCOMMAND("$2That is not a valid subcommand", "CommandConfig"),
    DID_YOU_MEAN("$2Did you mean: $1%s", "CommandConfig"),
    SUBCOMMAND_SET_OPTIONS_HEADER("$2Possible Values: ", "CommandConfig"),
    COMMAND_SYNTAX("$1Usage: $2%s", "CommandConfig"),
    //</editor-fold>
    //<editor-fold desc="Errors">
    INVALID_PLAYER("$2Player not found: $1%s$2.", "Errors"),
    INVALID_PLAYER_OFFLINE("$2The player must be online: $1%s.", "Errors"),
    INVALID_COMMAND_FLAG("$2Invalid command flag: %s0", "Errors"),
    ERROR("$2An error occurred: %s", "Errors"),
    COMMAND_WENT_WRONG("$2Something went wrong when executing that command...", "Errors"),
    NO_FREE_PLOTS("$2There are no free plots available", "Errors"),
    NOT_IN_PLOT("$2You're not in a plot", "Errors"),
    NOT_LOADED("$2The plot could not be loaded", "Errors"),
    NOT_IN_CLUSTER("$2You must be within a plot cluster to perform that action", "Errors"),
    NOT_IN_PLOT_WORLD("$2You're not in a plot area", "Errors"),
    PLOTWORLD_INCOMPATIBLE("$2The two worlds must be compatible", "Errors"),
    NOT_VALID_WORLD("$2That is not a valid world (case sensitive)", "Errors"),
    NOT_VALID_PLOT_WORLD("$2That is not a valid plot area (case sensitive)", "Errors"),
    NO_PLOTS("$2You don't have any plots", "Errors"),
    WAIT_FOR_TIMER("$2A set block timer is bound to either the current plot or you. Please wait for it to finish", "Errors"),
    //</editor-fold>
    DEBUG_REPORT_CREATED("$1Uploaded a full debug to: $1%url%", "Paste"),
    PURGE_SUCCESS("$4Successfully purged %s plots", "Purge"),
    //<editor-fold desc="Trim">
    TRIM_IN_PROGRESS("A world trim task is already in progress!", "Trim"),
    //</editor-fold>
    //<editor-fold desc="Block List">
    BLOCK_LIST_SEPARATOR("$1,$2 ", "Block List"),
    //</editor-fold>
    //<editor-fold desc="Biome">
    NEED_BIOME("$2You need to specify a valid biome.", "Biome"),
    BIOME_SET_TO("$2Plot biome set to $2", "Biome"),
    //</editor-fold>
    //<editor-fold desc="Teleport">
    TELEPORTED_TO_PLOT("$1You have been teleported", "Teleport"),
    TELEPORTED_TO_ROAD("$2You got teleported to the road", "Teleport"),
    TELEPORT_IN_SECONDS("$1Teleporting in %s seconds. Do not move...", "Teleport"),
    TELEPORT_FAILED("$2Teleportation cancelled due to movement or damage", "Teleport"),
    //</editor-fold>
    //<editor-fold desc="Set Block">
    SET_BLOCK_ACTION_FINISHED("$1The last setblock action is now finished.", "Set Block"),
    //</editor-fold>
    //<editor-fold desc="AllowUnsafe">
    DEBUGALLOWUNSAFE_ON("$2Unsafe actions allowed", "unsafe"),
    DEBUGALLOWUNSAFE_OFF("$2Unsafe actions disabled", "unsafe"),
    //</editor-fold>
    //<editor-fold desc="Debug">
    DEBUG_HEADER("$1Debug Information&-", "Debug"),
    DEBUG_SECTION("$2>> $1&l%val%", "Debug"),
    DEBUG_LINE("$2>> $1%var%$2:$1 %val%&-", "Debug"),
    //</editor-fold>
    //<editor-fold desc="Invalid">
    NOT_VALID_BLOCK("$2That's not a valid block: %s", "Invalid"),
    NOT_ALLOWED_BLOCK("$2That block is not allowed: %s", "Invalid"),
    NOT_VALID_NUMBER("$2That's not a valid number within the range: %s", "Invalid"),
    NOT_VALID_PLOT_ID("$2That's not a valid plot id.", "Invalid"),
    FOUND_NO_PLOTS("$2Found no plots with your search query", "Invalid"),
    NUMBER_NOT_IN_RANGE("That's not a valid number within the range: (%s, %s)", "Invalid"),
    NUMBER_NOT_POSITIVE("That's not a positive number: %s", "Invalid"),
    NOT_A_NUMBER("%s is not a valid number.", "Invalid"),
    //</editor-fold>
    //<editor-fold desc="Need">
    NEED_BLOCK("$2You've got to specify a block", "Need"),
    //</editor-fold>
    //<editor-fold desc="Near">
    PLOT_NEAR("$1Players: %s0", "Near"),
    //</editor-fold>
    //<editor-fold desc="Info">
    NONE(" None", "Info"),
    NOW("Now", "Info"),
    NEVER("Never", "Info"),
    UNKNOWN("Unknown", "Info"),
    SERVER("Server", "Info"),
    EVERYONE("Everyone", "Info"),
    PLOT_UNOWNED("$2The current plot must have an owner to perform this action", "Info"),
    PLOT_INFO_UNCLAIMED("$2Plot $1%s$2 is not yet claimed", "Info"),
    PLOT_INFO_HEADER("$3&m---------&r $1INFO $3&m---------", false, "Info"),
    PLOT_INFO_HIDDEN("$2You cannot view the information about this plot", "Info"),
    PLOT_INFO("$1ID: $2%id%$1&-" + "$1Alias:$2%alias%$1&-" + "$1Owner:$2%owner%$1&-"
        + "$1Biome: $2%biome%$1&-" + "$1Can Build: $2%build%$1&-" + "$1Rating: $2%rating%&-"
        + "$1Seen: $2%seen%&-" + "$1Trusted:$2%trusted%$1&-" + "$1Members:$2%members%$1&-"
        + "$1Denied:$2%denied%$1&-" + "$1Flags:$2%flags%", "Info"),
    PLOT_INFO_FOOTER("$3&m---------&r $1INFO $3&m---------", false, "Info"),
    PLOT_INFO_TRUSTED("$1Trusted:$2%trusted%", "Info"),
    PLOT_INFO_MEMBERS("$1Members:$2%members%", "Info"),
    PLOT_INFO_DENIED("$1Denied:$2%denied%", "Info"),
    PLOT_INFO_FLAGS("$1Flags:$2 %flags%", "Info"),
    PLOT_INFO_BIOME("$1Biome:$2 %biome%", "Info"),
    PLOT_INFO_RATING("$1Rating:$2 %rating%", "Info"),
    PLOT_INFO_LIKES("$1Like Ratio:$2 %likes%%", "Info"),
    PLOT_INFO_OWNER("$1Owner:$2%owner%", "Info"),
    PLOT_INFO_ID("$1ID:$2 %id%", "Info"),
    PLOT_INFO_ALIAS("$1Alias:$2 %alias%", "Info"),
    PLOT_INFO_SIZE("$1Size:$2 %size%", "Info"),
    PLOT_INFO_SEEN("$1Seen:$2 %seen%", "Info"),
    PLOT_USER_LIST(" $1%user%$2,", "Info"),
    PLOT_FLAG_LIST("$1%s0:%s1$2", "Info"),
    INFO_SYNTAX_CONSOLE("$2/plot info X;Z", "Info"),
    //</editor-fold>
    //<editor-fold desc="Working">
    GENERATING_COMPONENT("$1Started generating component from your settings", "Working"),
    CLEARING_DONE("$4Clear completed! Took %sms.", "Working"),
    DELETING_DONE("$4Delete completed! Took %sms.", "Working"),
    PLOT_NOT_CLAIMED("$2Plot not claimed", "Working"),
    PLOT_IS_CLAIMED("$2This plot is already claimed", "Working"),
    CLAIMED("$4You successfully claimed the plot", "Working"),
    //</editor-fold>
    //<editor-fold desc="List">
    COMMENT_LIST_HEADER_PAGED("$2(Page $1%cur$2/$1%max$2) $1List of %amount% comments", "List"),
    CLICKABLE(" (interactive)", "List"),
    AREA_LIST_HEADER_PAGED("$2(Page $1%cur$2/$1%max$2) $1List of %amount% areas", "List"),
    PLOT_LIST_HEADER_PAGED("$2(Page $1%cur$2/$1%max$2) $1List of %amount% plots", "List"),
    PLOT_LIST_HEADER("$1List of %word% plots", "List"),
    PLOT_LIST_ITEM("$2>> $1%id$2:$1%world $2- $1%owner", "List"),
    PLOT_LIST_ITEM_ORDERED("$2[$1%in$2] >> $1%id$2:$1%world $2- $1%owner", "List"),
    PLOT_LIST_FOOTER("$2>> $1%word% a total of $2%num% $1claimed %plot%.", "List"),
    //</editor-fold>
    //<editor-fold desc="Chat">
    PLOT_CHAT_SPY_FORMAT("$2[$1Plot Spy$2][$1%plot_id%$2] $1%sender%$2: $1%msg%", "Chat"),
    PLOT_CHAT_FORMAT("$2[$1Plot Chat$2][$1%plot_id%$2] $1%sender%$2: $1%msg%", "Chat"),
    PLOT_CHAT_FORCED("$2This world forces everyone to use plot chat.", "Chat"),
    PLOT_CHAT_ON("$4Plot chat enabled.", "Chat"),
    PLOT_CHAT_OFF("$4Plot chat disabled.", "Chat"),
    //</editor-fold>
    //<editor-fold desc="Deny">
    DENIED_ADDED("$4You successfully denied the player from this plot", "Deny"),
    DENIED_NEED_ARGUMENT("$2Arguments are missing. $1/plot denied add <name> $2or $1/plot denied remove <name>", "Deny"),
    WAS_NOT_DENIED("$2That player was not denied on this plot", "Deny"),
    YOU_GOT_DENIED("$4You are denied from the plot you were previously on, and got teleported to spawn", "Deny"),
    CANT_REMOVE_OWNER("$2You can't remove the plot owner", "Deny"),
    //</editor-fold>
    YOU_GOT_KICKED("$4You got kicked!", "Kick"),
    //<editor-fold desc="Flag">
    FLAG_KEY("$2Key: %s", "Flag"),
    FLAG_TYPE("$2Type: %s", "Flag"),
    FLAG_DESC("$2Desc: %s", "Flag"),
    NOT_VALID_FLAG("$2That is not a valid flag", "Flag"),
    NOT_VALID_FLAG_SUGGESTED("$2That is not a valid flag. Did you mean: $1%s", "Flag"),
    NOT_VALID_VALUE("$2Flag values must be alphanumerical", "Flag"),
    FLAG_NOT_REMOVED("$2The flag could not be removed", "Flag"),
    FLAG_NOT_ADDED("$2The flag could not be added", "Flag"),
    FLAG_REMOVED("$4Successfully removed flag", "Flag"),
    FLAG_PARTIALLY_REMOVED("$4Successfully removed flag value(s)", "Flag"),
    FLAG_ADDED("$4Successfully added flag", "Flag"),
    FLAG_TUTORIAL_USAGE("$1Have an admin set the flag: $2%s", "CommandConfig"),
    FLAG_LIST_ENTRY("$2%s: $1%s", "Flag"),
    FLAG_LIST_SEE_INFO("Click to view information about the flag", "Flag"),
    FLAG_PARSE_EXCEPTION("$2Failed to parse flag '%s', value '%s': %s", "Flag"),
    //</editor-fold>
    //<editor-fold desc="Flag">
    FLAG_INFO_HEADER("$3&m---------&r $1Plot² Flags $3&m---------", "Flag"),
    FLAG_INFO_FOOTER("$3&m---------&r $1Plot² Flags $3&m---------", "Flag"),
    FLAG_INFO_COLOR_KEY("$1", "Flag"),
    FLAG_INFO_COLOR_VALUE("$2", "Flag"),
    FLAG_INFO_NAME("Name: ", "Flag"),
    FLAG_INFO_CATEGORY("Category: ", "Flag"),
    FLAG_INFO_DESCRIPTION("Description: ", "Flag"),
    FLAG_INFO_EXAMPLE("Example: ", "Flag"),
    FLAG_INFO_DEFAULT_VALUE("Default Value: ", "Flag"),
    //</editor-fold>
    //<editor-fold desc="Flag category captions">
    FLAG_CATEGORY_STRING("String Flags", "Flags"),
    FLAG_CATEGORY_INTEGERS("Integer Flags", "Flags"),
    FLAG_CATEGORY_DOUBLES("Decimal Flags", "Flags"),
    FLAG_CATEGORY_TELEPORT_DENY("Teleport Deny Flag", "Flags"),
    FLAG_CATEGORY_STRING_LIST("String List Flags", "Flags"),
    FLAG_CATEGORY_WEATHER("Weather Flags", "Flags"),
    FLAG_CATEGORY_MUSIC("Music Flags", "Flags"),
    FLAG_CATEGORY_BLOCK_LIST("Material Flags", "Flags"),
    FLAG_CATEGORY_INTERVALS("Interval Flags", "Flags"),
    FLAG_CATEGORY_INTEGER_LIST("Integer List Flags", "Flags"),
    FLAG_CATEGORY_GAMEMODE("Game Mode Flags", "Flags"),
    FLAG_CATEGORY_ENUM("Generic Enum Flags", "Flags"),
    FLAG_CATEGORY_DECIMAL("Decimal Flags", "Flags"),
    FLAG_CATEGORY_BOOLEAN("Boolean Flags", "Flags"),
    FLAG_CATEGORY_MIXED("Mixed Value Flags", "Flags"),
    //</editor-fold>
    //<editor-fold desc="Flag descriptions">
    FLAG_DESCRIPTION_ENTITY_CAP("Set to an integer value to limit the amount of entities on the plot.", "Flags"),
    FLAG_DESCRIPTION_EXPLOSION("Set to 'true' to enable explosions in the plot, and 'false' to disable them.", "Flags"),
    FLAG_DESCRIPTION_MUSIC("Set to a music disk ID (item name) to play the music disc inside of the plot.", "Flags"),
    FLAG_DESCRIPTION_FLIGHT("Set to 'true' to enable flight within the plot when in survival or adventure mode.", "Flags"),
    FLAG_DESCRIPTION_UNTRUSTED("Set to 'false' to disallow untrusted players from visiting the plot.", "Flags"),
    FLAG_DESCRIPTION_DENY_EXIT("Set to 'true' to disallow players from exiting the plot.", "Flags"),
    FLAG_DESCRIPTION_DESCRIPTION("Plot description. Supports '&' color codes.", "Flags"),
    FLAG_DESCRIPTION_GREETING("Message sent to players on plot entry. Supports '&' color codes.", "Flags"),
    FLAG_DESCRIPTION_FAREWELL("Message sent to players when leaving the plot. Supports '&' color codes.", "Flags"),
    FLAG_DESCRIPTION_WEATHER("Specifies the weather conditions inside of the plot.", "Flags"),
    FLAG_DESCRIPTION_ANIMAL_ATTACK("Set to `true` to allow animals to be attacked in the plot.", "Flags"),
    FLAG_DESCRIPTION_ANIMAL_CAP("Set to an integer value to limit the amount of animals on the plot.", "Flags"),
    FLAG_DESCRIPTION_ANIMAL_INTERACT("Set to `true` to allow animals to be interacted with in the plot.", "Flags"),
    FLAG_DESCRIPTION_BLOCK_BURN("Set to `true` to allow blocks to burn within the plot.", "Flags"),
    FLAG_DESCRIPTION_BLOCK_IGNITION("Set to `true` to allow blocks to ignite within the plot.", "Flags"),
    FLAG_DESCRIPTION_BREAK("Define a list of materials players should be able to break even when they aren't added to the plot.", "Flags"),
    FLAG_DESCRIPTION_DEVICE_INTERACT("Set to `true` to allow devices to be interacted with in the plot.", "Flags"),
    FLAG_DESCRIPTION_DISABLE_PHYSICS("Set to `true` to disable block physics in the plot.", "Flags"),
    FLAG_DESCRIPTION_DROP_PROTECTION("Set to `true` to prevent dropped items from being picked up by non-members of the plot.", "Flags"),
    FLAG_DESCRIPTION_FEED("Specify an interval in seconds and an optional amount by which the players will be fed (amount is 1 by default).", "Flags"),
    FLAG_DESCRIPTION_FORCEFIELD("Set to `true` to enable member forcefield in the plot.", "Flags"),
    FLAG_DESCRIPTION_GRASS_GROW("Set to `false` to disable grass to grow within the plot.", "Flags"),
    FLAG_DESCRIPTION_HANGING_BREAK("Set to `true` to allow guests to break hanging objects in the plot.", "Flags"),
    FLAG_DESCRIPTION_HANGING_PLACE("Set to `true` to allow guests to hang objects in the plot.", "Flags"),
    FLAG_DESCRIPTION_HEAL("Specify an interval in seconds and an optional amount by which the players will be healed (amount is 1 by default).", "Flags"),
    FLAG_DESCRIPTION_HIDE_INFO("Set to `true` to hide plot information.", "Flags"),
    FLAG_DESCRIPTION_HOSTILE_ATTACK("Set to `true` to enable players to attack hostile mobs in the plot.", "Flags"),
    FLAG_DESCRIPTION_HOSTILE_CAP("Set to an integer value to limit the amount of hostile entities on the plot.", "Flags"),
    FLAG_DESCRIPTION_HOSTILE_INTERACT("Set to `true` to allow players to interact with hostile mobs in the plot.", "Flags"),
    FLAG_DESCRIPTION_ICE_FORM("Set to `true` to allow ice to form in the plot.", "Flags"),
    FLAG_DESCRIPTION_ICE_MELT("Set to `true` to allow ice to melt in the plot.", "Flags"),
    FLAG_DESCRIPTION_INSTABREAK("Set to `true` to allow blocks to be instantaneously broken in survival mode.", "Flags"),
    FLAG_DESCRIPTION_INVINCIBLE("Set to `true` to prevent players from taking damage inside of the plot.", "Flags"),
    FLAG_DESCRIPTION_ITEM_DROP("Set to `false` to prevent items from being dropped inside of the plot.", "Flags"),
    FLAG_DESCRIPTION_KELP_GROW("Set to `true` to allow kelp to grow in the plot.", "Flags"),
    FLAG_DESCRIPTION_LIQUID_FLOW("Set to `false` to disable liquids from flowing within the plot.", "Flags"),
    FLAG_DESCRIPTION_MISC_BREAK("Set to `true` to allow guests to break miscellaneous items.", "Flags"),
    FLAG_DESCRIPTION_MISC_CAP("Set to an integer value to limit the amount of miscellaneous entities on the plot.", "Flags"),
    FLAG_DESCRIPTION_MISC_INTERACT("Set to `true` to allow guests to interact with miscellaneous items.", "Flags"),
    FLAG_DESCRIPTION_MISC_PLACE("Set to `true` to allow guests to place miscellaneous items.", "Flags"),
    FLAG_DESCRIPTION_MOB_BREAK("Set to `true` to allow mobs to break blocks within the plot.", "Flags"),
    FLAG_DESCRIPTION_MOB_CAP("Set to an integer value to limit the amount of mobs on the plot.", "Flags"),
    FLAG_DESCRIPTION_MOB_PLACE("Set to `true` to allow mobs to place blocks within the plot.", "Flags"),
    FLAG_DESCRIPTION_MYCEL_GROW("Set to `true` to allow mycelium to grow in the plot.", "Flags"),
    FLAG_DESCRIPTION_NOTIFY_ENTER("Set to `true` to notify the plot owners when someone enters the plot.", "Flags"),
    FLAG_DESCRIPTION_NOTIFY_LEAVE("Set to `true` to notify the plot owners when someone leaves the plot.", "Flags"),
    FLAG_DESCRIPTION_NO_WORLDEDIT("Set to `true` to disable WorldEdit usage within the plot.", "Flags"),
    FLAG_DESCRIPTION_PLACE("Define a list of materials players should be able to place in the plot.", "Flags"),
    FLAG_DESCRIPTION_PLAYER_INTERACT("Set to `true` to allow guests to interact with players in the plot.", "Flags"),
    FLAG_DESCRIPTION_PRICE("Set a price for a plot. Must be a positive decimal number.", "Flags"),
    FLAG_DESCRIPTION_PVE("Set to `true` to enable PVE inside the plot.", "Flags"),
    FLAG_DESCRIPTION_PVP("Set to `true` to enable PVP inside the plot.", "Flags"),
    FLAG_DESCRIPTION_REDSTONE("Set to `false` to disable redstone in the plot.", "Flags"),
    FLAG_DESCRIPTION_SERVER_PLOT("Set to `true` to turn the plot into a server plot. This is equivalent to setting the server as the plot owner.", "Flags"),
    FLAG_DESCRIPTION_SNOW_FORM("Set to `true` to allow snow to form within the plot.", "Flags"),
    FLAG_DESCRIPTION_SNOW_MELT("Set to `true` to allow snow to melt within the plot.", "Flags"),
    FLAG_DESCRIPTION_SOIL_DRY("Set to `true` to allow soil to dry within the plot.", "Flags"),
    FLAG_DESCRIPTION_TAMED_ATTACK("Set to `true` to allow guests to attack tamed animals in the plot.", "Flags"),
    FLAG_DESCRIPTION_TAMED_INTERACT("Set to `true` to allow guests to interact with tamed animals in the plot.", "Flags"),
    FLAG_DESCRIPTION_TIME("Set the time in the plot to a fixed value.", "Flags"),
    FLAG_DESCRIPTION_TITLES("Set to `false` to disable plot titles. Can be set to: 'none' (to inherit world settings), 'true', or 'false'", "Flags"),
    FLAG_DESCRIPTION_USE("Define a list of materials players should be able to interact with in the plot.", "Flags"),
    FLAG_DESCRIPTION_VEHICLE_BREAK("Set to `true` to allow guests to break vehicles in the plot.", "Flags"),
    FLAG_DESCRIPTION_VEHICLE_CAP("Set to an integer value to limit the amount of vehicles on the plot.", "Flags"),
    FLAG_DESCRIPTION_VEHICLE_PLACE("Set to `true` to allow guests to place vehicles in the plot.", "Flags"),
    FLAG_DESCRIPTION_VEHICLE_USE("Set to `true` to allow guests to use vehicles in the plot.", "Flags"),
    FLAG_DESCRIPTION_VILLAGER_INTERACT("Set to `true` to allow guests to interact with villagers in the plot.", "Flags"),
    FLAG_DESCRIPTION_VINE_GROW("Set to `true` to allow vines to grow within the plot.", "Flags"),
    FLAG_DESCRIPTION_DENY_TELEPORT("Deny a certain group from teleporting to the plot. Available groups: members, nonmembers, trusted, nontrusted, nonowners", "Flags"),
    FLAG_DESCRIPTION_GAMEMODE("Determines the gamemode in the plot.", "Flags"),
    FLAG_DESCRIPTION_GUEST_GAMEMODE("Determines the guest gamemode in the plot.", "Flags"),
    FLAG_DESCRIPTION_BLOCKED_CMDS("A list of commands that are blocked in the plot.", "Flags"),
    FLAG_DESCRIPTION_KEEP("Prevents the plot from expiring. Can be set to: true, false, the number of milliseconds to keep the plot for or a timestamp (3w 2d 5h).", "Flags"),
    //</editor-fold>
    //<editor-fold desc="Flag category errors">
    FLAG_ERROR_BOOLEAN("Flag value must be a boolean (true|false)", "Flags"),
    FLAG_ERROR_ENUM("Must be one of: %s", "Flags"),
    FLAG_ERROR_GAMEMODE("Flag value must be a gamemode: 'survival', 'creative', 'adventure' or 'spectator.", "Flags"),
    FLAG_ERROR_INTEGER("Flag value must be a whole number", "Flags"),
    FLAG_ERROR_INTEGER_LIST("Flag value must be an integer list", "Flags"),
    FLAG_ERROR_INTERVAL("Value(s) must be numeric. /plot set flag <flag> <interval> [amount]", "Flags"),
    FLAG_ERROR_KEEP("Flag value must be a timestamp or a boolean", "Flags"),
    FLAG_ERROR_LONG("Flag value must be a whole number (large numbers allowed)", "Flags"),
    FLAG_ERROR_PLOTBLOCKLIST("Flag value must be a block list", "Flags"),
    FLAG_ERROR_INVALID_BLOCK("The provided value is not a valid block", "Flags"),
    FLAG_ERROR_DOUBLE("Flag value must be a decimal number.", "Flags"),
    FLAG_ERROR_STRING("Flag value must be alphanumeric. Some special characters are allowed.", "Flags"),
    FLAG_ERROR_STRINGLIST("Flag value must be a string list", "Flags"),
    FLAG_ERROR_WEATHER("Flag must be a weather: 'rain' or 'sun'", "Flags"),
    FLAG_ERROR_MUSIC("Flag value must be a valid music disc ID.", "Flags"),
    //</editor-fold>
    //<editor-fold desc="Trusted">
    TRUSTED_ADDED("$4You successfully trusted a user to the plot", "Trusted"),
    WAS_NOT_ADDED("$2That player was not trusted on this plot", "Trusted"),
    PLOT_REMOVED_USER("$1Plot %s of which you were added to has been deleted due to owner inactivity", "Trusted"),
    //</editor-fold>
    //<editor-fold desc="Member">
    REMOVED_PLAYERS("$2Removed %s players from this plot.", "Member"),
    PLOT_LEFT("$2%s left the plot.", "Member"),
    ALREADY_OWNER("$2That user is already the plot owner: %s0", "Member"),
    ALREADY_ADDED("$2That user is already added to that category: %s0", "Member"),
    MEMBER_ADDED("$4That user can now build while the plot owner is online", "Member"),
    PLOT_MAX_MEMBERS("$2You are not allowed to add any more players to this plot", "Member"),
    NOT_ADDED_TRUSTED("$2You must be added or trusted to the plot to run that command", "Member"),
    //</editor-fold>
    //<editor-fold desc="Set Owner">
    SET_OWNER("$4You successfully set the plot owner", "Owner"),
    SET_OWNER_CANCELLED("$2The set owner action was cancelled", "Owner"),
    SET_OWNER_MISSING_PLAYER("$1You need to specify a new owner. Correct usage is: $2/plot setowner <owner>", "Owner"),
    NOW_OWNER("$4You are now owner of plot %s", "Owner"),
    //</editor-fold>
    //<editor-fold desc="Signs">
    OWNER_SIGN_LINE_1("$1ID: $1%id%", "Signs"),
    OWNER_SIGN_LINE_2("$1Owner:", "Signs"),
    OWNER_SIGN_LINE_3("$2%plr%", "Signs"),
    OWNER_SIGN_LINE_4("$3Claimed", "Signs"),
    //</editor-fold>
    //<editor-fold desc="Help">
    HELP_HEADER("$3&m---------&r $1Plot² Help $3&m---------", "Help"),
    HELP_PAGE_HEADER("$1Category: $2%category%$2,$1 Page: $2%current%$3/$2%max%$2", "Help"),
    HELP_FOOTER("$3&m---------&r $1Plot² Help $3&m---------", "Help"),
    HELP_INFO_ITEM("$1/plot help %category% $3- $2%category_desc%", "Help"),
    HELP_ITEM("$1%usage% [%alias%]&- $3- $2%desc%&-", "Help"),
    HELP_DISPLAY_ALL_COMMANDS("Display all commands", "Help"),
    DIRECTION("$1Current direction: %dir%", "Help"),
    //</editor-fold>
    BUCKET_ENTRIES_IGNORED("$2Total bucket values add up to 1 or more. Blocks without a specified chance will be ignored","Generator_Bucket"),

    //<editor-fold desc="Command Categories">
    COMMAND_CATEGORY_CLAIMING("Claiming", "Category"),
    COMMAND_CATEGORY_TELEPORT("Teleport", "Category"),
    COMMAND_CATEGORY_SETTINGS("Protection", "Category"),
    COMMAND_CATEGORY_CHAT("Chat", "Category"),
    COMMAND_CATEGORY_SCHEMATIC("Web", "Category"),
    COMMAND_CATEGORY_APPEARANCE("Cosmetic", "Category"),
    COMMAND_CATEGORY_INFO("Info", "Category"),
    COMMAND_CATEGORY_DEBUG("Debug", "Category"),
    COMMAND_CATEGORY_ADMINISTRATION("Admin", "Category"),
    //</editor-fold>

    //<editor-fold desc="Grants">
    GRANTED_PLOTS("$1Result: $2%s $1grants left", "Grants"),
    GRANTED_PLOT("$1You granted %s0 plot to $2%s1", "Grants"),
    GRANTED_PLOT_FAILED("$1Grant failed: $2%s", "Grants"),
    //</editor-fold>

    /**
     * Legacy Configuration Conversion
     */
    LEGACY_CONFIG_FOUND("A legacy configuration file was detected. Conversion will be attempted.",
        "LegacyConfig"), LEGACY_CONFIG_BACKUP(
        "A copy of worlds.yml $1have been saved in the file worlds.yml.old$1.",
        "LegacyConfig"), LEGACY_CONFIG_REPLACED("> %s has been replaced with %s",
        "LegacyConfig"), LEGACY_CONFIG_DONE(
        "The conversion has finished. PlotSquared will now be disabled and the new configuration file will"
            + " be used at next startup. Please review the new worlds.yml file. "
            + "Please note that schematics will not be converted, as we are now using WorldEdit to handle schematics. "
            + "You need to re-generate the schematics.",
        "LegacyConfig"), LEGACY_CONFIG_CONVERSION_FAILED(
        "Failed to convert the legacy configuration file. See stack trace for information.",
        "LegacyConfig"),

    CUSTOM_STRING("-", "-");
    //@formatter:on

    public static final HashMap<String, String> replacements = new HashMap<>();

    private final String defaultString;
    private final String category;
    private final boolean prefix;
    private String translatedString;

    Captions(String defaultString, boolean prefix, String category) {
        this.defaultString = defaultString;
        this.translatedString = defaultString;
        this.prefix = prefix;
        this.category = category.toLowerCase();
    }

    Captions(String defaultString, String category) {
        this(defaultString, true, category.toLowerCase());
    }

    public static String color(String string) {
        return StringMan.replaceFromMap(string, replacements);
    }

    public static void load(File file) {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = yml.getKeys(true);
            EnumSet<Captions> allEnums = EnumSet.allOf(Captions.class);
            HashSet<String> allNames = new HashSet<>();
            HashSet<String> categories = new HashSet<>();
            for (Captions caption : allEnums) {
                allNames.add(caption.name());
                categories.add(caption.category.toLowerCase());
            }
            HashSet<Captions> captions = new HashSet<>();
            boolean changed = false;
            HashSet<String> toRemove = new HashSet<>();
            for (String key : keys) {
                if (!yml.isString(key)) {
                    if (!categories.contains(key)) {
                        toRemove.add(key);
                    }
                    continue;
                }
                String[] split = key.split("\\.");
                String node = split[split.length - 1].toUpperCase();
                Captions caption;
                if (allNames.contains(node)) {
                    caption = valueOf(node);
                } else {
                    caption = null;
                }
                if (caption != null) {
                    if (caption.category.startsWith("static")) {
                        continue;
                    }
                    String value = yml.getString(key);
                    if (!split[0].equalsIgnoreCase(caption.category)) {
                        changed = true;
                        yml.set(key, null);
                        yml.set(caption.category + '.' + caption.name().toLowerCase(), value);
                    }
                    captions.add(caption);
                    caption.translatedString = value;
                } else {
                    toRemove.add(key);
                }
            }
            for (String remove : toRemove) {
                changed = true;
                yml.set(remove, null);
            }
            ConfigurationSection config = PlotSquared.get().style.getConfigurationSection("color");
            Set<String> styles = config.getKeys(false);
            // HashMap<String, String> replacements = new HashMap<>();
            replacements.clear();
            for (String style : styles) {
                replacements.put('$' + style, '\u00A7' + config.getString(style));
            }
            for (char letter : "1234567890abcdefklmnor".toCharArray()) {
                replacements.put("&" + letter, "\u00a7" + letter);
            }
            replacements.put("\\\\n", "\n");
            replacements.put("\\n", "\n");
            replacements.put("&-", "\n");
            for (Captions caption : allEnums) {
                if (!captions.contains(caption)) {
                    if (caption.getCategory().startsWith("static")) {
                        continue;
                    }
                    changed = true;
                    yml.set(caption.category + '.' + caption.name().toLowerCase(),
                        caption.defaultString);
                }
                caption.translatedString =
                    StringMan.replaceFromMap(caption.translatedString, replacements);
            }
            if (changed) {
                yml.save(file);
            }
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override public String getTranslated() {
        return this.translatedString;
    }

    public boolean usePrefix() {
        return this.prefix;
    }

    public String getCategory() {
        return this.category;
    }

}
