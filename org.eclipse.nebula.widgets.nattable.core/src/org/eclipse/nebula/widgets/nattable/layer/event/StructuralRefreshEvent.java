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

/**
 * General event indicating that structures cached by the layers need refreshing. <br/>
 * TIP: Consider throwing a more focused event (subclass) if you need to do this.
 */
public class StructuralRefreshEvent implements IStructuralChangeEvent {

	private ILayer layer;

	public StructuralRefreshEvent(ILayer layer) {
		this.layer = layer;
	}
	
	protected StructuralRefreshEvent(StructuralRefreshEvent event) {
		this.layer = event.layer;
	}
	
	public ILayer getLayer() {
		return layer;
	}

	public boolean convertToLocal(ILayer localLayer) {
		layer = localLayer;
		
		return true;
	}
	
	public Collection<Rectangle> getChangedPositionRectangles() {
		return Arrays.asList(new Rectangle[] { new Rectangle(0, 0, layer.getColumnCount(), layer.getRowCount()) });
	}
	
	public boolean isHorizontalStructureChanged() {
		return true;
	}
	
	public boolean isVerticalStructureChanged() {
		return true;
	}

	public Collection<StructuralDiff> getColumnDiffs() {
		return null;
	}

	public Collection<StructuralDiff> getRowDiffs() {
		return null;
	}

	public ILayerEvent cloneEvent() {
		return this;
	}

}
