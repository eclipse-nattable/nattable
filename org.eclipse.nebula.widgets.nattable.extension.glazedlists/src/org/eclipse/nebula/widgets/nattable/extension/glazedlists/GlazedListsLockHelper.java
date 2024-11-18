/*******************************************************************************
 * Copyright (c) 2024 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.odell.glazedlists.util.concurrent.Lock;
import ca.odell.glazedlists.util.concurrent.ReadWriteLock;

/**
 * Helper class to perform write operations on a GlazedLists that uses
 * {@link Lock#tryLock()} with a waiting time of 1 minute to reduce the risk of
 * deadlocks or endless waiting.
 *
 * @since 2.5
 */
public final class GlazedListsLockHelper {

    private static final Logger LOG = LoggerFactory.getLogger(GlazedListsLockHelper.class);

    private GlazedListsLockHelper() {
        // do nothing
    }

    /**
     * Try to acquire the write lock for the given {@link Lock} using
     * {@link Lock#tryLock()}. Waits for 1 minute to acquire the write lock, if
     * not possible fire an {@link IllegalStateException}.
     *
     * @param writeLock
     *            The write {@link Lock}.
     * @throws IllegalStateException
     *             if the write lock can not be acquired in 1 minute.
     */
    public static void acquireWriteLock(Lock writeLock) {
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        while ((end - start) < 60_000) {
            try {
                boolean success = writeLock.tryLock();

                if (success) {
                    return;
                }

                Thread.sleep(50);
            } catch (InterruptedException e) {
                LOG.debug("thread interrupted while waiting for writeLock", e); //$NON-NLS-1$
            }

            end = System.currentTimeMillis();
        }

        throw new IllegalStateException("Failed to acquire the write lock!"); //$NON-NLS-1$
    }

    /**
     * Performs a write operation on a GlazedLists that is wrapped by acquiring
     * and releasing the write lock.
     *
     * @param lock
     *            The GlazedLists {@link Lock} object to get the write lock
     *            from.
     * @param writeRunnable
     *            The {@link Runnable} that should be executed between obtaining
     *            the write lock and releasing it.
     */
    public static void performWriteOperation(ReadWriteLock lock, Runnable writeRunnable) {
        Lock writeLock = lock.writeLock();
        try {
            acquireWriteLock(writeLock);
            writeRunnable.run();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Performs a write operation on a GlazedLists that is wrapped by acquiring
     * and releasing the write lock.
     *
     * @param lock
     *            The GlazedLists {@link Lock} object to get the write lock
     *            from.
     * @param writeRunnable
     *            The {@link Runnable} that should be executed between obtaining
     *            the write lock and releasing it.
     * @param finallyRunnable
     *            The {@link Runnable} that should be executed after the write
     *            lock is released.
     */
    public static void performWriteOperation(ReadWriteLock lock, Runnable writeRunnable, Runnable finallyRunnable) {
        Lock writeLock = lock.writeLock();
        try {
            acquireWriteLock(writeLock);
            writeRunnable.run();
        } finally {
            writeLock.unlock();
            finallyRunnable.run();
        }
    }

    /**
     * Performs a write operation on a GlazedLists that is wrapped by acquiring
     * and releasing the write lock.
     *
     * @param lock
     *            The GlazedLists {@link Lock} object to get the write lock
     *            from.
     * @param writeSupplier
     *            The {@link Supplier} that should be executed between obtaining
     *            the write lock and releasing it.
     * @return The result of the {@link Supplier#get()} method.
     */
    public static <T> T performWriteOperation(ReadWriteLock lock, Supplier<T> writeSupplier) {
        Lock writeLock = lock.writeLock();
        try {
            acquireWriteLock(writeLock);
            return writeSupplier.get();
        } finally {
            writeLock.unlock();
        }
    }
}
