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
package org.eclipse.nebula.widgets.nattable.viewport.event;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralDiff;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;


public class ViewportEventHandler implements ILayerEventHandler<IStructuralChangeEvent> {

	private final ViewportLayer viewportLayer;

	public ViewportEventHandler(ViewportLayer viewportLayer) {
		this.viewportLayer = viewportLayer;
	}
	
	@Override
	public Class<IStructuralChangeEvent> getLayerEventClass() {
		return IStructuralChangeEvent.class;
	}

	@Override
	public void handleLayerEvent(IStructuralChangeEvent event) {
		IUniqueIndexLayer scrollableLayer = viewportLayer.getScrollableLayer();
		
		if (event.isHorizontalStructureChanged()) {
			viewportLayer.invalidateHorizontalStructure();
			
			int columnOffset = 0;
			int minimumOriginColumnPosition = viewportLayer.getMinimumOriginColumnPosition();
			
			Collection<StructuralDiff> columnDiffs = event.getColumnDiffs();
			if (columnDiffs != null) {
				if (minimumOriginColumnPosition < 0) minimumOriginColumnPosition = scrollableLayer.getColumnCount();
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
			}
			
			viewportLayer.setMinimumOriginX(scrollableLayer.getStartXOfColumnPosition(minimumOriginColumnPosition + columnOffset));
		}
		
		if (event.isVerticalStructureChanged()) {
			viewportLayer.invalidateVerticalStructure();
			
			int rowOffset = 0;
			int minimumOriginRowPosition = viewportLayer.getMinimumOriginRowPosition();
			
			Collection<StructuralDiff> rowDiffs = event.getRowDiffs();
			if (rowDiffs != null) {
				if (minimumOriginRowPosition < 0) minimumOriginRowPosition = scrollableLayer.getRowCount();
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
			}
			
			viewportLayer.setMinimumOriginY(scrollableLayer.getStartYOfRowPosition(minimumOriginRowPosition + rowOffset));
		}
	}

}
