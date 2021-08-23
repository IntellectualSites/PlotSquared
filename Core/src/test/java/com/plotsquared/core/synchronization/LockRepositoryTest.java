package com.plotsquared.core.synchronization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LockRepositoryTest {

    private LockKey key;
    private LockRepository lockRepository;

    @BeforeEach
    void setUp() {
        this.key = LockKey.of("test");
        this.lockRepository = new LockRepository();
    }

    @Test
    @DisplayName("Unlock even if there is an error")
    void useLockUnlock() {
        Lock l = this.lockRepository.getLock(this.key);
        // Striped uses a ReentrantLock internally, and we need its isLocked method for this test
        if (!(l instanceof ReentrantLock lock)) {
            throw new IllegalStateException("Expected a ReentrantLock");
        }

        assertThrows(IllegalStateException.class, () -> {
            this.lockRepository.useLock(this.key, () -> {
                throw new IllegalStateException();
            });
        });
        assertFalse(lock.isLocked());
    }
}
