/*******************************************************************************
 * Copyright (c) 2014, 2015 Dirk Fauth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 459422
 *     Evan O'Connell <oconn.e@gmail.com> - Bug 460640
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy.summary.IGroupBySummaryProvider;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;

import ca.odell.glazedlists.TreeList;

/**
 * {@link Comparator} that is used to sort the {@link TreeList} based on the
 * groupBy information. Necessary for building the tree structure correctly.
 *
 * @param <T>
 *            The type of the base objects carried in the TreeList
 *
 * @see GroupByTreeFormat
 */
public class GroupByComparator<T> implements IGroupByComparator<T> {

    protected final GroupByModel groupByModel;
    protected final IColumnAccessor<T> columnAccessor;

    protected ISortModel sortModel;
    protected IUniqueIndexLayer treeLayer;

    private GroupByDataLayer<T> dataLayer;

    /**
     * Cache that is used to increase the performance on sorting. The
     * information whether a column is a summary column is only retrieved once
     * and not calculated everytime.
     */
    protected Map<Integer, Boolean> summaryColumnCache = new HashMap<Integer, Boolean>();

    /**
     * Cache that is used to increase the performance on sorting by summary
     * values. Necessary because the summary value is not carried by the
     * {@link GroupByObject} but retrieved from the {@link GroupByDataLayer}.
     * Since the retrieval is done via row index rather than the
     * {@link GroupByObject}, the row index needs to be calculated in order to
     * get the correct values. This is done via {@link List#indexOf(Object)}
     * which is quite time consuming. So this cache is used to reduce the amount
     * of indexOf calls drastically.
     * <p>
     * As this cache is only used to increase the performance on sorting, the
     * life time of this cache is reduced to a sort operation. It therefore gets
     * cleared on every structural change.
     * </p>
     */
    protected Map<GroupByObject, GroupByObjectValueCache> groupByObjectComparatorCache =
            new HashMap<GroupByObject, GroupByObjectValueCache>();

    /**
     *
     * @param groupByModel
     *            The {@link GroupByModel} necessary to retrieve information
     *            about the current groupBy state.
     * @param columnAccessor
     *            The {@link IColumnAccessor} necessary to retrieve the column
     *            values of elements.
     */
    public GroupByComparator(GroupByModel groupByModel, IColumnAccessor<T> columnAccessor) {
        this.groupByModel = groupByModel;
        this.columnAccessor = columnAccessor;
    }

