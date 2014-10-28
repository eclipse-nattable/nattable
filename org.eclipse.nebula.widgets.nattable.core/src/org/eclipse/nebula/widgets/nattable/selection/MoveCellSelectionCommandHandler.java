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

                if (this.newSelectedColumnPosition < 0) {
                    if (traversalStrategy.getTraversalScope().equals(TraversalScope.AXIS)) {
                        if (!traversalStrategy.isCycle()) {
                            // on axis scope with no cycle, stop moving
                            this.newSelectedColumnPosition = 0;
                        }
                        else {
                            // on axis scope with cycle, move to end
                            // TODO check if end is valid using step count
                            this.newSelectedColumnPosition = this.selectionLayer.getColumnCount() - 1;
                        }
                    }
                    else if (traversalStrategy.getTraversalScope().equals(TraversalScope.TABLE)) {
                        // on table scope, move to end
                        // TODO check if end is valid using step count
                        this.newSelectedColumnPosition = this.selectionLayer.getColumnCount() - 1;
                        this.newSelectedRowPosition = this.newSelectedRowPosition - 1;
                        if (this.newSelectedRowPosition < 0) {
                            if (traversalStrategy.isCycle()) {
                                // at the top and cycle so go to bottom
                                this.newSelectedRowPosition = this.selectionLayer.getRowCount() - 1;
                            }
                            else {
                                // at the top and no cycle so stop moving
                                this.newSelectedColumnPosition = 0;
                                this.newSelectedRowPosition = 0;
                            }
                        }
                    }
                }

                if (this.newSelectedColumnPosition != this.lastSelectedCellPosition.columnPosition) {
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

    @Override
    protected void moveLastSelectedRight(ITraversalStrategy traversalStrategy, boolean withShiftMask, boolean withControlMask) {
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

                        if (this.newSelectedColumnPosition >= this.selectionLayer.getColumnCount()) {
                            if (traversalStrategy.getTraversalScope().equals(TraversalScope.AXIS)) {
                                if (!traversalStrategy.isCycle()) {
                                    // on axis scope with no cycle, stop moving
                                    this.newSelectedColumnPosition = this.selectionLayer.getColumnCount() - 1;
                                }
                                else {
                                    // on axis scope with cycle, move to 0
                                    // TODO check if 0 is valid using step count
                                    this.newSelectedColumnPosition = 0;
                                }
                            }
                            else if (traversalStrategy.getTraversalScope().equals(TraversalScope.TABLE)) {
                                // on table scope, move to 0
                                // TODO check if 0 is valid using step count
                                this.newSelectedColumnPosition = 0;
                                this.newSelectedRowPosition = this.newSelectedRowPosition + 1;
                                if (this.newSelectedRowPosition >= this.selectionLayer.getRowCount()) {
                                    if (traversalStrategy.isCycle()) {
                                        // at the bottom and cycle so go to top
                                        this.newSelectedRowPosition = 0;
                                    }
                                    else {
                                        // at the bottom and no cycle so stop moving
                                        this.newSelectedColumnPosition = this.selectionLayer.getColumnCount() - 1;
                                        this.newSelectedRowPosition = this.selectionLayer.getRowCount() - 1;
                                    }
                                }
                            }
                        }

                        if (this.newSelectedColumnPosition != this.lastSelectedCellPosition.columnPosition) {
                            if (stepSize == SelectionLayer.MOVE_ALL && !withShiftMask) {
                                this.selectionLayer.clear(false);
                            }
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

                if (this.newSelectedRowPosition < 0) {
                    if (traversalStrategy.getTraversalScope().equals(TraversalScope.AXIS)) {
                        if (!traversalStrategy.isCycle()) {
                            // on axis scope with no cycle, stop moving
                            this.newSelectedRowPosition = 0;
                        }
                        else {
                            // on axis scope with cycle, move to bottom
                            // TODO check if bottom is valid using step count
                            this.newSelectedRowPosition = this.selectionLayer.getRowCount() - 1;
                        }
                    }
                    else if (traversalStrategy.getTraversalScope().equals(TraversalScope.TABLE)) {
                        // on table scope, move to bottom
                        // TODO check if bottom is valid using step count
                        this.newSelectedColumnPosition = this.newSelectedColumnPosition - 1;
                        this.newSelectedRowPosition = this.selectionLayer.getRowCount() - 1;
                        if (this.newSelectedColumnPosition < 0) {
                            if (traversalStrategy.isCycle()) {
                                // at the beginning and cycle so go to end
                                this.newSelectedColumnPosition = this.selectionLayer.getColumnCount() - 1;
                            }
                            else {
                                // at the top and no cycle so stop moving
                                this.newSelectedColumnPosition = 0;
                                this.newSelectedRowPosition = 0;
                            }
                        }
                    }
                }

                if (this.newSelectedRowPosition != this.lastSelectedCellPosition.rowPosition) {
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

                if (this.newSelectedRowPosition >= this.selectionLayer.getRowCount()) {
                    if (traversalStrategy.getTraversalScope().equals(TraversalScope.AXIS)) {
                        if (!traversalStrategy.isCycle()) {
                            // on axis scope with no cycle, stop moving
                            this.newSelectedRowPosition = this.selectionLayer.getRowCount() - 1;
                        }
                        else {
                            // on axis scope with cycle, move to top
                            // TODO check if top is valid using step count
                            this.newSelectedRowPosition = 0;
                        }
                    }
                    else if (traversalStrategy.getTraversalScope().equals(TraversalScope.TABLE)) {
                        // on table scope, move to top
                        // TODO check if top is valid using step count
                        this.newSelectedColumnPosition = this.newSelectedColumnPosition + 1;
                        this.newSelectedRowPosition = 0;
                        if (this.newSelectedColumnPosition >= this.selectionLayer.getColumnCount()) {
                            if (traversalStrategy.isCycle()) {
                                // at the end and cycle so go to beginning
                                this.newSelectedColumnPosition = 0;
                            }
                            else {
                                // at the end and no cycle so stop moving
                                this.newSelectedColumnPosition = this.selectionLayer.getColumnCount() - 1;
                                this.newSelectedRowPosition = this.selectionLayer.getRowCount() - 1;
                            }
                        }
                    }
                }

                if (this.newSelectedRowPosition != this.lastSelectedCellPosition.rowPosition) {
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

    @Override
    public Class<MoveSelectionCommand> getCommandClass() {
        return MoveSelectionCommand.class;
    }

}
