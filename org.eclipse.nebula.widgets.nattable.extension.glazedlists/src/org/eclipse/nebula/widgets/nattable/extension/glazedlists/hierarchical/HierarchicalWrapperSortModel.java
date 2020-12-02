/*****************************************************************************
 * Copyright (c) 2018, 2020 Dirk Fauth.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.hierarchical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.NullComparator;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalWrapper;
import org.eclipse.nebula.widgets.nattable.hierarchical.HierarchicalWrapperComparator;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;

import ca.odell.glazedlists.SortedList;

/**
 * {@link ISortModel} that is used to support sorting of
 * {@link HierarchicalWrapper} in a {@link SortedList}. Internally uses the
 * {@link HierarchicalWrapperComparator} to keep the tree structure while
 * supporting dynamic column based sorting.
 *
 * @since 1.6
 */
public class HierarchicalWrapperSortModel implements ISortModel {

    private SortedList<HierarchicalWrapper> sortedList;
    private IColumnAccessor<HierarchicalWrapper> columnAccessor;
    private Map<Integer, List<Integer>> levelIndexMapping;
    private DataLayer columnHeaderDataLayer;
    private ConfigRegistry configRegistry;

    private Map<Integer, SortDirectionEnum> sortingState = new LinkedHashMap<>();

    /**
     *
     * @param sortedList
     *            The {@link SortedList} that should be sorted.
     * @param columnAccessor
     *            The {@link IColumnAccessor} to access the data value for a
     *            specific column.
     * @param levelIndexMapping
     *            The mapping from hierarchical level to column indexes for that
     *            level.
     * @param columnHeaderDataLayer
     *            The {@link DataLayer} of the column header, needed to retrieve
     *            the configured {@link Comparator}.
     * @param configRegistry
     *            The {@link ConfigRegistry} needed to evaluate the configured
     *            {@link Comparator}.
     */
    public HierarchicalWrapperSortModel(
            SortedList<HierarchicalWrapper> sortedList,
            IColumnAccessor<HierarchicalWrapper> columnAccessor,
            Map<Integer, List<Integer>> levelIndexMapping,
            DataLayer columnHeaderDataLayer,
            ConfigRegistry configRegistry) {

        this.sortedList = sortedList;
        this.columnAccessor = columnAccessor;
        this.columnHeaderDataLayer = columnHeaderDataLayer;
        this.levelIndexMapping = levelIndexMapping;
        this.configRegistry = configRegistry;
    }

    @Override
    public List<Integer> getSortedColumnIndexes() {
        return new ArrayList<>(this.sortingState.keySet());
    }

    @Override
    public boolean isColumnIndexSorted(int columnIndex) {
        return this.sortingState.containsKey(columnIndex);
    }

    @Override
    public SortDirectionEnum getSortDirection(int columnIndex) {
        SortDirectionEnum sort = this.sortingState.get(columnIndex);
        return sort != null ? sort : SortDirectionEnum.NONE;
    }

    @Override
    public int getSortOrder(int columnIndex) {
        int index = getSortedColumnIndexes().indexOf(columnIndex);
        return index >= 0 ? index : 0;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<Comparator> getComparatorsForColumnIndex(int columnIndex) {
        SortDirectionEnum sort = this.sortingState.get(columnIndex);
        // we only support one comparator per column
        return sort != null ? Arrays.asList(getColumnComparator(columnIndex)) : Collections.emptyList();
    }

    @Override
    public Comparator<?> getColumnComparator(int columnIndex) {
        ILayerCell cell = this.columnHeaderDataLayer.getCellByPosition(columnIndex, 0);
        if (cell == null) {
            return null;
        }
        Comparator<?> comparator = this.configRegistry.getConfigAttribute(
                SortConfigAttributes.SORT_COMPARATOR,
                cell.getDisplayMode(),
                cell.getConfigLabels());

        return (comparator instanceof NullComparator) ? null : comparator;
    }

    @Override
    public void sort(int columnIndex, SortDirectionEnum sortDirection, boolean accumulate) {
        if (columnIndex >= 0) {
            if (!accumulate) {
                clear();
            }

            switch (sortDirection) {
                case NONE:
                    // remove
                    this.sortingState.remove(columnIndex);
                    break;
                case ASC:
                case DESC:
                    this.sortingState.remove(columnIndex);
                    this.sortingState.put(columnIndex, sortDirection);
                    break;
                default:
                    break;
            }

            // perform the sorting
            this.sortedList.getReadWriteLock().writeLock().lock();
            try {
                if (this.sortingState.isEmpty()) {
                    // if we do not have a sorting state, we disable sorting
                    this.sortedList.setComparator(null);
                } else {
                    // we have some sorting state, so we trigger a re-sort
                    this.sortedList.setComparator(new HierarchicalWrapperComparator(this.columnAccessor, this.levelIndexMapping, this));
                }
            } finally {
                this.sortedList.getReadWriteLock().writeLock().unlock();
            }
        }
    }

    @Override
    public void clear() {
        this.sortingState.clear();
    }

}
