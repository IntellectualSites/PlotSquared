package com.github.intellectualsites.plotsquared.plot.object;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public final class BlockWrapperFactory<BlockType> {

  @RequiredArgsConstructor
  public static final class StateEntry<StateType extends Class, ObjectType> {

    @Getter
    private final StateType stateType;
    @Getter
    private final ObjectType value;

  }

  public interface BlockStateDeserializer<StateType extends Class, ObjectType> {
    StateEntry<StateType, ObjectType> deserialize(PlotBlock type, String serialized);
    boolean isOfType(String serializedString);
  }

  @FunctionalInterface
  public interface BlockStateSerializer<StateType extends Class, ObjectType> {
    String serialize(PlotBlock type, StateEntry<StateType, ObjectType> entry);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class StateSerializationMapping<StateType extends Class, ObjectType extends Class> {
    @Getter
    private final StateType type;
    @Getter
    private final ObjectType objectType;
    @Getter
    private final BlockStateSerializer<StateType, ObjectType> serializer;
    @Getter
    private final BlockStateDeserializer<StateType, ObjectType> deserializer;
  }

  private final Map<Class, StateSerializationMapping> stateSerializationMappings = new HashMap<>();

  public <StateType extends Class, ObjectType extends Class> StateSerializationMapping<StateType, ObjectType>
    addStateMapping(@NonNull final StateType type, @NonNull final ObjectType objectType, @NonNull final BlockStateSerializer<StateType, ObjectType> serializer, @NonNull final BlockStateDeserializer<StateType, ObjectType> deserializer) {
    final StateSerializationMapping<StateType, ObjectType> stateSerializationMapping = new StateSerializationMapping<>(type, objectType, serializer, deserializer);
    this.stateSerializationMappings.put(type, stateSerializationMapping);
    return stateSerializationMapping;
  }

  public <StateType extends Class, ObjectType extends Class> StateSerializationMapping<StateType, ObjectType>
    getStateMapping(@NonNull final StateType type) {
    return this.stateSerializationMappings.get(type);
  }

  public Collection<String> serializeStates(@NonNull final BlockWrapper blockWrapper) {
    final List<String> serializedStates = new ArrayList<>();
    blockWrapper.getAllStates().entrySet().stream().map(entry -> new StateEntry(entry.getKey(), entry.getValue()))
        .forEach(entry -> {
          final StateSerializationMapping stateSerializationMapping = getStateMapping(entry.getStateType());
          final BlockStateSerializer blockStateSerializer = stateSerializationMapping.getSerializer();
          final String serialized = blockStateSerializer.serialize(blockWrapper.getType(), entry);
          serializedStates.add(serialized);
        });
    return serializedStates;
  }

  public BlockStateDeserializer getDeserializerRaw(@NonNull final String serializedString) {
    for (final StateSerializationMapping stateSerializationMapping : this.stateSerializationMappings.values()) {
      if (stateSerializationMapping.getDeserializer().isOfType(serializedString)) {
        return stateSerializationMapping.getDeserializer();
      }
    }
    return null;
  }

  public Collection<StateEntry> deserializeStates(@NonNull final PlotBlock plotBlock, @NonNull final Collection<String> serializedStates) {
    final Collection<StateEntry> stateEntries = new ArrayList<>(serializedStates.size());
    for (final String serializedState : serializedStates) {
      if (serializedState == null || serializedState.isEmpty()) {
        continue;
      }
      final BlockStateDeserializer blockStateDeserializer = getDeserializerRaw(serializedState);
      if (blockStateDeserializer == null) {
        throw new IllegalStateException(String.format("No deserializer available for %s", serializedState));
      }
      stateEntries.add(blockStateDeserializer.deserialize(plotBlock, serializedState));
    }
    return stateEntries;
  }

}
