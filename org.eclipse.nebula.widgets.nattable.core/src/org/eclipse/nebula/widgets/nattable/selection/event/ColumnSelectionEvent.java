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
package org.eclipse.nebula.widgets.nattable.selection.event;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;


public class ColumnSelectionEvent extends ColumnVisualChangeEvent implements ISelectionEvent {

	private final SelectionLayer selectionLayer;
	
	
	public ColumnSelectionEvent(SelectionLayer selectionLayer, Collection<Range> columnPositions) {
		super(selectionLayer, columnPositions);
		this.selectionLayer = selectionLayer;
	}
	
	protected ColumnSelectionEvent(ColumnSelectionEvent event) {
		super(event);
		this.selectionLayer = event.selectionLayer;
	}
	
	@Override
	public ColumnSelectionEvent cloneEvent() {
		return new ColumnSelectionEvent(this);
	}
	
	
	@Override
	public SelectionLayer getSelectionLayer() {
		return selectionLayer;
	}
	
}
