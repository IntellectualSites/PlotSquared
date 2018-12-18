package com.github.intellectualsites.plotsquared.plot.object;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;

public abstract class BlockRegistry<T> {

  @Getter
  private final Class<T> type;
  private final Map<PlotBlock, T> map = new HashMap<>();

  public BlockRegistry(@NonNull final Class<T> type, final T... preInitializedItems) {
    this.type = type;
    for (final T preInitializedItem : preInitializedItems) {
      this.addMapping(getPlotBlock(preInitializedItem), preInitializedItem);
    }
  }

  public final void addMapping(@NonNull final PlotBlock plotBlock, @NonNull final T t) {
    if (map.containsKey(plotBlock)) {
      return;
    }
    this.map.put(plotBlock, t);
  }

  public abstract PlotBlock getPlotBlock(final T item);

  public final T getItem(final PlotBlock plotBlock) {
    return this.map.get(plotBlock);
  }

}
