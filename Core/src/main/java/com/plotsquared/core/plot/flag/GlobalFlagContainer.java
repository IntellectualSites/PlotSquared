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
package com.plotsquared.core.plot.flag;

import com.google.common.base.Preconditions;
import com.plotsquared.core.plot.flag.implementations.*;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class GlobalFlagContainer extends FlagContainer {

    private static GlobalFlagContainer instance;
    private static Map<String, Class<?>> stringClassMap;

    private GlobalFlagContainer() {
        super(null, (flag, type) -> {
            if (type == PlotFlagUpdateType.FLAG_ADDED) {
                stringClassMap.put(flag.getName().toLowerCase(Locale.ENGLISH), flag.getClass());
            }
        });
        stringClassMap = new HashMap<>();

        // Register all default flags here
        // Block type list flags
        this.addFlag(BreakFlag.BREAK_NONE);
        this.addFlag(UseFlag.USE_NONE);
        this.addFlag(PlaceFlag.PLACE_NONE);

        // Boolean flags
        this.addFlag(AnimalAttackFlag.ANIMAL_ATTACK_FALSE);
        this.addFlag(AnimalInteractFlag.ANIMAL_INTERACT_FALSE);
        this.addFlag(BlockBurnFlag.BLOCK_BURN_FALSE);
        this.addFlag(BeaconEffectsFlag.BEACON_EFFECT_TRUE);
        this.addFlag(BlockIgnitionFlag.BLOCK_IGNITION_TRUE);
        this.addFlag(ChatFlag.CHAT_FLAG_TRUE);
        this.addFlag(ConcreteHardenFlag.CONCRETE_HARDEN_TRUE);
        this.addFlag(CopperOxideFlag.COPPER_OXIDE_FALSE);
        this.addFlag(CoralDryFlag.CORAL_DRY_FALSE);
        this.addFlag(CropGrowFlag.CROP_GROW_TRUE);
        this.addFlag(DenyExitFlag.DENY_EXIT_FLAG_FALSE);
        this.addFlag(DenyPortalsFlag.DENY_PORTALS_FALSE);
        this.addFlag(DenyPortalTravelFlag.DENY_PORTAL_TRAVEL_FALSE);
        this.addFlag(DeviceInteractFlag.DEVICE_INTERACT_FALSE);
        this.addFlag(DisablePhysicsFlag.DISABLE_PHYSICS_FALSE);
        this.addFlag(DropProtectionFlag.DROP_PROTECTION_FALSE);
        this.addFlag(EditSignFlag.EDIT_SIGN_FALSE);
        this.addFlag(EntityChangeBlockFlag.ENTITY_CHANGE_BLOCK_FALSE);
        this.addFlag(ExplosionFlag.EXPLOSION_FALSE);
        this.addFlag(FishingFlag.FISHING_FALSE);
        this.addFlag(ForcefieldFlag.FORCEFIELD_FALSE);
        this.addFlag(GrassGrowFlag.GRASS_GROW_TRUE);
        this.addFlag(HangingBreakFlag.HANGING_BREAK_FALSE);
        this.addFlag(HangingPlaceFlag.HANGING_PLACE_FALSE);
        this.addFlag(HideInfoFlag.HIDE_INFO_FALSE);
        this.addFlag(HostileAttackFlag.HOSTILE_ATTACK_FALSE);
        this.addFlag(HostileInteractFlag.HOSTILE_INTERACT_FALSE);
        this.addFlag(IceFormFlag.ICE_FORM_FALSE);
        this.addFlag(IceMeltFlag.ICE_MELT_FALSE);
        this.addFlag(InstabreakFlag.INSTABREAK_FALSE);
        this.addFlag(InvincibleFlag.INVINCIBLE_FALSE);
        this.addFlag(ItemDropFlag.ITEM_DROP_TRUE);
        this.addFlag(KeepInventoryFlag.KEEP_INVENTORY_FALSE);
        this.addFlag(KelpGrowFlag.KELP_GROW_TRUE);
        this.addFlag(LeafDecayFlag.LEAF_DECAY_TRUE);
        this.addFlag(LecternReadBookFlag.LECTERN_READ_BOOK_FALSE);
        this.addFlag(MiscBreakFlag.MISC_BREAK_FALSE);
        this.addFlag(MobBreakFlag.MOB_BREAK_FALSE);
        this.addFlag(MobPlaceFlag.MOB_PLACE_FALSE);
        this.addFlag(MiscInteractFlag.MISC_INTERACT_FALSE);
        this.addFlag(SculkSensorInteractFlag.SCULK_SENSOR_INTERACT_FALSE);
        this.addFlag(MiscPlaceFlag.MISC_PLACE_FALSE);
        this.addFlag(MycelGrowFlag.MYCEL_GROW_TRUE);
        this.addFlag(NotifyEnterFlag.NOTIFY_ENTER_FALSE);
        this.addFlag(NotifyLeaveFlag.NOTIFY_LEAVE_FALSE);
        this.addFlag(NoWorldeditFlag.NO_WORLDEDIT_FALSE);
        this.addFlag(PlayerInteractFlag.PLAYER_INTERACT_FALSE);
        this.addFlag(PreventCreativeCopyFlag.PREVENT_CREATIVE_COPY_FALSE);
        this.addFlag(ProjectileChangeBlockFlag.PROJECTILE_CHANGE_BLOCK_FALSE);
        this.addFlag(PveFlag.PVE_FALSE);
        this.addFlag(PvpFlag.PVP_FALSE);
        this.addFlag(RedstoneFlag.REDSTONE_TRUE);
        this.addFlag(ServerPlotFlag.SERVER_PLOT_FALSE);
        this.addFlag(SnowFormFlag.SNOW_FORM_FALSE);
        this.addFlag(SnowMeltFlag.SNOW_MELT_TRUE);
        this.addFlag(SoilDryFlag.SOIL_DRY_FALSE);
        this.addFlag(TamedAttackFlag.TAMED_ATTACK_FALSE);
        this.addFlag(TamedInteractFlag.TAMED_INTERACT_FALSE);
        this.addFlag(TileDropFlag.TILE_DROP_TRUE);
        this.addFlag(UntrustedVisitFlag.UNTRUSTED_VISIT_FLAG_TRUE);
        this.addFlag(VehicleBreakFlag.VEHICLE_BREAK_FALSE);
        this.addFlag(VehiclePlaceFlag.VEHICLE_PLACE_FALSE);
        this.addFlag(VehicleUseFlag.VEHICLE_USE_FALSE);
        this.addFlag(VillagerInteractFlag.VILLAGER_INTERACT_FALSE);
        this.addFlag(VineGrowFlag.VINE_GROW_TRUE);
        this.addFlag(ProjectilesFlag.PROJECTILES_FALSE);
        this.addFlag(WeavingDeathPlace.WEAVING_DEATH_PLACE_FALSE);

        // Double flags
        this.addFlag(PriceFlag.PRICE_NOT_BUYABLE);

        // Enum Flags
        this.addFlag(DenyTeleportFlag.DENY_TELEPORT_FLAG_NONE);
        this.addFlag(FlyFlag.FLIGHT_FLAG_DEFAULT);
        this.addFlag(LiquidFlowFlag.LIQUID_FLOW_DEFAULT);
        this.addFlag(TitlesFlag.TITLES_NONE);
        this.addFlag(WeatherFlag.PLOT_WEATHER_FLAG_OFF);

        // Internal flags
        this.addFlag(new AnalysisFlag(Collections.emptyList()));
        this.addFlag(new DoneFlag(""));

        // Integer flags
        this.addFlag(AnimalCapFlag.ANIMAL_CAP_UNLIMITED);
        this.addFlag(EntityCapFlag.ENTITY_CAP_UNLIMITED);
        this.addFlag(HostileCapFlag.HOSTILE_CAP_UNLIMITED);
        this.addFlag(MiscCapFlag.MISC_CAP_UNLIMITED);
        this.addFlag(MobCapFlag.MOB_CAP_UNLIMITED);
        this.addFlag(TimeFlag.TIME_DISABLED);
        this.addFlag(VehicleCapFlag.VEHICLE_CAP_UNLIMITED);

        // Misc
        this.addFlag(BlockedCmdsFlag.BLOCKED_CMDS_FLAG_NONE);
        this.addFlag(GamemodeFlag.GAMEMODE_FLAG_DEFAULT);
        this.addFlag(GuestGamemodeFlag.GUEST_GAMEMODE_FLAG_DEFAULT);
        this.addFlag(KeepFlag.KEEP_FLAG_FALSE);
        this.addFlag(MusicFlag.MUSIC_FLAG_NONE);

        // String flags
        this.addFlag(DescriptionFlag.DESCRIPTION_FLAG_EMPTY);
        this.addFlag(GreetingFlag.GREETING_FLAG_EMPTY);
        this.addFlag(FarewellFlag.FAREWELL_FLAG_EMPTY);
        this.addFlag(PlotTitleFlag.TITLE_FLAG_DEFAULT);

        // Timed flags
        this.addFlag(FeedFlag.FEED_NOTHING);
        this.addFlag(HealFlag.HEAL_NOTHING);
    }

    public static void setup() {
        Preconditions.checkState(instance == null, "Cannot setup the container twice");
        instance = new GlobalFlagContainer();
    }

    public static GlobalFlagContainer getInstance() {
        return GlobalFlagContainer.instance;
    }

    @Override
    public PlotFlag<?, ?> getFlagErased(Class<?> flagClass) {
        final PlotFlag<?, ?> flag = super.getFlagErased(flagClass);
        if (flag != null) {
            return flag;
        } else {
            throw new IllegalStateException(String.format("Unrecognized flag '%s'. All flag types"
                    + " must be present in the global flag container.", flagClass.getSimpleName()));
        }
    }

    @NonNull
    @Override
    public <V, T extends PlotFlag<V, ?>> T getFlag(Class<? extends T> flagClass) {
        final PlotFlag<?, ?> flag = super.getFlag(flagClass);
        if (flag != null) {
            return castUnsafe(flag);
        } else {
            throw new IllegalStateException(String.format("Unrecognized flag '%s'. All flag types"
                    + " must be present in the global flag container.", flagClass.getSimpleName()));
        }
    }

    public Class<?> getFlagClassFromString(final String name) {
        return stringClassMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    public PlotFlag<?, ?> getFlagFromString(final String name) {
        final Class<?> flagClass = this.getFlagClassFromString(name);
        if (flagClass == null) {
            return null;
        }
        return getFlagErased(flagClass);
    }

}
