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
package org.eclipse.nebula.widgets.nattable.viewport.action;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseClickAction;
import org.eclipse.nebula.widgets.nattable.viewport.command.ViewportSelectRowCommand;
import org.eclipse.swt.events.MouseEvent;

/**
 * Event fired when the <i>ctrl</i> key is pressed and the row header is clicked.
 * Note: Fires command in NatTable coordinates.
 */
public class ViewportSelectRowAction implements IMouseClickAction {
	
	private final boolean withShiftMask;
	private final boolean withControlMask;

	public ViewportSelectRowAction(boolean withShiftMask, boolean withControlMask) {
		this.withShiftMask = withShiftMask;
		this.withControlMask = withControlMask;
	}
	
	@Override
	public void run(NatTable natTable, MouseEvent event) {
		natTable.doCommand(new ViewportSelectRowCommand(natTable, natTable.getRowPositionByY(event.y), withShiftMask, withControlMask));
	}

	@Override
	public boolean isExclusive() {
		return false;
	}
	
}
