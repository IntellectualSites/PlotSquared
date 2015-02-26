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

import org.bukkit.ChatColor;

import com.intellectualsites.translation.TranslationFile;
import com.intellectualsites.translation.TranslationLanguage;
import com.intellectualsites.translation.TranslationManager;
import com.intellectualsites.translation.TranslationObject;
import com.intellectualsites.translation.YamlTranslationFile;
import com.intellectualsites.translation.bukkit.BukkitTranslation;

/**
 * Captions class.
 *
 * @author Citymonstret
 */
public enum C {
    /*
     * Compass
     */
    COMPASS_TARGET("$4Successfully targeted plot with compass"),
    /*
     * Cluster
     */
    CLUSTER_AVAILABLE_ARGS("$1The following sub commands are available: $4list$2, $4create$2, $4delete$2, $4resize$2, $4invite$2, $4kick$2, $4leave$2, $4helpers$2, $4info$2, $4tp$2, $4sethome"),
    CLUSTER_LIST_HEADING("$2There are $1%s$2 clusters in this world"),
    CLUSTER_LIST_ELEMENT("$2 - $1%s\n"),
    CLUSTER_INTERSECTION("$2The proposed area overlaps with $1%s$2 existing cluster/s"),
    CLUSTER_ADDED("$4Successfully created the cluster."),
    CLUSTER_DELETED("$4Successfully deleted the cluster."),
    CLUSTER_RESIZED("$4Successfully resized the cluster."),
    CLUSTER_ADDED_USER("$4Successfully added user to the cluster."),
    CANNOT_KICK_PLAYER("$2You cannot kick that player"),
    CLUSTER_INVITED("$1You have been invited to the following cluster: $2%s"),
    CLUSTER_REMOVED("$1You have been removed from cluster: $2%s"),
    CLUSTER_KICKED_USER("$4Successfully kicked the user"),
    INVALID_CLUSTER("$1Invalid cluster name: $2%s"),
    CLUSTER_NOT_ADDED("$2That player was not added to the plot cluster"),
    CLUSTER_CANNOT_LEAVE("$1You must delete or transfer ownership before leaving"),
    CLUSTER_ADDED_HELPER("$4Successfully added a helper to the cluster"),
    CLUSTER_REMOVED_HELPER("$4Successfully removed a helper to the cluster"),
    CLUSTER_REGENERATED("$4Successfully started cluster regeneration"),
    CLUSTER_TELEPORTING("$4Teleporting..."),
    CLUSTER_INFO("$1Current cluster: $2%id%\n$1Name: $2%name%\n$1Owner: $2%owner%\n$1Size: $2%size%\n$1Rights: $2%rights%"),
    CLUSTER_CURRENT_PLOTID("$1Current plot: $2%s"),
    /*
     * Border
     */
    BORDER("$2You are outside the current map border"),
    /*
     * Unclaim
     */
    UNCLAIM_SUCCESS("$4You successfully unclaimed the plot."),
    /*
     * WorldEdit masks
     */
    REQUIRE_SELECTION_IN_MASK("$2%s of your selection is not within your plot mask. You can only make edits within your plot."),
    /*
     * Records
     */
    RECORD_PLAY("$2%player $2started playing record $1%name"),
    NOTIFY_ENTER("$2%player $2entered your plot ($1%plot$2)"),
    NOTIFY_LEAVE("$2%player $2left your plot ($1%plot$2)"),
    /*
     * Swap
     */
    SWAP_SYNTAX("$2/plots swap <plot id>"),
    SWAP_SUCCESS("$4Successfully swapped plots"),
    /*
     * Comment
     */
    COMMENT_SYNTAX("$2Use /plots comment <everyone|trusted|helper|owner|admin> <comment>"),
    INVALID_INBOX("$2That is not a valid inbox.\n$1Accepted values: %s"),
    COMMENT_REMOVED("$4Successfully deleted %s."),
    COMMENT_ADDED("$4A comment has been left"),
    /*
     * Console
     */
    NOT_CONSOLE("$2For safety reasons, this command can only be executed by console."),
    IS_CONSOLE("$2This command can only be executed by a player."),
    /*
     * Clipboard
     */
    CLIPBOARD_SET("$2The current plot is now copied to your clipboard, use $1/plot paste$2 to paste it"),
    PASTED("$4The plot selection was successfully pasted. It has been cleared from your clipboard."),
    PASTE_FAILED("$2Failed to paste the selection. Reason: $2%s"),
    NO_CLIPBOARD("$2You don't have a selection in your clipboard"),
    CLIPBOARD_INFO("$2Current Selection - Plot ID: $1%id$2, Width: $1%width$2, Total Blocks: $1%total$2"),
    /*
     * Ratings
     */
    RATING_NOT_VALID("$2You need to specify a number between 1 and 10"),
    RATING_ALREADY_EXISTS("$2You have already rated plot $2%s"),
    RATING_APPLIED("$4You successfully rated plot $2%s"),
    RATING_NOT_YOUR_OWN("$2You cannot rate your own plot"),
    RATING_NOT_OWNED("$2You cannot rate a plot that is not claimed by anyone"),
    /*
     * Economy Stuff
     */
    ECON_DISABLED("$2Economy is not enabled"),
    CANNOT_AFFORD_PLOT("$2You cannot afford to buy this plot. It costs $1%s"),
    NOT_FOR_SALE("$2This plot is not for sale"),
    CANNOT_BUY_OWN("$2You cannot buy your own plot"),
    PLOT_SOLD("$4Your plot; $1%s$4, has been sold to $1%s$4 for $1$%s"),
    CANNOT_AFFORD_MERGE("$2You cannot afford to merge the plots. It costs $1%s"),
    ADDED_BALANCE("$1%s $2has been added to your balance"),
    REMOVED_BALANCE("$1%s $2has been taken from your balance"),
    /*
     * Setup Stuff
     */
    SETUP_INIT("$1Usage: $2/plot setup <value>"),
    SETUP_STEP("$3[$1Step %s$3] $1%s $2- $1Expecting: $2%s $1Default: $2%s"),
    SETUP_INVALID_ARG("$2%s is not a valid argument for step %s. To cancel setup use: $1/plot setup cancel"),
    SETUP_VALID_ARG("$2Value $1%s $2set to %s"),
    SETUP_FINISHED("$3If you are using MULTIVERSE or MULTIWORLD the world should have just been created. Otherwise you will need to add the world manually through the bukkit.yml"),
    SETUP_WORLD_TAKEN("$2%s is already a registered plotworld"),
    SETUP_MISSING_WORLD("$2You need to specify a world name ($1/plot setup &l<world>$1 <generator>$2)\n$1Additional commands:\n$2 - $1/plot setup <value>\n$2 - $1/plot setup back\n$2 - $1/plot setup cancel"),
    SETUP_MISSING_GENERATOR("$2You need to specify a generator ($1/plot setup <world> &l<generator>&r$2)\n$1Additional commands:\n$2 - $1/plot setup <value>\n$2 - $1/plot setup back\n$2 - $1/plot setup cancel"),
    SETUP_INVALID_GENERATOR("$2Invalid generator. Possible options: %s"),
    /*
     * Schematic Stuff
     */
    SCHEMATIC_MISSING_ARG("$2You need to specify an argument. Possible values: $1test <name>$2 , $1save$2 , $1paste $2, $1exportall"),
    SCHEMATIC_INVALID("$2That is not a valid schematic. Reason: $2%s"),
    SCHEMATIC_VALID("$2That is a valid schematic"),
    SCHEMATIC_PASTE_FAILED("$2Failed to paste the schematic"),
    SCHEMATIC_PASTE_SUCCESS("$4The schematic pasted successfully"),
    /*
     * Title Stuff
     */
    TITLE_ENTERED_PLOT("You entered plot %world%;%x%;%z%"),
    TITLE_ENTERED_PLOT_COLOR("GOLD"),
    TITLE_ENTERED_PLOT_SUB("Owned by %s"),
    TITLE_ENTERED_PLOT_SUB_COLOR("RED"),
    TITLE_LEFT_PLOT("You entered plot %s"),
    TITLE_LEFT_PLOT_COLOR("GOLD"),
    TITLE_LEFT_PLOT_SUB("Owned by %s"),
    TITLE_LEFT_PLOT_SUB_COLOR("RED"),
    PREFIX_GREETING("$1%id%$2> "),
    PREFIX_FAREWELL("$1%id%$2> "),
    /*
     * Core Stuff
     */
    PREFIX("$3[$1P\u00B2$3] "),
    ENABLED("$1PlotSquared is now enabled"),
    EXAMPLE_MESSAGE("$2This is an example message &k!!!"),
    /*
     * Reload
     */
    RELOADED_CONFIGS("$1Translations and world settings have been reloaded"),
    RELOAD_FAILED("$2Failed to reload file configurations"),
    /*
     * BarAPI
     */
    BOSSBAR_CLEARING("$2Clearing plot: $1%id%"),
    /*
     * Alias
     */
    ALIAS_SET_TO("$2Plot alias set to $1%alias%"),
    MISSING_ALIAS("$2You need to specify an alias"),
    ALIAS_TOO_LONG("$2The alias must be < 50 characters in length"),
    ALIAS_IS_TAKEN("$2That alias is already taken"),
    /*
     * Position
     */
    MISSING_POSITION("$2You need to specify a position. Possible values: $1none"),
    POSITION_SET("$1Home position set to your current location"),
    HOME_ARGUMENT("$2Use /plot set home [none]"),
    INVALID_POSITION("$2That is not a valid position value"),
    /*
     * Time
     */
    TIME_FORMAT("$1%hours%, %min%, %sec%"),
    /*
     * Permission
     */
    NO_SCHEMATIC_PERMISSION("$2You don't have the permission required to use schematic $1%s"),
    NO_PERMISSION("$2You are lacking the permission node: $1%s"),
    NO_PLOT_PERMS("$2You must be the plot owner to perform this action"),
    CANT_CLAIM_MORE_PLOTS("$2You can't claim more plots."),
    CANT_CLAIM_MORE_PLOTS_NUM("$2You can't claim more than $1%s $2plots at once"),
    YOU_BE_DENIED("$2You are not allowed to enter this plot"),
    NO_PERM_MERGE("$2You are not the owner of the plot: $1%plot%"),
    UNLINK_REQUIRED("$2An unlink is required to do this."),
    UNLINK_IMPOSSIBLE("$2You can only unlink a mega-plot"),
    NO_MERGE_TO_MEGA("$2Mega plots cannot be merged into. Please merge from the desired mega plot."),
    /*
     * Commands
     */
    NOT_VALID_SUBCOMMAND("$2That is not a valid subcommand"),
    DID_YOU_MEAN("$2Did you mean: $1%s"),
    NAME_LITTLE("$2%s name is too short, $1%s$2<$1%s"),
    NO_COMMANDS("$2I'm sorry, but you're not permitted to use any subcommands."),
    SUBCOMMAND_SET_OPTIONS_HEADER("$2Possible Values: "),
    COMMAND_SYNTAX("$1Usage: $2%s"),
    /*
     * Player not found
     */
    INVALID_PLAYER("$2Player not found: $1%s."),
    /*
     *
     */
    COMMAND_WENT_WRONG("$2Something went wrong when executing that command..."),
    /*
     * purge
     */
    PURGE_SYNTAX("Use /plot purge <x;z|player|unowned|unknown|all> <world>"),
    PURGE_SUCCESS("$4Successfully purge %s plots"),
    /*
     * trim
     */
    TRIM_SYNTAX("Use /plot trim <all|x;y> <world>"),
    TRIM_START("Starting a world trim task..."),
    TRIM_IN_PROGRESS("A world trim task is already in progress!"),
    NOT_VALID_HYBRID_PLOT_WORLD("The hybrid plot manager is required to perform this action"),
    /*
     * No <plot>
     */
    NO_FREE_PLOTS("$2There are no free plots available"),
    NOT_IN_PLOT("$2You're not in a plot"),
    NOT_IN_CLUSTER("$2You must be within a plot cluster to perform that action"),
    NOT_IN_PLOT_WORLD("$2You're not in a plot world"),
    NOT_VALID_WORLD("$2That is not a valid world (case sensitive)"),
    NOT_VALID_PLOT_WORLD("$2That is not a valid plot world (case sensitive)"),
    NO_PLOTS("$2You don't have any plots"),
    /*
     * Block List
     */
    NOT_VALID_BLOCK_LIST_HEADER("$2That's not a valid block. Valid blocks are:\\n"),
    BLOCK_LIST_ITEM(" $1%mat%$2,"),
    BLOCK_LIST_SEPARATER("$1,$2 "),
    /*
     * Biome
     */
    NEED_BIOME("$2You have got to specify a biome"),
    BIOME_SET_TO("$2Plot biome set to $2"),
    /*
     * Teleport / Entry
     */
    TELEPORTED_TO_PLOT("$1You have been teleported"),
    TELEPORTED_TO_ROAD("$2You got teleported to the road"),
    TELEPORT_IN_SECONDS("$1Teleporting in %s seconds. Do not move..."),
    TELEPORT_FAILED("$2Teleportation cancelled due to movement or damage"),
    /*
     * Set Block
     */
    SET_BLOCK_ACTION_FINISHED("$1The last setblock action is now finished."),
    /*
     * Debug
     */
    DEUBG_HEADER("$1Debug Information\\n"),
    DEBUG_SECTION("$2>> $1&l%val%"),
    DEBUG_LINE("$2>> $1%var%$2:$1 %val%\\n"),
    /*
     * Invalid
     */
    NOT_VALID_DATA("$2That's not a valid data id."),
    NOT_VALID_BLOCK("$2That's not a valid block."),
    NOT_VALID_NUMBER("$2That's not a valid number"),
    NOT_VALID_PLOT_ID("$2That's not a valid plot id."),
    PLOT_ID_FORM("$2The plot id must be in the form: $1X;Y $2e.g. $1-5;7"),
    NOT_YOUR_PLOT("$2That is not your plot."),
    NO_SUCH_PLOT("$2There is no such plot"),
    PLAYER_HAS_NOT_BEEN_ON("$2That player hasn't been in the plotworld"),
    FOUND_NO_PLOTS("$2Found no plots with your search query"),
    /*
     * Camera
     */
    CAMERA_STARTED("$2You have entered camera mode for plot $1%s"),
    CAMERA_STOPPED("$2You are no longer in camera mode"),
    /*
     * Need
     */
    NEED_PLOT_NUMBER("$2You've got to specify a plot number or alias"),
    NEED_BLOCK("$2You've got to specify a block"),
    NEED_PLOT_ID("$2You've got to specify a plot id."),
    NEED_PLOT_WORLD("$2You've got to specify a plot world."),
    NEED_USER("$2You need to specify a username"),
    /*
     * Info
     */
    PLOT_UNOWNED("$2The current plot must have an owner to perform this action"),
    PLOT_INFO_UNCLAIMED("$2Plot $1%s$2 is not yet claimed"),
    /*
     * PLOT_INFO("" +
     * "$1ID$2: $4%id%$2\n" +
     * "$1Alias$2: $4%alias%\n" +
     * "$1Owner$2: $4%owner%\n" +
     * "$1Helpers$2: $4%helpers%\n" +
     * "$1Trusted$2: $4%trusted%\n" +
     * "$1Denied$2: $4%denied%\n" +
     * "$1Flags$2: $4%flags%\n" +
     * "$1Biome$2: $4%biome%\n" +
     * "$1Rating$2: $4%rating%$2/$410\n" +
     * "$1Can build$2: $4%build%"
     * ),
     */
    PLOT_INFO_HEADER("$3====== $1INFO $3======", false),
    PLOT_INFO("$1ID: $2%id%$1\n" + "$1Alias: $2%alias%$1\n" + "$1Owner: $2%owner%$1\n" + "$1Biome: $2%biome%$1\n" + "$1Can Build: $2%build%$1\n" + "$1Rating: $2%rating%$1/$210$1\n" + "$1Helpers: $2%helpers%$1\n" + "$1Trusted: $2%trusted%$1\n" + "$1Denied: $2%denied%$1\n" + "$1Flags: $2%flags%"),
    PLOT_INFO_HELPERS("$1Helpers:$2 %helpers%"),
    PLOT_INFO_TRUSTED("$1Trusted:$2 %trusted%"),
    PLOT_INFO_DENIED("$1Denied:$2 %denied%"),
    PLOT_INFO_FLAGS("$1Flags:$2 %flags%"),
    PLOT_INFO_BIOME("$1Biome:$2 %biome%"),
    PLOT_INFO_RATING("$1Rating:$2 %rating%"),
    PLOT_INFO_OWNER("$1Owner:$2 %owner%"),
    PLOT_INFO_ID("$1ID:$2 %id%"),
    PLOT_INFO_ALIAS("$1Alias:$2 %alias%"),
    PLOT_INFO_SIZE("$1Size:$2 %size%"),
    PLOT_USER_LIST(" $1%user%$2,"),
    INFO_SYNTAX_CONSOLE("$2/plot info <world> X;Y"),
    /*
     * Generating
     */
    GENERATING_COMPONENT("$1Started generating component from your settings"),
    /*
     * Clearing
     */
    CLEARING_PLOT("$2Clearing plot async."),
    CLEARING_DONE("$4Clear completed! Took %sms."),
    /*
     * Claiming
     */
    PLOT_NOT_CLAIMED("$2Plot not claimed"),
    PLOT_IS_CLAIMED("$2This plot is already claimed"),
    CLAIMED("$4You successfully claimed the plot"),
    /*
     * List
     */
    PLOT_LIST_HEADER_PAGED("$2(Page $1%cur$2/$1%max$2) $1List of %word% plots"),
    PLOT_LIST_HEADER("$1List of %word% plots"),
    PLOT_LIST_ITEM("$2>> $1%id$2:$1%world $2- $1%owner"),
    PLOT_LIST_ITEM_ORDERED("$2[$1%in$2] >> $1%id$2:$1%world $2- $1%owner"),
    PLOT_LIST_FOOTER("$2>> $1%word% a total of $2%num% $1claimed %plot%."),
    /*
     * Left
     */
    LEFT_PLOT("$2You left a plot"),
    /*
     * PlotMe
     */
    NOT_USING_PLOTME("$2This server uses the $1PlotSquared $2plot management system. Please use the $1/plots $2instead"),
    /*
     * Wait
     */
    WAIT_FOR_TIMER("$2A setblock timer is bound to either the current plot or you. Please wait for it to finish"),
    /*
     * Chat
     */
    PLOT_CHAT_FORMAT("$2[$1Plot Chat$2][$1%plot_id%$2] $1%sender%$2: $1%msg%"),
    /*
     * Denied
     */
    DENIED_REMOVED("$4You successfully undenied the player from this plot"),
    DENIED_ADDED("$4You successfully denied the player from this plot"),
    DENIED_NEED_ARGUMENT("$2Arguments are missing. $1/plot denied add <name> $2or $1/plot helpers remove <name>"),
    WAS_NOT_DENIED("$2That player was not denied on this plot"),
    /*
     * Rain
     */
    NEED_ON_OFF("$2You need to specify a value. Possible values: $1on$2, $1off"),
    SETTING_UPDATED("$4You successfully updated the setting"),
    /*
     * Flag
     */
    FLAG_KEY("$2Key: %s"),
    FLAG_TYPE("$2Type: %s"),
    FLAG_DESC("$2Desc: %s"),
    NEED_KEY("$2Possible values: $1%values%"),
    NOT_VALID_FLAG("$2That is not a valid flag"),
    NOT_VALID_VALUE("$2Flag values must be alphanumerical"),
    FLAG_NOT_IN_PLOT("$2The plot does not have that flag"),
    FLAG_NOT_REMOVED("$2The flag could not be removed"),
    FLAG_NOT_ADDED("$2The flag could not be added"),
    FLAG_REMOVED("$4Successfully removed flag"),
    FLAG_ADDED("$4Successfully added flag"),
    /*
     * Helper
     */
    HELPER_ADDED("$4You successfully added a helper to the plot"),
    HELPER_REMOVED("$4You successfully removed a helper from the plot"),
    HELPER_NEED_ARGUMENT("$2Arguments are missing. $1/plot helpers add <name> $2or $1/plot helpers remove <name>"),
    WAS_NOT_ADDED("$2That player was not added as a helper on this plot"),
    PLOT_REMOVED_HELPER("$1Plot %s of which you were added to has been deleted due to owner inactivity"),
    /*
     * Trusted
     */
    ALREADY_OWNER("$2That user is already the plot owner."),
    ALREADY_ADDED("$2That user is already added to that category."),
    TRUSTED_ADDED("$4You successfully added a trusted user to the plot"),
    TRUSTED_REMOVED("$1You successfully removed a trusted user from the plot"),
    TRUSTED_NEED_ARGUMENT("$2Arguments are missing. $1/plot trusted add <name> $2or $1/plot trusted remove <name>"),
    T_WAS_NOT_ADDED("$2That player was not added as a trusted user on this plot"),
    /*
     * Set Owner
     */
    SET_OWNER("$4You successfully set the plot owner"),
    /*
     * Signs
     */
    OWNER_SIGN_LINE_1("$1ID: $1%id%"),
    OWNER_SIGN_LINE_2("$1Owner:"),
    OWNER_SIGN_LINE_3("$2%plr%"),
    OWNER_SIGN_LINE_4("$3Claimed"),
    /*
     * Help
     */
    HELP_HEADER("$3====== $1Plot\u00B2 Help $3======"),
    HELP_CATEGORY("$1Category: $2%category%$2,$1 Page: $2%current%$3/$2%max%$2,$1 Displaying: $2%dis%$3/$2%total%"),
    HELP_INFO("$3====== $1Choose a Category $3======", false),
    HELP_INFO_ITEM("$1/plots help %category% $3- $2%category_desc%"),
    HELP_ITEM("$1%usage% [%alias%]\n $3- $2%desc%\n"),
    /*
     * Direction
     */
    DIRECTION("$1Current direction: %dir%"),
    /*
     * Custom
     */
    CUSTOM_STRING("-");
    /**
     * Special Language
     *
     * @see com.intellectualsites.translation.TranslationLanguage
     */
    protected final static TranslationLanguage lang = new TranslationLanguage("PlotSquared", "this", "use");
    public static String COLOR_1 = "&6", COLOR_2 = "&7", COLOR_3 = "&8", COLOR_4 = "&3";
    /**
     * The TranslationManager
     *
     * @see com.intellectualsites.translation.TranslationManager
     */
    private static TranslationManager manager;
    /**
     * The default file
     *
     * @see com.intellectualsites.translation.TranslationFile
     */
    private static TranslationFile defaultFile;
    /**
     * Default
     */
    private String d;
    /**
     * Translated
     */
    private String s;
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
    C(final String d, final boolean prefix) {
        this.d = d;
        if (this.s == null) {
            this.s = "";
        }
        this.prefix = prefix;
    }

