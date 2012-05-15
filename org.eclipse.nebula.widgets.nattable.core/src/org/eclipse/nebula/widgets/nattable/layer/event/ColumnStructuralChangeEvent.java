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

/**
 * Event indicating a change in the structure of the columns. <br/>
 * This event carried ColumnDiffs (Collection<StructuralDiff>) indicating the columns which have changed.<br/> 
 */
public abstract class ColumnStructuralChangeEvent extends ColumnVisualChangeEvent implements IStructuralChangeEvent {

	public ColumnStructuralChangeEvent(ILayer layer, Range...columnPositionRanges) {
		this(layer, Arrays.asList(columnPositionRanges));
	}
	
	public ColumnStructuralChangeEvent(ILayer layer, Collection<Range> columnPositionRanges) {
		super(layer, columnPositionRanges);
	}
	
	protected ColumnStructuralChangeEvent(ColumnStructuralChangeEvent event) {
		super(event);
	}
	
	@Override
	public Collection<Rectangle> getChangedPositionRectangles() {
		Collection<Rectangle> changedPositionRectangles = new ArrayList<Rectangle>();
		
		Collection<Range> columnPositionRanges = getColumnPositionRanges();
		if (columnPositionRanges != null && columnPositionRanges.size() > 0) {
			int leftmostColumnPosition = Integer.MAX_VALUE;
			for (Range range : columnPositionRanges) {
				if (range.start < leftmostColumnPosition) {
					leftmostColumnPosition = range.start;
				}
			}
			
			int columnCount = getLayer().getColumnCount();
			int rowCount = getLayer().getRowCount();
			changedPositionRectangles.add(new Rectangle(leftmostColumnPosition, 0, columnCount - leftmostColumnPosition, rowCount));
		}
		
		return changedPositionRectangles;
	}
	
	public boolean isHorizontalStructureChanged() {
		return true;
	}
	
	public boolean isVerticalStructureChanged() {
		return false;
	}
	
	public Collection<StructuralDiff> getRowDiffs() {
		return null;
	}
	
}
