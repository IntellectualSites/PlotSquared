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

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualsites.translation.*;
import com.intellectualsites.translation.bukkit.BukkitTranslation;
import org.bukkit.ChatColor;

/**
 * Captions class.
 *
 * @author Citymonstret
 */
public enum C {
    /*
     * Records
     */
    RECORD_PLAY("&c%player &cstarted playing record &6%name"),
    NOTIFY_ENTER("&c%player &centered your plot (&6%plot&c)"),
    NOTIFY_LEAVE("&c%player &left your plot (&6%plot&c)"),
    /*
     * Swap
     */
    SWAP_SYNTAX("&c/plots swap <plot id>"),
    SWAP_SUCCESS("&6Successfully swapped plots"),
    /*
     * Comment
     */
    COMMENT_SYNTAX("&cUse /plots comment <everyone|trusted|helper|owner|admin> <comment>"),
    INVALID_INBOX("&cThat is not a valid inbox.\n&6Accepted values: %s"),
    NO_PERM_INBOX("&cYou do not have permission to read that inbox."),
    COMMENT_REMOVED("&aSuccessfully deleted %s."),
    /*
     * Console
     */
    NOT_CONSOLE("&cFor safety reasons, this command can only be executed by console."),
    IS_CONSOLE("&cThis command can only be executed by a player."),
    /*
     * Clipboard
     */
    CLIPBOARD_SET("&cThe current plot is now copied to your clipboard, use &6/plot paste&c to paste it"),
    PASTED("&cThe plot selection was successfully pasted. It has been cleared from your clipboard."),
    PASTE_FAILED("&cFailed to paste the selection. Reason: &c%s"),
    NO_CLIPBOARD("&cYou don't have a selection in your clipboard"),
    CLIPBOARD_INFO("&cCurrent Selection - Plot ID: &6%id&c, Width: &6%width&c, Total Blocks: &6%total&c"),
    /*
     * Ratings
     */
    RATING_NOT_VALID("&cYou need to specify a number between 1 and 10"),
    RATING_ALREADY_EXISTS("&cYou have already rated plot &c%s"),
    RATING_APPLIED("&cYou successfully rated plot &6%s"),
    RATING_NOT_YOUR_OWN("&cYou cannot rate your own plot"),
    RATING_NOT_OWNED("&cYou cannot rate a plot that is not claimed by anyone"),
    /*
     * Economy Stuff
     */
    CANNOT_AFFORD_PLOT("&cYou cannot afford to buy this plot. It costs &6%s"),
    CANNOT_AFFORD_MERGE("&cYou cannot afford to merge the plots. It costs &6%s"),
    ADDED_BALANCE("&6%s &chas been added to your balance"),
    REMOVED_BALANCE("&6%s &chas been taken from your balance"),
    /*
     * Setup Stuff
     */
    SETUP_INIT("&6PlotSquared Setup -> Setup a new plotworld"),
    SETUP_STEP("&cStep &6%s&c: %s &c<Expecting: &6%s&c, Default: &6%s&c>"),
    SETUP_INVALID_ARG("&c%s is not a valid argument for step %s. To cancel setup use: &6/plot setup cancel"),
    SETUP_VALID_ARG("&cValue &6%s &cset for step %s"),
    SETUP_FINISHED("&cFinished setup for world  &c%s.\n&4If you are using MULTIVERSE or MULTIWORLD the world should have just been created. Otherwise you will need to add the world manually through the bukkit.yml"),
    SETUP_WORLD_TAKEN("&c%s is already a registered plotworld"),
    SETUP_MISSING_WORLD("&cYou need to specify a world name (&6/p setup &l<world>&6 <generator>&c)\n&6Additional commands:\n&c - &6/p setup <value>\n&c - &6/p setup back\n&c - &6/p setup cancel"),
    SETUP_MISSING_GENERATOR("&cYou need to specify a generator (&6/p setup <world> &l<generator>&r&c)\n&6Additional commands:\n&c - &6/p setup <value>\n&c - &6/p setup back\n&c - &6/p setup cancel"),
    SETUP_INVALID_GENERATOR("&cInvalid generator. Possible options: %s"),
    /*
     * Schematic Stuff
     */
    SCHEMATIC_MISSING_ARG("&cYou need to specify an argument. Possible values: &6test <name>&7 , &6save&7 , &6paste &7, &6exportall"),
    SCHEMATIC_INVALID("&cThat is not a valid schematic. Reason: &c%s"),
    SCHEMATIC_VALID("&cThat is a valid schematic"),
    SCHEMATIC_PASTE_FAILED("&cFailed to paste the schematic"),
    SCHEMATIC_PASTE_SUCCESS("&cThe schematic pasted successfully"),
    /*
     * Title Stuff
     */
    TITLE_ENTERED_PLOT("You entered plot %s"),
    TITLE_ENTERED_PLOT_COLOR("GOLD"),
    TITLE_ENTERED_PLOT_SUB("Owned by %s"),
    TITLE_ENTERED_PLOT_SUB_COLOR("RED"),
    TITLE_LEFT_PLOT("You entered plot %s"),
    TITLE_LEFT_PLOT_COLOR("GOLD"),
    TITLE_LEFT_PLOT_SUB("Owned by %s"),
    TITLE_LEFT_PLOT_SUB_COLOR("RED"),
    /*
     * Core Stuff
     */
    PREFIX("&c[&6&lPlot&c] "),
    ENABLED("&6PlotSquared is now enabled"),
    EXAMPLE_MESSAGE("&cThis is an example message &k!!!"),
    /*
     * Reload
     */
    RELOADED_CONFIGS("&6The translation files has been reloaded"),
    RELOAD_FAILED("&cFailed to reload the translations file"),
    /*
     * BarAPI
     */
    BOSSBAR_CLEARING("&cClearing plot: &6%id%"),
    /*
     * Alias
     */
    ALIAS_SET_TO("&cPlot alias set to &6%alias%"),
    MISSING_ALIAS("&cYou need to specify an alias"),
    ALIAS_IS_TAKEN("&cThat alias is already taken"),
    /*
     * Position
     */
    MISSING_POSITION("&cYou need to specify a position. Possible values: &6default&c, &6center"),
    POSITION_SET("&cPlot home position set"),
    INVALID_POSITION("&cThat is not a valid position value"),
    /*
     * Time
     */
    TIME_FORMAT("&6%hours%, %min%, %sec%"),
    /*
     * Permission
     */
    NO_SCHEMATIC_PERMISSION("&cYou don't have the permission required to use schematic &6%s"),
    NO_PERMISSION("&cYou are lacking the permission node: &6%s"),
    NO_PLOT_PERMS("&cYou don't have the permissions to do that in this plot"),
    CANT_CLAIM_MORE_PLOTS("&cYou can't claim more plots."),
    YOU_BE_DENIED("&cYou are not allowed to enter this plot"),

