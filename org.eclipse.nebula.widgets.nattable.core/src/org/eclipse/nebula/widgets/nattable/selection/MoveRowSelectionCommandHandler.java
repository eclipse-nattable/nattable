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
package org.eclipse.nebula.widgets.nattable.selection;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;

/**
 * Preserves the basic semantics of the cell selection.
 * Additionally it selects the entire row when a cell in the row is selected.
 */
public class MoveRowSelectionCommandHandler extends MoveCellSelectionCommandHandler {

	public MoveRowSelectionCommandHandler(SelectionLayer selectionLayer) {
		super(selectionLayer);
	}

	@Override
	protected void moveLastSelectedLeft(int stepSize, boolean withShiftMask, boolean withControlMask) {
		super.moveLastSelectedLeft(stepSize, withShiftMask, withControlMask);

		if (lastSelectedCellPosition != null) {
			selectionLayer.selectRow(newSelectedColumnPosition, lastSelectedCellPosition.rowPosition, withShiftMask, withControlMask);
		}
	}

	@Override
	protected void moveLastSelectedRight(int stepSize, boolean withShiftMask, boolean withControlMask) {
		super.moveLastSelectedRight(stepSize, withShiftMask, withControlMask);

		if (lastSelectedCellPosition != null) {
			selectionLayer.selectRow(lastSelectedCellPosition.columnPosition, lastSelectedCellPosition.rowPosition, withShiftMask, withControlMask);
		}
	}

	@Override
	protected void moveLastSelectedUp(int stepSize, boolean withShiftMask, boolean withControlMask) {
		if (selectionLayer.hasRowSelection()) {
			PositionCoordinate lastSelectedCell = selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
			int newSelectedRowPosition = stepSize >= 0 ? lastSelectedCell.rowPosition - stepSize : 0;
			if (newSelectedRowPosition < 0) {
				newSelectedRowPosition = 0;
			}
			selectionLayer.selectRow(lastSelectedCell.columnPosition, newSelectedRowPosition, withShiftMask, withControlMask);
		}
	}

	@Override
	protected void moveLastSelectedDown(int stepSize, boolean withShiftMask, boolean withControlMask) {
		if (selectionLayer.hasRowSelection()) {
			PositionCoordinate lastSelectedCell = selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
			int newSelectedRowPosition = stepSize >= 0 ? lastSelectedCell.rowPosition + stepSize : selectionLayer.getRowCount() - 1;
			if (newSelectedRowPosition >= selectionLayer.getRowCount()) {
				newSelectedRowPosition = selectionLayer.getRowCount() - 1;
			}
			selectionLayer.selectRow(lastSelectedCell.columnPosition, newSelectedRowPosition, withShiftMask, withControlMask);
		}
	}

}
