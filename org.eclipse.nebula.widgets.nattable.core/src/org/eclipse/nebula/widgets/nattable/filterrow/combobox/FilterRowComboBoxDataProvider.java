/*******************************************************************************
 * Copyright (c) 2013, 2016 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *    Dirk Fauth <dirk.fauth@googlemail.com> - Bug 454505
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.filterrow.combobox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;

/**
 * IComboBoxDataProvider that provides items for a combobox in the filter row.
 * These items are calculated dynamically based on the content contained in the
 * column it is connected to.
 * <p>
 * On creating this IComboBoxDataProvider, the possible values for all columns
 * will be calculated taking the whole data provided by the body IDataProvider
 * into account. Therefore you shouldn't use this one if you show huge datasets
 * at once.
 * <p>
 * As the values are cached in here, this IComboBoxDataProvider registers itself
 * as ILayerListener to the body DataLayer. If values are updated or rows get
 * added/deleted, it will update the cache accordingly.
 *
 * @param <T>
 *            The type of the objects shown within the NatTable. Needed to
 *            access the data columnwise.
 */
public class FilterRowComboBoxDataProvider<T> implements IComboBoxDataProvider, ILayerListener {
    /**
     * The base collection used to collect the unique values from. This need to
     * be a collection that is not filtered, otherwise after modifications the
     * content of the filter row combo boxes will only contain the current
     * visible (not filtered) elements.
     */
    private Collection<T> baseCollection;
    /**
     * The IColumnAccessor to be able to read the values out of the base
     * collection objects.
     */
    private IColumnAccessor<T> columnAccessor;
    /**
     * The local cache for the values to show in the filter row combobox. This
     * is needed because otherwise the calculation of the necessary values would
     * happen everytime the combobox is opened and if a filter is applied using
     * GlazedLists for example, the combobox would only contain the value which
     * is currently used for filtering.
     */
    private final Map<Integer, List<?>> valueCache = new HashMap<Integer, List<?>>();
    /**
     * List of listeners that get informed if the value cache gets updated.
     */
    private List<IFilterRowComboUpdateListener> cacheUpdateListener = new ArrayList<IFilterRowComboUpdateListener>();
    /**
     * Flag to indicate whether the combo box content should be loaded lazily.
     *
     * @since 1.4
     */
    protected final boolean lazyLoading;

    /**
     * Flag for enabling/disabling caching of filter combo box values.
     *
     * @since 1.4
     */
    protected boolean cachingEnabled = true;

    /**
     * @param bodyLayer
     *            A layer in the body region. Usually the DataLayer or a layer
     *            that is responsible for list event handling. Needed to
     *            register ourself as listener for data changes.
     * @param baseCollection
     *            The base collection used to collect the unique values from.
     *            This need to be a collection that is not filtered, otherwise
     *            after modifications the content of the filter row combo boxes
     *            will only contain the current visible (not filtered) elements.
     * @param columnAccessor
     *            The IColumnAccessor to be able to read the values out of the
     *            base collection objects.
     */
    public FilterRowComboBoxDataProvider(
            ILayer bodyLayer, Collection<T> baseCollection, IColumnAccessor<T> columnAccessor) {
        this(bodyLayer, baseCollection, columnAccessor, true);
    }

    /**
     * @param bodyLayer
     *            A layer in the body region. Usually the DataLayer or a layer
     *            that is responsible for list event handling. Needed to
     *            register ourself as listener for data changes.
     * @param baseCollection
     *            The base collection used to collect the unique values from.
     *            This need to be a collection that is not filtered, otherwise
     *            after modifications the content of the filter row combo boxes
     *            will only contain the current visible (not filtered) elements.
     * @param columnAccessor
     *            The IColumnAccessor to be able to read the values out of the
     *            base collection objects.
     * @param lazy
     *            <code>true</code> to configure this
     *            {@link FilterRowComboBoxDataProvider} should load the combobox
     *            values lazily, <code>false</code> to pre-build the value
     *            cache.
     * @since 1.4
     */
    public FilterRowComboBoxDataProvider(
            ILayer bodyLayer,
            Collection<T> baseCollection,
            IColumnAccessor<T> columnAccessor,
            boolean lazy) {
        this.baseCollection = baseCollection;
        this.columnAccessor = columnAccessor;
        this.lazyLoading = lazy;

        if (!this.lazyLoading) {
            // build the cache
            buildValueCache();
        }

        bodyLayer.addLayerListener(this);
    }

