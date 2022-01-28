package com.plotsquared.sponge.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.plotsquared.core.PlotPlatform;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.generator.HybridGen;
import com.plotsquared.core.generator.IndependentPlotGenerator;
import com.plotsquared.core.inject.annotations.ConsoleActor;
import com.plotsquared.core.inject.annotations.DefaultGenerator;
import com.plotsquared.core.inject.factory.ChunkCoordinatorBuilderFactory;
import com.plotsquared.core.inject.factory.ChunkCoordinatorFactory;
import com.plotsquared.core.inject.factory.HybridPlotWorldFactory;
import com.plotsquared.core.inject.factory.ProgressSubscriberFactory;
import com.plotsquared.core.plot.world.DefaultPlotAreaManager;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.plot.world.SinglePlotAreaManager;
import com.plotsquared.core.queue.ChunkCoordinator;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.QueueProvider;
import com.plotsquared.core.queue.subscriber.DefaultProgressSubscriber;
import com.plotsquared.core.queue.subscriber.ProgressSubscriber;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.InventoryUtil;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.SetupUtils;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.sponge.SpongePlatform;
import com.plotsquared.sponge.player.SpongePlayerManager;
import com.plotsquared.sponge.util.SpongeEconHandler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.EconomyService;

public class SpongeModule extends AbstractModule {

    private final SpongePlatform spongePlatform;

    public SpongeModule(SpongePlatform spongePlatform) {
        this.spongePlatform = spongePlatform;
    }

    @Override
    protected void configure() {
        bind(PlayerManager.class).to(SpongePlayerManager.class);
        bind(PlotPlatform.class).toInstance(spongePlatform);
        bind(SpongePlatform.class).toInstance(spongePlatform);
        bind(IndependentPlotGenerator.class).annotatedWith(DefaultGenerator.class).to(HybridGen.class);
        // Console actor
        @NonNull ConsoleCommandSender console = Sponge.server().getServer().getConsoleSender();
        WorldEditPlugin wePlugin = ((WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit"));
        bind(Actor.class).annotatedWith(ConsoleActor.class).toInstance(wePlugin.wrapCommandSender(console));
        bind(InventoryUtil.class).to(BukkitInventoryUtil.class);
        bind(SetupUtils.class).to(BukkitSetupUtils.class);
        bind(WorldUtil.class).to(BukkitUtil.class);
        install(new FactoryModuleBuilder()
                .implement(ProgressSubscriber.class, DefaultProgressSubscriber.class)
                .build(ProgressSubscriberFactory.class));
        bind(ChunkManager.class).to(BukkitChunkManager.class);
        if (PlotSquared.platform().isFaweHooking()) {
            bind(SchematicHandler.class).to(FaweSchematicHandler.class);
            bind(RegionManager.class).to(FaweRegionManager.class);
        } else {
            bind(SchematicHandler.class).to(BukkitSchematicHandler.class);
            bind(RegionManager.class).to(BukkitRegionManager.class);
        }
        bind(GlobalBlockQueue.class).toInstance(new GlobalBlockQueue(QueueProvider.of(BukkitQueueCoordinator.class)));
        if (Settings.Enabled_Components.WORLDS) {
            bind(PlotAreaManager.class).to(SinglePlotAreaManager.class);
            try {
                bind(SingleWorldListener.class).toInstance(new SingleWorldListener());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            bind(PlotAreaManager.class).to(DefaultPlotAreaManager.class);
        }
        install(new FactoryModuleBuilder().build(HybridPlotWorldFactory.class));
        install(new FactoryModuleBuilder()
                .implement(ChunkCoordinator.class, BukkitChunkCoordinator.class)
                .build(ChunkCoordinatorFactory.class));
        install(new FactoryModuleBuilder().build(ChunkCoordinatorBuilderFactory.class));
    }

    @Provides
    @Singleton
    @NonNull EconHandler provideEconHandler() {
        if (!Settings.Enabled_Components.ECONOMY) {
            return EconHandler.nullEconHandler();
        }
        SpongeEconHandler economyService = new SpongeEconHandler();

        if (!economyService.isSupported()) {
            spongePlatform.getLogger().warn("Economy is enabled but no plugin is providing an economy service.");
        }

        return economyService;
    }

}
