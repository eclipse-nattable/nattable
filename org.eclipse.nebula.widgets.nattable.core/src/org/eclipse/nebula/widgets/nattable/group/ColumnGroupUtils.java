/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.group.performance.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel;
import org.eclipse.nebula.widgets.nattable.group.performance.GroupModel.Group;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;

public final class ColumnGroupUtils {

    private ColumnGroupUtils() {
        // private default constructor for helper class
    }

    /**
     * Calculates the move direction based on the from and to position.
     *
     * @param fromColumnPosition
     *            The column position from which a move is triggered.
     * @param toColumnPosition
     *            The column position to which a move is triggered.
     * @return The direction of the triggered move operation.
     */
    public static MoveDirectionEnum getMoveDirection(int fromColumnPosition, int toColumnPosition) {
        if (fromColumnPosition > toColumnPosition) {
            return MoveDirectionEnum.LEFT;
        } else if (fromColumnPosition < toColumnPosition) {
            return MoveDirectionEnum.RIGHT;
        } else {
            return MoveDirectionEnum.NONE;
        }
    }

    public static boolean isInTheSameGroup(int fromColumnIndex, int toColumnIndex, ColumnGroupModel model) {
        ColumnGroup fromColumnGroup = model.getColumnGroupByIndex(fromColumnIndex);
        ColumnGroup toColumnGroup = model.getColumnGroupByIndex(toColumnIndex);

        return fromColumnGroup != null
                && toColumnGroup != null
                && fromColumnGroup == toColumnGroup;
    }

    /**
     * Checks whether <code>columnIndex</code> is either a defined static column
     * or (if not) the first visible column in the group containing group. This
     * method provides downward compatibility for all group definitions without
     * static columns. When no static columns are defined the first visible
     * column will be used.
     *
     * @param columnIndex
     *            The column index to check.
     * @param layer
     *            unused
     * @param underlyingLayer
     *            The underlying layer.
     * @param model
     *            The column group model.
     *
     * @return <code>TRUE</code> if the given <code>columnIndex</code> is either
     *         a defined static column or (if not) the first visible column the
     *         it's group
     *
     * @deprecated Use
     *             {@link #isStaticOrFirstVisibleColumn(int, IUniqueIndexLayer, ColumnGroupModel)}
     */
    @Deprecated
    public static boolean isStaticOrFirstVisibleColumn(
            int columnIndex, ILayer layer, IUniqueIndexLayer underlyingLayer, ColumnGroupModel model) {
        return isStaticOrFirstVisibleColumn(columnIndex, underlyingLayer, model);
    }

    /**
     * Checks whether <code>columnIndex</code> is either a defined static column
     * or (if not) the first visible column in the group containing group. This
     * method provides downward compatibility for all group definitions without
     * static columns. When no static columns are defined the first visible
     * column will be used.
     *
     * @param columnIndex
     *            The column index to check.
     * @param underlyingLayer
     *            The underlying layer.
     * @param model
     *            The column group model.
     *
     * @return <code>TRUE</code> if the given <code>columnIndex</code> is either
     *         a defined static column or (if not) the first visible column the
     *         it's group
     *
     * @since 2.0
     */
    public static boolean isStaticOrFirstVisibleColumn(
            int columnIndex, IUniqueIndexLayer underlyingLayer, ColumnGroupModel model) {

        ColumnGroup columnGroup = model.getColumnGroupByIndex(columnIndex);

        if (columnGroup.getStaticColumnIndexes().isEmpty()) {
            return isFirstVisibleColumnIndexInGroup(columnIndex, underlyingLayer, model);
        } else {
            return model.isStaticColumn(columnIndex);
        }
    }

    /**
     *
     * @param columnIndex
     *            The column index to check.
     * @param layer
     *            unused
     * @param underlyingLayer
     *            The underlying layer.
     * @param model
     *            The column group model.
     * @return <code>true</code> if the given column index is the first column
     *         in a column group.
     *
     * @deprecated Use
     *             {@link #isFirstVisibleColumnIndexInGroup(int, IUniqueIndexLayer, ColumnGroupModel)}
     */
    @Deprecated
    public static boolean isFirstVisibleColumnIndexInGroup(
            int columnIndex, ILayer layer, IUniqueIndexLayer underlyingLayer, ColumnGroupModel model) {
        return isFirstVisibleColumnIndexInGroup(columnIndex, underlyingLayer, model);
    }