    @Override
    public List<?> getValues(int columnIndex, int rowIndex) {
        if (this.cachingEnabled) {
            List<?> result = this.valueCache.get(columnIndex);
            if (result == null) {
                result = collectValues(columnIndex);
                this.valueCache.put(columnIndex, result);
                fireCacheUpdateEvent(buildUpdateEvent(columnIndex, null, result));
            }
            return result;
        } else {
            return collectValues(columnIndex);
        }
    }

    /**
     * Builds the local value cache for all columns.
     */
    protected void buildValueCache() {
        for (int i = 0; i < this.columnAccessor.getColumnCount(); i++) {
            this.valueCache.put(i, collectValues(i));
        }
    }

    /**
     * This method returns the column indexes of the columns for which values
     * was cached. Usually it will return all column indexes that are available
     * in the table.
     *
     * @return The column indexes of the columns for which values was cached.
     */
    public Collection<Integer> getCachedColumnIndexes() {
        return this.valueCache.keySet();
    }

    /**
     * Iterates over all rows of the local body IDataProvider and collects the
     * unique values for the given column index.
     *
     * @param columnIndex
     *            The column index for which the values should be collected
     * @return List of all unique values that are contained in the body
     *         IDataProvider for the given column.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected List<?> collectValues(int columnIndex) {
        Set uniqueValues = new HashSet();

        boolean nullFound = false;
        for (T rowObject : this.baseCollection) {
            Object dataValue = this.columnAccessor.getDataValue(rowObject, columnIndex);
            if (dataValue != null) {
                uniqueValues.add(dataValue);
            } else {
                nullFound = true;
            }
        }

        List result = new ArrayList(uniqueValues);
        if (!result.isEmpty() && result.get(0) instanceof Comparable) {
            Collections.sort(result);
        }

        if (nullFound) {
            result.add(0, null);
        }

        return result;
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        // we only need to perform event handling if caching is enabled
        if (this.cachingEnabled) {
            if (event instanceof CellVisualChangeEvent) {
                // usually this is fired for data updates
                // so we need to update the value cache for the updated column
                int column = ((CellVisualChangeEvent) event).getColumnPosition();

                List<?> cacheBefore = this.valueCache.get(column);

                this.valueCache.put(column, collectValues(column));

                // get the diff and fire the event
                fireCacheUpdateEvent(buildUpdateEvent(column, cacheBefore, this.valueCache.get(column)));
            } else if (event instanceof IStructuralChangeEvent
                    && ((IStructuralChangeEvent) event).isVerticalStructureChanged()) {
                // a new row was added or a row was deleted

                // remember the cache before updating
                Map<Integer, List<?>> cacheBefore = new HashMap<Integer, List<?>>(this.valueCache);

                // perform a refresh of the whole cache
                this.valueCache.clear();
                if (!this.lazyLoading) {
                    buildValueCache();
                }

                // fire events for every column
                for (Map.Entry<Integer, List<?>> entry : cacheBefore.entrySet()) {
                    fireCacheUpdateEvent(buildUpdateEvent(entry.getKey(), entry.getValue(), this.valueCache.get(entry.getKey())));
                }
            }
        }
    }

    /**
     * Creates a FilterRowComboUpdateEvent for the given column index.
     * Calculates the diffs of the value cache for that column based on the
     * given lists.
     *
     * @param columnIndex
     *            The column index for which the value cache was updated.
     * @param cacheBefore
     *            The value cache for the column before the change. Needed to
     *            determine which values where removed by the update.
     * @param cacheAfter
     *            The value cache for the column after the change. Needed to
     *            determine which values where added by the update.
     * @return Event to tell about value cache updates for the given column or
     *         <code>null</code> if nothing has changed.
     */
    protected FilterRowComboUpdateEvent buildUpdateEvent(int columnIndex, List<?> cacheBefore, List<?> cacheAfter) {
        Set<Object> addedValues = new HashSet<Object>();
        Set<Object> removedValues = new HashSet<Object>();

        // find the added values
        if (cacheAfter != null && cacheBefore != null) {
            for (Object after : cacheAfter) {
                if (!cacheBefore.contains(after)) {
                    addedValues.add(after);
                }
            }

            // find the removed values
            for (Object before : cacheBefore) {
                if (!cacheAfter.contains(before)) {
                    removedValues.add(before);
                }
            }
        } else if ((cacheBefore == null || cacheBefore.isEmpty()) && cacheAfter != null) {
            addedValues.addAll(cacheAfter);
        } else if (cacheBefore != null && (cacheAfter == null || cacheAfter.isEmpty())) {
            removedValues.addAll(cacheBefore);
        }

        // only create a new update event if there has something changed
        if (!addedValues.isEmpty() || !removedValues.isEmpty()) {
            return new FilterRowComboUpdateEvent(columnIndex, addedValues, removedValues);
        }

        // nothing has changed so nothing to update
        return null;
    }

