/*******************************************************************************
 * Copyright (c) 2012, 2013, 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth - added ITraversalStrategy handling
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.ITraversalStrategy.TraversalScope;
import org.eclipse.nebula.widgets.nattable.selection.command.MoveSelectionCommand;

/**
 * Specifies the semantics of moving the selection in the table, based on
 * selecting the adjoining cell(s).
 */
public class MoveCellSelectionCommandHandler extends MoveSelectionCommandHandler<MoveSelectionCommand> {

    protected PositionCoordinate lastSelectedCellPosition;
    protected int newSelectedColumnPosition;
    protected int newSelectedRowPosition;

    /**
     * Create a MoveCellSelectionCommandHandler for the given
     * {@link SelectionLayer}. Uses the
     * {@link ITraversalStrategy#AXIS_TRAVERSAL_STRATEGY} as default strategy
     * for selection movement.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} on which the selection should be
     *            performed.
     */
    public MoveCellSelectionCommandHandler(SelectionLayer selectionLayer) {
        super(selectionLayer);
    }

    /**
     * Create a MoveCellSelectionCommandHandler for the given
     * {@link SelectionLayer}.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} on which the selection should be
     *            performed.
     * @param traversalStrategy
     *            The strategy that should be used for selection movements. Can
     *            not be <code>null</code>.
     */
    public MoveCellSelectionCommandHandler(SelectionLayer selectionLayer, ITraversalStrategy traversalStrategy) {
        super(selectionLayer, traversalStrategy);
    }

    /**
     * Create a MoveCellSelectionCommandHandler for the given
     * {@link SelectionLayer} .
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} on which the selection should be
     *            performed.
     * @param horizontalTraversalStrategy
     *            The strategy that should be used for horizontal selection
     *            movements. Can not be <code>null</code>.
     * @param verticalTraversalStrategy
     *            The strategy that should be used for vertical selection
     *            movements. Can not be <code>null</code>.
     */
    public MoveCellSelectionCommandHandler(SelectionLayer selectionLayer,
            ITraversalStrategy horizontalTraversalStrategy, ITraversalStrategy verticalTraversalStrategy) {
        super(selectionLayer, horizontalTraversalStrategy, verticalTraversalStrategy);
    }

