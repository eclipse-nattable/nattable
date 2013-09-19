/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.selection;

import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.bothShiftAndControl;
import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.isControlOnly;
import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.isShiftOnly;
import static org.eclipse.nebula.widgets.nattable.selection.SelectionUtils.noShiftOrControl;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectColumnCommand;
import org.eclipse.nebula.widgets.nattable.selection.event.ColumnSelectionEvent;
import org.eclipse.swt.graphics.Rectangle;


public class SelectColumnCommandHandler implements ILayerCommandHandler<SelectColumnCommand> {

	private final SelectionLayer selectionLayer;

	public SelectColumnCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}

	@Override
	public boolean doCommand(ILayer targetLayer, SelectColumnCommand command) {
		if (command.convertToTargetLayer(selectionLayer)) {
			selectColumn(command.getColumnPosition(), command.getRowPosition(), command.isWithShiftMask(), command.isWithControlMask());
			return true;
		}
		return false;
	}

	protected void selectColumn(int columnPosition, int rowPosition, boolean withShiftMask, boolean withControlMask) {
		if (noShiftOrControl(withShiftMask, withControlMask)) {
			selectionLayer.clear(false);
			selectionLayer.selectCell(columnPosition, 0, false, false);
			selectionLayer.selectRegion(columnPosition, 0, 1, Integer.MAX_VALUE);
			selectionLayer.moveSelectionAnchor(columnPosition, rowPosition);
		} else if (bothShiftAndControl(withShiftMask, withControlMask)) {
			selectColumnWithShiftKey(columnPosition);
		} else if (isShiftOnly(withShiftMask, withControlMask)) {
			selectColumnWithShiftKey(columnPosition);
		} else if (isControlOnly(withShiftMask, withControlMask)) {
			selectColumnWithCtrlKey(columnPosition, rowPosition);
		}

		// Set last selected column position to the recently clicked column
		selectionLayer.lastSelectedCell.columnPosition = columnPosition;
		selectionLayer.lastSelectedCell.rowPosition = rowPosition;

		selectionLayer.fireLayerEvent(new ColumnSelectionEvent(selectionLayer, columnPosition));
	}

	private void selectColumnWithCtrlKey(int columnPosition, int rowPosition) {
		Rectangle selectedColumnRectangle = new Rectangle(columnPosition, 0, 1, Integer.MAX_VALUE);

		if (selectionLayer.isColumnPositionFullySelected(columnPosition)) {
			selectionLayer.clearSelection(selectedColumnRectangle);
			if(selectionLayer.lastSelectedRegion != null && selectionLayer.lastSelectedRegion.equals(selectedColumnRectangle)){
				selectionLayer.lastSelectedRegion = null;
			}
		}else{
			if(selectionLayer.lastSelectedRegion != null){
				selectionLayer.selectionModel.addSelection(
						new Rectangle(selectionLayer.lastSelectedRegion.x,
								selectionLayer.lastSelectedRegion.y,
								selectionLayer.lastSelectedRegion.width,
								selectionLayer.lastSelectedRegion.height));
			}
			selectionLayer.selectRegion(columnPosition, 0, 1, Integer.MAX_VALUE);
			selectionLayer.moveSelectionAnchor(columnPosition, rowPosition);
		}
	}

	private void selectColumnWithShiftKey(int columnPosition) {
		int numOfColumnsToIncludeInRegion = 1;
		int startColumnPosition = columnPosition;

		//if multiple selection is disabled, we need to ensure to only select the current columnPosition
		//modifying the selection anchor here ensures that the anchor also moves
		if (!selectionLayer.getSelectionModel().isMultipleSelectionAllowed()) {
			selectionLayer.selectionAnchor.columnPosition = columnPosition;
		}
		 
		if (selectionLayer.lastSelectedRegion != null) {
			
			// Negative when we move left, but we are only concerned with the num. of columns  
			numOfColumnsToIncludeInRegion = Math.abs(selectionLayer.selectionAnchor.columnPosition - columnPosition) + 1;
			
			// Select to the Left
			if (columnPosition < selectionLayer.selectionAnchor.columnPosition) {
				startColumnPosition = columnPosition;
			} else {
				startColumnPosition = selectionLayer.selectionAnchor.columnPosition;
			}
		}
		selectionLayer.selectRegion(startColumnPosition, 0, numOfColumnsToIncludeInRegion, Integer.MAX_VALUE);
	}
	
	@Override
	public Class<SelectColumnCommand> getCommandClass() {
		return SelectColumnCommand.class;
	}

}