    NO_PERM_MERGE("&cYou are not the owner of the plot: &6%plot%"),
    UNLINK_REQUIRED("&cAn unlink is required to do this."),
    UNLINK_IMPOSSIBLE("&cYou can only unlink a mega-plot"),
    NO_MERGE_TO_MEGA("&cMega plots cannot be merged into. Please merge from the desired mega plot."),
    /*
     * Commands
     */
    NOT_VALID_SUBCOMMAND("&cThat is not a valid subcommand"),
    DID_YOU_MEAN("&cDid you mean: &6%s"),
    NAME_LITTLE("&c%s name is too short, &6%s&c<&6%s"),
    NO_COMMANDS("&cI'm sorry, but you're not permitted to use any subcommands."),
    SUBCOMMAND_SET_OPTIONS_HEADER("&cPossible Values: "),
    /*
     * Player not found
     */
    INVALID_PLAYER("&cPlayer not found: &6%player%."),
    /*
     *
     */
    COMMAND_WENT_WRONG("&cSomething went wrong when executing that command..."),
    /*
     * purge
     */
    PURGE_SYNTAX("&c/plots purge <world|world;x,z>"),
    PURGE_SUCCESS("All plots for the specified world have now been purged."),
    /*
     * No <plot>
     */
    NOT_IN_PLOT("&cYou're not in a plot"),
    NOT_IN_PLOT_WORLD("&cYou're not in a plot world"),
    NOT_VALID_WORLD("&cThat is not a valid world (case sensitive)"),
    NOT_VALID_PLOT_WORLD("&cThat is not a valid plot world (case sensitive)"),
    NO_PLOTS("&cYou don't have any plots"),
    /*
     * Block List
     */
    NOT_VALID_BLOCK_LIST_HEADER("&cThat's not a valid block. Valid blocks are:\\n"),
    BLOCK_LIST_ITEM(" &6%mat%&c,"),
    BLOCK_LIST_SEPARATER("&6,&c "),
    /*
     * Biome
     */
    NEED_BIOME("&cYou have got to specify a biome"),
    BIOME_SET_TO("&cPlot biome set to &c"),
    /*
     * Teleport / Entry
     */
    TELEPORTED_TO_PLOT("&6You have been teleported"),
    /*
     * Set Block
     */
    SET_BLOCK_ACTION_FINISHED("&6The last setblock action is now finished."),
    /*
     * Debug
     */
    DEUBG_HEADER("&6Debug Information\\n"),
    DEBUG_SECTION("&c>> &6&l%val%"),
    DEBUG_LINE("&c>> &6%var%&c:&6 %val%\\n"),
    /*
     * Invalid
     */
    NOT_VALID_DATA("&cThat's not a valid data id."),
    NOT_VALID_BLOCK("&cThat's not a valid block."),
    NOT_VALID_NUMBER("&cThat's not a valid number"),
    NOT_VALID_PLOT_ID("&cThat's not a valid plot id."),
    NOT_YOUR_PLOT("&cThat is not your plot."),
    NO_SUCH_PLOT("&cThere is no such plot"),
    PLAYER_HAS_NOT_BEEN_ON("&cThat player hasn't been in the plotworld"),
    FOUND_NO_PLOTS("&cFound no plots with your search query"),
    /*
     * Camera
     */
    CAMERA_STARTED("&cYou have entered camera mode for plot &6%s"),
    CAMERA_STOPPED("&cYou are no longer in camera mode"),
    /*
     * Need
     */
    NEED_PLOT_NUMBER("&cYou've got to specify a plot number or alias"),
    NEED_BLOCK("&cYou've got to specify a block"),
    NEED_PLOT_ID("&cYou've got to specify a plot id."),
    NEED_USER("&cYou need to specify a username"),
    /*
     * Info
     */
    PLOT_INFO_UNCLAIMED("&cPlot &6%s&c is not yet claimed"),
    /*PLOT_INFO("" +
            "&6ID&7: &a%id%&7\n" +
            "&6Alias&7: &a%alias%\n" +
            "&6Owner&7: &a%owner%\n" +
            "&6Helpers&7: &a%helpers%\n" +
            "&6Trusted&7: &a%trusted%\n" +
            "&6Denied&7: &a%denied%\n" +
            "&6Flags&7: &a%flags%\n" +
            "&6Biome&7: &a%biome%\n" +
            "&6Rating&7: &a%rating%&7/&a10\n" +
            "&6Can build&7: &a%build%"
    ),*/
    PLOT_INFO(
            "&cID: &6%id%&c\n" +
                    "&cAlias: &6%alias%&c\n" +
                    "&cOwner: &6%owner%&c\n" +
                    "&cBiome: &6%biome%&c\n" +
                    "&cCan Build: &6%build%&c\n" +
                    "&cRating: &6%rating%&c/&610&c\n" +
                    "&cHelpers: &6%helpers%&c\n" +
                    "&cTrusted: &6%trusted%&c\n" +
                    "&cDenied: &6%denied%&c\n" +
                    "&cFlags: &6%flags%"
    ),
    PLOT_INFO_HELPERS("&6Helpers&7: %helpers%"),
    PLOT_INFO_TRUSTED("&6Trusted&7: %trusted%"),
    PLOT_INFO_DENIED("&6Denied&7: %denied%"),
    PLOT_INFO_FLAGS("&6Flags&7: %flags%"),
    PLOT_INFO_BIOME("&6Biome&7: %biome%"),
    PLOT_INFO_RATING("&6Rating&7: %rating%"),
    PLOT_INFO_OWNER("&6Owner&7: %owner%"),
    PLOT_INFO_ID("&6ID&7: %id%"),
    PLOT_INFO_ALIAS("&6Alias&7: %alias%"),
    PLOT_INFO_SIZE("&6Size&7: %size%"),

