/*******************************************************************************
 * Copyright (c) 2012, 2019 Original authors and others.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroup;
import org.eclipse.nebula.widgets.nattable.group.model.IRowGroupModel;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.group.performance.RowGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.util.ArrayUtil;

/**
 * The utility methods in this class bridge the divide between the world of row
 * indexes and positions and row objects used the model.
 *
 * @author Stefan Bolton
 * @author Matt Biggs
 *
 */
public class RowGroupUtils {

    public static <T> IRowGroup<T> getRowGroupForRowIndex(final IRowGroupModel<T> model, final int rowIndex) {
        final T row = model.getRowFromIndexCache(rowIndex);
        return model.getRowGroupForRow(row);
    }

    public static <T> IRowGroup<T> getOwnRowGroupForRowIndex(final IRowGroupModel<T> model, final int rowIndex) {
        final T row = model.getRowFromIndexCache(rowIndex);
        IRowGroup<T> rowGroup = model.getRowGroupForRow(row);

        // If this is a sub-group row, then rowGroup will currently point to the
        // parent group.
        // We need to find the real, sub-group this row belongs to.
        if (rowGroup != null && !rowGroup.getOwnMemberRows(true).contains(row)) {
            rowGroup = rowGroup.getRowGroupForRow(row);
        }

        return rowGroup;
    }

    public static <T> boolean isPartOfAGroup(final IRowGroupModel<T> model, final int rowIndex) {
        final T row = model.getRowFromIndexCache(rowIndex);

        if (row != null) {
            return (model.getRowGroupForRow(row) != null);
        }

        return false;
    }

    public static <T> boolean isInTheSameGroup(final int fromRowIndex, final int toRowIndex, final IRowGroupModel<T> model) {
        final T fromRow = model.getRowFromIndexCache(fromRowIndex);
        final T toRow = model.getRowFromIndexCache(toRowIndex);

        IRowGroup<T> rowGroupFrom = getTopMostParentGroup(model.getRowGroupForRow(fromRow));
        IRowGroup<T> rowGroupTo = getTopMostParentGroup(model.getRowGroupForRow(toRow));

        return rowGroupFrom != null
                && rowGroupTo != null
                && rowGroupFrom.equals(rowGroupTo);
    }

    /**
     *
     * @param model
     *            The {@link IRowGroupModel} the given {@link IRowGroup} belongs
     *            to.
     * @param group
     *            The {@link IRowGroup} to check.
     * @return <code>true</code> if the given row group or one of its parent
     *         groups is collapsed.
     */
    public static <T> boolean isCollapsed(final IRowGroupModel<T> model, final IRowGroup<T> group) {
        return group == null
                || group.isCollapsed()
                || isAnyParentCollapsed(group);
    }

    /**
     *
     * @param group
     *            The {@link IRowGroup} to check.
     * @return <code>true</code> if any of the groups parent groups is
     *         collapsed.
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
     *
     * @param model
     *            The {@link IRowGroupModel} to check.
     * @param bodyRowIndex
     *            The index of a row whose row group should be inspected.
     * @return The number of rows in the row group to which the given
     *         bodyRowIndex belongs to.
     */
    public static <T> int sizeOfGroup(final IRowGroupModel<T> model, final int bodyRowIndex) {
        IRowGroup<T> group = getRowGroupForRowIndex(model, bodyRowIndex);

        if (group != null) {
            return getTopMostParentGroup(group).getMemberRows(true).size();
        } else {
            return 0;
        }
    }

    /**
     *
     * @param rowGroup
     *            The {@link IRowGroup} to check.
     * @return the top-most parent group of the given group or the group
     *         specified if it has no parents.
     */
    public static <T> IRowGroup<T> getTopMostParentGroup(final IRowGroup<T> rowGroup) {
        return (rowGroup == null
                ? null
                : (rowGroup.getParentGroup() == null)
                        ? rowGroup
                        : getTopMostParentGroup(rowGroup.getParentGroup()));
    }

    /**
     *
     * @param model
     *            The {@link IRowGroupModel} to check.
     * @param bodyRowIndex
     *            The index of a row whose row group should be inspected.
     * @return <code>true</code> if <code>bodyRowIndex</code> is contained in
     *         the list of static rows of the row group this index belongs to
     */
    public static <T> boolean isStaticRow(final IRowGroupModel<T> model, final int bodyRowIndex) {
        final T row = model.getRowFromIndexCache(bodyRowIndex);

        if (row != null) {
            IRowGroup<T> group = model.getRowGroupForRow(row);

            if (group != null) {
                return group.getStaticMemberRows().contains(row);
            }
        }

        return false;
    }

