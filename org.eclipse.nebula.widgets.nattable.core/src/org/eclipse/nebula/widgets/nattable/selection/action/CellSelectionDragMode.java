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
package org.eclipse.nebula.widgets.nattable.selection.action;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectCellCommand;
import org.eclipse.nebula.widgets.nattable.ui.action.IDragMode;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;

/**
 * Fires commands to select a range of cells when the mouse is dragged in the viewport.
 */
public class CellSelectionDragMode implements IDragMode {

	private Point lastDragInCellPosition = null;

	@Override
	public void mouseDown(NatTable natTable, MouseEvent event) {
		natTable.forceFocus();
		lastDragInCellPosition = new Point(natTable.getColumnPositionByX(event.x), natTable.getRowPositionByY(event.y));
	}

	@Override
	public void mouseMove(NatTable natTable, MouseEvent event) {
		if (event.x > natTable.getWidth()) {
			return;
		}
		int selectedColumnPosition = natTable.getColumnPositionByX(event.x);
		int selectedRowPosition = natTable.getRowPositionByY(event.y);

		if (selectedColumnPosition > -1 && selectedRowPosition > -1) {
			Point dragInCellPosition = new Point(selectedColumnPosition, selectedRowPosition);
			if (lastDragInCellPosition == null || !dragInCellPosition.equals(lastDragInCellPosition)){
				lastDragInCellPosition = dragInCellPosition;

				fireSelectionCommand(natTable, selectedColumnPosition, selectedRowPosition, true, false);
			}
		}
	}

	public void fireSelectionCommand(NatTable natTable, int columnPosition,	int rowPosition, boolean shiftMask, boolean controlMask) {
		natTable.doCommand(new SelectCellCommand(natTable, columnPosition, rowPosition, shiftMask, controlMask));
	}

	@Override
	public void mouseUp(NatTable natTable, MouseEvent event) {
		endDrag();
	}

	private void endDrag(){
		lastDragInCellPosition = null;
	}
}
