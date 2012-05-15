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
package org.eclipse.nebula.widgets.nattable.viewport.event;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;


public class ViewportEventHandler implements ILayerEventHandler<IStructuralChangeEvent> {

	private final ViewportLayer viewportLayer;

	public ViewportEventHandler(ViewportLayer viewportLayer) {
		this.viewportLayer = viewportLayer;
	}
	
	public Class<IStructuralChangeEvent> getLayerEventClass() {
		return IStructuralChangeEvent.class;
	}

	public void handleLayerEvent(IStructuralChangeEvent event) {
		if (event.isHorizontalStructureChanged()) {
			viewportLayer.invalidateHorizontalStructure();
		}
		if (event.isVerticalStructureChanged()) {
			viewportLayer.invalidateVerticalStructure();
		}
		
		Collection<StructuralDiff> columnDiffs = event.getColumnDiffs();
		if (columnDiffs != null) {
			int columnOffset = 0;
			
			int minimumOriginColumnPosition = viewportLayer.getMinimumOriginColumnPosition();
			for (StructuralDiff columnDiff : columnDiffs) {
				switch (columnDiff.getDiffType()) {
				case ADD:
					Range afterPositionRange = columnDiff.getAfterPositionRange();
					if (afterPositionRange.start < minimumOriginColumnPosition) {
						columnOffset += afterPositionRange.size();
					}
					break;
				case DELETE:
					Range beforePositionRange = columnDiff.getBeforePositionRange();
					if (beforePositionRange.start < minimumOriginColumnPosition) {
						columnOffset -= Math.min(beforePositionRange.end, minimumOriginColumnPosition + 1) - beforePositionRange.start;
					}
					break;
				}
			}
			
			viewportLayer.setMinimumOriginColumnPosition(minimumOriginColumnPosition + columnOffset);
		}
		
		Collection<StructuralDiff> rowDiffs = event.getRowDiffs();
		if (rowDiffs != null) {
			int rowOffset = 0;
			
			int minimumOriginRowPosition = viewportLayer.getMinimumOriginRowPosition();
			for (StructuralDiff rowDiff : rowDiffs) {
				switch (rowDiff.getDiffType()) {
				case ADD:
					Range afterPositionRange = rowDiff.getAfterPositionRange();
					if (afterPositionRange.start < minimumOriginRowPosition) {
						rowOffset += afterPositionRange.size();
					}
					break;
				case DELETE:
					Range beforePositionRange = rowDiff.getBeforePositionRange();
					if (beforePositionRange.start < minimumOriginRowPosition) {
						rowOffset -= Math.min(beforePositionRange.end, minimumOriginRowPosition + 1) - beforePositionRange.start;
					}
					break;
				}
			}
			
			viewportLayer.setMinimumOriginRowPosition(minimumOriginRowPosition + rowOffset);
		}
	}

}
