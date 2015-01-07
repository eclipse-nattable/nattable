/*******************************************************************************
 * Copyright (c) 2012, 2013, 2014, 2015 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 455327
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 444839, 444855, 453885
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.extension.glazedlists.groupBy;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;

import ca.odell.glazedlists.TreeList;

/**
 * The TreeList.Format that is used by the TreeList that is created and used by
 * the GroupByDataLayer. Note that the TreeList created by the GroupByDataLayer
 * is generic for Object because the groupBy functionality will add
 * GroupByObjects to the path for creating the grouping.
 *
 * @param <T>
 *            The type of the base objects carried in the TreeList.
 */
public class GroupByTreeFormat<T> implements TreeList.Format<Object> {

    /**
     * The GroupByModel that carries the information about the groupBy states.
     */
    private final GroupByModel model;
    /**
     * The IColumnAccessor that is used to get the column value for the columns
     * that are grouped by. Needed for compare operations and creating the path
     * in the tree.
     */
    private final IColumnAccessor<T> columnAccessor;
    /**
     * Comparator that is used to sort the TreeList based on the groupBy
     * information.
     */
    private IGroupByComparator<T> groupByComparator;

    /**
     *
     * @param model
     *            The GroupByModel that carries the information about the
     *            groupBy states.
     * @param columnAccessor
     *            The IColumnAccessor that is used to get the column value for
     *            the columns that are grouped by. Needed for compare operations
     *            and creating the path in the tree.
     */
    public GroupByTreeFormat(GroupByModel model, IColumnAccessor<T> columnAccessor) {
        this.model = model;
        this.columnAccessor = columnAccessor;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getPath(List<Object> path, Object element) {
        List<Integer> groupByColumns = this.model.getGroupByColumnIndexes();
        if (!groupByColumns.isEmpty()) {
            LinkedHashMap<Integer, Object> descriptor = new LinkedHashMap<Integer, Object>();
            for (int columnIndex : groupByColumns) {
                // Build a unique descriptor for the group
                Object columnValue = this.columnAccessor.getDataValue((T) element, columnIndex);
                descriptor.put(columnIndex, columnValue);
                GroupByObject groupByObject = getGroupByObject(columnValue, descriptor);
                path.add(groupByObject);
            }
        }
        path.add(element);
    }

    /**
     *
     * @param columnValue
     *            The column value that is used to create the
     *            {@link GroupByObject}. Specifies the groupBy value.
     * @param descriptor
     *            The descriptor that is used to create the
     *            {@link GroupByObject}. Specifies the groupBy depth.
     * @return The {@link GroupByObject} for the given value and descriptor.
     */
    protected GroupByObject getGroupByObject(Object columnValue, Map<Integer, Object> descriptor) {
        return new GroupByObject(columnValue, new LinkedHashMap<Integer, Object>(descriptor));
    }

    @Override
    public boolean allowsChildren(Object element) {
        return true;
    }

    @Override
    public Comparator<Object> getComparator(int depth) {
        // if there is no grouping we do not provide a comparator for the tree
        if (this.model.getGroupByColumnIndexes().isEmpty()) {
            return null;
        }

        return this.groupByComparator;
    }

    /**
     * Clear the comparator local cache of summary information that is used to
     * increase performance on sorting. Can be called often since the cache is
     * only valid for a sorting operation.
     */
    public void clearComparatorCache() {
        this.groupByComparator.clearCache();
    }

    /**
     *
     * @param model
     *            The {@link ISortModel} that should be set to the
     *            {@link IGroupByComparator}.
     * @see IGroupByComparator#setSortModel(ISortModel)
     */
    public void setSortModel(ISortModel model) {
        this.groupByComparator.setSortModel(model);
    }

    /**
     *
     * @return The {@link ISortModel} that is set to the
     *         {@link IGroupByComparator}.
     * @see IGroupByComparator#getSortModel()
     */
    public ISortModel getSortModel() {
        return this.groupByComparator.getSortModel();
    }

    /**
     *
     * @param treeLayer
     *            The {@link IUniqueIndexLayer} that should be set to the
     *            {@link IGroupByComparator}.
     * @see IGroupByComparator#setTreeLayer(IUniqueIndexLayer)
     */
    void setTreeLayer(IUniqueIndexLayer treeLayer) {
        this.groupByComparator.setTreeLayer(treeLayer);
    }

    /**
     *
     * @param dataLayer
     *            The {@link GroupByDataLayer} that should be set to the
     *            {@link IGroupByComparator}.
     * @see IGroupByComparator#setDataLayer(GroupByDataLayer)
     */
    void setDataLayer(GroupByDataLayer<T> dataLayer) {
        this.groupByComparator.setDataLayer(dataLayer);
    }

    /**
     *
     * @param comparator
     *            The {@link IGroupByComparator} that should be used to sort the
     *            {@link TreeList} in order to be able to build the tree
     *            structure correctly.
     */
    public void setComparator(IGroupByComparator<T> comparator) {
        this.groupByComparator = comparator;
    }

}