    /**
     *
     * @param columnIndex
     *            The column index to check.
     * @param underlyingLayer
     *            The underlying layer.
     * @param model
     *            The column group model.
     * @return <code>true</code> if the given column index is the first column
     *         in a column group.
     *
     * @since 2.0
     */
    public static boolean isFirstVisibleColumnIndexInGroup(
            int columnIndex, IUniqueIndexLayer underlyingLayer, ColumnGroupModel model) {

        if (isColumnIndexHiddenInUnderLyingLayer(columnIndex, underlyingLayer)) {
            return false;
        }

        int columnPosition = underlyingLayer.getColumnPositionByIndex(columnIndex);
        List<Integer> columnIndexesInGroup = model.getColumnGroupByIndex(columnIndex).getMembers();
        List<Integer> previousVisibleColumnIndexes = new ArrayList<>();

        // All other indexes in the column group which are visible and
        // are positioned before me
        for (Integer currentIndex : columnIndexesInGroup) {
            int currentPosition = underlyingLayer.getColumnPositionByIndex(currentIndex.intValue());
            if (!isColumnIndexHiddenInUnderLyingLayer(currentIndex.intValue(), underlyingLayer)
                    && currentPosition < columnPosition) {
                previousVisibleColumnIndexes.add(currentIndex);
            }
        }

        return previousVisibleColumnIndexes.isEmpty();
    }

    /**
     *
     * @param columnIndex
     *            The column index to check.
     * @param layer
     *            unused
     * @param underlyingLayer
     *            The underlying layer.
     * @param model
     *            The column group model.
     * @return <code>true</code> if the given column index is the last column in
     *         a column group.
     *
     * @deprecated Use
     *             {@link #isLastVisibleColumnIndexInGroup(int, IUniqueIndexLayer, ColumnGroupModel)}
     */
    @Deprecated
    public static boolean isLastVisibleColumnIndexInGroup(
            int columnIndex, ILayer layer, IUniqueIndexLayer underlyingLayer, ColumnGroupModel model) {
        return isLastVisibleColumnIndexInGroup(columnIndex, underlyingLayer, model);
    }

    /**
     *
     * @param columnIndex
     *            The column index to check.
     * @param underlyingLayer
     *            The underlying layer.
     * @param model
     *            The column group model.
     * @return <code>true</code> if the given column index is the last column in
     *         a column group.
     *
     * @since 2.0
     */
    public static boolean isLastVisibleColumnIndexInGroup(
            int columnIndex, IUniqueIndexLayer underlyingLayer, ColumnGroupModel model) {

        if (isColumnIndexHiddenInUnderLyingLayer(columnIndex, underlyingLayer)) {
            return false;
        }

        List<Integer> visibleIndexesToTheRight = getVisibleIndexesToTheRight(columnIndex, underlyingLayer, model);
        return visibleIndexesToTheRight.size() == 1
                && visibleIndexesToTheRight.get(0).intValue() == columnIndex;
    }

    /**
     *
     * @param columnIndex
     *            The index to check.
     * @param layer
     *            unused
     * @param underlyingLayer
     *            The underlying layer to retrieve the visible columns to the
     *            right.
     * @param model
     *            The {@link ColumnGroupModel} to get the group from.
     * @return The column indexes to the right of the given column index
     *         (inclusive)
     *
     * @deprecated Use
     *             {@link #getVisibleIndexesToTheRight(int, IUniqueIndexLayer, ColumnGroupModel)}
     */
    @Deprecated
    public static List<Integer> getVisibleIndexesToTheRight(
            int columnIndex, ILayer layer, IUniqueIndexLayer underlyingLayer, ColumnGroupModel model) {

        return getVisibleIndexesToTheRight(columnIndex, underlyingLayer, model);
    }

