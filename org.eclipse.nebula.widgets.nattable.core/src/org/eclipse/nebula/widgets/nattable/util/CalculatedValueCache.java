/*******************************************************************************
 * Copyright (c) 2014, 2016 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *    Roman Flueckiger <roman.flueckiger@mac.com - switched to concurrent hash maps to prevent a concurrency issue
 *    Dirk Fauth <dirk.fauth@googlemail.com> - Bug 459246
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;

/**
 * This class is intended as a value cache that is able to perform calculations
 * in a background thread. By default it is configured for smooth updates, which
 * means that on re-calculation of values, the old values will be returned until
 * the calculation is done.
 * <p>
 * The CalculatedValueCache uses implementations of
 * {@link ICalculatedValueCacheKey} as the key for the value cache. Usually the
 * internal default implementations for column or row position, or the
 * column-row coordinates should fit most of the use cases.
 */
public class CalculatedValueCache implements ICalculatedValueCache {

    /**
     * The ILayer this value cache is connected to. Needed to perform cell
     * update events when background calculation processes are finished.
     */
    private ILayer layer;

    /**
     * ExecutorService that is used to create background threads to process
     * calculations.
     */
    private ExecutorService executor;

    /**
     * Cache that contains the calculated values. Introduced for performance
     * reasons since the calculation could be CPU intensive.
     * <p>
     * This cache will receive updates, e.g. gets cleared on data structure
     * updates, and will be used to determine whether a new calculation is
     * necessary.
     */
    private Map<ICalculatedValueCacheKey, Object> cache = new ConcurrentHashMap<ICalculatedValueCacheKey, Object>();

    /**
     * Cache copy of the calculated values.
     * <p>
     * This cache will be used to return the value to display. If a value was
     * calculated before it will be returned until it is recalculated.
     * <p>
     * Using a cache copy we ensure smooth updates of calculated values as the
     * prior calculated values will be returned and updated after the new
     * calculation has finished instead of switching to the default calculation
     * value on updates.
     */
    private Map<ICalculatedValueCacheKey, Object> cacheCopy = new ConcurrentHashMap<ICalculatedValueCacheKey, Object>();

    /**
     * Flag to specify if the column position should be used as cache key.
     * <p>
     * Can be used together with the row position, so the column/row coordinates
     * will be used as cache key together.
     */
    private final boolean useColumnAsKey;

    /**
     * Flag to specify if the row position should be used as cache key.
     * <p>
     * Can be used together with the column position, so the column/row
     * coordinates will be used as cache key together.
     */
    private final boolean useRowAsKey;

    /**
     * Flag to specify if the updates on re-calculation should be performed
     * smoothly or not. If this value is <code>true</code> the values that were
     * calculated before will be returned until the new value calculation is
     * done. Otherwise <code>null</code> will be returned until the calculation
     * is finished.
     */
    private final boolean smoothUpdates;

    /**
     * Creates a new CalculatedValueCache for the specified layer that performs
     * smooth updates of the calculated values.
     * <p>
     * Setting one or both key flags to <code>true</code> will enable automatic
     * cache key resolution dependent on the configuration. Setting both values
     * to <code>false</code> will leave the developer to use
     * {@link CalculatedValueCache#getCalculatedValue(int, int, ICalculatedValueCacheKey, boolean, ICalculator)}
     * as it is not possible to determine the ICalculatedValueCacheKey
     * automatically.
     *
     * @param layer
     *            The layer to which the CalculatedValueCache is connected.
     * @param useColumnAsKey
     *            Flag to specify if the column position should be used as cache
     *            key.
     * @param useRowAsKey
     *            Flag to specify if the row position should be used as cache
     *            key.
     */
    public CalculatedValueCache(ILayer layer, boolean useColumnAsKey, boolean useRowAsKey) {
        this(layer, useColumnAsKey, useRowAsKey, true);
    }

    /**
     * Creates a new CalculatedValueCache for the specified layer. This
     * constructor additionally allows to specify if the updates of the
     * calculated values should be performed smoothly or not. That means if a
     * value needs to be recalculated, on smooth updating the old value will be
     * returned until the new value is calculated. Non-smooth updates will
     * return <code>null</code> until the re-calculation is done.
     * <p>
     * Setting one or both key flags to <code>true</code> will enable automatic
     * cache key resolution dependent on the configuration. Setting both values
     * to <code>false</code> will leave the developer to use
     * {@link CalculatedValueCache#getCalculatedValue(int, int, ICalculatedValueCacheKey, boolean, ICalculator)}
     * as it is not possible to determine the ICalculatedValueCacheKey
     * automatically.
     *
     * @param layer
     *            The layer to which the CalculatedValueCache is connected.
     * @param useColumnAsKey
     *            Flag to specify if the column position should be used as cache
     *            key.
     * @param useRowAsKey
     *            Flag to specify if the row position should be used as cache
     *            key.
     * @param smoothUpdates
     *            Flag to specify if the update of the calculated values should
     *            be performed smoothly.
     */
    public CalculatedValueCache(ILayer layer, boolean useColumnAsKey, boolean useRowAsKey, boolean smoothUpdates) {
        this.layer = layer;
        this.executor = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors() + 1,
                Runtime.getRuntime().availableProcessors() + 1,
                5000,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());

