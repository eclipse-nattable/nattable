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
package org.eclipse.nebula.widgets.nattable.layer.event;

import java.util.Arrays;
import java.util.Collection;


import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.graphics.Rectangle;

public class CellVisualChangeEvent implements IVisualChangeEvent {

	protected ILayer layer;
	
	protected int columnPosition;
	
	protected int rowPosition;
	
	public CellVisualChangeEvent(ILayer layer, int columnPosition, int rowPosition) {
		this.layer = layer;
		this.columnPosition = columnPosition;
		this.rowPosition = rowPosition;
	}
	
	protected CellVisualChangeEvent(CellVisualChangeEvent event) {
		this.layer = event.layer;
		this.columnPosition = event.columnPosition;
		this.rowPosition = event.rowPosition;
	}
	
	public ILayer getLayer() {
		return layer;
	}
	
	public int getColumnPosition() {
		return columnPosition;
	}
	
	public int getRowPosition() {
		return rowPosition;
	}
	
	public boolean convertToLocal(ILayer localLayer) {
		columnPosition = localLayer.underlyingToLocalColumnPosition(getLayer(), columnPosition);
		rowPosition = localLayer.underlyingToLocalRowPosition(getLayer(), rowPosition);
		
		layer = localLayer;
		
		return columnPosition >= 0 && rowPosition >= 0
			&& columnPosition < layer.getColumnCount() && rowPosition < layer.getRowCount();
	}
	
	public Collection<Rectangle> getChangedPositionRectangles() {
		return Arrays.asList(new Rectangle[] { new Rectangle(columnPosition, rowPosition, 1, 1) });
	}
	
	public CellVisualChangeEvent cloneEvent() {
		return new CellVisualChangeEvent(this);
	}

}
