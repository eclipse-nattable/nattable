/*****************************************************************************
 * Copyright (c) 2018 Dirk Fauth.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.nattable.hierarchical;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.DefaultComparator;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.sort.ISortModel;
import org.eclipse.nebula.widgets.nattable.sort.SortDirectionEnum;

/**
 * {@link Comparator} for collections of {@link HierarchicalWrapper}. Ensures
 * that the objects are ordered in a way that the expected tree structure is
 * kept. Additionally provides a way to sort by column if an {@link ISortModel}
 * is set, by still keeping the tree structure. That means in deeper levels of
 * the tree, only the sub nodes are sorted, not the tree itself is changed.
 *
 * @since 1.6
 */
public class HierarchicalWrapperComparator implements Comparator<HierarchicalWrapper> {

    private IColumnAccessor<HierarchicalWrapper> columnAccessor;
    private Map<Integer, List<Integer>> levelIndexMapping;
    private ISortModel sortModel;

    /**
     * Creates a new {@link HierarchicalWrapperComparator} without an
     * {@link ISortModel}. Without an {@link ISortModel} the
     * {@link DefaultComparator} is used for comparison of column values to keep
     * the tree structure. The sort order can not be influenced via column
     * sorting, until a {@link ISortModel} is set via
     * {@link #setSortModel(ISortModel)}.
     *
     * @param columnAccessor
     *            The {@link IColumnAccessor} needed to access the column values
     *            of the {@link HierarchicalWrapper} row objects.
     * @param levelIndexMapping
     *            The mapping of tree level to column indexes needed for level
     *            based sorting.
     */
    public HierarchicalWrapperComparator(
            IColumnAccessor<HierarchicalWrapper> columnAccessor,
            Map<Integer, List<Integer>> levelIndexMapping) {

        this(columnAccessor, levelIndexMapping, null);
    }

    /**
     * Creates a new {@link HierarchicalWrapperComparator} with an
     * {@link ISortModel} to support dynamic configurable column based sorting.
     *
     * @param columnAccessor
     *            The {@link IColumnAccessor} needed to access the column values
     *            of the {@link HierarchicalWrapper} row objects.
     * @param levelIndexMapping
     *            The mapping of tree level to column indexes needed for level
     *            based sorting.
     * @param sortModel
     *            The {@link ISortModel} that provides access to configured
     *            column comparators and supports dynamic sorting per column.
     */
    public HierarchicalWrapperComparator(
            IColumnAccessor<HierarchicalWrapper> columnAccessor,
            Map<Integer, List<Integer>> levelIndexMapping,
            ISortModel sortModel) {

        this.columnAccessor = columnAccessor;
        this.levelIndexMapping = levelIndexMapping;
        this.sortModel = sortModel;
    }

