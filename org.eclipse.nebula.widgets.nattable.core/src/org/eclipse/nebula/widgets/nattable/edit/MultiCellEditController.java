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
package org.eclipse.nebula.widgets.nattable.edit;

import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.edit.command.EditUtils;
import org.eclipse.nebula.widgets.nattable.edit.command.UpdateDataCommand;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.edit.event.InlineCellEditEvent;
import org.eclipse.nebula.widgets.nattable.edit.gui.MultiCellEditDialog;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.tickupdate.ITickUpdateHandler;
import org.eclipse.nebula.widgets.nattable.tickupdate.TickUpdateConfigAttributes;
import org.eclipse.swt.widgets.Composite;

/**
 * Controls edit behaviour when multiple cells are being edited at once. Multi-
 * edit is only allowed if all of the selected cells share the same editor type.
 * Multi-edit behaviour involves popping up an edit dialog with an appropriate
 * edit control. When the value in the popup dialog is submitted, the values of
 * all of the selected cells are changed to this value.
 */
public class MultiCellEditController {

	public static boolean editSelectedCells(SelectionLayer selectionLayer, Character initialEditValue, Composite parent, IConfigRegistry configRegistry, boolean useAdjustOnMultiEdit) {

		ILayerCell lastSelectedCell = EditUtils.getLastSelectedCell(selectionLayer);
		
		// IF cell is selected
		if (lastSelectedCell != null) {
			final List<String> lastSelectedCellLabelsArray = lastSelectedCell.getConfigLabels().getLabels();
			
			PositionCoordinate[] selectedCells = selectionLayer.getSelectedCellPositions();
			// AND selected cell count > 1
			if (selectedCells.length > 1) {
				
				ICellEditor lastSelectedCellEditor = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITOR, DisplayMode.EDIT, lastSelectedCellLabelsArray);
				
				// AND all selected cells are of the same editor type
				// AND all selected cells are editable
				if (EditUtils.isEditorSame(selectionLayer, configRegistry, lastSelectedCellEditor) 
						&& EditUtils.allCellsEditable(selectionLayer, configRegistry)) {
					
					// THEN use multi commit handler and populate editor in popup
					ICellEditor cellEditor = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITOR, DisplayMode.EDIT, lastSelectedCellLabelsArray);

					ITickUpdateHandler tickUpdateHandler = configRegistry.getConfigAttribute(TickUpdateConfigAttributes.UPDATE_HANDLER, DisplayMode.EDIT, lastSelectedCellLabelsArray);
					boolean allowIncrementDecrement = tickUpdateHandler != null;

					Object originalCanonicalValue = lastSelectedCell.getDataValue();
					for (PositionCoordinate selectedCell : selectedCells) {
						Object cellValue = selectionLayer.getCellByPosition(selectedCell.columnPosition, selectedCell.rowPosition).getDataValue();
						allowIncrementDecrement = allowIncrementDecrement && tickUpdateHandler.isApplicableFor(cellValue);
						if (cellValue != null && !cellValue.equals(originalCanonicalValue)) {
							originalCanonicalValue = null;
							break;
						}
					}                        
					
					MultiCellEditDialog dialog = new MultiCellEditDialog(parent.getShell(), cellEditor, originalCanonicalValue, initialEditValue, allowIncrementDecrement, configRegistry, lastSelectedCell);

					int returnValue = dialog.open();
					
					ActiveCellEditor.close();
					
					if (returnValue == Window.OK) {
						Object editorValue = dialog.getEditorValue();
						Object newValue = editorValue;
						
						for (PositionCoordinate selectedCell : selectedCells) {
    						if (allowIncrementDecrement) {
    							double delta = editorValue instanceof Number ? ((Number)editorValue).doubleValue() : Double.valueOf((String)editorValue).doubleValue();
    						    originalCanonicalValue = selectionLayer.getCellByPosition(selectedCell.columnPosition, selectedCell.rowPosition).getDataValue();
                                EditTypeEnum editType = dialog.getEditType();
        						if (editType == EditTypeEnum.ADJUST) {
        						    if(delta >= 0) {
        						        editType = EditTypeEnum.INCREASE;
        						    } else {
        						        editType = EditTypeEnum.DECREASE;
        						    }
        						}
                                switch (editType) {
    							case INCREASE:
    								newValue = tickUpdateHandler.getIncrementedValue(originalCanonicalValue, delta);
    								break;
    							case DECREASE:
    								newValue = tickUpdateHandler.getDecrementedValue(originalCanonicalValue, delta);
    								break;
								default:
								    break;
    							}
    						}
						
							selectionLayer.doCommand(new UpdateDataCommand(selectionLayer, selectedCell.columnPosition, selectedCell.rowPosition, newValue));
						}
					}
				}
			} else {
				// ELSE use single commit handler and populate editor inline in cell rectangle
				selectionLayer.fireLayerEvent(new InlineCellEditEvent(selectionLayer, new PositionCoordinate(selectionLayer, lastSelectedCell.getColumnPosition(), lastSelectedCell.getRowPosition()), parent, configRegistry, initialEditValue));
			}
			return true;
		}
		return false;
	}
	
}
