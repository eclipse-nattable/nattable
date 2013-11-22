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
package org.eclipse.nebula.widgets.nattable.grid.layer.event;

import java.util.Collection;

import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.ColumnVisualChangeEvent;


public class ColumnHeaderSelectionEvent extends ColumnVisualChangeEvent {
	
	
	public ColumnHeaderSelectionEvent(ILayer layer, Collection<Range> columnPositionRanges) {
		super(layer, columnPositionRanges);
	}

	protected ColumnHeaderSelectionEvent(ColumnHeaderSelectionEvent event) {
		super(event);
	}
	
	@Override
	public ColumnHeaderSelectionEvent cloneEvent() {
		return new ColumnHeaderSelectionEvent(this);
	}

}