    PLOT_USER_LIST(" &6%user%&c,"),
    INFO_SYNTAX_CONSOLE("/plot info <world> X;Y"),
    /*
     * Generating
     */
    GENERATING_FLOOR("&6Started generating floor from your settings. It will take %time%"),
    GENERATING_WALL("&6Started generating wall from your settings"),
    GENERATING_WALL_FILLING("&cStarted generating wall filling from your settings."),
    /*
     * Clearing
     */
    CLEARING_PLOT("&cClearing plot."),
    CLEARING_DONE("&6Done, took &a%time%&6 ms!"),
    CLEARING_DONE_PACKETS("&6(&a%time% &6ms for packets)"),
    /*
     * Claiming
     */
    PLOT_NOT_CLAIMED("&cPlot not claimed"),
    PLOT_IS_CLAIMED("&cThis plot is already claimed"),
    CLAIMED("&6You successfully claimed the plot"),
    /*
     * List
     */
    PLOT_LIST_HEADER_PAGED("&c(Page &6%cur&c/&6%max&c) &6List of %word% plots"),
    PLOT_LIST_HEADER("&6List of %word% plots"),
    PLOT_LIST_ITEM("&c>> &6%id&c:&6%world &c- &6%owner"),
    PLOT_LIST_ITEM_ORDERED("&c[&6%in&c] >> &6%id&c:&6%world &c- &6%owner"),
    PLOT_LIST_FOOTER("&c>> &6%word% a total of &c%num% &6claimed %plot%."),
    /*
     * Left
     */
    LEFT_PLOT("&cYou left a plot"),
    /*
     * Wait
     */
    WAIT_FOR_TIMER("&cA setblock timer is bound to either the current plot or you. Please wait for it to finish"),
    /*
     * Chat
     */
    PLOT_CHAT_FORMAT("&c[&6Plot Chat&c][&6%plot_id%&c] &6%sender%&c: &6%msg%"),
    /*
     * Denied
     */
    DENIED_REMOVED("&cYou successfully undenied the player from this plot"),
    DENIED_ADDED("&cYou successfully denied the player from this plot"),
    DENIED_NEED_ARGUMENT("&cArguments are missing. &6/plot denied add <name> &cor &6/plot helpers remove <name>"),
    WAS_NOT_DENIED("&cThat player was not denied on this plot"),
    /*
     * Rain
     */
    NEED_ON_OFF("&cYou need to specify a value. Possible values: &6on&c, &6off"),
    SETTING_UPDATED("&cYou successfully updated the setting"),
    /*
     * Flag
     */
    NEED_KEY("&cPossible values: &6%values%"),
    NOT_VALID_FLAG("&cThat is not a valid flag"),
    NOT_VALID_VALUE("&cFlag values must be alphanumerical"),
    FLAG_NOT_IN_PLOT("&cThe plot does not have that flag"),
    FLAG_NOT_REMOVED("&cThe flag could not be removed"),
    FLAG_NOT_ADDED("&cThe flag could not be added"),
    FLAG_REMOVED("&6Successfully removed flag"),
    FLAG_ADDED("&6Successfully added flag"),
    /*
     * Helper
     */
    HELPER_ADDED("&6You successfully added a helper to the plot"),
    HELPER_REMOVED("&6You successfully removed a helper from the plot"),
    HELPER_NEED_ARGUMENT("&cArguments are missing. &6/plot helpers add <name> &cor &6/plot helpers remove <name>"),
    WAS_NOT_ADDED("&cThat player was not added as a helper on this plot"),
    /*
     * Trusted
     */
    ALREADY_OWNER("&cThat user is already the plot owner."),
    ALREADY_ADDED("&cThat user is already added to that category."),
    TRUSTED_ADDED("&6You successfully added a trusted user to the plot"),
    TRUSTED_REMOVED("&6You successfully removed a trusted user from the plot"),
    TRUSTED_NEED_ARGUMENT("&cArguments are missing. &6/plot trusted add <name> &cor &6/plot trusted remove <name>"),
    T_WAS_NOT_ADDED("&cThat player was not added as a trusted user on this plot"),
    /*
     * Set Owner
     */
    SET_OWNER("&6You successfully set the plot owner"),
    /*
     * Signs
     */
    OWNER_SIGN_LINE_1("&cID: &6%id%"),
    OWNER_SIGN_LINE_2("&cOwner:"),
    OWNER_SIGN_LINE_3("&6%plr%"),
    OWNER_SIGN_LINE_4("&2Claimed"),
    /*
     * Help
     */
    HELP_HEADER("&c----------[ &6PlotSquared Help &c]----------"),
    HELP_CATEGORY("&cCategory: &6%category%&c, Page: &6%current%&c/&6%max%&c, Displaying: &6%dis%&c/&6%total%"),
    HELP_INFO("&6You have to specify a category"),
    HELP_INFO_ITEM("&6/plots help %category% &c- &6%category_desc%"),
    HELP_ITEM("&6%usage% [%alias%]\n   &c%desc%\n"),
    /*
     * Direction
     */
    DIRECTION("&6Current direction: %dir%"),
    /*
     * Custom
     */
    CUSTOM_STRING("-");