    /**
     *
     * @param columnIndex
     *            The index to check.
     * @param underlyingLayer
     *            The underlying layer to retrieve the visible columns to the
     *            right.
     * @param model
     *            The {@link ColumnGroupModel} to get the group from.
     * @return The column indexes to the right of the given column index
     *         (inclusive)
     *
     * @since 2.0
     */
    public static List<Integer> getVisibleIndexesToTheRight(
            int columnIndex, IUniqueIndexLayer underlyingLayer, ColumnGroupModel model) {

        ColumnGroup columnGroup = model.getColumnGroupByIndex(columnIndex);

        if (columnGroup.isCollapsed()) {
            return Collections.emptyList();
        }

        List<Integer> columnIndexesInGroup = columnGroup.getMembers();
        int columnPosition = underlyingLayer.getColumnPositionByIndex(columnIndex);
        List<Integer> visibleColumnIndexesOnRight = new ArrayList<>();

        for (Integer currentIndex : columnIndexesInGroup) {
            int currentPosition = underlyingLayer.getColumnPositionByIndex(currentIndex.intValue());
            if (!isColumnIndexHiddenInUnderLyingLayer(currentIndex.intValue(), underlyingLayer)
                    && currentPosition >= columnPosition) {
                visibleColumnIndexesOnRight.add(currentIndex);
            }
        }

        return visibleColumnIndexesOnRight;
    }

    /**
     *
     * @param columnIndex
     *            The column index to check.
     * @param layer
     *            unused
     * @param underlyingLayer
     *            The underlying layer to check against.
     * @return <code>true</code> if the column is hidden in the underlying
     *         layer, <code>false</code> if not.
     *
     * @deprecated Use
     *             {@link #isColumnIndexHiddenInUnderLyingLayer(int, IUniqueIndexLayer)}
     */
    @Deprecated
    public static boolean isColumnIndexHiddenInUnderLyingLayer(int columnIndex, ILayer layer, IUniqueIndexLayer underlyingLayer) {
        return isColumnIndexHiddenInUnderLyingLayer(columnIndex, underlyingLayer);
    }

    /**
     *
     * @param columnIndex
     *            The column index to check.
     * @param underlyingLayer
     *            The underlying layer to check against.
     * @return <code>true</code> if the column is hidden in the underlying
     *         layer, <code>false</code> if not.
     *
     * @since 2.0
     */
    public static boolean isColumnIndexHiddenInUnderLyingLayer(int columnIndex, IUniqueIndexLayer underlyingLayer) {
        return underlyingLayer.getColumnPositionByIndex(columnIndex) == -1;
    }

    /**
     *
     * @param columnPosition
     *            The column position matching the underlying layer.
     * @param layer
     *            unused
     * @param underlyingLayer
     *            The underlying layer to check against.
     * @return <code>true</code> if the column position is hidden in the
     *         underlying layer, <code>false</code> if not.
     *
     * @deprecated Use
     *             {@link #isColumnPositionHiddenInUnderLyingLayer(int, IUniqueIndexLayer)}
     */
    @Deprecated
    public static boolean isColumnPositionHiddenInUnderLyingLayer(int columnPosition, ILayer layer, IUniqueIndexLayer underlyingLayer) {
        return isColumnPositionHiddenInUnderLyingLayer(columnPosition, underlyingLayer);
    }

    /**
     *
     * @param columnPosition
     *            The column position matching the underlying layer.
     * @param underlyingLayer
     *            The underlying layer to check against.
     * @return <code>true</code> if the column position is hidden in the
     *         underlying layer, <code>false</code> if not.
     *
     * @since 2.0
     */
    public static boolean isColumnPositionHiddenInUnderLyingLayer(int columnPosition, IUniqueIndexLayer underlyingLayer) {
        if (columnPosition < underlyingLayer.getColumnCount() && columnPosition >= 0) {
            int columnIndex = underlyingLayer.getColumnIndexByPosition(columnPosition);
            return isColumnIndexHiddenInUnderLyingLayer(columnIndex, underlyingLayer);
        }
        return true;
    }

    /**
     * See ColumnGroupUtilsTest
     *
     * @return TRUE if the given column is the <i>right</i> most column in a
     *         group
     */
    public static boolean isRightEdgeOfAColumnGroup(ILayer natLayer, int columnPosition, int columnIndex, ColumnGroupModel model) {
        int nextColumnPosition = columnPosition + 1;

        if (nextColumnPosition < natLayer.getColumnCount()) {
            int nextColumnIndex = natLayer.getColumnIndexByPosition(nextColumnPosition);
            if ((model.isPartOfAGroup(columnIndex) && !model.isPartOfAGroup(nextColumnIndex))) {
                return true;
            }
            if ((model.isPartOfAGroup(columnIndex)
                    && model.isPartOfAGroup(nextColumnIndex))
                    && !ColumnGroupUtils.isInTheSameGroup(columnIndex, nextColumnIndex, model)) {
                return true;
            }
        }
        return false;
    }

