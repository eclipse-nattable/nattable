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
package org.eclipse.nebula.widgets.nattable.group.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;

/**
 * A thread-safe implementation of {@link IRowGroupModel} which is optimised for
 * larger data-sets (it should cope with at least 10k rows spread across 2-300
 * groups).
 *
 * @author Stefan Bolton
 *
 * @param <T>
 */
public class RowGroupModel<T> implements IRowGroupModel<T> {

    // A map of top-level group names-to-RowGroups.
    private final Map<String, IRowGroup<T>> namesToGroups;

    // A performance cache of rows-to-row group nodes. Kept directly in sync
    // with the model.
    private final Map<T, IRowGroup<T>> rowToGroups;

    // Row group change listeners.
    private final Set<IRowGroupModelListener> listeners;

    // A convenience cache of row indexes to/from row objects. Items are added
    // on demand
    // from the layers as-and-when they are needed.
    private final RowCache<T> rowCache;

    // For big model changes it can be easier to suppress model change
    // notifications and fire a single one.
    private boolean suppressNoficiations;

    public RowGroupModel() {
        this.rowToGroups = new ConcurrentHashMap<T, IRowGroup<T>>();
        this.namesToGroups = new ConcurrentHashMap<String, IRowGroup<T>>();
        this.rowCache = new RowCache<T>();
        this.listeners = new HashSet<IRowGroupModelListener>();
        this.suppressNoficiations = false;
    }

    @Override
    public T getRowFromIndexCache(final int rowIndex) {
        return this.rowCache.getRowFromIndexCache(rowIndex);
    }

    @Override
    public int getIndexFromRowCache(final T row) {
        return this.rowCache.getIndexFromRowCache(row);
    };

    @Override
    public void invalidateIndexCache() {
        this.rowCache.invalidateIndexCache();
    }

    @Override
    public void setDataProvider(IRowDataProvider<T> dataProvider) {
        this.rowCache.setDataProvider(dataProvider);
    }

    @Override
    public IRowDataProvider<T> getDataProvider() {
        return this.rowCache.getDataProvider();
    }

    /**
     * <p>
     * Notify any {@link IRowGroupModelListener}s that something in the model
     * has changed.
     * </p>
     */
    @Override
    public void notifyListeners() {
        invalidateIndexCache();

        if (!this.suppressNoficiations) {
            for (IRowGroupModelListener listener : this.listeners) {
                listener.rowGroupModelChanged();
            }
        }
    }

    /**
     * Set to true to stop model change notifications.
     *
     * @param suppressNoficiations
     */
    public void setSuppressNoficiations(boolean suppressNoficiations) {
        this.suppressNoficiations = suppressNoficiations;
    }

    public boolean isSuppressNoficiations() {
        return this.suppressNoficiations;
    }

    void addMemberRow(final T row, final RowGroup<T> rowGroup) {
        this.rowToGroups.put(row, rowGroup);
    }

    void removeMemberRow(final T row) {
        this.rowToGroups.remove(row);
    }

    @Override
    public void addRowGroups(final List<IRowGroup<T>> rowGroups) {

        // Add the group into the model now.
        for (IRowGroup<T> rowGroup : rowGroups) {
            this.namesToGroups.put(rowGroup.getGroupName(), rowGroup);
        }

        notifyListeners();
    }

    @Override
    public boolean addRowGroup(final IRowGroup<T> rowGroup) {

        // Only allow unique names.
        if (this.namesToGroups.containsKey(rowGroup.getGroupName())) {
            return false;
        }

        // Add the group into the model now.
        this.namesToGroups.put(rowGroup.getGroupName(), rowGroup);

        notifyListeners();
        return true;
    }

    @Override
    public boolean removeRowGroup(final IRowGroup<T> rowGroup) {

        boolean removed = this.namesToGroups.containsKey(rowGroup.getGroupName());

        if (removed) {
            // Remove the group itself now.
            this.namesToGroups.remove(rowGroup.getGroupName());
            notifyListeners();
        }

        return removed;
    }

    @Override
    public List<IRowGroup<T>> getRowGroups() {
        return Collections.unmodifiableList(new ArrayList<IRowGroup<T>>(
                this.namesToGroups.values()));
    }

    @Override
    public IRowGroup<T> getRowGroupForName(final String groupName) {
        return this.namesToGroups.get(groupName);
    }

    @Override
    public IRowGroup<T> getRowGroupForRow(T row) {

        if (this.rowToGroups.containsKey(row)) {
            return getUltimateParent(this.rowToGroups.get(row));
        }

        return null;
    }

    private IRowGroup<T> getUltimateParent(IRowGroup<T> group) {
        return (group.getParentGroup() == null ? group
                : getUltimateParent(group.getParentGroup()));
    }

    @Override
    public boolean isEmpty() {
        return this.namesToGroups.isEmpty();
    }

    @Override
    public void clear() {
        this.namesToGroups.clear();
        this.rowToGroups.clear();
        this.rowCache.invalidateIndexCache();
        notifyListeners();
    }

    @Override
    public void registerRowGroupModelListener(
            final IRowGroupModelListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void unregisterRowGroupModelListener(
            final IRowGroupModelListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void saveState(String prefix, Properties properties) {}

    @Override
    public void loadState(String prefix, Properties properties) {}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ===== Row Group Model ==== \n"); //$NON-NLS-1$

        synchronized (this.namesToGroups) {
            for (IRowGroup<T> rowGroup : this.namesToGroups.values()) {
                sb.append(((RowGroup<T>) rowGroup).toString());
            }
        }

        return sb.toString();
    }

    private class RowCache<E> {

        private IRowDataProvider<E> dataProvider;

        private final Map<Integer, E> indexesToRows;
        private final Map<E, Integer> rowsToIndexes;

        public RowCache() {
            this.indexesToRows = new LinkedHashMap<Integer, E>();
            this.rowsToIndexes = new LinkedHashMap<E, Integer>();
        }

        public IRowDataProvider<E> getDataProvider() {
            return this.dataProvider;
        }

        public void setDataProvider(IRowDataProvider<E> dataProvider) {
            this.dataProvider = dataProvider;
        }

        public E getRowFromIndexCache(final int rowIndex) {
            if (this.indexesToRows.containsKey(rowIndex)) {
                return this.indexesToRows.get(rowIndex);
            }

            final E row = this.dataProvider.getRowObject(rowIndex);

            // If the row we want to use is already in use, then clear the
            // cache.
            if (this.rowsToIndexes.containsKey(row)) {
                invalidateIndexCache();
            }

            this.rowsToIndexes.put(row, rowIndex);
            this.indexesToRows.put(rowIndex, row);
            return row;
        }

        public int getIndexFromRowCache(final E row) {
            if (this.rowsToIndexes.containsKey(row)) {
                return this.rowsToIndexes.get(row);
            }

            final int rowIndex = this.dataProvider.indexOfRowObject(row);

            // If the index we want to use is already in use, then clear the
            // cache.
            if (this.indexesToRows.containsKey(rowIndex)) {
                invalidateIndexCache();
            }

            this.indexesToRows.put(rowIndex, row);
            this.rowsToIndexes.put(row, rowIndex);
            return rowIndex;
        }

        public void invalidateIndexCache() {
            this.indexesToRows.clear();
            this.rowsToIndexes.clear();
        }
    }
}
