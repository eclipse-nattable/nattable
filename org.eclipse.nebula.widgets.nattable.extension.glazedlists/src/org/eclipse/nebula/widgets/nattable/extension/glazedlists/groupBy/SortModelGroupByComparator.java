/*******************************************************************************
 * Copyright (c) 2025 Dirk Fauth and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dirk Fauth <dirk.fauth@googlemail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;

/**
 * Specialization of {@link GroupByComparator} that primarily uses the
 * {@link ISortModel} to sort the list by values, and the {@link GroupByObject}s
 * in second place. This changes the behavior when sorting a table with active
 * groupby in that way, that the groupby tree structure could also update when
 * sorting a column that is used for grouping.
 *
 * As the name suggests, this comparator requires the {@link ISortModel} to be
 * set. This can be done via
 * {@link GroupByDataLayer#initializeTreeComparator(ISortModel, IUniqueIndexLayer, boolean)}
 * or directly via {@link #setSortModel(ISortModel)} as the {@link TreeLayer} is
 * not needed by this implementation and the {@link GroupByDataLayer} needs to
 * be passed already via constructor.
 *
 * @since 2.6
 */
public class SortModelGroupByComparator<T> extends GroupByComparator<T> {

    private boolean sortBySummaryRows = true;

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
    public SortModelGroupByComparator(GroupByModel groupByModel, IColumnAccessor<T> columnAccessor, GroupByDataLayer<T> dataLayer) {
        super(groupByModel, columnAccessor, dataLayer);
    }

    @Override
    public int compare(Object o1, Object o2) {
        int result = 0;

        if (o1 instanceof GroupByObject && o2 instanceof GroupByObject) {
            result = compareGroupByObjects((GroupByObject) o1, (GroupByObject) o2);
        } else if (!(o1 instanceof GroupByObject) && !(o2 instanceof GroupByObject)) {
            // already compared by the GroupByObjects
            // so just return 0
            result = 0;
        } else {
            throw new IllegalStateException(
                    "Comparison of GroupByObjects with non-GroupByObjects is not supported: " + o1 + " vs. " + o2); //$NON-NLS-1$//$NON-NLS-2$
        }

        return result;
    }

    @SuppressWarnings({ "unchecked" })
    protected int compareGroupByObjects(GroupByObject g1, GroupByObject g2) {
        List<Integer> sortColumns = this.sortModel.getSortedColumnIndexes();
        int result = 0;

        for (int sortColumnIndex : sortColumns) {
            // check if a comparison for summary rows is needed
            result = compareSummaryRows(g1, g2, sortColumnIndex);

            // if there is no summary row comparison result, compare the values
            // of the column
            if (result == 0) {
                Object o1 = g1.getDescriptor().get(sortColumnIndex);
                Object o2 = g2.getDescriptor().get(sortColumnIndex);
                result = getComparator(sortColumnIndex).compare(o1, o2);
            }

            if (result != 0) {
                if (SortDirectionEnum.DESC.equals(this.sortModel.getSortDirection(sortColumnIndex))) {
                    result *= -1;
                }
                break;
            }
        }

        // if there is no result after comparing all sorted columns, compare the
        // values in the GroupByObjects
        if (result == 0) {
            result = defaultCompareGroupByColumns(g1, g2);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    protected int compareSummaryRows(GroupByObject g1, GroupByObject g2, int columnIndex) {
        if (!isSortBySummaryRows()) {
            return 0;
        }

        // group building columns are also interpreted as summary columns
        // we do not want that here, so we just return 0
        if (this.groupByModel.getGroupByColumnIndexes().contains(columnIndex)) {
            return 0;
        }

        Boolean summaryColumn = isSummaryColumn(g1, columnIndex);
        if (summaryColumn == null) {
            // in case we were not able to retrieve the
            // information for the one GroupByObject we
            // try to find the information the other
            // GroupByObject
            summaryColumn = isSummaryColumn(g2, columnIndex);
        }

        if (summaryColumn != null && summaryColumn) {
            // compare GroupByObjects by summary value
            Object sumValue1 = getSummaryValueFromCache(g1, columnIndex);
            Object sumValue2 = getSummaryValueFromCache(g2, columnIndex);
            return getComparator(columnIndex).compare(sumValue1, sumValue2);
        }

        return 0;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected int defaultCompareGroupByColumns(GroupByObject g1, GroupByObject g2) {
        int result = 0;

        int groupByIndex = 0;
        for (Map.Entry<Integer, Object> entry : g1.getDescriptor().entrySet()) {
            groupByIndex = entry.getKey();
        }

        Comparator comparator = getComparator(groupByIndex);
        result = comparator.compare(g1.getValue(), g2.getValue());

        return result;
    }

    public boolean isSortBySummaryRows() {
        return this.sortBySummaryRows;
    }

    public void setSortBySummaryRows(boolean sortBySummaryRows) {
        this.sortBySummaryRows = sortBySummaryRows;
    }

}
