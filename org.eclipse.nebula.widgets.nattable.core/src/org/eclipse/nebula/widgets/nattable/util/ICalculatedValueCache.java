/*******************************************************************************
 * Copyright (c) 2015 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.util;

import org.eclipse.nebula.widgets.nattable.layer.ILayer;

/**
 * Interface for specifying a value cache to support value calculations in a
 * background thread.
 *
 * @see CalculatedValueCache
 * @since 1.3
 */
public interface ICalculatedValueCache {

    /**
     * Returns the calculated value for the specified column and row position.
     * If there is no calculated value for that coordinates in the cache or
     * there is a potentially stale value, the re-calculation of the value is
     * executed.
     * <p>
     * This method tries to use a predefined cache key dependent on the
     * configuration of this CalculatedValueCache.
     *
     * @param columnPosition
     *            The column position of the requested value.
     * @param rowPosition
     *            The row position of the requested value.
     * @param calculateInBackground
     *            Flag to specify whether the value calculation should be
     *            processed in the background or not. Setting this value to
     *            <code>false</code> will cause calculation in the UI thread,
     *            which is usually necessary in case of exporting and printing.
     * @param calculator
     *            The {@link ICalculator} that is used for calculating the
     *            values.
     * @return The value for the given coordinates.
     *
     * @throws IllegalStateException
     *             if this CalculatedValueCache is configured to not use the
     *             column and row position for cache key definition.
     */
    public abstract Object getCalculatedValue(int columnPosition,
            int rowPosition, boolean calculateInBackground,
            ICalculator calculator);

    /**
     * Returns the calculated value for the specified column and row position.
     * If there is no calculated value for that coordinates in the cache or
     * there is a potentially stale value, the re-calculation of the value is
     * executed.
     * <p>
     * This method uses the given ICalculatedValueCacheKey instead of
     * determining the cache key out of the CalculatedValueCache key
     * configuration.
     *
     * @param columnPosition
     *            The column position of the requested value.
     * @param rowPosition
     *            The row position of the requested value.
     * @param key
     *            The key that is used by this CalculatedValueCache.
     * @param calculateInBackground
     *            Flag to specify whether the value calculation should be
     *            processed in the background or not. Setting this value to
     *            <code>false</code> will cause calculation in the UI thread,
     *            which is usually necessary in case of exporting and printing.
     * @param calculator
     *            The {@link ICalculator} that is used for calculating the
     *            values.
     * @return The value for the given coordinates.
     */
    public abstract Object getCalculatedValue(int columnPosition,
            int rowPosition, ICalculatedValueCacheKey key,
            boolean calculateInBackground, ICalculator calculator);

    /**
     * Clear the internal cache. Doing this will result in triggering new
     * calculations. If the values where calculated before, using the cache copy
     * still the already calculated values will be returned until the new
     * calculation is done.
     */
    public abstract void clearCache();

    /**
     * Kills all cached values. The internal cache aswell as the cache copy to
     * support smooth updates of values. This is necessary because on structural
     * changes, e.g. deleting/adding rows, the cache copy would return false
     * values.
     */
    public abstract void killCache();

    /**
     * Cleaning up internal resources like shutting down the ExecutorService.
     */
    public abstract void dispose();

    /**
     * Set the layer that should be used by this CalculatedValueCache to trigger
     * updates after the calculation processing is done. Necessary if the
     * caching is connected to a data provider for example, which is not able to
     * fire events itself.
     *
     * @param layer
     *            The ILayer that should be used to fire the
     *            CellVisualChangeEvent after the background calculation process
     *            is done.
     */
    public abstract void setLayer(ILayer layer);

}