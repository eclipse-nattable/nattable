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
package org.eclipse.nebula.widgets.nattable.tickupdate.command;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.validate.IDataValidator;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.command.EditUtils;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.tickupdate.ITickUpdateHandler;
import org.eclipse.nebula.widgets.nattable.tickupdate.TickUpdateConfigAttributes;

/**
 * The command handler that will handle {@link TickUpdateCommand}s on selected cells.
 */
public class TickUpdateCommandHandler extends AbstractLayerCommandHandler<TickUpdateCommand> {

	private static final Log log = LogFactory.getLog(TickUpdateCommandHandler.class);
	
	/**
	 * The {@link SelectionLayer} needed to retrieve the selected cells on
	 * which the tick update should be processed.
	 */
	private SelectionLayer selectionLayer;

	/**
	 * @param selectionLayer The {@link SelectionLayer} needed to retrieve the selected cells on
	 * 			which the tick update should be processed.
	 */
	public TickUpdateCommandHandler(SelectionLayer selectionLayer) {
		this.selectionLayer = selectionLayer;
	}

	@Override
	public boolean doCommand(TickUpdateCommand command) {
		PositionCoordinate[] selectedPositions = selectionLayer.getSelectedCellPositions();
		IConfigRegistry configRegistry = command.getConfigRegistry();
		
		// Tick update for multiple cells in selection 
		if (selectedPositions.length > 1) {
			// Can all cells be updated ?
			if (EditUtils.allCellsEditable(selectionLayer, configRegistry)
					&& EditUtils.isEditorSame(selectionLayer, configRegistry) 
					&& EditUtils.isConverterSame(selectionLayer, configRegistry)) {
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

	/**
	 * Will calculate the new value after tick update processing for the cell at the given coordinates,
	 * trying to update the value represented by that cell. The update will only be processed if the
	 * new value is valid. 
	 * @param command The command to process
 	 * @param selectedPosition The coordinates of the cell on which the tick update
 	 * 			should be executed
	 */
	private void updateSingleCell(TickUpdateCommand command, PositionCoordinate selectedPosition) {
		ILayerCell cell = selectionLayer.getCellByPosition(
				selectedPosition.columnPosition, selectedPosition.rowPosition);
		
		IConfigRegistry configRegistry = command.getConfigRegistry();
		
		IEditableRule editableRule = configRegistry.getConfigAttribute(
				EditConfigAttributes.CELL_EDITABLE_RULE, 
				DisplayMode.EDIT,
				cell.getConfigLabels().getLabels());
		
		IDataValidator validator = configRegistry.getConfigAttribute(
				EditConfigAttributes.DATA_VALIDATOR, 
				DisplayMode.EDIT, 
				cell.getConfigLabels().getLabels());
		
		if (editableRule.isEditable(cell, configRegistry)) {
			//process the tick update
			Object newValue = getNewCellValue(command, cell);
			//validate the value
			try {
				if (validator == null || validator.validate(cell, configRegistry, newValue)) {
					selectionLayer.doCommand(new UpdateDataCommand(
							selectionLayer,
							selectedPosition.columnPosition, 
							selectedPosition.rowPosition,
							newValue));
				}
				else {
					log.warn("Tick update failed for cell at " + selectedPosition + " and value " + newValue //$NON-NLS-1$ //$NON-NLS-2$
							+ ". New value is not valid!"); //$NON-NLS-1$
				}
			}
			catch (Exception e) {
				log.warn("Tick update failed for cell at " + selectedPosition + " and value " + newValue //$NON-NLS-1$ //$NON-NLS-2$
						+ ". " + e.getLocalizedMessage()); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Will calculate the new value for the given cell after tick update is processed.
	 * @param command The command to process
	 * @param cell The cell on which the command should be processed
	 * @return The processed value after the command was executed on the current cell value
	 */
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

	@Override
	public Class<TickUpdateCommand> getCommandClass() {
		return TickUpdateCommand.class;
	}
}
