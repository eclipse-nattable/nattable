/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * GUI Update Event Queue
 */
public class UpdateQueue {

    private static final Log log = LogFactory.getLog(UpdateQueue.class);

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private Map<String, Runnable> runnableMap = new HashMap<String, Runnable>();

    private Thread thread = null;

    private boolean stop = false;

    protected long sleep = 100;

    private static UpdateQueue queue = null;

    protected UpdateQueue() {
        // no-op
    }

    public static UpdateQueue getInstance() {
        if (queue == null) {
            queue = new UpdateQueue();
        }
        return queue;
    }

    private Runnable runnable = new Runnable() {

        @Override
        public void run() {
            try {
                while (!UpdateQueue.this.stop) {

                    // Block thread and make sure that we are doing the
                    // latest orders only

                    UpdateQueue.this.lock.writeLock().lock();
                    Runnable[] runnables = UpdateQueue.this.runnableMap.values().toArray(
                            new Runnable[UpdateQueue.this.runnableMap.size()]);
                    UpdateQueue.this.runnableMap.clear();
                    UpdateQueue.this.lock.writeLock().unlock();

                    int len = runnables != null ? runnables.length : 0;

                    for (int i = 0; i < len; i++) {
                        try {
                            runnables[i].run();
                        } catch (Exception e) {
                            log.error(e);
                        }
                    }

                    if (len > 0) {
                        // Allow sleep
                        try {
                            Thread.sleep(UpdateQueue.this.sleep);
                        } catch (Exception e) {
                            log.error(e);
                        }

                    } else {
                        // Sleep when nothing to do
                        synchronized (UpdateQueue.this.thread) {
                            try {
                                UpdateQueue.this.thread.wait();
                            } catch (Exception e) {
                                log.error(e);
                            }
                        }
                    }

                }

            } catch (Exception e) {
                log.error(e);
            }
        }

    };

    /**
     * Add a new runnable to a map along with a unique id<br>
     * The last update runnable of an id will be executed only.
     *
     * @param id
     * @param runnable
     */
    public void addRunnable(String id, Runnable runnable) {
        try {
            // Block thread, ensure no one is going to update the vector
            this.lock.writeLock().lock();
            try {
                this.runnableMap.put(id, runnable);
            } finally {
                this.lock.writeLock().unlock();
            }
            runInThread();
        } catch (Exception e) {
            log.error(e);
        }
    }

    // public void addRunnable(Runnable runnable) {
    // // Block thread, ensure no one is going to update the vector
    // lock.writeLock().lock();
    // runnableList.add(runnable);
    // lock.writeLock().unlock();
    // runInThread();
    // }

    private void runInThread() {
        try {
            if (this.thread == null) {
                this.thread = new Thread(this.runnable, "GUI Display Delay Queue " //$NON-NLS-1$
                        + System.nanoTime());
                this.thread.setDaemon(true);
                this.thread.start();
            } else {
                synchronized (this.thread) {
                    this.thread.notify();
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void stopThread() {
        try {
            if (this.thread != null) {
                this.stop = true;
                synchronized (this.thread) {
                    this.thread.notify();
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
    }
}