    @Override
    protected void moveLastSelectedLeft(ITraversalStrategy traversalStrategy, boolean withShiftMask, boolean withControlMask) {
        if (this.selectionLayer.hasColumnSelection()) {
            this.lastSelectedCellPosition = this.selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
            ILayerCell lastSelectedCell = this.selectionLayer.getCellByPosition(
                    this.lastSelectedCellPosition.columnPosition,
                    this.lastSelectedCellPosition.rowPosition);
            if (lastSelectedCell != null) {
                int stepSize = traversalStrategy.getStepCount();
                this.newSelectedColumnPosition = (stepSize >= 0) ? (lastSelectedCell.getOriginColumnPosition() - stepSize) : 0;

                this.newSelectedRowPosition = this.lastSelectedCellPosition.rowPosition;

                // boolean flag to stop traversal if calculated target is
                // invalid needed to avoid endless loop if there are no further
                // valid traversal targets
                boolean stopTraversalOnInvalid = false;

                if (this.newSelectedColumnPosition < 0) {
                    if (traversalStrategy.getTraversalScope().equals(TraversalScope.AXIS)) {
                        if (!traversalStrategy.isCycle()) {
                            // on axis scope with no cycle, stop moving
                            this.newSelectedColumnPosition = 0;
                            stopTraversalOnInvalid = true;
                        }
                        else {
                            // on axis scope with cycle, move to end
                            while (this.newSelectedColumnPosition < 0) {
                                this.newSelectedColumnPosition = this.newSelectedColumnPosition + this.selectionLayer.getColumnCount();
                            }
                        }
                    }
                    else if (traversalStrategy.getTraversalScope().equals(TraversalScope.TABLE)) {
                        // on table scope, move to end
                        int rowMove = 0;
                        while (this.newSelectedColumnPosition < 0) {
                            this.newSelectedColumnPosition = this.newSelectedColumnPosition + this.selectionLayer.getColumnCount();
                            rowMove++;
                        }
                        this.newSelectedRowPosition = this.newSelectedRowPosition - rowMove;
                        if (this.newSelectedRowPosition < 0) {
                            if (traversalStrategy.isCycle()) {
                                // at the top and cycle so go to bottom
                                this.newSelectedRowPosition = this.newSelectedRowPosition + this.selectionLayer.getRowCount();
                            }
                            else {
                                // at the top and no cycle so stop moving
                                this.newSelectedColumnPosition = 0;
                                this.newSelectedRowPosition = 0;
                                stopTraversalOnInvalid = true;
                            }
                        }
                    }
                }

                if (positionMoved()) {
                    // check if calculated target is valid, otherwise move to
                    // adjacent
                    if (!traversalStrategy.isValidTarget(lastSelectedCell,
                            this.selectionLayer.getCellByPosition(this.newSelectedColumnPosition, this.newSelectedRowPosition))) {

                        if (!stopTraversalOnInvalid) {
                            moveLastSelectedLeft(
                                    createIncrementalStrategy(traversalStrategy),
                                    withShiftMask, withControlMask);
                        }
                        else {
                            // since the calculated target is invalid and
                            // invalid traversal movement should stop, the new
                            // selected position is the last valid one
                            this.newSelectedColumnPosition = this.lastSelectedCellPosition.columnPosition;
                            this.newSelectedRowPosition = this.lastSelectedCellPosition.rowPosition;
                        }
                    }
                    else {
                        if (stepSize == SelectionLayer.MOVE_ALL && !withShiftMask) {
                            this.selectionLayer.clear(false);
                        }
                        this.selectionLayer.selectCell(this.newSelectedColumnPosition,
                                this.newSelectedRowPosition,
                                withShiftMask, withControlMask);
                        this.selectionLayer.fireCellSelectionEvent(
                                this.lastSelectedCellPosition.columnPosition,
                                this.lastSelectedCellPosition.rowPosition, true,
                                withShiftMask, withControlMask);
                    }
                }
            }
        }
    }

