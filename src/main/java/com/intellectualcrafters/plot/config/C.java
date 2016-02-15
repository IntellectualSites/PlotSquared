////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.config;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.configuration.file.YamlConfiguration;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.plotsquared.general.commands.CommandCaller;

/**
 * Captions class.
 *

 */
public enum C {
    
    /*
     * Static flags
     */
    FLAG_USE("use", "static.flags"),
    FLAG_BREAK("break", "static.flags"),
    FLAG_PLACE("place", "static.flags"),
    FLAG_PVP("pvp", "static.flags"),
    FLAG_HANGING_PLACE("hanging-place", "static.flags"),
    FLAG_HANGING_BREAK("hanging-break", "static.flags"),
    FLAG_HANGING_INTERACT("hanging-interact", "static.flags"),
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
    /*
     * Static permission
     */
    PERMISSION_STAR("*", "static.permissions"),
    PERMISSION_ADMIN("plots.admin", "static.permissions"),
    PERMISSION_PROJECTILE_UNOWNED("plots.projectile.unowned", "static.permissions"),
    PERMISSION_PROJECTILE_OTHER("plots.projectile.other", "static.permissions"),
    PERMISSION_ADMIN_INTERACT_BLOCKED_CMDS("plots.admin.interact.blockedcommands", "static.permissions"),
    PERMISSION_WORLDEDIT_BYPASS("plots.worldedit.bypass", "static.permissions"),
    PERMISSION_PLOT_TOGGLE_TITLES("plots.toggle.titles", "static.permissions"),
    PERMISSION_PLOT_TOGGLE_CHAT("plots.toggle.chat", "static.permissions"),
    PERMISSION_ADMIN_EXIT_DENIED("plots.admin.exit.denied", "static.permissions"),
    PERMISSION_ADMIN_ENTRY_DENIED("plots.admin.entry.denied", "static.permissions"),
    PERMISSION_COMMANDS_CHAT("plots.admin.command.chat", "static.permissions"),
    PERMISSION_MERGE_OTHER("plots.merge.other", "static.permissions"),
    PERMISSION_ADMIN_DESTROY_UNOWNED("plots.admin.destroy.unowned", "static.permissions"),
    PERMISSION_ADMIN_DESTROY_OTHER("plots.admin.destroy.other", "static.permissions"),
    PERMISSION_ADMIN_DESTROY_ROAD("plots.admin.destroy.road", "static.permissions"),
    PERMISSION_ADMIN_BUILD_ROAD("plots.admin.build.road", "static.permissions"),
    PERMISSION_ADMIN_BUILD_UNOWNED("plots.admin.build.unowned", "static.permissions"),
    PERMISSION_ADMIN_BUILD_OTHER("plots.admin.build.other", "static.permissions"),
    PERMISSION_ADMIN_INTERACT_ROAD("plots.admin.interact.road", "static.permissions"),
    PERMISSION_ADMIN_INTERACT_UNOWNED("plots.admin.interact.unowned", "static.permissions"),
    PERMISSION_ADMIN_INTERACT_OTHER("plots.admin.interact.other", "static.permissions"),
    PERMISSION_ADMIN_BUILD_HEIGHTLIMIT("plots.admin.build.heightlimit", "static.permissions"),
    PERMISSION_ADMIN_UPDATE("plots.admin.command.update", "static.permissions"),
    /*
     * Static console
     */
    CONSOLE_JAVA_OUTDATED_1_7("&cYour java version is outdated. Please update to at least 1.7.\n&cURL: &6https://java.com/en/download/index.jsp","static.console"),
    CONSOLE_JAVA_OUTDATED_1_8("&cIt's really recommended to run Java 1.8, as it increases performance","static.console"),
    CONSOLE_PLEASE_ENABLE_METRICS("&dUsing metrics will allow us to improve the plugin, please consider it :)","static.console"),
    /*
     * Confirm
     */
    FAILED_CONFIRM("$2You have no pending actions to confirm!", "Confirm"),
    REQUIRES_CONFIRM("$2Are you sure you wish to execute: $1%s$2?&-$2This cannot be undone! If you are sure: $1/plot confirm", "Confirm"),
    /*
     * Move
     */
    MOVE_SUCCESS("$4Successfully moved plot.", "Move"),
    COPY_SUCCESS("$4Successfully copied plot.", "Move"),
    REQUIRES_UNOWNED("$2The location specified is already occupied.", "Move"),
    /*
     * Area Create
     */
    SET_ATTRIBUTE("$4Successfully set %s0 set to %s1", "Set"),
    /*
     * Web
     */
    GENERATING_LINK("$1Processing plot...", "Web"),
    GENERATING_LINK_FAILED("$2Failed to generate download link!", "Web"),
    SAVE_FAILED("$2Failed to save", "Web"),
    LOAD_NULL("$2Please use $4/plot load $2to get a list of schematics", "Web"),
    LOAD_FAILED("$2Failed to load schematic", "Web"),
    LOAD_LIST("$2To load a schematic, use $1/plot load #", "Web"),
    SAVE_SUCCESS("$1Successfully saved!", "Web"),
    /*
     * Compass
     */
    COMPASS_TARGET("$4Successfully targeted plot with compass", "Compass"),
    /*
     * Cluster
     */
    CLUSTER_AVAILABLE_ARGS(
    "$1The following sub commands are available: $4list$2, $4create$2, $4delete$2, $4resize$2, $4invite$2, $4kick$2, $4leave$2, $4members$2, $4info$2, $4tp$2, $4sethome",
    "Cluster"),
    CLUSTER_LIST_HEADING("$2There are $1%s$2 clusters in this world", "Cluster"),
    CLUSTER_LIST_ELEMENT("$2 - $1%s&-", "Cluster"),
    CLUSTER_INTERSECTION("$2The proposed area overlaps with: %s0", "Cluster"),
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
    /*
     * Border
     */
    BORDER("$2You are outside the current map border", "Border"),
    /*
     * Unclaim
     */
    UNCLAIM_SUCCESS("$4You successfully unclaimed the plot.", "Unclaim"),
    UNCLAIM_FAILED("$2Could not unclaim the plot", "Unclaim"),
    /*
     * WorldEdit masks
     */
    WORLDEDIT_DELAYED("$2Please wait while we process your WorldEdit action...", "WorldEdit Masks"),
    WORLDEDIT_RUN("$2Apologies for the delay. Now executing: %s", "WorldEdit Masks"),
    REQUIRE_SELECTION_IN_MASK("$2%s of your selection is not within your plot mask. You can only make edits within your plot.", "WorldEdit Masks"),
    WORLDEDIT_VOLUME("$2You cannot select a volume of %current%. The maximum volume you can modify is %max%.", "WorldEdit Masks"),
    WORLDEDIT_ITERATIONS("$2You cannot iterate %current% times. The maximum number of iterations allowed is %max%.", "WorldEdit Masks"),
    WORLDEDIT_UNSAFE("$2Access to that command has been blocked", "WorldEdit Masks"),
    WORLDEDIT_BYPASS("$2&oTo bypass your restrictions use $4/plot wea", "WorldEdit Masks"),
    WORLDEDIT_BYPASSED("$2Currently bypassing WorldEdit restriction.", "WorldEdit Masks"),
    WORLDEDIT_UNMASKED("$1Your WorldEdit is now unrestricted.", "WorldEdit Masks"),
    WORLDEDIT_RESTRICTED("$1Your WorldEdit is now restricted.", "WorldEdit Masks"),
    
