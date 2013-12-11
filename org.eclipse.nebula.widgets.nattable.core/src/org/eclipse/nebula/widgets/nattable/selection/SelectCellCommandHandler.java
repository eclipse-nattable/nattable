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

import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.isControlOnly;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.swt.graphics.Rectangle;

public class SelectCellCommandHandler implements ILayerCommandHandler<SelectCellCommand> {

	private final SelectionLayer selectionLayer;

	public SelectCellCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}

	@Override
	public boolean doCommand(ILayer targetLayer, SelectCellCommand command) {
		if (command.convertToTargetLayer(selectionLayer)) {
			toggleCell(command.getColumnPosition(), command.getRowPosition(), command.isShiftMask(), command.isControlMask(), command.isForcingEntireCellIntoViewport());
			selectionLayer.fireCellSelectionEvent(command.getColumnPosition(), command.getRowPosition(), command.isForcingEntireCellIntoViewport(), command.isShiftMask(), command.isControlMask());
			return true;
		}
		return false;
	}

	/**
	 * Toggles the selection state of the given row and column.
	 */
	protected void toggleCell(int columnPosition, int rowPosition, boolean withShiftMask, boolean withControlMask, boolean forcingEntireCellIntoViewport) {
		boolean selectCell = true;
		if (isControlOnly(withShiftMask, withControlMask)) {
			if (selectionLayer.isCellPositionSelected(columnPosition, rowPosition)) {
				ILayerCell cell = selectionLayer.getCellByPosition(columnPosition, rowPosition);
				Rectangle cellRect = new Rectangle(cell.getOriginColumnPosition(), cell.getOriginRowPosition(), cell.getColumnSpan(), cell.getRowSpan());
				selectionLayer.clearSelection(cellRect);
				selectCell = false;
			}
		}
		if (selectCell) {
			selectCell(columnPosition, rowPosition, withShiftMask, withControlMask);
		}
	}

	/**
	 * Selects a cell, optionally clearing current selection
	 */
	public void selectCell(int columnPosition, int rowPosition, boolean withShiftMask, boolean withControlMask) {
		if (!withShiftMask && !withControlMask) {
			selectionLayer.clear(false);
		}
		
		ILayerCell cell = selectionLayer.getCellByPosition(columnPosition, rowPosition);
		
		if (cell != null) {
			selectionLayer.setLastSelectedCell(cell.getOriginColumnPosition(), cell.getOriginRowPosition());
			
			// Shift pressed + row selected
			if (selectionLayer.getSelectionModel().isMultipleSelectionAllowed() 
					&& withShiftMask 
					&& selectionLayer.lastSelectedRegion != null 
					&& selectionLayer.hasRowSelection()
					&& (selectionLayer.selectionAnchor.rowPosition != SelectionLayer.NO_SELECTION)
					&& (selectionLayer.selectionAnchor.columnPosition != SelectionLayer.NO_SELECTION)) {
				// if cell.rowPosition > selectionAnchor.rowPositon, then use cell.rowPosition + span - 1 (maxRowPosition)
				// else use cell.originRowPosition (minRowPosition)
				// and compare with selectionAnchor.rowPosition
				if (cell.getRowPosition() > selectionLayer.selectionAnchor.rowPosition) {
					int maxRowPosition = cell.getOriginRowPosition() + cell.getRowSpan() - 1;
					selectionLayer.lastSelectedRegion.height = Math.abs(selectionLayer.selectionAnchor.rowPosition - maxRowPosition) + 1;
				} else {
					int minRowPosition = cell.getOriginRowPosition();
					selectionLayer.lastSelectedRegion.height = Math.abs(selectionLayer.selectionAnchor.rowPosition - minRowPosition) + 1;
				}
				selectionLayer.lastSelectedRegion.y = Math.min(selectionLayer.selectionAnchor.rowPosition, cell.getOriginRowPosition());
				
				if (cell.getColumnPosition() > selectionLayer.selectionAnchor.columnPosition) {
					int maxColumnPosition = cell.getOriginColumnPosition() + cell.getColumnSpan() - 1;
					selectionLayer.lastSelectedRegion.width = Math.abs(selectionLayer.selectionAnchor.columnPosition - maxColumnPosition) + 1;
				} else {
					int minColumnPosition = cell.getOriginColumnPosition();
					selectionLayer.lastSelectedRegion.width = Math.abs(selectionLayer.selectionAnchor.columnPosition - minColumnPosition) + 1;
				}
				selectionLayer.lastSelectedRegion.x = Math.min(selectionLayer.selectionAnchor.columnPosition, cell.getOriginColumnPosition());
				
				selectionLayer.addSelection(selectionLayer.lastSelectedRegion);
			} else {
				selectionLayer.lastSelectedRegion = null;
				Rectangle selection = new Rectangle(cell.getOriginColumnPosition(), cell.getOriginRowPosition(), cell.getColumnSpan(), cell.getRowSpan());
				
				selectionLayer.addSelection(selection);
			}
		}
	}

	@Override
	public Class<SelectCellCommand> getCommandClass() {
		return SelectCellCommand.class;
	}

}
