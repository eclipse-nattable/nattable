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

import java.util.Collection;
import java.util.LinkedHashSet;


import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.events.MouseEvent;

public class AggregateDragMode implements IDragMode {
	
	private MouseEvent initialEvent;
	private MouseEvent currentEvent;
	
	private final Collection<IDragMode> dragModes = new LinkedHashSet<IDragMode>();
	
	public AggregateDragMode() {
	}
	
	public AggregateDragMode(IDragMode...dragModes) {
		for (IDragMode dragMode : dragModes) {
			addDragMode(dragMode);
		}
	}
	
	public void addDragMode(IDragMode dragMode) {
		dragModes.add(dragMode);
	}
	
	public void removeDragMode(IDragMode dragMode) {
		dragModes.remove(dragMode);
	}
	
	public void mouseDown(NatTable natTable, MouseEvent event) {
		initialEvent = event;
		currentEvent = initialEvent;
		
		for (IDragMode dragMode : dragModes) {
			dragMode.mouseDown(natTable, event);
		}
		
		natTable.forceFocus();
	}

	public void mouseMove(NatTable natTable, MouseEvent event) {
		currentEvent = event;
		
		for (IDragMode dragMode : dragModes) {
			dragMode.mouseMove(natTable, event);
		}
	}

	public void mouseUp(NatTable natTable, MouseEvent event) {
		for (IDragMode dragMode : dragModes) {
			dragMode.mouseUp(natTable, event);
		}
	}
	
	protected MouseEvent getInitialEvent() {
		return initialEvent;
	}
	
	protected MouseEvent getCurrentEvent() {
		return currentEvent;
	}
	
}
