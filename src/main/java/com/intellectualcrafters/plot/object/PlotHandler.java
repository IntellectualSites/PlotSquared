package com.intellectualcrafters.plot.object;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.UUIDHandler;

public class PlotHandler {
    public static HashSet<UUID> getOwners(final Plot plot) {
        if (plot.owner == null) {
            return new HashSet<UUID>();
        }
        if (plot.isMerged()) {
            HashSet<Plot> plots = MainUtil.getConnectedPlots(plot);
            final HashSet<UUID> owners = new HashSet<UUID>(2);
            UUID last = plot.owner;
            owners.add(plot.owner);
            for (Plot current : plots) {
                if (last == null || current.owner.getMostSignificantBits() != last.getMostSignificantBits()) {
                    owners.add(current.owner);
                    last = current.owner;
                }
            }
            return owners;
        }
        return new HashSet<>(Collections.singletonList(plot.owner));
    }
    
    public static boolean isOwner(final Plot plot, final UUID uuid) {
        if (plot.owner == null) {
            return false;
        }
        if (plot.owner.equals(uuid)) {
            return true;
        }
        if (!plot.isMerged()) {
            return false;
        }
        for (Plot current : MainUtil.getConnectedPlots(plot)) {
            if (current.owner.equals(uuid)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isOnline(final Plot plot) {
        if (plot.owner == null) {
            return false;
        }
        if (!plot.isMerged()) {
            return UUIDHandler.getPlayer(plot.owner) != null;
        }
        for (Plot current : MainUtil.getConnectedPlots(plot)) {
            if (current.owner != null && UUIDHandler.getPlayer(current.owner) != null) {
                return true;
            }
        }
        return false;
    }
    
    public static void setOwner(Plot plot, UUID owner) {
        if (!plot.hasOwner()) {
            plot.owner = owner;
            plot.create();
            return;
        }
        if (!plot.isMerged()) {
            if (!plot.owner.equals(owner)) {
                plot.owner = owner;
                DBFunc.setOwner(plot, owner);
            }
            return;
        }
        for (Plot current : MainUtil.getConnectedPlots(plot)) {
            if (!owner.equals(current.owner)) {
                current.owner = owner;
                DBFunc.setOwner(current, owner);
            }
        }
    }
    
    public static boolean sameOwners(final Plot plot1, final Plot plot2) {
        if ((plot1.owner == null) || (plot2.owner == null)) {
            return false;
        }
        final HashSet<UUID> owners = getOwners(plot1);
        owners.retainAll(getOwners(plot2));
        return owners.size() > 0;
    }
    
    public static boolean isAdded(final Plot plot, final UUID uuid) {
        if (plot.owner == null) {
            return false;
        }
        if (plot.getDenied().contains(uuid)) {
            return false;
        }
        if (plot.getTrusted().contains(uuid) || plot.getTrusted().contains(DBFunc.everyone)) {
            return true;
        }
        if (isOwner(plot, uuid)) {
            return true;
        }
        if (plot.getMembers().contains(uuid) || plot.getMembers().contains(DBFunc.everyone)) {
            if (PlotHandler.isOnline(plot)) {
                return true;
            }
        }
        return false;
    }
    
    public static void setDenied(Plot plot, Set<UUID> uuids) {
        boolean larger = uuids.size() > plot.getDenied().size();
        HashSet<UUID> intersection = new HashSet<>(larger ? plot.getDenied() : uuids);
        intersection.retainAll(larger ? uuids : plot.getDenied());
        uuids.removeAll(intersection);
        HashSet<UUID> toRemove = new HashSet<>(plot.getDenied());
        toRemove.removeAll(intersection);
        for (UUID uuid : toRemove) {
            plot.removeDenied(uuid);
        }
        for (UUID uuid : uuids) {
            plot.addDenied(uuid);
        }
    }
    
    public static void setTrusted(Plot plot, Set<UUID> uuids) {
        boolean larger = uuids.size() > plot.getTrusted().size();
        HashSet<UUID> intersection = new HashSet<>(larger ? plot.getTrusted() : uuids);
        intersection.retainAll(larger ? uuids : plot.getTrusted());
        uuids.removeAll(intersection);
        HashSet<UUID> toRemove = new HashSet<>(plot.getTrusted());
        toRemove.removeAll(intersection);
        for (UUID uuid : toRemove) {
            plot.removeTrusted(uuid);
        }
        for (UUID uuid : uuids) {
            plot.addTrusted(uuid);
        }
    }
    
    public static void setMembers(Plot plot, Set<UUID> uuids) {
        boolean larger = uuids.size() > plot.getMembers().size();
        HashSet<UUID> intersection = new HashSet<>(larger ? plot.getMembers() : uuids);
        intersection.retainAll(larger ? uuids : plot.getMembers());
        uuids.removeAll(intersection);
        HashSet<UUID> toRemove = new HashSet<>(plot.getMembers());
        toRemove.removeAll(intersection);
        for (UUID uuid : toRemove) {
            plot.removeMember(uuid);
        }
        for (UUID uuid : uuids) {
            plot.addMember(uuid);
        }
    }
    
    public static void set(Plot plot, Set<UUID> uuids) {
        boolean larger = uuids.size() > plot.getDenied().size();
        HashSet<UUID> intersection = new HashSet<>(larger ? plot.getDenied() : uuids);
        intersection.retainAll(larger ? uuids : plot.getDenied());
        uuids.removeAll(intersection);
        HashSet<UUID> toRemove = new HashSet<>(plot.getDenied());
        toRemove.removeAll(intersection);
        for (UUID uuid : toRemove) {
            plot.removeDenied(uuid);
        }
        for (UUID uuid : uuids) {
            plot.addDenied(uuid);
        }
    }

    public static void addDenied(Plot plot, UUID uuid) {
        for (Plot current : MainUtil.getConnectedPlots(plot)) {
            if (current.getDenied().add(uuid)) {
                DBFunc.setDenied(current, uuid);
            }
        }
    }
    
    public static void addMember(Plot plot, UUID uuid) {
        for (Plot current : MainUtil.getConnectedPlots(plot)) {
            if (current.getMembers().add(uuid)) {
                DBFunc.setMember(current, uuid);
            }
        }
    }
    
    public static void addTrusted(Plot plot, UUID uuid) {
        for (Plot current : MainUtil.getConnectedPlots(plot)) {
            if (current.getTrusted().add(uuid)) {
                DBFunc.setTrusted(current, uuid);
            }
        }
    }
    
    public static boolean removeDenied(Plot plot, UUID uuid) {
        for (Plot current : MainUtil.getConnectedPlots(plot)) {
            if (current.getDenied().remove(uuid)) {
                DBFunc.removeDenied(current, uuid);
            } else {
                return false;
            }
        }
        return true;
    }
    
    public static boolean removeMember(Plot plot, UUID uuid) {
        for (Plot current : MainUtil.getConnectedPlots(plot)) {
            if (current.getMembers().remove(uuid)) {
                DBFunc.removeMember(current, uuid);
            } else {
                return false;
            }
        }
        return true;
    }
    
    public static boolean removeTrusted(Plot plot, UUID uuid) {
        for (Plot current : MainUtil.getConnectedPlots(plot)) {
            if (current.getTrusted().remove(uuid)) {
                DBFunc.removeTrusted(current, uuid);
            } else {
                return false;
            }
        }
        return true;
    }

    public static boolean unclaim(Plot plot) {
        if (plot.owner == null) {
            return false;
        }
        for (Plot current : MainUtil.getConnectedPlots(plot)) {
            PS.get().removePlot(current.world, current.getId(), true);
            DBFunc.delete(current);
            current.settings = null;
        }
        return true;
    }
}
