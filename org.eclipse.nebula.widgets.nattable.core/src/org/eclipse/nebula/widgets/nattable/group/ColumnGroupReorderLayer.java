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
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 460052, 460074
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.group;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel.ColumnGroup;
import org.eclipse.nebula.widgets.nattable.group.command.GroupColumnReorderCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.command.GroupColumnReorderEndCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.command.GroupColumnReorderStartCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.command.GroupMultiColumnReorderCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnGroupCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnGroupEndCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnGroupStartCommandHandler;
import org.eclipse.nebula.widgets.nattable.group.command.ReorderColumnsAndGroupsCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.reorder.command.MultiColumnReorderCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adds functionality allowing the reordering of the the column groups.
 */
public class ColumnGroupReorderLayer extends AbstractLayerTransform implements IUniqueIndexLayer {

    private static final Logger LOG = LoggerFactory.getLogger(ColumnGroupReorderLayer.class);

    private final ColumnGroupModel model;

    private int reorderFromColumnPosition;

    public ColumnGroupReorderLayer(IUniqueIndexLayer underlyingLayer, ColumnGroupModel model) {
        super(underlyingLayer);

        this.model = model;

        registerCommandHandlers();
    }

    /**
     *
     * @param fromColumnPosition
     *            The column position of a column in the column group that
     *            should be reordered.
     * @param toColumnPosition
     *            The column position to which a column group should be
     *            reordered to.
     * @return <code>true</code> if the reorder command was executed and
     *         consumed successfully
     */
    public boolean reorderColumnGroup(int fromColumnPosition, int toColumnPosition) {
        int fromColumnIndex = this.underlyingLayer.getColumnIndexByPosition(fromColumnPosition);

        List<Integer> fromColumnPositions = getColumnGroupPositions(fromColumnIndex);
        return this.underlyingLayer.doCommand(
                new MultiColumnReorderCommand(this, fromColumnPositions, toColumnPosition));
    }

    /**
     * @return The {@link ColumnGroupModel} that is used to specify the column
     *         groups.
     */
    public ColumnGroupModel getModel() {
        return this.model;
    }

    /**
     * @since 1.3
     */
    @Override
    public IUniqueIndexLayer getUnderlyingLayer() {
        return (IUniqueIndexLayer) super.getUnderlyingLayer();
    }

    // Configuration

    @Override
    protected void registerCommandHandlers() {
        registerCommandHandler(new ReorderColumnGroupCommandHandler(this));
        registerCommandHandler(new ReorderColumnGroupStartCommandHandler(this));
        registerCommandHandler(new ReorderColumnGroupEndCommandHandler(this));
        registerCommandHandler(new ReorderColumnsAndGroupsCommandHandler(this));
        registerCommandHandler(new GroupColumnReorderCommandHandler(this));
        registerCommandHandler(new GroupMultiColumnReorderCommandHandler(this));
        registerCommandHandler(new GroupColumnReorderStartCommandHandler(this));
        registerCommandHandler(new GroupColumnReorderEndCommandHandler(this));
    }

    // Horizontal features

    // Columns

    @Override
    public int getColumnPositionByIndex(int columnIndex) {
        return getUnderlyingLayer().getColumnPositionByIndex(columnIndex);
    }

    // Vertical features

    // Rows

    @Override
    public int getRowPositionByIndex(int rowIndex) {
        return getUnderlyingLayer().getRowPositionByIndex(rowIndex);
    }

    // Column Groups

    /**
     * @param fromColumnIndex
     *            The column index of a column that is part of a column group.
     * @return The column positions for all the columns in the column group
     *         specified by the given index.
     */
    public List<Integer> getColumnGroupPositions(int fromColumnIndex) {
        List<Integer> fromColumnIndexes =
                this.model.getColumnGroupByIndex(fromColumnIndex).getMembers();
        List<Integer> fromColumnPositions = new ArrayList<>(fromColumnIndexes.size());

        for (Integer columnIndex : fromColumnIndexes) {
            fromColumnPositions.add(
                    getUnderlyingLayer().getColumnPositionByIndex(columnIndex.intValue()));
        }
        // These positions are actually consecutive but the Column Group does
        // not know about the order
        Collections.sort(fromColumnPositions);
        return fromColumnPositions;
    }