    @Override
    public int compare(HierarchicalWrapper o1, HierarchicalWrapper o2) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null && o2 != null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        } else if (o1 == o2) {
            return 0;
        } else {
            // different levels = different objects based on levels supported
            if (o1.getLevels() != o2.getLevels()) {
                return o1.getLevels() - o2.getLevels();
            } else {
                // first perform the level comparison to keep the tree structure
                int level = 0;
                Object o1LvlObject = o1.getObject(level);
                Object o2LvlObject = o2.getObject(level);

                // find the level objects that differ
                // do not consider the leaf level as it is not important for the
                // tree structure
                while (o1LvlObject == o2LvlObject && level < (this.levelIndexMapping.size() - 1)) {
                    level++;
                    o1LvlObject = o1.getObject(level);
                    o2LvlObject = o2.getObject(level);
                }

                int result = 0;
                if (o1LvlObject == null && o2LvlObject != null) {
                    result = -1;
                } else if (o2LvlObject == null) {
                    result = 1;
                } else if (o1LvlObject != null && o2LvlObject != null) {
                    // compare level objects
                    result = compareLevel(o1, o2, level);
                }

                if (result == 0 && this.sortModel != null) {
                    // check if the sortModel is configured for leaf level
                    // columns
                    List<Integer> leafLevelColumns = this.levelIndexMapping.get(this.levelIndexMapping.size() - 1);
                    List<Integer> sortedColumnIndexes = this.sortModel.getSortedColumnIndexes();
                    for (Integer sorted : sortedColumnIndexes) {
                        if (leafLevelColumns.contains(sorted)) {
                            result = compareColumn(o1, o2, sorted, false);
                            if (result != 0) {
                                break;
                            }
                        }
                    }
                }

                if (result < 0) {
                    result = -1;
                } else if (result > 0) {
                    result = 1;
                }
                return result;
            }
        }
    }

    /**
     * Compares two {@link HierarchicalWrapper} objects based on the information
     * in the given level.
     *
     * @param o1
     *            The first {@link HierarchicalWrapper} object to be compared.
     * @param o2
     *            The second {@link HierarchicalWrapper} object to be compared.
     * @param level
     *            The level that should be compared.
     * @return a negative integer, zero, or a positive integer as the first
     *         argument is less than, equal to, or greater than the second.
     */
    protected int compareLevel(HierarchicalWrapper o1, HierarchicalWrapper o2, int level) {
        List<Integer> levelIndexes = this.levelIndexMapping.get(level);
        int result = 0;

        boolean sortComparatorFound = false;
        if (this.sortModel != null) {
            List<Integer> sortedColumnIndexes = this.sortModel.getSortedColumnIndexes();

            for (Integer sorted : sortedColumnIndexes) {
                if (levelIndexes.contains(sorted)) {
                    sortComparatorFound = true;
                    result = compareColumn(o1, o2, sorted, false);
                    if (result != 0) {
                        break;
                    }
                }
            }
        }

        if (!sortComparatorFound) {
            for (int columnIndex : levelIndexes) {
                result = compareColumn(o1, o2, columnIndex, true);
                if (result != 0) {
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Compares two {@link HierarchicalWrapper} objects based on the information
     * in the given column.
     *
     * @param o1
     *            The first {@link HierarchicalWrapper} object to be compared.
     * @param o2
     *            The second {@link HierarchicalWrapper} object to be compared.
     * @param columnIndex
     * @param useDefault
     *            flag to configure whether the {@link DefaultComparator} should
     *            be used in case no dedicated comparator is configured. Should
     *            be set to <code>true</code> in case the comparator for a
     *            column is requested that is needed to ensure the tree
     *            structure.
     * @return a negative integer, zero, or a positive integer as the first
     *         argument is less than, equal to, or greater than the second.
     */
    @SuppressWarnings("unchecked")
    protected int compareColumn(HierarchicalWrapper o1, HierarchicalWrapper o2, int columnIndex, boolean useDefault) {
        Object value1 = this.columnAccessor.getDataValue(o1, columnIndex);
        Object value2 = this.columnAccessor.getDataValue(o2, columnIndex);

        int result = getComparator(columnIndex, useDefault).compare(value1, value2);
        if (result != 0
                && this.sortModel != null
                && this.sortModel.getSortDirection(columnIndex).equals(SortDirectionEnum.DESC)) {
            result *= -1;
        }
        return result;
    }

    /**
     * Returns the {@link Comparator} that should be used to compare the values
     * in a column for the given column index.
     *
     * @param columnIndex
     *            The column index of the column for which the
     *            {@link Comparator} is requested.
     * @param useDefault
     *            flag to configure whether the {@link DefaultComparator} should
     *            be used in case no dedicated comparator is configured. Should
     *            be set to <code>true</code> in case the comparator for a
     *            column is requested that is needed to ensure the tree
     *            structure.
     * @return The {@link Comparator} that should be used to compare the values
     *         for elements in the given column. Returns the
     *         {@link DefaultComparator} in case there is no {@link ISortModel}
     *         configured, no {@link Comparator} is found for the given column
     *         or a NullComparator is configured for the given column.
     */
    @SuppressWarnings("rawtypes")
    protected Comparator getComparator(int columnIndex, boolean useDefault) {
        Comparator result = null;

        if (this.sortModel != null) {
            result = this.sortModel.getColumnComparator(columnIndex);
        }

        if (result == null && useDefault) {
            result = DefaultComparator.getInstance();
        }

        return result;
    }

    /**
     *
     * @return The {@link ISortModel} that is set in this
     *         {@link HierarchicalWrapperComparator} to support dynamic column
     *         based sorting in combination with tree sorting.
     */
    public ISortModel getSortModel() {
        return this.sortModel;
    }

    /**
     *
     * @param sortModel
     *            The {@link ISortModel} that should be used by this
     *            {@link HierarchicalWrapperComparator} to support dynamic
     *            column based sorting in combination with tree sorting.
     */
    public void setSortModel(ISortModel sortModel) {
        this.sortModel = sortModel;
    }

}