        ((ThreadPoolExecutor) this.executor).allowCoreThreadTimeOut(true);

        this.useColumnAsKey = useColumnAsKey;
        this.useRowAsKey = useRowAsKey;
        this.smoothUpdates = smoothUpdates;
    }

    @Override
    public Object getCalculatedValue(final int columnPosition, final int rowPosition,
            boolean calculateInBackground, final ICalculator calculator) {

        ICalculatedValueCacheKey key = null;
        if (this.useColumnAsKey && this.useRowAsKey) {
            key = new CoordinateValueCacheKey(columnPosition, rowPosition);
        } else if (this.useColumnAsKey && !this.useRowAsKey) {
            key = new PositionValueCacheKey(columnPosition);
        } else if (!this.useColumnAsKey && this.useRowAsKey) {
            key = new PositionValueCacheKey(rowPosition);
        } else {
            throw new IllegalStateException(
                    "CalculatedValueCacheKey is configured to not use column or row position. " //$NON-NLS-1$
                            + "Use getCalculatedValue() with ICalculatedValueCacheKey parameter instead."); //$NON-NLS-1$
        }

        return getCalculatedValue(columnPosition, rowPosition, key,
                calculateInBackground, calculator);
    }

    @Override
    public Object getCalculatedValue(final int columnPosition, final int rowPosition,
            final ICalculatedValueCacheKey key, boolean calculateInBackground, final ICalculator calculator) {

        Object result = null;

        if (calculateInBackground) {
            final Object cacheValue = this.cache.get(key);
            final Object cacheCopyValue = this.cacheCopy.get(key);

            result = cacheCopyValue;

            // if the calculated value is not the same as the cache value, we
            // need to start the calculation process
            if (cacheCopyValue == null
                    || !cacheValuesEqual(cacheValue, cacheCopyValue)) {

                // if this CalculatedValueCache is not configured for smooth
                // updates, return null instead of the previous calculated value
                if (!this.smoothUpdates) {
                    result = null;
                }

                this.executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Object summaryValue = calculator.executeCalculation();
                        addToCache(key, summaryValue);

                        // only fire an update event if the new calculated value
                        // is different to the value in the cache copy
                        if (!cacheValuesEqual(summaryValue, cacheCopyValue)
                                && CalculatedValueCache.this.layer != null) {
                            CalculatedValueCache.this.layer.fireLayerEvent(new CellVisualChangeEvent(
                                    CalculatedValueCache.this.layer, columnPosition, rowPosition));
                        }
                    }
                });
            }
        } else {
            // Execute the calculation in the same thread to make printing and
            // exporting work
            // Note: this could cause a performance leak and should be used
            // carefully
            result = calculator.executeCalculation();
            addToCache(key, result);
        }

        return result;
    }

    @Override
    public void clearCache() {
        this.cache.clear();
    }

    @Override
    public void killCache() {
        this.cache.clear();
        this.cacheCopy.clear();
    }

    /**
     * Adds the given value to the cache and the cache-copy. This way the new
     * calculated value gets propagated to both cache instances.
     *
     * @param key
     *            The key to which the calculated value belongs to.
     * @param value
     *            The value for the given coordinates to be cached.
     */
    protected void addToCache(ICalculatedValueCacheKey key, Object value) {
        if (value != null) {
            this.cache.put(key, value);
            this.cacheCopy.put(key, value);
        } else {
            this.cache.remove(key);
            this.cacheCopy.remove(key);
        }
    }

    @Override
    public void dispose() {
        this.executor.shutdownNow();
    }

    /**
     * Null-safe equals check.
     *
     * @param value1
     *            The first value.
     * @param value2
     *            The second value.
     * @return <code>true</code> if both values are equal, <code>false</code> if
     *         not.
     */
    private boolean cacheValuesEqual(Object value1, Object value2) {
        return ((value1 == null && value2 == null) || (value1 != null
                && value2 != null && value1.equals(value2)));
    }

    @Override
    public void setLayer(ILayer layer) {
        this.layer = layer;
    }

    /**
     * ICalculatedValueCacheKey that uses either the column or row position as
     * key.
     */
    class PositionValueCacheKey implements ICalculatedValueCacheKey {

        private final int position;

        public PositionValueCacheKey(int position) {
            this.position = position;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + this.position;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PositionValueCacheKey other = (PositionValueCacheKey) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (this.position != other.position)
                return false;
            return true;
        }

        private CalculatedValueCache getOuterType() {
            return CalculatedValueCache.this;
        }
    }

    /**
     * ICalculatedValueCacheKey that uses the column and row position as key.
     */
    class CoordinateValueCacheKey implements ICalculatedValueCacheKey {

        private final int columnPosition;
        private final int rowPosition;

        public CoordinateValueCacheKey(int columnPosition, int rowPosition) {
            this.columnPosition = columnPosition;
            this.rowPosition = rowPosition;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + this.columnPosition;
            result = prime * result + this.rowPosition;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CoordinateValueCacheKey other = (CoordinateValueCacheKey) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            if (this.columnPosition != other.columnPosition)
                return false;
            if (this.rowPosition != other.rowPosition)
                return false;
            return true;
        }

        private CalculatedValueCache getOuterType() {
            return CalculatedValueCache.this;
        }

    }
}