    GAMEMODE_WAS_BYPASSED("$1You bypassed the gamemode ($2{gamemode}$1) $1set for $2{plot}", "Gamemode"),
    HEIGHT_LIMIT("$1This plot area has a height limit of $2{limit}", "Height Limit"),
    /*
     * Records
     */
    RECORD_PLAY("$2%player $2started playing record $1%name", "Records"),
    NOTIFY_ENTER("$2%player $2entered your plot ($1%plot$2)", "Records"),
    NOTIFY_LEAVE("$2%player $2left your plot ($1%plot$2)", "Records"),
    /*
     * Swap
     */
    SWAP_OVERLAP("$2The proposed areas are not allowed to overlap", "Swap"),
    SWAP_DIMENSIONS("$2The proposed areas must have comparable dimensions", "Swap"),
    SWAP_SYNTAX("$2/plots swap <id>", "Swap"),
    SWAP_SUCCESS("$4Successfully swapped plots", "Swap"),
    STARTED_SWAP("$2Started plot swap task. You will be notified when it finishes", "Swap"),
    /*
     * Comment
     */
    INBOX_NOTIFICATION("%s unread messages. Use /plot inbox", "Comment"),
    NOT_VALID_INBOX_INDEX("$2No comment at index %s", "Comment"),
    INBOX_ITEM("$2 - $4%s", "Comment"),
    COMMENT_SYNTAX("$2Use /plots comment [X;Z] <%s> <comment>", "Comment"),
    INVALID_INBOX("$2That is not a valid inbox.&-$1Accepted values: %s", "Comment"),
    NO_PERM_INBOX("$2You do not have permission for that inbox", "Comment"),
    NO_PERM_INBOX_MODIFY("$2You do not have permission to modify that inbox", "Comment"),
    NO_PLOT_INBOX("$2You must stand in or supply a plot argument", "Comment"),
    COMMENT_REMOVED("$4Successfully deleted comment/s:n$2 - '$3%s$2'", "Comment"),
    COMMENT_ADDED("$4A comment has been left", "Comment"),
    COMMENT_HEADER("$2&m---------&r $1Comments $2&m---------&r", "Comment"),
    INBOX_EMPTY("$2No comments", "Comment"),
    /*
     * Console
     */
    NOT_CONSOLE("$2For safety reasons, this command can only be executed by console.", "Console"),
    IS_CONSOLE("$2This command can only be executed by a player.", "Console"),
    