    /**
     * See ColumnGroupUtilsTest
     *
     * @return TRUE if the given column is the <i>left</i> most column in a
     *         group
     */
    public static boolean isLeftEdgeOfAColumnGroup(ILayer natLayer, int columnPosition, int columnIndex, ColumnGroupModel model) {
        int previousColumnPosition = columnPosition - 1;

        // First column && in a group
        if (columnPosition == 0 && model.isPartOfAGroup(columnIndex)) {
            return true;
        }

        if (previousColumnPosition >= 0) {
            int previousColumnIndex = natLayer.getColumnIndexByPosition(previousColumnPosition);
            if ((model.isPartOfAGroup(columnIndex) && !model.isPartOfAGroup(previousColumnIndex))) {
                return true;
            }
            if ((model.isPartOfAGroup(columnIndex)
                    && model.isPartOfAGroup(previousColumnIndex))
                    && !ColumnGroupUtils.isInTheSameGroup(columnIndex, previousColumnIndex, model)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return TRUE if there is a column group boundary between startX and endX
     */
    public static boolean isBetweenTwoGroups(ILayer natLayer, int startX, int endX, ColumnGroupModel model) {
        return !ColumnGroupUtils.isInTheSameGroup(
                natLayer.getColumnIndexByPosition(natLayer.getColumnPositionByX(startX)),
                natLayer.getColumnIndexByPosition(natLayer.getColumnPositionByX(endX)),
                model);
    }

    /**
     * Checks if the two given column positions on the given layer belong to the
     * same group at the given level on the given
     * {@link ColumnGroupHeaderLayer}.
     *
     * @param layer
     *            The {@link ColumnGroupHeaderLayer} which is needed to perform
     *            the check against.
     * @param level
     *            The grouping level to check.
     * @param fromPosition
     *            The column position to check based on the position layer of
     *            the given {@link ColumnGroupHeaderLayer}.
     * @param toPosition
     *            The column position to check based on the position layer of
     *            the given {@link ColumnGroupHeaderLayer}.
     * @return <code>true</code> if both given positions belong to the same
     *         group, <code>false</code> if not.
     *
     * @since 1.6
     */
    public static boolean isInTheSameGroup(ColumnGroupHeaderLayer layer, int level, int fromPosition, int toPosition) {
        Group fromColumnGroup = layer.getGroupModel(level).getGroupByPosition(fromPosition);
        Group toColumnGroup = layer.getGroupModel(level).getGroupByPosition(toPosition);

        return fromColumnGroup != null && toColumnGroup != null && fromColumnGroup == toColumnGroup;
    }

    /**
     * Checks if the column positions at the given x coordinates belong to the
     * same group or not.
     *
     * @param natLayer
     *            The layer to which the given positions match. Typically the
     *            NatTable itself.
     * @param startX
     *            The x coordinate of the column that should be checked.
     *            Typically the drag start x coordinate.
     * @param endX
     *            The x coordinate of the column that should be checked.
     *            Typically the drag end x coordinate.
     * @param layer
     *            The {@link ColumnGroupHeaderLayer} which is needed to perform
     *            the check against.
     * @param level
     *            The grouping level to check.
     * @return <code>true</code> if there is a column group boundary between
     *         startX and endX, <code>false</code> if both positions are in the
     *         same group.
     *
     * @since 1.6
     */
    public static boolean isBetweenTwoGroups(ILayer natLayer, int startX, int endX, ColumnGroupHeaderLayer layer, int level) {
        int natFromPosition = natLayer.getColumnPositionByX(startX);
        int natToPosition = natLayer.getColumnPositionByX(endX);

        // convert grid position to layer position
        int fromPosition = LayerUtil.convertColumnPosition(natLayer, natFromPosition, layer.getPositionLayer());
        int toPosition = LayerUtil.convertColumnPosition(natLayer, natToPosition, layer.getPositionLayer());

        boolean result = !ColumnGroupUtils.isInTheSameGroup(
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
     * Checks if the edge of a column position is the left-most or the right
     * most column on any level of a column group.
     *
     * @param columnGroupHeaderLayer
     *            The {@link ColumnGroupHeaderLayer} to handle the checks.
     * @param toPosition
     *            The position to check. Needs to be related to the
     *            positionLayer.
     * @param reorderToLeftEdge
     *            <code>true</code> if the check should be performed to the left
     *            edge or the right edge of the toPosition.
     * @param moveDirection
     *            The direction in which the reordering is performed.
     * @return <code>true</code> if the destination would be between two groups,
     *         <code>false</code> if the destination would be inside a group.
     *
     * @since 1.6
     */
    public static boolean isBetweenTwoGroups(
            ColumnGroupHeaderLayer columnGroupHeaderLayer, int toPosition, boolean reorderToLeftEdge, MoveDirectionEnum moveDirection) {

        if ((toPosition == 0 && reorderToLeftEdge)
                || (toPosition == (columnGroupHeaderLayer.getPositionLayer().getColumnCount() - 1) && !reorderToLeftEdge)) {
            // start or end of the table
            return true;
        }

        int toPositionToCheck = toPosition;
        if (reorderToLeftEdge
                && MoveDirectionEnum.RIGHT == moveDirection) {
            toPositionToCheck--;
        }

        boolean valid = true;
        for (int level = 0; level < columnGroupHeaderLayer.getLevelCount(); level++) {
            if (MoveDirectionEnum.RIGHT == moveDirection) {
                valid = !isInTheSameGroup(columnGroupHeaderLayer, level, toPosition, toPositionToCheck);
            } else {
                valid = !isInTheSameGroup(columnGroupHeaderLayer, level, toPosition, toPositionToCheck + (reorderToLeftEdge ? -1 : 1));
            }

            if (!valid) {
                break;
            }
        }

        return valid;
    }

    /**
     * Checks if the edge of a column position for a specific grouping level is
     * the left-most or the right most column of a column group.
     *
     * @param columnGroupHeaderLayer
     *            The {@link ColumnGroupHeaderLayer} to handle the checks.
     * @param level
     *            The grouping level on which the check should be performed.
     * @param toPosition
     *            The position to check. Needs to be related to the
     *            positionLayer.
     * @param reorderToLeftEdge
     *            <code>true</code> if the check should be performed to the left
     *            edge or the right edge of the toPosition.
     * @param moveDirection
     *            The direction in which the reordering is performed.
     * @return <code>true</code> if the destination would be between two groups,
     *         <code>false</code> if the destination would be inside a group.
     *
     * @since 1.6
     */
    public static boolean isBetweenTwoGroups(
            ColumnGroupHeaderLayer columnGroupHeaderLayer, int level, int toPosition, boolean reorderToLeftEdge, MoveDirectionEnum moveDirection) {

        if ((toPosition == 0 && reorderToLeftEdge)
                || (toPosition == (columnGroupHeaderLayer.getPositionLayer().getColumnCount() - 1) && !reorderToLeftEdge)) {
            // start or end of the table
            return true;
        }

        int toPositionToCheck = toPosition;
        if (MoveDirectionEnum.RIGHT == moveDirection) {
            if (reorderToLeftEdge) {
                toPositionToCheck--;
            } else {
                toPositionToCheck++;
            }
        }

        boolean valid = true;
        if (MoveDirectionEnum.RIGHT == moveDirection) {
            valid = !isInTheSameGroup(columnGroupHeaderLayer, level, toPosition, toPositionToCheck);
        } else {
            valid = !isInTheSameGroup(columnGroupHeaderLayer, level, toPosition, toPositionToCheck + (reorderToLeftEdge ? -1 : 1));
        }

        if (valid && level > 0) {
            // check on deeper levels
            valid = isBetweenTwoGroups(columnGroupHeaderLayer, level - 1, toPosition, reorderToLeftEdge, moveDirection);
        }

        return valid;
    }

    /**
     * Checks if a reorder operation is valid by checking the unbreakable states
     * of the groups below the from and the to position.
     *
     * @param columnGroupHeaderLayer
     *            The {@link ColumnGroupHeaderLayer} to get the groups to check.
     * @param fromPosition
     *            The position from which a column should be reordered.
     * @param toPosition
     *            The position to which a column should be reordered.
     * @param reorderToLeftEdge
     *            <code>true</code> if the reorder should be performed to the
     *            left edge of the toPosition.
     * @return <code>true</code> if the reorder operation would be valid,
     *         <code>false</code> if the either the source or the target belongs
     *         to an unbreakable group.
     *
     * @since 1.6
     */
    public static boolean isReorderValid(ColumnGroupHeaderLayer columnGroupHeaderLayer, int fromPosition, int toPosition, boolean reorderToLeftEdge) {
        for (int level = 0; level < columnGroupHeaderLayer.getLevelCount(); level++) {
            if (!isReorderValid(columnGroupHeaderLayer, level, fromPosition, toPosition, reorderToLeftEdge)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a reorder operation is valid by checking the unbreakable states
     * of the groups below the from and the to position.
     *
     * @param columnGroupHeaderLayer
     *            The {@link ColumnGroupHeaderLayer} to get the groups to check.
     * @param level
     *            The grouping level that should be checked.
     * @param fromPosition
     *            The position from which a column should be reordered.
     * @param toPosition
     *            The position to which a column should be reordered.
     * @param reorderToLeftEdge
     *            <code>true</code> if the reorder should be performed to the
     *            left edge of the toPosition.
     * @return <code>true</code> if the reorder operation would be valid,
     *         <code>false</code> if the either the source or the target belongs
     *         to an unbreakable group.
     *
     * @since 1.6
     */
    public static boolean isReorderValid(
            ColumnGroupHeaderLayer columnGroupHeaderLayer, int level, int fromPosition, int toPosition, boolean reorderToLeftEdge) {

        MoveDirectionEnum moveDirection = PositionUtil.getHorizontalMoveDirection(fromPosition, toPosition);

        int toPositionToCheck = toPosition;
        if (MoveDirectionEnum.RIGHT == moveDirection && reorderToLeftEdge
                || toPosition == columnGroupHeaderLayer.getPositionLayer().getColumnCount() && !reorderToLeftEdge) {
            toPositionToCheck--;
        }

        boolean fromUnbreakable = false;
        boolean valid = true;
        // check if the from position is unbreakable
        GroupModel model = columnGroupHeaderLayer.getGroupModel(level);
        Group group = model.getGroupByPosition(fromPosition);
        if (group != null && group.isUnbreakable() && group.getVisibleSpan() > 1) {
            fromUnbreakable = true;
            valid = isInTheSameGroup(columnGroupHeaderLayer, level, fromPosition, toPositionToCheck);
        }

        // if the from position is part of an unbreakable group, we already know
        // the result
        if (!fromUnbreakable) {
            if ((toPosition == 0 && reorderToLeftEdge)
                    || (toPosition == (columnGroupHeaderLayer.getPositionLayer().getColumnCount() - 1) && !reorderToLeftEdge)) {
                // start or end of the table
                return true;
            }

            // check if the to position is unbreakable
            if (model.isPartOfAnUnbreakableGroup(toPositionToCheck)) {
                // check if the original toPosition is in another group to
                // see if we might reorder between groups
                if (MoveDirectionEnum.RIGHT == moveDirection) {
                    valid = !isInTheSameGroup(columnGroupHeaderLayer, level, toPosition, toPositionToCheck);
                } else {
                    valid = !isInTheSameGroup(columnGroupHeaderLayer, level, toPosition, toPositionToCheck + (reorderToLeftEdge ? -1 : 1));
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
     * @param fromColumnPositions
     *            The column positions to check.
     * @return <code>true</code> if the fromColumnPositions are all part of the
     *         given group.
     * @since 1.6
     */
    public static boolean isGroupReordered(Group fromGroup, int[] fromColumnPositions) {
        int[] visiblePositions = fromGroup.getVisiblePositions();
        if (visiblePositions.length > fromColumnPositions.length) {
            return false;
        } else if (visiblePositions.length < fromColumnPositions.length) {
            MutableIntList fromPositions = IntLists.mutable.of(fromColumnPositions);
            return fromPositions.containsAll(visiblePositions);
        } else {
            return Arrays.equals(visiblePositions, fromColumnPositions);
        }
    }
}
