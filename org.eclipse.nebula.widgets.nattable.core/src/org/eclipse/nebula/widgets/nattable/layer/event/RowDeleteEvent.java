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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff.DiffTypeEnum;


public class RowDeleteEvent extends RowStructuralChangeEvent {
	
	public RowDeleteEvent(ILayer layer, int rowPosition) {
		// Start range from position before the deleted row position so that
		// if the last row in a layer is deleted the delete event will still propagate
//		this(layer, new Range(rowPosition - 1, rowPosition + 1));
		this(layer, new Range(rowPosition, rowPosition + 1));
	}
	
	public RowDeleteEvent(ILayer layer, Range rowPositionRange) {
		super(layer, rowPositionRange);
	}
	
	public RowDeleteEvent(ILayer layer, Range...rowPositionRanges) {
		super(layer, Arrays.asList(rowPositionRanges));
	}
	
	public RowDeleteEvent(ILayer layer, Collection<Range> rowPositionRanges) {
		super(layer, rowPositionRanges);
	}
	
	protected RowDeleteEvent(RowDeleteEvent event) {
		super(event);
	}
	
	@Override
	public RowDeleteEvent cloneEvent() {
		return new RowDeleteEvent(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.layer.event.RowVisualChangeEvent#convertToLocal(org.eclipse.nebula.widgets.nattable.layer.ILayer)
	 */
//	@Override
//	public boolean convertToLocal(ILayer localLayer) {
//		// TODO Auto-generated method stub
//		return true;
//	}
	
	public Collection<Integer> getDeletedRowIndexes() {
		Set<Integer> rowIndexes = new HashSet<Integer>();
		for (Range range : getRowPositionRanges()) {
			for (int i = range.start; i < range.end; i++) {
				rowIndexes.add(getLayer().getRowIndexByPosition(i));
			}
		}
		return getDeletedRowIndexes();
	}
	
	@Override
	public Collection<StructuralDiff> getRowDiffs() {
		Collection<StructuralDiff> rowDiffs = new ArrayList<StructuralDiff>();
		
		for (Range range : getRowPositionRanges()) {
			rowDiffs.add(new StructuralDiff(DiffTypeEnum.DELETE, range, new Range(range.start, range.start)));
		}
		
		return rowDiffs;
	}
	
}