    /*
    Inventory
     */
    INVENTORY_USAGE("&cUsage: &6{usage}", "Inventory"),
    INVENTORY_DESC("&cDescription: &6{desc}", "Inventory"),
    INVENTORY_CATEGORY("&cCategory: &6{category}", "Inventory"),
    
    /*
     * Clipboard
     */
    CLIPBOARD_SET("$2The current plot is now copied to your clipboard, use $1/plot paste$2 to paste it", "Clipboard"),
    PASTED("$4The plot selection was successfully pasted. It has been cleared from your clipboard.", "Clipboard"),
    PASTE_FAILED("$2Failed to paste the selection. Reason: $2%s", "Clipboard"),
    NO_CLIPBOARD("$2You don't have a selection in your clipboard", "Clipboard"),
    CLIPBOARD_INFO("$2Current Selection - Plot ID: $1%id$2, Width: $1%width$2, Total Blocks: $1%total$2", "Clipboard"),
    /*
     * Toggle
     */
    TOGGLE_ENABLED("$2Enabled setting: %s", "Toggle"),
    TOGGLE_DISABLED("$2Disabled setting: %s", "Toggle"),
    
    COMMAND_BLOCKED("$2That command is not allowed in this plot", "Blocked Command"),
    /*
     * Done
     */
    DONE_ALREADY_DONE("$2This plot is already marked as done", "Done"),
    DONE_NOT_DONE("$2This plot is not marked as done.", "Done"),
    DONE_INSUFFICIENT_COMPLEXITY("$2This plot is too simple. Please add more detail before using this command.", "Done"),
    DONE_SUCCESS("$1Successfully marked this plot as done.", "Done"),
    DONE_REMOVED("$1You may now continue building in this plot.", "Done"),
    /*
     * Ratings
     */
    RATING_NOT_VALID("$2You need to specify a number between 1 and 10", "Ratings"),
    RATING_ALREADY_EXISTS("$2You have already rated plot $2%s", "Ratings"),
    RATING_APPLIED("$4You successfully rated plot $2%s", "Ratings"),
    RATING_NOT_YOUR_OWN("$2You cannot rate your own plot", "Ratings"),
    RATING_NOT_DONE("$2You can only rate finished plots.", "Ratings"),
    RATING_NOT_OWNED("$2You cannot rate a plot that is not claimed by anyone", "Ratings"),
    /*
     * Tutorial
     */
    RATE_THIS("$2Rate this plot!", "Tutorial"),
    COMMENT_THIS("$2Leave some feedback on this plot: %s", "Tutorial"),
    /*
     * Economy Stuff
     */
    ECON_DISABLED("$2Economy is not enabled", "Economy"),
    CANNOT_AFFORD_PLOT("$2You cannot afford to buy this plot. It costs $1%s", "Economy"),
    NOT_FOR_SALE("$2This plot is not for sale", "Economy"),
    CANNOT_BUY_OWN("$2You cannot buy your own plot", "Economy"),
    PLOT_SOLD("$4Your plot; $1%s0$4, has been sold to $1%s1$4 for $1$%s2", "Economy"),
    CANNOT_AFFORD_MERGE("$2You cannot afford to merge the plots. It costs $1%s", "Economy"),
    ADDED_BALANCE("$1%s $2has been added to your balance", "Economy"),
    REMOVED_BALANCE("$1%s $2has been taken from your balance", "Economy"),
    REMOVED_GRANTED_PLOT("$2You used %s plot grant(s), you've got $1%s $2left", "Economy"),
    /*
     * Setup Stuff
     */
    SETUP_INIT("$1Usage: $2/plot setup <value>", "Setup"),
    SETUP_STEP("$3[$1Step %s0$3] $1%s1 $2- $1Expecting: $2%s2 $1Default: $2%s3", "Setup"),
    SETUP_INVALID_ARG("$2%s0 is not a valid argument for step %s1. To cancel setup use: $1/plot setup cancel", "Setup"),
    SETUP_VALID_ARG("$2Value $1%s0 $2set to %s1", "Setup"),
    SETUP_FINISHED(
    "$4You should have been teleported to the created world. Otherwise you will need to set the generator manually using the bukkit.yml or your chosen world management plugin.",
    "Setup"),
    SETUP_WORLD_TAKEN("$2%s is already a registered plotworld", "Setup"),
    SETUP_MISSING_WORLD(
    "$2You need to specify a world name ($1/plot setup &l<world>$1 <generator>$2)&-$1Additional commands:&-$2 - $1/plot setup <value>&-$2 - $1/plot setup back&-$2 - $1/plot setup cancel",
    "Setup"),
    SETUP_MISSING_GENERATOR(
    "$2You need to specify a generator ($1/plot setup <world> &l<generator>&r$2)&-$1Additional commands:&-$2 - $1/plot setup <value>&-$2 - $1/plot setup back&-$2 - $1/plot setup cancel",
    "Setup"),
    SETUP_INVALID_GENERATOR("$2Invalid generator. Possible options: %s", "Setup"),
    /*
     * Schematic Stuff
     */
    SCHEMATIC_MISSING_ARG("$2You need to specify an argument. Possible values: $1test <name>$2 , $1save$2 , $1paste $2, $1exportall", "Schematics"),
    SCHEMATIC_INVALID("$2That is not a valid schematic. Reason: $2%s", "Schematics"),
    SCHEMATIC_VALID("$2That is a valid schematic", "Schematics"),
    SCHEMATIC_PASTE_FAILED("$2Failed to paste the schematic", "Schematics"),
    SCHEMATIC_PASTE_SUCCESS("$4The schematic pasted successfully", "Schematics"),
    /*
     * Title Stuff
     */
    TITLE_ENTERED_PLOT("$1Plot: %world%;%x%;%z%", "Titles"),
    TITLE_ENTERED_PLOT_SUB("$4Owned by %s", "Titles"),
    PREFIX_GREETING("$1%id%$2> ", "Titles"),
    PREFIX_FAREWELL("$1%id%$2> ", "Titles"),
    /*
     * Core Stuff
     */
    TASK_START("Starting task...", "Core"),
    PREFIX("$3[$1P2$3] $2", "Core"),
    ENABLED("$1PlotSquared is now enabled", "Core"),
    EXAMPLE_MESSAGE("$2This is an example message &k!!!", "Core"),
    /*
     * Reload
     */
    RELOADED_CONFIGS("$1Translations and world settings have been reloaded", "Reload"),
    RELOAD_FAILED("$2Failed to reload file configurations", "Reload"),
    /*
     * BarAPI
     */
    
