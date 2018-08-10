package com.github.intellectualsites.plotsquared.bukkit.listeners;

import com.github.intellectualsites.plotsquared.plot.PS;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.vehicle.*;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class EntityPortal_1_7_9 implements Listener {
    private static boolean ignoreTP = false;

    public EntityPortal_1_7_9() {
    }

    public static void test(Entity entity) {
        List<MetadataValue> meta = entity.getMetadata("plotworld");
        World world = entity.getLocation().getWorld();
        if (meta == null || meta.isEmpty()) {
            if (PS.get().isPlotWorld(world.getName())) {
                entity.setMetadata("plotworld",
                    new FixedMetadataValue((Plugin) PS.get().IMP, entity.getLocation()));
            }
        } else {
            Location origin = (Location) meta.get(0).value();
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
