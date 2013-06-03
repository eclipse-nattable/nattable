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
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.command.MoveSelectionCommand;

/**
 * Specifies the semantics of moving the selection in the table, based on selecting the adjoining cell(s).
 */
public class MoveCellSelectionCommandHandler extends MoveSelectionCommandHandler<MoveSelectionCommand> {

	protected PositionCoordinate lastSelectedCellPosition;
	protected int newSelectedColumnPosition;
	protected int newSelectedRowPosition;

	public MoveCellSelectionCommandHandler(SelectionLayer selectionLayer) {
		super(selectionLayer);
	}

	@Override
	protected void moveLastSelectedLeft(int stepSize, boolean withShiftMask, boolean withControlMask) {
		if (selectionLayer.hasColumnSelection()) {
			lastSelectedCellPosition = selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
			ILayerCell lastSelectedCell = selectionLayer.getCellByPosition(lastSelectedCellPosition.columnPosition, lastSelectedCellPosition.rowPosition);
			if (lastSelectedCell != null) {
				newSelectedColumnPosition =
						stepSize >= 0
						? lastSelectedCell.getOriginColumnPosition() - stepSize
						: 0;
				if (newSelectedColumnPosition < 0) {
					newSelectedColumnPosition = 0;
				}
	
				newSelectedRowPosition = lastSelectedCellPosition.rowPosition;
				
				if (newSelectedColumnPosition != lastSelectedCellPosition.columnPosition) {
					if (stepSize == SelectionLayer.MOVE_ALL && !withShiftMask) {
						selectionLayer.clear(false);
					}
					selectionLayer.selectCell(newSelectedColumnPosition, newSelectedRowPosition, withShiftMask, withControlMask);
					selectionLayer.fireCellSelectionEvent(lastSelectedCellPosition.columnPosition, lastSelectedCellPosition.rowPosition, true, withShiftMask, withControlMask);
				}
			}
		}
	}

	@Override
	protected void moveLastSelectedRight(int stepSize, boolean withShiftMask, boolean withControlMask) {
		if (selectionLayer.hasColumnSelection()) {
			lastSelectedCellPosition = selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
			ILayerCell lastSelectedCell = selectionLayer.getCellByPosition(lastSelectedCellPosition.columnPosition, lastSelectedCellPosition.rowPosition);
			if (lastSelectedCell != null) {
				newSelectedColumnPosition =
						stepSize >= 0 
						? lastSelectedCell.getOriginColumnPosition() + lastSelectedCell.getColumnSpan() - 1 + stepSize 
						: selectionLayer.getColumnCount() - 1;
				if (newSelectedColumnPosition >= selectionLayer.getColumnCount()) {
					newSelectedColumnPosition = selectionLayer.getColumnCount() - 1;
				}
				
				newSelectedRowPosition = lastSelectedCellPosition.rowPosition;
				
				if (newSelectedColumnPosition != lastSelectedCellPosition.columnPosition) {
					if (stepSize == SelectionLayer.MOVE_ALL && !withShiftMask) {
						selectionLayer.clear(false);
					}
					selectionLayer.selectCell(newSelectedColumnPosition, newSelectedRowPosition, withShiftMask, withControlMask);
					selectionLayer.fireCellSelectionEvent(lastSelectedCellPosition.columnPosition, lastSelectedCellPosition.rowPosition, true, withShiftMask, withControlMask);
				}
			}
		}
	}

	@Override
	protected void moveLastSelectedUp(int stepSize, boolean withShiftMask, boolean withControlMask) {
		if (selectionLayer.hasRowSelection()) {
			lastSelectedCellPosition = selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
			ILayerCell lastSelectedCell = selectionLayer.getCellByPosition(lastSelectedCellPosition.columnPosition, lastSelectedCellPosition.rowPosition);
			if (lastSelectedCell != null) {
				newSelectedColumnPosition = lastSelectedCellPosition.columnPosition;
				
				newSelectedRowPosition =
						stepSize >= 0
						? lastSelectedCell.getOriginRowPosition() - stepSize
						: 0;
				if (newSelectedRowPosition < 0) {
					newSelectedRowPosition = 0;
				}
				
				if (newSelectedRowPosition != lastSelectedCellPosition.rowPosition) {
					selectionLayer.selectCell(lastSelectedCellPosition.columnPosition, newSelectedRowPosition, withShiftMask, withControlMask);
					selectionLayer.fireCellSelectionEvent(lastSelectedCellPosition.columnPosition, lastSelectedCellPosition.rowPosition, true, withShiftMask, withControlMask);
				}
			}
		}
	}

	@Override
	protected void moveLastSelectedDown(int stepSize, boolean withShiftMask, boolean withControlMask) {
		if (selectionLayer.hasRowSelection()) {
			lastSelectedCellPosition = selectionLayer.getCellPositionToMoveFrom(withShiftMask, withControlMask);
			ILayerCell lastSelectedCell = selectionLayer.getCellByPosition(lastSelectedCellPosition.columnPosition, lastSelectedCellPosition.rowPosition);
			if (lastSelectedCell != null) {
				newSelectedColumnPosition = lastSelectedCellPosition.columnPosition;
				
				newSelectedRowPosition =
						stepSize >= 0
						? lastSelectedCell.getOriginRowPosition() + lastSelectedCell.getRowSpan() - 1 + stepSize
						: selectionLayer.getRowCount() - 1;
				if (newSelectedRowPosition >= selectionLayer.getRowCount()) {
					newSelectedRowPosition = selectionLayer.getRowCount() - 1;
				}
				
				if (newSelectedRowPosition != lastSelectedCellPosition.rowPosition) {
					selectionLayer.selectCell(lastSelectedCellPosition.columnPosition, newSelectedRowPosition, withShiftMask, withControlMask);
					selectionLayer.fireCellSelectionEvent(lastSelectedCellPosition.columnPosition, lastSelectedCellPosition.rowPosition, true, withShiftMask, withControlMask);
				}
			}
		}
	}

	public Class<MoveSelectionCommand> getCommandClass() {
		return MoveSelectionCommand.class;
	}

}