    DESC_SET("$2Plot description set", "Desc"),
    DESC_UNSET("$2Plot description unset", "Desc"),
    MISSING_DESC("$2You need to specify a description", "Desc"),
    
    /*
     * Alias
     */
    ALIAS_SET_TO("$2Plot alias set to $1%alias%", "Alias"),
    MISSING_ALIAS("$2You need to specify an alias", "Alias"),
    ALIAS_TOO_LONG("$2The alias must be < 50 characters in length", "Alias"),
    ALIAS_IS_TAKEN("$2That alias is already taken", "Alias"),
    /*
     * Position
     */
    MISSING_POSITION("$2You need to specify a position. Possible values: $1none", "Position"),
    POSITION_SET("$1Home position set to your current location", "Position"),
    POSITION_UNSET("$1Home position reset to the default location", "Position"),
    HOME_ARGUMENT("$2Use /plot set home [none]", "Position"),
    INVALID_POSITION("$2That is not a valid position value", "Position"),
    /*
     * Time
     */
    TIME_FORMAT("$1%hours%, %min%, %sec%", "Time"),
    /*
     * Permission
     */
    NO_SCHEMATIC_PERMISSION("$2You don't have the permission required to use schematic $1%s", "Permission"),
    NO_PERMISSION("$2You are lacking the permission node: $1%s", "Permission"),
    NO_PERMISSION_EVENT("$2You are lacking the permission node: $1%s", "Permission"),
    NO_PLOT_PERMS("$2You must be the plot owner to perform this action", "Permission"),
    CANT_CLAIM_MORE_PLOTS("$2You can't claim more plots.", "Permission"),
    CANT_TRANSFER_MORE_PLOTS("$2You can't send more plots to that user", "Permission"),
    CANT_CLAIM_MORE_PLOTS_NUM("$2You can't claim more than $1%s $2plots at once", "Permission"),
    YOU_BE_DENIED("$2You are not allowed to enter this plot", "Permission"),
    
