package com.github.intellectualsites.plotsquared.bukkit.listeners;

import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.vehicle.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.List;

@SuppressWarnings("unused") public class EntitySpawnListener implements Listener {

    private static boolean ignoreTP = false;

    public static void test(Entity entity) {
        List<MetadataValue> meta = entity.getMetadata("plotworld");
        World world = entity.getLocation().getWorld();
        if (meta == null || meta.isEmpty()) {
            if (PlotSquared.get().hasPlotArea(world.getName())) {
                entity.setMetadata("plotworld",
                    new FixedMetadataValue((Plugin) PlotSquared.get().IMP, entity.getLocation()));
            }
        } else {
            org.bukkit.Location origin = (org.bukkit.Location) meta.get(0).value();
            World originWorld = origin.getWorld();
            if (!originWorld.equals(world)) {
                if (!ignoreTP) {
                    if (!world.getName().equalsIgnoreCase(originWorld + "_the_end")) {
                        try {
                            ignoreTP = true;
                            entity.teleport(origin);
                        } finally {
                            ignoreTP = false;
                        }
                        if (entity.getLocation().getWorld().equals(world)) {
                            entity.remove();
                        }
                    }
                } else {
                    entity.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void creatureSpawnEvent(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        Location location = BukkitUtil.getLocation(entity.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlotAbs(location);
        if (plot == null) {
            if (!area.MOB_SPAWNING) {
                EntityType type = entity.getType();
                switch (type) {
                    case DROPPED_ITEM:
                        if (Settings.Enabled_Components.KILL_ROAD_ITEMS) {
                            event.setCancelled(true);
                            break;
                        }
                    case PLAYER:
                        return;
                }
                if (type.isAlive() || !area.MISC_SPAWN_UNOWNED) {
                    event.setCancelled(true);
                }
            }
            return;
        }
        if (Settings.Done.RESTRICT_BUILDING && plot.hasFlag(Flags.DONE)) {
            event.setCancelled(true);
        }
        switch (entity.getType()) {
            case ENDER_CRYSTAL:
                if (PlayerEvents.checkEntity(entity, plot)) {
                    event.setCancelled(true);
                }
            case SHULKER:
                if (!entity.hasMetadata("plot")) {
                    entity.setMetadata("plot",
                        new FixedMetadataValue((Plugin) PlotSquared.get().IMP, plot.getId()));
                }
        }
    }

    @EventHandler public void onVehicle(VehicleUpdateEvent event) {
        test(event.getVehicle());
    }

    @EventHandler public void onVehicle(VehicleDestroyEvent event) {
        test(event.getVehicle());
    }

    @EventHandler public void onVehicle(VehicleEntityCollisionEvent event) {
        test(event.getVehicle());
    }

    @EventHandler public void onVehicle(VehicleCreateEvent event) {
        test(event.getVehicle());
    }

    @EventHandler public void onVehicle(VehicleBlockCollisionEvent event) {
        test(event.getVehicle());
    }

    @EventHandler public void onTeleport(EntityTeleportEvent event) {
        Entity ent = event.getEntity();
        if (ent instanceof Vehicle || ent instanceof ArmorStand)
            test(event.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void vehicleMove(VehicleMoveEvent event) throws IllegalAccessException {
        test(event.getVehicle());
    }

    @EventHandler public void spawn(CreatureSpawnEvent event) {
        switch (event.getEntityType()) {
            case ARMOR_STAND:
                test(event.getEntity());
        }
    }
}
