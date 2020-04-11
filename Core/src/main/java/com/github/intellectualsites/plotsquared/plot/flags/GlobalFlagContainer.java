/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.intellectualsites.plotsquared.plot.flags;

import com.github.intellectualsites.plotsquared.plot.flags.implementations.AnalysisFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.AnimalAttackFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.AnimalCapFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.AnimalInteractFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.BlockBurnFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.BlockIgnitionFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.BlockedCmdsFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.BreakFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.CoralDryFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DenyExitFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DenyTeleportFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DescriptionFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DeviceInteractFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DisablePhysicsFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DoneFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DropProtectionFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.EntityCapFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.ExplosionFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.FarewellFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.FeedFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.FlyFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.ForcefieldFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.GamemodeFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.GrassGrowFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.GreetingFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.GuestGamemodeFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.HangingBreakFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.HangingPlaceFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.HealFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.HideInfoFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.HostileAttackFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.HostileCapFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.HostileInteractFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.IceFormFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.IceMeltFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.InstabreakFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.InvincibleFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.ItemDropFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.KeepFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.KelpGrowFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.LiquidFlowFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MiscBreakFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MiscCapFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MiscInteractFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MiscPlaceFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MobBreakFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MobCapFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MobPlaceFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MusicFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MycelGrowFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.NoWorldeditFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.NotifyEnterFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.NotifyLeaveFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.PlaceFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.PlayerInteractFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.PriceFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.PveFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.PvpFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.RedstoneFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.ServerPlotFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.SnowFormFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.SnowMeltFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.SoilDryFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.TamedAttackFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.TamedInteractFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.TimeFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.TitlesFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.UntrustedVisitFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.UseFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.VehicleBreakFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.VehicleCapFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.VehiclePlaceFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.VehicleUseFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.VillagerInteractFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.VineGrowFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.WeatherFlag;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class GlobalFlagContainer extends FlagContainer {

    @Getter private static final GlobalFlagContainer instance = new GlobalFlagContainer();
    private static Map<String, Class<?>> stringClassMap;

    private GlobalFlagContainer() {
        super(null, (flag, type) -> {
            if (type == PlotFlagUpdateType.FLAG_ADDED) {
                stringClassMap.put(flag.getName().toLowerCase(Locale.ENGLISH), flag.getClass());
            }
        });
        stringClassMap = new HashMap<>();
        // Register all default flags here
        // Boolean flags
        this.addFlag(ExplosionFlag.EXPLOSION_FALSE);
        this.addFlag(UntrustedVisitFlag.UNTRUSTED_VISIT_FLAG_TRUE);
        this.addFlag(DenyExitFlag.DENY_EXIT_FLAG_FALSE);
        this.addFlag(DescriptionFlag.DESCRIPTION_FLAG_EMPTY);
        this.addFlag(GreetingFlag.GREETING_FLAG_EMPTY);
        this.addFlag(FarewellFlag.FAREWELL_FLAG_EMPTY);
        this.addFlag(AnimalAttackFlag.ANIMAL_ATTACK_FALSE);
        this.addFlag(AnimalInteractFlag.ANIMAL_INTERACT_FALSE);
        this.addFlag(BlockBurnFlag.BLOCK_BURN_FALSE);
        this.addFlag(BlockIgnitionFlag.BLOCK_IGNITION_TRUE);
        this.addFlag(DeviceInteractFlag.DEVICE_INTERACT_FALSE);
        this.addFlag(DisablePhysicsFlag.DISABLE_PHYSICS_FALSE);
        this.addFlag(DropProtectionFlag.DROP_PROTECTION_FALSE);
        this.addFlag(ForcefieldFlag.FORCEFIELD_FALSE);
        this.addFlag(GrassGrowFlag.GRASS_GROW_TRUE);
        this.addFlag(HangingBreakFlag.HANGING_BREAK_FALSE);
        this.addFlag(HangingPlaceFlag.HANGING_PLACE_FALSE);
        this.addFlag(HideInfoFlag.HIDE_INFO_FALSE);
        this.addFlag(HostileAttackFlag.HOSTILE_ATTACK_FALSE);
        this.addFlag(HostileInteractFlag.HOSTILE_INTERACT_FALSE);
        this.addFlag(IceFormFlag.ICE_FORM_FALSE);
        this.addFlag(IceMeltFlag.ICE_MELT_FALSE);
        this.addFlag(KelpGrowFlag.KELP_GROW_TRUE);
        this.addFlag(LiquidFlowFlag.LIQUID_FLOW_TRUE);
        this.addFlag(RedstoneFlag.REDSTONE_TRUE);
        this.addFlag(ServerPlotFlag.SERVER_PLOT_FALSE);
        this.addFlag(MiscBreakFlag.MISC_BREAK_FALSE);
        this.addFlag(MiscInteractFlag.MISC_INTERACT_FALSE);
        this.addFlag(MiscPlaceFlag.MISC_PLACE_FALSE);
        this.addFlag(MobBreakFlag.MOB_BREAK_FALSE);
        this.addFlag(MobPlaceFlag.MOB_PLACE_FALSE);
        this.addFlag(MycelGrowFlag.MYCEL_GROW_TRUE);
        this.addFlag(NotifyEnterFlag.NOTIFY_ENTER_FALSE);
        this.addFlag(NotifyLeaveFlag.NOTIFY_LEAVE_FALSE);
        this.addFlag(NoWorldeditFlag.NO_WORLDEDIT_FALSE);
        this.addFlag(PlayerInteractFlag.PLAYER_INTERACT_FALSE);
        this.addFlag(PveFlag.PVE_FALSE);
        this.addFlag(PvpFlag.PVP_FALSE);
        this.addFlag(SnowFormFlag.SNOW_FORM_FALSE);
        this.addFlag(SnowMeltFlag.SNOW_MELT_TRUE);
        this.addFlag(SoilDryFlag.SOIL_DRY_FALSE);
        this.addFlag(CoralDryFlag.CORAL_DRY_FALSE);
        this.addFlag(TamedAttackFlag.TAMED_ATTACK_FALSE);
        this.addFlag(TamedInteractFlag.TAMED_INTERACT_FALSE);
        this.addFlag(VehicleBreakFlag.VEHICLE_BREAK_FALSE);
        this.addFlag(VehiclePlaceFlag.VEHICLE_PLACE_FALSE);
        this.addFlag(VehicleUseFlag.VEHICLE_USE_FALSE);
        this.addFlag(VillagerInteractFlag.VILLAGER_INTERACT_FALSE);
        this.addFlag(VineGrowFlag.VINE_GROW_TRUE);
        this.addFlag(ItemDropFlag.ITEM_DROP_TRUE);
        this.addFlag(InstabreakFlag.INSTABREAK_FALSE);
        this.addFlag(InvincibleFlag.INVINCIBLE_FALSE);

        // Enum Flags
        this.addFlag(WeatherFlag.PLOT_WEATHER_FLAG_OFF);
        this.addFlag(DenyTeleportFlag.DENY_TELEPORT_FLAG_NONE);
        this.addFlag(TitlesFlag.TITLES_NONE);
        this.addFlag(FlyFlag.FLIGHT_FLAG_DEFAULT);

        // Integer flags
        this.addFlag(AnimalCapFlag.ANIMAL_CAP_UNLIMITED);
        this.addFlag(EntityCapFlag.ENTITY_CAP_UNLIMITED);
        this.addFlag(HostileCapFlag.HOSTILE_CAP_UNLIMITED);
        this.addFlag(MiscCapFlag.MISC_CAP_UNLIMITED);
        this.addFlag(MobCapFlag.MOB_CAP_UNLIMITED);
        this.addFlag(TimeFlag.TIME_DISABLED);
        this.addFlag(VehicleCapFlag.VEHICLE_CAP_UNLIMITED);

        // Timed flags
        this.addFlag(FeedFlag.FEED_NOTHING);
        this.addFlag(HealFlag.HEAL_NOTHING);

        // Double flags
        this.addFlag(PriceFlag.PRICE_NOT_BUYABLE);

        // Block type list flags
        this.addFlag(BreakFlag.BREAK_NONE);
        this.addFlag(PlaceFlag.PLACE_NONE);
        this.addFlag(UseFlag.USE_NONE);

        // Misc
        this.addFlag(GamemodeFlag.GAMEMODE_FLAG_DEFAULT);
        this.addFlag(GuestGamemodeFlag.GUEST_GAMEMODE_FLAG_DEFAULT);
        this.addFlag(BlockedCmdsFlag.BLOCKED_CMDS_FLAG_NONE);
        this.addFlag(KeepFlag.KEEP_FLAG_FALSE);
        this.addFlag(MusicFlag.MUSIC_FLAG_NONE);

        // Internal flags
        this.addFlag(new AnalysisFlag(Collections.emptyList()));
        this.addFlag(new DoneFlag(""));
    }

    @Override public PlotFlag<?, ?> getFlagErased(Class<?> flagClass) {
        final PlotFlag<?, ?> flag = super.getFlagErased(flagClass);
        if (flag != null) {
            return flag;
        } else {
            throw new IllegalStateException(String.format("Unrecognized flag '%s'. All flag types"
                + " must be present in the global flag container.", flagClass.getSimpleName()));
        }
    }

    @Nonnull @Override
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
