package com.plotsquared.core.repository;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

/**
 * Generic object repository.
 *
 * @param <T> the type of object stored in the repository.
 * @param <U> the type used to identify objects stored in the repository.
 */
public interface Repository<@NonNull T, @NonNull U> {

    /**
     * Saves the given object.
     *
     * @param object {@code the object}.
     */
    void save(T object);

    /**
     * Finds the object by its {@code id}.
     *
     * @param id the id
     */
    @NonNull Optional<T> findById(U id);
}