    /*
     * Merge
     */
    MERGE_NOT_VALID("$2This merge request is no longer valid.", "Merge"),
    MERGE_ACCEPTED("$2The merge request has been accepted", "Merge"),
    SUCCESS_MERGE("$2Plots have been merged!", "Merge"),
    MERGE_REQUESTED("$2Successfully sent a merge request", "Merge"),
    MERGE_REQUEST_CONFIRM("merge request from %s", "Permission"),
    NO_PERM_MERGE("$2You are not the owner of the plot: $1%plot%", "Merge"),
    NO_AVAILABLE_AUTOMERGE("$2You do not own any adjacent plots in the specified direction or are not allowed to merge to the required size.", "Merge"),
    UNLINK_REQUIRED("$2An unlink is required to do this.", "Merge"),
    UNLINK_IMPOSSIBLE("$2You can only unlink a mega-plot", "Merge"),
    UNLINK_SUCCESS("$2Successfully unlinked plots.", "Merge"),
    /*
     * Commands
     */
    NOT_VALID_SUBCOMMAND("$2That is not a valid subcommand", "Commands"),
    DID_YOU_MEAN("$2Did you mean: $1%s", "Commands"),
    NAME_LITTLE("$2%s0 name is too short, $1%s1$2<$1%s3", "Commands"),
    NO_COMMANDS("$2I'm sorry, but you're not permitted to use any subcommands.", "Commands"),
    SUBCOMMAND_SET_OPTIONS_HEADER("$2Possible Values: ", "Commands"),
    COMMAND_SYNTAX("$1Usage: $2%s", "Commands"),
    /*
     * Player not found
     */
    INVALID_PLAYER_WAIT("$2Player not found: $1%s$2, fetching it. Try again soon.", "Errors"),
    INVALID_PLAYER("$2Player not found: $1%s$2.", "Errors"),
    INVALID_PLAYER_OFFLINE("$2The player must be online: $1%s.", "Errors"),
    // SETTINGS_PASTE_UPLOADED("$2settings.yml was uploaded to: $1%url%", "Paste"),
    // LATEST_LOG_UPLOADED("$2latest.log was uploaded to: $1%url%", "Paste"),
    DEBUG_REPORT_CREATED("$1Uploaded a full debug to: $1%url%", "Paste"),
    /*
     *
     */
    COMMAND_WENT_WRONG("$2Something went wrong when executing that command...", "Errors"),
    /*
     * purge
     */
    PURGE_SUCCESS("$4Successfully purged %s plots", "Purge"),
    /*
     * trim
     */
    TRIM_SYNTAX("Use /plot trim <all|x;y> <world>", "Trim"),
    TRIM_IN_PROGRESS("A world trim task is already in progress!", "Trim"),
    NOT_VALID_HYBRID_PLOT_WORLD("The hybrid plot manager is required to perform this action", "Trim"),
    /*
     * No <plot>
     */
    NO_FREE_PLOTS("$2There are no free plots available", "Errors"),
    NOT_IN_PLOT("$2You're not in a plot", "Errors"),
    NOT_IN_CLUSTER("$2You must be within a plot cluster to perform that action", "Errors"),
    NOT_IN_PLOT_WORLD("$2You're not in a plot area", "Errors"),
    PLOTWORLD_INCOMPATIBLE("$2The two worlds must be compatible", "Errors"),
    NOT_VALID_WORLD("$2That is not a valid world (case sensitive)", "Errors"),
    NOT_VALID_PLOT_WORLD("$2That is not a valid plot area (case sensitive)", "Errors"),
    NO_PLOTS("$2You don't have any plots", "Errors"),
    /*
     * Block List
     */
    BLOCK_LIST_SEPARATER("$1,$2 ", "Block List"),
    /*
     * Biome
     */
    NEED_BIOME("$2You need to specify a valid biome.", "Biome"),
    BIOME_SET_TO("$2Plot biome set to $2", "Biome"),
    /*
     * Teleport / Entry
     */
    TELEPORTED_TO_PLOT("$1You have been teleported", "Teleport"),
    TELEPORTED_TO_ROAD("$2You got teleported to the road", "Teleport"),
    TELEPORT_IN_SECONDS("$1Teleporting in %s seconds. Do not move...", "Teleport"),
    TELEPORT_FAILED("$2Teleportation cancelled due to movement or damage", "Teleport"),
    /*
     * Set Block
     */
    SET_BLOCK_ACTION_FINISHED("$1The last setblock action is now finished.", "Set Block"),
    