    /**
     * Constructor
     *
     * @param d default
     */
    C(final String d) {
        this(d, true);
    }

    public static void setupTranslations() {
        manager = new TranslationManager();
        defaultFile = new YamlTranslationFile(BukkitTranslation.getParent(), lang, "PlotSquared", manager).read();
        // register everything in this class
        for (final C c : values()) {
            manager.addTranslationObject(new TranslationObject(c.toString(), c.d, "", ""));
        }
    }

    public static void saveTranslations() {
        try {
            manager.saveAll(defaultFile).saveFile(defaultFile);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the default string
     *
     * @return default
     */
    public String d() {
        return this.d;
    }

    /**
     * Get translated if exists
     *
     * @return translated if exists else default
     */
    public String s() {
        final String s = manager.getTranslated(toString(), lang).getTranslated().replaceAll("&-", "\n").replaceAll("\\n", "\n");
        return s.replace("$1", COLOR_1.toString()).replace("$2", COLOR_2.toString()).replace("$3", COLOR_3.toString()).replace("$4", COLOR_4.toString());
        /*
         * if (PlotSquared.translations != null) {
         * final String t = PlotSquared.translations.getString(this.toString());
         * if (t != null) {
         * this.s = t;
         * }
         * }
         * if (this.s.length() < 1) {
         * return "";
         * }
         * return this.s.replace("\\n", "\n");
         */
    }

    public boolean usePrefix() {
        return this.prefix;
    }

    /**
     * @return translated and color decoded
     *
     * @see org.bukkit.ChatColor#translateAlternateColorCodes(char, String)
     */
    public String translated() {
        return ChatColor.translateAlternateColorCodes('&', this.s());
    }
}