    @Override
    protected void moveLastSelectedRight(final ITraversalStrategy traversalStrategy, boolean withShiftMask, boolean withControlMask) {
        if (this.selectionLayer.hasColumnSelection()) {
            this.lastSelectedCellPosition = this.selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
            ILayerCell lastSelectedCell = this.selectionLayer.getCellByPosition(
                    this.lastSelectedCellPosition.columnPosition,
                    this.lastSelectedCellPosition.rowPosition);
            if (lastSelectedCell != null) {
                int stepSize = traversalStrategy.getStepCount();
                this.newSelectedColumnPosition = (stepSize >= 0)
                        ? (lastSelectedCell.getOriginColumnPosition() + lastSelectedCell.getColumnSpan() - 1 + stepSize)
                        : (this.selectionLayer.getColumnCount() - 1);

                this.newSelectedRowPosition = this.lastSelectedCellPosition.rowPosition;

                // boolean flag to stop traversal if calculated target is
                // invalid needed to avoid endless loop if there are no further
                // valid traversal targets
                boolean stopTraversalOnInvalid = false;

                if (this.newSelectedColumnPosition >= this.selectionLayer.getColumnCount()) {
                    if (traversalStrategy.getTraversalScope().equals(TraversalScope.AXIS)) {
                        if (!traversalStrategy.isCycle()) {
                            // on axis scope with no cycle, stop moving
                            this.newSelectedColumnPosition = this.selectionLayer.getColumnCount() - 1;
                            stopTraversalOnInvalid = true;
                        }
                        else {
                            // on axis scope with cycle, start over at table
                            // beginning
                            while (this.newSelectedColumnPosition >= this.selectionLayer.getColumnCount()) {
                                this.newSelectedColumnPosition = this.newSelectedColumnPosition - this.selectionLayer.getColumnCount();
                            }
                        }
                    }
                    else if (traversalStrategy.getTraversalScope().equals(TraversalScope.TABLE)) {
                        // on table scope, start over at table beginning
                        int rowMove = 0;
                        while (this.newSelectedColumnPosition >= this.selectionLayer.getColumnCount()) {
                            this.newSelectedColumnPosition = this.newSelectedColumnPosition - this.selectionLayer.getColumnCount();
                            rowMove++;
                        }
                        this.newSelectedRowPosition = this.newSelectedRowPosition + rowMove;
                        if (this.newSelectedRowPosition >= this.selectionLayer.getRowCount()) {
                            if (traversalStrategy.isCycle()) {
                                // at the bottom and cycle so go to top
                                this.newSelectedRowPosition = this.newSelectedRowPosition - this.selectionLayer.getRowCount();
                            }
                            else {
                                // at the bottom and no cycle so stop moving
                                this.newSelectedColumnPosition = this.selectionLayer.getColumnCount() - 1;
                                this.newSelectedRowPosition = this.selectionLayer.getRowCount() - 1;
                                stopTraversalOnInvalid = true;
                            }
                        }
                    }
                }

                if (positionMoved()) {
                    if (stepSize == SelectionLayer.MOVE_ALL && !withShiftMask) {
                        this.selectionLayer.clear(false);
                    }

                    // check if calculated target is valid, otherwise move to
                    // adjacent
                    if (!traversalStrategy.isValidTarget(lastSelectedCell,
                            this.selectionLayer.getCellByPosition(this.newSelectedColumnPosition, this.newSelectedRowPosition))) {

                        if (!stopTraversalOnInvalid) {
                            moveLastSelectedRight(
                                    createIncrementalStrategy(traversalStrategy),
                                    withShiftMask, withControlMask);
                        }
                        else {
                            // since the calculated target is invalid and
                            // invalid traversal movement should stop, the new
                            // selected position is the last valid one
                            this.newSelectedColumnPosition = this.lastSelectedCellPosition.columnPosition;
                            this.newSelectedRowPosition = this.lastSelectedCellPosition.rowPosition;
                        }
                    }
                    else {
                        this.selectionLayer.selectCell(
                                this.newSelectedColumnPosition, this.newSelectedRowPosition,
                                withShiftMask, withControlMask);
                        this.selectionLayer.fireCellSelectionEvent(
                                this.lastSelectedCellPosition.columnPosition,
                                this.lastSelectedCellPosition.rowPosition, true,
                                withShiftMask, withControlMask);
                    }
                }
            }
        }
    }