    /*
    AllowUnsafe
     */
    DEBUGALLOWUNSAFE_ON("$2Unsafe actions allowed", "unsafe"),
    DEBUGALLOWUNSAFE_OFF("$2Unsafe actions disabled", "unsafe"),
    /*
     * Debug
     */
    DEBUG_HEADER("$1Debug Information&-", "Debug"),
    DEBUG_SECTION("$2>> $1&l%val%", "Debug"),
    DEBUG_LINE("$2>> $1%var%$2:$1 %val%&-", "Debug"),
    /*
     * Invalid
     */
    NOT_VALID_DATA("$2That's not a valid data id.", "Invalid"),
    NOT_VALID_BLOCK("$2That's not a valid block: %s", "Invalid"),
    NOT_ALLOWED_BLOCK("$2That block is not allowed: %s", "Invalid"),
    NOT_VALID_NUMBER("$2That's not a valid number within the range: %s", "Invalid"),
    NOT_VALID_PLOT_ID("$2That's not a valid plot id.", "Invalid"),
    PLOT_ID_FORM("$2The plot id must be in the form: $1X;Y $2e.g. $1-5;7", "Invalid"),
    NOT_YOUR_PLOT("$2That is not your plot.", "Invalid"),
    NO_SUCH_PLOT("$2There is no such plot", "Invalid"),
    PLAYER_HAS_NOT_BEEN_ON("$2That player hasn't been in the plotworld", "Invalid"),
    FOUND_NO_PLOTS("$2Found no plots with your search query", "Invalid"),
    /*
     * Camera
     */
    CAMERA_STARTED("$2You have entered camera mode for plot $1%s", "Camera"),
    CAMERA_STOPPED("$2You are no longer in camera mode", "Camera"),
    /*
     * Need
     */
    NEED_PLOT_NUMBER("$2You've got to specify a plot number or alias", "Need"),
    NEED_BLOCK("$2You've got to specify a block", "Need"),
    NEED_PLOT_ID("$2You've got to specify a plot id.", "Need"),
    NEED_PLOT_WORLD("$2You've got to specify a plot area.", "Need"),
    NEED_USER("$2You need to specify a username", "Need"),
    /*
     * Info
     */
    NONE("None", "Info"),
    UNKNOWN("Unknown", "Info"),
    EVERYONE("Everyone", "Info"),
    PLOT_UNOWNED("$2The current plot must have an owner to perform this action", "Info"),
    PLOT_INFO_UNCLAIMED("$2Plot $1%s$2 is not yet claimed", "Info"),
    PLOT_INFO_HEADER("$3&m---------&r $1INFO $3&m---------", false, "Info"),
    PLOT_INFO("$1ID: $2%id%$1&-"
    + "$1Alias: $2%alias%$1&-"
    + "$1Owner: $2%owner%$1&-"
    + "$1Biome: $2%biome%$1&-"
    + "$1Can Build: $2%build%$1&-"
    + "$1Rating: $2%rating%&-"
    + "$1Trusted: $2%trusted%$1&-"
    + "$1Members: $2%members%$1&-"
    + "$1Denied: $2%denied%$1&-"
    + "$1Flags: $2%flags%", "Info"),
    PLOT_INFO_FOOTER("$3&m---------&r $1INFO $3&m---------", false, "Info"),
    PLOT_INFO_TRUSTED("$1Trusted:$2 %trusted%", "Info"),
    PLOT_INFO_MEMBERS("$1Members:$2 %members%", "Info"),
    PLOT_INFO_DENIED("$1Denied:$2 %denied%", "Info"),
    PLOT_INFO_FLAGS("$1Flags:$2 %flags%", "Info"),
    PLOT_INFO_BIOME("$1Biome:$2 %biome%", "Info"),
    PLOT_INFO_RATING("$1Rating:$2 %rating%", "Info"),
    PLOT_INFO_OWNER("$1Owner:$2 %owner%", "Info"),
    PLOT_INFO_ID("$1ID:$2 %id%", "Info"),
    PLOT_INFO_ALIAS("$1Alias:$2 %alias%", "Info"),
    PLOT_INFO_SIZE("$1Size:$2 %size%", "Info"),
    PLOT_USER_LIST(" $1%user%$2,", "Info"),
    INFO_SYNTAX_CONSOLE("$2/plot info X;Y", "Info"),
    /*
     * Generating
     */
    GENERATING_COMPONENT("$1Started generating component from your settings", "Working"),
    /*
     * Clearing
     */
    CLEARING_PLOT("$2Clearing plot async.", "Working"),
    CLEARING_DONE("$4Clear completed! Took %sms.", "Working"),
    /*
     * Claiming
     */
    PLOT_NOT_CLAIMED("$2Plot not claimed", "Working"),
    PLOT_IS_CLAIMED("$2This plot is already claimed", "Working"),
    CLAIMED("$4You successfully claimed the plot", "Working"),
    /*
     * List
     */
    COMMENT_LIST_HEADER_PAGED("$2(Page $1%cur$2/$1%max$2) $1List of %amount% comments", "List"),
    CLICKABLE(" (interactive)", "List"),
    AREA_LIST_HEADER_PAGED("$2(Page $1%cur$2/$1%max$2) $1List of %amount% areas", "List"),
    PLOT_LIST_HEADER_PAGED("$2(Page $1%cur$2/$1%max$2) $1List of %amount% plots", "List"),
    PLOT_LIST_HEADER("$1List of %word% plots", "List"),
    PLOT_LIST_ITEM("$2>> $1%id$2:$1%world $2- $1%owner", "List"),
    PLOT_LIST_ITEM_ORDERED("$2[$1%in$2] >> $1%id$2:$1%world $2- $1%owner", "List"),
    PLOT_LIST_FOOTER("$2>> $1%word% a total of $2%num% $1claimed %plot%.", "List"),
    /*
     * Left
     */
    LEFT_PLOT("$2You left a plot", "Left"),
    /*
     * PlotMe
     */
    NOT_USING_PLOTME("$2This server uses the far superior $1PlotSquared $2plot management system. Please use the $1/ps $2or $1/p2 $2or $1/plots $2instead", "Errors"),
    /*
     * Wait
     */
    WAIT_FOR_TIMER("$2A setblock timer is bound to either the current plot or you. Please wait for it to finish", "Errors"),
    /*
     * Chat
     */
    PLOT_CHAT_FORMAT("$2[$1Plot Chat$2][$1%plot_id%$2] $1%sender%$2: $1%msg%", "Chat"),
    PLOT_CHAT_FORCED("$2This world forces everyone to use plot chat.", "Chat"),
    PLOT_CHAT_ON("$4Plot chat enabled.", "Chat"),
    PLOT_CHAT_OFF("$4Plot chat disabled.", "Chat"),
    /*
     * Denied
     */
    DENIED_REMOVED("$4You successfully undenied the player from this plot", "Deny"),
    DENIED_ADDED("$4You successfully denied the player from this plot", "Deny"),
    DENIED_NEED_ARGUMENT("$2Arguments are missing. $1/plot denied add <name> $2or $1/plot denied remove <name>", "Deny"),
    WAS_NOT_DENIED("$2That player was not denied on this plot", "Deny"),
    YOU_GOT_DENIED("$4You are denied from the plot you were previously on, and got teleported to spawn", "Deny"),
    /*
     * Rain
     */
    NEED_ON_OFF("$2You need to specify a value. Possible values: $1on$2, $1off", "Rain"),
    SETTING_UPDATED("$4You successfully updated the setting", "Rain"),
    /*
     * Flag
     */
    FLAG_KEY("$2Key: %s", "Flag"),
    FLAG_TYPE("$2Type: %s", "Flag"),
    FLAG_DESC("$2Desc: %s", "Flag"),
    NOT_VALID_FLAG("$2That is not a valid flag", "Flag"),
    NOT_VALID_VALUE("$2Flag values must be alphanumerical", "Flag"),
    FLAG_NOT_IN_PLOT("$2The plot does not have that flag", "Flag"),
    FLAG_NOT_REMOVED("$2The flag could not be removed", "Flag"),
    FLAG_NOT_ADDED("$2The flag could not be added", "Flag"),
    FLAG_REMOVED("$4Successfully removed flag", "Flag"),
    FLAG_ADDED("$4Successfully added flag", "Flag"),
    FLAG_TUTORIAL_USAGE("$1Have an admin set the flag: $2%s", "Commands"),
    /*
     * Trusted
     */
    TRUSTED_ADDED("$4You successfully trusted a user to the plot", "Trusted"),
    TRUSTED_REMOVED("$4You successfully removed a trusted user from the plot", "Trusted"),
    WAS_NOT_ADDED("$2That player was not trusted on this plot", "Trusted"),
    PLOT_REMOVED_USER("$1Plot %s of which you were added to has been deleted due to owner inactivity", "Trusted"),
    /*
     * Member
     */
    REMOVED_PLAYERS("$2Removed %s players from this plot.", "Member"),
    ALREADY_OWNER("$2That user is already the plot owner.", "Member"),
    ALREADY_ADDED("$2That user is already added to that category.", "Member"),
    MEMBER_ADDED("$4That user can now build while the plot owner is online", "Member"),
    MEMBER_REMOVED("$1You successfully removed a user from the plot", "Member"),
    MEMBER_WAS_NOT_ADDED("$2That player was not added as a user on this plot", "Member"),
    PLOT_MAX_MEMBERS("$2You are not allowed to add any more players to this plot", "Member"),
    /*
     * Set Owner
     */
    SET_OWNER("$4You successfully set the plot owner", "Owner"),
    NOW_OWNER("$4You are now owner of plot %s", "Owner"),
    /*
     * Signs
     */
    OWNER_SIGN_LINE_1("$1ID: $1%id%", "Signs"),
    OWNER_SIGN_LINE_2("$1Owner:", "Signs"),
    OWNER_SIGN_LINE_3("$2%plr%", "Signs"),
    OWNER_SIGN_LINE_4("$3Claimed", "Signs"),
    /*
     * Help
     */
    HELP_HEADER("$3&m---------&r $1Plot\u00B2 Help $3&m---------", "Help"),
    HELP_PAGE_HEADER("$1Category: $2%category%$2,$1 Page: $2%current%$3/$2%max%$2", "Help"),
    HELP_FOOTER("$3&m---------&r $1Plot\u00B2 Help $3&m---------", "Help"),
    
