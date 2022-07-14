/*******************************************************************************
 * Copyright (c) 2012, 2022 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 *     Dirk Fauth <dirk.fauth@googlemail.com> - added ITraversalStrategy handling
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;

/**
 * Preserves the basic semantics of the cell selection. Additionally it selects
 * the entire row when a cell in the row is selected.
 */
public class MoveRowSelectionCommandHandler extends MoveCellSelectionCommandHandler {

    /**
     * Create a MoveRowSelectionCommandHandler for the given
     * {@link SelectionLayer}. Uses the
     * {@link ITraversalStrategy#AXIS_TRAVERSAL_STRATEGY} as default strategy
     * for selection movement.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} on which the selection should be
     *            performed.
     */
    public MoveRowSelectionCommandHandler(SelectionLayer selectionLayer) {
        super(selectionLayer);
    }

    /**
     * Create a MoveRowSelectionCommandHandler for the given
     * {@link SelectionLayer}.
     *
     * @param selectionLayer
     *            The {@link SelectionLayer} on which the selection should be
     *            performed.
     * @param traversalStrategy
     *            The strategy that should be used for selection movements. Can
     *            not be <code>null</code>.
     */
    public MoveRowSelectionCommandHandler(SelectionLayer selectionLayer, ITraversalStrategy traversalStrategy) {
        super(selectionLayer, traversalStrategy);
    }

    /**
     * Create a MoveRowSelectionCommandHandler for the given
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
    public MoveRowSelectionCommandHandler(SelectionLayer selectionLayer,
            ITraversalStrategy horizontalTraversalStrategy, ITraversalStrategy verticalTraversalStrategy) {
        super(selectionLayer, horizontalTraversalStrategy, verticalTraversalStrategy);
    }

    @Override
    protected void moveLastSelectedLeft(ITraversalStrategy traversalStrategy, boolean withShiftMask, boolean withControlMask) {
        super.moveLastSelectedLeft(traversalStrategy, withShiftMask, withControlMask);

        if (this.lastSelectedCellPosition != null) {
            this.selectionLayer.selectRow(
                    this.lastSelectedCellPosition.columnPosition, this.lastSelectedCellPosition.rowPosition,
                    withShiftMask, withControlMask);
        }
    }

    @Override
    protected void moveLastSelectedRight(ITraversalStrategy traversalStrategy, boolean withShiftMask, boolean withControlMask) {
        super.moveLastSelectedRight(traversalStrategy, withShiftMask, withControlMask);

        if (this.lastSelectedCellPosition != null) {
            this.selectionLayer.selectRow(
                    this.lastSelectedCellPosition.columnPosition, this.lastSelectedCellPosition.rowPosition,
                    withShiftMask, withControlMask);
        }
    }

    @Override
    protected void moveLastSelectedUp(ITraversalStrategy traversalStrategy, boolean withShiftMask, boolean withControlMask) {
        if (this.selectionLayer.hasRowSelection()) {

            PositionCoordinate anchor = new PositionCoordinate(this.selectionLayer.getSelectionAnchor());
            PositionCoordinate from = this.selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
            boolean deselect = this.selectionLayer.isRowPositionSelected(from.rowPosition) && this.selectionLayer.isRowPositionSelected(from.rowPosition - 1) && withShiftMask;
            if (deselect) {
                // deselect
                this.selectionLayer.selectRow(this.lastSelectedCellPosition.columnPosition, from.rowPosition, false, true);
            }

            super.moveLastSelectedUp(traversalStrategy, withShiftMask, withControlMask);

            if (deselect) {
                // keep the selection anchor at its original position
                this.selectionLayer.setSelectionAnchor(anchor.columnPosition, anchor.rowPosition);
            }

            if (!deselect && this.lastSelectedCellPosition != null) {
                this.selectionLayer.selectRow(
                        this.lastSelectedCellPosition.columnPosition, this.lastSelectedCellPosition.rowPosition,
                        withShiftMask, withControlMask);
            }
        }
    }

    @Override
    protected void moveLastSelectedDown(ITraversalStrategy traversalStrategy, boolean withShiftMask, boolean withControlMask) {
        if (this.selectionLayer.hasRowSelection()) {

            PositionCoordinate anchor = new PositionCoordinate(this.selectionLayer.getSelectionAnchor());
            PositionCoordinate from = this.selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
            boolean deselect = this.selectionLayer.isRowPositionSelected(from.rowPosition) && this.selectionLayer.isRowPositionSelected(from.rowPosition + 1) && withShiftMask;
            if (deselect) {
                // deselect
                this.selectionLayer.selectRow(this.lastSelectedCellPosition.columnPosition, from.rowPosition, false, true);
            }

            super.moveLastSelectedDown(traversalStrategy, withShiftMask, withControlMask);

            if (deselect) {
                // keep the selection anchor at its original position
                this.selectionLayer.setSelectionAnchor(anchor.columnPosition, anchor.rowPosition);
            }

            if (!deselect && this.lastSelectedCellPosition != null) {
                this.selectionLayer.selectRow(
                        this.lastSelectedCellPosition.columnPosition, this.lastSelectedCellPosition.rowPosition,
                        withShiftMask, withControlMask);
            }
        }
    }

    @Override
    void selectCell(int columnPosition, int rowPosition, boolean withShiftMask, boolean withControlMask, boolean fireEvent) {
        PositionCoordinate selectionAnchor = this.selectionLayer.getSelectionAnchor();
        int col = columnPosition;
        boolean fire = fireEvent;
        if (selectionAnchor.columnPosition == columnPosition || withShiftMask) {
            // ignore the given column position and stick with the selection
            // anchor
            // also don't fire a CellSelectionEvent to avoid column scrolling on
            // row selection as the required selection event is based on the row
            // selection afterwards
            col = selectionAnchor.columnPosition;
            fire = false;
        }

        super.selectCell(
                col,
                rowPosition,
                withShiftMask,
                withControlMask,
                fire);
    }
}
