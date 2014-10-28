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
            super.moveLastSelectedUp(traversalStrategy, withShiftMask, withControlMask);

            if (this.lastSelectedCellPosition != null) {
                this.selectionLayer.selectRow(
                        this.lastSelectedCellPosition.columnPosition, this.lastSelectedCellPosition.rowPosition,
                        withShiftMask, withControlMask);
            }
        }
    }

    @Override
    protected void moveLastSelectedDown(ITraversalStrategy traversalStrategy, boolean withShiftMask, boolean withControlMask) {
        if (this.selectionLayer.hasRowSelection()) {
            super.moveLastSelectedDown(traversalStrategy, withShiftMask, withControlMask);

            if (this.lastSelectedCellPosition != null) {
                this.selectionLayer.selectRow(
                        this.lastSelectedCellPosition.columnPosition, this.lastSelectedCellPosition.rowPosition,
                        withShiftMask, withControlMask);
            }
        }
    }

}
