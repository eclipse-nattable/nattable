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
package org.eclipse.nebula.widgets.nattable.edit.event;

import org.eclipse.nebula.widgets.nattable.edit.InlineCellEditController;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEventHandler;

public class InlineCellEditEventHandler implements ILayerEventHandler<InlineCellEditEvent> {
	
	private final ILayer layer;

	public InlineCellEditEventHandler(ILayer layer) {
		this.layer = layer;
	}

	public Class<InlineCellEditEvent> getLayerEventClass() {
		return InlineCellEditEvent.class;
	}

	public void handleLayerEvent(InlineCellEditEvent event) {
		if (event.convertToLocal(layer)) {
			ILayerCell cell = layer.getCellByPosition(event.getColumnPosition(), event.getRowPosition());
			InlineCellEditController.editCellInline(cell, event.getInitialValue(), event.getParent(), event.getConfigRegistry());
		}
	}
}