    /**
     * Used to support column reordering via drag and drop.
     *
     * @return The position from which a column reorder operation was started
     *         from.
     */
    public int getReorderFromColumnPosition() {
        return this.reorderFromColumnPosition;
    }

    /**
     * Used to support column reordering via drag and drop.
     *
     * @param fromColumnPosition
     *            The position from which a column reorder operation was started
     *            from.
     */
    public void setReorderFromColumnPosition(int fromColumnPosition) {
        this.reorderFromColumnPosition = fromColumnPosition;
    }

    /**
     * Updates the {@link ColumnGroupModel} with respect to a column reordering
     * operation.
     *
     * @param fromColumnPositions
     *            The column positions that should be reordered.
     * @param toColumnPosition
     *            The column position to which a column should be reordered.
     * @param reorderToLeftEdge
     *            Whether the toPosition should be calculated for attachment to
     *            the left or not.
     * @return <code>true</code> if only a {@link ColumnGroupModel} modification
     *         was triggered and no column reordering is necessary,
     *         <code>false</code> if additionally a column reordering needs to
     *         be performed.
     * @since 1.3
     */
    public boolean updateColumnGroupModel(
            List<Integer> fromColumnPositions, int toColumnPosition, boolean reorderToLeftEdge) {

        boolean consumeCommand = false;

        final int fromColumnPositionsCount = fromColumnPositions.size();

        if (toColumnPosition > fromColumnPositions.get(fromColumnPositionsCount - 1)) {
            // Moving from left to right
            int rightMostPosition = fromColumnPositions.get(fromColumnPositionsCount - 1);
            consumeCommand = updateColumnGroupModel(rightMostPosition, toColumnPosition, reorderToLeftEdge, fromColumnPositions);
        } else if (toColumnPosition < fromColumnPositions.get(fromColumnPositionsCount - 1)) {
            // Moving from right to left
            int leftMostPosition = fromColumnPositions.get(0);
            consumeCommand = updateColumnGroupModel(leftMostPosition, toColumnPosition, reorderToLeftEdge, fromColumnPositions);
        }

        return consumeCommand;
    }

    /**
     * Updates the {@link ColumnGroupModel} with respect to a column reordering
     * operation.
     *
     * @param fromColumnPosition
     *            The column position that should be reordered.
     * @param toColumnPosition
     *            The column position to which a column should be reordered.
     * @param reorderToLeftEdge
     *            Whether the toPosition should be calculated for attachment to
     *            the left or not.
     * @return <code>true</code> if only a {@link ColumnGroupModel} modification
     *         was triggered and no column reordering is necessary,
     *         <code>false</code> if additionally a column reordering needs to
     *         be performed.
     * @since 1.3
     */
    public boolean updateColumnGroupModel(
            int fromColumnPosition, int toColumnPosition, boolean reorderToLeftEdge) {

        return updateColumnGroupModel(
                fromColumnPosition, toColumnPosition, reorderToLeftEdge, Arrays.asList(fromColumnPosition));
    }

    /**
     * Transforms the given collection of column positions to an array of column
     * indexes.
     *
     * @param fromColumnPositions
     *            The column positions to transform.
     * @return An array that contains the indexes for the given positions.
     * @since 1.3
     */
    protected int[] getColumnIndexesForPositions(List<Integer> fromColumnPositions) {
        int[] fromColumnIndexes = new int[fromColumnPositions.size()];
        for (int i = 0; i < fromColumnPositions.size(); i++) {
            int from = fromColumnPositions.get(i);
            fromColumnIndexes[i] = this.getUnderlyingLayer().getColumnIndexByPosition(from);
        }
        return fromColumnIndexes;
    }