    @Override
    protected void moveLastSelectedUp(ITraversalStrategy traversalStrategy, boolean withShiftMask, boolean withControlMask) {
        if (this.selectionLayer.hasRowSelection()) {
            this.lastSelectedCellPosition = this.selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
            ILayerCell lastSelectedCell = this.selectionLayer.getCellByPosition(
                    this.lastSelectedCellPosition.columnPosition,
                    this.lastSelectedCellPosition.rowPosition);
            if (lastSelectedCell != null) {
                int stepSize = traversalStrategy.getStepCount();
                this.newSelectedColumnPosition = this.lastSelectedCellPosition.columnPosition;

                this.newSelectedRowPosition = (stepSize >= 0) ? lastSelectedCell.getOriginRowPosition() - stepSize : 0;

                // boolean flag to stop traversal if calculated target is
                // invalid needed to avoid endless loop if there are no further
                // valid traversal targets
                boolean stopTraversalOnInvalid = false;

                if (this.newSelectedRowPosition < 0) {
                    if (traversalStrategy.getTraversalScope().equals(TraversalScope.AXIS)) {
                        if (!traversalStrategy.isCycle()) {
                            // on axis scope with no cycle, stop moving
                            this.newSelectedRowPosition = 0;
                            stopTraversalOnInvalid = true;
                        }
                        else {
                            // on axis scope with cycle, move to bottom
                            while (this.newSelectedRowPosition < 0) {
                                this.newSelectedRowPosition = this.newSelectedRowPosition + this.selectionLayer.getRowCount();
                            }
                        }
                    }
                    else if (traversalStrategy.getTraversalScope().equals(TraversalScope.TABLE)) {
                        // on table scope, move to bottom
                        int columnMove = 0;
                        while (this.newSelectedRowPosition < 0) {
                            this.newSelectedRowPosition = this.newSelectedRowPosition + this.selectionLayer.getRowCount();
                            columnMove++;
                        }
                        this.newSelectedColumnPosition = this.newSelectedColumnPosition - columnMove;
                        if (this.newSelectedColumnPosition < 0) {
                            if (traversalStrategy.isCycle()) {
                                // at the beginning and cycle so go to end
                                this.newSelectedColumnPosition = this.newSelectedColumnPosition + this.selectionLayer.getColumnCount();
                            }
                            else {
                                // at the top and no cycle so stop moving
                                this.newSelectedColumnPosition = 0;
                                this.newSelectedRowPosition = 0;
                                stopTraversalOnInvalid = true;
                            }
                        }
                    }
                }

                if (positionMoved()) {
                    // check if calculated target is valid, otherwise move to
                    // adjacent
                    if (!traversalStrategy.isValidTarget(lastSelectedCell,
                            this.selectionLayer.getCellByPosition(this.newSelectedColumnPosition, this.newSelectedRowPosition))) {

                        if (!stopTraversalOnInvalid) {
                            moveLastSelectedUp(
                                    createIncrementalStrategy(traversalStrategy),
                                    withShiftMask, withControlMask);
                        }
                        else {
                            // since the calculated target is invalid and
                            // invalid traversal movement should stop, the new
                            // selected position is the last valid one
                            this.newSelectedColumnPosition = this.lastSelectedCellPosition.columnPosition;
                            this.newSelectedRowPosition = this.lastSelectedCellPosition.rowPosition;
                        }
                    }
                    else {
                        this.selectionLayer.selectCell(
                                this.newSelectedColumnPosition,
                                this.newSelectedRowPosition,
                                withShiftMask, withControlMask);
                        this.selectionLayer.fireCellSelectionEvent(
                                this.lastSelectedCellPosition.columnPosition,
                                this.lastSelectedCellPosition.rowPosition, true,
                                withShiftMask, withControlMask);
                    }
                }
            }
        }
    }

