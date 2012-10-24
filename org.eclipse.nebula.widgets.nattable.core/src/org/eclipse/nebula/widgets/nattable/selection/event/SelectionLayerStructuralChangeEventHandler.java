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
import java.util.Set;


import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;
import org.eclipse.nebula.widgets.nattable.layer.event.IStructuralChangeEvent;
import org.eclipse.nebula.widgets.nattable.selection.ISelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.swt.graphics.Rectangle;

public class SelectionLayerStructuralChangeEventHandler implements ILayerEventHandler<IStructuralChangeEvent> {

	private ISelectionModel selectionModel;
	private final SelectionLayer selectionLayer;
	
	public SelectionLayerStructuralChangeEventHandler(SelectionLayer selectionLayer, ISelectionModel selectionModel) {
		this.selectionLayer = selectionLayer;
		this.selectionModel = selectionModel;
	}

	public Class<IStructuralChangeEvent> getLayerEventClass() {
		return IStructuralChangeEvent.class;
	}

	public void handleLayerEvent(IStructuralChangeEvent event) {
		if (event.isHorizontalStructureChanged()) {
			// TODO handle column deletion
		}
		
		if (event.isVerticalStructureChanged()) {
			Collection<Rectangle> rectangles = event.getChangedPositionRectangles();
			for (Rectangle rectangle : rectangles) {
				Range changedRange = new Range(rectangle.y, rectangle.y + rectangle.height);
				if (selectedRowModified(changedRange)) {
					selectionLayer.clear();
					break;
				}
			}
		}
	}
	
	private boolean selectedRowModified(Range changedRange){
		Set<Range> selectedRows = selectionModel.getSelectedRowPositions();
		for (Range rowRange : selectedRows) {
			if (rowRange.overlap(changedRange)){
				return true;
			}
		}
		return false;
	}

	public void setSelectionModel(ISelectionModel selectionModel) {
		this.selectionModel = selectionModel;
	}

}