    public static boolean isRowIndexHiddenInUnderLyingLayer(
            final int rowIndex, final ILayer layer, final IUniqueIndexLayer underlyingLayer) {
        return underlyingLayer.getRowPositionByIndex(rowIndex) == -1;
    }

    /**
     * Helper method to get the row positions for a specified layer
     *
     * If a row is currently invisible (-1) it will not be returned within the
     * collection
     *
     * @param layer
     *            The layer for which the position transformation should be
     *            performed.
     * @param bodyRowIndexes
     *            The row indexes for which the positions are requested.
     * @return Unmodifiable list of the row positions for the given layer
     */
    public static List<Integer> getRowPositionsInGroup(
            final IUniqueIndexLayer layer, final Collection<Integer> bodyRowIndexes) {

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
     *
     * @param model
     *            The {@link IRowGroupModel} to check.
     * @param rowIndex
     *            The index of a row whose row group should be inspected.
     * @return Unmodifiable list of row indexes and static row indexes in the
     *         same group as this index
     */
    public static <T> List<Integer> getRowIndexesInGroup(final IRowGroupModel<T> model, final int rowIndex) {
        final IRowGroup<T> group = getRowGroupForRowIndex(model, rowIndex);
        return getRowIndexesInGroup(model, group, true);
    }

    public static <T> List<Integer> getRowIndexesInGroup(
            final IRowGroupModel<T> model, final IRowGroup<T> group, final boolean includeStatic) {
        List<Integer> indexes = new ArrayList<Integer>();

        for (T row : group.getMemberRows(includeStatic)) {
            indexes.add(model.getIndexFromRowCache(row));
        }

        return indexes;
    }

    public static <T> String getRowGroupNameForIndex(IRowGroupModel<T> model, int bodyRowIndex) {
        IRowGroup<T> group = getRowGroupForRowIndex(model, bodyRowIndex);
        if (group != null) {
            return group.getGroupName();
        }
        return null;
    }

    /**
     * Checks if the two given row positions on the given layer belong to the
     * same group at the given level on the given {@link RowGroupHeaderLayer}.
     *
     * @param layer
     *            The {@link RowGroupHeaderLayer} which is needed to perform the
     *            check against.
     * @param level
     *            The grouping level to check.
     * @param fromPosition
     *            The row position to check based on the position layer of the
     *            given {@link RowGroupHeaderLayer}.
     * @param toPosition
     *            The row position to check based on the position layer of the
     *            given {@link RowGroupHeaderLayer}.
     * @return <code>true</code> if both given positions belong to the same
     *         group, <code>false</code> if not.
     *
     * @since 1.6
     */
    public static boolean isInTheSameGroup(RowGroupHeaderLayer layer, int level, int fromPosition, int toPosition) {
        Group fromGroup = layer.getGroupModel(level).getGroupByPosition(fromPosition);
        Group toGroup = layer.getGroupModel(level).getGroupByPosition(toPosition);

        return fromGroup != null && toGroup != null && fromGroup == toGroup;
    }

    /**
     * Checks if the row positions at the given y coordinates belong to the same
     * group or not.
     *
     * @param natLayer
     *            The layer to which the given positions match. Typically the
     *            NatTable itself.
     * @param startY
     *            The y coordinate of the row that should be checked. Typically
     *            the drag start y coordinate.
     * @param endY
     *            The y coordinate of the row that should be checked. Typically
     *            the drag end y coordinate.
     * @param layer
     *            The {@link RowGroupHeaderLayer} which is needed to perform the
     *            check against.
     * @param level
     *            The grouping level to check.
     * @return <code>true</code> if there is a row group boundary between startY
     *         and endY, <code>false</code> if both positions are in the same
     *         group.
     *
     * @since 1.6
     */
    public static boolean isBetweenTwoGroups(ILayer natLayer, int startY, int endY, RowGroupHeaderLayer layer, int level) {
        int natFromPosition = natLayer.getRowPositionByY(startY);
        int natToPosition = natLayer.getRowPositionByY(endY);

        // convert grid position to layer position
        int fromPosition = LayerUtil.convertRowPosition(natLayer, natFromPosition, layer.getPositionLayer());
        int toPosition = LayerUtil.convertRowPosition(natLayer, natToPosition, layer.getPositionLayer());

        boolean result = !RowGroupUtils.isInTheSameGroup(
                layer,
                level,
                fromPosition,
                toPosition);

        // special check for reordering to position 0 left of an unbreakable
        // group
        if (!result && fromPosition == toPosition && natFromPosition < natToPosition) {
            result = true;
        }

        return result;
    }

    /**
     * Checks if the edge of a row position is the top-most or the bottom most
     * row on any level of a row group.
     *
     * @param rowGroupHeaderLayer
     *            The {@link RowGroupHeaderLayer} to handle the checks.
     * @param toPosition
     *            The position to check. Needs to be related to the
     *            positionLayer.
     * @param reorderToTopEdge
     *            <code>true</code> if the check should be performed to the top
     *            edge or the bottom edge of the toPosition.
     * @param moveDirection
     *            The direction in which the reordering is performed.
     * @return <code>true</code> if the destination would be between two groups,
     *         <code>false</code> if the destination would be inside a group.
     *
     * @since 1.6
     */
    public static boolean isBetweenTwoGroups(
            RowGroupHeaderLayer rowGroupHeaderLayer, int toPosition, boolean reorderToTopEdge, MoveDirectionEnum moveDirection) {

        if ((toPosition == 0 && reorderToTopEdge)
                || (toPosition == (rowGroupHeaderLayer.getPositionLayer().getRowCount() - 1) && !reorderToTopEdge)) {
            // start or end of the table
            return true;
        }

        int toPositionToCheck = toPosition;
        if (reorderToTopEdge
                && MoveDirectionEnum.DOWN == moveDirection) {
            toPositionToCheck--;
        }

        boolean valid = true;
        for (int level = 0; level < rowGroupHeaderLayer.getLevelCount(); level++) {
            if (MoveDirectionEnum.DOWN == moveDirection) {
                valid = !isInTheSameGroup(rowGroupHeaderLayer, level, toPosition, toPositionToCheck);
            } else {
                valid = !isInTheSameGroup(rowGroupHeaderLayer, level, toPosition, toPositionToCheck + (reorderToTopEdge ? -1 : 1));
            }

            if (!valid) {
                break;
            }
        }

        return valid;
    }

    /**
     * Checks if the edge of a row position for a specific grouping level is the
     * top-most or the bottom most row of a row group.
     *
     * @param rowGroupHeaderLayer
     *            The {@link RowGroupHeaderLayer} to handle the checks.
     * @param level
     *            The grouping level on which the check should be performed.
     * @param toPosition
     *            The position to check. Needs to be related to the
     *            positionLayer.
     * @param reorderToTopEdge
     *            <code>true</code> if the check should be performed to the top
     *            edge or the bottom edge of the toPosition.
     * @param moveDirection
     *            The direction in which the reordering is performed.
     * @return <code>true</code> if the destination would be between two groups,
     *         <code>false</code> if the destination would be inside a group.
     *
     * @since 1.6
     */
    public static boolean isBetweenTwoGroups(
            RowGroupHeaderLayer rowGroupHeaderLayer, int level, int toPosition, boolean reorderToTopEdge, MoveDirectionEnum moveDirection) {

        if ((toPosition == 0 && reorderToTopEdge)
                || (toPosition == (rowGroupHeaderLayer.getPositionLayer().getRowCount() - 1) && !reorderToTopEdge)) {
            // start or end of the table
            return true;
        }

        int toPositionToCheck = toPosition;
        if (MoveDirectionEnum.DOWN == moveDirection) {
            if (reorderToTopEdge) {
                toPositionToCheck--;
            } else {
                toPositionToCheck++;
            }
        }

        boolean valid = true;
        if (MoveDirectionEnum.DOWN == moveDirection) {
            valid = !isInTheSameGroup(rowGroupHeaderLayer, level, toPosition, toPositionToCheck);
        } else {
            valid = !isInTheSameGroup(rowGroupHeaderLayer, level, toPosition, toPositionToCheck + (reorderToTopEdge ? -1 : 1));
        }

        if (valid && level > 0) {
            // check on deeper levels
            valid = isBetweenTwoGroups(rowGroupHeaderLayer, level - 1, toPosition, reorderToTopEdge, moveDirection);
        }

        return valid;
    }

    /**
     * Checks if a reorder operation is valid by checking the unbreakable states
     * of the groups below the from and the to position.
     *
     * @param rowGroupHeaderLayer
     *            The {@link RowGroupHeaderLayer} to get the groups to check.
     * @param fromPosition
     *            The position from which a row should be reordered.
     * @param toPosition
     *            The position to which a row should be reordered.
     * @param reorderToTopEdge
     *            <code>true</code> if the reorder should be performed to the
     *            top edge of the toPosition.
     * @return <code>true</code> if the reorder operation would be valid,
     *         <code>false</code> if the either the source or the target belongs
     *         to an unbreakable group.
     *
     * @since 1.6
     */
    public static boolean isReorderValid(RowGroupHeaderLayer rowGroupHeaderLayer, int fromPosition, int toPosition, boolean reorderToTopEdge) {
        for (int level = 0; level < rowGroupHeaderLayer.getLevelCount(); level++) {
            if (!isReorderValid(rowGroupHeaderLayer, level, fromPosition, toPosition, reorderToTopEdge)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a reorder operation is valid by checking the unbreakable states
     * of the groups below the from and the to position.
     *
     * @param rowGroupHeaderLayer
     *            The {@link RowGroupHeaderLayer} to get the groups to check.
     * @param level
     *            The grouping level that should be checked.
     * @param fromPosition
     *            The position from which a row should be reordered.
     * @param toPosition
     *            The position to which a row should be reordered.
     * @param reorderToTopEdge
     *            <code>true</code> if the reorder should be performed to the
     *            top edge of the toPosition.
     * @return <code>true</code> if the reorder operation would be valid,
     *         <code>false</code> if the either the source or the target belongs
     *         to an unbreakable group.
     *
     * @since 1.6
     */
    public static boolean isReorderValid(
            RowGroupHeaderLayer rowGroupHeaderLayer, int level, int fromPosition, int toPosition, boolean reorderToTopEdge) {

        MoveDirectionEnum moveDirection = PositionUtil.getVerticalMoveDirection(fromPosition, toPosition);

        int toPositionToCheck = toPosition;
        if (MoveDirectionEnum.DOWN == moveDirection && reorderToTopEdge
                || toPosition == rowGroupHeaderLayer.getPositionLayer().getRowCount() && !reorderToTopEdge) {
            toPositionToCheck--;
        }

        boolean fromUnbreakable = false;
        boolean valid = true;
        // check if the from position is unbreakable
        GroupModel model = rowGroupHeaderLayer.getGroupModel(level);
        Group group = model.getGroupByPosition(fromPosition);
        if (group != null && group.isUnbreakable() && group.getVisibleSpan() > 1) {
            fromUnbreakable = true;
            valid = isInTheSameGroup(rowGroupHeaderLayer, level, fromPosition, toPositionToCheck);
        }

        // if the from position is part of an unbreakable group, we already know
        // the result
        if (!fromUnbreakable) {
            if ((toPosition == 0 && reorderToTopEdge)
                    || (toPosition == (rowGroupHeaderLayer.getPositionLayer().getRowCount() - 1) && !reorderToTopEdge)) {
                // start or end of the table
                return true;
            }

            // check if the to position is unbreakable
            if (model.isPartOfAnUnbreakableGroup(toPositionToCheck)) {
                // check if the original toPosition is in another group to
                // see if we might reorder between groups
                if (MoveDirectionEnum.DOWN == moveDirection) {
                    valid = !isInTheSameGroup(rowGroupHeaderLayer, level, toPosition, toPositionToCheck);
                } else {
                    valid = !isInTheSameGroup(rowGroupHeaderLayer, level, toPosition, toPositionToCheck + (reorderToTopEdge ? -1 : 1));
                }
            }
        }

        return valid;
    }

    /**
     * Check if a complete group is reordered.
     *
     * @param fromGroup
     *            The group to check.
     * @param fromPositions
     *            The positions to check.
     * @return <code>true</code> if the fromPositions are all part of the given
     *         group.
     * @since 1.6
     */
    public static boolean isGroupReordered(Group fromGroup, int[] fromPositions) {
        Collection<Integer> visiblePositions = fromGroup.getVisiblePositions();
        if (visiblePositions.size() > fromPositions.length) {
            return false;
        } else if (visiblePositions.size() < fromPositions.length) {
            List<Integer> from = ArrayUtil.asIntegerList(fromPositions);
            return from.containsAll(visiblePositions);
        } else {
            int[] positionsArray = ArrayUtil.asIntArray(visiblePositions);
            return Arrays.equals(positionsArray, fromPositions);
        }
    }
}
