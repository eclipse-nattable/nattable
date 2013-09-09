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
package org.eclipse.nebula.widgets.nattable.selection.action;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;

public class AbstractMouseSelectionAction implements IMouseAction {

    private boolean withShiftMask;
    private boolean withControlMask;
    private int gridColumnPosition;
    private int gridRowPosition;

	@Override
	public void run(NatTable natTable, MouseEvent event) {
    	withShiftMask = (event.stateMask & SWT.SHIFT) != 0;
    	withControlMask = (event.stateMask & SWT.CTRL) != 0;

    	gridColumnPosition = natTable.getColumnPositionByX(event.x);
    	gridRowPosition = natTable.getRowPositionByY(event.y);

    	natTable.forceFocus();
	}

	public boolean isWithShiftMask() {
		return withShiftMask;
	}

	public boolean isWithControlMask() {
		return withControlMask;
	}

	public int getGridColumnPosition() {
		return gridColumnPosition;
	}

	public int getGridRowPosition() {
		return gridRowPosition;
	}
}
