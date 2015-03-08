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
public enum C_german {
    /*
     * Confirm
     */
    FAILED_CONFIRM("$2Es gibt keine zur Bestätigung ausstehenden Befehle!"),
    REQUIRES_CONFIRM("$2Bist du sicher, dass du diesen Befehl ausführen willst: $1%s$2?\n$2Die Änderung ist unwiderruflich! Wenn du sicher bist: $1/plot confirm"),
    /*
     * Move
     */
    MOVE_SUCCESS("$4Plot erfolgreich verschoben."),
    REQUIRES_UNOWNED("$2Der angegebene Ort ist bereits belegt."),
    /*
     * Compass
     */
    COMPASS_TARGET("$4Plot erfolgreich mit dem Kompass anvisiert."),
    /*
     * Cluster
     */
    CLUSTER_AVAILABLE_ARGS("$1Die folgenden Parameter sind verfügbar: $4list$2, $4create$2, $4delete$2, $4resize$2, $4invite$2, $4kick$2, $4leave$2, $4helpers$2, $4info$2, $4tp$2, $4sethome"),
    CLUSTER_LIST_HEADING("$2Es gibt $1%s$2 Cluster in dieser Welt."),
    CLUSTER_LIST_ELEMENT("$2 - $1%s\n"),
    CLUSTER_INTERSECTION("$2Der vorgeschlagene Bereich überlappt mit $1%s$2 existierendem/n Cluster/n"),
    CLUSTER_ADDED("$4Cluster erfolgreich erstellt."),
    CLUSTER_DELETED("$4Cluster erfolgreich gelöscht."),
    CLUSTER_RESIZED("$4Größe des Clusters wurde erfolgreich geändert."),
    CLUSTER_ADDED_USER("$4Spieler erfolgreich zum Cluster hinzugefügt."),
    CANNOT_KICK_PLAYER("$2Du kannst diesen Spieler nicht kicken."),
    CLUSTER_INVITED("$1Du wurdest in folgenden Cluster eingeladen: $2%s"),
    CLUSTER_REMOVED("$1Du wurdest aus folgendem Cluster enfernt: $2%s"),
    CLUSTER_KICKED_USER("$4Spieler erfolgreich gekickt."),
    INVALID_CLUSTER("$1Clustername ungültig: $2%s"),
    CLUSTER_NOT_ADDED("$2Dieser Spieler war nicht zum Cluster hinzugefügt."),
    CLUSTER_CANNOT_LEAVE("$1Du musst deinen Besitz löschen oder transferieren bevor du gehen kannst."),
    CLUSTER_ADDED_HELPER("$4Helfer erfolgreich hinzugefügt."),
    CLUSTER_REMOVED_HELPER("$4Helfer erfolgreich vom Cluster entfernt."),
    CLUSTER_REGENERATED("$4Clusterregeneration erfolgreich gestartet"),
    CLUSTER_TELEPORTING("$4Teleportiere..."),
    CLUSTER_INFO("$1Aktueller Cluster: $2%id%\n$1Name: $2%name%\n$1Besitzer: $2%owner%\n$1Größe: $2%size%\n$1Rechte: $2%rights%"),
    CLUSTER_CURRENT_PLOTID("$1Aktueller Plot: $2%s"),
    /*
     * Border
     */
    BORDER("$2Du befindest dich ausserhalb der aktuellen Weltengrenze"),
    /*
     * Unclaim
     */
    UNCLAIM_SUCCESS("$4Dieser Plot gehört dir jetzt nicht mehr."),
    /*
     * WorldEdit masks
     */
    REQUIRE_SELECTION_IN_MASK("$2%s deiner Selektion befindet sich nicht innerhalb deines Plots. Du kannst Änderungen nur innerhalb deines Plots vornehmen."),
    /*
     * Records
     */
    RECORD_PLAY("$2%player $2startete Spielaufzeichnung $1%name"),
    NOTIFY_ENTER("$2%player $2betritt deinen Plot ($1%plot$2)"),
    NOTIFY_LEAVE("$2%player $2verlies deinen Plot ($1%plot$2)"),
    /*
     * Swap
     */
    SWAP_SYNTAX("$2/plots swap <plot id>"),
    SWAP_SUCCESS("$4Plots erfolgreich getauscht"),
    /*
     * Comment
     */
    COMMENT_SYNTAX("$2Syntax: /plots comment <everyone|trusted|helper|owner|admin> <comment>"),
    INVALID_INBOX("$2Dieses Postfach ist ungültig.\n$1Akzeptierte Werte: %s"),
    COMMENT_REMOVED("$4Erfolgreich gelöscht: %s."),
    COMMENT_ADDED("$4Ein Kommentar wurde hinterlassen."),
    /*
     * Console
     */
    NOT_CONSOLE("$2Aus Sicherheitsgründen kann dieser Befehl nur von der Konsole ausgeführt werden."),
    IS_CONSOLE("$2Dieser Befehl kann nur von einem Spieler ausgeführt werden."),
    /*
     * Clipboard
     */
    CLIPBOARD_SET("$2Der aktuelle Plot wird in die Zwischenablage kopiert. Benutze $1/plot paste$2 um ihn einzufügen."),
    PASTED("$4Die Plotauswahl wurde erfolgreich eingefügt. Die Zwischenablage wurde geleert."),
    PASTE_FAILED("$2Einfügen fehlgeschlagen: $2%s"),
    NO_CLIPBOARD("$2Deine Zwischenablage ist leer."),
    CLIPBOARD_INFO("$2Aktuelle Auswahl - Plot ID: $1%id$2, Breite: $1%width$2, Anzahl Blöcke: $1%total$2"),
    /*
     * Ratings
     */
    RATING_NOT_VALID("$2Wähle eine Zahl zwischen 1 und 10"),
    RATING_ALREADY_EXISTS("$2Du hast diesen Plot bereits bewertet: $2%s"),
    RATING_APPLIED("$4Du hast diesen Plot erfolgreich bewertet: $2%s"),
    RATING_NOT_YOUR_OWN("$2Du kannst deinen eigenen Plot nicht selbst bewerten."),
    RATING_NOT_OWNED("$2Plots ohne Besitzer können nicht bewertet werden."),
    /*
     * Economy Stuff
     */
    ECON_DISABLED("$2Ökonomie ist nicht aktiviert."),
    CANNOT_AFFORD_PLOT("$2Du kannst dir diesen Plot nicht leisten. Er kostet $1%s"),
    NOT_FOR_SALE("$2Dieser Plot steht nicht zum Verkauf."),
    CANNOT_BUY_OWN("$2Du kannst deinen eigenen Plot nicht kaufen."),
    PLOT_SOLD("$4Dein Plot $1%s$4, wurde an $1%s$4 für $1$%s$4 verkauft."),
    CANNOT_AFFORD_MERGE("$2Du kannst dir das Zusammenfügen der Plots nicht leisten. Es kostet $1%s"),
    ADDED_BALANCE("$1%s $2wurden deinem Guthaben hinzugefügt."),
    REMOVED_BALANCE("$1%s $2wurden von deinem Guthaben abgezogen."),
    /*
     * Setup Stuff
     */
    SETUP_INIT("$1Verwendung: $2/plot setup <value>"),
    SETUP_STEP("$3[$1Schritt %s$3] $1%s $2- $1Erwarte: $2%s $1Standard: $2%s"),
    SETUP_INVALID_ARG("$2%s ist kein gültiger Wer für Schritt %s. Um Setup abzubrechen verwende: $1/plot setup cancel"),
    SETUP_VALID_ARG("$2Wert $1%s $2gesetzt auf %s"),
    SETUP_FINISHED("$3Falls du MULTIVERSE oder MULTIWORLD verwendest sollte die Welt generiert worden sein. Andernfalls musst du die Welt manuell über bukkit.yml hinzufügen."),
    SETUP_WORLD_TAKEN("$2%s ist bereits eine bekannte Plotwelt"),
    SETUP_MISSING_WORLD("$2Du musst einen Namen für die Welt vergeben ($1/plot setup &l<world>$1 <generator>$2)\n$1Zusätzliche Befehle:\n$2 - $1/plot setup <value>\n$2 - $1/plot setup back\n$2 - $1/plot setup cancel"),
    SETUP_MISSING_GENERATOR("$2Du musst einen Generator angeben ($1/plot setup <world> &l<generator>&r$2)\n$1Zusätzliche Befehle:\n$2 - $1/plot setup <value>\n$2 - $1/plot setup back\n$2 - $1/plot setup cancel"),
    SETUP_INVALID_GENERATOR("$2Ungültiger Generarator. Mögliche Optionen: %s"),
    /*
     * Schematic Stuff
     */
    SCHEMATIC_MISSING_ARG("$2Du musst einen Wert angeben. Gültige Werte: $1test <name>$2 , $1save$2 , $1paste $2, $1exportall"),
    SCHEMATIC_INVALID("$2Diese Schematic ist ungültig: $2%s"),
    SCHEMATIC_VALID("$2Diese Schematic ist gültig."),
    SCHEMATIC_PASTE_FAILED("$2Einfügen der Schematic fehlgeschlagen"),
    SCHEMATIC_PASTE_SUCCESS("$4Einfügen der Schematic erfolgreich."),
    /*
     * Title Stuff
     */
    TITLE_ENTERED_PLOT("Du betrittst Plot %world%;%x%;%z%"),
    TITLE_ENTERED_PLOT_COLOR("GOLD"),
    TITLE_ENTERED_PLOT_SUB("Besitzer: %s"),
    TITLE_ENTERED_PLOT_SUB_COLOR("RED"),
    TITLE_LEFT_PLOT("Du verlässt Plot %s"),
    TITLE_LEFT_PLOT_COLOR("GOLD"),
    TITLE_LEFT_PLOT_SUB("Besitzer: %s"),
    TITLE_LEFT_PLOT_SUB_COLOR("RED"),
    PREFIX_GREETING("$1%id%$2> "),
    PREFIX_FAREWELL("$1%id%$2> "),
    /*
     * Core Stuff
     */
    PREFIX("$3[$1P\u00B2$3] "),
    ENABLED("$1PlotSquared wurde aktiviert"),
    EXAMPLE_MESSAGE("$2Das ist eine Beispielnachricht &k!!!"),
    /*
     * Reload
     */
    RELOADED_CONFIGS("$1Übersetzungen und Welteneinstellungen wurden neu geladen"),
    RELOAD_FAILED("$2Erneutes Laden der Konfiguration fehlgeschlagen"),
    /*
     * BarAPI
     */
    BOSSBAR_CLEARING("$2Leere Plot: $1%id%"),
    /*
     * Alias
     */
    ALIAS_SET_TO("$2Plot Alias geseztt auf $1%alias%"),
    MISSING_ALIAS("$2Du musst ein Alias angeben"),
    ALIAS_TOO_LONG("$2Der Alias darf nicht länger als 50 Zeichen sein"),
    ALIAS_IS_TAKEN("$2Dieser Alias wird bereits verwendet"),
    /*
     * Position
     */
    MISSING_POSITION("$2Du musst eine Position angeben. Mögliche Werte: $1none"),
    POSITION_SET("$1Die Position wurde auf deinen aktuellen Standort gesetzt"),
    HOME_ARGUMENT("$2Verwende /plot set home [none]"),
    INVALID_POSITION("$2That is not a valid position value"),
    /*
     * Time
     */
    TIME_FORMAT("$1%hours%, %min%, %sec%"),
    /*
     * Permission
     */
    NO_SCHEMATIC_PERMISSION("$2Du hast keine Berechtigung Schmatics zu verwenden: $1%s"),
    NO_PERMISSION("$2Dir fehlt folgende Berechtigung: $1%s"),
    NO_PLOT_PERMS("$2Diese Aktion kann nur der Besitzer des Plots"),
    CANT_CLAIM_MORE_PLOTS("$2Du kannst keine weiteren Plots besitzen."),
    CANT_CLAIM_MORE_PLOTS_NUM("$2Du kannst nicht mehr als $1%s $2Plots auf einmal einnehmen."),
    YOU_BE_DENIED("$2Es ist dir nicht gestattet diesen Plot zu betreten."),
    NO_PERM_MERGE("$2Du bist nicht Besitzer des Plots $1%plot%"),
    UNLINK_REQUIRED("$2Die Plots müssen vorher getrennt (unlink) werden."),
    UNLINK_IMPOSSIBLE("$2Die Trennung (unlink) funktioniert nur auf Megaplots."),
    UNLINK_SUCCESS("$2Trennung erfolgreich."),
    NO_MERGE_TO_MEGA("$2Füge einzelne Plots deinem Megaplot hinzu, nicht anders herum."),
    /*
     * Commands
     */
    NOT_VALID_SUBCOMMAND("$2Das ist kein gültiger Parameter."),
    DID_YOU_MEAN("$2Meinst du: $1%s"),
    NAME_LITTLE("$2%s Name ist zu kurz, $1%s$2<$1%s"),
    NO_COMMANDS("$2Du hast für keinen Befehl eine Berechtigung."),
    SUBCOMMAND_SET_OPTIONS_HEADER("$2Mögliche Werte: "),
    COMMAND_SYNTAX("$1Verwendung: $2%s"),
    /*
     * Player not found
     */
    INVALID_PLAYER("$2Spieler nicht gefunden: $1%s."),
    /*
     *
     */
    COMMAND_WENT_WRONG("$2Beim ausführen des Befehls ging etwas schief..."),
    /*
     * purge
     */
    PURGE_SYNTAX("Verwende /plot purge <x;z|player|unowned|unknown|all> <world>"),
    PURGE_SUCCESS("$4%s Plots erfolgreich gelöscht."),
    /*
     * trim
     */
    TRIM_SYNTAX("Verwende /plot trim <all|x;y> <world>"),
    TRIM_START("Starte Weltenbeschneidung..."),
    TRIM_IN_PROGRESS("Die Weltenbeschneidung ist bereits gestartet!"),
    NOT_VALID_HYBRID_PLOT_WORLD("Hybrid Plot Manager wird für diese Aktion benötigt."),
    /*
     * No <plot>
     */
    NO_FREE_PLOTS("$2Es sind keine freien Plots verfügbar."),
    NOT_IN_PLOT("$2Du befindest dich nicht auf einem Plot."),
    NOT_IN_CLUSTER("$2Du musst dich innerhalb eines Plot Clusters befinden um das zu tun."),
    NOT_IN_PLOT_WORLD("$2Du befindest dich nicht in einer Plotwelt."),
    NOT_VALID_WORLD("$2Das ist keine gültige Welt (Groß- / Kleinschreibung beachten)"),
    NOT_VALID_PLOT_WORLD("$2Das ist keine gültige Plotwelt (Groß- / Kleinschreibung beachten)"),
    NO_PLOTS("$2Du hast keine Plots"),
    /*
     * Block List
     */
    NOT_VALID_BLOCK_LIST_HEADER("$2Das ist kein gültiger Block. Gültige Blöcke sind:\\n"),
    BLOCK_LIST_ITEM(" $1%mat%$2,"),
    BLOCK_LIST_SEPARATER("$1,$2 "),
    /*
     * Biome
     */
    NEED_BIOME("$2Du musst ein Biom angeben."),
    BIOME_SET_TO("$2Plot biome gesetzt: $2"),
    /*
     * Teleport / Entry
     */
    TELEPORTED_TO_PLOT("$1Du wurdest teleportiert."),
    TELEPORTED_TO_ROAD("$2Du wurdest auf die Straße teleportiert."),
    TELEPORT_IN_SECONDS("$1Teleportiere in %s Sekunden. Bewege dich nicht..."),
    TELEPORT_FAILED("$2Teleportation wurde wegen einer Bewegen or Schaden abgebrochen."),
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
    NOT_VALID_DATA("$2Das ist kein gültiger Datenwert."),
    NOT_VALID_BLOCK("$2Das ist kein gültiger Block."),
    NOT_VALID_NUMBER("$2Das ist keine gültige Zahl."),
    NOT_VALID_PLOT_ID("$2Das ist keine gültige Plot-ID."),
    PLOT_ID_FORM("$2Die Plot-ID muss wie folgt angegeben werden: $1X;Y $2z.B. $1-5;7"),
    NOT_YOUR_PLOT("$2Das ist nicht dein Plot."),
    NO_SUCH_PLOT("$2Ein solcher Plot existiert nicht."),
    PLAYER_HAS_NOT_BEEN_ON("$2Dieser Spieler war nicht in der Plotwelt."),
    FOUND_NO_PLOTS("$2Dein Suchmuster ergab keine Treffer."),
    /*
     * Camera
     */
    CAMERA_STARTED("$2Du verwendest den Kameramodus für Plot $1%s"),
    CAMERA_STOPPED("$2Der Kameramodus wurde abeschaltet."),
    /*
     * Need
     */
    NEED_PLOT_NUMBER("$2Du musst eine Plotnummer oder einen Alias angeben."),
    NEED_BLOCK("$2Du musst einen Block angeben."),
    NEED_PLOT_ID("$2Du musst eine Plot-ID angeben."),
    NEED_PLOT_WORLD("$2Du musst eine Plotwelt angeben."),
    NEED_USER("$2Du musst einen Spielernamen angeben."),
    /*
     * Info
     */
    PLOT_UNOWNED("$2Für diese Aktion muss dieser Plot einen Besitzer aufweisen."),
    PLOT_INFO_UNCLAIMED("$2Plot $1%s$2 hat bisher keinen Besitzer."),
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
    PLOT_INFO("$1ID: $2%id%$1\n" + "$1Alias: $2%alias%$1\n" + "$1Besitzer: $2%owner%$1\n" + "$1Biom: $2%biome%$1\n" + "$1Baurechte: $2%build%$1\n" + "$1Wertung: $2%rating%$1/$210$1\n" + "$1Helfer: $2%helpers%$1\n" + "$1Vertraut: $2%trusted%$1\n" + "$1Verboten: $2%denied%$1\n" + "$1Flags: $2%flags%"),
    PLOT_INFO_HELPERS("$1Helfer:$2 %helpers%"),
    PLOT_INFO_TRUSTED("$1Vertraute:$2 %trusted%"),
    PLOT_INFO_DENIED("$1Verboten:$2 %denied%"),
    PLOT_INFO_FLAGS("$1Flags:$2 %flags%"),
    PLOT_INFO_BIOME("$1Biom:$2 %biome%"),
    PLOT_INFO_RATING("$1Wertung:$2 %rating%"),
    PLOT_INFO_OWNER("$1Besitzer:$2 %owner%"),
    PLOT_INFO_ID("$1ID:$2 %id%"),
    PLOT_INFO_ALIAS("$1Alias:$2 %alias%"),
    PLOT_INFO_SIZE("$1Größe:$2 %size%"),
    PLOT_USER_LIST(" $1%user%$2,"),
    INFO_SYNTAX_CONSOLE("$2/plot info <world> X;Y"),
    /*
     * Generating
     */
    GENERATING_COMPONENT("$1Die Generierung wurde gemäß deiner Einstellungen gestartet."),
    /*
     * Clearing
     */
    CLEARING_PLOT("$2Plot wird asyncron geleert."),
    CLEARING_DONE("$4Erfolgreich geleert. Dauerte %sms."),
    /*
     * Claiming
     */
    PLOT_NOT_CLAIMED("$2Dieser Plot hat keinen Besitzer"),
    PLOT_IS_CLAIMED("$2Dieser Plot hat bereits einen Besitzer."),
    CLAIMED("$4Plot erfolgreich in Besitz genommen."),
    /*
     * List
     */
    PLOT_LIST_HEADER_PAGED("$2(Seite $1%von$2/$1%max$2) $1Liste Plots nach %word%"),
    PLOT_LIST_HEADER("$1Liste aller %word% Plots"),
    PLOT_LIST_ITEM("$2>> $1%id$2:$1%Welt $2- $1%owner"),
    PLOT_LIST_ITEM_ORDERED("$2[$1%in$2] >> $1%id$2:$1%Welt $2- $1%owner"),
    PLOT_LIST_FOOTER("$2>> $1%word% umfasst insgesamt $2%num% $1Plots %plot%."),
    /*
     * Left
     */
    LEFT_PLOT("$2Du hast einen Plot verlassen."),
    /*
     * PlotMe
     */
    NOT_USING_PLOTME("$2Dieser Server verwendet das $1PlotSquared $2Plotmanagement System. Verwende stattdessen $1/plots"),
    /*
     * Wait
     */
    WAIT_FOR_TIMER("$2Ein zeitgeber wurde an den Plot oder an dich gebunden. Bitte warte bis die Zeit abgelaufen."),
    /*
     * Chat
     */
    PLOT_CHAT_FORMAT("$2[$1Plot Chat$2][$1%plot_id%$2] $1%sender%$2: $1%msg%"),
    /*
     * Denied
     */
    DENIED_REMOVED("$4Der Spieler darf diesen Plot wieder betreten."),
    DENIED_ADDED("$4Der Spieler darf diesen Plot nicht mehr betreten."),
    DENIED_NEED_ARGUMENT("$2Argumente fehlen. $1/plot denied add <name> $2oder $1/plot helpers remove <name>"),
    WAS_NOT_DENIED("$2Der Spieler durfte diesen Plot bereits betreten."),
    /*
     * Rain
     */
    NEED_ON_OFF("$2Du musst einen Wert angeben. Mögliche Werte: $1on$2, $1off"),
    SETTING_UPDATED("$4Einstellungen erfolgreich aktualisiert."),
    /*
     * Flag
     */
    FLAG_KEY("$2Schlüssel: %s"),
    FLAG_TYPE("$2Typ: %s"),
    FLAG_DESC("$2Beschreibung: %s"),
    NEED_KEY("$2Mögliche Werte: $1%values%"),
    NOT_VALID_FLAG("$2Ungültige Flag"),
    NOT_VALID_VALUE("$2Wert der Flag muss alphanumerisch angegeben werden."),
    FLAG_NOT_IN_PLOT("$2Diese Flag wurde in diesem Plot nicht gesetzt."),
    FLAG_NOT_REMOVED("$2Die Flag konnte nicht entfernt werden."),
    FLAG_NOT_ADDED("$2Flag konnte nicht gesetzt werden."),
    FLAG_REMOVED("$4Flag erfolgreich entfernt."),
    FLAG_ADDED("$4Successfully added flag"),
    /*
     * Helper
     */
    HELPER_ADDED("$4Helfer erfolgreich zu diesem Plot hinzugefügt."),
    HELPER_REMOVED("$4Helfer erfolgreich von diesem Plot enfernt."),
    HELPER_NEED_ARGUMENT("$2Argumente fehlen. $1/plot helpers add <name> $2oder $1/plot helpers remove <name>"),
    WAS_NOT_ADDED("$2Dieser Spieler war bisher kein Helfer auf diesem Plot."),
    PLOT_REMOVED_HELPER("$1Plot %s auf dem du Helfer warst wurde wegen Inaktivität des Besitzers gelöscht."),
    /*
     * Trusted
     */
    ALREADY_OWNER("$2Dieser Spieler ist bereits Besitzer des Plots."),
    ALREADY_ADDED("$2Dieser Spieler ist bereits in dieser Kategorie."),
    TRUSTED_ADDED("$4Spieler erfolgreich in diesem Plot vertraut."),
    TRUSTED_REMOVED("$1Diesem Spieler wird auf diesem Plot nicht mehr vertraut."),
    TRUSTED_NEED_ARGUMENT("$2Argumente fehlen. $1/plot trusted add <name> $2oder $1/plot trusted remove <name>"),
    T_WAS_NOT_ADDED("$2Diesem Spieler wurde bisher nicht vertraut."),
    /*
     * Set Owner
     */
    SET_OWNER("$4lotbesitzer erfolgreich gesetzt."),
    /*
     * Signs
     */
    OWNER_SIGN_LINE_1("$1ID: $1%id%"),
    OWNER_SIGN_LINE_2("$1Besitzer:"),
    OWNER_SIGN_LINE_3("$2%plr%"),
    OWNER_SIGN_LINE_4("$3in Besitz"),
    /*
     * Help
     */
    HELP_HEADER("$3====== $1Plot\u00B2 Hilfe $3======"),
    HELP_CATEGORY("$1Kategorie: $2%category%$2,$1 Seite: $2%currentt%$3/$2%max%$2,$1 Zeige: $2%dis%$3/$2%total%"),
    HELP_INFO("$3====== $1Wähle eine Kategorie $3======", false),
    HELP_INFO_ITEM("$1/plots help %category% $3- $2%category_desc%"),
    HELP_ITEM("$1%usage% [%alias%]\n $3- $2%desc%\n"),
    /*
     * Direction
     */
    DIRECTION("$1Aktuelle Himmelsrichtung: %dir%"),
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
    C_german() {
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
    C_german(final String d, final boolean prefix) {
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
    C_german(final String d) {
        this(d, true);
    }

    public static void setupTranslations() {
        manager = new TranslationManager();
        defaultFile = new YamlTranslationFile(BukkitTranslation.getParent(), lang, "PlotSquared", manager).read();
        // register everything in this class
        for (final C_german c : values()) {
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
