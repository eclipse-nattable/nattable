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
package org.eclipse.nebula.widgets.nattable.tickupdate.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.command.EditUtils;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.tickupdate.ITickUpdateHandler;
import org.eclipse.nebula.widgets.nattable.tickupdate.TickUpdateConfigAttributes;

public class TickUpdateCommandHandler extends AbstractLayerCommandHandler<TickUpdateCommand> {

	private SelectionLayer selectionLayer;

	public TickUpdateCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}

	public boolean doCommand(TickUpdateCommand command) {
		PositionCoordinate[] selectedPositions = selectionLayer.getSelectedCellPositions();
		IConfigRegistry configRegistry = command.getConfigRegistry();
		
		// Tick update for multiple cells in selection 
		if (selectedPositions.length > 1) {
			
			ICellEditor lastSelectedCellEditor = EditUtils.lastSelectedCellEditor(selectionLayer, configRegistry);
			// Can all cells be updated ?
			if (EditUtils.isEditorSame(selectionLayer, configRegistry, lastSelectedCellEditor) 
					&& EditUtils.allCellsEditable(selectionLayer, configRegistry)){
				
				for (PositionCoordinate position : selectedPositions) {
					updateSingleCell(command, position);
				}
			}
		} else {
			// Tick update for single selected cell
			updateSingleCell(command, selectionLayer.getLastSelectedCellPosition());
		}

		return true;
	}


	private void updateSingleCell(TickUpdateCommand command, PositionCoordinate selectedPosition) {
		ILayerCell cell = selectionLayer.getCellByPosition(selectedPosition.columnPosition, selectedPosition.rowPosition);
		
		IEditableRule editableRule = command.getConfigRegistry().getConfigAttribute(
				EditConfigAttributes.CELL_EDITABLE_RULE, 
				DisplayMode.EDIT,
				cell.getConfigLabels().getLabels());
		
		if(editableRule.isEditable(cell, command.getConfigRegistry())){
			selectionLayer.doCommand(new UpdateDataCommand(
					selectionLayer,
					selectedPosition.columnPosition, 
					selectedPosition.rowPosition,
					getNewCellValue(command, cell)));
		}
	}

	private Object getNewCellValue(TickUpdateCommand command, ILayerCell cell) {
		ITickUpdateHandler tickUpdateHandler = command.getConfigRegistry().getConfigAttribute(
				TickUpdateConfigAttributes.UPDATE_HANDLER,
				DisplayMode.EDIT, 
				cell.getConfigLabels().getLabels());

		Object dataValue = cell.getDataValue();

		if (tickUpdateHandler != null && tickUpdateHandler.isApplicableFor(dataValue)) {
			if (command.isIncrement()) {
				return tickUpdateHandler.getIncrementedValue(dataValue);
			} else {
				return tickUpdateHandler.getDecrementedValue(dataValue);
			}
		} else {
			return dataValue;
		}
	}

	public Class<TickUpdateCommand> getCommandClass() {
		return TickUpdateCommand.class;
	}
}
