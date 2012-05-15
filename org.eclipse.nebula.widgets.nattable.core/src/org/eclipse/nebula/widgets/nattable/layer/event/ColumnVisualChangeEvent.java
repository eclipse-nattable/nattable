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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.swt.graphics.Rectangle;

public abstract class ColumnVisualChangeEvent implements IVisualChangeEvent {

	private ILayer layer;
	
	private Collection<Range> columnPositionRanges;
	
	public ColumnVisualChangeEvent(ILayer layer, Range...columnPositionRanges) {
		this(layer, Arrays.asList(columnPositionRanges));
	}
	
	public ColumnVisualChangeEvent(ILayer layer, Collection<Range> columnPositionRanges) {
		this.layer = layer;
		this.columnPositionRanges = columnPositionRanges;
	}
	
	// Copy constructor
	protected ColumnVisualChangeEvent(ColumnVisualChangeEvent event) {
		this.layer = event.layer;
		this.columnPositionRanges = event.columnPositionRanges;
	}
	
	public ILayer getLayer() {
		return layer;
	}
	
	public Collection<Range> getColumnPositionRanges() {
		return columnPositionRanges;
	}
	
	protected void setColumnPositionRanges(Collection<Range> columnPositionRanges) {
		this.columnPositionRanges = columnPositionRanges;
	}
	
	public boolean convertToLocal(ILayer localLayer) {
		columnPositionRanges = localLayer.underlyingToLocalColumnPositions(layer, columnPositionRanges);
		layer = localLayer;
		
		return columnPositionRanges != null && columnPositionRanges.size() > 0;
	}
	
	public Collection<Rectangle> getChangedPositionRectangles() {
		Collection<Rectangle> changedPositionRectangles = new ArrayList<Rectangle>();
		
		int rowCount = layer.getRowCount();
		for (Range range : columnPositionRanges) {
			changedPositionRectangles.add(new Rectangle(range.start, 0, range.end - range.start, rowCount));
		}
		
		return changedPositionRectangles;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
	
}