    HELP_INFO_ITEM("$1/plots help %category% $3- $2%category_desc%", "Help"),
    HELP_ITEM("$1%usage% [%alias%]&- $3- $2%desc%&-", "Help"),
    /*
     * Direction
     */
    DIRECTION("$1Current direction: %dir%", "Help"),
    GRANTED_PLOTS("$1You've got $2%s $1grants left", "Grants"),
    GRANTED_PLOT("$1You granted 1 plot to $2%s", "Grants"),
    GRANTED_PLOT_FAILED("$1Grant failed: $2%s", "Grants"),
    /*
     * Custom
     */
    CUSTOM_STRING("-", "-");
    public static final HashMap<String, String> replacements = new HashMap<>();
    /**
     * Translated
     */
    private String s;
    /**
     * Default
     */
    private String d;
    /**
     * What locale category should this translation fall under
     */
    private String cat;
    /**
     * Should the string be prefixed?
     */
    private boolean prefix;
    
    /**
     * Constructor for custom strings.
     */
    C() {
        /*
         * use setCustomString();
         */
    }
    
    /**
     * Constructor
     *
     * @param d default
     * @param prefix use prefix
     */
    C(final String d, final boolean prefix, final String cat) {
        this.d = d;
        if (s == null) {
            s = d;
        }
        this.prefix = prefix;
        this.cat = cat.toLowerCase();
    }
    