    /**
     * Fire the given event to all registered listeners.
     *
     * @param event
     *            The event to handle.
     */
    protected void fireCacheUpdateEvent(FilterRowComboUpdateEvent event) {
        if (event != null) {
            for (IFilterRowComboUpdateListener listener : this.cacheUpdateListener) {
                listener.handleEvent(event);
            }
        }
    }

    /**
     * Adds the given listener to the list of listeners for value cache updates.
     *
     * @param listener
     *            The listener to add.
     */
    public void addCacheUpdateListener(IFilterRowComboUpdateListener listener) {
        this.cacheUpdateListener.add(listener);
    }

    /**
     * Removes the given listener from the list of listeners for value cache
     * updates.
     *
     * @param listener
     *            The listener to remove.
     */
    public void removeCacheUdpateListener(IFilterRowComboUpdateListener listener) {
        this.cacheUpdateListener.remove(listener);
    }

    /**
     * @return The local cache for the values to show in the filter row
     *         combobox. This is needed because otherwise the calculation of the
     *         necessary values would happen everytime the combobox is opened
     *         and if a filter is applied using GlazedLists for example, the
     *         combobox would only contain the value which is currently used for
     *         filtering.
     */
    protected Map<Integer, List<?>> getValueCache() {
        return this.valueCache;
    }

    /**
     *
     * @return <code>true</code> if caching of filterrow combobox values is
     *         enabled, <code>false</code> if the combobox values should be
     *         calculated on request.
     * @since 1.4
     */
    public boolean isCachingEnabled() {
        return this.cachingEnabled;
    }

    /**
     * Enable/disable the caching of filterrow combobox values. By default the
     * caching is enabled.
     * <p>
     * You should disable caching if the base collection that is used to
     * determine the filterrow combobox values changes its contents dynamically,
     * e.g. if the base collection is a GlazedLists FilterList that returns only
     * the current non-filtered items.
     * </p>
     *
     * @param cachingEnabled
     *            <code>true</code> to enable caching of filter row combobox
     *            values, <code>false</code> if the combobox values should be
     *            calculated on request.
     * @since 1.4
     */
    public void setCachingEnabled(boolean cachingEnabled) {
        this.cachingEnabled = cachingEnabled;
    }

    /**
     * Cleanup acquired resources.
     *
     * @since 1.5
     */
    public void dispose() {
        // nothing to do here
    }
}