    @Override
    protected void moveLastSelectedDown(ITraversalStrategy traversalStrategy, boolean withShiftMask, boolean withControlMask) {
        if (this.selectionLayer.hasRowSelection()) {
            this.lastSelectedCellPosition = this.selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
            ILayerCell lastSelectedCell = this.selectionLayer.getCellByPosition(
                    this.lastSelectedCellPosition.columnPosition,
                    this.lastSelectedCellPosition.rowPosition);
            if (lastSelectedCell != null) {
                int stepSize = traversalStrategy.getStepCount();
                this.newSelectedColumnPosition = this.lastSelectedCellPosition.columnPosition;

                this.newSelectedRowPosition = (stepSize >= 0)
                        ? lastSelectedCell.getOriginRowPosition() + lastSelectedCell.getRowSpan() - 1 + stepSize
                        : this.selectionLayer.getRowCount() - 1;

                // boolean flag to stop traversal if calculated target is
                // invalid needed to avoid endless loop if there are no further
                // valid traversal targets
                boolean stopTraversalOnInvalid = false;

                if (this.newSelectedRowPosition >= this.selectionLayer.getRowCount()) {
                    if (traversalStrategy.getTraversalScope().equals(TraversalScope.AXIS)) {
                        if (!traversalStrategy.isCycle()) {
                            // on axis scope with no cycle, stop moving
                            this.newSelectedRowPosition = this.selectionLayer.getRowCount() - 1;
                            stopTraversalOnInvalid = true;
                        }
                        else {
                            // on axis scope with cycle, move to top
                            while (this.newSelectedRowPosition >= this.selectionLayer.getRowCount()) {
                                this.newSelectedRowPosition = this.newSelectedRowPosition - this.selectionLayer.getRowCount();
                            }
                        }
                    }
                    else if (traversalStrategy.getTraversalScope().equals(TraversalScope.TABLE)) {
                        // on table scope, move to top
                        int columnMove = 0;
                        while (this.newSelectedRowPosition >= this.selectionLayer.getRowCount()) {
                            this.newSelectedRowPosition = this.newSelectedRowPosition - this.selectionLayer.getRowCount();
                            columnMove++;
                        }
                        this.newSelectedColumnPosition = this.newSelectedColumnPosition + columnMove;
                        if (this.newSelectedColumnPosition >= this.selectionLayer.getColumnCount()) {
                            if (traversalStrategy.isCycle()) {
                                // at the end and cycle so go to beginning
                                this.newSelectedColumnPosition = this.newSelectedColumnPosition - this.selectionLayer.getColumnCount();
                            }
                            else {
                                // at the end and no cycle so stop moving
                                this.newSelectedColumnPosition = this.selectionLayer.getColumnCount() - 1;
                                this.newSelectedRowPosition = this.selectionLayer.getRowCount() - 1;
                                stopTraversalOnInvalid = true;
                            }
                        }
                    }
                }

                if (positionMoved()) {
                    // check if calculated target is valid, otherwise move to
                    // adjacent
                    if (!traversalStrategy.isValidTarget(lastSelectedCell,
                            this.selectionLayer.getCellByPosition(this.newSelectedColumnPosition, this.newSelectedRowPosition))) {

                        if (!stopTraversalOnInvalid) {
                            moveLastSelectedDown(
                                    createIncrementalStrategy(traversalStrategy),
                                    withShiftMask, withControlMask);
                        }
                        else {
                            // since the calculated target is invalid and
                            // invalid traversal movement should stop, the new
                            // selected position is the last valid one
                            this.newSelectedColumnPosition = this.lastSelectedCellPosition.columnPosition;
                            this.newSelectedRowPosition = this.lastSelectedCellPosition.rowPosition;
                        }
                    }
                    else {
                        this.selectionLayer.selectCell(
                                this.newSelectedColumnPosition, this.newSelectedRowPosition,
                                withShiftMask, withControlMask);
                        this.selectionLayer.fireCellSelectionEvent(
                                this.lastSelectedCellPosition.columnPosition,
                                this.lastSelectedCellPosition.rowPosition, true,
                                withShiftMask, withControlMask);
                    }
                }
            }
        }
    }

    /**
     * Creates a {@link ITraversalStrategy} that wraps the given base strategy
     * but returning the step count + 1. Used to perform incremental movement in
     * case the base strategy specifies logic to determine whether a target cell
     * is a valid move target or not.
     *
     * @param baseStrategy
     *            The {@link ITraversalStrategy} to wrap.
     * @return A {@link ITraversalStrategy} that wraps the given base strategy
     *         using the given step count.
     */
    protected ITraversalStrategy createIncrementalStrategy(final ITraversalStrategy baseStrategy) {
        return new ITraversalStrategy() {

            @Override
            public TraversalScope getTraversalScope() {
                return baseStrategy.getTraversalScope();
            }

            @Override
            public boolean isCycle() {
                return baseStrategy.isCycle();
            }

            @Override
            public int getStepCount() {
                return baseStrategy.getStepCount() + 1;
            }

            @Override
            public boolean isValidTarget(ILayerCell from, ILayerCell to) {
                return baseStrategy.isValidTarget(from, to);
            }

        };
    }

    /**
     *
     * @return <code>true</code> if the selection moved in any direction,
     *         <code>false</code> if the selection stays at the same position
     */
    protected boolean positionMoved() {
        return (this.newSelectedColumnPosition != this.lastSelectedCellPosition.columnPosition
        || this.newSelectedRowPosition != this.lastSelectedCellPosition.rowPosition);
    }

    @Override
    public Class<MoveSelectionCommand> getCommandClass() {
        return MoveSelectionCommand.class;
    }

}