    /**
     * Special Language
     *
     * @see com.intellectualsites.translation.TranslationLanguage
     */
    protected static TranslationLanguage lang = new TranslationLanguage("PlotSquared", "this", "use");

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
     * Constructor for custom strings.
     */
    @SuppressWarnings("unused")
    C() {
        /*
         * use setCustomString();
         */
    }

    /**
     * Constructor
     *
     * @param d default
     */
    C(final String d) {
        this.d = d;
        if (this.s == null) {
            this.s = "";
        }
    }

    public static void setupTranslations() {
        if (manager == null) {
            manager = new TranslationManager();
        }

        // FIXME: generating a blank file
        // FIXME: translations aren't customizable
        // FIXME: Some messages still have the %arg stuff in them
        if (defaultFile == null) {
            defaultFile = new YamlTranslationFile(BukkitTranslation.getParent(PlotMain.getPlugin(PlotMain.class)), lang, "PlotSquared", manager)
                    .read();
        }
        // register everything in this class
        for (C c : values()) {
            manager.addTranslationObject(
                    new TranslationObject(c.toString(), c.d, "", "")
            );
        }
    }

    public static void saveTranslations() {
        manager.saveAll(defaultFile).saveFile(defaultFile);
    }

    /**
     * Get the default string
     *
     * @return default
     */
    @SuppressWarnings("unused")
    public String d() {
        return this.d;
    }

    /**
     * Get translated if exists
     *
     * @return translated if exists else default
     */
    public String s() {
        return manager.getTranslated(toString(), lang).getTranslated().replaceAll("&-", "\n").replaceAll("\\n", "\n");
        /*
        if (PlotMain.translations != null) {
            final String t = PlotMain.translations.getString(this.toString());
            if (t != null) {
                this.s = t;
            }
        }
        if (this.s.length() < 1) {
            return "";
        }
        return this.s.replace("\\n", "\n");*/
    }

    /**
     * @return translated and color decoded
     * @see org.bukkit.ChatColor#translateAlternateColorCodes(char, String)
     */
    public String translated() {
        return ChatColor.translateAlternateColorCodes('&', this.s());
    }
}
