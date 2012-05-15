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
package org.eclipse.nebula.widgets.nattable.selection.event;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionUtil;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;


public class RowSelectionEvent extends RowVisualChangeEvent implements ISelectionEvent {

	private final SelectionLayer selectionLayer;
	private int rowPositionToMoveIntoViewport;

	public RowSelectionEvent(SelectionLayer selectionLayer, Collection<Integer> rowPositions, int rowPositionToMoveIntoViewport) {
		super(selectionLayer, PositionUtil.getRanges(rowPositions));
		this.selectionLayer = selectionLayer;
		this.rowPositionToMoveIntoViewport = rowPositionToMoveIntoViewport;
	}
	
	// Copy constructor
	protected RowSelectionEvent(RowSelectionEvent event) {
		super(event);
		this.selectionLayer = event.selectionLayer;
		this.rowPositionToMoveIntoViewport = event.rowPositionToMoveIntoViewport;
	}
	
	public SelectionLayer getSelectionLayer() {
		return selectionLayer;
	}
	
	public int getRowPositionToMoveIntoViewport() {
		return rowPositionToMoveIntoViewport;
	}
	
	@Override
	public boolean convertToLocal(ILayer localLayer) {
		rowPositionToMoveIntoViewport = localLayer.underlyingToLocalRowPosition(getLayer(), rowPositionToMoveIntoViewport);
		
		return super.convertToLocal(localLayer);
	}
	
	public RowSelectionEvent cloneEvent() {
		return new RowSelectionEvent(this);
	}
	
}
