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
package org.eclipse.nebula.widgets.nattable.edit.command;

import java.util.HashSet;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;

public class EditUtils {

	public static ILayerCell getLastSelectedCell(SelectionLayer selectionLayer) {
		PositionCoordinate selectionAnchor = selectionLayer.getSelectionAnchor();
		return selectionLayer.getCellByPosition(selectionAnchor.columnPosition, selectionAnchor.rowPosition);
	}

	public static ICellEditor lastSelectedCellEditor(SelectionLayer selectionLayer, IConfigRegistry configRegistry) {
		final List<String> lastSelectedCellLabelsArray = EditUtils.getLastSelectedCell(selectionLayer).getConfigLabels().getLabels();
		return configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITOR, DisplayMode.EDIT, lastSelectedCellLabelsArray);
	}

	public static boolean allCellsEditable(SelectionLayer selectionLayer, IConfigRegistry configRegistry) {
		PositionCoordinate[] selectedCells = selectionLayer.getSelectedCellPositions();
		ILayerCell layerCell = null;
		for (PositionCoordinate cell : selectedCells) {
			layerCell = selectionLayer.getCellByPosition(cell.columnPosition, cell.rowPosition);
			LabelStack labelStack = layerCell.getConfigLabels();
			IEditableRule editableRule = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, DisplayMode.EDIT, labelStack.getLabels());
			
			if (!editableRule.isEditable(layerCell, configRegistry)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean isCellEditable(SelectionLayer selectionLayer, IConfigRegistry configRegistry, PositionCoordinate cell){
		ILayerCell layerCell = selectionLayer.getCellByPosition(cell.columnPosition, cell.rowPosition);
		LabelStack labelStack = layerCell.getConfigLabels();
//		LabelStack labelStack = selectionLayer.getConfigLabelsByPosition(cell.columnPosition, cell.rowPosition);
//		int columnIndex = selectionLayer.getColumnIndexByPosition(cell.columnPosition);
//		int rowIndex = selectionLayer.getRowIndexByPosition(cell.rowPosition);
		
		IEditableRule editableRule = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, DisplayMode.EDIT, labelStack.getLabels());
		if (editableRule == null)
			return false;
		
		if (!editableRule.isEditable(layerCell, configRegistry)) {
			return false;
		}
		return true;
	}

	public static boolean isEditorSame(SelectionLayer selectionLayer, IConfigRegistry configRegistry, ICellEditor lastSelectedCellEditor) {
		PositionCoordinate[] selectedCells = selectionLayer.getSelectedCellPositions();

		boolean isAllSelectedCellsHaveSameEditor = true;
		for (PositionCoordinate selectedCell : selectedCells) {
			LabelStack labelStack = selectionLayer.getConfigLabelsByPosition(selectedCell.columnPosition, selectedCell.rowPosition);
			ICellEditor cellEditor = configRegistry.getConfigAttribute(EditConfigAttributes.CELL_EDITOR, DisplayMode.EDIT, labelStack.getLabels());
			if (cellEditor != lastSelectedCellEditor) {
				isAllSelectedCellsHaveSameEditor = false;
			}
		}
		return isAllSelectedCellsHaveSameEditor;
	}
	
	public static boolean isConverterSame(SelectionLayer selectionLayer, IConfigRegistry configRegistry, ICellEditor lastSelectedCellEditor){
		PositionCoordinate[] selectedCells = selectionLayer.getSelectedCellPositions();
		HashSet<Class> converterSet = new HashSet<Class>();
		
		for (PositionCoordinate selectedCell : selectedCells) {
			LabelStack labelStack = selectionLayer.getConfigLabelsByPosition(selectedCell.columnPosition, selectedCell.rowPosition);
			IDisplayConverter dataTypeConverter = configRegistry.getConfigAttribute(CellConfigAttributes.DISPLAY_CONVERTER, DisplayMode.EDIT, labelStack.getLabels());
			converterSet.add(dataTypeConverter.getClass());
			if (converterSet.size() > 1)
				return false;	
		}
		return true;
	}
	
}
