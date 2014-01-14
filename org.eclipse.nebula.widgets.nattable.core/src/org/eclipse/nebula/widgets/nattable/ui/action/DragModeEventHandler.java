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
package org.eclipse.nebula.widgets.nattable.ui.action;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.ui.mode.AbstractModeEventHandler;
import org.eclipse.nebula.widgets.nattable.ui.mode.Mode;
import org.eclipse.nebula.widgets.nattable.ui.mode.ModeSupport;
import org.eclipse.nebula.widgets.nattable.ui.mode.MouseModeEventHandler;
import org.eclipse.swt.events.MouseEvent;

public class DragModeEventHandler extends AbstractModeEventHandler {

	private final NatTable natTable;
	
	private final IDragMode dragMode;
	
	private final MouseModeEventHandler parentModeEventHandler;
	private final MouseEvent mouseDownEvent;
	
	public DragModeEventHandler(ModeSupport modeSupport, NatTable natTable, IDragMode dragMode, 
			MouseModeEventHandler parentModeEventHandler, MouseEvent mouseDownEvent) {
		super(modeSupport);
		
		this.natTable = natTable;
		this.dragMode = dragMode;
		this.parentModeEventHandler = parentModeEventHandler;
		this.mouseDownEvent = mouseDownEvent;
	}
	
	@Override
	public void mouseMove(MouseEvent event) {
		dragMode.mouseMove(natTable, event);
	}
	
	@Override
	public void mouseUp(MouseEvent event) {
		dragMode.mouseUp(natTable, event);
		switchMode(Mode.NORMAL_MODE);
		
		//Bug 379884
		//check if the drag operation started and ended within the same cell
		//in that case the registered click operation is executed also
		int startCol = natTable.getColumnPositionByX(mouseDownEvent.x);
		int startRow = natTable.getRowPositionByY(mouseDownEvent.y);
		
		int col = natTable.getColumnPositionByX(event.x);
		int row = natTable.getRowPositionByY(event.y);
		
		if (startCol == col && startRow == row) {
			parentModeEventHandler.mouseUp(event);
		}
	}
	
}
