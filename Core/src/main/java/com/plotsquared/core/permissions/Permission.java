/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.permissions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

/**
 * Permission class.
 */
public enum Permission implements ComponentLike {

    //@formatter:off
    //<editor-fold desc="Static Permission">
    PERMISSION_STAR("*"),
    PERMISSION_ADMIN("plots.admin"),
    PERMISSION_ADMIN_AREA_SUDO("plots.admin.area.sudo"),
    PERMISSION_ADMIN_INTERACT_BLOCKED_CMDS("plots.admin.interact.blockedcommands"),
    PERMISSION_WORLDEDIT_BYPASS("plots.worldedit.bypass"),
    PERMISSION_PLOT_TOGGLE_TITLES("plots.toggle.titles"),
    PERMISSION_PLOT_TOGGLE_CHAT("plots.toggle.chat"),
    PERMISSION_PLOT_TOGGLE_TIME("plots.toggle.time"),
    PERMISSION_ADMIN_UPDATE_NOTIFICATION("plots.admin.update.notify"),
    PERMISSION_ADMIN_EXIT_DENIED("plots.admin.exit.denied"),
    PERMISSION_ADMIN_ENTRY_DENIED("plots.admin.entry.denied"),
    PERMISSION_ADMIN_VISIT_UNTRUSTED("plots.admin.visit.untrusted"),
    PERMISSION_ADMIN_ENTRY_FORCEFIELD("plots.admin.entry.forcefield"),
    PERMISSION_ADMIN_COMMANDS_CHATSPY("plots.admin.command.chatspy"),
    PERMISSION_MERGE("plots.merge"),
    PERMISSION_MERGE_ALL("plots.merge.all"),
    PERMISSION_MERGE_OTHER("plots.merge.other"),
    PERMISSION_MERGE_KEEP_ROAD("plots.merge.keeproad"),
    PERMISSION_ADMIN_CAPS_OTHER("plots.admin.caps.other"),
    PERMISSION_ADMIN_MUSIC_OTHER("plots.admin.music.other"),
    PERMISSION_ADMIN_DESTROY("plots.admin.destroy"),
    PERMISSION_ADMIN_DESTROY_UNOWNED("plots.admin.destroy.unowned"),
    PERMISSION_ADMIN_DESTROY_GROUNDLEVEL("plots.admin.destroy.groundlevel"),
    PERMISSION_ADMIN_DESTROY_OTHER("plots.admin.destroy.other"),
    PERMISSION_ADMIN_DESTROY_ROAD("plots.admin.destroy.road"),
    PERMISSION_ADMIN_DESTROY_VEHICLE_ROAD("plots.admin.vehicle.break.road"),
    PERMISSION_ADMIN_DESTROY_VEHICLE_UNOWNED("plots.admin.vehicle.break.unowned"),
    PERMISSION_ADMIN_DESTROY_VEHICLE_OTHER("plots.admin.vehicle.break.other"),
    PERMISSION_ADMIN_PVE("plots.admin.pve"),
    PERMISSION_ADMIN_PLACE_VEHICLE_ROAD("plots.admin.vehicle.place.road"),
    PERMISSION_ADMIN_PLACE_VEHICLE_UNOWNED("plots.admin.vehicle.place.unowned"),
    PERMISSION_ADMIN_PLACE_VEHICLE_OTHER("plots.admin.vehicle.place.other"),
    PERMISSION_ADMIN_PVP("plots.admin.pvp"),
    PERMISSION_ADMIN_BUILD_ROAD("plots.admin.build.road"),
    PERMISSION_ADMIN_PROJECTILE_ROAD("plots.admin.projectile.road"),
    PERMISSION_ADMIN_PROJECTILE_UNOWNED("plots.admin.projectile.unowned"),
    PERMISSION_ADMIN_PROJECTILE_OTHER("plots.admin.projectile.other"),
    PERMISSION_ADMIN_BUILD_UNOWNED("plots.admin.build.unowned"),
    PERMISSION_ADMIN_BUILD_OTHER("plots.admin.build.other"),
    PERMISSION_ADMIN_INTERACT_ROAD("plots.admin.interact.road"),
    PERMISSION_ADMIN_INTERACT_UNOWNED("plots.admin.interact.unowned"),
    PERMISSION_ADMIN_INTERACT_OTHER("plots.admin.interact.other"),
    PERMISSION_ADMIN_BUILD_HEIGHT_LIMIT("plots.admin.build.heightlimit"),
    PERMISSION_ADMIN_COMMAND_PURGE_RATINGS("plots.admin.command.purge.ratings"),
    PERMISSION_ADMIN_COMMAND_ADD("plots.admin.command.trust"),
    PERMISSION_ADMIN_COMMAND_TRUST("plots.admin.command.trust"),
    PERMISSION_TRUST("plots.trust"),
    PERMISSION_DENY("plots.deny"),
    PERMISSION_ADD("plots.add"),
    PERMISSION_TRUST_EVERYONE("plots.trust.everyone"),
    PERMISSION_AREA_CREATE("plots.area.create"),
    PERMISSION_AREA_INFO("plots.area.info"),
    PERMISSION_AREA_INFO_FORCE("plots.admin.info.force"),
    PERMISSION_AREA_LIST("plots.area.list"),
    PERMISSION_AREA_REGEN("plots.area.regen"),
    PERMISSION_AREA_TP("plots.area.tp"),
    PERMISSION_AREA("plots.area"),
    PERMISSION_AUTO_MEGA("plots.auto.mega"),
    PERMISSION_CLAIM_SCHEMATIC("plots.claim.%s"),
    PERMISSION_ADMIN_COMMAND_SCHEMATIC("plots.admin.command.schematic"),
    PERMISSION_ADMIN_COMMAND_CLEAR("plots.admin.command.clear"),
    PERMISSION_CONTINUE("plots.continue"),
    PERMISSION_CLUSTER("plots.cluster"),
    PERMISSION_CLUSTER_LIST("plots.cluster.list"),
    PERMISSION_CLUSTER_CREATE("plots.cluster.create"),
    PERMISSION_CLUSTER_CREATE_OTHER("plots.cluster.create.other"),
    PERMISSION_CLUSTER_SIZE("plots.cluster.size"),
    PERMISSION_CLUSTER_DELETE("plots.cluster.delete"),
    PERMISSION_CLUSTER_DELETE_OTHER("plots.cluster.delete.other"),
    PERMISSION_CLUSTER_RESIZE("plots.cluster.resize"),
    PERMISSION_CLUSTER_RESIZE_OTHER("plots.cluster.resize.other"),
    PERMISSION_CLUSTER_RESIZE_SHRINK("plots.cluster.resize.shrink"),
    PERMISSION_CLUSTER_RESIZE_EXPAND("plots.cluster.resize.expand"),
    PERMISSION_CLUSTER_INVITE("plots.cluster.invite"),
    PERMISSION_CLUSTER_INVITE_OTHER("plots.cluster.invite.other"),
    PERMISSION_CLUSTER_KICK("plots.cluster.kick"),
    PERMISSION_CLUSTER_KICK_OTHER("plots.cluster.kick.other"),
    PERMISSION_CLUSTER_LEAVE("plots.cluster.leave"),
    PERMISSION_CLUSTER_HELPERS("plots.cluster.helpers"),
    PERMISSION_CLUSTER_TP("plots.cluster.tp"),
    PERMISSION_CLUSTER_TP_OTHER("plots.cluster.tp.other"),
    PERMISSION_CLUSTER_INFO("plots.cluster.info"),
    PERMISSION_CLUSTER_SETHOME("plots.cluster.sethome"),
    PERMISSION_CLUSTER_SETHOME_OTHER("plots.cluster.sethome.other"),
    PERMISSION_ADMIN_COMMAND_CONTINUE("plots.admin.command.continue"),
    PERMISSION_ADMIN_COMMAND_DELETE("plots.admin.command.delete"),
    PERMISSION_ADMIN_COMMAND_DENY("plots.admin.command.deny"),
    PERMISSION_DENY_EVERYONE("plots.deny.everyone"),
    PERMISSION_ADMIN_COMMAND_DONE("plots.admin.command.done"),
    PERMISSION_ADMIN_COMMAND_DOWNLOAD("plots.admin.command.download"),
    PERMISSION_DOWNLOAD("plots.download"),
    PERMISSION_DOWNLOAD_WORLD("plots.download.world"),
    PERMISSION_SET_FLAG_OTHER("plots.set.flag.other"),
    PERMISSION_SET_FLAG("plots.set.flag"),
    PERMISSION_SET_FLAG_KEY("plots.set.flag.%s"),
    PERMISSION_SET_FLAG_KEY_VALUE("plots.set.flag.%s.%s"),
    PERMISSION_SET("plots.set"),
    PERMISSION_SET_BIOME("plots.set.biome"),
    PERMISSION_SET_ALIAS("plots.set.alias"),
    PERMISSION_SET_HOME("plots.set.home"),
    PERMISSION_SET_MAIN("plots.set.main"),
    PERMISSION_SET_FLOOR("plots.set.floor"),
    PERMISSION_SET_AIR("plots.set.air"),
    PERMISSION_SET_ALL("plots.set.all"),
    PERMISSION_SET_BORDER("plots.set.border"),
    PERMISSION_SET_WALL("plots.set.wall"),
    PERMISSION_SET_OUTLINE("plots.set.outline"),
    PERMISSION_SET_MIDDLE("plots.set.middle"),
    PERMISSION_TARGET("plots.target"),
    PERMISSION_TEMPLATE("plots.template"),
    PERMISSION_TEMPLATE_IMPORT("plots.template.import"),
    PERMISSION_TEMPLATE_EXPORT("plots.template.import"),
    PERMISSION_FLAG_REMOVE("plots.flag.remove"),
    PERMISSION_FLAG_ADD("plots.flag.add"),
    PERMISSION_FLAG_LIST("plots.flag.list"),
    PERMISSION_ADMIN_COMMAND_KICK("plots.admin.command.kick"),
    PERMISSION_GRANT_SINGLE("plots.grant"),
    PERMISSION_GRANT("plots.grant.%s"),
    PERMISSION_GRANT_ADD("plots.grant.add"),
    PERMISSION_GRANT_CHECK("plots.grant.check"),
    PERMISSION_LIST_FOR_SALE("plots.list.forsale"),
    PERMISSION_LIST_MINE("plots.list.mine"),
    PERMISSION_LIST_SHARED("plots.list.shared"),
    PERMISSION_LIST_WORLD("plots.list.world"),
    PERMISSION_LIST_WORLD_NAME("plots.list.world.%s"),
    PERMISSION_LIST_TOP("plots.list.top"),
    PERMISSION_LIST_ALL("plots.list.all"),
    PERMISSION_LIST_UNOWNED("plots.list.unowned"),
    PERMISSION_LIST_PLAYER("plots.list.player"),
    PERMISSION_LIST_DONE("plots.list.done"),
    PERMISSION_LIST_EXPIRED("plots.list.expired"),
    PERMISSION_LIST_FUZZY("plots.list.fuzzy"),
    PERMISSION_LIST_AREA("plots.list.area"),
    PERMISSION_ADMIN_COMMAND_LOAD("plots.admin.command.load"),
    PERMISSION_ADMIN_COMMAND_MERGE("plots.admin.command.merge"),
    PERMISSION_ADMIN_COMMAND_MERGE_OTHER_OFFLINE("plots.admin.command.merge.other.offline"),
    PERMISSION_ADMIN_COMMAND_SET_OWNER("plots.admin.command.setowner"),
    PERMISSION_COMMENT("plots.comment"),
    PERMISSION_INBOX("plots.inbox"),
    PERMISSION_INBOX_READ_OWNER("plots.inbox.read.owner"),
    PERMISSION_INBOX_READ_PUBLIC("plots.inbox.read.public"),
    PERMISSION_INBOX_READ_REPORT("plots.inbox.read.report"),
    PERMISSION_ADMIN_COMMAND_REMOVE("plots.admin.command.remove"),
    PERMISSION_ADMIN_COMMAND_SAVE("plots.admin.command.save"),
    PERMISSION_ADMIN_COMMAND_SCHEMATIC_PASTE("plots.admin.command.schematic.paste"),
    PERMISSION_SCHEMATIC("plots.schematic.paste"),
    PERMISSION_SCHEMATIC_PASTE("plots.schematic.paste"),
    PERMISSION_SCHEMATIC_LIST("plots.schematic.list"),
    PERMISSION_SCHEMATIC_SAVE("plots.schematic.save"),
    PERMISSION_ADMIN_COMMAND_SCHEMATIC_SAVE("plots.admin.command.schematic.save"),
    PERMISSION_SET_COMPONENT("plots.set.%s"),
    PERMISSION_ADMIN_COMMAND("plots.admin.command.%s"),
    PERMISSION_ADMIN_COMMAND_UNLINK("plots.admin.command.unlink"),
    PERMISSION_VISIT_UNOWNED("plots.visit.unowned"),
    PERMISSION_VISIT_OWNED("plots.visit.owned"),
    PERMISSION_SHARED("plots.visit.shared"),
    PERMISSION_VISIT_DENIED("plots.visit.denied"),
    PERMISSION_VISIT_OTHER("plots.visit.other"),
    PERMISSION_HOME("plots.home"),
    PERMISSION_ALIAS_SET("plots.alias.set"),
    PERMISSION_ADMIN_ALIAS_SET("plots.admin.alias.set"),
    PERMISSION_ALIAS_REMOVE("plots.alias.remove"),
    PERMISSION_ADMIN_ALIAS_REMOVE("plots.admin.alias.remove"),
    PERMISSION_ADMIN_CHAT_BYPASS("plots.admin.chat.bypass"),
    PERMISSION_BACKUP("plots.backup"),
    PERMISSION_BACKUP_SAVE("plots.backup.save"),
    PERMISSION_BACKUP_LIST("plots.backup.list"),
    PERMISSION_BACKUP_LOAD("plots.backup.load"),
    PERMISSION_ADMIN_BACKUP_OTHER("plots.admin.backup.other"),
    PERMISSION_ADMIN_ALLOW_UNSAFE("plots.admin.unsafe"),
    PERMISSION_ADMIN_DEBUG_OTHER("plots.admin.debug.other"),
    PERMISSION_RATE("plots.rate"),
    PERMISSION_ADMIN_FLIGHT("plots.admin.flight"),
    PERMISSION_ADMIN_COMPONENTS_OTHER("plots.admin.component.other"),
    PERMISSION_ADMIN_BYPASS_BORDER("plots.admin.border.bypass"),
    PERMISSION_ADMIN_BYPASS_ECON("plots.admin.econ.bypass");
    //</editor-fold>

    private final String text;

    Permission(final @NonNull String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }

    public String format(Object... replacements) {
        return String.format(this.toString(), replacements);
    }

    @Override
    public @NotNull Component asComponent() {
        return Component.text(text);
    }

}
