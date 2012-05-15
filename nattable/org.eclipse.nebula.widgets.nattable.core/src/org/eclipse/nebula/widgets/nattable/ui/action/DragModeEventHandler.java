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
import org.eclipse.swt.events.MouseEvent;

public class DragModeEventHandler extends AbstractModeEventHandler {

	private final NatTable natTable;
	
	private final IDragMode dragMode;
	
	public DragModeEventHandler(ModeSupport modeSupport, NatTable natTable, IDragMode dragMode) {
		super(modeSupport);
		
		this.natTable = natTable;
		this.dragMode = dragMode;
	}
	
	@Override
	public void mouseMove(MouseEvent event) {
		dragMode.mouseMove(natTable, event);
	}
	
	@Override
	public void mouseUp(MouseEvent event) {
		dragMode.mouseUp(natTable, event);
		switchMode(Mode.NORMAL_MODE);
	}
	
}