    /**
     * Constructor
     *
     * @param d default
     */
    C(final String d, final String cat) {
        this(d, true, cat.toLowerCase());
    }
    
    public static String format(String m, final Object... args) {
        if (args.length == 0) {
            return m;
        }
        final Map<String, String> map = new LinkedHashMap<String, String>();
        if (args.length > 0) {
            for (int i = args.length - 1; i >= 0; i--) {
                String arg = args[i].toString();
                if (arg == null || arg.isEmpty()) {
                    map.put("%s" + i, "");
                } else {
                    arg = C.color(arg);
                    map.put("%s" + i, arg);
                }
                if (i == 0) {
                    map.put("%s", arg);
                }
            }
        }
        m = StringMan.replaceFromMap(m, map);
        return m;
    }
    
    public static String format(final C c, final Object... args) {
        return format(c.s, args);
    }
    
    public static String color(final String string) {
        return StringMan.replaceFromMap(string, replacements);
    }
    
    public static void load(final File file) {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            final YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            final Set<String> keys = yml.getKeys(true);
            final EnumSet<C> all = EnumSet.allOf(C.class);
            final HashSet<String> allNames = new HashSet<>();
            final HashSet<String> allCats = new HashSet<>();
            final HashSet<String> toRemove = new HashSet<>();
            for (final C c : all) {
                allNames.add(c.name());
                allCats.add(c.cat.toLowerCase());
            }
            final HashSet<C> captions = new HashSet<>();
            boolean changed = false;
            for (final String key : keys) {
                if (!yml.isString(key)) {
                    if (!allCats.contains(key)) {
                        toRemove.add(key);
                    }
                    continue;
                }
                final String[] split = key.split("\\.");
                final String node = split[split.length - 1].toUpperCase();
                final C caption = allNames.contains(node) ? valueOf(node) : null;
                if (caption != null) {
                    if (caption.cat.startsWith("static")) {
                        continue;
                    }
                    final String value = yml.getString(key);
                    if (!split[0].equalsIgnoreCase(caption.cat)) {
                        changed = true;
                        yml.set(key, null);
                        yml.set(caption.cat + "." + caption.name().toLowerCase(), value);
                    }
                    captions.add(caption);
                    caption.s = value;
                } else {
                    toRemove.add(key);
                }
            }
            for (final String remove : toRemove) {
                changed = true;
                yml.set(remove, null);
            }
            final ConfigurationSection config = PS.get().style.getConfigurationSection("color");
            final Set<String> styles = config.getKeys(false);
            // HashMap<String, String> replacements = new HashMap<>();
            replacements.clear();
            for (final String style : styles) {
                replacements.put("$" + style, "\u00a7" + config.getString(style));
            }
            for (final char letter : "1234567890abcdefklmnor".toCharArray()) {
                replacements.put("&" + letter, "\u00a7" + letter);
            }
            replacements.put("\\\\n", "\n");
            replacements.put("\\n", "\n");
            replacements.put("&-", "\n");
            for (final C caption : all) {
                if (!captions.contains(caption)) {
                    if (caption.cat.startsWith("static")) {
                        continue;
                    }
                    changed = true;
                    yml.set(caption.cat + "." + caption.name().toLowerCase(), caption.d);
                }
                caption.s = StringMan.replaceFromMap(caption.s, replacements);
            }
            if (changed) {
                yml.save(file);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return s;
    }
    
    public String s() {
        return s;
    }
    
    public boolean usePrefix() {
        return prefix;
    }
    
    public String formatted() {
        return StringMan.replaceFromMap(s(), replacements);
    }
    
    public String getCat() {
        return cat;
    }
    
    public void send(final CommandCaller plr, final String... args) {
        if (plr == null) {
            MainUtil.sendConsoleMessage(this, args);
        } else {
            plr.sendMessage(this, args);
        }
    }
}