    /**
     *
     * @param groupByModel
     *            The {@link GroupByModel} necessary to retrieve information
     *            about the current groupBy state.
     * @param columnAccessor
     *            The {@link IColumnAccessor} necessary to retrieve the column
     *            values of elements.
     * @param dataLayer
     *            The {@link GroupByDataLayer} that should be used to retrieve
     *            groupBy summary values for sorting the tree structure. Can be
     *            <code>null</code> to avoid retrieving and inspecting summary
     *            values on sorting.
     */
    public GroupByComparator(GroupByModel groupByModel, IColumnAccessor<T> columnAccessor, GroupByDataLayer<T> dataLayer) {
        this.groupByModel = groupByModel;
        this.columnAccessor = columnAccessor;
        this.dataLayer = dataLayer;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public int compare(Object o1, Object o2) {
        if (o1 == null) {
            if (o2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (o2 == null) {
            return 1;
        } else {
            int result = 0;
            for (int columnIndex : this.groupByModel.getGroupByColumnIndexes()) {
                if (o1 instanceof GroupByObject && o2 instanceof GroupByObject) {
                    // handle GroupByObject comparison
                    GroupByObject g1 = (GroupByObject) o1;
                    GroupByObject g2 = (GroupByObject) o2;

                    // get column index for groupBy value
                    // we assume the descriptor is an ordered map, therefore we
                    // choose the last element in the map
                    int groupByIndex = columnIndex;
                    for (Map.Entry<Integer, Object> entry : g1.getDescriptor().entrySet()) {
                        groupByIndex = entry.getKey();
                    }

                    Comparator comparator = getComparator(groupByIndex);
                    result = g1.getDescriptor().size() - g2.getDescriptor().size();
                    if (result == 0) {
                        if (this.sortModel != null
                                && this.sortModel.getSortedColumnIndexes() != null
                                && !this.sortModel.getSortedColumnIndexes().isEmpty()) {

                            for (int sortColumnIndex : this.sortModel.getSortedColumnIndexes()) {

                                Boolean summaryColumn = isSummaryColumn(g1, sortColumnIndex);
                                if (summaryColumn == null) {
                                    // in case we were not able to retrieve the
                                    // information for the one GroupByObject we
                                    // try to find the information the other
                                    // GroupByObject
                                    summaryColumn = isSummaryColumn(g2, sortColumnIndex);
                                }
                                if (summaryColumn != null && summaryColumn) {
                                    // compare GroupByObjects by summary
                                    // value
                                    Object sumValue1 = getSummaryValueFromCache(g1, sortColumnIndex);
                                    Object sumValue2 = getSummaryValueFromCache(g2, sortColumnIndex);
                                    result = getComparator(sortColumnIndex).compare(sumValue1, sumValue2);
                                }

                                if (result == 0) {
                                    result = comparator.compare(g1.getValue(), g2.getValue());
                                }

                                if ((isTreeColumn(sortColumnIndex) || (summaryColumn != null && summaryColumn))
                                        && this.sortModel.getSortDirection(sortColumnIndex).equals(SortDirectionEnum.DESC)) {

                                    result = result * -1;
                                }
                            }

                        } else {
                            result = comparator.compare(g1.getValue(), g2.getValue());
                        }

                        return result;
                    }
                } else if (o1 instanceof GroupByObject
                        && !(o2 instanceof GroupByObject)) {
                    result = 1;
                } else if (!(o1 instanceof GroupByObject)
                        && o2 instanceof GroupByObject) {
                    result = -1;
                } else {
                    // both values are not a GroupByObject so we need to sort by
                    // value to ensure the correct ordering for the tree
                    // structure
                    Object value1 = this.columnAccessor.getDataValue((T) o1, columnIndex);
                    Object value2 = this.columnAccessor.getDataValue((T) o2, columnIndex);

                    result = getComparator(columnIndex).compare(value1, value2);
                }

                if (result != 0) {
                    return result;
                }
            }
        }

        return 0;
    }

    /**
     *
     * @param columnIndex
     *            The column index of the column that should be checked.
     * @return <code>true</code> if the column at the given index is the tree
     *         column, <code>false</code> if not or if no treeLayer reference is
     *         set to this {@link GroupByComparator}
     */
    protected boolean isTreeColumn(int columnIndex) {
        if (this.treeLayer != null) {
            int columnPosition = this.treeLayer.getColumnPositionByIndex(columnIndex);
            ILayerCell cell = this.treeLayer.getCellByPosition(columnPosition, 0);
            if (cell != null) {
                return cell.getConfigLabels().hasLabel(TreeLayer.TREE_COLUMN_CELL);
            }
        }
        // there is no layer set, so we can not determine which column is the
        // tree column and therefore no column is treated that way
        return false;
    }

    /**
     *
     * @param columnIndex
     *            The column index of the column for which the
     *            {@link Comparator} is requested.
     * @return The {@link Comparator} that should be used to compare the values
     *         for elements in the given column. Returns the
     *         {@link DefaultComparator} in case there is no {@link ISortModel}
     *         configured for this {@link GroupByComparator} or no
     *         {@link Comparator} found for the given column.
     */
    @SuppressWarnings("rawtypes")
    protected Comparator getComparator(int columnIndex) {
        Comparator result = null;

        if (this.sortModel != null) {
            result = this.sortModel.getColumnComparator(columnIndex);
        }

        if (result == null) {
            result = DefaultComparator.getInstance();
        }

        return result;
    }

    @Override
    public ISortModel getSortModel() {
        return this.sortModel;
    }

    @Override
    public void setSortModel(ISortModel sortModel) {
        this.sortModel = sortModel;
    }

    @Override
    public void setTreeLayer(IUniqueIndexLayer treeLayer) {
        this.treeLayer = treeLayer;
    }

    @Override
    public void setDataLayer(GroupByDataLayer<T> dataLayer) {
        this.dataLayer = dataLayer;
    }

    @Override
    public void clearCache() {
        this.summaryColumnCache.clear();
        this.groupByObjectComparatorCache.clear();
    }

    /**
     *
     * @param groupBy
     *            The {@link GroupByObject} for which the cache information is
     *            requested. Needed to retrieve the information if it is not yet
     *            cached.
     * @param columnIndex
     *            The column for which the cache information is requested.
     * @return <code>true</code> if the given column is a summary column,
     *         <code>false</code> if not
     */
    protected Boolean isSummaryColumn(GroupByObject groupBy, int columnIndex) {
        if (!this.summaryColumnCache.containsKey(columnIndex)) {
            // build cache information
            // calling the get method will retrieve and cache the necessary
            // information
            getSummaryValueFromCache(groupBy, columnIndex);
        }
        return this.summaryColumnCache.get(columnIndex);
    }

    /**
     * Returns the cached summary value for the given {@link GroupByObject} and
     * column index. If there is no cache information yet, it will be build.
     *
     * @param groupBy
     *            The {@link GroupByObject} for which the cache information is
     *            requested.
     * @param columnIndex
     *            The column for which the cache information is requested.
     * @return The summary value for the given {@link GroupByObject} and column
     *         index or <code>null</code> in case there is no
     *         {@link GroupByDataLayer} configured in for this
     *         {@link GroupByComparator}.
     */
    protected Object getSummaryValueFromCache(GroupByObject groupBy, int columnIndex) {
        Object columnCache = null;

        // it is only possible to retrieve the summary values if the
        // GroupByDataLayer is set and therefore we only have a cache if it is
        // set
        if (this.dataLayer != null) {

            // check if a cache object is already there
            GroupByObjectValueCache cache = this.groupByObjectComparatorCache.get(groupBy);
            if (cache == null) {
                cache = new GroupByObjectValueCache();
                this.groupByObjectComparatorCache.put(groupBy, cache);
            }

            // check if the cache object already contains information about the
            // column
            columnCache = cache.valueCache.get(columnIndex);
            if (columnCache == null) {

                // This is the performance bottleneck that is the reason for the
                // caching!
                int rowIndex = this.dataLayer.getTreeList().indexOf(groupBy);
                if (rowIndex >= 0) {
                    LabelStack labelStack = this.dataLayer.getConfigLabelsByPosition(columnIndex, rowIndex);
                    IGroupBySummaryProvider<T> provider = this.dataLayer.getGroupBySummaryProvider(labelStack);

                    boolean isSummaryColumn = provider != null;
                    this.summaryColumnCache.put(columnIndex, isSummaryColumn);
                    if (isSummaryColumn) {
                        /*
                         * Special Case: If a summary column is grouped, the
                         * summary value is the same as the GroupByObject value.
                         * In that case the roundtrip using the summary provider
                         * is not necessary and we should improve the
                         * performance if the sorting should be applied for that
                         * column.
                         */

                        // the descriptor map needs to be ordered
                        // (LinkedHashMap) and we need to find the last one in
                        // the order to check if a summary column was used for
                        // grouping
                        Entry<Integer, Object> last = null;
                        for (Entry<Integer, Object> entry : groupBy.getDescriptor().entrySet()) {
                            last = entry;
                        }

                        if (last != null && last.getKey() == columnIndex) {
                            columnCache = groupBy.getValue();
                        } else {
                            columnCache = this.dataLayer.getDataValueByPosition(columnIndex, rowIndex, labelStack, false);
                        }

                        cache.valueCache.put(columnIndex, columnCache);
                    }
                }
            }
        } else {
            this.summaryColumnCache.put(columnIndex, false);
        }

        return columnCache;
    }

    /**
     * Cache object that holds the the summary values for all summary columns of
     * a {@link GroupByObject}.
     */
    class GroupByObjectValueCache {
        Map<Integer, Object> valueCache = new HashMap<Integer, Object>();
    }
}