    /**
     * Updates the {@link ColumnGroupModel} with respect to a column reordering
     * operation.
     *
     * @param fromColumnPosition
     *            The column position that should be used for determining the
     *            necessary operation.
     * @param toColumnPosition
     *            The column position to which a column should be reordered.
     * @param reorderToLeftEdge
     *            Whether the toPosition should be calculated for attachment to
     *            the left or not.
     * @param fromColumnPositions
     *            The column positions that should be reordered.
     * @return <code>true</code> if only a {@link ColumnGroupModel} modification
     *         was triggered and no column reordering is necessary,
     *         <code>false</code> if additionally a column reordering needs to
     *         be performed.
     * @since 1.3
     */
    public boolean updateColumnGroupModel(
            int fromColumnPosition, int toColumnPosition, boolean reorderToLeftEdge, List<Integer> fromColumnPositions) {
        MoveDirectionEnum moveDirection =
                ColumnGroupUtils.getMoveDirection(fromColumnPosition, toColumnPosition);

        if (reorderToLeftEdge
                && toColumnPosition > 0
                && MoveDirectionEnum.RIGHT == moveDirection) {
            toColumnPosition--;
        }

        if (fromColumnPosition == -1 || toColumnPosition == -1) {
            LOG.error("Invalid reorder positions, fromPosition: {}, toPosition: {}", fromColumnPosition, toColumnPosition); //$NON-NLS-1$
        }

        boolean jump = Math.abs(fromColumnPosition - toColumnPosition) > 1;

        int fromColumnIndex = this.getUnderlyingLayer().getColumnIndexByPosition(fromColumnPosition);
        int toColumnIndex = this.getUnderlyingLayer().getColumnIndexByPosition(toColumnPosition);

        // check if the column to reorder is part of a group
        ColumnGroup fromColumnGroup = this.model.getColumnGroupByIndex(fromColumnIndex);
        ColumnGroup toColumnGroup = this.model.getColumnGroupByIndex(toColumnIndex);

        int[] fromColumnIndexes = getColumnIndexesForPositions(fromColumnPositions);

        boolean consumeCommand = false;

        // if from and to column groups are the same, let the reorder happen
        // without group modifications
        if (fromColumnGroup != null
                && toColumnGroup != null
                && fromColumnGroup.getName().equals(toColumnGroup.getName())
                && fromColumnPosition == toColumnPosition) {

            // special case
            // handle first/last column in last group
            if (fromColumnIndexes.length > 1) {
                if (MoveDirectionEnum.RIGHT == moveDirection) {
                    // check if there are columns to the left that are reordered
                    // too
                    int collectionPos = fromColumnPositions.indexOf(Integer.valueOf(fromColumnPosition));
                    if (collectionPos > 0) {
                        this.model.insertColumnIndexes(toColumnGroup.getName(), fromColumnIndexes);
                    } else {
                        this.model.removeColumnIndexes(fromColumnGroup.getName(), fromColumnIndexes);
                    }
                    consumeCommand = true;
                } else if (MoveDirectionEnum.LEFT == moveDirection) {
                    // check if there are columns to the right that are
                    // reordered too
                    int collectionPos = fromColumnPositions.indexOf(Integer.valueOf(fromColumnPosition));
                    if (collectionPos == 0) {
                        this.model.insertColumnIndexes(toColumnGroup.getName(), fromColumnIndexes);
                    } else {
                        this.model.removeColumnIndexes(fromColumnGroup.getName(), fromColumnIndexes);
                    }
                    consumeCommand = true;
                } else {
                    // if there are multiple columns to reorder but no move
                    // direction, we probably perform a reorder to add new
                    // columns to an existing group
                    consumeCommand = false;
                }
            } else {
                // only remove if we are at the edge of a column group
                if (ColumnGroupUtils.isLeftEdgeOfAColumnGroup(this, fromColumnPosition, fromColumnIndex, this.model)
                        || ColumnGroupUtils.isRightEdgeOfAColumnGroup(this, fromColumnPosition, fromColumnIndex, this.model)) {
                    this.model.removeColumnIndexes(fromColumnGroup.getName(), fromColumnIndexes);
                }
                consumeCommand = true;
            }
        } else if (fromColumnGroup == null && toColumnGroup != null) {
            // special case
            // add the column to the column group if we step instead of jumping
            consumeCommand = this.model.insertColumnIndexes(toColumnGroup.getName(), fromColumnIndexes);
            if (jump) {
                consumeCommand = false;
            }
        } else if (fromColumnGroup != null && toColumnGroup == null) {
            this.model.removeColumnIndexes(fromColumnGroup.getName(), fromColumnIndexes);
        } else if (fromColumnGroup == null && toColumnGroup == null && fromColumnPosition == toColumnPosition) {
            // this might happen on drag and drop operations when trying to add
            // a column back into an adjacent column group
            int adjacentPos = (moveDirection == MoveDirectionEnum.RIGHT) ? fromColumnPosition + 1 : fromColumnPosition - 1;
            int adjacentIndex = this.getUnderlyingLayer().getColumnIndexByPosition(adjacentPos);
            // check if there is an adjacent column group
            ColumnGroup adjacentColumnGroup = this.model.getColumnGroupByIndex(adjacentIndex);
            if (adjacentColumnGroup != null) {
                this.model.insertColumnIndexes(adjacentColumnGroup.getName(), fromColumnIndexes);
                consumeCommand = false;
            }
        } else if (fromColumnGroup != null
                && toColumnGroup != null
                && !fromColumnGroup.getName().equals(toColumnGroup.getName())) {
            // the target location would be the first position in another column
            // group, but since we do not reorder to left edge, we only move out
            // of the current column group
            if (MoveDirectionEnum.RIGHT == moveDirection) {
                if (toColumnGroup != null) {
                    // check if reorder is started from right edge of a group
                    boolean fromRightEdge = ColumnGroupUtils.isRightEdgeOfAColumnGroup(
                            this, fromColumnPosition, fromColumnIndex, this.model);

                    boolean removed = true;
                    if (fromColumnGroup != null) {
                        removed = this.model.removeColumnIndexes(fromColumnGroup.getName(), fromColumnIndexes);
                    }

                    if (removed && ((!fromRightEdge && !jump) || jump)) {
                        // column removed from another group
                        // not from the right edge of another group
                        // add it to the following group
                        consumeCommand = this.model.insertColumnIndexes(toColumnGroup.getName(), fromColumnIndexes);
                        if (jump) {
                            consumeCommand = !jump;
                        }
                    } else {
                        consumeCommand = !jump;
                    }
                }
            } else if (MoveDirectionEnum.LEFT == moveDirection) {
                if (toColumnGroup != null) {
                    // check if reorder is started from left edge of a group
                    boolean fromLeftEdge = ColumnGroupUtils.isLeftEdgeOfAColumnGroup(
                            this, fromColumnPosition, fromColumnIndex, this.model);

                    boolean removed = true;
                    if (fromColumnGroup != null) {
                        removed = this.model.removeColumnIndexes(fromColumnGroup.getName(), fromColumnIndexes);
                    }

                    if (removed && ((!fromLeftEdge && !jump) || jump)) {
                        // column removed from another group
                        // not from the left edge of another group
                        // add it to the previous group
                        consumeCommand = this.model.insertColumnIndexes(toColumnGroup.getName(), fromColumnIndexes);
                        if (jump) {
                            consumeCommand = !jump;
                        }
                    } else {
                        consumeCommand = !jump;
                    }
                }
            }
        }

        // don't consume the command to perform reordering
        return consumeCommand;
    }
}
