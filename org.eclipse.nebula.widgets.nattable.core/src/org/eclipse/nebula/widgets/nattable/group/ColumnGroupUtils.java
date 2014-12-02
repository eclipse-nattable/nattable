/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
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
import java.util.Collections;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;

public class ColumnGroupUtils {

    public static MoveDirectionEnum getMoveDirection(int fromColumnPosition,
            int toColumnPosition) {
        if (fromColumnPosition > toColumnPosition) {
            return MoveDirectionEnum.LEFT;
        } else if (fromColumnPosition < toColumnPosition) {
            return MoveDirectionEnum.RIGHT;
        } else {
            return MoveDirectionEnum.NONE;
        }
    }

    public static boolean isInTheSameGroup(int fromColumnIndex,
            int toColumnIndex, ColumnGroupModel model) {
        ColumnGroup fromColumnGroup = model
                .getColumnGroupByIndex(fromColumnIndex);
        ColumnGroup toColumnGroup = model.getColumnGroupByIndex(toColumnIndex);

        return fromColumnGroup != null && toColumnGroup != null
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
     * @param layer
     * @param underlyingLayer
     * @param model
     *
     * @return <code>TRUE</code> if the given <code>columnIndex</code> is either
     *         a defined static column or (if not) the first visible column the
     *         it's group
     */
    public static boolean isStaticOrFirstVisibleColumn(int columnIndex,
            ILayer layer, IUniqueIndexLayer underlyingLayer,
            ColumnGroupModel model) {
        ColumnGroup columnGroup = model.getColumnGroupByIndex(columnIndex);

        if (columnGroup.getStaticColumnIndexes().size() == 0) {
            return isFirstVisibleColumnIndexInGroup(columnIndex, layer,
                    underlyingLayer, model);
        } else {
            return model.isStaticColumn(columnIndex);
        }
    }

    public static boolean isFirstVisibleColumnIndexInGroup(int columnIndex,
            ILayer layer, IUniqueIndexLayer underlyingLayer,
            ColumnGroupModel model) {
        if (isColumnIndexHiddenInUnderLyingLayer(columnIndex, layer,
                underlyingLayer)) {
            return false;
        }

        int columnPosition = underlyingLayer
                .getColumnPositionByIndex(columnIndex);
        List<Integer> columnIndexesInGroup = model.getColumnGroupByIndex(
                columnIndex).getMembers();
        List<Integer> previousVisibleColumnIndexes = new ArrayList<Integer>();

        // All other indexes in the column group which are visible and
        // are positioned before me
        for (Integer currentIndex : columnIndexesInGroup) {
            int currentPosition = underlyingLayer
                    .getColumnPositionByIndex(currentIndex.intValue());
            if (!isColumnIndexHiddenInUnderLyingLayer(currentIndex.intValue(),
                    layer, underlyingLayer) && currentPosition < columnPosition) {
                previousVisibleColumnIndexes.add(currentIndex);
            }
        }

        return previousVisibleColumnIndexes.isEmpty();
    }

    public static boolean isLastVisibleColumnIndexInGroup(int columnIndex,
            ILayer layer, IUniqueIndexLayer underlyingLayer,
            ColumnGroupModel model) {
        if (isColumnIndexHiddenInUnderLyingLayer(columnIndex, layer,
                underlyingLayer)) {
            return false;
        }

        List<Integer> visibleIndexesToTheRight = getVisibleIndexesToTheRight(
                columnIndex, layer, underlyingLayer, model);
        return visibleIndexesToTheRight.size() == 1
                && visibleIndexesToTheRight.get(0).intValue() == columnIndex;
    }

    /**
     * Inclusive of the columnIndex passed as the parameter.
     */
    public static List<Integer> getVisibleIndexesToTheRight(int columnIndex,
            ILayer layer, IUniqueIndexLayer underlyingLayer,
            ColumnGroupModel model) {
        ColumnGroup columnGroup = model.getColumnGroupByIndex(columnIndex);

        if (columnGroup.isCollapsed()) {
            return Collections.emptyList();
        }

        List<Integer> columnIndexesInGroup = columnGroup.getMembers();
        int columnPosition = underlyingLayer
                .getColumnPositionByIndex(columnIndex);
        List<Integer> visibleColumnIndexesOnRight = new ArrayList<Integer>();

        for (Integer currentIndex : columnIndexesInGroup) {
            int currentPosition = underlyingLayer
                    .getColumnPositionByIndex(currentIndex.intValue());
            if (!isColumnIndexHiddenInUnderLyingLayer(currentIndex.intValue(),
                    layer, underlyingLayer)
                    && currentPosition >= columnPosition) {
                visibleColumnIndexesOnRight.add(currentIndex);
            }
        }

        return visibleColumnIndexesOnRight;
    }

    public static boolean isColumnIndexHiddenInUnderLyingLayer(int columnIndex,
            ILayer layer, IUniqueIndexLayer underlyingLayer) {
        return underlyingLayer.getColumnPositionByIndex(columnIndex) == -1;
    }

    public static boolean isColumnPositionHiddenInUnderLyingLayer(
            int columnPosition, ILayer layer, IUniqueIndexLayer underlyingLayer) {
        if (columnPosition < underlyingLayer.getColumnCount()
                && columnPosition >= 0) {
            int columnIndex = underlyingLayer
                    .getColumnIndexByPosition(columnPosition);
            return isColumnIndexHiddenInUnderLyingLayer(columnIndex, layer,
                    underlyingLayer);
        }
        return true;
    }

    /**
     * See ColumnGroupUtilsTest
     *
     * @return TRUE if the given column is the <i>right</i> most column in a
     *         group
     */
    public static boolean isRightEdgeOfAColumnGroup(ILayer natLayer,
            int columnPosition, int columnIndex, ColumnGroupModel model) {
        int nextColumnPosition = columnPosition + 1;

        if (nextColumnPosition < natLayer.getColumnCount()) {
            int nextColumnIndex = natLayer
                    .getColumnIndexByPosition(nextColumnPosition);
            if ((model.isPartOfAGroup(columnIndex) && !model
                    .isPartOfAGroup(nextColumnIndex))) {
                return true;
            }
            if ((model.isPartOfAGroup(columnIndex) && model
                    .isPartOfAGroup(nextColumnIndex))
                    && !ColumnGroupUtils.isInTheSameGroup(columnIndex,
                            nextColumnIndex, model)) {
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
    public static boolean isLeftEdgeOfAColumnGroup(ILayer natLayer,
            int columnPosition, int columnIndex, ColumnGroupModel model) {
        int previousColumnPosition = columnPosition - 1;

        // First column && in a group
        if (columnPosition == 0 && model.isPartOfAGroup(columnIndex)) {
            return true;
        }

        if (previousColumnPosition >= 0) {
            int previousColumnIndex = natLayer
                    .getColumnIndexByPosition(previousColumnPosition);
            if ((model.isPartOfAGroup(columnIndex) && !model
                    .isPartOfAGroup(previousColumnIndex))) {
                return true;
            }
            if ((model.isPartOfAGroup(columnIndex) && model
                    .isPartOfAGroup(previousColumnIndex))
                    && !ColumnGroupUtils.isInTheSameGroup(columnIndex,
                            previousColumnIndex, model)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return TRUE if there is a column group boundary between startX and endX
     */
    public static boolean isBetweenTwoGroups(ILayer natLayer, int startX,
            int endX, ColumnGroupModel model) {
        return !ColumnGroupUtils.isInTheSameGroup(
                natLayer.getColumnIndexByPosition(natLayer
                        .getColumnPositionByX(startX)), natLayer
                        .getColumnIndexByPosition(natLayer
                                .getColumnPositionByX(endX)), model);
    }

}
