package com.plotsquared.core.util.gui;

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotItemStack;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class ExtendablePlotInventory<P, I> extends PlotInventory<P, I> {

    private final PlotInventory<P, I> delegate;

    public ExtendablePlotInventory(PlotInventory<P, I> delegate) {
        super(delegate.player(), delegate.size(), delegate.titleCaption(), delegate.titleResolvers());
        this.delegate = delegate;
    }

    public ExtendablePlotInventory(
            PlotInventoryProvider<P, I> provider, PlotPlayer<?> player, int size, Caption title,
            TagResolver... titleResolver
    ) {
        this(provider.createInventory(player, size, title, titleResolver));
    }

    @Override
    public void setItem(final int slot, final PlotItemStack item, final PlotInventoryClickHandler onClick) {
        delegate.setItem(slot, item, onClick);
    }

    @Override
    public void addItem(final PlotItemStack item, final PlotInventoryClickHandler onClick) {
        delegate.addItem(item, onClick);
    }

    @Override
    public void open() {
        delegate.open();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public I toPlatformItem(final PlotItemStack item) {
        return delegate.toPlatformItem(item);
    }

}
