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
package org.eclipse.nebula.widgets.nattable.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.group.model.IRowGroup;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroupModel;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;

/**
 * The utility methods in this class bridge the divide between the world of row
 * indexes and positions and row objects used the model.
 *
 * @author Stefan Bolton
 * @author Matt Biggs
 *
 */
public class RowGroupUtils {

    public static <T> IRowGroup<T> getRowGroupForRowIndex(
            final IRowGroupModel<T> model, final int rowIndex) {
        final T row = model.getRowFromIndexCache(rowIndex);
        return model.getRowGroupForRow(row);
    }

    public static <T> IRowGroup<T> getOwnRowGroupForRowIndex(
            final IRowGroupModel<T> model, final int rowIndex) {
        final T row = model.getRowFromIndexCache(rowIndex);
        IRowGroup<T> rowGroup = model.getRowGroupForRow(row);

        // If this is a sub-group row, then rowGroup will currently point to the
        // parent group.
        // We need to find the real, sub-group this row belongs to.
        if ((rowGroup != null)
                && !rowGroup.getOwnMemberRows(true).contains(row)) {
            rowGroup = rowGroup.getRowGroupForRow(row);
        }

        return rowGroup;
    }

    public static <T> boolean isPartOfAGroup(final IRowGroupModel<T> model,
            final int rowIndex) {
        final T row = model.getRowFromIndexCache(rowIndex);

        if (row != null) {
            return (model.getRowGroupForRow(row) != null);
        }

        return false;
    }

    public static <T> boolean isInTheSameGroup(final int fromRowIndex,
            final int toRowIndex, final IRowGroupModel<T> model) {
        final T fromRow = model.getRowFromIndexCache(fromRowIndex);
        final T toRow = model.getRowFromIndexCache(toRowIndex);

        IRowGroup<T> rowGroupFrom = getTopMostParentGroup(model
                .getRowGroupForRow(fromRow));
        IRowGroup<T> rowGroupTo = getTopMostParentGroup(model
                .getRowGroupForRow(toRow));

        return rowGroupFrom != null && rowGroupTo != null
                && rowGroupFrom.equals(rowGroupTo);
    }

    /**
     * @return TRUE if the row group this index belongs to is collapsed <b>or
     *         one of it's parent groups is collapsed.</b>
     */
    public static <T> boolean isCollapsed(final IRowGroupModel<T> model,
            final IRowGroup<T> group) {
        return group == null || group.isCollapsed()
                || isAnyParentCollapsed(group);
    }

    /**
     * Returns true if any of the groups parent groups is collapsed.
     */
    public static <T> boolean isAnyParentCollapsed(IRowGroup<T> group) {
        boolean collapsed = false;

        if (group != null) {
            IRowGroup<T> topMostGroup = getTopMostParentGroup(group);

            // Walk up the group hierarchy until we find a collapsed group.
            while (!collapsed && group != topMostGroup) {
                group = group.getParentGroup();

                if (group == null) {
                    break;
                }

                collapsed = group.isCollapsed();
            }
        }

        return collapsed;
    }

    /**
     * Number of rows in the Group which the bodyRowIndex belongs to.
     */
    public static <T> int sizeOfGroup(final IRowGroupModel<T> model,
            final int bodyRowIndex) {
        IRowGroup<T> group = getRowGroupForRowIndex(model, bodyRowIndex);

        if (group != null) {
            return getTopMostParentGroup(group).getMemberRows(true).size();
        } else {
            return 0;
        }
    }

    /**
     * Return the top-most parent group of the group specified, or the group
     * specified if it has no parents.
     */
    public static <T> IRowGroup<T> getTopMostParentGroup(
            final IRowGroup<T> rowGroup) {
        return (rowGroup == null ? null
                : (rowGroup.getParentGroup() == null) ? rowGroup
                        : getTopMostParentGroup(rowGroup.getParentGroup()));
    }

    /**
     * @return TRUE if <code>bodyRowIndex</code> is contained in the list of
     *         static rows of the row group this index belongs to
     */
    public static <T> boolean isStaticRow(final IRowGroupModel<T> model,
            final int bodyRowIndex) {
        final T row = model.getRowFromIndexCache(bodyRowIndex);

        if (row != null) {
            IRowGroup<T> group = model.getRowGroupForRow(row);

            if (group != null) {
                return group.getStaticMemberRows().contains(row);
            }
        }

        return false;
    }

    public static boolean isRowIndexHiddenInUnderLyingLayer(final int rowIndex,
            final ILayer layer, final IUniqueIndexLayer underlyingLayer) {
        return underlyingLayer.getRowPositionByIndex(rowIndex) == -1;
    }

    /**
     * Helper method to get the row positions for a specified layer
     *
     * If a row is currently invisible (-1) it will not be returned within the
     * collection
     *
     * @param layer
     * @param bodyRowIndexes
     * @return the row positions for a specified layer
     */
    public static List<Integer> getRowPositionsInGroup(
            final IUniqueIndexLayer layer,
            final Collection<Integer> bodyRowIndexes) {
        final List<Integer> rowPositions = new ArrayList<Integer>();
        for (Integer bodyRowIndex : bodyRowIndexes) {
            final int rowPosition = layer.getRowPositionByIndex(bodyRowIndex);
            if (rowPosition != -1) {
                rowPositions.add(rowPosition);
            }
        }
        return Collections.unmodifiableList(rowPositions);
    }

    /**
     * @return Unmodifiable list of row indexes and static row indexes in the
     *         same group as this index
     */
    public static <T> List<Integer> getRowIndexesInGroup(
            final IRowGroupModel<T> model, final int rowIndex) {
        final IRowGroup<T> group = getRowGroupForRowIndex(model, rowIndex);
        return getRowIndexesInGroup(model, group, true);
    }

    public static <T> List<Integer> getRowIndexesInGroup(
            final IRowGroupModel<T> model, final IRowGroup<T> group,
            final boolean includeStatic) {
        List<Integer> indexes = new ArrayList<Integer>();

        for (T row : group.getMemberRows(includeStatic)) {
            indexes.add(model.getIndexFromRowCache(row));
        }

        return indexes;
    }

    public static <T> String getRowGroupNameForIndex(IRowGroupModel<T> model,
            int bodyRowIndex) {
        IRowGroup<T> group = getRowGroupForRowIndex(model, bodyRowIndex);
        if (group != null) {
            return group.getGroupName();
        }
        return null;
    }
}
