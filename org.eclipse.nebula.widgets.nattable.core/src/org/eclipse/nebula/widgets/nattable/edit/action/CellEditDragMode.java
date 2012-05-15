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
package org.eclipse.nebula.widgets.nattable.edit.action;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.edit.command.EditCellCommand;
import org.eclipse.nebula.widgets.nattable.selection.action.CellSelectionDragMode;
import org.eclipse.swt.events.MouseEvent;

public class CellEditDragMode extends CellSelectionDragMode {

	private int originalColumnPosition;
	
	private int originalRowPosition;

	public void mouseDown(NatTable natTable, MouseEvent event) {
		super.mouseDown(natTable, event);
		
		originalColumnPosition = natTable.getColumnPositionByX(event.x);
		originalRowPosition = natTable.getRowPositionByY(event.y);
	}

	@Override
	public void mouseMove(NatTable natTable, MouseEvent event) {
		super.mouseMove(natTable, event);
		
		int columnPosition = natTable.getColumnPositionByX(event.x);
		int rowPosition = natTable.getRowPositionByY(event.y);
		
		if (columnPosition != originalColumnPosition || rowPosition != originalRowPosition) {
			// Left original cell, cancel edit
			originalColumnPosition = -1;
			originalRowPosition = -1;
		}
	}
	
	public void mouseUp(NatTable natTable, MouseEvent event) {
		super.mouseUp(natTable, event);
		
		int columnPosition = natTable.getColumnPositionByX(event.x);
		int rowPosition = natTable.getRowPositionByY(event.y);
		
		if (columnPosition == originalColumnPosition && rowPosition == originalRowPosition) {
			natTable.doCommand(
					new EditCellCommand(
							natTable,
							natTable.getConfigRegistry(),
							natTable.getCellByPosition(columnPosition, rowPosition)));
		}
	}

}
