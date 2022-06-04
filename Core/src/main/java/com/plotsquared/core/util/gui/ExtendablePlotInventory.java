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
 *               Copyright (C) 2014 - 2022 IntellectualSites
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
