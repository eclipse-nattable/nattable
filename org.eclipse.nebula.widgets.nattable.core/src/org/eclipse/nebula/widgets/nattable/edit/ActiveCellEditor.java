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


import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.editor.ICellEditor;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.widget.EditModeEnum;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class ActiveCellEditor {

	private static ICellEditor cellEditor;
	private static Control activeCellEditorControl;
	private static int columnPosition = -1;
	private static int rowPosition = -1;
	private static int columnIndex = -1;
	private static int rowIndex = -1;
	
	public static void activate(ICellEditor cellEditor, Composite parent, Object originalCanonicalValue, Character initialEditValue, EditModeEnum editMode, ICellEditHandler editHandler, ILayerCell cell, IConfigRegistry configRegistry) {
		close();
		
		ActiveCellEditor.cellEditor = cellEditor;
		ActiveCellEditor.columnPosition = cell.getColumnPosition();
		ActiveCellEditor.rowPosition = cell.getRowPosition();
		ActiveCellEditor.columnIndex = cell.getColumnIndex();
		ActiveCellEditor.rowIndex = cell.getRowIndex();

		activeCellEditorControl = cellEditor.activateCell(parent, originalCanonicalValue, initialEditValue, editMode, editHandler, cell, configRegistry);
	}
	
	public static boolean commit() {
		if (isValid()) {
			return cellEditor.commit(MoveDirectionEnum.NONE, true);
		}
		return true;
	}
	
	public static void close() {
		if (isValid()) {
			cellEditor.close();
		}
		cellEditor = null;

		if (activeCellEditorControl != null && !activeCellEditorControl.isDisposed()) {
			activeCellEditorControl.dispose();
		}
		activeCellEditorControl = null;
		
		columnPosition = -1;
		rowPosition = -1;
		columnIndex = -1;
		rowIndex = -1;
	}

	public static ICellEditor getCellEditor() {
		return cellEditor;
	}
	
	public static Control getControl() {
		if (isValid()) {
			return activeCellEditorControl;
		} else {
			return null;
		}
	}
	
	public static int getColumnPosition() {
		return columnPosition;
	}
	
	public static int getRowPosition() {
		return rowPosition;
	}
	
	public static int getColumnIndex() {
		return columnIndex;
	}
	
	public static int getRowIndex() {
		return rowIndex;
	}

	public static Object getCanonicalValue() {
		if (isValid()) {
			return cellEditor.getCanonicalValue();
		} else {
			return null;
		}
	}

	public static boolean validateCanonicalValue() {
		if (isValid()) {
			return cellEditor.validateCanonicalValue();
		} else {
			return true;
		}
	}

	public static boolean isValid() {
		return cellEditor != null && !cellEditor.isClosed();
	}

}
