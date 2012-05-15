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
package org.eclipse.nebula.widgets.nattable.resize.action;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;

public class RowResizeCursorAction implements IMouseAction {
	
	private Cursor rowResizeCursor;

	public void run(NatTable natTable, MouseEvent event) {
		if (rowResizeCursor == null) {
			 rowResizeCursor = new Cursor(Display.getDefault(), SWT.CURSOR_SIZENS);
			 
			 natTable.addDisposeListener(new DisposeListener() {
				
				public void widgetDisposed(DisposeEvent e) {
					rowResizeCursor.dispose();
				}
				
			});
		}
		
		natTable.setCursor(rowResizeCursor);
	}
	
}
