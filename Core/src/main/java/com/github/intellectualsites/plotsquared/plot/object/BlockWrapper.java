package com.github.intellectualsites.plotsquared.plot.object;

import java.util.Collections;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class BlockWrapper {

  @Getter
  private final PlotBlock type;
  private final Map<Class, Object> blockStates;

  public <T> T getState(@NonNull final Class stateType, @NonNull final Class<T> valueType, @NonNull final T defaultValue) {
    if (!blockStates.containsKey(stateType)) {
      return defaultValue;
    }
    final Object rawValue = blockStates.get(stateType);
    if (!rawValue.getClass().equals(valueType)) {
      throw new ClassCastException(String.format("State type %s has a value of type %s but %s was requested",
          stateType.getSimpleName(), rawValue.getClass().getSimpleName(), valueType.getSimpleName()));
    }
    return valueType.cast(rawValue);
  }

  public <T> void setState(@NonNull final Class stateType, @NonNull final T value) {
    this.blockStates.put(stateType, value);
  }

  public Map<Class, Object> getAllStates() {
    return Collections.unmodifiableMap(this.blockStates);
  }

}
