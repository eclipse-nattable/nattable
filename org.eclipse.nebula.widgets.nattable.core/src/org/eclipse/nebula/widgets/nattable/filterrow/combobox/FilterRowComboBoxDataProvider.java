/*******************************************************************************
 * Copyright (c) 2013, 2024 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *    Dirk Fauth <dirk.fauth@googlemail.com> - Bug 454505
 *******************************************************************************/
package org.eclipse.nebula.widgets.nattable.filterrow.combobox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.NullComparator;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.edit.EditConstants;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.edit.editor.IComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.event.DataUpdateEvent;
import org.eclipse.nebula.widgets.nattable.filterrow.event.FilterAppliedEvent;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.IVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;

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
public class FilterRowComboBoxDataProvider<T> implements IComboBoxDataProvider, ILayerListener, ILayerCommandHandler<UpdateDataCommand> {

    /**
     * The base collection used to collect the distinct values from. This need
     * to be a collection that is not filtered, otherwise after modifications
     * the content of the filter row combo boxes will only contain the current
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
    private final Map<Integer, List<?>> valueCache = new HashMap<>();
    /**
     * The local cache for all values available in the filter row combobox.
     * Needed if the {@link FilterRowComboBoxDataProvider} is configured to show
     * only the current visible items in the body and not always all items.
     * Otherwise this collection references the
     * {@link FilterRowComboBoxDataProvider#valueCache}.
     */
    private Map<Integer, List<?>> allValueCache = this.valueCache;
    /**
     * List of listeners that get informed if the value cache gets updated.
     */
    private List<IFilterRowComboUpdateListener> cacheUpdateListener = new ArrayList<>();
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
     * Flag for enabling/disabling firing a {@link FilterRowComboUpdateEvent} if
     * the filter value cache is updated. Important for use cases where the
     * cache is not build up yet and the filter is restored from properties,
     * e.g. on opening a table with stored properties.
     *
     * @since 1.6
     */
    private boolean updateEventsEnabled = true;

    /**
     * Lock used for accessing the value cache.
     *
     * @since 1.6
     */
    private final ReadWriteLock valueCacheLock = new ReentrantReadWriteLock();

    /**
     * Lock used for accessing the all value cache.
     *
     * @since 2.1
     */
    private ReadWriteLock allValueCacheLock = this.valueCacheLock;

    /**
     * Flag to configure if <code>null</code> and empty values should be used as
     * different values or as a general "empty" value in the values list.
     *
     * @since 2.1
     */
    private boolean distinctNullAndEmpty = false;

    /**
     * The collection that contains filtered body data. Used to collect the
     * distinct values based on the current filtered content. Set via
     * {@link FilterRowComboBoxDataProvider#setFilterCollection(Collection, ILayer)}.
     * Can be <code>null</code>.
     *
     * @since 2.1
     */
    private Collection<T> filterCollection;
    /**
     * A layer in the column header region in which the filter row is included.
     * Needed to handle the {@link FilterAppliedEvent}. Set via
     * {@link FilterRowComboBoxDataProvider#setFilterCollection(Collection, ILayer)}.
     * Can be <code>null</code>.
     *
     * @since 2.1
     */
    private ILayer columnHeaderLayer;

    /**
     * A layer in the body region. Usually the DataLayer or a layer that is
     * responsible for list event handling. Needed to register ourself as
     * listener for data changes.
     *
     * @since 2.1
     */
    private ILayer bodyLayer;

    /**
     * The state of the collection at the time a filter was applied for the
     * {@link FilterRowComboBoxDataProvider#lastAppliedFilterColumn}.
     *
     * @since 2.1
     */
    private Collection<T> previousAppliedFilterCollection;
    /**
     * The column index of the column with a filter combobox editor that was
     * filtered last.
     *
     * @since 2.1
     */
    private int lastAppliedFilterColumn = -1;

