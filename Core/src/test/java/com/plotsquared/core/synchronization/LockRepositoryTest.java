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
package com.plotsquared.core.synchronization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

        Assertions.assertThrows(IllegalStateException.class, () -> this.lockRepository.useLock(this.key, () -> {
            throw new IllegalStateException();
        }));
        Assertions.assertFalse(lock.isLocked());
    }

}