    /**
     * The DataLayer of the column header region. Needed to be able to get the
     * cell which is needed to get the comparator that should be used to sort
     * the filter collection.
     *
     * @since 2.3
     */
    private ILayer columnHeaderDataLayer;

    /**
     * The {@link IConfigRegistry} used to retrieve the comparator to be used to
     * sort the filter collection. Only has an effect if also
     * {@link #columnHeaderLayer} is set.
     *
     * @since 2.3
     */
    private IConfigRegistry configRegistry;

    /**
     * {@link Predicate} that allows to filter the values that are contained in
     * the filter combo box. By default filters nothing.
     *
     * @since 2.3
     */
    private Predicate<T> contentFilter = t -> true;

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
            this.valueCacheLock.writeLock().lock();
            try {
                buildValueCache();
            } finally {
                this.valueCacheLock.writeLock().unlock();
            }
        }

        this.bodyLayer = bodyLayer;
        this.bodyLayer.addLayerListener(this);
    }

    @Override
    public List<?> getValues(int columnIndex, int rowIndex) {
        if (this.previousAppliedFilterCollection != null && columnIndex == this.lastAppliedFilterColumn) {
            return getValues(this.previousAppliedFilterCollection, columnIndex, this.valueCache, this.valueCacheLock);
        } else if (this.getFilterCollection() != null && columnIndex != this.lastAppliedFilterColumn) {
            return getValues(this.getFilterCollection(), columnIndex, this.valueCache, this.valueCacheLock);
        }
        return getAllValues(columnIndex);
    }

    /**
     * Returns the collection of all distinct values for the given column. Will
     * use the non-filtered base collection, so it returns always all values,
     * even if they are not visible.
     *
     * @param columnIndex
     *            The column index for which the values are requested.
     * @return List of all distinct values for the given column.
     *
     * @since 2.1
     */
    public List<?> getAllValues(int columnIndex) {
        return getValues(this.baseCollection, columnIndex, this.allValueCache, this.allValueCacheLock);
    }

    /**
     *
     * @param collection
     *            The collection out of which the distinct values should be
     *            collected.
     * @param columnIndex
     *            The column index for which the values are requested.
     * @return List of all distinct values that are contained in the given
     *         collection for the given column.
     *
     * @since 2.1
     */
    protected List<?> getValues(Collection<T> collection, int columnIndex, Map<Integer, List<?>> cache, ReadWriteLock cacheLock) {
        if (this.cachingEnabled) {

            cacheLock.readLock().lock();
            List<?> result = null;
            try {
                result = cache.get(columnIndex);
            } finally {
                cacheLock.readLock().unlock();
            }

            if (result == null) {
                cacheLock.writeLock().lock();
                try {
                    result = collectValues(collection, columnIndex);
                    cache.put(columnIndex, result);
                } finally {
                    cacheLock.writeLock().unlock();
                }

                if (isUpdateEventsEnabled()
                        && (this.valueCache == this.allValueCache || cache != this.allValueCache)) {
                    fireCacheUpdateEvent(buildUpdateEvent(columnIndex, null, result));
                }
            }

            return result;
        } else {
            return collectValues(collection, columnIndex);
        }
    }

    /**
     * Builds the local value cache for all columns.
     */
    protected void buildValueCache() {
        buildValueCache(this.allValueCache);
    }

    private void buildValueCache(Map<Integer, List<?>> cache) {
        for (int i = 0; i < this.columnAccessor.getColumnCount(); i++) {
            cache.put(i, collectValues(i));
        }
    }

    /**
     * This method returns the column indexes of the columns for which values
     * were cached. Usually it will return all column indexes that are available
     * in the table.
     *
     * @return The column indexes of the columns for which values were cached.
     */
    public Collection<Integer> getCachedColumnIndexes() {
        this.valueCacheLock.readLock().lock();
        try {
            return this.valueCache.keySet();
        } finally {
            this.valueCacheLock.readLock().unlock();
        }
    }

    /**
     * Iterates over all rows of the base collection and collects the distinct
     * values for the given column index.
     *
     * @param columnIndex
     *            The column index for which the values should be collected.
     * @return List of all distinct values that are contained in the base
     *         collection for the given column.
     */
    protected List<?> collectValues(int columnIndex) {
        return collectValues(this.baseCollection, columnIndex);
    }

    /**
     * Collects the distinct values for the given column index. Determines the
     * collection to iterate over based on the information whether a filter list
     * is configured and a filter is applied.
     *
     * @param columnIndex
     *            The column index for which the values should be collected.
     * @return List of all distinct values that are contained in the determined
     *         collection for the given column.
     *
     * @since 2.1
     */
    protected List<?> collectValuesForColumn(int columnIndex) {
        if (this.previousAppliedFilterCollection != null && columnIndex == this.lastAppliedFilterColumn) {
            return collectValues(this.previousAppliedFilterCollection, columnIndex);
        } else if (this.getFilterCollection() != null && columnIndex != this.lastAppliedFilterColumn) {
            return collectValues(this.getFilterCollection(), columnIndex);
        }
        return collectValues(columnIndex);
    }

    /**
     * Iterates over all rows of the given collection and collects the distinct
     * values for the given column index.
     *
     * @param collection
     *            The collection out of which the distinct values should be
     *            collected.
     * @param columnIndex
     *            The column index for which the values should be collected
     * @return List of all distinct values that are contained in the given
     *         collection for the given column.
     *
     * @since 2.1
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected List<?> collectValues(Collection<T> collection, int columnIndex) {
        List result = collection.stream()
                .unordered()
                .parallel()
                .filter(this.contentFilter)
                .map(x -> this.columnAccessor.getDataValue(x, columnIndex))
                .map(x -> {
                    if (isDistinctNullAndEmpty()) {
                        return (x instanceof String && ((String) x).isEmpty()) ? null : x;
                    }
                    return x;
                })
                .distinct()
                .collect(Collectors.toList());

        Object firstNonNull = result.stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        if (firstNonNull instanceof Comparable) {
            result.sort(Comparator.nullsFirst(getColumnComparator(columnIndex)));
        } else {
            // always ensure that null is at the first position
            int index = result.indexOf(null);
            if (index >= 0) {
                result.remove(index);
                result.add(0, null);
            }
        }

        return result;
    }

    @Override
    public void handleLayerEvent(ILayerEvent event) {
        // we only need to perform event handling if caching is enabled
        if (this.cachingEnabled && isEventFromBodyLayer(event)) {
            if (event instanceof DataUpdateEvent) {
                // this is fired for data updates so we need to update the value
                // cache for the updated column
                updateCache(((DataUpdateEvent) event).getColumnPosition());
            } else if (event instanceof IStructuralChangeEvent
                    && ((IStructuralChangeEvent) event).isVerticalStructureChanged()) {
                clearCache(false);
            }
        }

        // The FilterAppliedEvent should only be fired in the column header
        // layer stack, so if setFilterCollection() was not called, those events
        // should never reach here. But in case someone fires
        // FilterAppliedEvents programmatically from the body layer stack, we
        // need to guard the execution to avoid exceptions.
        // Note:
        // The FilterAppliedEvent is handled in a LayerListener instead of a
        // dedicated ILayerEventHandler as there can only be one
        // ILayerEventHandler registered per layer, which could interfere with
        // user code.
        if (event instanceof FilterAppliedEvent && this.filterCollection != null) {
            // only update the last applied filter column if
            // - the editor is a FilterRowComboBoxCellEditor
            // - the filter was not cleared
            FilterAppliedEvent filterEvent = (FilterAppliedEvent) event;

            // if caching is enabled and the filter was cleared for a single
            // column, we update the valueCache for that column to ensure that
            // the filter changed check operates on the correct values and the
            // correct values are shown in the combo
            if (this.cachingEnabled && filterEvent.isCleared() && filterEvent.getColumnIndex() > -1) {
                this.valueCache.put(filterEvent.getColumnIndex(), collectValues(this.getFilterCollection(), filterEvent.getColumnIndex()));
            }

            // only update the collection references if the value was changed
            boolean filterChanged = isFilterChanged(filterEvent.getColumnIndex(), filterEvent.getOldValue(), filterEvent.getNewValue());

            if ((filterEvent.isCleared() && filterEvent.getColumnIndex() != this.lastAppliedFilterColumn)
                    || (filterEvent.isFilterComboEditor() && (filterEvent.isCleared() || filterEvent.getColumnIndex() == -1))
                    || (filterEvent.getColumnIndex() == -1 && filterEvent.getOldValue() == null && filterEvent.getNewValue() == null)) {
                setLastFilter(-1, null);
            }

            // the cache needs to be cleaned whenever a filter is applied,
            // because the content in the comboboxes depends on the visible
            // content for the not last applied filter column

            if (this.cachingEnabled && filterChanged) {
                this.valueCacheLock.writeLock().lock();
                try {
                    int column = filterEvent.getColumnIndex();
                    for (Iterator<Entry<Integer, List<?>>> it = this.valueCache.entrySet().iterator(); it.hasNext();) {
                        Entry<Integer, List<?>> entry = it.next();
                        if (entry.getKey() != column) {
                            this.valueCache.put(entry.getKey(), collectValues(this.getFilterCollection(), entry.getKey()));
                        }
                    }
                } finally {
                    this.valueCacheLock.writeLock().unlock();
                }
            }
        }
    }

    /**
     * Update the cache for the given column index.
     *
     * @param columnIndex
     *            The column index for which the cache should be updated.
     * @since 2.1
     */
    protected void updateCache(int columnIndex) {
        updateCache(columnIndex, this.valueCache, this.valueCacheLock);
        if (this.valueCache != this.allValueCache) {
            // we also need to update the all value cache in case it is
            // different from the value cache
            boolean wasEnabled = this.updateEventsEnabled;
            this.updateEventsEnabled = false;
            updateCache(columnIndex, this.allValueCache, this.allValueCacheLock);
            this.updateEventsEnabled = wasEnabled;
        }
    }

    /**
     * Update the cache for the given column index.
     *
     * @param columnIndex
     *            The column index for which the cache should be updated.
     * @param cache
     *            The cache to update (cache for current values or cache for all
     *            values).
     * @param cacheLock
     *            The lock that matches the given cache.
     * @since 2.1
     */
    protected void updateCache(int columnIndex, Map<Integer, List<?>> cache, ReadWriteLock cacheLock) {
        cacheLock.writeLock().lock();
        try {
            List<?> cacheBefore = cache.get(columnIndex);

            if (!this.lazyLoading || cacheBefore != null) {
                cache.put(columnIndex, (cache == this.allValueCache)
                        ? collectValues(columnIndex)
                        : collectValuesForColumn(columnIndex));
            }

            if (isUpdateEventsEnabled()) {
                // get the diff and fire the event
                fireCacheUpdateEvent(buildUpdateEvent(columnIndex, cacheBefore, cache.get(columnIndex)));
            }
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * Clear the cache.
     *
     * @param updateEventsFromAll
     *            <code>true</code> if the filter update events should be fired
     *            for the all value cache, <code>false</code> if the events
     *            should be fired for the value cache.
     *
     * @since 2.1
     */
    protected void clearCache(boolean updateEventsFromAll) {
        boolean fireUpdateEvents = isUpdateEventsEnabled();
        if (fireUpdateEvents) {
            // if update events are enabled in general, check if the update
            // events should be fired for the value cache or the all value cache
            fireUpdateEvents = (this.valueCache == this.allValueCache)
                    || ((this.valueCache != this.allValueCache) && !updateEventsFromAll);
        }
        if (this.valueCache != this.allValueCache) {
            // we also need to update the all value cache in case it is
            // different from the value cache
            clearCache(this.allValueCache, this.allValueCacheLock, !fireUpdateEvents);
        }

        clearCache(this.valueCache, this.valueCacheLock, fireUpdateEvents);
    }

    /**
     * Clear the cache.
     *
     * @param cache
     *            The cache to clear (cache for current values or cache for all
     *            values).
     * @param cacheLock
     *            The lock that matches the given cache.
     * @param fireUpdateEvents
     *            <code>true</code> if {@link FilterRowComboUpdateEvent}s should
     *            be fired, <code>false</code> if not.
     * @since 2.1
     */
    protected void clearCache(Map<Integer, List<?>> cache, ReadWriteLock cacheLock, boolean fireUpdateEvents) {
        cacheLock.writeLock().lock();
        try {
            // remember the cache before updating
            Map<Integer, List<?>> cacheBefore = new HashMap<>(cache);

            // perform a refresh of the whole cache
            cache.clear();
            if (!this.lazyLoading) {
                buildValueCache();
            } else {
                // to determine the diff for the update event
                // the current values need to be collected,
                // otherwise on clear() - addAll() a full reset
                // will be triggered since there are no cached
                // values
                for (Map.Entry<Integer, List<?>> entry : cacheBefore.entrySet()) {
                    cache.put(entry.getKey(), (cache == this.allValueCache)
                            ? collectValues(entry.getKey())
                            : collectValuesForColumn(entry.getKey()));
                }
            }

            if (fireUpdateEvents) {
                FilterRowComboUpdateEvent updateEvent = null;
                // fire event for every column that has cached data
                for (Map.Entry<Integer, List<?>> entry : cacheBefore.entrySet()) {
                    updateEvent = buildUpdateEvent(
                            updateEvent,
                            entry.getKey(),
                            entry.getValue(),
                            cache.get(entry.getKey()));
                }

                // fire event for every column
                fireCacheUpdateEvent(updateEvent);
            }
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * Checks if the given {@link ILayerEvent} was fired from the body layer.
     *
     * @param event
     *            The event to check.
     * @return <code>true</code> if the event was fired from the body layer,
     *         <code>false</code> if not.
     * @since 2.1
     */
    protected boolean isEventFromBodyLayer(ILayerEvent event) {
        if (event instanceof IVisualChangeEvent && ((IVisualChangeEvent) event).getLayer() == this.bodyLayer) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a filter was changed. Also handles
     * {@link EditConstants#SELECT_ALL_ITEMS_VALUE} and a collection of values
     * on testing.
     *
     * @param columnIndex
     *            The column index to check.
     * @param oldValue
     *            The old value.
     * @param newValue
     *            The new value.
     * @return <code>true</code> if a value was changed, <code>false</code> if
     *         not.
     * @since 2.1
     */
    @SuppressWarnings("rawtypes")
    protected boolean isFilterChanged(int columnIndex, Object oldValue, Object newValue) {
        if (columnIndex == -1 && oldValue == null && newValue == null) {
            // global change for non-specific column and no filter data change
            // values
            return true;
        }

        if (EditConstants.SELECT_ALL_ITEMS_VALUE.equals(oldValue) && newValue instanceof Collection) {
            return !ObjectUtils.collectionsEqual((Collection) newValue, getValues(columnIndex, 0));
        } else if (EditConstants.SELECT_ALL_ITEMS_VALUE.equals(newValue) && oldValue instanceof Collection) {
            return !ObjectUtils.collectionsEqual((Collection) oldValue, getValues(columnIndex, 0));
        } else if (oldValue instanceof Collection && newValue instanceof Collection) {
            Collection oldFilter = (Collection) oldValue;
            Collection newFilter = (Collection) newValue;
            return !ObjectUtils.collectionsEqual(oldFilter, newFilter);
        }

        return !((oldValue == null && newValue == null) || ((oldValue != null && newValue != null) && oldValue.equals(newValue)));
    }

    /**
     * Creates a new {@link FilterRowComboUpdateEvent} for the given column
     * index. Calculates the diffs of the value cache for that column based on
     * the given lists.
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
        return buildUpdateEvent(null, columnIndex, cacheBefore, cacheAfter);
    }

    /**
     * Creates a new {@link FilterRowComboUpdateEvent} or updates the given
     * {@link FilterRowComboUpdateEvent} for the given column index. Calculates
     * the diffs of the value cache for that column based on the given lists.
     *
     * @param event
     *            the {@link FilterRowComboUpdateEvent} to update, or
     *            <code>null</code> if a new instance should be created.
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
     * @since 2.1
     */
    protected FilterRowComboUpdateEvent buildUpdateEvent(FilterRowComboUpdateEvent event, int columnIndex, List<?> cacheBefore, List<?> cacheAfter) {
        Set<Object> addedValues = new HashSet<>();
        Set<Object> removedValues = new HashSet<>();

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
            if (event == null) {
                return new FilterRowComboUpdateEvent(columnIndex, addedValues, removedValues);
            } else {
                event.addUpdate(columnIndex, addedValues, removedValues);
            }
        }

        // nothing has changed so nothing to update
        return event;
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
     *
     * @since 1.6
     */
    public void removeCacheUpdateListener(IFilterRowComboUpdateListener listener) {
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

    /**
     *
     * @return <code>true</code> if a {@link FilterRowComboUpdateEvent} is fired
     *         in case of filter value cache updates, <code>false</code> if not.
     *
     * @since 1.6
     */
    public boolean isUpdateEventsEnabled() {
        return this.updateEventsEnabled;
    }

    /**
     * Enable firing of {@link FilterRowComboUpdateEvent} if the filter value
     * cache is updated.
     *
     * <p>
     * By default it should be enabled to automatically update applied filters
     * in case new values are added, otherwise the row containing the new value
     * will be filtered directly.
     * </p>
     * <p>
     * <b>Note:</b> It is important to disable firing the events in use cases
     * where the cache is not build up yet and the filter is restored from
     * properties, e.g. on opening a table with stored properties.
     * </p>
     *
     * @since 1.6
     */
    public void enableUpdateEvents() {
        this.updateEventsEnabled = true;
    }

    /**
     * Disable firing of {@link FilterRowComboUpdateEvent} if the filter value
     * cache is updated.
     *
     * <p>
     * By default it should be enabled to automatically update applied filters
     * in case new values are added, otherwise the row containing the new value
     * will be filtered directly.
     * </p>
     * <p>
     * <b>Note:</b> It is important to disable firing the events in use cases
     * where the cache is not build up yet and the filter is restored from
     * properties, e.g. on opening a table with stored properties.
     * </p>
     *
     * @since 1.6
     */
    public void disableUpdateEvents() {
        this.updateEventsEnabled = false;
    }

    /**
     *
     * @return The {@link ReadWriteLock} that should be used for locking on
     *         accessing the {@link #valueCache}.
     *
     * @since 1.6
     */
    public ReadWriteLock getValueCacheLock() {
        return this.valueCacheLock;
    }

    /**
     * @return <code>true</code> if <code>null</code> and empty values are
     *         distinct to a single "empty" value, <code>false</code> if they
     *         are treated as separate values.
     *
     * @since 2.1
     */
    public boolean isDistinctNullAndEmpty() {
        return this.distinctNullAndEmpty;
    }

    /**
     * Setting this value effects on how <code>null</code> and empty values are
     * handled on collecting the values. <code>false</code> means
     * <code>null</code> and empty are collected as two separate values,
     * <code>true</code> will distinct them into a single "empty" value.
     *
     * @param distinctNullAndEmpty
     *            <code>true</code> if <code>null</code> and empty values are
     *            distinct to a single "empty" value, <code>false</code> if they
     *            are treated as separate values.
     *
     * @since 2.1
     */
    public void setDistinctNullAndEmpty(boolean distinctNullAndEmpty) {
        this.distinctNullAndEmpty = distinctNullAndEmpty;
    }

    /**
     * @return The collection that contains filtered body data. Can be
     *         <code>null</code>.
     *
     * @since 2.1
     */
    public Collection<T> getFilterCollection() {
        return this.filterCollection;
    }

    /**
     * By setting a filter collection it is possible to only show the distinct
     * values for the current available items in the table.
     *
     * @param filterCollection
     *            The collection that contains filtered body data. Can be
     *            <code>null</code>.
     * @param columnHeaderLayer
     *            A layer in the column header region in which the filter row is
     *            included. Needed to handle the {@link FilterAppliedEvent}.
     * @throws IllegalArgumentException
     *             if one of the parameters is <code>null</code> while the other
     *             isn't
     *
     * @since 2.1
     */
    public void setFilterCollection(Collection<T> filterCollection, ILayer columnHeaderLayer) {
        if (filterCollection != null && columnHeaderLayer != null) {
            // filter collection and column header layer passed, so we configure
            // for dynamic combobox contents that are collected from the filter
            // list
            this.filterCollection = filterCollection;
            this.columnHeaderLayer = columnHeaderLayer;

            this.allValueCache = new HashMap<>();
            this.allValueCacheLock = new ReentrantReadWriteLock();

            this.columnHeaderLayer.addLayerListener(this);
            this.columnHeaderLayer.registerCommandHandler(this);

            boolean wasEnabled = this.updateEventsEnabled;
            this.updateEventsEnabled = false;
            clearCache(true);
            this.updateEventsEnabled = wasEnabled;
        } else if (filterCollection == null && columnHeaderLayer == null) {
            // filter collection and column header layer are null, so we
            // configure for combobox contents that are collected from the base
            // list

            // if a column header layer was set before, we need to unregister
            // ourself from there first
            if (this.columnHeaderLayer != null) {
                this.columnHeaderLayer.removeLayerListener(this);
                this.columnHeaderLayer.unregisterCommandHandler(this.getCommandClass());
            }

            this.filterCollection = null;
            this.columnHeaderLayer = null;

            this.allValueCache = this.valueCache;
            this.allValueCacheLock = this.valueCacheLock;

            setLastFilter(-1, null);

            boolean wasEnabled = this.updateEventsEnabled;
            this.updateEventsEnabled = false;
            clearCache(true);
            this.updateEventsEnabled = wasEnabled;
        } else {
            throw new IllegalArgumentException("not supported to have only one null value"); //$NON-NLS-1$
        }
    }

    /**
     * Remember the column and the previous collection state to be able to
     * restore the filter combobox state of the last used combobox filter.
     *
     * @param columnIndex
     *            The column index of the column that was used for filtering.
     * @param collection
     *            The previous collection state to be able to restore the
     *            combobox contents.
     * @since 2.1
     */
    protected void setLastFilter(int columnIndex, Collection<T> collection) {
        this.lastAppliedFilterColumn = columnIndex;
        this.previousAppliedFilterCollection = collection;
    }

    /**
     *
     * @param targetLayer
     *            The target {@link ILayer}.
     * @param command
     *            The {@link UpdateDataCommand} to process.
     * @return <code>false</code> as this {@link ILayerCommandHandler} does not
     *         consume the command, it only modifies the command to avoid
     *         incorrect processing.
     *
     * @since 2.1
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean doCommand(ILayer targetLayer, UpdateDataCommand command) {
        if (this.columnHeaderLayer != null && command.convertToTargetLayer(targetLayer)) {
            Object newValue = command.getNewValue();
            Collection filterValue = (newValue instanceof Collection) ? (Collection) newValue : null;
            int columnIndex = this.columnHeaderLayer.getColumnIndexByPosition(command.getColumnPosition());

            if (filterValue != null && ObjectUtils.collectionsEqual(filterValue, getValues(columnIndex, 0))) {
                // if all currently visible values are selected, we ensure that
                // in the back all possible values are set to avoid side effects
                // once another filter is cleared
                // check against the currently applied value and avoid an update
                // if the new value is the same
                Object dataValue = this.columnHeaderLayer.getDataValueByPosition(command.getColumnPosition(), this.columnHeaderLayer.getRowCount() - 1);
                if (EditConstants.SELECT_ALL_ITEMS_VALUE.equals(dataValue)
                        || (dataValue instanceof Collection
                                && (ObjectUtils.collectionsEqual(filterValue, (Collection) dataValue)
                                        || ((Collection) dataValue).containsAll(filterValue)))) {
                    return true;
                }
            } else {
                // remember the filter list state to be able to show the
                // previous available entries
                if (this.lastAppliedFilterColumn != columnIndex) {
                    setLastFilter(
                            columnIndex,
                            new ArrayList<>((this.previousAppliedFilterCollection == null && !isFilterApplied()) ? this.baseCollection : this.filterCollection));
                }
            }
        }
        return false;
    }

    /**
     * Simplified check if a filter is applied by comparing the size of the base
     * collection with the size of the filter collection.
     *
     * @return <code>true</code> if a filter collection is set and the size is
     *         less than the size of the base collection, <code>false</code> if
     *         no filter collection is set or the size is equal.
     *
     * @since 2.2
     */
    protected boolean isFilterApplied() {
        if (this.filterCollection != null && this.baseCollection.size() > this.filterCollection.size()) {
            return true;
        }
        return false;
    }

    /**
     * Set the {@link IConfigRegistry} that should be used to retrieve the
     * comparator to sort the filter collection. If one of the parameters is
     * <code>null</code> the filter collection will always be sorted via
     * {@link Comparator#naturalOrder()}.
     *
     * @param columnHeaderDataLayer
     *            The DataLayer of the column header region. Needed to be able
     *            to get the cell which is needed to get the comparator that
     *            should be used to sort the filter collection.
     * @param configRegistry
     *            The {@link IConfigRegistry} of the underlying NatTable.
     * @since 2.3
     */
    public void configureComparator(ILayer columnHeaderDataLayer, IConfigRegistry configRegistry) {
        this.columnHeaderDataLayer = columnHeaderDataLayer;
        this.configRegistry = configRegistry;
    }

    /**
     * Return the {@link Comparator} that should be used to sort the filter
     * collection of the given column.
     *
     * @param columnIndex
     *            The column for which the {@link Comparator} should be
     *            returned.
     * @return The {@link Comparator} that should be used to sort the filter
     *         collection of the given column. The default is
     *         {@link Comparator#naturalOrder()}.
     * @since 2.3
     */
    protected Comparator<?> getColumnComparator(int columnIndex) {
        if (this.configRegistry != null && this.columnHeaderDataLayer != null) {
            ILayerCell cell = this.columnHeaderDataLayer.getCellByPosition(columnIndex, 0);
            if (cell != null) {
                Comparator<?> comparator = this.configRegistry.getConfigAttribute(
                        SortConfigAttributes.SORT_COMPARATOR,
                        cell.getDisplayMode(),
                        cell.getConfigLabels());

                if (comparator != null && !(comparator instanceof NullComparator)) {
                    return comparator;
                }
            }
        }
        return Comparator.naturalOrder();
    }

    /**
     *
     * @param predicate
     *            The {@link Predicate} to define which values should not be
     *            added to the filter combobox. Setting <code>null</code> will
     *            result in no filtering, which is the default.
     * @since 2.3
     */
    public void setContentFilter(Predicate<T> predicate) {
        if (predicate == null) {
            this.contentFilter = t -> true;
        } else {
            this.contentFilter = predicate;
        }
    }

    /**
     *
     * @return The class that is handled by this {@link ILayerCommandHandler}
     *
     * @since 2.1
     */
    @Override
    public Class<UpdateDataCommand> getCommandClass() {
        return UpdateDataCommand.class;
    }

}
